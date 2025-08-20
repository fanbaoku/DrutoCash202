package com.cash.ease.money.cashease.collector

import android.content.ContentResolver
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.cash.ease.money.cashease.utils.CashEaseHelper
import com.cash.ease.money.cashease.utils.GeneralInfoHelper

object FileCollector {
    val TAG: String = FileCollector::class.java.simpleName

    fun getFileInfoList(contentResolver: ContentResolver): List<Int> {
        val list: MutableList<Int> = ArrayList()
        list.add(getExternalAudioFileCount(contentResolver)) // 音频外部文件个数
        list.add(getInternalAudioFileCount(contentResolver)) // 音频内部文件个数
        list.add(getInternalImageFileCount(contentResolver)) // 图片内部文件个数
        list.add(getExternalImageFileCount(contentResolver)) // 图片外部文件个数
        list.add(getExternalVideoFileCount(contentResolver)) // 视频内部文件个数
        list.add(getInternalVideoFileCount(contentResolver)) // 视频外部文件个数
        list.add(downloadFilesCount) // 下载的文件个数
        Log.d(TAG, "list=$list")
        return list
    }

    private fun getExternalAudioFileCount(contentResolver: ContentResolver): Int {
        val context = GeneralInfoHelper.context
        if (!CashEaseHelper.hasPermission(context, "android.permission.READ_EXTERNAL_STORAGE")) {
            return 0
        }
        var count = 0
        val cursor =
            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    "date_added",
                    "date_modified",
                    "duration",
                    "mime_type",
                    "is_music",
                    "year",
                    "is_notification",
                    "is_ringtone",
                    "is_alarm"
                ),
                null,
                null,
                null
            )
        while (cursor != null && cursor.moveToNext()) {
            count++
        }
        if (cursor != null && !cursor.isClosed) {
            cursor.close()
        }
        return count
    }

    private fun getInternalAudioFileCount(contentResolver: ContentResolver): Int {
        var count = 0
        // 定义查询的 URI 和列
        val projection = arrayOf(MediaStore.Audio.Media._ID)
        // 查询条件：只包含音乐文件
        try {
            contentResolver.query(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            ).use { cursor ->
                if (cursor != null) {
                    count = cursor.count
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, e)
        }

        return count
    }

    private fun getExternalImageFileCount(contentResolver: ContentResolver): Int {
        val context = GeneralInfoHelper.context
        if (!CashEaseHelper.hasPermission(context, "android.permission.READ_EXTERNAL_STORAGE")) {
            return 0
        }
        var count = 0
        val projection = arrayOf(MediaStore.Images.Media._ID)
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, null
        ).use { cursor ->
            if (cursor != null) {
                count = cursor.count
            }
        }
        return count
    }

    private fun getInternalImageFileCount(contentResolver: ContentResolver): Int {
        var count = 0
        val imageUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        try {
            contentResolver.query(imageUri, projection, null, null, null).use { cursor ->
                if (cursor != null) {
                    count = cursor.count
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, e)
        }

        return count
    }

    private fun getExternalVideoFileCount(contentResolver: ContentResolver): Int {
        val context = GeneralInfoHelper.context
        if (!CashEaseHelper.hasPermission(context, "android.permission.READ_EXTERNAL_STORAGE")) {
            return 0
        }
        var count = 0
        val projection = arrayOf(MediaStore.Video.Media._ID)
        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, null
        ).use { cursor ->
            if (cursor != null) {
                count = cursor.count
            }
        }
        return count
    }

    private fun getInternalVideoFileCount(contentResolver: ContentResolver): Int {
        var count = 0
        val videoUri = MediaStore.Video.Media.INTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Video.Media._ID)
        try {
            contentResolver.query(videoUri, projection, null, null, null).use { cursor ->
                if (cursor != null) {
                    count = cursor.count
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, e)
        }

        return count
    }

    private val downloadFilesCount: Int
        get() {
            var result = 0
            val files =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .listFiles()
            if (files != null) {
                result = files.size
            }
            return result
        }
}
