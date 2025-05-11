// app/src/main/java/com/rfm/quickpos/presentation/features/kiosk/KioskCartScreen.kt

package com.rfm.quickpos.presentation.features.kiosk

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.QuickPOSApplication
import com.rfm.quickpos.presentation.common.components.RfmDivider
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmQuantitySelector
import com.rfm.quickpos.presentation.common.theme.PriceTextLarge
import com.rfm.quickpos.presentation.common.theme.posColors
import com.rfm.quickpos.presentation.features.cart.CartItemWithModifiers
import kotlinx.coroutines.delay

/**
 * Enhanced Kiosk cart screen with backend integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KioskCartScreen(
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get repositories from application
    val context = LocalContext.current
    val cartRepository = (context.applicationContext as QuickPOSApplication).cartRepository

    // Collect cart state
    val cartItems by cartRepository.cartItems.collectAsState()
    val cartTotal by cartRepository.cartTotal.collectAsState()

    // Calculate totals
    val subtotal = cartItems.sumOf { it.totalPrice }
    val tax = subtotal * 0.05
    val total = subtotal + tax

    // Auto-reset timer to attract screen after inactivity
    var inactivitySeconds by remember { mutableStateOf(0) }
    val maxInactivitySeconds = 120 // 2 minutes of inactivity

    LaunchedEffect(inactivitySeconds) {
        while (true) {
            delay(1000)
            inactivitySeconds++

            if (inactivitySeconds >= maxInactivitySeconds) {
                // Return to attract screen in real app
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
                        text = "Your Order",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            resetInactivityTimer()
                            onBackClick()
                        }
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
        if (cartItems.isEmpty()) {
            // Empty cart state
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(96.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Your cart is empty",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Add items to your cart to begin your order",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    RfmPrimaryButton(
                        text = "Browse Menu",
                        onClick = {
                            resetInactivityTimer()
                            onBackClick()
                        },
                        leadingIcon = Icons.Default.QrCode
                    )
                }
            }
        } else {
            // Cart with items
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Cart items list (scrollable)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(cartItems) { item ->
                        KioskCartItemRow(
                            cartItem = item,
                            onQuantityChange = { newQuantity ->
                                resetInactivityTimer()
                                cartRepository.updateItemQuantity(item.id, newQuantity)
                            },
                            onRemove = {
                                resetInactivityTimer()
                                cartRepository.removeItem(item.id)
                            }
                        )

                        RfmDivider()
                    }
                }

                // Order summary and checkout button
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Order summary
                        Text(
                            text = "Order Summary",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Subtotal",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "AED ${String.format("%.2f", subtotal)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Tax (5%)",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "AED ${String.format("%.2f", tax)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(thickness = 2.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Text(
                                text = "AED ${String.format("%.2f", total)}",
                                style = PriceTextLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Checkout button
                        RfmPrimaryButton(
                            text = "Proceed to Payment",
                            onClick = {
                                resetInactivityTimer()
                                onCheckoutClick()
                            },
                            modifier = Modifier.height(64.dp),
                            fullWidth = true,
                            leadingIcon = Icons.Default.QrCode
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kiosk cart item row with variations display
 */
@Composable
private fun KioskCartItemRow(
    cartItem: CartItemWithModifiers,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main item row
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Quantity selector
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .align(Alignment.Top)
                ) {
                    RfmQuantitySelector(
                        quantity = cartItem.quantity,
                        onQuantityChange = onQuantityChange,
                        minValue = 1,
                        maxValue = 99
                    )
                }

                // Item info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = cartItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Unit Price: AED ${String.format("%.2f", cartItem.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Show variations if any
                    if (cartItem.variations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        cartItem.variations.forEach { (variationName, option) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$variationName: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = option.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                if (option.priceAdjustment != 0.0) {
                                    Text(
                                        text = " (${if (option.priceAdjustment > 0) "+" else ""}${String.format("%.2f", option.priceAdjustment)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (option.priceAdjustment > 0)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.posColors.success
                                    )
                                }
                            }
                        }
                    }

                    // Show notes if any
                    cartItem.notes?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                // Total price and remove button
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "AED ${String.format("%.2f", cartItem.totalPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}