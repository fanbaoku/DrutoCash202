package com.druto.loan.cash.drutocash.utils

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.os.Build
import android.util.Log
import com.druto.loan.cash.drutocash.BuildConfig
import com.su.component.annotation.HiddenReflection
import java.lang.reflect.InvocationTargetException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException

/**
 * Created by su on 14-6-3.
 */
object NetworkUtil {
    private val TAG: String = NetworkUtil::class.java.simpleName

    val macAddress: String
        get() {
            try {
                val networkInterfaces =
                    NetworkInterface.getNetworkInterfaces()
                while (networkInterfaces.hasMoreElements()) {
                    val networkInterface =
                        networkInterfaces.nextElement()
                    if ("wlan0" == networkInterface.name) {
                        val hardwareAddress = networkInterface.hardwareAddress
                        if (hardwareAddress == null || hardwareAddress.size == 0) {
                            continue
                        }
                        val buf = StringBuilder()
                        for (b in hardwareAddress) {
                            buf.append(String.format("%02X:", b))
                        }
                        if (buf.length > 0) {
                            buf.deleteCharAt(buf.length - 1)
                        }
                        return buf.toString()
                    }
                }
            } catch (e: SocketException) {
                Log.w(TAG, e)
            }
            return ""
        }

    @SuppressLint("HardwareIds")
    fun getBluetoothAddress(adapter: BluetoothAdapter?): String? {
        if (adapter == null) {
            return null
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return adapter.address
        }
        val context = GeneralInfoHelper.context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasPermission =
                CashEaseHelper.hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            if (!hasPermission) {
                return null
            }
        }
        val hasPermission = CashEaseHelper.hasPermission(context, Manifest.permission.BLUETOOTH)
        if (!hasPermission) {
            return null
        }
        val btAdapterClass: Class<out BluetoothAdapter?> = adapter.javaClass
        try {
            @HiddenReflection @SuppressLint("PrivateApi") val mServiceField =
                btAdapterClass.getDeclaredField("mService")
            mServiceField.isAccessible = true
            val mService = mServiceField[adapter]
            return if (mService != null) {
                mService.javaClass.getMethod("getAddress").invoke(mService) as String
            } else {
                adapter.address
            }
        } catch (e: NoSuchFieldException) {
            Log.w(TAG, e)
            return adapter.address
        } catch (e: IllegalAccessException) {
            Log.w(TAG, e)
            return adapter.address
        } catch (e: InvocationTargetException) {
            Log.w(TAG, e)
            return adapter.address
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, e)
            return adapter.address
        }
    }

    val systemProxy: Array<String?>
        /**
         * 获取系统代理信息
         */
        get() {
            val proxyAddress = System.getProperty("http.proxyHost")
            val portStr = System.getProperty("http.proxyPort")
            val proxyPort = portStr ?: "-1"
            return arrayOf(proxyAddress, proxyPort)
        }

    val ipv4: String?
        get() {
            try {
                val interfaces =
                    NetworkInterface.getNetworkInterfaces() ?: return null
                while (interfaces.hasMoreElements()) {
                    val intf = interfaces.nextElement()
                    val enumIpAddr =
                        intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                            val hostAddress = inetAddress.getHostAddress()
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "ipv4Address: $hostAddress")
                            }
                            return hostAddress
                        }
                    }
                }
            } catch (ex: SocketException) {
                Log.e(TAG, ex.toString())
            }
            return null
        }
}
