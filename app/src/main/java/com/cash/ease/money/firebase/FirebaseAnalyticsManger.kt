package com.cash.ease.money.firebase

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Firebase Installations Service is unavailable. Please try again later.
 * -- If you have recently change your google-services.json file then just delete build folder from android project and re-build your project.
 */
class FirebaseAnalyticsManger {
    fun getFirebaseInstanceId(context: Context, listener: OnResultListener) {
        try {
            // 需要3个权限
            // INTERNET/ACCESS_NETWORK_STATE/WAKE_LOCK
            val analytics = FirebaseAnalytics.getInstance(context)
            analytics.appInstanceId.addOnCompleteListener { task: Task<String?> ->
                if (task.isSuccessful) {
                    listener.onSuccess(task.result)
                } else {
                    val exception = task.exception
                    if (exception == null) {
                        listener.onFailed("")
                    } else {
                        listener.onFailed(exception.message)
                    }
                }
            }
        } catch (e: Exception) {
            listener.onFailed(e.message)
            throw RuntimeException(e)
        }
    }

    interface OnResultListener {
        fun onSuccess(result: String?)
        fun onFailed(reason: String?)
    }

    companion object {
        val TAG: String = FirebaseAnalyticsManger::class.java.simpleName
    }
}
