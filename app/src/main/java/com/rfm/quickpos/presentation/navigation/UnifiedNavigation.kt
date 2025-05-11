package com.rfm.quickpos.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.*
import androidx.navigation.compose.*
import com.rfm.quickpos.QuickPOSApplication
import com.rfm.quickpos.data.repository.AuthRepository
import com.rfm.quickpos.data.repository.DeviceRepository
import com.rfm.quickpos.domain.model.DevicePairingInfo
import com.rfm.quickpos.domain.model.PairingStatus
import com.rfm.quickpos.domain.model.UiMode
import com.rfm.quickpos.presentation.debug.DebugMenu
import com.rfm.quickpos.presentation.features.auth.DualModePinLoginScreen
import com.rfm.quickpos.presentation.features.auth.LoginScreen
import com.rfm.quickpos.presentation.features.catalog.*
import com.rfm.quickpos.presentation.features.cart.*
import com.rfm.quickpos.presentation.features.setup.DevicePairingScreen
import com.rfm.quickpos.presentation.features.setup.DevicePairingState
import com.rfm.quickpos.presentation.features.shift.*
import com.rfm.quickpos.presentation.features.splash.SplashScreen
import com.rfm.quickpos.presentation.features.error.*
import com.rfm.quickpos.presentation.features.home.*
import com.rfm.quickpos.presentation.features.history.*
import com.rfm.quickpos.presentation.features.kiosk.*
import com.rfm.quickpos.presentation.features.payment.*
import com.rfm.quickpos.presentation.common.models.ActionCardData
import java.util.Date
import com.rfm.quickpos.data.remote.models.CatalogState
import com.rfm.quickpos.data.remote.models.Product
import com.rfm.quickpos.data.remote.models.ProductCategory

@Composable
fun UnifiedNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AuthScreen.PinLogin.route,
    uiMode: UiMode,
    onChangeMode: (UiMode) -> Unit,
    onLoginSuccess: ((String) -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    deviceRepository: DeviceRepository? = null,
    authRepository: AuthRepository
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
                            onLoginSuccess?.invoke("email_login") // Call login success callback with dummy id
                        } else {
                            navController.navigate(Screen.OpenShift.route) {
                                popUpTo(0) { inclusive = true }
                            }
                            onLoginSuccess?.invoke("email_login") // Call login success callback with dummy id
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

                    // The user is now authenticated by the ViewModel
                    // Get the user ID from the auth repository if available
                    onLoginSuccess?.invoke("pin_login") // Call login success callback
                },
                onBackToEmailLogin = {
                    navController.navigate(AuthScreen.Login.route) {
                        popUpTo(AuthScreen.PinLogin.route) { inclusive = true }
                    }
                },
                onDevicePairing = {
                    navController.navigate(Screen.DevicePairing.route)
                }
            )
        }

        composable(Screen.DevicePairing.route) {
            var pairingState by remember {
                mutableStateOf(
                    DevicePairingState(
                        pairingInfo = DevicePairingInfo(),
                        status = PairingStatus.INITIAL,
                        isLoading = false
                    )
                )
            }

            DevicePairingScreen(
                state = pairingState,
                onPairingInfoChange = { newInfo ->
                    pairingState = pairingState.copy(pairingInfo = newInfo)
                },
                onPairingSubmit = {
                    // Set loading state
                    pairingState = pairingState.copy(
                        isLoading = true,
                        status = PairingStatus.PAIRING
                    )

                    // Simulate network request
                    android.os.Handler().postDelayed({
                        // Check if required fields are filled (this would be a real API call)
                        // UPDATE THIS PART - use deviceAlias and branchId instead of merchantId and terminalId
                        if (pairingState.pairingInfo.deviceAlias.isNotBlank() &&
                            pairingState.pairingInfo.branchId.isNotBlank()) {

                            // Success - navigate to login
                            pairingState = pairingState.copy(
                                isLoading = false,
                                status = PairingStatus.SUCCESS,
                                isPaired = true
                            )

                            // Here you would actually store the pairing info to persistent storage

                            navController.navigate(AuthScreen.PinLogin.route) {
                                popUpTo(Screen.DevicePairing.route) { inclusive = true }
                            }
                        } else {
                            // Error - show error message
                            pairingState = pairingState.copy(
                                isLoading = false,
                                status = PairingStatus.ERROR,
                                errorMessage = "Please fill in all required fields"
                            )
                        }
                    }, 1500)
                },
                onSkipSetup = {
                    // For development only
                    navController.navigate(AuthScreen.PinLogin.route) {
                        popUpTo(Screen.DevicePairing.route) { inclusive = true }
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
        // Update Dashboard to handle logout properly
        composable(Screen.Dashboard.route) {
            if (uiMode == UiMode.CASHIER) {
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
                    onSettingsClicked = {
                        // Show a dialog with settings and logout option
                        showDebugMenu = true
                    },
                    userName = authRepository.authState.collectAsState().value.let {
                        when (it) {
                            is com.rfm.quickpos.data.repository.AuthState.Success ->
                                it.userData.fullName
                            else ->
                                "User"
                        }
                    },
                    // Add shift-related action cards to the dashboard
                    additionalActions = shiftActions,
                    // Add logout action
                    onLogoutClick = {
                        // Call the logout callback
                        onLogout?.invoke()
                    }
                )
            } else {
                // If somehow we get here in kiosk mode, redirect to attract screen
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }
        composable(Screen.Catalog.route) {
            if (uiMode == UiMode.CASHIER) {
                val catalogRepository =
                    (LocalContext.current.applicationContext as QuickPOSApplication).catalogRepository

                BusinessTypeCatalogScreen(
                    catalogRepository = catalogRepository,
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { item ->
                        // FIXED: Navigate to item detail with the correct item ID
                        navController.navigate(Screen.ItemDetail.createRoute(item.id))
                    },
                    onAddToCart = { item ->
                        // Add to cart logic
                        // TODO: Implement cart repository integration
                    },
                    onScanBarcode = {
                        // Handle barcode scanning
                        // TODO: Implement barcode scanning
                    },
                    onCartClick = {
                        navController.navigate(Screen.Cart.route)
                    },
                    onAddCustomItem = {
                        // This is now handled within the screen using bottom sheet
                        // The bottom sheet opens automatically when the FAB is clicked
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // Cart
        composable(Screen.Cart.route) {
            if (uiMode == UiMode.CASHIER) {
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
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // Payment
        composable(Screen.Payment.route) {
            if (uiMode == UiMode.CASHIER) {
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
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // Payment Success
        composable(Screen.PaymentSuccess.route) {
            if (uiMode == UiMode.CASHIER) {
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
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // Sales History
        composable(Screen.SalesHistory.route) {
            if (uiMode == UiMode.CASHIER) {
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
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
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

        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) {
            if (uiMode == UiMode.CASHIER) {
                val productId = it.arguments?.getString("productId") ?: ""
                val catalogRepository = (LocalContext.current.applicationContext as QuickPOSApplication).catalogRepository

                // Ensure navigation runs on main thread
                LaunchedEffect(productId) {
                    if (productId.isBlank()) {
                        navController.popBackStack()
                    }
                }

                // USE THE NEW UNIFIED SCREENN
                ItemDetailScreenWithVariations(
                    itemId = productId,
                    catalogRepository = catalogRepository,
                    onClose = { navController.popBackStack() },
                    onAddToCart = { cartItem ->
                        // Add to cart logic
                        navController.popBackStack()
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // ===== SHIFT MANAGEMENT SCREENS =====

        // Open Shift
        composable(Screen.OpenShift.route) {
            if (uiMode == UiMode.CASHIER) {
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
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // Cash Movement
        composable(Screen.CashMovement.route) {
            if (uiMode == UiMode.CASHIER) {
                CashMovementScreen(
                    onBackClick = { navController.popBackStack() },
                    onCashMovementSubmit = { type, amount, reason, note ->
                        // After cash movement, return to dashboard
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // Close Shift
        composable(Screen.CloseShift.route) {
            if (uiMode == UiMode.CASHIER) {
                CloseShiftScreen(
                    shiftSummary = sampleData.shiftSummary,
                    onBackClick = { navController.popBackStack() },
                    onCloseShift = { closingAmount, notes, printReceipt, emailReceipt ->
                        isShiftOpen = false // Mark shift as closed
                        navController.navigate(Screen.ShiftSummary.route)
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // Shift Summary
        composable(Screen.ShiftSummary.route) {
            if (uiMode == UiMode.CASHIER) {
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
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // ===== KIOSK MODE SCREENS =====
        // Attract Screen (Kiosk home)
        composable(KioskScreen.Attract.route) {
            if (uiMode == UiMode.KIOSK) {
                AttractScreen(
                    onStartOrderClick = {
                        navController.navigate(KioskScreen.Catalog.route)
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // Kiosk Catalog
        composable(KioskScreen.Catalog.route) {
            if (uiMode == UiMode.KIOSK) {
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
                        onProductClick = { itemId ->
                            // Navigate to item detail
                            navController.navigate("kiosk_item_detail/$itemId")
                        },
                        modifier = inactivityModifier
                    )
                }
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        composable(
            route = "kiosk_item_detail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) {
            if (uiMode == UiMode.KIOSK) {
                val itemId = it.arguments?.getString("itemId") ?: ""

                KioskInactivityDetector(
                    onTimeout = {
                        navController.navigate(KioskScreen.Attract.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    timeoutMillis = 180_000 // 3 minutes for item detail
                ) { inactivityModifier ->
                    KioskItemDetailScreen(
                        itemId = itemId,
                        onClose = { navController.popBackStack() },
                        modifier = inactivityModifier
                    )
                }
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // Kiosk Cart
        composable(KioskScreen.Cart.route) {
            if (uiMode == UiMode.KIOSK) {
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
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // Kiosk Payment
        composable(KioskScreen.Payment.route) {
            if (uiMode == UiMode.KIOSK) {
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
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
            }
        }

        // Kiosk Payment Success
        composable(KioskScreen.PaymentSuccess.route) {
            if (uiMode == UiMode.KIOSK) {
                KioskPaymentSuccessScreen(
                    onFinishClick = {
                        navController.navigate(KioskScreen.Attract.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navigateToHomeScreen(navController, uiMode)
                }
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
                icon = Icons.Default.ArrowUpward,
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
                icon = Icons.Default.ArrowDownward,
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
                icon = Icons.Default.DoNotDisturb,
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
                icon = Icons.Default.PlayArrow,
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
 * Generate sample data for screens
 */
private fun getSampleData(): SampleData {
    // Sample data generation
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