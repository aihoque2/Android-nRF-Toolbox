package no.nordicsemi.android.feature.profile.data

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.hts.repository.HTSBleService
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.service.DEVICE_DATA
import no.nordicsemi.android.service.NotificationService
import javax.inject.Inject

@SuppressLint("MissingPermission")
@AndroidEntryPoint
internal class ProfileService : NotificationService() {

    private lateinit var client: ClientBleGatt

    @Inject
    private lateinit var htsProfile: HTSBleService

    @Inject
    private lateinit var profileRepository: ProfileRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        profileRepository.setServiceRunning(true)

        val device = intent!!.getParcelableExtra<ServerDevice>(DEVICE_DATA)!!

        startGattClient(device)

        profileRepository.stopEvent
            .onEach { disconnect() }
            .launchIn(lifecycleScope)

        return Service.START_REDELIVER_INTENT
    }

    private fun startGattClient(device: ServerDevice) = lifecycleScope.launch {
        client = ClientBleGatt.connect(this@ProfileService, device, logger = { p, s -> profileRepository.log(p, s) })

        client.connectionStateWithStatus
            .onEach { profileRepository.onConnectionStateChanged(it) }
            .filterNotNull()
            .onEach { stopIfDisconnected(it) }
            .launchIn(lifecycleScope)

        if (!client.isConnected) {
            return@launch
        }

        try {
            val services = client.discoverServices()
            configureGatt(services)
        } catch (e: Exception) {
            profileRepository.onMissingServices()
        }
    }

    private suspend fun configureGatt(services: ClientBleGattServices) {
        htsProfile.configureGatt(services)
    }

    private fun stopIfDisconnected(connectionState: GattConnectionStateWithStatus) {
        if (connectionState.state == GattConnectionState.STATE_DISCONNECTED) {
            stopSelf()
        }
    }

    private fun disconnect() {
        client.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        profileRepository.setServiceRunning(false)
    }
}
