package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.zynt.sumviltadconnect.ui.theme.AppDimensions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zynt.sumviltadconnect.data.model.AppNotification
import com.zynt.sumviltadconnect.ui.viewmodel.NotificationsUiState
import com.zynt.sumviltadconnect.ui.viewmodel.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(vm: NotificationsViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    // Filter states
    var filterType by remember { mutableStateOf("all") }
    var showActions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar with Actions
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall())
                ) {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (state is NotificationsUiState.Success) {
                        val data = (state as NotificationsUiState.Success).data
                        val unreadCount = data.count { it.isUnread }
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            },
            actions = {
                // Manual refresh button
                IconButton(onClick = { vm.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }

                IconButton(onClick = { showActions = !showActions }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }

                DropdownMenu(
                    expanded = showActions,
                    onDismissRequest = { showActions = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Mark all as read") },
                        onClick = {
                            vm.markAllAsRead()
                            showActions = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.DoneAll, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Refresh") },
                        onClick = {
                            vm.refresh()
                            showActions = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                        }
                    )
                }
            }
        )

        when (val currentState = state) {
            is NotificationsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Loading notifications...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is NotificationsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(AppDimensions.buttonHeight())
                        )
                        Text(
                            "Error loading notifications",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            currentState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { vm.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                            Text("Try Again")
                        }
                    }
                }
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

                Column {
                    // Offline indicator if applicable
                    if (currentState.offline) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondaryContainer
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
                                    modifier = Modifier.size(AppDimensions.paddingMedium())
                                )
                                Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                                Text(
                                    "Showing cached notifications",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Filter Chips
                    LazyRow(
                        modifier = Modifier.padding(horizontal = AppDimensions.paddingMedium(), vertical = AppDimensions.paddingSmall()),
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
                            FilterChip(
                                selected = filterType == key,
                                onClick = { filterType = key },
                                label = {
                                    Text(
                                        text = if (key == "unread") {
                                            val unreadCount = data.count { it.isUnread }
                                            "$label ($unreadCount)"
                                        } else {
                                            label
                                        }
                                    )
                                }
                            )
                        }
                    }

                    if (filteredData.isEmpty()) {
                        // Empty State
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
                            ) {
                                Icon(
                                    Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(AppDimensions.fabSize())
                                )
                                Text(
                                    "No notifications",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    if (filterType == "all")
                                        "You don't have any notifications yet."
                                    else
                                        "No ${filterType} notifications found.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Notifications List
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(AppDimensions.paddingMedium()),
                            verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
                        ) {
                            items(filteredData) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onMarkRead = { vm.markRead(it.id) },
                                    onMarkUnread = { vm.markUnread(it.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: AppNotification,
    onMarkRead: (AppNotification) -> Unit,
    onMarkUnread: (AppNotification) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppDimensions.paddingExtraSmall()
        ),
        border = null
    ) {
        Row(
            modifier = Modifier
                .padding(AppDimensions.paddingMedium())
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
        ) {
            // Notification Type Icon (matching Laravel web design)
            Box(
                modifier = Modifier
                    .size(AppDimensions.iconSizeLarge())
                    .clip(CircleShape)
                    .background(
                        when (notification.type) {
                            "success" -> Color(0xFF10B981).copy(alpha = 0.1f)
                            "warning" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                            "error" -> Color(0xFFEF4444).copy(alpha = 0.1f)
                            else -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
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
                        else -> Color(0xFF3B82F6)
                    },
                    modifier = Modifier.size(AppDimensions.iconSizeSmall())
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingExtraSmall())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingExtraSmall())
                    ) {
                        // Title
                        Text(
                            text = notification.title ?: "Notification",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (notification.isUnread) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Message
                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Unread indicator
                    if (notification.isUnread) {
                        Box(
                            modifier = Modifier
                                .size(AppDimensions.paddingSmall())
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                // Time and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time
                    Text(
                        text = formatNotificationTime(notification.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingExtraSmall())
                    ) {
                        if (notification.isUnread) {
                            FilledTonalButton(
                                onClick = { onMarkRead(notification) },
                                modifier = Modifier.height(AppDimensions.paddingExtraLarge()),
                                contentPadding = PaddingValues(horizontal = AppDimensions.paddingMedium(), vertical = AppDimensions.paddingExtraSmall())
                            ) {
                                Icon(
                                    Icons.Default.Done,
                                    contentDescription = "Mark as read",
                                    modifier = Modifier.size(AppDimensions.paddingMedium())
                                )
                                Spacer(modifier = Modifier.width(AppDimensions.paddingExtraSmall()))
                                Text(
                                    "Mark read",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onMarkUnread(notification) },
                                modifier = Modifier.height(AppDimensions.paddingExtraLarge()),
                                contentPadding = PaddingValues(horizontal = AppDimensions.paddingMedium(), vertical = AppDimensions.paddingExtraSmall())
                            ) {
                                Icon(
                                    Icons.Default.MarkEmailUnread,
                                    contentDescription = "Mark as unread",
                                    modifier = Modifier.size(AppDimensions.paddingMedium())
                                )
                                Spacer(modifier = Modifier.width(AppDimensions.paddingExtraSmall()))
                                Text(
                                    "Mark unread",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                // Expandable content
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AppDimensions.paddingSmall()),
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall())
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Additional notification details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingExtraSmall())) {
                                Text(
                                    "Type",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingExtraSmall()),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(AppDimensions.paddingSmall())
                                            .clip(CircleShape)
                                            .background(
                                                when (notification.type) {
                                                    "success" -> Color(0xFF10B981)
                                                    "warning" -> Color(0xFFF59E0B)
                                                    "error" -> Color(0xFFEF4444)
                                                    else -> Color(0xFF3B82F6)
                                                }
                                            )
                                    )
                                    Text(
                                        notification.type.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingExtraSmall()),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    "Status",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                                    color = if (notification.isRead) {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer
                                    }
                                ) {
                                    Text(
                                        if (notification.isRead) "Read" else "Unread",
                                        modifier = Modifier.padding(horizontal = AppDimensions.paddingSmall(), vertical = AppDimensions.paddingExtraSmall()),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (notification.isRead) {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        }
                                    )
                                }
                            }
                        }

                        // Link button if available
                        notification.link?.let {
                            Button(
                                onClick = {
                                    // Handle link navigation here
                                    if (notification.isUnread) {
                                        onMarkRead(notification)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(AppDimensions.paddingMedium())
                                )
                                Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                                Text("View Details")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun formatNotificationTime(createdAt: String): String {
    return try {
        // Use SimpleDateFormat for API compatibility (works on API 24+)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())

        // Parse the date string (remove Z if present)
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
