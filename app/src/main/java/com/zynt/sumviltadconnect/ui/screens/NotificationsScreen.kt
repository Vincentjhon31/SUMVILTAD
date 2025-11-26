package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.zynt.sumviltadconnect.data.model.AppNotification
import com.zynt.sumviltadconnect.ui.theme.AppDimensions
import com.zynt.sumviltadconnect.ui.viewmodel.NotificationsUiState
import com.zynt.sumviltadconnect.ui.viewmodel.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(vm: NotificationsViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()

    // Filter states
    var filterType by remember { mutableStateOf("all") }
    
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            EnhancedNotificationsHeader(
                unreadCount = if (state is NotificationsUiState.Success) {
                    (state as NotificationsUiState.Success).data.count { it.isUnread }
                } else 0,
                onRefresh = { vm.refresh() },
                onMarkAllRead = { vm.markAllAsRead() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val currentState = state) {
                is NotificationsUiState.Loading -> {
                    EnhancedLoadingState()
                }

                is NotificationsUiState.Error -> {
                    EnhancedErrorState(
                        message = currentState.message,
                        onRetry = { vm.refresh() }
                    )
                }

                is NotificationsUiState.Success -> {
                    val data = currentState.data
                    val filteredData = when (filterType) {
                        "unread" -> data.filter { it.isUnread }
                        "info" -> data.filter { it.type == "info" }
                        "success" -> data.filter { it.type == "success" }
                        "warning" -> data.filter { it.type == "warning" }
                        "error" -> data.filter { it.type == "error" }
                        else -> data
                    }

                    SwipeRefresh(
                        state = swipeRefreshState,
                        onRefresh = { vm.refresh() },
                        indicatorPadding = PaddingValues(top = AppDimensions.paddingMedium())
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Filter Chips
                            NotificationsFilterCard(
                                currentFilter = filterType,
                                onFilterSelected = { filterType = it },
                                counts = mapOf(
                                    "all" to data.size,
                                    "unread" to data.count { it.isUnread },
                                    "info" to data.count { it.type == "info" },
                                    "success" to data.count { it.type == "success" },
                                    "warning" to data.count { it.type == "warning" },
                                    "error" to data.count { it.type == "error" }
                                )
                            )

                            if (filteredData.isEmpty()) {
                                EnhancedEmptyState(filterType)
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        start = AppDimensions.paddingMedium(),
                                        end = AppDimensions.paddingMedium(),
                                        bottom = AppDimensions.paddingLarge()
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
                                ) {
                                    // Offline Indicator
                                    if (currentState.offline) {
                                        item {
                                            OfflineIndicator()
                                        }
                                    }

                                    items(filteredData) { notification ->
                                        EnhancedNotificationCard(
                                            notification = notification,
                                            onMarkRead = { vm.markRead(it.id) },
                                            onMarkUnread = { vm.markUnread(it.id) }
                                        )
                                    }

                                    // Load More Button
                                    if (currentState.hasMore && filterType == "all") {
                                        item {
                                            LoadMoreButton(
                                                isLoading = currentState.isLoadingMore,
                                                onClick = { vm.loadMore() }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedNotificationsHeader(
    unreadCount: Int,
    onRefresh: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDimensions.paddingSmall()),
        shape = RoundedCornerShape(
            bottomStart = AppDimensions.cornerRadiusLarge(),
            bottomEnd = AppDimensions.cornerRadiusLarge()
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.cardElevation())
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
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
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                            Surface(
                                color = MaterialTheme.colorScheme.error,
                                shape = CircleShape,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mark all as read") },
                                onClick = {
                                    onMarkAllRead()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.DoneAll, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Refresh") },
                                onClick = {
                                    onRefresh()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsFilterCard(
    currentFilter: String,
    onFilterSelected: (String) -> Unit,
    counts: Map<String, Int>
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDimensions.paddingMedium()),
        contentPadding = PaddingValues(horizontal = AppDimensions.paddingMedium()),
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall())
    ) {
        val filters = listOf(
            "all" to "All",
            "unread" to "Unread",
            "info" to "Info",
            "success" to "Success",
            "warning" to "Warning",
            "error" to "Error"
        )

        items(filters) { (key, label) ->
            val isSelected = currentFilter == key
            val count = counts[key] ?: 0
            
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(key) },
                label = {
                    Text(
                        text = if (key == "unread" && count > 0) "$label ($count)" else label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun EnhancedNotificationCard(
    notification: AppNotification,
    onMarkRead: (AppNotification) -> Unit,
    onMarkUnread: (AppNotification) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isExpanded) 1.02f else 1f, label = "scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isUnread) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (notification.isUnread) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    else Color.Transparent
                )
        ) {
            // Status Strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (isExpanded) 120.dp else 80.dp)
                    .background(
                        when (notification.type) {
                            "success" -> Color(0xFF10B981)
                            "warning" -> Color(0xFFF59E0B)
                            "error" -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
            )

            Column(
                modifier = Modifier
                    .padding(AppDimensions.paddingMedium())
                    .weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
                ) {
                    // Icon
                    Surface(
                        shape = CircleShape,
                        color = when (notification.type) {
                            "success" -> Color(0xFF10B981).copy(alpha = 0.1f)
                            "warning" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                            "error" -> Color(0xFFEF4444).copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (notification.type) {
                                    "success" -> Icons.Default.CheckCircle
                                    "warning" -> Icons.Default.Warning
                                    "error" -> Icons.Default.Error
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = when (notification.type) {
                                    "success" -> Color(0xFF10B981)
                                    "warning" -> Color(0xFFF59E0B)
                                    "error" -> Color(0xFFEF4444)
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = notification.title ?: "Notification",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (notification.isUnread) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            if (notification.isUnread) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = formatNotificationTime(notification.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Expanded Actions
                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.padding(top = AppDimensions.paddingMedium())) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AppDimensions.paddingSmall()),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (notification.isUnread) {
                                TextButton(onClick = { onMarkRead(notification) }) {
                                    Icon(
                                        Icons.Default.Done,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark as Read")
                                }
                            } else {
                                TextButton(onClick = { onMarkUnread(notification) }) {
                                    Icon(
                                        Icons.Default.MarkEmailUnread,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark as Unread")
                                }
                            }
                            
                            notification.link?.let {
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { 
                                        if (notification.isUnread) onMarkRead(notification)
                                        // Handle navigation
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("View Details")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadMoreButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimensions.paddingMedium()),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Text("Load More Notifications")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
        }
    }
}

@Composable
private fun EnhancedLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                "Loading notifications...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EnhancedErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium()),
            modifier = Modifier.padding(AppDimensions.paddingLarge())
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Unable to load notifications",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun EnhancedEmptyState(filterType: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimensions.paddingLarge()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Text(
                "No notifications found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                if (filterType == "all")
                    "You're all caught up! No new notifications."
                else
                    "No ${filterType} notifications found.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun OfflineIndicator() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDimensions.paddingMedium()),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(AppDimensions.cornerRadiusSmall())
    ) {
        Row(
            modifier = Modifier.padding(AppDimensions.paddingSmall()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "You are offline. Showing cached notifications.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun formatNotificationTime(createdAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())

        val cleanDateString = createdAt.replace("Z", "").substringBefore(".")
        val date = inputFormat.parse(cleanDateString) ?: return createdAt

        val now = Date()
        val calendar = Calendar.getInstance()
        calendar.time = now
        val today = calendar.clone() as Calendar
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val yesterday = today.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)

        calendar.time = date
        val notificationDay = calendar.clone() as Calendar
        notificationDay.set(Calendar.HOUR_OF_DAY, 0)
        notificationDay.set(Calendar.MINUTE, 0)
        notificationDay.set(Calendar.SECOND, 0)
        notificationDay.set(Calendar.MILLISECOND, 0)

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        when {
            notificationDay.timeInMillis == today.timeInMillis -> {
                "Today ${timeFormat.format(date)}"
            }
            notificationDay.timeInMillis == yesterday.timeInMillis -> {
                "Yesterday ${timeFormat.format(date)}"
            }
            else -> {
                outputFormat.format(date)
            }
        }
    } catch (_: Exception) {
        createdAt
    }
}
