package com.cash.ease.money.cashease.http

import android.text.TextUtils
import android.util.Log
import com.cash.ease.money.cashease.CashEaseApplication
import com.cash.ease.money.cashease.http.Util.compress
import com.cash.ease.money.cashease.http.Util.encrypt
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

object ParserUtil {
    val TAG: String = ParserUtil::class.java.simpleName

    fun prepareRequestBody(json: String?, isCompression: Boolean): RequestBody {
        val encrypt = prepareEncryptString(json, isCompression)
        return RequestBody.create("application/octet-stream".toMediaTypeOrNull(), encrypt!!
        )
    }

    fun parerPseudoJson(pseudoJson: String): Map<String, Any?> {
        val map: MutableMap<String, Any?> = HashMap()
        val properties = pseudoJson.replace("{", "")
            .replace("}", "")
            .split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (property in properties) {
            val index = property.indexOf("=")
            if (index < 0) {
                continue
            }
            val key = property.substring(0, index)
            var value = if (index + 1 >= property.length) {
                null
            } else {
                property.substring(index + 1)
            }
            map[key] = value
        }
        return map
    }

    fun prepareEncryptString(jsonString: String?, isCompression: Boolean): ByteArray? {
        var data: ByteArray? = ByteArray(0)
        if (jsonString != null && !TextUtils.isEmpty(jsonString)) {
            data = jsonString.toByteArray()
            if (isCompression) {
                data = compress(data)
            }
            data = encrypt(data, CashEaseApplication.SECURITY_KEY)
        }
        val preview = String(data!!)
        Log.i(TAG, "previewEncryptString=$preview")
        return data
    }
}
