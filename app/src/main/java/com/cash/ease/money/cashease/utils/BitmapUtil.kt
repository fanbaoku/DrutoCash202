package com.cash.ease.money.cashease.utils

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by su on 17-3-13.
 */
object BitmapUtil {
    private val TAG: String = BitmapUtil::class.java.simpleName

    private fun saveBitmap(bitmap: Bitmap, filename: String, format: CompressFormat, quality: Int): File? {
        val file = File(filename)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(filename)
            bitmap.compress(format, quality, out)
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "filename: $filename", e)
            return null
        } finally {
            close(out)
        }
        return file
    }

    private fun compressBitmapFromFile(file: File, inSampleSize: Int): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(file.absolutePath, options)
    }

    @Throws(IOException::class)
    fun compressImage(originFile: File, newFile: File): File {
        val bitmap = compressBitmapFromFile(originFile, 2)
        val compressedFile = saveBitmap(
            bitmap,
            newFile.absolutePath,
            CompressFormat.JPEG,
            80
        )
            ?: throw IOException("compress error: " + originFile.absolutePath)
        return compressedFile
    }

    private fun close(closeable: AutoCloseable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: Exception) {
                Log.w(TAG, e)
            }
        }
    }
}
