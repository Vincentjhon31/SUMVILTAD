package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import com.zynt.sumviltadconnect.ui.theme.AppDimensions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zynt.sumviltadconnect.data.model.IrrigationSchedule
import com.zynt.sumviltadconnect.ui.viewmodel.IrrigationViewModel
import com.zynt.sumviltadconnect.ui.viewmodel.IrrigationUiState
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
                .padding(AppDimensions.paddingMedium()),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium()),
            state = rememberLazyListState()
        ) {
        // Header
        item {
            IrrigationHeader()
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(AppDimensions.paddingMedium()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                        Text(
                            text = currentState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                        Button(
                            onClick = { viewModel.loadIrrigationSchedules() }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }

        // Show content when data is available
        if (currentState is IrrigationUiState.Success || upcomingSchedules.isNotEmpty() || pastSchedules.isNotEmpty()) {
            // Next Irrigation Alert
            nextIrrigation?.let { next ->
                item {
                    NextIrrigationCard(
                        schedule = next,
                        viewModel = viewModel
                    )
                }
            }

            // Upcoming Irrigation Section
            item {
                UpcomingIrrigationSection(
                    schedules = upcomingSchedules,
                    viewModel = viewModel
                )
            }

            // Past Irrigation History Section
            if (pastSchedules.isNotEmpty()) {
                item {
                    PastIrrigationSection(
                        schedules = pastSchedules,
                        viewModel = viewModel
                    )
                }
            }

            // My Requests Section
            item {
                MyRequestsSection(
                    requests = myRequests,
                    isLoading = isLoadingRequests,
                    onRefresh = { viewModel.loadMyRequests() }
                )
            }

            // Info Card
            item {
                InfoCard()
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
private fun IrrigationHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.paddingExtraSmall())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.paddingLarge()),
            horizontalArrangement = Arrangement.Center,
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
                text = "Irrigation Schedule",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
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
private fun UpcomingIrrigationSection(
    schedules: List<IrrigationSchedule>,
    viewModel: IrrigationViewModel
) {
    val upcomingCurrentPage by viewModel.upcomingCurrentPage.collectAsState()
    val upcomingTotalPages by viewModel.upcomingTotalPages.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.paddingExtraSmall())
    ) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF4CAF50).copy(alpha = 0.1f),
                                Color(0xFF009688).copy(alpha = 0.1f)
                            )
                        )
                    )
                    .padding(AppDimensions.paddingLarge())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimensions.iconSizeMedium()),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(AppDimensions.paddingMedium()))
                    Column {
                        Text(
                            text = "Upcoming Irrigation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${schedules.size} scheduled irrigation${if (schedules.size != 1) "s" else ""} for your area",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Content
            if (schedules.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimensions.iconSizeLarge()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimensions.buttonHeight()),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                    Text(
                        text = "No upcoming irrigation scheduled",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                    Text(
                        text = "Check back later or contact your local irrigation manager",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(AppDimensions.paddingLarge()),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
                ) {
                    schedules.forEach { schedule ->
                        IrrigationScheduleItem(
                            schedule = schedule,
                            viewModel = viewModel,
                            isUpcoming = true
                        )
                    }

                    // Pagination control
                    if (upcomingCurrentPage < upcomingTotalPages - 1) {
                        Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                        Button(
                            onClick = { viewModel.loadMoreUpcoming() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(AppDimensions.iconSizeSmall())
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                            Text("Load More (${upcomingCurrentPage + 1}/${upcomingTotalPages})")
                        }
                    } else if (upcomingTotalPages > 1) {
                        Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                        Text(
                            text = "Showing all ${schedules.size} irrigation schedules",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PastIrrigationSection(
    schedules: List<IrrigationSchedule>,
    viewModel: IrrigationViewModel
) {
    val pastCurrentPage by viewModel.pastCurrentPage.collectAsState()
    val pastTotalPages by viewModel.pastTotalPages.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.paddingExtraSmall())
    ) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF607D8B).copy(alpha = 0.1f),
                                Color(0xFF455A64).copy(alpha = 0.1f)
                            )
                        )
                    )
                    .padding(AppDimensions.paddingLarge())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimensions.iconSizeMedium()),
                        tint = Color(0xFF607D8B)
                    )
                    Spacer(modifier = Modifier.width(AppDimensions.paddingMedium()))
                    Column {
                        Text(
                            text = "Irrigation History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Past ${schedules.size} irrigation record${if (schedules.size != 1) "s" else ""} for your area",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier.padding(AppDimensions.paddingLarge()),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
            ) {
                schedules.forEach { schedule ->
                    IrrigationScheduleItem(
                        schedule = schedule,
                        viewModel = viewModel,
                        isUpcoming = false
                    )
                }

                // Pagination control
                if (pastCurrentPage < pastTotalPages - 1) {
                    Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                    Button(
                        onClick = { viewModel.loadMorePast() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF607D8B)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(AppDimensions.iconSizeSmall())
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                        Text("Load More (${pastCurrentPage + 1}/${pastTotalPages})")
                    }
                } else if (pastTotalPages > 1) {
                    Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                    Text(
                        text = "Showing all ${schedules.size} past irrigation records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun IrrigationScheduleItem(
    schedule: IrrigationSchedule,
    viewModel: IrrigationViewModel,
    isUpcoming: Boolean
) {
    val isToday = viewModel.isToday(schedule.date)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isToday) BorderStroke(AppDimensions.paddingExtraSmall(), Color(0xFFFF9800)) else null
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
                    Card(
                        shape = RoundedCornerShape(AppDimensions.paddingSmall()),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUpcoming) Color(0xFF2196F3).copy(alpha = 0.1f) else Color(0xFF607D8B).copy(alpha = 0.1f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier
                                .size(AppDimensions.iconSizeLarge())
                                .padding(AppDimensions.paddingSmall()),
                            tint = if (isUpcoming) Color(0xFF2196F3) else Color(0xFF607D8B)
                        )
                    }
                    Spacer(modifier = Modifier.width(AppDimensions.paddingMedium()))
                    Column {
                        Text(
                            text = viewModel.formatDate(schedule.date),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(AppDimensions.paddingMedium()),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.paddingExtraSmall()))
                            Text(
                                text = schedule.location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = " • ${viewModel.getDaysUntil(schedule.date)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(AppDimensions.paddingSmall())
                ) {
                    Text(
                        text = "Note: $notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(AppDimensions.paddingSmall())
                    )
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
        border = BorderStroke(AppDimensions.paddingExtraSmall(), textColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppDimensions.paddingSmall(), vertical = AppDimensions.paddingExtraSmall()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isToday) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(AppDimensions.paddingMedium()),
                    tint = textColor
                )
                Spacer(modifier = Modifier.width(AppDimensions.paddingExtraSmall()))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
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
        border = BorderStroke(AppDimensions.paddingExtraSmall(), MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
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
