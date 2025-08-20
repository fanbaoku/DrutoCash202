package com.druto.loan.cash.drutocash.presenter

import com.druto.loan.cash.drutocash.collector.PermissionEntity
import com.druto.loan.cash.drutocash.model.PermissionSpecModel
import com.druto.loan.cash.drutocash.utils.ToastUtil.showToast
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper
import com.druto.loan.cash.drutocash.view.PermissionSpecView

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
