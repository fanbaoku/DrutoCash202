package com.cash.ease.money.cashease.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.cash.ease.money.cashease.CashEaseApplication
import com.cash.ease.money.cashease.CashEaseApplication.Companion.uploadAllInfo
import com.cash.ease.money.cashease.utils.CashEaseHelper.hasPermission
import com.cash.ease.money.cashease.utils.CashEaseHelper.isPackageExisted
import com.cash.ease.money.cashease.R
import com.cash.ease.money.cashease.collector.PermissionEntity
import com.cash.ease.money.cashease.collector.PermissionEntity.CREATOR.hasStrongLocationPermission
import com.cash.ease.money.cashease.collector.SomeInfoCollector.isGpsEnabled
import com.cash.ease.money.cashease.collector.SomeInfoCollector.openGPSSettings
import com.cash.ease.money.cashease.entity.Armament
import com.cash.ease.money.cashease.entity.Boneless.Companion.transfer
import com.cash.ease.money.cashease.entity.FirstEntity
import com.cash.ease.money.cashease.model.SplashModel
import com.cash.ease.money.cashease.presenter.SplashPresenter
import com.cash.ease.money.cashease.service.MyEventUploader.uploadStartFromWebEvent
import com.cash.ease.money.cashease.service.MyEventUploader.uploadLocationEvent
import com.cash.ease.money.cashease.utils.ToastUtil.showToast
import com.cash.ease.money.cashease.ui.LanguageDialog
import com.cash.ease.money.cashease.ui.base.BaseAppCompatActivity
import com.cash.ease.money.cashease.utils.AppLauncher.openAppInPlayStore
import com.cash.ease.money.cashease.utils.AppLauncher.openWhatsAppChat
import com.cash.ease.money.cashease.utils.GeneralInfoHelper.context
import com.cash.ease.money.cashease.utils.SpHelper
import com.cash.ease.money.cashease.utils.SpHelper.mainSp
import com.cash.ease.money.cashease.utils.SpHelper.whatsapp
import com.cash.ease.money.cashease.view.SplashView
import com.su.request.http.HttpResult
import com.su.request.http.observer.BaseObserver
import java.util.stream.Collectors

class InitialActivity : BaseAppCompatActivity(), SplashView {
    private val mOnBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(
        true
    ) {
        override fun handleOnBackPressed() {
        }
    }
    private var mPermissionLauncher: ActivityResultLauncher<Intent>? = null

    private val mBatteryInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (Intent.ACTION_BATTERY_CHANGED == action) {
                mPresenter!!.onBatteryChanged(intent)
                unregisterReceiver(this)
            }
        }
    }

    private var mShouldShowUpgradeDialog = false
    private var mShouldShowGpsDialog = false
    private var mPresenter: SplashPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val window = window
        fullscreen(window)

        val intent = intent
        startFromWebEvent(intent)
        // 添加 OnBackPressedCallback 到 Dispatcher
        onBackPressedDispatcher.addCallback(this, mOnBackPressedCallback)
        mPresenter = SplashPresenter(this, SplashModel(observers))
        ContextCompat.registerReceiver(
            this, mBatteryInfoReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED), ContextCompat.RECEIVER_EXPORTED
        )
        mPresenter!!.init()
//        initPermissionLauncher()
        CashEaseApplication.country=0
        mPresenter!!.fetchUsableIp()
    }

    override fun getActivityObservers(): List<BaseObserver<*>> = observers

    override fun onRequestInitialSuccess(result: HttpResult<FirstEntity?>?) {
        val entity = result?.exploited
        val armament = entity!!.armament
        val language = mainSp!!.getString(SpHelper.COLUMN_LANGUAGE_CODE, null)
        Log.w(logTag, "language=$language")
        if (language == null) {
            LanguageDialog().showDialog(this, object : LanguageDialog.OnFinishListener {
                override fun onFinish() {
                    processPermissions(entity)
                }
            })
        }
        // 强升跳转到google play
        if (armament != null && armament.assured == 1) {
            showUpgradeDialog(armament)
            return
        }
        if (language != null) {
            processPermissions(entity)
        }
    }

    private fun processPermissions(entity: FirstEntity) {
        // 检查强授权
        val required = entity.boneless
        Log.d(logTag, "required=$required")
        val list = transfer(required)
        // H5稍后会使用，目前数据只在此处更新一次
        CashEaseApplication.APPLICATION_DATA.addAllPermissionEntity(list)
        val hasStrongLocationPermission = hasStrongLocationPermission(list)
        val isGpsEnabled = isGpsEnabled(this)
        // 找出需要授权(level > 0)并且目前没有被授权的权限
        val authorizeList = list.stream().filter { permission: PermissionEntity ->
            val context = context
            val level = permission.level
            level > 0 && !permission.check(context).isGranted
        }.collect(Collectors.toList())
//        if (authorizeList.isNotEmpty()) {
//            RequirePermissionActivity.startPermissionActivity(
//                this,
//                mPermissionLauncher!!,
//                list
//            )
//        } else if (hasStrongLocationPermission && !isGpsEnabled) {
//            showLocationDialog()
//        } else {
//            uploadAllInfo(null)
            startMainActivity(entity.h5MainUrl)
//        }
    }

    override fun onRequestInitialFail(e: Throwable) {
        showCustomerServiceTeamDialog()
    }

    override fun observeLiveData(liveData: LiveData<*>, from: String) {
        liveData.observe(this) { data: Any? -> mPresenter!!.checkData(data!!, from) }
    }

    override fun observeHostRequestLiveData(liveData: LiveData<*>, from: String) {
        liveData.observe(this) { data: Any? ->
            if (java.lang.Boolean.TRUE == data) {
                mPresenter!!.checkData(true, "IpRequest")
            } else if (data != null) {
                Log.e(logTag, "failToFetchHost=$data")
                showCustomerServiceTeamDialog()
            }
            val locationLiveData =
                CashEaseApplication.LOCATION_LIVE_DATA
            val location = locationLiveData.value
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                uploadLocationEvent(latitude, longitude)
            }
        }
    }

    private fun startFromWebEvent(intent: Intent) {
        val uri = intent.data ?: return
        val scheme = uri.scheme ?: return
        uploadStartFromWebEvent(uri.toString())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        startFromWebEvent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (mShouldShowUpgradeDialog) {
            mShouldShowUpgradeDialog = false
            val armament = CashEaseApplication.APPLICATION_DATA.upgradeInfo
            showUpgradeDialog(armament!!)
            return
        } else if (!mShouldShowGpsDialog) {
            return
        }
        onPermissionGranted()
        mShouldShowGpsDialog = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mOnBackPressedCallback.remove()
    }

    private fun fullscreen(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val layoutParams = window.attributes
            layoutParams.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = layoutParams
        }
        val visibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.decorView.systemUiVisibility = visibility
    }

    private fun showLocationDialog() {
        AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle(R.string.enable_gps_dialog_title)
            .setMessage(R.string.enable_gps_dialog_msg)
            .setCancelable(false)
            .setPositiveButton(
                R.string.go
            ) { _: DialogInterface?, _: Int ->
                mShouldShowGpsDialog = true
                openGPSSettings(this)
            }
            .show()
    }

    private fun showUpgradeDialog(armament: Armament) {
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle(R.string.upgrade)
            .setMessage(armament.torch)
            .setPositiveButton(
                R.string.confirm
            ) { _: DialogInterface?, _: Int ->
                if (armament.assured == 1) {
                    mShouldShowUpgradeDialog = true
                }
                openAppInPlayStore(this@InitialActivity)
            }
        if (armament.assured == 1) {
            builder.setCancelable(false)
        } else {
            builder.setNegativeButton(R.string.cancel, null)
        }
        builder.show()
    }

    private fun showCustomerServiceTeamDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setMessage(R.string.service_team_whatsapp_msg)
            .setCancelable(false)
            .setPositiveButton(
                R.string.confirm
            ) { _: DialogInterface?, _: Int -> launchWhatsapp() }
            .setNegativeButton(
                R.string.cancel
            ) { _: DialogInterface?, _: Int -> finish() }
            .show()
    }

    private fun launchWhatsapp() {
        val whatsapp = whatsapp
        Log.d(logTag, "callWhatsapp=$whatsapp")
        if (whatsapp == null) {
            showToast(this, "init failed")
            return
        }
        val installed = isPackageExisted("com.whatsapp")
        if (installed) {
            openWhatsAppChat(this, whatsapp)
        } else {
            showToast(this, "whatsapp is not installed")
        }
    }

    private fun initPermissionLauncher() {
        val contract = ActivityResultContracts.StartActivityForResult()
        mPermissionLauncher = registerForActivityResult(
            contract
        ) { result: ActivityResult ->
            val code = result.resultCode
            if (code != RESULT_OK) {
                Log.e(logTag, "permissions error, code=$code")
                return@registerForActivityResult
            }
            onPermissionGranted()
        }
    }

    private fun onPermissionGranted() {
        val origin = CashEaseApplication.APPLICATION_DATA.getPermissionEntityList()
        val hasStrongLocationPermission = hasStrongLocationPermission(origin)
        if (hasStrongLocationPermission && !isGpsEnabled(this)) {
            showLocationDialog()
            return
        }
        if (isGpsEnabled(this) &&
            hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            startLocate()
        }
        val data = CashEaseApplication.APPLICATION_DATA
        // H5稍后会使用，目前数据只在此处更新一次
        val list = data.getPermissionEntityList()
        // 找出需要授权(level > 0)并且目前没有被授权的权限
        val authorizeList = list.stream().filter { permission: PermissionEntity ->
            val context = context
            val level = permission.level
            level > 0 && !permission.check(context).isGranted
        }.collect(Collectors.toList())
        if (authorizeList.isEmpty()) {
            uploadAllInfo(null)
            startMainActivity(data.mainH5Url)
        } else {
            Log.e(logTag, "permissions error, authorizeList=$authorizeList")
        }
    }

    private fun startMainActivity(url: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
        finish()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}
