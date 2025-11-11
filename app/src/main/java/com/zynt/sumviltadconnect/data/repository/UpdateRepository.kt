package com.zynt.sumviltadconnect.data.repository

import android.util.Log
import com.zynt.sumviltadconnect.data.api.GitHubApiService
import com.zynt.sumviltadconnect.data.model.GitHubRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Repository for managing GitHub release data
 * Handles fetching and comparing app versions
 */
class UpdateRepository {
    
    private val githubApi: GitHubApiService
    
    companion object {
        private const val TAG = "UpdateRepository"
        private const val GITHUB_API_BASE_URL = "https://api.github.com/"
        private const val TIMEOUT_SECONDS = 30L
    }
    
    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(GITHUB_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        githubApi = retrofit.create(GitHubApiService::class.java)
    }
    
    /**
     * Check for updates by comparing current version with latest GitHub release
     * @param owner GitHub repository owner
     * @param repo GitHub repository name
     * @param currentVersion Current app version (e.g., "1.0.0")
     * @return UpdateCheckResult with update status
     */
    suspend fun checkForUpdates(
        owner: String,
        repo: String,
        currentVersion: String
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Checking for updates from $owner/$repo")
            Log.d(TAG, "Current version: $currentVersion")
            
            val response = githubApi.getLatestRelease(owner, repo)
            
            if (response.isSuccessful && response.body() != null) {
                val latestRelease = response.body()!!
                val latestVersion = latestRelease.tagName.removePrefix("v")
                
                Log.d(TAG, "Latest version: $latestVersion")
                
                val updateAvailable = isNewerVersion(currentVersion, latestVersion)
                
                if (updateAvailable) {
                    Log.d(TAG, "Update available!")
                    UpdateCheckResult.UpdateAvailable(latestRelease)
                } else {
                    Log.d(TAG, "App is up to date")
                    UpdateCheckResult.UpToDate
                }
            } else {
                Log.e(TAG, "Failed to fetch release: ${response.code()} - ${response.message()}")
                UpdateCheckResult.Error("Failed to check for updates: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            UpdateCheckResult.Error("Network error: ${e.message}")
        }
    }
    
    /**
     * Compare two version strings using semantic versioning
     * @param current Current version (e.g., "1.0.0")
     * @param latest Latest version from GitHub (e.g., "1.0.1")
     * @return true if latest is newer than current
     */
    private fun isNewerVersion(current: String, latest: String): Boolean {
        try {
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
            
            val maxLength = maxOf(currentParts.size, latestParts.size)
            
            for (i in 0 until maxLength) {
                val currentPart = currentParts.getOrNull(i) ?: 0
                val latestPart = latestParts.getOrNull(i) ?: 0
                
                when {
                    latestPart > currentPart -> return true
                    latestPart < currentPart -> return false
                    // If equal, continue to next part
                }
            }
            
            // Versions are equal
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error comparing versions", e)
            return false
        }
    }
}

/**
 * Sealed class representing the result of an update check
 */
sealed class UpdateCheckResult {
    data class UpdateAvailable(val release: GitHubRelease) : UpdateCheckResult()
    object UpToDate : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}
