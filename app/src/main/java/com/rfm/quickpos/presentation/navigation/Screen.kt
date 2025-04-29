package com.rfm.quickpos.presentation.navigation

/**
 * Screen routes for navigation in RFM QuickPOS
 */
sealed class Screen(val route: String) {
    // Authentication
    object Splash : Screen("splash")
    object Login : Screen("login")
    object PinEntry : Screen("pin_entry")

    // Main flow
    object Home : Screen("home")
    object Catalog : Screen("catalog")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object Checkout : Screen("checkout")
    object Payment : Screen("payment/{amount}") {
        fun createRoute(amount: Double) = "payment/$amount"
    }
    object PaymentSuccess : Screen("payment_success/{transactionId}") {
        fun createRoute(transactionId: String) = "payment_success/$transactionId"
    }

    // Sales Management
    object OrderHistory : Screen("order_history")
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
    object Refund : Screen("refund/{orderId}") {
        fun createRoute(orderId: String) = "refund/$orderId"
    }

    // Reports
    object Reports : Screen("reports")
    object SalesReport : Screen("sales_report")
    object ProductReport : Screen("product_report")

    // Settings & User Management
    object Settings : Screen("settings")
    object UserProfile : Screen("user_profile")
    object CompanySettings : Screen("company_settings")
    object DeviceSettings : Screen("device_settings")

    // Cash & Shift Management
    object OpenShift : Screen("open_shift")
    object CloseShift : Screen("close_shift")
    object CashIn : Screen("cash_in")
    object CashOut : Screen("cash_out")

    // Customer Management
    object Customers : Screen("customers")
    object CustomerDetail : Screen("customer_detail/{customerId}") {
        fun createRoute(customerId: String) = "customer_detail/$customerId"
    }
}