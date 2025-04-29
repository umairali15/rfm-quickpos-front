package com.rfm.quickpos.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.theme.ButtonShape
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * Primary button with RFM styling
 */
@Composable
fun RfmPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 12.dp, horizontal = 24.dp),
    fullWidth: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = if (fullWidth) modifier.fillMaxWidth() else modifier,
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = contentPadding
    ) {
        leadingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Secondary button with RFM styling
 */
@Composable
fun RfmSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 12.dp, horizontal = 24.dp),
    fullWidth: Boolean = false
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = if (fullWidth) modifier.fillMaxWidth() else modifier,
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.posColors.secondaryButton,
            contentColor = MaterialTheme.posColors.onSecondaryButton,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = contentPadding
    ) {
        leadingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Outlined button with RFM styling
 */
@Composable
fun RfmOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 12.dp, horizontal = 24.dp),
    fullWidth: Boolean = false,
    borderColor: Color = MaterialTheme.colorScheme.primary
) {
    OutlinedButton(
        onClick = onClick,
        modifier = if (fullWidth) modifier.fillMaxWidth() else modifier,
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.posColors.onOutlinedButton,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = BorderStroke(1.dp, if (enabled) borderColor else MaterialTheme.colorScheme.outline),
        contentPadding = contentPadding
    ) {
        leadingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Text button with RFM styling
 */
@Composable
fun RfmTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding
    ) {
        leadingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Pay button - Special case for checkout flow
 */
@Composable
fun RfmPayButton(
    amount: String,
    currencyCode: String = "AED",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pay",
                style = MaterialTheme.typography.titleMedium
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "$currencyCode $amount",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ButtonsPreview() {
    RFMQuickPOSTheme {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            RfmPrimaryButton(
                text = "Primary Button",
                onClick = {},
                fullWidth = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            RfmSecondaryButton(
                text = "Secondary Button",
                onClick = {}
            )

            Spacer(modifier = Modifier.height(8.dp))

            RfmOutlinedButton(
                text = "Outlined Button",
                onClick = {}
            )

            Spacer(modifier = Modifier.height(8.dp))

            RfmTextButton(
                text = "Text Button",
                onClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            RfmPayButton(
                amount = "244.00",
                onClick = {}
            )
        }
    }
}