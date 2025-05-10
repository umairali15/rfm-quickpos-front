// app/src/main/java/com/rfm/quickpos/QuickPOSApplication.kt

package com.rfm.quickpos

import android.app.Application
import android.util.Log
import com.rfm.quickpos.data.local.storage.SecureCredentialStore
import com.rfm.quickpos.data.remote.api.ApiService
import com.rfm.quickpos.data.remote.api.RetrofitClient
import com.rfm.quickpos.data.repository.AuthRepository
import com.rfm.quickpos.data.repository.CatalogRepository
import com.rfm.quickpos.data.repository.DeviceRepository
import com.rfm.quickpos.domain.manager.ConnectivityManager
import com.rfm.quickpos.presentation.features.cart.CartRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class QuickPOSApplication : Application() {

    // Application coroutine scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Global app components
    lateinit var secureCredentialStore: SecureCredentialStore
    lateinit var apiService: ApiService
    lateinit var deviceRepository: DeviceRepository
    lateinit var authRepository: AuthRepository
    lateinit var connectivityManager: ConnectivityManager
    lateinit var catalogRepository: CatalogRepository
    lateinit var cartRepository: CartRepository

    override fun onCreate() {
        super.onCreate()

        Log.d("QuickPOSApplication", "Initializing application")

        // Initialize secure credential store
        secureCredentialStore = SecureCredentialStore(this)

        // Initialize API service
        apiService = RetrofitClient.create(secureCredentialStore)

        // Initialize repositories
        deviceRepository = DeviceRepository(apiService, secureCredentialStore, this)
        authRepository = AuthRepository(apiService, secureCredentialStore)
        catalogRepository = CatalogRepository(apiService, secureCredentialStore, this)

        // Initialize connectivity manager
        connectivityManager = ConnectivityManager(this)
        cartRepository = CartRepository()

        // Attempt to load initial configuration
        applicationScope.launch {
            try {
                // If device is registered, try to authenticate in background
                if (deviceRepository.isDeviceRegistered()) {
                    deviceRepository.authenticateDevice()
                }
            } catch (e: Exception) {
                Log.e("QuickPOSApplication", "Error during initial device authentication", e)
                // We'll handle this in MainActivity if needed
            }
        }
    }
}