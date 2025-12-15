package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.zynt.sumviltadconnect.data.model.Task
import com.zynt.sumviltadconnect.ui.theme.AppDimensions
import com.zynt.sumviltadconnect.ui.viewmodel.TaskFilter
import com.zynt.sumviltadconnect.ui.viewmodel.TasksUiState
import com.zynt.sumviltadconnect.ui.viewmodel.TasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(vm: TasksViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val query by vm.query.collectAsState()
    val filter by vm.filter.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()
    val notificationMessage by vm.notificationMessage.collectAsState()

    val tasks = (state as? TasksUiState.Success)?.data.orEmpty()
    val pendingCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }

    val snackbarHostState = remember { SnackbarHostState() }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { vm.refresh() },
                indicatorPadding = PaddingValues(top = padding.calculateTopPadding())
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + AppDimensions.paddingSmall(),
                        bottom = padding.calculateBottomPadding() + AppDimensions.paddingMedium(),
                        start = AppDimensions.paddingMedium(),
                        end = AppDimensions.paddingMedium()
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
                ) {
                    // Header Section
                    item {
                        EnhancedTasksHeader(
                            pendingCount = pendingCount,
                            completedCount = completedCount,
                            offline = (state as? TasksUiState.Success)?.offline == true
                        )
                    }

                    // Filter & Search Section
                    item {
                        TasksFilterCard(
                            query = query,
                            onQueryChange = vm::setQuery,
                            filter = filter,
                            onFilterChange = vm::setFilter
                        )
                    }

                    // Content
                    when (state) {
                        is TasksUiState.Loading -> {
                            item { EnhancedLoadingSkeleton() }
                        }
                        is TasksUiState.Error -> {
                            item { 
                                EnhancedErrorState(
                                    message = (state as TasksUiState.Error).message,
                                    onRetry = { vm.refresh() }
                                )
                            }
                        }
                        is TasksUiState.Success -> {
                            if (tasks.isEmpty()) {
                                item { EnhancedEmptyState() }
                            } else {
                                items(tasks, key = { it.id }) { task ->
                                    EnhancedTaskCard(
                                        task = task,
                                        onToggle = { vm.toggleTask(task.id) },
                                        vm = vm
                                    )
                                }
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(AppDimensions.paddingLarge())) }
                }
            }
        }
    }
}

@Composable
private fun EnhancedTasksHeader(
    pendingCount: Int,
    completedCount: Int,
    offline: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Tasks",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (offline) "Offline Mode" else "Manage your farming activities",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
                ) {
                    TaskStatChip(
                        label = "Pending",
                        value = pendingCount.toString(),
                        icon = Icons.Default.PendingActions,
                        color = Color(0xFFFFA726),
                        modifier = Modifier.weight(1f)
                    )
                    TaskStatChip(
                        label = "Completed",
                        value = completedCount.toString(),
                        icon = Icons.Default.TaskAlt,
                        color = Color(0xFF66BB6A),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskStatChip(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium())
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TasksFilterCard(
    query: String,
    onQueryChange: (String) -> Unit,
    filter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.paddingMedium())) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search tasks...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
            
            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFilter.values().forEach { f ->
                    FilterChip(
                        selected = f == filter,
                        onClick = { onFilterChange(f) },
                        label = { Text(f.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedTaskCard(
    task: Task,
    onToggle: () -> Unit,
    vm: TasksViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (expanded) 1.02f else 1f, tween(300))
    
    // Local state logic preserved
    var localStatus by remember(task.id, task.status) { mutableStateOf(task.status) }
    var localDescription by remember(task.id, task.description) { mutableStateOf(task.description ?: "") }
    var localProgress by remember(task.id) { mutableStateOf(if (task.isCompleted) 100f else task.progress.toFloat()) }
    var localFeedback by remember(task.id) { mutableStateOf(task.feedback ?: "") }

    LaunchedEffect(task.status) {
        localStatus = task.status
        localProgress = if (task.isCompleted) 100f else when (task.status.lowercase()) {
            "completed" -> 100f
            "in_progress", "in progress" -> task.progress.toFloat()
            else -> task.progress.toFloat()
        }
    }
    LaunchedEffect(task.description) { localDescription = task.description ?: "" }
    LaunchedEffect(task.feedback) { localFeedback = task.feedback ?: "" }

    val currentProgress = localProgress.toInt()
    val currentStatus = when {
        currentProgress == 0 -> "pending"
        currentProgress == 100 -> "completed"
        else -> "in_progress"
    }

    val statusColor = when (currentStatus) {
        "completed" -> Color(0xFF4CAF50)
        "in_progress" -> Color(0xFFFF9800)
        "pending" -> Color(0xFF9E9E9E)
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.paddingMedium())) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (currentProgress == 100) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onToggle() }
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (currentProgress > 0 && currentProgress < 100) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { currentProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = statusColor,
                            trackColor = statusColor.copy(alpha = 0.2f),
                        )
                    }
                }
                
                StatusBadge(status = currentStatus, color = statusColor)
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                    
                    if (localDescription.isNotBlank()) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = localDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                    }

                    // Progress Control
                    Text(
                        text = "Progress: $currentProgress%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Slider(
                        value = localProgress,
                        onValueChange = { localProgress = it },
                        onValueChangeFinished = { vm.updateTaskProgress(task.id, localProgress.toInt()) },
                        valueRange = 0f..100f,
                        steps = 19,
                        colors = SliderDefaults.colors(thumbColor = statusColor, activeTrackColor = statusColor)
                    )

                    // Status Chips
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
                                label = { Text(status.replace("_", " ").capitalize()) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = statusColor.copy(alpha = 0.1f),
                                    selectedLabelColor = statusColor
                                )
                            )
                        }
                    }

                    // Feedback Section
                    Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                    var showFeedbackDialog by remember { mutableStateOf(false) }
                    
                    if (localFeedback.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Feedback", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(localFeedback, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    OutlinedButton(
                        onClick = { showFeedbackDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (localFeedback.isNotBlank()) "Update Feedback" else "Add Feedback")
                    }

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
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status.replace("_", " ").capitalize(),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EnhancedLoadingSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())) {
        repeat(5) {
            Card(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {}
        }
    }
}

@Composable
private fun EnhancedEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AssignmentTurnedIn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tasks found",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "You're all caught up!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EnhancedErrorState(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium())
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.paddingLarge()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Failed to load tasks",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Retry")
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

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
}
