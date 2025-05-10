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
 * Handles data synchronization and caching
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
     * Sync all catalog data
     * This should be called once after login or when needed, not on every app start
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
            // Fetch categories with null safety
            Log.d(TAG, "Fetching categories...")
            val categoriesResponse = apiService.getCategories()

            Log.d(TAG, "Categories response - success: ${categoriesResponse.success}, " +
                    "categories: ${categoriesResponse.categories?.size ?: "null"}")

            if (!categoriesResponse.success) {
                _syncState.value = CatalogSyncState.Error("Failed to fetch categories: ${categoriesResponse.error}")
                return false
            }

            // Handle null categories list
            _categories.value = categoriesResponse.categories ?: emptyList()
            Log.d(TAG, "Synced ${_categories.value.size} categories")

            // Fetch items with null safety
            Log.d(TAG, "Fetching items...")
            val itemsResponse = apiService.getItems()

            Log.d(TAG, "Items response - success: ${itemsResponse.success}, " +
                    "items: ${itemsResponse.items?.size ?: "null"}")

            if (!itemsResponse.success) {
                _syncState.value = CatalogSyncState.Error("Failed to fetch items: ${itemsResponse.error}")
                return false
            }

            // Handle null items list
            _items.value = itemsResponse.items ?: emptyList()
            Log.d(TAG, "Synced ${_items.value.size} items")

            // Fetch modifier groups based on business type
            if (_businessTypeConfig.value?.supportsModifiers == true) {
                Log.d(TAG, "Fetching modifier groups...")
                val modifiersResponse = apiService.getModifierGroups()

                Log.d(TAG, "Modifiers response - success: ${modifiersResponse.success}, " +
                        "modifierGroups: ${modifiersResponse.modifierGroups?.size ?: "null"}")

                if (!modifiersResponse.success) {
                    _syncState.value = CatalogSyncState.Error("Failed to fetch modifiers: ${modifiersResponse.error}")
                    return false
                }

                // Handle null modifier groups list
                _modifierGroups.value = modifiersResponse.modifierGroups ?: emptyList()
                Log.d(TAG, "Synced ${_modifierGroups.value.size} modifier groups")
            } else {
                Log.d(TAG, "Business type does not support modifiers, skipping modifier sync")
                _modifierGroups.value = emptyList()
            }

            // Save to cache
            saveCatalogCache()
            lastSyncTime = System.currentTimeMillis()

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
     */
    fun getModifierGroupsForItem(itemId: String): List<ModifierGroup> {
        val item = _items.value.find { it.id == itemId } ?: return emptyList()
        return item.modifierGroupIds?.mapNotNull { groupId ->
            _modifierGroups.value.find { it.id == groupId }
        } ?: emptyList()
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