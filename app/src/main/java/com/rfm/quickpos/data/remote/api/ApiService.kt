// app/src/main/java/com/rfm/quickpos/data/remote/api/ApiService.kt

package com.rfm.quickpos.data.remote.api

import com.rfm.quickpos.data.remote.models.CategoryResponse
import com.rfm.quickpos.data.remote.models.CompanyInfoResponse
import com.rfm.quickpos.data.remote.models.DeviceAuthRequest
import com.rfm.quickpos.data.remote.models.DeviceAuthResponse
import com.rfm.quickpos.data.remote.models.DeviceRegistrationRequest
import com.rfm.quickpos.data.remote.models.DeviceRegistrationResponse
import com.rfm.quickpos.data.remote.models.ItemResponse
import com.rfm.quickpos.data.remote.models.ModifierGroupResponse
import com.rfm.quickpos.data.remote.models.UserAuthRequest
import com.rfm.quickpos.data.remote.models.UserAuthResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    /**
     * Register a new device with the server
     */
    @POST("api/auth/device/register")
    suspend fun registerDevice(@Body request: DeviceRegistrationRequest): DeviceRegistrationResponse

    /**
     * Authenticate a previously registered device
     */
    @POST("api/auth/device/authenticate")
    suspend fun authenticateDevice(@Body request: DeviceAuthRequest): DeviceAuthResponse

    /**
     * Authenticate a user via PIN login
     */
    @POST("api/auth/login")
    suspend fun loginUser(@Body request: UserAuthRequest): UserAuthResponse

    /**
     * Get company information and business type configuration
     */
    @GET("api/companies/info")
    suspend fun getCompanyInfo(): CompanyInfoResponse

    /**
     * Get categories
     */
    @GET("api/catalog/categories")
    suspend fun getCategories(): CategoryResponse

    /**
     * FIXED: Get items with variations and modifiers embedded
     * The backend should return items with variations and modifier_groups included
     */
    @GET("api/catalog/items")
    suspend fun getItems(
        @Query("category_id") categoryId: String? = null,
        @Query("include_variations") includeVariations: Boolean = true,
        @Query("include_modifiers") includeModifiers: Boolean = true
    ): ItemResponse

    /**
     * Get modifier groups (legacy endpoint for backward compatibility)
     */
    @GET("api/catalog/modifier-groups")
    suspend fun getModifierGroups(): ModifierGroupResponse
}