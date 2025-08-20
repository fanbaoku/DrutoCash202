package com.druto.loan.cash.drutocash.model

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.MutableLiveData
import com.adjust.sdk.Adjust
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.druto.loan.cash.advertisingid.AdvertisingIdManager
import com.druto.loan.cash.advertisingid.GaidEntity
import com.druto.loan.cash.firebase.FirebaseAnalyticsManger
import com.druto.loan.cash.firebase.FirebaseInstanceIdEntity
import com.druto.loan.cash.installreferrer.InstallReferrerEntity
import com.druto.loan.cash.installreferrer.InstallReferrerManager
import com.druto.loan.cash.drutocash.DrutoCashApplication
import com.druto.loan.cash.drutocash.Url
import com.druto.loan.cash.drutocash.collector.MyInfo
import com.druto.loan.cash.drutocash.collector.BatteryInfo
import com.druto.loan.cash.drutocash.collector.DeviceBaseInfoCollector
import com.druto.loan.cash.drutocash.collector.GeneralInfoCollector
import com.druto.loan.cash.drutocash.collector.KeyAppsCollector
import com.druto.loan.cash.drutocash.collector.LocaleInfoCollector
import com.druto.loan.cash.drutocash.collector.PermissionCollector
import com.druto.loan.cash.drutocash.collector.ScreenInfoCollector
import com.druto.loan.cash.drutocash.collector.StorageInfoCollector
import com.druto.loan.cash.drutocash.entity.FirstEntity
import com.druto.loan.cash.drutocash.utils.ToastUtil.showToast
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper
import com.druto.loan.cash.drutocash.http.ParserUtil
import com.druto.loan.cash.drutocash.utils.SpHelper
import com.su.request.http.HttpResult
import com.su.request.http.NetRequest
import com.su.request.http.NullObject
import com.su.request.http.function.BusinessFunction
import com.su.request.http.observer.BaseObserver
import com.su.request.http.transformer.ParserTransformer
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SplashModel(private val mObservers: List<BaseObserver<*>>) {
    private var mUuid: String? = null // 0
    private var mMyInfo: MyInfo? = null
    private var mDeviceBaseInfo: List<Any?>? = null // 4.0
    private var mGeneralInfo: List<Any?>? = null // 4.1
    private var mLocaleInfo: List<String>? = null // 4.2
    private var mScreenInfo: List<Any>? = null // 4.3
    private var mStorageInfo: List<Long>? = null // 4.4
    private var mBatteryInfo: MutableList<Any>? = null // 4.5
    private var mKeyAppsVersion: List<String>? = null // 4.6
    private var mPermissionInfo: List<Int>? = null // 5
    private var mAllGranted = false // 6
    val directDataDoneLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    // 1st GAID
    val gaidEntityLiveData: MutableLiveData<GaidEntity> = MutableLiveData()
    val adidLiveData: MutableLiveData<String?> = MutableLiveData()
    val firebaseInstanceIdLiveData: MutableLiveData<FirebaseInstanceIdEntity> = MutableLiveData()

    // 2nd 安装来源 6项数据/错误信息
    val installReferrerLiveData: MutableLiveData<InstallReferrerEntity?> = MutableLiveData()

    // 4.5 电池信息
    val mBatteryInfoLiveData: MutableLiveData<BatteryInfo> = MutableLiveData()

    fun requestInitial(callback: InitialRequestCallback<FirstEntity?>) {
        val host = DrutoCashApplication.APPLICATION_DATA.getHost()
        Log.d(TAG, "requestInitial host=$host")
        if (host == null) {
            showToast(GeneralInfoHelper.context, "No usable host")
            return
        }
        System.out.println("json--------->")
        // prepare http data
        val json = prepareJson()
        System.out.println("json--------->"+json)
        val requestBody = ParserUtil.prepareRequestBody(json, true)
        // request initial http
        NetRequest.create<HttpResult<FirstEntity?>>(
            host + Url.STARTUP,
            object : TypeToken<HttpResult<FirstEntity?>?>() {
            })
            .method("POST")
            .build(DrutoCashApplication.client, requestBody)
            .compose(ParserTransformer())
            .map<Any>(object : BusinessFunction<HttpResult<FirstEntity?>?, Any?>() {
                override fun onSuccess(result: HttpResult<FirstEntity?>?): NullObject {
                    onInitialSuccess(result)
                    callback.onSuccess(result)
                    return NullObject.NULL_OBJECT
                }
            })
            .retry(3) // 最多执行1+3=4次
            .safeSubscribe(object : BaseObserver<Any?>(mObservers) {
                override fun onComplete() {
                    super.onComplete()
                    Log.w(TAG, "requestInitialComplete")
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    Log.w(TAG, "requestInitialError", e)
                    callback.onError(e)
                }
            })
    }

    private fun onInitialSuccess(result: HttpResult<FirstEntity?>?) {
        val context = GeneralInfoHelper.context
        Log.d(TAG, "initial = $result")
        if (result == null) {
            showToast(GeneralInfoHelper.context, "init failed")
            return
        }
        if (result.hum != 0) {
            showToast(context, result.inverse)
            return
        }
        val entity = result.silently
        if (entity == null) {
            showToast(context, "init failed")
            return
        }
        val data = DrutoCashApplication.APPLICATION_DATA
        data.mainH5Url = entity.h5MainUrl
        data.sessionId = entity.jeans ?: ""
        data.separation = entity.genesis
        val armament = entity.powdery
        data.upgradeInfo = armament
        DrutoCashApplication.getFCMTokenFromServer()
        val whatsapp = entity.powdery!!.calculation
        if (whatsapp == null || whatsapp.trim { it <= ' ' }.isEmpty()) {
            Log.w(TAG, "no whatsapp account found")
        }
        SpHelper.whatsapp = whatsapp
    }

    private fun prepareJson(): String {
        val list: MutableList<Any?> = ArrayList()
        list.add(mUuid) // 0 uuid
        list.add(gaidInfoList) // 1 gaid
        list.add(installSourceList) // 2 referrer
        list.add(appInfoList) // 3 app
        list.add(deviceInfoList) // 4 device info
        list.add(mPermissionInfo) // 5 permission
        list.add(if (mAllGranted) 1 else 0) // 6 是否完成授权
        val gson = Gson()
        return gson.toJson(list)
    }

    fun synchronizeData() {
        Observable.create<Any?>{
            prepareUuid()
            prepareAppInfo()
            prepareDeviceInfo()
            preparePermissionInfo()
            directDataDoneLiveData.postValue(true)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .safeSubscribe(object : BaseObserver<Any?>(mObservers) {
                override fun onComplete() {
                    super.onComplete()
                    Log.w(TAG, "getInfoListComplete")
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    Log.w(TAG, "getInfoListError")
                }
            })
    }

    fun onBatteryChanged(intent: Intent) {
        val batteryInfo = BatteryInfo()
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
        when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> {
                batteryInfo.plugged = 1
                val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) {
                    batteryInfo.usbPlugged = 1
                } else {
                    batteryInfo.acPlugged = 1
                }
            }

            BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {
                batteryInfo.plugged = 0
                batteryInfo.usbPlugged = 0
                batteryInfo.acPlugged = 0
            }

            BatteryManager.BATTERY_STATUS_FULL -> {}
            BatteryManager.BATTERY_STATUS_UNKNOWN -> {}
            else -> {}
        }
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
        batteryInfo.level = level * 100 / scale
        batteryInfo.capacity = BatteryInfo.getBatteryCapacity(GeneralInfoHelper.context)
        val batteryLevel = (batteryInfo.capacity * (level * 100 / scale) / 100)
        batteryInfo.current = batteryLevel
        mBatteryInfoLiveData.postValue(batteryInfo)
        if (mBatteryInfo == null) {
            mBatteryInfo = ArrayList()
            mBatteryInfo?.add(batteryInfo.plugged)
            mBatteryInfo?.add(batteryInfo.level)
            mBatteryInfo?.add(batteryInfo.usbPlugged)
            mBatteryInfo?.add(batteryInfo.acPlugged)
            mBatteryInfo?.add(batteryInfo.current)
            mBatteryInfo?.add(batteryInfo.capacity)
        }
    }

    // 0
    private fun prepareUuid() {
        mUuid = DrutoCashApplication.APPLICATION_DATA.uuid
    }

    // 3rd
    private fun prepareAppInfo() {
        val context = GeneralInfoHelper.context
        mMyInfo = MyInfo()
        mMyInfo!!.packageName = context.packageName
        val pm = context.packageManager
        try {
            val packageInfo = pm.getPackageInfo(mMyInfo?.packageName ?: "", 0)
            mMyInfo!!.versionCode =
                PackageInfoCompat.getLongVersionCode(packageInfo)
            mMyInfo!!.versionName = packageInfo.versionName
            mMyInfo!!.channel = "GooglePlay"
            val applicationInfo = packageInfo.applicationInfo ?: return
            mMyInfo!!.appName = applicationInfo.loadLabel(pm).toString()
            mMyInfo!!.isDebuggable =
                ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)
                        == ApplicationInfo.FLAG_DEBUGGABLE)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, e)
        }
        Log.d(TAG, "appInfo=$mMyInfo")
    }

    // 4th
    private fun prepareDeviceInfo() {
        val context = GeneralInfoHelper.context
        mDeviceBaseInfo = DeviceBaseInfoCollector.getInfoList(context) // 4.0
        mGeneralInfo = GeneralInfoCollector.getInfoList(context) // 4.1
        mLocaleInfo = LocaleInfoCollector.getInfoList(context) // 4.2
        mScreenInfo = ScreenInfoCollector.getInfoList(context) // 4.3
        mStorageInfo = StorageInfoCollector.getInfoList(context) // 4.3
        // BatteryStatus 动态获取，使用LiveData // 4.5
        mKeyAppsVersion = KeyAppsCollector.getInfoList(context) // 4.6
    }

    // 5th and 6th
    private fun preparePermissionInfo() {
        val context = GeneralInfoHelper.context
        mPermissionInfo = PermissionCollector.getInfoList(context)
        mAllGranted = PermissionCollector.allGranted(mPermissionInfo!!)
    }

    fun init() {
        val context = GeneralInfoHelper.context
        val installReferrerManager = InstallReferrerManager()
        installReferrerManager.tryStartConnection(context,
            object : InstallReferrerManager.OnResultListener {
                override fun onSuccess(result: InstallReferrerEntity?) {
                    Log.d(TAG, "install referrer result = $result")
                    val data = DrutoCashApplication.APPLICATION_DATA
                    data.installReferrer = result!!.installReferrer
                    installReferrerLiveData.postValue(result)
                }

                override fun onFail(reason: InstallReferrerEntity?) {
                    installReferrerLiveData.postValue(reason)
                }

                override fun onDisconnected() {}
            })

        val advertisingIdManager = AdvertisingIdManager()
        Observable.create { emitter: ObservableEmitter<Any?>? ->
            advertisingIdManager.getAdvertisingId(context,
                object : AdvertisingIdManager.OnFinishListener {
                    override fun onSuccess(
                        id: String?,
                        isLimitAdTrackingEnabled: Boolean
                    ) {
                        Log.d(
                            TAG, ("id=" + id + ", isLimitAdTrackingEnabled="
                                    + isLimitAdTrackingEnabled)
                        )
                        val data = DrutoCashApplication.APPLICATION_DATA
                        data.advertisingId = id
                        gaidEntityLiveData.postValue(GaidEntity(id, null))
                    }

                    override fun onError(msg: String?, error: Throwable?) {
                        Log.w(TAG, msg!!)
                        Log.w(TAG, error)
                        gaidEntityLiveData.postValue(GaidEntity(null, msg))
                    }
                })
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .safeSubscribe(BaseObserver(mObservers))

        val firebaseAnalyticsManger = FirebaseAnalyticsManger()
        firebaseAnalyticsManger.getFirebaseInstanceId(context,
            object : FirebaseAnalyticsManger.OnResultListener {
                override fun onSuccess(result: String?) {
                    Log.d(TAG, "getFirebaseInstanceId=$result")
                    firebaseInstanceIdLiveData.postValue(
                        FirebaseInstanceIdEntity(result, null)
                    )
                }

                override fun onFailed(reason: String?) {
                    Log.w(TAG, "getFirebaseInstanceIdFailed=$reason")
                    firebaseInstanceIdLiveData.postValue(
                        FirebaseInstanceIdEntity(null, reason)
                    )
                }
            })

        Observable.interval(500, TimeUnit.MILLISECONDS)
            .takeUntil { ignored: Long? -> adidLiveData.value != null }
            .flatMap { ignored: Long? ->
                Observable.just(
                    performTask()
                )
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .safeSubscribe(BaseObserver(mObservers))
    }

    private fun performTask(): String {
        val adid = Adjust.getAdid()
        Log.d(TAG, "try to get adid=$adid")
        if (adid != null) {
            adidLiveData.postValue(adid)
            return "Task succeeded!"
        }
        return "Task failed, retrying..."
    }

    private val deviceInfoList: List<Any?>
        get() {
            val lists: MutableList<Any?> =
                ArrayList()
            lists.add(mDeviceBaseInfo) // 4.0
            lists.add(mGeneralInfo) // 4.1
            lists.add(mLocaleInfo) // 4.2
            lists.add(mScreenInfo) // 4.3
            lists.add(mStorageInfo) // 4.4
            lists.add(mBatteryInfo) // 4.5
            lists.add(mKeyAppsVersion) // 4.6
            Log.d(TAG, "mBatteryInfo=$mBatteryInfo")
            return lists
        }

    private val appInfoList: List<Any?>
        get() {
            val list: MutableList<Any?> = ArrayList()
            list.add(mMyInfo?.packageName)
            list.add(mMyInfo?.appName)
            list.add(mMyInfo?.channel)
            list.add(mMyInfo?.versionName)
            list.add(mMyInfo?.versionCode)
            return list
        }

    private val installSourceList: List<Any?>
        get() {
            val lists: MutableList<Any?> =
                ArrayList()
            val list: MutableList<Any?> =
                ArrayList()
            val installReferrer = installReferrerLiveData.value
            if (installReferrer != null) {
                list.add(installReferrer!!.installReferrer)
                list.add(installReferrer.referrerClickTimestampSeconds)
                list.add(installReferrer.installBeginTimestampSeconds)
                list.add(installReferrer.referrerClickTimestampServerSeconds)
                list.add(installReferrer.installBeginTimestampServerSeconds)
                list.add(installReferrer.installVersion)
                list.add(if (installReferrer.isGooglePlayInstantParam) 1 else 0)
                lists.add(list)
                lists.add(installReferrer.error)
            } else {
                lists.add(null)
                lists.add("no data")
            }
            return lists
        }

    private val gaidInfoList: List<Any?>
        get() {
            val list: MutableList<Any?> =
                ArrayList()
            val gaidEntity = gaidEntityLiveData.value
            if (gaidEntity!=null){
                list.add(gaidEntity!!.id)
                list.add(gaidEntity.error)
            }else{
                list.add(null)
                list.add("no data")
            }
            val firebaseInstanceId =
                firebaseInstanceIdLiveData.value
            list.add(firebaseInstanceId!!.id)
            list.add(adidLiveData.value)
            Log.d(TAG, "adid=" + adidLiveData.value)
            return list
        }

    interface InitialRequestCallback<T> {
        fun onSuccess(result: HttpResult<T?>?)
        fun onError(e: Throwable)
    }

    companion object {
        val TAG: String = SplashModel::class.java.simpleName
    }
}
