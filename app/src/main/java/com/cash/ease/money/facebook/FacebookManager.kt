package com.cash.ease.money.facebook

import com.facebook.FacebookSdk.addLoggingBehavior
import com.facebook.FacebookSdk.setIsDebugEnabled
import com.facebook.LoggingBehavior

object FacebookManager {
    fun enableDebug(debug: Boolean) {
        setIsDebugEnabled(debug)
        val behavior = if (debug) {
            LoggingBehavior.APP_EVENTS
        } else {
            LoggingBehavior.DEVELOPER_ERRORS
        }
        addLoggingBehavior(behavior)
    }
}
