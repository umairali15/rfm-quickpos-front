// app/src/main/java/com/rfm/quickpos/presentation/features/setup/DevicePairingViewModel.kt

package com.rfm.quickpos.presentation.features.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfm.quickpos.data.repository.DeviceRegistrationState
import com.rfm.quickpos.data.repository.DeviceRepository
import com.rfm.quickpos.domain.model.DevicePairingInfo
import com.rfm.quickpos.domain.model.PairingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        // Check required fields
        if (_state.value.pairingInfo.merchantId.isBlank()) {
            _state.value = _state.value.copy(
                status = PairingStatus.ERROR,
                errorMessage = "Pairing code is required"
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
            when (val result = deviceRepository.registerDevice(_state.value.pairingInfo.merchantId)) {
                is DeviceRegistrationState.Success -> {
                    _state.value = _state.value.copy(
                        status = PairingStatus.SUCCESS,
                        isLoading = false,
                        isPaired = true
                    )
                }
                is DeviceRegistrationState.Error -> {
                    _state.value = _state.value.copy(
                        status = PairingStatus.ERROR,
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {
                    // Handle other states
                }
            }
        }
    }

    /**
     * Check if device is already registered
     */
    fun checkDeviceRegistration() {
        if (deviceRepository.isDeviceRegistered()) {
            viewModelScope.launch {
                when (val result = deviceRepository.authenticateDevice()) {
                    is DeviceRegistrationState.Success -> {
                        _state.value = _state.value.copy(
                            status = PairingStatus.SUCCESS,
                            isLoading = false,
                            isPaired = true
                        )
                    }
                    is DeviceRegistrationState.Error -> {
                        // If authentication fails, we'll need to re-register
                        _state.value = _state.value.copy(
                            status = PairingStatus.INITIAL
                        )
                    }
                    else -> {
                        // Handle other states
                    }
                }
            }
        }
    }
}