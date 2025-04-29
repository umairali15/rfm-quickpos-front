// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/CatalogModels.kt
package com.rfm.quickpos.presentation.features.catalog

/**
 * Data class representing a product in the catalog
 */
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val categoryId: String,
    val barcode: String? = null,
    val imageUrl: String? = null,
    val discountPercentage: Int? = null,
    val isActive: Boolean = true
)

/**
 * Data class representing a product category
 */
data class ProductCategory(
    val id: String,
    val name: String,
    val sortOrder: Int = 0
)

/**
 * State for the catalog screen
 */
data class CatalogState(
    val isLoading: Boolean = false,
    val categories: List<ProductCategory> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val cartItemCount: Int = 0,
    val error: String? = null
)