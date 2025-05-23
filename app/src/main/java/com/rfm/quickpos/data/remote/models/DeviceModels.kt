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
 * Response from device authentication - FIXED to match backend structure
 */
data class DeviceAuthResponse(
    val success: Boolean,
    val token: String? = null,
    // FIX: appMode is at the root level, not in the device object
    @SerializedName("appMode") val appMode: String? = null,
    val device: DeviceData
)

/**
 * Device data returned from API - FIXED field mappings to match backend
 */
data class DeviceData(
    val id: String,
    val alias: String,

    // FIX: Map all the actual fields from backend using proper serialization names
    @SerializedName("branch_id") val branchId: String? = null,
    @SerializedName("company_id") val companyId: String? = null,
    @SerializedName("company_schema") val companySchema: String? = null,
    @SerializedName("table_id") val tableId: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("serial_number") val serialNumber: String? = null,
    val model: String? = null,
    @SerializedName("app_version") val appVersion: String? = null,
    @SerializedName("is_dashboard_device") val isDashboardDevice: Boolean = false,
    @SerializedName("last_sync") val lastSync: String? = null,
    val settings: Map<String, Any>? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,

    // FIX: Handle both app_mode and appMode from different response contexts
    @SerializedName(value = "app_mode", alternate = ["appMode"])
    val appMode: String? = null,

    // Branch information (from backend logs shows this is included)
    val branch: BranchData? = null
)

/**
 * Branch data structure - NEW to handle branch information from device responses
 */
data class BranchData(
    val id: String,
    val name: String,
    val address: String? = null,
    val phone: String? = null,
    @SerializedName("manager_id") val managerId: String? = null,
    val timezone: String? = null,
    val active: Boolean = true,
    val settings: BranchSettings? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    val companyId: String? = null
)

/**
 * Branch settings structure - NEW to handle branch settings
 */
data class BranchSettings(
    val hasDelivery: Boolean = false,
    val hasDriveThru: Boolean = false,
    val parkingSpaces: Int? = null,
    val hasTableService: Boolean = false,
    val seatingCapacity: Int? = null
)