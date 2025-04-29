package com.rfm.quickpos.presentation.features.kiosk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.R
import com.rfm.quickpos.domain.manager.UiModeManager
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import kotlinx.coroutines.delay

/**
 * Attract screen for Kiosk mode - displays welcome message and "Start Order" button
 */
@Composable
fun AttractScreen(
    onStartOrderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showManagerDialog by remember { mutableStateOf(false) }
    var managerPin by remember { mutableStateOf("") }
    var showInvalidPinError by remember { mutableStateOf(false) }

    // Auto-reset timer
    LaunchedEffect(true) {
        // Reset inactivity timer every time this screen is shown
    }

    // Animation for pulsing effect
    val infiniteTransition = rememberInfiniteTransition(label = "attract-screen-pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Manager settings button in top corner
        IconButton(
            onClick = { showManagerDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Manager Settings",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        // Main content
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
                modifier = Modifier
                    .size(220.dp)
                    .padding(bottom = 32.dp)
            )

            // Welcome text
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Touch to begin your order",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Start order button with animation
            Button(
                onClick = onStartOrderClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(vertical = 20.dp, horizontal = 32.dp),
                modifier = Modifier
                    .scale(scale)
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "START ORDER",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        // Manager dialog
        if (showManagerDialog) {
            AlertDialog(
                onDismissRequest = {
                    showManagerDialog = false
                    managerPin = ""
                    showInvalidPinError = false
                },
                title = {
                    Text("Manager Authentication")
                },
                text = {
                    Column {
                        Text("Enter manager PIN to exit kiosk mode")

                        Spacer(modifier = Modifier.height(16.dp))

                        TextField(
                            value = managerPin,
                            onValueChange = {
                                if (it.length <= 4) {
                                    managerPin = it
                                    showInvalidPinError = false
                                }
                            },
                            label = { Text("Manager PIN") },
                            singleLine = true,
                            isError = showInvalidPinError
                        )

                        if (showInvalidPinError) {
                            Text(
                                text = "Invalid PIN. Please try again.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // In real app, validate against UiModeManager
                            val validManagerPin = "1234" // For demo only
                            if (managerPin == validManagerPin) {
                                // Exit kiosk mode
                                showManagerDialog = false
                                // This would be handled by viewModel in real implementation
                            } else {
                                showInvalidPinError = true
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showManagerDialog = false
                            managerPin = ""
                            showInvalidPinError = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AttractScreenPreview() {
    RFMQuickPOSTheme {
        Surface {
            AttractScreen(
                onStartOrderClick = {}
            )
        }
    }
}