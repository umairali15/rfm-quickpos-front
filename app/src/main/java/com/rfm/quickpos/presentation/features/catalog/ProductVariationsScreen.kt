// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/ProductVariationsScreen.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.data.remote.models.Item
import com.rfm.quickpos.data.remote.models.Variation
import com.rfm.quickpos.data.remote.models.VariationOption
import com.rfm.quickpos.presentation.common.components.RfmOutlinedButton
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmQuantitySelector
import com.rfm.quickpos.presentation.common.theme.PriceTextLarge
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * Screen for selecting product variations and modifiers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductVariationsScreen(
    item: Item,
    onBackClick: () -> Unit,
    onAddToCart: (ProductWithSelections) -> Unit,
    modifier: Modifier = Modifier
) {
    var quantity by remember { mutableIntStateOf(1) }

    // Track selected variations
    val selectedVariations = remember { mutableStateMapOf<String, VariationOption>() }

    // Track selected modifiers (if needed in the future)
    val selectedModifiers = remember { mutableStateListOf<String>() }

    // Calculate total price including variations
    val basePrice = item.price
    val variationsPriceAdjustment = selectedVariations.values.sumOf { it.priceAdjustment }
    val totalUnitPrice = basePrice + variationsPriceAdjustment
    val totalPrice = totalUnitPrice * quantity

    // Get variations from item settings
    val variations = item.settings?.variations ?: emptyList()

    // Check if all required variations are selected
    val allRequiredSelected = true // For now, all variations are considered required
    val canAddToCart = allRequiredSelected

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Customize Order",
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Product header
                item {
                    ProductHeader(
                        item = item,
                        basePrice = basePrice,
                        totalPrice = totalUnitPrice
                    )
                }

                // Quantity selector
                item {
                    QuantitySection(
                        quantity = quantity,
                        onQuantityChange = { quantity = it }
                    )
                }

                // Variations section
                if (variations.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        VariationsSection(
                            variations = variations,
                            selectedVariations = selectedVariations,
                            onVariationSelected = { variationName, option ->
                                selectedVariations[variationName] = option
                            }
                        )
                    }
                }

                // Price summary
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    PriceSummary(
                        basePrice = basePrice,
                        variations = selectedVariations.values.toList(),
                        quantity = quantity,
                        totalPrice = totalPrice
                    )
                }
            }

            // Bottom action bar
            Surface(
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                            val productWithSelections = ProductWithSelections(
                                item = item,
                                quantity = quantity,
                                selectedVariations = selectedVariations.toMap(),
                                selectedModifiers = selectedModifiers.toList(),
                                totalPrice = totalPrice
                            )
                            onAddToCart(productWithSelections)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = canAddToCart,
                        leadingIcon = Icons.Default.ShoppingCart
                    )
                }
            }
        }
    }
}

/**
 * Product header section
 */
@Composable
private fun ProductHeader(
    item: Item,
    basePrice: Double,
    totalPrice: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        item.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Base Price:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "AED ${String.format("%.2f", basePrice)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (totalPrice != basePrice) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )

            if (totalPrice != basePrice) {
                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "AED ${String.format("%.2f", totalPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Quantity selection section
 */
@Composable
private fun QuantitySection(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Quantity:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.width(16.dp))

        RfmQuantitySelector(
            quantity = quantity,
            onQuantityChange = onQuantityChange,
            minValue = 1,
            maxValue = 99
        )
    }
}

/**
 * Variations section
 */
@Composable
private fun VariationsSection(
    variations: List<Variation>,
    selectedVariations: Map<String, VariationOption>,
    onVariationSelected: (String, VariationOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Select Options",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        variations.forEach { variation ->
            VariationGroup(
                variation = variation,
                selectedOption = selectedVariations[variation.name],
                onOptionSelected = { option ->
                    onVariationSelected(variation.name, option)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Single variation group
 */
@Composable
private fun VariationGroup(
    variation: Variation,
    selectedOption: VariationOption?,
    onOptionSelected: (VariationOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = variation.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            variation.options.forEach { option ->
                VariationOptionChip(
                    option = option,
                    isSelected = selectedOption?.name == option.name,
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }
}

/**
 * Variation option chip
 */
@Composable
private fun VariationOptionChip(
    option: VariationOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        },
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (option.priceAdjustment != 0.0) {
                    Text(
                        text = if (option.priceAdjustment > 0) {
                            "+AED ${String.format("%.2f", option.priceAdjustment)}"
                        } else {
                            "-AED ${String.format("%.2f", -option.priceAdjustment)}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (option.priceAdjustment > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.posColors.success
                        }
                    )
                }
            }
        }
    }
}

/**
 * Price summary section
 */
@Composable
private fun PriceSummary(
    basePrice: Double,
    variations: List<VariationOption>,
    quantity: Int,
    totalPrice: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Price Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Base price
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Base Price",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "AED ${String.format("%.2f", basePrice)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Variations adjustments
        if (variations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))

            variations.forEach { variation ->
                if (variation.priceAdjustment != 0.0) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "• ${variation.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = if (variation.priceAdjustment > 0) {
                                "+AED ${String.format("%.2f", variation.priceAdjustment)}"
                            } else {
                                "-AED ${String.format("%.2f", -variation.priceAdjustment)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (variation.priceAdjustment > 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.posColors.success
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Divider()

        Spacer(modifier = Modifier.height(8.dp))

        // Quantity
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Quantity",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "$quantity",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Divider()

        Spacer(modifier = Modifier.height(12.dp))

        // Total
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
                style = PriceTextLarge.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Data class to represent a product with selected variations and modifiers
 */
data class ProductWithSelections(
    val item: Item,
    val quantity: Int,
    val selectedVariations: Map<String, VariationOption>,
    val selectedModifiers: List<String>,
    val totalPrice: Double
)