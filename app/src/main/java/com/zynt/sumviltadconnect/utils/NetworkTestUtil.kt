package com.zynt.sumviltadconnect.utils

import android.util.Log
import com.zynt.sumviltadconnect.data.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object NetworkTestUtil {
    private const val TAG = "NetworkTestUtil"

    fun testServerConnection(callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${ApiClient.getBaseUrl()}api/test-connection")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                val message = if (responseCode == 200) {
                    "Server connection successful"
                } else {
                    "Server responded with code: $responseCode"
                }

                withContext(Dispatchers.Main) {
                    callback(responseCode == 200, message)
                }

                Log.d(TAG, "Connection test result: $responseCode - $message")

            } catch (e: Exception) {
                val errorMessage = "Connection failed: ${e.message}"
                Log.e(TAG, errorMessage, e)

                withContext(Dispatchers.Main) {
                    callback(false, errorMessage)
                }
            }
        }
    }

    fun pingServer(callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val baseUrl = ApiClient.getBaseUrl().removeSuffix("/")
                val url = URL(baseUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                val success = responseCode in 200..299
                val message = "Server ping: $responseCode ${connection.responseMessage}"

                Log.d(TAG, "Ping result: $message")

                withContext(Dispatchers.Main) {
                    callback(success, message)
                }

            } catch (e: Exception) {
                val errorMessage = "Ping failed: ${e.message}"
                Log.e(TAG, errorMessage, e)

                withContext(Dispatchers.Main) {
                    callback(false, errorMessage)
                }
            }
        }
    }
}
