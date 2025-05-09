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
     * Authenticate a user with PIN
     */
    suspend fun loginWithPin(pin: String, email: String? = null): AuthState {
        _authState.value = AuthState.Loading

        return try {
            val request = UserAuthRequest(email, pin)
            val response = apiService.loginUser(request)

            if (response.success) {
                // Save auth token
                credentialStore.saveAuthToken(response.token)

                // Check company schema matches device registration
                val deviceSchema = credentialStore.getCompanySchema()
                if (deviceSchema != null && deviceSchema != response.companySchema) {
                    return AuthState.Error("User belongs to a different company than this device")
                }

                AuthState.Success(response.user)
            } else {
                AuthState.Error("Authentication failed")
            }
        } catch (e: Exception) {
            AuthState.Error(e.message ?: "Unknown error")
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