package no.nordicsemi.android.feature.profile.viewmodel

import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.NavigationResult
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.feature.profile.data.ProfileRepository
import no.nordicsemi.android.feature.profile.data.ProfileServiceData
import no.nordicsemi.android.feature.profile.view.DisconnectViewEvent
import no.nordicsemi.android.feature.profile.view.HtsProfileViewEvent
import no.nordicsemi.android.feature.profile.view.NavigateUpViewEvent
import no.nordicsemi.android.feature.profile.view.OpenLoggerViewEvent
import no.nordicsemi.android.feature.profile.view.ProfileViewEvent
import no.nordicsemi.android.hts.viewmodel.HTSRepository
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val htsRepository: HTSRepository,
    private val navigationManager: Navigator,
): ViewModel() {

    private val _state = MutableStateFlow(ProfileServiceData())
    val state = _state.asStateFlow()

    private var isOnScreen = false
    private var isServiceRunning = false

    init {
        isOnScreen = true

        viewModelScope.launch {
            if (profileRepository.isRunning.firstOrNull() == false) {
                requestBluetoothDevice()
            }
        }

        htsRepository.data.onEach {
            val newData = _state.value.data.copy(htsData = it)
            _state.value = _state.value.copy(data = newData)
        }.launchIn(viewModelScope)
    }

    private fun requestBluetoothDevice() {
        navigationManager.navigateTo(ScannerDestinationId, ParcelUuid(CSC_SERVICE_UUID))

        navigationManager.resultFrom(ScannerDestinationId)
            .onEach { handleResult(it) }
            .launchIn(viewModelScope)
    }

    private fun handleResult(result: NavigationResult<ServerDevice>) {
        when (result) {
            is NavigationResult.Cancelled -> navigationManager.navigateUp()
            is NavigationResult.Success -> onDeviceSelected(result.value)
        }
    }

    private fun onDeviceSelected(device: ServerDevice) {
        profileRepository.launch(device)
    }

    fun setOnScreen(isOnScreen: Boolean) {
        this.isOnScreen = isOnScreen

        if (shouldClean()) {
//            clean()
        }
    }

    fun setServiceRunning(serviceRunning: Boolean) {
        this.isServiceRunning = serviceRunning

        if (shouldClean()) {
//            clean()
        }
    }

    private fun shouldClean() = !isOnScreen && !isServiceRunning

    fun onEvent(event: ProfileViewEvent) {
        when (event) {
            DisconnectViewEvent -> navigationManager.navigateUp()
            is HtsProfileViewEvent -> htsRepository.onEvent(event.event)
            OpenLoggerViewEvent -> profileRepository.openLogger()
            NavigateUpViewEvent -> profileRepository.disconnect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        isOnScreen = false
        //TODO clean repositories
    }
}
