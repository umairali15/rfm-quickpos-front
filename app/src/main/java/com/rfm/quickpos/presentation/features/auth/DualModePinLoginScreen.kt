// app/src/main/java/com/rfm/quickpos/presentation/features/auth/DualModePinLoginScreen.kt

package com.rfm.quickpos.presentation.features.auth

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.R
import com.rfm.quickpos.domain.model.UiMode
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.QuickPOSApplication

private const val TAG = "DualModePinLoginScreen"

@Composable
fun DualModePinLoginScreen(
    onPinSubmit: (pin: String, mode: UiMode) -> Unit,
    onBackToEmailLogin: () -> Unit,
    onDevicePairing: () -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    // Get application context
    val context = LocalContext.current
    val app = context.applicationContext as QuickPOSApplication

    // Create view model
    val viewModel = remember {
        PinLoginViewModel(app.authRepository, app.deviceRepository)
    }

    // Collect states
    val viewState by viewModel.viewState.collectAsState()
    val uiMode by viewModel.uiMode.collectAsState()

    // Handle authentication result
    LaunchedEffect(viewState.isAuthenticated) {
        Log.d(TAG, "Authentication state changed: ${viewState.isAuthenticated}")
        if (viewState.isAuthenticated) {
            Log.d(TAG, "Authentication successful, calling onPinSubmit")
            onPinSubmit("", uiMode)
        }
    }

    // Add email state
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(UiMode.CASHIER) }
    var isEditingMode by remember { mutableStateOf(false) }

    // Update error message display
    var showError by remember { mutableStateOf(false) }
    val currentError = viewState.errorMessage ?: errorMessage

    // Update error state when error message changes
    LaunchedEffect(currentError) {
        showError = currentError != null
    }

    // Get screen configuration
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isSmallScreen = screenHeight < 700.dp

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
                .padding(horizontal = 24.dp)
                .padding(
                    top = if (isSmallScreen) 16.dp else 24.dp,
                    bottom = if (isSmallScreen) 16.dp else 24.dp
                )
        ) {
            // Logo - smaller on small screens
            Image(
                painter = painterResource(id = R.drawable.rfm_quickpos_logo),
                contentDescription = "RFM QuickPOS Logo",
                modifier = Modifier
                    .size(if (isSmallScreen) 80.dp else 120.dp)
                    .padding(bottom = if (isSmallScreen) 8.dp else 16.dp)
            )

            // Title
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isSmallScreen) 24.sp else 28.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(if (isSmallScreen) 4.dp else 8.dp))

            // Subtitle
            Text(
                text = "Enter your email and PIN to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(if (isSmallScreen) 12.dp else 24.dp))

            // Error message
            AnimatedVisibility(
                visible = showError,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isSmallScreen) 8.dp else 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = currentError ?: "Please enter both email and PIN.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Loading indicator
            if (viewState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(if (isSmallScreen) 8.dp else 16.dp)
                )
            }

            // Email input field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isSmallScreen) 8.dp else 16.dp)
            )

            // Mode selector (Cashier/Kiosk)
            if (isEditingMode) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isSmallScreen) 8.dp else 16.dp)
                ) {
                    // Cashier mode
                    ModeButton(
                        title = "Cashier",
                        icon = Icons.Default.Person,
                        isSelected = mode == UiMode.CASHIER,
                        onClick = { mode = UiMode.CASHIER }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Kiosk mode
                    ModeButton(
                        title = "Kiosk",
                        icon = Icons.Default.Storefront,
                        isSelected = mode == UiMode.KIOSK,
                        onClick = { mode = UiMode.KIOSK }
                    )
                }
            } else {
                // Show current mode
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isSmallScreen) 8.dp else 16.dp)
                        .clickable { isEditingMode = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (mode == UiMode.CASHIER) Icons.Default.Person else Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Mode: ${mode.name.lowercase().capitalize()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Mode",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // PIN display
            Text(
                text = pin.replace(".".toRegex(), "•"),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (isSmallScreen) 8.dp else 16.dp)
            )

            // PIN keypad - made responsive
            PinKeypad(
                pin = pin,
                onPinChange = { pin = it },
                onSubmit = {
                    if (pin.length >= 4 && email.isNotEmpty()) {
                        viewModel.authenticateWithPin(email, pin, mode)
                    } else {
                        viewModel.clearError()
                        showError = true
                    }
                },
                isLoading = viewState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isSmallScreen) 0.dp else 24.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (isSmallScreen) 8.dp else 16.dp)
            ) {
                // Email login button
                TextButton(
                    onClick = onBackToEmailLogin
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Email Login")
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Device Pairing button
                TextButton(
                    onClick = onDevicePairing
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Device Setup")
                    }
                }
            }
        }
    }
}

/**
 * Responsive PIN keypad component
 */
@Composable
private fun PinKeypad(
    pin: String,
    onPinChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isSmallScreen = screenHeight < 700.dp
    val buttonSpacing = if (isSmallScreen) 8.dp else 16.dp

    Column(
        verticalArrangement = Arrangement.spacedBy(buttonSpacing),
        modifier = modifier
    ) {
        // Row 1: 1, 2, 3
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            PinButton(
                digit = "1",
                onClick = { onPinChange(pin + "1") },
                modifier = Modifier.weight(1f)
            )
            PinButton(
                digit = "2",
                onClick = { onPinChange(pin + "2") },
                modifier = Modifier.weight(1f)
            )
            PinButton(
                digit = "3",
                onClick = { onPinChange(pin + "3") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: 4, 5, 6
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            PinButton(
                digit = "4",
                onClick = { onPinChange(pin + "4") },
                modifier = Modifier.weight(1f)
            )
            PinButton(
                digit = "5",
                onClick = { onPinChange(pin + "5") },
                modifier = Modifier.weight(1f)
            )
            PinButton(
                digit = "6",
                onClick = { onPinChange(pin + "6") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: 7, 8, 9
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            PinButton(
                digit = "7",
                onClick = { onPinChange(pin + "7") },
                modifier = Modifier.weight(1f)
            )
            PinButton(
                digit = "8",
                onClick = { onPinChange(pin + "8") },
                modifier = Modifier.weight(1f)
            )
            PinButton(
                digit = "9",
                onClick = { onPinChange(pin + "9") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: Delete, 0, Submit
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Delete button
            IconButton(
                onClick = {
                    if (pin.isNotEmpty()) {
                        onPinChange(pin.substring(0, pin.length - 1))
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // 0 digit
            PinButton(
                digit = "0",
                onClick = { onPinChange(pin + "0") },
                modifier = Modifier.weight(1f)
            )

            // Submit button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                IconButton(
                    onClick = onSubmit,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Submit",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * PIN keypad button component
 */
@Composable
private fun PinButton(
    digit: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isSmallScreen = screenHeight < 700.dp

    IconButton(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = if (isSmallScreen) 20.sp else 24.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Mode selection button component
 */
@Composable
private fun ModeButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        },
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}