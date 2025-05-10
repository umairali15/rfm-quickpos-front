// app/src/main/java/com/rfm/quickpos/presentation/common/components/BusinessTypeAwareProductCard.kt

package com.rfm.quickpos.presentation.common.components

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
    val hasModifiers = item.modifierGroupIds?.isNotEmpty() == true
    val hasAllergens = item.allergens?.isNotEmpty() == true
    val isServiceItem = itemType == "service" || item.pricingType == "time-based"
    val hasVariations = item.settings?.variations?.isNotEmpty() == true

    // Delegate to the standard ProductCard but add business-specific attributes
    ProductCard(
        name = item.name,
        price = "AED ${String.format("%.2f", item.price)}",
        onClick = onClick,
        imageUrl = item.imageUrl,
        discountPercentage = null, // Add discount logic as needed
        modifier = modifier,
        additionalContent = {
            // Business type specific additional content
            when {
                // Show variations indicator first
                hasVariations -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

                // For restaurant items, show preparation time and allergens
                hasAllergens || item.preparationTime != null -> {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        // Show preparation time if available
                        item.preparationTime?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = "$it min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Show allergens if available
                        if (hasAllergens) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = "Allergens",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // Show if item has modifiers
                        if (hasModifiers) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalDining,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = "Customizable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // For service items, show duration
                isServiceItem && item.duration != null -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        // Format duration (e.g., "1h 30m" for 90 minutes)
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}