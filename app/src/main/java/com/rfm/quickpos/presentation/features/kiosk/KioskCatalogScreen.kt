package com.rfm.quickpos.presentation.features.kiosk

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfm.quickpos.presentation.common.components.ProductCard
import com.rfm.quickpos.presentation.common.components.RfmCategoryChip
import com.rfm.quickpos.presentation.common.components.RfmLoadingIndicator
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.features.catalog.Product
import com.rfm.quickpos.presentation.features.catalog.ProductCategory
import kotlinx.coroutines.delay

/**
 * Kiosk mode catalog screen - simplified version of the cashier catalog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KioskCatalogScreen(
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sample data for preview
    val sampleCategories = listOf(
        ProductCategory("1", "Beverages"),
        ProductCategory("2", "Food"),
        ProductCategory("3", "Snacks")
    )

    val sampleProducts = listOf(
        Product(
            id = "1",
            name = "Coffee",
            price = 15.00,
            categoryId = "1"
        ),
        Product(
            id = "2",
            name = "Croissant",
            price = 10.00,
            categoryId = "2"
        ),
        Product(
            id = "3",
            name = "Water Bottle",
            price = 5.00,
            categoryId = "1"
        ),
        Product(
            id = "4",
            name = "Sandwich",
            price = 20.00,
            categoryId = "2"
        ),
        Product(
            id = "5",
            name = "Chocolate Bar",
            price = 8.00,
            categoryId = "3"
        ),
        Product(
            id = "6",
            name = "Protein Bar",
            price = 12.00,
            categoryId = "3"
        )
    )

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var cartItemCount by remember { mutableStateOf(0) }

    // Filter products based on selected category
    val displayedProducts = if (selectedCategoryId != null) {
        sampleProducts.filter { it.categoryId == selectedCategoryId }
    } else {
        sampleProducts
    }

    // Auto-reset timer to attract screen after inactivity
    var inactivitySeconds by remember { mutableStateOf(0) }
    val maxInactivitySeconds = 120 // 2 minutes of inactivity

    LaunchedEffect(inactivitySeconds) {
        while (true) {
            delay(1000)
            inactivitySeconds++

            if (inactivitySeconds >= maxInactivitySeconds) {
                // In real app, navigate back to attract screen
                break
            }
        }
    }

    // Reset inactivity timer on user interaction
    fun resetInactivityTimer() {
        inactivitySeconds = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Menu",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            resetInactivityTimer()
                            onBackClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            resetInactivityTimer()
                            onCartClick()
                        }
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge {
                                        Text(text = cartItemCount.toString())
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
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                // Categories row - larger for touch targets in kiosk mode
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    // All category option
                    item {
                        RfmCategoryChip(
                            text = "All",
                            selected = selectedCategoryId == null,
                            onClick = {
                                resetInactivityTimer()
                                selectedCategoryId = null
                            }
                        )
                    }

                    // Categories from data
                    items(sampleCategories) { category ->
                        RfmCategoryChip(
                            text = category.name,
                            selected = selectedCategoryId == category.id,
                            onClick = {
                                resetInactivityTimer()
                                selectedCategoryId = category.id
                            }
                        )
                    }
                }

                // Products grid - larger cells for kiosk mode
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayedProducts) { product ->
                        KioskProductCard(
                            product = product,
                            onClick = {
                                resetInactivityTimer()
                                onProductClick(product)
                                // For demo, just increment cart count
                                cartItemCount++
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kiosk-specific product card with larger touch targets
 */
@Composable
fun KioskProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProductCard(
        name = product.name,
        price = "AED ${String.format("%.2f", product.price)}",
        onClick = onClick,
        imageUrl = null, // No images in sample data
        discountPercentage = product.discountPercentage,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun KioskCatalogScreenPreview() {
    RFMQuickPOSTheme {
        Surface {
            KioskCatalogScreen(
                onBackClick = {},
                onCartClick = {},
                onProductClick = {}
            )
        }
    }
}