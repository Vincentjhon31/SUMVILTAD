package com.zynt.sumviltadconnect.utils

import android.util.Log
import com.zynt.sumviltadconnect.data.network.ApiClient
import com.zynt.sumviltadconnect.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Context

object ApiDebugHelper {
    private const val TAG = "ApiDebugHelper"

    fun logApiConfiguration(context: Context) {
        Log.d(TAG, "=== API Configuration Debug ===")
        Log.d(TAG, "Base URL: ${ApiClient.getBaseUrl()}")

        val token = TokenManager.getToken(context)
        Log.d(TAG, "Token exists: ${token != null}")
        if (token != null) {
            Log.d(TAG, "Token preview: ${token.take(20)}...")
        }

        Log.d(TAG, "Full API endpoints:")
        Log.d(TAG, "- Dashboard: ${ApiClient.getBaseUrl()}api/dashboard")
        Log.d(TAG, "- Events: ${ApiClient.getBaseUrl()}api/events")
        Log.d(TAG, "- Tasks: ${ApiClient.getBaseUrl()}api/tasks")
        Log.d(TAG, "- Notifications: ${ApiClient.getBaseUrl()}api/notifications")
        Log.d(TAG, "- Irrigation: ${ApiClient.getBaseUrl()}api/irrigation-schedules")
    }

    fun testApiConnectivity(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        scope.launch {
            try {
                Log.d(TAG, "=== Testing API Connectivity ===")
                logApiConfiguration(context)

                // Test each endpoint
                testEndpoint("Dashboard", "api/dashboard") {
                    ApiClient.apiService.getDashboard()
                }

                testEndpoint("Events", "api/events") {
                    ApiClient.apiService.getEvents()
                }

                testEndpoint("Tasks", "api/tasks") {
                    ApiClient.apiService.getTasks()
                }

                testEndpoint("Notifications", "api/notifications") {
                    ApiClient.apiService.getNotifications()
                }

                testEndpoint("Irrigation", "api/irrigation-schedules") {
                    ApiClient.apiService.getIrrigationSchedules()
                }

            } catch (e: Exception) {
                Log.e(TAG, "API connectivity test failed", e)
            }
        }
    }

    private suspend fun testEndpoint(name: String, endpoint: String, apiCall: suspend () -> Any) {
        try {
            Log.d(TAG, "Testing $name endpoint: ${ApiClient.getBaseUrl()}$endpoint")
            val result = apiCall()
            Log.d(TAG, "✅ $name endpoint successful")
        } catch (e: Exception) {
            Log.e(TAG, "❌ $name endpoint failed: ${e.message}")

            // Log specific error details
            when {
                e.message?.contains("Unable to resolve host") == true -> {
                    Log.e(TAG, "   → Network/DNS issue - check internet connection")
                }
                e.message?.contains("401") == true -> {
                    Log.e(TAG, "   → Authentication issue - check token")
                }
                e.message?.contains("404") == true -> {
                    Log.e(TAG, "   → Endpoint not found - check Laravel routes")
                }
                e.message?.contains("500") == true -> {
                    Log.e(TAG, "   → Server error - check Laravel backend")
                }
                else -> {
                    Log.e(TAG, "   → Unknown error: ${e.javaClass.simpleName}")
                }
            }
        }
    }

    fun logResponseDetails(endpoint: String, responseCode: Int, responseMessage: String, responseBody: String?) {
        Log.d(TAG, "=== Response Details for $endpoint ===")
        Log.d(TAG, "Code: $responseCode")
        Log.d(TAG, "Message: $responseMessage")
        if (responseBody != null) {
            val preview = if (responseBody.length > 200) {
                responseBody.take(200) + "..."
            } else {
                responseBody
            }
            Log.d(TAG, "Body preview: $preview")
        }
    }
}
