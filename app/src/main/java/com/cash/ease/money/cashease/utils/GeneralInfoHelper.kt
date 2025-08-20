package com.cash.ease.money.cashease.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.Display
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.core.app.ActivityCompat
import kotlin.math.max
import kotlin.math.min

/**
 * Created by su on 17-2-8.
 */
@SuppressLint("StaticFieldLeak")
object GeneralInfoHelper {
    private val TAG: String = GeneralInfoHelper::class.java.simpleName

    //application context
    lateinit var context: Context
        private set
    var configurationResources: Resources? = null
    private var sScreenWidth: Int = 0
    private var sScreenHeight: Int = 0

    private var sVersionName: String? = ""

    fun init(context: Context) {
        GeneralInfoHelper.context = context.applicationContext
        initPackageInfo()
        initScreenSize()
    }

    private fun initPackageInfo() {
        if (TextUtils.isEmpty(sVersionName)) {
            try {
                val pm: PackageManager = context.packageManager
                val pi: PackageInfo = pm.getPackageInfo(context.packageName, 0)
                sVersionName = pi.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(TAG, e)
            }
        }
    }

    private fun initScreenSize() {
        val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        // https://developer.android.com/about/versions/12/behavior-changes-all#displaymetrics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val windowMetrics: WindowMetrics = wm.currentWindowMetrics
            val insets: WindowInsets = windowMetrics.windowInsets
            val rect: Rect = windowMetrics.bounds
            Log.d(TAG, "insets=$insets")
            Log.d(TAG, "rect=$rect")
            sScreenWidth =
                min(rect.width().toDouble(), rect.height().toDouble()).toInt()
            sScreenHeight =
                max(rect.width().toDouble(), rect.height().toDouble()).toInt()
        } else {
            val realSize = Point()
            display.getRealSize(realSize)
            sScreenWidth =
                min(realSize.x.toDouble(), realSize.y.toDouble()).toInt()
            sScreenHeight =
                max(realSize.x.toDouble(), realSize.y.toDouble()).toInt()
        }
        Log.d(TAG, "screenWidth=$sScreenWidth, screenHeight=$sScreenHeight")
        val availableSize: Point = Point()
        display.getSize(availableSize)
    }

    @get:SuppressLint("HardwareIds")
    val androidId: String
        get() = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

    @get:SuppressLint("HardwareIds", "MissingPermission")
    val imei: String?
        get() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val manager = context.getSystemService(
                    Context.TELEPHONY_SERVICE
                ) as TelephonyManager?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return manager?.imei
                }
                return manager?.deviceId
            }
            return null
        }
}
