package com.cash.ease.money.cashease.activity

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.cash.ease.money.cashease.R
import com.cash.ease.money.cashease.collector.PermissionEntity
import com.cash.ease.money.cashease.model.PermissionSpecModel
import com.cash.ease.money.cashease.presenter.PermissionSpecPresenter
import com.cash.ease.money.cashease.service.MyEventUploader.uploadLaunchPermissionSpecEvent
import com.cash.ease.money.cashease.ui.adapter.PermissionSpecAdapter
import com.cash.ease.money.cashease.ui.base.BaseAppCompatActivity
import com.cash.ease.money.cashease.ui.base.PermissionRequiredActivity
import com.cash.ease.money.cashease.utils.AppLauncher.goSettings
import com.cash.ease.money.cashease.utils.PermissionHelper.DangerousPermission
import com.cash.ease.money.cashease.utils.PermissionHelper.permissionRequest
import com.cash.ease.money.cashease.view.PermissionSpecView

class NewPermissionActivity: PermissionRequiredActivity(),
    View.OnClickListener,
    DangerousPermission,
    PermissionSpecView {
    private val mDataList: MutableList<CharSequence> = ArrayList()
    private var mShouldShowDialog = false
//    private var mPermissionEntities: List<PermissionEntity>? = null
    private val mOnBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(
        true
    ) {
        override fun handleOnBackPressed() {
        }
    }
    private var mPresenter: PermissionSpecPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_permission)
        uploadLaunchPermissionSpecEvent()
        val intent = intent
        // 添加 OnBackPressedCallback 到 Dispatcher
        onBackPressedDispatcher.addCallback(this, mOnBackPressedCallback)
//        mPermissionEntities = intent.getParcelableArrayListExtra("permissions")
        setTitle()
        findViewById<View>(R.id.access).setOnClickListener(this)
        initRecyclerView()
        mPresenter = PermissionSpecPresenter(this, PermissionSpecModel())
    }

    override fun onResume() {
        super.onResume()
//        if (!mShouldShowDialog) {
//            return
//        }
//        val result = mPresenter!!.onlyStrongPermissions(
//            mPermissionEntities!!
//        )
//        if (result.isEmpty()) {
//            setResult(RESULT_OK)
//            finish()
//            return
//        }
//        mShouldShowDialog = false
//        showPermissionRequestDialog(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mOnBackPressedCallback.remove()
    }

    private fun setTitle() {
        try {
            val titleView = findViewById<TextView>(R.id.page_title)
            val pm = packageManager
            val packageInfo = pm.getPackageInfo(packageName, 0)
            val applicationInfo = packageInfo.applicationInfo ?: return
            val name = applicationInfo.loadLabel(pm).toString()
            titleView.text = name
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(logTag, e)
        }
    }

    private fun initRecyclerView() {
        val resources = resources
        val desc = resources.getString(R.string.permission_spec_desc)
        val titles = resources.getStringArray(R.array.permission_spec_titles)
        val contents = resources.getStringArray(R.array.permission_spec_contents)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val adapter = PermissionSpecAdapter(this, mDataList)
        mDataList.add(desc)
        val length = titles.size
        for (i in 0 until length) {
            mDataList.add(titles[i])
            mDataList.add(contents[i])
        }
        recyclerView.adapter = adapter
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.access) {
//            mPresenter!!.request(mPermissionEntities)
            ActivityCompat.requestPermissions(
                this@NewPermissionActivity,
                arrayOf<String>(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
                ),
                100
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        setResult(RESULT_OK)
        finish()
    }

    override fun onPermissionGranted(reqCode: Int) {
//        mPresenter!!.onPermissionGranted(mPermissionEntities!!)
        setResult(RESULT_OK)
        finish()
    }

    override fun onPermissionDenied(requestCode: Int, vararg permissions: String) {
//        mPresenter!!.onPermissionDenied(mPermissionEntities!!)
//        val result = mPresenter!!.onlyStrongPermissions(
//            mPermissionEntities!!
//        )
//        if (result.isEmpty()) {
//            setResult(RESULT_OK)
//            finish()
//            return
//        }
        setResult(RESULT_OK)
            finish()

    }
    override fun requestPermissions(permissionArray: Array<String>) {
        Log.d(logTag, "permissionArray=" + permissionArray.contentToString())
        permissionRequest(this, this, 1, *permissionArray)
    }

    companion object {
        private val ALL_PERMISSIONS: MutableList<String> = ArrayList()

        init {
            ALL_PERMISSIONS.add(Manifest.permission.READ_CALENDAR)
            ALL_PERMISSIONS.add(Manifest.permission.WRITE_CALENDAR)
        }

        fun startPermissionActivity(
            context: Context,
            launcher: ActivityResultLauncher<Intent>
        ) {
            val intent = Intent(context, NewPermissionActivity::class.java)
            launcher.launch(intent)
        }
    }
}