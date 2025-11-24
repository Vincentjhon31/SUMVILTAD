package com.zynt.sumviltadconnect.data.repository

import android.util.Log
import com.zynt.sumviltadconnect.data.model.*
import com.zynt.sumviltadconnect.data.network.ApiClient
import com.zynt.sumviltadconnect.data.local.TaskCache
import com.zynt.sumviltadconnect.data.local.EventsCache
import com.zynt.sumviltadconnect.data.local.NotificationsCache
import com.zynt.sumviltadconnect.data.local.IrrigationCache
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.HttpException
import retrofit2.Response
import com.google.gson.JsonSyntaxException
import java.net.ConnectException
import java.net.SocketTimeoutException

class FarmerRepository(
    private val api: com.zynt.sumviltadconnect.data.network.ApiService = ApiClient.apiService,
    private val taskCache: TaskCache? = null,
    private val eventsCache: EventsCache? = null,
    private val notificationsCache: NotificationsCache? = null,
    private val irrigationCache: IrrigationCache? = null
) {
    private val TAG = "FarmerRepository"

    // DASHBOARD
    suspend fun dashboard(): DashboardSummary {
        return try {
            Log.d(TAG, "Fetching dashboard from: ${ApiClient.getBaseUrl()}api/dashboard")
            val response = api.getDashboard()
            if (response.isSuccessful) {
                Log.d(TAG, "Dashboard API success")
                response.body() ?: DashboardSummary(
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
            } else {
                Log.e(TAG, "Dashboard API error: ${response.code()} - ${response.message()}")
                handleApiError("Dashboard", response.code(), response.message())
                DashboardSummary(
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
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Dashboard API JSON parsing error - likely received HTML instead of JSON", e)
            logJsonParsingError("Dashboard", e)
            DashboardSummary(
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
        } catch (e: Exception) {
            Log.e(TAG, "Dashboard API exception: ${e.message}", e)
            logConnectionError("Dashboard", e)
            DashboardSummary(
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
        }
    }

    // EVENTS
    suspend fun events(): Pair<List<Event>, Boolean> {
        return try {
            Log.d(TAG, "Fetching events from: ${ApiClient.getBaseUrl()}api/events")
            val response = api.getEvents()
            if (response.isSuccessful) {
                val eventsResponse = response.body()
                val events = eventsResponse?.data ?: eventsResponse?.events ?: emptyList()
                Log.d(TAG, "Events API success, got ${events.size} events")
                eventsCache?.saveEvents(events)
                Pair(events, false)
            } else {
                Log.e(TAG, "Events API error: ${response.code()} - ${response.message()}")
                handleApiError("Events", response.code(), response.message())
                val cached = eventsCache?.loadEvents() ?: emptyList()
                Pair(cached, true)
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Events API JSON parsing error - likely received HTML instead of JSON", e)
            logJsonParsingError("Events", e)
            val cached = eventsCache?.loadEvents() ?: emptyList()
            Pair(cached, true)
        } catch (e: Exception) {
            Log.e(TAG, "Events API exception: ${e.message}", e)
            logConnectionError("Events", e)
            val cached = eventsCache?.loadEvents() ?: emptyList()
            Pair(cached, true)
        }
    }

    // TASKS
    suspend fun tasks(): Pair<List<Task>, Boolean> {
        return try {
            Log.d(TAG, "Fetching tasks from: ${ApiClient.getBaseUrl()}api/tasks")
            val response = api.getTasks()
            if (response.isSuccessful) {
                val tasksResponse = response.body()
                val tasks = tasksResponse?.data ?: tasksResponse?.tasks ?: emptyList()
                Log.d(TAG, "Tasks API success, got ${tasks.size} tasks")
                taskCache?.saveTasks(tasks)
                Pair(tasks, false)
            } else {
                Log.e(TAG, "Tasks API error: ${response.code()} - ${response.message()}")
                handleApiError("Tasks", response.code(), response.message())
                val cached = taskCache?.loadTasks() ?: emptyList()
                Pair(cached, true)
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Tasks API JSON parsing error - likely received HTML instead of JSON", e)
            logJsonParsingError("Tasks", e)
            val cached = taskCache?.loadTasks() ?: emptyList()
            Pair(cached, true)
        } catch (e: Exception) {
            Log.e(TAG, "Tasks API exception: ${e.message}", e)
            logConnectionError("Tasks", e)
            val cached = taskCache?.loadTasks() ?: emptyList()
            Pair(cached, true)
        }
    }

    // NOTIFICATIONS
    suspend fun notifications(): Pair<List<AppNotification>, Boolean> {
        return try {
            Log.d(TAG, "Fetching notifications from: ${ApiClient.getBaseUrl()}api/notifications")
            val response = api.getNotifications()
            if (response.isSuccessful) {
                val notificationsResponse = response.body()
                val notifications = notificationsResponse?.data ?: notificationsResponse?.notifications ?: emptyList()
                Log.d(TAG, "Notifications API success, got ${notifications.size} notifications")
                notificationsCache?.saveNotifications(notifications)
                Pair(notifications, false)
            } else {
                Log.e(TAG, "Notifications API error: ${response.code()} - ${response.message()}")
                handleApiError("Notifications", response.code(), response.message())
                val cached = notificationsCache?.loadNotifications() ?: emptyList()
                Pair(cached, true)
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Notifications API JSON parsing error - likely received HTML instead of JSON", e)
            logJsonParsingError("Notifications", e)
            val cached = notificationsCache?.loadNotifications() ?: emptyList()
            Pair(cached, true)
        } catch (e: Exception) {
            Log.e(TAG, "Notifications API exception: ${e.message}", e)
            logConnectionError("Notifications", e)
            val cached = notificationsCache?.loadNotifications() ?: emptyList()
            Pair(cached, true)
        }
    }

    // IRRIGATION SCHEDULES
    suspend fun irrigationSchedules(): IrrigationSchedulesResponse {
        return try {
            Log.d(TAG, "Fetching irrigation schedules from: ${ApiClient.getBaseUrl()}api/irrigation-schedules")
            val response = api.getIrrigationSchedules()
            if (response.isSuccessful) {
                Log.d(TAG, "Irrigation schedules API success")
                response.body() ?: IrrigationSchedulesResponse(emptyList())
            } else {
                Log.e(TAG, "Irrigation schedules API error: ${response.code()} - ${response.message()}")
                IrrigationSchedulesResponse(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Irrigation schedules API exception: ${e.message}", e)
            IrrigationSchedulesResponse(emptyList())
        }
    }

    // IRRIGATION REQUESTS
    suspend fun submitIrrigationRequest(
        location: String,
        requestedDate: String,
        reason: String?
    ): IrrigationRequestResponse {
        return try {
            Log.d(TAG, "Submitting irrigation request for location: $location, date: $requestedDate")
            val request = IrrigationRequestRequest(
                location = location,
                requestedDate = requestedDate,
                reason = reason
            )
            val response = api.submitIrrigationRequest(request)
            if (response.isSuccessful) {
                Log.d(TAG, "Irrigation request submitted successfully")
                response.body() ?: IrrigationRequestResponse(
                    success = false,
                    message = "Empty response from server"
                )
            } else {
                Log.e(TAG, "Irrigation request API error: ${response.code()} - ${response.message()}")
                IrrigationRequestResponse(
                    success = false,
                    message = response.message() ?: "Failed to submit request"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Irrigation request API exception: ${e.message}", e)
            IrrigationRequestResponse(
                success = false,
                message = "Network error: ${e.message}"
            )
        }
    }

    suspend fun getIrrigationRequests(): IrrigationRequestsResponse {
        return try {
            Log.d(TAG, "Fetching irrigation requests from: ${ApiClient.getBaseUrl()}api/irrigation-requests")
            val response = api.getIrrigationRequests()
            if (response.isSuccessful) {
                Log.d(TAG, "Irrigation requests API success")
                response.body() ?: IrrigationRequestsResponse(
                    success = false,
                    message = "Empty response",
                    data = emptyList()
                )
            } else {
                Log.e(TAG, "Irrigation requests API error: ${response.code()} - ${response.message()}")
                IrrigationRequestsResponse(
                    success = false,
                    message = "Failed to fetch requests",
                    data = emptyList()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Irrigation requests API exception: ${e.message}", e)
            IrrigationRequestsResponse(
                success = false,
                message = "Network error",
                data = emptyList()
            )
        }
    }

    // EVENT PARTICIPATION
    suspend fun participateInEvent(eventId: Int, status: String) {
        try {
            Log.d(TAG, "Participating in event $eventId with status: $status")
            val response = api.participateInEvent(eventId, EventParticipationRequest(status))
            if (response.isSuccessful) {
                Log.d(TAG, "Event participation success")
            } else {
                Log.e(TAG, "Event participation error: ${response.code()} - ${response.message()}")
                throw Exception("Failed to update participation: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Event participation exception: ${e.message}", e)
            throw e
        }
    }

    // EVENT DETAILS
    suspend fun getEventDetails(eventId: Int): retrofit2.Response<EventResponse> {
        return try {
            Log.d(TAG, "Fetching event details for event $eventId")
            api.getEventDetails(eventId)
        } catch (e: Exception) {
            Log.e(TAG, "Event details API exception: ${e.message}", e)
            throw e
        }
    }

    // TASK UPDATES
    suspend fun updateTask(taskId: Int, request: UpdateTaskRequest): Task {
        try {
            Log.d(TAG, "Updating task $taskId")
            val response = api.updateTask(taskId, request)
            when {
                response.isSuccessful -> {
                    val taskResponse = response.body()
                    Log.d(TAG, "Task update success")
                    return taskResponse?.task ?: throw Exception("Invalid response format")
                }
                response.code() == 401 -> {
                    Log.w(TAG, "Update task: 401 Unauthorized - but update may have succeeded")
                    throw Exception("Authentication issue, but task may have been updated")
                }
                else -> {
                    Log.e(TAG, "Task update error: ${response.code()} - ${response.message()}")
                    throw Exception("Failed to update task: ${response.message()}")
                }
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Task update JSON parsing error", e)
            logJsonParsingError("UpdateTask", e)
            throw Exception("Data format error")
        } catch (e: Exception) {
            Log.e(TAG, "Task update exception: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateTaskProgress(taskId: Int, progress: Int): Task {
        try {
            Log.d(TAG, "Updating task $taskId progress to $progress%")
            val response = api.updateTaskProgress(taskId, TaskProgressRequest(progress))
            when {
                response.isSuccessful -> {
                    val taskResponse = response.body()
                    Log.d(TAG, "Task progress update success")
                    return taskResponse?.task ?: throw Exception("Invalid response format")
                }
                response.code() == 401 -> {
                    Log.w(TAG, "Update task progress: 401 Unauthorized - but update may have succeeded")
                    throw Exception("Authentication issue, but task progress may have been updated")
                }
                else -> {
                    Log.e(TAG, "Task progress update error: ${response.code()} - ${response.message()}")
                    throw Exception("Failed to update task progress: ${response.message()}")
                }
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Task progress update JSON parsing error", e)
            logJsonParsingError("UpdateTaskProgress", e)
            throw Exception("Data format error")
        } catch (e: Exception) {
            Log.e(TAG, "Task progress update exception: ${e.message}", e)
            throw e
        }
    }

    suspend fun getTask(taskId: Int): Task {
        try {
            Log.d(TAG, "Fetching task $taskId")
            val response = api.getTask(taskId)
            when {
                response.isSuccessful -> {
                    val taskResponse = response.body()
                    Log.d(TAG, "Get task success")
                    return taskResponse?.task ?: throw Exception("Task not found")
                }
                response.code() == 404 -> {
                    Log.w(TAG, "Task $taskId not found")
                    throw Exception("Task not found")
                }
                response.code() == 401 -> {
                    Log.w(TAG, "Get task: 401 Unauthorized")
                    throw Exception("Authentication required")
                }
                else -> {
                    Log.e(TAG, "Get task error: ${response.code()} - ${response.message()}")
                    throw Exception("Failed to get task: ${response.message()}")
                }
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Get task JSON parsing error", e)
            logJsonParsingError("GetTask", e)
            throw Exception("Data format error")
        } catch (e: Exception) {
            Log.e(TAG, "Get task exception: ${e.message}", e)
            throw e
        }
    }

    // MARK NOTIFICATION AS READ
    suspend fun markNotificationRead(notificationId: Int) {
        try {
            Log.d(TAG, "Marking notification $notificationId as read")
            val response = api.markNotificationRead(notificationId)
            if (response.isSuccessful) {
                Log.d(TAG, "Notification marked as read successfully")
            } else {
                Log.e(TAG, "Mark notification read error: ${response.code()} - ${response.message()}")
                throw Exception("Failed to mark notification as read: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Mark notification read exception: ${e.message}", e)
            throw e
        }
    }

    suspend fun markNotificationUnread(notificationId: Int) {
        try {
            Log.d(TAG, "Marking notification $notificationId as unread")
            val response = api.markNotificationUnread(notificationId)
            if (response.isSuccessful) {
                Log.d(TAG, "Notification marked as unread successfully")
            } else {
                Log.e(TAG, "Mark notification unread error: ${response.code()} - ${response.message()}")
                throw Exception("Failed to mark notification as unread: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Mark notification unread exception: ${e.message}", e)
            throw e
        }
    }

    suspend fun markAllNotificationsRead() {
        try {
            Log.d(TAG, "Marking all notifications as read")
            val response = api.markAllNotificationsRead()
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully marked all notifications as read")
            } else {
                Log.e(TAG, "Failed to mark all notifications as read: ${response.code()} - ${response.message()}")
                throw Exception("Failed to mark all notifications as read")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception marking all notifications as read: ${e.message}", e)
            throw e
        }
    }

    // CROP HEALTH REPORTS
    suspend fun getCropHealthReports(): Pair<List<CropHealthRecord>, Boolean> {
        return try {
            Log.d(TAG, "Fetching crop health reports from: ${ApiClient.getBaseUrl()}api/crop-health")
            val response = api.getCropHealthReports()
            if (response.isSuccessful) {
                val cropHealthResponse = response.body()
                val reports = cropHealthResponse?.data ?: emptyList()
                Log.d(TAG, "Crop health API success, got ${reports.size} reports")
                Pair(reports, false)
            } else {
                Log.e(TAG, "Crop health API error: ${response.code()} - ${response.message()}")
                Pair(emptyList(), true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Crop health API exception: ${e.message}", e)
            Pair(emptyList(), true)
        }
    }

    // Add methods that your ViewModels expect
    suspend fun getCropHealthHistory(): CropHealthResponse {
        return try {
            Log.d(TAG, "Fetching crop health history from: ${ApiClient.getBaseUrl()}api/crop-health")
            val response = api.getCropHealthReports()
            if (response.isSuccessful) {
                response.body() ?: CropHealthResponse(emptyList(), "No data", 0)
            } else {
                Log.e(TAG, "Crop health history API error: ${response.code()} - ${response.message()}")
                CropHealthResponse(emptyList(), "Error loading data", 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Crop health history API exception: ${e.message}", e)
            CropHealthResponse(emptyList(), "Error loading data", 0)
        }
    }

    suspend fun getCropHealthHistoryPaginated(page: Int = 1, perPage: Int = 10): PaginatedCropHealthResponse {
        return try {
            Log.d(TAG, "Fetching paginated crop health history page $page (per_page=$perPage) from: ${ApiClient.getBaseUrl()}api/crop-health")
            val response = api.getCropHealthReportsPaginated(page, perPage)
            if (response.isSuccessful) {
                response.body() ?: PaginatedCropHealthResponse(emptyList(), "No data", 0, 1, 1, perPage, 0, null, null, false)
            } else {
                Log.e(TAG, "Paginated crop health API error: ${response.code()} - ${response.message()}")
                // Fallback: fetch all and paginate client-side
                val fallbackResponse = api.getCropHealthReports()
                if (fallbackResponse.isSuccessful) {
                    val basicResponse = fallbackResponse.body() ?: CropHealthResponse(emptyList(), "No data", 0)
                    val allRecords = basicResponse.data
                    val totalRecords = allRecords.size
                    val totalPages = if (totalRecords == 0) 1 else (totalRecords + perPage - 1) / perPage
                    val safePage = page.coerceIn(1, totalPages)
                    val startIndex = (safePage - 1) * perPage
                    val endIndex = kotlin.math.min(startIndex + perPage, totalRecords)
                    val pageRecords = if (startIndex < totalRecords) {
                        allRecords.subList(startIndex, endIndex)
                    } else {
                        emptyList()
                    }
                    PaginatedCropHealthResponse(
                        data = pageRecords,
                        message = basicResponse.message,
                        count = pageRecords.size,
                        current_page = safePage,
                        last_page = totalPages,
                        per_page = perPage,
                        total = totalRecords,
                        from = if (pageRecords.isNotEmpty()) startIndex + 1 else null,
                        to = if (pageRecords.isNotEmpty()) endIndex else null,
                        has_more_pages = safePage < totalPages
                    )
                } else {
                    PaginatedCropHealthResponse(emptyList(), "Error loading data", 0, 1, 1, perPage, 0, null, null, false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Paginated crop health API exception: ${e.message}", e)
            // Fallback to client-side pagination
            try {
                val fallbackResponse = api.getCropHealthReports()
                if (fallbackResponse.isSuccessful) {
                    val basicResponse = fallbackResponse.body() ?: CropHealthResponse(emptyList(), "No data", 0)
                    val allRecords = basicResponse.data
                    val totalRecords = allRecords.size
                    val totalPages = if (totalRecords == 0) 1 else (totalRecords + perPage - 1) / perPage
                    val safePage = page.coerceIn(1, totalPages)
                    val startIndex = (safePage - 1) * perPage
                    val endIndex = kotlin.math.min(startIndex + perPage, totalRecords)
                    val pageRecords = if (startIndex < totalRecords) {
                        allRecords.subList(startIndex, endIndex)
                    } else {
                        emptyList()
                    }
                    PaginatedCropHealthResponse(
                        data = pageRecords,
                        message = basicResponse.message,
                        count = pageRecords.size,
                        current_page = safePage,
                        last_page = totalPages,
                        per_page = perPage,
                        total = totalRecords,
                        from = if (pageRecords.isNotEmpty()) startIndex + 1 else null,
                        to = if (pageRecords.isNotEmpty()) endIndex else null,
                        has_more_pages = safePage < totalPages
                    )
                } else {
                    PaginatedCropHealthResponse(emptyList(), "Error loading data", 0, 1, 1, perPage, 0, null, null, false)
                }
            } catch (fallbackException: Exception) {
                Log.e(TAG, "Fallback pagination also failed: ${fallbackException.message}", fallbackException)
                PaginatedCropHealthResponse(emptyList(), "Error loading data", 0, 1, 1, perPage, 0, null, null, false)
            }
        }
    }

    suspend fun deleteCropHealthRecord(recordId: Int): Boolean {
        return try {
            Log.d(TAG, "Deleting crop health record $recordId from: ${ApiClient.getBaseUrl()}api/crop-health/$recordId")
            val response = api.deleteCropHealthReport(recordId)

            when {
                response.isSuccessful -> {
                    Log.d(TAG, "Crop health record $recordId deleted successfully")
                    true
                }
                response.code() == 401 -> {
                    Log.w(TAG, "Delete crop health record: 401 Unauthorized - but deletion may have succeeded")
                    Log.w(TAG, "This is a known issue where the server returns 401 but the deletion works")
                    // Return true since the deletion actually works despite the 401 error
                    true
                }
                response.code() == 404 -> {
                    Log.w(TAG, "Delete crop health record: 404 Not Found - record may already be deleted")
                    // Return true since the record is effectively deleted
                    true
                }
                else -> {
                    Log.e(TAG, "Delete crop health record error: ${response.code()} - ${response.message()}")
                    // For other errors, we're not sure if it worked
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Delete crop health record exception: ${e.message}", e)
            false
        }
    }

    // TEST API CONNECTION
    suspend fun testConnection(): Boolean {
        return try {
            Log.d(TAG, "Testing API connection to: ${ApiClient.getBaseUrl()}api/ping")
            val response = api.ping()
            val isSuccess = response.isSuccessful
            Log.d(TAG, "API ping test result: $isSuccess")
            isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "API ping test failed: ${e.message}", e)
            false
        }
    }

    /**
     * Handle API errors with detailed logging and suggestions
     */
    private fun handleApiError(endpoint: String, statusCode: Int, message: String?) {
        when (statusCode) {
            401 -> {
                Log.e(TAG, "$endpoint: Unauthorized (401) - Token may be expired or invalid")
                Log.e(TAG, "Suggestion: Try logging out and logging back in")
            }
            403 -> {
                Log.e(TAG, "$endpoint: Forbidden (403) - Insufficient permissions")
            }
            404 -> {
                Log.e(TAG, "$endpoint: Not Found (404) - API endpoint may not exist")
                Log.e(TAG, "Current base URL: ${ApiClient.getBaseUrl()}")
                Log.e(TAG, "Suggestion: Check if the API base URL is correct")
            }
            500 -> {
                Log.e(TAG, "$endpoint: Internal Server Error (500) - Server-side issue")
            }
            502 -> {
                Log.e(TAG, "$endpoint: Bad Gateway (502) - Possible HTML response instead of JSON")
                Log.e(TAG, "Suggestion: The server may be returning HTML instead of JSON responses")
            }
            else -> {
                Log.e(TAG, "$endpoint: HTTP $statusCode - $message")
            }
        }
    }

    /**
     * Log detailed information about JSON parsing errors
     */
    private fun logJsonParsingError(endpoint: String, exception: JsonSyntaxException) {
        Log.e(TAG, "=== JSON PARSING ERROR DETAILS ===")
        Log.e(TAG, "Endpoint: $endpoint")
        Log.e(TAG, "Current API URL: ${ApiClient.getBaseUrl()}")
        Log.e(TAG, "Error: ${exception.message}")
        Log.e(TAG, "This usually means the server returned HTML instead of JSON")
        Log.e(TAG, "Common causes:")
        Log.e(TAG, "1. Wrong API base URL (pointing to website instead of API)")
        Log.e(TAG, "2. Server configuration issue")
        Log.e(TAG, "3. Authentication redirecting to login page")
        Log.e(TAG, "4. Missing '/api' in the base URL")
        Log.e(TAG, "Suggestion: Try using the API diagnostic tool")
        Log.e(TAG, "=== END ERROR DETAILS ===")
    }

    /**
     * Log connection-related errors with helpful information
     */
    private fun logConnectionError(endpoint: String, exception: Exception) {
        Log.e(TAG, "=== CONNECTION ERROR DETAILS ===")
        Log.e(TAG, "Endpoint: $endpoint")
        Log.e(TAG, "Current API URL: ${ApiClient.getBaseUrl()}")
        Log.e(TAG, "Error Type: ${exception.javaClass.simpleName}")
        Log.e(TAG, "Error Message: ${exception.message}")

        when (exception) {
            is ConnectException -> {
                Log.e(TAG, "Connection failed - server may be down or URL incorrect")
            }
            is SocketTimeoutException -> {
                Log.e(TAG, "Connection timed out - slow network or server overloaded")
            }
            is HttpException -> {
                Log.e(TAG, "HTTP ${exception.code()}: ${exception.message()}")
            }
        }

        Log.e(TAG, "Suggestions:")
        Log.e(TAG, "1. Check internet connection")
        Log.e(TAG, "2. Verify API URL is correct")
        Log.e(TAG, "3. Check if server is running")
        Log.e(TAG, "4. Use diagnostic tool to test endpoints")
        Log.e(TAG, "=== END ERROR DETAILS ===")
    }
}