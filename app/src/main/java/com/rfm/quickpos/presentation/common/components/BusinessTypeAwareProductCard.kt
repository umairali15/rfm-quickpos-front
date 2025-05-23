// app/src/main/java/com/rfm/quickpos/presentation/common/components/BusinessTypeAwareProductCard.kt

package com.rfm.quickpos.presentation.common.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.data.remote.models.BusinessTypeConfig
import com.rfm.quickpos.data.remote.models.Item

/**
 * Business type-aware product card that adapts display based on the business type
 */
@Composable
fun BusinessTypeAwareProductCard(
    item: Item,
    businessTypeConfig: BusinessTypeConfig?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine business type-specific UI
    val itemType = item.itemType ?: ""
    val hasModifiers = !item.modifierGroups.isNullOrEmpty()
    val hasVariations = !item.variations.isNullOrEmpty()
    val hasAllergens = item.allergens?.isNotEmpty() == true
    val isServiceItem = itemType == "service" || item.pricingType == "time-based"

    // Delegate to the standard ProductCard but add business-specific attributes
    ProductCard(
        name = item.name,
        price = "AED ${String.format("%.2f", item.price)}",
        onClick = onClick,
        imageUrl = item.imageUrl,
        discountPercentage = null,
        modifier = modifier,
        additionalContent = {
            // Add debug logging
            if (hasVariations || hasModifiers) {
                Log.d("ProductCard", "${item.name}: Variations=$hasVariations, Modifiers=$hasModifiers")
            }

            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                // Show variations indicator
                if (hasVariations) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(16.dp)
                        )
                        Text(
                            text = "Options available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Show modifiers indicator
                if (hasModifiers) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalDining,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(16.dp)
                        )
                        Text(
                            text = "Customizable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Restaurant-specific indicators
                if (item.preparationTime != null || hasAllergens) {
                    // Show preparation time
                    item.preparationTime?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(16.dp)
                            )
                            Text(
                                text = "$it min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Show allergens warning
                    if (hasAllergens) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(16.dp)
                            )
                            Text(
                                text = "Contains allergens",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Service-specific indicators
                if (isServiceItem && item.duration != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(16.dp)
                        )

                        val hours = item.duration!! / 60
                        val minutes = item.duration!! % 60
                        val durationText = if (hours > 0) {
                            "${hours}h${if (minutes > 0) " ${minutes}m" else ""}"
                        } else {
                            "${minutes}m"
                        }

                        Text(
                            text = durationText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    )
}