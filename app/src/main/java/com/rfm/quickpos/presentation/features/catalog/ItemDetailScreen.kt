// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/ItemDetailScreen.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.components.RfmDivider
import com.rfm.quickpos.presentation.common.components.RfmOutlinedButton
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmQuantitySelector
import com.rfm.quickpos.presentation.common.theme.PriceTextLarge
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * Data class representing the modifiable properties of an item
 */
data class ItemModifications(
    val quantity: Int = 1,
    val priceOverride: Double? = null,
    val discountPercentage: Int? = null,
    val notes: String = ""
)

/**
 * Screen for viewing item details and applying modifications before adding to cart
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    product: Product,
    onClose: () -> Unit,
    onAddToCart: (product: Product, modifications: ItemModifications) -> Unit,
    userCanOverridePrice: Boolean = false,
    modifier: Modifier = Modifier
) {
    var quantity by remember { mutableIntStateOf(1) }
    var hasPriceOverride by remember { mutableStateOf(false) }
    var priceOverride by remember { mutableStateOf("") }
    var hasDiscount by remember { mutableStateOf(false) }
    var discountPercentage by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }

    // Calculate the effective price
    val effectivePrice = remember(product.price, hasPriceOverride, priceOverride, hasDiscount, discountPercentage) {
        val basePrice = if (hasPriceOverride) {
            priceOverride.toDoubleOrNull() ?: product.price
        } else {
            product.price
        }

        if (hasDiscount) {
            basePrice * (1 - discountPercentage / 100.0)
        } else {
            basePrice
        }
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Item Details",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
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
            // Item name and original price
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Display prices: original, discounted and/or overridden
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                if (hasPriceOverride || hasDiscount) {
                    // Show original price with strikethrough
                    Text(
                        text = "AED ${String.format("%.2f", product.price)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Effective price (with discounts applied)
                Text(
                    text = "AED ${String.format("%.2f", effectivePrice)}",
                    style = PriceTextLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Discount badge if applicable
                if (hasDiscount) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.posColors.discount)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "-$discountPercentage%",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Display SKU and barcode if available
            if (product.barcode != null) {
                Text(
                    text = "Barcode: ${product.barcode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            RfmDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Quantity selector
            Text(
                text = "Quantity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            RfmQuantitySelector(
                quantity = quantity,
                onQuantityChange = { quantity = it },
                minValue = 1,
                maxValue = 99,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            RfmDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Price override (only available for managers)
            if (userCanOverridePrice) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Price Override",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(
                        onClick = { hasPriceOverride = !hasPriceOverride }
                    ) {
                        Icon(
                            imageVector = if (hasPriceOverride) Icons.Default.Check else Icons.Default.Clear,
                            contentDescription = if (hasPriceOverride) "Enabled" else "Disabled",
                            tint = if (hasPriceOverride) MaterialTheme.posColors.success else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AnimatedVisibility(
                    visible = hasPriceOverride,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutlinedTextField(
                        value = priceOverride,
                        onValueChange = { input ->
                            // Format as currency
                            val filtered = input.replace(Regex("[^0-9.]"), "")
                            val parts = filtered.split(".")
                            if (parts.size <= 2 && (parts.size != 2 || parts[1].length <= 2)) {
                                priceOverride = filtered
                            }
                        },
                        label = { Text("Override Price") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null
                            )
                        },
                        placeholder = { Text("Enter new price") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                RfmDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Discount section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Discount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = { hasDiscount = !hasDiscount }
                ) {
                    Icon(
                        imageVector = if (hasDiscount) Icons.Default.Check else Icons.Default.Clear,
                        contentDescription = if (hasDiscount) "Enabled" else "Disabled",
                        tint = if (hasDiscount) MaterialTheme.posColors.success else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = hasDiscount,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Discount percentage: $discountPercentage%",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Discount badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.posColors.discount)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "-$discountPercentage%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Discount percentage slider
                    Slider(
                        value = discountPercentage.toFloat(),
                        onValueChange = { discountPercentage = it.toInt() },
                        valueRange = 0f..50f,
                        steps = 50,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick discount buttons
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(5, 10, 15, 20, 25).forEach { discount ->
                            Button(
                                onClick = { discountPercentage = discount },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (discountPercentage == discount)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (discountPercentage == discount)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "$discount%",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            if (discount != 25) {
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }

                    // Display effective price after discount
                    if (discountPercentage > 0) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val basePrice = if (hasPriceOverride && priceOverride.isNotEmpty()) {
                                priceOverride.toDoubleOrNull() ?: product.price
                            } else {
                                product.price
                            }

                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = MaterialTheme.posColors.discount
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Discount Amount: AED ${String.format("%.2f", basePrice * discountPercentage / 100.0)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.posColors.discount
                            )
                        }
                    }
                }
            }

            RfmDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Notes section
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Add special instructions") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Notes,
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

            // Total price calculation
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total: AED ${String.format("%.2f", effectivePrice * quantity)}",
                    style = PriceTextLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End
                )

                if (quantity > 1) {
                    Text(
                        text = "(${quantity} × AED ${String.format("%.2f", effectivePrice)})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                RfmOutlinedButton(
                    text = "Cancel",
                    onClick = onClose,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                RfmPrimaryButton(
                    text = "Add to Cart",
                    onClick = {
                        val priceOverrideValue = if (hasPriceOverride && priceOverride.isNotEmpty()) {
                            priceOverride.toDoubleOrNull()
                        } else {
                            null
                        }

                        val discountValue = if (hasDiscount && discountPercentage > 0) {
                            discountPercentage
                        } else {
                            null
                        }

                        onAddToCart(
                            product,
                            ItemModifications(
                                quantity = quantity,
                                priceOverride = priceOverrideValue,
                                discountPercentage = discountValue,
                                notes = notes
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.QrCode
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemDetailScreenPreview() {
    val sampleProduct = Product(
        id = "1",
        name = "Coffee",
        price = 15.00,
        categoryId = "1",
        barcode = "5901234123457"
    )

    RFMQuickPOSTheme {
        Surface {
            ItemDetailScreen(
                product = sampleProduct,
                onClose = {},
                onAddToCart = { _, _ -> },
                userCanOverridePrice = true
            )
        }
    }
}