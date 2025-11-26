package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.zynt.sumviltadconnect.ui.components.BrandProgressIndicator
import com.zynt.sumviltadconnect.ui.components.FullScreenSkeleton
import com.zynt.sumviltadconnect.ui.theme.AppDimensions
import com.zynt.sumviltadconnect.ui.theme.rememberWindowSize
import com.zynt.sumviltadconnect.ui.theme.WindowSize
import com.zynt.sumviltadconnect.ui.viewmodel.CropHealthUiState
import com.zynt.sumviltadconnect.ui.viewmodel.CropHealthViewModel
import com.zynt.sumviltadconnect.ui.viewmodel.CropHealthCommentsViewModel
import com.zynt.sumviltadconnect.ui.viewmodel.CommentsUiState
import java.text.SimpleDateFormat
import java.util.*

// Cache date formatters to avoid recreating them on every recomposition
private val dateFormatCache = mutableMapOf<String, String>()
private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
private val inputDateFormatAlt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
private val outputDateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropHealthScreen(
    navController: NavController? = null, 
    vm: CropHealthViewModel = viewModel(),
    onNewDetection: () -> Unit = {}
) {
    val state by vm.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<CropHealthRecord?>(null) }
    var showCommentsDialog by remember { mutableStateOf<CropHealthRecord?>(null) }
    val windowSize = rememberWindowSize()

    // Enhanced UI with better visual design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Enhanced Header
            EnhancedCropHealthHeader(
                onRefresh = { vm.refresh() }
            )

            // Content
            Box(modifier = Modifier.weight(1f)) {
                when (val currentState = state) {
                    is CropHealthUiState.Loading -> EnhancedLoadingState()
                    is CropHealthUiState.Error -> EnhancedErrorState(
                        message = currentState.message,
                        onRetry = { vm.refresh() }
                    )
                    is CropHealthUiState.Success -> {
                        val list = currentState.data
                        if (list.isEmpty()) {
                            EnhancedEmptyState(onNewDetection = onNewDetection)
                        } else {
                            EnhancedCropHealthList(
                                records = list,
                                onRecordClick = { record ->
                                    // Navigate to detail screen instead of showing dialog
                                    // TODO: You'll need to pass the record data via navigation
                                    // For now, storing in a shared ViewModel or passing serialized data
                                    navController?.navigate("crop_health_detail/${record.id}")
                                },
                                onDeleteClick = { showDeleteDialog = it },
                                paginationState = currentState,
                                vm = vm,
                                onCommentsClick = { showCommentsDialog = it }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNewDetection,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AppDimensions.paddingLarge()),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = AppDimensions.paddingSmall())
        ) {
            Icon(
                Icons.Default.AddAPhoto,
                contentDescription = "New Detection"
            )
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { record ->
        DeleteConfirmationDialog(
            record = record,
            onConfirm = {
                vm.deleteRecord(record.id)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    // Comments dialog (keeping this for quick access from list)
    showCommentsDialog?.let { record ->
        CommentsDialog(
            record = record,
            onDismiss = { showCommentsDialog = null }
        )
    }
}

@Composable
private fun EnhancedCropHealthHeader(
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimensions.paddingMedium(), vertical = AppDimensions.paddingSmall()),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.paddingSmall()),
        shape = RoundedCornerShape(AppDimensions.paddingLarge())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(AppDimensions.paddingLarge())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Crop Health Records",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Track your rice crop detections",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Row {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimensions.paddingMedium())
    ) {
        FullScreenSkeleton(listItems = 6)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BrandProgressIndicator(size = AppDimensions.buttonHeight())
                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                Text(
                    "Loading crop health records...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EnhancedErrorState(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimensions.paddingMedium()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.cardElevation()),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium())
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.paddingLarge()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(AppDimensions.iconSizeLarge())
            )
            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
            Text(
                text = "Error loading records",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EnhancedEmptyState(onNewDetection: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimensions.paddingExtraLarge()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.cardElevation()),
            shape = RoundedCornerShape(AppDimensions.paddingLarge())
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.paddingExtraLarge()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(AppDimensions.fabSize())
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimensions.iconSizeLarge()),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

                Text(
                    text = "No Records Yet",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))

                Text(
                    text = "Start detecting rice diseases to build your crop health history",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

                Button(
                    onClick = onNewDetection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppDimensions.buttonHeight()),
                    shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium())
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null)
                    Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                    Text(
                        "Start First Detection",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedCropHealthList(
    records: List<CropHealthRecord>,
    onRecordClick: (CropHealthRecord) -> Unit,
    onDeleteClick: (CropHealthRecord) -> Unit,
    paginationState: CropHealthUiState.Success,
    vm: CropHealthViewModel,
    onCommentsClick: (CropHealthRecord) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Records List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppDimensions.paddingMedium()),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium()),
            contentPadding = PaddingValues(
                top = AppDimensions.paddingMedium(),
                bottom = AppDimensions.fabSize() + AppDimensions.paddingLarge() // Space for FAB
            )
        ) {
            item(key = "header") {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it / 2 },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(300))
                ) {
                    Text(
                        text = "Showing ${records.size} of ${paginationState.totalRecords} records",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = AppDimensions.paddingSmall())
                    )
                }
            }

            // Add animated items with staggered entrance
            itemsIndexed(
                items = records,
                key = { _, record -> record.id }
            ) { index, record ->
                AnimatedCropHealthItem(
                    record = record,
                    index = index,
                    isVisible = isVisible,
                    onClick = { onRecordClick(record) },
                    onDeleteClick = { onDeleteClick(record) },
                    onCommentsClick = { onCommentsClick(record) }
                )
            }

            // Load More Button
            if (paginationState.hasMore) {
                item(key = "loadMoreButton") {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(400))
                    ) {
                        LoadMoreButton(
                            isLoading = paginationState.isLoadingMore,
                            onLoadMore = { vm.loadMoreRecords() }
                        )
                    }
                }
            } else if (records.isNotEmpty()) {
                item(key = "endMessage") {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(400))
                    ) {
                        Text(
                            text = "You've reached the end",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AppDimensions.paddingMedium()),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedCropHealthItem(
    record: CropHealthRecord,
    index: Int,
    isVisible: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCommentsClick: (CropHealthRecord) -> Unit
) {
    var itemVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            kotlinx.coroutines.delay((index * 50L).coerceAtMost(400L))
            itemVisible = true
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (itemVisible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "itemScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (itemVisible) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "itemAlpha"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha
            }
    ) {
        EnhancedCropHealthItem(
            record = record,
            onClick = onClick,
            onDeleteClick = onDeleteClick,
            onCommentsClick = onCommentsClick
        )
    }
}

@Composable
private fun EnhancedCropHealthItem(
    record: CropHealthRecord,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCommentsClick: (CropHealthRecord) -> Unit = {}
) {
    val isHealthy = record.disease?.contains("healthy", ignoreCase = true) == true ||
        record.disease?.contains("no disease", ignoreCase = true) == true
    
    Card(
        modifier = Modifier
            .fillMaxWidth(), // removed overall clickable to avoid intercepting icon taps
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.cardElevation()),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = if (!isHealthy) BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)) else null
    ) {
        Column(
            modifier = Modifier
                .padding(AppDimensions.paddingLarge())
                .clickable { onClick() } // make body clickable instead
        ) {
            // Header with disease status and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Disease status
                Surface(
                    color = if (isHealthy) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFFF5722).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(AppDimensions.paddingMedium())
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = AppDimensions.paddingMedium(), vertical = AppDimensions.paddingExtraSmall()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isHealthy) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isHealthy) Color(0xFF4CAF50) else Color(0xFFFF5722),
                            modifier = Modifier.size(AppDimensions.iconSizeSmall())
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.paddingExtraSmall()))
                        Text(
                            text = record.disease ?: "Unknown",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHealthy) Color(0xFF4CAF50) else Color(0xFFFF5722)
                        )
                    }
                }

                // Action buttons
                Row {
                    IconButton(onClick = { onCommentsClick(record) }, modifier = Modifier.size(AppDimensions.iconSizeLarge())) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Comments", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(AppDimensions.iconSizeLarge())
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "View Details",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(AppDimensions.iconSizeLarge())
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

            // Image and details
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Image
                record.image?.let { imagePath ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(com.zynt.sumviltadconnect.data.network.ApiClient.getImageUrl(imagePath))
                            .crossfade(true)
                            .memoryCacheKey("crop_health_image_${record.id}")
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "Detection image",
                        modifier = Modifier
                            .size(AppDimensions.fabSize())
                            .clip(RoundedCornerShape(AppDimensions.paddingMedium())),
                        contentScale = ContentScale.Crop,
                        error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                        placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                    )

                    Spacer(modifier = Modifier.width(AppDimensions.paddingMedium()))
                }

                // Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Confidence
                    record.confidence?.let { confidence ->
                        Text(
                            text = "Confidence: ${"%.1f".format(confidence)}%",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(AppDimensions.paddingExtraSmall()))
                    }

                    // Recommendation preview
                    record.recommendation?.let { recommendation ->
                        Text(
                            text = recommendation,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(AppDimensions.paddingExtraSmall()))
                    }

                    // Admin recommendation indicator
                    record.admin_recommendation?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(AppDimensions.iconSizeSmall())
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.paddingExtraSmall()))
                            Text(
                                text = "Admin recommendation available",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Date
                    Text(
                        text = formatDate(record.created_at),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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
            shape = RoundedCornerShape(AppDimensions.paddingLarge()),
            tonalElevation = AppDimensions.paddingSmall(),
            shadowElevation = AppDimensions.paddingMedium(),
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
                                .width(AppDimensions.iconSizeLarge())
                                .height(AppDimensions.paddingExtraSmall())
                                .align(Alignment.CenterHorizontally)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(AppDimensions.paddingExtraSmall())
                                )
                        )

                        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

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

                HorizontalDivider(thickness = AppDimensions.paddingExtraSmall(), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

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
                                modifier = Modifier.size(AppDimensions.iconSizeMedium()),
                                strokeWidth = AppDimensions.paddingExtraSmall(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
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
                            modifier = Modifier.padding(AppDimensions.paddingLarge())
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(AppDimensions.iconSizeLarge())
                            )
                            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
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
                                modifier = Modifier.padding(AppDimensions.paddingExtraLarge())
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(AppDimensions.buttonHeight())
                                )
                                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                                Text(
                                    text = "No messages yet",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
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
                            verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium()),
                            contentPadding = PaddingValues(vertical = AppDimensions.paddingMedium())
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
            shadowElevation = AppDimensions.paddingSmall()
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
                        .heightIn(min = AppDimensions.iconSizeLarge(), max = AppDimensions.detectionImageHeight()),
                    placeholder = {
                        Text(
                            "Message...",
                            fontSize = 15.sp
                        )
                    },
                    maxLines = 4,
                    shape = RoundedCornerShape(AppDimensions.paddingLarge()),
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
                    modifier = Modifier.size(AppDimensions.iconSizeLarge()),
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
                            modifier = Modifier.size(AppDimensions.iconSizeSmall()),
                            strokeWidth = AppDimensions.paddingExtraSmall(),
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
            .padding(horizontal = AppDimensions.paddingMedium()),
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
                    modifier = Modifier.size(AppDimensions.iconSizeSmall())
                )
                Spacer(modifier = Modifier.width(AppDimensions.paddingExtraSmall()))
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
            Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
            Text(
                text = formatDate(comment.created_at),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // Message bubble - iPhone style
        Surface(
            modifier = Modifier.widthIn(max = AppDimensions.detectionImageHeight()),
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
            shadowElevation = AppDimensions.paddingExtraSmall()
        ) {
            Text(
                text = comment.message,
                fontSize = 15.sp,
                color = if (isFromExpert)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onPrimary,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = AppDimensions.iconSizeSmall(), vertical = AppDimensions.paddingSmall())
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
                        .size(AppDimensions.paddingExtraSmall())
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(AppDimensions.paddingExtraSmall()))
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

@Composable
private fun DeleteConfirmationDialog(
    record: CropHealthRecord,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Delete Record",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Text(
                "Are you sure you want to delete this crop health record? This action cannot be undone.\n\n" +
                "Disease: ${record.disease ?: "Unknown"}\n" +
                "Date: ${formatDate(record.created_at)}"
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


// Utility function to format dates
private fun formatDate(dateString: String): String {
    return try {
        dateFormatCache.getOrPut(dateString) {
            val date = inputDateFormat.parse(dateString) ?: inputDateFormatAlt.parse(dateString)
            date?.let { outputDateFormat.format(it) } ?: dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

@Composable
private fun LoadMoreButton(
    isLoading: Boolean,
    onLoadMore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Button(
            onClick = onLoadMore,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Loading more...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Icon(
                    Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Load More Records",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
