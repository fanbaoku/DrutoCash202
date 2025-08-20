package com.cash.ease.money.cashease.service

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import com.google.gson.Gson
import com.cash.ease.money.cashease.CashEaseApplication
import com.cash.ease.money.cashease.collector.PermissionEntity
import com.cash.ease.money.cashease.utils.GeneralInfoHelper
import com.cash.ease.money.cashease.utils.SpHelper

object MyEventUploader {
    val TAG: String = MyEventUploader::class.java.simpleName

    // 收到到推送消息
    fun uploadReceiveMessageEvent(messageId: String) {
        val map: MutableMap<String, String> = HashMap()
        map["id"] = messageId
        val gson = Gson()
        CashEaseApplication.uploadEvent("8011", null, gson.toJson(map))
    }

    // 用户授权结果
    fun uploadPermissionResultEvent(list: List<PermissionEntity>) {
        val context = GeneralInfoHelper.context
        val map: MutableMap<String, Int> = HashMap()
        for (entity in list) {
            val isGranted = entity.check(context).isGranted
            map[entity.permission ?: ""] = if (isGranted) 0 else 1
        }
        val gson = Gson()
        CashEaseApplication.uploadEvent("8013", null, gson.toJson(map))
    }

    // 开始上传设备信息（准备上传设备信息的时候调用）
    fun uploadStartUploadDeviceInfoEvent(bytesCount: Int) {
        val map: MutableMap<String, Int> = HashMap()
        map["device"] = bytesCount
        val gson = Gson()
        CashEaseApplication.uploadEvent("8015", null, gson.toJson(map))
    }

    // 设备信息收集及上传成功通知
    fun uploadDeviceInfoSuccessEvent() {
        CashEaseApplication.uploadEvent("8014", null, null)
    }

    // 设备信息上传失败事件
    fun uploadDeviceInfoFailureEvent(info: DeviceInfoRequestInfo) {
        val map: MutableMap<String, Number> = HashMap()
        map["device"] = info.bytesCount
        map["time"] = info.elapse / 1000
        val gson = Gson()
        CashEaseApplication.uploadEvent("8020", null, gson.toJson(map))
    }

    // 更新地理位置信息
    fun uploadLocationEvent(lat: Double, lag: Double) {
        val map: MutableMap<String, Double> = HashMap()
        map["lat"] = lat
        map["lag"] = lag
        val gson = Gson()
        CashEaseApplication.uploadEvent("8017", null, gson.toJson(map))
    }

    // 点击展示信息披露页面
    fun uploadLaunchPermissionSpecEvent() {
        CashEaseApplication.uploadEvent("8019", null, null)
    }

    // 记录Toast提示内容
    fun uploadToastEvent(content: CharSequence) {
        val map: MutableMap<String, CharSequence> = HashMap()
        map["message"] = content
        val gson = Gson()
        CashEaseApplication.uploadEvent("8016", null, gson.toJson(map))
    }

    // 请求授权 被调用
    fun uploadRequestPermissionsEvent() {
        CashEaseApplication.uploadEvent("8012", null, null)
    }

    // 点击拍照按钮（静态活体）
    fun uploadCameraGoofTakePhotoButtonEvent() {
        CashEaseApplication.uploadEvent("8022", null, null)
    }

    // 点击拍照确认按钮（静态活体）
    fun uploadCameraGoofTakePhotoConfirmEvent() {
        CashEaseApplication.uploadEvent("8024", null, null)
    }

    // 点击拍照取消按钮（静态活体）
    fun uploadCameraGoofTakePhotoCancelEvent() {
        CashEaseApplication.uploadEvent("8025", null, null)
    }

    // 活体成功
    fun uploadSilentLivingSuccessEvent() {
        CashEaseApplication.uploadEvent("8027", null, null)
    }

    // 活体失败
    fun uploadSilentLivingFailEvent(reason: String) {
        val map: MutableMap<String, CharSequence> = HashMap()
        map["message"] = reason
        val gson = Gson()
        CashEaseApplication.uploadEvent("8028", null, gson.toJson(map))
    }

    // 评论成功
    fun uploadCallCommentSuccessEvent() {
        CashEaseApplication.uploadEvent("8233", null, null)
    }

    // 评论失败
    fun uploadCallCommentFailEvent(reason: String) {
        val map: MutableMap<String, CharSequence> = HashMap()
        map["message"] = reason
        val gson = Gson()
        CashEaseApplication.uploadEvent("8234", null, gson.toJson(map))
    }

    // 评论取消
    fun uploadCallCommentCancelEvent() {
        CashEaseApplication.uploadEvent("8235", null, null)
    }

    // 选择联系人成功
    fun uploadSelectNameMobileSuccessEvent(name: String?, phone: String?) {
        val map: MutableMap<String, CharSequence?> = HashMap()
        map["name"] = name
        map["phone"] = phone
        val gson = Gson()
        CashEaseApplication.uploadEvent("8018", null, gson.toJson(map))
    }

    // 选择联系人失败
    // "SDK版本",
    // "系统版本",
    // "主板"]
    fun uploadSelectNameMobileFailEvent() {
        val map: MutableMap<String, Any> = HashMap()
        val list: MutableList<Any?> = ArrayList()
        list.add(SpHelper.uuid)
        val data = CashEaseApplication.APPLICATION_DATA
        list.add(data.advertisingId)
        list.add(data.installReferrer)
        val context = GeneralInfoHelper.context
        val pm = context.packageManager
        try {
            val packageInfo = pm.getPackageInfo(context.packageName, 0)
            list.add(packageInfo.versionName)
            list.add(PackageInfoCompat.getLongVersionCode(packageInfo))
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, e)
        }
        list.add(Build.BRAND)
        list.add(Build.MODEL)
        list.add(Build.PRODUCT)
        list.add(Build.VERSION.SDK_INT)
        list.add(Build.VERSION.RELEASE)
        map["device"] = list
        val gson = Gson()
        CashEaseApplication.uploadEvent("8237", null, gson.toJson(map))
    }

    fun uploadStartFromWebEvent(link: String) {
        val map: MutableMap<String, CharSequence> = HashMap()
        map["link_source"] = link
        val gson = Gson()
        CashEaseApplication.uploadEvent("8239", null, gson.toJson(map))
    }

    // host都不通
    fun uploadHostFailEvent() {
        CashEaseApplication.uploadEvent("ibjc86", null, null)
    }

    // firebase获取host不通
    fun uploadFirebaseHostFailEvent() {
        CashEaseApplication.uploadEvent("dkq654", null, null)
    }

    class DeviceInfoRequestInfo {
        var bytesCount: Int = 0
        var elapse: Long = 0

        override fun toString(): String {
            return "DeviceInfoRequestInfo{" +
                    "bytesCount=" + bytesCount +
                    ", elapse=" + elapse +
                    '}'
        }
    }
}
