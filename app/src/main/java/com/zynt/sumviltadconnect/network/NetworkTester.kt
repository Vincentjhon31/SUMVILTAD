package com.zynt.sumviltadconnect.network

import android.util.Log
import com.zynt.sumviltadconnect.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.HttpURLConnection
import java.net.URL

object NetworkTester {
    private const val TAG = "NetworkTester"

    suspend fun testFieldConnectSite(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Testing connectivity to fieldconnect.site...")

            // Test basic website connectivity
            val url = URL("https://fieldconnect.site")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            Log.d(TAG, "Website response code: $responseCode")

            if (responseCode == 200) {
                Log.d(TAG, "✅ fieldconnect.site is accessible")
                testApiEndpoints()
                true
            } else {
                Log.e(TAG, "❌ fieldconnect.site returned code: $responseCode")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to connect to fieldconnect.site: ${e.message}")
            false
        }
    }

    private suspend fun testApiEndpoints() = withContext(Dispatchers.IO) {
        val baseUrl = "https://fieldconnect.site/"
        val endpoints = listOf(
            "api/dashboard",
            "api/events",
            "api/tasks",
            "api/notifications",
            "api/irrigation-schedules"
        )

        val client = OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        endpoints.forEach { endpoint ->
            try {
                val request = Request.Builder()
                    .url("$baseUrl$endpoint")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                Log.d(TAG, "Endpoint $endpoint: ${response.code} ${response.message}")

                when (response.code) {
                    200 -> Log.d(TAG, "✅ $endpoint - Success")
                    401 -> Log.w(TAG, "🔐 $endpoint - Authentication required (normal)")
                    404 -> Log.e(TAG, "❌ $endpoint - Not found (check Laravel routes)")
                    500 -> Log.e(TAG, "💥 $endpoint - Server error (check Laravel backend)")
                    else -> Log.w(TAG, "⚠️ $endpoint - Unexpected response: ${response.code}")
                }

                response.close()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to test $endpoint: ${e.message}")
            }
        }
    }
}
