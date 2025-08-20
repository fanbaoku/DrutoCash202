package com.cash.ease.money.cashease.collector

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat

object ImeiCollector {
    @SuppressLint("HardwareIds", "MissingPermission")
    fun getImei(context: Context): List<String?> {
        val list: MutableList<String?> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            list.add("")
            list.add("")
            list.add("")
            list.add("")
            list.add("")
            return list
        }
        val manager = context.getSystemService(
            Context.TELEPHONY_SERVICE
        ) as TelephonyManager
        val imei1 = getImei(context, 0)
        val imei2 = getImei(context, 1)
        list.add(imei1)
        list.add(imei2)
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.READ_PHONE_NUMBERS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            list.add(manager.line1Number)
//        } else {
            list.add("")
//        }
        list.add(manager.simSerialNumber)
        list.add(manager.subscriberId)
        return list
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun getImei(context: Context, slotIndex: Int): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return manager.getImei(slotIndex)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return manager.getDeviceId(slotIndex)
            } else if (slotIndex == 0) {
                return manager.deviceId
            }
        }
        return ""
    }
}
