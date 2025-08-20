package com.druto.loan.cash.drutocash.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadSilentLivingFailEvent
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadCameraGoofTakePhotoButtonEvent
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadCameraGoofTakePhotoCancelEvent
import com.druto.loan.cash.drutocash.service.MyEventUploader.uploadCameraGoofTakePhotoConfirmEvent
import com.druto.loan.cash.drutocash.activity.CameraActivity
import com.druto.loan.cash.drutocash.utils.GeneralInfoHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 806没有选民证
 * */
class PhotoModel(private val mType: Int) {
    fun uploadTakePhotoEvent() {
        if (mType == CameraActivity.TYPE_CAMERA) {
            uploadCameraGoofTakePhotoButtonEvent()
        }
    }

    fun uploadConfirmEvent() {
        if (mType == CameraActivity.TYPE_CAMERA) {
            uploadCameraGoofTakePhotoConfirmEvent()
        }
    }

    fun uploadCancelEvent() {
        if (mType == CameraActivity.TYPE_CAMERA) {
            uploadCameraGoofTakePhotoCancelEvent()
        }
    }

    fun onCaptureSuccess(imageProxy: ImageProxy, listener: OnSaveImageListener): File? {
        val context = GeneralInfoHelper.context
        try {
            imageProxy.use {
                // 从 ImageProxy 获取旋转角度
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                // 将 ImageProxy 转换为 Bitmap
                val bitmap = imageProxyToBitmap(imageProxy)
                val result =
                    rotateAndMirrorBitmap(
                        bitmap,
                        rotationDegrees,
                        mType == CameraActivity.TYPE_CAMERA
                    )
                // 保存或显示处理后的图片
                val resultFile = File(context.getExternalFilesDir(null), "corrected_photo.jpg")
                saveBitmapToFile(result, resultFile)
                listener.onSaveSuccess(result)
                return resultFile
            }
        } catch (e: Throwable) {
            uploadSilentLivingFailEvent(e.message ?: "")
            listener.onSaveFailed(e.message ?: "")
        }
        return null
    }

    // 图片旋转和镜像处理
    private fun rotateAndMirrorBitmap(bitmap: Bitmap, rotationDegrees: Int, mirror: Boolean): Bitmap {
        val matrix = Matrix()
        // 旋转
        if (rotationDegrees != 0) {
            matrix.postRotate(rotationDegrees.toFloat())
        }
        if (mirror) {
            // 前置摄像头需要进行水平镜像
            matrix.postScale(-1f, 1f)
        }
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix,
            true
        )
    }

    // ImageProxy 转换为 Bitmap
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer[bytes]
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    // 保存 Bitmap 到文件
    private fun saveBitmapToFile(bitmap: Bitmap, file: File?) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            Log.w(TAG, e)
        }
    }

    interface OnSaveImageListener {
        fun onSaveSuccess(bitmap: Bitmap)
        fun onSaveFailed(reason: String)
    }

    companion object {
        val TAG: String = CameraActivity::class.java.simpleName
    }
}
