package com.druto.loan.cash.drutocash.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object ThreadUtil {
    private val SDF_THREAD_LOCAL: ThreadLocal<Map<String, SimpleDateFormat>> =
        object : ThreadLocal<Map<String, SimpleDateFormat>>() {
            override fun initialValue(): Map<String, SimpleDateFormat> {
                return HashMap()
            }
        }

    fun getSimpleDateFormat(pattern: String): SimpleDateFormat {
        val map = SDF_THREAD_LOCAL.get()
        var sdf = map!![pattern]
        if (sdf == null) {
            sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault()
        }
        return sdf
    }
}
