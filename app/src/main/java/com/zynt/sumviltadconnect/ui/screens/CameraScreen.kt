package com.zynt.sumviltadconnect.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.zynt.sumviltadconnect.utils.ImageProcessor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    var isCapturing by remember { mutableStateOf(false) }
    var showGrid by remember { mutableStateOf(false) }
    var cameraReady by remember { mutableStateOf(false) }
    
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { ContextCompat.getMainExecutor(context) }
    
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val view = PreviewView(ctx)
                previewView = view
                view
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Rebuild camera when lens facing changes
        LaunchedEffect(lensFacing, flashMode) {
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView?.surfaceProvider)
                }
                
                val imageCaptureBuilder = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setFlashMode(flashMode)
                
                imageCapture = imageCaptureBuilder.build()
                
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
                
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                
                cameraReady = true
            } catch (e: Exception) {
                Log.e("CameraScreen", "Camera binding failed", e)
                cameraReady = false
            }
        }
        
        // Guide Overlay - Square frame for leaf positioning with optional grid
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            val squareSize = size.minDimension * 0.8f
            val left = (size.width - squareSize) / 2f
            val top = (size.height - squareSize) / 2f
            
            // Draw semi-transparent dark overlay outside the box
            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                size = this.size
            )
            
            // Clear the center square (make it transparent)
            drawRect(
                color = Color.Transparent,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )
            
            // Draw guide square border
            drawRect(
                color = Color(0xFF4CAF50),
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                style = Stroke(width = 3f)
            )
            
            // Corner brackets for better visual guidance
            val bracketLength = 50f
            val bracketWidth = 5f
            
            // Top-left bracket
            drawLine(
                color = Color(0xFF4CAF50),
                start = androidx.compose.ui.geometry.Offset(left, top),
                end = androidx.compose.ui.geometry.Offset(left + bracketLength, top),
                strokeWidth = bracketWidth
            )
            drawLine(
                color = Color(0xFF4CAF50),
                start = androidx.compose.ui.geometry.Offset(left, top),
                end = androidx.compose.ui.geometry.Offset(left, top + bracketLength),
                strokeWidth = bracketWidth
            )
            
            // Top-right bracket
            drawLine(
                color = Color(0xFF4CAF50),
                start = androidx.compose.ui.geometry.Offset(left + squareSize, top),
                end = androidx.compose.ui.geometry.Offset(left + squareSize - bracketLength, top),
                strokeWidth = bracketWidth
            )
            drawLine(
                color = Color(0xFF4CAF50),
                start = androidx.compose.ui.geometry.Offset(left + squareSize, top),
                end = androidx.compose.ui.geometry.Offset(left + squareSize, top + bracketLength),
                strokeWidth = bracketWidth
            )
            
            // Bottom-left bracket
            drawLine(
                color = Color(0xFF4CAF50),
                start = androidx.compose.ui.geometry.Offset(left, top + squareSize),
                end = androidx.compose.ui.geometry.Offset(left + bracketLength, top + squareSize),
                strokeWidth = bracketWidth
            )
            drawLine(
                color = Color(0xFF4CAF50),
                start = androidx.compose.ui.geometry.Offset(left, top + squareSize),
                end = androidx.compose.ui.geometry.Offset(left, top + squareSize - bracketLength),
                strokeWidth = bracketWidth
            )
            
            // Bottom-right bracket
            drawLine(
                color = Color(0xFF4CAF50),
                start = androidx.compose.ui.geometry.Offset(left + squareSize, top + squareSize),
                end = androidx.compose.ui.geometry.Offset(left + squareSize - bracketLength, top + squareSize),
                strokeWidth = bracketWidth
            )
            drawLine(
                color = Color(0xFF4CAF50),
                start = androidx.compose.ui.geometry.Offset(left + squareSize, top + squareSize),
                end = androidx.compose.ui.geometry.Offset(left + squareSize, top + squareSize - bracketLength),
                strokeWidth = bracketWidth
            )
            
            // Draw grid if enabled
            if (showGrid) {
                val gridColor = Color.White.copy(alpha = 0.4f)
                val gridStroke = 1.5f
                
                // Vertical lines (rule of thirds)
                for (i in 1..2) {
                    val x = left + (squareSize * i / 3)
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(x, top),
                        end = androidx.compose.ui.geometry.Offset(x, top + squareSize),
                        strokeWidth = gridStroke
                    )
                }
                
                // Horizontal lines (rule of thirds)
                for (i in 1..2) {
                    val y = top + (squareSize * i / 3)
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(left, y),
                        end = androidx.compose.ui.geometry.Offset(left + squareSize, y),
                        strokeWidth = gridStroke
                    )
                }
            }
        }
        
        // Top Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Color.Black.copy(alpha = 0.5f)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Position leaf in frame",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(
                    onClick = { 
                        flashMode = when(flashMode) {
                            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                            else -> ImageCapture.FLASH_MODE_OFF
                        }
                        imageCapture?.flashMode = flashMode
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Icon(
                        when(flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                            ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                            else -> Icons.Default.FlashOff
                        },
                        contentDescription = "Flash",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Position the rice leaf inside the frame for best results",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }
        }
        
        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flip Camera Button - Now functional
            IconButton(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                ),
                enabled = !isCapturing
            ) {
                Icon(
                    Icons.Default.FlipCameraAndroid,
                    contentDescription = "Flip Camera",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Capture Button
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        color = Color(0xFF4CAF50),
                        strokeWidth = 4.dp
                    )
                } else {
                    // Outer ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner button
                        IconButton(
                            onClick = {
                                imageCapture?.let { capture ->
                                    isCapturing = true
                                    captureImage(context, capture, executor) { uri ->
                                        isCapturing = false
                                        if (uri != null) {
                                            onImageCaptured(uri)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.size(60.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.White
                            ),
                            enabled = cameraReady
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Capture",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
            
            // Grid Toggle - Now functional
            IconButton(
                onClick = { showGrid = !showGrid },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (showGrid) {
                        Color(0xFF4CAF50).copy(alpha = 0.5f)
                    } else {
                        Color.White.copy(alpha = 0.2f)
                    }
                ),
                enabled = !isCapturing
            ) {
                Icon(
                    if (showGrid) Icons.Default.GridOn else Icons.Default.GridOff,
                    contentDescription = "Toggle Grid",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Uri?) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
    )
    
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                Log.d("CameraScreen", "Photo capture succeeded: $savedUri")
                
                // Process image for ML model (resize to 224x224, correct orientation)
                val processedUri = ImageProcessor.processImageForML(context, savedUri, 224)
                
                if (processedUri != null) {
                    Log.d("CameraScreen", "Image processed successfully: $processedUri")
                    onImageCaptured(processedUri)
                } else {
                    Log.e("CameraScreen", "Image processing failed, using original")
                    onImageCaptured(savedUri) // Fallback to original if processing fails
                }
            }
            
            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Photo capture failed: ${exception.message}", exception)
                onImageCaptured(null)
            }
        }
    )
}
