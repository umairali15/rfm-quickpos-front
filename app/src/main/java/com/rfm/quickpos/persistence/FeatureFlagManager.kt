package com.rfm.quickpos.domain.manager

import android.content.Context
import android.content.SharedPreferences
import com.rfm.quickpos.domain.model.UiMode

/**
 * Manages feature flags for the application based on UI mode.
 * Controls which features are enabled in each mode.
 */
class FeatureFlagManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "rfm_quickpos_feature_flags"

        // Feature flag keys
        const val FEATURE_MANUAL_AMOUNT_ENTRY = "feature_manual_amount_entry"
        const val FEATURE_QUANTITY_STEPPER = "feature_quantity_stepper"
        const val FEATURE_DISCOUNT = "feature_discount"
        const val FEATURE_COMMENTS = "feature_comments"
        const val FEATURE_CUSTOMER_SELECTION = "feature_customer_selection"
        const val FEATURE_CASH_PAYMENT = "feature_cash_payment"
        const val FEATURE_SPLIT_PAYMENT = "feature_split_payment"
        const val FEATURE_WALLET_PAYMENT = "feature_wallet_payment"
        const val FEATURE_AUTO_PRINT = "feature_auto_print"
        const val FEATURE_AUTO_RESET = "feature_auto_reset"
        const val FEATURE_MANAGER_PIN = "feature_manager_pin"

        // Default values for cashier mode
        private val CASHIER_DEFAULTS = mapOf(
            FEATURE_MANUAL_AMOUNT_ENTRY to true,
            FEATURE_QUANTITY_STEPPER to true,
            FEATURE_DISCOUNT to true,
            FEATURE_COMMENTS to true,
            FEATURE_CUSTOMER_SELECTION to true,
            FEATURE_CASH_PAYMENT to true,
            FEATURE_SPLIT_PAYMENT to true,
            FEATURE_WALLET_PAYMENT to true,
            FEATURE_AUTO_PRINT to false,
            FEATURE_AUTO_RESET to false,
            FEATURE_MANAGER_PIN to false
        )

        // Default values for kiosk mode
        private val KIOSK_DEFAULTS = mapOf(
            FEATURE_MANUAL_AMOUNT_ENTRY to false,
            FEATURE_QUANTITY_STEPPER to false,
            FEATURE_DISCOUNT to false,
            FEATURE_COMMENTS to false,
            FEATURE_CUSTOMER_SELECTION to false,
            FEATURE_CASH_PAYMENT to false,
            FEATURE_SPLIT_PAYMENT to false,
            FEATURE_WALLET_PAYMENT to true,
            FEATURE_AUTO_PRINT to true,
            FEATURE_AUTO_RESET to true,
            FEATURE_MANAGER_PIN to true
        )
    }

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Check if a feature is enabled for the current UI mode
     */
    fun isFeatureEnabled(featureKey: String, uiMode: UiMode): Boolean {
        // Get default value based on UI mode
        val defaultValue = when (uiMode) {
            UiMode.CASHIER -> CASHIER_DEFAULTS[featureKey] ?: false
            UiMode.KIOSK -> KIOSK_DEFAULTS[featureKey] ?: false
        }

        // Return the stored value or default
        return preferences.getBoolean("${uiMode.name}_$featureKey", defaultValue)
    }

    /**
     * Set a feature flag value
     */
    fun setFeatureEnabled(featureKey: String, uiMode: UiMode, enabled: Boolean) {
        preferences.edit().putBoolean("${uiMode.name}_$featureKey", enabled).apply()
    }

    /**
     * Reset all feature flags to their default values for the given UI mode
     */
    fun resetToDefaults(uiMode: UiMode) {
        val editor = preferences.edit()

        val defaults = when (uiMode) {
            UiMode.CASHIER -> CASHIER_DEFAULTS
            UiMode.KIOSK -> KIOSK_DEFAULTS
        }

        defaults.forEach { (key, value) ->
            editor.putBoolean("${uiMode.name}_$key", value)
        }

        editor.apply()
    }

    /**
     * Get all feature flags for a given UI mode
     */
    fun getAllFeatures(uiMode: UiMode): Map<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()

        val defaults = when (uiMode) {
            UiMode.CASHIER -> CASHIER_DEFAULTS
            UiMode.KIOSK -> KIOSK_DEFAULTS
        }

        defaults.forEach { (key, defaultValue) ->
            val prefKey = "${uiMode.name}_$key"
            result[key] = preferences.getBoolean(prefKey, defaultValue)
        }

        return result
    }
}