package no.nordicsemi.android.feature.profile.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.feature.profile.viewmodel.ProfileViewModel
import no.nordicsemi.android.hts.R
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.ui.scanner.view.DeviceConnectingView
import no.nordicsemi.android.kotlin.ble.ui.scanner.view.DeviceDisconnectedView
import no.nordicsemi.android.ui.view.NavigateUpButton
import no.nordicsemi.android.ui.view.ProfileAppBar

@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state = viewModel.state.collectAsStateWithLifecycle().value

    val navigateUp = { viewModel.onEvent(NavigateUpViewEvent) }

    Scaffold(
        topBar = {
            ProfileAppBar(
                deviceName = state.deviceName,
                connectionState = state.connectionState,
                title = R.string.hts_title,
                navigateUp = navigateUp,
                disconnect = { viewModel.onEvent(DisconnectViewEvent) },
                openLogger = { viewModel.onEvent(OpenLoggerViewEvent) }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (state.connectionState?.state) {
                null,
                GattConnectionState.STATE_CONNECTING -> DeviceConnectingView { NavigateUpButton(navigateUp) }
                GattConnectionState.STATE_DISCONNECTED,
                GattConnectionState.STATE_DISCONNECTING -> DeviceDisconnectedView(state.disconnectStatus) {
                    NavigateUpButton(navigateUp)
                }
                GattConnectionState.STATE_CONNECTED -> ProfileContentView(state) { viewModel.onEvent(it) }
            }
        }
    }
}