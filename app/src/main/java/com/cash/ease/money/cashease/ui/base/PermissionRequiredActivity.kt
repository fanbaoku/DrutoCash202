package com.cash.ease.money.cashease.ui.base

import android.annotation.TargetApi
import android.os.Build
import com.cash.ease.money.cashease.utils.PermissionHelper
import com.cash.ease.money.cashease.utils.PermissionHelper.DangerousPermission

abstract class PermissionRequiredActivity : BaseAppCompatActivity(), DangerousPermission {
    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun onPermissionGranted(reqCode: Int) {}

    abstract override fun onPermissionDenied(requestCode: Int, vararg permissions: String)
}
