package com.cash.ease.money.cashease.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.cash.ease.money.cashease.utils.ToastUtil.showToast

/**
 * Created by su on 2024/12/3.
 */
object CashEaseHelper {
    private val TAG: String = CashEaseHelper::class.java.simpleName

    val isPhone: Boolean
        get() = hasFeature(PackageManager.FEATURE_TELEPHONY)

    fun hasFeature(feature: String): Boolean {
        val pm = GeneralInfoHelper.context.packageManager
        return pm.hasSystemFeature(feature)
    }

    fun hasPermission(context: Context, permission: String): Boolean {
        if (Build.VERSION.SDK_INT < 23) {
            return true
        }
        val permissionCheck = ContextCompat.checkSelfPermission(context, permission)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    fun startActivity(context: Context, intent: Intent?) {
        if (intent == null) {
            Log.d(TAG, "startActivity: intent is null")
            return
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showToast(context, e.message ?: "")
            Log.d(TAG, "intent: $intent", e)
        } catch (e: SecurityException) {
            showToast(context, e.message ?: "")
            Log.d(TAG, "intent: $intent", e)
        }
    }

    fun isPackageExisted(packageName: String): Boolean {
        val context = GeneralInfoHelper.context
        val pm = context.packageManager
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }
}
