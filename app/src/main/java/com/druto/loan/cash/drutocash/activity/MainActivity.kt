package com.druto.loan.cash.drutocash.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.druto.loan.cash.drutocash.BuildConfig
import com.druto.loan.cash.drutocash.DrutoCashApplication
import com.druto.loan.cash.drutocash.R
import com.druto.loan.cash.drutocash.collector.SomeInfoCollector.openGPSSettings
import com.druto.loan.cash.drutocash.ui.PermissionLocationDialog
import com.druto.loan.cash.drutocash.ui.PermissionSmsDialog
import com.druto.loan.cash.drutocash.ui.base.BaseAppCompatActivity
import com.druto.loan.cash.drutocash.ui.base.JavascriptManager
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper.context
import com.druto.loan.cash.drutocash.utils.SpHelper
import com.druto.loan.cash.drutocash.utils.ToastUtil.showToast


class MainActivity : BaseAppCompatActivity(), OnRefreshListener, View.OnClickListener {
    private var mUrl: String? = null
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private var mWebView: WebView? = null
    private lateinit var mLoadingErrorLayout: View
    private val mHandler = Handler(GeneralInfoHelper.context.mainLooper)
    private lateinit var mOnBackPressedCallback: OnBackPressedCallback
    private var mDisableBackKey = false
    private var mShouldShowGpsDialog = false
    private var mJm: JavascriptManager? = null
    private var mPermissionLauncherNew: ActivityResultLauncher<Intent>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = intent
        mUrl = intent.getStringExtra("url")
        val sessionId = DrutoCashApplication.APPLICATION_DATA.sessionId;
        // 从通知栏跳转过来，但是此时应用还没有初始化 || 系统清理内存杀死app重启时，没有sessionId
        if (mUrl == null || sessionId.isNullOrEmpty()) {
            finish()
            Log.w(logTag, "onCreate finish, sessionId=$sessionId")
            val splashIntent = Intent(this, InitialActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(splashIntent)
            return
        }
        Log.i(logTag, "main url=$mUrl")
        initWebView()
        // 添加 OnBackPressedCallback 到 Dispatcher
        mOnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.w(
                    logTag, ("disableBack=" + mDisableBackKey + ", focused=" + mWebView!!.isFocused
                            + ", canGoBack=" + mWebView!!.canGoBack())
                )
                if (mDisableBackKey) {
                    return
                }
                if (mWebView!!.isFocused && mWebView!!.canGoBack()) {
                    showError(false)
                    mWebView!!.goBack()
                    mUrl = mWebView!!.url
                } else {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, mOnBackPressedCallback)
        val contract = ActivityResultContracts.StartActivityForResult()
        mPermissionLauncherNew = registerForActivityResult(
            contract
        ) { result: ActivityResult ->
            val intent = Intent(context, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }
        System.out.println("SpHelper.isFirst--------->"+SpHelper.isFirst)
        if (SpHelper.isFirst){
            SpHelper.isFirst=false
            System.out.println("SpHelper.isFirst--------->"+SpHelper.isFirst)
            NewPermissionActivity.startPermissionActivity(this@MainActivity,mPermissionLauncherNew!!)

        }
    }

    override fun onResume() {
        super.onResume()
        if (mShouldShowGpsDialog) {
            mJm!!.onPermissionGranted()
            mShouldShowGpsDialog = false
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // app没有启动或者还在splash中
        if (mUrl == null) {
            finish()
            Log.w(logTag, "onNewIntent finish")
            val splashIntent = Intent(this, InitialActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(splashIntent)
        }
    }

    private fun initWebView() {
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh)
        mSwipeRefreshLayout.setColorSchemeResources(
            R.color.swipe_color_1, R.color.swipe_color_2,
            R.color.swipe_color_3, R.color.swipe_color_4
        )
        mSwipeRefreshLayout.setOnRefreshListener(this)
        mSwipeRefreshLayout.setEnabled(false)
        mWebView = findViewById(R.id.web_view)

        if (!verifyUrl(mUrl)) {
            var toast: String? = "url error: "
            toast += mUrl
            showToast(GeneralInfoHelper.context, toast!!)
            finish()
        }

        mLoadingErrorLayout = findViewById(R.id.load_error_layout)
        mLoadingErrorLayout.setOnClickListener(this)

        initWebViewSettings()
        removeAdditionalJavascriptInterface()
        mWebView!!.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY)
        mWebView!!.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("tel:")) {
                    dail(url)
                    return true
                }
                mUrl = url
                Log.d(logTag, "newUrl: $mUrl")
                mHandler.post {
                    loadUrl(mUrl!!)
                }
                return true
            }
        })

        mLoadingErrorLayout.visibility = View.GONE
        mSwipeRefreshLayout.post {
            mSwipeRefreshLayout.isRefreshing = true
            loadUrl(mUrl!!)
        }
    }

    private fun dail(url: String) {
        val finalUrl = if (url.contains("://")) {
            url.substring(0, url.indexOf(":") + 1) + Uri.encode(
                url.substring(url.lastIndexOf("/") + 1)
            )
        } else {
            url.substring(0, url.indexOf(":") + 1) + Uri.encode(
                url.substring(url.indexOf(":") + 1)
            )
        }
        val intent = Intent()
        intent.setAction(Intent.ACTION_DIAL)
        intent.setData(Uri.parse(finalUrl))
        startActivity(intent)
    }

    fun showError(show: Boolean) {
        Log.d(logTag, "showError=$show")
        mHandler.post {
            if (show) {
                mLoadingErrorLayout.visibility = View.VISIBLE
            } else {
                mLoadingErrorLayout.visibility = View.GONE
            }
        }
    }

    fun showLoadingAnim(show: Boolean) {
        Log.d(logTag, "showLoadingAnim=$show")
        mHandler.post {
            mSwipeRefreshLayout.visibility = View.VISIBLE
            mSwipeRefreshLayout.isRefreshing = show
        }
    }

    override fun onRefresh() {
        mWebView!!.reload()
    }

    fun loadUrl(url: String) {
        mWebView!!.loadUrl(url)
    }

    private fun verifyUrl(url: String?): Boolean {
        return URLUtil.isNetworkUrl(url) || URLUtil.isAssetUrl(url)
    }

    private fun initWebViewSettings() {
        val webSettings = mWebView!!.settings
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.javaScriptEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.domStorageEnabled = true
        webSettings.builtInZoomControls = true
        mJm = JavascriptManager(this)
        mWebView!!.addJavascriptInterface(mJm!!, "webWield")
    }

    private fun removeAdditionalJavascriptInterface() {
        mWebView!!.removeJavascriptInterface("searchBoxJavaBridge_")
        mWebView!!.removeJavascriptInterface("accessibility")
        mWebView!!.removeJavascriptInterface("accessibilityTraversal")
    }

    /**
     * 禁用返回键功能
     */
    fun disableBackPressed(disable: Boolean) {
        mDisableBackKey = disable
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("url", mUrl)
    }

    fun showGpsDialog() {
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
    var permissionSmsDialog:PermissionSmsDialog?=null
    var permissionLocationDialog:PermissionLocationDialog?=null
    @SuppressLint("SuspiciousIndentation")
    fun showPermissionDialog(flags:String){
        if(flags.equals("register")){
//            val readSmsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            val readPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
//            if (readPhonePermission!= PackageManager.PERMISSION_GRANTED||readSmsPermission!=PackageManager.PERMISSION_GRANTED){
            if (readPhonePermission!= PackageManager.PERMISSION_GRANTED){
          permissionSmsDialog=PermissionSmsDialog(this@MainActivity,
              @SuppressLint("SuspiciousIndentation")
              object :PermissionSmsDialog.OnSmsClick{
              override fun onSmsClick() {
                  permissionSmsDialog!!.dismiss()
                  ActivityCompat.requestPermissions(
                      this@MainActivity,
                      arrayOf<String>(
                          Manifest.permission.READ_PHONE_STATE,
//                          Manifest.permission.READ_SMS
                      ),
                      100
                  )
              }
          })
            permissionSmsDialog!!.show()}
        }else{
            val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (locationPermission!=PackageManager.PERMISSION_GRANTED){
            permissionLocationDialog= PermissionLocationDialog(this@MainActivity,object :PermissionLocationDialog.OnLocationClick{
                override fun onLocationClick() {
                    permissionLocationDialog!!.dismiss()
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf<String>(
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        100
                    )
                }
            })
            permissionLocationDialog!!.show()}
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.load_error_layout) {
            refresh()
        }
    }

    private fun refresh() {
        showLoadingAnim(true)
        showError(false)
        mWebView!!.reload()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (this::mOnBackPressedCallback.isInitialized) {
            mOnBackPressedCallback.remove()
        }
        // 个别机型会导致WebView为null
        // android6 + vivo Y66 / vivo X9
        if (mWebView == null) {
            return
        }
        val viewGroup = mWebView!!.parent as ViewGroup
        viewGroup.removeAllViews()
        mWebView!!.destroy()
    }

    fun loadJs(
        method: String, params: List<String>?,
        resultCallback: ValueCallback<String?>?
    ) {
        if (mWebView == null) {
            if (BuildConfig.DEBUG) {
                Log.d(logTag, "webView = null")
            }
            return
        }

        val script: String
        if (params.isNullOrEmpty()) {
            script = "javascript:$method()"
        } else {
            val sb = StringBuilder(16)
            for (p in params) {
                sb.append("'$p',")
            }
            sb.deleteCharAt(sb.length - 1)
            script = "javascript:$method($sb)"
        }
        mWebView!!.post {
            mWebView!!.evaluateJavascript(script, resultCallback)
            if (BuildConfig.DEBUG) {
                Log.d(logTag, "loadJs: $script callback: $resultCallback")
            }
        }
    }
}
