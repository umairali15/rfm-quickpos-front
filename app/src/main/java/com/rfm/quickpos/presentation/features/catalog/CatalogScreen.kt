// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/BusinessTypeCatalogScreen.kt

package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.data.remote.models.Item
import com.rfm.quickpos.data.repository.CatalogRepository
import com.rfm.quickpos.data.repository.CatalogSyncState
import com.rfm.quickpos.presentation.common.components.BusinessTypeAwareProductCard
import com.rfm.quickpos.presentation.common.components.RfmCategoryChip
import com.rfm.quickpos.presentation.common.components.RfmLoadingIndicator
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmSearchBar
import com.rfm.quickpos.presentation.common.theme.RfmRed
import kotlinx.coroutines.launch

/**
 * Business type-aware catalog screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessTypeCatalogScreen(
    catalogRepository: CatalogRepository,
    onBackClick: () -> Unit,
    onProductClick: (Item) -> Unit,
    onAddToCart: (Item) -> Unit,
    onScanBarcode: () -> Unit,
    onCartClick: () -> Unit,
    onAddCustomItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect data from repository
    val categories by catalogRepository.categories.collectAsState()
    val items by catalogRepository.items.collectAsState()
    val businessTypeConfig by catalogRepository.businessTypeConfig.collectAsState()
    val syncState by catalogRepository.syncState.collectAsState()

    // Local UI state
    val selectedCategoryId = remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val searchQuery = remember { androidx.compose.runtime.mutableStateOf("") }

    // Create coroutine scope for refreshing
    val coroutineScope = rememberCoroutineScope()

    // Filter items based on selected category and search query
    val filteredItems = remember(selectedCategoryId.value, searchQuery.value, items) {
        var result = if (selectedCategoryId.value != null) {
            items.filter { it.categoryId == selectedCategoryId.value }
        } else {
            items
        }

        if (searchQuery.value.isNotEmpty()) {
            val query = searchQuery.value.lowercase()
            result = result.filter {
                it.name.lowercase().contains(query) ||
                        it.sku?.lowercase()?.contains(query) == true ||
                        it.barcode?.lowercase()?.contains(query) == true
            }
        }

        result
    }

    // Refresh all data function
    suspend fun refreshAllData() {
        try {
            // First get company info (if not already cached)
            if (catalogRepository.companyInfo.value == null) {
                catalogRepository.fetchCompanyInfo()
            }

            // Then sync catalog data
            catalogRepository.syncCatalogData(forceRefresh = true)
        } catch (e: Exception) {
            // Error is already handled in the repository
            // Just log it for debugging
            android.util.Log.e("CatalogScreen", "Error refreshing data", e)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RFM",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = RfmRed
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Catalog",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Filter button
                    IconButton(onClick = { /* Not implemented in demo */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }

                    // Cart icon with badge
                    Box(
                        modifier = Modifier.padding(end = 8.dp, top = 4.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        IconButton(
                            onClick = onCartClick,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Placeholder for cart badge
                        Surface(
                            shape = CircleShape,
                            color = RfmRed,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = "0", // Replace with actual cart count
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = onScanBarcode,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.shadow(elevation = 4.dp, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Barcode",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                FloatingActionButton(
                    onClick = onAddCustomItem,
                    containerColor = RfmRed,
                    contentColor = Color.White,
                    modifier = Modifier.shadow(elevation = 4.dp, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Add Custom Item",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                // Search bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    RfmSearchBar(
                        query = searchQuery.value,
                        onQueryChange = { searchQuery.value = it },
                        onSearch = { /* Perform search */ },
                        placeholder = "Search products",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                when (syncState) {
                    is CatalogSyncState.Loading -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            RfmLoadingIndicator(
                                size = 48f,
                                strokeWidth = 4f,
                                text = (syncState as CatalogSyncState.Loading).message,
                                animated = true
                            )
                        }
                    }
                    is CatalogSyncState.Error -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = (syncState as CatalogSyncState.Error).message,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.error
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Fixed: Implement proper retry function
                                RfmPrimaryButton(
                                    text = "Retry",
                                    onClick = {
                                        coroutineScope.launch {
                                            refreshAllData()
                                        }
                                    },
                                    leadingIcon = Icons.Default.Refresh
                                )
                            }
                        }
                    }
                    else -> {
                        if (filteredItems.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(48.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "No products found",
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = "Try adjusting your search or category filters",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        } else {
                            // Categories row
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .shadow(elevation = 1.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(vertical = 8.dp)
                            ) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // All category option
                                    item {
                                        RfmCategoryChip(
                                            text = "All",
                                            selected = selectedCategoryId.value == null,
                                            onClick = { selectedCategoryId.value = null }
                                        )
                                    }

                                    // Categories from data
                                    items(categories) { category ->
                                        RfmCategoryChip(
                                            text = category.name,
                                            selected = selectedCategoryId.value == category.id,
                                            onClick = { selectedCategoryId.value = category.id }
                                        )
                                    }
                                }
                            }

                            // Products grid
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 160.dp),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredItems) { item ->
                                    BusinessTypeAwareProductCard(
                                        item = item,
                                        businessTypeConfig = businessTypeConfig,
                                        onClick = {
                                            // Check if item has variations
                                            val hasVariations = item.settings?.variations?.isNotEmpty() == true
                                            val hasModifiers = item.modifierGroupIds?.isNotEmpty() == true

                                            if (hasVariations || hasModifiers) {
                                                // Navigate to variations screen
                                                onProductClick(item)
                                            } else {
                                                // Add directly to cart
                                                onAddToCart(item)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}