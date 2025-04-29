package com.rfm.quickpos.domain.model

/**
 * Represents the UI mode of the application.
 * The app can run in either Cashier mode (staff-facing) or Kiosk mode (customer-facing).
 */
enum class UiMode {
    CASHIER,
    KIOSK
}