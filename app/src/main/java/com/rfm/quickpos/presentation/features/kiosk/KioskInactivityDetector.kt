package com.rfm.quickpos.presentation.features.kiosk

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.rfm.quickpos.domain.manager.InactivityManager
import kotlinx.coroutines.delay

/**
 * Wrapper composable that detects user inactivity in kiosk mode
 * and automatically redirects to the attract screen after timeout
 */
@Composable
fun KioskInactivityDetector(
    onTimeout: () -> Unit,
    timeoutMillis: Long = InactivityManager.DEFAULT_TIMEOUT_MILLIS,
    content: @Composable (Modifier) -> Unit
) {
    // Create InactivityManager
    val inactivityManager = remember { InactivityManager(timeoutMillis, onTimeout) }

    // Start inactivity manager when component is composed
    LaunchedEffect(true) {
        inactivityManager.start()
    }

    // Clean up when component is disposed
    DisposableEffect(true) {
        onDispose {
            inactivityManager.stop()
        }
    }

    // Create tap detector modifier
    val inactivityModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onTap = { inactivityManager.reset() },
            onDoubleTap = { inactivityManager.reset() },
            onLongPress = { inactivityManager.reset() },
            onPress = {
                try {
                    // Reset on press down
                    inactivityManager.reset()
                    awaitRelease()
                    // Reset on press up
                    inactivityManager.reset()
                } catch (e: Exception) {
                    // Cancelled, do nothing
                }
            }
        )
    }

    // Render content with the inactivity modifier
    content(inactivityModifier)
}

/**
 * Extension function to add inactivity detection to any Modifier
 */
fun Modifier.detectKioskInactivity(inactivityManager: InactivityManager): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures(
            onTap = { inactivityManager.reset() },
            onDoubleTap = { inactivityManager.reset() },
            onLongPress = { inactivityManager.reset() },
            onPress = {
                try {
                    inactivityManager.reset()
                    awaitRelease()
                    inactivityManager.reset()
                } catch (e: Exception) {
                    // Cancelled, do nothing
                }
            }
        )
    }
}