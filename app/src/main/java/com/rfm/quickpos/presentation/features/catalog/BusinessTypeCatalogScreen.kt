// app/src/main/java/com/rfm/quickpos/presentation/features/catalog/BusinessTypeCatalogScreen.kt

package com.rfm.quickpos.presentation.features.catalog

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.data.remote.models.Item
import com.rfm.quickpos.data.repository.CatalogRepository
import com.rfm.quickpos.data.repository.CatalogSyncState
import com.rfm.quickpos.presentation.common.components.BusinessTypeAwareProductCard
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmSearchBar
import com.rfm.quickpos.presentation.common.theme.RfmRed
import kotlinx.coroutines.launch

private const val TAG = "CatalogScreen"

/**
 * Enhanced business type-aware catalog screen with modern design
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
    val companyInfo by catalogRepository.companyInfo.collectAsState()
    val syncState by catalogRepository.syncState.collectAsState()

    // Local UI state
    val selectedCategoryId = remember { mutableStateOf<String?>(null) }
    val searchQuery = remember { mutableStateOf("") }
    val cartItemCount = remember { mutableStateOf(2) } // This should come from cart repository

    // Create coroutine scope for refreshing
    val coroutineScope = rememberCoroutineScope()

    // Bottom sheet state
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    // Debug logging for items with variations/modifiers
    LaunchedEffect(items) {
        Log.d(TAG, "Total items loaded: ${items.size}")
        items.forEach { item ->
            if (!item.variations.isNullOrEmpty() || !item.modifierGroups.isNullOrEmpty()) {
                Log.d(TAG, "Item: ${item.name} (${item.id})")
                Log.d(TAG, "  - Variations: ${item.variations?.size ?: 0}")
                item.variations?.forEach { variation ->
                    Log.d(TAG, "    - ${variation.name}: ${variation.options.map { it.name }}")
                }
                Log.d(TAG, "  - Modifier Groups: ${item.modifierGroups?.size ?: 0}")
                item.modifierGroups?.forEach { group ->
                    Log.d(TAG, "    - ${group.name}: ${group.modifiers.map { it.name }}")
                }
            }
        }
    }

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

        result.filter { it.active }
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        topBar = {
            EnhancedCatalogTopBar(
                companyInfo = companyInfo,
                cartItemCount = cartItemCount.value,
                onBackClick = onBackClick,
                onCartClick = onCartClick
            )
        },
        sheetContent = {
            AddCustomItemBottomSheet(
                onDismiss = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.partialExpand()
                    }
                },
                onAddItem = { customItem ->
                    // Handle adding custom item
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.hide()
                    }
                }
            )
        },
        sheetPeekHeight = 0.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                // Search bar with modern design
                SearchSection(
                    searchQuery = searchQuery.value,
                    onSearchQueryChange = { searchQuery.value = it },
                    selectedCategoryId = selectedCategoryId.value,
                    categories = categories,
                    onCategorySelected = { categoryId ->
                        selectedCategoryId.value = categoryId
                    }
                )

                // Main content based on state
                AnimatedContent(
                    targetState = syncState,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "catalog-content"
                ) { state ->
                    when (state) {
                        is CatalogSyncState.Loading -> {
                            LoadingContent(message = state.message)
                        }
                        is CatalogSyncState.Error -> {
                            ErrorContent(
                                message = state.message,
                                onRetry = {
                                    coroutineScope.launch {
                                        catalogRepository.syncCatalogData(forceRefresh = true)
                                    }
                                }
                            )
                        }
                        else -> {
                            if (filteredItems.isEmpty()) {
                                EmptyStateContent(
                                    hasCategory = selectedCategoryId.value != null,
                                    onResetFilters = {
                                        selectedCategoryId.value = null
                                        searchQuery.value = ""
                                    }
                                )
                            } else {
                                ProductsContent(
                                    items = filteredItems,
                                    businessTypeConfig = businessTypeConfig,
                                    onProductClick = onProductClick,
                                    onAddToCart = onAddToCart
                                )
                            }
                        }
                    }
                }
            }

            // Floating action buttons
            FloatingActionButtons(
                onScanBarcode = onScanBarcode,
                onAddCustomItem = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            )
        }
    }
}

/**
 * Enhanced top bar with company and branch info
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedCatalogTopBar(
    companyInfo: com.rfm.quickpos.data.remote.models.CompanyInfo?,
    cartItemCount: Int,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = RfmRed,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = companyInfo?.name ?: "Catalog",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (companyInfo?.businessType != null) {
                        Text(
                            text = companyInfo.businessType.replaceFirstChar { it.titlecase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
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
                // Cart icon with badge
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge {
                                Text(
                                    text = if (cartItemCount > 99) "99+" else cartItemCount.toString()
                                )
                            }
                        }
                    }
                ) {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart"
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Subtle divider for better separation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    }
}

/**
 * Enhanced search section with horizontal categories
 */
@Composable
private fun SearchSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategoryId: String?,
    categories: List<com.rfm.quickpos.data.remote.models.Category>,
    onCategorySelected: (String?) -> Unit
) {
    Column {
        // Modern search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            RfmSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = { /* Perform search */ },
                placeholder = "Search products, SKU, or barcode",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Categories with sticky scroll
        if (categories.isNotEmpty()) {
            Card(
                shape = RectangleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // All category chip with modern design
                    item {
                        EnhancedCategoryChip(
                            text = "All",
                            selected = selectedCategoryId == null,
                            isReset = selectedCategoryId != null,
                            onClick = { onCategorySelected(null) }
                        )
                    }

                    // Category chips
                    items(categories) { category ->
                        EnhancedCategoryChip(
                            text = category.name,
                            selected = selectedCategoryId == category.id,
                            onClick = { onCategorySelected(category.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Enhanced category chip with animations
 */
@Composable
private fun EnhancedCategoryChip(
    text: String,
    selected: Boolean,
    isReset: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        selected -> MaterialTheme.colorScheme.primary
        isReset -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        isReset -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = if (selected) 4.dp else 0.dp,
        modifier = Modifier
            .animateContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isReset) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Loading content with modern design
 */
@Composable
private fun LoadingContent(message: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator(
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error content with retry option
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            RfmPrimaryButton(
                text = "Try Again",
                onClick = onRetry,
                leadingIcon = Icons.Default.Refresh
            )
        }
    }
}

/**
 * Empty state content with helpful actions
 */
@Composable
private fun EmptyStateContent(
    hasCategory: Boolean,
    onResetFilters: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No products found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Try adjusting your search or category filters",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (hasCategory) {
                Spacer(modifier = Modifier.height(24.dp))

                RfmPrimaryButton(
                    text = "Show All Products",
                    onClick = onResetFilters,
                    leadingIcon = Icons.Default.Refresh
                )
            }
        }
    }
}

/**
 * Products grid content
 */
@Composable
private fun ProductsContent(
    items: List<Item>,
    businessTypeConfig: com.rfm.quickpos.data.remote.models.BusinessTypeConfig?,
    onProductClick: (Item) -> Unit,
    onAddToCart: (Item) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            // Debug log for each item
            if (!item.variations.isNullOrEmpty() || !item.modifierGroups.isNullOrEmpty()) {
                Log.d(TAG, "Rendering card for: ${item.name} - Variations: ${item.variations?.size}, Modifiers: ${item.modifierGroups?.size}")
            }

            BusinessTypeAwareProductCard(
                item = item,
                businessTypeConfig = businessTypeConfig,
                onClick = {
                    Log.d(TAG, "Product clicked: ${item.name} (${item.id})")
                    onProductClick(item)
                }
            )
        }
    }
}

/**
 * Modern floating action buttons
 */
@Composable
private fun FloatingActionButtons(
    onScanBarcode: () -> Unit,
    onAddCustomItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scan barcode FAB
        FloatingActionButton(
            onClick = onScanBarcode,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.shadow(elevation = 6.dp, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan Barcode",
                modifier = Modifier.size(24.dp)
            )
        }

        // Add custom item FAB
        ExtendedFloatingActionButton(
            onClick = onAddCustomItem,
            containerColor = RfmRed,
            contentColor = Color.White,
            modifier = Modifier.shadow(elevation = 6.dp, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Custom Item",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Custom",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Bottom sheet for adding custom items
 */
@Composable
private fun AddCustomItemBottomSheet(
    onDismiss: () -> Unit,
    onAddItem: (CustomItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Add Custom Item",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price") },
            prefix = { Text("AED ") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = barcode,
            onValueChange = { barcode = it },
            label = { Text("Barcode (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            RfmPrimaryButton(
                text = "Add Item",
                onClick = {
                    val priceValue = price.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && priceValue > 0) {
                        onAddItem(CustomItem(name, priceValue, barcode.ifBlank { null }))
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank() && price.toDoubleOrNull() != null
            )
        }
    }
}

