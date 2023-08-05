package no.nordicsemi.android.feature.profile.data

import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus

data class ProfileServiceData(
    val data: ProfileData = ProfileData(),
    val batteryLevel: Int? = null,
    val deviceName: String? = null,
    val missingServices: Boolean = false,
    val connectionState: GattConnectionStateWithStatus? = null,
) {

    val disconnectStatus = if (missingServices) {
        BleGattConnectionStatus.NOT_SUPPORTED
    } else {
        connectionState?.status ?: BleGattConnectionStatus.UNKNOWN
    }
}
