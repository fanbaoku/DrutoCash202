package com.cash.ease.money.cashease.presenter

import com.cash.ease.money.cashease.collector.PermissionEntity
import com.cash.ease.money.cashease.model.PermissionSpecModel
import com.cash.ease.money.cashease.utils.ToastUtil.showToast
import com.cash.ease.money.cashease.utils.GeneralInfoHelper
import com.cash.ease.money.cashease.view.PermissionSpecView

class PermissionSpecPresenter(
    private val view: PermissionSpecView,
    private val model: PermissionSpecModel
) {
    fun request(permissionEntities: List<PermissionEntity>?) {
        model.requestPermissionsEvent()
        if (permissionEntities.isNullOrEmpty()) {
            showToast(GeneralInfoHelper.context, "no permissions to request")
            return
        }
        val permissionArray: Array<String> = permissionEntities.mapNotNull { it.permission }.toTypedArray()
        view.requestPermissions(permissionArray)
    }

    fun onPermissionGranted(list: List<PermissionEntity>) {
        model.onPermissionGranted(list)
    }

    fun onPermissionDenied(list: List<PermissionEntity>) {
        model.onPermissionDenied(list)
    }

    fun onlyStrongPermissions(list: List<PermissionEntity>): List<PermissionEntity> {
        return model.onlyStrongPermissions(list)
    }
}
