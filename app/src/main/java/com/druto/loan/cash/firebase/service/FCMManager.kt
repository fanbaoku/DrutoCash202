package com.druto.loan.cash.firebase.service

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging

object FCMManager {
    val TAG: String = FCMManager::class.java.simpleName

    fun getFCMToken(listener: OnResultListener) {
        Log.w(TAG, "Fetching FCM Token")
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String> ->
                if (!task.isSuccessful) {
                    Log.w(
                        TAG,
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    val exception = task.exception
                    if (exception == null) {
                        listener.onResult(null, "no message")
                    } else {
                        listener.onResult(null, task.exception!!.message)
                    }
                    return@addOnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                Log.w(TAG, "Fetching FCM registration token=$token")
                listener.onResult(token, null)
            }
    }

    interface OnResultListener {
        fun onResult(token: String?, exception: String?)
    }
}
