package com.zynt.sumviltadconnect.utils

import android.content.Context
import android.util.Log
import com.zynt.sumviltadconnect.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException

/**
 * API Connection Diagnostic Tool
 * Helps identify and troubleshoot API connection issues
 */
object ApiConnectionDiagnostic {
    private const val TAG = "ApiConnectionDiagnostic"

    data class DiagnosticResult(
        val isSuccess: Boolean,
        val statusCode: Int,
        val message: String,
        val suggestions: List<String>
    )

    /**
     * Performs comprehensive API connection diagnostics
     */
    suspend fun performDiagnostics(context: Context): DiagnosticResult {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Starting API connection diagnostics...")

            try {
                // Test basic connectivity first
                val baseUrl = ApiClient.getBaseUrl()
                Log.d(TAG, "Testing connection to: $baseUrl")

                // Try a simple API call
                val response = ApiClient.apiService.getDashboard()

                when {
                    response.isSuccessful -> {
                        Log.d(TAG, "✅ API connection successful")
                        DiagnosticResult(
                            isSuccess = true,
                            statusCode = response.code(),
                            message = "API connection is working properly",
                            suggestions = emptyList()
                        )
                    }

                    response.code() == 404 -> {
                        Log.w(TAG, "⚠️ API endpoint not found (404)")
                        DiagnosticResult(
                            isSuccess = false,
                            statusCode = 404,
                            message = "API endpoints not found on server",
                            suggestions = listOf(
                                "Verify the server URL is correct: $baseUrl",
                                "Check if the Laravel API routes are properly configured",
                                "Ensure the web server is serving the API endpoints",
                                "Try switching to localhost mode for testing"
                            )
                        )
                    }

                    response.code() == 401 -> {
                        Log.w(TAG, "⚠️ Authentication failed (401)")
                        DiagnosticResult(
                            isSuccess = false,
                            statusCode = 401,
                            message = "Authentication token is invalid or expired",
                            suggestions = listOf(
                                "Try logging out and logging in again",
                                "Check if the authentication system is working on the website",
                                "Verify the API authentication endpoints"
                            )
                        )
                    }

                    response.code() == 500 -> {
                        Log.e(TAG, "❌ Server error (500)")
                        DiagnosticResult(
                            isSuccess = false,
                            statusCode = 500,
                            message = "Server internal error",
                            suggestions = listOf(
                                "Check server logs for errors",
                                "Verify database connection on server",
                                "Ensure Laravel environment is properly configured"
                            )
                        )
                    }

                    else -> {
                        val errorBody = response.errorBody()?.string()
                        Log.w(TAG, "⚠️ API response error: ${response.code()}")
                        Log.w(TAG, "Error body: $errorBody")

                        val isHtmlResponse = errorBody?.contains("<html", ignoreCase = true) == true ||
                                           errorBody?.contains("<!DOCTYPE", ignoreCase = true) == true

                        if (isHtmlResponse) {
                            DiagnosticResult(
                                isSuccess = false,
                                statusCode = response.code(),
                                message = "Server is returning HTML instead of JSON - likely a configuration issue",
                                suggestions = listOf(
                                    "Check if the server is properly configured to serve API endpoints",
                                    "Verify the .htaccess file is correctly configured",
                                    "Ensure Laravel routes are working properly",
                                    "Check if the API is behind a web server that's redirecting to a default page"
                                )
                            )
                        } else {
                            DiagnosticResult(
                                isSuccess = false,
                                statusCode = response.code(),
                                message = "API returned error: ${response.message()}",
                                suggestions = listOf(
                                    "Check server logs for more details",
                                    "Verify API endpoint configuration",
                                    "Test the API directly with tools like Postman"
                                )
                            )
                        }
                    }
                }

            } catch (e: IOException) {
                Log.e(TAG, "❌ Network connection failed", e)
                DiagnosticResult(
                    isSuccess = false,
                    statusCode = -1,
                    message = "Network connection failed: ${e.message}",
                    suggestions = listOf(
                        "Check internet connection",
                        "Verify the server URL is accessible",
                        "Check if the server is running",
                        "Try using localhost mode if testing locally"
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error during diagnostics", e)
                DiagnosticResult(
                    isSuccess = false,
                    statusCode = -1,
                    message = "Diagnostic failed: ${e.message}",
                    suggestions = listOf(
                        "Check application logs for more details",
                        "Restart the application",
                        "Clear app data and try again"
                    )
                )
            }
        }
    }

    /**
     * Tests specific API endpoints
     */
    suspend fun testEndpoint(endpointName: String): DiagnosticResult {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<*> = when (endpointName) {
                    "tasks" -> ApiClient.apiService.getTasks()
                    "events" -> ApiClient.apiService.getEvents()
                    "notifications" -> ApiClient.apiService.getNotifications()
                    "dashboard" -> ApiClient.apiService.getDashboard()
                    "user" -> ApiClient.apiService.getUser()
                    else -> return@withContext DiagnosticResult(
                        false, -1, "Unknown endpoint: $endpointName", emptyList()
                    )
                }

                if (response.isSuccessful) {
                    DiagnosticResult(
                        true, response.code(),
                        "$endpointName endpoint is working",
                        emptyList()
                    )
                } else {
                    DiagnosticResult(
                        false, response.code(),
                        "$endpointName endpoint failed: ${response.message()}",
                        listOf("Check server configuration for this endpoint")
                    )
                }

            } catch (e: Exception) {
                DiagnosticResult(
                    false, -1,
                    "$endpointName endpoint error: ${e.message}",
                    listOf("Check network connection and server status")
                )
            }
        }
    }

    /**
     * Switches to localhost mode for development testing
     */
    fun enableLocalhostMode(context: Context) {
        ApiClient.setUseLocalhost(true)
        Log.d(TAG, "Switched to localhost mode: ${ApiClient.getBaseUrl()}")
    }

    /**
     * Switches back to production server
     */
    fun enableProductionMode(context: Context) {
        ApiClient.setUseLocalhost(false)
        Log.d(TAG, "Switched to production mode: ${ApiClient.getBaseUrl()}")
    }
}
