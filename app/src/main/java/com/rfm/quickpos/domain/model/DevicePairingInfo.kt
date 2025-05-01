// app/src/main/java/com/rfm/quickpos/domain/model/DevicePairingInfo.kt

package com.rfm.quickpos.domain.model

data class DevicePairingInfo(
    val deviceSerial: String = "",
    val merchantId: String = "",  // MID
    val terminalId: String = "",  // TID
    val deviceName: String = ""
)

enum class PairingStatus {
    INITIAL,    // First time setup
    PAIRING,    // Attempting to pair
    SUCCESS,    // Successfully paired
    ERROR       // Error during pairing
}