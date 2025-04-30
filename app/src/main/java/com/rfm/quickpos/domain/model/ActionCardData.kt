package com.rfm.quickpos.presentation.common.models

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class for action card specifications
 */
data class ActionCardData(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val backgroundColor: Color,
    val contentColor: Color
)

/**
 * Factory function to create ActionCardData with default theme colors
 */
@Composable
fun createActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
): ActionCardData {
    return ActionCardData(
        title = title,
        icon = icon,
        onClick = onClick,
        backgroundColor = backgroundColor,
        contentColor = contentColor
    )
}