package com.druto.loan.cash.drutocash.collector

import android.content.Context

object ScreenInfoCollector {

    fun getInfoList(context: Context): List<Any> {
        val list: MutableList<Any> = ArrayList()
        val metrics = context.resources.displayMetrics
        list.add(metrics.widthPixels)
        list.add(metrics.heightPixels)
        list.add(metrics.xdpi.toString())
        list.add(metrics.ydpi.toString())
        list.add(metrics.densityDpi)
        list.add(metrics.density.toString())
        list.add(metrics.scaledDensity.toString())
        return list
    }
}
