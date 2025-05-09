// app/src/main/java/com/rfm/quickpos/MainActivity.kt

package com.rfm.quickpos

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.rfm.quickpos.data.repository.AuthRepository
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
import com.rfm.quickpos.presentation.navigation.UnifiedNavigation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

        setContent {
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

            RFMQuickPOSTheme {
                // Provide feature flags based on UI mode
                FeatureFlagProvider(uiMode = uiMode) {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentAppState) {
                            AppState.Initializing -> {
                                // Show splash screen during initialization
                                SplashScreen(
                                    onInitializationComplete = { /* Controlled by state machine */ }
                                )
                            }
                            AppState.NeedsDeviceRegistration -> {
                                // Create device pairing view model
                                val viewModel = remember {
                                    DevicePairingViewModel(deviceRepository)
                                }

                                // Show device pairing screen
                                DevicePairingScreen(
                                    state = viewModel.state.collectAsState().value,
                                    onPairingInfoChange = { viewModel.updatePairingInfo(it) },
                                    onPairingSubmit = {
                                        viewModel.submitPairing()
                                    },
                                    onSkipSetup = {
                                        // For development, allow skipping
                                        _appState.value = AppState.Ready
                                    }
                                )

                                // Observe device registration success
                                LaunchedEffect(viewModel.state) {
                                    val state = viewModel.state.first()
                                    if (state.isPaired) {
                                        // If device is paired, move to ready state
                                        if (uiMode == UiMode.KIOSK) {
                                            // Kiosk mode doesn't need user login
                                            _appState.value = AppState.Ready
                                        } else {
                                            // Cashier mode needs user login
                                            _appState.value = AppState.NeedsAuthentication
                                        }
                                    }
                                }
                            }
                            AppState.NeedsAuthentication -> {
                                // Main app content with connectivity banner and PIN login
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
                                        onLoginSuccess = {
                                            // When login is successful, transition to appropriate state
                                            if (deviceRepository.isDeviceRegistered()) {
                                                _appState.value = AppState.Ready
                                            } else {
                                                _appState.value = AppState.NeedsDeviceRegistration
                                            }
                                        },
                                        deviceRepository = deviceRepository // Pass the repository
                                    )
                                }
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

                                    // Use UnifiedNavigation with the appropriate start destination
                                    UnifiedNavigation(
                                        navController = navController,
                                        // Skip to the home screen since we're already authenticated
                                        startDestination = if (uiMode == UiMode.CASHIER)
                                            "dashboard" else "kiosk_attract",
                                        uiMode = uiMode,
                                        onChangeMode = { newMode ->
                                            lifecycleScope.launch {
                                                deviceRepository.updateUiMode(newMode)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize app state based on device registration and authentication status
     */
    private fun initializeAppState() {
        lifecycleScope.launch {
            // Start with initializing state
            _appState.value = AppState.Initializing

            // Simulate splash screen delay
            delay(1500)

            // First step should be authentication
            _appState.value = AppState.NeedsAuthentication

            // Check if device is registered
            if (!deviceRepository.isDeviceRegistered()) {
                _appState.value = AppState.NeedsDeviceRegistration
                return@launch
            }

            // Try to authenticate device
            try {
                deviceRepository.authenticateDevice()
                // Device authenticated successfully

                // Check UI mode to determine next step
                if (deviceRepository.uiMode.first() == UiMode.KIOSK) {
                    // Kiosk mode doesn't need user authentication
                    _appState.value = AppState.Ready
                } else {
                    // In Cashier mode, check if user is authenticated
                    if (authRepository.isAuthenticated()) {
                        _appState.value = AppState.Ready
                    } else {
                        _appState.value = AppState.NeedsAuthentication
                    }
                }
            } catch (e: Exception) {
                // Failed to authenticate device, need to re-register
                _appState.value = AppState.NeedsDeviceRegistration
            }
        }
    }

    /**
     * Apply system UI changes based on the UI mode
     */
    @Composable
    private fun ApplySystemUIChanges(uiMode: UiMode) {
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
}