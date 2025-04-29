package com.rfm.quickpos.presentation.features.cart

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.rfm.quickpos.presentation.common.components.RfmDivider
import com.rfm.quickpos.presentation.common.components.RfmOutlinedButton
import com.rfm.quickpos.presentation.common.components.RfmPayButton
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmQuantitySelector
import com.rfm.quickpos.presentation.common.components.RfmSecondaryButton
import com.rfm.quickpos.presentation.common.theme.PriceTextLarge
import com.rfm.quickpos.presentation.common.theme.PriceTextMedium
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.TextFieldShape
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * Cart item data class
 */
data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val discountPercentage: Int? = null,
    val notes: String? = null
)

/**
 * Cart screen state
 */
data class CartState(
    val cartItems: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val discountCode: String = "",
    val isLoading: Boolean = false,
    val customerName: String? = null,
    val error: String? = null,
    val note: String = ""
)

/**
 * Cart screen for reviewing items before checkout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    state: CartState,
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onItemQuantityChange: (CartItem, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    onDiscountCodeChange: (String) -> Unit,
    onApplyDiscountClick: () -> Unit,
    onNoteChange: (String) -> Unit,
    onAddCustomerClick: () -> Unit,
    onClearCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cart",
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
                    if (state.cartItems.isNotEmpty()) {
                        IconButton(onClick = onClearCart) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Cart"
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
        if (state.cartItems.isEmpty()) {
            // Empty cart state
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your cart is empty",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add items to your cart to begin a sale",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    RfmPrimaryButton(
                        text = "Continue Shopping",
                        onClick = onBackClick
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
                    items(state.cartItems) { item ->
                        CartItemRow(
                            cartItem = item,
                            onQuantityChange = { newQuantity ->
                                onItemQuantityChange(item, newQuantity)
                            },
                            onRemove = { onRemoveItem(item) }
                        )

                        RfmDivider()
                    }

                    // Additional options
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Discount code
                            Text(
                                text = "Discount",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Use standard TextField instead of RfmTextField
                                TextField(
                                    value = state.discountCode,
                                    onValueChange = onDiscountCodeChange,
                                    placeholder = { Text("Enter discount code") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocalOffer,
                                            contentDescription = null
                                        )
                                    },
                                    colors = TextFieldDefaults.textFieldColors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
                                    ),
                                    shape = TextFieldShape,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                RfmOutlinedButton(
                                    text = "Apply",
                                    onClick = onApplyDiscountClick,
                                    enabled = state.discountCode.isNotEmpty()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Add customer
                            RfmSecondaryButton(
                                text = if (state.customerName != null)
                                    "Customer: ${state.customerName}"
                                else
                                    "Add Customer",
                                onClick = onAddCustomerClick,
                                leadingIcon = Icons.Default.Person,
                                fullWidth = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Note
                            Text(
                                text = "Note",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Use standard TextField instead of RfmTextField
                            TextField(
                                value = state.note,
                                onValueChange = onNoteChange,
                                placeholder = { Text("Add a note to this order") },
                                maxLines = 3,
                                colors = TextFieldDefaults.textFieldColors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                shape = TextFieldShape,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Order summary and checkout button
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Order summary
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Subtotal",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "AED ${String.format("%.2f", state.subtotal)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (state.discount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Discount",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.posColors.discount
                                )

                                Text(
                                    text = "-AED ${String.format("%.2f", state.discount)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.posColors.discount
                                )
                            }
                        }

                        if (state.tax > 0) {
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Tax",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = "AED ${String.format("%.2f", state.tax)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleLarge
                            )

                            Text(
                                text = "AED ${String.format("%.2f", state.total)}",
                                style = PriceTextMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Checkout button
                        RfmPrimaryButton(
                            text = "Proceed to Payment",
                            onClick = onCheckoutClick,
                            fullWidth = true
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cart item row component
 */
@Composable
fun CartItemRow(
    cartItem: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Item info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = cartItem.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AED ${String.format("%.2f", cartItem.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (cartItem.discountPercentage != null)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                if (cartItem.discountPercentage != null) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "-${cartItem.discountPercentage}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.posColors.discount
                    )
                }
            }

            if (cartItem.notes != null) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = cartItem.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Quantity selector
        RfmQuantitySelector(
            quantity = cartItem.quantity,
            onQuantityChange = onQuantityChange
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Remove button
        IconButton(
            onClick = onRemove
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    val sampleItems = listOf(
        CartItem(
            id = "1",
            name = "Coffee",
            price = 15.00,
            quantity = 2
        ),
        CartItem(
            id = "2",
            name = "Croissant",
            price = 10.00,
            quantity = 1,
            discountPercentage = 10
        ),
        CartItem(
            id = "3",
            name = "Water Bottle",
            price = 5.00,
            quantity = 3,
            notes = "Cold"
        )
    )

    val subtotal = sampleItems.sumOf { it.price * it.quantity }
    val discount = 5.0
    val tax = subtotal * 0.05
    val total = subtotal - discount + tax

    val state = CartState(
        cartItems = sampleItems,
        subtotal = subtotal,
        discount = discount,
        tax = tax,
        total = total,
        discountCode = "SUMMER10"
    )

    RFMQuickPOSTheme {
        Surface {
            CartScreen(
                state = state,
                onBackClick = {},
                onCheckoutClick = {},
                onItemQuantityChange = { _, _ -> },
                onRemoveItem = {},
                onDiscountCodeChange = {},
                onApplyDiscountClick = {},
                onNoteChange = {},
                onAddCustomerClick = {},
                onClearCart = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty Cart")
@Composable
fun EmptyCartScreenPreview() {
    RFMQuickPOSTheme {
        Surface {
            CartScreen(
                state = CartState(),
                onBackClick = {},
                onCheckoutClick = {},
                onItemQuantityChange = { _, _ -> },
                onRemoveItem = {},
                onDiscountCodeChange = {},
                onApplyDiscountClick = {},
                onNoteChange = {},
                onAddCustomerClick = {},
                onClearCart = {}
            )
        }
    }
}