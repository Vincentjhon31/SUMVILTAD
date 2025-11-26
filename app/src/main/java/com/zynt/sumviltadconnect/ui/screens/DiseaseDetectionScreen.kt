package com.zynt.sumviltadconnect.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.zynt.sumviltadconnect.ui.theme.AppDimensions
import com.zynt.sumviltadconnect.ui.viewmodel.DiseaseDetectionViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DiseaseDetectionScreen(
    navController: NavController,
    viewModel: DiseaseDetectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Permission states
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Image selection states
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val detectionResult by viewModel.detectionResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Camera screen state
    var showCameraScreen by remember { mutableStateOf(false) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.setSelectedImage(it)
            viewModel.uploadImage(context, it)
        }
    }
    
    // Show camera screen if requested
    if (showCameraScreen && cameraPermissionState.status.isGranted) {
        CameraScreen(
            onImageCaptured = { uri ->
                viewModel.setSelectedImage(uri)
                viewModel.uploadImage(context, uri)
                showCameraScreen = false
            },
            onBack = { showCameraScreen = false }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Enhanced Header
            EnhancedDiseaseDetectionHeader(
                onBackClick = { navController.popBackStack() },
                hasResult = detectionResult != null,
                onClearClick = { viewModel.clearResult() }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp) // Overlap with header
                    .padding(horizontal = AppDimensions.paddingMedium()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Instructions Card - Only show if no image selected
                if (selectedImageUri == null && detectionResult == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(AppDimensions.cornerRadiusMedium())),
                        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(AppDimensions.paddingLarge()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(AppDimensions.paddingMedium()))
                            Column {
                                Text(
                                    text = "How to get accurate results:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                                Text(
                                    text = "• Use clear, well-lit photos\n• Focus on diseased leaf areas\n• Avoid blurry or dark images\n• One leaf per photo works best",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))
                }

                // Image capture buttons - Enhanced design
                if (selectedImageUri == null && detectionResult == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(AppDimensions.cornerRadiusLarge()),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(AppDimensions.paddingLarge()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Select Image Source",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

                            // Camera Button - Enhanced
                            Button(
                                onClick = {
                                    if (cameraPermissionState.status.isGranted) {
                                        showCameraScreen = true
                                    } else {
                                        cameraPermissionState.launchPermissionRequest()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AppDimensions.buttonHeight()),
                                shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(AppDimensions.iconSizeMedium())
                                )
                                Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                                Text(
                                    "Take Photo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

                            // Gallery Button - Enhanced
                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AppDimensions.buttonHeight()),
                                shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 1.dp
                                )
                            ) {
                                Icon(
                                    Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(AppDimensions.iconSizeMedium())
                                )
                                Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                                Text(
                                    "Choose from Gallery",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Selected Image Display - Enhanced
                selectedImageUri?.let { uri ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(AppDimensions.cornerRadiusLarge())),
                        shape = RoundedCornerShape(AppDimensions.cornerRadiusLarge())
                    ) {
                        Box {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Selected rice leaf",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AppDimensions.detectionImageHeight()),
                                contentScale = ContentScale.Crop
                            )

                            // Gradient overlay for better text visibility
                            if (isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(AppDimensions.fabSize())
                                        )
                                        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                                        Text(
                                            "Analyzing image...",
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                                        Text(
                                            "This may take a few seconds",
                                            color = Color.White.copy(alpha = 0.8f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Error Message - Enhanced
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.error,
                                RoundedCornerShape(AppDimensions.cornerRadiusMedium())
                            ),
                        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(AppDimensions.paddingMedium()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(AppDimensions.iconSizeMedium())
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Detection Results - Completely redesigned
                detectionResult?.let { result ->
                    Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

                    if (result.is_rice_leaf) {
                        // Success Result Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(AppDimensions.cornerRadiusLarge()),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(AppDimensions.paddingLarge())
                            ) {
                                // Header with success icon
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(AppDimensions.iconSizeLarge() + 16.dp)
                                            .background(
                                                Color(0xFF4CAF50).copy(alpha = 0.1f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Success",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(AppDimensions.iconSizeMedium())
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(AppDimensions.paddingMedium()))
                                    Column {
                                        Text(
                                            text = "Analysis Complete",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Rice leaf detected",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

                                // Disease Information Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(AppDimensions.paddingMedium())
                                    ) {
                                        Text(
                                            text = "DETECTED DISEASE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        result.disease?.let { disease ->
                                            Text(
                                                text = disease,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                // Confidence Level - Visual indicator
                                result.confidence?.let { confidence ->
                                    Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

                                    val confidenceColor = when {
                                        confidence >= 80 -> Color(0xFF4CAF50)
                                        confidence >= 60 -> Color(0xFFFFA726)
                                        else -> Color(0xFFEF5350)
                                    }

                                    val confidenceLabel = when {
                                        confidence >= 80 -> "High Confidence"
                                        confidence >= 60 -> "Medium Confidence"
                                        else -> "Low Confidence"
                                    }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                                        colors = CardDefaults.cardColors(
                                            containerColor = confidenceColor.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(AppDimensions.paddingMedium())
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Confidence Level",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    )
                                                    Spacer(modifier = Modifier.height(AppDimensions.paddingExtraSmall()))
                                                    Text(
                                                        text = confidenceLabel,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = confidenceColor
                                                    )
                                                }
                                                Text(
                                                    text = "${"%.1f".format(confidence)}%",
                                                    style = MaterialTheme.typography.headlineMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = confidenceColor
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))

                                            // Progress bar
                                            LinearProgressIndicator(
                                                progress = (confidence / 100).toFloat(),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(AppDimensions.cornerRadiusSmall())),
                                                color = confidenceColor,
                                                trackColor = confidenceColor.copy(alpha = 0.2f)
                                            )
                                        }
                                    }
                                }

                                // Recommendations Section
                                result.recommendation?.let { recommendation ->
                                    Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(AppDimensions.paddingMedium())
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Lightbulb,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(AppDimensions.iconSizeMedium())
                                                )
                                                Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                                                Text(
                                                    text = "Recommendations",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                                            Text(
                                                text = recommendation,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                lineHeight = 22.sp
                                            )
                                        }
                                    }
                                }

                                // Details Section
                                result.details?.let { details ->
                                    Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(AppDimensions.paddingMedium())
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Description,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(AppDimensions.iconSizeSmall())
                                                )
                                                Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                                                Text(
                                                    text = "Additional Details",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                                            Text(
                                                text = details,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        // Not a rice leaf - Error card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.error,
                                    RoundedCornerShape(AppDimensions.cornerRadiusLarge())
                                ),
                            shape = RoundedCornerShape(AppDimensions.cornerRadiusLarge()),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(AppDimensions.paddingLarge()),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(AppDimensions.iconSizeLarge() + 28.dp)
                                        .background(
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(AppDimensions.iconSizeLarge())
                                    )
                                }

                                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

                                Text(
                                    text = "Not a Rice Leaf",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))

                                Text(
                                    text = "The image doesn't appear to be a rice leaf. Please capture a clear photo of rice plant leaves for accurate disease detection.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    // Action Buttons - Enhanced
                    Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall())
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearResult() },
                            modifier = Modifier
                                .weight(1f)
                                .height(AppDimensions.buttonHeight()),
                            shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium())
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(AppDimensions.iconSizeSmall())
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                            Text("New Scan", style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = { navController.navigate("crop_health") },
                            modifier = Modifier
                                .weight(1f)
                                .height(AppDimensions.buttonHeight()),
                            shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium())
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(AppDimensions.iconSizeSmall())
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                            Text("View History", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))
            }
        }
    }
}

@Composable
private fun EnhancedDiseaseDetectionHeader(
    onBackClick: () -> Unit,
    hasResult: Boolean,
    onClearClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
            .padding(bottom = 40.dp) // Extra padding for overlap
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.paddingLarge())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.1f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                if (hasResult) {
                    IconButton(
                        onClick = onClearClick,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.1f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

            Text(
                text = "Disease Detection",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "AI-powered rice disease analysis",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}
