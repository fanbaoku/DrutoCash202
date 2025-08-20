package com.druto.loan.cash.drutocash.collector

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.os.ConfigurationCompat
import java.util.TimeZone


object LocaleInfoCollector {

    fun getInfoList(context: Context): List<String> {
        val list: MutableList<String> = ArrayList()
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val locales = context.resources.configuration.getLocales()
            locales.get(0)
        } else {
             context.resources.configuration.locale
        }

        list.add(locale.isO3Language)
        list.add(locale.displayLanguage)
        val listCompat = ConfigurationCompat.getLocales(Resources.getSystem().configuration)
        if (!listCompat.isEmpty) {
            list.add(listCompat[0]?.isO3Country ?: "")
        } else {
            list.add("")
        }

        list.add(locale.language)
        list.add(TimeZone.getDefault().id)
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        list.add(tm?.simCountryIso ?: "")
        list.add(tm?.networkOperatorName ?: "")
        Log.d("LocaleInfoCollector", list.toString())
        return list
    }
}
