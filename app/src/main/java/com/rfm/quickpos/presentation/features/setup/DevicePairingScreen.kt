// app/src/main/java/com/rfm/quickpos/presentation/features/setup/DevicePairingScreen.kt

package com.rfm.quickpos.presentation.features.setup

import android.os.Build
import android.util.Log
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.R
import com.rfm.quickpos.domain.model.DevicePairingInfo
import com.rfm.quickpos.domain.model.PairingStatus

private const val TAG = "DevicePairingScreen"

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
    var showSerialEditor by remember { mutableStateOf(false) }

    // Log state changes
    LaunchedEffect(state.status, state.isPaired) {
        Log.d(TAG, "Pairing state updated: ${state.status}, isPaired: ${state.isPaired}")

        if (state.isPaired) {
            Log.d(TAG, "Device pairing successful, should navigate to next screen")
        }

        if (state.status == PairingStatus.ERROR) {
            Log.e(TAG, "Device pairing error: ${state.errorMessage}")
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

                    // Device Serial Field (Editable by toggle)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Serial Number",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.weight(1f)
                        )

                        Switch(
                            checked = showSerialEditor,
                            onCheckedChange = { showSerialEditor = it }
                        )
                    }

                    if (showSerialEditor) {
                        // Editable serial field
                        OutlinedTextField(
                            value = state.pairingInfo.deviceSerial,
                            onValueChange = { newSerial ->
                                onPairingInfoChange(state.pairingInfo.copy(deviceSerial = newSerial))
                            },
                            label = { Text("Custom Serial Number") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Non-editable serial display
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Device Alias Field
                    OutlinedTextField(
                        value = state.pairingInfo.deviceAlias,
                        onValueChange = {
                            onPairingInfoChange(state.pairingInfo.copy(deviceAlias = it))
                        },
                        label = { Text("Device Alias (Required)") },
                        placeholder = { Text("Enter Device Name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DeviceHub,
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
                        isError = state.status == PairingStatus.ERROR && state.pairingInfo.deviceAlias.isBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Branch ID Field
                    OutlinedTextField(
                        value = state.pairingInfo.branchId,
                        onValueChange = {
                            onPairingInfoChange(state.pairingInfo.copy(branchId = it))
                        },
                        label = { Text("Branch ID (Required)") },
                        placeholder = { Text("Enter Branch ID") },
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
                        isError = state.status == PairingStatus.ERROR && state.pairingInfo.branchId.isBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Device Model Field (Optional)
                    OutlinedTextField(
                        value = state.pairingInfo.deviceModel.ifEmpty { Build.MODEL },
                        onValueChange = {
                            onPairingInfoChange(state.pairingInfo.copy(deviceModel = it))
                        },
                        label = { Text("Device Model (Optional)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
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
                    Button(
                        onClick = {
                            Log.d(TAG, "Attempting to pair device: ${state.pairingInfo}")
                            onPairingSubmit()
                        },
                        enabled = isFormValid(state.pairingInfo) && !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (state.isLoading) "Registering..." else "Register Device"
                        )
                    }

                    if (state.isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Skip button (for development or pre-configured devices)
            OutlinedButton(
                onClick = onSkipSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Skip for Testing")
            }

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
                        text = "All fields marked as required must be filled to register the device. Contact support if you don't have this information.",
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
    return info.deviceAlias.isNotBlank() && info.branchId.isNotBlank() && info.deviceSerial.isNotBlank()
}