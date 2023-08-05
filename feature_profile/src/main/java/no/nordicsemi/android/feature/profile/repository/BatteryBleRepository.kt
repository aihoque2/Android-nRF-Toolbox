package no.nordicsemi.android.feature.profile.repository

import android.annotation.SuppressLint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.feature.profile.data.ProfileRepository
import no.nordicsemi.android.hts.viewmodel.HTSRepository
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.profile.battery.BatteryLevelParser
import java.util.UUID
import javax.inject.Inject

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

@SuppressLint("MissingPermission")
class BatteryBleRepository @Inject constructor(
    private val repository: ProfileRepository,
    private val scope: CoroutineScope
) {

    suspend fun configureGatt(services: ClientBleGattServices) {
        val batteryService = services.findService(BATTERY_SERVICE_UUID)!!
        val batteryLevelCharacteristic = batteryService.findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)!!

        batteryLevelCharacteristic.getNotifications()
            .mapNotNull { BatteryLevelParser.parse(it) }
            .onEach { repository.onBatteryLevelChanged(it) }
            .catch { it.printStackTrace() }
            .launchIn(scope)
    }
}