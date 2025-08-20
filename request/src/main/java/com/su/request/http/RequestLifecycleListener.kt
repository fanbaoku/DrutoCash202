package com.su.request.http

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class RequestLifecycleListener : DefaultLifecycleObserver {
    private val tag = RequestLifecycleListener::class.java.simpleName

    companion object {
        private val APP_LIFECYCLE_LISTENER: RequestLifecycleListener = RequestLifecycleListener()
        private const val PREVENT_INTERVAL = (30 * 1000).toLong()
        private var sForeground = false
        private var sLastForeground: Long = 0
        // 如果已经进入后台超过一分钟，则不再弹出Toast
        fun shouldToast(): Boolean {
            return sForeground || System.currentTimeMillis() - sLastForeground <= PREVENT_INTERVAL
        }

        fun getInstance(): RequestLifecycleListener {
            return APP_LIFECYCLE_LISTENER
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(tag, "onStart")
        sForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d(tag, "onStop")
        sForeground = false
        sLastForeground = System.currentTimeMillis()
    }
}
