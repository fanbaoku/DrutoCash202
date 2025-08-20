package com.druto.loan.cash.drutocash.ui.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.druto.loan.cash.drutocash.DrutoCashApplication
import com.druto.loan.cash.drutocash.utils.CashEaseHelper.hasPermission
import com.druto.loan.cash.drutocash.collector.SomeInfoCollector.isGpsEnabled
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadLocationEvent
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper
import com.druto.loan.cash.drutocash.utils.LanguageUtil.getUpdatedResourcesContext
import com.druto.loan.cash.drutocash.utils.SpHelper
import com.druto.loan.cash.drutocash.utils.ThreadUtil.getSimpleDateFormat
import com.su.request.http.observer.BaseObserver
import java.util.Date

/**
 * Created by su on 18-1-2.
 */
@SuppressLint("Registered")
open class BaseAppCompatActivity : AppCompatActivity() {
    private val lifecycleLogTag: String = javaClass.simpleName
    val LOCATION_TAG: String = "LOCATION"
    var logTag: String = javaClass.simpleName
    val observers: MutableList<BaseObserver<*>> = ArrayList()

    private val mHandler = Handler(Looper.getMainLooper())
    private var mLocationManager: LocationManager? = null
    private val mLocationListener = LocationListener { location: Location ->
        logLocation(location, "New location update")
        DrutoCashApplication.LOCATION_LIVE_DATA.postValue(location)
    }
    private val mLocationTask: Runnable = object : Runnable {
        override fun run() {
            if (ActivityCompat.checkSelfPermission(
                    GeneralInfoHelper.context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                // 获取最后一次已知的位置（可选）
                val lastKnownLocation = fetchLastKnownLocation()
                if (lastKnownLocation != null) {
                    logLocation(lastKnownLocation, "Last known location")
                    DrutoCashApplication.LOCATION_LIVE_DATA.postValue(lastKnownLocation)
                }

                // 请求最新的位置
                mLocationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 6000L,
                    10f, mLocationListener
                )
            }

            // 每隔10分钟重新执行一次任务
            val sdf = getSimpleDateFormat("HH:mm:ss SSS")
            val date = Date(System.currentTimeMillis() + TEN_MINUTES)
            Log.d(LOCATION_TAG, "nextLocation=" + sdf.format(date))
            mHandler.postDelayed(this, TEN_MINUTES)
        }
    }

    private fun logLocation(location: Location, tag: String) {
        val latitude = location.latitude
        val longitude = location.longitude
        Log.d(
            LOCATION_TAG,
            (tag + ": Latitude = " + latitude + ", Longitude = " + longitude + ", provider="
                    + location.provider)
        )
        DrutoCashApplication.LOCATION_LIVE_DATA.postValue(location)
        if (SpHelper.host != null) {
            uploadLocationEvent(latitude, longitude)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastKnownLocation(): Location? {
//        val lastKnownLocation =
//            mLocationManager!!.getLastKnownLocation(
//                LocationManager.GPS_PROVIDER
//            )
//        if (lastKnownLocation != null) {
//            return lastKnownLocation
//        }
        return mLocationManager!!.getLastKnownLocation(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun startLocate() {
        Log.d(LOCATION_TAG, "startLocate")
        mHandler.removeCallbacks(mLocationTask)
        mLocationTask.run()
    }

    override fun setTitle(titleRes: Int) {
        title = getString(titleRes)
    }

    override fun attachBaseContext(newBase: Context) {
        val context = getUpdatedResourcesContext(newBase)
        super.attachBaseContext(context)
        logLifecycle(lifecycleLogTag, this, "attachBaseContext")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logLifecycle(
            lifecycleLogTag, this,
            "onCreate=$this"
        )
        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (isGpsEnabled(this) &&
            hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            startLocate()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        logLifecycle(lifecycleLogTag, this, "onPostCreate")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        logLifecycle(lifecycleLogTag, this, "onNewIntent")
    }

    override fun onStart() {
        super.onStart()
        logLifecycle(lifecycleLogTag, this, "onStart")
    }

    override fun onRestart() {
        super.onRestart()
        logLifecycle(lifecycleLogTag, this, "onRestart")
    }

    override fun onResume() {
        super.onResume()
        logLifecycle(lifecycleLogTag, this, "onResume")
    }

    override fun onPause() {
        super.onPause()
        logLifecycle(lifecycleLogTag, this, "onPause")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        logLifecycle(lifecycleLogTag, this, "onRestoreInstanceState")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        logLifecycle(lifecycleLogTag, this, "onSaveInstanceState")
    }

    override fun onStop() {
        super.onStop()
        logLifecycle(lifecycleLogTag, this, "onStop")
    }

    override fun onDestroy() {
        disposeAll(observers.iterator())
        super.onDestroy()
        logLifecycle(lifecycleLogTag, this, "onDestroy")
        mLocationManager!!.removeUpdates(mLocationListener)
        mHandler.removeCallbacks(mLocationTask)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        logLifecycle(
            lifecycleLogTag, this,
            "onConfigurationChanged: $newConfig"
        )
    }

    fun disposeAll(iterator: MutableIterator<BaseObserver<*>>) {
        while (iterator.hasNext()) {
            val baseObserver = iterator.next()
            if (!baseObserver.isDisposed) {
                baseObserver.dispose()
            }
            iterator.remove()
        }
    }

    companion object {
        var sLogState: Boolean = false
        private const val TEN_MINUTES = 10 * 60 * 1000L // 10分钟
        private fun logLifecycle(tag: String, activity: Activity, lifecycle: String) {
            val content: String
            if (sLogState) {
                val finishing = activity.isFinishing
                val destroyed = activity.isDestroyed
                content = ("ACTIVITY-" + lifecycle + ", finishing=" + finishing + ", destroyed="
                        + destroyed)
            } else {
                content = "ACTIVITY-$lifecycle"
            }
            Log.i(tag, content)
        }
    }
}
