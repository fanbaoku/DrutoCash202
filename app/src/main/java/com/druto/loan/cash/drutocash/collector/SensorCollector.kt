package com.druto.loan.cash.drutocash.collector

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper

object SensorCollector {
    const val TAG: String = "COLLECTOR-SENSOR"

    val sensorInfoList: List<Any?>
        get() {
            val sensors = allSensors
            val lists: MutableList<Any?> =
                ArrayList()
            for (sensor in sensors) {
                val list: MutableList<Any?> =
                    ArrayList()
                list.add(sensor.type)
                list.add(sensor.name)
                list.add(sensor.version)
                list.add(sensor.vendor)
                list.add(sensor.maximumRange.toString())
                list.add(sensor.minDelay)
                list.add(sensor.power.toString())
                list.add(sensor.resolution.toString())
                lists.add(list)
            }
            return lists
        }

    private val allSensors: List<Sensor>
        get() {
            val sensorManager =
                GeneralInfoHelper.context.getSystemService(
                    Context.SENSOR_SERVICE
                ) as SensorManager
            return sensorManager.getSensorList(Sensor.TYPE_ALL)
        }
}
