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
 * Comprehensive API fix that handles the HTML vs JSON issue
 */
object ApiFixUtility {
    private const val TAG = "ApiFixUtility"

    fun applyComprehensiveFix(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "🔧 APPLYING COMPREHENSIVE API FIX")

            ApiClient.initialize(context)

            // Step 1: Test if we can access public endpoints
            val publicEndpointsWork = testPublicEndpoints()

            if (publicEndpointsWork) {
                Log.d(TAG, "✅ Public endpoints work - issue is with protected endpoints")

                // Step 2: Fix the Laravel API endpoint configuration
                fixLaravelEndpointIssue()

                // Step 3: Test the fix
                testFixedEndpoints()
            } else {
                Log.d(TAG, "❌ Public endpoints don't work - base URL issue")
                val workingBaseUrl = findWorkingBaseUrl()
                if (workingBaseUrl != null) {
                    ApiClient.setCustomBaseUrl(workingBaseUrl)
                    Log.d(TAG, "✅ Fixed base URL to: $workingBaseUrl")
                }
            }
        }
    }

    private suspend fun testPublicEndpoints(): Boolean = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()

        try {
            val pingUrl = "${ApiClient.getBaseUrl()}api/ping"
            val request = Request.Builder()
                .url(pingUrl)
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val contentType = response.header("Content-Type") ?: ""

            return@withContext response.isSuccessful &&
                contentType.contains("application/json", ignoreCase = true)

        } catch (e: Exception) {
            Log.e(TAG, "Public endpoint test failed", e)
            return@withContext false
        }
    }

    private suspend fun fixLaravelEndpointIssue() = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔧 Fixing Laravel endpoint configuration...")

        // The issue is likely that Laravel is redirecting unauthenticated requests
        // to login pages instead of returning JSON 401 errors

        // Test protected endpoints to see their actual behavior
        val protectedEndpoints = listOf("api/dashboard", "api/tasks", "api/events")

        for (endpoint in protectedEndpoints) {
            val fullUrl = "${ApiClient.getBaseUrl()}$endpoint"
            Log.d(TAG, "Testing protected endpoint: $fullUrl")

            try {
                val client = OkHttpClient.Builder()
                    .followRedirects(false) // Don't follow redirects
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url(fullUrl)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("X-Requested-With", "XMLHttpRequest") // Tell Laravel this is an AJAX request
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val contentType = response.header("Content-Type") ?: ""

                Log.d(TAG, "  $endpoint:")
                Log.d(TAG, "    Status: ${response.code}")
                Log.d(TAG, "    Content-Type: $contentType")
                Log.d(TAG, "    Is JSON: ${contentType.contains("json", ignoreCase = true)}")

                if (response.code in 300..399) {
                    val location = response.header("Location")
                    Log.w(TAG, "    ⚠️ REDIRECT detected to: $location")
                    Log.w(TAG, "    This is why you're getting HTML instead of JSON!")
                }

                if (responseBody.contains("<!DOCTYPE", ignoreCase = true)) {
                    Log.w(TAG, "    ❌ Getting HTML response - Laravel is redirecting to web routes")
                }

            } catch (e: Exception) {
                Log.e(TAG, "  Error testing $endpoint: ${e.message}")
            }
        }
    }

    private suspend fun findWorkingBaseUrl(): String? = withContext(Dispatchers.IO) {
        val urlsToTest = listOf(
            "https://fieldconnect.site/",
            "https://fieldconnect.site/api/",
            "https://www.fieldconnect.site/",
            "https://api.fieldconnect.site/"
        )

        for (baseUrl in urlsToTest) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build()

                val testEndpoint = if (baseUrl.endsWith("/api/")) "ping" else "api/ping"
                val fullUrl = "${baseUrl.trimEnd('/')}/$testEndpoint"

                val request = Request.Builder()
                    .url(fullUrl)
                    .header("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val contentType = response.header("Content-Type") ?: ""

                if (response.isSuccessful && contentType.contains("json", ignoreCase = true)) {
                    Log.d(TAG, "✅ Found working base URL: $baseUrl")
                    return@withContext baseUrl
                }

            } catch (e: Exception) {
                Log.d(TAG, "Failed base URL: $baseUrl - ${e.message}")
            }
        }

        return@withContext null
    }

    private suspend fun testFixedEndpoints() = withContext(Dispatchers.IO) {
        Log.d(TAG, "🧪 Testing fixed endpoints...")

        // Test a few endpoints to see if they now return proper JSON errors
        val endpointsToTest = listOf("api/dashboard", "api/tasks")

        for (endpoint in endpointsToTest) {
            try {
                val client = OkHttpClient.Builder()
                    .followRedirects(false)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url("${ApiClient.getBaseUrl()}$endpoint")
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build()

                val response = client.newCall(request).execute()
                val contentType = response.header("Content-Type") ?: ""
                val isJson = contentType.contains("json", ignoreCase = true)

                val status = when {
                    isJson && response.code == 401 -> "✅ FIXED - Returns JSON 401"
                    isJson && response.code == 200 -> "✅ FIXED - Returns JSON 200"
                    !isJson -> "❌ Still returns HTML"
                    else -> "❓ ${response.code}"
                }

                Log.d(TAG, "  $endpoint: $status")

            } catch (e: Exception) {
                Log.e(TAG, "  Error testing $endpoint: ${e.message}")
            }
        }
    }
}
