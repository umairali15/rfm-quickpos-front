package com.rfm.quickpos.presentation.common.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light Theme Color Scheme
private val RfmLightColorScheme = lightColorScheme(
    primary = RfmRed,
    onPrimary = RfmWhite,
    primaryContainer = TransparentRfmRed,
    onPrimaryContainer = RfmRedDark,

    secondary = RfmGray,
    onSecondary = RfmWhite,
    secondaryContainer = RfmLightGray,
    onSecondaryContainer = RfmGrayDark,

    tertiary = RfmBlue,
    onTertiary = RfmWhite,
    tertiaryContainer = Color(0xFFD5E3FF),
    onTertiaryContainer = Color(0xFF001C3B),

    error = ErrorColor,
    onError = RfmWhite,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = RfmOffWhite,
    onBackground = RfmBlack,

    surface = SurfaceLight,
    onSurface = RfmBlack,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = RfmDarkGray,

    outline = RfmMediumGray,
    outlineVariant = RfmLightGray
)

// Dark Theme Color Scheme
private val RfmDarkColorScheme = darkColorScheme(
    primary = RfmRedLight,
    onPrimary = Color(0xFF690000),
    primaryContainer = RfmRed,
    onPrimaryContainer = Color(0xFFFFDAD6),

    secondary = RfmGrayLight,
    onSecondary = Color(0xFF303030),
    secondaryContainer = RfmGray,
    onSecondaryContainer = Color(0xFFE0E0E0),

    tertiary = Color(0xFFA5C8FF),
    onTertiary = Color(0xFF00315D),
    tertiaryContainer = Color(0xFF004884),
    onTertiaryContainer = Color(0xFFD5E3FF),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690003),
    errorContainer = Color(0xFF930006),
    onErrorContainer = Color(0xFFFFDAD6),

    background = RfmBlack,
    onBackground = RfmOffWhite,

    surface = SurfaceDark,
    onSurface = RfmOffWhite,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = RfmLightGray,

    outline = RfmMediumGray,
    outlineVariant = RfmDarkGray
)

// Custom POS color palette for extended design system needs
data class PosColorPalette(
    // Success State Colors
    val success: Color = SuccessColor,
    val onSuccess: Color = RfmWhite,
    val successContainer: Color = TransparentRfmGreen,
    val onSuccessContainer: Color = Color(0xFF003917),

    // Warning State Colors
    val warning: Color = WarningColor,
    val onWarning: Color = RfmBlack,
    val warningContainer: Color = Color(0xFFFFECB7),
    val onWarningContainer: Color = Color(0xFF261900),

    // Discount State Colors
    val discount: Color = DiscountColor,
    val onDiscount: Color = RfmWhite,

    // Payment method icons
    val cashIcon: Color = RfmGreen,
    val cardIcon: Color = RfmBlue,

    // Notification colors
    val info: Color = InfoColor,
    val pending: Color = PendingColor,

    // Receipt colors
    val receiptBackground: Color = RfmWhite,
    val receiptText: Color = RfmBlack,

    // Status indicators
    val activeStatus: Color = RfmGreen,
    val inactiveStatus: Color = RfmMediumGray,

    // Product grid
    val productCardBackground: Color = RfmWhite,
    val categoryChipBackground: Color = RfmLightGray,
    val selectedCategoryChip: Color = RfmRed,
    val onSelectedCategoryChip: Color = RfmWhite,

    // Button variants
    val secondaryButton: Color = RfmOffWhite,
    val onSecondaryButton: Color = RfmGrayDark,
    val outlinedButton: Color = Color.Transparent,
    val onOutlinedButton: Color = RfmRed
)

// Create a composition local provider for our extended color palette
val LocalPosColorPalette = staticCompositionLocalOf { PosColorPalette() }

// Dark mode version of our POS color palette
private fun posDarkColorPalette(): PosColorPalette {
    return PosColorPalette(
        success = Color(0xFF59DD95),
        onSuccess = Color(0xFF003917),
        successContainer = Color(0xFF00522A),
        onSuccessContainer = Color(0xFF83FBB1),

        warning = Color(0xFFFFC955),
        onWarning = Color(0xFF412D00),
        warningContainer = Color(0xFF5C4200),
        onWarningContainer = Color(0xFFFFE4C0),

        discount = Color(0xFF59DD95),
        onDiscount = Color(0xFF003917),

        cashIcon = Color(0xFF59DD95),
        cardIcon = Color(0xFFA5C8FF),

        info = Color(0xFFA5C8FF),
        pending = Color(0xFFFFB77D),

        receiptBackground = Color(0xFF2D2D2D),
        receiptText = RfmOffWhite,

        activeStatus = Color(0xFF59DD95),
        inactiveStatus = Color(0xFF5A5A5A),

        productCardBackground = Color(0xFF2D2D2D),
        categoryChipBackground = Color(0xFF3D3D3D),
        selectedCategoryChip = RfmRedLight,
        onSelectedCategoryChip = Color(0xFF000000),

        secondaryButton = Color(0xFF383838),
        onSecondaryButton = RfmOffWhite,
        outlinedButton = Color.Transparent,
        onOutlinedButton = RfmRedLight
    )
}

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