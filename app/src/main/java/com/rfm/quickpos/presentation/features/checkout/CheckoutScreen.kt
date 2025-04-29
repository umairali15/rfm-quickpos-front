package com.rfm.quickpos.presentation.features.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.components.CartItemCard
import com.rfm.quickpos.presentation.common.components.RfmPayButton
import com.rfm.quickpos.presentation.common.theme.DiscountTagShape
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors
import com.rfm.quickpos.presentation.features.payment.PaymentMethodOverlay
import java.text.NumberFormat
import java.util.Locale

/**
 * Data class for cart items
 */
data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Double,
    val unitLabel: String = "x 1 pc",
    val discountAmount: Double? = null,
    val discountPercent: Double? = null
)

/**
 * Data class for cart summary
 */
data class CartSummary(
    val subtotal: Double,
    val taxAmount: Double,
    val discountAmount: Double = 0.0,
    val total: Double,
    val currencyCode: String = "AED"
)

/**
 * Checkout Screen
 */
@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    cartSummary: CartSummary,
    onPayClicked: (Double) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    discountAppliedMessage: String? = null
) {
    var showPaymentMethods by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Surface(
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Total row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = formatCurrency(cartSummary.total, cartSummary.currencyCode),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pay button
                        RfmPayButton(
                            amount = formatNumber(cartSummary.total),
                            currencyCode = cartSummary.currencyCode,
                            onClick = {
                                showPaymentMethods = true
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Success message on discount applied
                AnimatedVisibility(
                    visible = discountAppliedMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    discountAppliedMessage?.let { message ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Number of items
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Items: ${cartItems.size}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Cart items list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(cartItems) { item ->
                        CartItemCard(
                            name = item.name,
                            price = formatNumber(item.price),
                            quantity = formatQuantity(item.quantity, item.unitLabel),
                            currencyCode = cartSummary.currencyCode,
                            discountTag = item.discountAmount?.let {
                                "-${cartSummary.currencyCode} ${formatNumber(it)}"
                            } ?: item.discountPercent?.let {
                                "-${it.toInt()}%"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    // Summary section
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Divider()

                            Spacer(modifier = Modifier.height(16.dp))

                            // VAT row
                            if (cartSummary.taxAmount > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "VAT",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Text(
                                        text = formatCurrency(cartSummary.taxAmount, cartSummary.currencyCode),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Discount row
                            if (cartSummary.discountAmount > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Discount",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Text(
                                        text = "-${formatCurrency(cartSummary.discountAmount, cartSummary.currencyCode)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.posColors.discount
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Subtotal row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Subtotal",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    text = formatCurrency(cartSummary.subtotal, cartSummary.currencyCode),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Payment method overlay
        PaymentMethodOverlay(
            visible = showPaymentMethods,
            amount = cartSummary.total,
            currencyCode = cartSummary.currencyCode,
            onPaymentMethodSelected = { method ->
                showPaymentMethods = false
                onPayClicked(cartSummary.total)
            },
            onDismiss = {
                showPaymentMethods = false
            }
        )
    }
}

// Utility functions for formatting
private fun formatCurrency(amount: Double, currencyCode: String): String {
    return "$currencyCode ${formatNumber(amount)}"
}

private fun formatNumber(amount: Double): String {
    return if (amount == amount.toInt().toDouble()) {
        amount.toInt().toString()
    } else {
        String.format("%.2f", amount)
    }
}

private fun formatQuantity(quantity: Double, unitLabel: String): String {
    return if (quantity == quantity.toInt().toDouble()) {
        "${quantity.toInt()} $unitLabel"
    } else {
        String.format("%.1f", quantity) + " $unitLabel"
    }
}

@Preview(showBackground = true)
@Composable
fun CheckoutScreenPreview() {
    // Sample data
    val items = listOf(
        CartItem(
            id = "1",
            name = "\"Paris\" set",
            price = 12.0,
            quantity = 12.0,
            unitLabel = "x 1 pc"
        ),
        CartItem(
            id = "2",
            name = "Liquid soap (pieces)",
            price = 220.0,
            quantity = 16.0,
            unitLabel = "x 1 pc",
            discountAmount = 100.0
        ),
        CartItem(
            id = "3",
            name = "Nivea Men shaving foam with skin restoration effect",
            price = 3.591,
            quantity = 3.0,
            unitLabel = "x 10 pc",
            discountPercent = 5.0
        )
    )

    val summary = CartSummary(
        subtotal = 4087.90,
        taxAmount = 12.0,
        discountAmount = 289.0,
        total = 4087.90
    )

    RFMQuickPOSTheme {
        CheckoutScreen(
            cartItems = items,
            cartSummary = summary,
            onPayClicked = {},
            onBackClicked = {},
            discountAppliedMessage = "Item discount added"
        )
    }
}