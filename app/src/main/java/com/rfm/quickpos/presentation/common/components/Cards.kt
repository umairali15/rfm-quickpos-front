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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rfm.quickpos.presentation.common.theme.CircularShape
import com.rfm.quickpos.presentation.common.theme.DiscountTagShape
import com.rfm.quickpos.presentation.common.theme.PaymentMethodCardShape
import com.rfm.quickpos.presentation.common.theme.ProductCardShape
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors
import androidx.compose.material3.ExperimentalMaterial3Api


fun BorderStroke?.toBorderStroke(): BorderStroke = this ?: BorderStroke(0.dp, Color.Transparent)
/**
 * Payment Method Card - For selecting payment type
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    selected: Boolean = false
) {
    val cardColors = if (selected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    OutlinedCard(
        onClick = onClick,
        shape = PaymentMethodCardShape,
        colors = cardColors,
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null.toBorderStroke(),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircularShape)
                    .background(Color.White.copy(alpha = 0.9f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Product Card - For catalog grid
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    name: String,
    price: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    currencyCode: String = "AED",
    discountTag: String? = null
) {
    ElevatedCard(
        onClick = onClick,
        shape = ProductCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.posColors.productCardBackground
        ),
        modifier = modifier
    ) {
        Box {
            Column {
                // Product Image
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Placeholder
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Product Details
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

                    Text(
                        text = "$currencyCode $price",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Discount Tag if present
            discountTag?.let {
                Surface(
                    color = MaterialTheme.posColors.discount,
                    shape = DiscountTagShape,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.posColors.onDiscount,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Status card - For KPI metrics on dashboard
 */
@Composable
fun StatusCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
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
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Cart Item Card - For checkout screen
 */
@Composable
fun CartItemCard(
    name: String,
    price: String,
    quantity: String,
    modifier: Modifier = Modifier,
    currencyCode: String = "AED",
    discountTag: String? = null,
    onQuantityChanged: ((Int) -> Unit)? = null
) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )

                    discountTag?.let {
                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = MaterialTheme.posColors.discount,
                            shape = DiscountTagShape
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.posColors.onDiscount,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$quantity x $currencyCode $price",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "$currencyCode $price",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardsPreview() {
    RFMQuickPOSTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row {
                PaymentMethodCard(
                    title = "Cash",
                    icon = Icons.Filled.Money,
                    onClick = {},
                    iconTint = MaterialTheme.posColors.cashIcon,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                PaymentMethodCard(
                    title = "Card",
                    icon = Icons.Filled.CreditCard,
                    onClick = {},
                    iconTint = MaterialTheme.posColors.cardIcon,
                    selected = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                ProductCard(
                    name = "French Fries",
                    price = "24",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                ProductCard(
                    name = "Nivea Men shaving foam with skin restoration effect",
                    price = "3.591",
                    discountTag = "-15%",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StatusCard(
                title = "Number of Sales",
                value = "2,148M",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CartItemCard(
                name = "\"Paris\" set",
                price = "12",
                quantity = "12",
                currencyCode = "AED",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            CartItemCard(
                name = "Liquid soap (pieces)",
                price = "220",
                quantity = "16",
                currencyCode = "AED",
                discountTag = "AED 100",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}