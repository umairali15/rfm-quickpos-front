// app/src/main/java/com/rfm/quickpos/domain/manager/UiModeManager.kt

package com.rfm.quickpos.domain.manager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.rfm.quickpos.data.local.storage.SecureCredentialStore
import com.rfm.quickpos.data.repository.DeviceRepository
import com.rfm.quickpos.domain.model.UiMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "UiModeManager"

/**
 * Enhanced UiModeManager that works with DeviceRepository
 * to respect backend UI mode settings
 */
class UiModeManager(
    private val context: Context,
    private val deviceRepository: DeviceRepository,
    private val credentialStore: SecureCredentialStore
) {

    companion object {
        private const val PREFS_NAME = "rfm_quickpos_preferences"
        private const val KEY_UI_MODE = "ui_mode"
        private const val KIOSK_PIN = "5678" // For demo purposes, different PIN for kiosk mode
    }

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Use device repository's UI mode flow
    val currentMode: StateFlow<UiMode> = deviceRepository.uiMode

    init {
        // Monitor device repository's UI mode changes
        CoroutineScope(Dispatchers.Main).launch {
            deviceRepository.uiMode.collectLatest { mode ->
                Log.d(TAG, "UI mode updated from DeviceRepository: $mode")

                // Update local preferences to stay in sync
                preferences.edit().putString(KEY_UI_MODE, mode.name).apply()
            }
        }
    }

    /**
     * Set the UI mode and persist it
     * This will update both the local preferences and the device repository
     */
    fun setMode(mode: UiMode) {
        Log.d(TAG, "Setting UI mode: $mode")

        // Update device repository (which handles persistence)
        deviceRepository.updateUiMode(mode)
    }

    /**
     * Check if the provided PIN corresponds to kiosk mode
     */
    fun isPinForKioskMode(pin: String): Boolean {
        return pin == KIOSK_PIN
    }

    /**
     * Toggle between cashier and kiosk modes
     */
    fun toggleMode() {
        val currentValue = currentMode.value
        val newMode = if (currentValue == UiMode.CASHIER) UiMode.KIOSK else UiMode.CASHIER
        setMode(newMode)
    }

    /**
     * Exit kiosk mode with manager PIN
     * @return true if PIN is correct and mode was changed
     */
    fun exitKioskMode(managerPin: String): Boolean {
        // In a real app, validate against actual manager credentials
        val validManagerPin = "1234" // For demo only
        if (managerPin == validManagerPin) {
            setMode(UiMode.CASHIER)
            return true
        }
        return false
    }

    /**
     * Check with backend for updated UI mode setting
     */
    suspend fun checkForUiModeUpdates() {
        Log.d(TAG, "Checking backend for UI mode updates")
        deviceRepository.checkUiModeUpdate()
    }
}