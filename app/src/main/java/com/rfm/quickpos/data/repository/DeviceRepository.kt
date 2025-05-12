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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "DeviceRepository"

/**
 * Repository for device management operations
 * Enhanced to better integrate with backend UI mode selection
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

    // Add a polling interval for UI mode checks
    private val uiModeCheckInterval = 1000 * 60 * 30L // 30 minutes

    init {
        // Set up periodic UI mode check if device is registered
        if (isDeviceRegistered()) {
            startPeriodicUiModeCheck()
        }
    }

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
                    // FIX: Use appMode instead of uiMode
                    appMode = response.device.appMode ?: UiMode.CASHIER.name,
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
                val newMode = deviceData.appMode?.let {
                    try {
                        UiMode.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        UiMode.CASHIER  // Default to cashier if invalid
                    }
                } ?: UiMode.CASHIER

                _uiMode.value = newMode
                credentialStore.saveUiMode(newMode)

                Log.d(TAG, "Device registered successfully: ${deviceData.id}, Mode: $newMode")

                // Start periodic UI mode checking after successful registration
                startPeriodicUiModeCheck()

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
     * Enhanced to better handle UI mode from the backend
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

            Log.d(TAG, "Raw auth response: $response")

            if (response.success) {
                // Log the complete response and the top-level appMode field
                Log.d(TAG, "Device data received from server: ${response.device}")
                Log.d(TAG, "Top-level appMode from response: ${response.appMode}")

                // Update device info
                credentialStore.saveDeviceInfo(response.device)

                // Add null check before saving token
                if (response.token != null) {
                    credentialStore.saveAuthToken(response.token)
                } else {
                    Log.w(TAG, "Server returned null token for successful authentication")
                }

                // FIX: Get UI mode from the response's top-level appMode field
                val serverUiMode = response.appMode?.let {
                    try {
                        // Convert to uppercase for enum
                        val mode = it.uppercase()
                        Log.d(TAG, "Server returned appMode: $it (will convert to $mode)")
                        UiMode.valueOf(mode)
                    } catch (e: IllegalArgumentException) {
                        Log.e(TAG, "Invalid UI mode from server: $it", e)
                        null // Invalid mode name
                    }
                }

                if (serverUiMode != null) {
                    if (serverUiMode != _uiMode.value) {
                        Log.d(TAG, "Server requested UI mode change: ${_uiMode.value} -> $serverUiMode")
                    }
                    _uiMode.value = serverUiMode
                    credentialStore.saveUiMode(serverUiMode)
                } else {
                    Log.d(TAG, "Server did not specify UI mode, keeping current: ${_uiMode.value}")
                }

                val success = DeviceRegistrationState.Success(response.device.id)
                _deviceRegistrationState.value = success
                success
            } else {
                val error = DeviceRegistrationState.Error("Authentication failed")
                _deviceRegistrationState.value = error
                error
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during device authentication", e)
            val error = DeviceRegistrationState.Error(e.message ?: "Unknown error")
            _deviceRegistrationState.value = error
            error
        }
    }

    /**
     * Update UI mode based on server configuration or local change
     */
    fun updateUiMode(mode: UiMode) {
        Log.d(TAG, "Updating UI mode to: $mode")
        _uiMode.value = mode
        credentialStore.saveUiMode(mode)

        // Optional: Report the UI mode change to the server
        // This could be implemented in the future to keep the server in sync
    }

    /**
     * Start periodic UI mode checks with the server
     * This allows the backend to change the UI mode remotely
     */
    private fun startPeriodicUiModeCheck() {
        CoroutineScope(Dispatchers.IO).launch {
            // Log initialization of periodic checks
            Log.d(TAG, "Starting periodic UI mode checks every ${uiModeCheckInterval/1000/60} minutes")

            while (true) {
                // Wait for the next check interval before first check
                delay(uiModeCheckInterval)

                // Only check if device is registered and authenticated
                if (isDeviceRegistered() && credentialStore.getAuthToken() != null) {
                    try {
                        Log.d(TAG, "Performing periodic UI mode check")
                        // Re-authenticate to get the latest UI mode
                        authenticateDevice()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during periodic UI mode check", e)
                    }
                } else {
                    Log.d(TAG, "Skipping UI mode check - device not registered or not authenticated")
                }
            }
        }
    }

    /**
     * Check for UI mode update now
     * Can be called manually when more immediate update is needed
     */
    suspend fun checkUiModeUpdate() {
        if (isDeviceRegistered() && credentialStore.getAuthToken() != null) {
            Log.d(TAG, "Manually checking for UI mode updates")
            authenticateDevice()
        } else {
            Log.d(TAG, "Cannot check UI mode - device not registered or not authenticated")
        }
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