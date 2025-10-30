package com.zynt.sumviltadconnect.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.model.User
import com.zynt.sumviltadconnect.data.model.UpdateProfileRequest
import com.zynt.sumviltadconnect.data.model.FarmAreaRequest
import com.zynt.sumviltadconnect.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FarmerViewModel : ViewModel() {

    companion object {
        private const val TAG = "FarmerViewModel"
    }

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Log.d(TAG, "Fetching user profile...")
                val response = ApiClient.apiService.getUser()

                if (response.isSuccessful && response.body() != null) {
                    val userResponse = response.body()
                    val user = userResponse?.user

                    if (user != null) {
                        _userProfile.value = user
                        Log.d(TAG, "User profile loaded successfully: ${user.name}")
                    } else {
                        _error.value = "User data not available"
                        Log.e(TAG, "User profile is null in response")
                    }
                } else {
                    _error.value = "Failed to load profile: ${response.message()}"
                    Log.e(TAG, "User profile API error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = "Error loading profile: ${e.message}"
                Log.e(TAG, "User profile fetch failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshProfile() {
        fetchUserProfile()
    }

    /**
     * Update user profile
     */
    fun updateProfile(
        name: String? = null,
        email: String? = null,
        firstName: String? = null,
        middleInitial: String? = null,
        lastName: String? = null,
        completeAddress: String? = null,
        farmLocation: String? = null,
        contactNumber: String? = null,
        birthday: String? = null,
        riceFieldArea: Double? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Log.d(TAG, "Updating user profile...")

                val request = UpdateProfileRequest(
                    name = name,
                    email = email,
                    firstName = firstName,
                    middleInitial = middleInitial,
                    lastName = lastName,
                    completeAddress = completeAddress,
                    farmLocation = farmLocation,
                    contactNumber = contactNumber,
                    birthday = birthday,
                    riceFieldArea = riceFieldArea
                )

                val response = ApiClient.apiService.updateProfile(request)

                if (response.isSuccessful && response.body() != null) {
                    val userResponse = response.body()
                    val user = userResponse?.user

                    if (user != null) {
                        _userProfile.value = user
                        Log.d(TAG, "Profile updated successfully: ${user.name}")
                        onSuccess()
                    } else {
                        val errorMsg = "User data not available after update"
                        _error.value = errorMsg
                        Log.e(TAG, errorMsg)
                        onError(errorMsg)
                    }
                } else {
                    val errorMsg = "Failed to update profile: ${response.message()}"
                    _error.value = errorMsg
                    Log.e(TAG, "Profile update API error: ${response.code()} - ${response.message()}")
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error updating profile: ${e.message}"
                _error.value = errorMsg
                Log.e(TAG, "Profile update failed", e)
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a new farm area
     */
    fun addFarmArea(
        farmLocation: String,
        riceFieldArea: Double,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Log.d(TAG, "Adding farm area: $farmLocation")

                val request = FarmAreaRequest(
                    farmLocation = farmLocation,
                    riceFieldArea = riceFieldArea
                )

                val response = ApiClient.apiService.createFarmArea(request)

                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "Farm area added successfully")
                    // Refresh the profile to get updated farm areas
                    fetchUserProfile()
                    onSuccess()
                } else {
                    val errorMsg = "Failed to add farm area: ${response.message()}"
                    _error.value = errorMsg
                    Log.e(TAG, "Add farm area API error: ${response.code()} - ${response.message()}")
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error adding farm area: ${e.message}"
                _error.value = errorMsg
                Log.e(TAG, "Add farm area failed", e)
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing farm area
     */
    fun updateFarmArea(
        farmAreaId: Int,
        farmLocation: String,
        riceFieldArea: Double,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Log.d(TAG, "Updating farm area: $farmAreaId")

                val request = FarmAreaRequest(
                    farmLocation = farmLocation,
                    riceFieldArea = riceFieldArea
                )

                val response = ApiClient.apiService.updateFarmArea(farmAreaId, request)

                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "Farm area updated successfully")
                    // Refresh the profile to get updated farm areas
                    fetchUserProfile()
                    onSuccess()
                } else {
                    val errorMsg = "Failed to update farm area: ${response.message()}"
                    _error.value = errorMsg
                    Log.e(TAG, "Update farm area API error: ${response.code()} - ${response.message()}")
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error updating farm area: ${e.message}"
                _error.value = errorMsg
                Log.e(TAG, "Update farm area failed", e)
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a farm area
     */
    fun deleteFarmArea(
        farmAreaId: Int,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Log.d(TAG, "Deleting farm area: $farmAreaId")

                val response = ApiClient.apiService.deleteFarmArea(farmAreaId)

                if (response.isSuccessful) {
                    Log.d(TAG, "Farm area deleted successfully")
                    // Refresh the profile to get updated farm areas
                    fetchUserProfile()
                    onSuccess()
                } else {
                    val errorMsg = "Failed to delete farm area: ${response.message()}"
                    _error.value = errorMsg
                    Log.e(TAG, "Delete farm area API error: ${response.code()} - ${response.message()}")
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error deleting farm area: ${e.message}"
                _error.value = errorMsg
                Log.e(TAG, "Delete farm area failed", e)
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
