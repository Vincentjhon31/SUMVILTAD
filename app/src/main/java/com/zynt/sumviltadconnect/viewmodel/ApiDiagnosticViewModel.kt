package com.zynt.sumviltadconnect.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.network.ApiClient
import com.zynt.sumviltadconnect.utils.ApiEndpointTester
import kotlinx.coroutines.launch

class ApiDiagnosticViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var lastTestResult by mutableStateOf<Boolean?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var testResults by mutableStateOf<List<ApiEndpointTester.EndpointTestResult>>(emptyList())
        private set

    fun getCurrentApiUrl(): String {
        return ApiClient.getBaseUrl()
    }

    fun testConnection() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val connectionResult = ApiClient.testConnection()
                val endpointResults = ApiEndpointTester.testAllEndpoints()

                lastTestResult = connectionResult && endpointResults.any { it.isSuccess }
                testResults = endpointResults

                if (!lastTestResult!!) {
                    val suggestions = ApiEndpointTester.diagnoseApiIssues()
                    errorMessage = suggestions.joinToString("\n")
                }

            } catch (e: Exception) {
                lastTestResult = false
                errorMessage = "Connection test failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun tryCommonFixes() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val fixSuccessful = ApiEndpointTester.attemptAutoFix()
                if (fixSuccessful) {
                    ApiClient.recreateApiService()
                    testConnection() // Retest after fix
                } else {
                    errorMessage = "Auto-fix was unsuccessful. Try manually setting a different API URL."
                }
            } catch (e: Exception) {
                errorMessage = "Auto-fix failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun testSpecificUrl(url: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Save current URL as backup
                val originalUrl = ApiClient.getBaseUrl()

                // Set new URL
                ApiClient.setCustomBaseUrl(url)
                ApiClient.recreateApiService()

                // Test the new URL
                val connectionResult = ApiClient.testConnection()
                val endpointResults = ApiEndpointTester.testAllEndpoints()

                val success = connectionResult && endpointResults.any { it.isSuccess }
                lastTestResult = success
                testResults = endpointResults

                if (success) {
                    errorMessage = "Success! API is working with URL: $url"
                } else {
                    // Revert to original URL if test failed
                    ApiClient.setCustomBaseUrl(if (originalUrl != url) originalUrl else null)
                    ApiClient.recreateApiService()
                    errorMessage = "URL $url did not work. Reverted to original settings."
                }

            } catch (e: Exception) {
                lastTestResult = false
                errorMessage = "Failed to test URL $url: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
