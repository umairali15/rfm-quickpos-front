// app/src/main/java/com/rfm/quickpos/data/remote/models/AuthModels.kt

package com.rfm.quickpos.data.remote.models

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