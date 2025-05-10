// app/src/main/java/com/rfm/quickpos/data/repository/AuthRepository.kt

package com.rfm.quickpos.data.repository

import android.util.Log
import com.rfm.quickpos.data.local.storage.SecureCredentialStore
import com.rfm.quickpos.data.remote.api.ApiService
import com.rfm.quickpos.data.remote.models.UserAuthRequest
import com.rfm.quickpos.data.remote.models.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "AuthRepository"

/**
 * Repository for user authentication with enhanced company validation
 */
class AuthRepository(
    private val apiService: ApiService,
    private val credentialStore: SecureCredentialStore
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Check if token exists on init and set initial state
        if (isAuthenticated()) {
            val storedUserData = credentialStore.getUserData()
            val companySchema = credentialStore.getCompanySchema()
            if (storedUserData != null && companySchema != null) {
                _authState.value = AuthState.Success(
                    storedUserData,
                    companySchema
                )
                Log.d(TAG, "Initialized with existing auth: ${storedUserData.id}, schema=$companySchema")
            } else {
                Log.d(TAG, "Token exists but missing user data or schema - will need to re-authenticate")
            }
        } else {
            Log.d(TAG, "No authentication found on init")
        }
    }

    /**
     * Authenticate a user with email and PIN
     * @param email User's email address
     * @param pin User's 4-digit PIN
     * @return Authentication state after login attempt
     */
    suspend fun loginWithPin(email: String, pin: String): AuthState {
        _authState.value = AuthState.Loading

        Log.d(TAG, "Attempting login with email: $email and PIN")

        // Check if device is registered to a company
        val deviceCompanyId = credentialStore.getCompanyId()
        val deviceCompanySchema = credentialStore.getCompanySchema()

        return try {
            val request = UserAuthRequest(email, pin)
            val response = apiService.loginUser(request)

            Log.d(TAG, "Login response: $response")

            if (response.success) {
                // Extract company info from JWT
                credentialStore.saveAuthToken(response.token)
                val userCompanyId = credentialStore.getCompanyId()
                val userCompanySchema = credentialStore.getCompanySchema()

                Log.d(TAG, "User company: $userCompanyId, Device company: $deviceCompanyId")

                // Validate company match if device is already registered
                if (deviceCompanyId != null && userCompanyId != null && deviceCompanyId != userCompanyId) {
                    Log.e(TAG, "Company mismatch: Device registered to $deviceCompanyId, user belongs to $userCompanyId")

                    // Clear the auth token since it's invalid for this device
                    credentialStore.clearAuthToken()

                    val errorState = AuthState.Error(
                        "This device is registered to a different company. Please use the correct device or contact your administrator."
                    )
                    _authState.value = errorState
                    return errorState
                }

                // Save user data
                credentialStore.saveUserData(response.user)

                // Update state with successful authentication
                val newState = AuthState.Success(response.user, userCompanySchema ?: "")
                _authState.value = newState

                Log.d(TAG, "Authentication successful: ${response.user.id}, Schema: $userCompanySchema")
                return newState
            } else {
                val errorState = AuthState.Error("Authentication failed")
                _authState.value = errorState
                Log.e(TAG, "Authentication failed: Server returned unsuccessful response")
                return errorState
            }
        } catch (e: Exception) {
            Log.e(TAG, "Authentication error", e)
            val errorState = AuthState.Error(e.message ?: "Unknown error")
            _authState.value = errorState
            return errorState
        }
    }

    /**
     * Log out the current user
     */
    fun logout() {
        Log.d(TAG, "Logging out user")
        credentialStore.clearAuthToken()
        credentialStore.clearUserData()
        _authState.value = AuthState.Initial
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        val hasToken = credentialStore.getAuthToken() != null
        Log.d(TAG, "isAuthenticated check: $hasToken")
        return hasToken
    }

    /**
     * Check if the current user belongs to the same company as the device
     */
    fun validateCompanyMatch(): Boolean {
        val deviceCompanyId = credentialStore.getCompanyId()
        val userCompanyId = credentialStore.getCompanyId()

        // If no device company is set, allow login
        if (deviceCompanyId == null) return true

        // Check if companies match
        return deviceCompanyId == userCompanyId
    }
}

/**
 * States for user authentication
 */
sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val userData: UserData, val companySchema: String) : AuthState()
    data class Error(val message: String) : AuthState()

    override fun toString(): String {
        return when(this) {
            is Initial -> "Initial"
            is Loading -> "Loading"
            is Success -> "Success(user=${userData.id}, schema=$companySchema)"
            is Error -> "Error(message=$message)"
        }
    }
}