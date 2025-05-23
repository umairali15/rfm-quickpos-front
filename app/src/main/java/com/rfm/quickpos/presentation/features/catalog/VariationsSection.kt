// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/VariationsSection.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.data.remote.models.ItemVariation
import com.rfm.quickpos.data.remote.models.ItemVariationOption

/**
 * Component to display and select item variations
 */
@Composable
fun VariationsSection(
    variations: List<ItemVariation>,
    selectedVariations: Map<String, ItemVariationOption>,
    onVariationSelected: (String, ItemVariationOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Options",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        variations.forEach { variation ->
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = variation.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (variation.isRequired) {
                        Text(
                            text = "Required",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(variation.options) { option ->
                        val isSelected = selectedVariations[variation.name]?.id == option.id

                        VariationOptionChip(
                            option = option,
                            isSelected = isSelected,
                            onClick = {
                                onVariationSelected(variation.name, option)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual variation option chip
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VariationOptionChip(
    option: ItemVariationOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Column {
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (option.priceAdjustment != 0.0) {
                    Text(
                        text = "${if (option.priceAdjustment > 0) "+" else ""}AED ${String.format("%.2f", option.priceAdjustment)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (option.priceAdjustment > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        },
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = false
            )
        }
    )
}