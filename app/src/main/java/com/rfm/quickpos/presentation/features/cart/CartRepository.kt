// app/src/main/java/com/rfm/quickpos/presentation/features/cart/CartRepository.kt

package com.rfm.quickpos.presentation.features.cart

import androidx.compose.runtime.mutableStateOf
import com.rfm.quickpos.data.remote.models.Item
import com.rfm.quickpos.data.remote.models.ItemVariationOption
import com.rfm.quickpos.data.remote.models.VariationOption
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
     * Add item to cart from catalog with proper variations and modifiers
     */
    fun addItemToCart(
        item: Item,
        quantity: Int = 1,
        selectedVariations: Map<String, ItemVariationOption> = emptyMap(),
        selectedModifierIds: Map<String, Set<String>> = emptyMap() // groupId to set of modifierIds
    ) {
        // Convert variations to the expected format
        val variationsForCart = selectedVariations.mapValues { entry ->
            VariationOption(
                name = entry.value.name,
                priceAdjustment = entry.value.priceAdjustment
            )
        }

        // Convert modifiers to the expected format
        val modifiersForCart = item.modifierGroups?.flatMap { group ->
            group.modifiers.filter { modifier ->
                selectedModifierIds[group.id]?.contains(modifier.id) == true
            }.map { modifier ->
                CartItemWithModifiers.ModifierData(
                    groupId = group.id,
                    groupName = group.name,
                    modifierId = modifier.id,
                    modifierName = modifier.name,
                    priceAdjustment = modifier.priceAdjustment
                )
            }
        } ?: emptyList()

        val cartItem = CartItemWithModifiers(
            id = item.id,
            name = item.name,
            price = item.price,
            quantity = quantity,
            variations = variationsForCart,
            modifiers = modifiersForCart
        )

        addCartItem(cartItem)
    }

    /**
     * Simple add to cart for items without variations/modifiers
     */
    fun addSimpleItemToCart(item: Item, quantity: Int = 1) {
        val cartItem = CartItemWithModifiers(
            id = item.id,
            name = item.name,
            price = item.price,
            quantity = quantity,
            variations = emptyMap(),
            modifiers = emptyList()
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
            variations = emptyMap(),
            modifiers = emptyList(),
            notes = customItem.notes.toString()
        )

        addCartItem(cartItem)
    }

    /**
     * Add cart item and update totals
     */
    fun addCartItem(cartItem: CartItemWithModifiers) {
        val currentItems = _cartItems.value.toMutableList()

        // Check if item already exists with same variations and modifiers
        val existingItemIndex = currentItems.indexOfFirst {
            it.id == cartItem.id &&
                    it.variations == cartItem.variations &&
                    it.modifiers == cartItem.modifiers
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
            // Calculate base price + variations + modifiers
            val basePrice = item.price
            val variationsPrice = item.variations.values.sumOf { it.priceAdjustment }
            val modifiersPrice = item.modifiers.sumOf { it.priceAdjustment }
            val totalUnitPrice = basePrice + variationsPrice + modifiersPrice

            totalUnitPrice * item.quantity
        }
    }

    /**
     * Get total price for a specific cart item
     */
    fun getItemTotal(item: CartItemWithModifiers): Double {
        val basePrice = item.price
        val variationsPrice = item.variations.values.sumOf { it.priceAdjustment }
        val modifiersPrice = item.modifiers.sumOf { it.priceAdjustment }
        val totalUnitPrice = basePrice + variationsPrice + modifiersPrice

        return totalUnitPrice * item.quantity
    }
}