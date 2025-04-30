// app/src/main/java/com/rfm/quickpos/presentation/features/shift/CashMovementScreen.kt

package com.rfm.quickpos.presentation.features.shift

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Type of cash movement
 */
enum class CashMovementType {
    CASH_IN,
    CASH_OUT
}

/**
 * Predefined reasons for cash movements
 */
enum class CashMovementReason(val display: String) {
    PETTY_CASH("Petty Cash"),
    BANK_DEPOSIT("Bank Deposit"),
    REFUND("Manual Refund"),
    PAID_OUT("Vendor Payment"),
    TIP_PAYOUT("Tip Payout"),
    CORRECTION("Correction"),
    OTHER("Other")
}

/**
 * Screen for recording cash in or cash out transactions during a shift
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashMovementScreen(
    onBackClick: () -> Unit,
    onCashMovementSubmit: (type: CashMovementType, amount: Double, reason: String, note: String) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var movementType by remember { mutableStateOf(CashMovementType.CASH_IN) }
    var amount by remember { mutableStateOf("0.00") }
    var isAmountValid by remember { mutableStateOf(false) }
    var reasonExpanded by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf(CashMovementReason.PETTY_CASH) }
    var note by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    // Format the current date and time for display
    val currentDateTime = remember {
        SimpleDateFormat("EEEE, MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date())
    }

    // Validate input when values change
    LaunchedEffect(amount) {
        val amountValue = amount.toDoubleOrNull() ?: 0.0
        isAmountValid = amountValue > 0.0 && !isLoading
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (movementType == CashMovementType.CASH_IN) "Cash In" else "Cash Out",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Current date/time
            Text(
                text = currentDateTime,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Movement type selector
            Text(
                text = "Movement Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = movementType == CashMovementType.CASH_IN,
                    onClick = { movementType = CashMovementType.CASH_IN },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.posColors.success,
                        activeContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cash In")
                }

                SegmentedButton(
                    selected = movementType == CashMovementType.CASH_OUT,
                    onClick = { movementType = CashMovementType.CASH_OUT },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.error,
                        activeContentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cash Out")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Amount field
            Text(
                text = "Amount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { input ->
                    // Format as currency
                    val filtered = input.replace(Regex("[^0-9.]"), "")
                    val parts = filtered.split(".")
                    if (parts.size <= 2 && (parts.size != 2 || parts[1].length <= 2)) {
                        amount = filtered
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reason dropdown
            Text(
                text = "Reason",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = reasonExpanded,
                onExpandedChange = { reasonExpanded = !reasonExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedReason.display,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonExpanded) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = reasonExpanded,
                    onDismissRequest = { reasonExpanded = false }
                ) {
                    CashMovementReason.values().forEach { reason ->
                        DropdownMenuItem(
                            text = { Text(reason.display) },
                            onClick = {
                                selectedReason = reason
                                reasonExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Note field
            Text(
                text = "Note (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Add additional details") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Submit button
            RfmPrimaryButton(
                text = "",                   // ignored because we supply a custom slot
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    onCashMovementSubmit(
                        movementType,
                        amountValue,
                        selectedReason.display,
                        note
                    )
                },
                enabled   = isAmountValid,
                fullWidth = true
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        Modifier.size(24.dp).padding(end = 8.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Processing…", style = MaterialTheme.typography.labelLarge)
                } else {
                    Text(
                        "Submit " +
                                if (movementType == CashMovementType.CASH_IN) "Cash In" else "Cash Out",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }



        }
    }
}

@Preview(showBackground = true)
@Composable
fun CashInScreenPreview() {
    RFMQuickPOSTheme {
        Surface {
            CashMovementScreen(
                onBackClick = {},
                onCashMovementSubmit = { _, _, _, _ -> }
            )
        }
    }
}