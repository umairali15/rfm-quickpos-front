package com.rfm.quickpos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rfm.quickpos.domain.model.UiMode
import com.rfm.quickpos.presentation.features.auth.DualModePinLoginScreen
import com.rfm.quickpos.presentation.features.auth.LoginScreen
import com.rfm.quickpos.presentation.features.kiosk.AttractScreen
import com.rfm.quickpos.presentation.features.kiosk.KioskCatalogScreen
import com.rfm.quickpos.presentation.features.kiosk.KioskCartScreen
import com.rfm.quickpos.presentation.features.kiosk.KioskInactivityDetector
import com.rfm.quickpos.presentation.features.kiosk.KioskPaymentScreen
import com.rfm.quickpos.presentation.features.kiosk.KioskPaymentSuccessScreen

/**
 * Combined navigation system that supports both Cashier and Kiosk modes
 */
@Composable
fun AppNavigationWithDualMode(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AuthScreen.PinLogin.route,
    startingMode: UiMode = UiMode.CASHIER
) {
    // Track current UI mode
    var currentMode by remember { mutableStateOf(startingMode) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(AuthScreen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    // Handle email login and navigate to appropriate screen
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        // For simplicity, always go to cashier mode in email login
                        currentMode = UiMode.CASHIER

                        // Navigate to appropriate home screen
                        navigateToHomeScreen(navController, currentMode)
                    }
                },
                onForgotPasswordClick = {
                    // Handle forgot password
                },
                onSwitchToPinLogin = {
                    navController.navigate(AuthScreen.PinLogin.route)
                }
            )
        }

        composable(AuthScreen.PinLogin.route) {
            DualModePinLoginScreen(
                onPinSubmit = { pin, mode ->
                    // Set current mode based on PIN
                    currentMode = mode

                    // Navigate to appropriate home screen
                    navigateToHomeScreen(navController, currentMode)
                },
                onBackToEmailLogin = {
                    navController.navigate(AuthScreen.Login.route) {
                        popUpTo(AuthScreen.PinLogin.route) { inclusive = true }
                    }
                }
            )
        }

        // Add cashier mode screens
        cashierScreens(navController)

        // Kiosk mode navigation
        composable(KioskScreen.Attract.route) {
            // Only accessible in kiosk mode
            if (currentMode == UiMode.KIOSK) {
                AttractScreen(
                    onStartOrderClick = {
                        navController.navigate(KioskScreen.Catalog.route)
                    }
                )
            } else {
                // Redirect to cashier home if somehow reached in cashier mode
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        // Kiosk catalog screen with inactivity detection
        composable(KioskScreen.Catalog.route) {
            KioskInactivityDetector(
                onTimeout = {
                    navController.navigate(KioskScreen.Attract.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) { inactivityModifier ->
                KioskCatalogScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onCartClick = {
                        navController.navigate(KioskScreen.Cart.route)
                    },
                    onProductClick = { /* Handle product selection */ },
                    modifier = inactivityModifier
                )
            }
        }

        // Kiosk cart screen with inactivity detection
        composable(KioskScreen.Cart.route) {
            KioskInactivityDetector(
                onTimeout = {
                    navController.navigate(KioskScreen.Attract.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) { inactivityModifier ->
                KioskCartScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onCheckoutClick = {
                        navController.navigate(KioskScreen.Payment.route)
                    },
                    modifier = inactivityModifier
                )
            }
        }

        // Kiosk payment screen with inactivity detection
        composable(KioskScreen.Payment.route) {
            KioskInactivityDetector(
                onTimeout = {
                    navController.navigate(KioskScreen.Attract.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                timeoutMillis = 180_000 // 3 minutes for payment
            ) { inactivityModifier ->
                KioskPaymentScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onPaymentComplete = {
                        navController.navigate(KioskScreen.PaymentSuccess.route) {
                            popUpTo(KioskScreen.Attract.route) {
                                inclusive = false
                            }
                        }
                    },
                    modifier = inactivityModifier
                )
            }
        }

        // Kiosk payment success screen
        composable(KioskScreen.PaymentSuccess.route) {
            KioskPaymentSuccessScreen(
                onFinishClick = {
                    navController.navigate(KioskScreen.Attract.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

/**
 * Navigate to the appropriate home screen based on current UI mode
 */
private fun navigateToHomeScreen(navController: NavHostController, mode: UiMode) {
    when (mode) {
        UiMode.CASHIER -> {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
        }
        UiMode.KIOSK -> {
            navController.navigate(KioskScreen.Attract.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}

/**
 * Auth screen routes
 */
sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("auth_login")
    object PinLogin : AuthScreen("auth_pin_login")
}