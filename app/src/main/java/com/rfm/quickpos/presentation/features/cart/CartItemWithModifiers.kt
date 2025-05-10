// app/src/main/java/com/rfm/quickpos/presentation/features/cart/CartItemWithModifiers.kt

package com.rfm.quickpos.presentation.features.cart

import com.rfm.quickpos.data.remote.models.VariationOption

/**
 * Data class representing a cart item with variations/modifiers
 */
data class CartItemWithModifiers(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val modifiers: List<ModifierData> = emptyList(),
    val variations: Map<String, VariationOption> = emptyMap(),
    val notes: String? = null,
    val discountPercentage: Double? = null
) {
    // Calculate total price including variations
    val totalPrice: Double
        get() {
            val basePrice = price * quantity
            val variationsAdjustment = variations.values.sumOf { it.priceAdjustment * quantity }
            val modifiersAdjustment = modifiers.sumOf { it.price * quantity }
            return basePrice + variationsAdjustment + modifiersAdjustment
        }

    data class ModifierData(
        val id: String,
        val name: String,
        val price: Double,
        val groupName: String
    )
}