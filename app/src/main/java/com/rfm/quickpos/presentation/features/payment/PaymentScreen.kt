package com.rfm.quickpos.presentation.features.payment

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.components.PaymentMethodCard
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmTextField
import com.rfm.quickpos.presentation.common.theme.PriceTextLarge
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme

/**
 * Enum representing payment methods
 */
enum class PaymentMethod {
    CARD,
    CASH,
    SPLIT
}

/**
 * Split payment data class
 */
data class SplitPaymentData(
    val cardAmount: Double = 0.0,
    val cashAmount: Double = 0.0
)

/**
 * Payment screen state
 */
data class PaymentState(
    val saleNumber: String,
    val totalAmount: Double,
    val selectedPaymentMethod: PaymentMethod? = null,
    val splitPaymentData: SplitPaymentData = SplitPaymentData(),
    val isProcessing: Boolean = false,
    val cashReceived: Double? = null,
    val changeDue: Double? = null,
    val error: String? = null
)

/**
 * Payment screen to select payment methods and process payment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    state: PaymentState,
    onBackClick: () -> Unit,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    onProcessPayment: () -> Unit,
    onSplitAmountChange: (cardAmount: Double, cashAmount: Double) -> Unit,
    onCashReceivedChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Payment",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Sale Number: ${state.saleNumber}",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Amount to pay
            Text(
                text = "Amount",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AED ${String.format("%.2f", state.totalAmount)}",
                style = PriceTextLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Select payment method text
            Text(
                text = "Select a Payment Method",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Payment methods
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Card payment
                PaymentMethodCard(
                    methodName = "Card",
                    icon = Icons.Default.CreditCard,
                    isSelected = state.selectedPaymentMethod == PaymentMethod.CARD,
                    onClick = { onPaymentMethodSelected(PaymentMethod.CARD) },
                    modifier = Modifier.weight(1f)
                )

                // Cash payment
                PaymentMethodCard(
                    methodName = "Cash",
                    icon = Icons.Default.Money,
                    isSelected = state.selectedPaymentMethod == PaymentMethod.CASH,
                    onClick = { onPaymentMethodSelected(PaymentMethod.CASH) },
                    modifier = Modifier.weight(1f)
                )

                // Split payment
                PaymentMethodCard(
                    methodName = "Split payment",
                    icon = Icons.Default.Payments,
                    isSelected = state.selectedPaymentMethod == PaymentMethod.SPLIT,
                    onClick = { onPaymentMethodSelected(PaymentMethod.SPLIT) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Additional fields based on payment method
            when (state.selectedPaymentMethod) {
                PaymentMethod.CARD -> {
                    // Card payment - show SmartPay integration message
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Ready to process card payment via SmartPay",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                PaymentMethod.CASH -> {
                    // Cash payment - ask for cash received to calculate change
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cash Received",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        RfmTextField(
                            value = state.cashReceived?.toString() ?: "",
                            onValueChange = { text ->
                                text.toDoubleOrNull()?.let { onCashReceivedChange(it) }
                            },
                            placeholder = "Enter amount received",
                            leadingIcon = {
                                Text(
                                    text = "AED",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (state.cashReceived != null && state.cashReceived >= state.totalAmount) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Change Due:",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "AED ${String.format("%.2f", state.cashReceived - state.totalAmount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                PaymentMethod.SPLIT -> {
                    // Split payment - allow entering card and cash amounts
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        var cardAmount by remember {
                            mutableStateOf(state.splitPaymentData.cardAmount.toString())
                        }
                        var cashAmount by remember {
                            mutableStateOf(state.splitPaymentData.cashAmount.toString())
                        }

                        Text(
                            text = "Card Amount",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        RfmTextField(
                            value = cardAmount,
                            onValueChange = { text ->
                                cardAmount = text
                                val card = text.toDoubleOrNull() ?: 0.0
                                val cash = cashAmount.toDoubleOrNull() ?: 0.0
                                onSplitAmountChange(card, cash)
                            },
                            placeholder = "Enter card amount",
                            leadingIcon = {
                                Text(
                                    text = "AED",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Cash Amount",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        RfmTextField(
                            value = cashAmount,
                            onValueChange = { text ->
                                cashAmount = text
                                val cash = text.toDoubleOrNull() ?: 0.0
                                val card = cardAmount.toDoubleOrNull() ?: 0.0
                                onSplitAmountChange(card, cash)
                            },
                            placeholder = "Enter cash amount",
                            leadingIcon = {
                                Text(
                                    text = "AED",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Show total of split payment vs total amount
                        Spacer(modifier = Modifier.height(16.dp))

                        val cardValue = cardAmount.toDoubleOrNull() ?: 0.0
                        val cashValue = cashAmount.toDoubleOrNull() ?: 0.0
                        val splitTotal = cardValue + cashValue
                        val difference = state.totalAmount - splitTotal

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total Split Amount:",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "AED ${String.format("%.2f", splitTotal)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (difference > 0) "Remaining:" else if (difference < 0) "Overpaid:" else "Exact Amount",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "AED ${String.format("%.2f", kotlin.math.abs(difference))}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    difference > 0 -> MaterialTheme.colorScheme.error
                                    difference < 0 -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                }

                else -> {
                    // No payment method selected yet
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Please select a payment method to continue",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Error message if any
            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Process payment button
            val canProcessPayment = when (state.selectedPaymentMethod) {
                PaymentMethod.CARD -> true
                PaymentMethod.CASH -> state.cashReceived != null && state.cashReceived >= state.totalAmount
                PaymentMethod.SPLIT -> {
                    val cardValue = state.splitPaymentData.cardAmount
                    val cashValue = state.splitPaymentData.cashAmount
                    val splitTotal = cardValue + cashValue
                    splitTotal >= state.totalAmount
                }
                else -> false
            }

            RfmPrimaryButton(
                text = "Pay Now",
                onClick = onProcessPayment,
                fullWidth = true,
                enabled = canProcessPayment && !state.isProcessing
            )

            if (state.isProcessing) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Processing payment...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    val state = PaymentState(
        saleNumber = "5917-1610-174122",
        totalAmount = 800.00,
        selectedPaymentMethod = PaymentMethod.CARD
    )

    RFMQuickPOSTheme {
        Surface {
            PaymentScreen(
                state = state,
                onBackClick = {},
                onPaymentMethodSelected = {},
                onProcessPayment = {},
                onSplitAmountChange = { _, _ -> },
                onCashReceivedChange = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Cash Payment")
@Composable
fun CashPaymentScreenPreview() {
    val state = PaymentState(
        saleNumber = "5917-1610-174122",
        totalAmount = 800.00,
        selectedPaymentMethod = PaymentMethod.CASH,
        cashReceived = 1000.00
    )

    RFMQuickPOSTheme {
        Surface {
            PaymentScreen(
                state = state,
                onBackClick = {},
                onPaymentMethodSelected = {},
                onProcessPayment = {},
                onSplitAmountChange = { _, _ -> },
                onCashReceivedChange = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Split Payment")
@Composable
fun SplitPaymentScreenPreview() {
    val state = PaymentState(
        saleNumber = "5917-1610-174122",
        totalAmount = 800.00,
        selectedPaymentMethod = PaymentMethod.SPLIT,
        splitPaymentData = SplitPaymentData(cardAmount = 500.00, cashAmount = 300.00)
    )

    RFMQuickPOSTheme {
        Surface {
            PaymentScreen(
                state = state,
                onBackClick = {},
                onPaymentMethodSelected = {},
                onProcessPayment = {},
                onSplitAmountChange = { _, _ -> },
                onCashReceivedChange = {}
            )
        }
    }
}