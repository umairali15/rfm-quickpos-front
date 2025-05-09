// app/src/main/java/com/rfm/quickpos/data/repository/DeviceRepository.kt

package com.rfm.quickpos.data.repository

import android.content.Context
import android.os.Build
import com.rfm.quickpos.BuildConfig
import com.rfm.quickpos.data.local.storage.SecureCredentialStore
import com.rfm.quickpos.data.remote.api.ApiService
import com.rfm.quickpos.data.remote.models.DeviceAuthRequest
import com.rfm.quickpos.data.remote.models.DeviceRegistrationRequest
import com.rfm.quickpos.domain.model.UiMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
        val companySchema = credentialStore.getCompanySchema()

        return deviceId != null && serialNumber != null && companySchema != null
    }

    /**
     * Get the device serial number or a unique identifier
     */
    fun getDeviceSerialNumber(): String {
        val savedSerial = credentialStore.getSerialNumber()
        if (!savedSerial.isNullOrEmpty()) {
            return savedSerial
        }

        // Get device serial or generate a unique ID
        val serial = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Build.getSerial()
            } catch (e: Exception) {
                Build.SERIAL ?: Build.FINGERPRINT
            }
        } else {
            Build.SERIAL ?: Build.FINGERPRINT
        }

        // Save and return serial
        credentialStore.saveSerialNumber(serial)
        return serial
    }

    /**
     * Save custom serial number
     */
    fun saveSerialNumber(serialNumber: String) {
        if (serialNumber.isNotBlank()) {
            credentialStore.saveSerialNumber(serialNumber)
        }
    }

    // Replace the registerDevice method with this version
    suspend fun registerDevice(
        deviceAlias: String,
        branchId: String,
        serialNumber: String? = null
    ): DeviceRegistrationState {
        _deviceRegistrationState.value = DeviceRegistrationState.Loading

        // The auth token should already be available from prior user login
        val authToken = credentialStore.getAuthToken()
        if (authToken == null) {
            return DeviceRegistrationState.Error("Authentication required before device registration")
        }

        // Validate required fields
        if (deviceAlias.isBlank()) {
            return DeviceRegistrationState.Error("Device alias is required")
        }

        if (branchId.isBlank()) {
            return DeviceRegistrationState.Error("Branch ID is required")
        }

        // Use provided serial number or get from device, but make sure it's not "unknown"
        var deviceSerialNumber = serialNumber?.takeIf { it.isNotBlank() } ?: getDeviceSerialNumber()

        // Ensure we never send "unknown" as a serial number
        if (deviceSerialNumber.equals("unknown", ignoreCase = true)) {
            // Generate something more unique
            deviceSerialNumber = "android_${Build.BRAND}_${Build.PRODUCT}_${System.currentTimeMillis()}"
        }

        return try {
            val request = DeviceRegistrationRequest(
                alias = deviceAlias,
                serialNumber = deviceSerialNumber,
                model = Build.MODEL,
                branchId = branchId,
                appVersion = BuildConfig.VERSION_NAME
            )

            val response = apiService.registerDevice(request)

            if (response.success && response.data != null && response.token != null) {
                // Save device info and token
                credentialStore.saveDeviceInfo(response.data)
                credentialStore.saveAuthToken(response.token)

                // Save the serial number we used
                credentialStore.saveSerialNumber(deviceSerialNumber)

                // Update UI mode from response
                val newMode = response.data.uiMode?.let {
                    try {
                        UiMode.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        UiMode.CASHIER  // Default to cashier if invalid
                    }
                } ?: UiMode.CASHIER

                _uiMode.value = newMode

                DeviceRegistrationState.Success(response.data.id)
            } else {
                DeviceRegistrationState.Error(response.error ?: "Registration failed: Backend timestamp format issue. Please contact support.")
            }
        } catch (e: Exception) {
            DeviceRegistrationState.Error(e.message ?: "Unknown error")
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

                DeviceRegistrationState.Success(response.device.id)
            } else {
                DeviceRegistrationState.Error("Authentication failed")
            }
        } catch (e: Exception) {
            DeviceRegistrationState.Error(e.message ?: "Unknown error")
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