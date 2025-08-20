package com.druto.loan.cash.drutocash.view

import android.graphics.Bitmap
import androidx.camera.core.ImageCapture

interface PhotoView {
    val type: Int
    val imageCapture: ImageCapture?

    fun finishWithResult()

    fun setPreview(preview: Boolean)

    fun displayImage(bitmap: Bitmap)
    fun displayImageError(reason: String)
}
