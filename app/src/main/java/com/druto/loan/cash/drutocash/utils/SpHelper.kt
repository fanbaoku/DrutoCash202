package com.druto.loan.cash.drutocash.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by su on 18-1-2.
 */
object SpHelper {
    val TAG: String = SpHelper::class.java.simpleName
    const val NAME: String = "druto_cash"

    const val COLUMN_LANGUAGE_CODE: String = "druto_cash_edoc_egaugnal"
    const val KEY_DIUU: String = "druto_cash_diuu"
    const val KEY_DEFUSERAGENT: String = "druto_cash_defUserAgent"
    const val KEY_TSOH: String = "druto_cash_tsoh"
    const val KEY_PPASTAHW: String = "druto_cash_ppastahw"

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

    var defUserAgent: String?
        get() = mainSp!!.getString(KEY_DEFUSERAGENT, null)
        set(uuid) {
            mainSp!!.edit().putString(KEY_DEFUSERAGENT, uuid).apply()
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
