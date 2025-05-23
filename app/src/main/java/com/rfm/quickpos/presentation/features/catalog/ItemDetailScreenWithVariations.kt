// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/ItemDetailScreenWithVariations.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.data.remote.models.Item
import com.rfm.quickpos.data.remote.models.ItemVariationOption
import com.rfm.quickpos.data.remote.models.VariationOption
import com.rfm.quickpos.data.repository.CatalogRepository
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmQuantitySelector
import com.rfm.quickpos.presentation.features.cart.CartItemWithModifiers

/**
 * Unified item detail screen with variations and modifiers
 */
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
    val selectedModifiers = remember { mutableStateMapOf<String, Set<String>>() }

    // Get variations and modifiers from item
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

    // Initialize default variations
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
                title = { Text(text = item.name) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Total price display
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AED ${String.format("%.2f", totalPrice)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Add to cart button
                    RfmPrimaryButton(
                        text = "Add to Cart",
                        onClick = {
                            // Check if all required variations are selected
                            val missingRequired = variations.filter { it.isRequired }
                                .any { variation -> !selectedVariations.containsKey(variation.name) }

                            if (!missingRequired) {
                                // Create cart item with variations
                                val cartItem = CartItemWithModifiers(
                                    id = item.id,
                                    name = item.name,
                                    price = basePrice,
                                    quantity = quantity,
                                    variations = selectedVariations.mapValues { entry ->
                                        VariationOption(
                                            name = entry.value.name,
                                            priceAdjustment = entry.value.priceAdjustment
                                        )
                                    },
                                    // FIX: Convert selected modifiers to List<ModifierData> with groupId
                                    modifiers = modifierGroups.flatMap { group ->
                                        group.modifiers.filter { modifier ->
                                            selectedModifiers[group.id]?.contains(modifier.id) == true
                                        }.map { modifier ->
                                            CartItemWithModifiers.ModifierData(
                                                groupId = group.id,  // INCLUDE GROUP ID
                                                groupName = group.name,
                                                modifierId = modifier.id,
                                                modifierName = modifier.name,
                                                priceAdjustment = modifier.priceAdjustment
                                            )
                                        }
                                    }
                                )

                                onAddToCart(cartItem)
                            }
                        },
                        fullWidth = true,
                        leadingIcon = Icons.Default.ShoppingCart
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Item header
            ItemDetailHeader(
                item = item,
                basePrice = basePrice,
                totalPrice = totalUnitPrice
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            // Variations section
            if (variations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

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
            }

            // Modifiers section
            if (modifierGroups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

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
            }

            // Business type specific info if any
            BusinessTypeItemInfo(item, businessTypeConfig)

            Spacer(modifier = Modifier.height(120.dp)) // Space for bottom bar
        }
    }
}

/**
 * Item detail header component
 */
@Composable
private fun ItemDetailHeader(
    item: Item,
    basePrice: Double,
    totalPrice: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Item name
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Description
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
                    text = "Price: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "AED ${String.format("%.2f", basePrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
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
}

/**
 * Business type specific info component
 */
@Composable
fun BusinessTypeItemInfo(
    item: Item,
    businessTypeConfig: com.rfm.quickpos.data.remote.models.BusinessTypeConfig?
) {
    // Add business type specific information display
    when (businessTypeConfig?.name) {
        "restaurant" -> {
            if (item.preparationTime != null || item.allergens?.isNotEmpty() == true || item.calories != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        item.preparationTime?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Preparation time: $it minutes",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        item.calories?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fireplace,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$it calories",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        item.allergens?.let { allergens ->
                            if (allergens.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Allergens: ${allergens.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        "service" -> {
            if (item.duration != null || item.serviceLevel != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        item.duration?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Duration: $it minutes",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        item.serviceLevel?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Service Level: $it",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}