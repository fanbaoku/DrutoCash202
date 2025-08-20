package com.cash.ease.money.cashease.http

import android.util.Log
import com.cash.ease.money.cashease.CashEaseApplication
import com.cash.ease.money.cashease.utils.NetworkUtil.ipv4
import com.cash.ease.money.cashease.http.Util.makeSignature
import com.cash.ease.money.cashease.http.Util.md5
import com.cash.ease.money.cashease.http.Util.token
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer

class HttpInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        //获取原始的Request请求
        val request: Request = chain.request()
        val body = request.body

        var md5 = ""
        val timestamp = System.currentTimeMillis().toString()
        val ip = ipv4
        val token = token

        // 有请求数据时, 计算 md5 值
        if (body != null && body.contentLength() > 0) {
            val buffer = Buffer()
            body.writeTo(buffer)
            md5 = md5(buffer.readByteArray())
        }

        // 计算签名
        val sessionId = CashEaseApplication.APPLICATION_DATA.sessionId
        val signature = makeSignature(timestamp, token, md5, sessionId ?: "")
        val cookie = ("mark=" + sessionId + ";terrorism=" + timestamp + ";interior="
                + signature + ";trout=" + token + ";idealize= 0")
        Log.d(TAG, "ip=$ip, cookie=$cookie")
        val build = request.newBuilder()
            .addHeader("cookie", cookie)
            .build()
        return chain.proceed(build)
    }

    companion object {
        val TAG: String = HttpInterceptor::class.java.simpleName
    }
}
