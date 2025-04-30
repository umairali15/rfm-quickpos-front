// app/src/main/java/com/rfm/quickpos/presentation/common/components/ConnectivityBanner.kt

package com.rfm.quickpos.presentation.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * Status of network connectivity and sync
 */
enum class ConnectivityStatus {
    ONLINE,       // Connected to the network, fully synced
    OFFLINE,      // No network connection
    SYNCING,      // Connected, actively syncing data
    PENDING_SYNC  // Connected but waiting to sync
}

/**
 * Banner that displays connectivity status across the app
 * Used in both Cashier and Kiosk modes
 */
@Composable
fun ConnectivityBanner(
    status: ConnectivityStatus,
    pendingSyncCount: Int = 0,
    onRetryClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Only show the banner when not fully online
    val shouldShow = status != ConnectivityStatus.ONLINE

    // Rotation animation for syncing icon
    val infiniteTransition = rememberInfiniteTransition(label = "sync-animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val (backgroundColor, contentColor, icon, message) = when (status) {
        ConnectivityStatus.ONLINE -> Quadruple(
            Color.Transparent,
            Color.Transparent,
            Icons.Default.CloudSync,
            ""
        )
        ConnectivityStatus.OFFLINE -> Quadruple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.WifiOff,
            "You're offline. Data will sync when connection is restored."
        )
        ConnectivityStatus.SYNCING -> Quadruple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.Sync,
            "Syncing data with server..."
        )
        ConnectivityStatus.PENDING_SYNC -> Quadruple(
            MaterialTheme.posColors.warningContainer,
            MaterialTheme.posColors.onWarningContainer,
            Icons.Default.HourglassTop,
            "Waiting to sync $pendingSyncCount ${if (pendingSyncCount == 1) "change" else "changes"}"
        )
    }

    AnimatedVisibility(
        visible = shouldShow,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (status == ConnectivityStatus.SYNCING) rotation else 0f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (status == ConnectivityStatus.OFFLINE || status == ConnectivityStatus.PENDING_SYNC) {
                    Spacer(modifier = Modifier.width(8.dp))

                    RfmTextButton(
                        text = "Retry",
                        onClick = onRetryClick
                    )
                }
            }
        }
    }
}

// Helper class for returning multiple values
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Preview(showBackground = true)
@Composable
fun ConnectivityBannerOfflinePreview() {
    RFMQuickPOSTheme {
        Surface {
            ConnectivityBanner(
                status = ConnectivityStatus.OFFLINE
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectivityBannerSyncingPreview() {
    RFMQuickPOSTheme {
        Surface {
            ConnectivityBanner(
                status = ConnectivityStatus.SYNCING
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectivityBannerPendingSyncPreview() {
    RFMQuickPOSTheme {
        Surface {
            ConnectivityBanner(
                status = ConnectivityStatus.PENDING_SYNC,
                pendingSyncCount = 5
            )
        }
    }
}