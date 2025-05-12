// app/src/main/java/com/rfm/quickpos/MainActivity.kt

package com.rfm.quickpos

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.rfm.quickpos.data.repository.AuthRepository
import com.rfm.quickpos.data.repository.AuthState
import com.rfm.quickpos.data.repository.DeviceRepository
import com.rfm.quickpos.domain.manager.ConnectivityManager
import com.rfm.quickpos.domain.model.UiMode
import com.rfm.quickpos.presentation.common.components.ConnectivityBanner
import com.rfm.quickpos.presentation.common.components.FeatureFlagProvider
import com.rfm.quickpos.presentation.common.theme.RFMQuickPOSTheme
import com.rfm.quickpos.presentation.features.setup.DevicePairingScreen
import com.rfm.quickpos.presentation.features.setup.DevicePairingViewModel
import com.rfm.quickpos.presentation.features.splash.SplashScreen
import com.rfm.quickpos.presentation.navigation.AuthScreen
import com.rfm.quickpos.presentation.navigation.KioskScreen
import com.rfm.quickpos.presentation.navigation.Screen
import com.rfm.quickpos.presentation.navigation.UnifiedNavigation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    // Repositories
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var connectivityManager: ConnectivityManager

    // App state
    private val _appState = MutableStateFlow<AppState>(AppState.Initializing)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get repositories from application
        deviceRepository = (application as QuickPOSApplication).deviceRepository
        authRepository = (application as QuickPOSApplication).authRepository

        // Initialize ConnectivityManager
        connectivityManager = ConnectivityManager(this)

        // Start monitoring connectivity
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                connectivityManager.startMonitoring()
            }
        }

        // Initialize app state
        initializeAppState()

        // Monitor auth state changes
        lifecycleScope.launch {
            authRepository.authState.collectLatest { state ->
                Log.d(TAG, "Auth state changed: $state")
                when (state) {
                    is AuthState.Success -> {
                        // Check device registration
                        if (!deviceRepository.isDeviceRegistered()) {
                            Log.d(TAG, "User authenticated but device not registered")
                            _appState.value = AppState.NeedsDeviceRegistration
                        } else {
                            Log.d(TAG, "User authenticated and device registered, moving to Ready")
                            _appState.value = AppState.Ready
                        }
                    }
                    is AuthState.Error -> {
                        // If auth error and we're in Ready state, go back to NeedsAuthentication
                        if (_appState.value == AppState.Ready) {
                            Log.d(TAG, "Auth error, going back to login screen")
                            _appState.value = AppState.NeedsAuthentication
                        }
                    }
                    else -> {
                        // Don't change app state for other auth states
                    }
                }
            }
        }

        // Monitor UI mode changes - FIX: Added more responsive UI mode monitoring
        lifecycleScope.launch {
            deviceRepository.uiMode.collectLatest { mode ->
                Log.d(TAG, "UI mode changed to: $mode - updating app behavior")

                // Only update navigation if we're in the Ready state
                if (_appState.value == AppState.Ready) {
                    // Force redraw of navigation with updated mode
                    setContent {
                        RFMQuickPOSTheme {
                            MainScreen()
                        }
                    }
                }
            }
        }

        setContent {
            RFMQuickPOSTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        // Get current UI mode
        val uiMode by deviceRepository.uiMode.collectAsState()

        // Get connectivity status
        val connectivityStatus by connectivityManager.connectionStatus.collectAsState()

        // Get pending sync count
        val pendingSyncCount by connectivityManager.pendingSyncCount.collectAsState()

        // Get current app state
        val currentAppState by appState.collectAsState()

        // Apply system UI changes based on mode
        ApplySystemUIChanges(uiMode)

        // Log state changes for debugging
        LaunchedEffect(currentAppState) {
            Log.d(TAG, "App state changed to: $currentAppState")
        }

        // Log UI mode to verify correct value is being used
        LaunchedEffect(uiMode) {
            Log.d(TAG, "Current UI mode in composition: $uiMode")
        }

        // Provide feature flags based on UI mode
        FeatureFlagProvider(uiMode = uiMode) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (currentAppState) {
                    AppState.Initializing -> {
                        SplashScreen(
                            onInitializationComplete = { /* Controlled by state machine */ }
                        )
                    }
                    AppState.NeedsDeviceRegistration -> {
                        DevicePairingScreenWithNavigation()
                    }
                    AppState.NeedsAuthentication -> {
                        // Create the navigation controller
                        val navController = rememberNavController()

                        // Use UnifiedNavigation with PIN login as the start
                        UnifiedNavigation(
                            navController = navController,
                            startDestination = AuthScreen.PinLogin.route,
                            uiMode = uiMode,
                            onChangeMode = { newMode ->
                                lifecycleScope.launch {
                                    deviceRepository.updateUiMode(newMode)
                                }
                            },
                            onLoginSuccess = { userId ->
                                Log.d(TAG, "Login success callback: $userId")
                                // Check device registration
                                lifecycleScope.launch {
                                    if (!deviceRepository.isDeviceRegistered()) {
                                        Log.d(TAG, "Login successful, but device needs registration")
                                        _appState.value = AppState.NeedsDeviceRegistration
                                    } else {
                                        Log.d(TAG, "Login successful, device already registered, moving to Ready")
                                        _appState.value = AppState.Ready
                                    }
                                }
                            },
                            onLogout = {
                                Log.d(TAG, "Logout requested")
                                lifecycleScope.launch {
                                    // Clear catalog cache on logout
                                    val catalogRepository = (application as QuickPOSApplication).catalogRepository
                                    catalogRepository.clearCache()

                                    // Logout user
                                    authRepository.logout()

                                    // Stop connectivity monitoring and clear any data
                                    connectivityManager.stopMonitoring()

                                    // Reset app state
                                    _appState.value = AppState.NeedsAuthentication
                                }
                            },
                            authRepository = authRepository,
                            deviceRepository = deviceRepository
                        )
                    }
                    AppState.Ready -> {
                        // Main app content with connectivity banner
                        Column {
                            // Global connectivity banner
                            ConnectivityBanner(
                                status = connectivityStatus,
                                pendingSyncCount = pendingSyncCount,
                                onRetryClick = {
                                    lifecycleScope.launch {
                                        connectivityManager.retryPendingSync()
                                    }
                                }
                            )

                            // Create the navigation controller
                            val navController = rememberNavController()

                            // Trigger catalog sync when app is ready
                            val catalogRepository = (application as QuickPOSApplication).catalogRepository

                            // Remember if we've already triggered sync for this session
                            val hasTriggeredSync = remember { mutableStateOf(false) }

                            LaunchedEffect(Unit) {
                                if (!hasTriggeredSync.value) {
                                    hasTriggeredSync.value = true

                                    launch {
                                        try {
                                            // First get company info (if not already cached)
                                            if (catalogRepository.companyInfo.value == null) {
                                                Log.d(TAG, "Fetching company info...")
                                                catalogRepository.fetchCompanyInfo()
                                            }

                                            // Check if we need to sync catalog data
                                            if (catalogRepository.categories.value.isEmpty() ||
                                                catalogRepository.items.value.isEmpty()) {
                                                Log.d(TAG, "Syncing catalog data...")
                                                catalogRepository.syncCatalogData()
                                            } else {
                                                Log.d(TAG, "Catalog data already available")
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error during catalog sync", e)
                                        }
                                    }
                                }
                            }

                            // FIX: Get the current UI mode from repository every time navigation is built
                            // to ensure it's always using the latest value
                            val currentUiMode = deviceRepository.uiMode.collectAsState().value
                            Log.d(TAG, "Building navigation with UI mode: $currentUiMode")

                            // Use UnifiedNavigation with the appropriate start destination
                            UnifiedNavigation(
                                navController = navController,
                                // Skip to the home screen since we're already authenticated
                                startDestination = if (currentUiMode == UiMode.CASHIER)
                                    Screen.Dashboard.route else KioskScreen.Attract.route,
                                uiMode = currentUiMode,
                                onChangeMode = { newMode ->
                                    lifecycleScope.launch {
                                        deviceRepository.updateUiMode(newMode)
                                    }
                                },
                                onLogout = {
                                    Log.d(TAG, "Logout requested")
                                    lifecycleScope.launch {
                                        // Clear catalog cache on logout
                                        val catalogRepository = (application as QuickPOSApplication).catalogRepository
                                        catalogRepository.clearCache()

                                        // Logout user
                                        authRepository.logout()

                                        // Stop connectivity monitoring
                                        connectivityManager.stopMonitoring()

                                        // Clear any cached device info (optional, depending on your needs)
                                        // deviceRepository.clearDeviceInfo()

                                        // Reset app state to authentication needed
                                        _appState.value = AppState.NeedsAuthentication
                                    }
                                },
                                authRepository = authRepository,
                                deviceRepository = deviceRepository
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Wraps the DevicePairingScreen composable with proper state handling
     */
    @Composable
    private fun DevicePairingScreenWithNavigation() {
        // Create the view model
        val viewModel = remember { DevicePairingViewModel(deviceRepository) }

        // Collect state
        val state by viewModel.state.collectAsState()

        // Handle navigation based on pairing state
        LaunchedEffect(state.isPaired) {
            if (state.isPaired) {
                Log.d(TAG, "Device pairing successful - transitioning to Ready state")
                // After device registration, go directly to Ready state
                _appState.value = AppState.Ready
            }
        }

        // Render the pairing screen
        DevicePairingScreen(
            state = state,
            onPairingInfoChange = { viewModel.updatePairingInfo(it) },
            onPairingSubmit = { viewModel.submitPairing() },
            onSkipSetup = {
                // For development, allow skipping
                lifecycleScope.launch {
                    _appState.value = AppState.Ready
                    Log.d(TAG, "Skipping setup, moving to Ready state")
                }
            }
        )
    }

    private fun initializeAppState() {
        lifecycleScope.launch {
            // Start with initializing state
            _appState.value = AppState.Initializing

            // Simulate splash screen delay
            delay(1500)

            // Always check with the server for the latest device settings
            if (deviceRepository.isDeviceRegistered()) {
                try {
                    // Force device re-authentication to get the latest settings including UI mode
                    Log.d(TAG, "Authenticating device on startup to get latest settings")
                    val deviceAuthResult = deviceRepository.authenticateDevice()
                    Log.d(TAG, "Device auth on startup completed with result: $deviceAuthResult")

                    // Allow a short delay for UI mode changes to propagate
                    delay(300)
                } catch (e: Exception) {
                    Log.e(TAG, "Error authenticating device on startup", e)
                }
            }

            // Now continue with normal app initialization
            if (!authRepository.isAuthenticated()) {
                Log.d(TAG, "User not authenticated, need login")
                _appState.value = AppState.NeedsAuthentication
            }
            // If authenticated, check device registration
            else if (!deviceRepository.isDeviceRegistered()) {
                Log.d(TAG, "User authenticated but device not registered, need registration")
                _appState.value = AppState.NeedsDeviceRegistration
            }
            // Both authenticated and registered, go to main app
            else {
                Log.d(TAG, "User authenticated and device registered, ready to start")
                _appState.value = AppState.Ready
            }
        }
    }

    /**
     * Apply system UI changes based on the UI mode
     */
    @Composable
    private fun ApplySystemUIChanges(uiMode: UiMode) {
        LaunchedEffect(uiMode) {
            if (uiMode == UiMode.KIOSK) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                    hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                WindowCompat.setDecorFitsSystemWindows(window, true)
                try {
                    stopLockTask()
                } catch (e: Exception) {
                    // Not in lock task mode, ignore
                }
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                }
            }
        }
    }

    /**
     * Helper method to navigate to the correct home screen based on UI mode
     */
    private fun navigateToHomeScreen(mode: UiMode) {
        Log.d(TAG, "Attempting to navigate to home screen for mode: $mode")

        // We're using the state machine pattern, so we just need to update app state
        // The composable logic will handle the actual navigation based on this state
        if (_appState.value == AppState.Ready) {
            // Trigger a re-composition by setting the state to itself
            _appState.value = AppState.Ready
        }
    }

    companion object {
        const val ACTION_SET_KIOSK_MODE = "com.rfm.quickpos.SET_KIOSK_MODE"
        const val EXTRA_ENABLE_KIOSK = "enable_kiosk"
    }
}

/**
 * States for the app startup flow
 */
sealed class AppState {
    // App is initializing (splash screen)
    object Initializing : AppState()

    // User needs to log in (cashier mode only)
    object NeedsAuthentication : AppState()

    // Device needs to be registered with the backend
    object NeedsDeviceRegistration : AppState()

    // All set up and ready to use the app
    object Ready : AppState()

    override fun toString(): String {
        return when(this) {
            is Initializing -> "Initializing"
            is NeedsAuthentication -> "NeedsAuthentication"
            is NeedsDeviceRegistration -> "NeedsDeviceRegistration"
            is Ready -> "Ready"
        }
    }
}