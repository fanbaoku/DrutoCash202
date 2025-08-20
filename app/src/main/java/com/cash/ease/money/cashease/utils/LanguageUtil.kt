package com.cash.ease.money.cashease.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.core.os.ConfigurationCompat
import com.cash.ease.money.cashease.utils.SpHelper.mainSp
import java.util.Locale

object LanguageUtil {
    const val DEFAULT_LANGUAGE: String = "en"

    @TargetApi(Build.VERSION_CODES.N)
    fun updateResources(context: Context, code: String): Context {
        val locale = getLocaleByLanguageCode(code)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    fun changeAppLanguage(code: String) {
        val context = GeneralInfoHelper.context
        val locale = getLocaleByLanguageCode(code)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        }
        val newContext = context.createConfigurationContext(configuration)
        GeneralInfoHelper.configurationResources = newContext.resources
    }

    private fun getLocaleByLanguageCode(code: String): Locale? {
        val resources = GeneralInfoHelper.context.resources
        val configuration = resources.configuration
        configuration.setLocale(Locale.forLanguageTag(code))
        return ConfigurationCompat.getLocales(configuration)[0]
    }

    fun getDisplayNameByLanguageCode(code: String): String {
        val locale = Locale.forLanguageTag(code)
        return locale.getDisplayName(locale)
    }

    fun getUpdatedResourcesContext(newBase: Context): Context {
        val context: Context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val sp = mainSp
            val language = sp!!.getString(SpHelper.COLUMN_LANGUAGE_CODE, DEFAULT_LANGUAGE)!!
            context = updateResources(newBase, language)
        } else {
            context = newBase
        }
        return context
    }
}
