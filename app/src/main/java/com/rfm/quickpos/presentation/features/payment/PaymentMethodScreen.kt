package com.rfm.quickpos.presentation.features.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rfm.quickpos.presentation.common.components.PaymentMethodCard
import com.rfm.quickpos.presentation.common.components.RfmOutlinedButton
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.ModalShape
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * Payment Method Selection Dialog
 */
@Composable
fun PaymentMethodDialog(
    amount: Double,
    currencyCode: String = "AED",
    onPaymentMethodSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMethod by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(ModalShape),
            color = MaterialTheme.colorScheme.surface,
            shape = ModalShape
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Amount Header
                Text(
                    text = "$currencyCode ${amount.toInt()}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Payment Method Options
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PaymentMethodCard(
                        title = "Cash",
                        icon = Icons.Filled.Money,
                        onClick = { selectedMethod = "cash" },
                        iconTint = MaterialTheme.posColors.cashIcon,
                        selected = selectedMethod == "cash",
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    PaymentMethodCard(
                        title = "Card",
                        icon = Icons.Filled.CreditCard,
                        onClick = { selectedMethod = "card" },
                        iconTint = MaterialTheme.posColors.cardIcon,
                        selected = selectedMethod == "card",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel Button
                RfmOutlinedButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    fullWidth = true
                )
            }
        }
    }
}

/**
 * Full-screen overlay version with animated entry/exit
 */
@Composable
fun PaymentMethodOverlay(
    visible: Boolean,
    amount: Double,
    currencyCode: String = "AED",
    onPaymentMethodSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInVertically(animationSpec = tween(300)) { it / 2 },
        exit = fadeOut(animationSpec = tween(300)) +
                slideOutVertically(animationSpec = tween(300)) { it / 2 }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(ModalShape),
                color = MaterialTheme.colorScheme.surface,
                shape = ModalShape
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Amount Header
                    Text(
                        text = "$currencyCode ${amount.toInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Select Payment Method",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Payment Method Options
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PaymentMethodCard(
                            title = "Cash",
                            icon = Icons.Filled.Money,
                            onClick = { onPaymentMethodSelected("cash") },
                            iconTint = MaterialTheme.posColors.cashIcon,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        PaymentMethodCard(
                            title = "Card",
                            icon = Icons.Filled.CreditCard,
                            onClick = { onPaymentMethodSelected("card") },
                            iconTint = MaterialTheme.posColors.cardIcon,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Cancel Button
                    RfmOutlinedButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        fullWidth = true
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentMethodPreview() {
    RFMQuickPOSTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
        ) {
            PaymentMethodDialog(
                amount = 244.0,
                currencyCode = "AED",
                onPaymentMethodSelected = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentMethodOverlayPreview() {
    RFMQuickPOSTheme {
        PaymentMethodOverlay(
            visible = true,
            amount = 244.0,
            currencyCode = "AED",
            onPaymentMethodSelected = {},
            onDismiss = {}
        )
    }
}