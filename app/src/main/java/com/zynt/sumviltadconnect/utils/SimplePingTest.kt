package com.zynt.sumviltadconnect.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Simple test to verify the exact Laravel API ping endpoint
 */
object SimplePingTest {
    private const val TAG = "SimplePingTest"

    suspend fun testPingEndpoint(context: Context): String = withContext(Dispatchers.IO) {
        val result = StringBuilder()
        result.appendLine("=== TESTING EXACT PING ENDPOINT ===")

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        // Test the exact URL your Laravel API should respond to
        val pingUrl = "https://fieldconnect.site/api/ping"

        try {
            result.appendLine("Testing: $pingUrl")

            val request = Request.Builder()
                .url(pingUrl)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "SumviltadConnect-Android/1.0")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            val contentType = response.header("Content-Type") ?: "unknown"

            result.appendLine("Status Code: ${response.code}")
            result.appendLine("Content-Type: $contentType")
            result.appendLine("Response Headers:")
            response.headers.forEach { (name, value) ->
                result.appendLine("  $name: $value")
            }
            result.appendLine("")
            result.appendLine("Response Body:")
            result.appendLine(responseBody)

            // Analysis
            result.appendLine("\n=== ANALYSIS ===")
            when {
                response.isSuccessful && contentType.contains("application/json", ignoreCase = true) -> {
                    result.appendLine("✅ SUCCESS: API is working correctly!")
                    result.appendLine("Your Laravel API ping endpoint is responding properly.")
                    return@withContext result.toString()
                }
                response.code == 200 && responseBody.contains("API is working", ignoreCase = true) -> {
                    result.appendLine("✅ API WORKS but content-type might be wrong")
                    result.appendLine("Response contains expected message but Content-Type is: $contentType")
                }
                responseBody.contains("<!DOCTYPE", ignoreCase = true) -> {
                    result.appendLine("❌ PROBLEM: Getting HTML instead of JSON")
                    result.appendLine("This means the URL is reaching a web page, not your API route")
                    result.appendLine("Check if your Laravel routes are properly configured")
                }
                response.code == 404 -> {
                    result.appendLine("❌ PROBLEM: 404 Not Found")
                    result.appendLine("The /api/ping route doesn't exist or isn't accessible")
                    result.appendLine("Check your Laravel api.php routes file")
                }
                response.code >= 500 -> {
                    result.appendLine("❌ PROBLEM: Server Error (${response.code})")
                    result.appendLine("Your Laravel server has an internal error")
                    result.appendLine("Check your server logs")
                }
                else -> {
                    result.appendLine("❓ UNEXPECTED: Response code ${response.code}")
                    result.appendLine("Check your Laravel configuration")
                }
            }

        } catch (e: Exception) {
            result.appendLine("💥 CONNECTION ERROR: ${e.message}")
            result.appendLine("\nPossible causes:")
            result.appendLine("- Domain not accessible")
            result.appendLine("- SSL certificate issues")
            result.appendLine("- Network connectivity problems")
            result.appendLine("- Server is down")
        }

        return@withContext result.toString()
    }

    /**
     * Test multiple URL variations to find the working one
     */
    suspend fun findWorkingUrl(): String? = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val urlsToTest = listOf(
            "https://fieldconnect.site/api/ping",
            "https://www.fieldconnect.site/api/ping",
            "https://api.fieldconnect.site/ping",
            "https://fieldconnect.site/public/api/ping",
            "http://fieldconnect.site/api/ping"  // In case HTTPS is not working
        )

        for (url in urlsToTest) {
            try {
                Log.d(TAG, "Testing URL: $url")

                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val isJson = response.header("Content-Type")?.contains("json", ignoreCase = true) ?: false

                if (response.isSuccessful && (isJson || responseBody.contains("API is working", ignoreCase = true))) {
                    Log.d(TAG, "✅ Found working URL: $url")
                    return@withContext url
                }

            } catch (e: Exception) {
                Log.d(TAG, "❌ Failed URL: $url - ${e.message}")
            }
        }

        return@withContext null
    }
}
