package com.rfm.quickpos.presentation.common.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Simple implementation of the RFM QuickPOS logo for the app bar
 * This is a simpler version that doesn't require the SimpleLogo.kt file
 */
@Composable
fun AppBarLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = "RFM",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        if (!compact) {
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "QuickPOS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}