package com.druto.loan.cash.drutocash.collector

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper.androidId
import java.io.File
import java.io.FileFilter
import java.util.regex.Pattern

object DeviceBaseInfoCollector {
    val TAG: String = DeviceBaseInfoCollector::class.java.simpleName

    fun getInfoList(context: Context): List<Any?> {
        val list: MutableList<Any?> = ArrayList()
        list.add(Build.BRAND)
        list.add(Build.MODEL)
        list.add(Build.PRODUCT)
        list.add(Build.VERSION.SDK_INT)
        list.add(Build.VERSION.RELEASE)
        list.add(Build.SERIAL)
        list.add(androidId)
        val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        list.add(manager.phoneType)
        list.add(Build.MANUFACTURER)
        list.add(Build.FINGERPRINT)
        list.add(cpuCores)
        list.add(Build.TIME)
        list.add(Build.BOARD)
        return list
    }

    private val cpuCores: Int
        //获取CPU核心数量
        get() {
            // Private Class to display only CPU devices in the directory listing
            class CpuFilter : FileFilter {
                override fun accept(pathname: File): Boolean {
                    // Check if filename is "cpu", followed by a single digit number
                    return Pattern.matches("cpu[0-9]", pathname.name)
                }
            }
            try {
                // Get directory containing CPU info
                val dir = File("/sys/devices/system/cpu/")
                // Filter to only list the devices we care about
                val files = dir.listFiles(CpuFilter())
                // Return the number of cores (virtual CPU devices)
                return files!!.size
            } catch (e: Exception) {
                // Default to return 1 core
                return 1
            }
        }
}
