// app/src/main/java/com/rfm/quickpos/presentation/features/auth/PinLoginViewModel.kt

package com.rfm.quickpos.presentation.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfm.quickpos.data.repository.AuthRepository
import com.rfm.quickpos.data.repository.AuthState
import com.rfm.quickpos.data.repository.DeviceRepository
import com.rfm.quickpos.domain.model.UiMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "PinLoginViewModel"

/**
 * ViewModel for PIN login screen
 */
class PinLoginViewModel(
    private val authRepository: AuthRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow(PinLoginViewState())
    val viewState: StateFlow<PinLoginViewState> = _viewState.asStateFlow()

    // Device UI mode from repository
    val uiMode: StateFlow<UiMode> = deviceRepository.uiMode

    init {
        Log.d(TAG, "Initializing PIN Login ViewModel")

        // Check device registration first
        if (!deviceRepository.isDeviceRegistered()) {
            Log.d(TAG, "Device not registered")
            _viewState.value = _viewState.value.copy(
                isDeviceRegistered = false
            )
        } else {
            Log.d(TAG, "Device is registered")
            _viewState.value = _viewState.value.copy(
                isDeviceRegistered = true
            )

            // Monitor auth state changes
            viewModelScope.launch {
                authRepository.authState.collectLatest { state ->
                    Log.d(TAG, "Auth state changed: $state")
                    when (state) {
                        is AuthState.Loading -> {
                            _viewState.value = _viewState.value.copy(
                                isLoading = true,
                                errorMessage = null
                            )
                        }
                        is AuthState.Success -> {
                            Log.d(TAG, "Authentication successful: ${state.userData.id}")
                            _viewState.value = _viewState.value.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                userData = state.userData,
                                errorMessage = null
                            )
                        }
                        is AuthState.Error -> {
                            Log.e(TAG, "Authentication error: ${state.message}")
                            _viewState.value = _viewState.value.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                errorMessage = state.message
                            )
                        }
                        is AuthState.Initial -> {
                            // Initial state
                            Log.d(TAG, "Initial auth state")
                            _viewState.value = _viewState.value.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                errorMessage = null
                            )
                        }
                    }
                }
            }
        }

        // Check if already authenticated
        if (authRepository.isAuthenticated()) {
            Log.d(TAG, "User is already authenticated")
            _viewState.value = _viewState.value.copy(
                isAuthenticated = true
            )
        }
    }

    /**
     * Authenticate user with email and PIN
     * @param email User's email address
     * @param pin User's 4-digit PIN
     * @param mode UI mode to use after authentication
     */
    fun authenticateWithPin(email: String, pin: String, mode: UiMode) {
        Log.d(TAG, "Attempting authentication with email: $email, PIN length: ${pin.length}, mode: $mode")
        viewModelScope.launch {
            // Check if inputs are valid
            if (pin.length < 4) {
                _viewState.value = _viewState.value.copy(
                    errorMessage = "PIN must be at least 4 digits"
                )
                return@launch
            }

            if (email.isEmpty()) {
                _viewState.value = _viewState.value.copy(
                    errorMessage = "Email is required"
                )
                return@launch
            }

            // Update UI mode if different
            if (uiMode.value != mode) {
                Log.d(TAG, "Updating UI mode to: $mode")
                deviceRepository.updateUiMode(mode)
            }

            // Set loading state
            _viewState.value = _viewState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                // Authenticate user with email and PIN
                val authState = authRepository.loginWithPin(email, pin)

                // Check if authentication was successful
                if (authState is AuthState.Success) {
                    Log.d(TAG, "Login successful with state: $authState")
                    _viewState.value = _viewState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        userData = authState.userData,
                        errorMessage = null
                    )
                } else if (authState is AuthState.Error) {
                    Log.e(TAG, "Login failed: ${authState.message}")
                    _viewState.value = _viewState.value.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        errorMessage = authState.message
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during login", e)
                _viewState.value = _viewState.value.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Clear any error message
     */
    fun clearError() {
        _viewState.value = _viewState.value.copy(
            errorMessage = null
        )
    }

    /**
     * Log out the user
     */
    fun logout() {
        Log.d(TAG, "Logging out user")
        viewModelScope.launch {
            authRepository.logout()
            _viewState.value = _viewState.value.copy(
                isAuthenticated = false,
                userData = null,
                errorMessage = null
            )
        }
    }
}

/**
 * View state for PIN login screen
 */
data class PinLoginViewState(
    val isDeviceRegistered: Boolean = true,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val userData: com.rfm.quickpos.data.remote.models.UserData? = null
)