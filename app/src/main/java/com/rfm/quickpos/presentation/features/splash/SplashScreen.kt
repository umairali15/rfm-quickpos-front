// app/src/main/java/com/rfm/quickpos/presentation/features/splash/SplashScreen.kt

package com.rfm.quickpos.presentation.features.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.R
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.RfmRed
import kotlinx.coroutines.delay

/**
 * Splash screen shown during app startup
 * Used for both Cashier and Kiosk modes
 */
@Composable
fun SplashScreen(
    onInitializationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var loadingText by remember { mutableStateOf("Initializing...") }
    var progress by remember { mutableStateOf(0f) }

    // Animation for fade-in effect
    val alpha by animateFloatAsState(
        targetValue = if (isLoading) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "alpha"
    )

    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
        )
    )

    // Simulate app initialization
    LaunchedEffect(Unit) {
        val steps = listOf(
            "Checking device status...",
            "Connecting to server...",
            "Loading catalog...",
            "Preparing workspace..."
        )

        for (i in steps.indices) {
            loadingText = steps[i]
            progress = (i + 1) / steps.size.toFloat()
            delay(800) // Simulate work being done
        }

        delay(500) // Short delay before navigating away
        isLoading = false
        delay(500) // Allow fade-out animation to play
        onInitializationComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .alpha(alpha)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.rfm_quickpos_logo),
                contentDescription = "RFM QuickPOS Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App Name
            Text(
                text = "RFM QuickPOS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = RfmRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Version
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator
            CircularProgressIndicator(
                progress = { progress },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loading text
            Text(
                text = loadingText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        }

        // Copyright text at bottom
        Text(
            text = "© 2025 RFM Loyalty. All rights reserved.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    RFMQuickPOSTheme {
        Surface {
            SplashScreen(
                onInitializationComplete = {}
            )
        }
    }
}