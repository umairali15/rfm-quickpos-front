// app/src/main/java/com/rfm/quickpos/presentation/features/cart/CartRepository.kt

package com.rfm.quickpos.presentation.features.cart

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing cart state
 */
class CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItemWithModifiers>>(emptyList())
    val cartItems: StateFlow<List<CartItemWithModifiers>> = _cartItems.asStateFlow()

    private val _cartCount = MutableStateFlow(0)
    val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> = _cartTotal.asStateFlow()

    /**
     * Add item to cart
     */
    fun addCartItem(item: CartItemWithModifiers) {
        val currentItems = _cartItems.value.toMutableList()

        // Check if item with same variations and modifiers exists
        val existingItemIndex = currentItems.indexOfFirst { cartItem ->
            cartItem.id == item.id &&
                    cartItem.variations == item.variations &&
                    cartItem.modifiers == item.modifiers
        }

        if (existingItemIndex != -1) {
            // Update quantity of existing item
            val existingItem = currentItems[existingItemIndex]
            currentItems[existingItemIndex] = existingItem.copy(
                quantity = existingItem.quantity + item.quantity
            )
        } else {
            // Add new item
            currentItems.add(item)
        }

        updateCart(currentItems)
    }

    /**
     * Remove item from cart
     */
    fun removeItem(itemId: String) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.removeAll { it.id == itemId }
        updateCart(currentItems)
    }

    /**
     * Update item quantity
     */
    fun updateItemQuantity(itemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(itemId)
            return
        }

        val currentItems = _cartItems.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.id == itemId }

        if (itemIndex != -1) {
            currentItems[itemIndex] = currentItems[itemIndex].copy(quantity = newQuantity)
            updateCart(currentItems)
        }
    }

    /**
     * Clear all items from cart
     */
    fun clearCart() {
        updateCart(emptyList())
    }

    /**
     * Update cart state and calculate totals
     */
    private fun updateCart(items: List<CartItemWithModifiers>) {
        _cartItems.value = items
        _cartCount.value = items.sumOf { it.quantity }
        _cartTotal.value = items.sumOf { it.totalPrice }
    }

    /**
     * Get cart item by ID
     */
    fun getCartItem(itemId: String): CartItemWithModifiers? {
        return _cartItems.value.find { it.id == itemId }
    }
}