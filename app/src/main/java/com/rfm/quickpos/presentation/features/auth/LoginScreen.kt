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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.R
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmTextField
import com.rfm.quickpos.presentation.common.components.RfmTextButton
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme

/**
 * Login screen with email/password authentication
 */
@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    onSwitchToPinLogin: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Sign in to continue",
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

            // Email field
            RfmTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            RfmTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onLoginClick(email, password)
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Forgot password
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                RfmTextButton(
                    text = "Forgot Password?",
                    onClick = onForgotPasswordClick
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login button
            RfmPrimaryButton(
                text = "Sign In",
                onClick = { onLoginClick(email, password) },
                fullWidth = true,
                enabled = email.isNotBlank() && password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PIN login option
            RfmTextButton(
                text = "Use PIN Login Instead",
                onClick = onSwitchToPinLogin
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

/**
 * PIN-based login screen for cashiers/managers
 */
@Composable
fun PinLoginScreen(
    onPinSubmit: (pin: String) -> Unit,
    onBackToEmailLogin: () -> Unit,
    modifier: Modifier = Modifier,
    userName: String = "Cashier",
    errorMessage: String? = null
) {
    var pin by remember { mutableStateOf("") }

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
                text = "Welcome, $userName",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Enter your PIN to continue",
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
                            onPinSubmit(pin)
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Submit button
            RfmPrimaryButton(
                text = "Submit PIN",
                onClick = { onPinSubmit(pin) },
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

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    RFMQuickPOSTheme {
        Surface {
            LoginScreen(
                onLoginClick = { _, _ -> },
                onForgotPasswordClick = { }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PinLoginScreenPreview() {
    RFMQuickPOSTheme {
        Surface {
            PinLoginScreen(
                onPinSubmit = { },
                onBackToEmailLogin = { },
                userName = "Ahmed"
            )
        }
    }
}

@Preview(showBackground = true, name = "Login Error State")
@Composable
fun LoginScreenErrorPreview() {
    RFMQuickPOSTheme {
        Surface {
            LoginScreen(
                onLoginClick = { _, _ -> },
                onForgotPasswordClick = { },
                errorMessage = "Invalid email or password. Please try again."
            )
        }
    }
}