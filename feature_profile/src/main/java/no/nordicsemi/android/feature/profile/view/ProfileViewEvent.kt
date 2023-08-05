package no.nordicsemi.android.feature.profile.view

import no.nordicsemi.android.hts.view.HTSScreenViewEvent

sealed interface ProfileViewEvent

data object OpenLoggerViewEvent : ProfileViewEvent

data object DisconnectViewEvent : ProfileViewEvent

data object NavigateUpViewEvent : ProfileViewEvent

data class HtsProfileViewEvent(val event: HTSScreenViewEvent) : ProfileViewEvent
