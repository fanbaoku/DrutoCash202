package com.druto.loan.cash.drutocash.entity

import android.Manifest
import com.druto.loan.cash.drutocash.collector.PermissionEntity

data class Brothel(
    var wince: Int = 0,
    var blast: Int = 0,
    var shoulder: Int = 0,
    var listener: Int = 0,
    var don: Int = 0,
    var doctrine: Int = 0
) {

    companion object {
        fun transfer(required: Brothel?): ArrayList<PermissionEntity> {
            val list: ArrayList<PermissionEntity> = ArrayList()
            if (required == null) {
                return list
            }
            val location = PermissionEntity(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            val phoneState = PermissionEntity(Manifest.permission.READ_PHONE_STATE)
//            val sms = PermissionEntity(Manifest.permission.READ_SMS)
//            val calendarRead = PermissionEntity(Manifest.permission.READ_CALENDAR)
//            val calendarWrite = PermissionEntity(Manifest.permission.WRITE_CALENDAR)
            val camera = PermissionEntity(Manifest.permission.CAMERA)
            location.level = 2
            phoneState.level = 2
//            sms.level = 2
//            calendarRead.level = 2
//            calendarWrite.level = 2
            camera.level = 2
            list.add(location)
            list.add(phoneState)
//            list.add(sms)
//            list.add(calendarRead)
//            list.add(calendarWrite)
            list.add(camera)
            return list
        }
    }
}

