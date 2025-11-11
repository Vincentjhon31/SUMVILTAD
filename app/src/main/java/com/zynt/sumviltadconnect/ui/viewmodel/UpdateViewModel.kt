package com.zynt.sumviltadconnect.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.BuildConfig
import com.zynt.sumviltadconnect.data.model.GitHubRelease
import com.zynt.sumviltadconnect.data.repository.UpdateCheckResult
import com.zynt.sumviltadconnect.data.repository.UpdateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing app update checks
 */
class UpdateViewModel : ViewModel() {
    
    private val updateRepository = UpdateRepository()
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    
    companion object {
        private const val TAG = "UpdateViewModel"
        private const val GITHUB_OWNER = "Vincentjhon31"
        private const val GITHUB_REPO = "SUMVILTAD"
    }
    
    /**
     * Check for app updates from GitHub
     */
    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                _updateState.value = UpdateState.Checking
                
                val currentVersion = BuildConfig.VERSION_NAME
                Log.d(TAG, "Checking updates. Current version: $currentVersion")
                
                val result = updateRepository.checkForUpdates(
                    owner = GITHUB_OWNER,
                    repo = GITHUB_REPO,
                    currentVersion = currentVersion
                )
                
                _updateState.value = when (result) {
                    is UpdateCheckResult.UpdateAvailable -> {
                        Log.d(TAG, "Update available: ${result.release.tagName}")
                        UpdateState.UpdateAvailable(result.release)
                    }
                    is UpdateCheckResult.UpToDate -> {
                        Log.d(TAG, "App is up to date")
                        UpdateState.UpToDate
                    }
                    is UpdateCheckResult.Error -> {
                        Log.e(TAG, "Error checking updates: ${result.message}")
                        UpdateState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error checking updates", e)
                _updateState.value = UpdateState.Error("Unexpected error: ${e.message}")
            }
        }
    }
    
    /**
     * Open the GitHub releases page in browser
     */
    fun openReleasePage(context: Context, release: GitHubRelease) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening release page", e)
        }
    }
    
    /**
     * Download APK directly (opens browser download)
     */
    fun downloadApk(context: Context, release: GitHubRelease) {
        try {
            // Find the APK asset
            val apkAsset = release.assets.firstOrNull { 
                it.name.endsWith(".apk", ignoreCase = true) 
            }
            
            if (apkAsset != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkAsset.browserDownloadUrl))
                context.startActivity(intent)
            } else {
                // Fallback to release page if no APK found
                openReleasePage(context, release)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading APK", e)
        }
    }
    
    /**
     * Reset update state to idle
     */
    fun resetState() {
        _updateState.value = UpdateState.Idle
    }
    
    /**
     * Dismiss current update notification
     */
    fun dismissUpdate() {
        _updateState.value = UpdateState.Idle
    }
}

/**
 * Sealed class representing update check states
 */
sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    object UpToDate : UpdateState()
    data class UpdateAvailable(val release: GitHubRelease) : UpdateState()
    data class Error(val message: String) : UpdateState()
}
