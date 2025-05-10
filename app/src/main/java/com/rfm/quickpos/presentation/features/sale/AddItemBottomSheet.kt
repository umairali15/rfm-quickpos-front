// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/AddCustomItemBottomSheet.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.presentation.common.components.RfmTextField
import com.rfm.quickpos.presentation.common.theme.PriceTextMedium

/**
 * Modern and sleek bottom sheet for adding custom items
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomItemBottomSheet(
    onDismiss: () -> Unit,
    onAddItem: (CustomItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // State for form fields
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemSku by remember { mutableStateOf("") }
    var itemBarcode by remember { mutableStateOf("") }
    var itemTaxRate by remember { mutableStateOf("5") }
    var itemNotes by remember { mutableStateOf("") }

    // Validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    // Focus states
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Animation state
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "scale"
    )

    // Launch effect to focus on name field when sheet opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .scale(animatedScale)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
    ) {
        // Bottom sheet handle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }

        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Add Custom Item",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Form content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Item Name
            FormField(
                label = "Item Name",
                value = itemName,
                onValueChange = {
                    itemName = it
                    nameError = if (it.isBlank()) "Item name is required" else null
                },
                placeholder = "Enter item name",
                leadingIcon = Icons.Default.ShoppingBag,
                isError = nameError != null,
                errorText = nameError,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Price and Tax Rate Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Item Price
                FormField(
                    label = "Price (AED)",
                    value = itemPrice,
                    onValueChange = {
                        // Only allow numbers and decimal point
                        if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            itemPrice = it
                            priceError = if (it.isBlank()) "Price is required" else null
                        }
                    },
                    placeholder = "0.00",
                    leadingIcon = Icons.Default.AttachMoney,
                    isError = priceError != null,
                    errorText = priceError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(2f)
                )

                // Tax Rate
                FormField(
                    label = "Tax (%)",
                    value = itemTaxRate,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            itemTaxRate = it
                        }
                    },
                    placeholder = "5.0",
                    leadingIcon = Icons.Default.Percent,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SKU and Barcode
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // SKU
                FormField(
                    label = "SKU (Optional)",
                    value = itemSku,
                    onValueChange = { itemSku = it },
                    placeholder = "SKU",
                    leadingIcon = Icons.Default.Tag,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Barcode
                FormField(
                    label = "Barcode (Optional)",
                    value = itemBarcode,
                    onValueChange = { itemBarcode = it },
                    placeholder = "Barcode",
                    leadingIcon = Icons.Default.QrCode,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notes
            FormField(
                label = "Notes (Optional)",
                value = itemNotes,
                onValueChange = { itemNotes = it },
                placeholder = "Additional notes",
                leadingIcon = Icons.Default.Notes,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Price summary
            if (itemPrice.isNotBlank()) {
                val price = itemPrice.toDoubleOrNull() ?: 0.0
                val taxPercent = itemTaxRate.toDoubleOrNull() ?: 0.0
                val taxAmount = price * (taxPercent / 100)
                val totalPrice = price + taxAmount

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Base Price:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "AED ${String.format("%.2f", price)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (taxAmount > 0) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Tax (${String.format("%.1f", taxPercent)}%):",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "AED ${String.format("%.2f", taxAmount)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total Price:",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "AED ${String.format("%.2f", totalPrice)}",
                                style = PriceTextMedium.copy(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 8.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    // Validate form
                    var isValid = true

                    if (itemName.isBlank()) {
                        nameError = "Item name is required"
                        isValid = false
                    }

                    if (itemPrice.isBlank()) {
                        priceError = "Price is required"
                        isValid = false
                    }

                    if (isValid) {
                        val customItem = CustomItem(
                            name = itemName,
                            price = itemPrice.toDoubleOrNull() ?: 0.0,
                            sku = itemSku.ifBlank { null },
                            barcode = itemBarcode.ifBlank { null },
                            taxRate = itemTaxRate.toDoubleOrNull() ?: 5.0,
                            notes = itemNotes.ifBlank { null }
                        )
                        onAddItem(customItem)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = itemName.isNotBlank() && itemPrice.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Item")
            }
        }
    }
}

/**
 * Reusable form field component
 */
@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                focusedLabelColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (isError && errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
            )
        }
    }
}

/**
 * Data class for custom item
 */
data class CustomItem(
    val name: String,
    val price: Double,
    val sku: String? = null,
    val barcode: String? = null,
    val taxRate: Double = 5.0,
    val notes: String? = null
)