package com.cash.ease.money.installreferrer

import android.content.Context
import android.os.RemoteException
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener

class InstallReferrerManager {
    private var referrerClient: InstallReferrerClient? = null

    fun tryStartConnection(context: Context, listener: OnResultListener) {
        if (getFromSp(context, listener)) {
            return
        }
        startConnection(context, listener)
    }

    private fun startConnection(context: Context, listener: OnResultListener) {
        referrerClient = InstallReferrerClient.newBuilder(context).build()
        referrerClient?.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK ->                         // Connection established.
                        getInfo(context, listener)

                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED ->                         // API not available on the current Play Store app.
                        listener.onFail(InstallReferrerEntity("FEATURE_NOT_SUPPORTED"))

                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE ->                         // Connection couldn't be established.
                        listener.onFail(InstallReferrerEntity("SERVICE_UNAVAILABLE"))
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                listener.onDisconnected()
            }
        })
    }

    private fun getFromSp(context: Context, listener: OnResultListener): Boolean {
        val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        if (!sp.contains(SP_INSTALL_REFERRER)
            || !sp.contains(SP_REFERRER_CLICK_TIMESTAMP_SECONDS)
            || !sp.contains(SP_INSTALL_BEGIN_TIMESTAMP_SECONDS)
            || !sp.contains(SP_REFERRER_CLICK_TIMESTAMP_SERVER_SECONDS)
            || !sp.contains(SP_INSTALL_BEGIN_TIMESTAMP_SERVER_SECONDS)
            || !sp.contains(SP_INSTALL_VERSION)
            || !sp.contains(SP_GOOGLE_PLAY_INSTANT)
        ) {
            return false
        }
        val installReferrer = sp.getString(SP_INSTALL_REFERRER, null)
        val referrerClickTimestampSeconds = sp.getLong(SP_REFERRER_CLICK_TIMESTAMP_SECONDS, 0)
        val installBeginTimestampSeconds = sp.getLong(SP_INSTALL_BEGIN_TIMESTAMP_SECONDS, 0)
        val referrerClickTimestampServerSeconds = sp.getLong(
            SP_REFERRER_CLICK_TIMESTAMP_SERVER_SECONDS, 0
        )
        val installBeginTimestampServerSeconds = sp.getLong(
            SP_INSTALL_BEGIN_TIMESTAMP_SERVER_SECONDS, 0
        )
        val installVersion = sp.getString(SP_INSTALL_VERSION, null)
        val googlePlayInstantParam = sp.getBoolean(SP_GOOGLE_PLAY_INSTANT, false)
        val entity = InstallReferrerEntity(
            installReferrer,
            referrerClickTimestampSeconds,
            installBeginTimestampSeconds,
            referrerClickTimestampServerSeconds,
            installBeginTimestampServerSeconds,
            installVersion,
            googlePlayInstantParam
        )
        listener.onSuccess(entity)
        return true
    }

    private fun getInfo(context: Context, listener: OnResultListener) {
        try {
            val response = referrerClient!!.installReferrer
            val installReferrer = response.installReferrer
            val referrerClickTimestampSeconds = response.referrerClickTimestampSeconds
            val installBeginTimestampSeconds = response.installBeginTimestampSeconds
            val referrerClickTimestampServerSeconds = response.referrerClickTimestampServerSeconds
            val installBeginTimestampServerSeconds = response.installBeginTimestampServerSeconds
            val installVersion = response.installVersion
            val googlePlayInstantParam = response.googlePlayInstantParam

            val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            sp.edit()
                .putString(SP_INSTALL_REFERRER, installReferrer)
                .putLong(SP_REFERRER_CLICK_TIMESTAMP_SECONDS, referrerClickTimestampSeconds)
                .putLong(SP_INSTALL_BEGIN_TIMESTAMP_SECONDS, installBeginTimestampSeconds)
                .putLong(
                    SP_REFERRER_CLICK_TIMESTAMP_SERVER_SECONDS,
                    referrerClickTimestampServerSeconds
                )
                .putLong(
                    SP_INSTALL_BEGIN_TIMESTAMP_SERVER_SECONDS,
                    installBeginTimestampServerSeconds
                )
                .putString(SP_INSTALL_VERSION, installVersion)
                .putBoolean(SP_GOOGLE_PLAY_INSTANT, googlePlayInstantParam)
                .apply()

            val entity = InstallReferrerEntity(
                installReferrer,
                referrerClickTimestampSeconds,
                installBeginTimestampSeconds,
                referrerClickTimestampServerSeconds,
                installBeginTimestampServerSeconds,
                installVersion,
                googlePlayInstantParam
            )
            listener.onSuccess(entity)
        } catch (e: RemoteException) {
            Log.w(TAG, e)
            listener.onFail(InstallReferrerEntity(e.message))
        }
    }

    interface OnResultListener {
        fun onSuccess(result: InstallReferrerEntity?)

        fun onFail(reason: InstallReferrerEntity?)

        fun onDisconnected()
    }

    companion object {
        private val TAG: String = InstallReferrerManager::class.java.simpleName
        private const val SP_NAME = "cash_ease_referrer"
        private const val SP_INSTALL_REFERRER = "install_referrer"
        private const val SP_REFERRER_CLICK_TIMESTAMP_SECONDS = "referrer_click_timestamp_seconds"
        private const val SP_INSTALL_BEGIN_TIMESTAMP_SECONDS = "install_begin_timestamp_seconds"
        private const val SP_REFERRER_CLICK_TIMESTAMP_SERVER_SECONDS =
            "referrer_click_timestamp_server_seconds"
        private const val SP_INSTALL_BEGIN_TIMESTAMP_SERVER_SECONDS =
            "install_begin_timestamp_server_seconds"
        private const val SP_INSTALL_VERSION = "install_version"
        private const val SP_GOOGLE_PLAY_INSTANT = "google_play_instant"
    }
}
