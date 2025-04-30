package com.rfm.quickpos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rfm.quickpos.domain.model.UiMode
import com.rfm.quickpos.presentation.features.catalog.CatalogScreen
import com.rfm.quickpos.presentation.features.catalog.CatalogState
import com.rfm.quickpos.presentation.features.catalog.ItemDetailScreen
import com.rfm.quickpos.presentation.features.catalog.Product
import com.rfm.quickpos.presentation.features.catalog.ProductCategory
import com.rfm.quickpos.presentation.features.cart.CartScreen
import com.rfm.quickpos.presentation.features.cart.CartState
import com.rfm.quickpos.presentation.features.cart.CartItem
import com.rfm.quickpos.presentation.features.error.ErrorScreen
import com.rfm.quickpos.presentation.features.error.ErrorType
import com.rfm.quickpos.presentation.features.home.DashboardMetrics
import com.rfm.quickpos.presentation.features.home.DashboardScreen
import com.rfm.quickpos.presentation.features.home.PaymentMethodsData
import com.rfm.quickpos.presentation.features.payment.PaymentScreen
import com.rfm.quickpos.presentation.features.payment.PaymentState
import com.rfm.quickpos.presentation.features.payment.PaymentMethod
import com.rfm.quickpos.presentation.features.payment.PaymentSuccessScreen
import com.rfm.quickpos.presentation.features.payment.PaymentSuccessState
import com.rfm.quickpos.presentation.features.payment.SplitPaymentData
import com.rfm.quickpos.presentation.features.sale.AddItemBottomSheet
import com.rfm.quickpos.presentation.features.history.DateRange
import com.rfm.quickpos.presentation.features.history.SaleHistoryItem
import com.rfm.quickpos.presentation.features.history.SaleStatus
import com.rfm.quickpos.presentation.features.history.SalesHistoryScreen
import com.rfm.quickpos.presentation.features.history.SalesHistoryState
import com.rfm.quickpos.presentation.features.shift.CashMovementScreen
import com.rfm.quickpos.presentation.features.shift.CashMovementType
import com.rfm.quickpos.presentation.features.shift.CloseShiftScreen
import com.rfm.quickpos.presentation.features.shift.OpenShiftScreen
import com.rfm.quickpos.presentation.features.shift.ShiftDetail
import com.rfm.quickpos.presentation.features.shift.ShiftStatus
import com.rfm.quickpos.presentation.features.shift.ShiftSummary
import com.rfm.quickpos.presentation.features.shift.ShiftSummaryScreen
import com.rfm.quickpos.presentation.features.splash.SplashScreen
import com.rfm.quickpos.presentation.debug.DebugMenu
import java.util.Date

/**
 * Properly structured navigation graph for Cashier mode
 * Uses standard NavHost with composable() destinations
 */
@Composable
fun ProperCashierNavGraph(
    navController: NavHostController,
    uiMode: UiMode,
    onChangeMode: (UiMode) -> Unit,
    startDestination: String = Screen.Dashboard.route
) {
    // Sample data for the screens
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

    // For debug menu
    var showDebugMenu by remember { mutableStateOf(false) }

    // Define the navigation graph using NavHost
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onInitializationComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
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
                    // Navigate to reports (not implemented yet)
                },
                onOrdersClicked = {
                    navController.navigate(Screen.SalesHistory.route)
                },
                onCatalogClicked = {
                    navController.navigate(Screen.Catalog.route)
                },
                onCustomersClicked = {
                    // Navigate to customers (not implemented yet)
                },
                onSettingsClicked = {
                    // For debugging, open debug menu instead of settings
                    showDebugMenu = true
                },
                userName = "Demo"
            )

            // Debug Menu (activated by settings button)
            if (showDebugMenu) {
                DebugMenu(
                    navController = navController,
                    currentMode = uiMode,
                    onChangeMode = onChangeMode,
                    onDismiss = { showDebugMenu = false }
                )
            }
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
                onProductClick = { product ->
                    // Navigate to item detail screen
                    navController.navigate(Screen.ItemDetail.createRoute(product.id))
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
                onRetryClick = {
                    navController.popBackStack()
                },
                onContactSupportClick = {
                    // Handle contact support
                }
            )
        }

        // Item Detail
        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) {
            val productId = it.arguments?.getString("productId") ?: ""
            val product = sampleProducts.find { it.id == productId } ?:
            Product(id = productId, name = "Unknown Product", price = 0.0, categoryId = "")

            ItemDetailScreen(
                product = product,
                onClose = {
                    navController.popBackStack()
                },
                onAddToCart = { product, modifications ->
                    // Handle adding to cart
                    navController.popBackStack()
                },
                userCanOverridePrice = true // Based on user role
            )
        }

        // Open Shift
        composable(Screen.OpenShift.route) {
            OpenShiftScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onOpenShift = { openingAmount, notes ->
                    // Handle opening shift
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.OpenShift.route) { inclusive = true }
                    }
                }
            )
        }

        // Cash Movement
        composable(Screen.CashMovement.route) {
            CashMovementScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCashMovementSubmit = { type, amount, reason, note ->
                    // Handle cash movement
                    navController.popBackStack()
                }
            )
        }

        // Close Shift
        composable(Screen.CloseShift.route) {
            val shiftSummary = ShiftSummary(
                openingBalance = 500.0,
                cashSales = 1234.56,
                cashIn = 200.0,
                cashOut = 300.0,
                expectedCash = 1634.56,
                startTime = Date(System.currentTimeMillis() - 28800000) // 8 hours ago
            )

            CloseShiftScreen(
                shiftSummary = shiftSummary,
                onBackClick = {
                    navController.popBackStack()
                },
                onCloseShift = { closingAmount, notes, printReceipt, emailReceipt ->
                    // Handle closing shift
                    navController.navigate(Screen.ShiftSummary.route)
                }
            )
        }

        // Shift Summary
        composable(Screen.ShiftSummary.route) {
            // Create a sample shift summary for demonstration
            val currentTime = System.currentTimeMillis()
            val startTime = Date(currentTime - 28800000) // 8 hours ago

            val sampleShift = ShiftDetail(
                id = "shift123",
                openedAt = startTime,
                closedAt = Date(currentTime),
                openingBalance = 500.0,
                closingBalance = 1430.0,
                expectedClosingBalance = 1450.0,
                variance = -20.0,
                cashMovements = emptyList(), // You'd populate this with real data
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

            ShiftSummaryScreen(
                shift = sampleShift,
                onBackClick = {
                    navController.popBackStack()
                },
                onExportCsv = {
                    // Handle export to CSV
                },
                onEmailReport = {
                    // Handle email report
                }
            )
        }
    }
}