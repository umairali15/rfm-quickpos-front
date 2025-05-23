// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/VariationsSection.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.data.remote.models.ItemVariation
import com.rfm.quickpos.data.remote.models.ItemVariationOption

/**
 * Section to display and select item variations (new structure)
 */
@Composable
fun VariationsSection(
    variations: List<ItemVariation>,
    selectedVariations: Map<String, ItemVariationOption>,
    onVariationSelected: (variationName: String, option: ItemVariationOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Options",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        variations.sortedBy { it.displayOrder }.forEach { variation ->
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

@Composable
private fun VariationGroup(
    variation: ItemVariation,
    selectedOption: ItemVariationOption?,
    onOptionSelected: (ItemVariationOption) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = variation.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (variation.isRequired) {
                Text(
                    text = "Required",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            variation.options.sortedBy { it.displayOrder }.forEach { option ->
                VariationOptionChip(
                    option = option,
                    isSelected = selectedOption?.id == option.id,
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}

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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (option.priceAdjustment != 0.0) {
                    Spacer(modifier = Modifier.width(4.dp))
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
        },
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            FilterChipDefaults.filterChipBorder(enabled = true, selected = false)
        }
    )
}