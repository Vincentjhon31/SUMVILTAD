package com.zynt.sumviltadconnect.utils

import android.content.Context
import android.util.Log
import com.zynt.sumviltadconnect.data.network.ApiClient
import kotlinx.coroutines.*

/**
 * Enhanced API diagnostic tool specifically for testing Laravel API endpoints
 * This matches the API routes from your hosted website
 */
object ApiFixDemo {
    private const val TAG = "ApiFixDemo"

    /**
     * Run a comprehensive API diagnostic test
     * Tests all the specific endpoints from your Laravel API
     */
    suspend fun runDiagnosticTest(context: Context) {
        Log.d(TAG, "=== STARTING LARAVEL API DIAGNOSTIC TEST ===")

        // Initialize ApiClient if not already done
        ApiClient.initialize(context)

        Log.d(TAG, "Current API URL: ${ApiClient.getBaseUrl()}")

        // Test basic connection first
        Log.d(TAG, "Testing basic connection...")
        val connectionResult = ApiClient.testConnection()
        Log.d(TAG, "Basic connection result: $connectionResult")

        // Test specific Laravel API endpoints
        val testEndpoints = listOf(
            // Public endpoints (no auth needed)
            "api/ping",
            "api/register",
            "api/login",

            // Root paths
            "",
            "api/",

            // Protected endpoints (might need auth)
            "api/user",
            "api/dashboard",
            "api/tasks",
            "api/events",
            "api/notifications",
            "api/crop-health"
        )

        Log.d(TAG, "Testing specific Laravel API endpoints...")
        val endpointResults = mutableListOf<ApiEndpointTester.EndpointTestResult>()

        for (endpoint in testEndpoints) {
            val result = ApiEndpointTester.testEndpoint(endpoint)
            endpointResults.add(result)

            val status = if (result.isSuccess) "✅ SUCCESS" else "❌ FAILED"
            Log.d(TAG, "  $endpoint: $status (${result.statusCode}, ${result.responseType})")

            if (result.errorMessage != null) {
                Log.d(TAG, "    Error: ${result.errorMessage}")
            }

            if (result.responseType == ApiEndpointTester.ResponseType.HTML) {
                Log.w(TAG, "    ⚠️ HTML detected - likely Laravel welcome page or error page")
            }
        }

        // Analyze results and provide specific suggestions
        Log.d(TAG, "=== ANALYSIS ===")

        val workingEndpoints = endpointResults.filter { it.isSuccess }
        val htmlEndpoints = endpointResults.filter { it.responseType == ApiEndpointTester.ResponseType.HTML }
        val errorEndpoints = endpointResults.filter { it.responseType == ApiEndpointTester.ResponseType.ERROR }

        Log.d(TAG, "Working endpoints: ${workingEndpoints.size}/${endpointResults.size}")
        Log.d(TAG, "HTML responses (likely Laravel pages): ${htmlEndpoints.size}")
        Log.d(TAG, "Connection errors: ${errorEndpoints.size}")

        // Provide specific suggestions based on results
        val suggestions = mutableListOf<String>()

        when {
            workingEndpoints.isEmpty() && htmlEndpoints.isNotEmpty() -> {
                suggestions.add("🔧 LIKELY ISSUE: Base URL is pointing to Laravel web routes, not API routes")
                suggestions.add("   Try adding '/api' to your base URL")
                suggestions.add("   Current: ${ApiClient.getBaseUrl()}")
                suggestions.add("   Try: ${ApiClient.getBaseUrl()}api/")
            }

            endpointResults.any { it.endpoint == "api/ping" && it.isSuccess } -> {
                suggestions.add("✅ API is working! /api/ping successful")
                if (endpointResults.any { it.statusCode == 401 }) {
                    suggestions.add("🔐 Some endpoints require authentication")
                    suggestions.add("   Test login first, then use the token for other endpoints")
                }
            }

            errorEndpoints.size == endpointResults.size -> {
                suggestions.add("🌐 CONNECTION ISSUE: Cannot reach server")
                suggestions.add("   Check if domain is accessible: ${ApiClient.getBaseUrl()}")
                suggestions.add("   Verify internet connection")
            }

            else -> {
                suggestions.add("🔍 MIXED RESULTS: Some endpoints work, others don't")
                suggestions.add("   This might be normal - some require authentication")
            }
        }

        Log.d(TAG, "=== SUGGESTIONS ===")
        suggestions.forEach { suggestion ->
            Log.d(TAG, suggestion)
        }

        // Try auto-fix for common Laravel API issues
        if (workingEndpoints.isEmpty() && htmlEndpoints.isNotEmpty()) {
            Log.d(TAG, "=== ATTEMPTING AUTO-FIX ===")
            val autoFixResult = attemptLaravelApiFix()
            if (autoFixResult) {
                Log.d(TAG, "✅ Auto-fix successful! Re-testing...")
                // Re-test ping endpoint
                val pingResult = ApiEndpointTester.testEndpoint("api/ping")
                Log.d(TAG, "Ping after fix: ${if (pingResult.isSuccess) "✅ SUCCESS" else "❌ FAILED"}")
            } else {
                Log.d(TAG, "❌ Auto-fix failed")
            }
        }

        Log.d(TAG, "=== LARAVEL API DIAGNOSTIC TEST COMPLETE ===")
    }

    /**
     * Specific auto-fix for Laravel API routing issues
     */
    private suspend fun attemptLaravelApiFix(): Boolean = withContext(Dispatchers.IO) {
        val currentUrl = ApiClient.getBaseUrl()

        // Common Laravel API URL patterns to test
        val laravelApiUrls = listOf(
            "${currentUrl.trimEnd('/')}/api/",
            "${currentUrl.trimEnd('/')}/public/api/",
            currentUrl.replace("https://", "https://api."),
            currentUrl.replace("http://", "http://api.")
        )

        for (testUrl in laravelApiUrls) {
            Log.d(TAG, "Testing Laravel API URL: $testUrl")

            try {
                // Temporarily set the test URL
                ApiClient.setCustomBaseUrl(testUrl)
                ApiClient.recreateApiService()

                // Test the ping endpoint specifically
                val pingResult = ApiEndpointTester.testEndpoint("api/ping")

                if (pingResult.isSuccess) {
                    Log.d(TAG, "✅ Found working Laravel API URL: $testUrl")
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.d(TAG, "Failed to test URL $testUrl: ${e.message}")
            }
        }

        // If no URL worked, revert to original
        ApiClient.setCustomBaseUrl(currentUrl)
        ApiClient.recreateApiService()
        return@withContext false
    }

    /**
     * Test Laravel authentication flow
     */
    suspend fun testLaravelAuth(email: String = "test@example.com", password: String = "password") {
        Log.d(TAG, "=== TESTING LARAVEL AUTHENTICATION ===")

        try {
            // This would require implementing actual API calls
            Log.d(TAG, "To test authentication, you need to:")
            Log.d(TAG, "1. Make a POST request to /api/register or /api/login")
            Log.d(TAG, "2. Get the bearer token from response")
            Log.d(TAG, "3. Use token for protected endpoints")
            Log.d(TAG, "Current base URL: ${ApiClient.getBaseUrl()}")

        } catch (e: Exception) {
            Log.e(TAG, "Auth test failed", e)
        }
    }

    /**
     * Quick test of the most common Laravel API endpoints
     */
    suspend fun quickLaravelTest(): Boolean {
        val testUrls = listOf(
            "https://fieldconnect.site/api/ping",
            "https://fieldconnect.site/api/",
            "https://api.fieldconnect.site/ping"
        )

        for (url in testUrls) {
            Log.d(TAG, "Quick testing: $url")

            // Extract base URL and set it
            val baseUrl = url.substringBeforeLast("/api/") + "/"
            ApiClient.setCustomBaseUrl(baseUrl)
            ApiClient.recreateApiService()

            val result = ApiEndpointTester.testEndpoint("api/ping")
            if (result.isSuccess) {
                Log.d(TAG, "✅ Quick test successful with: $baseUrl")
                return true
            }
        }

        return false
    }
}
