package com.rfm.quickpos.presentation.features.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.rfm.quickpos.presentation.common.components.RfmBadge
import com.rfm.quickpos.presentation.common.components.RfmCategoryChip
import com.rfm.quickpos.presentation.common.components.RfmDivider
import com.rfm.quickpos.presentation.common.components.RfmElevatedCard
import com.rfm.quickpos.presentation.common.components.RfmLoadingIndicator
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmSearchBar
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enum representing sale status
 */
enum class SaleStatus {
    OPEN,
    COMPLETED,
    VOIDED,
    REFUNDED,
    NOT_PAID
}

/**
 * Data class representing a sale in history
 */
data class SaleHistoryItem(
    val id: String,
    val saleNumber: String,
    val dateTime: Date,
    val status: SaleStatus,
    val amount: Double,
    val paymentMethod: String? = null,
    val customerName: String? = null,
    val items: List<String> = emptyList()
)

/**
 * Data class for sales history screen state
 */
data class SalesHistoryState(
    val salesHistory: List<SaleHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedStatusFilter: SaleStatus? = null,
    val selectedDateRange: DateRange = DateRange.TODAY
)

/**
 * Enum representing date range filters
 */
enum class DateRange(val displayName: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    LAST_7_DAYS("Last 7 days"),
    THIS_MONTH("This month"),
    CUSTOM("Custom")
}

/**
 * Sales history screen that shows list of past sales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    state: SalesHistoryState,
    onBackClick: () -> Unit,
    onNewSaleClick: () -> Unit,
    onSaleClick: (SaleHistoryItem) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterClick: (SaleStatus?) -> Unit,
    onDateRangeClick: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormatterWithDay = SimpleDateFormat("dd MMM", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sales",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Filter options */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }

                    IconButton(onClick = { /* Search */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewSaleClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Sale"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date range filter chips
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(DateRange.values()) { dateRange ->
                    RfmCategoryChip(
                        text = dateRange.displayName,
                        selected = state.selectedDateRange == dateRange,
                        onClick = { onDateRangeClick(dateRange) }
                    )
                }
            }

            // Status filter chips
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // All status option
                item {
                    RfmCategoryChip(
                        text = "All",
                        selected = state.selectedStatusFilter == null,
                        onClick = { onStatusFilterClick(null) }
                    )
                }

                // Status options
                items(SaleStatus.values()) { status ->
                    RfmCategoryChip(
                        text = status.name.lowercase().capitalize(),
                        selected = state.selectedStatusFilter == status,
                        onClick = { onStatusFilterClick(status) }
                    )
                }
            }

            // New Sale Card
            RfmElevatedCard(
                onClick = onNewSaleClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "NEW SALE",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Date label
            Text(
                text = "${state.selectedDateRange.displayName}, ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (state.isLoading) {
                // Loading state
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    RfmLoadingIndicator()
                }
            } else if (state.error != null) {
                // Error state
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        RfmPrimaryButton(
                            text = "Retry",
                            onClick = { /* Retry logic */ }
                        )
                    }
                }
            } else if (state.salesHistory.isEmpty()) {
                // Empty state
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No sales for this period",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Sales history list
                LazyColumn {
                    items(state.salesHistory) { sale ->
                        SaleHistoryCard(
                            sale = sale,
                            onClick = { onSaleClick(sale) },
                            dateFormatter = dateFormatter,
                            dateFormatterWithDay = dateFormatterWithDay
                        )
                    }

                    // Add some bottom padding
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

/**
 * Single sale history card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleHistoryCard(
    sale: SaleHistoryItem,
    onClick: () -> Unit,
    dateFormatter: SimpleDateFormat,
    dateFormatterWithDay: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Date and time
                Text(
                    text = "${dateFormatter.format(sale.dateTime)}, Today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                // Status badge
                StatusBadge(status = sale.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sale details (sale number or customer name)
            Text(
                text = sale.customerName ?: sale.saleNumber,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Payment status pill
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            if (sale.status == SaleStatus.NOT_PAID)
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (sale.status == SaleStatus.NOT_PAID) "Not paid" else "Paid",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (sale.status == SaleStatus.NOT_PAID)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Amount with payment method icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Payment method icon
                    if (sale.paymentMethod != null && sale.status != SaleStatus.NOT_PAID) {
                        val icon = when (sale.paymentMethod.toLowerCase()) {
                            "card" -> Icons.Default.CreditCard
                            "cash" -> Icons.Default.Money
                            else -> Icons.Default.Receipt
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Amount
                    Text(
                        text = "AED ${String.format("%.0f", sale.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Status badge for sale history
 */
@Composable
fun StatusBadge(
    status: SaleStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = when (status) {
        SaleStatus.OPEN -> Triple(
            MaterialTheme.posColors.info.copy(alpha = 0.2f),
            MaterialTheme.posColors.info,
            "Opened"
        )
        SaleStatus.COMPLETED -> Triple(
            MaterialTheme.posColors.success.copy(alpha = 0.2f),
            MaterialTheme.posColors.success,
            "Completed"
        )
        SaleStatus.VOIDED -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.error,
            "Voided"
        )
        SaleStatus.REFUNDED -> Triple(
            MaterialTheme.posColors.warning.copy(alpha = 0.2f),
            MaterialTheme.posColors.warning,
            "Refunded"
        )
        SaleStatus.NOT_PAID -> Triple(
            MaterialTheme.posColors.pending.copy(alpha = 0.2f),
            MaterialTheme.posColors.pending,
            "Not Paid"
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}

// Extension to capitalize the first letter of a string
private fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}

@Preview(showBackground = true)
@Composable
fun SalesHistoryScreenPreview() {
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

    val state = SalesHistoryState(
        salesHistory = salesHistory,
        selectedDateRange = DateRange.TODAY
    )

    RFMQuickPOSTheme {
        Surface {
            SalesHistoryScreen(
                state = state,
                onBackClick = {},
                onNewSaleClick = {},
                onSaleClick = {},
                onSearchQueryChange = {},
                onStatusFilterClick = {},
                onDateRangeClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty Sales History")
@Composable
fun EmptySalesHistoryScreenPreview() {
    val state = SalesHistoryState(
        salesHistory = emptyList(),
        selectedDateRange = DateRange.TODAY
    )

    RFMQuickPOSTheme {
        Surface {
            SalesHistoryScreen(
                state = state,
                onBackClick = {},
                onNewSaleClick = {},
                onSaleClick = {},
                onSearchQueryChange = {},
                onStatusFilterClick = {},
                onDateRangeClick = {}
            )
        }
    }
}