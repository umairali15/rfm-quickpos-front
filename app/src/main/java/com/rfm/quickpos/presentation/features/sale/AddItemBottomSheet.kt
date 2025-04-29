package com.rfm.quickpos.presentation.features.sale

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.theme.BottomSheetShape
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme

/**
 * Bottom sheet for adding items to a sale with various options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemBottomSheet(
    onDismiss: () -> Unit,
    onCatalogItemClick: () -> Unit,
    onScanClick: () -> Unit,
    onNonCatalogItemClick: () -> Unit,
    onDiscountClick: () -> Unit,
    onCommentClick: () -> Unit,
    onCustomerClick: () -> Unit,
    currentSaleNumber: String? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = BottomSheetShape,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Sale number if provided
            if (currentSaleNumber != null) {
                Text(
                    text = currentSaleNumber,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                Divider()
            }

            // Add section
            Text(
                text = "Add",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Catalog items option
            BottomSheetOption(
                icon = Icons.Default.ShoppingBag,
                title = "Catalog items",
                onClick = onCatalogItemClick
            )

            // Scan option
            BottomSheetOption(
                icon = Icons.Default.QrCodeScanner,
                title = "Scan",
                onClick = onScanClick
            )

            // Non-catalog item option
            BottomSheetOption(
                icon = Icons.Default.Calculate,
                title = "Non catalog item",
                onClick = onNonCatalogItemClick
            )

            // Discount option
            BottomSheetOption(
                icon = Icons.Default.LocalOffer,
                title = "Discount",
                onClick = onDiscountClick
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            // Additional Information section
            Text(
                text = "Additional Information",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Comment option
            BottomSheetOption(
                icon = Icons.Default.Comment,
                title = "Comment",
                onClick = onCommentClick
            )

            // Customer option
            BottomSheetOption(
                icon = Icons.Default.Person,
                title = "Customer",
                onClick = onCustomerClick
            )
        }
    }
}

/**
 * Single option item in the bottom sheet
 */
@Composable
fun BottomSheetOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Non-catalog item entry bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonCatalogItemSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = BottomSheetShape,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Add Custom Item",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Custom item form would go here
            // For now just showing a placeholder
            Text(
                text = "Form fields for name, price, quantity, etc. would go here",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Discount entry bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = BottomSheetShape,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Add Discount",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Discount form would go here
            Text(
                text = "Form fields for discount type (percentage/amount) and value would go here",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Comment entry bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = BottomSheetShape,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Add Comment",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Comment text field would go here
            Text(
                text = "Multi-line text field for comment would go here",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddItemBottomSheetPreview() {
    RFMQuickPOSTheme {
        Surface {
            // Can't directly preview ModalBottomSheet due to preview limitations
            // Showing just the content instead
            Column {
                // Sale number
                Text(
                    text = "5917-1610-174122",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                Divider()

                // Add section
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                BottomSheetOption(
                    icon = Icons.Default.ShoppingBag,
                    title = "Catalog items",
                    onClick = {}
                )

                BottomSheetOption(
                    icon = Icons.Default.QrCodeScanner,
                    title = "Scan",
                    onClick = {}
                )

                BottomSheetOption(
                    icon = Icons.Default.Calculate,
                    title = "Non catalog item",
                    onClick = {}
                )

                BottomSheetOption(
                    icon = Icons.Default.LocalOffer,
                    title = "Discount",
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider()

                Text(
                    text = "Additional Information",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                BottomSheetOption(
                    icon = Icons.Default.Comment,
                    title = "Comment",
                    onClick = {}
                )

                BottomSheetOption(
                    icon = Icons.Default.Person,
                    title = "Customer",
                    onClick = {}
                )
            }
        }
    }
}