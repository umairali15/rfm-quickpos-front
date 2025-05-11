// app/src/main/java/com/rfm/quickpos/presentation/features/kiosk/KioskPaymentScreen.kt

package com.rfm.quickpos.presentation.features.kiosk

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.QuickPOSApplication
import com.rfm.quickpos.presentation.common.theme.PriceTextLarge
import kotlinx.coroutines.delay
import com.rfm.quickpos.presentation.features.payment.PaymentMethod

/**
 * Enhanced payment screen for kiosk mode with backend integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KioskPaymentScreen(
    onBackClick: () -> Unit,
    onPaymentComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get cart repository to get total amount
    val context = LocalContext.current
    val cartRepository = (context.applicationContext as QuickPOSApplication).cartRepository
    val cartTotal by cartRepository.cartTotal.collectAsState()

    // Calculate final amount with tax
    val subtotal = cartTotal
    val tax = subtotal * 0.05
    val totalAmount = subtotal + tax

    // State to track payment processing
    var isProcessing by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    // Simulate payment processing
    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            delay(3000) // Simulate payment processing time
            onPaymentComplete()
        }
    }

    // Animation for the payment methods
    val infiniteTransition = rememberInfiniteTransition(label = "payment-method-animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "scale"
    )

    // Auto-reset timer to attract screen after inactivity
    var inactivitySeconds by remember { mutableStateOf(0) }
    val maxInactivitySeconds = 180 // 3 minutes for payment

    LaunchedEffect(inactivitySeconds) {
        while (true) {
            delay(1000)
            inactivitySeconds++

            if (inactivitySeconds >= maxInactivitySeconds && !isProcessing) {
                // Navigate back to attract screen
                onBackClick()
                break
            }
        }
    }

    // Reset inactivity timer on user interaction
    fun resetInactivityTimer() {
        inactivitySeconds = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Payment",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!isProcessing) {
                                resetInactivityTimer()
                                onBackClick()
                            }
                        },
                        enabled = !isProcessing
                    ) {
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isProcessing) {
                // Payment processing view
                PaymentProcessingContent(selectedPaymentMethod)
            } else {
                // Payment method selection
                PaymentSelectionContent(
                    totalAmount = totalAmount,
                    selectedPaymentMethod = selectedPaymentMethod,
                    onPaymentMethodSelected = { method ->
                        resetInactivityTimer()
                        selectedPaymentMethod = method
                        isProcessing = true
                    },
                    modifier = Modifier.scale(scale)
                )
            }
        }
    }
}

/**
 * Payment selection content
 */
@Composable
private fun PaymentSelectionContent(
    totalAmount: Double,
    selectedPaymentMethod: PaymentMethod?,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Amount to pay
        Text(
            text = "Amount to Pay",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "AED ${String.format("%.2f", totalAmount)}",
            style = PriceTextLarge.copy(fontSize = PriceTextLarge.fontSize * 1.5),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Select payment method text
        Text(
            text = "Select Payment Method",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Card payment - main option for kiosk
        KioskPaymentMethodCard(
            title = "Card",
            subtitle = "Pay with credit or debit card",
            icon = Icons.Default.CreditCard,
            onClick = { onPaymentMethodSelected(PaymentMethod.CARD) },
            modifier = modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Digital wallet option
        KioskPaymentMethodCard(
            title = "Digital Wallet",
            subtitle = "Pay with Apple Pay, Google Pay, etc.",
            icon = Icons.Default.Payments,
            onClick = { onPaymentMethodSelected(PaymentMethod.CARD) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // QR code payment
        KioskPaymentMethodCard(
            title = "Scan QR Code",
            subtitle = "Use your phone to scan and pay",
            icon = Icons.Default.QrCode,
            onClick = { onPaymentMethodSelected(PaymentMethod.CARD) }
        )
    }
}

/**
 * Payment processing content
 */
@Composable
private fun PaymentProcessingContent(selectedPaymentMethod: PaymentMethod?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(80.dp),
            strokeWidth = 6.dp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Processing Payment...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (selectedPaymentMethod) {
                PaymentMethod.CARD -> "Please do not remove your card"
                PaymentMethod.CARD -> "Please complete payment on your device"
                PaymentMethod.CARD -> "Please scan the QR code to complete payment"
                else -> "Please wait while we process your payment"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Payment method card for kiosk mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KioskPaymentMethodCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Icon on the left
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Text content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = 20.sp
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun KioskPaymentScreenPreview() {
    com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme {
        Surface {
            KioskPaymentScreen(
                onBackClick = {},
                onPaymentComplete = {}
            )
        }
    }
}