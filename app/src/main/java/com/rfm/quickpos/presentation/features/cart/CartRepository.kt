// app/src/main/java/com/rfm/quickpos/presentation/features/cart/CartRepository.kt

package com.rfm.quickpos.presentation.features.cart

import androidx.compose.runtime.mutableStateOf
import com.rfm.quickpos.data.remote.models.Item
import com.rfm.quickpos.presentation.features.catalog.CustomItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing cart state and operations
 */
class CartRepository {

    private val _cartItems = MutableStateFlow<List<CartItemWithModifiers>>(emptyList())
    val cartItems: StateFlow<List<CartItemWithModifiers>> = _cartItems.asStateFlow()

    private val _cartCount = MutableStateFlow(0)
    val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> = _cartTotal.asStateFlow()

    /**
     * Add item to cart from catalog
     */
    fun addItemToCart(item: Item, quantity: Int = 1, modifiers: List<String> = emptyList()) {
        val cartItem = CartItemWithModifiers(
            id = item.id,
            name = item.name,
            price = item.price,
            quantity = quantity,
            modifiers = modifiers.map { modifierId ->
                // This should be filled from actual modifier data
                CartItemWithModifiers.ModifierData(
                    id = modifierId,
                    name = "Custom Modifier",
                    price = 0.0,
                    groupName = "Custom"
                )
            }
        )

        addCartItem(cartItem)
    }

    /**
     * Add custom item to cart
     */
    fun addCustomItemToCart(customItem: CustomItem) {
        val cartItem = CartItemWithModifiers(
            id = "custom_${System.currentTimeMillis()}",
            name = customItem.name,
            price = customItem.price,
            quantity = 1,
            modifiers = emptyList(),
            notes = customItem.notes.toString()
        )

        addCartItem(cartItem)
    }

    /**
     * Add cart item and update totals
     */
    private fun addCartItem(cartItem: CartItemWithModifiers) {
        val currentItems = _cartItems.value.toMutableList()

        // Check if item already exists
        val existingItemIndex = currentItems.indexOfFirst {
            it.id == cartItem.id && it.modifiers == cartItem.modifiers
        }

        if (existingItemIndex != -1) {
            // Update quantity if item already exists
            currentItems[existingItemIndex] = currentItems[existingItemIndex].copy(
                quantity = currentItems[existingItemIndex].quantity + cartItem.quantity
            )
        } else {
            // Add new item
            currentItems.add(cartItem)
        }

        _cartItems.value = currentItems
        updateCartSummary()
    }

    /**
     * Remove item from cart
     */
    fun removeItem(itemId: String) {
        _cartItems.value = _cartItems.value.filter { it.id != itemId }
        updateCartSummary()
    }

    /**
     * Update item quantity
     */
    fun updateItemQuantity(itemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(itemId)
            return
        }

        _cartItems.value = _cartItems.value.map { item ->
            if (item.id == itemId) {
                item.copy(quantity = newQuantity)
            } else {
                item
            }
        }
        updateCartSummary()
    }

    /**
     * Clear cart
     */
    fun clearCart() {
        _cartItems.value = emptyList()
        updateCartSummary()
    }

    /**
     * Update cart summary (count and total)
     */
    private fun updateCartSummary() {
        val items = _cartItems.value
        _cartCount.value = items.sumOf { it.quantity }
        _cartTotal.value = items.sumOf { item ->
            val itemPrice = item.price + item.modifiers.sumOf { it.price }
            itemPrice * item.quantity
        }
    }
}

