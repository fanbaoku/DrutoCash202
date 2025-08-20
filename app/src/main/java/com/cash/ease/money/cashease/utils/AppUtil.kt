package com.cash.ease.money.cashease.utils

import android.content.Context
import android.os.Build

object AppUtil {
    val TAG: String = AppUtil::class.java.simpleName

    fun getApps(context: Context): List<Any?> {
        var valueOf: Any?
        var longVersionCode: Long
        val packageManager = context.packageManager
        val appList = packageManager.getInstalledPackages(0)
        val appParams: MutableList<Any?> = ArrayList()
        for (it in appList) {
            val applicationInfo = it.applicationInfo ?: continue
            if ((applicationInfo.flags and 1) != 0) {
                continue
            }
            val objArr = arrayOfNulls<Any>(8)
            objArr[0] = packageManager.getApplicationLabel(applicationInfo).toString()
            objArr[1] = it.packageName
            objArr[2] = it.firstInstallTime
            objArr[3] = it.lastUpdateTime
            objArr[4] = it.versionName
            if (Build.VERSION.SDK_INT >= 28) {
                longVersionCode = it.longVersionCode
                valueOf = longVersionCode
            } else {
                valueOf = it.versionCode
            }
            objArr[5] = valueOf
            objArr[6] = it.applicationInfo!!.flags and 1
            objArr[7] = it.applicationInfo!!.flags
            appParams.add(listOf(*objArr))
        }
        return appParams
    }
}
