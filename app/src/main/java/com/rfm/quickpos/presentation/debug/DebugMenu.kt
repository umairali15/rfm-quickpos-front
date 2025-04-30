package com.rfm.quickpos.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.rfm.quickpos.domain.model.UiMode
import com.rfm.quickpos.presentation.features.error.ErrorType
import com.rfm.quickpos.presentation.navigation.Screen

/**
 * Debug menu for navigating directly to screens during development
 * Only for development use - will be removed in production
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenu(
    navController: NavController,
    currentMode: UiMode,
    onChangeMode: (UiMode) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Developer Menu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Mode switcher
                Text(
                    text = "UI Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    UiMode.values().forEach { mode ->
                        FilterChip(
                            selected = mode == currentMode,
                            onClick = { onChangeMode(mode) },
                            label = { Text(mode.name) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Screen list
                Text(
                    text = "Screens",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                LazyColumn {
                    // All routes for easy access
                    item {
                        ScreenSection(title = "Core Screens") {
                            ScreenButton("Splash") { navController.navigate(Screen.Splash.route) }
                            ScreenButton("Dashboard") { navController.navigate(Screen.Dashboard.route) }
                            ScreenButton("Catalog") { navController.navigate(Screen.Catalog.route) }
                            ScreenButton("Cart") { navController.navigate(Screen.Cart.route) }
                            ScreenButton("Payment") { navController.navigate(Screen.Payment.route) }
                            ScreenButton("Payment Success") { navController.navigate(Screen.PaymentSuccess.route) }
                            ScreenButton("Sales History") { navController.navigate(Screen.SalesHistory.route) }
                        }
                    }

                    item {
                        ScreenSection(title = "Shift Management") {
                            ScreenButton("Open Shift") { navController.navigate(Screen.OpenShift.route) }
                            ScreenButton("Cash Movement") { navController.navigate(Screen.CashMovement.route) }
                            ScreenButton("Close Shift") { navController.navigate(Screen.CloseShift.route) }
                            ScreenButton("Shift Summary") { navController.navigate(Screen.ShiftSummary.route) }
                        }
                    }

                    item {
                        ScreenSection(title = "Error Screens") {
                            val errorTypes = ErrorType.values()
                            errorTypes.forEach { errorType ->
                                ScreenButton("Error: ${errorType.name}") {
                                    navController.navigate(Screen.Error.createRoute(errorType))
                                }
                            }
                        }
                    }

                    item {
                        ScreenSection(title = "Item Details") {
                            // Sample product IDs
                            val sampleIds = listOf("1", "2", "3", "4")
                            sampleIds.forEach { id ->
                                ScreenButton("Item Detail: $id") {
                                    navController.navigate(Screen.ItemDetail.createRoute(id))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        content()

        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun ScreenButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(text)
    }
}