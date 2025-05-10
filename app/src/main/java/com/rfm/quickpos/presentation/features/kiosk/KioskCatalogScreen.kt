package com.rfm.quickpos.presentation.features.kiosk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.presentation.common.components.ProductCard
import com.rfm.quickpos.presentation.common.components.RfmCategoryChip
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.data.remote.models.Product
import com.rfm.quickpos.data.remote.models.ProductCategory
import kotlinx.coroutines.delay

/**
 * Enhanced Kiosk mode catalog screen with prominent cart button
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
    var showCartButton by remember { mutableStateOf(false) }

    // Filter products based on selected category
    val displayedProducts = if (selectedCategoryId != null) {
        sampleProducts.filter { it.categoryId == selectedCategoryId }
    } else {
        sampleProducts
    }

    // Show cart button only when items are added
    LaunchedEffect(cartItemCount) {
        if (cartItemCount > 0 && !showCartButton) {
            showCartButton = true
        } else if (cartItemCount == 0) {
            delay(300) // Delay to allow animation to complete
            showCartButton = false
        }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // Use bottom app bar instead of normal action buttons
        bottomBar = {
            AnimatedVisibility(
                visible = showCartButton,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
            ) {
                // Large Cart Button - Only shown when cart has items
                Card(
                    onClick = {
                        resetInactivityTimer()
                        onCartClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(72.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text(
                                            text = cartItemCount.toString(),
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "View Cart",
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.padding(horizontal = 12.dp))

                            Text(
                                text = "View Cart",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "AED ${String.format("%.2f", 35.0)}", // Replace with actual total
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
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

            // Floating action button for cart when empty
            if (!showCartButton) {
                FloatingActionButton(
                    onClick = {
                        resetInactivityTimer()
                        onCartClick()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "View Cart",
                        modifier = Modifier.size(32.dp)
                    )
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