// app/src/main/java/com/rfm/quickpos/presentation/navigation/UnifiedNavigation.kt

package com.rfm.quickpos.presentation.navigation

import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.rfm.quickpos.domain.model.UiMode
import com.rfm.quickpos.presentation.debug.DebugMenu
import com.rfm.quickpos.presentation.features.auth.DualModePinLoginScreen
import com.rfm.quickpos.presentation.features.auth.LoginScreen
import com.rfm.quickpos.presentation.features.catalog.*
import com.rfm.quickpos.presentation.features.cart.*
import com.rfm.quickpos.presentation.features.shift.*
import com.rfm.quickpos.presentation.features.splash.SplashScreen
import com.rfm.quickpos.presentation.features.error.*
import com.rfm.quickpos.presentation.features.home.*
import com.rfm.quickpos.presentation.features.history.*
import com.rfm.quickpos.presentation.features.kiosk.*
import com.rfm.quickpos.presentation.features.payment.*
import com.rfm.quickpos.presentation.features.sale.*
import java.util.Date
import com.rfm.quickpos.presentation.common.models.ActionCardData

/**
 * Unified navigation system that handles both Cashier and Kiosk modes
 */
@Composable
fun UnifiedNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AuthScreen.PinLogin.route,
    uiMode: UiMode,
    onChangeMode: (UiMode) -> Unit
) {
    // Debug menu state
    var showDebugMenu by remember { mutableStateOf(false) }

    // Track whether a shift is currently open or not
    var isShiftOpen by remember { mutableStateOf(false) }

    // Sample data for screens
    val sampleData = getSampleData()

    // Debug Menu dialog
    if (showDebugMenu) {
        DebugMenu(
            navController = navController,
            currentMode = uiMode,
            onChangeMode = onChangeMode,
            onDismiss = { showDebugMenu = false }
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ===== AUTH SCREENS (COMMON) =====
        composable(AuthScreen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        // Force cashier mode for email login
                        onChangeMode(UiMode.CASHIER)

                        // Check if a shift is open, otherwise go to open shift screen
                        if (isShiftOpen) {
                            navigateToHomeScreen(navController, UiMode.CASHIER)
                        } else {
                            navController.navigate(Screen.OpenShift.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                },
                onForgotPasswordClick = { },
                onSwitchToPinLogin = {
                    navController.navigate(AuthScreen.PinLogin.route)
                }
            )
        }

        composable(AuthScreen.PinLogin.route) {
            DualModePinLoginScreen(
                onPinSubmit = { pin, mode ->
                    // Set UI mode based on PIN
                    onChangeMode(mode)

                    // For Cashier mode, check if a shift is open
                    if (mode == UiMode.CASHIER) {
                        if (isShiftOpen) {
                            navigateToHomeScreen(navController, mode)
                        } else {
                            navController.navigate(Screen.OpenShift.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    } else {
                        // For Kiosk mode, go directly to attract screen
                        navigateToHomeScreen(navController, mode)
                    }
                },
                onBackToEmailLogin = {
                    navController.navigate(AuthScreen.Login.route) {
                        popUpTo(AuthScreen.PinLogin.route) { inclusive = true }
                    }
                }
            )
        }

        // ===== SPLASH SCREEN =====
        composable(Screen.Splash.route) {
            SplashScreen(
                onInitializationComplete = {
                    if (uiMode == UiMode.CASHIER && !isShiftOpen) {
                        navController.navigate(Screen.OpenShift.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navigateToHomeScreen(navController, uiMode)
                    }
                }
            )
        }

        // ===== CASHIER MODE SCREENS =====
        if (uiMode == UiMode.CASHIER) {
            // Dashboard
            composable(Screen.Dashboard.route) {
                val shiftActions = getShiftActions(isShiftOpen, navController)

                DashboardScreen(
                    metrics = sampleData.dashboardMetrics,
                    onNewSaleClicked = {
                        navController.navigate(Screen.Catalog.route)
                    },
                    onReportsClicked = {
                        navController.navigate(Screen.SalesHistory.route)
                    },
                    onOrdersClicked = {
                        navController.navigate(Screen.SalesHistory.route)
                    },
                    onCatalogClicked = {
                        navController.navigate(Screen.Catalog.route)
                    },
                    onCustomersClicked = { /* Not implemented yet */ },
                    onSettingsClicked = { showDebugMenu = true }, // Show debug on settings click
                    userName = "Demo",
                    // Add shift-related action cards to the dashboard
                    additionalActions = shiftActions
                )
            }

            // Catalog
            composable(Screen.Catalog.route) {
                var showAddItemSheet by remember { mutableStateOf(false) }

                CatalogScreen(
                    state = sampleData.catalogState,
                    onBackClick = { navController.popBackStack() },
                    onSearchQueryChange = { /* Update search query */ },
                    onSearchSubmit = { /* Perform search */ },
                    onCategorySelected = { /* Filter by category */ },
                    onProductClick = { product ->
                        navController.navigate(Screen.ItemDetail.createRoute(product.id))
                    },
                    onAddToCart = { /* Add to cart */ },
                    onScanBarcode = { showAddItemSheet = true },
                    onCartClick = { navController.navigate(Screen.Cart.route) },
                    onAddCustomItem = { showAddItemSheet = true }
                )

                if (showAddItemSheet) {
                    AddItemBottomSheet(
                        onDismiss = { showAddItemSheet = false },
                        onCatalogItemClick = { showAddItemSheet = false },
                        onScanClick = { showAddItemSheet = false },
                        onNonCatalogItemClick = { showAddItemSheet = false },
                        onDiscountClick = { showAddItemSheet = false },
                        onCommentClick = { showAddItemSheet = false },
                        onCustomerClick = { showAddItemSheet = false },
                        currentSaleNumber = "5917-1610-174122"
                    )
                }
            }

            // Cart
            composable(Screen.Cart.route) {
                CartScreen(
                    state = sampleData.cartState,
                    onBackClick = { navController.popBackStack() },
                    onCheckoutClick = { navController.navigate(Screen.Payment.route) },
                    onItemQuantityChange = { _, _ -> /* Update item quantity */ },
                    onRemoveItem = { /* Remove item from cart */ },
                    onDiscountCodeChange = { /* Update discount code */ },
                    onApplyDiscountClick = { /* Apply discount */ },
                    onNoteChange = { /* Update note */ },
                    onAddCustomerClick = { /* Add customer */ },
                    onClearCart = { /* Clear cart */ }
                )
            }

            // Payment
            composable(Screen.Payment.route) {
                var paymentState by remember { mutableStateOf(sampleData.paymentState) }

                PaymentScreen(
                    state = paymentState,
                    onBackClick = { navController.popBackStack() },
                    onPaymentMethodSelected = { method ->
                        paymentState = paymentState.copy(selectedPaymentMethod = method)
                    },
                    onProcessPayment = {
                        paymentState = paymentState.copy(isProcessing = true)
                        // Simulate payment processing
                        android.os.Handler().postDelayed({
                            navController.navigate(Screen.PaymentSuccess.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = false }
                            }
                        }, 1500)
                    },
                    onSplitAmountChange = { cardAmount, cashAmount ->
                        paymentState = paymentState.copy(
                            splitPaymentData = SplitPaymentData(cardAmount, cashAmount)
                        )
                    },
                    onCashReceivedChange = { amount ->
                        paymentState = paymentState.copy(cashReceived = amount)
                    }
                )
            }

            // Payment Success
            composable(Screen.PaymentSuccess.route) {
                var successState by remember { mutableStateOf(sampleData.paymentSuccessState) }

                PaymentSuccessScreen(
                    state = successState,
                    onPrintReceiptClick = {
                        successState = successState.copy(isPrinting = true)
                        android.os.Handler().postDelayed({
                            successState = successState.copy(isPrinting = false)
                        }, 2000)
                    },
                    onEmailReceiptClick = { /* Email receipt */ },
                    onNewSaleClick = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                        }
                    },
                    onViewOrdersClick = {
                        navController.navigate(Screen.SalesHistory.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                        }
                    }
                )
            }

            // Sales History
            composable(Screen.SalesHistory.route) {
                SalesHistoryScreen(
                    state = sampleData.salesHistoryState,
                    onBackClick = { navController.popBackStack() },
                    onNewSaleClick = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                        }
                    },
                    onSaleClick = { /* View sale details */ },
                    onSearchQueryChange = { /* Update search query */ },
                    onStatusFilterClick = { /* Filter by status */ },
                    onDateRangeClick = { /* Filter by date range */ }
                )
            }

            // Error Screen
            composable(
                route = Screen.Error.route,
                arguments = listOf(navArgument("errorType") { type = NavType.StringType })
            ) {
                val errorTypeStr = it.arguments?.getString("errorType") ?: ErrorType.UNKNOWN.name
                val errorType = try {
                    ErrorType.valueOf(errorTypeStr)
                } catch (e: Exception) {
                    ErrorType.UNKNOWN
                }

                ErrorScreen(
                    errorType = errorType,
                    onRetryClick = { navController.popBackStack() },
                    onContactSupportClick = { /* Handle contact support */ }
                )
            }

            // Item Detail
            composable(
                route = Screen.ItemDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) {
                val productId = it.arguments?.getString("productId") ?: ""
                val product = sampleData.products.find { it.id == productId }
                    ?: Product(id = productId, name = "Unknown Product", price = 0.0, categoryId = "")

                ItemDetailScreen(
                    product = product,
                    onClose = { navController.popBackStack() },
                    onAddToCart = { product, modifications ->
                        navController.popBackStack()
                    },
                    userCanOverridePrice = true // Based on user role
                )
            }

            // ===== SHIFT MANAGEMENT SCREENS =====

            // Open Shift
            composable(Screen.OpenShift.route) {
                OpenShiftScreen(
                    onBackClick = {
                        // If coming from login, need to go back to login instead of exit
                        if (navController.previousBackStackEntry?.destination?.route == AuthScreen.PinLogin.route ||
                            navController.previousBackStackEntry?.destination?.route == AuthScreen.Login.route) {
                            navController.navigate(AuthScreen.PinLogin.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onOpenShift = { openingAmount, notes ->
                        isShiftOpen = true // Mark shift as open
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Cash Movement
            composable(Screen.CashMovement.route) {
                CashMovementScreen(
                    onBackClick = { navController.popBackStack() },
                    onCashMovementSubmit = { type, amount, reason, note ->
                        // After cash movement, return to dashboard
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            // Close Shift
            composable(Screen.CloseShift.route) {
                CloseShiftScreen(
                    shiftSummary = sampleData.shiftSummary,
                    onBackClick = { navController.popBackStack() },
                    onCloseShift = { closingAmount, notes, printReceipt, emailReceipt ->
                        isShiftOpen = false // Mark shift as closed
                        navController.navigate(Screen.ShiftSummary.route)
                    }
                )
            }

            // Shift Summary
            composable(Screen.ShiftSummary.route) {
                ShiftSummaryScreen(
                    shift = sampleData.shiftDetail,
                    onBackClick = {
                        // If we just closed a shift, the next back should take us to login
                        if (!isShiftOpen) {
                            navController.navigate(AuthScreen.PinLogin.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onExportCsv = { /* Handle export to CSV */ },
                    onEmailReport = { /* Handle email report */ }
                )
            }
        }

        // ===== KIOSK MODE SCREENS =====
        if (uiMode == UiMode.KIOSK) {
            composable(KioskScreen.Attract.route) {
                AttractScreen(
                    onStartOrderClick = {
                        navController.navigate(KioskScreen.Catalog.route)
                    }
                )
            }

            composable(KioskScreen.Catalog.route) {
                KioskInactivityDetector(
                    onTimeout = {
                        navController.navigate(KioskScreen.Attract.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) { inactivityModifier ->
                    KioskCatalogScreen(
                        onBackClick = { navController.popBackStack() },
                        onCartClick = { navController.navigate(KioskScreen.Cart.route) },
                        onProductClick = { /* Handle product selection */ },
                        modifier = inactivityModifier
                    )
                }
            }

            composable(KioskScreen.Cart.route) {
                KioskInactivityDetector(
                    onTimeout = {
                        navController.navigate(KioskScreen.Attract.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) { inactivityModifier ->
                    KioskCartScreen(
                        onBackClick = { navController.popBackStack() },
                        onCheckoutClick = { navController.navigate(KioskScreen.Payment.route) },
                        modifier = inactivityModifier
                    )
                }
            }

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
                        onBackClick = { navController.popBackStack() },
                        onPaymentComplete = {
                            navController.navigate(KioskScreen.PaymentSuccess.route) {
                                popUpTo(KioskScreen.Attract.route) { inclusive = false }
                            }
                        },
                        modifier = inactivityModifier
                    )
                }
            }

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
}

/**
 * Helper function to navigate to the appropriate home screen based on mode
 */
private fun navigateToHomeScreen(navController: NavController, mode: UiMode) {
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


@Composable
private fun getShiftActions(isShiftOpen: Boolean, navController: NavController): List<ActionCardData> {
    val actions = mutableListOf<ActionCardData>()

    if (isShiftOpen) {
        // Only show these actions if a shift is open
        actions.add(
            ActionCardData(
                title = "Cash In",
                icon = androidx.compose.material.icons.Icons.Default.ArrowUpward,
                onClick = {
                    navController.navigate(Screen.CashMovement.route)
                },
                backgroundColor = androidx.compose.ui.graphics.Color(0xFF00C853), // Green
                contentColor = androidx.compose.ui.graphics.Color.White
            )
        )

        actions.add(
            ActionCardData(
                title = "Cash Out",
                icon = androidx.compose.material.icons.Icons.Default.ArrowDownward,
                onClick = {
                    navController.navigate(Screen.CashMovement.route)
                },
                backgroundColor = androidx.compose.ui.graphics.Color(0xFFFF5252), // Red
                contentColor = androidx.compose.ui.graphics.Color.White
            )
        )

        actions.add(
            ActionCardData(
                title = "Close Shift",
                icon = androidx.compose.material.icons.Icons.Default.DoNotDisturb,
                onClick = {
                    navController.navigate(Screen.CloseShift.route)
                },
                backgroundColor = androidx.compose.ui.graphics.Color(0xFFFFAB00), // Amber
                contentColor = androidx.compose.ui.graphics.Color.Black
            )
        )
    } else {
        // Show this action if no shift is open
        actions.add(
            ActionCardData(
                title = "Open Shift",
                icon = androidx.compose.material.icons.Icons.Default.PlayArrow,
                onClick = {
                    navController.navigate(Screen.OpenShift.route)
                },
                backgroundColor = androidx.compose.ui.graphics.Color(0xFF2196F3), // Blue
                contentColor = androidx.compose.ui.graphics.Color.White
            )
        )
    }

    return actions
}

/**
 * Data class for action card specifications
 */
data class ActionCardData(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit,
    val backgroundColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color
)

/**
 * Generate sample data for screens
 */
private fun getSampleData(): SampleData {
    // Same implementation as before
    val categories = listOf(
        ProductCategory("1", "Beverages"),
        ProductCategory("2", "Food"),
        ProductCategory("3", "General")
    )

    val products = listOf(
        Product("1", "Coffee", 15.00, "1", "5901234123457"),
        Product("2", "Croissant", 10.00, "2", "4003994155486", discountPercentage = 10),
        Product("3", "Water Bottle", 5.00, "1", "7622210146083"),
        Product("4", "Sandwich", 20.00, "2", "1234567890123")
    )

    val cartItems = listOf(
        CartItem("1", "Coffee", 15.00, 2),
        CartItem("2", "Croissant", 10.00, 1, discountPercentage = 10),
        CartItem("3", "Water Bottle", 5.00, 3, notes = "Cold")
    )

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val discount = 5.0
    val tax = subtotal * 0.05
    val total = subtotal - discount + tax

    val currentDate = Date()

    return SampleData(
        categories = categories,
        products = products,
        cartItems = cartItems,
        catalogState = CatalogState(
            isLoading = false,
            categories = categories,
            products = products,
            selectedCategoryId = null,
            cartItemCount = 2
        ),
        cartState = CartState(
            cartItems = cartItems,
            subtotal = subtotal,
            discount = discount,
            tax = tax,
            total = total
        ),
        paymentState = PaymentState(
            saleNumber = "5917-1610-174122",
            totalAmount = 800.00,
            selectedPaymentMethod = null,
            cashReceived = null,
            splitPaymentData = SplitPaymentData(),
            isProcessing = false
        ),
        paymentSuccessState = PaymentSuccessState(
            receiptNumber = "5917-1610-174122",
            amount = 800.00,
            paymentMethod = PaymentMethod.CARD,
            isPrinting = false
        ),
        salesHistoryState = SalesHistoryState(
            salesHistory = listOf(
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
            ),
            selectedDateRange = DateRange.TODAY
        ),
        dashboardMetrics = DashboardMetrics(
            totalSales = "2,148",
            salesAmount = "16.94",
            customers = "126.8",
            dateRange = "01.01.2024 - 01.01.2025",
            paymentMethodChart = PaymentMethodsData(
                cashPercentage = 35f,
                cardPercentage = 65f
            )
        ),
        shiftSummary = ShiftSummary(
            openingBalance = 500.0,
            cashSales = 1234.56,
            cashIn = 200.0,
            cashOut = 300.0,
            expectedCash = 1634.56,
            startTime = Date(System.currentTimeMillis() - 28800000) // 8 hours ago
        ),
        shiftDetail = ShiftDetail(
            id = "shift123",
            openedAt = Date(System.currentTimeMillis() - 28800000), // 8 hours ago
            closedAt = Date(),
            openingBalance = 500.0,
            closingBalance = 1430.0,
            expectedClosingBalance = 1450.0,
            variance = -20.0,
            cashMovements = emptyList(),
            totalCashSales = 900.0,
            totalCardSales = 1500.0,
            totalWalletSales = 300.0,
            totalRefunds = 120.0,
            openedByUserId = "user1",
            openedByUserName = "John Smith",
            closedByUserId = "user1",
            closedByUserName = "John Smith",
            status = ShiftStatus.CLOSED
        )
    )
}

/**
 * Data class to hold all sample data for screens
 */
private data class SampleData(
    val categories: List<ProductCategory>,
    val products: List<Product>,
    val cartItems: List<CartItem>,
    val catalogState: CatalogState,
    val cartState: CartState,
    val paymentState: PaymentState,
    val paymentSuccessState: PaymentSuccessState,
    val salesHistoryState: SalesHistoryState,
    val dashboardMetrics: DashboardMetrics,
    val shiftSummary: ShiftSummary,
    val shiftDetail: ShiftDetail
)