// app/src/main/java/com/rfm/quickpos/presentation/common/components/Cards.kt
package com.rfm.quickpos.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.theme.PriceTextSmall
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors

// Define shapes with consistent radius
private val ProductCardShape = RoundedCornerShape(12.dp)
private val PaymentMethodCardShape = RoundedCornerShape(16.dp)

/**
 * Enhanced standard card with RFM styling
 * Added proper borders and elevation for better visibility in light mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RfmCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Float = 1f,
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
            // Added border for better visibility in light mode
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.posColors.cardBorder.copy(alpha = 0.5f)
            ),
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
            // Added border for better visibility in light mode
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.posColors.cardBorder.copy(alpha = 0.5f)
            ),
            modifier = modifier
        ) {
            content()
        }
    }
}

/**
 * Enhanced elevated card with RFM styling
 * Increased elevation and shadow spread for better visibility
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
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation.dp,
                // Increased for better visibility
                pressedElevation = (elevation + 2).dp,
                focusedElevation = (elevation + 1).dp
            ),
            modifier = modifier.shadow(
                elevation = elevation.dp,
                shape = shape,
                clip = false
            )
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
            modifier = modifier.shadow(
                elevation = elevation.dp,
                shape = shape,
                clip = false
            )
        ) {
            content()
        }
    }
}

/**
 * Enhanced outlined card with RFM styling
 * More pronounced border for better visibility
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
            border = BorderStroke(1.5.dp, outlineColor), // Increased width
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
            border = BorderStroke(1.5.dp, outlineColor), // Increased width
            modifier = modifier
        ) {
            content()
        }
    }
}

/**
 * Enhanced product card for the catalog
 * Added border and better shadow for light mode visibility
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    name: String,
    price: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    discountPercentage: Int? = null,
    additionalContent: @Composable (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        shape = ProductCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.posColors.productCardBackground,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        // Added border for light mode visibility
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.posColors.cardBorder.copy(alpha = 0.5f)
        ),
        // Enhanced elevation
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        modifier = modifier.shadow(
            elevation = 2.dp,
            shape = ProductCardShape,
            spotColor = MaterialTheme.posColors.cardShadow,
            ambientColor = MaterialTheme.posColors.cardShadow
        )
    ) {
        Column {
            // Product image or placeholder
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                // Use actual image or placeholder
                if (imageUrl != null) {
                    // Use an image loading library here
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(ProductCardShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                } else {
                    // Placeholder box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(ProductCardShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                // Discount tag if applicable
                discountPercentage?.let {
                    // Using the RfmDiscountTag from RfmDiscountTag.kt
                    RfmDiscountTag(
                        discountPercentage = it,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Product info
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
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
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
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

                // Additional business type-specific content if provided
                additionalContent?.invoke()
            }
        }
    }
}

/**
 * Enhanced payment method card with better borders and contrast
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
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
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
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp, // Thicker border when selected
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        modifier = modifier.shadow(
            elevation = if (isSelected) 4.dp else 1.dp,
            shape = PaymentMethodCardShape,
            clip = false
        )
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
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * Enhanced status indicator card
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
        elevation = 3f,
        shape = RoundedCornerShape(12.dp)
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

@Preview(showBackground = true)
@Composable
fun CardsPreview() {
    RFMQuickPOSTheme {
        Surface {
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
}