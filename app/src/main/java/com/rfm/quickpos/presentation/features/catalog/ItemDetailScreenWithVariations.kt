// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/ItemDetailScreenWithVariations.kt

package com.rfm.quickpos.presentation.features.catalog

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.rfm.quickpos.data.remote.models.BusinessTypeConfig
import com.rfm.quickpos.data.remote.models.Item
import com.rfm.quickpos.data.remote.models.Variation
import com.rfm.quickpos.data.remote.models.VariationOption
import com.rfm.quickpos.data.repository.CatalogRepository
import com.rfm.quickpos.presentation.common.components.*
import com.rfm.quickpos.presentation.common.theme.PriceTextLarge
import com.rfm.quickpos.presentation.common.theme.posColors
import com.rfm.quickpos.presentation.features.cart.CartItemWithModifiers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreenWithVariations(
    itemId: String,
    catalogRepository: CatalogRepository,
    onClose: () -> Unit,
    onAddToCart: (CartItemWithModifiers) -> Unit,
    modifier: Modifier = Modifier
) {
    // Get data from repositories
    val items by catalogRepository.items.collectAsState()
    val businessTypeConfig by catalogRepository.businessTypeConfig.collectAsState()

    // Find the selected item
    val item = items.find { it.id == itemId }
    if (item == null) {
        LaunchedEffect(Unit) {
            Log.d("ItemDetail", "Item not found: $itemId")
            onClose()
        }
        return
    }

    // Debug logging
    LaunchedEffect(item) {
        Log.d("ItemDetail", "Item: ${item.name}")
        Log.d("ItemDetail", "Settings: ${item.settings}")
        Log.d("ItemDetail", "Inventory: ${item.settings?.inventory}")
        Log.d("ItemDetail", "Variations: ${item.settings?.inventory?.variations}")
        Log.d("ItemDetail", "Has Variations: ${item.settings?.inventory?.hasVariations}")
    }

    // Get variations from settings.inventory.variations
    val variations = remember(item) {
        val v = item.settings?.inventory?.variations ?: emptyList()
        Log.d("ItemDetail", "Variations count: ${v.size}")
        v.forEach { variation ->
            Log.d("ItemDetail", "Variation: ${variation.name}, Options: ${variation.options.size}")
        }
        v
    }
    val hasVariations = variations.isNotEmpty()

    // Local state
    var quantity by remember { mutableIntStateOf(1) }
    val selectedVariations = remember { mutableStateMapOf<String, VariationOption>() }

    // Initialize with first option of each variation if available
    LaunchedEffect(variations) {
        variations.forEach { variation ->
            if (variation.options.isNotEmpty() && !selectedVariations.containsKey(variation.name)) {
                selectedVariations[variation.name] = variation.options.first()
                Log.d("ItemDetail", "Initialized variation '${variation.name}' with '${variation.options.first().name}'")
            }
        }
    }

    // Calculate total price with variations
    val basePrice = item.price
    val variationsPriceAdjustment = selectedVariations.values.sumOf { it.priceAdjustment }
    val totalUnitPrice = basePrice + variationsPriceAdjustment
    val totalPrice = totalUnitPrice * quantity

    // Debug price calculation
    LaunchedEffect(selectedVariations.values) {
        Log.d("ItemDetail", "Base price: $basePrice")
        Log.d("ItemDetail", "Variations adjustment: $variationsPriceAdjustment")
        Log.d("ItemDetail", "Total unit price: $totalUnitPrice")
    }

    // Check if all required variations are selected
    val allRequiredSelected = if (hasVariations) {
        variations.all { variation ->
            selectedVariations.containsKey(variation.name)
        }
    } else {
        true
    }

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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp)
            ) {
                // Item header
                ProductHeader(
                    item = item,
                    basePrice = basePrice,
                    totalPrice = totalUnitPrice
                )

                // Quantity selector
                QuantitySection(
                    quantity = quantity,
                    onQuantityChange = { quantity = it }
                )

                // Business type specific information
                BusinessTypeItemInfo(item, businessTypeConfig)

                // Debug section - Show raw data
                if (hasVariations) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "DEBUG: Has Variations = True",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Variations found: ${variations.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            variations.forEach { variation ->
                                Text(
                                    text = "- ${variation.name} (${variation.options.size} options)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Variations section (the actual UI)
                if (hasVariations) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Log.d("ItemDetail", "Rendering variations section with ${variations.size} variations")

                    VariationsSection(
                        variations = variations,
                        selectedVariations = selectedVariations,
                        onVariationSelected = { variationName, option ->
                            selectedVariations[variationName] = option
                            Log.d("ItemDetail", "Selected '$option.name' for '$variationName'")
                        }
                    )
                }

                // Price summary
                Spacer(modifier = Modifier.height(24.dp))
                PriceSummary(
                    basePrice = basePrice,
                    variations = selectedVariations.values.toList(),
                    quantity = quantity,
                    totalPrice = totalPrice,
                    discountInfo = item.settings?.inventory?.let { inventory ->
                        when (inventory.discountType) {
                            "percentage" -> "Discount: ${inventory.discountValue?.toInt()}%"
                            "flat" -> "Discount: AED ${String.format("%.2f", inventory.discountValue ?: 0.0)}"
                            else -> null
                        }
                    }
                )
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
                        onClick = onClose,
                        modifier = Modifier.weight(1f)
                    )

                    RfmPrimaryButton(
                        text = "Add to Cart",
                        onClick = {
                            // Create cart item with variations
                            val cartItem = CartItemWithModifiers(
                                id = item.id,
                                name = item.name,
                                price = basePrice,
                                quantity = quantity,
                                variations = selectedVariations.toMap()
                            )

                            onAddToCart(cartItem)
                            onClose()
                        },
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.ShoppingCart,
                        enabled = allRequiredSelected
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
                    imageVector = Icons.Default.ArrowForward,
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

// Include the helper composables here
@Composable
private fun VariationsSection(
    variations: List<Variation>,
    selectedVariations: Map<String, VariationOption>,
    onVariationSelected: (String, VariationOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("ItemDetail", "VariationsSection rendering with ${variations.size} variations")

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
            Log.d("ItemDetail", "Rendering variation: ${variation.name}")
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

        // Use LazyRow for horizontal scrolling if many options
        if (variation.options.size > 3) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(variation.options) { option ->
                    VariationOptionChip(
                        option = option,
                        isSelected = selectedOption?.name == option.name,
                        onClick = { onOptionSelected(option) }
                    )
                }
            }
        } else {
            // Use Row for fewer options
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
 * Display business type specific item information
 */
@Composable
private fun BusinessTypeItemInfo(
    item: Item,
    businessTypeConfig: BusinessTypeConfig?
) {
    val businessType = businessTypeConfig?.name ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        // Common information
        if (item.barcode != null || item.sku != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = item.sku ?: item.barcode ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Business type specific information
        when {
            // Restaurant items
            businessType.equals("restaurant", ignoreCase = true) -> {
                // Show preparation time if available
                item.preparationTime?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Preparation time: $it minutes",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Show allergens if available
                if (!item.allergens.isNullOrEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Allergens: ${item.allergens.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Show calories if available
                item.calories?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalDining,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Calories: $it kcal",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Service items
            businessType.equals("service", ignoreCase = true) && item.duration != null -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Format duration (e.g., "1h 30m" for 90 minutes)
                    val hours = item.duration / 60
                    val minutes = item.duration % 60
                    val durationText = if (hours > 0) {
                        "${hours}h${if (minutes > 0) " ${minutes}m" else ""}"
                    } else {
                        "${minutes}m"
                    }

                    Text(
                        text = "Duration: $durationText",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Show service level if available
                item.serviceLevel?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Service level: ${it.replaceFirstChar { char -> char.uppercase() }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Retail items - show inventory if available
            businessType.equals("retail", ignoreCase = true) -> {
                item.settings?.inventory?.let { inventory ->
                    inventory.currentStock?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            val unitText = inventory.primaryUnit ?: "units"
                            Text(
                                text = "In stock: ${it.toInt()} $unitText",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
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
    discountInfo: String? = null,
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

        // Discount info if available
        discountInfo?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.posColors.discount
                )
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