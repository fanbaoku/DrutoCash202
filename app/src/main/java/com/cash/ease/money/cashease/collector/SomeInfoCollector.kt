package com.cash.ease.money.cashease.collector

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import com.cash.ease.money.cashease.utils.CashEaseHelper
import com.cash.ease.money.cashease.utils.NetworkUtil.getBluetoothAddress

object SomeInfoCollector {
    val TAG: String = SomeInfoCollector::class.java.simpleName

    fun getSomeInfoList(context: Context): List<Any?> {
        val list: MutableList<Any?> = ArrayList()
        val runtime = Runtime.getRuntime()
        list.add(runtime.totalMemory())
        list.add(runtime.maxMemory())
        list.add(runtime.freeMemory())
        val gpsEnabled = isGpsEnabled(context)
        val hasCalendar = CashEaseHelper.isPackageExisted("com.android.providers.calendar")
        var cameraCount: Int
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val bluetoothMac = getBluetoothAddress(adapter)
        val cameraManager = context.getSystemService(Activity.CAMERA_SERVICE) as CameraManager?
        try {
            val cameraIdList = cameraManager?.cameraIdList
            cameraCount = cameraIdList?.size ?: 0
        } catch (e: CameraAccessException) {
            cameraCount = 0
            Log.w(TAG, e)
        }
        list.add(if (gpsEnabled) 1 else 0)
        list.add(if (hasCalendar) 1 else 0)
        list.add(bluetoothMac ?: "")
        list.add(cameraCount)
        return list
    }

    fun isGpsEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager != null) {
            // 检查 GPS 是否开启
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
        return false // 如果获取 LocationManager 失败，返回 false
    }

    fun openGPSSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
}
