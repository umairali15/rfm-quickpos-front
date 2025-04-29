package com.rfm.quickpos.presentation.navigation

/**
 * Screen routes for navigation in RFM QuickPOS
 */
sealed class Screen(val route: String) {
    // We only need Home for our simplified app
    object Home : Screen("home")
}