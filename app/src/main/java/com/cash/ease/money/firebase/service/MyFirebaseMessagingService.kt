package com.cash.ease.money.firebase.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.cash.ease.money.cashease.CashEaseApplication
import com.cash.ease.money.cashease.R
import com.cash.ease.money.cashease.Url
import com.cash.ease.money.cashease.activity.MainActivity
import com.cash.ease.money.cashease.http.ParserUtil
import com.cash.ease.money.cashease.service.MyEventUploader
import com.cash.ease.money.cashease.utils.ToastUtil
import com.cash.ease.money.cashease.utils.GeneralInfoHelper
import com.cash.ease.money.cashease.utils.SpHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.su.request.http.HttpResult
import com.su.request.http.NetRequest
import com.su.request.http.NullObject
import com.su.request.http.function.BusinessFunction
import com.su.request.http.observer.BaseObserver
import com.su.request.http.transformer.ParserTransformer

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        updateFCMToken(token, null, 1)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // 获取消息内容
        val notification = message.notification
        MyEventUploader.uploadReceiveMessageEvent(message.messageId ?: "")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck =
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "need permission POST_NOTIFICATIONS")
                return
            }
        }

        val title = notification?.title
        val body = notification?.body
        // 显示通知
        showNotification(title, body)
    }

    private fun showNotification(title: String?, body: String?) {
        // 创建点击通知后跳转的 Intent
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // 确保只启动一个 Activity 实例
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // 创建 PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 显示通知
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0+ 需要创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "msg",
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 构建通知
        val notificationBuilder =
            NotificationCompat.Builder(this, "msg")
                .setSmallIcon(R.drawable.ic_notification) // 替换为你的图标资源
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true) // 点击后自动取消通知
                .setContentIntent(pendingIntent) // 设置点击动作
        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        val TAG: String = MyFirebaseMessagingService::class.java.simpleName

        fun updateFCMToken(token: String?, exceptionMsg: String?, way: Int) {
            // 如果在初始化完成之前收到新的token则不同步至服务器，因为初始化之后还会请求
            if (SpHelper.host == null) {
                Log.w(TAG, "do not update FCM token until finish initializing")
                return
            }
            val url = SpHelper.host + Url.SEND_FCM_TOKEN
            Log.d(TAG, "updateFCMToken url=$url")
            val jsonString = prepareJson(token, exceptionMsg, way)
            val requestBody = ParserUtil.prepareRequestBody(jsonString, false)
            NetRequest.create(url, object : TypeToken<HttpResult<*>?>() {})
                .method("POST")
                .build(CashEaseApplication.client, requestBody)
                .compose(ParserTransformer())
                .map<Any>(object : BusinessFunction<HttpResult<*>, NullObject>() {
                    override fun onSuccess(result: HttpResult<*>?): NullObject {
                        if (result?.total == 0) {
                            Log.d(TAG, "way=$way, FCMTokenUpdated=$token")
                        } else {
                            Log.w(TAG, "updateFCMTokenError=" + result?.swell)
                        }
                        return NullObject.NULL_OBJECT
                    }

                }).safeSubscribe(object : BaseObserver<Any?>(null) {
                    override fun onComplete() {
                        super.onComplete()
                        Log.d(TAG, "updateFCMTokenComplete")
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        Log.w(TAG, "updateFCMTokenError")
                        ToastUtil.showToast(GeneralInfoHelper.context, e.message ?: "")
                    }
                })
        }

        private fun prepareJson(token: String?, exceptionMsg: String?, way: Int): String {
            val list: MutableList<Any?> = ArrayList()
            list.add(SpHelper.uuid)
            list.add(token)
            list.add(exceptionMsg)
            list.add(way) // 获取方法："getToken：0" 或 "onNewToken: 1"
            val gson = Gson()
            return gson.toJson(list)
        }
    }

    class FCMTokenEntity(
        val token: String?, val exceptionMsg: String?, val way: Int) {
        override fun toString(): String {
            return "FCMTokenEntity{" +
                    "token='" + token + '\'' +
                    ", exceptionMsg='" + exceptionMsg + '\'' +
                    ", way=" + way +
                    '}'
        }
    }
}
