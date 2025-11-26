package com.zynt.sumviltadconnect.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.zynt.sumviltadconnect.ui.theme.AppDimensions

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

    // Theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    // Dimensions
    val paddingMedium = AppDimensions.paddingMedium()
    val paddingLarge = AppDimensions.paddingLarge()
    val cornerRadiusMedium = AppDimensions.cornerRadiusMedium()
    val paddingExtraSmall = AppDimensions.paddingExtraSmall()
    val iconSizeSmall = AppDimensions.iconSizeSmall()
    val paddingSmall = AppDimensions.paddingSmall()
    val buttonHeight = AppDimensions.buttonHeight()
    val iconSizeMedium = AppDimensions.iconSizeMedium()

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
                color = primaryColor,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                style = Stroke(width = 3f)
            )
            
            // Corner brackets for better visual guidance
            val bracketLength = 50f
            val bracketWidth = 5f
            
            // Top-left bracket
            drawLine(
                color = primaryColor,
                start = androidx.compose.ui.geometry.Offset(left, top),
                end = androidx.compose.ui.geometry.Offset(left + bracketLength, top),
                strokeWidth = bracketWidth
            )
            drawLine(
                color = primaryColor,
                start = androidx.compose.ui.geometry.Offset(left, top),
                end = androidx.compose.ui.geometry.Offset(left, top + bracketLength),
                strokeWidth = bracketWidth
            )
            
            // Top-right bracket
            drawLine(
                color = primaryColor,
                start = androidx.compose.ui.geometry.Offset(left + squareSize, top),
                end = androidx.compose.ui.geometry.Offset(left + squareSize - bracketLength, top),
                strokeWidth = bracketWidth
            )
            drawLine(
                color = primaryColor,
                start = androidx.compose.ui.geometry.Offset(left + squareSize, top),
                end = androidx.compose.ui.geometry.Offset(left + squareSize, top + bracketLength),
                strokeWidth = bracketWidth
            )
            
            // Bottom-left bracket
            drawLine(
                color = primaryColor,
                start = androidx.compose.ui.geometry.Offset(left, top + squareSize),
                end = androidx.compose.ui.geometry.Offset(left + bracketLength, top + squareSize),
                strokeWidth = bracketWidth
            )
            drawLine(
                color = primaryColor,
                start = androidx.compose.ui.geometry.Offset(left, top + squareSize),
                end = androidx.compose.ui.geometry.Offset(left, top + squareSize - bracketLength),
                strokeWidth = bracketWidth
            )
            
            // Bottom-right bracket
            drawLine(
                color = primaryColor,
                start = androidx.compose.ui.geometry.Offset(left + squareSize, top + squareSize),
                end = androidx.compose.ui.geometry.Offset(left + squareSize - bracketLength, top + squareSize),
                strokeWidth = bracketWidth
            )
            drawLine(
                color = primaryColor,
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
        
        // Top Bar with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
                .padding(paddingMedium)
                .padding(bottom = paddingLarge)
        ) {
            Column {
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
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
                
                Spacer(modifier = Modifier.height(paddingMedium))
                
                // Instructions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(cornerRadiusMedium),
                    colors = CardDefaults.cardColors(
                        containerColor = primaryColor.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = paddingExtraSmall)
                ) {
                    Row(
                        modifier = Modifier.padding(paddingMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = onPrimaryColor,
                            modifier = Modifier.size(iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(paddingSmall))
                        Text(
                            text = "Position the rice leaf inside the frame for best results",
                            color = onPrimaryColor,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Bottom Controls with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(paddingLarge)
                .padding(top = paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flip Camera Button
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier.size(buttonHeight),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    enabled = !isCapturing
                ) {
                    Icon(
                        Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip Camera",
                        tint = Color.White,
                        modifier = Modifier.size(iconSizeMedium)
                    )
                }
                
                // Capture Button - Enhanced Design
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Button(
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
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            enabled = cameraReady,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            // Inner circle is the button itself
                        }
                    }
                }
                
                // Grid Toggle
                IconButton(
                    onClick = { showGrid = !showGrid },
                    modifier = Modifier.size(buttonHeight),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (showGrid) {
                            primaryColor.copy(alpha = 0.8f)
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
                        modifier = Modifier.size(iconSizeMedium)
                    )
                }
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
