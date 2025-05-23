// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/VariationsAndModifiersComponents.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.data.remote.models.Item
import com.rfm.quickpos.data.remote.models.ItemVariation
import com.rfm.quickpos.data.remote.models.ItemVariationOption
import com.rfm.quickpos.data.remote.models.ModifierGroup
import com.rfm.quickpos.data.remote.models.BusinessTypeConfig

/**
 * Business type aware item information display
 */
@Composable
fun BusinessTypeItemInfo(
    item: Item,
    businessTypeConfig: BusinessTypeConfig?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Restaurant specific info
        if (businessTypeConfig?.name == "restaurant") {
            if (item.preparationTime != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Preparation time: ${item.preparationTime} minutes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.calories != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Calories: ${item.calories}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!item.allergens.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Allergens: ${item.allergens.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Service specific info
        if (businessTypeConfig?.name == "service" && item.duration != null) {
            Spacer(modifier = Modifier.height(8.dp))
            val hours = item.duration / 60
            val minutes = item.duration % 60
            val durationText = if (hours > 0) {
                "${hours}h${if (minutes > 0) " ${minutes}m" else ""}"
            } else {
                "${minutes}m"
            }
            Text(
                text = "Duration: $durationText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Retail specific info
        if (businessTypeConfig?.name == "retail") {
            item.sku?.let { sku ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SKU: $sku",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Enhanced variations section - renamed to avoid conflicts
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnhancedVariationsSection(
    variations: List<ItemVariation>,
    selectedVariations: Map<String, ItemVariationOption>,
    onVariationSelected: (String, ItemVariationOption) -> Unit,
    modifier: Modifier = Modifier
) {
    if (variations.isEmpty()) return

    Column(modifier = modifier) {
        variations.forEach { variation ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Variation title
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = variation.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        if (variation.isRequired) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Required",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Variation options
                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        variation.options.forEach { option ->
                            val isSelected = selectedVariations[variation.name]?.name == option.name

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = isSelected,
                                        onClick = {
                                            onVariationSelected(
                                                variation.name,
                                                option
                                            )
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    if (option.priceAdjustment != 0.0) {
                                        Text(
                                            text = "${if (option.priceAdjustment > 0) "+" else ""}AED ${String.format("%.2f", option.priceAdjustment)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (option.priceAdjustment > 0)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Enhanced modifiers section with better styling - renamed to avoid conflicts
 */
@Composable
fun EnhancedModifiersSection(
    modifierGroups: List<ModifierGroup>,
    selectedModifiers: Map<String, Set<String>>,
    onModifierToggled: (String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (modifierGroups.isEmpty()) return

    Column(modifier = modifier) {
        modifierGroups.forEach { group ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Modifier group title
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        if (group.isRequired) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Required",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Selection info
                    val selectedCount = selectedModifiers[group.id]?.size ?: 0
                    if (group.minSelections > 0 || group.maxSelections > 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select ${group.minSelections}-${group.maxSelections} items (${selectedCount} selected)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Modifier options
                    Column {
                        group.modifiers.forEach { modifierItem ->
                            val isSelected = selectedModifiers[group.id]?.contains(modifierItem.id) == true
                            val canSelect = selectedCount < group.maxSelections || isSelected

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = isSelected,
                                        enabled = canSelect,
                                        onClick = {
                                            if (canSelect) {
                                                onModifierToggled(group.id, modifierItem.id, !isSelected)
                                            }
                                        },
                                        role = if (group.maxSelections == 1) Role.RadioButton else Role.Checkbox
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (group.maxSelections == 1) {
                                    RadioButton(
                                        selected = isSelected,
                                        enabled = canSelect,
                                        onClick = null
                                    )
                                } else {
                                    Checkbox(
                                        checked = isSelected,
                                        enabled = canSelect,
                                        onCheckedChange = null
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = modifierItem.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (canSelect)
                                            MaterialTheme.colorScheme.onSurface
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )

                                    if (modifierItem.priceAdjustment != 0.0) {
                                        Text(
                                            text = "${if (modifierItem.priceAdjustment > 0) "+" else ""}AED ${String.format("%.2f", modifierItem.priceAdjustment)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (modifierItem.priceAdjustment > 0)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}