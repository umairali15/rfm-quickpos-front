package com.rfm.quickpos.presentation.common.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * RFM styled category chip with enhanced visibility
 */


/**
 * RFM styled search bar with enhanced visibility
 */
@Composable
fun RfmSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    enabled: Boolean = true
) {
    val searchBarShape = RoundedCornerShape(8.dp)

    Card(
        shape = searchBarShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch() }
            ),
            singleLine = true,
            enabled = enabled,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
        ) { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }

                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Enhanced RFM cart icon with badge
 */
@Composable
fun RfmCartIcon(
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

}

/**
 * RFM styled divider with customizable color, thickness and padding
 */
@Composable
fun RfmDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    thickness: Float = 1f,
    paddingValues: PaddingValues = PaddingValues(vertical = 8.dp)
) {
    Divider(
        color = color,
        thickness = thickness.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues)
    )
}

/**
 * Quantity selector component with increment/decrement buttons
 */
@Composable
fun RfmQuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Int = 1,
    maxValue: Int = 99
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Decrease button
        IconButton(
            onClick = {
                if (quantity > minValue) {
                    onQuantityChange(quantity - 1)
                }
            },
            enabled = quantity > minValue
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.titleLarge,
                color = if (quantity > minValue)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }

        // Quantity display
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Increase button
        IconButton(
            onClick = {
                if (quantity < maxValue) {
                    onQuantityChange(quantity + 1)
                }
            },
            enabled = quantity < maxValue
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleLarge,
                color = if (quantity < maxValue)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RfmDividerPreview() {
    RFMQuickPOSTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Content above divider")
            RfmDivider()
            Text(text = "Content below divider")

            // Thicker divider
            RfmDivider(
                thickness = 2f,
                color = MaterialTheme.colorScheme.primary,
                paddingValues = PaddingValues(vertical = 16.dp)
            )

            Text(text = "Content below thick divider")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RfmQuantitySelectorPreview() {
    RFMQuickPOSTheme {
        Surface {
            var quantity by remember { mutableIntStateOf(1) }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                RfmQuantitySelector(
                    quantity = quantity,
                    onQuantityChange = { quantity = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Current quantity: $quantity",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * RFM styled loading indicator with optional text and animation
 */
@Composable
fun RfmLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Float = 40f,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 4f,
    text: String? = null,
    animated: Boolean = false
) {
    // Optional pulsing animation
    val transition = rememberInfiniteTransition(label = "loading-animation")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (animated) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing)
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.scale(scale)
        ) {
            CircularProgressIndicator(
                color = color,
                strokeWidth = strokeWidth.dp,
                modifier = Modifier.size(size.dp)
            )
        }

        // Optional text below the spinner
        if (text != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Full-screen loading indicator with centered placement
 */
@Composable
fun RfmFullScreenLoading(
    text: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        RfmLoadingIndicator(
            size = 56f,
            strokeWidth = 5f,
            text = text,
            animated = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RfmLoadingIndicatorPreview() {
    RFMQuickPOSTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RfmLoadingIndicator()

                Spacer(modifier = Modifier.height(24.dp))

                RfmLoadingIndicator(
                    text = "Loading...",
                    size = 48f,
                    animated = true
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun RfmFullScreenLoadingPreview() {
    RFMQuickPOSTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            RfmFullScreenLoading()
        }
    }
}