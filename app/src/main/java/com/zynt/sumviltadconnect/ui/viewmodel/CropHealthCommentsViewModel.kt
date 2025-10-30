package com.zynt.sumviltadconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.model.CropHealthComment
import com.zynt.sumviltadconnect.data.model.CommentRequest
import com.zynt.sumviltadconnect.data.model.CommentUser
import com.zynt.sumviltadconnect.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CommentsUiState {
    object Loading : CommentsUiState()
    data class Success(
        val comments: List<CropHealthComment>,
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false
    ) : CommentsUiState()
    data class Error(val message: String) : CommentsUiState()
}

class CropHealthCommentsViewModel : ViewModel() {
    private val _state = MutableStateFlow<CommentsUiState>(CommentsUiState.Loading)
    val state: StateFlow<CommentsUiState> = _state

    private var cropHealthId: Int = 0
    private var isInitialLoad = true

    fun loadComments(cropHealthId: Int, forceRefresh: Boolean = false) {
        this.cropHealthId = cropHealthId

        // Only show loading spinner on initial load or force refresh
        if (isInitialLoad || forceRefresh) {
            _state.value = CommentsUiState.Loading
        } else {
            // For background updates, show refreshing state
            val currentState = _state.value
            if (currentState is CommentsUiState.Success) {
                _state.value = currentState.copy(isRefreshing = true)
            }
        }

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getCropHealthComments(cropHealthId)
                if (response.isSuccessful) {
                    val commentsResponse = response.body()
                    _state.value = CommentsUiState.Success(
                        comments = commentsResponse?.comments ?: emptyList(),
                        isLoading = false,
                        isRefreshing = false
                    )
                } else {
                    // If we have existing comments, don't completely fail - just stop refreshing
                    val currentState = _state.value
                    if (currentState is CommentsUiState.Success && !isInitialLoad) {
                        _state.value = currentState.copy(isLoading = false, isRefreshing = false)
                    } else {
                        _state.value = CommentsUiState.Error("Failed to load comments: ${response.message()}")
                    }
                }
                isInitialLoad = false
            } catch (e: Exception) {
                // If we have existing comments, don't completely fail - just stop refreshing
                val currentState = _state.value
                if (currentState is CommentsUiState.Success && !isInitialLoad) {
                    _state.value = currentState.copy(isLoading = false, isRefreshing = false)
                } else {
                    _state.value = CommentsUiState.Error(e.message ?: "Failed to load comments")
                }
            }
        }
    }

    fun sendComment(message: String) {
        if (message.isBlank()) return

        val currentState = _state.value
        if (currentState is CommentsUiState.Success) {
            _state.value = currentState.copy(isLoading = true)
        }

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.addCropHealthComment(
                    cropHealthId,
                    CommentRequest(message.trim())
                )

                if (response.isSuccessful) {
                    // Refresh comments to get the latest list
                    loadComments(cropHealthId, forceRefresh = false)
                } else {
                    val currentState = _state.value
                    if (currentState is CommentsUiState.Success) {
                        _state.value = currentState.copy(isLoading = false)
                    }
                    // For now, add comment locally if API fails (temporary fallback)
                    addCommentLocally(message.trim())
                }
            } catch (e: Exception) {
                val currentState = _state.value
                if (currentState is CommentsUiState.Success) {
                    _state.value = currentState.copy(isLoading = false)
                }
                // For now, add comment locally if API fails (temporary fallback)
                addCommentLocally(message.trim())
            }
        }
    }

    private fun addCommentLocally(message: String) {
        val currentState = _state.value
        if (currentState is CommentsUiState.Success) {
            val newComment = CropHealthComment(
                id = System.currentTimeMillis().toInt(), // Temporary ID
                cropHealthId = cropHealthId,
                userId = 1, // TODO: Get actual user ID from auth session
                message = message,
                isAdminResponseInt = 0, // Regular user comment (0 = false)
                isReadInt = 0, // Unread initially (0 = false)
                created_at = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", java.util.Locale.getDefault()).format(java.util.Date()),
                updated_at = null,
                user = CommentUser(
                    id = 1,
                    name = "You",
                    email = "user@example.com",
                    role = "farmer"
                )
            )

            val updatedComments = (currentState.comments + newComment).sortedByDescending {
                try {
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", java.util.Locale.getDefault()).parse(it.created_at)?.time ?: 0L
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            }

            _state.value = currentState.copy(
                comments = updatedComments,
                isLoading = false
            )
        }
    }

    fun markAsRead(commentId: Int) {
        // Update local state immediately for better UX
        val currentState = _state.value
        if (currentState is CommentsUiState.Success) {
            val updatedComments = currentState.comments.map { comment ->
                if (comment.id == commentId && !comment.is_read) {
                    // Create a new comment with isReadInt = 1 (true)
                    comment.copy(isReadInt = 1)
                } else {
                    comment
                }
            }
            _state.value = currentState.copy(comments = updatedComments)
        }

        // TODO: Make API call to mark as read on server when endpoint is available
        viewModelScope.launch {
            try {
                // ApiClient.apiService.markCommentAsRead(commentId)
                // For now, this is just a local operation
            } catch (e: Exception) {
                // Silently handle error - marking as read is not critical
            }
        }
    }

    fun refresh() {
        if (cropHealthId != 0) {
            loadComments(cropHealthId, forceRefresh = true)
        }
    }
}
