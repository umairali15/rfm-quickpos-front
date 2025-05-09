// app/src/main/java/com/rfm/quickpos/data/repository/DeviceRepository.kt

package com.rfm.quickpos.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import com.rfm.quickpos.BuildConfig
import com.rfm.quickpos.data.local.storage.SecureCredentialStore
import com.rfm.quickpos.data.remote.api.ApiService
import com.rfm.quickpos.data.remote.models.DeviceAuthRequest
import com.rfm.quickpos.data.remote.models.DeviceData
import com.rfm.quickpos.data.remote.models.DeviceRegistrationRequest
import com.rfm.quickpos.domain.model.UiMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "DeviceRepository"

/**
 * Repository for device management operations
 */
class DeviceRepository(
    private val apiService: ApiService,
    private val credentialStore: SecureCredentialStore,
    private val context: Context
) {
    private val _deviceRegistrationState = MutableStateFlow<DeviceRegistrationState>(DeviceRegistrationState.Initial)
    val deviceRegistrationState: StateFlow<DeviceRegistrationState> = _deviceRegistrationState.asStateFlow()

    private val _uiMode = MutableStateFlow(credentialStore.getUiMode())
    val uiMode: StateFlow<UiMode> = _uiMode.asStateFlow()

    /**
     * Check if device is already registered
     */
    fun isDeviceRegistered(): Boolean {
        val deviceId = credentialStore.getDeviceId()
        val serialNumber = credentialStore.getSerialNumber()
        return deviceId != null && serialNumber != null
    }

    /**
     * Get the device serial number or a unique identifier
     */
    fun getDeviceSerialNumber(): String {
        val savedSerial = credentialStore.getSerialNumber()
        if (!savedSerial.isNullOrEmpty()) {
            return savedSerial
        }

        // Generate a unique device identifier
        val uniqueSerial = "android_${Build.BRAND}_${Build.PRODUCT}_${System.currentTimeMillis()}"

        // Save and return serial
        credentialStore.saveSerialNumber(uniqueSerial)
        return uniqueSerial
    }

    /**
     * Save custom serial number
     */
    fun saveSerialNumber(serialNumber: String) {
        if (serialNumber.isNotBlank()) {
            credentialStore.saveSerialNumber(serialNumber)
        }
    }

    /**
     * Register device with the server
     */
    suspend fun registerDevice(branchId: String): DeviceRegistrationState {
        _deviceRegistrationState.value = DeviceRegistrationState.Loading

        // Validate branch ID
        if (branchId.isBlank()) {
            val error = DeviceRegistrationState.Error("Branch ID is required")
            _deviceRegistrationState.value = error
            return error
        }

        // Get device serial number
        val deviceSerialNumber = getDeviceSerialNumber()

        // Generate device alias based on model name
        val deviceAlias = "Device-${Build.MODEL.take(8)}-${System.currentTimeMillis() % 10000}"

        return try {
            val request = DeviceRegistrationRequest(
                alias = deviceAlias,
                serialNumber = deviceSerialNumber,
                model = Build.MODEL,
                branchId = branchId,
                appVersion = BuildConfig.VERSION_NAME
            )

            Log.d(TAG, "Registering device with branch ID: $branchId, serial: $deviceSerialNumber")
            val response = apiService.registerDevice(request)
            Log.d(TAG, "Registration response: $response")

            if (response.success && response.device != null) {
                // Create a DeviceData object from the response
                val deviceData = DeviceData(
                    id = response.device.id,
                    alias = response.device.alias,
                    branchId = response.device.branchId,
                    // If these are null, use default values
                    companyId = response.device.companyId ?: "",
                    companySchema = response.device.companySchema ?: "company_default",
                    tableId = response.device.tableId,
                    isActive = response.device.isActive,
                    uiMode = response.device.uiMode ?: UiMode.CASHIER.name,
                    serialNumber = response.device.serialNumber ?: deviceSerialNumber
                )

                // Save device info
                credentialStore.saveDeviceInfo(deviceData)

                // Save token if provided
                if (response.token != null) {
                    credentialStore.saveAuthToken(response.token)
                }

                // Save serial number again to ensure it's consistent
                credentialStore.saveSerialNumber(deviceSerialNumber)

                // Update UI mode
                val newMode = deviceData.uiMode?.let {
                    try {
                        UiMode.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        UiMode.CASHIER  // Default to cashier if invalid
                    }
                } ?: UiMode.CASHIER

                _uiMode.value = newMode

                Log.d(TAG, "Device registered successfully: ${deviceData.id}, Mode: $newMode")
                val success = DeviceRegistrationState.Success(deviceData.id)
                _deviceRegistrationState.value = success
                success
            } else {
                val errorMsg = response.error ?: response.message ?: "Registration failed"
                Log.e(TAG, "Registration failed: $errorMsg")
                val error = DeviceRegistrationState.Error(errorMsg)
                _deviceRegistrationState.value = error
                error
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during registration", e)
            val error = DeviceRegistrationState.Error(e.message ?: "Unknown error")
            _deviceRegistrationState.value = error
            error
        }
    }

    /**
     * Authenticate a previously registered device
     */
    suspend fun authenticateDevice(): DeviceRegistrationState {
        val deviceId = credentialStore.getDeviceId() ?:
        return DeviceRegistrationState.Error("Device not registered")

        val serialNumber = credentialStore.getSerialNumber() ?:
        return DeviceRegistrationState.Error("Serial number missing")

        _deviceRegistrationState.value = DeviceRegistrationState.Loading

        return try {
            val request = DeviceAuthRequest(deviceId, serialNumber)
            val response = apiService.authenticateDevice(request)

            if (response.success) {
                // Update device info and token
                credentialStore.saveDeviceInfo(response.device)
                credentialStore.saveAuthToken(response.token)

                // Update UI mode from response
                val newMode = response.device.uiMode?.let {
                    try {
                        UiMode.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        UiMode.CASHIER  // Default to cashier if invalid
                    }
                } ?: UiMode.CASHIER

                _uiMode.value = newMode

                val success = DeviceRegistrationState.Success(response.device.id)
                _deviceRegistrationState.value = success
                success
            } else {
                val error = DeviceRegistrationState.Error("Authentication failed")
                _deviceRegistrationState.value = error
                error
            }
        } catch (e: Exception) {
            val error = DeviceRegistrationState.Error(e.message ?: "Unknown error")
            _deviceRegistrationState.value = error
            error
        }
    }

    /**
     * Update UI mode based on server configuration
     */
    fun updateUiMode(mode: UiMode) {
        _uiMode.value = mode
        credentialStore.saveUiMode(mode)
    }
}

/**
 * States for device registration process
 */
sealed class DeviceRegistrationState {
    object Initial : DeviceRegistrationState()
    object Loading : DeviceRegistrationState()
    data class Success(val deviceId: String) : DeviceRegistrationState()
    data class Error(val message: String) : DeviceRegistrationState()
}