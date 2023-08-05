package no.nordicsemi.android.feature.profile.view

import androidx.compose.runtime.Composable
import no.nordicsemi.android.feature.profile.data.ProfileServiceData
import no.nordicsemi.android.hts.view.HTSContentView

@Composable
fun ProfileContentView(state: ProfileServiceData, onEvent: (ProfileViewEvent) -> Unit) {

    state.data.htsData?.let {
        HTSContentView(state = it) { onEvent(HtsProfileViewEvent(it)) }
    }
}
