package com.druto.loan.cash.drutocash.collector

import android.util.Log
import com.druto.loan.cash.drutocash.DrutoCashApplication
import com.druto.loan.cash.drutocash.entity.Powdery
import com.druto.loan.cash.drutocash.utils.SpHelper

class ApplicationData {
    var sessionId: String? = "" //会话id ,由初始化接口返回
    private var host: String? = null
    var uuid: String? = null
    var separation: Int = 0 // 紧急联系人选择方式
    var mainH5Url: String? = null // 紧急联系人选择方式
    private val permissionEntityList: MutableList<PermissionEntity> = ArrayList()
    var upgradeInfo: Powdery? = null
    var advertisingId: String? = null
    var installReferrer: String? = null

    fun getHost(): String? {
        return host
    }

    fun setHost(host: String) {
        Log.i(DrutoCashApplication.TAG, "setHost=$host")
        SpHelper.host = host
        this.host = host
    }

    fun getPermissionEntityList(): List<PermissionEntity> {
        return permissionEntityList
    }

    fun addAllPermissionEntity(list: List<PermissionEntity>) {
        permissionEntityList.clear()
        permissionEntityList.addAll(list)
    }

    override fun toString(): String {
        return "ApplicationData{" +
                "sessionId='" + sessionId + '\'' +
                ", host='" + host + '\'' +
                ", uuid='" + uuid + '\'' +
                ", separation=" + separation +
                ", mainH5Url='" + mainH5Url + '\'' +
                ", permissionEntityList=" + permissionEntityList +
                ", upgradeInfo=" + upgradeInfo +
                '}'
    }
}
