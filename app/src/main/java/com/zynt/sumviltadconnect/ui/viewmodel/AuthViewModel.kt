package com.zynt.sumviltadconnect.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.model.LoginRequest
import com.zynt.sumviltadconnect.data.model.RegisterRequest
import com.zynt.sumviltadconnect.data.network.ApiClient
import com.zynt.sumviltadconnect.data.sync.DataSynchronizer
import com.zynt.sumviltadconnect.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus

    private var dataSynchronizer: DataSynchronizer? = null

    fun checkLoginStatus(context: Context) {
        _isLoggedIn.value = TokenManager.isLoggedIn(context)
        if (_isLoggedIn.value) {
            // Initialize synchronizer for logged-in users
            initializeDataSync(context)
        }
    }

    private fun initializeDataSync(context: Context) {
        if (dataSynchronizer == null) {
            dataSynchronizer = DataSynchronizer(context)
            dataSynchronizer?.startBackgroundSync()
        }
    }

    fun login(email: String, password: String, context: Context, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        _syncStatus.value = "Logging in..."

        Log.d(TAG, "Attempting login for email: $email")

        val loginRequest = LoginRequest(email, password)

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.login(loginRequest)

                Log.d(TAG, "Login response code: ${response.code()}")
                Log.d(TAG, "Login response body: ${response.body()}")

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        Log.d(TAG, "Login response: $authResponse")

                        // Check for Laravel validation errors
                        if (authResponse.errors != null && authResponse.errors.isNotEmpty()) {
                            val errorMessages = authResponse.errors.values.flatten().joinToString(", ")
                            _errorMessage.value = errorMessages
                            _isLoading.value = false
                            _syncStatus.value = null
                            return@launch
                        }

                        // Check if login was successful
                        if (authResponse.success == false) {
                            _errorMessage.value = authResponse.message
                            _isLoading.value = false
                            _syncStatus.value = null
                            return@launch
                        }

                        authResponse.token?.let { token ->
                            // Save authentication data
                            ApiClient.saveAuthToken(token)
                            TokenManager.saveToken(context, token)
                            authResponse.user?.let { user ->
                                TokenManager.saveUserInfo(context, user.name, user.email, user.id)
                            }
                            _isLoggedIn.value = true

                            // Send FCM token to server after successful login
                            sendFcmTokenToServer(context)

                            // Initialize data synchronization
                            _syncStatus.value = "Synchronizing your data from website..."
                            initializeDataSync(context)

                            // Perform initial data sync to get all website data (in background)
                            launch {
                                dataSynchronizer?.let { sync ->
                                    val syncResult = sync.performInitialSync()
                                    when (syncResult) {
                                        is DataSynchronizer.SyncResult.Success -> {
                                            _syncStatus.value = "✓ All data synchronized successfully"
                                            Log.d(TAG, "Data sync completed: ${syncResult.message}")
                                        }
                                        is DataSynchronizer.SyncResult.PartialSuccess -> {
                                            _syncStatus.value = "⚠ Most data synchronized: ${syncResult.message}"
                                            Log.w(TAG, "Partial data sync: ${syncResult.message}")
                                        }
                                        is DataSynchronizer.SyncResult.Error -> {
                                            _syncStatus.value = "⚠ Login successful, but some data sync failed"
                                            Log.e(TAG, "Data sync failed: ${syncResult.message}")
                                        }
                                    }

                                    // Clear sync status after a delay
                                    kotlinx.coroutines.delay(3000)
                                    _syncStatus.value = null
                                }
                            }

                            _isLoading.value = false
                            onSuccess()
                        } ?: run {
                            _errorMessage.value = "No authentication token received"
                            _isLoading.value = false
                            _syncStatus.value = null
                        }
                    } ?: run {
                        _errorMessage.value = "Invalid response from server"
                        _isLoading.value = false
                        _syncStatus.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Login failed with code: ${response.code()}, error: $errorBody")
                    _errorMessage.value = when (response.code()) {
                        401 -> "Invalid email or password"
                        422 -> "Invalid input data"
                        500 -> "Server error. Please try again later"
                        else -> "Login failed: ${response.message()}"
                    }
                    _isLoading.value = false
                    _syncStatus.value = null
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _syncStatus.value = null
                Log.e(TAG, "Login error", e)
                _errorMessage.value = "Network error: ${e.message}"
            }
        }
    }

    fun register(
        context: Context,
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _errorMessage.value = "Please fill all fields"
            return
        }

        if (password != confirmPassword) {
            _errorMessage.value = "Passwords do not match"
            return
        }

        if (password.length < 8) {
            _errorMessage.value = "Password must be at least 8 characters"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        Log.d(TAG, "Attempting registration for email: $email")

        val registerRequest = RegisterRequest(
            name = name,
            email = email,
            password = password,
            password_confirmation = confirmPassword,
            role = "farmer"
        )

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.register(registerRequest)
                _isLoading.value = false

                Log.d(TAG, "Register response code: ${response.code()}")
                Log.d(TAG, "Register response body: ${response.body()}")

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        Log.d(TAG, "Registration response: $authResponse")

                        // Check for Laravel validation errors
                        if (authResponse.errors != null && authResponse.errors.isNotEmpty()) {
                            val errorMessages = authResponse.errors.values.flatten().joinToString(", ")
                            _errorMessage.value = errorMessages
                            return@launch
                        }

                        // Check if registration was successful
                        if (authResponse.success == false) {
                            _errorMessage.value = authResponse.message
                            return@launch
                        }

                        authResponse.token?.let { token ->
                            TokenManager.saveToken(context, token)
                            authResponse.user?.let { user ->
                                TokenManager.saveUserInfo(context, user.name, user.email, user.id)
                            }
                            _isLoggedIn.value = true
                            onSuccess()
                        } ?: run {
                            Log.e(TAG, "No token received in registration response")
                            _errorMessage.value = authResponse.message ?: "Registration completed but login failed. Please try logging in."
                        }
                    } ?: run {
                        Log.e(TAG, "Empty registration response body")
                        _errorMessage.value = "Registration failed: Empty response from server"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Registration failed with code ${response.code()}: $errorBody")

                    try {
                        val gson = com.google.gson.Gson()
                        val errorResponse = gson.fromJson(errorBody, com.zynt.sumviltadconnect.data.model.AuthResponse::class.java)

                        if (errorResponse?.errors != null && errorResponse.errors.isNotEmpty()) {
                            val errorMessages = errorResponse.errors.values.flatten().joinToString(", ")
                            _errorMessage.value = errorMessages
                        } else {
                            _errorMessage.value = errorResponse?.message ?: "Registration failed"
                        }
                    } catch (e: Exception) {
                        when (response.code()) {
                            422 -> _errorMessage.value = "Email already exists or invalid data provided"
                            500 -> _errorMessage.value = "Server error during registration. Please try again"
                            else -> _errorMessage.value = "Registration failed. Please try again."
                        }
                    }
                }
            } catch (t: Exception) {
                _isLoading.value = false
                Log.e(TAG, "Registration network error", t)

                val errorMessage = when {
                    t.message?.contains("ConnectException") == true -> "Cannot connect to server. Please check your internet connection and server status."
                    t.message?.contains("SocketTimeoutException") == true -> "Connection timeout. Please try again."
                    t.message?.contains("UnknownHostException") == true -> "Cannot reach server. Please check the server address."
                    else -> "Network error: ${t.message}"
                }
                _errorMessage.value = errorMessage
            }
        }
    }

    fun logout(context: Context, onSuccess: () -> Unit) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.logout()
                _isLoading.value = false
                TokenManager.clearAllData(context)
                _isLoggedIn.value = false
                onSuccess()
            } catch (t: Exception) {
                _isLoading.value = false
                // Even if logout fails on server, clear local data
                TokenManager.clearAllData(context)
                _isLoggedIn.value = false
                onSuccess()
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        _isLoggedIn.value = isLoggedIn
    }

    /**
     * Send FCM token to server after successful login
     */
    private fun sendFcmTokenToServer(context: Context) {
        viewModelScope.launch {
            try {
                // Get stored FCM token from SharedPreferences
                val prefs = context.getSharedPreferences("sumviltad_prefs", android.content.Context.MODE_PRIVATE)
                val fcmToken = prefs.getString("fcm_token", null)

                if (fcmToken != null) {
                    Log.d(TAG, "Sending FCM token to server after login...")

                    // Get auth token
                    val authToken = TokenManager.getToken(context)

                    if (authToken != null) {
                        val response = ApiClient.apiService.storeFcmToken(
                            "Bearer $authToken",
                            mapOf("token" to fcmToken)
                        )

                        if (response.isSuccessful) {
                            Log.d(TAG, "✅ FCM token sent to server successfully after login")
                        } else {
                            Log.e(TAG, "❌ Failed to send FCM token: ${response.code()}")
                            Log.e(TAG, "Response: ${response.errorBody()?.string()}")
                        }
                    } else {
                        Log.w(TAG, "⚠️ Auth token not available")
                    }
                } else {
                    Log.w(TAG, "⚠️ No FCM token stored locally")
                    // Request new FCM token
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.let { newToken ->
                                // Save token locally
                                prefs.edit().putString("fcm_token", newToken).apply()
                                Log.d(TAG, "📱 New FCM token obtained: $newToken")

                                // Send token to server
                                viewModelScope.launch {
                                    try {
                                        val authToken = TokenManager.getToken(context)
                                        if (authToken != null) {
                                            val response = ApiClient.apiService.storeFcmToken(
                                                "Bearer $authToken",
                                                mapOf("token" to newToken)
                                            )
                                            if (response.isSuccessful) {
                                                Log.d(TAG, "✅ New FCM token sent to server successfully")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error sending new FCM token", e)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in sendFcmTokenToServer", e)
            }
        }
    }

    /**
     * Auto-login with token after email verification
     * This method handles the complete login flow when the user verifies their email
     */
    fun autoLoginWithToken(context: Context, token: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        _syncStatus.value = "Verifying and logging in..."
        
        Log.d(TAG, "Auto-login with verification token")
        
        viewModelScope.launch {
            try {
                // Save the token immediately
                ApiClient.saveAuthToken(token)
                TokenManager.saveToken(context, token)
                
                // Fetch user data using the token
                val response = ApiClient.apiService.getUser()
                
                if (response.isSuccessful) {
                    response.body()?.user?.let { user ->
                        // Save user information
                        TokenManager.saveUserInfo(context, user.name, user.email, user.id)
                        _isLoggedIn.value = true
                        
                        Log.d(TAG, "✅ Auto-login successful for user: ${user.email}")
                        
                        // Send FCM token to server
                        sendFcmTokenToServer(context)
                        
                        // Initialize data synchronization
                        _syncStatus.value = "Synchronizing your data..."
                        initializeDataSync(context)
                        
                        // Perform initial data sync
                        launch {
                            dataSynchronizer?.let { sync ->
                                val syncResult = sync.performInitialSync()
                                when (syncResult) {
                                    is DataSynchronizer.SyncResult.Success -> {
                                        _syncStatus.value = "✓ All data synchronized"
                                        Log.d(TAG, "Data sync completed: ${syncResult.message}")
                                    }
                                    is DataSynchronizer.SyncResult.PartialSuccess -> {
                                        _syncStatus.value = "⚠ Most data synchronized"
                                        Log.w(TAG, "Partial data sync: ${syncResult.message}")
                                    }
                                    is DataSynchronizer.SyncResult.Error -> {
                                        _syncStatus.value = null
                                        Log.e(TAG, "Data sync failed: ${syncResult.message}")
                                    }
                                }
                                
                                kotlinx.coroutines.delay(2000)
                                _syncStatus.value = null
                            }
                        }
                        
                        _isLoading.value = false
                        onSuccess()
                    } ?: run {
                        _errorMessage.value = "Failed to get user information"
                        _isLoading.value = false
                        _syncStatus.value = null
                        Log.e(TAG, "No user data in response")
                    }
                } else {
                    _errorMessage.value = "Verification failed. Please try logging in manually."
                    _isLoading.value = false
                    _syncStatus.value = null
                    Log.e(TAG, "Auto-login failed with code: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                _isLoading.value = false
                _syncStatus.value = null
                Log.e(TAG, "Auto-login error", e)
            }
        }
    }
}
