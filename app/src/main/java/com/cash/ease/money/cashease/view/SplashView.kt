package com.cash.ease.money.cashease.view

import androidx.lifecycle.LiveData
import com.cash.ease.money.cashease.entity.FirstEntity
import com.su.request.http.HttpResult
import com.su.request.http.observer.BaseObserver

interface SplashView {
    fun observeLiveData(liveData: LiveData<*>, from: String)

    fun observeHostRequestLiveData(liveData: LiveData<*>, from: String)

    fun getActivityObservers(): List<BaseObserver<*>>

    fun onRequestInitialSuccess(result: HttpResult<FirstEntity?>?)

    fun onRequestInitialFail(e: Throwable)
}
