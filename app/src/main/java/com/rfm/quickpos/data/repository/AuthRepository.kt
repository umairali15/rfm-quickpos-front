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
 * Repository for user authentication
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

        return try {
            val request = UserAuthRequest(email, pin)
            val response = apiService.loginUser(request)

            Log.d(TAG, "Login response: $response")

            if (response.success) {
                // Save auth token (this also extracts and saves JWT data including schema)
                credentialStore.saveAuthToken(response.token)

                // Save user data
                credentialStore.saveUserData(response.user)

                // Get company schema that was extracted from JWT
                val companySchema = credentialStore.getCompanySchema()
                if (companySchema == null) {
                    Log.w(TAG, "Could not retrieve company schema after login")
                }

                // Update state
                val newState = AuthState.Success(response.user, companySchema ?: "")
                _authState.value = newState

                Log.d(TAG, "Authentication successful: ${response.user.id}, Schema: $companySchema")
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