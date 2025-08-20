package com.cash.ease.money.cashease.collector

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import java.io.IOException
import java.util.Locale

object LocationCollector {
    val TAG: String = LocationCollector::class.java.simpleName

    fun getLocationInfoList(context: Context, location: Location?): List<Any?> {
        val list: MutableList<Any?> = ArrayList()
        if (location == null) {
            return list
        }
        list.add(location.latitude)
        list.add(location.longitude)
        list.add(location.accuracy)
        list.add(location.time)

        val gc = Geocoder(context, Locale.getDefault())
        var locationList: List<Address?>? = null
        try {
            locationList = gc.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: IOException) {
            Log.e(TAG, "locationList: error " + e.message, e)
        }
        var addressDetail = ""
        val addressDetail2: MutableList<Any?> = ArrayList()
        if (!locationList.isNullOrEmpty()) {
            Log.d(TAG, "locationList: " + locationList.size)
            val address = locationList[0] //得到Address实例
            if (address != null) {
                addressDetail2.add(address.countryName) //国家
                addressDetail2.add(address.countryCode) //国家Code
                addressDetail2.add(address.adminArea) //省
                addressDetail2.add(address.locality) //市
                addressDetail2.add(address.subAdminArea) //区
                addressDetail2.add(address.featureName) //街道
                val list2: MutableList<Any?> = ArrayList()
                var i = 0
                while (address.getAddressLine(i) != null) {
                    list2.add(address.getAddressLine(i)) //地址数组
                    i++
                }
                addressDetail2.add(list2)
                addressDetail = address.getAddressLine(0).toString() //地址详细信息
            }
        }
        list.add(addressDetail)
        list.add(addressDetail2)
        return list
    }
}
