package com.zynt.sumviltadconnect.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.graphics.ImageDecoder
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.model.CropHealthRecord
import com.zynt.sumviltadconnect.data.model.DiseaseDetectionResponse
import com.zynt.sumviltadconnect.data.network.ApiClient
import com.zynt.sumviltadconnect.data.network.DiseaseDetectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class DiseaseDetectionViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _detectionResult = MutableStateFlow<DiseaseDetectionResponse?>(null)
    val detectionResult: StateFlow<DiseaseDetectionResponse?> = _detectionResult

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    private val _historyRecords = MutableStateFlow<List<CropHealthRecord>>(emptyList())
    val historyRecords: StateFlow<List<CropHealthRecord>> = _historyRecords

    fun setSelectedImage(uri: Uri?) {
        _selectedImageUri.value = uri
        _detectionResult.value = null
        _errorMessage.value = null
    }

    fun uploadImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Decode URI to a software (non-HARDWARE) ARGB_8888 Bitmap
                var bitmap = decodeUriToSoftwareBitmap(context, imageUri)

                if (bitmap == null) {
                    _errorMessage.value = "Failed to load image"
                    return@launch
                }

                // CRITICAL FIX: Handle EXIF orientation to rotate image correctly
                val rotation = getExifRotation(context, imageUri)
                if (rotation != 0f) {
                    bitmap = rotateBitmap(bitmap, rotation)
                }

                val diseaseDetectionService = DiseaseDetectionService(context)
                val result = diseaseDetectionService.detectDisease(bitmap)

                _detectionResult.value = result

                // Show error message if detection failed
                if (!result.success) {
                    _errorMessage.value = result.message
                }

            } catch (e: Exception) {
                _errorMessage.value = "Detection failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get the rotation angle from EXIF data
     */
    private fun getExifRotation(context: Context, uri: Uri): Float {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (e: IOException) {
            0f
        }
    }

    /**
     * Rotate bitmap by the specified angle
     */
    private fun rotateBitmap(bitmap: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(angle) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun decodeUriToSoftwareBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = false
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val opts = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                        inScaled = false
                        inMutable = false
                    }
                    BitmapFactory.decodeStream(input, null, opts)
                }
            }
        } catch (t: Throwable) {
            null
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.apiService.getCropHealthReports()
                if (response.isSuccessful && response.body() != null) {
                    _historyRecords.value = response.body()!!.data
                } else {
                    _errorMessage.value = "Failed to load history: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearResult() {
        _detectionResult.value = null
        _selectedImageUri.value = null
    }
}
