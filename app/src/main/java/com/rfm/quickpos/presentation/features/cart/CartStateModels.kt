// app/src/main/java/com/rfm/quickpos/presentation/features/cart/CartStateModels.kt

package com.rfm.quickpos.presentation.features.cart



/**
 * Basic cart item for legacy compatibility
 */
data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val discountPercentage: Int? = null,
    val notes: String? = null
) {
    val totalPrice: Double
        get() {
            val subtotal = price * quantity
            return if (discountPercentage != null) {
                subtotal * (1 - discountPercentage / 100.0)
            } else {
                subtotal
            }
        }
}