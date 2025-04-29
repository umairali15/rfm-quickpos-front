package com.rfm.quickpos.presentation.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.R
import com.rfm.quickpos.domain.model.UiMode
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmTextField
import com.rfm.quickpos.presentation.common.components.RfmTextButton
import com.rfm.quickpos.presentation.common.theme.ButtonShape

/**
 * Enhanced PIN login screen that supports both cashier and kiosk modes
 * Added proper card borders and elevation for better visibility in light mode
 */
@Composable
fun DualModePinLoginScreen(
    onPinSubmit: (pin: String, mode: UiMode) -> Unit,
    onBackToEmailLogin: () -> Unit,
    modifier: Modifier = Modifier,
    userName: String = "User",
    errorMessage: String? = null
) {
    var pin by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Mode selection state
    var selectedMode by remember { mutableStateOf(UiMode.CASHIER) }

    // Predefined PINs (in a real app, these would be validated against the backend)
    val cashierPin = "1234"
    val kioskPin = "5678"

    // Auto-validate PIN when 4 digits are entered
    LaunchedEffect(pin) {
        if (pin.length == 4) {
            focusManager.clearFocus()

            // Determine mode based on PIN
            val mode = when (pin) {
                cashierPin -> UiMode.CASHIER
                kioskPin -> UiMode.KIOSK
                else -> selectedMode // Keep selected mode if PIN doesn't match known PINs
            }

            onPinSubmit(pin, mode)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.rfm_quickpos_logo),
                contentDescription = "RFM QuickPOS Logo",
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 24.dp)
            )

            // Welcome text
            Text(
                text = "Welcome${if (userName != "User") ", $userName" else ""}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Enter PIN to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // PIN Input Card with elevated styling
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Pin input field
                    RfmTextField(
                        value = pin,
                        onValueChange = { newPin ->
                            // Only accept digits and limit to 4 characters
                            if (newPin.length <= 4 && newPin.all { it.isDigit() }) {
                                pin = newPin
                            }
                        },
                        label = "PIN Code",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (pin.length == 4) {
                                    focusManager.clearFocus()
                                    // Determine mode based on PIN
                                    val mode = when (pin) {
                                        cashierPin -> UiMode.CASHIER
                                        kioskPin -> UiMode.KIOSK
                                        else -> selectedMode
                                    }
                                    onPinSubmit(pin, mode)
                                }
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit button with enhanced styling
                    RfmPrimaryButton(
                        text = "Submit PIN",
                        onClick = {
                            if (pin.length == 4) {
                                // Determine mode based on PIN
                                val mode = when (pin) {
                                    cashierPin -> UiMode.CASHIER
                                    kioskPin -> UiMode.KIOSK
                                    else -> selectedMode
                                }
                                onPinSubmit(pin, mode)
                            }
                        },
                        fullWidth = true,
                        enabled = pin.length == 4,
                        modifier = Modifier.height(56.dp)
                    )
                }
            }

            // Mode selection help text
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Mode Selection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Enter 1234 for Cashier Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Enter 5678 for Kiosk Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch to email login
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = ButtonShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                RfmTextButton(
                    text = "Use Email Login Instead",
                    onClick = onBackToEmailLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Version info
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}