// app/src/main/java/com/rfm/quickpos/presentation/features/auth/PinLoginViewModel.kt

package com.rfm.quickpos.presentation.features.auth

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
        // Check device registration first
        if (!deviceRepository.isDeviceRegistered()) {
            _viewState.value = _viewState.value.copy(
                isDeviceRegistered = false
            )
        } else {
            _viewState.value = _viewState.value.copy(
                isDeviceRegistered = true
            )

            // Monitor auth state changes
            viewModelScope.launch {
                authRepository.authState.collectLatest { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            _viewState.value = _viewState.value.copy(
                                isLoading = true,
                                errorMessage = null
                            )
                        }
                        is AuthState.Success -> {
                            _viewState.value = _viewState.value.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                userData = state.userData
                            )
                        }
                        is AuthState.Error -> {
                            _viewState.value = _viewState.value.copy(
                                isLoading = false,
                                errorMessage = state.message
                            )
                        }
                        else -> {
                            // Initial state
                        }
                    }
                }
            }
        }
    }

    /**
     * Authenticate user with email and PIN
     * @param email User's email address
     * @param pin User's 4-digit PIN
     * @param mode UI mode to use after authentication
     */
    fun authenticateWithPin(email: String, pin: String, mode: UiMode) {
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
                deviceRepository.updateUiMode(mode)
            }

            // Authenticate user with email and PIN
            authRepository.loginWithPin(email, pin)
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