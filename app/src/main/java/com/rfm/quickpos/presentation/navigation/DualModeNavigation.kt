package com.rfm.quickpos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rfm.quickpos.domain.model.UiMode
import com.rfm.quickpos.presentation.features.kiosk.AttractScreen
import com.rfm.quickpos.presentation.features.kiosk.KioskCatalogScreen
import com.rfm.quickpos.presentation.features.kiosk.KioskCartScreen
import com.rfm.quickpos.presentation.features.kiosk.KioskPaymentScreen
import com.rfm.quickpos.presentation.features.kiosk.KioskPaymentSuccessScreen

/**
 * Main navigation component that handles both Cashier and Kiosk modes
 */
@Composable
fun DualModeNavigation(
    uiMode: UiMode,
    navController: NavHostController = rememberNavController()
) {
    when (uiMode) {
        UiMode.CASHIER -> {
            // Use the NavHost and cashierScreens directly since CashierNavGraph is not available
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route
            ) {
                // Add cashier mode screens
                cashierScreens(navController)
            }
        }
        UiMode.KIOSK -> KioskNavGraph(navController)
    }
}

/**
 * Navigation graph for the Kiosk mode
 */
@Composable
fun KioskNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = KioskScreen.Attract.route
    ) {
        // Attract Screen (Splash/Welcome)
        composable(KioskScreen.Attract.route) {
            AttractScreen(
                onStartOrderClick = {
                    navController.navigate(KioskScreen.Catalog.route)
                }
            )
        }

        // Catalog Screen
        composable(KioskScreen.Catalog.route) {
            KioskCatalogScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCartClick = {
                    navController.navigate(KioskScreen.Cart.route)
                },
                onProductClick = { /* Handle product selection */ }
            )
        }

        // Cart Screen
        composable(KioskScreen.Cart.route) {
            KioskCartScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCheckoutClick = {
                    navController.navigate(KioskScreen.Payment.route)
                }
            )
        }

        // Payment Screen
        composable(KioskScreen.Payment.route) {
            KioskPaymentScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPaymentComplete = {
                    navController.navigate(KioskScreen.PaymentSuccess.route) {
                        // Clear back stack up to attract screen
                        popUpTo(KioskScreen.Attract.route) {
                            inclusive = false
                        }
                    }
                }
            )
        }

        // Payment Success Screen
        composable(KioskScreen.PaymentSuccess.route) {
            KioskPaymentSuccessScreen(
                onFinishClick = {
                    // Return to attract screen after transaction
                    navController.navigate(KioskScreen.Attract.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}