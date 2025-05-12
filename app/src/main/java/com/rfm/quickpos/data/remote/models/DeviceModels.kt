// app/src/main/java/com/rfm/quickpos/data/remote/models/DeviceModels.kt

package com.rfm.quickpos.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Request to register a new device
 */
data class DeviceRegistrationRequest(
    val alias: String,
    val serialNumber: String,
    val model: String,
    val branchId: String,
    val appVersion: String
)

/**
 * Response from device registration
 */
data class DeviceRegistrationResponse(
    val success: Boolean,
    val message: String? = null,
    val device: DeviceData? = null,
    val token: String? = null,
    val error: String? = null
)

/**
 * Request to authenticate a registered device
 */
data class DeviceAuthRequest(
    val deviceId: String,
    val serialNumber: String
)

/**
 * Response from device authentication
 */
data class DeviceAuthResponse(
    val success: Boolean,
    val token: String,
    // FIX: appMode is at the root level, not in the device object
    @SerializedName("appMode") val appMode: String? = null,
    val device: DeviceData
)

/**
 * Device data returned from API
 */
data class DeviceData(
    val id: String,
    val alias: String,
    @SerializedName("branch_id") val branchId: String? = null,
    @SerializedName("company_id") val companyId: String? = null,
    @SerializedName("company_schema") val companySchema: String? = null,
    @SerializedName("table_id") val tableId: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,

    // Fix: Use multiple alternate names to catch all possible field variants
    @SerializedName(value = "appMode", alternate = ["app_mode"])
    val appMode: String? = null,

    // Additional fields
    @SerializedName("serial_number") val serialNumber: String? = null,
    val model: String? = null,
    @SerializedName("app_version") val appVersion: String? = null
)

/**
 * Request to authenticate a user via PIN
 */
data class UserAuthRequest(
    val email: String? = null,
    val pin: String
)

/**
 * Response from user authentication
 */
data class UserAuthResponse(
    val success: Boolean,
    val token: String,
    val user: UserData
)

/**
 * User data returned from API
 */
data class UserData(
    val id: String,
    val fullName: String,
    val email: String?,
    val role: String,
    val companyId: String? = null,
    val branches: List<String>? = null
)

/**
 * JWT token payload structure
 */
data class JwtPayload(
    val sub: String,  // User ID
    val email: String,
    val name: String,
    val role: String,
    val companyId: String,
    val schemaName: String,
    val businessType: String,
    val branches: List<String>,
    val iat: Long,  // Issued at timestamp
    val exp: Long   // Expiration timestamp
)