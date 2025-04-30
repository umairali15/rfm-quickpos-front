// app/src/main/java/com/rfm/quickpos/presentation/features/shift/ShiftSummaryScreen.kt

package com.rfm.quickpos.presentation.features.shift

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.components.RfmOutlinedButton
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Type of cash movement
 */
enum class MovementType {
    CASH_IN,
    CASH_OUT,
    CASH_SALE,
    CARD_SALE,
    REFUND
}

/**
 * Data class representing a cash movement
 */
data class CashMovement(
    val id: String,
    val type: MovementType,
    val amount: Double,
    val reason: String,
    val timestamp: Date,
    val userId: String,
    val userName: String
)

/**
 * Detailed shift data for the summary
 */
data class ShiftDetail(
    val id: String,
    val openedAt: Date,
    val closedAt: Date? = null,
    val openingBalance: Double,
    val closingBalance: Double? = null,
    val expectedClosingBalance: Double,
    val variance: Double? = null,
    val cashMovements: List<CashMovement>,
    val totalCashSales: Double,
    val totalCardSales: Double,
    val totalWalletSales: Double,
    val totalRefunds: Double,
    val openedByUserId: String,
    val openedByUserName: String,
    val closedByUserId: String? = null,
    val closedByUserName: String? = null,
    val status: ShiftStatus
)

/**
 * Status of a shift
 */
enum class ShiftStatus {
    OPEN,
    CLOSED
}

/**
 * Screen that displays the summary of a shift
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftSummaryScreen(
    shift: ShiftDetail,
    onBackClick: () -> Unit,
    onExportCsv: () -> Unit,
    onEmailReport: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showOptionsMenu by remember { mutableStateOf(false) }

    // Format time for display
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Shift Summary",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = if (shift.status == ShiftStatus.OPEN) "Current Shift" else "Closed Shift",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    // More options menu
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options"
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export as CSV") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onExportCsv()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Email Report") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onEmailReport()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // Shift overview card
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Shift duration
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = "Opened: ${dateFormatter.format(shift.openedAt)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (shift.closedAt != null) {
                                        Text(
                                            text = "Closed: ${dateFormatter.format(shift.closedAt)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Shift operator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = "Opened by: ${shift.openedByUserName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (shift.closedByUserName != null) {
                                        Text(
                                            text = "Closed by: ${shift.closedByUserName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Shift status
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val statusColor = if (shift.status == ShiftStatus.OPEN)
                                    MaterialTheme.posColors.success else MaterialTheme.colorScheme.primary
                                val statusText = if (shift.status == ShiftStatus.OPEN) "Active" else "Closed"
                                val statusIcon = if (shift.status == ShiftStatus.OPEN)
                                    Icons.Default.Check else Icons.Default.CalendarMonth

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(statusColor.copy(alpha = 0.1f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = statusIcon,
                                            contentDescription = null,
                                            tint = statusColor,
                                            modifier = Modifier.size(16.dp)
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        Text(
                                            text = statusText,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = statusColor,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Cash summary card
                item {
                    Text(
                        text = "Cash Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Cash opening balance
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Opening Balance",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = formatCurrency(shift.openingBalance),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Cash sales
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Cash Sales",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = formatCurrency(shift.totalCashSales),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Calculate total cash in
                            val totalCashIn = shift.cashMovements
                                .filter { it.type == MovementType.CASH_IN }
                                .sumOf { it.amount }

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Cash In",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = formatCurrency(totalCashIn),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Calculate total cash out
                            val totalCashOut = shift.cashMovements
                                .filter { it.type == MovementType.CASH_OUT }
                                .sumOf { it.amount }

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Cash Out",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "- ${formatCurrency(totalCashOut)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            // Expected cash
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Expected Cash",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = formatCurrency(shift.expectedClosingBalance),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Actual cash and variance if closed
                            if (shift.closingBalance != null && shift.variance != null) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Actual Cash",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = formatCurrency(shift.closingBalance),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Variance
                                val varianceColor = when {
                                    shift.variance > 0 -> MaterialTheme.posColors.success
                                    shift.variance < 0 -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurface
                                }

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Variance",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = formatCurrency(shift.variance),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = varianceColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Sales summary card
                item {
                    Text(
                        text = "Sales Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Cash sales
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Cash Sales",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = formatCurrency(shift.totalCashSales),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Card sales
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Card Sales",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = formatCurrency(shift.totalCardSales),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Digital wallet sales
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Wallet Sales",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = formatCurrency(shift.totalWalletSales),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Refunds
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Refunds",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "- ${formatCurrency(shift.totalRefunds)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            // Total net sales
                            val totalNetSales = shift.totalCashSales + shift.totalCardSales +
                                    shift.totalWalletSales - shift.totalRefunds

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Total Net Sales",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = formatCurrency(totalNetSales),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Cash movements
                item {
                    Text(
                        text = "Cash Movements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (shift.cashMovements.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        ) {
                            Text(
                                text = "No cash movements recorded for this shift",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Filter for cash in/out movements only
                    val cashMovements = shift.cashMovements.filter {
                        it.type == MovementType.CASH_IN || it.type == MovementType.CASH_OUT
                    }

                    items(cashMovements) { movement ->
                        CashMovementItem(
                            movement = movement,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Action buttons
                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        RfmOutlinedButton(
                            text = "Export CSV",
                            onClick = onExportCsv,
                            leadingIcon = Icons.Default.FileDownload,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        RfmPrimaryButton(
                            text = "Email Report",
                            onClick = onEmailReport,
                            leadingIcon = Icons.Default.Email,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * Display a single cash movement item
 */
@Composable
fun CashMovementItem(
    movement: CashMovement,
    modifier: Modifier = Modifier
) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    val (icon, backgroundColor, contentColor) = when (movement.type) {
        MovementType.CASH_IN -> Triple(
            Icons.Default.ArrowDownward,
            MaterialTheme.posColors.successContainer,
            MaterialTheme.posColors.success
        )
        MovementType.CASH_OUT -> Triple(
            Icons.Default.ArrowUpward,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error
        )
        else -> Triple(
            Icons.Default.ArrowDownward,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Movement type icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Movement details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = movement.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = movement.userName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = " • ${timeFormatter.format(movement.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Amount
            Text(
                text = formatCurrency(movement.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

// Helper function to format currency
private fun formatCurrency(amount: Double): String {
    return "AED ${String.format("%.2f", amount)}"
}

@Preview(showBackground = true)
@Composable
fun ShiftSummaryScreenPreview() {
    val currentTime = System.currentTimeMillis()
    val startTime = Date(currentTime - 28800000) // 8 hours ago

    val sampleCashMovements = listOf(
        CashMovement(
            id = "1",
            type = MovementType.CASH_IN,
            amount = 200.0,
            reason = "Petty Cash",
            timestamp = Date(currentTime - 14400000), // 4 hours ago
            userId = "user1",
            userName = "John Smith"
        ),
        CashMovement(
            id = "2",
            type = MovementType.CASH_OUT,
            amount = 150.0,
            reason = "Bank Deposit",
            timestamp = Date(currentTime - 7200000), // 2 hours ago
            userId = "user1",
            userName = "John Smith"
        )
    )

    val shiftDetail = ShiftDetail(
        id = "shift123",
        openedAt = startTime,
        closedAt = Date(currentTime),
        openingBalance = 500.0,
        closingBalance = 1430.0,
        expectedClosingBalance = 1450.0,
        variance = -20.0,
        cashMovements = sampleCashMovements,
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

    RFMQuickPOSTheme {
        Surface {
            ShiftSummaryScreen(
                shift = shiftDetail,
                onBackClick = {},
                onExportCsv = {},
                onEmailReport = {}
            )
        }
    }
}