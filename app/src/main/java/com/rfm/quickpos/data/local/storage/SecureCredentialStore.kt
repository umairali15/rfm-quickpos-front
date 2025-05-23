// app/src/main/java/com/rfm/quickpos/data/local/storage/SecureCredentialStore.kt

package com.rfm.quickpos.data.local.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.rfm.quickpos.data.remote.models.DeviceData
import com.rfm.quickpos.data.remote.models.JwtPayload
import com.rfm.quickpos.data.remote.models.UserData
import com.rfm.quickpos.domain.model.UiMode

private const val TAG = "SecureCredentialStore"

/**
 * Secure store for credentials and device information
 * FIXED to properly handle all device data fields
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

    private val gson = Gson()

    /**
     * FIXED: Save device info with proper field mapping
     */
    fun saveDeviceInfo(deviceData: DeviceData) {
        Log.d(TAG, "Saving device info: $deviceData")

        val editor = preferences.edit()

        // Save essential device information
        editor.putString(KEY_DEVICE_ID, deviceData.id)
        editor.putString(KEY_DEVICE_ALIAS, deviceData.alias)

        // FIXED: Save all available device information with null checks
        deviceData.branchId?.let { editor.putString(KEY_BRANCH_ID, it) }
        deviceData.companyId?.let { editor.putString(KEY_COMPANY_ID, it) }
        deviceData.companySchema?.let { editor.putString(KEY_COMPANY_SCHEMA, it) }
        deviceData.tableId?.let { editor.putString(KEY_TABLE_ID, it) }
        deviceData.serialNumber?.let { editor.putString(KEY_SERIAL_NUMBER, it) }
        deviceData.model?.let { editor.putString(KEY_DEVICE_MODEL, it) }
        deviceData.appVersion?.let { editor.putString(KEY_APP_VERSION, it) }
        deviceData.appMode?.let { editor.putString(KEY_DEVICE_APP_MODE, it) }
        deviceData.lastSync?.let { editor.putString(KEY_LAST_SYNC, it) }

        editor.putBoolean(KEY_IS_ACTIVE, deviceData.isActive)
        editor.putBoolean(KEY_IS_DASHBOARD_DEVICE, deviceData.isDashboardDevice ?: false)

        // FIXED: Save branch information as JSON if available
        deviceData.branch?.let { branch ->
            val branchJson = gson.toJson(branch)
            editor.putString(KEY_BRANCH_DATA, branchJson)

            // Also save branch company ID if available
            branch.companyId?.let { companyId ->
                editor.putString(KEY_COMPANY_ID, companyId)
            }
        }

        // Apply changes
        editor.apply()

        Log.d(TAG, "Device info saved successfully")
    }

    /**
     * FIXED: Get complete device data
     */
    fun getDeviceData(): DeviceData? {
        val deviceId = preferences.getString(KEY_DEVICE_ID, null) ?: return null
        val deviceAlias = preferences.getString(KEY_DEVICE_ALIAS, null) ?: return null

        // Get branch data if available
        val branchJson = preferences.getString(KEY_BRANCH_DATA, null)
        val branchData = if (branchJson != null) {
            try {
                gson.fromJson(branchJson, com.rfm.quickpos.data.remote.models.BranchData::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing branch data", e)
                null
            }
        } else null

        return DeviceData(
            id = deviceId,
            alias = deviceAlias,
            branchId = preferences.getString(KEY_BRANCH_ID, null),
            companyId = preferences.getString(KEY_COMPANY_ID, null),
            companySchema = preferences.getString(KEY_COMPANY_SCHEMA, null),
            tableId = preferences.getString(KEY_TABLE_ID, null),
            isActive = preferences.getBoolean(KEY_IS_ACTIVE, true),
            serialNumber = preferences.getString(KEY_SERIAL_NUMBER, null),
            model = preferences.getString(KEY_DEVICE_MODEL, null),
            appVersion = preferences.getString(KEY_APP_VERSION, null),
            appMode = preferences.getString(KEY_DEVICE_APP_MODE, null),
            isDashboardDevice = preferences.getBoolean(KEY_IS_DASHBOARD_DEVICE, false),
            lastSync = preferences.getString(KEY_LAST_SYNC, null),
            branch = branchData
        )
    }

    // Add a new method to save app mode directly
    fun saveAppMode(appMode: String?) {
        if (appMode != null) {
            try {
                val uiMode = UiMode.valueOf(appMode.uppercase())
                Log.d(TAG, "Saving app mode from server: $appMode -> $uiMode")
                saveUiMode(uiMode)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid appMode value from server: $appMode", e)
                // Default to CASHIER if invalid
                saveUiMode(UiMode.CASHIER)
            }
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

    // NEW METHODS FOR JWT DATA
    fun getCompanyId(): String? {
        return preferences.getString(KEY_COMPANY_ID, null)
    }

    fun getBusinessType(): String? {
        return preferences.getString(KEY_BUSINESS_TYPE, null)
    }

    // FIXED: Enhanced auth token handling with better JWT parsing
    fun saveAuthToken(token: String?) {
        if (token == null) {
            Log.w(TAG, "Attempted to save null auth token, ignoring")
            return
        }

        preferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
        Log.d(TAG, "Auth token saved")

        // Try to decode JWT payload and save essential information
        try {
            decodeAndSaveJwtInfo(token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode JWT token", e)
        }
    }

    /**
     * FIXED: Enhanced JWT decoding with better error handling
     */
    private fun decodeAndSaveJwtInfo(token: String) {
        try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e(TAG, "Invalid JWT token format")
                return
            }

            // Base64 decode the payload
            val payload = parts[1]
            // Add padding if necessary
            val paddedPayload = payload + "=".repeat((4 - payload.length % 4) % 4)
            val decodedBytes = android.util.Base64.decode(
                paddedPayload.replace("-", "+").replace("_", "/"),
                android.util.Base64.DEFAULT
            )
            val decodedJson = String(decodedBytes)

            Log.d(TAG, "Decoded JWT payload: $decodedJson")

            // Parse payload JSON
            val jwtPayload = gson.fromJson(decodedJson, JwtPayload::class.java)

            // Save important information
            val editor = preferences.edit()
            jwtPayload.schemaName?.let { editor.putString(KEY_COMPANY_SCHEMA, it) }
            jwtPayload.companyId?.let { editor.putString(KEY_COMPANY_ID, it) }
            jwtPayload.businessType?.let { editor.putString(KEY_BUSINESS_TYPE, it) }
            editor.apply()

            Log.d(TAG, "JWT info decoded and saved: schema=${jwtPayload.schemaName}, " +
                    "companyId=${jwtPayload.companyId}, businessType=${jwtPayload.businessType}")

        } catch (e: Exception) {
            Log.e(TAG, "Error decoding JWT token", e)
        }
    }

    fun getAuthToken(): String? {
        return preferences.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearAuthToken() {
        preferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .apply()
        Log.d(TAG, "Auth token cleared")
    }

    fun saveCompanySchema(schema: String?) {
        if (schema == null) {
            Log.w(TAG, "Attempted to save null company schema")
            return
        }

        preferences.edit()
            .putString(KEY_COMPANY_SCHEMA, schema)
            .apply()
        Log.d(TAG, "Company schema saved: $schema")
    }

    // User Data
    fun saveUserData(userData: UserData) {
        val userJson = gson.toJson(userData)
        preferences.edit()
            .putString(KEY_USER_DATA, userJson)
            .apply()
        Log.d(TAG, "User data saved: ${userData.id}")
    }

    fun getUserData(): UserData? {
        val userJson = preferences.getString(KEY_USER_DATA, null) ?: return null
        return try {
            gson.fromJson(userJson, UserData::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing user data", e)
            null
        }
    }

    fun clearUserData() {
        preferences.edit()
            .remove(KEY_USER_DATA)
            .apply()
        Log.d(TAG, "User data cleared")
    }

    // UI Mode
    fun saveUiMode(mode: UiMode) {
        preferences.edit()
            .putString(KEY_UI_MODE, mode.name)
            .apply()
        Log.d(TAG, "UI mode saved: $mode")
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
        Log.d(TAG, "All credentials cleared")
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
        private const val KEY_UI_MODE = "app_mode"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_BUSINESS_TYPE = "business_type"

        // FIXED: Add new keys for enhanced device data
        private const val KEY_DEVICE_MODEL = "device_model"
        private const val KEY_APP_VERSION = "app_version"
        private const val KEY_DEVICE_APP_MODE = "device_app_mode"
        private const val KEY_IS_DASHBOARD_DEVICE = "is_dashboard_device"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_BRANCH_DATA = "branch_data"
    }
}