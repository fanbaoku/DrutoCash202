package com.druto.loan.cash.drutocash.collector

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log

class BatteryInfo {
    var plugged: Int = 0
    var level: Int = 0
    var usbPlugged: Int = 0
    var acPlugged: Int = 0
    var current: Int = 0
    var capacity: Int = 0

    override fun toString(): String {
        return "BatteryInfo{" +
                "plugged=" + plugged +
                ", level=" + level +
                ", usbPlugged=" + usbPlugged +
                ", acPlugged=" + acPlugged +
                ", current=" + current +
                ", capacity=" + capacity +
                '}'
    }

    companion object {
        val TAG: String = BatteryInfo::class.java.simpleName

        //电池容量
        @SuppressLint("PrivateApi")
        fun getBatteryCapacity(context: Context?): Int {
            val powerProfile: Any
            var batteryCapacity = 0.0
            val powerProfileClass = "com.android.internal.os.PowerProfile"
            try {
                powerProfile = Class.forName(powerProfileClass)
                    .getConstructor(Context::class.java)
                    .newInstance(context)

                batteryCapacity = Class.forName(powerProfileClass)
                    .getMethod("getBatteryCapacity")
                    .invoke(powerProfile) as Double
            } catch (e: Exception) {
                Log.w(TAG, e)
            }

            return batteryCapacity.toInt()
        }
    }
}
