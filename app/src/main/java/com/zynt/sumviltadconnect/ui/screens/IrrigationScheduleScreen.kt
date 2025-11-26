package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zynt.sumviltadconnect.data.model.IrrigationSchedule
import com.zynt.sumviltadconnect.ui.theme.AppDimensions
import com.zynt.sumviltadconnect.ui.theme.WindowSize
import com.zynt.sumviltadconnect.ui.theme.rememberWindowSize
import com.zynt.sumviltadconnect.ui.viewmodel.IrrigationUiState
import com.zynt.sumviltadconnect.ui.viewmodel.IrrigationViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IrrigationScheduleScreen(
    modifier: Modifier = Modifier,
    viewModel: IrrigationViewModel = viewModel()
) {
    val upcomingSchedules by viewModel.upcomingSchedules.collectAsState()
    val pastSchedules by viewModel.pastSchedules.collectAsState()
    val nextIrrigation by viewModel.nextIrrigation.collectAsState()
    val state by viewModel.state.collectAsState()
    val windowSize = rememberWindowSize()

    // Pagination state
    val upcomingCurrentPage by viewModel.upcomingCurrentPage.collectAsState()
    val upcomingTotalPages by viewModel.upcomingTotalPages.collectAsState()
    val pastCurrentPage by viewModel.pastCurrentPage.collectAsState()
    val pastTotalPages by viewModel.pastTotalPages.collectAsState()

    // Request dialog state
    var showRequestDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val isSubmittingRequest by viewModel.isSubmittingRequest.collectAsState()
    val requestSubmitSuccess by viewModel.requestSubmitSuccess.collectAsState()
    val requestSubmitError by viewModel.requestSubmitError.collectAsState()
    
    // My Requests state
    val myRequests by viewModel.myRequests.collectAsState()
    val isLoadingRequests by viewModel.isLoadingRequests.collectAsState()

    // Animation state
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    // Show error snackbar
    val currentState = state
    if (currentState is IrrigationUiState.Error) {
        LaunchedEffect(currentState.message) {
            // You can add snackbar host here if needed
        }
    }

    // Handle request submission feedback
    LaunchedEffect(requestSubmitSuccess) {
        requestSubmitSuccess?.let {
            showRequestDialog = false
            viewModel.clearRequestMessages()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            state = rememberLazyListState(),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
        ) {
            // Header
            item {
                EnhancedIrrigationHeader()
                Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))
            }

            // Show loading state
            if (currentState is IrrigationUiState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Show error state
            if (currentState is IrrigationUiState.Error) {
                item {
                    EnhancedErrorState(
                        message = currentState.message,
                        onRetry = { viewModel.loadIrrigationSchedules() }
                    )
                }
            }

            // Show content when data is available
            if (currentState is IrrigationUiState.Success || upcomingSchedules.isNotEmpty() || pastSchedules.isNotEmpty()) {
                // Next Irrigation Alert
                nextIrrigation?.let { next ->
                    item {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = slideInVertically(
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(300))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = AppDimensions.paddingMedium())) {
                                NextIrrigationCard(
                                    schedule = next,
                                    viewModel = viewModel
                                )
                                Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))
                            }
                        }
                    }
                }

                // Responsive Layout
                if (windowSize == WindowSize.EXPANDED) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppDimensions.paddingMedium()),
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium()),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left Column: Schedules
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                if (upcomingSchedules.isNotEmpty()) {
                                    Text(
                                        text = "Upcoming Irrigation",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = AppDimensions.paddingMedium())
                                    )
                                    
                                    upcomingSchedules.forEachIndexed { index, schedule ->
                                        EnhancedScheduleCard(
                                            schedule = schedule,
                                            viewModel = viewModel,
                                            isUpcoming = true,
                                            animationDelay = index * 50
                                        )
                                        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                                    }
                                }
                                
                                if (pastSchedules.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))
                                    Text(
                                        text = "Irrigation History",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = AppDimensions.paddingMedium())
                                    )
                                    
                                    pastSchedules.forEachIndexed { index, schedule ->
                                        EnhancedScheduleCard(
                                            schedule = schedule,
                                            viewModel = viewModel,
                                            isUpcoming = false,
                                            animationDelay = (index + upcomingSchedules.size) * 50
                                        )
                                        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                                    }
                                }
                            }
                            
                            // Right Column: Requests & Info
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
                            ) {
                                MyRequestsSection(
                                    requests = myRequests,
                                    isLoading = isLoadingRequests,
                                    onRefresh = { viewModel.loadMyRequests() }
                                )
                                
                                InfoCard()
                            }
                        }
                    }
                } else {
                    // Upcoming Irrigation Section
                    if (upcomingSchedules.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = AppDimensions.paddingMedium())) {
                                Text(
                                    text = "Upcoming Irrigation",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = AppDimensions.paddingMedium())
                                )
                            }
                        }
                        
                        itemsIndexed(upcomingSchedules) { index, schedule ->
                            Column(modifier = Modifier.padding(horizontal = AppDimensions.paddingMedium())) {
                                EnhancedScheduleCard(
                                    schedule = schedule,
                                    viewModel = viewModel,
                                    isUpcoming = true,
                                    animationDelay = index * 50
                                )
                                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                            }
                        }
                        
                        // Pagination for upcoming
                        if (upcomingCurrentPage < upcomingTotalPages - 1) {
                            item {
                                Button(
                                    onClick = { viewModel.loadMoreUpcoming() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = AppDimensions.paddingMedium()),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Load More")
                                }
                                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                            }
                        }
                    } else if (currentState is IrrigationUiState.Success) {
                        item {
                            EmptyStateMessage(
                                icon = Icons.Outlined.DateRange,
                                message = "No upcoming irrigation scheduled",
                                description = "Check back later or contact your local irrigation manager"
                            )
                        }
                    }

                    // My Requests Section
                    item {
                        Column(modifier = Modifier.padding(horizontal = AppDimensions.paddingMedium())) {
                            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                            MyRequestsSection(
                                requests = myRequests,
                                isLoading = isLoadingRequests,
                                onRefresh = { viewModel.loadMyRequests() }
                            )
                            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                        }
                    }

                    // Past Irrigation History Section
                    if (pastSchedules.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = AppDimensions.paddingMedium())) {
                                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                                Text(
                                    text = "Irrigation History",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = AppDimensions.paddingMedium())
                                )
                            }
                        }
                        
                        itemsIndexed(pastSchedules) { index, schedule ->
                            Column(modifier = Modifier.padding(horizontal = AppDimensions.paddingMedium())) {
                                EnhancedScheduleCard(
                                    schedule = schedule,
                                    viewModel = viewModel,
                                    isUpcoming = false,
                                    animationDelay = (index + upcomingSchedules.size) * 50
                                )
                                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                            }
                        }
                        
                        // Pagination for past
                        if (pastCurrentPage < pastTotalPages - 1) {
                            item {
                                Button(
                                    onClick = { viewModel.loadMorePast() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = AppDimensions.paddingMedium()),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text("Load More History")
                                }
                                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                            }
                        }
                    }

                    // Info Card
                    item {
                        Column(modifier = Modifier.padding(horizontal = AppDimensions.paddingMedium())) {
                            InfoCard()
                            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                        }
                    }
                }
            }
        }

        // Floating Action Button for Request (moved to bottom-left)
        FloatingActionButton(
            onClick = { showRequestDialog = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(AppDimensions.paddingLarge()),
            containerColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = AppDimensions.paddingSmall())
        ) {
            Row(
                modifier = Modifier.padding(horizontal = AppDimensions.paddingLarge()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Request Irrigation",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                Text(
                    text = "Request",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Request Dialog
        if (showRequestDialog) {
            IrrigationRequestDialog(
                isLoading = isSubmittingRequest,
                errorMessage = requestSubmitError,
                showDatePicker = showDatePicker,
                onShowDatePickerChange = { showDatePicker = it },
                onDismiss = {
                    showRequestDialog = false
                    viewModel.clearRequestMessages()
                },
                onSubmit = { location, date, reason ->
                    viewModel.submitIrrigationRequest(location, date, reason)
                }
            )
        }
    }
}

@Composable
private fun EnhancedIrrigationHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimensions.paddingMedium()),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.paddingSmall()),
        shape = RoundedCornerShape(AppDimensions.paddingLarge())
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
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
                        text = "Irrigation Schedule",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Manage your water resources",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
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
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Retry", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimensions.paddingLarge()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EnhancedScheduleCard(
    schedule: IrrigationSchedule,
    viewModel: IrrigationViewModel,
    isUpcoming: Boolean,
    animationDelay: Int = 0
) {
    val isToday = viewModel.isToday(schedule.date)
    
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.cardElevation()),
        border = if (isToday) BorderStroke(1.dp, Color(0xFFFF9800)) else null
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.paddingMedium())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(AppDimensions.paddingSmall()),
                        color = if (isUpcoming) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp),
                            tint = if (isUpcoming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.width(AppDimensions.paddingMedium()))
                    Column {
                        Text(
                            text = viewModel.formatDate(schedule.date),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = schedule.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = " • ${viewModel.getDaysUntil(schedule.date)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                StatusBadge(
                    status = schedule.status,
                    isToday = isToday,
                    isPast = !isUpcoming
                )
            }

            schedule.notes?.let { notes ->
                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(AppDimensions.paddingSmall())
                ) {
                    Text(
                        text = "Note: $notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(AppDimensions.paddingSmall())
                    )
                }
            }
        }
    }
}

@Composable
private fun NextIrrigationCard(
    schedule: IrrigationSchedule,
    viewModel: IrrigationViewModel
) {
    val isToday = viewModel.isToday(schedule.date)
    val gradientColors = if (isToday) {
        listOf(Color(0xFFFFC107), Color(0xFFFF9800)) // Amber to Orange
    } else {
        listOf(Color(0xFF2196F3), Color(0xFF00BCD4)) // Blue to Cyan
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.paddingSmall())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(gradientColors)
                )
                .padding(AppDimensions.paddingLarge())
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isToday) Icons.Filled.Warning else Icons.Filled.Info,
                            contentDescription = null,
                            modifier = Modifier.size(AppDimensions.iconSizeLarge()),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.paddingMedium()))
                        Column {
                            Text(
                                text = if (isToday) "Irrigation Today!" else "Next Irrigation",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = viewModel.formatDate(schedule.date),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = viewModel.getDaysUntil(schedule.date),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Status: ${schedule.status}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                schedule.notes?.let { notes ->
                    Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium())
                    ) {
                        Column(
                            modifier = Modifier.padding(AppDimensions.paddingMedium())
                        ) {
                            Text(
                                text = "Note: $notes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: String,
    isToday: Boolean,
    isPast: Boolean
) {
    val (backgroundColor, textColor, text) = when {
        isToday -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFFF8F00),
            if (status == "completed") "Today - Completed" else "Today - Scheduled"
        )
        isPast -> when (status) {
            "completed" -> Triple(Color(0xFFE8F5E8), Color(0xFF2E7D32), "Completed")
            "cancelled" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Cancelled")
            else -> Triple(Color(0xFFF5F5F5), Color(0xFF616161), "Not Completed")
        }
        else -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "Upcoming")
    }

    Card(
        shape = RoundedCornerShape(AppDimensions.paddingExtraSmall()),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppDimensions.paddingSmall(), vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isToday) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = textColor
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(AppDimensions.paddingLarge()),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(AppDimensions.iconSizeMedium()),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(AppDimensions.paddingMedium()))
            Column {
                Text(
                    text = "Important Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingExtraSmall())
                ) {
                    InfoPoint("Irrigation schedules are managed by your local agricultural office")
                    InfoPoint("Please prepare your fields according to the scheduled dates")
                    InfoPoint("Check this page regularly for schedule updates")
                }
            }
        }
    }
}

@Composable
private fun InfoPoint(text: String) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IrrigationRequestDialog(
    isLoading: Boolean,
    errorMessage: String?,
    showDatePicker: Boolean,
    onShowDatePickerChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (location: String, date: String, reason: String?) -> Unit
) {
    var selectedLocation by remember { mutableStateOf("") }
    var requestedDate by remember { mutableStateOf("") }
    var displayDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var showLocationDropdown by remember { mutableStateOf(false) }

    val locations = listOf("Sumagui", "Villa Pagasa", "Libertad")
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // Tomorrow
    )

    // Set default date to tomorrow
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        requestedDate = dateFormat.format(calendar.time)
        displayDate = displayFormat.format(calendar.time)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimensions.cornerRadiusLarge()),
            elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.paddingSmall())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimensions.paddingLarge())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimensions.iconSizeLarge()),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(AppDimensions.paddingMedium()))
                    Text(
                        text = "Request Irrigation",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

                // Location Dropdown
                ExposedDropdownMenuBox(
                    expanded = showLocationDropdown,
                    onExpandedChange = { showLocationDropdown = !showLocationDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Location *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLocationDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !isLoading,
                        singleLine = true,
                        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    ExposedDropdownMenu(
                        expanded = showLocationDropdown,
                        onDismissRequest = { showLocationDropdown = false }
                    ) {
                        locations.forEach { location ->
                            DropdownMenuItem(
                                text = { Text(location) },
                                onClick = {
                                    selectedLocation = location
                                    showLocationDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

                // Date Field with Date Picker
                OutlinedTextField(
                    value = displayDate,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Requested Date *") },
                    leadingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { onShowDatePickerChange(true) }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShowDatePickerChange(true) },
                    enabled = false,
                    singleLine = true,
                    shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                
                // Date Picker Dialog
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { onShowDatePickerChange(false) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val calendar = Calendar.getInstance().apply {
                                            timeInMillis = millis
                                        }
                                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val displayFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                                        requestedDate = dateFormat.format(calendar.time)
                                        displayDate = displayFormat.format(calendar.time)
                                    }
                                    onShowDatePickerChange(false)
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { onShowDatePickerChange(false) }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState,
                            colors = DatePickerDefaults.colors(
                                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                                todayContentColor = MaterialTheme.colorScheme.primary,
                                todayDateBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

                // Reason Field
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (Optional)") },
                    leadingIcon = {
                        Icon(Icons.Default.Notes, contentDescription = null)
                    },
                    placeholder = { Text("e.g., Preparation for planting") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium())
                )

                // Error Message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(AppDimensions.paddingMedium()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(AppDimensions.iconSizeMedium())
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (selectedLocation.isNotBlank() && requestedDate.isNotBlank()) {
                                onSubmit(
                                    selectedLocation,
                                    requestedDate,
                                    if (reason.isBlank()) null else reason
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && selectedLocation.isNotBlank() && requestedDate.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(AppDimensions.iconSizeMedium()),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Submit Request")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyRequestsSection(
    requests: List<com.zynt.sumviltadconnect.data.model.IrrigationRequest>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusLarge()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.paddingExtraSmall())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.paddingLarge())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimensions.iconSizeLarge()),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(AppDimensions.paddingMedium()))
                    Text(
                        text = "My Requests",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                IconButton(onClick = onRefresh, enabled = !isLoading) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

            // Content
            if (isLoading) {
                // Loading state
                repeat(2) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppDimensions.paddingSmall()),
                        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(AppDimensions.paddingMedium())) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(20.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .height(16.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            } else if (requests.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimensions.paddingLarge()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                    Text(
                        text = "No requests yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                    Text(
                        text = "Tap the button below to submit an irrigation request",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                // Requests list
                requests.forEach { request ->
                    RequestCard(request = request)
                    Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                }
            }
        }
    }
}

@Composable
private fun RequestCard(request: com.zynt.sumviltadconnect.data.model.IrrigationRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.paddingMedium())
        ) {
            // Status badge and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RequestStatusBadge(status = request.status)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatRequestDate(request.requestedDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                Text(
                    text = request.location,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Reason (if provided)
            request.reason?.let { reason ->
                if (reason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                    Text(
                        text = "Reason: $reason",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // Submitted date
            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
            Text(
                text = "Submitted: ${formatRequestDate(request.createdAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RequestStatusBadge(status: String) {
    val (backgroundColor, textColor, displayText) = when (status.lowercase()) {
        "pending" -> Triple(Color(0xFFFFF3E0), Color(0xFFFF8F00), "Pending")
        "approved" -> Triple(Color(0xFFE8F5E8), Color(0xFF2E7D32), "Approved")
        "rejected" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Rejected")
        else -> Triple(Color(0xFFF5F5F5), Color(0xFF616161), status)
    }

    Card(
        shape = RoundedCornerShape(AppDimensions.paddingExtraSmall()),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = AppDimensions.paddingSmall(), vertical = 4.dp)
        )
    }
}

private fun formatRequestDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
