// app/src/main/java/com/rfm/quickpos/presentation/features/setup/DevicePairingViewModel.kt

package com.rfm.quickpos.presentation.features.setup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfm.quickpos.data.repository.DeviceRegistrationState
import com.rfm.quickpos.data.repository.DeviceRepository
import com.rfm.quickpos.domain.model.DevicePairingInfo
import com.rfm.quickpos.domain.model.PairingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "DevicePairingViewModel"

/**
 * ViewModel for device pairing screen
 */
class DevicePairingViewModel(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        DevicePairingState(
            pairingInfo = DevicePairingInfo(
                deviceSerial = deviceRepository.getDeviceSerialNumber()
            ),
            status = PairingStatus.INITIAL
        )
    )
    val state: StateFlow<DevicePairingState> = _state.asStateFlow()

    init {
        // Monitor device registration state changes
        viewModelScope.launch {
            deviceRepository.deviceRegistrationState.collectLatest { registrationState ->
                when (registrationState) {
                    is DeviceRegistrationState.Loading -> {
                        _state.value = _state.value.copy(
                            status = PairingStatus.PAIRING,
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                    is DeviceRegistrationState.Success -> {
                        _state.value = _state.value.copy(
                            status = PairingStatus.SUCCESS,
                            isLoading = false,
                            isPaired = true
                        )
                        Log.d(TAG, "Device paired successfully with ID: ${registrationState.deviceId}")
                    }
                    is DeviceRegistrationState.Error -> {
                        _state.value = _state.value.copy(
                            status = PairingStatus.ERROR,
                            isLoading = false,
                            errorMessage = registrationState.message
                        )
                        Log.e(TAG, "Device pairing failed: ${registrationState.message}")
                    }
                    else -> {
                        // Initial state, do nothing
                    }
                }
            }
        }
    }

    /**
     * Update pairing info
     */
    fun updatePairingInfo(pairingInfo: DevicePairingInfo) {
        _state.value = _state.value.copy(pairingInfo = pairingInfo)
    }

    /**
     * Submit device pairing request
     */
    fun submitPairing() {
        // Check required fields - using branchId not merchantId
        if (_state.value.pairingInfo.branchId.isBlank()) {
            _state.value = _state.value.copy(
                status = PairingStatus.ERROR,
                errorMessage = "Branch ID is required"
            )
            return
        }

        // Update state to loading
        _state.value = _state.value.copy(
            status = PairingStatus.PAIRING,
            isLoading = true,
            errorMessage = null
        )

        // Submit pairing request
        viewModelScope.launch {
            try {
                // Register device with the provided branch ID
                deviceRepository.registerDevice(_state.value.pairingInfo.branchId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    status = PairingStatus.ERROR,
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    /**
     * Check if device is already registered
     */
    fun checkDeviceRegistration() {
        if (deviceRepository.isDeviceRegistered()) {
            viewModelScope.launch {
                deviceRepository.authenticateDevice()
            }
        }
    }
}