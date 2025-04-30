// app/src/main/java/com/rfm/quickpos/domain/manager/ConnectivityManager.kt

package com.rfm.quickpos.domain.manager

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.rfm.quickpos.presentation.common.components.ConnectivityStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages network connectivity state and handles offline synchronization
 */
class ConnectivityManager(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // Connection status flow
    private val _connectionStatus = MutableStateFlow(ConnectivityStatus.ONLINE)
    val connectionStatus: StateFlow<ConnectivityStatus> = _connectionStatus.asStateFlow()

    // Pending sync count flow
    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    // Network callback
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Network is available
            coroutineScope.launch {
                if (_pendingSyncCount.value > 0) {
                    _connectionStatus.value = ConnectivityStatus.SYNCING
                    // Simulate sync
                    kotlinx.coroutines.delay(1500)
                    _pendingSyncCount.value = 0
                    _connectionStatus.value = ConnectivityStatus.ONLINE
                } else {
                    _connectionStatus.value = ConnectivityStatus.ONLINE
                }
            }
        }

        override fun onLost(network: Network) {
            // Network is lost
            _connectionStatus.value = ConnectivityStatus.OFFLINE
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            // Network capabilities changed (e.g. WiFi to mobile)
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            if (hasInternet) {
                if (_pendingSyncCount.value > 0) {
                    _connectionStatus.value = ConnectivityStatus.PENDING_SYNC
                } else {
                    _connectionStatus.value = ConnectivityStatus.ONLINE
                }
            } else {
                _connectionStatus.value = ConnectivityStatus.OFFLINE
            }
        }
    }

    /**
     * Start monitoring network connectivity
     */
    fun startMonitoring() {
        // Check initial state
        checkInitialConnectionState()

        // Register network callback
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    /**
     * Stop monitoring network connectivity
     */
    fun stopMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // May not be registered
        }
    }

    /**
     * Check initial connection state
     */
    private fun checkInitialConnectionState() {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        if (activeNetwork != null && networkCapabilities != null &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            _connectionStatus.value = ConnectivityStatus.ONLINE
        } else {
            _connectionStatus.value = ConnectivityStatus.OFFLINE
        }
    }

    /**
     * Simulate adding a pending change when offline
     */
    fun addPendingChange() {
        if (_connectionStatus.value == ConnectivityStatus.OFFLINE) {
            _pendingSyncCount.value = _pendingSyncCount.value + 1
        }
    }

    /**
     * Retry syncing pending changes
     */
    suspend fun retryPendingSync() {
        if (_connectionStatus.value == ConnectivityStatus.PENDING_SYNC ||
            _connectionStatus.value == ConnectivityStatus.OFFLINE) {

            // Check if we can connect
            checkInitialConnectionState()

            if (_connectionStatus.value != ConnectivityStatus.OFFLINE &&
                _pendingSyncCount.value > 0) {
                _connectionStatus.value = ConnectivityStatus.SYNCING

                // Simulate sync process
                kotlinx.coroutines.delay(2000)

                _pendingSyncCount.value = 0
                _connectionStatus.value = ConnectivityStatus.ONLINE
            }
        }
    }
}