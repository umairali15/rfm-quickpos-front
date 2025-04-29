package com.rfm.quickpos.presentation.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.rfm.quickpos.domain.manager.FeatureFlagManager
import com.rfm.quickpos.domain.model.UiMode

/**
 * Local composition provider for feature flags
 */
val LocalFeatureFlagManager = compositionLocalOf<FeatureFlagManager?> { null }

/**
 * Provides feature flags to the composition via CompositionLocal
 */
@Composable
fun FeatureFlagProvider(
    uiMode: UiMode,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val featureFlagManager = remember { FeatureFlagManager(context) }

    CompositionLocalProvider(
        LocalFeatureFlagManager provides featureFlagManager,
        content = content
    )
}

/**
 * Composable that conditionally renders content based on a feature flag
 */
@Composable
fun FeatureFlag(
    featureKey: String,
    uiMode: UiMode,
    content: @Composable () -> Unit
) {
    val featureFlagManager = LocalFeatureFlagManager.current

    // Only render content if feature is enabled
    if (featureFlagManager?.isFeatureEnabled(featureKey, uiMode) == true) {
        content()
    }
}

/**
 * Extension function to check if a feature is enabled
 */
@Composable
fun isFeatureEnabled(featureKey: String, uiMode: UiMode): Boolean {
    val featureFlagManager = LocalFeatureFlagManager.current
    return featureFlagManager?.isFeatureEnabled(featureKey, uiMode) ?: false
}