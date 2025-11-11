package com.zynt.sumviltadconnect.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageProcessor {
    
    /**
     * Process image for ML model:
     * - Correct orientation based on EXIF data
     * - Crop to center square (simulating camera guide box)
     * - Resize to optimal size for ML processing
     * - Compress for efficient upload
     */
    fun processImageForML(
        context: Context,
        imageUri: Uri,
        targetSize: Int = 224 // Match your ML model's input size
    ): Uri? {
        try {
            // Load bitmap from URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap == null) {
                return null
            }
            
            // Correct orientation based on EXIF data
            bitmap = correctBitmapOrientation(context, imageUri, bitmap)
            
            Log.d("ImageProcessor", "Original image size: ${bitmap.width}x${bitmap.height}")
            
            // First crop to center square (70% for tighter crop, less background)
            // Camera guide shows 80%, but we crop to 70% to ensure only content inside frame
            bitmap = cropToCenterSquare(bitmap, 0.70f)
            
            Log.d("ImageProcessor", "After crop size: ${bitmap.width}x${bitmap.height}")
            
            // Then resize to target size for ML model
            bitmap = resizeBitmap(bitmap, targetSize)
            
            Log.d("ImageProcessor", "Final ML size: ${bitmap.width}x${bitmap.height}")
            
            // Save processed image to cache
            val processedFile = File(context.cacheDir, "processed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(processedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            // Clean up
            bitmap.recycle()
            
            return Uri.fromFile(processedFile)
            
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Crop bitmap to center square matching the camera guide box
     * 
     * The camera preview shows a guide box at 80% of screen's min dimension,
     * but we crop to 70% of the captured image to ensure we only get content
     * that was truly inside the frame (accounting for preview/capture differences)
     * 
     * Example: 
     * - Camera shows: 80% guide box on screen
     * - We capture: Full resolution image
     * - We crop: 70% center square (tighter than guide to avoid edges)
     * 
     * @param bitmap Source bitmap from camera
     * @param squareRatio How much of the minimum dimension to keep (0.7 = 70%)
     */
    private fun cropToCenterSquare(bitmap: Bitmap, squareRatio: Float = 0.70f): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        Log.d("ImageProcessor", "cropToCenterSquare - Input: ${width}x${height}")
        
        // Calculate the guide box size exactly as shown in camera preview
        // In CameraScreen, it's: size.minDimension * 0.8f
        val minDimension = minOf(width, height)
        val squareSize = (minDimension * squareRatio).toInt()
        
        Log.d("ImageProcessor", "Square size calculated: $squareSize (${squareRatio * 100}% of $minDimension)")
        
        // Calculate crop position to center the square
        val xOffset = (width - squareSize) / 2
        val yOffset = (height - squareSize) / 2
        
        Log.d("ImageProcessor", "Crop position: x=$xOffset, y=$yOffset, size=$squareSize")
        
        // Create the cropped bitmap - this will be ONLY what's inside the guide box
        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            xOffset.coerceAtLeast(0),
            yOffset.coerceAtLeast(0),
            squareSize.coerceAtMost(width),
            squareSize.coerceAtMost(height)
        )
        
        Log.d("ImageProcessor", "Cropped result: ${croppedBitmap.width}x${croppedBitmap.height}")
        
        // Recycle original bitmap to free memory if it's different
        if (croppedBitmap != bitmap) {
            bitmap.recycle()
        }
        
        return croppedBitmap
    }
    
    /**
     * Resize bitmap to target size maintaining aspect ratio
     */
    private fun resizeBitmap(bitmap: Bitmap, targetSize: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
    }
    
    /**
     * Correct image orientation based on EXIF data
     * This is important for photos taken with different device orientations
     */
    private fun correctBitmapOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = inputStream?.let { ExifInterface(it) }
            inputStream?.close()
            
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            }
            
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            
        } catch (e: Exception) {
            e.printStackTrace()
            return bitmap
        }
    }
    
    /**
     * Get file size in MB
     */
    fun getFileSizeInMB(context: Context, uri: Uri): Double {
        val inputStream = context.contentResolver.openInputStream(uri)
        val size = inputStream?.available()?.toDouble() ?: 0.0
        inputStream?.close()
        return size / (1024 * 1024)
    }
}
