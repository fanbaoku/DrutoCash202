package com.cash.ease.money.cashease.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionHelper {
    fun permissionRequest(
        activity: Activity,
        dangerousPermission: DangerousPermission,
        requestCode: Int,
        vararg permissions: String
    ) {
        if (checkSelfPermission(*permissions)) {
            dangerousPermission.onPermissionGranted(requestCode)
            return
        }
        val deniedArray = shouldShowRequestPermissionRationale(activity, *permissions)
        if (deniedArray.isNotEmpty()) {
            dangerousPermission.onPermissionDenied(requestCode, *deniedArray)
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }

    fun permissionRequest(
        fragment: Fragment,
        dangerousPermission: DangerousPermission,
        requestCode: Int,
        vararg permissions: String
    ) {
        if (checkSelfPermission(*permissions)) {
            dangerousPermission.onPermissionGranted(requestCode)
            return
        }
        val deniedArray =
            shouldShowRequestPermissionRationale(fragment.requireActivity(), *permissions)
        if (deniedArray.isNotEmpty()) {
            dangerousPermission.onPermissionDenied(requestCode, *deniedArray)
        } else {
            try {
                fragment.requestPermissions(permissions, requestCode)
            } catch (e: Exception) {
                val context = GeneralInfoHelper.context
                ToastUtil.showToast(context, e.message ?: "")
            }
        }
    }

    private fun shouldShowRequestPermissionRationale(
        activity: Activity,
        vararg permissions: String
    ): Array<String> {
        val deniedList: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                deniedList.add(permission)
            }
        }
        return deniedList.toTypedArray()
    }

    fun checkSelfPermission(vararg permissions: String): Boolean {
        for (permission in permissions) {
            val permissionCheck =
                ContextCompat.checkSelfPermission(GeneralInfoHelper.context, permission)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun onRequestPermissionsResult(
        dangerousPermission: DangerousPermission,
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        var granted = true
        val length = grantResults.size
        var deniedIndex = -1
        for (i in 0 until length) {
            granted = granted && grantResults[i] == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                deniedIndex = i
                break
            }
        }
        if (granted) {
            dangerousPermission.onPermissionGranted(requestCode)
        } else {
            dangerousPermission.onPermissionDenied(requestCode, permissions[deniedIndex]!!)
        }
    }

    fun <T> execIfGranted(func: () -> T, notGranted: T, vararg permissions: String): T {
        val context = GeneralInfoHelper.context
        for (permission in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(context, permission)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return notGranted
            }
        }
        return func()
    }

    interface DangerousPermission {
        fun onPermissionGranted(reqCode: Int)
        fun onPermissionDenied(requestCode: Int, vararg permissions: String)
    }
}
