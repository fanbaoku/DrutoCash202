package com.cash.ease.money.cashease.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.cash.ease.money.firebase.FirebaseDatabaseManager
import com.cash.ease.money.firebase.FirebaseDatabaseManager.getHost
import com.cash.ease.money.cashease.CashEaseApplication
import com.cash.ease.money.cashease.Url
import com.cash.ease.money.cashease.entity.HostList
import com.cash.ease.money.cashease.service.MyEventUploader.uploadFirebaseHostFailEvent
import com.cash.ease.money.cashease.service.MyEventUploader.uploadHostFailEvent
import com.cash.ease.money.cashease.http.ParserUtil
import com.cash.ease.money.cashease.utils.SpHelper
import com.su.request.http.HttpResult
import com.su.request.http.NetRequest
import com.su.request.http.function.BusinessFunction
import com.su.request.http.observer.BaseObserver
import com.su.request.http.transformer.ParserTransformer
import io.reactivex.Observable
import okhttp3.FormBody

/**
 * 流程
 * 1. 从SP获取缓存IP
 * 2. 如果有，则ping，没有则从服务端请求
 */
class HostModel {
    val hostRequestLiveData: MutableLiveData<Boolean?> = MutableLiveData(null)

    fun fetchUsableIp(observers: List<BaseObserver<*>>) {
        tryHostFromCached().safeSubscribe(object : BaseObserver<Boolean?>(observers) {
            override fun onNext(result: Boolean) {
                super.onNext(result)
                if (result) {
                    Log.d(TAG, "use CachedIp")
                    hostRequestLiveData.postValue(true)
                    return
                }
                Log.i(TAG, "try get ip/host from internet")
                val now = System.currentTimeMillis()
                // WARNING: firebase需要翻墙，获取数据较慢，可能会出现onComplete先执行，或者getHost长时间不返回数据的情况
                getHost(object : FirebaseDatabaseManager.OnResultListener {
                    override fun onSuccess(result: String?) {
                        Log.d(
                            TAG,
                            "hostsString from firebase, elapse=" + ((System.currentTimeMillis() - now) / 1000) + "s"
                        )
                        if (result == null || result.trim { it <= ' ' }.isEmpty()) {
                            tryHostFromRemoteJson(observers, hostFromTencent)
                            return
                        }
                        val map = ParserUtil.parerPseudoJson(result)
                        val ipListType = TypeToken.get(
                            HostList::class.java
                        ).type
                        val gson = Gson()
                        val hostList = gson.fromJson<HostList>(gson.toJson(map), ipListType)
                        val list = hostList.cashEaseHostList
                        Log.d(TAG, "firebase ip list=$list")
                        if (list.isEmpty()) {
                            tryHostFromRemoteJson(observers, hostFromTencent)
                            return
                        }
                        val ipListObservable = pingHosts(list)
                            .flatMap { bool: Boolean ->
                                if (bool) {
                                    return@flatMap Observable.empty<List<String>>()
                                } else {
                                    uploadFirebaseHostFailEvent()
                                    return@flatMap hostFromTencent
                                }
                            }
                        tryHostFromRemoteJson(observers, ipListObservable)
                    }

                    override fun onFail(reason: String?) {
                        uploadFirebaseHostFailEvent()
                    }
                })
            }

            override fun onComplete() {
                super.onComplete()
                Log.w(TAG, "tryHostFromCachedComplete")
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                Log.w(TAG, "tryHostFromCachedError")
            }

            override fun observerToastable(): Boolean {
                return false
            }
        })
    }

    private fun tryHostFromRemoteJson(
        observers: List<BaseObserver<*>>,
        hostListObservable: Observable<List<String>>
    ) {
        Log.i(TAG, "tryHostFromRemoteJson")
        hostListObservable.flatMap { ipList: List<String> ->
            Log.d(
                TAG,
                "loop hosts from alibaba list=$ipList"
            )
            pingHosts(ipList)
        }.doOnNext { aBoolean: Boolean ->
            if (aBoolean) {
                return@doOnNext
            }
            throw RuntimeException("fail to fetch host list")
        }.safeSubscribe(object : BaseObserver<Boolean?>(observers) {
            override fun onComplete() {
                super.onComplete()
                hostRequestLiveData.postValue(true)
                Log.d(TAG, "prepare http init")
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                hostRequestLiveData.postValue(false)
                uploadHostFailEvent()
                Log.w(TAG, "tryHostFromRemoteJsonError")
            }

            override fun observerToastable(): Boolean {
                return false
            }
        })
    }

    private fun tryHostFromCached(): Observable<Boolean> {
        val cachedHost = SpHelper.host
        Log.d(TAG, "tryHostFromCached cachedHost=$cachedHost")
        if (!cachedHost.isNullOrEmpty()) {
            Log.d(
                TAG,
                "tryHostFromCached pingCachedIp=$cachedHost"
            )
            return pingHost(cachedHost)
        }
        return Observable.just(false)
    }

    // 尝试缓存IP
    private fun pingHost(host: String): Observable<Boolean> {
        val url = host + Url.PING_HOST
        Log.d(TAG, "callMethod PingHost = $host")
        val observable =
            NetRequest.create<HttpResult<*>>(url, object : TypeToken<HttpResult<*>?>() {})
                .method("POST")
                .build(CashEaseApplication.client, FormBody.Builder().build())
                .compose(ParserTransformer())
                .map<Boolean>(object : BusinessFunction<HttpResult<*>?, Boolean?>() {
                    override fun onSuccess(result: HttpResult<*>?): Boolean {
                        val effectiveness = result?.total
                        if (effectiveness == 0) {
                            Log.d(TAG, "ping host success = $host")
                            CashEaseApplication.APPLICATION_DATA.setHost(host)
                            return true
                        } else {
                            Log.d(
                                TAG,
                                "ping host error = " + host + ", cautiously=" + result?.swell
                            )
                            return false
                        }
                    }
                })
        return observable.onErrorResumeNext(Observable.just(false))
    }

    private fun pingHosts(ipList: List<String>): Observable<Boolean> {
        val list: List<String> = ArrayList(ipList)
        val observables: MutableList<Observable<Boolean>> = ArrayList()
        for (host in list) {
            val finalHost = if (!host.startsWith("http://") && !host.startsWith("https://")) {
                "https://$host"
            } else {
                host
            }
            val pingObservable = pingHost(finalHost)
            observables.add(pingObservable)
        }
        return Observable.fromIterable(observables)
            .concatMap { obs: Observable<Boolean>? -> obs } // 按顺序订阅
            .takeUntil { aBoolean: Boolean ->
                Log.w(
                    TAG,
                    "aBoolean=$aBoolean"
                )
                aBoolean
            }
            .lastOrError()
            .toObservable()
    }

    private val hostFromTencent: Observable<List<String>>
        // 腾讯
        get() = getHostsFromRemoteJson(
            "https://sdfsf806dsfdssf-1313125604.cos.eu-frankfurt.myqcloud.com/806.json"
        )

    private fun getHostsFromRemoteJson(url: String): Observable<List<String>> {
        return NetRequest.create(url, object : TypeToken<HostList?>() {
        })
            .method("GET")
            .build()
            .compose(ParserTransformer())
            .map(object : BusinessFunction<HostList?, List<String>>() {
                override fun onSuccess(hostList: HostList?): List<String> {
                    return hostList?.cashEaseHostList ?: ArrayList()
                }
            })
    }

    companion object {
        val TAG: String = HostModel::class.java.simpleName
    }
}
