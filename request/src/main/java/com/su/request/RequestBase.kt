package com.su.request

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import androidx.lifecycle.ProcessLifecycleOwner
import com.su.request.http.RequestLifecycleListener
import kotlin.math.max
import kotlin.math.min

@SuppressLint("StaticFieldLeak")
object RequestBase {
    lateinit var context: Context
    var deviceId: String? = null
    var screenWidth = 0
    var screenHeight = 0
    var versionCode: Long = 0
    lateinit var versionName: String
    lateinit var packageName: String
    lateinit var appName: String

    fun init(context: Context, deviceId: String?) {
        this.context = context

        // events will be dispatched with a delay after a last activity passed through them.
        // This delay is long enough to guarantee that ProcessLifecycleOwner won't send any events if activities are destroyed and recreated due to a configuration change
        ProcessLifecycleOwner.get().lifecycle.addObserver(RequestLifecycleListener.getInstance())
        this.deviceId = deviceId
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val realSize = Point()
        display.getRealSize(realSize)
        screenWidth = min(realSize.x, realSize.y)
        screenHeight = max(realSize.x, realSize.y)

        val pm: PackageManager = context.packageManager
        val pi: PackageInfo = pm.getPackageInfo(context.packageName, 0)
        versionName = pi.versionName ?: ""
        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pi.longVersionCode
        } else {
            pi.versionCode.toLong()
        }
        packageName = pi.packageName
        appName = pi.applicationInfo?.loadLabel(pm).toString()
    }
}
