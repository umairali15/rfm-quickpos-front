// app/src/main/java/com/rfm/quickpos/presentation/features/home/DashboardScreen.kt

package com.rfm.quickpos.presentation.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.rfm.quickpos.domain.model.UiMode
import com.rfm.quickpos.presentation.common.models.ActionCardData
import com.rfm.quickpos.presentation.debug.DebugMenu
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modern dashboard screen with improved design and functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    metrics: DashboardMetrics,
    onNewSaleClicked: () -> Unit,
    onReportsClicked: () -> Unit,
    onOrdersClicked: () -> Unit,
    onCatalogClicked: () -> Unit,
    onCustomersClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    userName: String,
    modifier: Modifier = Modifier,
    additionalActions: List<ActionCardData> = emptyList(),
    navController: NavController? = null,
    currentUiMode: UiMode? = null,
    onChangeMode: ((UiMode) -> Unit)? = null,
    onLogout: () -> Unit = {},
    onLogoutClick: () -> Unit // Changed from nullable to required
) {
    // Debug menu and profile menu visibility states
    var showDebugMenu by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }

    // Current date display
    val dateFormatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    val currentDate = dateFormatter.format(Date())

    // Show debug menu if requested
    if (showDebugMenu && navController != null && currentUiMode != null && onChangeMode != null) {
        DebugMenu(
            navController = navController,
            currentMode = currentUiMode,
            onChangeMode = onChangeMode,
            onDismiss = { showDebugMenu = false }
        )
    }

    // Profile menu dialog
    if (showProfileMenu) {
        ProfileMenuDialog(
            userName = userName,
            onDismiss = { showProfileMenu = false },
            onLogout = {
                showProfileMenu = false
                onLogoutClick() // Call the logout function
            },
            onSettings = {
                showProfileMenu = false
                onSettingsClicked()
            }
        )
    }

    Scaffold(
        topBar = {
            // Modern top app bar with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App title and logo
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "RFM QuickPOS",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    // Action buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Debug button (only in development)
                        IconButton(onClick = { showDebugMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Debug Menu",
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        // User profile button
                        IconButton(onClick = { showProfileMenu = true }) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = userName.first().toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Greeting and date section with animation
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500)) +
                        slideInVertically(animationSpec = tween(500)) { it / 2 }
            ) {
                Column {
                    // Welcome message
                    Text(
                        text = "Hello, $userName",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Current date
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Quick action - New Sale button (prominent)
            ElevatedCard(
                onClick = onNewSaleClicked,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 4.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "New Sale",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "Start a new transaction",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key metrics with modern design
            Text(
                text = "Today's Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )

            // Metrics cards in a grid layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sales metric
                MetricCard(
                    title = "Sales",
                    value = metrics.totalSales,
                    icon = Icons.Default.Money,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )

                // Orders metric
                MetricCard(
                    title = "Avg. Sale",
                    value = metrics.salesAmount,
                    icon = Icons.Default.Receipt,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )

                // Customers metric
                MetricCard(
                    title = "Customers",
                    value = metrics.customers,
                    icon = Icons.Default.People,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Section
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Grid of actions - 2 columns
            GridActions(
                actions = listOf(
                    ActionItem(
                        title = "Orders",
                        icon = Icons.Outlined.Receipt,
                        onClick = onOrdersClicked
                    ),
                    ActionItem(
                        title = "Catalog",
                        icon = Icons.Outlined.ShoppingBag,
                        onClick = onCatalogClicked
                    ),
                    ActionItem(
                        title = "Reports",
                        icon = Icons.Outlined.Description,
                        onClick = onReportsClicked
                    ),
                    ActionItem(
                        title = "Customers",
                        icon = Icons.Outlined.Person,
                        onClick = onCustomersClicked
                    )
                )
            )

            // Show additional actions (shift management) if provided
            if (additionalActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                // Shift Management section header
                Text(
                    text = "Shift Management",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Create rows of shift actions with modern styling
                val shiftActions = additionalActions.map { action ->
                    ActionItem(
                        title = action.title,
                        icon = action.icon,
                        onClick = action.onClick,
                        backgroundColor = action.backgroundColor,
                        contentColor = action.contentColor
                    )
                }

                GridActions(actions = shiftActions)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Profile menu dialog component
 */
@Composable
private fun ProfileMenuDialog(
    userName: String,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onSettings: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // User avatar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = userName.first().toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User name
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = "Cashier",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Menu options
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    text = "Settings",
                    onClick = onSettings
                )

                ProfileMenuItem(
                    icon = Icons.Default.Logout,
                    text = "Logout",
                    onClick = onLogout,
                    tint = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Close button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

/**
 * Profile menu item component
 */
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = tint
            )
        }
    }
}

/**
 * Modern metric card component
 */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Data class for action items in the grid
 */
private data class ActionItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val backgroundColor: Color = Color.Transparent,
    val contentColor: Color = Color.Unspecified
)

/**
 * Grid layout for action buttons
 */
@Composable
private fun GridActions(
    actions: List<ActionItem>,
    columns: Int = 2
) {
    // Calculate number of rows needed
    val rows = (actions.size + columns - 1) / columns

    // Create grid layout
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < actions.size) {
                        val action = actions[index]

                        // Use custom colors if provided, otherwise use defaults
                        val backgroundColor = if (action.backgroundColor != Color.Transparent) {
                            action.backgroundColor
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }

                        val contentColor = if (action.contentColor != Color.Unspecified) {
                            action.contentColor
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        ActionCard(
                            title = action.title,
                            icon = action.icon,
                            onClick = action.onClick,
                            backgroundColor = backgroundColor,
                            contentColor = contentColor,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Empty space to maintain grid layout
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Modern action card component
 */
@Composable
private fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(110.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}