package com.zynt.sumviltadconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.model.CropHealthRecord
import com.zynt.sumviltadconnect.data.repository.FarmerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

sealed class CropHealthUiState {
    object Loading : CropHealthUiState()
    data class Success(
        val data: List<CropHealthRecord>,
        val hasMore: Boolean,
        val totalRecords: Int,
        val isLoadingMore: Boolean = false
    ) : CropHealthUiState()
    data class Error(val message: String) : CropHealthUiState()
}

class CropHealthViewModel : ViewModel() {
    private val _state = MutableStateFlow<CropHealthUiState>(CropHealthUiState.Loading)
    val state: StateFlow<CropHealthUiState> = _state

    private val repository = FarmerRepository()
    private val recordsPerPage = 5
    private var currentPage = 1
    private var allLoadedRecords = mutableListOf<CropHealthRecord>()

    // Store all available records for client-side pagination fallback
    private var allAvailableRecords = listOf<CropHealthRecord>()
    private var usingClientSidePagination = false
    private var totalRecordsCount = 0

    init {
        loadInitialRecords()
    }

    fun refresh() {
        currentPage = 1
        allLoadedRecords.clear()
        allAvailableRecords = emptyList()
        usingClientSidePagination = false
        totalRecordsCount = 0
        loadInitialRecords()
    }

    private fun loadInitialRecords() {
        _state.value = CropHealthUiState.Loading

        viewModelScope.launch {
            try {
                // Try server-side pagination first
                val paginatedResponse = repository.getCropHealthHistoryPaginated(1, recordsPerPage)

                Log.d("CropHealthVM", "Server response - total: ${paginatedResponse.total}, " +
                        "current_page: ${paginatedResponse.current_page}, " +
                        "last_page: ${paginatedResponse.last_page}, " +
                        "data size: ${paginatedResponse.data.size}")

                // Check if API is actually paginating or returning all records
                val isActuallyPaginating = paginatedResponse.data.size <= recordsPerPage &&
                        paginatedResponse.total > recordsPerPage

                if (isActuallyPaginating) {
                    // True server-side pagination
                    allLoadedRecords.clear()
                    allLoadedRecords.addAll(paginatedResponse.data.sortedByDescending { it.created_at })
                    currentPage = 1
                    totalRecordsCount = paginatedResponse.total
                    usingClientSidePagination = false

                    Log.d("CropHealthVM", "Using server-side pagination")

                    _state.value = CropHealthUiState.Success(
                        data = allLoadedRecords.toList(),
                        hasMore = currentPage < paginatedResponse.last_page,
                        totalRecords = totalRecordsCount,
                        isLoadingMore = false
                    )
                } else {
                    // API returned all records - use client-side pagination
                    Log.d("CropHealthVM", "API returned all records, switching to client-side pagination")

                    val allRecords = paginatedResponse.data.sortedByDescending { it.created_at }
                    allAvailableRecords = allRecords
                    totalRecordsCount = allRecords.size
                    usingClientSidePagination = true
                    currentPage = 1

                    // Load first page (5 records)
                    allLoadedRecords.clear()
                    val firstPageRecords = allRecords.take(recordsPerPage)
                    allLoadedRecords.addAll(firstPageRecords)

                    _state.value = CropHealthUiState.Success(
                        data = allLoadedRecords.toList(),
                        hasMore = allLoadedRecords.size < allRecords.size,
                        totalRecords = totalRecordsCount,
                        isLoadingMore = false
                    )
                }
            } catch (e: Exception) {
                Log.e("CropHealthVM", "Server pagination failed: ${e.message}, falling back to basic history")

                // Fallback to basic history with client-side pagination
                try {
                    val healthResponse = repository.getCropHealthHistory()
                    val allRecords = healthResponse.data.sortedByDescending { it.created_at }
                    allAvailableRecords = allRecords
                    totalRecordsCount = allRecords.size
                    usingClientSidePagination = true
                    currentPage = 1

                    Log.d("CropHealthVM", "Using client-side pagination fallback - total records: ${allRecords.size}")

                    // Load first page (5 records)
                    allLoadedRecords.clear()
                    val firstPageRecords = allRecords.take(recordsPerPage)
                    allLoadedRecords.addAll(firstPageRecords)

                    _state.value = CropHealthUiState.Success(
                        data = allLoadedRecords.toList(),
                        hasMore = allLoadedRecords.size < allRecords.size,
                        totalRecords = totalRecordsCount,
                        isLoadingMore = false
                    )
                } catch (fallbackException: Exception) {
                    Log.e("CropHealthVM", "Fallback failed: ${fallbackException.message}")
                    _state.value = CropHealthUiState.Error("Failed to load crop health records: ${fallbackException.message}")
                }
            }
        }
    }

    fun loadMoreRecords() {
        val currentState = _state.value
        if (currentState is CropHealthUiState.Success &&
            currentState.hasMore &&
            !currentState.isLoadingMore) {

            Log.d("CropHealthVM", "Loading more records - current loaded: ${allLoadedRecords.size}, " +
                    "using client-side: $usingClientSidePagination")

            _state.value = currentState.copy(isLoadingMore = true)

            viewModelScope.launch {
                try {
                    if (usingClientSidePagination) {
                        // Client-side pagination: load next batch from allAvailableRecords
                        val startIndex = currentPage * recordsPerPage
                        val endIndex = kotlin.math.min(startIndex + recordsPerPage, allAvailableRecords.size)

                        Log.d("CropHealthVM", "Client-side load more - startIndex: $startIndex, endIndex: $endIndex")

                        if (startIndex < allAvailableRecords.size) {
                            val nextPageRecords = allAvailableRecords.subList(startIndex, endIndex)
                            allLoadedRecords.addAll(nextPageRecords)
                            currentPage++

                            Log.d("CropHealthVM", "Added ${nextPageRecords.size} records, " +
                                    "total loaded now: ${allLoadedRecords.size}")
                        }

                        _state.value = CropHealthUiState.Success(
                            data = allLoadedRecords.toList(),
                            hasMore = allLoadedRecords.size < allAvailableRecords.size,
                            totalRecords = totalRecordsCount,
                            isLoadingMore = false
                        )
                    } else {
                        // Server-side pagination: fetch next page from API
                        val nextPage = currentPage + 1
                        Log.d("CropHealthVM", "Server-side load more - fetching page $nextPage")

                        val paginatedResponse = repository.getCropHealthHistoryPaginated(nextPage, recordsPerPage)

                        val newRecords = paginatedResponse.data.sortedByDescending { it.created_at }
                        allLoadedRecords.addAll(newRecords)
                        currentPage = nextPage

                        Log.d("CropHealthVM", "Server returned ${newRecords.size} records")

                        _state.value = CropHealthUiState.Success(
                            data = allLoadedRecords.toList(),
                            hasMore = currentPage < paginatedResponse.last_page,
                            totalRecords = paginatedResponse.total,
                            isLoadingMore = false
                        )
                    }
                } catch (e: Exception) {
                    Log.e("CropHealthVM", "Load more failed: ${e.message}")
                    // If loading more fails, keep current data but stop loading
                    _state.value = currentState.copy(
                        hasMore = false,
                        isLoadingMore = false
                    )
                }
            }
        }
    }

    fun deleteRecord(recordId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteCropHealthRecord(recordId)

                // Remove from loaded records
                allLoadedRecords.removeAll { it.id == recordId }

                // Remove from all available records if using client-side pagination
                if (usingClientSidePagination) {
                    allAvailableRecords = allAvailableRecords.filter { it.id != recordId }
                    totalRecordsCount = allAvailableRecords.size
                }

                val currentState = _state.value
                if (currentState is CropHealthUiState.Success) {
                    _state.value = currentState.copy(
                        data = allLoadedRecords.toList(),
                        totalRecords = totalRecordsCount,
                        hasMore = if (usingClientSidePagination) {
                            allLoadedRecords.size < allAvailableRecords.size
                        } else {
                            currentState.hasMore // Keep existing hasMore for server pagination
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("CropHealthVM", "Delete failed: ${e.message}")
            }
        }
    }

    /**
     * Get a specific record by ID from the loaded records
     */
    fun getRecordById(id: Int): CropHealthRecord? {
        // First check in loaded records
        val fromLoaded = allLoadedRecords.find { it.id == id }
        if (fromLoaded != null) {
            return fromLoaded
        }

        // If using client-side pagination, check all available records
        if (usingClientSidePagination) {
            return allAvailableRecords.find { it.id == id }
        }

        // For server-side pagination, check current state
        val currentState = _state.value
        if (currentState is CropHealthUiState.Success) {
            return currentState.data.find { it.id == id }
        }

        return null
    }
}