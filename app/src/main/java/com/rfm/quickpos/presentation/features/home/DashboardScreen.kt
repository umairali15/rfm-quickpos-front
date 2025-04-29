package com.rfm.quickpos.presentation.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * Dashboard Screen with sales metrics
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RFM",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "QuickPOS",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClicked) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)  // Using CircleShape from androidx.compose.foundation.shape
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
        ) {
            // Date Range
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = metrics.dateRange,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Key Metrics Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                StatusCard(
                    title = "Number of Sales",
                    value = metrics.totalSales,
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                StatusCard(
                    title = "Average Sale Amount",
                    value = metrics.salesAmount,
                    icon = Icons.Default.Money,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                StatusCard(
                    title = "Total Customers",
                    value = metrics.customers,
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Revenue Chart Section
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Revenue",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simplified Chart Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        // Here we would render a real chart using Compose Charts library
                        // For the prototype, we'll just show colored lines
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .align(Alignment.Center)
                        )

                        // Green revenue line with a peak
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 0.dp,
                                        topEnd = 0.dp,
                                        bottomStart = 100.dp,
                                        bottomEnd = 100.dp
                                    )
                                )
                                .background(Color(0xFF00BF69).copy(alpha = 0.2f))
                                .align(Alignment.Center)
                        )

                        // Highlight point
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)  // Using CircleShape
                                .background(Color(0xFF00BF69))
                                .align(Alignment.CenterStart)
                                .padding(start = 80.dp)
                        )

                        Text(
                            text = "10:00",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 80.dp, top = 8.dp)
                        )
                    }

                    // X-axis labels
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        listOf("8:00", "10:00", "14:00", "18:00", "22:00", "4:00").forEach { time ->
                            Text(
                                text = time,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Methods Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Payment Methods Card
                ElevatedCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Payment Methods",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Simplified Pie Chart
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Card vs Cash distribution
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .aspectRatio(1f)
                                    .padding(8.dp)
                            ) {
                                // Cash segment (green)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 100.dp,
                                                topEnd = 0.dp,
                                                bottomStart = 100.dp,
                                                bottomEnd = 0.dp
                                            )
                                        )
                                        .background(Color(0xFF00BF69))
                                )

                                // Card segment (blue)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 0.dp,
                                                topEnd = 100.dp,
                                                bottomStart = 0.dp,
                                                bottomEnd = 100.dp
                                            )
                                        )
                                        .background(Color(0xFF0057B8))
                                )
                            }

                            // Legend
                            Column(
                                modifier = Modifier.padding(start = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(Color(0xFF00BF69))
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "By Cash",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "${metrics.paymentMethodChart.cashPercentage.toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(Color(0xFF0057B8))
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "By Card",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "${metrics.paymentMethodChart.cardPercentage.toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Section
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Action Buttons Grid
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickActionButton(
                        icon = Icons.Default.ShoppingCart,
                        label = "New Sale",
                        onClick = onNewSaleClicked,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    QuickActionButton(
                        icon = Icons.Default.ShoppingCart,
                        label = "Orders",
                        onClick = onOrdersClicked,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    QuickActionButton(
                        icon = Icons.Default.InsertChart,
                        label = "Reports",
                        onClick = onReportsClicked,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickActionButton(
                        icon = Icons.Default.ShoppingBag,
                        label = "Catalog",
                        onClick = onCatalogClicked,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    QuickActionButton(
                        icon = Icons.Default.Person,
                        label = "Customers",
                        onClick = onCustomersClicked,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Empty space for balance
                    Box(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Status card - For KPI metrics on dashboard
 */
@Composable
fun StatusCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Quick Action Button Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    // Sample data
    val metrics = DashboardMetrics(
        totalSales = "2,148M",
        salesAmount = "16.94K",
        customers = "126.8K",
        dateRange = "01.01.2024 - 01.01.2025",
        paymentMethodChart = PaymentMethodsData(
            cashPercentage = 35f,
            cardPercentage = 65f
        )
    )

    RFMQuickPOSTheme {
        DashboardScreen(
            metrics = metrics,
            onNewSaleClicked = {},
            onReportsClicked = {},
            onOrdersClicked = {},
            onCatalogClicked = {},
            onCustomersClicked = {},
            onSettingsClicked = {},
            userName = "User"
        )
    }
}