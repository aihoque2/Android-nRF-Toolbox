package no.nordicsemi.dfu.data

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DFURepository @Inject constructor(
    private val deviceHolder: SelectedBluetoothDeviceHolder,
    private val fileManger: DFUFileManager
) {

    private val _data = MutableStateFlow<DFUData>(NoFileSelectedState())
    val data: StateFlow<DFUData> = _data.asStateFlow()

    fun setZipFile(file: Uri) {
        val currentState = _data.value as NoFileSelectedState
        _data.value = fileManger.createFile(file)?.let {
            FileReadyState(ZipFile(it), requireNotNull(deviceHolder.device))
        } ?: currentState.copy(isError = true)
    }

    fun setHexFile(file: Uri) {
        val currentState = _data.value as NoFileSelectedState
        _data.value = fileManger.createFile(file)?.let {
            HexFileLoadedState(PartialHexFile(it, DFUFileType.TYPE_APPLICATION))
        } ?: currentState.copy(isError = true)
    }

    fun setDatFile(file: Uri) {
        val currentState = _data.value as HexFileLoadedState
        _data.value = fileManger.createFile(file)?.let {
            FileReadyState(FullHexFile(it, currentState.file.data, DFUFileType.TYPE_APPLICATION), requireNotNull(deviceHolder.device))
        } ?: currentState.copy(isDatFileError = true)
    }

    fun setSuccess() {
        _data.value = UploadSuccessState
    }

    fun setError(message: String?) {
        _data.value = UploadFailureState(message)
    }

    fun install() {
        _data.value = FileInstallingState()
    }

    fun clear() {
        _data.value = NoFileSelectedState()
    }
}
