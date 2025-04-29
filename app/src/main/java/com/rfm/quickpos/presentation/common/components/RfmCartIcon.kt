package com.rfm.quickpos.presentation.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Enhanced cart icon with properly positioned badge counter
 * Fixes the issue where the badge was being cut off
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RfmCartIcon(
    count: Int,
    icon: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(modifier = modifier.padding(8.dp)) {
        BadgedBox(
            badge = {
                if (count > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        // Increased badge size
                        modifier = Modifier.size(if (count > 9) 20.dp else 18.dp)
                    ) {
                        // Use smaller text if double-digit
                        Text(
                            text = if (count > 99) "99+" else count.toString(),
                            fontSize = if (count > 9) 10.sp else 12.sp,
                            modifier = Modifier.padding(
                                // Added horizontal padding for double digits
                                horizontal = if (count > 9) 1.dp else 0.dp
                            )
                        )
                    }
                }
            },
            modifier = Modifier.padding(top = 4.dp, end = 4.dp) // Added padding to prevent cut-off
        ) {
            androidx.compose.material3.IconButton(
                onClick = onClick,
                modifier = Modifier.size(48.dp) // Increased clickable area
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}