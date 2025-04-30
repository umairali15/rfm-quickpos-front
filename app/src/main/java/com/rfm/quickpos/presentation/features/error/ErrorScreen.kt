// app/src/main/java/com/rfm/quickpos/presentation/features/error/ErrorScreen.kt

package com.rfm.quickpos.presentation.features.error

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.R
import com.rfm.quickpos.presentation.common.components.RfmOutlinedButton
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme

/**
 * Types of errors that can occur in the app
 */
enum class ErrorType {
    NETWORK,       // No internet connection
    SERVER,        // Server error (5xx)
    MAINTENANCE,   // System under maintenance
    UNKNOWN,       // Unspecified error
    DEVICE         // Device-specific error
}

/**
 * Screen displayed when a critical error occurs
 * Used in both Cashier and Kiosk modes
 */
@Composable
fun ErrorScreen(
    errorType: ErrorType,
    errorCode: String? = null,
    errorMessage: String? = null,
    onRetryClick: () -> Unit,
    onContactSupportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
        )
    )

    // Configure based on error type
    val (icon, title, description) = when(errorType) {
        ErrorType.NETWORK -> Triple(
            Icons.Default.WifiOff,
            "Network Connection Error",
            "Unable to connect to the server. Please check your internet connection and try again."
        )
        ErrorType.SERVER -> Triple(
            Icons.Default.Whatshot,
            "Server Error",
            "We're experiencing some issues with our servers. Our team is working to resolve this as soon as possible."
        )
        ErrorType.MAINTENANCE -> Triple(
            Icons.Default.Warning,
            "System Maintenance",
            "RFM QuickPOS is currently undergoing scheduled maintenance. Please try again later."
        )
        ErrorType.DEVICE -> Triple(
            Icons.Default.BrokenImage,
            "Device Error",
            "There was a problem with your device. Please restart the application and try again."
        )
        ErrorType.UNKNOWN -> Triple(
            Icons.Default.ErrorOutline,
            "Something Went Wrong",
            "An unexpected error occurred. Please try again or contact support if the problem persists."
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.rfm_quickpos_logo),
                contentDescription = "RFM QuickPOS Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
            )

            // Error icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error description
            Text(
                text = errorMessage ?: description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error code if available
            if (errorCode != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Error Code: $errorCode",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action buttons
            RfmPrimaryButton(
                text = "Try Again",
                onClick = onRetryClick,
                fullWidth = true,
                leadingIcon = Icons.Default.QrCode
            )

            Spacer(modifier = Modifier.height(12.dp))

            RfmOutlinedButton(
                text = "Contact Support",
                onClick = onContactSupportClick,
                fullWidth = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenNetworkPreview() {
    RFMQuickPOSTheme {
        Surface {
            ErrorScreen(
                errorType = ErrorType.NETWORK,
                onRetryClick = {},
                onContactSupportClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenMaintenancePreview() {
    RFMQuickPOSTheme {
        Surface {
            ErrorScreen(
                errorType = ErrorType.MAINTENANCE,
                errorCode = "MAINT-2025-04-30",
                onRetryClick = {},
                onContactSupportClick = {}
            )
        }
    }
}