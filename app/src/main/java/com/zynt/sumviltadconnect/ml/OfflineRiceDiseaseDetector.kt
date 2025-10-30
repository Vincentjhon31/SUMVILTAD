package com.zynt.sumviltadconnect.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import java.util.Locale
import kotlin.math.roundToInt

class OfflineRiceDiseaseDetector(private val context: Context) {

    private var diseaseModel: Module? = null
    private var validatorModel: Module? = null

    // Dynamically loaded labels (fallback to defaults)
    private var diseaseClasses: List<String> = listOf(
        "Bacterialblight",
        "Blast",
        "Brownspot",
        "Tungro"
    )

    companion object {
        private const val TAG = "OfflineDetector"
        private const val IMAGE_SIZE = 224
        private const val VALIDATOR_POSITIVE_INDEX = 0 // adjusted: index 0 is 'is_rice' per logs
        private const val DISEASE_CONFIDENCE_OVERRIDE = 0.60f

        // Kept for backward compatibility but not used for predictions mapping anymore
        @Suppress("unused")
        private val DISEASE_CLASSES = arrayOf(
            "Bacterialblight",
            "Blast",
            "Brownspot",
            "Tungro"
        )

        private val NORM_MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
        private val NORM_STD = floatArrayOf(0.229f, 0.224f, 0.225f)
    }

    data class DiseaseDetectionResult(
        val isRiceLeaf: Boolean,
        val disease: String?,
        val confidence: String?,
        val recommendation: String?,
        val details: String?,
        val predictions: List<DiseasePrediction>
    )

    data class DiseasePrediction(
        val label: String,
        val confidence: Float
    )

    private val diseaseRecommendations = mapOf(
        "Bacterialblight" to mapOf(
            "recommendation" to "Use resistant varieties like PSB Rc18 (ALA), NSIC Rc354, or NSIC Rc302. Apply balanced nutrients, especially nitrogen (avoid excess). Maintain good drainage and field sanitation: weed removal, plow under stubbles and straw. Destroy ratoons and volunteer seedlings. Allow fallow fields to dry to suppress pathogens. Consult your local DA technician for specific advice.",
            "details" to "Bacterial Leaf Blight (BLB) is caused by Xanthomonas oryzae pv. oryzae. Symptoms include water-soaked stripes that expand to large grayish-white lesions with wavy light brown margins. In susceptible varieties, yield loss can reach 70%. At booting stage, causes poor quality grains and high broken grain percentage. Spread by ooze droplets, strong winds, heavy rains, contaminated stubbles, and infected weeds in tropical and temperate lowlands (25-34°C, >70% relative humidity)."
        ),
        "Blast" to mapOf(
            "recommendation" to "Use resistant varieties such as PSB Rc18 or IRRI-derived lines. Avoid high nitrogen application and split nitrogen fertilizer application. Maintain proper irrigation (avoid water stress) and field sanitation (remove stubbles, debris). Adjust planting calendar to avoid peak infection period. Apply calcium silicate to strengthen cell walls. Apply fungicides: triazoles and strobilurins at first signs, especially during tillering/booting. Burn or plow under infected straw. Consult local DA.",
            "details" to "Rice Blast is caused by Magnaporthe oryzae fungus. Symptoms include leaf blast (small, spindle-shaped spots with brown border and gray center), node blast (nodes become black and break easily), and panicle blast (base becomes black, grains remain unfilled). Occurs from seedling to reproductive stage under cool temperature, high relative humidity, continuous rain, and large day-night temperature differences. Can reduce yield significantly by reducing leaf area for grain filling."
        ),
        "Brownspot" to mapOf(
            "recommendation" to "Plant resistant varieties and ensure balanced fertilization, especially sufficient potassium (use 'complete' or potash fertilizers). Conduct soil test and correct deficiencies. Improve soil health and maintain proper field drainage. Use clean, healthy seeds and practice field sanitation (remove stubbles, weeds). Split nitrogen fertilizer application. Apply calcium silicate & potassium fertilizers. If severe, apply fungicides (triazoles, strobilurins) following BPI registration and safety rules. Consult local DA.",
            "details" to "Brown Spot is caused by Cochliobolus miyabeanus. Symptoms include small, circular to oval spots with gray centers on leaves, black spots on glumes with dark brown velvety fungal spores, and discolored, shriveled grains. Historically caused the 1943 Great Bengal Famine. Yield loss ranges 5-45% and causes seedling blight mortality of 10-58%. Fungus survives 2-4 years in infected tissues under high humidity (86-100%), 16-36°C, in nutrient-deficient or toxic soils."
        ),
        "Tungro" to mapOf(
            "recommendation" to "Plant tungro- or GLH-resistant varieties like IR64 if available. Practice synchronous planting ('sabay-sabay') with neighbors and maintain a fallow period (at least 1 month) between crops to break vector cycles. Remove ('rogue') and destroy infected plants immediately. Plow infected stubbles after harvest to destroy inoculum and GLH breeding sites. Maintain balanced nutrient management. Note: Chemical control of GLH is not effective as they move quickly and spread tungro even with short feeding. Consult local DA.",
            "details" to "Rice Tungro Disease is caused by a combination of two viruses (Rice Tungro Bacilliform Virus + Rice Tungro Spherical Virus) transmitted by green leafhoppers (GLH). Symptoms include young leaves becoming mottled, older leaves turning yellow to orange, stunted growth, reduced tillers, delayed flowering, small panicles, and high sterility. Highly destructive in South & Southeast Asia with yield loss up to 100% in susceptible varieties infected early. Most damaging during vegetative/tillering stage."
        ),
        // Added entries to handle models with Healthy/Other classes
        "Healthy" to mapOf(
            "recommendation" to "Leaf appears healthy. Keep good field sanitation, balanced fertilization, and regular monitoring.",
            "details" to "The AI model predicts this rice leaf is healthy. No disease symptoms detected with the current image."
        ),
        "Other" to mapOf(
            "recommendation" to "The image doesn't match known rice diseases. Ensure clear leaf photos against a simple background and good lighting.",
            "details" to "The model classified the image as 'Other/Unknown' rather than a known disease. Try another angle or closer shot if needed."
        )
    )

    init {
        loadModels()
        loadLabels()
    }

    private fun loadLabels() {
        try {
            context.assets.open("labels.txt").bufferedReader().use { br ->
                val lines = br.readLines().map { it.trim() }.filter { it.isNotEmpty() }
                if (lines.isNotEmpty()) {
                    diseaseClasses = lines
                    Log.d(TAG, "Loaded ${lines.size} class labels from assets/labels.txt: ${lines}")
                } else {
                    Log.w(TAG, "labels.txt is empty; using default labels: $diseaseClasses")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "labels.txt not found; using default labels: $diseaseClasses", e)
        }
    }

    private fun loadModels() {
        try {
            Log.d(TAG, "Attempting to load PyTorch models...")
            val diseasePath = assetFilePath("rice_disease_model_traced.pt")
            val validatorPath = assetFilePath("rice_leaf_validator_traced.pt")
            diseaseModel = Module.load(diseasePath)
            Log.d(TAG, "✅ Disease model loaded successfully")
            validatorModel = Module.load(validatorPath)
            Log.d(TAG, "✅ Validator model loaded successfully")
        } catch (e: Throwable) {
            Log.e(TAG, "❌ Failed to load models: ${e.message}", e)
            diseaseModel = null
            validatorModel = null
        }
    }

    private fun assetFilePath(assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) return file.absolutePath
        try {
            context.assets.open(assetName).use { input ->
                FileOutputStream(file).use { output ->
                    val buf = ByteArray(8 * 1024)
                    while (true) {
                        val r = input.read(buf)
                        if (r <= 0) break
                        output.write(buf, 0, r)
                    }
                    output.flush()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error copying asset: $assetName", e)
        }
        return file.absolutePath
    }

    fun detectDisease(bitmap: Bitmap): DiseaseDetectionResult {
        if (diseaseModel == null || validatorModel == null) {
            return DiseaseDetectionResult(
                isRiceLeaf = false,
                disease = null,
                confidence = null,
                recommendation = "Offline AI models not available. Please use online mode.",
                details = "Models failed to load.",
                predictions = emptyList()
            )
        }
        return try {
            val isRiceLeaf = validateRiceLeaf(bitmap)
            if (!isRiceLeaf) {
                // Safety fallback: try disease model; if highly confident, accept as rice
                return try {
                    val attempt = detectDiseaseInternal(bitmap)
                    val topConf = attempt.predictions.firstOrNull()?.confidence ?: 0f
                    if (topConf >= DISEASE_CONFIDENCE_OVERRIDE) {
                        Log.w(TAG, "Validator rejected, but disease model confident (top=$topConf). Overriding to accept.")
                        attempt.copy(isRiceLeaf = true)
                    } else {
                        DiseaseDetectionResult(
                            isRiceLeaf = false,
                            disease = null,
                            confidence = null,
                            recommendation = "Please capture an image of a rice leaf",
                            details = "The image does not appear to contain a rice leaf",
                            predictions = emptyList()
                        )
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Fallback detection failed: ${t.message}", t)
                    DiseaseDetectionResult(
                        isRiceLeaf = false,
                        disease = null,
                        confidence = null,
                        recommendation = "Please capture an image of a rice leaf",
                        details = "The image does not appear to contain a rice leaf",
                        predictions = emptyList()
                    )
                }
            }
            detectDiseaseInternal(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Detection failed: ${e.message}", e)
            DiseaseDetectionResult(
                isRiceLeaf = false,
                disease = null,
                confidence = null,
                recommendation = "Detection failed: ${e.message}",
                details = "An error occurred during disease detection",
                predictions = emptyList()
            )
        }
    }

    private fun validateRiceLeaf(bitmap: Bitmap): Boolean {
        return try {
            val inputTensor = preprocessImage(bitmap)
            val outputs = validatorModel!!.forward(IValue.from(inputTensor)).toTensor()
            val raw = outputs.dataAsFloatArray
            Log.d(TAG, "Validator raw scores size=${raw.size}, sample=${raw.joinToString(limit=4)}")

            when (raw.size) {
                1 -> {
                    val prob = sigmoid(raw[0])
                    Log.d(TAG, "Validator (1-logit sigmoid) prob_is_rice=$prob")
                    prob >= 0.5f
                }
                2 -> {
                    val probs = applySoftmax(raw)
                    val argmax = if (probs[0] >= probs[1]) 0 else 1
                    Log.d(TAG, "Validator (2-class softmax) probs=${probs.toList()} argmax=$argmax")
                    argmax == VALIDATOR_POSITIVE_INDEX
                }
                else -> {
                    Log.w(TAG, "Validator returned unexpected size=${raw.size}; bypassing validation")
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Rice leaf validation failed: ${e.message}", e)
            true
        }
    }

    private fun sigmoid(x: Float): Float {
        val ex = kotlin.math.exp(x.toDouble()).toFloat()
        return ex / (1f + ex)
    }

    private fun detectDiseaseInternal(bitmap: Bitmap): DiseaseDetectionResult {
        // Baseline pass (ImageNet normalization)
        val baseline = runDiseaseModel(bitmap, NORM_MEAN, NORM_STD)

        val baselineTop = baseline.first
        val baselinePreds = baseline.second
        var bestTop = baselineTop
        var bestPreds = baselinePreds

        // Try alternative normalization: no mean/std
        if (bestTop.confidence < 0.5f) {
            val alt = runDiseaseModel(bitmap, floatArrayOf(0f, 0f, 0f), floatArrayOf(1f, 1f, 1f))
            val altTop = alt.first
            if (altTop.confidence > bestTop.confidence + 0.05f) {
                bestTop = altTop
                bestPreds = alt.second
                Log.d(TAG, "Using alternative normalization (none);")
            }
        }

        // Try second alternative normalization: mean=0.5, std=0.5 (common torchvision variant)
        if (bestTop.confidence < 0.5f) {
            val alt2 = runDiseaseModel(bitmap, floatArrayOf(0.5f, 0.5f, 0.5f), floatArrayOf(0.5f, 0.5f, 0.5f))
            val alt2Top = alt2.first
            if (alt2Top.confidence > bestTop.confidence + 0.05f) {
                bestTop = alt2Top
                bestPreds = alt2.second
                Log.d(TAG, "Using alternative normalization (mean=0.5,std=0.5);")
            }
        }

        val confPct = String.format(Locale.getDefault(), "%.1f%%", bestTop.confidence * 100)
        val rec = diseaseRecommendations[bestTop.label]
        return DiseaseDetectionResult(
            isRiceLeaf = true,
            disease = bestTop.label,
            confidence = confPct,
            recommendation = rec?.get("recommendation") ?: "No specific recommendation available",
            details = rec?.get("details") ?: "Disease detected with $confPct confidence.",
            predictions = bestPreds
        )
    }

    private fun runDiseaseModel(bitmap: Bitmap, mean: FloatArray, std: FloatArray): Pair<DiseasePrediction, List<DiseasePrediction>> {
        val softwareBitmap = ensureSoftwareBitmap(bitmap)
        val cropped = resizeWithCenterCrop(softwareBitmap, IMAGE_SIZE)
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(cropped, mean, std)
        val outputs = diseaseModel!!.forward(IValue.from(inputTensor)).toTensor()
        val scores = outputs.dataAsFloatArray
        val probs = applySoftmax(scores)
        val outLen = probs.size
        Log.d(TAG, "Disease logits=${scores.joinToString(limit=5)}, probs=${probs.joinToString(limit=5)}, outLen=$outLen labels=${diseaseClasses.size}")

        // If labels exist, prefer mapping strictly to their count to avoid placeholder classes
        if (diseaseClasses.isNotEmpty()) {
            val usedLen = minOf(outLen, diseaseClasses.size)
            val probsUsed = probs.copyOfRange(0, usedLen)
            val sum = probsUsed.sum()
            val probsNorm = if (sum > 0f) probsUsed.map { it / sum }.toFloatArray() else FloatArray(usedLen) { 1f / usedLen }

            if (diseaseClasses.size != outLen) {
                Log.w(TAG, "Model output count ($outLen) != labels count (${diseaseClasses.size}). Using first $usedLen entries and renormalizing. Update assets/labels.txt or retrain to match.")
            }

            val labelsUsed = diseaseClasses.take(usedLen)
            val predictions = labelsUsed.mapIndexed { idx, name ->
                DiseasePrediction(name, probsNorm[idx])
            }.sortedByDescending { it.confidence }

            val top = predictions.first()
            val topIdx = labelsUsed.indexOf(top.label)
            Log.d(TAG, "Top prediction (mapped): idx=$topIdx label='${top.label}' conf=${top.confidence}")
            return top to predictions
        }

        // Fallback: no labels available; generate generic names sized to the model output
        Log.w(TAG, "No labels available; generating generic class names [Class 0..${outLen - 1}]")
        val lbls = (0 until outLen).map { "Class $it" }
        val predictions = lbls.mapIndexed { idx, name ->
            DiseasePrediction(name, probs[idx])
        }.sortedByDescending { it.confidence }
        val top = predictions.first()
        val topIdx = lbls.indexOf(top.label)
        Log.d(TAG, "Top prediction (generic): idx=$topIdx label='${top.label}' conf=${top.confidence}")
        return top to predictions
    }

    private fun applySoftmax(scores: FloatArray): FloatArray {
        val max = scores.maxOrNull() ?: 0f
        val exps = scores.map { kotlin.math.exp((it - max).toDouble()).toFloat() }
        val sum = exps.sum()
        return exps.map { it / sum }.toFloatArray()
    }

    private fun preprocessImage(bitmap: Bitmap): Tensor {
        val softwareBitmap = ensureSoftwareBitmap(bitmap)
        val cropped = resizeWithCenterCrop(softwareBitmap, IMAGE_SIZE)
        return TensorImageUtils.bitmapToFloat32Tensor(cropped, NORM_MEAN, NORM_STD)
    }

    private fun resizeWithCenterCrop(src: Bitmap, target: Int): Bitmap {
        val w = src.width
        val h = src.height
        if (w == target && h == target) return src
        val scale = target / minOf(w, h).toFloat()
        val newW = (w * scale).roundToInt().coerceAtLeast(target)
        val newH = (h * scale).roundToInt().coerceAtLeast(target)
        val scaled = Bitmap.createScaledBitmap(src, newW, newH, true)
        val x = ((newW - target) / 2f).roundToInt().coerceAtLeast(0)
        val y = ((newH - target) / 2f).roundToInt().coerceAtLeast(0)
        return try {
            Bitmap.createBitmap(scaled, x, y, target, target)
        } catch (_: Throwable) {
            scaled // fallback if crop fails for any reason
        }
    }

    private fun ensureSoftwareBitmap(src: Bitmap): Bitmap {
        return try {
            // If already software ARGB_8888, return as-is
            if (src.config == Bitmap.Config.ARGB_8888 && (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || src.config != Bitmap.Config.HARDWARE)) {
                return src
            }

            // If HARDWARE, first try copy (may fail/return null), then try compress/decode fallback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && src.config == Bitmap.Config.HARDWARE) {
                // 1) Try copy
                val copied = try { src.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }
                if (copied != null && copied.config == Bitmap.Config.ARGB_8888) {
                    Log.d(TAG, "Converted HARDWARE bitmap via copy()")
                    return copied
                }
                // 2) Fallback: compress to bytes then decode as software ARGB_8888
                val baos = ByteArrayOutputStream()
                val compressedOk = try { src.compress(Bitmap.CompressFormat.JPEG, 95, baos) } catch (_: Throwable) { false }
                if (compressedOk) {
                    val bytes = baos.toByteArray()
                    val opts = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                        // inDither is deprecated; omit to avoid warnings
                        inScaled = false
                        inMutable = false
                    }
                    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                    if (decoded != null && decoded.config == Bitmap.Config.ARGB_8888) {
                        Log.d(TAG, "Converted HARDWARE bitmap via compress/decode")
                        return decoded
                    }
                }
                // 3) Last resort: draw into ARGB_8888 (may fail on some devices)
                try {
                    val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(out)
                    canvas.drawBitmap(src, 0f, 0f, null)
                    Log.d(TAG, "Converted HARDWARE bitmap via Canvas draw")
                    return out
                } catch (t: Throwable) {
                    Log.w(TAG, "Failed to convert bitmap to software: ${t.message}")
                }
                // Return original as last resort (may still fail downstream)
                return src
            }

            // Non-HARDWARE but non-ARGB_8888: convert
            src.copy(Bitmap.Config.ARGB_8888, false) ?: src

        } catch (t: Throwable) {
            Log.w(TAG, "Failed to convert bitmap to software: ${t.message}")
            src
        }
    }

    fun cleanup() {
        try {
            diseaseModel?.destroy()
            validatorModel?.destroy()
        } catch (_: Throwable) {
        } finally {
            diseaseModel = null
            validatorModel = null
        }
    }
}
