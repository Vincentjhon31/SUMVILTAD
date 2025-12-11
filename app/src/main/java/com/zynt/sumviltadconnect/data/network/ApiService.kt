package com.zynt.sumviltadconnect.data.network

import com.zynt.sumviltadconnect.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Authentication endpoints
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/logout")
    suspend fun logout(): Response<LogoutResponse>

    @GET("api/user")
    suspend fun getUser(): Response<UserResponse>

    @PATCH("api/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponse>

    // Farm Area Management
    @POST("api/farm-areas")
    suspend fun createFarmArea(@Body request: FarmAreaRequest): Response<FarmAreaResponse>

    @PATCH("api/farm-areas/{id}")
    suspend fun updateFarmArea(@Path("id") id: Int, @Body request: FarmAreaRequest): Response<FarmAreaResponse>

    @DELETE("api/farm-areas/{id}")
    suspend fun deleteFarmArea(@Path("id") id: Int): Response<ApiResponse>

    // Dashboard endpoint
    @GET("api/dashboard")
    suspend fun getDashboard(): Response<DashboardSummary>

    // Task Management endpoints
    @GET("api/tasks")
    suspend fun getTasks(): Response<TaskResponse>

    @GET("api/tasks/{id}")
    suspend fun getTask(@Path("id") id: Int): Response<TaskResponse>

    @PUT("api/tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Body request: UpdateTaskRequest
    ): Response<TaskResponse>

    @PATCH("api/tasks/{id}/progress")
    suspend fun updateTaskProgress(
        @Path("id") id: Int,
        @Body request: TaskProgressRequest
    ): Response<TaskResponse>

    // Events endpoints
    @GET("api/events")
    suspend fun getEvents(): Response<EventsResponse>

    @GET("api/events/{id}")
    suspend fun getEventDetails(@Path("id") id: Int): Response<EventResponse>

    @POST("api/events/{id}/participate")
    suspend fun participateInEvent(@Path("id") eventId: Int, @Body request: EventParticipationRequest): Response<EventParticipationResponse>

    // Notifications endpoints
    @GET("api/notifications")
    suspend fun getNotifications(): Response<NotificationsResponse>

    @POST("api/notifications/{id}/mark-read")
    suspend fun markNotificationRead(@Path("id") id: Int): Response<NotificationResponse>

    @POST("api/notifications/{id}/mark-unread")
    suspend fun markNotificationUnread(@Path("id") id: Int): Response<NotificationResponse>

    @POST("api/notifications/mark-all-read")
    suspend fun markAllNotificationsRead(): Response<NotificationResponse>

    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: Int): Response<NotificationResponse>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadNotificationsCount(): Response<UnreadCountResponse>

    @POST("api/notifications/fcm-token")
    suspend fun storeFcmToken(
        @Header("Authorization") token: String,
        @Body request: Map<String, String>
    ): Response<NotificationResponse>

    // Irrigation schedules endpoint
    @GET("api/irrigation-schedules")
    suspend fun getIrrigationSchedules(): Response<IrrigationSchedulesResponse>

    // Irrigation request endpoints
    @POST("api/irrigation-requests")
    suspend fun submitIrrigationRequest(@Body request: IrrigationRequestRequest): Response<IrrigationRequestResponse>

    @GET("api/irrigation-requests")
    suspend fun getIrrigationRequests(): Response<IrrigationRequestsResponse>

    // Crop Health / Disease Detection endpoints - Fixed to match existing models
    @GET("api/crop-health")
    suspend fun getCropHealthReports(): Response<CropHealthResponse>

    @GET("api/crop-health")
    suspend fun getCropHealthReportsPaginated(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<PaginatedCropHealthResponse>

    @Multipart
    @POST("api/crop-health/upload")
    suspend fun uploadCropHealthImage(
        @Part image: MultipartBody.Part,
        @Part("notes") notes: RequestBody?
    ): Response<CropHealthUploadResponse>

    @DELETE("api/crop-health/{id}")
    suspend fun deleteCropHealthReport(@Path("id") id: Int): Response<ApiResponse>

    @GET("api/crop-health/{id}/comments")
    suspend fun getCropHealthComments(@Path("id") id: Int): Response<CommentsResponse>

    @POST("api/crop-health/{id}/comments")
    suspend fun addCropHealthComment(@Path("id") id: Int, @Body request: CommentRequest): Response<CommentResponse>

    // Email verification via link
    @GET
    suspend fun verifyEmailViaLink(@Url url: String): Response<AuthResponse>

    // Test endpoint to verify API connectivity
    @GET("api/ping")
    suspend fun ping(): Response<PingResponse>
}
