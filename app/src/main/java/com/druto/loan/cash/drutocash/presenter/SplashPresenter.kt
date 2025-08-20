package com.druto.loan.cash.drutocash.presenter

import android.content.Intent
import android.util.Log
import com.druto.loan.cash.drutocash.entity.FirstEntity
import com.druto.loan.cash.drutocash.model.SplashModel
import com.druto.loan.cash.drutocash.model.SplashModel.InitialRequestCallback
import com.druto.loan.cash.drutocash.model.HostModel
import com.druto.loan.cash.drutocash.view.SplashView
import com.su.request.http.HttpResult

class SplashPresenter(private val view: SplashView, private val model: SplashModel) {
    private val mHostModel = HostModel()

    fun init() {
        prepareLiveData()
        model.init()
        model.synchronizeData()
    }

    fun fetchUsableIp() {
        mHostModel.fetchUsableIp(view.getActivityObservers())
    }

    fun onBatteryChanged(intent: Intent) {
        model.onBatteryChanged(intent)
    }

    private fun prepareLiveData() {
        view.observeLiveData(model.adidLiveData, "Adid")
        view.observeLiveData(model.gaidEntityLiveData, "Gaid")
        view.observeLiveData(model.firebaseInstanceIdLiveData, "FirebaseInstanceId")
        view.observeLiveData(model.installReferrerLiveData, "Referrer")
        view.observeLiveData(model.mBatteryInfoLiveData, "Battery")
        view.observeLiveData(model.directDataDoneLiveData, "Direct")
        view.observeHostRequestLiveData(mHostModel.hostRequestLiveData, "Direct")
    }

    fun checkData(data: Any, from: String) {
        Log.d(TAG, "checkData=$data, from=$from")
        if (java.lang.Boolean.FALSE == model.directDataDoneLiveData.value) {
            return
        }
//        if (model.gaidEntityLiveData.value == null
//            || model.gaidEntityLiveData.value!!.id == null
//        ) {
//            return
//        }
        if (model.adidLiveData.value == null) {
            return
        }
        if (model.firebaseInstanceIdLiveData.value == null
            || model.firebaseInstanceIdLiveData.value!!.id == null
        ) {
            return
        }
//        if (model.installReferrerLiveData.value == null) {
//            return
//        }
        if (model.mBatteryInfoLiveData.value == null) {
            return
        }
        if (java.lang.Boolean.TRUE != mHostModel.hostRequestLiveData.value) {
            return
        }
        model.requestInitial(object : InitialRequestCallback<FirstEntity?> {
            override fun onSuccess(result: HttpResult<FirstEntity?>?) {
                view.onRequestInitialSuccess(result)
            }

            override fun onError(e: Throwable) {
                view.onRequestInitialFail(e)
            }
        })
    }

    companion object {
        val TAG: String = SplashPresenter::class.java.simpleName
    }
}
