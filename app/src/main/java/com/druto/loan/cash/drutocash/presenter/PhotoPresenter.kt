package com.druto.loan.cash.drutocash.presenter

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.druto.loan.cash.drutocash.model.PhotoModel
import com.druto.loan.cash.drutocash.model.PhotoModel.OnSaveImageListener
import com.druto.loan.cash.drutocash.view.PhotoView
import java.io.File

class PhotoPresenter(private val view: PhotoView, private val model: PhotoModel) {
    fun onPhotoCancel() {
        model.uploadCancelEvent()
        view.setPreview(true)
    }

    fun onPhotoUpload() {
        model.uploadConfirmEvent()
        view.finishWithResult()
    }

    fun onTakePhoto() {
        model.uploadTakePhotoEvent()
    }

    fun onCaptureSuccess(imageProxy: ImageProxy): File? {
        return model.onCaptureSuccess(imageProxy, object : OnSaveImageListener {
            override fun onSaveSuccess(bitmap: Bitmap) {
                view.displayImage(bitmap)
            }

            override fun onSaveFailed(reason: String) {
                view.displayImageError(reason)
            }
        })
    }
}
