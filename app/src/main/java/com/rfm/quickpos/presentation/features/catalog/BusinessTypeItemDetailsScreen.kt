// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/BusinessTypeItemDetailScreen.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.data.remote.models.BusinessTypeConfig
import com.rfm.quickpos.data.remote.models.Item
import com.rfm.quickpos.data.repository.CatalogRepository
import com.rfm.quickpos.presentation.common.components.RfmDivider
import com.rfm.quickpos.presentation.common.components.RfmOutlinedButton
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmQuantitySelector
import com.rfm.quickpos.presentation.common.theme.PriceTextLarge
import com.rfm.quickpos.presentation.features.cart.CartItemWithModifiers

/**
 * Business type-aware item detail screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessTypeItemDetailScreen(
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
        // Item not found
        LaunchedEffect(Unit) {
            onClose()
        }
        return
    }

    // Get modifier groups for the item
    val modifierGroups = remember(item) {
        item.modifierGroupIds?.mapNotNull { groupId ->
            catalogRepository.modifierGroups.value.find { it.id == groupId }
        } ?: emptyList()
    }

    // Local state for quantity and selected modifiers
    var quantity by remember { mutableIntStateOf(1) }
    val selectedModifiers = remember { mutableStateListOf<Pair<String, String>>() } // Pairs of (groupId, modifierId)

    // Calculate total price with modifiers
    val basePrice = item.price
    val modifiersPrice = selectedModifiers.sumOf { (groupId, modifierId) ->
        modifierGroups.find { it.id == groupId }?.modifiers?.find { it.id == modifierId }?.priceAdjustment ?: 0.0
    }
    val totalPrice = (basePrice + modifiersPrice) * quantity

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
            // Item name
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Item description
            item.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Business type specific information
            BusinessTypeItemInfo(item, businessTypeConfig)

            Spacer(modifier = Modifier.height(16.dp))

            // Base price
            Text(
                text = "Base Price: AED ${String.format("%.2f", basePrice)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity selector
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quantity:",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.width(16.dp))

                RfmQuantitySelector(
                    quantity = quantity,
                    onQuantityChange = { quantity = it }
                )
            }

            // Only show modifiers for restaurant items
            if (businessTypeConfig?.supportsModifiers == true && modifierGroups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Customize Your Order",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Display modifier groups and options
                modifierGroups.forEach { group ->
                    Spacer(modifier = Modifier.height(16.dp))

                    // Modifier group header
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (group.required) {
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "(Required)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Selection instructions
                    Text(
                        text = if (group.maxSelections > 1) {
                            "Select ${if (group.minSelections > 0) "at least ${group.minSelections}" else "up to ${group.maxSelections}"}"
                        } else {
                            "Select one option"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Display modifiers with appropriate selection control
                    if (group.maxSelections == 1) {
                        // Radio button group for single selection
                        val selectedModifier = selectedModifiers.find { it.first == group.id }?.second

                        group.modifiers.forEach { modifier ->
                            ModifierRadioOption(
                                modifier = modifier,
                                isSelected = selectedModifier == modifier.id,
                                onSelect = {
                                    // Remove any existing selection for this group
                                    selectedModifiers.removeAll { it.first == group.id }
                                    // Add new selection
                                    selectedModifiers.add(group.id to modifier.id)
                                }
                            )
                        }
                    } else {
                        // Checkboxes for multiple selection
                        group.modifiers.forEach { modifier ->
                            ModifierCheckboxOption(
                                modifier = modifier,
                                isSelected = selectedModifiers.any { it.first == group.id && it.second == modifier.id },
                                onToggle = { isSelected ->
                                    if (isSelected) {
                                        // Add selection if not exceeding max
                                        val currentSelections = selectedModifiers.count { it.first == group.id }
                                        if (currentSelections < group.maxSelections) {
                                            selectedModifiers.add(group.id to modifier.id)
                                        }
                                    } else {
                                        // Remove selection
                                        selectedModifiers.removeAll { it.first == group.id && it.second == modifier.id }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider
            RfmDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // Total price with modifiers
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Total Price:",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (modifiersPrice > 0) {
                        Text(
                            text = "Includes AED ${String.format("%.2f", modifiersPrice)} in extras",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "AED ${String.format("%.2f", totalPrice)}",
                    style = PriceTextLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
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

                // Check if all required modifier groups have selections
                val allRequiredSelected = modifierGroups
                    .filter { it.required && it.minSelections > 0 }
                    .all { group ->
                        selectedModifiers.count { it.first == group.id } >= group.minSelections
                    }

                RfmPrimaryButton(
                    text = "Add to Cart",
                    onClick = {
                        // Create cart item with modifiers
                        val cartItem = CartItemWithModifiers(
                            id = item.id,
                            name = item.name,
                            price = basePrice,
                            quantity = quantity,
                            modifiers = selectedModifiers.map { (groupId, modifierId) ->
                                val group = modifierGroups.find { it.id == groupId }
                                val modifier = group?.modifiers?.find { it.id == modifierId }

                                // Format modifier data
                                CartItemWithModifiers.ModifierData(
                                    id = modifierId,
                                    name = modifier?.name ?: "",
                                    price = modifier?.priceAdjustment ?: 0.0,
                                    groupName = group?.name ?: ""
                                )
                            }
                        )

                        onAddToCart(cartItem)
                        onClose()
                    },
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.ShoppingCart,
                    enabled = allRequiredSelected
                )
            }

            // If there are unmet requirements, show a message
            if (modifierGroups.any { it.required && selectedModifiers.count { mod -> mod.first == it.id } < it.minSelections }) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Please make all required selections",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Display business type specific item information
 */
@Composable
fun BusinessTypeItemInfo(
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
 * Radio button for single-select modifier options
 */
@Composable
fun ModifierRadioOption(
    modifier: com.rfm.quickpos.data.remote.models.Modifier,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = modifier.name,
                style = MaterialTheme.typography.bodyLarge
            )

            if (modifier.priceAdjustment != 0.0) {
                Text(
                    text = if (modifier.priceAdjustment > 0) {
                        "+AED ${String.format("%.2f", modifier.priceAdjustment)}"
                    } else {
                        "-AED ${String.format("%.2f", -modifier.priceAdjustment)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (modifier.priceAdjustment > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color.Green
                    }
                )
            }
        }
    }
}

/**
 * Checkbox for multi-select modifier options
 */
@Composable
fun ModifierCheckboxOption(
    modifier: com.rfm.quickpos.data.remote.models.Modifier,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onToggle
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = modifier.name,
                style = MaterialTheme.typography.bodyLarge
            )

            if (modifier.priceAdjustment != 0.0) {
                Text(
                    text = if (modifier.priceAdjustment > 0) {
                        "+AED ${String.format("%.2f", modifier.priceAdjustment)}"
                    } else {
                        "-AED ${String.format("%.2f", -modifier.priceAdjustment)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (modifier.priceAdjustment > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color.Green
                    }
                )
            }
        }
    }
}