package com.zynt.sumviltadconnect.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.model.Task
import com.zynt.sumviltadconnect.data.model.UpdateTaskRequest
import com.zynt.sumviltadconnect.data.repository.FarmerRepository
import com.zynt.sumviltadconnect.data.local.TaskCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar

sealed class TasksUiState {
    object Loading: TasksUiState()
    data class Success(val data: List<Task>, val offline: Boolean = false): TasksUiState()
    data class Error(val message: String): TasksUiState()
}

enum class TaskFilter { ALL, PENDING, COMPLETED }

class TasksViewModel(app: Application): AndroidViewModel(app) {
    private val repo = FarmerRepository(taskCache = TaskCache(app))

    private val _state = MutableStateFlow<TasksUiState>(TasksUiState.Loading)
    val state: StateFlow<TasksUiState> = _state

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    private val _offline = MutableStateFlow(false)
    private val _query = MutableStateFlow("")
    private val _filter = MutableStateFlow(TaskFilter.ALL)
    private val _isRefreshing = MutableStateFlow(false)
    private val _notificationMessage = MutableStateFlow<String?>(null)

    val query: StateFlow<String> = _query.asStateFlow()
    val filter: StateFlow<TaskFilter> = _filter.asStateFlow()
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    private val dateParsers = listOf(
        "yyyy-MM-dd",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    ).map { pattern -> SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    init {
        refresh()
        // Recompute UI state whenever underlying inputs change
        viewModelScope.launch {
            combine(_allTasks, _offline, _query, _filter) { tasks, offline, q, f ->
                val filtered = tasks.filter { task ->
                    val matchQuery = q.isBlank() || task.title.contains(q, true) || (task.description?.contains(q, true) == true)
                    val matchFilter = when (f) {
                        TaskFilter.ALL -> true
                        TaskFilter.PENDING -> task.status != "completed" && (task.progress ?: 0) < 100
                        TaskFilter.COMPLETED -> task.status == "completed" || (task.progress ?: 0) >= 100
                    }
                    matchQuery && matchFilter
                }.sortedWith(compareBy<Task> { it.status == "completed" || (it.progress ?: 0) >= 100 }.thenBy { it.id })
                TasksUiState.Success(filtered, offline)
            }.collect { _state.value = it }
        }
    }

    private fun parseDateMillis(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        for (parser in dateParsers) {
            try { return parser.parse(raw)?.time } catch (_: Exception) {}
            // try substring first 10 chars (date only)
            if (raw.length >= 10) {
                try { return parser.parse(raw.substring(0,10))?.time } catch (_: Exception) {}
            }
        }
        return null
    }

    fun setQuery(q: String) { _query.value = q }
    fun setFilter(f: TaskFilter) { _filter.value = f }

    fun refresh() {
        _isRefreshing.value = true
        // Only show skeleton loading if we have no data
        if (_allTasks.value.isEmpty()) {
            _state.value = TasksUiState.Loading
        }
        viewModelScope.launch {
            try {
                // Get tasks from repository
                val (tasks, offline) = repo.tasks()
                _offline.value = offline
                _allTasks.value = tasks
                android.util.Log.d("TasksViewModel", "Received ${tasks.size} tasks from repository")
            } catch (e: Exception) {
                android.util.Log.e("TasksViewModel", "Error fetching tasks: ${e.message}", e)
                _allTasks.value = emptyList()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleTask(id: Int) {
        // Toggle task by updating progress: 0% = pending, 100% = completed
        val current = _allTasks.value
        val idx = current.indexOfFirst { it.id == id }
        if (idx != -1) {
            val currentTask = current[idx]
            val newProgress = if (currentTask.status == "completed" || (currentTask.progress ?: 0) >= 100) 0 else 100
            updateTaskProgress(id, newProgress)
        }
    }

    fun updateTaskStatus(id: Int, status: String) {
        // Status updates are handled server-side based on progress
        // Just update progress to trigger correct status
        val progressValue = when (status) {
            "pending" -> 0
            "completed" -> 100
            "in_progress" -> 50 // Default middle value
            else -> 0
        }
        updateTaskProgress(id, progressValue)
    }

    fun updateTaskProgress(id: Int, progress: Int) {
        // optimistic update with server-side status calculation
        val current = _allTasks.value
        val idx = current.indexOfFirst { it.id == id }
        if (idx != -1) {
            val updated = current[idx].copy(
                progress = progress,
                // Let server determine status based on progress
                status = when {
                    progress == 0 -> "pending"
                    progress == 100 -> "completed"
                    else -> "in_progress"
                }
            )
            _allTasks.value = current.toMutableList().also { it[idx] = updated }
        }
        viewModelScope.launch {
            try {
                // Send proper UpdateTaskRequest to backend
                val updated = repo.updateTask(id, UpdateTaskRequest(
                    progress = progress,
                    status = when {
                        progress == 0 -> "pending"
                        progress == 100 -> "completed"
                        else -> "in_progress"
                    }
                ))
                val list = _allTasks.value.toMutableList()
                val i = list.indexOfFirst { it.id == id }
                if (i != -1) {
                    list[i] = updated
                    _allTasks.value = list
                }
                _notificationMessage.value = "Progress updated successfully"
                // Clear notification after 3 seconds
                kotlinx.coroutines.delay(3000)
                _notificationMessage.value = null
            } catch (e: Exception) {
                // Revert optimistic update on error
                if (idx != -1) {
                    val revertedList = _allTasks.value.toMutableList()
                    val originalTask = current[idx]
                    revertedList[idx] = originalTask
                    _allTasks.value = revertedList
                }
                _notificationMessage.value = "Failed to update progress: ${e.message}"
                // Clear error notification after 5 seconds
                kotlinx.coroutines.delay(5000)
                _notificationMessage.value = null
            }
        }
    }

    fun updateTaskFeedback(id: Int, feedback: String) {
        // Show immediate optimistic update
        val current = _allTasks.value
        val idx = current.indexOfFirst { it.id == id }
        if (idx != -1) {
            val updated = current[idx].copy(feedback = feedback)
            _allTasks.value = current.toMutableList().also { it[idx] = updated }
        }

        viewModelScope.launch {
            try {
                val updated = repo.updateTask(id, UpdateTaskRequest(
                    feedback = feedback
                ))
                val list = _allTasks.value.toMutableList()
                val i = list.indexOfFirst { it.id == id }
                if (i != -1) {
                    list[i] = updated
                    _allTasks.value = list
                }
                _notificationMessage.value = "Feedback updated successfully"
                // Clear notification after 3 seconds
                kotlinx.coroutines.delay(3000)
                _notificationMessage.value = null
            } catch (e: Exception) {
                // Revert optimistic update on error
                if (idx != -1) {
                    val revertedList = _allTasks.value.toMutableList()
                    val originalTask = current[idx]
                    revertedList[idx] = originalTask
                    _allTasks.value = revertedList
                }
                _notificationMessage.value = "Failed to update feedback: ${e.message}"
                // Clear error notification after 5 seconds
                kotlinx.coroutines.delay(5000)
                _notificationMessage.value = null
            }
        }
    }

    fun saveAllTaskChanges(id: Int, status: String, progress: Int, feedback: String) {
        // Show immediate optimistic update
        val current = _allTasks.value
        val idx = current.indexOfFirst { it.id == id }
        if (idx != -1) {
            val updated = current[idx].copy(
                progress = progress,
                feedback = feedback,
                // Status is calculated server-side based on progress
                status = when {
                    progress == 0 -> "pending"
                    progress == 100 -> "completed"
                    else -> "in_progress"
                }
            )
            _allTasks.value = current.toMutableList().also { it[idx] = updated }
        }

        viewModelScope.launch {
            try {
                // Send proper UpdateTaskRequest to backend
                val updated = repo.updateTask(id, UpdateTaskRequest(
                    progress = progress,
                    feedback = feedback,
                    status = when {
                        progress == 0 -> "pending"
                        progress == 100 -> "completed"
                        else -> "in_progress"
                    }
                ))
                val list = _allTasks.value.toMutableList()
                val i = list.indexOfFirst { it.id == id }
                if (i != -1) {
                    list[i] = updated
                    _allTasks.value = list
                }
                _notificationMessage.value = "Task saved successfully"
                // Auto refresh after save
                refresh()
                // Clear notification after 3 seconds
                kotlinx.coroutines.delay(3000)
                _notificationMessage.value = null
            } catch (e: Exception) {
                // Revert optimistic update on error
                if (idx != -1) {
                    val revertedList = _allTasks.value.toMutableList()
                    val originalTask = current[idx]
                    revertedList[idx] = originalTask
                    _allTasks.value = revertedList
                }
                _notificationMessage.value = "Failed to save task: ${e.message}"
                // Clear error notification after 5 seconds
                kotlinx.coroutines.delay(5000)
                _notificationMessage.value = null
            }
        }
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }
}
