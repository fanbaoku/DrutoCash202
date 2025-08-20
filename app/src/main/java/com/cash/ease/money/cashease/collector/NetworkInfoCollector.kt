package com.cash.ease.money.cashease.collector

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.cash.ease.money.cashease.utils.NetworkUtil.ipv4
import com.cash.ease.money.cashease.utils.NetworkUtil.macAddress

object NetworkInfoCollector {
    fun getNetworkInfoList(context: Context): List<Any?> {
        val lists: MutableList<Any?> = ArrayList()
        lists.add(getNetworkType(context))
        lists.add(ipv4)
        lists.add(macAddress)
        val wifiInfo = getCurrentWifiInfo(context)
        lists.add(formatWifiInfo(wifiInfo))
        lists.add(ArrayList<Any>())
        return lists
    }

    private fun getNetworkType(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                ?: return "NONE"

        val activeNetwork = connectivityManager.activeNetworkInfo
        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting) {
            return "NONE"
        }

        if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
            return "WIFI"
        } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return "OTHER"
            }
            val networkType = telephonyManager.networkType
            return when (networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> "2G"

                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                20 ->                     // report as 20
                    "5G"

                else -> "OTHER"
            }
        } else {
            return "OTHER"
        }
    }

    private fun formatWifiInfo(wifiInfo: WifiInfo?): List<Any?> {
        val list: MutableList<Any?> = ArrayList()
        if (wifiInfo == null) {
            return list
        }
        list.add(wifiInfo.ssid)
        list.add(wifiInfo.bssid)
        return list
    }

    private fun getCurrentWifiInfo(context: Context): WifiInfo? {
        val wifiManager = context.applicationContext.getSystemService(
            Context.WIFI_SERVICE
        ) as WifiManager?
        if (wifiManager != null) {
            return wifiManager.connectionInfo
        }
        return null // 没有连接 WiFi 或获取失败
    }
}
