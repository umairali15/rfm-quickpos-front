// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/ItemDetailScreenWithVariations.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.data.remote.models.ItemVariationOption
import com.rfm.quickpos.data.repository.CatalogRepository
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmQuantitySelector
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
    // Collect data from repository
    val items by catalogRepository.items.collectAsState()
    val businessTypeConfig by catalogRepository.businessTypeConfig.collectAsState()

    // Find the item
    val item = items.find { it.id == itemId }

    // Handle navigation if item not found
    if (item == null) {
        LaunchedEffect(Unit) {
            onClose()
        }
        return
    }

    // Local state
    var quantity by remember { mutableIntStateOf(1) }
    val selectedVariations = remember { mutableStateMapOf<String, ItemVariationOption>() }
    val selectedModifiers = remember { mutableStateMapOf<String, Set<String>>() } // modifierGroupId to set of optionIds

    // Get variations and modifiers from the item
    val variations = item.variations ?: emptyList()
    val modifierGroups = item.modifierGroups ?: emptyList()

    // Calculate pricing
    val basePrice = item.price
    val variationsPriceAdjustment = selectedVariations.values.sumOf { it.priceAdjustment }
    val modifiersPriceAdjustment = modifierGroups.sumOf { group ->
        group.modifiers.filter { modifier ->
            selectedModifiers[group.id]?.contains(modifier.id) == true
        }.sumOf { it.priceAdjustment }
    }
    val totalUnitPrice = basePrice + variationsPriceAdjustment + modifiersPriceAdjustment
    val totalPrice = totalUnitPrice * quantity

    // Initialize default variations (select first option if required)
    LaunchedEffect(variations) {
        variations.forEach { variation ->
            if (variation.isRequired && variation.options.isNotEmpty() && !selectedVariations.containsKey(variation.name)) {
                selectedVariations[variation.name] = variation.options.first()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.name) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
        ) {
            // Item info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Image placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    item.description?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Price display
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AED ${String.format("%.2f", basePrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (totalUnitPrice != basePrice) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )

                        if (totalUnitPrice != basePrice) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "AED ${String.format("%.2f", totalUnitPrice)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Business type specific info
                    BusinessTypeItemInfo(item, businessTypeConfig)
                }
            }

            // Variations section
            if (variations.isNotEmpty()) {
                VariationsSection(
                    variations = variations,
                    selectedVariations = selectedVariations.mapKeys { entry ->
                        // Find variation name by searching through variations
                        variations.find { variation ->
                            variation.options.any { it.id == entry.value.id }
                        }?.name ?: entry.key
                    },
                    onVariationSelected = { variationName, option ->
                        selectedVariations[variationName] = option
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Modifiers section
            if (modifierGroups.isNotEmpty()) {
                ModifiersSection(
                    modifierGroups = modifierGroups,
                    selectedModifiers = selectedModifiers,
                    onModifierToggled = { groupId, modifierId, isSelected ->
                        val currentSelection = selectedModifiers[groupId] ?: emptySet()
                        selectedModifiers[groupId] = if (isSelected) {
                            currentSelection + modifierId
                        } else {
                            currentSelection - modifierId
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quantity selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    RfmQuantitySelector(
                        quantity = quantity,
                        onQuantityChange = { quantity = it },
                        minValue = 1,
                        maxValue = 99
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Add to cart button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                RfmPrimaryButton(
                    text = "Add to Cart - AED ${String.format("%.2f", totalPrice)}",
                    onClick = {
                        // Check if all required variations are selected
                        val missingRequired = variations.filter { it.isRequired }
                            .any { variation -> !selectedVariations.containsKey(variation.name) }

                        if (!missingRequired) {
                            // Create cart item with variations in the new format
                            val cartItem = CartItemWithModifiers(
                                id = item.id,
                                name = item.name,
                                price = basePrice,
                                quantity = quantity,
                                variations = selectedVariations.mapValues { entry ->
                                    com.rfm.quickpos.data.remote.models.VariationOption(
                                        name = entry.value.name,
                                        priceAdjustment = entry.value.priceAdjustment
                                    )
                                },
                                // Convert selected modifiers to the expected format
                                modifiers = modifierGroups.flatMap { group ->
                                    group.modifiers.filter { modifier ->
                                        selectedModifiers[group.id]?.contains(modifier.id) == true
                                    }.map { modifier ->
                                        group.name to modifier
                                    }
                                }.toMap()
                            )

                            onAddToCart(cartItem)
                            onClose()
                        }
                    },
                    leadingIcon = Icons.Default.ShoppingCart,
                    fullWidth = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Display business type specific item information
 */
@Composable
fun BusinessTypeItemInfo(
    item: com.rfm.quickpos.data.remote.models.Item,
    businessTypeConfig: com.rfm.quickpos.data.remote.models.BusinessTypeConfig?
) {
    Column {
        // Show allergens for restaurant items
        if (!item.allergens.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Allergens: ${item.allergens.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Show preparation time for restaurant items
        item.preparationTime?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Preparation time: $it minutes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Show service duration for service items
        if (item.itemType == "service" && item.duration != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Service duration: ${item.duration} minutes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Show calories for restaurant items
        item.calories?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalDining,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$it calories",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}