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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.presentation.common.components.ProductCard
import com.rfm.quickpos.presentation.common.components.RfmCategoryChip
import com.rfm.quickpos.presentation.common.components.RfmLoadingIndicator
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmSearchBar
import com.rfm.quickpos.presentation.common.theme.RfmRed

/**
 * Enhanced Catalog screen with fixed cart badge and improved visuals
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
                        // Enhanced title with better spacing and brand emphasis
                        Text(
                            text = "RFM",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
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

                    // Explicit implementation of cart icon with badge
                    // This ensures the cart icon is always visible with proper badge
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

                        // Only show badge if cart has items
                        if (state.cartItemCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = RfmRed,
                                modifier = Modifier
                                    .size(if (state.cartItemCount > 9) 22.dp else 20.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = if (state.cartItemCount > 99) "99+" else state.cartItemCount.toString(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
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
                // Barcode scanner FAB with enhanced styling
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

                // Custom item FAB with enhanced styling
                FloatingActionButton(
                    onClick = onAddCustomItem,
                    containerColor = RfmRed,
                    contentColor = Color.White,
                    modifier = Modifier.shadow(elevation = 4.dp, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
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
                // Enhanced search bar with subtle shadow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    RfmSearchBar(
                        query = state.searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onSearch = onSearchSubmit,
                        placeholder = "Search products",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (state.isLoading) {
                    // Enhanced loading state with animation
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        RfmLoadingIndicator(
                            size = 48f,
                            strokeWidth = 4f,
                            text = "Loading products...",
                            animated = true
                        )
                    }
                } else if (state.error != null) {
                    // Enhanced error state with better visual styling
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
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            RfmPrimaryButton(
                                text = "Retry",
                                onClick = onSearchSubmit,
                                leadingIcon = Icons.Default.QrCode
                            )
                        }
                    }
                } else if (state.products.isEmpty()) {
                    // Enhanced empty state with icon and better messaging
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
                    // Categories row with enhanced visual styling
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
                    }

                    // Enhanced products grid with better item sizing
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.products) { product ->
                            // Enhanced product card with click handling for entire card
                            ProductCard(
                                name = product.name,
                                price = "AED ${String.format("%.2f", product.price)}",
                                onClick = {
                                    // Add to cart first, then proceed to product details if needed
                                    onAddToCart(product)
                                    onProductClick(product)
                                },
                                imageUrl = product.imageUrl,
                                discountPercentage = product.discountPercentage,
                            )
                        }
                    }
                }
            }
        }
    }
}