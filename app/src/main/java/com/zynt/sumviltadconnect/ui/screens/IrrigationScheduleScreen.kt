package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zynt.sumviltadconnect.data.model.IrrigationSchedule
import com.zynt.sumviltadconnect.ui.viewmodel.IrrigationViewModel
import com.zynt.sumviltadconnect.ui.viewmodel.IrrigationUiState

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

    // Show error snackbar
    val currentState = state
    if (currentState is IrrigationUiState.Error) {
        LaunchedEffect(currentState.message) {
            // You can add snackbar host here if needed
        }
    }

    LazyColumn(
        modifier = modifier
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

            // Info Card
            item {
                InfoCard()
            }
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
