package com.rfm.quickpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.features.catalog.CatalogScreen
import com.rfm.quickpos.presentation.features.catalog.Product
import com.rfm.quickpos.presentation.features.catalog.ProductCategory
import com.rfm.quickpos.presentation.features.checkout.CartItem
import com.rfm.quickpos.presentation.features.checkout.CartSummary
import com.rfm.quickpos.presentation.features.checkout.CheckoutScreen
import com.rfm.quickpos.presentation.features.home.DashboardMetrics
import com.rfm.quickpos.presentation.features.home.DashboardScreen
import com.rfm.quickpos.presentation.features.home.PaymentMethodsData
import com.rfm.quickpos.presentation.features.payment.PaymentMethodDialog
import com.rfm.quickpos.presentation.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Apply our custom theme to the entire application
            RFMQuickPOSTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                // Sample data for previews - in a real app, this would come from a ViewModel
                val sampleDashboardMetrics = DashboardMetrics(
                    totalSales = "2,148M",
                    salesAmount = "16.94K",
                    customers = "126.8K",
                    dateRange = "01.01.2024 - 01.01.2025",
                    paymentMethodChart = PaymentMethodsData(
                        cashPercentage = 35f,
                        cardPercentage = 65f
                    )
                )

                val sampleCategories = listOf(
                    ProductCategory("cat1", "Food", 15),
                    ProductCategory("cat2", "Beverages", 12),
                    ProductCategory("cat3", "General", 8)
                )

                val sampleProducts = listOf(
                    Product(
                        id = "p1",
                        name = "Coffee",
                        price = 15.0,
                        categoryId = "cat2"
                    ),
                    Product(
                        id = "p2",
                        name = "Croissant",
                        price = 10.0,
                        categoryId = "cat1"
                    ),
                    Product(
                        id = "p3",
                        name = "Water Bottle",
                        price = 5.0,
                        categoryId = "cat2"
                    )
                )

                val sampleCartItems = listOf(
                    CartItem(
                        id = "1",
                        name = "\"Paris\" set",
                        price = 12.0,
                        quantity = 12.0,
                        unitLabel = "x 1 pc"
                    ),
                    CartItem(
                        id = "2",
                        name = "Liquid soap (pieces)",
                        price = 220.0,
                        quantity = 16.0,
                        unitLabel = "x 1 pc",
                        discountAmount = 100.0
                    ),
                    CartItem(
                        id = "3",
                        name = "Nivea Men shaving foam with skin restoration effect",
                        price = 3.591,
                        quantity = 3.0,
                        unitLabel = "x 10 pc",
                        discountPercent = 5.0
                    )
                )

                val sampleCartSummary = CartSummary(
                    subtotal = 4087.90,
                    taxAmount = 12.0,
                    discountAmount = 289.0,
                    total = 4087.90
                )

                var showDemoMessage by remember { mutableStateOf(true) }

                // Display a welcome message the first time
                LaunchedEffect(true) {
                    if (showDemoMessage) {
                        snackbarHostState.showSnackbar("Welcome to RFM QuickPOS Demo")
                        showDemoMessage = false
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route
                        ) {
                            // Home/Dashboard Screen
                            composable(Screen.Home.route) {
                                DashboardScreen(
                                    metrics = sampleDashboardMetrics,
                                    onNewSaleClicked = {
                                        navController.navigate(Screen.Catalog.route)
                                    },
                                    onReportsClicked = {
                                        // Navigate to reports screen (not implemented in demo)
                                    },
                                    onOrdersClicked = {
                                        // Navigate to orders screen (not implemented in demo)
                                    },
                                    onCatalogClicked = {
                                        navController.navigate(Screen.Catalog.route)
                                    },
                                    onCustomersClicked = {
                                        // Navigate to customers screen (not implemented in demo)
                                    },
                                    onSettingsClicked = {
                                        // Navigate to settings screen (not implemented in demo)
                                    },
                                    userName = "Demo User"
                                )
                            }

                            // Product Catalog Screen
                            composable(Screen.Catalog.route) {
                                CatalogScreen(
                                    categories = sampleCategories,
                                    products = sampleProducts,
                                    onProductClicked = { product ->
                                        // In a real app, add to cart and show details
                                        Timber.d("Product clicked: ${product.name}")
                                    },
                                    onCartClicked = {
                                        navController.navigate(Screen.Checkout.route)
                                    },
                                    onBackClicked = {
                                        navController.navigateUp()
                                    },
                                    cartItemCount = sampleCartItems.size
                                )
                            }

                            // Checkout Screen
                            composable(Screen.Checkout.route) {
                                var showPaymentDialog by remember { mutableStateOf(false) }

                                CheckoutScreen(
                                    cartItems = sampleCartItems,
                                    cartSummary = sampleCartSummary,
                                    onPayClicked = {
                                        showPaymentDialog = true
                                    },
                                    onBackClicked = {
                                        navController.navigateUp()
                                    },
                                    discountAppliedMessage = "Item discount added"
                                )

                                if (showPaymentDialog) {
                                    PaymentMethodDialog(
                                        amount = sampleCartSummary.total,
                                        onPaymentMethodSelected = { paymentMethod ->
                                            showPaymentDialog = false
                                            // Process payment (not implemented in demo)
                                        },
                                        onDismiss = {
                                            showPaymentDialog = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}