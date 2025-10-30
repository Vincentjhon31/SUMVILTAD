package com.zynt.sumviltadconnect.utils

import android.util.Log
import com.zynt.sumviltadconnect.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object ApiEndpointTester {
    private const val TAG = "ApiEndpointTester"

    data class EndpointTestResult(
        val endpoint: String,
        val isSuccess: Boolean,
        val statusCode: Int,
        val responseType: ResponseType,
        val errorMessage: String? = null
    )

    enum class ResponseType {
        JSON,
        HTML,
        TEXT,
        ERROR,
        UNKNOWN
    }

    private val commonEndpoints = listOf(
        "",
        "api/",
        "api/user",
        "api/login",
        "api/health"
    )

    suspend fun testAllEndpoints(): List<EndpointTestResult> = withContext(Dispatchers.IO) {
        val baseUrl = ApiClient.getBaseUrl()
        val results = mutableListOf<EndpointTestResult>()

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        for (endpoint in commonEndpoints) {
            try {
                val fullUrl = "${baseUrl.trimEnd('/')}/${endpoint.trimStart('/')}"
                val request = Request.Builder()
                    .url(fullUrl)
                    .header("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val contentType = response.header("Content-Type") ?: ""

                val responseType = when {
                    contentType.contains("application/json", ignoreCase = true) -> ResponseType.JSON
                    contentType.contains("text/html", ignoreCase = true) ||
                    responseBody.trimStart().startsWith("<html", ignoreCase = true) ||
                    responseBody.trimStart().startsWith("<!DOCTYPE", ignoreCase = true) -> ResponseType.HTML
                    contentType.contains("text/", ignoreCase = true) -> ResponseType.TEXT
                    else -> ResponseType.UNKNOWN
                }

                results.add(
                    EndpointTestResult(
                        endpoint = endpoint.ifEmpty { "root" },
                        isSuccess = response.isSuccessful && responseType == ResponseType.JSON,
                        statusCode = response.code,
                        responseType = responseType,
                        errorMessage = if (!response.isSuccessful) response.message else null
                    )
                )

                Log.d(TAG, "Tested endpoint: $endpoint, Status: ${response.code}, Type: $responseType")

            } catch (e: Exception) {
                results.add(
                    EndpointTestResult(
                        endpoint = endpoint.ifEmpty { "root" },
                        isSuccess = false,
                        statusCode = 0,
                        responseType = ResponseType.ERROR,
                        errorMessage = e.message
                    )
                )
                Log.e(TAG, "Error testing endpoint: $endpoint", e)
            }
        }

        return@withContext results
    }

    suspend fun diagnoseApiIssues(): List<String> = withContext(Dispatchers.IO) {
        val suggestions = mutableListOf<String>()
        val testResults = testAllEndpoints()
        val baseUrl = ApiClient.getBaseUrl()

        // Check if any endpoint returned JSON successfully
        val hasWorkingJsonEndpoint = testResults.any { it.isSuccess && it.responseType == ResponseType.JSON }

        if (!hasWorkingJsonEndpoint) {
            // Check if we're getting HTML responses (common redirect issue)
            val htmlResponses = testResults.filter { it.responseType == ResponseType.HTML }
            if (htmlResponses.isNotEmpty()) {
                suggestions.add("• The server is returning HTML instead of JSON. This usually indicates:")
                suggestions.add("  - The API URL is incorrect (missing /api/ path)")
                suggestions.add("  - The server is redirecting to a web page")
                suggestions.add("  - Authentication issues causing redirect to login page")
            }

            // URL format suggestions
            if (!baseUrl.contains("/api")) {
                suggestions.add("• Try adding '/api/' to your base URL")
                suggestions.add("  Example: ${baseUrl}api/")
            }

            // Connection issues
            val connectionErrors = testResults.filter { it.responseType == ResponseType.ERROR }
            if (connectionErrors.isNotEmpty()) {
                suggestions.add("• Check your internet connection")
                suggestions.add("• Verify the server is running and accessible")
                suggestions.add("• Check if the domain/IP address is correct")
            }

            // Status code analysis
            val statusCodes = testResults.map { it.statusCode }.distinct()
            when {
                statusCodes.contains(404) -> {
                    suggestions.add("• 404 errors indicate the endpoint doesn't exist")
                    suggestions.add("• Verify the correct API path structure")
                }
                statusCodes.contains(401) || statusCodes.contains(403) -> {
                    suggestions.add("• Authentication issues detected")
                    suggestions.add("• Check if API tokens are required")
                }
                statusCodes.contains(500) -> {
                    suggestions.add("• Server error detected")
                    suggestions.add("• Contact the API administrator")
                }
            }

            // Generic suggestions
            if (suggestions.isEmpty()) {
                suggestions.add("• Double-check the base URL is correct")
                suggestions.add("• Ensure the server supports CORS for mobile apps")
                suggestions.add("• Verify SSL certificates if using HTTPS")
            }
        } else {
            suggestions.add("✓ API connection is working correctly!")
        }

        return@withContext suggestions
    }

    suspend fun attemptAutoFix(): Boolean = withContext(Dispatchers.IO) {
        try {
            val baseUrl = ApiClient.getBaseUrl().trimEnd('/')
            val testUrls = listOf(
                "$baseUrl/api/",
                "$baseUrl/api",
                baseUrl
            )

            for (testUrl in testUrls) {
                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .build()

                    val request = Request.Builder()
                        .url(testUrl)
                        .header("Accept", "application/json")
                        .build()

                    val response = client.newCall(request).execute()
                    val contentType = response.header("Content-Type") ?: ""

                    if (response.isSuccessful && contentType.contains("application/json", ignoreCase = true)) {
                        // Found a working URL
                        if (testUrl != ApiClient.getBaseUrl()) {
                            ApiClient.setCustomBaseUrl(testUrl)
                            Log.d(TAG, "Auto-fix applied: Changed base URL to $testUrl")
                            return@withContext true
                        }
                        return@withContext true
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Auto-fix attempt failed for URL: $testUrl", e)
                    continue
                }
            }

            // Try common API path variations
            val urlWithApi = if (!baseUrl.endsWith("/api") && !baseUrl.endsWith("/api/")) {
                "$baseUrl/api/"
            } else null

            if (urlWithApi != null) {
                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .build()

                    val request = Request.Builder()
                        .url(urlWithApi)
                        .header("Accept", "application/json")
                        .build()

                    val response = client.newCall(request).execute()
                    val contentType = response.header("Content-Type") ?: ""

                    if (response.isSuccessful && contentType.contains("application/json", ignoreCase = true)) {
                        ApiClient.setCustomBaseUrl(urlWithApi)
                        Log.d(TAG, "Auto-fix applied: Added /api/ path")
                        return@withContext true
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Auto-fix attempt failed for API path", e)
                }
            }

            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Auto-fix failed", e)
            return@withContext false
        }
    }

    suspend fun testEndpoint(endpoint: String): EndpointTestResult = withContext(Dispatchers.IO) {
        val baseUrl = ApiClient.getBaseUrl()
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        try {
            val fullUrl = "${baseUrl.trimEnd('/')}/${endpoint.trimStart('/')}"
            val request = Request.Builder()
                .url(fullUrl)
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            val contentType = response.header("Content-Type") ?: ""

            val responseType = when {
                contentType.contains("application/json", ignoreCase = true) -> ResponseType.JSON
                contentType.contains("text/html", ignoreCase = true) ||
                responseBody.trimStart().startsWith("<html", ignoreCase = true) ||
                responseBody.trimStart().startsWith("<!DOCTYPE", ignoreCase = true) -> ResponseType.HTML
                contentType.contains("text/", ignoreCase = true) -> ResponseType.TEXT
                else -> ResponseType.UNKNOWN
            }

            return@withContext EndpointTestResult(
                endpoint = endpoint,
                isSuccess = response.isSuccessful && responseType == ResponseType.JSON,
                statusCode = response.code,
                responseType = responseType,
                errorMessage = if (!response.isSuccessful) response.message else null
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error testing endpoint: $endpoint", e)
            return@withContext EndpointTestResult(
                endpoint = endpoint,
                isSuccess = false,
                statusCode = 0,
                responseType = ResponseType.ERROR,
                errorMessage = e.message
            )
        }
    }
}
