package com.druto.loan.cash.drutocash.model

import com.druto.loan.cash.drutocash.collector.PermissionEntity
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadRequestPermissionsEvent
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadPermissionResultEvent
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper
import java.util.stream.Collectors

class PermissionSpecModel {
    fun requestPermissionsEvent() {
        uploadRequestPermissionsEvent()
    }

    fun onPermissionGranted(list: List<PermissionEntity>) {
        uploadPermissionResultEvent(list)
    }

    fun onPermissionDenied(list: List<PermissionEntity>) {
        uploadPermissionResultEvent(list)
    }

    fun onlyStrongPermissions(list: List<PermissionEntity>): List<PermissionEntity> {
        val context = GeneralInfoHelper.context
        return list.stream()
            .map { e: PermissionEntity -> e.check(context) }
            .filter { entity: PermissionEntity -> !entity.isGranted && entity.level == 2 }  // 只检查强授权
            .collect(Collectors.toList())
    }
}
