package com.rfm.quickpos.presentation.features.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.components.ProductCard
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.common.theme.posColors

/**
 * Data class for product category
 */
data class ProductCategory(
    val id: String,
    val name: String,
    val itemCount: Int
)

/**
 * Data class for product
 */
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String? = null,
    val discountAmount: Double? = null,
    val discountPercent: Double? = null,
    val categoryId: String
)

/**
 * Catalog Screen with categories and product grid
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    categories: List<ProductCategory>,
    products: List<Product>,
    onProductClicked: (Product) -> Unit,
    onCartClicked: () -> Unit,
    onBackClicked: () -> Unit,
    cartItemCount: Int = 0,
    modifier: Modifier = Modifier,
    currencyCode: String = "AED",
    title: String = "Products"
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeSearch by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    val filteredProducts = products.filter { product ->
        val matchesSearch = if (searchQuery.isBlank()) true else
            product.name.contains(searchQuery, ignoreCase = true)

        val matchesCategory = if (selectedCategoryId == null) true else
            product.categoryId == selectedCategoryId

        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            if (activeSearch) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { activeSearch = false },
                    active = true,
                    onActiveChange = { activeSearch = it },
                    placeholder = { Text("Search products") },
                    leadingIcon = {
                        IconButton(onClick = { activeSearch = false }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Search suggestions could go here
                }
            } else {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { activeSearch = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                        IconButton(onClick = onCartClicked) {
                            BadgedBox(
                                badge = {
                                    if (cartItemCount > 0) {
                                        Badge {
                                            Text(cartItemCount.toString())
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
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Categories row
            if (categories.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // "All" category chip
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.posColors.selectedCategoryChip,
                            selectedLabelColor = MaterialTheme.posColors.onSelectedCategoryChip
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = {
                                selectedCategoryId = if (selectedCategoryId == category.id) null else category.id
                            },
                            label = {
                                Text("${category.name} ${category.itemCount}")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.posColors.selectedCategoryChip,
                                selectedLabelColor = MaterialTheme.posColors.onSelectedCategoryChip
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                // Empty state
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            "No products found matching '$searchQuery'"
                        else
                            "No products available in this category",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Products grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { product ->
                        ProductCard(
                            name = product.name,
                            price = formatNumber(product.price),
                            imageUrl = product.imageUrl,
                            currencyCode = currencyCode,
                            discountTag = product.discountAmount?.let {
                                "-$currencyCode ${formatNumber(it)}"
                            } ?: product.discountPercent?.let {
                                "-${it.toInt()}%"
                            },
                            onClick = { onProductClicked(product) }
                        )
                    }
                }
            }
        }
    }
}

// Utility function for formatting
private fun formatNumber(amount: Double): String {
    return if (amount == amount.toInt().toDouble()) {
        amount.toInt().toString()
    } else {
        String.format("%.2f", amount)
    }
}

@Preview(showBackground = true)
@Composable
fun CatalogScreenPreview() {
    // Sample data
    val categories = listOf(
        ProductCategory("cat1", "Balls, shuttlecocks", 3),
        ProductCategory("cat2", "Rackets", 11),
        ProductCategory("cat3", "Training accessories", 17),
        ProductCategory("cat4", "Jump ropes", 7)
    )

    val products = listOf(
        Product(
            id = "p1",
            name = "Tennis Racket Pro",
            price = 125.0,
            categoryId = "cat2"
        ),
        Product(
            id = "p2",
            name = "Badminton Shuttlecocks (12-pack)",
            price = 15.99,
            discountPercent = 10.0,
            categoryId = "cat1"
        ),
        Product(
            id = "p3",
            name = "Speed Jump Rope",
            price = 24.50,
            categoryId = "cat4"
        ),
        Product(
            id = "p4",
            name = "Resistance Band Set",
            price = 35.0,
            discountAmount = 5.0,
            categoryId = "cat3"
        ),
        Product(
            id = "p5",
            name = "Tennis Balls (4-pack)",
            price = 9.99,
            categoryId = "cat1"
        ),
        Product(
            id = "p6",
            name = "Premium Badminton Racket",
            price = 89.99,
            categoryId = "cat2"
        )
    )

    RFMQuickPOSTheme {
        CatalogScreen(
            categories = categories,
            products = products,
            onProductClicked = {},
            onCartClicked = {},
            onBackClicked = {},
            cartItemCount = 3,
            title = "Sports goods"
        )
    }
}