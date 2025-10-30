package com.zynt.sumviltadconnect.utils

import android.content.Context
import android.util.Log
import com.zynt.sumviltadconnect.data.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Advanced diagnostic tool to debug the exact URLs being called
 * and identify why API calls return HTML instead of JSON
 */
object DetailedApiDiagnostic {
    private const val TAG = "DetailedApiDiagnostic"

    fun runDetailedTest(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "🔍 RUNNING DETAILED API DIAGNOSTIC")

            ApiClient.initialize(context)
            val baseUrl = ApiClient.getBaseUrl()
            Log.d(TAG, "Base URL from ApiClient: $baseUrl")

            // Test the exact URLs that your app endpoints would create
            val endpointsToTest = listOf(
                "api/dashboard",
                "api/tasks",
                "api/events",
                "api/notifications",
                "api/crop-health",
                "api/irrigation-schedules"
            )

            Log.d(TAG, "Testing exact endpoint URLs that your app uses...")

            for (endpoint in endpointsToTest) {
                testSpecificEndpoint(baseUrl, endpoint)
            }

            // Test different base URL variations
            Log.d(TAG, "\n🔧 Testing different base URL configurations...")
            testBaseUrlVariations(endpointsToTest[0]) // Test with dashboard endpoint
        }
    }

    private suspend fun testSpecificEndpoint(baseUrl: String, endpoint: String) = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        try {
            val fullUrl = "${baseUrl.trimEnd('/')}/$endpoint"
            Log.d(TAG, "\n📍 Testing: $fullUrl")

            val request = Request.Builder()
                .url(fullUrl)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            val contentType = response.header("Content-Type") ?: "unknown"

            Log.d(TAG, "   Status: ${response.code}")
            Log.d(TAG, "   Content-Type: $contentType")
            Log.d(TAG, "   Response length: ${responseBody.length} chars")

            when {
                responseBody.startsWith("{") -> {
                    Log.d(TAG, "   ✅ Got JSON response")
                    Log.d(TAG, "   Preview: ${responseBody.take(100)}...")
                }
                responseBody.contains("<!DOCTYPE", ignoreCase = true) -> {
                    Log.w(TAG, "   ❌ Got HTML response (Laravel welcome/error page)")
                    // Extract title if possible
                    val titleMatch = Regex("<title>(.*?)</title>", RegexOption.IGNORE_CASE)
                        .find(responseBody)
                    if (titleMatch != null) {
                        Log.w(TAG, "   Page title: ${titleMatch.groupValues[1]}")
                    }
                    if (responseBody.contains("Laravel", ignoreCase = true)) {
                        Log.w(TAG, "   🚨 ISSUE: Hitting Laravel web routes instead of API routes")
                    }
                }
                response.code == 401 -> {
                    Log.d(TAG, "   🔐 401 Unauthorized (needs auth token)")
                }
                response.code == 404 -> {
                    Log.w(TAG, "   ❌ 404 Not Found - Route doesn't exist")
                }
                else -> {
                    Log.w(TAG, "   ❓ Unexpected response: ${responseBody.take(200)}")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "   💥 Error testing $endpoint: ${e.message}")
        }
    }

    private suspend fun testBaseUrlVariations(testEndpoint: String) = withContext(Dispatchers.IO) {
        val baseUrlVariations = listOf(
            "https://fieldconnect.site/",
            "https://fieldconnect.site/api/",
            "https://www.fieldconnect.site/",
            "https://api.fieldconnect.site/"
        )

        for (baseUrl in baseUrlVariations) {
            Log.d(TAG, "\n🧪 Testing base URL: $baseUrl")

            // Test with and without api prefix in endpoint
            val endpointVariations = if (baseUrl.endsWith("/api/")) {
                listOf(testEndpoint.removePrefix("api/")) // Remove api/ if base URL already has it
            } else {
                listOf(testEndpoint) // Keep api/ prefix
            }

            for (endpoint in endpointVariations) {
                val fullUrl = "${baseUrl.trimEnd('/')}/${endpoint.trimStart('/')}"
                Log.d(TAG, "   Testing: $fullUrl")

                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .build()

                    val request = Request.Builder()
                        .url(fullUrl)
                        .header("Accept", "application/json")
                        .build()

                    val response = client.newCall(request).execute()
                    val contentType = response.header("Content-Type") ?: ""
                    val isJson = contentType.contains("application/json", ignoreCase = true)

                    if (response.isSuccessful && isJson) {
                        Log.d(TAG, "   ✅ FOUND WORKING CONFIGURATION!")
                        Log.d(TAG, "   📝 SOLUTION: Set base URL to: $baseUrl")
                        if (baseUrl.endsWith("/api/")) {
                            Log.d(TAG, "   📝 SOLUTION: Remove 'api/' prefix from ApiService endpoints")
                        }
                        return@withContext
                    } else if (response.code == 401 && isJson) {
                        Log.d(TAG, "   🔐 Working but needs auth (this is expected)")
                        Log.d(TAG, "   📝 SOLUTION: Set base URL to: $baseUrl")
                        return@withContext
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "   ❌ Failed: ${e.message}")
                }
            }
        }
    }
}
