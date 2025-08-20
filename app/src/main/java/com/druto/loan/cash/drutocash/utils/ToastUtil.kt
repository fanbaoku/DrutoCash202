package com.druto.loan.cash.drutocash.utils

import android.content.Context
import android.widget.Toast
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadToastEvent

object ToastUtil {
    fun showToast(context: Context, content: String) {
        uploadToastEvent(content)
        Toast.makeText(context, content, Toast.LENGTH_LONG).show()
    }
}
