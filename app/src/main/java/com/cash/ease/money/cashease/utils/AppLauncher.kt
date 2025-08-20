package com.cash.ease.money.cashease.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.cash.ease.money.cashease.utils.ToastUtil.showToast

object AppLauncher {
    fun openWhatsAppChat(context: Context, phoneNumber: String?) {
        try {
            // 构建 WhatsApp URI
            val uri = "whatsapp://send?phone=$phoneNumber"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(uri))
            intent.setPackage("com.whatsapp") // 限定只用 WhatsApp 打开
            // 启动 WhatsApp
            CashEaseHelper.startActivity(context, intent)
        } catch (e: Exception) {
            showToast(context, e.message!!)
        }
    }

    fun openAppInPlayStore(context: Context) {
        val appPackageName = context.packageName // 获取当前应用的包名

        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$appPackageName")
            )
            intent.setPackage("com.android.vending")
            // 直接跳转到Google Play应用页面
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // 如果设备上没有安装Google Play，则跳转到网页版
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    fun goSettings(context: Context, packageName: String?) {
        val intent =
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        CashEaseHelper.startActivity(context, intent)
    }
}
