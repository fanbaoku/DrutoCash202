package com.cash.ease.money.cashease.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import com.cash.ease.money.cashease.R

class PermissionSmsDialog (context: Context,onClick:OnSmsClick) : Dialog(context) {

    init {
        // 设置弹框样式
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_permission_sms)

        // 设置弹框背景透明（可选）
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 设置弹框大小（可选）
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        findViewById<Button>(R.id.access).setOnClickListener{
            if (onClick!=null){
                onClick.onSmsClick()
            }
        }
    }

    interface OnSmsClick{
        fun onSmsClick()
    }

}