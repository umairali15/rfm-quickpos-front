// app/src/main/java/com/rfm/quickpos/presentation/navigation/Screen.kt

package com.rfm.quickpos.presentation.navigation

import com.rfm.quickpos.presentation.features.error.ErrorType

/**
 * Define all navigation routes in the Cashier mode
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Catalog : Screen("catalog")
    object Cart : Screen("cart")
    object Payment : Screen("payment")
    object PaymentSuccess : Screen("payment_success")
    object SalesHistory : Screen("sales_history")

    // Setup screens
    object DevicePairing : Screen("device_pairing")

    // Utility screens
    object Splash : Screen("splash")
    object Error : Screen("error/{errorType}") {
        fun createRoute(errorType: ErrorType) = "error/${errorType.name}"
    }

    // Shift management
    object OpenShift : Screen("open_shift")
    object CashMovement : Screen("cash_movement")
    object CloseShift : Screen("close_shift")
    object ShiftSummary : Screen("shift_summary")

    // Catalog detail
    object ItemDetail : Screen("item_detail/{productId}") {
        fun createRoute(productId: String) = "item_detail/$productId"
    }
}

/**
 * Screen routes for Kiosk mode
 */
sealed class KioskScreen(val route: String) {
    object Attract : KioskScreen("kiosk_attract")
    object Catalog : KioskScreen("kiosk_catalog")
    object Cart : KioskScreen("kiosk_cart")
    object Payment : KioskScreen("kiosk_payment")
    object PaymentSuccess : KioskScreen("kiosk_payment_success")
}

// REMOVED: AuthScreen duplicate declaration