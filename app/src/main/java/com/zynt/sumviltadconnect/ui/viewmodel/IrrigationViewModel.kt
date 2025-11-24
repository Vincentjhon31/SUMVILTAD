package com.zynt.sumviltadconnect.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.model.IrrigationSchedule
import com.zynt.sumviltadconnect.data.repository.FarmerRepository
import com.zynt.sumviltadconnect.data.local.IrrigationCache
import com.zynt.sumviltadconnect.data.local.TaskCache
import com.zynt.sumviltadconnect.data.local.EventsCache
import com.zynt.sumviltadconnect.data.local.NotificationsCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class IrrigationUiState {
    object Loading : IrrigationUiState()
    data class Success(
        val schedules: List<IrrigationSchedule>,
        val userLocation: String?,
        val offline: Boolean = false
    ) : IrrigationUiState()
    data class Error(val message: String) : IrrigationUiState()
}

class IrrigationViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = FarmerRepository(
        taskCache = TaskCache(app),
        eventsCache = EventsCache(app),
        notificationsCache = NotificationsCache(app),
        irrigationCache = IrrigationCache(app)
    )

    private val _state = MutableStateFlow<IrrigationUiState>(IrrigationUiState.Loading)
    val state: StateFlow<IrrigationUiState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Filtered schedules based on date
    private val _upcomingSchedules = MutableStateFlow<List<IrrigationSchedule>>(emptyList())
    val upcomingSchedules: StateFlow<List<IrrigationSchedule>> = _upcomingSchedules.asStateFlow()

    private val _pastSchedules = MutableStateFlow<List<IrrigationSchedule>>(emptyList())
    val pastSchedules: StateFlow<List<IrrigationSchedule>> = _pastSchedules.asStateFlow()

    private val _nextIrrigation = MutableStateFlow<IrrigationSchedule?>(null)
    val nextIrrigation: StateFlow<IrrigationSchedule?> = _nextIrrigation.asStateFlow()

    // Pagination state
    private val _upcomingCurrentPage = MutableStateFlow(0)
    val upcomingCurrentPage: StateFlow<Int> = _upcomingCurrentPage.asStateFlow()

    private val _pastCurrentPage = MutableStateFlow(0)
    val pastCurrentPage: StateFlow<Int> = _pastCurrentPage.asStateFlow()

    private val _upcomingPageSize = 5
    private val _pastPageSize = 5

    private val _upcomingTotalPages = MutableStateFlow(0)
    val upcomingTotalPages: StateFlow<Int> = _upcomingTotalPages.asStateFlow()

    private val _pastTotalPages = MutableStateFlow(0)
    val pastTotalPages: StateFlow<Int> = _pastTotalPages.asStateFlow()

    // All schedules (unpaginated)
    private var _allUpcomingSchedules = listOf<IrrigationSchedule>()
    private var _allPastSchedules = listOf<IrrigationSchedule>()

    // Irrigation request state
    private val _isSubmittingRequest = MutableStateFlow(false)
    val isSubmittingRequest: StateFlow<Boolean> = _isSubmittingRequest.asStateFlow()

    private val _requestSubmitSuccess = MutableStateFlow<String?>(null)
    val requestSubmitSuccess: StateFlow<String?> = _requestSubmitSuccess.asStateFlow()

    private val _requestSubmitError = MutableStateFlow<String?>(null)
    val requestSubmitError: StateFlow<String?> = _requestSubmitError.asStateFlow()

    // My requests state
    private val _myRequests = MutableStateFlow<List<com.zynt.sumviltadconnect.data.model.IrrigationRequest>>(emptyList())
    val myRequests: StateFlow<List<com.zynt.sumviltadconnect.data.model.IrrigationRequest>> = _myRequests.asStateFlow()

    private val _isLoadingRequests = MutableStateFlow(false)
    val isLoadingRequests: StateFlow<Boolean> = _isLoadingRequests.asStateFlow()

    init {
        loadIrrigationSchedules()
        loadMyRequests()
    }

    fun loadIrrigationSchedules() {
        _isRefreshing.value = true

        // Keep current state while refreshing - following CropHealth pattern
        val currentState = _state.value
        if (currentState !is IrrigationUiState.Success) {
            _state.value = IrrigationUiState.Loading
        }

        viewModelScope.launch {
            try {
                val response = repository.irrigationSchedules()
                _state.value = IrrigationUiState.Success(
                    schedules = response.schedules,
                    userLocation = response.userLocation,
                    offline = !(response.success ?: false) // Safe null check for success property
                )
                filterSchedules(response.schedules)
                Log.d("IrrigationViewModel", "Loaded ${response.schedules.size} irrigation schedules")
            } catch (e: Exception) {
                // Only show error if we have no existing data - graceful degradation
                if (currentState !is IrrigationUiState.Success) {
                    _state.value = IrrigationUiState.Error("Failed to load irrigation schedules: ${e.message}")
                }
                Log.e("IrrigationViewModel", "Failed to load irrigation schedules", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun filterSchedules(schedules: List<IrrigationSchedule>) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val upcoming = mutableListOf<IrrigationSchedule>()
        val past = mutableListOf<IrrigationSchedule>()

        schedules.forEach { schedule ->
            try {
                val scheduleDate = dateFormat.parse(schedule.date)
                if (scheduleDate != null) {
                    if (scheduleDate >= today) {
                        upcoming.add(schedule)
                    } else {
                        past.add(schedule)
                    }
                }
            } catch (e: Exception) {
                Log.e("IrrigationViewModel", "Error parsing date: ${schedule.date}", e)
            }
        }

        // Sort upcoming by date (earliest first)
        upcoming.sortBy { it.date }
        // Sort past by date (most recent first)
        past.sortByDescending { it.date }

        _allUpcomingSchedules = upcoming
        _allPastSchedules = past

        // Set initial paginated data
        _upcomingSchedules.value = upcoming.take(_upcomingPageSize)
        _pastSchedules.value = past.take(_pastPageSize)

        _upcomingTotalPages.value = (upcoming.size + _upcomingPageSize - 1) / _upcomingPageSize
        _pastTotalPages.value = (past.size + _pastPageSize - 1) / _pastPageSize

        _nextIrrigation.value = upcoming.firstOrNull()
    }

    fun refreshSchedules() {
        loadIrrigationSchedules()
    }

    // Load more schedules for pagination
    fun loadMoreUpcoming() {
        if (_upcomingCurrentPage.value < _upcomingTotalPages.value) {
            val nextPage = _upcomingCurrentPage.value + 1
            val startIndex = nextPage * _upcomingPageSize
            val endIndex = startIndex + _upcomingPageSize

            _upcomingSchedules.value = _allUpcomingSchedules.subList(0, minOf(endIndex, _allUpcomingSchedules.size))

            _upcomingCurrentPage.value = nextPage
        }
    }

    fun loadMorePast() {
        if (_pastCurrentPage.value < _pastTotalPages.value) {
            val nextPage = _pastCurrentPage.value + 1
            val startIndex = nextPage * _pastPageSize
            val endIndex = startIndex + _pastPageSize

            _pastSchedules.value = _allPastSchedules.subList(0, minOf(endIndex, _allPastSchedules.size))

            _pastCurrentPage.value = nextPage
        }
    }

    // Helper functions for UI
    fun getDaysUntil(dateString: String): String {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val scheduleDate = dateFormat.parse(dateString)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            if (scheduleDate != null && today != null) {
                val diffInMillis = scheduleDate.time - today.time
                val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

                when {
                    diffInDays == 0 -> "Today"
                    diffInDays == 1 -> "Tomorrow"
                    diffInDays > 1 -> "In $diffInDays days"
                    diffInDays == -1 -> "Yesterday"
                    else -> "${kotlin.math.abs(diffInDays)} days ago"
                }
            } else {
                dateString
            }
        } catch (e: Exception) {
            Log.e("IrrigationViewModel", "Error calculating days until: $dateString", e)
            dateString
        }
    }

    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            Log.e("IrrigationViewModel", "Error formatting date: $dateString", e)
            dateString
        }
    }

    fun isToday(dateString: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val scheduleDate = dateFormat.parse(dateString)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            scheduleDate != null && scheduleDate == today
        } catch (e: Exception) {
            Log.e("IrrigationViewModel", "Error checking if date is today: $dateString", e)
            false
        }
    }

    fun submitIrrigationRequest(location: String, requestedDate: String, reason: String?) {
        viewModelScope.launch {
            _isSubmittingRequest.value = true
            _requestSubmitError.value = null
            _requestSubmitSuccess.value = null

            try {
                Log.d("IrrigationViewModel", "Submitting irrigation request: location=$location, date=$requestedDate")
                val response = repository.submitIrrigationRequest(location, requestedDate, reason)

                if (response.success) {
                    _requestSubmitSuccess.value = response.message
                    Log.d("IrrigationViewModel", "Irrigation request submitted successfully")
                    // Refresh schedules and requests
                    loadIrrigationSchedules()
                    loadMyRequests()
                } else {
                    _requestSubmitError.value = response.message
                    Log.e("IrrigationViewModel", "Failed to submit irrigation request: ${response.message}")
                }
            } catch (e: Exception) {
                _requestSubmitError.value = "Failed to submit request: ${e.message}"
                Log.e("IrrigationViewModel", "Exception submitting irrigation request", e)
            } finally {
                _isSubmittingRequest.value = false
            }
        }
    }

    fun clearRequestMessages() {
        _requestSubmitSuccess.value = null
        _requestSubmitError.value = null
    }

    fun loadMyRequests() {
        viewModelScope.launch {
            _isLoadingRequests.value = true
            try {
                Log.d("IrrigationViewModel", "Loading my irrigation requests")
                val response = repository.getIrrigationRequests()

                if (response.success && response.data != null) {
                    _myRequests.value = response.data
                    Log.d("IrrigationViewModel", "Loaded ${response.data.size} irrigation requests")
                } else {
                    Log.e("IrrigationViewModel", "Failed to load requests: ${response.message}")
                    _myRequests.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("IrrigationViewModel", "Exception loading irrigation requests", e)
                _myRequests.value = emptyList()
            } finally {
                _isLoadingRequests.value = false
            }
        }
    }
}
