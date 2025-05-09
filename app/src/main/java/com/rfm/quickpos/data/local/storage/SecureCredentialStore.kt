// app/src/main/java/com/rfm/quickpos/data/local/storage/SecureCredentialStore.kt

package com.rfm.quickpos.data.local.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.rfm.quickpos.data.remote.models.DeviceData
import com.rfm.quickpos.domain.model.UiMode

private const val TAG = "SecureCredentialStore"

/**
 * Secure store for credentials and device information
 */
class SecureCredentialStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Device information
    fun saveDeviceInfo(deviceData: DeviceData) {
        Log.d(TAG, "Saving device info: $deviceData")

        val editor = preferences.edit()

        // Save essential device information
        editor.putString(KEY_DEVICE_ID, deviceData.id)
        editor.putString(KEY_DEVICE_ALIAS, deviceData.alias)

        // Save optional device information
        deviceData.branchId?.let { editor.putString(KEY_BRANCH_ID, it) }
        deviceData.companyId?.let { editor.putString(KEY_COMPANY_ID, it) }
        deviceData.companySchema?.let { editor.putString(KEY_COMPANY_SCHEMA, it) }
        deviceData.tableId?.let { editor.putString(KEY_TABLE_ID, it) }
        editor.putBoolean(KEY_IS_ACTIVE, deviceData.isActive)

        // Apply changes
        editor.apply()

        // Convert UI mode string to enum (if available)
        deviceData.uiMode?.let {
            val uiMode = try {
                UiMode.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                UiMode.CASHIER // Default to CASHIER if invalid
            }
            saveUiMode(uiMode)
        }
    }

    fun getDeviceId(): String? {
        return preferences.getString(KEY_DEVICE_ID, null)
    }

    fun getSerialNumber(): String? {
        return preferences.getString(KEY_SERIAL_NUMBER, null)
    }

    fun saveSerialNumber(serialNumber: String) {
        preferences.edit()
            .putString(KEY_SERIAL_NUMBER, serialNumber)
            .apply()
    }

    fun getCompanySchema(): String? {
        return preferences.getString(KEY_COMPANY_SCHEMA, null)
    }

    // Authentication
    fun saveAuthToken(token: String) {
        preferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }

    fun getAuthToken(): String? {
        return preferences.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearAuthToken() {
        preferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .apply()
    }

    fun saveCompanySchema(schema: String) {
        preferences.edit()
            .putString(KEY_COMPANY_SCHEMA, schema)
            .apply()
    }

    // UI Mode
    fun saveUiMode(mode: UiMode) {
        preferences.edit()
            .putString(KEY_UI_MODE, mode.name)
            .apply()
    }

    fun getUiMode(): UiMode {
        val modeName = preferences.getString(KEY_UI_MODE, UiMode.CASHIER.name)
        return try {
            UiMode.valueOf(modeName ?: UiMode.CASHIER.name)
        } catch (e: IllegalArgumentException) {
            UiMode.CASHIER
        }
    }

    // Device registration status
    fun isDeviceRegistered(): Boolean {
        return preferences.getString(KEY_DEVICE_ID, null) != null
    }

    fun clearAll() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "rfm_quickpos_secure_store"

        // Keys
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DEVICE_ALIAS = "device_alias"
        private const val KEY_SERIAL_NUMBER = "serial_number"
        private const val KEY_BRANCH_ID = "branch_id"
        private const val KEY_COMPANY_ID = "company_id"
        private const val KEY_COMPANY_SCHEMA = "company_schema"
        private const val KEY_TABLE_ID = "table_id"
        private const val KEY_IS_ACTIVE = "is_active"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_UI_MODE = "ui_mode"
    }
}