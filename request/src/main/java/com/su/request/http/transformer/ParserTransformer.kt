package com.su.request.http.transformer

import android.util.Log
import com.google.gson.Gson
import com.su.request.http.NetException
import com.su.request.http.NetRequest
import com.su.request.http.NetResponse
import com.su.request.http.ProgressResult
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.Response

/**
 * Created by su on 18-2-5.
 */

class ParserTransformer<T> : ObservableTransformer<ProgressResult<T, Response>, NetResponse<T>> {
    private val tag = ParserTransformer::class.java.simpleName
    override fun apply(upstream: Observable<ProgressResult<T, Response>>): ObservableSource<NetResponse<T>> {
        return upstream
                .filter { progressResult -> progressResult.type == ProgressResult.NetStatus.OVER }
                .map { progressResult -> NetResponse(progressResult.request, parseNetworkResponse(progressResult.request, progressResult.response.body!!.string())) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseNetworkResponse(request: NetRequest<T>, json: String): T {
        val type = request.typeToken.type
        try {
            return if (type == String::class.java) {
                json as T
            } else {
                Gson().fromJson(json, type)
            }
        } catch (e: RuntimeException) {
            Log.e(tag, "parse error, type=$type")
            throw NetException(NetException.PARSER, request.url, e.message, e)
        }

    }
}
