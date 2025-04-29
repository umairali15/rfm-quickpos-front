package com.rfm.quickpos.presentation.features.home

/**
 * Data class for dashboard metrics display
 */
data class DashboardMetrics(
    val totalSales: String,
    val salesAmount: String,
    val customers: String,
    val dateRange: String,
    val paymentMethodChart: PaymentMethodsData
)

/**
 * Data for payment methods chart
 */
data class PaymentMethodsData(
    val cashPercentage: Float,
    val cardPercentage: Float
)