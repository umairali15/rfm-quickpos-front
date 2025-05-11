// app/src/main/java/com/rfm/quickpos/presentation/features/kiosk/KioskItemDetailScreen.kt

package com.rfm.quickpos.presentation.features.kiosk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfm.quickpos.QuickPOSApplication
import com.rfm.quickpos.data.remote.models.Item
import com.rfm.quickpos.data.remote.models.VariationOption
import com.rfm.quickpos.presentation.common.components.RfmOutlinedButton
import com.rfm.quickpos.presentation.common.components.RfmPrimaryButton
import com.rfm.quickpos.presentation.common.components.RfmQuantitySelector
import com.rfm.quickpos.presentation.features.cart.CartItemWithModifiers
import com.rfm.quickpos.presentation.features.catalog.BusinessTypeItemInfo
import com.rfm.quickpos.presentation.features.catalog.VariationsSection
import kotlinx.coroutines.delay

/**
 * Kiosk version of item detail screen with larger touch targets
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KioskItemDetailScreen(
    itemId: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get repositories
    val context = LocalContext.current
    val catalogRepository = (context.applicationContext as QuickPOSApplication).catalogRepository
    val cartRepository = (context.applicationContext as QuickPOSApplication).cartRepository

    // Collect data
    val items by catalogRepository.items.collectAsState()
    val businessTypeConfig by catalogRepository.businessTypeConfig.collectAsState()

    // Find the item
    val item = items.find { it.id == itemId }

    // Auto-reset timer to attract screen after inactivity
    var inactivitySeconds by remember { mutableStateOf(0) }
    val maxInactivitySeconds = 180 // 3 minutes for item detail

    LaunchedEffect(inactivitySeconds) {
        while (true) {
            delay(1000)
            inactivitySeconds++

            if (inactivitySeconds >= maxInactivitySeconds) {
                onClose() // Return to menu
                break
            }
        }
    }

    // Reset inactivity timer on user interaction
    fun resetInactivityTimer() {
        inactivitySeconds = 0
    }

    // Handle navigation if item not found
    if (item == null) {
        LaunchedEffect(Unit) {
            onClose()
        }
        return
    }

    // Local state
    var quantity by remember { mutableIntStateOf(1) }
    val selectedVariations = remember { mutableStateMapOf<String, VariationOption>() }

    // Get variations
    val variations = item.settings?.inventory?.variations ?: emptyList()

    // Calculate pricing
    val basePrice = item.price
    val variationsPriceAdjustment = selectedVariations.values.sumOf { it.priceAdjustment }
    val totalUnitPrice = basePrice + variationsPriceAdjustment
    val totalPrice = totalUnitPrice * quantity

    // Initialize default variations
    LaunchedEffect(variations) {
        variations.forEach { variation ->
            if (variation.options.isNotEmpty() && !selectedVariations.containsKey(variation.name)) {
                selectedVariations[variation.name] = variation.options.first()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            resetInactivityTimer()
                            onClose()
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
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 120.dp) // Space for bottom button
            ) {
                // Item header card
                ItemHeaderCard(
                    item = item,
                    basePrice = basePrice,
                    totalPrice = totalUnitPrice,
                    businessTypeConfig = businessTypeConfig
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quantity selector
                QuantityCard(
                    quantity = quantity,
                    onQuantityChange = {
                        resetInactivityTimer()
                        quantity = it
                    }
                )

                // Variations section
                if (variations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    VariationsSection(
                        variations = variations,
                        selectedVariations = selectedVariations,
                        onVariationSelected = { variationName, option ->
                            resetInactivityTimer()
                            selectedVariations[variationName] = option
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Price summary card
                PriceSummaryCard(
                    basePrice = basePrice,
                    variations = selectedVariations.values.toList(),
                    quantity = quantity,
                    total = totalPrice
                )
            }

            // Bottom action section
            Surface(
                shadowElevation = 16.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    RfmOutlinedButton(
                        text = "Back to Menu",
                        onClick = {
                            resetInactivityTimer()
                            onClose()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    RfmPrimaryButton(
                        text = "Add to Cart",
                        onClick = {
                            // Create cart item with variations
                            val cartItem = CartItemWithModifiers(
                                id = item.id,
                                name = item.name,
                                price = basePrice,
                                quantity = quantity,
                                variations = selectedVariations.toMap()
                            )

                            cartRepository.addCartItem(cartItem)
                            onClose()
                        },
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.ShoppingCart
                    )
                }
            }
        }
    }
}

/**
 * Item header card with larger layout for kiosk
 */
@Composable
private fun ItemHeaderCard(
    item: Item,
    basePrice: Double,
    totalPrice: Double,
    businessTypeConfig: com.rfm.quickpos.data.remote.models.BusinessTypeConfig?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Image placeholder - larger for kiosk
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                // Placeholder for item image
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Item name and description
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            item.description?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price display
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Price: ",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "AED ${String.format("%.2f", basePrice)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (totalPrice != basePrice) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )

                if (totalPrice != basePrice) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "AED ${String.format("%.2f", totalPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Business type specific info
            BusinessTypeItemInfo(item, businessTypeConfig)
        }
    }
}

/**
 * Quantity selector card
 */
@Composable
private fun QuantityCard(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Quantity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )

            RfmQuantitySelector(
                quantity = quantity,
                onQuantityChange = onQuantityChange,
                minValue = 1,
                maxValue = 99
            )
        }
    }
}

/**
 * Price summary card
 */
@Composable
private fun PriceSummaryCard(
    basePrice: Double,
    variations: List<VariationOption>,
    quantity: Int,
    total: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Base price
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Base Price",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "AED ${String.format("%.2f", basePrice)}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Variations
            variations.forEach { variation ->
                if (variation.priceAdjustment != 0.0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "• ${variation.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${if (variation.priceAdjustment > 0) "+" else ""}AED ${String.format("%.2f", variation.priceAdjustment)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (variation.priceAdjustment > 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Quantity
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Quantity",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "$quantity",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Total
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AED ${String.format("%.2f", total)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}