// app/src/main/java/com/rfm/quickpos/presentation/features/shift/CloseShiftScreen.kt

package com.rfm.quickpos.presentation.features.shift

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.components.RfmOutlinedButton
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmSecondaryButton
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing shift summary for closing
 */
data class ShiftSummary(
    val openingBalance: Double,
    val cashSales: Double,
    val cashIn: Double,
    val cashOut: Double,
    val expectedCash: Double,
    val startTime: Date,
    val endTime: Date = Date()
)

/**
 * Screen for closing a shift
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseShiftScreen(
    shiftSummary: ShiftSummary,
    onBackClick: () -> Unit,
    onCloseShift: (closingAmount: Double, notes: String, printReceipt: Boolean, emailReceipt: Boolean) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var closingAmount by remember { mutableStateOf("0.00") }
    var closingAmountValue by remember { mutableDoubleStateOf(0.0) }
    var notes by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }
    var variance by remember { mutableDoubleStateOf(0.0) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var printReceipt by remember { mutableStateOf(true) }
    var emailReceipt by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Format dates for display
    val startTimeFormatted = remember(shiftSummary) {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(shiftSummary.startTime)
    }

    val endTimeFormatted = remember(shiftSummary) {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(shiftSummary.endTime)
    }

    // Calculate variance when closing amount changes
    LaunchedEffect(closingAmount) {
        closingAmountValue = closingAmount.toDoubleOrNull() ?: 0.0
        variance = closingAmountValue - shiftSummary.expectedCash
        isValid = closingAmountValue >= 0.0 && !isLoading
    }

    // Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Shift Close") },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to close this shift?" +
                                if (variance != 0.0) "\n\nThere is a cash variance of ${formatCurrency(variance)}." else ""
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        TextButton(
                            onClick = { printReceipt = !printReceipt },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Print Z-Report")
                                Text(if (printReceipt) "Yes" else "No")
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        TextButton(
                            onClick = { emailReceipt = !emailReceipt },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Email Z-Report")
                                Text(if (emailReceipt) "Yes" else "No")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onCloseShift(closingAmountValue, notes, printReceipt, emailReceipt)
                    }
                ) {
                    Text("Close Shift")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Close Shift",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // Shift info card
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "RFM Store",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "Shift Duration",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "From: $startTimeFormatted",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "To: $endTimeFormatted",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cash summary card
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Cash Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

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
                            text = formatCurrency(shiftSummary.openingBalance),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

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
                            text = formatCurrency(shiftSummary.cashSales),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

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
                            text = formatCurrency(shiftSummary.cashIn),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

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
                            text = "- ${formatCurrency(shiftSummary.cashOut)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Expected Cash",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = formatCurrency(shiftSummary.expectedCash),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Closing cash drawer amount field
            Text(
                text = "Actual Closing Cash",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = closingAmount,
                onValueChange = { input ->
                    // Format as currency
                    val filtered = input.replace(Regex("[^0-9.]"), "")
                    val parts = filtered.split(".")
                    if (parts.size <= 2 && (parts.size != 2 || parts[1].length <= 2)) {
                        closingAmount = filtered
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Variance section
            if (closingAmountValue > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                // Determine variance card color based on value
                val varianceColor = when {
                    variance > 0 -> MaterialTheme.posColors.successContainer
                    variance < 0 -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }

                val varianceTextColor = when {
                    variance > 0 -> MaterialTheme.posColors.success
                    variance < 0 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = varianceColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Cash Variance",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = formatCurrency(variance),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = varianceTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notes field
            Text(
                text = "Closing Notes (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Add any notes about this shift") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Report options
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                RfmSecondaryButton(
                    text = "Print Z-Report",
                    onClick = { printReceipt = !printReceipt },
                    leadingIcon = Icons.Default.Print,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                RfmOutlinedButton(
                    text = "Email Z-Report",
                    onClick = { emailReceipt = !emailReceipt },
                    leadingIcon = Icons.Default.Email,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Close shift button
            RfmPrimaryButton(
                text = if (isLoading) "Closing Shift..." else "Close Shift",
                onClick = {
                    showConfirmDialog = true
                },
                modifier = Modifier.padding(vertical = 16.dp),
                enabled = isValid && !isLoading,
                fullWidth = true,
                leadingIcon = Icons.Default.QrCode
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
fun CloseShiftScreenPreview() {
    val shiftSummary = ShiftSummary(
        openingBalance = 500.0,
        cashSales = 1234.56,
        cashIn = 200.0,
        cashOut = 300.0,
        expectedCash = 1634.56,
        startTime = Date(System.currentTimeMillis() - 28800000) // 8 hours ago
    )

    RFMQuickPOSTheme {
        Surface {
            CloseShiftScreen(
                shiftSummary = shiftSummary,
                onBackClick = {},
                onCloseShift = { _, _, _, _ -> }
            )
        }
    }
}