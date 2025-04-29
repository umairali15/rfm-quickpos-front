package com.rfm.quickpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
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
                    // Create some sample data for our dashboard
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

                    // Display our Dashboard screen
                    DashboardScreen(
                        metrics = sampleDashboardMetrics,
                        onNewSaleClicked = { /* Not implemented in demo */ },
                        onReportsClicked = { /* Not implemented in demo */ },
                        onOrdersClicked = { /* Not implemented in demo */ },
                        onCatalogClicked = { /* Not implemented in demo */ },
                        onCustomersClicked = { /* Not implemented in demo */ },
                        onSettingsClicked = { /* Not implemented in demo */ },
                        userName = "Demo"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    RFMQuickPOSTheme {
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

        DashboardScreen(
            metrics = sampleDashboardMetrics,
            onNewSaleClicked = {},
            onReportsClicked = {},
            onOrdersClicked = {},
            onCatalogClicked = {},
            onCustomersClicked = {},
            onSettingsClicked = {},
            userName = "Preview"
        )
    }
}