package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import com.zynt.sumviltadconnect.ui.theme.AppDimensions
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zynt.sumviltadconnect.ui.components.BrandProgressIndicator
import com.zynt.sumviltadconnect.ui.viewmodel.TaskFilter
import com.zynt.sumviltadconnect.ui.viewmodel.TasksUiState
import com.zynt.sumviltadconnect.ui.viewmodel.TasksViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(vm: TasksViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val query by vm.query.collectAsState()
    val filter by vm.filter.collectAsState()
    val refreshing by vm.isRefreshing.collectAsState()
    val notificationMessage by vm.notificationMessage.collectAsState()

    val tasks = (state as? TasksUiState.Success)?.data.orEmpty()
    val pendingCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }

    // Create SnackbarHostState outside of Scaffold
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle notification messages
    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            snackbarHostState.showSnackbar(
                message = notificationMessage!!,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
            vm.clearNotification()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            HeaderSection(
                query = query,
                onQueryChange = vm::setQuery,
                refreshing = refreshing,
                offline = (state as? TasksUiState.Success)?.offline == true
            )
            AnimatedVisibility(state is TasksUiState.Loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            when (state) {
                is TasksUiState.Loading -> SkeletonTaskList()
                is TasksUiState.Error -> ErrorState((state as TasksUiState.Error).message) { vm.refresh() }
                is TasksUiState.Success -> {
                    StatsRow(pending = pendingCount, completed = completedCount, onRefresh = vm::refresh)
                    Spacer(Modifier.height(8.dp))
                    FilterRow(selected = filter, onSelect = vm::setFilter, modifier = Modifier.padding(horizontal = 12.dp))
                    Spacer(Modifier.height(4.dp))
                    if (tasks.isEmpty()) EmptyState() else TaskList(tasks = tasks, onToggle = vm::toggleTask, vm = vm)
                }
            }
            Spacer(Modifier.height(64.dp))
        }
    }
}

@Composable
private fun HeaderSection(query: String, onQueryChange: (String) -> Unit, refreshing: Boolean, offline: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimensions.paddingMedium()),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.cardElevation()),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusLarge())
    ) {
        Box(
            Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Your Tasks", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = if (offline) "Offline cache" else if (refreshing) "Refreshing..." else "Stay organized and on track",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (refreshing) {
                        BrandProgressIndicator(size = 40.dp)
                    } else {
                        Spacer(Modifier.size(40.dp)) // Maintain layout space
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    label = { Text("Search tasks", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatsRow(pending: Int, completed: Int, onRefresh: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(title = "Pending", value = pending, color = Color(0xFFFFA726), modifier = Modifier.weight(1f))
        StatCard(title = "Completed", value = completed, color = Color(0xFF66BB6A), modifier = Modifier.weight(1f))
        RefreshCard(onRefresh = onRefresh, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(title: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.Start) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value.toString(), style = MaterialTheme.typography.titleLarge, color = color)
        }
    }
}

@Composable
private fun RefreshCard(onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(160),
        finishedListener = {
            if (pressed) {
                pressed = false
            }
        }
    )
    Card(
        modifier = modifier
            .scale(scale)
            .clickable {
                if (!pressed) { // Prevent multiple rapid clicks
                    pressed = true
                    onRefresh()
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.Start) {
            Text("Refresh", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun EmptyState() {
    Column(Modifier.fillMaxWidth().padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(12.dp))
        Text("No tasks found", style = MaterialTheme.typography.titleMedium)
        Text("You're all caught up!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text("Failed to load tasks", style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Icon(Icons.Default.Refresh, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Retry") }
    }
}

@Composable
private fun TaskList(tasks: List<com.zynt.sumviltadconnect.data.model.Task>, onToggle: (Int) -> Unit, vm: TasksViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = AppDimensions.paddingSmall())) {
        items(tasks, key = { it.id }) { task ->
            AnimatedTaskItem(task = task, onToggle = { onToggle(task.id) }, vm = vm)
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun AnimatedTaskItem(task: com.zynt.sumviltadconnect.data.model.Task, onToggle: () -> Unit, vm: TasksViewModel) {
    var pressed by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(180))

    // Local state for real-time updates - using actual Task properties
    var localStatus by remember(task.id, task.status) { mutableStateOf(task.status) }
    var localDescription by remember(task.id, task.description) { mutableStateOf(task.description ?: "") }
    var localProgress by remember(task.id) { mutableStateOf(if (task.isCompleted) 100f else task.progress.toFloat()) }
    var localFeedback by remember(task.id) { mutableStateOf(task.feedback ?: "") }

    // Update local state when task changes from server
    LaunchedEffect(task.status) {
        localStatus = task.status
        localProgress = if (task.isCompleted) 100f else when (task.status.lowercase()) {
            "completed" -> 100f
            "in_progress", "in progress" -> task.progress.toFloat()
            else -> task.progress.toFloat()
        }
    }
    LaunchedEffect(task.description) {
        localDescription = task.description ?: ""
    }
    LaunchedEffect(task.feedback) {
        localFeedback = task.feedback ?: ""
    }

    // Status color mapping with real-time progress consideration
    val currentProgress = localProgress.toInt()
    val currentStatus = when {
        currentProgress == 0 -> "pending"
        currentProgress == 100 -> "completed"
        else -> "in_progress"
    }

    val statusColor = when (currentStatus) {
        "completed" -> Color(0xFF4CAF50)  // Green
        "in_progress" -> Color(0xFFFF9800)  // Orange
        "pending" -> Color(0xFF9E9E9E)  // Gray
        else -> MaterialTheme.colorScheme.outline
    }

    ElevatedCard(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(scale)
            .clickable { showDetails = !showDetails },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Main task header
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (currentProgress == 100) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onToggle() }
                )

                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            task.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Status chip with real-time updates
                        Surface(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                currentStatus.replaceFirstChar { it.uppercase() }.replace("_", " "),
                                color = statusColor,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Progress bar with real-time updates
                    if (currentProgress > 0 || currentStatus == "in_progress") {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { currentProgress / 100f },
                                modifier = Modifier.weight(1f).height(6.dp),
                                color = statusColor,
                                trackColor = statusColor.copy(alpha = 0.2f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${currentProgress}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = statusColor
                            )
                        }
                    }

                    // Description preview (removed dueDate since it doesn't exist)
                    task.description?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = if (showDetails) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Expanded details and controls
            AnimatedVisibility(showDetails) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))

                    // Status selector - now updates progress instead
                    Text("Update Status", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("pending", "in_progress", "completed").forEach { status ->
                            FilterChip(
                                selected = currentStatus == status,
                                onClick = {
                                    val newProgress = when (status) {
                                        "pending" -> 0f
                                        "completed" -> 100f
                                        "in_progress" -> if (localProgress == 0f) 50f else localProgress
                                        else -> 0f
                                    }
                                    localProgress = newProgress
                                    vm.updateTaskProgress(task.id, newProgress.toInt())
                                },
                                label = { Text(status.replaceFirstChar { it.uppercase() }.replace("_", " ")) }
                            )
                        }
                    }

                    // Progress slider with real-time updates
                    Spacer(Modifier.height(16.dp))
                    Text("Progress: ${currentProgress}%", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = localProgress,
                        onValueChange = { newValue ->
                            localProgress = newValue
                        },
                        onValueChangeFinished = {
                            // Update server when user finishes dragging
                            vm.updateTaskProgress(task.id, localProgress.toInt())
                        },
                        valueRange = 0f..100f,
                        steps = 19, // 5% increments
                        colors = SliderDefaults.colors(
                            thumbColor = statusColor,
                            activeTrackColor = statusColor
                        )
                    )

                    // Feedback section with real-time updates
                    if (localFeedback.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Feedback", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                localFeedback,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Add feedback button
                    Spacer(Modifier.height(8.dp))
                    var showFeedbackDialog by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { showFeedbackDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (localFeedback.isNotBlank()) "Update Feedback" else "Add Feedback")
                    }

                    // Feedback dialog
                    if (showFeedbackDialog) {
                        TaskFeedbackDialog(
                            currentFeedback = localFeedback,
                            onSave = { feedback ->
                                localFeedback = feedback
                                vm.updateTaskFeedback(task.id, feedback)
                                showFeedbackDialog = false
                            },
                            onDismiss = { showFeedbackDialog = false }
                        )
                    }

                    // Removed "Save All Changes" button since individual updates work perfectly
                    // Progress updates happen automatically via slider
                    // Feedback updates happen via dialog
                    // Status updates happen via filter chips
                }
            }
        }
    }
}

@Composable
private fun TaskFeedbackDialog(
    currentFeedback: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var feedback by remember { mutableStateOf(currentFeedback) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Task Feedback") },
        text = {
            OutlinedTextField(
                value = feedback,
                onValueChange = { feedback = it },
                label = { Text("Add your feedback...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(feedback) },
                enabled = feedback.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SkeletonTaskList(count: Int = 6) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(16.dp))
        repeat(count) {
            SkeletonTaskCard()
        }
    }
}

@Composable
private fun SkeletonTaskCard() {
    ElevatedCard(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            )
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Box(
                    Modifier
                        .height(18.dp)
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
private fun FilterRow(selected: TaskFilter, onSelect: (TaskFilter) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(Modifier.width(8.dp))
        TaskFilter.values().forEach { f ->
            FilterChip(
                selected = f == selected,
                onClick = { onSelect(f) },
                label = { Text(f.name.lowercase().replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}
