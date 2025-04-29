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
import com.rfm.quickpos.presentation.features.auth.LoginScreen
import com.rfm.quickpos.presentation.features.auth.PinLoginScreen
import com.rfm.quickpos.presentation.features.cart.CartItem
import com.rfm.quickpos.presentation.features.cart.CartScreen
import com.rfm.quickpos.presentation.features.cart.CartState
import com.rfm.quickpos.presentation.features.catalog.CatalogScreen
import com.rfm.quickpos.presentation.features.catalog.CatalogState
import com.rfm.quickpos.presentation.features.catalog.Product
import com.rfm.quickpos.presentation.features.catalog.ProductCategory
import com.rfm.quickpos.presentation.features.history.DateRange
import com.rfm.quickpos.presentation.features.history.SaleHistoryItem
import com.rfm.quickpos.presentation.features.history.SaleStatus
import com.rfm.quickpos.presentation.features.history.SalesHistoryScreen
import com.rfm.quickpos.presentation.features.history.SalesHistoryState
import com.rfm.quickpos.presentation.features.home.DashboardMetrics
import com.rfm.quickpos.presentation.features.home.DashboardScreen
import com.rfm.quickpos.presentation.features.home.PaymentMethodsData
import com.rfm.quickpos.presentation.features.payment.PaymentMethod
import com.rfm.quickpos.presentation.features.payment.PaymentScreen
import com.rfm.quickpos.presentation.features.payment.PaymentState
import com.rfm.quickpos.presentation.features.payment.PaymentSuccessScreen
import com.rfm.quickpos.presentation.features.payment.PaymentSuccessState
import com.rfm.quickpos.presentation.features.payment.SplitPaymentData
import com.rfm.quickpos.presentation.features.sale.AddItemBottomSheet
import java.util.Date

/**
 * Define all navigation routes in the app
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object PinLogin : Screen("pin_login")
    object Dashboard : Screen("dashboard")
    object Catalog : Screen("catalog")
    object Cart : Screen("cart")
    object Payment : Screen("payment")
    object PaymentSuccess : Screen("payment_success")
    object SalesHistory : Screen("sales_history")

    // Add more screens as needed
}

/**
 * Main navigation component for the app using Jetpack Navigation
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    // Sample data setup for preview/demo purposes
    val sampleCategories = listOf(
        ProductCategory("1", "Beverages"),
        ProductCategory("2", "Food"),
        ProductCategory("3", "General")
    )

    val sampleProducts = listOf(
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

    // Dashboard metrics
    val dashboardMetrics = DashboardMetrics(
        totalSales = "2,148",
        salesAmount = "16.94",
        customers = "126.8",
        dateRange = "01.01.2024 - 01.01.2025",
        paymentMethodChart = PaymentMethodsData(
            cashPercentage = 35f,
            cardPercentage = 65f
        )
    )

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Email/Password Login
        composable(Screen.Login.route) {
            var errorMessage by remember { mutableStateOf<String?>(null) }

            LoginScreen(
                onLoginClick = { email, password ->
                    // In a real app, validate credentials here
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        navController.navigate(Screen.Dashboard.route) {
                            // Clear back stack so user can't go back to login
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        errorMessage = null
                    } else {
                        errorMessage = "Please enter both email and password."
                    }
                },
                onForgotPasswordClick = {
                    // Handle forgot password
                },
                onSwitchToPinLogin = {
                    navController.navigate(Screen.PinLogin.route)
                },
                errorMessage = errorMessage
            )
        }

        // PIN Login
        composable(Screen.PinLogin.route) {
            var errorMessage by remember { mutableStateOf<String?>(null) }

            PinLoginScreen(
                onPinSubmit = { pin ->
                    // In a real app, validate PIN here
                    if (pin == "1234") {
                        navController.navigate(Screen.Dashboard.route) {
                            // Clear back stack so user can't go back to login
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        errorMessage = null
                    } else {
                        errorMessage = "Invalid PIN. Please try again."
                    }
                },
                onBackToEmailLogin = {
                    navController.navigate(Screen.Login.route) {
                        // Clear back stack
                        popUpTo(Screen.PinLogin.route) { inclusive = true }
                    }
                },
                userName = "Cashier",
                errorMessage = errorMessage
            )
        }

        // Dashboard
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                metrics = dashboardMetrics,
                onNewSaleClicked = {
                    navController.navigate(Screen.Catalog.route)
                },
                onReportsClicked = {
                    // Navigate to reports - Not implemented yet
                },
                onOrdersClicked = {
                    navController.navigate(Screen.SalesHistory.route)
                },
                onCatalogClicked = {
                    navController.navigate(Screen.Catalog.route)
                },
                onCustomersClicked = {
                    // Navigate to customers - Not implemented yet
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
            var showAddItemSheet by remember { mutableStateOf(false) }

            val catalogState = CatalogState(
                isLoading = false,
                categories = sampleCategories,
                products = sampleProducts,
                selectedCategoryId = null,
                cartItemCount = 2
            )

            if (showAddItemSheet) {
                AddItemBottomSheet(
                    onDismiss = { showAddItemSheet = false },
                    onCatalogItemClick = {
                        showAddItemSheet = false
                    },
                    onScanClick = {
                        showAddItemSheet = false
                        // Open barcode scanner
                    },
                    onNonCatalogItemClick = {
                        showAddItemSheet = false
                        // Show non-catalog item form
                    },
                    onDiscountClick = {
                        showAddItemSheet = false
                        // Show discount form
                    },
                    onCommentClick = {
                        showAddItemSheet = false
                        // Show comment form
                    },
                    onCustomerClick = {
                        showAddItemSheet = false
                        // Show customer selection
                    },
                    currentSaleNumber = "5917-1610-174122"
                )
            }

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
                    // Show bottom sheet
                    showAddItemSheet = true
                },
                onCartClick = {
                    navController.navigate(Screen.Cart.route)
                },
                onAddCustomItem = {
                    showAddItemSheet = true
                }
            )
        }

        // Cart
        composable(Screen.Cart.route) {
            val sampleCartItems = listOf(
                CartItem(
                    id = "1",
                    name = "Coffee",
                    price = 15.00,
                    quantity = 2
                ),
                CartItem(
                    id = "2",
                    name = "Croissant",
                    price = 10.00,
                    quantity = 1,
                    discountPercentage = 10
                ),
                CartItem(
                    id = "3",
                    name = "Water Bottle",
                    price = 5.00,
                    quantity = 3,
                    notes = "Cold"
                )
            )

            val subtotal = sampleCartItems.sumOf { it.price * it.quantity }
            val discount = 5.0
            val tax = subtotal * 0.05
            val total = subtotal - discount + tax

            val cartState = CartState(
                cartItems = sampleCartItems,
                subtotal = subtotal,
                discount = discount,
                tax = tax,
                total = total
            )

            CartScreen(
                state = cartState,
                onBackClick = {
                    navController.popBackStack()
                },
                onCheckoutClick = {
                    navController.navigate(Screen.Payment.route)
                },
                onItemQuantityChange = { _, _ ->
                    // Update item quantity
                },
                onRemoveItem = {
                    // Remove item from cart
                },
                onDiscountCodeChange = {
                    // Update discount code
                },
                onApplyDiscountClick = {
                    // Apply discount
                },
                onNoteChange = {
                    // Update note
                },
                onAddCustomerClick = {
                    // Add customer
                },
                onClearCart = {
                    // Clear cart
                }
            )
        }

        // Payment
        composable(Screen.Payment.route) {
            var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
            var cashReceived by remember { mutableStateOf<Double?>(null) }
            var splitPaymentData by remember { mutableStateOf(SplitPaymentData()) }
            var isProcessing by remember { mutableStateOf(false) }

            val paymentState = PaymentState(
                saleNumber = "5917-1610-174122",
                totalAmount = 800.00,
                selectedPaymentMethod = selectedPaymentMethod,
                cashReceived = cashReceived,
                splitPaymentData = splitPaymentData,
                isProcessing = isProcessing
            )

            PaymentScreen(
                state = paymentState,
                onBackClick = {
                    navController.popBackStack()
                },
                onPaymentMethodSelected = {
                    selectedPaymentMethod = it
                },
                onProcessPayment = {
                    // Process payment (simulate processing)
                    isProcessing = true

                    // Navigate to success screen after delay
                    // In a real app, this would happen after API response
                    android.os.Handler().postDelayed({
                        navController.navigate(Screen.PaymentSuccess.route) {
                            // Clear back stack up to dashboard
                            popUpTo(Screen.Dashboard.route) {
                                inclusive = false
                            }
                        }
                    }, 1500)
                },
                onSplitAmountChange = { cardAmount, cashAmount ->
                    splitPaymentData = SplitPaymentData(cardAmount, cashAmount)
                },
                onCashReceivedChange = {
                    cashReceived = it
                }
            )
        }

        // Payment Success
        composable(Screen.PaymentSuccess.route) {
            var isPrinting by remember { mutableStateOf(false) }

            val successState = PaymentSuccessState(
                receiptNumber = "5917-1610-174122",
                amount = 800.00,
                paymentMethod = PaymentMethod.CARD,
                isPrinting = isPrinting
            )

            PaymentSuccessScreen(
                state = successState,
                onPrintReceiptClick = {
                    // Print receipt
                    isPrinting = true

                    // Reset after "printing"
                    android.os.Handler().postDelayed({
                        isPrinting = false
                    }, 2000)
                },
                onEmailReceiptClick = {
                    // Email receipt
                },
                onNewSaleClick = {
                    navController.navigate(Screen.Catalog.route) {
                        // Clear back stack up to dashboard
                        popUpTo(Screen.Dashboard.route) {
                            inclusive = false
                        }
                    }
                },
                onViewOrdersClick = {
                    navController.navigate(Screen.SalesHistory.route) {
                        // Clear back stack up to dashboard
                        popUpTo(Screen.Dashboard.route) {
                            inclusive = false
                        }
                    }
                }
            )
        }

        // Sales History
        composable(Screen.SalesHistory.route) {
            val currentDate = Date()

            val salesHistory = listOf(
                SaleHistoryItem(
                    id = "1",
                    saleNumber = "0488-1610-173606",
                    dateTime = currentDate,
                    status = SaleStatus.OPEN,
                    amount = 0.0
                ),
                SaleHistoryItem(
                    id = "2",
                    saleNumber = "5704-1610-173513",
                    dateTime = Date(currentDate.time - 3600000), // 1 hour ago
                    status = SaleStatus.NOT_PAID,
                    amount = 2000.0,
                    customerName = "Interior design services 191991"
                )
            )

            val salesHistoryState = SalesHistoryState(
                salesHistory = salesHistory,
                selectedDateRange = DateRange.TODAY
            )

            SalesHistoryScreen(
                state = salesHistoryState,
                onBackClick = {
                    navController.popBackStack()
                },
                onNewSaleClick = {
                    navController.navigate(Screen.Catalog.route) {
                        // Clear back stack up to dashboard
                        popUpTo(Screen.Dashboard.route) {
                            inclusive = false
                        }
                    }
                },
                onSaleClick = {
                    // View sale details
                },
                onSearchQueryChange = {
                    // Update search query
                },
                onStatusFilterClick = {
                    // Filter by status
                },
                onDateRangeClick = {
                    // Filter by date range
                }
            )
        }

        // Add more screens here
    }
}