package com.cash.ease.money.cashease.collector

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

object PermissionCollector {
    val TAG: String = PermissionCollector::class.java.simpleName
    private val PERMISSIONS: Array<String> = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.CAMERA
    )

    private fun getInfoMap(context: Context): Map<String, Int> {
        val map: MutableMap<String, Int> = HashMap()
        for (permission in PERMISSIONS) {
            map[permission] =
                granted(context, permission)
        }
        return map
    }

    fun getInfoList(context: Context): List<Int> {
        val map = getInfoMap(context)
        val list: MutableList<Int> = ArrayList()
        for (permission in PERMISSIONS) {
            list.add(map[permission] ?: 0)
        }
        Log.d(
            TAG,
            "permissions=$list"
        )
        return list
    }

    fun allGranted(status: List<Int>): Boolean {
        for (state in status) {
            if (state != 1) {
                return false
            }
        }
        return true
    }

    private fun granted(context: Context, permission: String): Int {
        return if (ActivityCompat.checkSelfPermission(context, permission)
            == PackageManager.PERMISSION_GRANTED
        ) 1 else 0
    }
}
