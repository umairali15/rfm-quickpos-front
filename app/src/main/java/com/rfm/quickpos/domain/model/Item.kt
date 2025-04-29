package com.rfm.quickpos.domain.model

data class Item(
    val id: String,
    val name: String,
    val price: Double,
    val barcode: String?
)
