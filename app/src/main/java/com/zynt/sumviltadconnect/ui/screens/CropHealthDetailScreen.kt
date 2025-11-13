package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.zynt.sumviltadconnect.data.model.CropHealthRecord
import com.zynt.sumviltadconnect.data.model.CropHealthComment
import com.zynt.sumviltadconnect.ui.viewmodel.CropHealthViewModel
import com.zynt.sumviltadconnect.ui.viewmodel.CropHealthCommentsViewModel
import com.zynt.sumviltadconnect.ui.viewmodel.CommentsUiState
import java.text.SimpleDateFormat
import java.util.*
import com.zynt.sumviltadconnect.ui.theme.AppDimensions

// Date formatter for this screen
private val detailDateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
private val inputDateFormatAlt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropHealthDetailScreen(
    navController: NavController,
    record: CropHealthRecord,
    viewModel: CropHealthViewModel = viewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(50)
        isVisible = true
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp,
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimensions.paddingSmall(), vertical = AppDimensions.paddingSmall()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back button
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(AppDimensions.buttonHeight())
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(AppDimensions.iconSizeMedium())
                        )
                    }

                    // Title
                    Text(
                        text = "Detection Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.2.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingExtraSmall())
                    ) {
                        // Message button
                        IconButton(
                            onClick = { showCommentsDialog = true },
                            modifier = Modifier.size(AppDimensions.buttonHeight())
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Messages",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(AppDimensions.iconSizeSmall())
                            )
                        }
                        // Delete button
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(AppDimensions.buttonHeight())
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(AppDimensions.iconSizeSmall())
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = AppDimensions.paddingExtraLarge()),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingLarge())
        ) {
            // Hero Image Section
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(400)) + 
                           slideInVertically(initialOffsetY = { -it / 4 })
                ) {
                    HeroImageSection(record)
                }
            }

            // Spacer after hero
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Disease Status Card
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = 100)) +
                           slideInVertically(initialOffsetY = { it / 4 })
                ) {
                    DiseaseStatusCard(record)
                }
            }

            // Confidence Section
            record.confidence?.let { conf ->
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(400, delayMillis = 200)) +
                               slideInVertically(initialOffsetY = { it / 4 })
                    ) {
                        ConfidenceCard(conf)
                    }
                }
            }

            // AI Recommendation
            val detailsContent = record.recommendation ?: record.details
            detailsContent?.let { details ->
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(400, delayMillis = 300)) +
                               slideInVertically(initialOffsetY = { it / 4 })
                    ) {
                        DetailCard(
                            title = "Recommendation",
                            icon = Icons.Default.AutoAwesome,
                            body = details,
                            color = Color(0xFF6366F1),
                            highlighted = true,
                            isExpert = false
                        )
                    }
                }
            }

            // Expert Advice
            record.admin_recommendation?.let { rec ->
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(400, delayMillis = 400)) +
                               slideInVertically(initialOffsetY = { it / 4 })
                    ) {
                        DetailCard(
                            title = "Expert Advice",
                            icon = Icons.Default.Verified,
                            body = rec,
                            color = Color(0xFF059669),
                            highlighted = true,
                            isExpert = true
                        )
                    }
                }
            }

            // Detection Metadata
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = 500)) +
                           slideInVertically(initialOffsetY = { it / 4 })
                ) {
                    MetadataCard(record)
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Record", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Are you sure you want to delete this crop health record? This action cannot be undone.\n\n" +
                    "Disease: ${record.disease ?: "Unknown"}\n" +
                    "Date: ${formatDetailDate(record.created_at)}"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecord(record.id)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Comments dialog
    if (showCommentsDialog) {
        CommentsDialog(
            record = record,
            onDismiss = { showCommentsDialog = false }
        )
    }
}

@Composable
private fun HeroImageSection(record: CropHealthRecord) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimensions.detectionImageHeight())
    ) {
        // Background image
        record.image?.let { img ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(com.zynt.sumviltadconnect.data.network.ApiClient.getImageUrl(img))
                    .crossfade(true)
                    .memoryCacheKey("detail_image_${record.id}")
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Detection image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
            )
        } ?: Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
        )

        // Gradient overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun DiseaseStatusCard(record: CropHealthRecord) {
    val healthy = record.disease?.contains("healthy", true) == true ||
                 record.disease?.contains("no disease", true) == true

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (healthy)
                Color(0xFF34C759).copy(alpha = 0.08f)
            else
                Color(0xFFFF3B30).copy(alpha = 0.08f)
        ),
        border = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.paddingLarge()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(AppDimensions.fabSize())
                    .background(
                        if (healthy)
                            Color(0xFF34C759).copy(alpha = 0.15f)
                        else
                            Color(0xFFFF3B30).copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (healthy) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (healthy) Color(0xFF34C759) else Color(0xFFFF3B30),
                    modifier = Modifier.size(AppDimensions.iconSizeMedium())
                )
            }

            Spacer(modifier = Modifier.width(AppDimensions.paddingMedium()))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (healthy) "Healthy Crop" else "Disease Detected",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = record.disease ?: "Unknown",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun ConfidenceCard(confidence: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimensions.paddingLarge()),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusLarge()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.paddingLarge())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(AppDimensions.iconSizeSmall())
                    )
                    Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                    Text(
                        text = "Confidence Level",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.3.sp
                    )
                }
                Text(
                    text = "${"%.1f".format(confidence)}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        confidence >= 80 -> Color(0xFF34C759)
                        confidence >= 60 -> Color(0xFFFFCC00)
                        else -> Color(0xFFFF3B30)
                    }
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimensions.paddingSmall())
                    .clip(RoundedCornerShape(AppDimensions.cornerRadiusSmall()))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((confidence.toFloat() / 100f).coerceIn(0f, 1f))
                        .background(
                            Brush.horizontalGradient(
                                colors = when {
                                    confidence >= 80 -> listOf(Color(0xFF34C759), Color(0xFF30D158))
                                    confidence >= 60 -> listOf(Color(0xFFFFCC00), Color(0xFFFFD60A))
                                    else -> listOf(Color(0xFFFF3B30), Color(0xFFFF453A))
                                }
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))

            Text(
                text = when {
                    confidence >= 80 -> "High confidence detection"
                    confidence >= 60 -> "Moderate confidence detection"
                    else -> "Low confidence - consider retaking image"
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    icon: ImageVector,
    body: String,
    color: Color,
    highlighted: Boolean = false,
    isExpert: Boolean = false
) {
    val cardColor = if (highlighted) {
        color.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        border = null
    ) {
        Column(Modifier.padding(24.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(AppDimensions.buttonHeight())
                        .background(
                            color.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(AppDimensions.iconSizeSmall())
                    )
                }

                Spacer(Modifier.width(AppDimensions.paddingMedium()))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isExpert) {
                        Text(
                            text = "Verified by agricultural expert",
                            fontSize = 12.sp,
                            color = color,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (highlighted) {
                    Surface(
                        color = color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(AppDimensions.cornerRadiusSmall())
                    ) {
                        Text(
                            text = if (isExpert) "⭐" else " ",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (highlighted) {
                Spacer(Modifier.height(AppDimensions.paddingLarge()))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    color.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(Modifier.height(AppDimensions.paddingLarge()))

            Text(
                text = body,
                fontSize = 15.sp,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                fontWeight = if (highlighted) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun MetadataCard(record: CropHealthRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimensions.paddingLarge()),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusLarge()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = null
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AppDimensions.iconSizeSmall())
                )
                Spacer(Modifier.width(AppDimensions.paddingSmall()))
                Text(
                    text = "Detection Info",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 0.3.sp
                )
            }

            Spacer(Modifier.height(AppDimensions.paddingLarge()))

            MetadataRow(
                icon = Icons.Default.Tag,
                label = "Record ID",
                value = "#${record.id}"
            )
            Spacer(Modifier.height(18.dp))
            MetadataRow(
                icon = Icons.Default.CalendarToday,
                label = "Date",
                value = formatDetailDate(record.created_at)
            )
            
            record.inference_time_seconds?.let {
                Spacer(Modifier.height(18.dp))
                MetadataRow(
                    icon = Icons.Default.Speed,
                    label = "Processing Time",
                    value = "${"%.2f".format(it)}s"
                )
            }
        }
    }
}

@Composable
private fun MetadataRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(AppDimensions.iconSizeSmall())
        )
        Spacer(Modifier.width(AppDimensions.paddingSmall()))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatDetailDate(dateString: String): String {
    return try {
        val date = inputDateFormat.parse(dateString) ?: inputDateFormatAlt.parse(dateString)
        date?.let { detailDateFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

// Comments Dialog - Shared with CropHealthScreen
@Composable
private fun CommentsDialog(
    record: CropHealthRecord,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(50)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dialogScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "dialogAlpha"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                    this.alpha = alpha
                },
            shape = RoundedCornerShape(AppDimensions.cornerRadiusLarge()),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.fillMaxSize()) {
                // Modern iPhone-style header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(AppDimensions.paddingMedium())
                    ) {
                        // Drag indicator
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .align(Alignment.CenterHorizontally)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(AppDimensions.paddingExtraSmall())
                                )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Discussion",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Chat with agricultural experts",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalIconButton(
                                onClick = onDismiss,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Enhanced comments content
                Box(
                    Modifier
                        .weight(1f)
                        .padding(AppDimensions.paddingMedium())
                ) {
                    EnhancedCommentsSection(record)
                }
            }
        }
    }
}

@Composable
private fun EnhancedCommentsSection(record: CropHealthRecord) {
    val commentsViewModel: CropHealthCommentsViewModel = viewModel()
    val commentsState by commentsViewModel.state.collectAsState()
    var newMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Load comments when the component is first created
    LaunchedEffect(record.id) {
        commentsViewModel.loadComments(record.id)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Comments display - iPhone Messages style
        val currentCommentsState = commentsState
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentCommentsState) {
                is CommentsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Loading conversation...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is CommentsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Failed to load conversation",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = currentCommentsState.message,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                is CommentsUiState.Success -> {
                    if (currentCommentsState.comments.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No messages yet",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Start a conversation with agricultural experts",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(currentCommentsState.comments) { comment ->
                                iMessageStyleComment(
                                    comment = comment,
                                    onMarkAsRead = { commentsViewModel.markAsRead(comment.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Message input - iPhone style
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimensions.paddingMedium()),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = AppDimensions.buttonHeight(), max = 120.dp),
                    placeholder = {
                        Text(
                            "Message...",
                            fontSize = 15.sp
                        )
                    },
                    maxLines = 4,
                    shape = RoundedCornerShape(AppDimensions.cornerRadiusLarge()),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))

                val isLoading = currentCommentsState is CommentsUiState.Success && currentCommentsState.isLoading

                FilledIconButton(
                    onClick = {
                        if (newMessage.isNotBlank()) {
                            commentsViewModel.sendComment(newMessage)
                            newMessage = ""
                        }
                    },
                    enabled = newMessage.isNotBlank() && !isLoading,
                    modifier = Modifier.size(AppDimensions.buttonHeight()),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (newMessage.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (newMessage.isNotBlank())
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(AppDimensions.iconSizeSmall())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun iMessageStyleComment(
    comment: CropHealthComment,
    onMarkAsRead: (Int) -> Unit
) {
    val isFromExpert = comment.is_admin_response
    val alignment = if (isFromExpert) Alignment.Start else Alignment.End

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimensions.paddingSmall()),
        horizontalAlignment = alignment
    ) {
        // Sender name and time
        Row(
            modifier = Modifier.padding(horizontal = AppDimensions.paddingSmall(), vertical = AppDimensions.paddingExtraSmall()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isFromExpert) Arrangement.Start else Arrangement.End
        ) {
            if (isFromExpert) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = comment.user?.name ?: "Unknown User",
                fontSize = 12.sp,
                fontWeight = if (isFromExpert) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isFromExpert)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatDetailDate(comment.created_at),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // Message bubble - iPhone style
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = if (isFromExpert) AppDimensions.paddingExtraSmall() else AppDimensions.cornerRadiusMedium(),
                topEnd = if (isFromExpert) AppDimensions.cornerRadiusMedium() else AppDimensions.paddingExtraSmall(),
                bottomStart = AppDimensions.cornerRadiusMedium(),
                bottomEnd = AppDimensions.cornerRadiusMedium()
            ),
            color = if (isFromExpert)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.primary,
            shadowElevation = 1.dp
        ) {
            Text(
                text = comment.message,
                fontSize = 15.sp,
                color = if (isFromExpert)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onPrimary,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = AppDimensions.paddingMedium(), vertical = AppDimensions.paddingSmall())
            )
        }

        // Unread indicator
        if (!comment.is_read && !comment.is_admin_response) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = AppDimensions.paddingSmall(), vertical = AppDimensions.paddingExtraSmall())
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "New",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            LaunchedEffect(comment.id) {
                kotlinx.coroutines.delay(1000)
                onMarkAsRead(comment.id)
            }
        }
    }
}
