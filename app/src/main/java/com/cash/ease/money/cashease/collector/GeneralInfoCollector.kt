package com.cash.ease.money.cashease.collector

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import android.text.TextUtils
import com.cash.ease.money.cashease.utils.CashEaseHelper
import com.cash.ease.money.cashease.utils.NetworkUtil.systemProxy
import java.io.File

object GeneralInfoCollector {

    fun getInfoList(context: Context): List<Any?> {
        val list: MutableList<Any?> = ArrayList()
        list.add(if (isSuEnable) 1 else 0)
        list.add(if (CashEaseHelper.isPhone) 1 else 0)
        list.add(getKeyboardType(context))
        list.add(if (TextUtils.isEmpty(systemProxy[0])) 0 else 1)
        list.add(if (TextUtils.isEmpty(systemProxy[0])) 0 else 1)
        list.add(if (isDebug(context)) 1 else 0)
        list.add(System.currentTimeMillis() - SystemClock.elapsedRealtimeNanos() / 1000000)
        list.add(if (isMockLocation(context)) 1 else 0)
        list.add(SystemClock.elapsedRealtime())
        list.add(SystemClock.uptimeMillis())
        return list
    }

    private val isSuEnable: Boolean
        get() {
            var file: File?
            val paths =
                arrayOf(
                    "/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/",
                    "/su/bin/"
                )
            try {
                for (path in paths) {
                    file = File(path + "su")
                    if (file.exists() && file.canExecute()) {
                        return true
                    }
                }
            } catch (x: Exception) {
                x.printStackTrace()
                return false
            }
            return false
        }

    private fun isDebug(context: Context): Boolean {
        val resolver = context.contentResolver
        return (Settings.Secure.getInt(resolver, Settings.Secure.ADB_ENABLED, 0) > 0)
    }

    private fun isMockLocation(context: Context): Boolean {
        val resolver = context.contentResolver
        return Settings.Secure.getInt(resolver, Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0
    }

    private fun getKeyboardType(context: Context): Int {
        val config = context.resources.configuration
        return config.keyboard
    }
}
