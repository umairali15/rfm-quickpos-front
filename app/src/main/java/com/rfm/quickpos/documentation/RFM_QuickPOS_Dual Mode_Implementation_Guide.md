# RFM QuickPOS: Dual Mode Implementation Guide

This document explains the architecture and implementation of the dual mode system in RFM QuickPOS, which supports both Cashier (staff-facing) and Kiosk (customer-facing) modes.

## Overview

RFM QuickPOS can operate in two distinct modes:

1. **Cashier Mode**: Full-featured point-of-sale functionality for staff, including all management capabilities
2. **Kiosk Mode**: Streamlined self-service experience for customers, with limited functionality and automatic reset

Both modes share the core business logic, database, and APIs, but present different UI/UX experiences tailored to their respective users.

## Architecture

The dual mode system is built around the following key components:

### 1. Mode Definition

The `UiMode` enum defines the available modes:

```kotlin
enum class UiMode {
    CASHIER,
    KIOSK
}
```

### 2. Mode Management

The `UiModeManager` handles:
- Determining the current mode
- Persisting mode selection
- Authorizing mode switches

In the current implementation, we use a PIN-based approach for determining mode (PIN 1234 for Cashier, 5678 for Kiosk), but this will eventually be replaced with backend-driven configuration.

### 3. Navigation

Each mode has its own navigation graph:

- `CashierNavGraph`: Full set of screens for staff use
- `KioskNavGraph`: Limited set of screens (Attract → Catalog → Cart → Pay → Success)

The `DualModeNavigation` composable selects the appropriate graph based on the current mode.

### 4. Feature Flags

The `FeatureFlagManager` controls which features are enabled in each mode:

```kotlin
// Example: Check if quantity stepper is enabled in current mode
val isQuantityStepperEnabled = featureFlagManager.isFeatureEnabled(
    FeatureFlagManager.FEATURE_QUANTITY_STEPPER, 
    currentMode
)
```

Defaults are set appropriately for each mode, with Kiosk mode having a more restricted set of features.

### 5. Kiosk-Specific Features

#### Auto Reset

The `InactivityManager` and `KioskInactivityDetector` components handle:
- Monitoring user activity
- Automatically returning to the attract screen after a period of inactivity
- Providing countdown functionality

#### Physical Lockdown

When in Kiosk mode, the app applies security features:
- Enabling Android's Lock Task Mode (when device owner permissions are available)
- Hiding system bars with swipe-to-reveal behavior
- Keeping the screen on

#### UI Adaptations

Kiosk mode UIs are optimized for customer use:
- Larger touch targets
- Simplified workflows
- Clearer instructions
- Center-aligned text
- Limited options

## Implementation Details

### Mode Selection

Users (administrators) can select the mode in two ways:

1. **PIN-based Selection (Demo Mode)**
    - PIN 1234 for Cashier Mode
    - PIN 5678 for Kiosk Mode

2. **Backend-driven Configuration (Production Mode)**
    - Device ID/serial is sent to backend
    - Backend returns device configuration including mode
    - Mode is cached for offline use

### Kiosk-specific Components

1. **Attract Screen**
    - Serves as the start page and idle screen
    - Displays welcome message and "Start Order" button
    - Hidden manager access in corner

2. **Streamlined Catalog**
    - Simplified product browsing
    - Tap-to-add (no quantity steppers)
    - Large category chips

3. **Simplified Cart**
    - Fixed quantities (no edit)
    - No discounts or comments
    - Large checkout button

4. **Card-focused Payment**
    - Card-first payment options
    - No cash handling
    - Optional wallet/QR payments

5. **Auto-print Receipt**
    - Shows printing status
    - Automatically returns to attract screen

### Mode-specific UI Differences

| UI Element | Cashier Mode | Kiosk Mode |
|------------|--------------|------------|
| Navigation | Full drawer/bottom bar | Back button + Cart only |
| Product Catalog | Compact grid with multiple tools | Large grid with tap-to-add |
| Cart | Edit quantities, add notes, discounts | Display only, no editing |
| Payment | All methods (cash, card, split) | Card-only (+ optional wallet) |
| Receipt | Manual print/email buttons | Auto-print, then return to home |
| Timeout | None | Auto-reset after inactivity |

## Connecting to Backend

To connect to a real backend system:

1. Modify `UiModeManager` to:
    - Read the device serial number with `Build.getSerial()`
    - Call your backend API with the serial
    - Parse the response to get the configured mode
    - Cache the result in case of offline use

2. Update `AppNavigationWithDualMode` to:
    - Observe the mode from `UiModeManager`
    - Switch navigation accordingly

## Testing

To test the dual mode functionality:

1. **Mode Switching**: Log in with different PINs
    - PIN 1234 for Cashier Mode
    - PIN 5678 for Kiosk Mode

2. **Kiosk Features**:
    - Test auto-reset by leaving the app inactive
    - Test manager access via the hidden settings button
    - Verify that simplified UI appears correctly

## Future Enhancements

Planned improvements to the dual mode system:

1. **Remote Configuration**: Mode configuration from a central dashboard
2. **Device Provisioning**: Easy setup for new devices with QR code scanning
3. **Feature Overrides**: Per-device feature flag configuration
4. **Analytics**: Mode-specific usage tracking
5. **Theme Customization**: Different themes for each mode

## Conclusion

The dual mode system provides a flexible foundation for both staff and customer-facing interactions using a single codebase. By leveraging feature flags, specialized navigation, and UI adaptations, RFM QuickPOS delivers an experience tailored to each user type.