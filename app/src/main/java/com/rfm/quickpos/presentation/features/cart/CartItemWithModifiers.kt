// app/src/main/java/com/rfm/quickpos/presentation/features/cart/CartItemWithModifiers.kt

package com.rfm.quickpos.presentation.features.cart

/**
 * Cart item model that supports modifiers for business type specific functionality
 */
data class CartItemWithModifiers(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val discountPercentage: Int? = null,
    val notes: String? = null,
    val modifiers: List<ModifierData> = emptyList()
) {
    /**
     * Modifier data for cart items
     */
    data class ModifierData(
        val id: String,
        val name: String,
        val price: Double,
        val groupName: String
    )

    /**
     * Calculate total price including modifiers
     */
    fun getTotalPrice(): Double {
        val basePrice = price * quantity
        val modifiersPrice = modifiers.sumOf { it.price } * quantity
        return basePrice + modifiersPrice
    }

    /**
     * Get formatted modifier string for display
     */
    fun getModifiersText(): String {
        if (modifiers.isEmpty()) return ""

        // Group modifiers by group name
        val groupedModifiers = modifiers.groupBy { it.groupName }

        return groupedModifiers.entries.joinToString("\n") { (groupName, mods) ->
            "$groupName: ${mods.joinToString(", ") { it.name }}"
        }
    }
}