// app/src/main/java/com/rfm/quickpos/data/repository/AuthRepository.kt

package com.rfm.quickpos.data.repository

import com.rfm.quickpos.data.local.storage.SecureCredentialStore
import com.rfm.quickpos.data.remote.api.ApiService
import com.rfm.quickpos.data.remote.models.UserAuthRequest
import com.rfm.quickpos.data.remote.models.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for user authentication
 */
class AuthRepository(
    private val apiService: ApiService,
    private val credentialStore: SecureCredentialStore
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Authenticate a user with email and PIN
     * @param email User's email address
     * @param pin User's 4-digit PIN
     * @return Authentication state after login attempt
     */
    suspend fun loginWithPin(email: String, pin: String): AuthState {
        _authState.value = AuthState.Loading

        return try {
            val request = UserAuthRequest(email, pin)
            val response = apiService.loginUser(request)

            if (response.success) {
                // Save auth token
                credentialStore.saveAuthToken(response.token)

                // Save company schema
                credentialStore.saveCompanySchema(response.companySchema)

                // Update state
                val newState = AuthState.Success(response.user)
                _authState.value = newState
                return newState
            } else {
                val errorState = AuthState.Error("Authentication failed")
                _authState.value = errorState
                return errorState
            }
        } catch (e: Exception) {
            val errorState = AuthState.Error(e.message ?: "Unknown error")
            _authState.value = errorState
            return errorState
        }
    }

    /**
     * Log out the current user
     */
    fun logout() {
        credentialStore.clearAuthToken()
        _authState.value = AuthState.Initial
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return credentialStore.getAuthToken() != null
    }
}

/**
 * States for user authentication
 */
sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val userData: UserData) : AuthState()
    data class Error(val message: String) : AuthState()
}