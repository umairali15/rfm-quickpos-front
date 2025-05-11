// app/src/main/java/com/rfm/quickpos/presentation/features/kiosk/AttractScreen.kt

package com.rfm.quickpos.presentation.features.kiosk

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.QuickPOSApplication
import com.rfm.quickpos.R
import com.rfm.quickpos.domain.model.UiMode
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme

/**
 * Enhanced attract screen with company branding
 */
@Composable
fun AttractScreen(
    onStartOrderClick: () -> Unit,
    onExitKioskMode: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var pinValue by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // Get company info from catalog repository
    val context = LocalContext.current
    val catalogRepository = (context.applicationContext as QuickPOSApplication).catalogRepository
    val companyInfo by catalogRepository.companyInfo.collectAsState()

    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
        )
    )

    // Animation for the start button
    val buttonAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000),
        label = "button-alpha"
    )

    // PIN dialog for exiting kiosk mode
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Enter Manager PIN") },
            text = {
                Column {
                    Text("Enter PIN to exit Kiosk mode")

                    OutlinedTextField(
                        value = pinValue,
                        onValueChange = {
                            pinValue = it
                            pinError = false
                        },
                        isError = pinError,
                        label = { Text("Manager PIN") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    if (pinError) {
                        Text(
                            "Invalid PIN. Please try again.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // For testing: PIN 1234 switches to Cashier mode
                        if (pinValue == "1234" && onExitKioskMode != null) {
                            onExitKioskMode()
                            showExitDialog = false
                        } else {
                            pinError = true
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Settings button for exiting kiosk mode (for development)
        if (onExitKioskMode != null) {
            IconButton(
                onClick = { showExitDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.rfm_quickpos_logo),
                contentDescription = "RFM QuickPOS Logo",
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Company name if available
            companyInfo?.name?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Welcome message
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tap below to start your order",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Start order button with animation
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .alpha(buttonAlpha)
                    .clip(RoundedCornerShape(24.dp))
                    .fillMaxWidth(0.8f)
            ) {
                Button(
                    onClick = onStartOrderClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    Text(
                        text = "Start Order",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Business type indicator if available
            companyInfo?.businessType?.let { businessType ->
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = businessType.replaceFirstChar { it.titlecase() },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AttractScreenPreview() {
    RFMQuickPOSTheme {
        Surface {
            AttractScreen(
                onStartOrderClick = {},
                onExitKioskMode = {}
            )
        }
    }
}