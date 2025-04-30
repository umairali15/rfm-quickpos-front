package com.rfm.quickpos.presentation.features.payment

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.components.RfmOutlinedButton
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmSecondaryButton
import com.rfm.quickpos.presentation.common.theme.PriceTextLarge
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * Payment success screen state
 */
data class PaymentSuccessState(
    val receiptNumber: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val customerName: String? = null,
    val cashReceived: Double? = null,
    val changeGiven: Double? = null,
    val receiptUrl: String? = null,
    val isPrinting: Boolean = false
)

/**
 * Payment success screen to show after a successful payment
 */
@Composable
fun PaymentSuccessScreen(
    state: PaymentSuccessState,
    onPrintReceiptClick: () -> Unit,
    onEmailReceiptClick: () -> Unit,
    onNewSaleClick: () -> Unit,
    onViewOrdersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation for the success circle
    val infiniteTransition = rememberInfiniteTransition(label = "success-pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Success icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.posColors.success.copy(alpha = 0.2f))
                    .padding(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.posColors.success)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Success message
            Text(
                text = "Payment Successful!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Receipt #${state.receiptNumber}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Amount
            Text(
                text = "AED ${String.format("%.2f", state.amount)}",
                style = PriceTextLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Payment method
            Text(
                text = "Paid via ${state.paymentMethod.name.lowercase().capitalize()}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Change given (for cash payments)
            if (state.paymentMethod == PaymentMethod.CASH && state.cashReceived != null && state.changeGiven != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cash received: AED ${String.format("%.2f", state.cashReceived)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Change: AED ${String.format("%.2f", state.changeGiven)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Customer name if available
            if (state.customerName != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Customer: ${state.customerName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Receipt actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Print receipt button
                RfmSecondaryButton(
                    text = if (state.isPrinting) "Printing..." else "Print Receipt",
                    onClick = onPrintReceiptClick,
                    leadingIcon = Icons.Default.Print,
                    enabled = !state.isPrinting,
                    modifier = Modifier.weight(1f)
                )

                // Email receipt button
                RfmSecondaryButton(
                    text = "Email Receipt",
                    onClick = onEmailReceiptClick,
                    leadingIcon = Icons.Default.Email,
                    enabled = !state.isPrinting,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // New sale button
            RfmPrimaryButton(
                text = "New Sale",
                onClick = onNewSaleClick,
                fullWidth = true,
                leadingIcon = Icons.Default.QrCode
            )

            Spacer(modifier = Modifier.height(16.dp))

            // View orders button
            RfmOutlinedButton(
                text = "View All Orders",
                onClick = onViewOrdersClick,
                fullWidth = true
            )
        }
    }
}

// Extension to capitalize the first letter of a string
private fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentSuccessScreenPreview() {
    val state = PaymentSuccessState(
        receiptNumber = "5917-1610-174122",
        amount = 800.00,
        paymentMethod = PaymentMethod.CARD,
        customerName = "John Smith"
    )

    RFMQuickPOSTheme {
        Surface {
            PaymentSuccessScreen(
                state = state,
                onPrintReceiptClick = {},
                onEmailReceiptClick = {},
                onNewSaleClick = {},
                onViewOrdersClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Cash Payment Success")
@Composable
fun CashPaymentSuccessScreenPreview() {
    val state = PaymentSuccessState(
        receiptNumber = "5917-1610-174122",
        amount = 800.00,
        paymentMethod = PaymentMethod.CASH,
        cashReceived = 1000.00,
        changeGiven = 200.00
    )

    RFMQuickPOSTheme {
        Surface {
            PaymentSuccessScreen(
                state = state,
                onPrintReceiptClick = {},
                onEmailReceiptClick = {},
                onNewSaleClick = {},
                onViewOrdersClick = {}
            )
        }
    }
}