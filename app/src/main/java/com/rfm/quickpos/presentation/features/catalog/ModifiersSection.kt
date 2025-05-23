// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/ModifiersSection.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.data.remote.models.ModifierGroup

/**
 * Component to display and select modifiers
 */
@Composable
fun ModifiersSection(
    modifierGroups: List<ModifierGroup>,
    selectedModifiers: Map<String, Set<String>>,
    onModifierToggled: (String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Customize",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        modifierGroups.forEach { group ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )

                            val selectionText = when {
                                group.minSelections > 0 && group.maxSelections == 1 -> "Choose one"
                                group.minSelections > 0 -> "Choose at least ${group.minSelections}"
                                group.maxSelections > 1 -> "Choose up to ${group.maxSelections}"
                                else -> "Optional"
                            }

                            Text(
                                text = selectionText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (group.isRequired) {
                            Text(
                                text = "Required",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    group.modifiers.forEach { modifier ->
                        val isSelected = selectedModifiers[group.id]?.contains(modifier.id) == true

                        ModifierOption(
                            modifier = modifier,
                            isSelected = isSelected,
                            onToggle = { isChecked ->
                                onModifierToggled(group.id, modifier.id, isChecked)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual modifier option
 */
@Composable
private fun ModifierOption(
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

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = modifier.name,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (modifier.priceAdjustment != 0.0) {
            Text(
                text = "${if (modifier.priceAdjustment > 0) "+" else ""}AED ${String.format("%.2f", modifier.priceAdjustment)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (modifier.priceAdjustment > 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.tertiary
            )
        }
    }
}