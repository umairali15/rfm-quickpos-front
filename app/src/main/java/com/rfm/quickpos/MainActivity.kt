package com.rfm.quickpos

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.rfm.quickpos.domain.manager.UiModeManager
import com.rfm.quickpos.domain.model.UiMode
import com.rfm.quickpos.presentation.common.components.FeatureFlagProvider
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.navigation.AppNavigationWithDualMode

class MainActivity : ComponentActivity() {
    // Create UiModeManager
    private lateinit var uiModeManager: UiModeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize UiModeManager
        uiModeManager = UiModeManager(this)

        setContent {
            // Get current UI mode
            val uiMode by uiModeManager.currentMode.collectAsState()

            // Apply system UI changes based on mode
            ApplySystemUIChanges(uiMode)

            RFMQuickPOSTheme {
                // Provide feature flags based on UI mode
                FeatureFlagProvider(uiMode = uiMode) {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Use the combined navigation with UI mode awareness
                        val navController = rememberNavController()
                        AppNavigationWithDualMode(
                            navController = navController,
                            startingMode = uiMode
                        )
                    }
                }
            }
        }
    }

    /**
     * Apply system UI changes based on the UI mode
     */
    @Composable
    private fun ApplySystemUIChanges(uiMode: UiMode) {
        if (uiMode == UiMode.KIOSK) {
            // For kiosk mode, apply physical lockdown
            // Keep screen on
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            // Implement immersive mode for kiosk
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // In a real app with proper device owner setup, we would use:
            // startLockTask()

            // For testing only: hide system bars without lock task mode
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
                hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // For cashier mode, use normal window behavior
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            WindowCompat.setDecorFitsSystemWindows(window, true)

            // Stop lock task mode if active
            try {
                stopLockTask()
            } catch (e: Exception) {
                // Not in lock task mode, ignore
            }

            // Show system bars
            WindowCompat.getInsetsController(window, window.decorView).apply {
                show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
    }

    companion object {
        const val ACTION_SET_KIOSK_MODE = "com.rfm.quickpos.SET_KIOSK_MODE"
        const val EXTRA_ENABLE_KIOSK = "enable_kiosk"
    }
}