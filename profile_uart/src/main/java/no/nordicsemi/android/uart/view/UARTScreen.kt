package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.service.*
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.LoggerIconAppBar
import no.nordicsemi.ui.scanner.ui.DeviceConnectingView
import no.nordicsemi.ui.scanner.ui.NoDeviceView
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.viewmodel.UARTViewModel
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.ui.scanner.ui.DeviceDisconnectedView
import no.nordicsemi.ui.scanner.ui.Reason

@Composable
fun UARTScreen() {
    val viewModel: UARTViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    if (state.showEditDialog) {
        UARTAddMacroDialog(state.selectedMacro) { viewModel.onEvent(it) }
    }

    Column {
        val navigateUp = { viewModel.onEvent(NavigateUp) }

        LoggerIconAppBar(stringResource(id = R.string.uart_title), navigateUp) {
            viewModel.onEvent(OpenLogger)
        }

        Column(modifier = Modifier) {
            when (state.uartManagerState) {
                NoDeviceState -> NoDeviceView()
                is WorkingState -> when (state.uartManagerState.result) {
                    is ConnectingResult -> Scroll { DeviceConnectingView { viewModel.onEvent(DisconnectEvent) } }
                    is DisconnectedResult -> Scroll { DeviceDisconnectedView(Reason.USER, navigateUp) }
                    is LinkLossResult -> Scroll { DeviceDisconnectedView(Reason.LINK_LOSS, navigateUp) }
                    is MissingServiceResult -> Scroll { DeviceDisconnectedView(Reason.MISSING_SERVICE, navigateUp) }
                    is UnknownErrorResult -> Scroll { DeviceDisconnectedView(Reason.UNKNOWN, navigateUp) }
                    is SuccessResult -> UARTContentView(state.uartManagerState.result.data, state) { viewModel.onEvent(it) }
                }
            }.exhaustive
        }
    }
}

@Composable
fun Scroll(content: @Composable () -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        content()
    }
}
