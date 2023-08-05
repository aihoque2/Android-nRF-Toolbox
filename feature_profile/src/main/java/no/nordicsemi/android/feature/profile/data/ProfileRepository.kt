package no.nordicsemi.android.feature.profile.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.common.core.simpleSharedFlow
import no.nordicsemi.android.common.logger.BleLoggerAndLauncher
import no.nordicsemi.android.common.logger.DefaultBleLogger
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.service.DisconnectAndStopEvent
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.ui.view.StringConst
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
    private val stringConst: StringConst
) {
    private var logger: BleLoggerAndLauncher? = null

    private val _data = MutableStateFlow(ProfileServiceData())
    internal val data = _data.asStateFlow()

    private val _stopEvent = simpleSharedFlow<DisconnectAndStopEvent>()
    internal val stopEvent = _stopEvent.asSharedFlow()

    val isRunning = data.map { it.connectionState?.state == GattConnectionState.STATE_CONNECTED }

    private var isOnScreen = false
    private var isServiceRunning = false

    fun setOnScreen(isOnScreen: Boolean) {
        this.isOnScreen = isOnScreen

        if (shouldClean()) clean()
    }

    fun setServiceRunning(serviceRunning: Boolean) {
        this.isServiceRunning = serviceRunning

        if (shouldClean()) clean()
    }

    private fun shouldClean() = !isOnScreen && !isServiceRunning

    fun launch(device: ServerDevice) {
        logger = DefaultBleLogger.create(context, stringConst.APP_NAME, "HRS", device.address)
        _data.value = _data.value.copy(deviceName = device.name)
        serviceManager.startService(ProfileService::class.java, device)
    }

    fun onConnectionStateChanged(connectionState: GattConnectionStateWithStatus?) {
        _data.value = _data.value.copy(connectionState = connectionState)
    }

    fun onBatteryLevelChanged(batteryLevel: Int) {
        _data.value = _data.value.copy(batteryLevel = batteryLevel)
    }

    fun onMissingServices() {
        _data.value = _data.value.copy(missingServices = true)
        _stopEvent.tryEmit(DisconnectAndStopEvent())
    }

    fun openLogger() {
        logger?.launch()
    }

    fun log(priority: Int, message: String) {
        logger?.log(priority, message)
    }

    fun disconnect() {
        _stopEvent.tryEmit(DisconnectAndStopEvent())
    }

    private fun clean() {
        logger = null
        _data.value = ProfileServiceData()
    }
}
