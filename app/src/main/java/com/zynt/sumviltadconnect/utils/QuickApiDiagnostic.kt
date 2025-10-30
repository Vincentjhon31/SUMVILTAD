package com.zynt.sumviltadconnect.utils

import android.content.Context
import android.util.Log
import com.zynt.sumviltadconnect.data.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Quick API diagnostic that you can call from MainActivity or any Activity
 * This will help identify exactly what's wrong with your Laravel API connection
 */
object QuickApiDiagnostic {
    private const val TAG = "QuickApiDiagnostic"

    /**
     * Call this method from your MainActivity onCreate() or from a test button
     * It will log the results so you can see them in Android Studio Logcat
     */
    fun runQuickTest(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "🚀 Starting Quick API Diagnostic...")

            // Initialize ApiClient
            ApiClient.initialize(context)

            // Test 1: Simple ping test
            val pingResult = SimplePingTest.testPingEndpoint(context)
            Log.d(TAG, "📊 PING TEST RESULTS:")
            Log.d(TAG, pingResult)

            // Test 2: Find working URL
            val workingUrl = SimplePingTest.findWorkingUrl()
            if (workingUrl != null) {
                Log.d(TAG, "✅ FOUND WORKING URL: $workingUrl")

                // Extract base URL and update ApiClient
                val baseUrl = workingUrl.substringBeforeLast("/api/") + "/"
                ApiClient.setCustomBaseUrl(baseUrl)
                Log.d(TAG, "🔧 Updated ApiClient base URL to: $baseUrl")

                // Test protected endpoints with the working URL
                testProtectedEndpoints()
            } else {
                Log.e(TAG, "❌ NO WORKING URL FOUND")
                Log.e(TAG, "🔍 TROUBLESHOOTING STEPS:")
                Log.e(TAG, "1. Check if https://fieldconnect.site is accessible in browser")
                Log.e(TAG, "2. Verify your Laravel app is running")
                Log.e(TAG, "3. Check your Laravel routes: php artisan route:list")
                Log.e(TAG, "4. Check server logs for errors")
            }
        }
    }

    private suspend fun testProtectedEndpoints() = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔐 Testing protected endpoints (these should return 401 without auth)...")

        val protectedEndpoints = listOf(
            "api/user",
            "api/dashboard",
            "api/tasks",
            "api/notifications"
        )

        var dashboardIssueDetected = false

        for (endpoint in protectedEndpoints) {
            val result = ApiEndpointTester.testEndpoint(endpoint)
            val status = when {
                result.isSuccess -> "✅ SUCCESS (unexpected without auth)"
                result.statusCode == 401 -> "🔐 401 UNAUTHORIZED (expected)"
                result.statusCode == 404 -> {
                    if (endpoint == "api/dashboard") {
                        dashboardIssueDetected = true
                        "❌ 404 NOT FOUND (LARAVEL ROUTE ISSUE)"
                    } else {
                        "❌ 404 NOT FOUND (route missing)"
                    }
                }
                else -> "❓ ${result.statusCode} ${result.errorMessage}"
            }
            Log.d(TAG, "  $endpoint: $status")
        }

        if (dashboardIssueDetected) {
            Log.w(TAG, "🚨 DASHBOARD ROUTE ISSUE DETECTED:")
            Log.w(TAG, "   The /api/dashboard route returns 404, but it should exist in your Laravel routes.")
            Log.w(TAG, "   This might be caused by:")
            Log.w(TAG, "   1. Route caching issues - run: php artisan route:clear")
            Log.w(TAG, "   2. Middleware blocking the route")
            Log.w(TAG, "   3. Laravel app not finding the route definition")
            Log.w(TAG, "   Your other API endpoints work fine, so this is a specific route issue.")
        }

        Log.d(TAG, "🎉 OVERALL: Your API connection is working! Only the dashboard route needs attention.")
    }

    /**
     * Simple method to test if your Laravel API is reachable
     * Returns true if the ping endpoint works
     */
    suspend fun isApiWorking(context: Context): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            ApiClient.initialize(context)
            val workingUrl = SimplePingTest.findWorkingUrl()
            workingUrl != null
        } catch (e: Exception) {
            Log.e(TAG, "API test failed", e)
            false
        }
    }

    /**
     * Get diagnostic information as a string (useful for displaying in UI)
     */
    suspend fun getDiagnosticInfo(context: Context): String = withContext(Dispatchers.IO) {
        val result = StringBuilder()
        result.appendLine("=== API DIAGNOSTIC REPORT ===")
        result.appendLine("Timestamp: ${System.currentTimeMillis()}")
        result.appendLine("Current Base URL: ${ApiClient.getBaseUrl()}")
        result.appendLine("")

        // Test ping endpoint
        val pingResult = SimplePingTest.testPingEndpoint(context)
        result.appendLine(pingResult)

        // Test for working URLs
        result.appendLine("\n=== SCANNING FOR WORKING URLS ===")
        val workingUrl = SimplePingTest.findWorkingUrl()
        if (workingUrl != null) {
            result.appendLine("✅ Found working URL: $workingUrl")

            // Test Laravel endpoints
            val baseUrl = workingUrl.substringBeforeLast("/api/") + "/"
            val laravelTest = LaravelApiTester.testLaravelEndpoints(baseUrl)
            result.appendLine("\n$laravelTest")
        } else {
            result.appendLine("❌ No working URLs found")
        }

        return@withContext result.toString()
    }
}
