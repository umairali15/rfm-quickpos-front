package com.rfm.quickpos.presentation.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.components.RfmCard
import com.rfm.quickpos.presentation.common.components.RfmElevatedCard
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.debug.DebugMenu

/**
 * Simplified Dashboard Screen that serves as the home page of the app
 * Updated to include debug menu during development
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    metrics: DashboardMetrics,
    onNewSaleClicked: () -> Unit,
    onReportsClicked: () -> Unit,
    onOrdersClicked: () -> Unit,
    onCatalogClicked: () -> Unit,
    onCustomersClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    userName: String,
    modifier: Modifier = Modifier
) {
    // State to show/hide the debug menu (optional, only for development phase)
    var showDebugMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RFM QuickPOS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    // Debug mode button (only visible during development)
                    IconButton(onClick = { showDebugMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "Debug Menu",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }

                    // Normal settings button
                    IconButton(onClick = onSettingsClicked) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }

                    // User avatar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = userName.first().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Welcome message
            Text(
                text = "Hello, $userName",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "What would you like to do today?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Key metrics in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sales metric
                MetricCard(
                    title = "Sales",
                    value = metrics.totalSales,
                    icon = Icons.Default.Money,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Orders metric
                MetricCard(
                    title = "Avg. Sale",
                    value = metrics.salesAmount,
                    icon = Icons.Default.Receipt,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Customers metric
                MetricCard(
                    title = "Customers",
                    value = metrics.customers,
                    icon = Icons.Default.People,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main actions grid - 2x3 layout
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // First row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // New Sale - featured prominently
                ActionCard(
                    title = "New Sale",
                    icon = Icons.Default.ShoppingCart,
                    onClick = onNewSaleClicked,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Orders
                ActionCard(
                    title = "Orders",
                    icon = Icons.Default.Receipt,
                    onClick = onOrdersClicked,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Second row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Catalog
                ActionCard(
                    title = "Catalog",
                    icon = Icons.Default.ShoppingBag,
                    onClick = onCatalogClicked,
                    modifier = Modifier.weight(1f)
                )

                // Reports
                ActionCard(
                    title = "Reports",
                    icon = Icons.Default.Description,
                    onClick = onReportsClicked,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Third row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customers
                ActionCard(
                    title = "Customers",
                    icon = Icons.Default.Person,
                    onClick = onCustomersClicked,
                    modifier = Modifier.weight(1f)
                )

                // Dashboard (placeholder)
                ActionCard(
                    title = "Dashboard",
                    icon = Icons.Default.Dashboard,
                    onClick = { /* Not implemented in demo */ },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Compact metric card component
 */
@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    RfmElevatedCard(
        containerColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Action card for quick access to app features
 */
@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    RfmCard(
        containerColor = backgroundColor,
        contentColor = contentColor,
        onClick = onClick,
        elevation = 1f,
        modifier = modifier.height(100.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    // Sample data
    val metrics = DashboardMetrics(
        totalSales = "2,148",
        salesAmount = "16.94",
        customers = "126.8",
        dateRange = "01.01.2024 - 01.01.2025",
        paymentMethodChart = PaymentMethodsData(
            cashPercentage = 35f,
            cardPercentage = 65f
        )
    )

    RFMQuickPOSTheme {
        Surface {
            DashboardScreen(
                metrics = metrics,
                onNewSaleClicked = {},
                onReportsClicked = {},
                onOrdersClicked = {},
                onCatalogClicked = {},
                onCustomersClicked = {},
                onSettingsClicked = {},
                userName = "Ahmed"
            )
        }
    }
}