package com.druto.loan.cash.drutocash.collector

import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

object KeyAppsCollector {
    val TAG: String = KeyAppsCollector::class.java.simpleName
    private val APPS: Array<String> = arrayOf(
        "com.google.android.gms",  // 通过google接口判断，无需在manifest配置
        "com.facebook.katana",
        "com.whatsapp",
        "com.tencent.mm",
        "com.eg.android.AlipayGphone",
        "com.android.vending"
    )

    private fun getInfoMap(context: Context): Map<String, String?> {
        val map: MutableMap<String, String?> = HashMap()
        val pm = context.packageManager
        val length = APPS.size
        map[APPS[0]] = getGoogleServiceVersionName(context)
        for (i in 1 until length) {
            map[APPS[i]] =
                getPackageVersion(pm, APPS[i])
        }
        return map
    }

    fun getInfoList(context: Context): List<String> {
        val map = getInfoMap(context)
        val list: MutableList<String> = ArrayList()
        for (packageName in APPS) {
            list.add(map[packageName] ?: "")
        }
        return list
    }

    private fun getPackageVersion(pm: PackageManager, packageName: String): String? {
        try {
            val packageInfo = pm.getPackageInfo(packageName, 0)
            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            // ignore
        }
        return ""
    }

    private fun checkGooglePlayServices(context: Context): Boolean {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            context
        )
        return status == ConnectionResult.SUCCESS
    }

    private fun getGoogleServiceVersionName(context: Context): String? {
        if (checkGooglePlayServices(context)) {
            val versionName = try {
                context.packageManager.getPackageInfo(
                    GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0
                ).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            return versionName
        } else {
            return null
        }
    }
}
