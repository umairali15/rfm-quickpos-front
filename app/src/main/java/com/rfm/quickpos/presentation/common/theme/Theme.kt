// app/src/main/java/com/rfm/quickpos/presentation/common/theme/Theme.kt
package com.rfm.quickpos.presentation.common.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


/**
 * RFM QuickPOS theme with Material 3 and custom color palette
 */
@Composable
fun RFMQuickPOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> RfmDarkColorScheme
        else -> RfmLightColorScheme
    }

    // Create the POS color palette based on dark/light theme
    val posColorPalette = if (darkTheme) posDarkColorPalette() else PosColorPalette()

    // Set the status bar color
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Provide our custom color palette alongside Material 3 theme
    CompositionLocalProvider(
        LocalPosColorPalette provides posColorPalette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RfmTypography,
            shapes = RfmShapes,
            content = content
        )
    }
}

// Extension property to easily access our POS color palette
val MaterialTheme.posColors: PosColorPalette
    @Composable
    get() = LocalPosColorPalette.current