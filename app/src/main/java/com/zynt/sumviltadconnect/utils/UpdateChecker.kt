package com.zynt.sumviltadconnect.utils

import android.content.Context
import android.util.Log
import com.zynt.sumviltadconnect.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppVersion(
    val versionName: String,
    val versionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String,
    val isUpdateAvailable: Boolean
)

object UpdateChecker {
    private const val TAG = "UpdateChecker"
    private const val GITHUB_API_URL = "https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases/latest"
    
    suspend fun checkForUpdates(): AppVersion? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Checking for updates from GitHub...")
            Log.d(TAG, "Current version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                // Parse version from tag_name (e.g., "v1.1.1" -> "1.1.1")
                val tagName = json.getString("tag_name")
                val latestVersionName = tagName.removePrefix("v")
                val latestVersionCode = latestVersionName.replace(".", "").toIntOrNull() ?: 0
                
                // Get download URL for APK
                val assets = json.getJSONArray("assets")
                var downloadUrl = ""
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.getString("name")
                    if (name.endsWith(".apk")) {
                        downloadUrl = asset.getString("browser_download_url")
                        break
                    }
                }
                
                val releaseNotes = json.optString("body", "New version available!")
                
                val isUpdateAvailable = latestVersionCode > BuildConfig.VERSION_CODE
                
                Log.d(TAG, "Latest version: $latestVersionName ($latestVersionCode)")
                Log.d(TAG, "Update available: $isUpdateAvailable")
                
                AppVersion(
                    versionName = latestVersionName,
                    versionCode = latestVersionCode,
                    downloadUrl = downloadUrl,
                    releaseNotes = releaseNotes,
                    isUpdateAvailable = isUpdateAvailable
                )
            } else {
                Log.e(TAG, "Failed to check updates. Response code: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            null
        }
    }
    
    fun getCurrentVersion(): String = BuildConfig.VERSION_NAME
    fun getCurrentVersionCode(): Int = BuildConfig.VERSION_CODE
}
