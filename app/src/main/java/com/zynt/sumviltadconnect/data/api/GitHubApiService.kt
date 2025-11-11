package com.zynt.sumviltadconnect.data.api

import com.zynt.sumviltadconnect.data.model.GitHubRelease
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * GitHub API Service for checking app updates
 * Base URL: https://api.github.com/
 */
interface GitHubApiService {
    
    /**
     * Get the latest release from a GitHub repository
     * @param owner Repository owner (e.g., "Vincentjhon31")
     * @param repo Repository name (e.g., "SUMVILTAD")
     * @return Latest non-draft, non-prerelease release
     */
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<GitHubRelease>
    
    /**
     * Get all releases from a GitHub repository
     * @param owner Repository owner
     * @param repo Repository name
     * @return List of all releases (including drafts and prereleases)
     */
    @GET("repos/{owner}/{repo}/releases")
    suspend fun getAllReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<List<GitHubRelease>>
}
