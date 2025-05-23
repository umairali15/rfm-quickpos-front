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
 * Section to display and select item modifiers
 */
@Composable
fun ModifiersSection(
    modifierGroups: List<ModifierGroup>,
    selectedModifiers: Map<String, Set<String>>, // groupId to set of selected modifier ids
    onModifierToggled: (groupId: String, modifierId: String, isSelected: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Add-ons",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        modifierGroups.sortedBy { it.displayOrder }.forEach { group ->
            ModifierGroupSection(
                group = group,
                selectedModifierIds = selectedModifiers[group.id] ?: emptySet(),
                onModifierToggled = { modifierId, isSelected ->
                    onModifierToggled(group.id, modifierId, isSelected)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModifierGroupSection(
    group: ModifierGroup,
    selectedModifierIds: Set<String>,
    onModifierToggled: (modifierId: String, isSelected: Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (group.isRequired) {
                    Text(
                        text = "Required",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (group.maxSelections > 1) {
                    Text(
                        text = "Select up to ${group.maxSelections}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            group.modifiers.sortedBy { it.displayOrder }.forEach { modifier ->
                ModifierItem(
                    modifier = modifier,
                    isSelected = selectedModifierIds.contains(modifier.id),
                    onToggled = { isSelected ->
                        // Check if we can select more
                        if (!isSelected || selectedModifierIds.size < group.maxSelections) {
                            onModifierToggled(modifier.id, isSelected)
                        }
                    },
                    enabled = modifier.available
                )
            }
        }
    }
}

@Composable
private fun ModifierItem(
    modifier: com.rfm.quickpos.data.remote.models.Modifier,
    isSelected: Boolean,
    onToggled: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onToggled,
            enabled = enabled
        )

        Text(
            text = modifier.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )

        if (modifier.priceAdjustment != 0.0) {
            Text(
                text = "${if (modifier.priceAdjustment > 0) "+" else ""}AED ${String.format("%.2f", modifier.priceAdjustment)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (modifier.priceAdjustment > 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.tertiary
            )
        }
    }
}