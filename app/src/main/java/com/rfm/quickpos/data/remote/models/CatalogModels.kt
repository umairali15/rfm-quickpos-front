// app/src/main/java/com/rfm/quickpos/data/remote/models/CatalogModels.kt

package com.rfm.quickpos.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Company information response
 */
data class CompanyInfoResponse(
    val success: Boolean,
    @SerializedName("company_info") val companyInfo: CompanyInfo,
    @SerializedName("business_type_config") val businessTypeConfig: BusinessTypeConfig? = null,
    val error: String? = null
)

/**
 * Company information
 */
data class CompanyInfo(
    val id: String,
    val name: String,
    @SerializedName("tax_number") val taxNumber: String? = null,
    @SerializedName("contact_email") val contactEmail: String? = null,
    @SerializedName("contact_phone") val contactPhone: String? = null,
    @SerializedName("logo_url") val logoUrl: String? = null,
    @SerializedName("business_type") val businessType: String,
    @SerializedName("schema_name") val schemaName: String
)

/**
 * Business type configuration
 */
data class BusinessTypeConfig(
    val name: String,
    val features: List<String> = emptyList(),
    @SerializedName("supports_modifiers") val supportsModifiers: Boolean = false,
    @SerializedName("supports_tables") val supportsTables: Boolean = false,
    @SerializedName("supports_inventory") val supportsInventory: Boolean = false,
    @SerializedName("supports_time_based_pricing") val supportsTimeBasedPricing: Boolean = false,
    @SerializedName("requires_customer") val requiresCustomer: Boolean = false,
    @SerializedName("ui_configuration") val uiConfiguration: Map<String, Any>? = null
)

/**
 * Category response
 */
data class CategoryResponse(
    val success: Boolean,
    val data: List<Category>,
    val error: String? = null
)

/**
 * Category model
 */
data class Category(
    val id: String,
    val name: String,
    @SerializedName("parent_id") val parentId: String? = null,
    @SerializedName("sort_order") val sortOrder: Int = 0
)

/**
 * Item (product) response
 */
data class ItemResponse(
    val success: Boolean,
    val data: List<Item>,
    val error: String? = null
)

/**
 * Item (product) model - FIXED to properly handle variations and modifiers
 */
data class Item(
    val id: String,
    val name: String,
    val description: String? = null,
    val sku: String? = null,
    val barcode: String? = null,
    @SerializedName("category_id") val categoryId: String? = null,
    val price: Double,
    @SerializedName("image_url") val imageUrl: String? = null,
    val active: Boolean = true,

    // Business-specific fields
    @SerializedName("item_type") val itemType: String? = null,
    @SerializedName("pricing_type") val pricingType: String? = null,
    val duration: Int? = null,  // For service items (in minutes)
    val calories: Int? = null,  // For restaurant items
    val allergens: List<String>? = null,  // For restaurant items
    @SerializedName("preparation_time") val preparationTime: Int? = null,  // For restaurant items
    @SerializedName("requires_preparation") val requiresPreparation: Boolean = false,
    @SerializedName("is_combo") val isCombo: Boolean = false,
    @SerializedName("combo_items") val comboItems: List<String>? = null,  // Array of item IDs in combo
    @SerializedName("service_level") val serviceLevel: String? = null,  // For service businesses

    // FIXED: Direct variations and modifier groups from backend
    val variations: List<ItemVariation>? = null,
    @SerializedName("modifier_groups") val modifierGroups: List<ModifierGroup>? = null,

    // Legacy field for backward compatibility
    @SerializedName("modifier_group_ids") val modifierGroupIds: List<String>? = null,

    // Item settings (includes inventory)
    val settings: ItemSettings? = null
)

/**
 * Item variation - FIXED structure to match backend
 */
data class ItemVariation(
    @SerializedName("link_id") val linkId: String,
    @SerializedName("type_id") val typeId: String,
    val name: String,
    @SerializedName("is_required") val isRequired: Boolean = false,
    @SerializedName("display_order") val displayOrder: Int = 0,
    val options: List<ItemVariationOption> = emptyList()
)

/**
 * Item variation option - FIXED structure
 */
data class ItemVariationOption(
    val id: String,
    val name: String,
    @SerializedName("price_adjustment") val priceAdjustment: Double = 0.0,
    @SerializedName("sku_suffix") val skuSuffix: String? = null,
    @SerializedName("display_order") val displayOrder: Int = 0
)

/**
 * Item settings
 */
data class ItemSettings(
    @SerializedName("inventory") val inventory: InventorySettings? = null
)

/**
 * Inventory settings
 */
data class InventorySettings(
    @SerializedName("cost_price") val costPrice: Double? = null,
    @SerializedName("current_stock") val currentStock: Double? = null,
    @SerializedName("low_stock_alert") val lowStockAlert: Double? = null,
    @SerializedName("primary_unit") val primaryUnit: String? = null,
    @SerializedName("secondary_unit") val secondaryUnit: String? = null,
    @SerializedName("available_branches") val availableBranches: List<String>? = null,
    @SerializedName("has_variations") val hasVariations: Boolean? = null,
    @SerializedName("discount_type") val discountType: String? = null,
    @SerializedName("discount_value") val discountValue: Double? = null
)

/**
 * Legacy Variation for backward compatibility (if needed)
 */
data class Variation(
    val name: String,
    val options: List<VariationOption>
)

/**
 * Legacy Variation option for backward compatibility
 */
data class VariationOption(
    val name: String,
    @SerializedName("price_adjustment") val priceAdjustment: Double = 0.0
)

/**
 * Modifier group response
 */
data class ModifierGroupResponse(
    val success: Boolean,
    @SerializedName("data") val data: List<ModifierGroup>,
    val error: String? = null
)

/**
 * Modifier group model - FIXED to match backend structure
 */
data class ModifierGroup(
    val id: String,
    val name: String,
    @SerializedName("min_selections") val minSelections: Int = 0,
    @SerializedName("max_selections") val maxSelections: Int = 1,
    @SerializedName("is_required") val isRequired: Boolean = false,
    @SerializedName("display_order") val displayOrder: Int = 0,
    val modifiers: List<Modifier> = emptyList(),
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

/**
 * Modifier model - FIXED to match backend structure
 */
data class Modifier(
    val id: String,
    val name: String,
    @SerializedName("price_adjustment") val priceAdjustment: Double = 0.0,
    @SerializedName("display_order") val displayOrder: Int = 0,
    @SerializedName("is_default") val isDefault: Boolean = false,
    val available: Boolean = true
)

// Include the presentation models here if they don't have a better place
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