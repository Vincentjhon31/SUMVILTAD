package com.zynt.sumviltadconnect.data.model

import com.google.gson.annotations.SerializedName

/**
 * GitHub Release API Response Model
 * Represents a single release from GitHub API
 */
data class GitHubRelease(
    @SerializedName("tag_name")
    val tagName: String,           // e.g., "v1.0.1"
    
    @SerializedName("name")
    val name: String,              // Release title
    
    @SerializedName("body")
    val body: String?,             // Release notes/description
    
    @SerializedName("html_url")
    val htmlUrl: String,           // Browser URL to release page
    
    @SerializedName("published_at")
    val publishedAt: String,       // ISO 8601 timestamp
    
    @SerializedName("prerelease")
    val prerelease: Boolean,       // Is this a pre-release?
    
    @SerializedName("draft")
    val draft: Boolean,            // Is this a draft?
    
    @SerializedName("assets")
    val assets: List<GitHubAsset>  // Download assets (APK files)
)

data class GitHubAsset(
    @SerializedName("name")
    val name: String,                      // e.g., "SumviltadConnect-v1.0.1.apk"
    
    @SerializedName("browser_download_url")
    val browserDownloadUrl: String,        // Direct download URL
    
    @SerializedName("size")
    val size: Long,                        // File size in bytes
    
    @SerializedName("content_type")
    val contentType: String                // MIME type
)
