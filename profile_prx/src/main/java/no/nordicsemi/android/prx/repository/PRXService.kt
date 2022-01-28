package no.nordicsemi.android.prx.repository

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.prx.data.AlarmLevel
import no.nordicsemi.android.prx.data.DisableAlarm
import no.nordicsemi.android.prx.data.Disconnect
import no.nordicsemi.android.prx.data.EnableAlarm
import no.nordicsemi.android.prx.data.PRXRepository
import no.nordicsemi.android.service.ForegroundBleService
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
internal class PRXService : ForegroundBleService() {

    @Inject
    lateinit var repository: PRXRepository

    @Inject
    lateinit var alarmHandler: AlarmHandler

    private var serverManager: ProximityServerManager = ProximityServerManager(this)

    override val manager: PRXManager by lazy {
        PRXManager(this, repository).apply {
            useServer(serverManager)
        }
    }

    override fun onCreate() {
        super.onCreate()

        serverManager.open()

        status.onEach {
            repository.setNewStatus(it)
        }.launchIn(scope)

        repository.command.onEach {
            when (it) {
                DisableAlarm -> manager.writeImmediateAlert(false)
                EnableAlarm -> manager.writeImmediateAlert(true)
                Disconnect -> stopSelf()
            }.exhaustive
        }.launchIn(scope)

        repository.data.onEach {
            if (it.localAlarmLevel != AlarmLevel.NONE) {
                alarmHandler.playAlarm()
            } else {
                alarmHandler.pauseAlarm()
            }
        }.launchIn(scope)
    }

    override fun onDestroy() {
        super.onDestroy()
        alarmHandler.releaseAlarm()
        serverManager.close()
    }
}