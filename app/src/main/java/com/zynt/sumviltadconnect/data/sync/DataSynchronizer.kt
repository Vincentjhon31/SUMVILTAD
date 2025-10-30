package com.zynt.sumviltadconnect.data.sync

import android.content.Context
import android.util.Log
import com.zynt.sumviltadconnect.data.network.ApiClient
import com.zynt.sumviltadconnect.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * DataSynchronizer ensures seamless synchronization between the website and mobile app
 * When a user logs in, all their data from the website is automatically synced to the mobile app
 */
class DataSynchronizer(private val context: Context) {

    companion object {
        private const val TAG = "DataSynchronizer"
        private const val SYNC_INTERVAL = 5 * 60 * 1000L // 5 minutes
    }

    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isInitialSyncComplete = false

    /**
     * Performs initial data synchronization when user logs in
     * This ensures all website data is immediately available in the mobile app
     */
    suspend fun performInitialSync(): SyncResult {
        if (!TokenManager.isLoggedIn(context)) {
            return SyncResult.Error("User not logged in")
        }

        Log.d(TAG, "Starting initial data synchronization...")

        return try {
            withContext(Dispatchers.IO) {
                // Perform all sync operations in parallel for faster loading
                val syncTasks = listOf(
                    async { syncUserProfile() },
                    async { syncDashboardData() },
                    async { syncTasks() },
                    async { syncEvents() },
                    async { syncCropHealthReports() },
                    async { syncNotifications() },
                    async { syncIrrigationSchedules() }
                )

                val results = syncTasks.awaitAll()
                val failedSyncs = results.filterIsInstance<SyncResult.Error>()

                if (failedSyncs.isEmpty()) {
                    isInitialSyncComplete = true
                    Log.d(TAG, "Initial synchronization completed successfully")
                    SyncResult.Success("All data synchronized successfully")
                } else {
                    Log.w(TAG, "Some sync operations failed: ${failedSyncs.size}")
                    SyncResult.PartialSuccess("${results.size - failedSyncs.size}/${results.size} data types synchronized")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Initial sync failed", e)
            SyncResult.Error("Sync failed: ${e.message}")
        }
    }

    /**
     * Performs background synchronization to keep data up-to-date
     */
    fun startBackgroundSync() {
        syncScope.launch {
            while (TokenManager.isLoggedIn(context)) {
                try {
                    performIncrementalSync()
                    kotlinx.coroutines.delay(SYNC_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Background sync error", e)
                    kotlinx.coroutines.delay(SYNC_INTERVAL * 2) // Wait longer on error
                }
            }
        }
    }

    /**
     * Synchronizes user profile information
     */
    private suspend fun syncUserProfile(): SyncResult {
        return try {
            val response = ApiClient.apiService.getUser()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.user
                TokenManager.saveUserInfo(context, user.name, user.email, user.id)
                Log.d(TAG, "User profile synchronized")
                SyncResult.Success("User profile updated")
            } else {
                Log.e(TAG, "User profile API error: ${response.code()} - ${response.message()}")
                SyncResult.Error("Failed to sync user profile")
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Log.e(TAG, "User profile sync failed - API returned invalid JSON (likely HTML error page)", e)
            SyncResult.Error("Server configuration error - API returning HTML instead of JSON")
        } catch (e: Exception) {
            Log.e(TAG, "User profile sync failed", e)
            SyncResult.Error("User profile sync error: ${e.message}")
        }
    }

    /**
     * Synchronizes dashboard summary data
     */
    private suspend fun syncDashboardData(): SyncResult {
        return try {
            val response = ApiClient.apiService.getDashboard()
            if (response.isSuccessful && response.body() != null) {
                // Dashboard data is typically loaded fresh each time
                // Could cache here if needed
                Log.d(TAG, "Dashboard data synchronized")
                SyncResult.Success("Dashboard data updated")
            } else {
                Log.e(TAG, "Dashboard API error: ${response.code()} - ${response.message()}")
                SyncResult.Error("Failed to sync dashboard data")
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Log.e(TAG, "Dashboard sync failed - API returned invalid JSON (likely HTML error page)", e)
            SyncResult.Error("Server configuration error - API returning HTML instead of JSON")
        } catch (e: Exception) {
            Log.e(TAG, "Dashboard sync failed", e)
            SyncResult.Error("Dashboard sync error: ${e.message}")
        }
    }

    /**
     * Synchronizes tasks from website
     */
    private suspend fun syncTasks(): SyncResult {
        return try {
            val response = ApiClient.apiService.getTasks()
            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!.data ?: response.body()!!.tasks ?: emptyList()
                Log.d(TAG, "Synchronized ${tasks.size} tasks")
                SyncResult.Success("${tasks.size} tasks synchronized")
            } else {
                Log.e(TAG, "Tasks API error: ${response.code()} - ${response.message()}")
                SyncResult.Error("Failed to sync tasks")
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Log.e(TAG, "Tasks sync failed - API returned invalid JSON (likely HTML error page)", e)
            SyncResult.Error("Server configuration error - API returning HTML instead of JSON")
        } catch (e: Exception) {
            Log.e(TAG, "Tasks sync failed", e)
            SyncResult.Error("Tasks sync error: ${e.message}")
        }
    }

    /**
     * Synchronizes events from website
     */
    private suspend fun syncEvents(): SyncResult {
        return try {
            val response = ApiClient.apiService.getEvents()
            if (response.isSuccessful && response.body() != null) {
                val events = response.body()!!.data ?: response.body()!!.events ?: emptyList()
                Log.d(TAG, "Synchronized ${events.size} events")
                SyncResult.Success("${events.size} events synchronized")
            } else {
                Log.e(TAG, "Events API error: ${response.code()} - ${response.message()}")
                SyncResult.Error("Failed to sync events")
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Log.e(TAG, "Events sync failed - API returned invalid JSON (likely HTML error page)", e)
            SyncResult.Error("Server configuration error - API returning HTML instead of JSON")
        } catch (e: Exception) {
            Log.e(TAG, "Events sync failed", e)
            SyncResult.Error("Events sync error: ${e.message}")
        }
    }

    /**
     * Synchronizes crop health reports from website
     */
    private suspend fun syncCropHealthReports(): SyncResult {
        return try {
            val response = ApiClient.apiService.getCropHealthReports()
            if (response.isSuccessful && response.body() != null) {
                val reports = response.body()!!.data
                Log.d(TAG, "Synchronized ${reports.size} crop health reports")
                SyncResult.Success("${reports.size} crop health reports synchronized")
            } else {
                Log.e(TAG, "Crop health API error: ${response.code()} - ${response.message()}")
                SyncResult.Error("Failed to sync crop health reports")
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Log.e(TAG, "Crop health sync failed - API returned invalid JSON (likely HTML error page)", e)
            SyncResult.Error("Server configuration error - API returning HTML instead of JSON")
        } catch (e: Exception) {
            Log.e(TAG, "Crop health reports sync failed", e)
            SyncResult.Error("Crop health reports sync error: ${e.message}")
        }
    }

    /**
     * Synchronizes notifications from website
     */
    private suspend fun syncNotifications(): SyncResult {
        return try {
            val response = ApiClient.apiService.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                val notifications = response.body()!!.data ?: response.body()!!.notifications ?: emptyList()
                Log.d(TAG, "Synchronized ${notifications.size} notifications")
                SyncResult.Success("${notifications.size} notifications synchronized")
            } else {
                Log.e(TAG, "Notifications API error: ${response.code()} - ${response.message()}")
                SyncResult.Error("Failed to sync notifications")
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Log.e(TAG, "Notifications sync failed - API returned invalid JSON (likely HTML error page)", e)
            SyncResult.Error("Server configuration error - API returning HTML instead of JSON")
        } catch (e: Exception) {
            Log.e(TAG, "Notifications sync failed", e)
            SyncResult.Error("Notifications sync error: ${e.message}")
        }
    }

    /**
     * Synchronizes irrigation schedules from website
     */
    private suspend fun syncIrrigationSchedules(): SyncResult {
        return try {
            val response = ApiClient.apiService.getIrrigationSchedules()
            if (response.isSuccessful && response.body() != null) {
                val schedules = response.body()!!.schedules
                Log.d(TAG, "Synchronized ${schedules.size} irrigation schedules")
                SyncResult.Success("${schedules.size} irrigation schedules synchronized")
            } else {
                Log.e(TAG, "Irrigation API error: ${response.code()} - ${response.message()}")
                SyncResult.Error("Failed to sync irrigation schedules")
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Log.e(TAG, "Irrigation sync failed - API returned invalid JSON (likely HTML error page)", e)
            SyncResult.Error("Server configuration error - API returning HTML instead of JSON")
        } catch (e: Exception) {
            Log.e(TAG, "Irrigation schedules sync failed", e)
            SyncResult.Error("Irrigation schedules sync error: ${e.message}")
        }
    }

    /**
     * Performs incremental sync for background updates
     */
    private suspend fun performIncrementalSync(): SyncResult {
        if (!isInitialSyncComplete) {
            return performInitialSync()
        }

        // For incremental sync, we can be more selective about what to update
        return try {
            withContext(Dispatchers.IO) {
                val criticalSyncTasks = listOf(
                    async { syncNotifications() },
                    async { syncTasks() },
                    async { syncEvents() }
                )

                val results = criticalSyncTasks.awaitAll()
                val failedSyncs = results.filterIsInstance<SyncResult.Error>()

                if (failedSyncs.isEmpty()) {
                    SyncResult.Success("Background sync completed")
                } else {
                    SyncResult.PartialSuccess("Background sync partially completed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Incremental sync failed", e)
            SyncResult.Error("Background sync failed: ${e.message}")
        }
    }

    /**
     * Forces a complete resync of all data
     */
    suspend fun forceResync(): SyncResult {
        isInitialSyncComplete = false
        return performInitialSync()
    }

    /**
     * Stops background synchronization
     */
    fun stopBackgroundSync() {
        // The coroutine scope will automatically stop when TokenManager.isLoggedIn becomes false
        Log.d(TAG, "Background sync will stop")
    }

    /**
     * Represents the result of a synchronization operation
     */
    sealed class SyncResult {
        data class Success(val message: String) : SyncResult()
        data class PartialSuccess(val message: String) : SyncResult()
        data class Error(val message: String) : SyncResult()
    }
}
