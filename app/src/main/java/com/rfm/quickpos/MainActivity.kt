package com.rfm.quickpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.features.auth.LoginScreen
import com.rfm.quickpos.presentation.features.auth.PinLoginScreen
import com.rfm.quickpos.presentation.features.catalog.CatalogScreen
import com.rfm.quickpos.presentation.features.catalog.CatalogState
import com.rfm.quickpos.presentation.features.catalog.Product
import com.rfm.quickpos.presentation.features.catalog.ProductCategory
import com.rfm.quickpos.presentation.features.home.DashboardMetrics
import com.rfm.quickpos.presentation.features.home.DashboardScreen
import com.rfm.quickpos.presentation.features.home.PaymentMethodsData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RFMQuickPOSTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("login") }
                    var loginErrorMessage by remember { mutableStateOf<String?>(null) }
                    var pinErrorMessage by remember { mutableStateOf<String?>(null) }
                    var usePinLogin by remember { mutableStateOf(false) }

                    when (currentScreen) {
                        "login" -> {
                            if (usePinLogin) {
                                PinLoginScreen(
                                    onPinSubmit = { pin ->
                                        // Demo validation (in production, this would call API)
                                        if (pin == "1234") {
                                            currentScreen = "dashboard"
                                            pinErrorMessage = null
                                        } else {
                                            pinErrorMessage = "Invalid PIN. Please try again."
                                        }
                                    },
                                    onBackToEmailLogin = { usePinLogin = false },
                                    userName = "Cashier",
                                    errorMessage = pinErrorMessage
                                )
                            } else {
                                LoginScreen(
                                    onLoginClick = { email, password ->
                                        // Demo validation (in production, this would call API)
                                        if (email.isNotEmpty() && password.isNotEmpty()) {
                                            currentScreen = "dashboard"
                                            loginErrorMessage = null
                                        } else {
                                            loginErrorMessage = "Please enter both email and password."
                                        }
                                    },
                                    onForgotPasswordClick = { /* Not implemented in demo */ },
                                    errorMessage = loginErrorMessage
                                )
                            }
                        }

                        "dashboard" -> {
                            // Create some sample data for our dashboard
                            val sampleDashboardMetrics = DashboardMetrics(
                                totalSales = "2,148",
                                salesAmount = "16.94",
                                customers = "126.8",
                                dateRange = "01.01.2024 - 01.01.2025",
                                paymentMethodChart = PaymentMethodsData(
                                    cashPercentage = 35f,
                                    cardPercentage = 65f
                                )
                            )

                            // Display our Dashboard screen
                            DashboardScreen(
                                metrics = sampleDashboardMetrics,
                                onNewSaleClicked = { /* Not implemented in demo */ },
                                onReportsClicked = { /* Not implemented in demo */ },
                                onOrdersClicked = { /* Not implemented in demo */ },
                                onCatalogClicked = { currentScreen = "catalog" },
                                onCustomersClicked = { /* Not implemented in demo */ },
                                onSettingsClicked = { currentScreen = "login" }, // For demo, goes back to login
                                userName = "Demo"
                            )
                        }

                        "catalog" -> {
                            // Sample data for catalog
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
                                onBackClick = { currentScreen = "dashboard" },
                                onSearchQueryChange = { /* Not implemented in demo */ },
                                onSearchSubmit = { /* Not implemented in demo */ },
                                onCategorySelected = { /* Not implemented in demo */ },
                                onProductClick = { /* Not implemented in demo */ },
                                onAddToCart = { /* Not implemented in demo */ },
                                onScanBarcode = { /* Not implemented in demo */ },
                                onCartClick = { /* Not implemented in demo */ },
                                onAddCustomItem = { /* Not implemented in demo */ }
                            )
                        }
                    }
                }
            }
        }
    }
}