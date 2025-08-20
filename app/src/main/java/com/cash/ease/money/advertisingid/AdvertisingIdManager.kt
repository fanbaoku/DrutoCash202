package com.cash.ease.money.advertisingid

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import java.io.IOException

class AdvertisingIdManager {
    @WorkerThread
    fun getAdvertisingId(context: Context, listener: OnFinishListener) {
        Log.d(TAG, "thread=" + Thread.currentThread().name)
        if (isPackageExisted(context, "com.android.vending")) {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                val advertisingId = adInfo.id // 获取广告 ID
                val isLimitAdTrackingEnabled = adInfo.isLimitAdTrackingEnabled // 是否限制广告跟踪
                Log.d(
                    TAG,
                    "advertisingId=$advertisingId"
                )
                Log.d(
                    TAG,
                    "isLimitAdTrackingEnabled=$isLimitAdTrackingEnabled"
                )
                listener.onSuccess(advertisingId, isLimitAdTrackingEnabled)
            } catch (e: IOException) {
                Log.w(TAG, e)
                listener.onError(e.message, e)
            } catch (e: GooglePlayServicesNotAvailableException) {
                Log.w(TAG, e)
                listener.onError(e.message, e)
            } catch (e: GooglePlayServicesRepairableException) {
                Log.w(TAG, e)
                listener.onError(e.message, e)
            }
        } else {
            listener.onError("not found google play", null)
        }
    }

    interface OnFinishListener {
        fun onSuccess(id: String?, isLimitAdTrackingEnabled: Boolean)

        fun onError(msg: String?, error: Throwable?)
    }

    companion object {
        val TAG: String = AdvertisingIdManager::class.java.simpleName

        private fun isPackageExisted(context: Context, packageName: String): Boolean {
            val pm = context.packageManager
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }
            return true
        }
    }
}
