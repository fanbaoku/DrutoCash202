package com.druto.loan.cash.drutocash.collector

import android.Manifest
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.druto.loan.cash.drutocash.utils.CashEaseHelper

class PermissionEntity : Parcelable, Cloneable {
    val permission: String? // 具体权限
    var level: Int = 0 // 级别要求 强授权、弱授权 - 只有启动接口看这个字段
    var isGranted: Boolean = false // 是否已授权

    constructor(permission: String?) {
        this.permission = permission
    }

    protected constructor(`in`: Parcel) {
        permission = `in`.readString()
        level = `in`.readInt()
        isGranted = `in`.readByte().toInt() != 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(permission)
        dest.writeInt(level)
        dest.writeByte((if (isGranted) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    fun check(context: Context): PermissionEntity {
        isGranted = CashEaseHelper.hasPermission(context, permission!!)
        return this
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }

    override fun toString(): String {
        return "PermissionEntity{" +
                "permission='" + permission + '\'' +
                ", level=" + level +
                ", granted=" + isGranted +
                '}'
    }

    companion object CREATOR : Creator<PermissionEntity> {
        override fun createFromParcel(parcel: Parcel): PermissionEntity {
            return PermissionEntity(parcel)
        }

        override fun newArray(size: Int): Array<PermissionEntity?> {
            return arrayOfNulls(size)
        }

        fun hasStrongLocationPermission(list: List<PermissionEntity>): Boolean {
            for (permissionEntity in list) {
                val permission = permissionEntity.permission
                if (Manifest.permission.ACCESS_COARSE_LOCATION != permission ) {
                    continue
                }
                if (permissionEntity.level == 2) {
                    return true
                }
            }
            return false
        }
    }
}
