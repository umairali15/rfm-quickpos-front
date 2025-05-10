// app/src/main/java/com/rfm/quickpos/presentation/features/cart/CartItemWithModifiers.kt

package com.rfm.quickpos.presentation.features.cart

/**
 * Enhanced cart item that supports both variations and modifiers
 */
data class CartItemWithModifiers(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val variations: Map<String, String> = emptyMap(), // variation name to selected option name
    val modifiers: List<ModifierData> = emptyList(),
    val notes: String = "",
    val discountPercentage: Int = 0,
    val priceOverride: Double? = null
) {
    /**
     * Data class for modifier information
     */
    data class ModifierData(
        val id: String,
        val name: String,
        val price: Double,
        val groupName: String
    )

    /**
     * Calculate the total unit price including variations, modifiers and overrides
     */
    val effectiveUnitPrice: Double
        get() {
            var price = priceOverride ?: this.price

            // Add modifier prices
            price += modifiers.sumOf { it.price }

            // Apply discount
            if (discountPercentage > 0) {
                price *= (1 - discountPercentage / 100.0)
            }

            return price
        }

    /**
     * Calculate the total line price
     */
    val totalPrice: Double
        get() = effectiveUnitPrice * quantity

    /**
     * Get a formatted string of all customizations
     */
    val customizationString: String
        get() {
            val parts = mutableListOf<String>()

            // Add variations
            if (variations.isNotEmpty()) {
                variations.forEach { (variationName, optionName) ->
                    parts.add("$variationName: $optionName")
                }
            }

            // Add modifiers
            if (modifiers.isNotEmpty()) {
                modifiers.forEach { modifier ->
                    val priceStr = if (modifier.price > 0) " (+${String.format("%.2f", modifier.price)})" else ""
                    parts.add("${modifier.name}$priceStr")
                }
            }

            // Add notes
            if (notes.isNotBlank()) {
                parts.add("Notes: $notes")
            }

            return parts.joinToString(", ")
        }

    /**
     * Check if this item has any customizations
     */
    val hasCustomizations: Boolean
        get() = variations.isNotEmpty() || modifiers.isNotEmpty() || notes.isNotBlank() ||
                discountPercentage > 0 || priceOverride != null
}