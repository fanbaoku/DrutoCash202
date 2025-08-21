package com.druto.loan.cash.drutocash.ui.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.druto.loan.cash.drutocash.BuildConfig
import com.druto.loan.cash.drutocash.DrutoCashApplication
import com.druto.loan.cash.drutocash.DrutoCashApplication.Companion.uploadAllInfo
import com.druto.loan.cash.drutocash.DrutoCashApplication.Companion.uploadEvent
import com.druto.loan.cash.drutocash.DrutoCashApplication.OnAllUploadResultListener
import com.druto.loan.cash.drutocash.R
import com.druto.loan.cash.drutocash.Url
import com.druto.loan.cash.drutocash.activity.CameraActivity
import com.druto.loan.cash.drutocash.activity.MainActivity
import com.druto.loan.cash.drutocash.activity.RequirePermissionActivity
import com.druto.loan.cash.drutocash.collector.CalendarCollector.EventParam
import com.druto.loan.cash.drutocash.collector.CalendarCollector.addOrUpdateCalendarEvent
import com.druto.loan.cash.drutocash.collector.PermissionEntity
import com.druto.loan.cash.drutocash.collector.SomeInfoCollector.isGpsEnabled
import com.druto.loan.cash.drutocash.entity.ImageResult
import com.druto.loan.cash.drutocash.http.ParserUtil.prepareRequestBody
import com.druto.loan.cash.drutocash.http.Util.encrypt
import com.druto.loan.cash.drutocash.http.Util.makeSignature
import com.druto.loan.cash.drutocash.http.Util.md5
import com.druto.loan.cash.drutocash.http.Util.token
import com.druto.loan.cash.drutocash.service.MyEventUploader
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadCallCommentFailEvent
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadCallCommentSuccessEvent
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadSilentLivingFailEvent
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadSilentLivingSuccessEvent
import com.druto.loan.cash.drutocash.ui.LanguageDialog
import com.druto.loan.cash.drutocash.utils.AppLauncher.openWhatsAppChat
import com.druto.loan.cash.drutocash.utils.BitmapUtil.compressImage
import com.druto.loan.cash.drutocash.utils.CashEaseHelper.hasPermission
import com.druto.loan.cash.drutocash.utils.CashEaseHelper.isPackageExisted
import com.druto.loan.cash.drutocash.utils.CashEaseHelper.startActivity
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper.configurationResources
import com.druto.loan.cash.drutocash.utils.LanguageUtil
import com.druto.loan.cash.drutocash.utils.LanguageUtil.changeAppLanguage
import com.druto.loan.cash.drutocash.utils.SpHelper
import com.druto.loan.cash.drutocash.utils.SpHelper.mainSp
import com.druto.loan.cash.drutocash.utils.ToastUtil.showToast
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.su.request.http.HttpResult
import com.su.request.http.MultipartFile
import com.su.request.http.NetRequest
import com.su.request.http.NullObject
import com.su.request.http.ProgressResult
import com.su.request.http.function.BusinessFunction
import com.su.request.http.observer.BaseObserver
import com.su.request.http.transformer.ParserTransformer
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.stream.Collectors
import kotlin.concurrent.Volatile

class JavascriptManager(private var mActivity: MainActivity) {
    private var mPermissionLauncher: ActivityResultLauncher<Intent>? = null
    private var mCameraLauncher: ActivityResultLauncher<Intent>? = null
    private var mContactLauncher: ActivityResultLauncher<Intent>? = null
    private var mIdPhotoLauncher: ActivityResultLauncher<Intent>? = null

    @Volatile
    private var mRequestId: String? = null
    private val mContactSubject = PublishSubject.create<Array<String?>>()
    private val mCameraSubject = PublishSubject.create<String?>()
    private val mIdPhotoSubject = PublishSubject.create<String?>()
    private var mContactArray: Array<String?>? = null
    private var mRemoteIdPhotoUrl: String? = null
    private var mRedirectUrl: String? = null

    init {
        initPermissionLauncher()
        initCameraLauncher()
        initContactLauncher()
        initIdPhotoLauncher()
    }

    private fun initPermissionLauncher() {
        val contract: ActivityResultContracts.StartActivityForResult =
            ActivityResultContracts.StartActivityForResult()
        mPermissionLauncher = mActivity.registerForActivityResult(
            contract
        ) { result: ActivityResult ->
            val code = result.resultCode
            if (code != Activity.RESULT_OK) {
                logCall(
                    "reqAuthorizationMinimum",
                    "permissions error, code=$code"
                )
                return@registerForActivityResult
            }
            onPermissionGranted()
        }
    }

    fun onPermissionGranted() {
        if (!isGpsEnabled(mActivity)) {
            mActivity.showGpsDialog()
            return
        }
        if (isGpsEnabled(mActivity) &&
            hasPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            mActivity.startLocate()
        }
        val data = DrutoCashApplication.APPLICATION_DATA
        val list = data.getPermissionEntityList()
        // 找出需要授权(level > 0)并且目前没有被授权的权限
        val authorizeList = list.stream().filter { permission: PermissionEntity ->
            val context = GeneralInfoHelper.context
            val level = permission.level
            level > 0 && !permission.check(context).isGranted
        }.collect(Collectors.toList())
        if (authorizeList.isEmpty()) {
            uploadAllInfo()
        } else {
            logCall(
                "reqAuthorizationMinimum",
                "permissions error, authorizeList=$authorizeList"
            )
        }
    }

    private fun initContactLauncher() {
        val contract: ActivityResultContracts.StartActivityForResult =
            ActivityResultContracts.StartActivityForResult()
        mContactLauncher = mActivity.registerForActivityResult(
            contract
        ) { result: ActivityResult ->
            val code = result.resultCode
            logCall("contactsMinimum", "code=$code")
            if (code != Activity.RESULT_OK) {
                mContactSubject.onError(RuntimeException("code=$code"))
                return@registerForActivityResult
            }
            val data = result.data
            if (data == null) {
                mContactSubject.onError(RuntimeException("no contact data"))
                return@registerForActivityResult
            }
            val uri = data.data
            if (uri == null) {
                mContactSubject.onError(RuntimeException("no contact uri"))
                return@registerForActivityResult
            }
            val cr = mActivity.contentResolver
            val cursor = cr.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                //取得联系人姓名
                val nameFieldColumnIndex = cursor.getColumnIndex(
                    ContactsContract.Contacts.DISPLAY_NAME
                )
                val name = cursor.getString(nameFieldColumnIndex)
                @SuppressLint("Range") val phone = cursor.getString(
                    cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                )
                cursor.close()
                mContactSubject.onNext(arrayOf(name, phone))
            } else {
                mContactSubject.onError(RuntimeException("no contact uri"))
            }
        }
    }

    // 静默活体
    private fun initCameraLauncher() {
        val contract: ActivityResultContracts.StartActivityForResult =
            ActivityResultContracts.StartActivityForResult()
        mCameraLauncher = mActivity.registerForActivityResult(
            contract
        ) { result: ActivityResult ->
            val code = result.resultCode
            if (code != Activity.RESULT_OK) {
                mRedirectUrl = null
                mCameraSubject.onNext("")
                return@registerForActivityResult
            }
            val intent = result.data
            val filepath = intent!!.getStringExtra("filepath")
            val photoFile = File(filepath)
            logCall("silentLivingMinimum", "photoFile=$photoFile")
            uploadPhotoAlive(photoFile)
        }
    }

    // 选民证
    private fun initIdPhotoLauncher() {
        val contract: ActivityResultContracts.StartActivityForResult =
            ActivityResultContracts.StartActivityForResult()
        mIdPhotoLauncher = mActivity.registerForActivityResult(
            contract
        ) { result: ActivityResult ->
            val code = result.resultCode
            if (code != Activity.RESULT_OK) {
                mRemoteIdPhotoUrl = null
                mIdPhotoSubject.onNext("")
                return@registerForActivityResult
            }
            val intent = result.data
            val filepath = intent!!.getStringExtra("filepath")
            val photoFile = File(filepath)
            logCall("selectCivilCertificateMinimum", "photoFile=$photoFile")
            uploadIdPhoto(photoFile)
        }
    }

    // 1. 加密（H5 -> 壳）
    // 传参：要加密的数据(json 字符串)
    // 返回：json 字符串  ["timestamp", "sessionId", "token", "signature", "加密后的数据"]
    @JavascriptInterface
    fun transmissionEncryptionWield(jsonData: String): String {
        val input = jsonData
        logCall("encryptMinimumApp", "input=$input")
        val bytes = encrypt(input.toByteArray(), DrutoCashApplication.SECURITY_KEY)
        val result = Base64.encodeToString(bytes, Base64.NO_WRAP)
            .replace("\\+".toRegex(), "-")
            .replace("/".toRegex(), "_")
            .replace("=".toRegex(), "")
            .trim { it <= ' ' }
        //        String result = new String(bytes);
        logCall("encryptMinimumApp", "result=$result")
        //  ["timestamp", "sessionId", "token", "signature", "加密后的数据"]
        val list: MutableList<String> = ArrayList()
        val timestamp = System.currentTimeMillis().toString()
        val sessionId = DrutoCashApplication.APPLICATION_DATA.sessionId
        val token = token
        val md5 = md5(result.toByteArray())
        val signature = makeSignature(timestamp, token, md5, sessionId ?: "")
        list.add(timestamp)
        list.add(sessionId ?: "")
        list.add(token)
        list.add(signature)
        list.add(result)
        logCall("encryptMinimumApp", "timestamp=$timestamp")
        logCall("encryptMinimumApp", "sessionId=$sessionId")
        logCall("encryptMinimumApp", "token=$token")
        logCall("encryptMinimumApp", "md5=$md5")
        logCall("encryptMinimumApp", "signature=$signature")
        return Gson().toJson(list)
    }

    // 2. 获取授权结果（H5 -> 壳）
    // 传参：json 字符串，格式["权限1", "权限2", "权限3"]
    // 返回：json 字符串，格式["权限1", "权限2"]，表示未授权。数组长度为0，表示全部授权完成。
    @JavascriptInterface
    fun checkAuthorizationWield(permissions: Array<String>?): String {
        logCall("authorizationMinimum", "param=" + permissions.contentToString())
        val unauthorized: MutableList<String> = ArrayList()
        if (permissions == null) {
            return "[]"
        }
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(mActivity, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                unauthorized.add(permission)
            }
        }
        logCall("authorizationMinimum", "getResult result=$unauthorized")

        val gson = Gson()
        return gson.toJson(unauthorized)
    }

    // 3. 授权（H5 -> 壳）
    //  1. 检查授权情况，若有未授权的项目，则展示授权信息披露，且不断提示用户直到授权为止。当用户完成授权后，检查"信息收集及上传"后台任务是否是运行状态，如果未运行则启动。
    //  2. 传参：requestId(String)（时间戳生一个ID，请求了多次，只能返回一次）
    //  3. 返回：无
    @JavascriptInterface
    fun equipmentInformationWield(requestId: String?) :Boolean{
        logCall(
            "reqAuthorizationMinimum",
            "requestId=$requestId, lastRequestId=$mRequestId"
        )
        if (mRequestId != null) {
            return false
        }
        mRequestId = requestId
        DrutoCashApplication.success=""
        val list =
            DrutoCashApplication.APPLICATION_DATA.getPermissionEntityList()
        // level全部设置为2，所有权限都强制要求授权
        val newList = list.stream().map { e: PermissionEntity ->
            try {
                val permission = e.clone() as PermissionEntity
                permission.check(mActivity).level = 2
                return@map permission
            } catch (ex: CloneNotSupportedException) {
                throw RuntimeException(ex)
            }
        }
            .map { e: PermissionEntity ->
                logCall("reqAuthorizationMinimum", "p=$e")
                e
            }
            .filter { entity: PermissionEntity -> !entity.check(mActivity).isGranted }
            .collect(Collectors.toList())
        val isGpsEnabled = isGpsEnabled(mActivity)
        logCall(
            "reqAuthorizationMinimum",
            "newList=" + newList.size + ", isGpsEnabled=" + isGpsEnabled
        )
//        if (newList.isNotEmpty()) {
            RequirePermissionActivity.startPermissionActivity(
                mActivity, mPermissionLauncher!!,
                ArrayList(list)
            )
//        } else if (!isGpsEnabled) {
//            mActivity.showGpsDialog()
//        } else {
//            uploadAllInfo()
//        }
        var getting = true
        while (getting) {
            try {
                Thread.sleep(50L)
            } catch (e: Throwable) {
            }
            getting = DrutoCashApplication.success == null || DrutoCashApplication.success.isEmpty()
        }
        return true
    }
    @JavascriptInterface
    fun limistWield(flags: String) {
        mActivity.showPermissionDialog(flags)
    }
    private fun uploadAllInfo() {
        logCall("uploadAllInfo", "requestId=$mRequestId")
        uploadAllInfo(
            object : OnAllUploadResultListener {
                override fun onSuccess() {
                    mRequestId = null
                    logCall("uploadAllInfo", "uploadAllInfo onSuccess, reset requestId")
                    DrutoCashApplication.success="success"
                    uploadSuccess()
                }

                override fun onFailed(msg: String?) {
                    mRequestId = null
                    logCall(
                        "uploadAllInfo",
                        "uploadAllInfo failed, reset requestId: $msg"
                    )
                }
            })
    }

    // 6. 记录客户端事件（H5 -> 壳）不要限制不能为空
    //  1. 传参： id(int),name(String), params(Json String)
    //  2. 返回：无
    @JavascriptInterface
    fun clientEventsWield(stringId: String?, name: String?, paramJson: String?) {
        logCall(
            "clientEventsMinimum",
            "stringId=$stringId, name=$name, paramJson=$paramJson"
        )
        uploadEvent(stringId, name, paramJson)
    }

    // 7. 开始显示H5画面（H5 -> 壳）
    //  1. 告知壳H5页面已加载完成，可以显示。壳需要停止并隐藏加载动画，显示WebView。
    //  2. 传参: 无
    //  3. 返回: bool // 默认true
    @JavascriptInterface
    fun animationOffWield(): String {
        logCall("animationOffMinimum", "called")
        mActivity.runOnUiThread {
            mActivity.showLoadingAnim(false)
            mActivity.showError(false)
        }
        return true.toString()
    }

    // 8. 添加系统日历事件并设置提醒（H5 -> 壳）
    //  1. 如果事件不存在（对比事件名称和日期），则创建。
    //  2. 传参：事件ID， title，description，(long)dtstart，(long)dtend，(int)remider_minutes
    //  3. 返回：事件ID
    @JavascriptInterface
    fun addCalendarEventsWield(paramJson: String?): String {
        val type = object : TypeToken<EventParam?>() {
        }.type
        val param = Gson().fromJson<EventParam>(paramJson, type)
        logCall("calendarEventMinimum", "event=$param")
        val newId = addOrUpdateCalendarEvent(
            mActivity,
            param.id, param.title, param.description,
            param.dtstart, param.dtend, param.reminderMinutes
        )
        logCall("calendarEventMinimum", "newId=$newId")
        return newId.toString()
    }
    // 9. 选择联系人（H5 -> 壳）
    @JavascriptInterface
    fun selectContactPeople(): String{
            System.out.println("11111111111111")

            logCall(
                "contactsMinimum",
                "called, thread=" + Thread.currentThread()
            )
            mContactArray = null
            val intent = Intent(Intent.ACTION_PICK)
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)
            mContactLauncher!!.launch(intent)
            val latch = CountDownLatch(1)
            mContactSubject.safeSubscribe(object : BaseObserver<Array<String?>?>(null) {
                override fun onNext(strings: Array<String?>) {
                    super.onNext(strings)
                    if (strings.size == 2) {
                        mContactArray = strings
                    }
                    logCall(
                        "contactsMinimum",
                        "onNext=" + mContactArray.contentToString()
                    )
                    MyEventUploader.uploadSelectNameMobileSuccessEvent(
                        mContactArray?.get(0),
                        mContactArray?.get(1)
                    )
                    latch.countDown()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    logCall(
                        "contactsMinimum",
                        "onError=" + e.message
                    )
                    MyEventUploader.uploadSelectNameMobileFailEvent()
                    latch.countDown()
                }
            })
            try {
                latch.await()
            } catch (e: InterruptedException) {
                logCall("contactsMinimum", e.message)
            }
            logCall(
                "contactsMinimum",
                "contact=" + mContactArray.contentToString()
            )
            val gson = Gson()
            if(mContactArray!=null){
                return gson.toJson(mContactArray)
            }else{
                return "false"
            }

        }

    // 10. 信息收集及上传成功通知（壳->H5）
    // 1. 传参: requestId(String)（当前时间戳）
    private fun uploadSuccess() {
        logCall("uploadSuccess", "call js method refreshPage")
        mActivity.loadJs(
            "refreshPage", null
        ) {
            logCall(
                "uploadAllInfo",
                "uploadAllInfo event send success"
            )
        }
    }

    // 11. 跳转外链（H5 -> 壳）
    //  1. 传参：url地址
    //  2. 返回：无
    //  3. 注：跳转第三方浏览器，先判断chrome浏览器是否安装，如果安装了用chrome打开连接，如果没有安装用默认浏览器打开连接
    @JavascriptInterface
    fun jumpToWebPageWield(url: String?) {
        logCall("jumpGooglePlayMinimum", "url=$url")
        val hasChrome = isPackageExisted("com.android.chrome")
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (hasChrome) {
            logCall("jumpGooglePlayMinimum", "has Chrome")
            intent.setPackage("com.android.chrome")
        } else {
            if (intent.resolveActivity(mActivity.packageManager) == null) {
                logCall("jumpGooglePlayMinimum", "no browser found")
                return
            }
        }
        logCall("jumpGooglePlayMinimum", "open browser for $url")
        startActivity(mActivity, intent)
    }

    // 12. 调用评论SDK（H5 -> 壳）
    //  1. 传参：无
    //  2. 返回：无
    //  https://developer.android.google.cn/guide/playcore/in-app-review?hl=zh-cn
    @JavascriptInterface
    fun callUpWield() {
        logCall("callCommentMinimum", "called")
        val manager = ReviewManagerFactory.create(mActivity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { infoTask: Task<ReviewInfo> ->
            if (infoTask.isSuccessful) {
                // We can get the ReviewInfo object
                val reviewInfo = infoTask.result
                logCall("callCommentMinimum", "reviewInfo=$reviewInfo")
                val flow =
                    manager.launchReviewFlow(mActivity, reviewInfo)
                flow.addOnCompleteListener { task: Task<Void?> ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                    if (task.isSuccessful) {
                        logCall("callCommentMinimum", "Complete")
                        uploadCallCommentSuccessEvent()
                    } else {
                        val exception = task.exception
                        if (exception == null) {
                            logCall(
                                "callCommentMinimum",
                                "exception is null"
                            )
                            uploadCallCommentFailEvent("exception is null")
                        } else {
                            logCall(
                                "callCommentMinimum",
                                exception.message
                            )
                            uploadCallCommentFailEvent(exception.message!!)
                        }
                    }
                }
                    .addOnCanceledListener { MyEventUploader.uploadCallCommentCancelEvent() }
            } else {
                // There was some problem, log or handle the error code.
                @ReviewErrorCode val reviewErrorCode =
                    (infoTask.exception as ReviewException).errorCode
                logCall(
                    "callCommentMinimum",
                    "reviewErrorCode=$reviewErrorCode"
                )
            }
        }
    }

    @get:JavascriptInterface
    val getVersionWield: String?
        // 13. 获取APP版本号（H5 -> 壳）
        get() {
            logCall("versionMinimum", "called")
            try {
                val pm = mActivity.packageManager
                val packageInfo =
                    pm.getPackageInfo(mActivity.packageName, 0)
                return packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                logCall("versionMinimum", e.message)
            }
            return null
        }

    // 14. 禁用系统返回键
    //  1. 传参：boolean（true禁用，false不禁用）
    //  2. 返回：无
    @JavascriptInterface
    fun disableSystemReturnKeyWield(disable: Boolean) {
        logCall("disableReturnButtonMinimum", "disable=$disable")
        mActivity.disableBackPressed(disable)
    }

    // 15. 获取联系人展示方式
    //  1. 传参：无
    //  2. 返回：int值（启动接口返回的值）
    @JavascriptInterface
    fun contactDisplayMethodWield(): String {
        val separation = DrutoCashApplication.APPLICATION_DATA.separation
        logCall("contactDisplayMethodMinimum", "separation=$separation")
        return separation.toString()
    }

    // 16. 活体SDK，仅站位
    @JavascriptInterface
    fun dynamicLivingWield() {
        logCall("livingBodyMinimum", "called")
    }

    // 17. 调用静默活体
    //  1. 传参：无
    //  2. 返回：无
    @JavascriptInterface
    fun silentLivingWield() {
        logCall("silentLivingMinimum", "called")
        CameraActivity.startIdPhotoActivity(
            mActivity,
            CameraActivity.TYPE_CAMERA,
            mCameraLauncher!!
        )
        val latch = CountDownLatch(1)
        val observers = ArrayList<BaseObserver<*>>()
        mCameraSubject.safeSubscribe(object : BaseObserver<String?>(observers) {
            override fun onNext(redirectUrl: String) {
                super.onNext(redirectUrl)
                if (redirectUrl.isNotEmpty()) {
                    mRedirectUrl = redirectUrl
                }
                logCall("silentLivingMinimum", "redirectUrl=$redirectUrl")
                latch.countDown()
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                logCall("silentLivingMinimum", "onError=" + e.message)
                latch.countDown()
            }
        })
        try {
            latch.await()
        } catch (e: InterruptedException) {
            logCall("silentLivingMinimum", e.message)
        }
        logCall("silentLivingMinimum", "mRedirectUrl=$mRedirectUrl")
        val remoteUrl = mRedirectUrl
        mRedirectUrl = null
        observers.forEach { it.dispose() }
        if (remoteUrl.isNullOrEmpty()) {
            return
        }
        mActivity.runOnUiThread { mActivity.loadUrl(remoteUrl) }
    }

    // 18. toast提示方法
    //  1. 传参：toast的提示信息
    //  2. 返回：无
    @JavascriptInterface
    fun toastTipWield(content: String?) {
        logCall("toastMinimum", "content")
        if (content == null) {
            logCall("toastMinimum", "js toast content is null!")
            return
        }
        mActivity.runOnUiThread { showToast(mActivity, content) }
    }

    // 19. 拨打电话（地址的方式，判断tel://）
    @JavascriptInterface
    fun callNumberWield(phoneNumber: String?) {
        logCall("dialMinimum", "phoneNumber$phoneNumber")
        val intent = Intent(Intent.ACTION_DIAL)
        intent.setData(Uri.parse("tel:$phoneNumber"))
        startActivity(mActivity, intent)
    }

    // 20. 调用whatsApp聊天界面（地址的方式，判断whatsapp://）
    // H5目前不调用此方法
    @JavascriptInterface
    fun callWhatsapp(phoneNumber: String?) {
        logCall("callWhatsapp", "phoneNumber$phoneNumber")
        val installed = isPackageExisted("com.whatsapp")
        if (installed) {
            openWhatsAppChat(mActivity, phoneNumber)
        } else {
            toastTipWield("whatsapp is not installed")
        }
    }

    @JavascriptInterface
    fun getLanguageWield(): String {
        // 21. 获取语言
        val resources = configurationResources
        val codeArray =
            resources!!.getStringArray(R.array.language_code_list)
        val sp = mainSp
        val languageCode = sp!!.getString(
            SpHelper.COLUMN_LANGUAGE_CODE,
            LanguageUtil.DEFAULT_LANGUAGE
        )!!
        logCall("getLangGoof", "languageCode=$languageCode")
        val currentLanguage =
            LanguageDialog.getUserCurrentLanguage(codeArray, languageCode)
        logCall(
            "getLangGoof",
            "languageIndex=$currentLanguage"
        )
        return currentLanguage.toString()
    }
    // 22. 切换语言
    @JavascriptInterface
    fun switchLanguageWield(data: String?) {
        var inputCode = "en"
        if (DrutoCashApplication.country==2){
        if ("1" == data) {
            inputCode = "bn"
        }}else{
            if ("1" == data) {
                inputCode = "sw"
            }
        }
        logCall("switchingAppMinimum", "setLangGoof=$inputCode")
        val newResources = configurationResources
        val codeArray = newResources!!.getStringArray(R.array.language_codetz_list)
        var found = false
        for (code in codeArray) {
            if (code == inputCode) {
                found = true
                break
            }
        }
        if (!found) {
            toastTipWield("not support language $inputCode, data=$data")
            return
        }
        logCall("switchingAppMinimum", "language=$inputCode")
        changeAppLanguage(inputCode)
        val sp = mainSp
        sp!!.edit()
            .putString(SpHelper.COLUMN_LANGUAGE_CODE, inputCode)
            .apply()
    }

    // 23. 选民证：拍照 IdFront, IdBack
    @JavascriptInterface
    fun photographWield(data: String): String? {
        logCall("selectCivilCertificateMinimum", "data=$data")
        CameraActivity.startIdPhotoActivity(
            mActivity,
            CameraActivity.TYPE_PHOTO,
            mIdPhotoLauncher!!
        )
        val latch = CountDownLatch(1)
        val observers = ArrayList<BaseObserver<*>>()
        mIdPhotoSubject.safeSubscribe(object : BaseObserver<String?>(observers) {
            override fun onNext(remoteUrl: String) {
                super.onNext(remoteUrl)
                if (remoteUrl.isNotEmpty()) {
                    mRemoteIdPhotoUrl = remoteUrl
                }
                logCall("selectCivilCertificateMinimum", "idPhotoUrl=$remoteUrl")
                latch.countDown()
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                mRemoteIdPhotoUrl = null
                logCall("selectCivilCertificateMinimum", "onError=" + e.message)
                latch.countDown()
            }
        })
        try {
            latch.await()
        } catch (e: InterruptedException) {
            logCall("selectCivilCertificateMinimum", e.message)
        }
        observers.forEach { it.dispose() }
        logCall("selectCivilCertificateMinimum", "mRemoteIdPhotoUrl=$mRemoteIdPhotoUrl")
        val remoteUrl = mRemoteIdPhotoUrl
        mRemoteIdPhotoUrl = null
        return remoteUrl
    }

    private fun uploadIdPhoto(photoFile: File) {
        val url = SpHelper.host + Url.SEND_ID_PHOTO
        makePhotoUploadObservable(mIdPhotoSubject, url, photoFile).safeSubscribe(
            object : BaseObserver<NullObject?>(null) {
                override fun onComplete() {
                    super.onComplete()
                    logCall("uploadIdPhoto", "uploaded=$photoFile")
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    mIdPhotoSubject.onError(e)
                    logCall("uploadIdPhoto", "uploadError=" + e.message)
                }
            })
    }

    // 静默活体
    private fun uploadPhotoAlive(photoFile: File) {
        val url = SpHelper.host + Url.SEND_PHOTO_ALIVE
        makePhotoUploadObservable(mCameraSubject, url, photoFile).safeSubscribe(
            object : BaseObserver<NullObject?>(null) {
                override fun onComplete() {
                    super.onComplete()
                    uploadSilentLivingSuccessEvent()
                    logCall(
                        "uploadPhotoAliveComplete",
                        "uploaded=$photoFile"
                    )
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    mCameraSubject.onError(e)
                    uploadSilentLivingFailEvent(e.message!!)
                    logCall("uploadPhotoAliveError", "uploadError=" + e.message)
                    makeImageUploadErrorObservable(e.message)
                }
            })
    }

    private fun makeImageUploadErrorObservable(error: String?) {
        val url = SpHelper.host + Url.SEND_PHOTO_ALIVE_ERROR
        logCall(
            "makeImageUploadErrorObservable",
            "ImageUploadError=$error"
        )
        logCall("makeImageUploadErrorObservable", "url=$url")
        val list: MutableList<Any?> = ArrayList()
        list.add(error)
        val gson = Gson()
        val jsonString = gson.toJson(list)
        val requestBody = prepareRequestBody(jsonString, false)
        NetRequest.create<HttpResult<*>>(url, object : TypeToken<HttpResult<*>?>() {
        })
            .method("POST")
            .build(DrutoCashApplication.client, requestBody)
            .compose(ParserTransformer())
            .map<Any>(object : BusinessFunction<HttpResult<*>?, NullObject>() {
                override fun onSuccess(result: HttpResult<*>?): NullObject {
                    if (result?.hum != 0) {
                        logCall(
                            "makeImageUploadErrorObservable",
                            "ImageUploadError=" + result?.inverse
                        )
                    }
                    return NullObject.NULL_OBJECT
                }
            }).safeSubscribe(object : BaseObserver<Any?>(null) {
                override fun onComplete() {
                    super.onComplete()
                    logCall("makeImageUploadErrorObservable", "ImageUploadErrorComplete")
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    logCall("makeImageUploadErrorObservable", "ImageUploadErrorError")
                    toastTipWield(e.message)
                }
            })
    }

    private fun createMultipartBody(type: MediaType, multipartFile: MultipartFile): RequestBody {
        val builder = MultipartBody.Builder()
        builder.setType(type)
        val file = multipartFile.file
        if (file != null && file.exists() && file.isFile) {
            builder.addFormDataPart(
                multipartFile.name, multipartFile.fileName,
                RequestBody.create(multipartFile.mimeType.toMediaTypeOrNull(), file)
            )
        }
        return builder.build()
    }

    private fun makePhotoUploadObservable(
        subject: PublishSubject<String?>,
        url: String, photoFile: File
    ): Observable<NullObject> {
        logCall("makePhotoUploadObservable", "url=$url")
        if (!photoFile.exists()) {
            toastTipWield("not found image: $photoFile")
            return Observable.empty()
        }
        return compressImage(photoFile)
            .observeOn(Schedulers.io())
            .flatMap(makeRequest(url))
            .compose(ParserTransformer())
            .map(object : BusinessFunction<HttpResult<ImageResult?>?, NullObject>() {
                override fun onSuccess(httpResult: HttpResult<ImageResult?>?): NullObject {
                    if (BuildConfig.DEBUG) {
                        logCall(
                            "makePhotoUploadObservable",
                            "response: $httpResult"
                        )
                    }
                    if (httpResult?.hum != HttpResult.CODE_SUCCESS) {
                        toastTipWield("Upload failed")
                        //上传失败,终止后续任务
                        throw IOException(httpResult?.inverse)
                    }
                    val upload = httpResult.silently
                    if (upload != null && upload.saver != 1) {
                        toastTipWield(upload.boat)
                        //上传失败,终止后续任务
                        throw IOException(upload.boat)
                    } else if (upload?.spleen != null) {
                        subject.onNext(upload.spleen!!)
                    } else if (upload?.drop != null) {
                        // 需要跳转的链接
                        subject.onNext(upload.drop!!)
                    }
                    return NullObject.NULL_OBJECT
                }

            })
    }

    private fun compressImage(photoFile: File): Observable<File> {
        val fileLimitation = 200 * 1024L // 200K
        return Observable.just<File>(photoFile) //子线程中压缩图片
            .flatMap<File> { input: File ->
                var compressCount = 0
                var compressedFile = input
                while (compressedFile.length() >= fileLimitation) {
                    compressCount++
                    logCall(
                        "tempImage",
                        "tempSize=" + compressedFile.length() + ", compressedFile=" + compressedFile
                    )
                    val file = File(
                        mActivity.externalCacheDir,
                        "temp-id-photo-upload-$compressCount.jpg"
                    )
                    compressedFile = compressImage(compressedFile, file)
                }
                logCall(
                    "compressImage",
                    "size=" + compressedFile.length() + ", compressedFile=" + compressedFile
                )
                logCall(
                    "compressImage",
                    "photoFile=" + photoFile + ", thread=" + Thread.currentThread().name
                )
                Observable.just<File>(compressedFile)
            }
    }

    private fun makeRequest(
        url: String
    ): Function<File, ObservableSource<ProgressResult<HttpResult<ImageResult?>?, Response>>> {
        return Function<File, ObservableSource<ProgressResult<HttpResult<ImageResult?>?, Response>>> { file: File ->
            logCall(
                "makeRequest",
                "url=$url, file=$file"
            )
            val type = "multipart/form-data".toMediaTypeOrNull()
            val multipartFile = MultipartFile(
                "image", file.name,
                "image/*", file
            )
            NetRequest.create<HttpResult<ImageResult?>?>(
                url,
                object : TypeToken<HttpResult<ImageResult?>?>() {})
                .addMultipart(multipartFile)
                .setMediaType("multipart/form-data")
                .build(
                    DrutoCashApplication.client,
                    createMultipartBody(type!!, multipartFile)
                )
        }
    }

    @JavascriptInterface
    fun log(data: String?) {
        if (!TextUtils.isEmpty(data)) {
            val gson = Gson()
            val param = gson.fromJson(data, object : TypeToken<LogEntity?>() {})
            var tag = param?.tag
            val level = param?.level
            var content = param?.content
            if (TextUtils.isEmpty(tag)) {
                tag = TAG
            }
            if (TextUtils.isEmpty(content)) {
                content = "no message!"
            }
            when (level) {
                "e" -> Log.e(tag, content!!)
                "w" -> Log.w(tag, content!!)
                "i" -> Log.i(tag, content!!)
                "d" -> Log.d(tag, content!!)
                "v" -> Log.v(tag, content!!)
                else -> Log.v(tag, content!!)
            }
        }
    }

    private class LogEntity {
        val tag: String? = null
        val level: String? = null
        val content: String? = null

        override fun toString(): String {
            return "LogEntity{" +
                    "tag='" + tag + '\'' +
                    ", level='" + level + '\'' +
                    ", content='" + content + '\'' +
                    '}'
        }
    }

    companion object {
        private val TAG: String = JavascriptManager::class.java.simpleName

        private fun logCall(functionName: String, data: String?) {
            if (BuildConfig.DEBUG) {
                Log.d(
                    TAG,
                    "function: $functionName data: $data"
                )
            }
        }
    }
}
