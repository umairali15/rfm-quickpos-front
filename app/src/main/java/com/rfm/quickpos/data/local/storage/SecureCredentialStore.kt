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

    // In SecureCredentialStore.kt
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
     * Decode JWT token and save essential information
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
            val decodedBytes = android.util.Base64.decode(
                payload.replace("-", "+").replace("_", "/"),
                android.util.Base64.DEFAULT
            )
            val decodedJson = String(decodedBytes)

            // Parse payload JSON
            val jwtPayload = gson.fromJson(decodedJson, JwtPayload::class.java)

            // Save important information
            val editor = preferences.edit()
            editor.putString(KEY_COMPANY_SCHEMA, jwtPayload.schemaName)
            editor.putString(KEY_COMPANY_ID, jwtPayload.companyId)
            editor.putString(KEY_BUSINESS_TYPE, jwtPayload.businessType)
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
    }
}