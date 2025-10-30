package com.zynt.sumviltadconnect.data.model

import com.google.gson.annotations.SerializedName

data class CropHealthComment(
    val id: Int,
    @SerializedName("crop_health_id")
    val cropHealthId: Int,
    @SerializedName("user_id")
    val userId: Int,
    val message: String,
    @SerializedName("is_admin_response")
    val isAdminResponseInt: Int = 0, // Changed to Int to match API response
    @SerializedName("is_read")
    val isReadInt: Int = 0, // Changed to Int to match API response
    @SerializedName("created_at")
    val created_at: String,
    @SerializedName("updated_at")
    val updated_at: String? = null,
    val user: CommentUser? = null
) {
    // Computed properties to convert Int to Boolean for easier use in UI
    val is_admin_response: Boolean
        get() = isAdminResponseInt == 1

    val is_read: Boolean
        get() = isReadInt == 1
}

data class CommentUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)

data class CommentsResponse(
    val comments: List<CropHealthComment>,
    @SerializedName("current_user_id")
    val currentUserId: Int,
    @SerializedName("is_admin")
    val isAdmin: Boolean
)
