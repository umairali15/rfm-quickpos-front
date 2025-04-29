package com.rfm.quickpos.domain.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import com.rfm.quickpos.domain.model.UiMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the UI mode of the application.
 * Handles switching between Cashier and Kiosk modes and persists the selection.
 */
class UiModeManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "rfm_quickpos_preferences"
        private const val KEY_UI_MODE = "ui_mode"
        private const val KIOSK_PIN = "5678" // For demo purposes, we'll use a different PIN for kiosk mode
    }

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _currentMode = MutableStateFlow(getPersistedMode())
    val currentMode: StateFlow<UiMode> = _currentMode.asStateFlow()

    /**
     * Get the persisted UI mode from SharedPreferences
     */
    private fun getPersistedMode(): UiMode {
        val modeName = preferences.getString(KEY_UI_MODE, UiMode.CASHIER.name)
        return try {
            UiMode.valueOf(modeName ?: UiMode.CASHIER.name)
        } catch (e: IllegalArgumentException) {
            UiMode.CASHIER
        }
    }

    /**
     * Set the UI mode and persist it
     */
    fun setMode(mode: UiMode) {
        _currentMode.value = mode
        preferences.edit().putString(KEY_UI_MODE, mode.name).apply()
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
        val newMode = if (_currentMode.value == UiMode.CASHIER) UiMode.KIOSK else UiMode.CASHIER
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
}