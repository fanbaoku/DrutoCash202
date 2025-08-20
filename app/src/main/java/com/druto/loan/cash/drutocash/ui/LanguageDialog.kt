package com.druto.loan.cash.drutocash.ui

import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.druto.loan.cash.drutocash.R
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper.configurationResources
import com.druto.loan.cash.drutocash.utils.LanguageUtil
import com.druto.loan.cash.drutocash.utils.LanguageUtil.changeAppLanguage
import com.druto.loan.cash.drutocash.utils.LanguageUtil.getDisplayNameByLanguageCode
import com.druto.loan.cash.drutocash.utils.SpHelper
import com.druto.loan.cash.drutocash.utils.SpHelper.mainSp


class LanguageDialog {
    private var mSelected = 0

    fun showDialog(context: Context, listener: OnFinishListener?): AlertDialog {
        val newResources = configurationResources
        val codeArray = newResources!!.getStringArray(R.array.language_code_list)
        val sp = mainSp
        val languageCode = sp!!.getString(
            SpHelper.COLUMN_LANGUAGE_CODE,
            LanguageUtil.DEFAULT_LANGUAGE
        )!!
        // 更新当前Context(Activity)的Configuration以便找到对应语言的资源
        val resources = context.resources
        resources.updateConfiguration(newResources.configuration, resources.displayMetrics)

        val currentLanguage = getUserCurrentLanguage(codeArray, languageCode)
        mSelected = currentLanguage

        val textArray = arrayOfNulls<String>(codeArray.size)
        for (i in codeArray.indices) {
            textArray[i] = getDisplayNameByLanguageCode(codeArray[i])
        }

        val builder = AlertDialog.Builder(context, R.style.CustomDialogTheme)
        builder.setTitle(R.string.select_language)
            .setCancelable(false)
            .setSingleChoiceItems(
                textArray, currentLanguage
            ) { _: DialogInterface?, which: Int -> mSelected = which }
            .setOnDismissListener { _: DialogInterface? ->
                listener?.onFinish()
            }
            .setPositiveButton(
                R.string.confirm
            ) { _: DialogInterface?, _: Int ->
                changeLanguage(
                    codeArray,
                    mSelected
                )
            }
            .setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int ->
                Log.d(
                    TAG,
                    "language=" + LanguageUtil.DEFAULT_LANGUAGE
                )
                mainSp
                    ?.edit()
                    ?.putString(SpHelper.COLUMN_LANGUAGE_CODE, LanguageUtil.DEFAULT_LANGUAGE)
                    ?.apply()
            }
        return builder.show()
    }

    interface OnFinishListener {
        fun onFinish()
    }

    companion object {
        val TAG: String = LanguageDialog::class.java.simpleName
        private fun changeLanguage(codeArray: Array<String>, selected: Int) {
            val language = codeArray[selected]
            changeAppLanguage(language)
            val sp = mainSp
            Log.d(TAG, "language=$language")
            sp!!.edit()
                .putString(SpHelper.COLUMN_LANGUAGE_CODE, language)
                .apply()
        }

        fun getUserCurrentLanguage(codeArray: Array<String>, languageCode: String?): Int {
            val length = codeArray.size
            for (i in 0 until length) {
                val code = codeArray[i]
                if (TextUtils.equals(code, languageCode)) {
                    return i
                }
            }
            return -1
        }
    }
}
