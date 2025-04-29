package com.rfm.quickpos.presentation.features.catalog

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.components.ProductCard
import com.rfm.quickpos.presentation.common.components.RfmCategoryChip
import com.rfm.quickpos.presentation.common.components.RfmLoadingIndicator
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmSearchBar
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme

/**
 * Data class to represent a product category
 */
data class ProductCategory(
    val id: String,
    val name: String
)

/**
 * Data class to represent a product
 */
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String? = null,
    val categoryId: String,
    val barcode: String? = null,
    val discountPercentage: Int? = null,
    val inStock: Boolean = true
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

/**
 * Catalog screen for browsing and adding products to cart
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    state: CatalogState,
    onBackClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onScanBarcode: () -> Unit,
    onCartClick: () -> Unit,
    onAddCustomItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RFM",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "Catalog",
                            style = MaterialTheme.typography.titleLarge
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
                    IconButton(onClick = { /* Not implemented in demo */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }

                    IconButton(onClick = onCartClick) {
                        BadgedBox(
                            badge = {
                                if (state.cartItemCount > 0) {
                                    Badge {
                                        Text(text = state.cartItemCount.toString())
                                    }
                                }
                            }
                        ) {
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
        },
        floatingActionButton = {
            Column {
                // Barcode scanner FAB
                FloatingActionButton(
                    onClick = onScanBarcode,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Barcode"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom item FAB
                FloatingActionButton(
                    onClick = onAddCustomItem,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Custom Item"
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
                RfmSearchBar(
                    query = state.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = onSearchSubmit,
                    placeholder = "Search products",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (state.isLoading) {
                    // Loading state
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        RfmLoadingIndicator()
                    }
                } else if (state.error != null) {
                    // Error state
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            RfmPrimaryButton(
                                text = "Retry",
                                onClick = onSearchSubmit
                            )
                        }
                    }
                } else if (state.products.isEmpty()) {
                    // Empty state
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "No products found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    // Categories row
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // All category option
                        item {
                            RfmCategoryChip(
                                text = "All",
                                selected = state.selectedCategoryId == null,
                                onClick = { onCategorySelected(null) }
                            )
                        }

                        // Categories from data
                        items(state.categories) { category ->
                            RfmCategoryChip(
                                text = category.name,
                                selected = state.selectedCategoryId == category.id,
                                onClick = { onCategorySelected(category.id) }
                            )
                        }
                    }

                    // Products grid
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.products) { product ->
                            ProductCard(
                                name = product.name,
                                price = "AED ${String.format("%.2f", product.price)}",
                                onClick = { onProductClick(product) },
                                imageUrl = product.imageUrl,
                                inStock = product.inStock,
                                discountPercentage = product.discountPercentage
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CatalogScreenPreview() {
    // Sample data
    val categories = listOf(
        ProductCategory("1", "Beverages"),
        ProductCategory("2", "Food"),
        ProductCategory("3", "General")
    )

    val products = listOf(
        Product(
            id = "1",
            name = "Coffee",
            price = 15.00,
            categoryId = "1",
            barcode = "5901234123457"
        ),
        Product(
            id = "2",
            name = "Croissant",
            price = 10.00,
            categoryId = "2",
            barcode = "4003994155486",
            discountPercentage = 10
        ),
        Product(
            id = "3",
            name = "Water Bottle",
            price = 5.00,
            categoryId = "1",
            barcode = "7622210146083"
        ),
        Product(
            id = "4",
            name = "Sandwich",
            price = 20.00,
            categoryId = "2",
            barcode = "1234567890123"
        )
    )

    val state = CatalogState(
        isLoading = false,
        categories = categories,
        products = products,
        selectedCategoryId = null,
        cartItemCount = 2
    )

    RFMQuickPOSTheme {
        CatalogScreen(
            state = state,
            onBackClick = {},
            onSearchQueryChange = {},
            onSearchSubmit = {},
            onCategorySelected = {},
            onProductClick = {},
            onAddToCart = {},
            onScanBarcode = {},
            onCartClick = {},
            onAddCustomItem = {}
        )
    }
}