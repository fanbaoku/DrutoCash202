package com.cash.ease.money.installreferrer

class InstallReferrerEntity {
    var installReferrer: String? = null
        private set
    var referrerClickTimestampSeconds: Long = 0
        private set
    var installBeginTimestampSeconds: Long = 0
        private set
    var referrerClickTimestampServerSeconds: Long = 0
        private set
    var installBeginTimestampServerSeconds: Long = 0
        private set
    var installVersion: String? = null
        private set
    var isGooglePlayInstantParam: Boolean = false
        private set
    var error: String? = null
        private set

    constructor(
        installReferrer: String?, referrerClickTimestampSeconds: Long,
        installBeginTimestampSeconds: Long, referrerClickTimestampServerSeconds: Long,
        installBeginTimestampServerSeconds: Long, installVersion: String?,
        googlePlayInstantParam: Boolean
    ) {
        this.installReferrer = installReferrer
        this.referrerClickTimestampSeconds = referrerClickTimestampSeconds
        this.installBeginTimestampSeconds = installBeginTimestampSeconds
        this.referrerClickTimestampServerSeconds = referrerClickTimestampServerSeconds
        this.installBeginTimestampServerSeconds = installBeginTimestampServerSeconds
        this.installVersion = installVersion
        isGooglePlayInstantParam = googlePlayInstantParam
    }

    constructor(error: String?) {
        this.error = error
    }

    override fun toString(): String {
        return "InstallReferrerEntity{" +
                "mInstallReferrer='" + installReferrer + '\'' +
                ", mReferrerClickTimestampSeconds=" + referrerClickTimestampSeconds +
                ", mInstallBeginTimestampSeconds=" + installBeginTimestampSeconds +
                ", mReferrerClickTimestampServerSeconds=" + referrerClickTimestampServerSeconds +
                ", mInstallBeginTimestampServerSeconds=" + installBeginTimestampServerSeconds +
                ", mInstallVersion='" + installVersion + '\'' +
                ", mGooglePlayInstantParam=" + isGooglePlayInstantParam +
                ", error='" + error + '\'' +
                '}'
    }
}
