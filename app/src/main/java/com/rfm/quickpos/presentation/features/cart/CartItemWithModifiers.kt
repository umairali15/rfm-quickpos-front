// app/src/main/java/com/rfm/quickpos/presentation/features/cart/CartItemWithModifiers.kt

package com.rfm.quickpos.presentation.features.cart

import com.rfm.quickpos.data.remote.models.VariationOption

/**
 * Cart item that includes variations and modifiers
 */
data class CartItemWithModifiers(
    val id: String,
    val name: String,
    val price: Double, // Base price
    val quantity: Int,
    val variations: Map<String, VariationOption> = emptyMap(), // variationName to selected option
    val modifiers: List<ModifierData> = emptyList(), // List of selected modifiers
    val notes: String? = null,
    val discountPercentage: Int? = null
) {
    /**
     * Data class for modifier information in cart
     */
    data class ModifierData(
        val groupId: String,
        val groupName: String,
        val modifierId: String,
        val modifierName: String,
        val priceAdjustment: Double = 0.0,
        val quantity: Int = 1
    )

    /**
     * Calculate total price including variations and modifiers
     */
    val totalPrice: Double
        get() {
            val variationAdjustments = variations.values.sumOf { it.priceAdjustment }
            val modifierAdjustments = modifiers.sumOf { it.priceAdjustment * it.quantity }
            val unitPrice = price + variationAdjustments + modifierAdjustments
            val subtotal = unitPrice * quantity

            return if (discountPercentage != null) {
                subtotal * (1 - discountPercentage / 100.0)
            } else {
                subtotal
            }
        }
}