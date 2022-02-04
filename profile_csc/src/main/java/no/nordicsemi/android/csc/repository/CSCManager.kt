/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.csc.repository

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.common.callback.csc.CyclingSpeedAndCadenceMeasurementResponse
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.csc.data.CSCRepository
import no.nordicsemi.android.csc.data.WheelSize
import no.nordicsemi.android.service.BatteryManager
import java.util.*

val CSC_SERVICE_UUID: UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb")
private val CSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb")

internal class CSCManager(
    context: Context,
    private val scope: CoroutineScope,
    private val repository: CSCRepository
) : BatteryManager(context) {

    private var cscMeasurementCharacteristic: BluetoothGattCharacteristic? = null
    private var wheelSize: WheelSize = WheelSize()

    private var previousResponse: CyclingSpeedAndCadenceMeasurementResponse? = null

    private val exceptionHandler = CoroutineExceptionHandler { context, t->
        Log.e("COROUTINE-EXCEPTION", "Uncaught exception", t)
    }

    override fun onBatteryLevelChanged(batteryLevel: Int) {
        repository.setBatteryLevel(batteryLevel)
    }

    override fun getGattCallback(): BatteryManagerGattCallback {
        return CSCManagerGattCallback()
    }

    fun setWheelSize(value: WheelSize) {
        wheelSize = value
    }

    private inner class CSCManagerGattCallback : BatteryManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            setNotificationCallback(cscMeasurementCharacteristic).asValidResponseFlow<CyclingSpeedAndCadenceMeasurementResponse>()
                .onEach {
                    previousResponse?.let { previousResponse ->
                        val wheelCircumference = wheelSize.value.toFloat()
                        val totalDistance = it.getTotalDistance(wheelSize.value.toFloat())
                        val distance = it.getDistance(wheelCircumference, previousResponse)
                        val speed = it.getSpeed(wheelCircumference, previousResponse)
                        repository.setNewDistance(totalDistance, distance, speed, wheelSize)

                        val crankCadence = it.getCrankCadence(previousResponse)
                        val gearRatio = it.getGearRatio(previousResponse)
                        repository.setNewCrankCadence(crankCadence, gearRatio, wheelSize)
                    }

                    previousResponse = it
                }.launchIn(scope)

            scope.launch(exceptionHandler) {
                enableNotifications(cscMeasurementCharacteristic).suspend()
            }
        }

        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(CSC_SERVICE_UUID)
            if (service != null) {
                cscMeasurementCharacteristic = service.getCharacteristic(CSC_MEASUREMENT_CHARACTERISTIC_UUID)
            }
            return cscMeasurementCharacteristic != null
        }

        override fun onDeviceDisconnected() {
            super.onDeviceDisconnected()
            cscMeasurementCharacteristic = null
        }

        override fun onServicesInvalidated() {}
    }
}
