// Updated CatalogRepository.kt

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
                        _categories.value = catalogCache.categories
                        _items.value = catalogCache.items
                        _modifierGroups.value = catalogCache.modifierGroups
                        lastSyncTime = catalogCache.timestamp

                        Log.d(TAG, "Loaded cached catalog data. Categories: ${catalogCache.categories.size}, " +
                                "Items: ${catalogCache.items.size}, Timestamp: ${Date(catalogCache.timestamp)}")
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
        _syncState.value = CatalogSyncState.Loading("Fetching company info")

        return try {
            val response = apiService.getCompanyInfo()

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
            // Fetch categories
            val categoriesResponse = apiService.getCategories()
            if (!categoriesResponse.success) {
                _syncState.value = CatalogSyncState.Error("Failed to fetch categories: ${categoriesResponse.error}")
                return false
            }
            _categories.value = categoriesResponse.categories
            Log.d(TAG, "Synced ${categoriesResponse.categories.size} categories")

            // Fetch items
            val itemsResponse = apiService.getItems()
            if (!itemsResponse.success) {
                _syncState.value = CatalogSyncState.Error("Failed to fetch items: ${itemsResponse.error}")
                return false
            }
            _items.value = itemsResponse.items
            Log.d(TAG, "Synced ${itemsResponse.items.size} items")

            // Fetch modifier groups based on business type
            if (_businessTypeConfig.value?.supportsModifiers == true) {
                val modifiersResponse = apiService.getModifierGroups()
                if (!modifiersResponse.success) {
                    _syncState.value = CatalogSyncState.Error("Failed to fetch modifiers: ${modifiersResponse.error}")
                    return false
                }
                _modifierGroups.value = modifiersResponse.modifierGroups
                Log.d(TAG, "Synced ${modifiersResponse.modifierGroups.size} modifier groups")
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
    val categories: List<Category>,
    val items: List<Item>,
    val modifierGroups: List<ModifierGroup>,
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