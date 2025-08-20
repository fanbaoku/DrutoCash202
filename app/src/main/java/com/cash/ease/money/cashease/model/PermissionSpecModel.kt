package com.cash.ease.money.cashease.model

import com.cash.ease.money.cashease.collector.PermissionEntity
import com.cash.ease.money.cashease.service.MyEventUploader.uploadRequestPermissionsEvent
import com.cash.ease.money.cashease.service.MyEventUploader.uploadPermissionResultEvent
import com.cash.ease.money.cashease.utils.GeneralInfoHelper
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
