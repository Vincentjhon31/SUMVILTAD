package com.zynt.sumviltadconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.model.Event
import com.zynt.sumviltadconnect.data.model.EventParticipation
import com.zynt.sumviltadconnect.data.repository.FarmerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import com.zynt.sumviltadconnect.data.local.EventsCache
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class EventsUiState {
    object Loading: EventsUiState()
    data class Success(
        val events: List<Event>,
        val filteredEvents: List<Event>,
        val filterType: EventFilter,
        val searchQuery: String,
        val sortBy: EventSort,
        val isRefreshing: Boolean = false,
        val participationStates: Map<Int, ParticipationState> = emptyMap(),
        val notification: String? = null
    ): EventsUiState()
    data class Error(val message: String): EventsUiState()
}

data class ParticipationState(
    val isLoading: Boolean = false,
    val status: String? = null
)

enum class EventFilter(val displayName: String) {
    ALL("All Events"),
    UPCOMING("Upcoming"),
    ONGOING("Happening Now"),
    PAST("Past")
}

enum class EventSort(val displayName: String) {
    DEFAULT("Default"),
    DATE_ASC("Date (Earliest)"),
    DATE_DESC("Date (Latest)"),
    TITLE("Title (A-Z)")
}

enum class EventStatus {
    UPCOMING, ONGOING, PAST
}

class EventsViewModel(private val repo: FarmerRepository = FarmerRepository()): ViewModel() {
    private val _state = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val state: StateFlow<EventsUiState> = _state

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    init {
        refresh()
    }

    fun refresh() {
        _isRefreshing.value = true

        // Keep current state while refreshing - following CropHealth pattern
        val currentState = _state.value
        if (currentState !is EventsUiState.Success) {
            _state.value = EventsUiState.Loading
        }

        viewModelScope.launch {
            try {
                val (events, offline) = repo.events()
                val currentSuccess = currentState as? EventsUiState.Success
                val filterType = currentSuccess?.filterType ?: EventFilter.ALL
                val searchQuery = currentSuccess?.searchQuery ?: ""
                val sortBy = currentSuccess?.sortBy ?: EventSort.DEFAULT

                val filtered = filterAndSortEvents(events, filterType, searchQuery, sortBy)

                _state.value = EventsUiState.Success(
                    events = events,
                    filteredEvents = filtered,
                    filterType = filterType,
                    searchQuery = searchQuery,
                    sortBy = sortBy,
                    participationStates = currentSuccess?.participationStates ?: emptyMap(),
                    isRefreshing = false
                )
            } catch (e: Exception) {
                // Only show error if we have no existing data - graceful degradation
                if (currentState !is EventsUiState.Success) {
                    _state.value = EventsUiState.Error(e.message ?: "Failed to load events")
                }
                // If we have existing data, keep it visible and just stop refreshing
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun setFilter(filter: EventFilter) {
        val currentState = _state.value as? EventsUiState.Success ?: return
        val filtered = filterAndSortEvents(currentState.events, filter, currentState.searchQuery, currentState.sortBy)
        _state.value = currentState.copy(
            filterType = filter,
            filteredEvents = filtered
        )
    }

    fun setSearchQuery(query: String) {
        val currentState = _state.value as? EventsUiState.Success ?: return
        val filtered = filterAndSortEvents(currentState.events, currentState.filterType, query, currentState.sortBy)
        _state.value = currentState.copy(
            searchQuery = query,
            filteredEvents = filtered
        )
    }

    fun setSortBy(sort: EventSort) {
        val currentState = _state.value as? EventsUiState.Success ?: return
        val filtered = filterAndSortEvents(currentState.events, currentState.filterType, currentState.searchQuery, sort)
        _state.value = currentState.copy(
            sortBy = sort,
            filteredEvents = filtered
        )
    }

    fun participateInEvent(eventId: Int, status: String) {
        val currentState = _state.value as? EventsUiState.Success ?: return

        // Update UI immediately for responsive feedback
        val newParticipationStates = currentState.participationStates.toMutableMap()
        newParticipationStates[eventId] = ParticipationState(isLoading = true, status = status)

        _state.value = currentState.copy(participationStates = newParticipationStates)

        viewModelScope.launch {
            try {
                repo.participateInEvent(eventId, status)

                // Update participation state on success
                val updatedStates = currentState.participationStates.toMutableMap()
                updatedStates[eventId] = ParticipationState(isLoading = false, status = status)

                val notification = if (status == "attending") "You're now attending this event!" else "You've declined this event."

                _state.value = (_state.value as EventsUiState.Success).copy(
                    participationStates = updatedStates,
                    notification = notification
                )

                // Clear notification after 3 seconds
                kotlinx.coroutines.delay(3000)
                val currentSuccessState = _state.value as? EventsUiState.Success
                if (currentSuccessState?.notification == notification) {
                    _state.value = currentSuccessState.copy(notification = null)
                }

            } catch (e: Exception) {
                // Revert on error
                val revertedStates = currentState.participationStates.toMutableMap()
                revertedStates[eventId] = ParticipationState(isLoading = false, status = null)

                _state.value = (_state.value as EventsUiState.Success).copy(
                    participationStates = revertedStates,
                    notification = "Failed to update attendance status."
                )

                // Clear error notification after 3 seconds
                kotlinx.coroutines.delay(3000)
                val currentSuccessState = _state.value as? EventsUiState.Success
                if (currentSuccessState?.notification == "Failed to update attendance status.") {
                    _state.value = currentSuccessState.copy(notification = null)
                }
            }
        }
    }

    fun clearNotification() {
        val currentState = _state.value as? EventsUiState.Success ?: return
        _state.value = currentState.copy(notification = null)
    }

    private fun filterAndSortEvents(events: List<Event>, filter: EventFilter, query: String, sort: EventSort): List<Event> {
        var filtered = events

        // Apply search filter
        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            filtered = filtered.filter { event ->
                event.title.lowercase().contains(lowerQuery) ||
                event.description?.lowercase()?.contains(lowerQuery) == true ||
                event.location?.lowercase()?.contains(lowerQuery) == true ||
                event.category?.lowercase()?.contains(lowerQuery) == true ||
                event.organizer?.lowercase()?.contains(lowerQuery) == true
            }
        }

        // Apply status filter
        if (filter != EventFilter.ALL) {
            filtered = filtered.filter { event ->
                when (filter) {
                    EventFilter.UPCOMING -> getEventStatus(event) == EventStatus.UPCOMING
                    EventFilter.ONGOING -> getEventStatus(event) == EventStatus.ONGOING
                    EventFilter.PAST -> getEventStatus(event) == EventStatus.PAST
                    EventFilter.ALL -> true
                }
            }
        }

        // Apply sorting
        return when (sort) {
            EventSort.DEFAULT -> {
                filtered.sortedWith { a, b ->
                    val statusA = getEventStatus(a)
                    val statusB = getEventStatus(b)

                    if (statusA == statusB) {
                        // Same status, sort by date
                        val dateA = parseEventDate(a.eventDate)
                        val dateB = parseEventDate(b.eventDate)

                        when {
                            dateA == null && dateB == null -> 0
                            dateA == null -> 1
                            dateB == null -> -1
                            statusA == EventStatus.UPCOMING -> dateA.compareTo(dateB) // Ascending for upcoming
                            else -> dateB.compareTo(dateA) // Descending for others
                        }
                    } else {
                        // Different status, prioritize: ongoing > upcoming > past
                        val priorityA = when (statusA) {
                            EventStatus.ONGOING -> 1
                            EventStatus.UPCOMING -> 2
                            EventStatus.PAST -> 3
                        }
                        val priorityB = when (statusB) {
                            EventStatus.ONGOING -> 1
                            EventStatus.UPCOMING -> 2
                            EventStatus.PAST -> 3
                        }
                        priorityA.compareTo(priorityB)
                    }
                }
            }
            EventSort.DATE_ASC -> {
                filtered.sortedWith { a, b ->
                    val dateA = parseEventDate(a.eventDate)
                    val dateB = parseEventDate(b.eventDate)
                    when {
                        dateA == null && dateB == null -> 0
                        dateA == null -> 1
                        dateB == null -> -1
                        else -> dateA.compareTo(dateB)
                    }
                }
            }
            EventSort.DATE_DESC -> {
                filtered.sortedWith { a, b ->
                    val dateA = parseEventDate(a.eventDate)
                    val dateB = parseEventDate(b.eventDate)
                    when {
                        dateA == null && dateB == null -> 0
                        dateA == null -> 1
                        dateB == null -> -1
                        else -> dateB.compareTo(dateA)
                    }
                }
            }
            EventSort.TITLE -> filtered.sortedBy { it.title }
        }
    }

    fun getEventStatus(event: Event): EventStatus {
        val now = Date()
        val eventDate = parseEventDate(event.eventDate) ?: return EventStatus.PAST
        val endDate = parseEventDate(event.endDate) ?: Date(eventDate.time + ((event.durationHours ?: 2) * 60 * 60 * 1000))

        return when {
            eventDate.after(now) -> EventStatus.UPCOMING
            endDate.after(now) -> EventStatus.ONGOING
            else -> EventStatus.PAST
        }
    }

    fun isHappeningSoon(event: Event): Boolean {
        val now = Date()
        val eventDate = parseEventDate(event.eventDate) ?: return false
        val hoursDiff = (eventDate.time - now.time) / (1000 * 60 * 60)
        return hoursDiff in 0..24
    }

    private fun parseEventDate(dateString: String?): Date? {
        if (dateString == null) return null
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            try {
                // Try alternative format
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString)
            } catch (e: Exception) {
                null
            }
        }
    }
}
