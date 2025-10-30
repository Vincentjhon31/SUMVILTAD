package com.zynt.sumviltadconnect.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.model.AppNotification
import com.zynt.sumviltadconnect.data.repository.FarmerRepository
import com.zynt.sumviltadconnect.data.local.NotificationsCache
import com.zynt.sumviltadconnect.data.local.TaskCache
import com.zynt.sumviltadconnect.data.local.EventsCache
import com.zynt.sumviltadconnect.data.local.IrrigationCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class NotificationsUiState {
    object Loading: NotificationsUiState()
    data class Success(val data: List<AppNotification>, val offline: Boolean = false): NotificationsUiState()
    data class Error(val message: String): NotificationsUiState()
}

class NotificationsViewModel(app: Application): AndroidViewModel(app) {
    private val repo = FarmerRepository(
        taskCache = TaskCache(app),
        eventsCache = EventsCache(app),
        notificationsCache = NotificationsCache(app),
        irrigationCache = IrrigationCache(app)
    )
    private val _state = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val state: StateFlow<NotificationsUiState> = _state

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init { refresh() }

    fun refresh() {
        _isRefreshing.value = true

        // Keep current state while refreshing - following CropHealth pattern
        val currentState = _state.value
        if (currentState !is NotificationsUiState.Success) {
            _state.value = NotificationsUiState.Loading
        }

        viewModelScope.launch {
            try {
                val (notifications, offline) = repo.notifications()
                _state.value = NotificationsUiState.Success(notifications, offline)
            } catch (e: Exception) {
                // Only show error if we have no existing data - graceful degradation
                if (currentState !is NotificationsUiState.Success) {
                    _state.value = NotificationsUiState.Error(e.message ?: "Failed to load notifications")
                }
                // If we have existing data, keep it visible and just stop refreshing
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun markRead(id: Int) {
        viewModelScope.launch {
            try {
                repo.markNotificationRead(id)
                refresh() // Refresh to get updated state
            } catch (_: Exception) {
                // Silently fail - user can try again
            }
        }
    }

    fun markUnread(id: Int) {
        viewModelScope.launch {
            try {
                repo.markNotificationUnread(id)
                refresh() // Refresh to get updated state
            } catch (_: Exception) {
                // Silently fail - user can try again
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                repo.markAllNotificationsRead()
                refresh() // Refresh to get updated state
            } catch (_: Exception) {
                // Silently fail - user can try again
            }
        }
    }
}
