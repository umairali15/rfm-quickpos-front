// app/src/main/java/com/rfm/quickpos/presentation/features/setup/DevicePairingState.kt

package com.rfm.quickpos.presentation.features.setup

import com.rfm.quickpos.domain.model.DevicePairingInfo
import com.rfm.quickpos.domain.model.PairingStatus

data class DevicePairingState(
    val pairingInfo: DevicePairingInfo = DevicePairingInfo(),
    val status: PairingStatus = PairingStatus.INITIAL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPaired: Boolean = false,
    val serializableSerialNumber: Boolean = false
)