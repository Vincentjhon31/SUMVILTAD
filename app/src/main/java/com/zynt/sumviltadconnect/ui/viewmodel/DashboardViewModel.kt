package com.zynt.sumviltadconnect.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.model.DashboardSummary
import com.zynt.sumviltadconnect.data.repository.FarmerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class DashboardUiState {
    object Loading: DashboardUiState()
    data class Success(val data: DashboardSummary, val warning: String? = null): DashboardUiState()
    data class Error(val message: String, val lastData: DashboardSummary? = null): DashboardUiState()
}

class DashboardViewModel(
    private val repo: FarmerRepository = FarmerRepository()
): ViewModel() {
    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val state: StateFlow<DashboardUiState> = _state

    private var lastGood: DashboardSummary? = null

    init {
        Log.d("DashboardViewModel", "ViewModel initialized, starting refresh")
        refresh()
    }

    fun refresh() {
        Log.d("DashboardViewModel", "Starting dashboard refresh")
        _state.value = DashboardUiState.Loading
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "Calling repository.dashboard()")
                val summary = repo.dashboard()
                Log.d("DashboardViewModel", "Dashboard API success: $summary")
                lastGood = summary
                _state.value = DashboardUiState.Success(summary)
            } catch (e: HttpException) {
                val errorMsg = "HTTP Error ${e.code()}: ${e.message()}"
                Log.e("DashboardViewModel", errorMsg, e)

                if (e.code() == 404) {
                    // Create fallback data with some test numbers to show the UI works
                    val fallback = DashboardSummary(
                        totalAnalyses = 0,
                        expertReviewed = 0,
                        pendingReview = 0,
                        diseaseDistribution = emptyMap(),
                        monthlyTrend = emptyList(),
                        taskProgress = 0,
                        cropHealthCount = 0,
                        upcomingEvents = 0,
                        pendingTasks = 0,
                        unreadNotifications = 0
                    )
                    lastGood = fallback
                    _state.value = DashboardUiState.Success(fallback, "Dashboard API not found (404) - Please check server")
                } else {
                    val fallback = lastGood ?: DashboardSummary(
                        totalAnalyses = 0,
                        expertReviewed = 0,
                        pendingReview = 0,
                        diseaseDistribution = emptyMap(),
                        monthlyTrend = emptyList(),
                        taskProgress = 0,
                        cropHealthCount = 0,
                        upcomingEvents = 0,
                        pendingTasks = 0,
                        unreadNotifications = 0
                    )
                    _state.value = DashboardUiState.Error(errorMsg, fallback)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message ?: "Unknown error"}"
                Log.e("DashboardViewModel", errorMsg, e)

                val fallback = lastGood ?: DashboardSummary(
                    totalAnalyses = 0,
                    expertReviewed = 0,
                    pendingReview = 0,
                    diseaseDistribution = emptyMap(),
                    monthlyTrend = emptyList(),
                    taskProgress = 0,
                    cropHealthCount = 0,
                    upcomingEvents = 0,
                    pendingTasks = 0,
                    unreadNotifications = 0
                )
                _state.value = DashboardUiState.Error(errorMsg, fallback)
            }
        }
    }
}
