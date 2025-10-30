package com.zynt.sumviltadconnect.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Simple Laravel API test utility
 * Tests your specific Laravel API endpoints
 */
object LaravelApiTester {
    private const val TAG = "LaravelApiTester"

    suspend fun testYourApi(): String = withContext(Dispatchers.IO) {
        val results = StringBuilder()
        results.appendLine("=== TESTING YOUR LARAVEL API ===")

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        // Test different URL patterns for your Laravel API
        val urlsToTest = listOf(
            "https://fieldconnect.site/api/ping",
            "https://fieldconnect.site/ping",
            "https://api.fieldconnect.site/ping",
            "https://fieldconnect.site/public/api/ping"
        )

        for (url in urlsToTest) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build()

                results.appendLine("\n🔍 Testing: $url")

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val contentType = response.header("Content-Type") ?: "unknown"

                results.appendLine("   Status: ${response.code}")
                results.appendLine("   Content-Type: $contentType")

                when {
                    response.isSuccessful && contentType.contains("application/json", ignoreCase = true) -> {
                        results.appendLine("   ✅ SUCCESS - Got JSON response!")
                        results.appendLine("   Response: ${responseBody.take(200)}")
                        return@withContext results.toString() + "\n\n🎉 FOUND WORKING API URL: $url"
                    }
                    responseBody.contains("<!DOCTYPE", ignoreCase = true) -> {
                        results.appendLine("   ❌ Got HTML instead of JSON (Laravel welcome page?)")
                    }
                    response.code == 404 -> {
                        results.appendLine("   ❌ 404 Not Found - Route doesn't exist")
                    }
                    else -> {
                        results.appendLine("   ❌ Response: ${responseBody.take(100)}")
                    }
                }

            } catch (e: Exception) {
                results.appendLine("   💥 Connection Error: ${e.message}")
            }
        }

        results.appendLine("\n=== RECOMMENDATIONS ===")
        results.appendLine("1. Check if https://fieldconnect.site/api/ping works in browser")
        results.appendLine("2. Verify your Laravel routes are published")
        results.appendLine("3. Check server logs for any errors")

        return@withContext results.toString()
    }

    /**
     * Test specific Laravel endpoints that should work
     */
    suspend fun testLaravelEndpoints(baseUrl: String): String = withContext(Dispatchers.IO) {
        val results = StringBuilder()
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        // Your Laravel API endpoints from the routes file
        val endpoints = listOf(
            "api/ping" to "Public - Should work",
            "api/register" to "Public - Should accept POST",
            "api/login" to "Public - Should accept POST",
            "api/user" to "Protected - Needs auth token",
            "api/dashboard" to "Protected - Needs auth token",
            "api/tasks" to "Protected - Needs auth token"
        )

        results.appendLine("Testing Laravel endpoints at: $baseUrl")

        for ((endpoint, description) in endpoints) {
            try {
                val fullUrl = "${baseUrl.trimEnd('/')}/$endpoint"
                val request = Request.Builder()
                    .url(fullUrl)
                    .header("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val isJson = response.header("Content-Type")?.contains("json", ignoreCase = true) ?: false

                val status = when {
                    response.isSuccessful && isJson -> "✅ SUCCESS"
                    response.code == 401 -> "🔐 NEEDS AUTH (Expected)"
                    response.code == 404 -> "❌ NOT FOUND"
                    else -> "❓ ${response.code}"
                }

                results.appendLine("$endpoint - $status ($description)")

            } catch (e: Exception) {
                results.appendLine("$endpoint - 💥 ERROR: ${e.message}")
            }
        }

        return@withContext results.toString()
    }
}
