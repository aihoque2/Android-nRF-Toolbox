/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.hts.viewmodel

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.common.logger.BleLoggerAndLauncher
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.hts.data.HTSServiceData
import no.nordicsemi.android.hts.view.HTSScreenViewEvent
import no.nordicsemi.android.hts.view.OnTemperatureUnitSelected
import no.nordicsemi.android.hts.view.TemperatureUnit
import no.nordicsemi.android.kotlin.ble.profile.hts.data.HTSData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HTSRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val navigationManager: Navigator,
    private val analytics: AppAnalytics,
    private val scope: CoroutineScope
) {
    private var logger: BleLoggerAndLauncher? = null

    private val _data = MutableStateFlow(HTSServiceData())
    val data = _data.asStateFlow()

    init {

//        repository.data.onEach {
//            if (it.connectionState?.state == GattConnectionState.STATE_CONNECTED) {
//                analytics.logEvent(ProfileConnectedEvent(Profile.HTS))
//            }
//        }.launchIn(scope)
    }

    fun onEvent(event: HTSScreenViewEvent) {
        when (event) {
            is OnTemperatureUnitSelected -> setTemperatureUnit(event.value)
        }
    }

    private fun setTemperatureUnit(temperatureUnit: TemperatureUnit) {
        _data.value = _data.value.copy(temperatureUnit = temperatureUnit)
    }

    fun onHTSDataChanged(data: HTSData) {
        _data.value = _data.value.copy(data = data)
    }

    fun log(priority: Int, message: String) {
        logger?.log(priority, message)
    }

    private fun disconnect() {
        navigationManager.navigateUp()
    }
}
