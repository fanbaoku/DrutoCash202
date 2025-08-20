package com.cash.ease.money.cashease.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by su on 18-1-2.
 */
object SpHelper {
    val TAG: String = SpHelper::class.java.simpleName
    const val NAME: String = "cash_ease"

    const val COLUMN_LANGUAGE_CODE: String = "cash_ease_edoc_egaugnal"
    const val KEY_DIUU: String = "cash_ease_diuu"
    const val KEY_TSOH: String = "cash_ease_tsoh"
    const val KEY_PPASTAHW: String = "cash_ease_ppastahw"

    var mainSp: SharedPreferences? = null
        private set

    fun initSharedPreferences(context: Context) {
        mainSp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    var uuid: String?
        get() = mainSp!!.getString(KEY_DIUU, null)
        set(uuid) {
            mainSp!!.edit().putString(KEY_DIUU, uuid).apply()
        }

    var isFirst:Boolean
        get() = mainSp!!.getBoolean("isFirst",true)
        set(isFirst){
            mainSp!!.edit().putBoolean("isFirst",isFirst).apply()
        }
    var host: String?
        get() = mainSp!!.getString(KEY_TSOH, null)
        set(host) {
            mainSp!!.edit().putString(KEY_TSOH, host).apply()
        }

    var whatsapp: String?
        get() = mainSp!!.getString(KEY_PPASTAHW, null)
        set(whatsapp) {
            mainSp!!.edit().putString(KEY_PPASTAHW, whatsapp).apply()
        }
}
