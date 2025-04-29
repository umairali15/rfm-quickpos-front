package com.rfm.quickpos.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.TextFieldShape
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * RFM styled text field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RfmTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let {
                { Text(text = it) }
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            enabled = enabled,
            readOnly = readOnly,
            visualTransformation = visualTransformation,
            shape = TextFieldShape,
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        )

        if (isError && errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
            )
        }
    }
}

/**
 * RFM search bar
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
    Surface(
        shape = TextFieldShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
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
 * RFM styled category chip
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RfmCategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text = text)
        },
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (selected)
                MaterialTheme.posColors.selectedCategoryChip
            else
                MaterialTheme.posColors.categoryChipBackground,
            labelColor = if (selected)
                MaterialTheme.posColors.onSelectedCategoryChip
            else
                MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.posColors.selectedCategoryChip,
            selectedLabelColor = MaterialTheme.posColors.onSelectedCategoryChip
        ),
        modifier = modifier
    )
}

/**
 * RFM loading indicator
 */
@Composable
fun RfmLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Float = 40f,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 4f
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = strokeWidth.dp,
            modifier = Modifier.size(size.dp)
        )
    }
}

/**
 * RFM section header
 */
@Composable
fun RfmSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * RFM badge
 */
@Composable
fun RfmBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * RFM setting row
 */
@Composable
fun RfmSettingRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )

                subtitle?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            trailing?.let {
                it()
            }
        }
    }
}

/**
 * RFM styled switch
 */
@Composable
fun RfmSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
    )
}

/**
 * RFM quantity selector
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

/**
 * RFM section divider
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

@Preview(showBackground = true)
@Composable
fun UIComponentsPreview() {
    RFMQuickPOSTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            var searchText by remember { mutableStateOf("") }
            var switchChecked by remember { mutableStateOf(false) }
            var quantity by remember { mutableStateOf(1) }

            RfmTextField(
                value = "Input Text",
                onValueChange = {},
                label = "Label",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            RfmSearchBar(
                query = searchText,
                onQueryChange = { searchText = it },
                onSearch = {},
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                RfmCategoryChip(
                    text = "Selected",
                    selected = true,
                    onClick = {}
                )

                Spacer(modifier = Modifier.width(8.dp))

                RfmCategoryChip(
                    text = "Not Selected",
                    selected = false,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            RfmLoadingIndicator()

            Spacer(modifier = Modifier.height(16.dp))

            RfmSectionHeader(
                title = "Section Title",
                actionText = "View All",
                onActionClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RfmBadge(text = "New")

                Spacer(modifier = Modifier.width(8.dp))

                RfmBadge(
                    text = "Sale",
                    color = MaterialTheme.posColors.discount,
                    textColor = MaterialTheme.posColors.onDiscount
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            RfmSettingRow(
                title = "Setting Item",
                subtitle = "Description text",
                trailing = {
                    RfmSwitch(
                        checked = switchChecked,
                        onCheckedChange = { switchChecked = it }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RfmQuantitySelector(
                quantity = quantity,
                onQuantityChange = { quantity = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RfmDivider()
        }
    }
}