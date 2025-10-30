package com.zynt.sumviltadconnect.data.model

import com.google.gson.annotations.SerializedName

// User and Authentication Models
data class FarmArea(
    val id: Int,
    @SerializedName("farm_location")
    val farmLocation: String,
    @SerializedName("rice_field_area")
    val riceFieldArea: Double,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class FarmerProfile(
    val id: Int? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("middle_initial")
    val middleInitial: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("complete_address")
    val completeAddress: String? = null,
    @SerializedName("farm_location")
    val farmLocation: String? = null,
    @SerializedName("contact_number")
    val contactNumber: String? = null,
    val birthday: String? = null,
    @SerializedName("rice_field_area")
    val riceFieldArea: Double? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    @SerializedName("email_verified_at")
    val email_verified_at: String? = null,
    @SerializedName("profile_photo_path")
    val profilePhotoPath: String? = null,
    @SerializedName("created_at")
    val created_at: String,
    @SerializedName("updated_at")
    val updated_at: String,
    @SerializedName("farmer_profile")
    val farmerProfile: FarmerProfile? = null,
    @SerializedName("farm_areas")
    val farmAreas: List<FarmArea>? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation")
    val password_confirmation: String,
    val role: String = "farmer"
)

data class AuthResponse(
    val message: String,
    val user: User? = null,
    val token: String? = null,
    val success: Boolean? = null,
    val errors: Map<String, List<String>>? = null
)

data class LogoutResponse(
    val message: String
)

data class UserResponse(
    val user: User,
    val message: String? = null,
    val success: Boolean? = null
)

// Profile Update Request
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("middle_initial")
    val middleInitial: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("complete_address")
    val completeAddress: String? = null,
    @SerializedName("farm_location")
    val farmLocation: String? = null,
    @SerializedName("contact_number")
    val contactNumber: String? = null,
    val birthday: String? = null,
    @SerializedName("rice_field_area")
    val riceFieldArea: Double? = null
)

// Farm Area Management
data class FarmAreaRequest(
    @SerializedName("farm_location")
    val farmLocation: String,
    @SerializedName("rice_field_area")
    val riceFieldArea: Double
)

data class FarmAreaResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("farm_area")
    val farmArea: FarmArea? = null
)

// Dashboard Model
data class DashboardSummary(
    @SerializedName("total_analyses")
    val totalAnalyses: Int,
    @SerializedName("expert_reviewed")
    val expertReviewed: Int,
    @SerializedName("pending_review")
    val pendingReview: Int,
    @SerializedName("disease_distribution")
    val diseaseDistribution: Map<String, Int>, // e.g., {"Rice Blast": 10, "Bacterial Leaf Blight": 5}
    @SerializedName("monthly_trend")
    val monthlyTrend: List<Int>, // e.g., [5, 8, 12, ...]
    @SerializedName("monthly_trend_labels")
    val monthlyTrendLabels: List<String>? = null, // e.g., ["Jan 2024", "Feb 2024", ...]
    @SerializedName("task_progress")
    val taskProgress: Int, // percentage
    // legacy fields for compatibility
    @SerializedName("crop_health_count")
    val cropHealthCount: Int = 0,
    @SerializedName("upcoming_events")
    val upcomingEvents: Int = 0,
    @SerializedName("pending_tasks")
    val pendingTasks: Int = 0,
    @SerializedName("unread_notifications")
    val unreadNotifications: Int = 0
)

// Task Models
data class TasksResponse(
    val data: List<Task>? = null,
    val tasks: List<Task>? = null,
    val message: String? = null,
    val success: Boolean? = null
)

data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
    val progress: Int? = null,
    val feedback: String? = null
)

data class TaskUpdateResponse(
    val task: Task,
    val message: String,
    val success: Boolean
)

// Event Models
data class Event(
    val id: Int,
    val title: String,
    val description: String?,
    @SerializedName("event_date")
    val eventDate: String,
    @SerializedName("end_date")
    val endDate: String?,
    val location: String?,
    val organizer: String?,
    val category: String?,
    @SerializedName("duration_hours")
    val durationHours: Int?,
    @SerializedName("user_participation")
    val userParticipation: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class EventsResponse(
    val data: List<Event>? = null,
    val events: List<Event>? = null,
    val message: String? = null,
    val success: Boolean? = null
)

data class EventResponse(
    val data: Event?,  // Changed from 'event' to 'data' to match API response
    val message: String,
    val success: Boolean
)

data class EventParticipationRequest(
    val status: String
)

data class EventParticipationResponse(
    val message: String,
    val participation: EventParticipation?,
    val success: Boolean
)

data class EventParticipation(
    val id: Int,
    @SerializedName("event_id")
    val eventId: Int,
    @SerializedName("user_id")
    val userId: Int,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

// Notification Models
data class AppNotification(
    val id: Int,
    val title: String? = null,
    val message: String,
    val type: String = "info", // info, success, warning, error
    val link: String? = null,
    @SerializedName("read_at")
    val readAt: String? = null,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String? = null
) {
    val isRead: Boolean get() = readAt != null
    val isUnread: Boolean get() = readAt == null
}

data class NotificationsResponse(
    val data: List<AppNotification>? = null,
    val notifications: List<AppNotification>? = null,
    val message: String? = null,
    val success: Boolean? = null
)

data class NotificationResponse(
    val message: String,
    val success: Boolean
)

data class UnreadCountResponse(
    val count: Int,
    val success: Boolean? = null
)

data class FCMTokenRequest(
    val token: String,
    val device_type: String = "android"
)

// Irrigation Models
data class IrrigationSchedule(
    val id: Int,
    val date: String,
    val location: String,
    val status: String,
    val notes: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class IrrigationSchedulesResponse(
    val schedules: List<IrrigationSchedule>,
    val userLocation: String? = null,
    val message: String? = null,
    val success: Boolean? = null
)

// Crop Health Models - Fixed to match existing CropHealthRecord structure
data class CropHealthRecord(
    val id: Int,
    val image: String,
    val disease: String?,
    val confidence: Double?,
    val recommendation: String?,
    val admin_recommendation: String?,
    val details: String?,
    val notes: String?,
    val uploaded_image_url: String?,
    val predictions: String?,
    val inference_time_seconds: Double?,
    val api_status: String?,
    val is_offline: Boolean?,
    @SerializedName("created_at")
    val created_at: String,
    @SerializedName("updated_at")
    val updated_at: String? = null
)

data class CropHealthUploadResponse(
    val record: CropHealthRecord?,
    val message: String,
    val success: Boolean
)

// Generic API Response Models
data class ApiResponse(
    val message: String,
    val success: Boolean,
    val data: Any? = null
)

data class PingResponse(
    val message: String,
    val timestamp: String,
    val version: String,
    val success: Boolean = true
)

// Comment Models
data class Comment(
    val id: Int,
    val content: String,
    val author: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class CommentRequest(
    val message: String  // Changed from 'content' to 'message' to match API expectation
)

data class CommentResponse(
    val comment: Comment,
    val message: String,
    val success: Boolean
)

// Disease Detection Models - Updated to match your existing structure
data class DiseaseDetectionResponse(
    val is_rice_leaf: Boolean,
    val disease: String?,
    val confidence: Double?,
    val recommendation: String?,
    val details: String?,
    val predictions: List<String>?,
    val inference_time_seconds: Double?,
    val api_status: String?,
    val is_offline: Boolean,
    val message: String?,
    val success: Boolean = true
)

data class DiseasePrediction(
    val disease: String,
    val confidence: Double,
    val description: String?
)

data class ApiDiseaseDetectionResponse(
    val success: Boolean,
    val message: String,
    val data: DiseaseDetectionResponse?
)

data class ApiErrorResponse(
    val error: String,
    val message: String
)
