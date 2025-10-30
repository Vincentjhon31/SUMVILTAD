package com.zynt.sumviltadconnect.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val TAG = "ApiClient"

    // IMPORTANT: Replace this with your actual Hostinger domain
    // Examples:
    // private const val BASE_URL = "https://yourdomain.hostingerapp.com/"
    // private const val BASE_URL = "https://your-custom-domain.com/"
    private const val BASE_URL = "https://fieldconnect.site/"

    // Fallback for local testing
    private const val LOCAL_URL = "http://10.0.2.2:8000/"

    private var retrofit: Retrofit? = null
    private var sharedPreferences: SharedPreferences? = null
    private var useLocalhost = false
    private var customBaseUrl: String? = null

    // Flag to track if we've received valid JSON from the server
    private var hasReceivedValidJson = false

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        // Check if we should use localhost (for development)
        useLocalhost = sharedPreferences?.getBoolean("use_localhost", false) ?: false
        customBaseUrl = sharedPreferences?.getString("custom_base_url", null)
    }

    fun getBaseUrl(): String {
        return customBaseUrl ?: if (useLocalhost) LOCAL_URL else BASE_URL
    }

    fun getImageUrl(imagePath: String): String {
        return "${getBaseUrl()}storage/$imagePath"
    }

    fun getStoredAuthToken(): String? {
        return getAuthToken()
    }

    fun setUseLocalhost(use: Boolean) {
        useLocalhost = use
        sharedPreferences?.edit()?.apply {
            putBoolean("use_localhost", use)
            apply()
        }
        // Recreate retrofit with new URL
        retrofit = null
    }

    fun setCustomBaseUrl(url: String?) {
        customBaseUrl = url
        sharedPreferences?.edit()?.apply {
            putString("custom_base_url", url)
            apply()
        }
        // Recreate retrofit with new URL
        retrofit = null
        Log.d(TAG, "Set custom base URL: $url")
    }

    private fun getAuthToken(): String? {
        return sharedPreferences?.getString("auth_token", null)
    }

    fun saveAuthToken(token: String) {
        sharedPreferences?.edit()?.apply {
            putString("auth_token", token)
            apply()
        }
        Log.d(TAG, "Auth token saved")
    }

    fun clearAuthToken() {
        sharedPreferences?.edit()?.apply {
            remove("auth_token")
            apply()
        }
        Log.d(TAG, "Auth token cleared")
    }

    private fun createAuthInterceptor() = Interceptor { chain ->
        val original = chain.request()
        val token = getAuthToken()

        val requestBuilder = original.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("X-Requested-With", "XMLHttpRequest") // Tell Laravel this is an AJAX request
            .removeHeader("Cookie") // Remove cookies to prevent Laravel session auth conflicts

        if (!token.isNullOrBlank()) {
            // Ensure token format exactly matches what Laravel Sanctum expects
            requestBuilder.header("Authorization", "Bearer $token")
            Log.d(TAG, "Adding auth token to request: Bearer ${token.take(10)}...")
        } else {
            Log.d(TAG, "No auth token - request will be unauthenticated")
        }

        val request = requestBuilder.build()
        Log.d(TAG, "API Request: ${request.method} ${request.url}")

        val response = chain.proceed(request)
        Log.d(TAG, "API Response: ${response.code} ${response.message}")

        // Check if response is JSON
        val contentType = response.header("Content-Type")
        val responseBody = response.peekBody(Long.MAX_VALUE).string()

        if (contentType?.contains("application/json", ignoreCase = true) == true) {
            hasReceivedValidJson = true
        } else {
            Log.e(TAG, "API returned non-JSON response: $contentType")
            Log.e(TAG, "Response body: ${responseBody.take(500)}")
        }

        // Check for HTML redirects (common Laravel issue)
        if (response.code in 300..399) {
            val location = response.header("Location")
            Log.e(TAG, "🚨 REDIRECT DETECTED: Laravel is redirecting to $location")
            Log.e(TAG, "This means the API endpoint is redirecting to a web page instead of returning JSON")
        }

        if (responseBody.startsWith("<html", ignoreCase = true) ||
            responseBody.startsWith("<!DOCTYPE", ignoreCase = true)) {
            Log.e(TAG, "API returned HTML instead of JSON - possible server configuration issue")
            Log.e(TAG, "HTML Response: ${responseBody.take(500)}")

            // If we get HTML and haven't received valid JSON before,
            // this might be a URL issue. Log detailed debug info
            if (!hasReceivedValidJson) {
                Log.e(TAG, "Possible URL configuration issue. Current base URL: ${getBaseUrl()}")
                Log.e(TAG, "Full request URL: ${request.url}")
                Log.e(TAG, "Try adding '/api' to the base URL or check server configuration")
            }
        }

        response
    }

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private fun createHtmlErrorInterceptor() = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        // Check for HTML response that would cause JSON parsing errors
        val contentType = response.header("Content-Type")
        if (contentType?.contains("application/json", ignoreCase = true) != true) {
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            if (responseBody.startsWith("<html", ignoreCase = true) ||
                responseBody.startsWith("<!DOCTYPE", ignoreCase = true)) {

                // Create a new response with a more helpful error message
                val message = "Server returned HTML instead of JSON. This may indicate an incorrect API URL, " +
                              "authentication issue, or server misconfiguration."

                val errorJson = """{"error":"non_json_response","message":"$message"}"""

                val newResponseBody = errorJson.toResponseBody("application/json".toMediaType())

                return@Interceptor response.newBuilder()
                    .code(502) // Bad Gateway
                    .message("API returned HTML instead of JSON")
                    .body(newResponseBody)
                    .build()
            }
        }

        response
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(createAuthInterceptor())
            .addInterceptor(createHtmlErrorInterceptor())
            .addInterceptor(createLoggingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun createRetrofit(): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create()

        return Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: ApiService by lazy {
        if (retrofit == null) {
            retrofit = createRetrofit()
            Log.d(TAG, "ApiService created with base URL: ${getBaseUrl()}")
        }
        retrofit!!.create(ApiService::class.java)
    }

    fun recreateApiService() {
        retrofit = null
        Log.d(TAG, "ApiService recreated")
    }

    /**
     * Tests the basic connectivity to the API
     * @return true if the connection is successful
     */
    fun testConnection(): Boolean {
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build()

            val request = okhttp3.Request.Builder()
                .url(getBaseUrl())
                .build()

            val response = client.newCall(request).execute()
            Log.d(TAG, "Connection test: ${response.code} ${response.message}")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            false
        }
    }
}
