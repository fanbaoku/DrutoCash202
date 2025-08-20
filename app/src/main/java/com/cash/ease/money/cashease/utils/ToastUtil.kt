package com.cash.ease.money.cashease.utils

import android.content.Context
import android.widget.Toast
import com.cash.ease.money.cashease.service.MyEventUploader.uploadToastEvent

object ToastUtil {
    fun showToast(context: Context, content: String) {
        uploadToastEvent(content)
        Toast.makeText(context, content, Toast.LENGTH_LONG).show()
    }
}
