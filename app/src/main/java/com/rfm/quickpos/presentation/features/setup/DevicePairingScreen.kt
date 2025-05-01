// app/src/main/java/com/rfm/quickpos/presentation/features/setup/DevicePairingScreen.kt

package com.rfm.quickpos.presentation.features.setup

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.R
import com.rfm.quickpos.domain.model.DevicePairingInfo
import com.rfm.quickpos.domain.model.PairingStatus
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmOutlinedButton
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme

/**
 * Screen for device pairing during first boot or manual setup
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicePairingScreen(
    state: DevicePairingState,
    onPairingInfoChange: (DevicePairingInfo) -> Unit,
    onPairingSubmit: () -> Unit,
    onSkipSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Get device serial number if available
    val deviceSerial = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Build.getSerial()
            } catch (e: Exception) {
                "Unknown Serial"
            }
        } else {
            Build.SERIAL ?: "Unknown Serial"
        }
    }

    // Update device serial if not already set
    LaunchedEffect(deviceSerial) {
        if (state.pairingInfo.deviceSerial.isEmpty()) {
            onPairingInfoChange(state.pairingInfo.copy(deviceSerial = deviceSerial))
        }
    }

    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
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
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )

            // Title
            Text(
                text = "Device Setup",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Register this device with your RFM QuickPOS account",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Pairing form
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Terminal Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Device Serial Field (Non-editable)
                    OutlinedTextField(
                        value = state.pairingInfo.deviceSerial,
                        onValueChange = { /* Read-only */ },
                        label = { Text("Device Serial") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null
                            )
                        },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Merchant ID Field
                    OutlinedTextField(
                        value = state.pairingInfo.merchantId,
                        onValueChange = {
                            onPairingInfoChange(state.pairingInfo.copy(merchantId = it))
                        },
                        label = { Text("Merchant ID (MID)") },
                        placeholder = { Text("Enter Merchant ID") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        isError = state.status == PairingStatus.ERROR && state.pairingInfo.merchantId.isBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Terminal ID Field
                    OutlinedTextField(
                        value = state.pairingInfo.terminalId,
                        onValueChange = {
                            onPairingInfoChange(state.pairingInfo.copy(terminalId = it))
                        },
                        label = { Text("Terminal ID (TID)") },
                        placeholder = { Text("Enter Terminal ID") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        isError = state.status == PairingStatus.ERROR && state.pairingInfo.terminalId.isBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Device Name Field (Optional)
                    OutlinedTextField(
                        value = state.pairingInfo.deviceName,
                        onValueChange = {
                            onPairingInfoChange(state.pairingInfo.copy(deviceName = it))
                        },
                        label = { Text("Device Name (Optional)") },
                        placeholder = { Text("Enter a friendly name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Label,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (isFormValid(state.pairingInfo)) {
                                    onPairingSubmit()
                                }
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error message
                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = state.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Pair button
                    RfmPrimaryButton(
                        text = if (state.isLoading) "Pairing..." else "Register Device",
                        onClick = onPairingSubmit,
                        enabled = isFormValid(state.pairingInfo) && !state.isLoading,
                        fullWidth = true,
                        leadingIcon = Icons.Default.AddToQueue
                    )

                    if (state.isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Skip button (for development or pre-configured devices)
            RfmOutlinedButton(
                text = "Skip for Testing",
                onClick = onSkipSetup,
                fullWidth = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info text
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Your MID and TID should be provided by your administrator. " +
                                "Contact support if you don't have this information.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Check if the pairing form has valid input
 */
private fun isFormValid(info: DevicePairingInfo): Boolean {
    return info.merchantId.isNotBlank() && info.terminalId.isNotBlank()
}

@Preview(showBackground = true)
@Composable
fun DevicePairingScreenPreview() {
    RFMQuickPOSTheme {
        DevicePairingScreen(
            state = DevicePairingState(
                pairingInfo = DevicePairingInfo(
                    deviceSerial = "ABCD1234EFGH5678",
                    merchantId = "",
                    terminalId = "",
                    deviceName = ""
                )
            ),
            onPairingInfoChange = {},
            onPairingSubmit = {},
            onSkipSetup = {}
        )
    }
}