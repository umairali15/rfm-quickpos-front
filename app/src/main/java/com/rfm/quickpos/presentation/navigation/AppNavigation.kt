package com.rfm.quickpos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rfm.quickpos.presentation.features.auth.LoginScreen
import com.rfm.quickpos.presentation.features.auth.PinLoginScreen
import com.rfm.quickpos.presentation.features.catalog.CatalogScreen
import com.rfm.quickpos.presentation.features.catalog.CatalogState
import com.rfm.quickpos.presentation.features.catalog.Product
import com.rfm.quickpos.presentation.features.catalog.ProductCategory
import com.rfm.quickpos.presentation.features.home.DashboardMetrics
import com.rfm.quickpos.presentation.features.home.DashboardScreen
import com.rfm.quickpos.presentation.features.home.PaymentMethodsData

/**
 * Define all navigation routes in the app
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object PinLogin : Screen("pin_login")
    object Dashboard : Screen("dashboard")
    object Catalog : Screen("catalog")

    // Add more screens here as they are created
    // object Cart : Screen("cart")
    // object Settings : Screen("settings")
}

/**
 * Main navigation component for the app
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Email/Password Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    // In a real app, validate credentials here
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        navController.navigate(Screen.Dashboard.route) {
                            // Clear back stack so user can't go back to login
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onForgotPasswordClick = {
                    // Handle forgot password
                },
                onSwitchToPinLogin = {
                    navController.navigate(Screen.PinLogin.route)
                },
                errorMessage = null
            )
        }

        // PIN Login
        composable(Screen.PinLogin.route) {
            PinLoginScreen(
                onPinSubmit = { pin ->
                    // In a real app, validate PIN here
                    if (pin == "1234") {
                        navController.navigate(Screen.Dashboard.route) {
                            // Clear back stack so user can't go back to login
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onBackToEmailLogin = {
                    navController.navigate(Screen.Login.route) {
                        // Clear back stack
                        popUpTo(Screen.PinLogin.route) { inclusive = true }
                    }
                },
                userName = "Cashier"
            )
        }

        // Dashboard
        composable(Screen.Dashboard.route) {
            // Sample metrics data - in a real app, this would come from a ViewModel
            val sampleMetrics = DashboardMetrics(
                totalSales = "2,148",
                salesAmount = "16.94",
                customers = "126.8",
                dateRange = "01.01.2024 - 01.01.2025",
                paymentMethodChart = PaymentMethodsData(
                    cashPercentage = 35f,
                    cardPercentage = 65f
                )
            )

            DashboardScreen(
                metrics = sampleMetrics,
                onNewSaleClicked = {
                    // Handle new sale
                },
                onReportsClicked = {
                    // Navigate to reports
                },
                onOrdersClicked = {
                    // Navigate to orders
                },
                onCatalogClicked = {
                    navController.navigate(Screen.Catalog.route)
                },
                onCustomersClicked = {
                    // Navigate to customers
                },
                onSettingsClicked = {
                    // In a real app, navigate to settings
                    // For demo, go back to login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                userName = "Demo"
            )
        }

        // Catalog
        composable(Screen.Catalog.route) {
            // Sample data - in a real app, this would come from a ViewModel
            val categories = listOf(
                ProductCategory("1", "Beverages"),
                ProductCategory("2", "Food"),
                ProductCategory("3", "General")
            )

            val products = listOf(
                Product(
                    id = "1",
                    name = "Coffee",
                    price = 15.00,
                    categoryId = "1",
                    barcode = "5901234123457"
                ),
                Product(
                    id = "2",
                    name = "Croissant",
                    price = 10.00,
                    categoryId = "2",
                    barcode = "4003994155486",
                    discountPercentage = 10
                ),
                Product(
                    id = "3",
                    name = "Water Bottle",
                    price = 5.00,
                    categoryId = "1",
                    barcode = "7622210146083"
                ),
                Product(
                    id = "4",
                    name = "Sandwich",
                    price = 20.00,
                    categoryId = "2",
                    barcode = "1234567890123"
                )
            )

            val catalogState = CatalogState(
                isLoading = false,
                categories = categories,
                products = products,
                selectedCategoryId = null,
                cartItemCount = 2
            )

            CatalogScreen(
                state = catalogState,
                onBackClick = {
                    navController.popBackStack()
                },
                onSearchQueryChange = {
                    // Update search query
                },
                onSearchSubmit = {
                    // Perform search
                },
                onCategorySelected = {
                    // Filter by category
                },
                onProductClick = {
                    // Show product details
                },
                onAddToCart = {
                    // Add to cart
                },
                onScanBarcode = {
                    // Open barcode scanner
                },
                onCartClick = {
                    // Navigate to cart
                },
                onAddCustomItem = {
                    // Add custom item
                }
            )
        }
    }
}