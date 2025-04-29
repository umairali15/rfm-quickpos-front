package com.rfm.quickpos.presentation.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Primary RFM Loyalty Colors (enhanced for better visibility)
val RfmRed = Color(0xFFB31B1B) // Primary brand color
val RfmRedLight = Color(0xFFE84545) // Lighter for dark theme
val RfmRedDark = Color(0xFF8A0000) // Darker for light theme

// Gray palette refined
val RfmGray = Color(0xFF5A5A5A)
val RfmGrayLight = Color(0xFF989898) // More visible in dark theme
val RfmGrayDark = Color(0xFF333333)

// Enhanced surface colors for better contrast in light mode
val RfmOffWhite = Color(0xFFF8F8F8) // Default background in light mode
val RfmLightGray = Color(0xFFEBEBEB) // Enhanced surface variant light for better borders
val RfmMediumGray = Color(0xFF8E8E8E)
val RfmWhite = Color(0xFFFFFFFF)

// Light Theme Color Scheme (enhanced)
val RfmLightColorScheme = lightColorScheme(
    primary = RfmRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6), // More visible container color
    onPrimaryContainer = RfmRedDark,

    secondary = RfmGray,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6E6E6), // Enhanced for visibility
    onSecondaryContainer = RfmGrayDark,

    tertiary = Color(0xFF0057B8), // RFM Blue
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5E3FF),
    onTertiaryContainer = Color(0xFF001C3B),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = RfmOffWhite,
    onBackground = RfmGrayDark,

    surface = Color.White,
    onSurface = RfmGrayDark,
    surfaceVariant = RfmLightGray,  // More visible in light mode
    onSurfaceVariant = RfmGray,

    outline = RfmMediumGray,
    outlineVariant = RfmLightGray
)

// Dark Theme Color Scheme (enhanced)
val RfmDarkColorScheme = darkColorScheme(
    primary = RfmRedLight,
    onPrimary = Color(0xFF690000),
    primaryContainer = Color(0xFF930000),
    onPrimaryContainer = Color(0xFFFFDAD6),

    secondary = RfmGrayLight,
    onSecondary = Color(0xFF2C2C2C),
    secondaryContainer = Color(0xFF404040),
    onSecondaryContainer = Color(0xFFE0E0E0),

    tertiary = Color(0xFFA5C8FF), // Lighter blue for dark theme
    onTertiary = Color(0xFF00315D),
    tertiaryContainer = Color(0xFF004884),
    onTertiaryContainer = Color(0xFFD5E3FF),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690003),
    errorContainer = Color(0xFF930006),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF1A1A1A),
    onBackground = Color.White,

    surface = Color(0xFF252525),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2F2F2F),
    onSurfaceVariant = Color(0xFFD5D5D5),

    outline = Color(0xFF979797),
    outlineVariant = Color(0xFF494949)
)

// Custom POS color palette enhanced for better visibility
data class PosColorPalette(
    // Success State Colors
    val success: Color = Color(0xFF00BF69),
    val onSuccess: Color = Color.White,
    val successContainer: Color = Color(0xFFBCF4D9), // More visible in light mode
    val onSuccessContainer: Color = Color(0xFF003917),

    // Warning State Colors
    val warning: Color = Color(0xFFFFC107),
    val onWarning: Color = Color(0xFF261A00),
    val warningContainer: Color = Color(0xFFFFECB7),
    val onWarningContainer: Color = Color(0xFF261900),

    // Discount State Colors
    val discount: Color = Color(0xFF00BF69),
    val onDiscount: Color = Color.White,

    // Payment method icons
    val cashIcon: Color = Color(0xFF00BF69),
    val cardIcon: Color = Color(0xFF0057B8),

    // Notification colors
    val info: Color = Color(0xFF0057B8),
    val pending: Color = Color(0xFFFF6B00),

    // Receipt colors
    val receiptBackground: Color = Color.White,
    val receiptText: Color = Color(0xFF121212),

    // Status indicators
    val activeStatus: Color = Color(0xFF00BF69),
    val inactiveStatus: Color = Color(0xFF999999),

    // Product grid
    val productCardBackground: Color = Color.White,
    val categoryChipBackground: Color = Color(0xFFEBEBEB), // Enhanced for visibility
    val selectedCategoryChip: Color = RfmRed,
    val onSelectedCategoryChip: Color = Color.White,

    // Button variants
    val secondaryButton: Color = Color(0xFFF0F0F0), // Enhanced for visibility
    val onSecondaryButton: Color = Color(0xFF333333),
    val outlinedButton: Color = Color.Transparent,
    val onOutlinedButton: Color = RfmRed,

    // Card borders for light mode
    val cardBorder: Color = Color(0xFFE0E0E0),  // Light border for cards
    val cardShadow: Color = Color(0x1A000000)   // Subtle shadow for depth
)

// Create a composition local provider for our extended color palette
val LocalPosColorPalette = staticCompositionLocalOf { PosColorPalette() }

// Dark mode version of our POS color palette (enhanced)
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
        receiptText = Color(0xFFF5F5F5),

        activeStatus = Color(0xFF59DD95),
        inactiveStatus = Color(0xFF5A5A5A),

        productCardBackground = Color(0xFF2D2D2D),
        categoryChipBackground = Color(0xFF3D3D3D),
        selectedCategoryChip = RfmRedLight,
        onSelectedCategoryChip = Color(0xFF000000),

        secondaryButton = Color(0xFF383838),
        onSecondaryButton = Color(0xFFF5F5F5),
        outlinedButton = Color.Transparent,
        onOutlinedButton = RfmRedLight,

        cardBorder: Color = Color(0xFF3D3D3D),   // Darker border for cards
    cardShadow: Color = Color(0x33000000)    // Stronger shadow for depth
    )
}