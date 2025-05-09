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
    val data: DeviceData? = null,
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
    val device: DeviceData
)

/**
 * Device data returned from API
 */
data class DeviceData(
    val id: String,
    val alias: String,
    @SerializedName("branch_id") val branchId: String?,
    @SerializedName("company_id") val companyId: String,
    @SerializedName("company_schema") val companySchema: String,
    @SerializedName("table_id") val tableId: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("ui_mode") val uiMode: String?
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
    val user: UserData,
    @SerializedName("company_schema") val companySchema: String
)

/**
 * User data returned from API
 */
data class UserData(
    val id: String,
    val fullName: String,
    val email: String?,
    val role: String,
    val branches: List<String>? = null
)