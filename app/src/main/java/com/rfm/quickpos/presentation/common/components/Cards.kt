package com.rfm.quickpos.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.ProductCardShape
import com.rfm.quickpos.presentation.common.theme.PaymentMethodCardShape
import com.rfm.quickpos.presentation.common.theme.PriceTextSmall
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * Standard card with RFM styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RfmCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Float = 0f,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
            modifier = modifier
        ) {
            content()
        }
    } else {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
            modifier = modifier
        ) {
            content()
        }
    }
}

/**
 * Elevated card with RFM styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RfmElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Float = 4f,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            shape = shape,
            colors = CardDefaults.elevatedCardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
            modifier = modifier
        ) {
            content()
        }
    } else {
        ElevatedCard(
            shape = shape,
            colors = CardDefaults.elevatedCardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
            modifier = modifier
        ) {
            content()
        }
    }
}

/**
 * Outlined card with RFM styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RfmOutlinedCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    outlineColor: Color = MaterialTheme.colorScheme.outline,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        OutlinedCard(
            onClick = onClick,
            shape = shape,
            colors = CardDefaults.outlinedCardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            border = BorderStroke(1.dp, outlineColor),
            modifier = modifier
        ) {
            content()
        }
    } else {
        OutlinedCard(
            shape = shape,
            colors = CardDefaults.outlinedCardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            border = BorderStroke(1.dp, outlineColor),
            modifier = modifier
        ) {
            content()
        }
    }
}

/**
 * Product card for the catalog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    name: String,
    price: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    inStock: Boolean = true,
    discountPercentage: Int? = null
) {
    Card(
        onClick = onClick,
        shape = ProductCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.posColors.productCardBackground,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    ) {
        Column {
            // Product image or placeholder
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                // For now, use a simple placeholder Box instead of AsyncImage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(ProductCardShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                // Discount tag if applicable
                discountPercentage?.let {
                    RfmDiscountTag(
                        discountPercentage = it,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Product info
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = price,
                        style = PriceTextSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Add to cart button
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to cart",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Payment method card (for selecting card/cash/etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodCard(
    methodName: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val iconColor = when (methodName.lowercase()) {
        "card" -> MaterialTheme.posColors.cardIcon
        "cash" -> MaterialTheme.posColors.cashIcon
        else -> contentColor
    }

    OutlinedCard(
        onClick = onClick,
        shape = PaymentMethodCardShape,
        colors = CardDefaults.outlinedCardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = methodName,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Status indicator card
 */
@Composable
fun StatusCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    RfmElevatedCard(
        modifier = modifier,
        containerColor = containerColor,
        elevation = 2f
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = valueColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Discount tag component
 */
@Composable
fun RfmDiscountTag(
    discountPercentage: Int,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.posColors.discount)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "-$discountPercentage%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.posColors.onDiscount,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CardsPreview() {
    RFMQuickPOSTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            RfmCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Standard Card",
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            RfmElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Elevated Card",
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            RfmOutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Outlined Card",
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ProductCard(
                    name = "Coffee",
                    price = "AED 15.00",
                    onClick = {},
                    discountPercentage = 10,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                ProductCard(
                    name = "Croissant",
                    price = "AED 10.00",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                PaymentMethodCard(
                    methodName = "Card",
                    icon = Icons.Default.CreditCard,
                    isSelected = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                PaymentMethodCard(
                    methodName = "Cash",
                    icon = Icons.Default.Money,
                    isSelected = false,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StatusCard(
                title = "Today's Sales",
                value = "AED 1,250.00",
                icon = Icons.Default.CreditCard,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}