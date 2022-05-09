package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.nordicsemi.android.theme.view.SectionTitle
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.UARTRecord
import no.nordicsemi.android.uart.data.UARTRecordType
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun OutputSection(records: List<UARTRecord>, onEvent: (UARTViewEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle(
                resId = R.drawable.ic_output,
                title = stringResource(R.string.uart_output),
                modifier = Modifier,
                menu = { Menu(onEvent) }
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        val scrollState = rememberLazyListState()
        val scrollDown = remember {
            derivedStateOf { scrollState.isScrolledToTheEnd() }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = scrollState
        ) {
            if (records.isEmpty()) {
                item { Text(text = stringResource(id = R.string.uart_output_placeholder)) }
            } else {
                records.forEach {
                    item {
                        MessageItem(record = it)

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        LaunchedEffect(records, scrollDown.value) {
            if (!scrollDown.value || records.isEmpty()) {
                return@LaunchedEffect
            }
            launch {
                scrollState.scrollToItem(records.lastIndex)
            }
        }
    }
}

fun LazyListState.isScrolledToTheEnd() = layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1

@Composable
private fun MessageItem(record: UARTRecord) {
    Column {
        Text(
            text = record.timeToString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = createRecordText(record = record),
            style = MaterialTheme.typography.bodyMedium,
            color = createRecordColor(record = record)
        )
    }
}

@Composable
private fun Menu(onEvent: (UARTViewEvent) -> Unit) {
    Row {
        IconButton(onClick = { onEvent(ClearOutputItems) }) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.uart_clear_items),
            )
        }
    }
}

@Composable
private fun createRecordText(record: UARTRecord): String {
    return when (record.type) {
        UARTRecordType.INPUT -> stringResource(id = R.string.uart_input_log, record.text)
        UARTRecordType.OUTPUT -> stringResource(id = R.string.uart_output_log, record.text)
    }
}

@Composable
private fun createRecordColor(record: UARTRecord): Color {
    return when (record.type) {
        UARTRecordType.INPUT -> colorResource(id = R.color.nordicGrass)
        UARTRecordType.OUTPUT -> MaterialTheme.colorScheme.onBackground
    }
}

private val datFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.ENGLISH)

private fun UARTRecord.timeToString(): String {
    return datFormatter.format(timestamp)
}
