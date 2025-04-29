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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * Enhanced PIN login screen that supports both cashier and kiosk modes
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
                text = "Welcome",
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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

            // Mode selection help text
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter 1234 for Cashier Mode or 5678 for Kiosk Mode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
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
                enabled = pin.length == 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Switch to email login
            RfmTextButton(
                text = "Use Email Login Instead",
                onClick = onBackToEmailLogin
            )

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