package com.rfm.quickpos.domain.model

data class Order(
    val id: String,
    val orderNumber: String,
    val total: Double,
    val status: String
)
