package com.zynt.sumviltadconnect.data.network

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.zynt.sumviltadconnect.data.model.DiseaseDetectionResponse
import com.zynt.sumviltadconnect.data.model.DiseasePrediction
import com.zynt.sumviltadconnect.data.model.ApiDiseaseDetectionResponse
import com.zynt.sumviltadconnect.data.model.ApiErrorResponse
import com.zynt.sumviltadconnect.data.model.CropHealthRecord
import com.zynt.sumviltadconnect.ml.OfflineRiceDiseaseDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * Enhanced Disease Detection Service
 * Intelligently chooses between online API and offline ML model
 * Provides seamless experience regardless of connectivity
 */
class DiseaseDetectionService(private val context: Context) {

    private val offlineDetector = OfflineRiceDiseaseDetector(context)
    private val apiService = ApiClient.apiService

    /**
     * Detect rice disease using online API only
     * @param bitmap The image to analyze
     * @return DiseaseDetectionResponse with results and recommendations
     */
    suspend fun detectDisease(
        bitmap: Bitmap
    ): DiseaseDetectionResponse = withContext(Dispatchers.IO) {

        val startTime = System.currentTimeMillis()

        // Check network connectivity
        if (!isNetworkAvailable()) {
            Log.e("DiseaseDetection", "No internet connection available")
            return@withContext DiseaseDetectionResponse(
                is_rice_leaf = false,
                disease = null,
                confidence = null,
                recommendation = "Please check your internet connection and try again.",
                details = "Internet connection is required for disease detection.",
                predictions = emptyList(),
                inference_time_seconds = 0.0,
                api_status = "no_internet",
                is_offline = false,
                message = "No internet connection",
                success = false
            )
        }

        Log.d("DiseaseDetection", "Using online API detection")

        return@withContext try {
            // Use online API detection
            val onlineResult = performOnlineDetection(bitmap)
            val inferenceTime = (System.currentTimeMillis() - startTime) / 1000.0

            Log.d("DiseaseDetection", "Online detection successful")
            onlineResult.copy(
                inference_time_seconds = inferenceTime,
                api_status = "online",
                is_offline = false
            )
        } catch (e: retrofit2.HttpException) {
            // Handle HTTP error responses
            Log.e("DiseaseDetection", "HTTP error ${e.code()}: ${e.message()}")
            handleHttpError(e)
        } catch (e: Exception) {
            // Handle other errors (network issues, timeouts, etc.)
            Log.e("DiseaseDetection", "API request failed: ${e.message}", e)
            DiseaseDetectionResponse(
                is_rice_leaf = false,
                disease = null,
                confidence = null,
                recommendation = "Please check your internet connection and try again.",
                details = "Failed to connect to the server. ${e.message}",
                predictions = emptyList(),
                inference_time_seconds = (System.currentTimeMillis() - startTime) / 1000.0,
                api_status = "error",
                is_offline = false,
                message = "Connection error: ${e.message}",
                success = false
            )
        }
    }

    private suspend fun performOnlineDetection(bitmap: Bitmap): DiseaseDetectionResponse {
        // Create temporary file for upload
        val tempFile = createTempImageFile(bitmap)

        try {
            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
            val notesBody = "".toRequestBody("text/plain".toMediaTypeOrNull())

            // Call API with crop health upload endpoint
            val response = apiService.uploadCropHealthImage(imagePart, notesBody)

            if (response.isSuccessful) {
                val uploadResponse = response.body()
                if (uploadResponse != null && uploadResponse.record != null) {
                    // Convert CropHealthRecord to DiseaseDetectionResponse
                    return convertCropHealthToDetectionResponse(uploadResponse.record!!)
                } else {
                    throw Exception("Empty response from API")
                }
            } else {
                throw retrofit2.HttpException(response)
            }
        } finally {
            // Clean up temp file
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    private fun convertCropHealthToDetectionResponse(record: CropHealthRecord): DiseaseDetectionResponse {
        return DiseaseDetectionResponse(
            is_rice_leaf = true,
            disease = record.disease,
            confidence = record.confidence,
            recommendation = record.recommendation,
            details = record.details,
            predictions = record.predictions?.split(",") ?: emptyList(),
            inference_time_seconds = record.inference_time_seconds,
            api_status = record.api_status,
            is_offline = record.is_offline ?: false,
            message = "Analysis completed successfully",
            success = true
        )
    }

    private suspend fun performOfflineDetection(
        bitmap: Bitmap,
        startTime: Long,
        apiStatus: String
    ): DiseaseDetectionResponse {
        return try {
            Log.d("DiseaseDetection", "Using offline ML model")

            // Use offline ML model
            val offlineResult = offlineDetector.detectDisease(bitmap)
            val inferenceTime = (System.currentTimeMillis() - startTime) / 1000.0

            // Convert confidence string (e.g., "85.2%") to double
            val confidenceDouble = try {
                offlineResult.confidence?.replace("%", "")?.toDouble()?.div(100.0)
            } catch (e: Exception) {
                null
            }

            // Convert predictions list to string list
            val predictionsStringList = offlineResult.predictions.map { "${it.label}: ${String.format("%.1f%%", it.confidence * 100)}" }

            DiseaseDetectionResponse(
                is_rice_leaf = offlineResult.isRiceLeaf,
                disease = offlineResult.disease,
                confidence = confidenceDouble,
                recommendation = offlineResult.recommendation,
                details = offlineResult.details,
                predictions = predictionsStringList,
                inference_time_seconds = inferenceTime,
                api_status = apiStatus,
                is_offline = true,
                message = "Offline analysis completed",
                success = true
            )
        } catch (e: Exception) {
            Log.e("DiseaseDetection", "Offline detection failed: ${e.message}", e)

            return DiseaseDetectionResponse(
                is_rice_leaf = false,
                disease = null,
                confidence = null,
                recommendation = null,
                details = null,
                predictions = emptyList(),
                inference_time_seconds = (System.currentTimeMillis() - startTime) / 1000.0,
                api_status = "error",
                is_offline = true,
                message = "Detection failed: ${e.message}",
                success = false
            )
        }
    }

    private fun handleHttpError(e: retrofit2.HttpException): DiseaseDetectionResponse {
        return when (e.code()) {
            400 -> {
                // Bad Request - likely "not a rice leaf"
                DiseaseDetectionResponse(
                    is_rice_leaf = false,
                    disease = null,
                    confidence = null,
                    recommendation = "Please upload a clear image of a rice leaf for analysis.",
                    details = "The uploaded image does not appear to be a rice leaf.",
                    predictions = emptyList(),
                    inference_time_seconds = 0.0,
                    api_status = "not_rice_leaf",
                    is_offline = false,
                    message = "Image is not recognized as a rice leaf",
                    success = false
                )
            }
            502, 503, 504 -> {
                // Server error - but could also be "not a rice leaf" misclassified as server error
                // Check if the error body contains rice leaf validation message
                val errorBody = try {
                    e.response()?.errorBody()?.string() ?: ""
                } catch (ex: Exception) {
                    ""
                }
                
                // If error mentions rice leaf, treat it as validation error, not server error
                if (errorBody.contains("rice leaf", ignoreCase = true) || 
                    errorBody.contains("not a rice", ignoreCase = true)) {
                    DiseaseDetectionResponse(
                        is_rice_leaf = false,
                        disease = null,
                        confidence = null,
                        recommendation = "Please upload a clear image of a rice leaf for analysis.",
                        details = "The uploaded image does not appear to be a rice leaf.",
                        predictions = emptyList(),
                        inference_time_seconds = 0.0,
                        api_status = "not_rice_leaf",
                        is_offline = false,
                        message = "Image is not recognized as a rice leaf",
                        success = false
                    )
                } else {
                    // Actual server error
                    DiseaseDetectionResponse(
                        is_rice_leaf = false,
                        disease = null,
                        confidence = null,
                        recommendation = "The server is temporarily unavailable. Please try again later.",
                        details = "The server encountered an error. This may be due to high server load or maintenance.",
                        predictions = emptyList(),
                        inference_time_seconds = 0.0,
                        api_status = "server_error",
                        is_offline = false,
                        message = "Unable to connect to server. Please try again later.",
                        success = false
                    )
                }
            }
            else -> {
                // Other HTTP errors
                DiseaseDetectionResponse(
                    is_rice_leaf = false,
                    disease = null,
                    confidence = null,
                    recommendation = "An error occurred. Please try again.",
                    details = "Server returned error ${e.code()}: ${e.message()}",
                    predictions = emptyList(),
                    inference_time_seconds = 0.0,
                    api_status = "error",
                    is_offline = false,
                    message = "An error occurred. Please try again.",
                    success = false
                )
            }
        }
    }

    private fun createTempImageFile(bitmap: Bitmap): File {
        val filename = "temp_crop_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, filename)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        return file
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Add missing utility methods for MainScreen
    fun hasInternetConnection(): Boolean {
        return isNetworkAvailable()
    }

    fun hasOfflineModel(): Boolean {
        return try {
            offlineDetector != null
        } catch (e: Exception) {
            false
        }
    }

    fun getDetectionInfo(): DetectionCapabilities {
        return DetectionCapabilities(
            hasInternet = hasInternetConnection(),
            hasOfflineModel = hasOfflineModel(),
            canDetectOnline = hasInternetConnection(),
            canDetectOffline = hasOfflineModel()
        )
    }

    // Data class for detection capabilities
    data class DetectionCapabilities(
        val hasInternet: Boolean,
        val hasOfflineModel: Boolean,
        val canDetectOnline: Boolean,
        val canDetectOffline: Boolean
    )
}
