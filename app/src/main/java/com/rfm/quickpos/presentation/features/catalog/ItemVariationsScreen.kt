// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/ItemVariationsScreen.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.data.remote.models.Item
import com.rfm.quickpos.data.remote.models.Variation
import com.rfm.quickpos.data.remote.models.VariationOption
import com.rfm.quickpos.presentation.common.components.RfmOutlinedButton
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmQuantitySelector
import com.rfm.quickpos.presentation.common.theme.PriceTextLarge
import com.rfm.quickpos.presentation.common.theme.posColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemVariationsScreen(
    item: Item,
    onBackClick: () -> Unit,
    onAddToCart: (Item, Int, Map<String, VariationOption>, Double) -> Unit,
    userCanOverridePrice: Boolean = false,
    modifier: Modifier = Modifier
) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedVariations by remember { mutableStateOf<Map<String, VariationOption>>(emptyMap()) }
    var priceOverride by remember { mutableStateOf<String?>(null) }
    var showPriceOverride by remember { mutableStateOf(false) }

    // Extract variations from item settings
    val variations = item.settings?.variations ?: emptyList()

    // Calculate total price with variations
    val basePrice = item.price
    val variationPriceAdjustment = selectedVariations.values.sumOf { it.priceAdjustment }
    val overriddenPrice = priceOverride?.toDoubleOrNull()
    val effectivePrice = overriddenPrice ?: (basePrice + variationPriceAdjustment)
    val totalPrice = effectivePrice * quantity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Options") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Item Details
            item {
                ItemDetailsCard(item, basePrice, effectivePrice)
            }

            // Quantity Selector
            item {
                QuantitySection(quantity) { quantity = it }
            }

            // Variations
            if (variations.isNotEmpty()) {
                item {
                    Text(
                        text = "Options",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(variations) { variation ->
                    VariationSelector(
                        variation = variation,
                        selectedOption = selectedVariations[variation.name],
                        onOptionSelected = { option ->
                            selectedVariations = selectedVariations.toMutableMap().apply {
                                this[variation.name] = option
                            }
                        }
                    )
                }
            }

            // Price Override (for managers)
            if (userCanOverridePrice) {
                item {
                    PriceOverrideSection(
                        showPriceOverride = showPriceOverride,
                        priceOverride = priceOverride,
                        onTogglePriceOverride = { showPriceOverride = !showPriceOverride },
                        onPriceOverrideChange = { priceOverride = it }
                    )
                }
            }

            // Total and Actions
            item {
                Spacer(modifier = Modifier.height(8.dp))

                PriceSummary(
                    basePrice = basePrice,
                    variationAdjustment = variationPriceAdjustment,
                    quantity = quantity,
                    totalPrice = totalPrice,
                    isOverridden = overriddenPrice != null
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RfmOutlinedButton(
                        text = "Cancel",
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f)
                    )

                    RfmPrimaryButton(
                        text = "Add to Cart",
                        onClick = {
                            onAddToCart(item, quantity, selectedVariations, totalPrice)
                        },
                        leadingIcon = Icons.Default.ShoppingCart,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ItemDetailsCard(
    item: Item,
    basePrice: Double,
    effectivePrice: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            item.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Base Price: ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "AED ${String.format("%.2f", basePrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (effectivePrice != basePrice) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )

                if (effectivePrice != basePrice) {
                    Text(
                        text = " → ",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "AED ${String.format("%.2f", effectivePrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun QuantitySection(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Quantity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.width(16.dp))

        RfmQuantitySelector(
            quantity = quantity,
            onQuantityChange = onQuantityChange
        )
    }
}

@Composable
private fun VariationSelector(
    variation: Variation,
    selectedOption: VariationOption?,
    onOptionSelected: (VariationOption) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = variation.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                variation.options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (selectedOption?.name == option.name),
                                onClick = { onOptionSelected(option) }
                            )
                            .border(
                                width = if (selectedOption?.name == option.name) 2.dp else 1.dp,
                                color = if (selectedOption?.name == option.name) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        RadioButton(
                            selected = (selectedOption?.name == option.name),
                            onClick = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedOption?.name == option.name) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                }
                            )

                            if (option.priceAdjustment != 0.0) {
                                Text(
                                    text = if (option.priceAdjustment > 0) {
                                        "+AED ${String.format("%.2f", option.priceAdjustment)}"
                                    } else {
                                        "-AED ${String.format("%.2f", -option.priceAdjustment)}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (option.priceAdjustment > 0) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.posColors.success
                                    }
                                )
                            }
                        }

                        if (selectedOption?.name == option.name) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceOverrideSection(
    showPriceOverride: Boolean,
    priceOverride: String?,
    onTogglePriceOverride: () -> Unit,
    onPriceOverrideChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Price Override",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = showPriceOverride,
                    onCheckedChange = { onTogglePriceOverride() }
                )
            }

            if (showPriceOverride) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = priceOverride ?: "",
                    onValueChange = onPriceOverrideChange,
                    label = { Text("New Price") },
                    prefix = { Text("AED ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PriceSummary(
    basePrice: Double,
    variationAdjustment: Double,
    quantity: Int,
    totalPrice: Double,
    isOverridden: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!isOverridden && variationAdjustment != 0.0) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Base Price")
                    Text(text = "AED ${String.format("%.2f", basePrice)}")
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Options")
                    Text(
                        text = if (variationAdjustment > 0) {
                            "+AED ${String.format("%.2f", variationAdjustment)}"
                        } else {
                            "-AED ${String.format("%.2f", -variationAdjustment)}"
                        },
                        color = if (variationAdjustment > 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.posColors.success
                        }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Price per item")
                Text(text = "AED ${String.format("%.2f", totalPrice / quantity)}")
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Quantity")
                Text(text = quantity.toString())
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AED ${String.format("%.2f", totalPrice)}",
                    style = PriceTextLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}