package com.rfm.quickpos.domain.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages inactivity timeouts for kiosk mode
 * This is used to automatically return to the attract screen after a period of inactivity
 */
class InactivityManager(
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
    private val onTimeout: () -> Unit
) {
    companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 120_000L // 2 minutes
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var timeoutJob: Job? = null

    private val _remainingTimeMillis = MutableStateFlow(timeoutMillis)
    val remainingTimeMillis: StateFlow<Long> = _remainingTimeMillis.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    /**
     * Start the inactivity timeout
     */
    fun start() {
        reset()
        _isActive.value = true
    }

    /**
     * Stop the inactivity timeout
     */
    fun stop() {
        timeoutJob?.cancel()
        timeoutJob = null
        _isActive.value = false
        _remainingTimeMillis.value = timeoutMillis
    }

    /**
     * Reset the inactivity timeout
     */
    fun reset() {
        timeoutJob?.cancel()

        timeoutJob = coroutineScope.launch {
            _remainingTimeMillis.value = timeoutMillis

            val tickInterval = 1000L // Update every second
            var elapsedTime = 0L

            while (elapsedTime < timeoutMillis) {
                delay(tickInterval)
                elapsedTime += tickInterval
                _remainingTimeMillis.value = timeoutMillis - elapsedTime
            }

            // Timeout reached
            _isActive.value = false
            onTimeout()
        }
    }

    /**
     * Get remaining time in seconds
     */
    fun getRemainingTimeSeconds(): Int {
        return (_remainingTimeMillis.value / 1000).toInt()
    }
}