package com.druto.loan.cash.drutocash.collector

class MyInfo {
    var packageName: String? = null
    var appName: String? = null
    var channel: String? = null
    var versionName: String? = null
    var versionCode: Long = 0
    var isDebuggable: Boolean = false

    override fun toString(): String {
        return "AppInfo{" +
                "packageName='" + packageName + '\'' +
                ", appName='" + appName + '\'' +
                ", channel='" + channel + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                ", debuggable=" + isDebuggable +
                '}'
    }
}
