package com.druto.loan.cash.adjust

import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.druto.loan.cash.drutocash.BuildConfig

object AdjustManager {
    fun onCreate(context: Context?) {
        val environment = AdjustConfig.ENVIRONMENT_PRODUCTION
        val config = AdjustConfig(context, BuildConfig.APP_TOKEN, environment)
        Adjust.onCreate(config)
    }
}
