// app/src/main/java/com/rfm/quickpos/data/repository/CatalogRepository.kt

package com.rfm.quickpos.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rfm.quickpos.data.local.storage.SecureCredentialStore
import com.rfm.quickpos.data.remote.api.ApiService
import com.rfm.quickpos.data.remote.models.BusinessTypeConfig
import com.rfm.quickpos.data.remote.models.Category
import com.rfm.quickpos.data.remote.models.CompanyInfo
import com.rfm.quickpos.data.remote.models.Item
import com.rfm.quickpos.data.remote.models.ItemVariation
import com.rfm.quickpos.data.remote.models.ModifierGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Date

private const val TAG = "CatalogRepository"
private const val CATALOG_CACHE_FILE = "catalog_cache.json"
private const val COMPANY_INFO_CACHE_FILE = "company_info_cache.json"
private const val CATALOG_CACHE_MAX_AGE_MS = 24 * 60 * 60 * 1000 // 24 hours

/**
 * Repository for catalog and business data
 * FIXED to properly handle variations and modifiers
 */
class CatalogRepository(
    private val apiService: ApiService,
    private val credentialStore: SecureCredentialStore,
    private val context: Context
) {
    private val gson = Gson()

    // State flows for catalog data
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _modifierGroups = MutableStateFlow<List<ModifierGroup>>(emptyList())
    val modifierGroups: StateFlow<List<ModifierGroup>> = _modifierGroups.asStateFlow()

    // State flow for company info
    private val _companyInfo = MutableStateFlow<CompanyInfo?>(null)
    val companyInfo: StateFlow<CompanyInfo?> = _companyInfo.asStateFlow()

    // State flow for business type
    private val _businessTypeConfig = MutableStateFlow<BusinessTypeConfig?>(null)
    val businessTypeConfig: StateFlow<BusinessTypeConfig?> = _businessTypeConfig.asStateFlow()

    // Sync state
    private val _syncState = MutableStateFlow<CatalogSyncState>(CatalogSyncState.Initial)
    val syncState: StateFlow<CatalogSyncState> = _syncState.asStateFlow()

    // Last sync time
    private var lastSyncTime: Long = 0

    init {
        // Load cached data on init
        loadCachedData()
    }

    /**
     * Initialize catalog data - this should be called when app is ready
     */
    suspend fun initialize(): Boolean {
        Log.d(TAG, "Initializing catalog...")

        if (_syncState.value is CatalogSyncState.Loading) {
            Log.d(TAG, "Already loading, skipping initialization")
            return false
        }

        try {
            // First make sure we have company info
            if (_companyInfo.value == null) {
                Log.d(TAG, "Company info missing, fetching...")
                val companyInfo = fetchCompanyInfo()
                if (companyInfo == null) {
                    Log.e(TAG, "Failed to fetch company info during initialization")
                    return false
                }
            }

            // Check if we need to sync or have valid cache
            if (!isCatalogCacheValid() || _categories.value.isEmpty() || _items.value.isEmpty()) {
                Log.d(TAG, "Catalog data missing or outdated, syncing...")
                return syncCatalogData(forceRefresh = true)
            } else {
                Log.d(TAG, "Using cached catalog data")
                _syncState.value = CatalogSyncState.Success
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during initialization", e)
            _syncState.value = CatalogSyncState.Error("Failed to initialize catalog: ${e.message}")
            return false
        }
    }

    /**
     * Load cached catalog data if available
     */
    private fun loadCachedData() {
        try {
            // Load cached catalog data
            val catalogCacheFile = File(context.filesDir, CATALOG_CACHE_FILE)
            if (catalogCacheFile.exists()) {
                catalogCacheFile.bufferedReader().use { reader ->
                    val catalogCacheJson = reader.readText()
                    val catalogCache = gson.fromJson(catalogCacheJson, CatalogCache::class.java)

                    if (catalogCache != null) {
                        _categories.value = catalogCache.categories ?: emptyList()
                        _items.value = catalogCache.items ?: emptyList()
                        _modifierGroups.value = catalogCache.modifierGroups ?: emptyList()
                        lastSyncTime = catalogCache.timestamp

                        Log.d(TAG, "Loaded cached catalog data. Categories: ${_categories.value.size}, " +
                                "Items: ${_items.value.size}, Timestamp: ${Date(catalogCache.timestamp)}")

                        // FIXED: Log variations and modifiers from cached data
                        logVariationsAndModifiersDebug(_items.value)
                    }
                }
            } else {
                Log.d(TAG, "No catalog cache file found - this is normal for first run")
            }

            // Load cached company info
            val companyInfoCacheFile = File(context.filesDir, COMPANY_INFO_CACHE_FILE)
            if (companyInfoCacheFile.exists()) {
                companyInfoCacheFile.bufferedReader().use { reader ->
                    val companyInfoJson = reader.readText()
                    val companyInfoCache = gson.fromJson(companyInfoJson, CompanyInfoCache::class.java)

                    if (companyInfoCache != null) {
                        _companyInfo.value = companyInfoCache.companyInfo
                        _businessTypeConfig.value = companyInfoCache.businessTypeConfig

                        Log.d(TAG, "Loaded cached company info. Business type: ${companyInfoCache.companyInfo?.businessType}")
                    }
                }
            } else {
                Log.d(TAG, "No company info cache file found - this is normal for first run")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cached data", e)
            // Initialize with empty data
            _categories.value = emptyList()
            _items.value = emptyList()
            _modifierGroups.value = emptyList()
            _companyInfo.value = null
            _businessTypeConfig.value = null
        }
    }

    /**
     * Save catalog data to cache
     */
    private fun saveCatalogCache() {
        try {
            val catalogCache = CatalogCache(
                categories = _categories.value,
                items = _items.value,
                modifierGroups = _modifierGroups.value,
                timestamp = System.currentTimeMillis()
            )

            val catalogCacheJson = gson.toJson(catalogCache)

            File(context.filesDir, CATALOG_CACHE_FILE).bufferedWriter().use { writer ->
                writer.write(catalogCacheJson)
            }

            Log.d(TAG, "Saved catalog data to cache")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving catalog cache", e)
        }
    }

    /**
     * Save company info to cache
     */
    private fun saveCompanyInfoCache() {
        try {
            val companyInfoCache = CompanyInfoCache(
                companyInfo = _companyInfo.value,
                businessTypeConfig = _businessTypeConfig.value,
                timestamp = System.currentTimeMillis()
            )

            val companyInfoJson = gson.toJson(companyInfoCache)

            File(context.filesDir, COMPANY_INFO_CACHE_FILE).bufferedWriter().use { writer ->
                writer.write(companyInfoJson)
            }

            Log.d(TAG, "Saved company info to cache")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving company info cache", e)
        }
    }

    /**
     * Check if catalog cache is still valid or needs refresh
     */
    private fun isCatalogCacheValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        val isValid = lastSyncTime > 0 && (currentTime - lastSyncTime < CATALOG_CACHE_MAX_AGE_MS)

        Log.d(TAG, "Cache validity check: isValid=$isValid, lastSyncTime=${Date(lastSyncTime)}, " +
                "age=${(currentTime - lastSyncTime)/1000}s")

        return isValid
    }

    /**
     * Fetch company and business type information
     */
    suspend fun fetchCompanyInfo(): CompanyInfo? {
        if (_syncState.value is CatalogSyncState.Loading) {
            Log.d(TAG, "Already loading, skipping company info fetch")
            return _companyInfo.value
        }

        // First check if we already have company info from JWT
        val companyId = credentialStore.getCompanyId()
        val companySchema = credentialStore.getCompanySchema()
        val businessType = credentialStore.getBusinessType()

        if (companyId != null && companySchema != null && businessType != null) {
            Log.d(TAG, "Using company info from JWT")

            val companyInfo = CompanyInfo(
                id = companyId,
                name = "", // You can get this from another source or set a default
                taxNumber = null,
                contactEmail = null,
                contactPhone = null,
                logoUrl = null,
                businessType = businessType,
                schemaName = companySchema
            )

            // Set business type config based on business type
            val businessConfig = createBusinessTypeConfig(businessType)

            _companyInfo.value = companyInfo
            _businessTypeConfig.value = businessConfig

            // Save to cache
            saveCompanyInfoCache()

            Log.d(TAG, "Company info from JWT: type=$businessType, schema=$companySchema")
            _syncState.value = CatalogSyncState.Success
            return companyInfo
        }

        // If we don't have JWT data, make API call
        _syncState.value = CatalogSyncState.Loading("Fetching company info")

        return try {
            Log.d(TAG, "Making API call to fetch company info...")

            // Use the correct endpoint based on available data
            val response = apiService.getCompanyInfo()

            Log.d(TAG, "Company info response received: success=${response.success}")

            if (response.success) {
                _companyInfo.value = response.companyInfo
                _businessTypeConfig.value = response.businessTypeConfig

                // Save to cache
                saveCompanyInfoCache()

                Log.d(TAG, "Fetched company info: ${response.companyInfo.name}, " +
                        "Business type: ${response.companyInfo.businessType}")

                _syncState.value = CatalogSyncState.Success
                response.companyInfo
            } else {
                Log.e(TAG, "Failed to fetch company info: ${response.error}")
                _syncState.value = CatalogSyncState.Error("Failed to fetch company information: ${response.error}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching company info", e)
            _syncState.value = CatalogSyncState.Error("Failed to fetch company information: ${e.message}")
            null
        }
    }

    /**
     * Create business type configuration based on business type
     */
    private fun createBusinessTypeConfig(businessType: String): BusinessTypeConfig {
        return when (businessType.lowercase()) {
            "restaurant" -> BusinessTypeConfig(
                name = "restaurant",
                features = listOf("menu_management", "table_management", "modifiers"),
                supportsModifiers = true,
                supportsTables = true,
                supportsInventory = false,
                supportsTimeBasedPricing = false,
                requiresCustomer = false
            )
            "retail" -> BusinessTypeConfig(
                name = "retail",
                features = listOf("inventory_management", "barcode_scanning", "variations"),
                supportsModifiers = false,
                supportsTables = false,
                supportsInventory = true,
                supportsTimeBasedPricing = false,
                requiresCustomer = false
            )
            "service" -> BusinessTypeConfig(
                name = "service",
                features = listOf("time_based_pricing", "appointments", "service_levels"),
                supportsModifiers = false,
                supportsTables = false,
                supportsInventory = false,
                supportsTimeBasedPricing = true,
                requiresCustomer = true
            )
            else -> BusinessTypeConfig(
                name = businessType,
                features = emptyList()
            )
        }
    }

    /**
     * FIXED: Sync all catalog data with enhanced variations/modifiers handling
     */
    suspend fun syncCatalogData(forceRefresh: Boolean = false): Boolean {
        // Check if we need to refresh data
        if (!forceRefresh && isCatalogCacheValid()) {
            Log.d(TAG, "Using cached catalog data, last sync: ${Date(lastSyncTime)}")
            _syncState.value = CatalogSyncState.Success
            return true
        }

        _syncState.value = CatalogSyncState.Loading("Syncing catalog data")

        return try {
            // Fetch categories
            val categoriesResponse = apiService.getCategories()
            if (!categoriesResponse.success) {
                _syncState.value = CatalogSyncState.Error("Failed to fetch categories: ${categoriesResponse.error}")
                return false
            }
            _categories.value = categoriesResponse.data
            Log.d(TAG, "Synced ${categoriesResponse.data.size} categories")

            // FIXED: Fetch items with explicit parameters for variations and modifiers
            val itemsResponse = apiService.getItems(
                categoryId = null, // Get all items
                includeVariations = true,
                includeModifiers = true
            )
            if (!itemsResponse.success) {
                _syncState.value = CatalogSyncState.Error("Failed to fetch items: ${itemsResponse.error}")
                return false
            }

            // FIXED: Enhanced logging to debug variations/modifiers
            Log.d(TAG, "=== CATALOG SYNC DEBUG - ENHANCED ===")
            Log.d(TAG, "Total items received: ${itemsResponse.data.size}")

            // Log the raw JSON structure for debugging
            val itemsJson = gson.toJson(itemsResponse.data)
            Log.d(TAG, "Raw items JSON length: ${itemsJson.length}")

            logVariationsAndModifiersDebug(itemsResponse.data)

            _items.value = itemsResponse.data
            Log.d(TAG, "Synced ${itemsResponse.data.size} items")

            // Fetch modifier groups (legacy - for backward compatibility if needed)
            if (_businessTypeConfig.value?.supportsModifiers == true) {
                try {
                    val modifiersResponse = apiService.getModifierGroups()
                    if (!modifiersResponse.success) {
                        Log.w(TAG, "Failed to fetch legacy modifier groups: ${modifiersResponse.error}")
                        // Don't fail the sync, as modifiers now come with items
                    } else {
                        _modifierGroups.value = modifiersResponse.data
                        Log.d(TAG, "Synced ${modifiersResponse.data.size} legacy modifier groups")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Exception fetching legacy modifier groups", e)
                    // Continue without legacy modifier groups
                }
            } else {
                Log.d(TAG, "Business type does not support modifiers, skipping modifier sync")
            }

            // Save to cache
            saveCatalogCache()

            _syncState.value = CatalogSyncState.Success
            Log.d(TAG, "Catalog sync completed successfully. Categories: ${_categories.value.size}, " +
                    "Items: ${_items.value.size}, ModifierGroups: ${_modifierGroups.value.size}")

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing catalog data", e)
            _syncState.value = CatalogSyncState.Error("Failed to sync catalog data: ${e.message}")
            false
        }
    }

    /**
     * FIXED: Enhanced debug logging for variations and modifiers
     */
    private fun logVariationsAndModifiersDebug(items: List<Item>) {
        var itemsWithVariations = 0
        var itemsWithModifiers = 0
        var totalVariations = 0
        var totalModifierGroups = 0

        items.forEachIndexed { index, item ->
            val hasVariations = !item.variations.isNullOrEmpty()
            val hasModifiers = !item.modifierGroups.isNullOrEmpty()
            val variationCount = item.variations?.size ?: 0
            val modifierGroupCount = item.modifierGroups?.size ?: 0

            if (hasVariations) itemsWithVariations++
            if (hasModifiers) itemsWithModifiers++
            totalVariations += variationCount
            totalModifierGroups += modifierGroupCount

            Log.d(TAG, "Item #$index: ${item.name} (ID: ${item.id})")
            Log.d(TAG, "  - Has variations: $hasVariations (count: $variationCount)")
            Log.d(TAG, "  - Has modifiers: $hasModifiers (count: $modifierGroupCount)")

            // Log detailed variations
            if (hasVariations) {
                item.variations?.forEach { variation ->
                    Log.d(TAG, "    Variation: ${variation.name} (${variation.options.size} options, required: ${variation.isRequired})")
                    variation.options.forEach { option ->
                        Log.d(TAG, "      Option: ${option.name} (+${option.priceAdjustment})")
                    }
                }
            }

            // Log detailed modifier groups
            if (hasModifiers) {
                item.modifierGroups?.forEach { group ->
                    Log.d(TAG, "    Modifier Group: ${group.name} (${group.modifiers.size} modifiers, required: ${group.isRequired})")
                    group.modifiers.forEach { modifier ->
                        Log.d(TAG, "      Modifier: ${modifier.name} (+${modifier.priceAdjustment})")
                    }
                }
            }
        }

        Log.d(TAG, "=== CATALOG SUMMARY ===")
        Log.d(TAG, "Total items: ${items.size}")
        Log.d(TAG, "Items with variations: $itemsWithVariations")
        Log.d(TAG, "Items with modifiers: $itemsWithModifiers")
        Log.d(TAG, "Total variations across all items: $totalVariations")
        Log.d(TAG, "Total modifier groups across all items: $totalModifierGroups")
        Log.d(TAG, "=== END CATALOG SYNC DEBUG ===")
    }

    /**
     * Get items for a specific category
     */
    fun getItemsForCategory(categoryId: String?): List<Item> {
        return if (categoryId == null) {
            _items.value.filter { it.active }
        } else {
            _items.value.filter { it.categoryId == categoryId && it.active }
        }
    }

    /**
     * Get modifier groups for a specific item
     * Updated to use the new structure where modifier groups come directly on items
     */
    fun getModifierGroupsForItem(itemId: String): List<ModifierGroup> {
        val item = _items.value.find { it.id == itemId } ?: return emptyList()

        // Return modifier groups directly from the item (new structure)
        return item.modifierGroups ?: emptyList()
    }

    /**
     * Get variations for a specific item
     * New method to get variations directly from the item
     */
    fun getVariationsForItem(itemId: String): List<ItemVariation> {
        val item = _items.value.find { it.id == itemId } ?: return emptyList()

        // Return variations directly from the item (new structure)
        return item.variations ?: emptyList()
    }

    /**
     * Search items by name, barcode, or sku
     */
    fun searchItems(query: String): List<Item> {
        if (query.isBlank()) return _items.value.filter { it.active }

        val searchTerm = query.lowercase().trim()
        return _items.value.filter { item ->
            item.active && (
                    item.name.lowercase().contains(searchTerm) ||
                            item.barcode?.lowercase()?.contains(searchTerm) == true ||
                            item.sku?.lowercase()?.contains(searchTerm) == true
                    )
        }
    }

    /**
     * Clear cached data (useful for logout)
     */
    fun clearCache() {
        try {
            File(context.filesDir, CATALOG_CACHE_FILE).delete()
            File(context.filesDir, COMPANY_INFO_CACHE_FILE).delete()

            // Reset state
            _categories.value = emptyList()
            _items.value = emptyList()
            _modifierGroups.value = emptyList()
            _companyInfo.value = null
            _businessTypeConfig.value = null
            lastSyncTime = 0

            Log.d(TAG, "Cleared catalog cache")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }
}

/**
 * Data class for catalog cache
 */
data class CatalogCache(
    val categories: List<Category>?,
    val items: List<Item>?,
    val modifierGroups: List<ModifierGroup>?,
    val timestamp: Long
)

/**
 * Data class for company info cache
 */
data class CompanyInfoCache(
    val companyInfo: CompanyInfo?,
    val businessTypeConfig: BusinessTypeConfig?,
    val timestamp: Long
)

/**
 * States for catalog sync
 */
sealed class CatalogSyncState {
    object Initial : CatalogSyncState()
    data class Loading(val message: String) : CatalogSyncState()
    object Success : CatalogSyncState()
    data class Error(val message: String) : CatalogSyncState()
}