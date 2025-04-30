// app/src/main/java/com/rfm/quickpos/presentation/navigation/AuthScreen.kt

package com.rfm.quickpos.presentation.navigation

/**
 * Authentication screen routes
 * Used in both Cashier and Kiosk modes
 */
sealed class AuthScreen(val route: String) {
    /**
     * Email/password login screen
     * This is typically used as a fallback or for administrative access
     */
    object Login : AuthScreen("auth_login")

    /**
     * PIN-based login screen
     * This is the primary login method for both Cashier and Kiosk modes
     * and determines which mode to enter based on the PIN entered
     */
    object PinLogin : AuthScreen("auth_pin_login")

    /**
     * Password reset screen (future implementation)
     */
    object PasswordReset : AuthScreen("auth_password_reset")

    /**
     * Device registration/pairing screen
     * Used during first-time setup
     */
    object DevicePairing : AuthScreen("auth_device_pairing")
}