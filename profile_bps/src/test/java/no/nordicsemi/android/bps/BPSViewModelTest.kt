package no.nordicsemi.android.bps

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import no.nordicsemi.android.bps.repository.BPSManager
import no.nordicsemi.android.bps.view.DisconnectEvent
import no.nordicsemi.android.bps.viewmodel.BPSViewModel
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.service.BleManagerStatus
import org.junit.After
import org.junit.Before
import org.junit.Test

class BPSViewModelTest {

    val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `check if navigation up called after disconnect event returns success`() {
        val repository = BPSRepository()
        val manager = mockk<BPSManager>()
        val navigationManager = mockk<NavigationManager>()

        every { navigationManager.recentResult } returns MutableSharedFlow(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        justRun { navigationManager.navigateTo(any(), any()) }
        every { manager.isConnected } returns true
        justRun { manager.setConnectionObserver(any()) }
        justRun { manager.disconnect().enqueue() }

        val viewModel = BPSViewModel(manager, repository, navigationManager)

        viewModel.onEvent(DisconnectEvent)

        //Invoke by manager
        repository.setNewStatus(BleManagerStatus.DISCONNECTED)

        verify { navigationManager.navigateUp() }
    }

    @Test
    fun `check if navigation up called after disconnect if manager not connected event returns success`() {
        val repository = BPSRepository()
        val manager = mockk<BPSManager>()
        val navigationManager = mockk<NavigationManager>()

        every { navigationManager.recentResult } returns MutableSharedFlow(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        justRun { navigationManager.navigateTo(any(), any()) }
        every { manager.isConnected } returns false
        justRun { manager.setConnectionObserver(any()) }

        val viewModel = BPSViewModel(manager, repository, navigationManager)

        viewModel.onEvent(DisconnectEvent)

        verify { navigationManager.navigateUp() }
    }
}
