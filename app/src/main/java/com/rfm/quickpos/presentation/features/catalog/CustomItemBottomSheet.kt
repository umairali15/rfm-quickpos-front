// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/CustomItemBottomSheet.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomItemBottomSheet(
    onDismiss: () -> Unit,
    onAddCustomItem: (name: String, price: Double, quantity: Int) -> Unit,
    onAddSaleAmount: (amount: Double) -> Unit, // New callback for sale amount
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Custom Item", "Sale Amount")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(4.dp)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.small
                )
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add to Sale",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> CustomItemTab(onAddCustomItem, onDismiss)
            1 -> SaleAmountTab(onAddSaleAmount, onDismiss)
        }
    }
}

@Composable
private fun CustomItemTab(
    onAddCustomItem: (String, Double, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableIntStateOf(1) }
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Item Name
        RfmTextField(
            value = itemName,
            onValueChange = {
                itemName = it
                nameError = false
            },
            label = "Item Name",
            placeholder = "Enter item name",
            isError = nameError,
            errorText = if (nameError) "Please enter item name" else null,
            modifier = Modifier.fillMaxWidth()
        )

        // Item Price
        RfmTextField(
            value = itemPrice,
            onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                    itemPrice = it
                    priceError = false
                }
            },
            label = "Price (AED)",
            placeholder = "Enter price",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = priceError,
            errorText = if (priceError) "Please enter a valid price" else null,
            modifier = Modifier.fillMaxWidth()
        )

        // Quantity
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Quantity",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { if (itemQuantity > 1) itemQuantity-- },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }

                Text(
                    text = itemQuantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { itemQuantity++ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }

        // Total Price Display
        val price = itemPrice.toDoubleOrNull() ?: 0.0
        val total = price * itemQuantity

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "AED ${String.format("%.2f", total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Add Button
        RfmPrimaryButton(
            text = "Add to Sale",
            onClick = {
                when {
                    itemName.isBlank() -> nameError = true
                    itemPrice.toDoubleOrNull() == null -> priceError = true
                    else -> {
                        onAddCustomItem(itemName, price, itemQuantity)
                        onDismiss()
                    }
                }
            },
            leadingIcon = Icons.Default.Add,
            fullWidth = true,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SaleAmountTab(
    onAddSaleAmount: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var saleAmount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sale Amount Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Column {
                    Text(
                        text = "Manual Sale Entry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Enter a sale amount directly without selecting catalog items. This creates a generic sale entry.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Sale Amount Input
        RfmTextField(
            value = saleAmount,
            onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                    saleAmount = it
                    amountError = false
                }
            },
            label = "Sale Amount (AED)",
            placeholder = "Enter total sale amount",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = amountError,
            errorText = if (amountError) "Please enter a valid amount" else null,
            modifier = Modifier.fillMaxWidth()
        )

        // Preview
        val amount = saleAmount.toDoubleOrNull() ?: 0.0
        if (amount > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sale Amount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "AED ${String.format("%.2f", amount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Add Button
        RfmPrimaryButton(
            text = "Proceed to Payment",
            onClick = {
                val parsedAmount = saleAmount.toDoubleOrNull()
                if (parsedAmount != null && parsedAmount > 0) {
                    onAddSaleAmount(parsedAmount)
                    onDismiss()
                } else {
                    amountError = true
                }
            },
            leadingIcon = Icons.Default.AttachMoney,
            fullWidth = true,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}