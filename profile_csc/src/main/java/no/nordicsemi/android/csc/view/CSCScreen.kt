package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.viewmodel.CSCViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar

@Composable
fun CSCScreen() {
    val viewModel: CSCViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column {
        BackIconAppBar(stringResource(id = R.string.csc_title)) {
            viewModel.onEvent(OnDisconnectButtonClick)
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
//            when (state) {
//                is DisplayDataState -> CSCContentView(state.data) { viewModel.onEvent(it) }
//                LoadingState -> DeviceConnectingView()
//            }.exhaustive
        }
    }
}
