package com.cash.ease.money.cashease.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cash.ease.money.cashease.R
import com.cash.ease.money.cashease.model.PhotoModel
import com.cash.ease.money.cashease.presenter.PhotoPresenter
import com.cash.ease.money.cashease.ui.PermissionCameraDialog
import com.cash.ease.money.cashease.ui.PermissionLocationDialog
import com.cash.ease.money.cashease.utils.ToastUtil.showToast
import com.cash.ease.money.cashease.ui.base.BaseAppCompatActivity
import com.cash.ease.money.cashease.view.PhotoView
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : BaseAppCompatActivity(), PhotoView {
    private var previewView: PreviewView? = null
    private var photoView: ImageView? = null
    override var imageCapture: ImageCapture? = null
        private set
    private var cameraExecutor: ExecutorService? = null
    private var previewButtonLayout: View? = null
    private var photoConfirmLayout: View? = null
    private var resultFile: File? = null
    override var type: Int = 0
        private set
    private var borderView: View? = null
    private var cardBorderView: View? = null
    private var mPhotoPresenter: PhotoPresenter? = null
    var permissionCameraDialog:PermissionCameraDialog?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_id_photo)
        val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (locationPermission!= PackageManager.PERMISSION_GRANTED){
            permissionCameraDialog= PermissionCameraDialog(this@CameraActivity,object :
                PermissionCameraDialog.OnCameraClick{
                override fun onCameraClick() {
                    permissionCameraDialog!!.dismiss()
                    ActivityCompat.requestPermissions(
                        this@CameraActivity,
                        arrayOf<String>(
                            Manifest.permission.CAMERA
                        ),
                        100
                    )
                }
            })
            permissionCameraDialog!!.show()
        }else{
            type = intent.getIntExtra("type", TYPE_CAMERA)
            borderView = findViewById(R.id.border)
            cardBorderView = findViewById(R.id.card_border)

            setHintAndTitle()
            showBorderView()
            previewButtonLayout = findViewById(R.id.preview_button_layout)
            photoConfirmLayout = findViewById(R.id.photo_confirm_layout)
            previewView = findViewById(R.id.preview_view)
            photoView = findViewById(R.id.photo_view) // 假设用于显示照片的ImageView
            cameraExecutor = Executors.newSingleThreadExecutor()
            mPhotoPresenter = PhotoPresenter(this, PhotoModel(type))
            setPreview(true)
            startCamera() // 初始化相机
            findViewById<View>(R.id.back).setOnClickListener { finish() } // 点击拍照
            findViewById<View>(R.id.take_photo).setOnClickListener { takePhoto() } // 点击拍照
            findViewById<View>(R.id.photo_cancel).setOnClickListener {
                // resultFile = null;
                mPhotoPresenter!!.onPhotoCancel()
            }
            findViewById<View>(R.id.photo_confirm).setOnClickListener { mPhotoPresenter!!.onPhotoUpload() }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        System.out.println("requestCode----------->"+requestCode)
        System.out.println("permissions----------->"+permissions)
        System.out.println("grantResults----------->"+grantResults)
        if (requestCode==100){
            val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            if (locationPermission!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                    this@CameraActivity,
                    arrayOf<String>(
                        Manifest.permission.CAMERA
                    ),
                    100
                )
            }else{
                type = intent.getIntExtra("type", TYPE_CAMERA)
                borderView = findViewById(R.id.border)
                cardBorderView = findViewById(R.id.card_border)

                setHintAndTitle()
                showBorderView()
                previewButtonLayout = findViewById(R.id.preview_button_layout)
                photoConfirmLayout = findViewById(R.id.photo_confirm_layout)
                previewView = findViewById(R.id.preview_view)
                photoView = findViewById(R.id.photo_view) // 假设用于显示照片的ImageView
                cameraExecutor = Executors.newSingleThreadExecutor()
                mPhotoPresenter = PhotoPresenter(this, PhotoModel(type))
                setPreview(true)
                startCamera() // 初始化相机
                findViewById<View>(R.id.back).setOnClickListener { finish() } // 点击拍照
                findViewById<View>(R.id.take_photo).setOnClickListener { takePhoto() } // 点击拍照
                findViewById<View>(R.id.photo_cancel).setOnClickListener {
                    // resultFile = null;
                    mPhotoPresenter!!.onPhotoCancel()
                }
                findViewById<View>(R.id.photo_confirm).setOnClickListener { mPhotoPresenter!!.onPhotoUpload() }
            }
        }
    }

    override fun finishWithResult() {
        val intent = Intent()
        intent.putExtra("filepath", resultFile!!.absolutePath)
        intent.putExtra("type", type)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun startCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val lensFacing = if (type == TYPE_CAMERA) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
                val cameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                val previewUseCase =
                    Preview.Builder().build()
                previewUseCase.setSurfaceProvider(previewView!!.surfaceProvider)

                imageCapture = ImageCapture.Builder().build() // 拍照用例

                cameraProvider.unbindAll() // 解绑之前的用例
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    previewUseCase,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.w(logTag, e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        mPhotoPresenter!!.onTakePhoto()
        if (imageCapture == null) return

        imageCapture!!.takePicture(cameraExecutor!!, object : OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                resultFile = mPhotoPresenter!!.onCaptureSuccess(imageProxy)
            }

            override fun onError(e: ImageCaptureException) {
                resultFile = null
                Log.e(logTag, "Photo capture failed: " + e.message, e)
            }
        })
    }

    override fun setPreview(preview: Boolean) {
        if (preview) {
            previewView!!.visibility = View.VISIBLE
            photoView!!.visibility = View.GONE
            previewButtonLayout!!.visibility = View.VISIBLE
            photoConfirmLayout!!.visibility = View.GONE
        } else {
            previewView!!.visibility = View.GONE
            photoView!!.visibility = View.VISIBLE
            previewButtonLayout!!.visibility = View.GONE
            photoConfirmLayout!!.visibility = View.VISIBLE
        }
    }

    override fun displayImage(bitmap: Bitmap) {
        // 显示拍摄的图片
        runOnUiThread {
            photoView!!.setImageBitmap(bitmap)
            setPreview(false)
        }
    }

    override fun displayImageError(reason: String) {
        runOnUiThread {
            photoView!!.setImageBitmap(null)
            setPreview(true)
            showToast(this, reason)
        }
    }

    private fun showBorderView() {
        if (type == TYPE_CAMERA) {
            borderView!!.visibility = View.VISIBLE
            cardBorderView!!.visibility = View.GONE
        } else {
            borderView!!.visibility = View.GONE
            cardBorderView!!.visibility = View.VISIBLE
        }
    }

    private fun setHintAndTitle() {
        val titleView = findViewById<TextView>(R.id.toolbar_title)
        val hintView = findViewById<TextView>(R.id.hint)
        if (type == TYPE_CAMERA) {
            titleView.setText(R.string.id_take_photo_title)
            hintView.setText(R.string.id_take_photo_hint)
        } else {
            titleView.setText(R.string.id_camera_title)
            hintView.setText(R.string.id_camera_hint)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor!!.shutdown() // 释放线程池资源
    }

    companion object {
        const val TYPE_CAMERA: Int = 1 // 调用静默活体
        const val TYPE_PHOTO: Int = 2 // 选民证
        fun startIdPhotoActivity(
            context: Context?, type: Int,
            launcher: ActivityResultLauncher<Intent>
        ) {
            val intent = Intent(context, CameraActivity::class.java)
            intent.putExtra("type", type)
            launcher.launch(intent)
        }
    }
}
