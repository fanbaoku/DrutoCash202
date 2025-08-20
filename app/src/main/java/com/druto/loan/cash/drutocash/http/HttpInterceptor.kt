package com.druto.loan.cash.drutocash.http

import android.text.TextUtils
import android.webkit.WebSettings
import com.druto.loan.cash.drutocash.DrutoCashApplication
import com.druto.loan.cash.drutocash.http.Util.makeSignature
import com.druto.loan.cash.drutocash.http.Util.md5
import com.druto.loan.cash.drutocash.http.Util.token
import com.druto.loan.cash.drutocash.utils.NetworkUtil.ipv4
import com.druto.loan.cash.drutocash.utils.SpHelper
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException

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
        val sessionId = DrutoCashApplication.APPLICATION_DATA.sessionId
        val signature = makeSignature(timestamp, token, md5, sessionId ?: "")
        val builder = request.url.newBuilder()
        builder.addQueryParameter("torment", sessionId)
            .addQueryParameter("monetary", timestamp)
            .addQueryParameter("ceasefire", signature)
            .addQueryParameter("headwaters", "0")
            .addQueryParameter("recycled", token)
        val build = request.newBuilder()
            .removeHeader("User-Agent")
            .addHeader("User-Agent", getDefaultUserAgent()!!)
            .url(builder.build()).build()
        return chain.proceed(build)
    }
    fun getDefaultUserAgent(): String? {

        var defUserAgent: String? = SpHelper.defUserAgent

        if (defUserAgent == null || TextUtils.isEmpty(defUserAgent)) {
            var ua: String?
            try {
                ua = WebSettings.getDefaultUserAgent(DrutoCashApplication.drutoCashApplication)
            } catch (e: Exception) {
                ua = System.getProperty("http.agent")
            }
            if (ua != null) {
                val sb = StringBuilder()
                var i = 0
                val length = ua.length
                while (i < length) {
                    val c = ua.get(i)
                    if (c <= '\u001f' || c >= '\u007f') {
                        sb.append(String.format("\\u%04x", c.code))
                    } else {
                        sb.append(c)
                    }
                    i++
                }
                defUserAgent = sb.toString()
                SpHelper.defUserAgent=defUserAgent
            }
        }
        return defUserAgent
    }
    companion object {
        val TAG: String = HttpInterceptor::class.java.simpleName
    }
}
