package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.zynt.sumviltadconnect.data.model.Event
import com.zynt.sumviltadconnect.ui.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import androidx.navigation.NavController
import com.zynt.sumviltadconnect.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun EventsScreen(navController: NavController? = null, vm: EventsViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Animation durations following the 250ms guideline
    val fastAnimation = 150.milliseconds
    val standardAnimation = 200.milliseconds
    val slowAnimation = 250.milliseconds

    Box(modifier = Modifier.fillMaxSize()) {
        when (val currentState = state) {
            is EventsUiState.Loading -> {
                EventsLoadingSkeleton()
            }
            is EventsUiState.Error -> {
                EventsErrorState(
                    message = currentState.message,
                    onRetry = { vm.refresh() }
                )
            }
            is EventsUiState.Success -> {
                EventsSuccessContent(
                    state = currentState,
                    onRefresh = { vm.refresh() },
                    onFilterChange = { vm.setFilter(it) },
                    onSearchChange = { vm.setSearchQuery(it) },
                    onSortChange = { vm.setSortBy(it) },
                    onParticipate = { eventId, status ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.participateInEvent(eventId, status)
                    },
                    onClearNotification = { vm.clearNotification() },
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun EventsLoadingSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header skeleton
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmerEffect()
                )
            }
        }

        // Filter skeleton
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmerEffect()
                )
            }
        }

        // Event cards skeleton
        items(5) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmerEffect()
                )
            }
        }
    }
}

@Composable
private fun EventsErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.EventBusy,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Failed to Load Events",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.animateContentSize()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
private fun EventsSuccessContent(
    state: EventsUiState.Success,
    onRefresh: () -> Unit,
    onFilterChange: (EventFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onSortChange: (EventSort) -> Unit,
    onParticipate: (Int, String) -> Unit,
    onClearNotification: () -> Unit,
    navController: NavController?
) {
    val swipeRefreshState = rememberSwipeRefreshState(state.isRefreshing)

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with gradient and stats
        EventsHeader(
            totalEvents = state.events.size,
            upcomingCount = state.events.count { event ->
                EventsViewModel().getEventStatus(event) == EventStatus.UPCOMING
            },
            ongoingCount = state.events.count { event ->
                EventsViewModel().getEventStatus(event) == EventStatus.ONGOING
            }
        )

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = onRefresh
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search and filters
                item {
                    EventsFiltersCard(
                        searchQuery = state.searchQuery,
                        onSearchChange = onSearchChange,
                        filterType = state.filterType,
                        onFilterChange = onFilterChange,
                        sortBy = state.sortBy,
                        onSortChange = onSortChange
                    )
                }

                // Notification
                state.notification?.let { notification ->
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                animationSpec = tween(200),
                                initialOffsetY = { -it }
                            ) + fadeIn(animationSpec = tween(200)),
                            exit = slideOutVertically(
                                animationSpec = tween(200),
                                targetOffsetY = { -it }
                            ) + fadeOut(animationSpec = tween(200))
                        ) {
                            NotificationCard(
                                message = notification,
                                onDismiss = onClearNotification
                            )
                        }
                    }
                }

                // Events list header
                item {
                    Text(
                        text = "${state.filteredEvents.size} ${state.filterType.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Events
                if (state.filteredEvents.isEmpty()) {
                    item {
                        EmptyEventsState(
                            hasSearch = state.searchQuery.isNotBlank(),
                            onClearFilters = {
                                onSearchChange("")
                                onFilterChange(EventFilter.ALL)
                            }
                        )
                    }
                } else {
                    itemsIndexed(
                        items = state.filteredEvents,
                        key = { _, event -> event.id }
                    ) { index, event ->
                        val animationDelay = (index * 50).coerceAtMost(200)

                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                animationSpec = tween(
                                    durationMillis = 250,
                                    delayMillis = animationDelay,
                                    easing = FastOutSlowInEasing
                                ),
                                initialOffsetY = { it / 2 }
                            ) + fadeIn(
                                animationSpec = tween(
                                    durationMillis = 250,
                                    delayMillis = animationDelay
                                )
                            )
                        ) {
                            EventCard(
                                event = event,
                                participationState = state.participationStates[event.id],
                                onParticipate = onParticipate,
                                viewModel = EventsViewModel(),
                                navController = navController
                            )
                        }
                    }
                }

                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EventsHeader(
    totalEvents: Int,
    upcomingCount: Int,
    ongoingCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF10B981), // Emerald
                            Color(0xFF14B8A6)  // Teal
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Farming Events",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Stay updated with workshops, training sessions, and community gatherings",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Total",
                        value = totalEvents.toString()
                    )
                    StatCard(
                        label = "Upcoming",
                        value = upcomingCount.toString()
                    )
                    StatCard(
                        label = "Live",
                        value = ongoingCount.toString()
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.size(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun EventsFiltersCard(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filterType: EventFilter,
    onFilterChange: (EventFilter) -> Unit,
    sortBy: EventSort,
    onSortChange: (EventSort) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search events by title, description, location...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter chips
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(EventFilter.values()) { filter ->
                        FilterChip(
                            selected = filterType == filter,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter.displayName) },
                            modifier = Modifier.animateContentSize(
                                animationSpec = tween(150)
                            )
                        )
                    }
                }

                // Sort button
                Box {
                    OutlinedIconButton(
                        onClick = { showSortMenu = !showSortMenu }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort"
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        EventSort.values().forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.displayName) },
                                onClick = {
                                    onSortChange(sort)
                                    showSortMenu = false
                                },
                                leadingIcon = if (sortBy == sort) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun EmptyEventsState(
    hasSearch: Boolean,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (hasSearch) Icons.Outlined.SearchOff else Icons.Outlined.EventNote,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasSearch) "No events match your search" else "No events available",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasSearch)
                "Try adjusting your search or filter to find what you're looking for."
            else
                "There are no events scheduled at this time. Check back later for new events.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (hasSearch) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onClearFilters) {
                Text("Clear filters")
            }
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    participationState: ParticipationState?,
    onParticipate: (Int, String) -> Unit,
    viewModel: EventsViewModel,
    navController: NavController?
) {
    val status = viewModel.getEventStatus(event)
    val isHappeningSoon = viewModel.isHappeningSoon(event)
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController?.navigate("eventDetails/${event.id}")
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                EventStatus.ONGOING -> MaterialTheme.colorScheme.secondaryContainer
                EventStatus.UPCOMING -> if (isHappeningSoon)
                    MaterialTheme.colorScheme.tertiaryContainer
                else
                    MaterialTheme.colorScheme.surface
                EventStatus.PAST -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Box {
            // Calendar date indicator
            Card(
                modifier = Modifier
                    .padding(12.dp)
                    .size(60.dp)
                    .align(Alignment.TopEnd),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val eventDate = parseEventDate(event.eventDate)
                    if (eventDate != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = SimpleDateFormat("MMM", Locale.getDefault()).format(eventDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = SimpleDateFormat("dd", Locale.getDefault()).format(eventDate),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(end = 76.dp) // Account for calendar
            ) {
                // Title and status badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status badges
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { StatusBadge(status = status, isHappeningSoon = isHappeningSoon) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Event details
                EventDetailRow(
                    icon = Icons.Default.Schedule,
                    text = formatEventDateTime(event.eventDate)
                )

                event.location?.let { location ->
                    Spacer(modifier = Modifier.height(4.dp))
                    EventDetailRow(icon = Icons.Default.LocationOn, text = location)
                }

                event.description?.let { description ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.category ?: "General Event",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Participation button (only for upcoming/ongoing events)
                        if (status == EventStatus.UPCOMING || status == EventStatus.ONGOING) {
                            val currentStatus = participationState?.status ?: event.userParticipation
                            AnimatedParticipationButton(
                                currentStatus = currentStatus,
                                isLoading = participationState?.isLoading == true,
                                onParticipate = { newStatus -> onParticipate(event.id, newStatus) }
                            )
                        }

                        // View details button
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                navController?.navigate("eventDetails/${event.id}")
                            },
                            modifier = Modifier.animateContentSize()
                        ) { Text("View Details") }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: EventStatus,
    isHappeningSoon: Boolean
) {
    val (backgroundColor, textColor, text, icon) = when {
        status == EventStatus.ONGOING -> Tuple4(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Happening Now",
            Icons.Default.Circle
        )
        status == EventStatus.UPCOMING && isHappeningSoon -> Tuple4(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Happening Soon",
            Icons.Default.Circle
        )
        status == EventStatus.UPCOMING -> Tuple4(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Upcoming",
            Icons.Default.Circle
        )
        else -> Tuple4(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Past",
            Icons.Default.Circle
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(8.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
private fun EventDetailRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AnimatedParticipationButton(
    currentStatus: String?,
    isLoading: Boolean,
    onParticipate: (String) -> Unit
) {
    val isAttending = currentStatus == "attending"

    val backgroundColor by animateColorAsState(
        targetValue = if (isAttending)
            MaterialTheme.colorScheme.secondary
        else
            MaterialTheme.colorScheme.primary,
        animationSpec = tween(200)
    )

    val textColor by animateColorAsState(
        targetValue = if (isAttending)
            MaterialTheme.colorScheme.onSecondary
        else
            MaterialTheme.colorScheme.onPrimary,
        animationSpec = tween(200)
    )

    Button(
        onClick = {
            val newStatus = if (isAttending) "not_attending" else "attending"
            onParticipate(newStatus)
        },
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        modifier = Modifier.animateContentSize(
            animationSpec = tween(150)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = textColor
            )
        } else {
            if (isAttending) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Attending")
            } else {
                Text("Attend")
            }
        }
    }
}

// Utility functions
private fun parseEventDate(dateString: String?): Date? {
    if (dateString == null) return null
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    return try {
        format.parse(dateString)
    } catch (e: Exception) {
        try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}

private fun formatEventDateTime(dateString: String?): String {
    val date = parseEventDate(dateString) ?: return "Date not specified"
    val format = SimpleDateFormat("EEE, MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    return format.format(date)
}

// Shimmer effect for loading state
@Composable
private fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    return background(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
    )
}

// Data class for multiple return values
private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
