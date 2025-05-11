// app/src/main/java/com/rfm/quickpos/presentation/features/kiosk/KioskCatalogScreen.kt

package com.rfm.quickpos.presentation.features.kiosk

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.QuickPOSApplication
import com.rfm.quickpos.data.repository.CatalogRepository
import com.rfm.quickpos.data.repository.CatalogSyncState
import com.rfm.quickpos.presentation.common.components.BusinessTypeAwareProductCard
import com.rfm.quickpos.presentation.common.components.RfmCategoryChip
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.features.cart.CartRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Enhanced Kiosk mode catalog screen with backend integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KioskCatalogScreen(
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onProductClick: (String) -> Unit, // Changed to take itemId
    modifier: Modifier = Modifier
) {
    // Get repositories from application
    val context = LocalContext.current
    val catalogRepository = (context.applicationContext as QuickPOSApplication).catalogRepository
    val cartRepository = (context.applicationContext as QuickPOSApplication).cartRepository

    // Collect data from repository
    val categories by catalogRepository.categories.collectAsState()
    val items by catalogRepository.items.collectAsState()
    val businessTypeConfig by catalogRepository.businessTypeConfig.collectAsState()
    val companyInfo by catalogRepository.companyInfo.collectAsState()
    val syncState by catalogRepository.syncState.collectAsState()

    // Collect cart state
    val cartItemCount by cartRepository.cartCount.collectAsState()
    val cartTotal by cartRepository.cartTotal.collectAsState()

    // Local UI state
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showCartButton by remember { mutableStateOf(false) }
    var inactivitySeconds by remember { mutableStateOf(0) }

    // Filter products based on selected category
    val displayedItems = remember(selectedCategoryId, items) {
        if (selectedCategoryId != null) {
            items.filter { it.categoryId == selectedCategoryId && it.active }
        } else {
            items.filter { it.active }
        }
    }

    // Coroutine scope for refreshing
    val coroutineScope = rememberCoroutineScope()

    // Auto-reset timer to attract screen after inactivity
    val maxInactivitySeconds = 120 // 2 minutes of inactivity

    LaunchedEffect(inactivitySeconds) {
        while (true) {
            delay(1000)
            inactivitySeconds++

            if (inactivitySeconds >= maxInactivitySeconds) {
                onBackClick() // Return to attract screen
                break
            }
        }
    }

    // Reset inactivity timer on user interaction
    fun resetInactivityTimer() {
        inactivitySeconds = 0
    }

    // Show cart button only when items are added
    LaunchedEffect(cartItemCount) {
        if (cartItemCount > 0 && !showCartButton) {
            showCartButton = true
        } else if (cartItemCount == 0) {
            delay(300)
            showCartButton = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = companyInfo?.name ?: "Menu",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
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
        bottomBar = {
            AnimatedVisibility(
                visible = showCartButton,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
            ) {
                // Large Cart Button
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
                            text = "AED ${String.format("%.2f", cartTotal)}",
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
            AnimatedContent(
                targetState = syncState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "kiosk-catalog-content"
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
                        Column {
                            // Categories row
                            if (categories.isNotEmpty()) {
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

                                    // Categories from backend
                                    items(categories) { category ->
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
                            }

                            // Products grid
                            if (displayedItems.isEmpty()) {
                                EmptyStateContent()
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 180.dp),
                                    contentPadding = PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(displayedItems) { item ->
                                        BusinessTypeAwareProductCard(
                                            item = item,
                                            businessTypeConfig = businessTypeConfig,
                                            onClick = {
                                                resetInactivityTimer()
                                                onProductClick(item.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
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
 * Loading content component
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
                strokeWidth = 4.dp,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Error content component
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
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            RfmPrimaryButton(
                text = "Try Again",
                onClick = onRetry,
                leadingIcon = Icons.Default.Refresh
            )
        }
    }
}

/**
 * Empty state content
 */
@Composable
private fun EmptyStateContent() {
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
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No items available",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please try a different category",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}