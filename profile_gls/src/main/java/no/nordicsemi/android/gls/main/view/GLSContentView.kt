package no.nordicsemi.android.gls.main.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.gls.R
import no.nordicsemi.android.gls.data.GLSData
import no.nordicsemi.android.gls.data.GLSRecord
import no.nordicsemi.android.gls.data.RequestStatus
import no.nordicsemi.android.gls.data.WorkingMode
import no.nordicsemi.android.gls.main.viewmodel.GLSViewModel
import androidx.compose.material3.CircularProgressIndicator
import no.nordicsemi.android.theme.view.BatteryLevelView
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.android.theme.view.SectionTitle

@Composable
internal fun GLSContentView(state: GLSData, onEvent: (GLSScreenViewEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SettingsView(state, onEvent)

        Spacer(modifier = Modifier.height(16.dp))

        RecordsView(state)

        Spacer(modifier = Modifier.height(16.dp))

        BatteryLevelView(state.batteryLevel)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onEvent(DisconnectEvent) }
        ) {
            Text(text = stringResource(id = R.string.disconnect))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsView(state: GLSData, onEvent: (GLSScreenViewEvent) -> Unit) {
    ScreenSection {
        SectionTitle(icon = Icons.Default.Settings, title = "Request items")

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (state.requestStatus == RequestStatus.PENDING) {
                CircularProgressIndicator()
            } else {
                WorkingMode.values().forEach {
                    Button(onClick = { onEvent(OnWorkingModeSelected(it)) }) {
                        Text(it.toDisplayString())
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsView(state: GLSData) {
    ScreenSection {
        if (state.records.isEmpty()) {
            RecordsViewWithoutData()
        } else {
            RecordsViewWithData(state)
        }

    }
}

@Composable
private fun RecordsViewWithData(state: GLSData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(resId = R.drawable.ic_records, title = "Records")

        Spacer(modifier = Modifier.height(16.dp))

        state.records.forEachIndexed { i, it ->
            RecordItem(it)

            if (i < state.records.size - 1) {
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }
}

@Composable
private fun RecordItem(record: GLSRecord) {
    val viewModel: GLSViewModel = hiltViewModel()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { viewModel.onEvent(OnGLSRecordClick(record)) }
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            record.time?.let {
                Text(
                    text = stringResource(R.string.gls_timestamp, it),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = record.type.toDisplayString(),
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = glucoseConcentrationDisplayValue(
                        record.glucoseConcentration,
                        record.unit
                    ),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun RecordsViewWithoutData() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionTitle(icon = Icons.Default.Search, title = "No items")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.gls_no_records_info),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
