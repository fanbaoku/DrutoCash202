package com.druto.loan.cash.drutocash.collector

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.UUID
import kotlin.math.abs

object StorageInfoCollector {
    val TAG: String = StorageInfoCollector::class.java.simpleName
    private const val ERROR: Long = -1L

    fun getInfoList(context: Context): List<Long> {
        val list: MutableList<Long> = ArrayList()
        list.add(getTotalMemorySize(context))
        list.add(getAvailableMemory(context))
        list.add(getAvailableInternalMemorySize(context))
        list.add(getTotalInternalMemorySize(context))
        list.add(getTotalSdcardMemorySize(context))
        list.add(getAvailableSdcardMemorySize(context))
        list.add(getTotalExternalMemorySize(context))
        list.add(getAvailableExternalMemorySize(context))
        return list
    }

    // 总内存大小
    private fun getTotalMemorySize(context: Context?): Long {
        val dir = "/proc/meminfo"
        try {
            val fr = FileReader(dir)
            val br = BufferedReader(fr, 2048)
            val memoryLine = br.readLine()
            val subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"))
            br.close()
            return subMemoryLine.replace("\\D+".toRegex(), "").toInt() * 1024L
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * 内存可用大小
     * 获取当前可用内存，返回数据以字节为单位。
     *
     * @param context 可传入应用程序上下文。
     * @return 当前可用内存单位为B。
     */
    private fun getAvailableMemory(context: Context): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }


    // 可用机身自带存储空间大小
    /**
     * 获取手机内部剩余存储空间
     */
    @SuppressLint("SoonBlockedPrivateApi")
    fun getAvailableInternalMemorySize(context: Context): Long {
//        File path = Environment.getDataDirectory();
//        StatFs stat = new StatFs(path.getPath());
//        long blockSize = stat.getBlockSize();
//        long availableBlocks = stat.getAvailableBlocks();
//        return availableBlocks * blockSize;
        var availableSize: Long = 0
        val storageManager = context.getSystemService(
            Context.STORAGE_SERVICE
        ) as StorageManager
        var unit = 1000f
        val version = Build.VERSION.SDK_INT
        if (version < Build.VERSION_CODES.M) { //小于6.0
            try {
                val getVolumeList = StorageManager::class.java.getDeclaredMethod("getVolumeList")
                val volumeList = getVolumeList.invoke(storageManager) as Array<StorageVolume>

                if (volumeList != null) {
                    var getPathFile: Method? = null
                    for (volume in volumeList) {
                        if (getPathFile == null) {
                            getPathFile = volume.javaClass.getDeclaredMethod("getPathFile")
                        }
                        val file = getPathFile!!.invoke(volume) as File
                        availableSize += file.usableSpace
                    }
                }
            } catch (e: NoSuchMethodException) {
                Log.w(TAG, e)
            } catch (e: IllegalAccessException) {
                Log.w(TAG, e)
            } catch (e: InvocationTargetException) {
                Log.w(TAG, e)
            }
        } else {
            try {
                val getVolumes = StorageManager::class.java.getDeclaredMethod("getVolumes") //6.0
                val getVolumeInfo = getVolumes.invoke(storageManager) as List<Any>

                for (obj in getVolumeInfo) {
                    val getType = obj.javaClass.getField("type")
                    val type = getType.getInt(obj)

                    Log.d(
                        TAG,
                        "type: $type"
                    )
                    if (type == 1) { //TYPE_PRIVATE
//                        long totalSize = 0L;
                        //获取内置内存总大小
                        if (version >= Build.VERSION_CODES.O) { //8.0
                            unit = 1000f
                            val getFsUuid = obj.javaClass.getDeclaredMethod("getFsUuid")
                            val fsUuid = getFsUuid.invoke(obj) as String?
                            //                            totalSize = getTotalSize(context, fsUuid); //8.0 以后使用
                        } else if (version >= Build.VERSION_CODES.N_MR1) { //7.1.1
                            val getPrimaryStorageSize = StorageManager::class.java.getMethod(
                                "getPrimaryStorageSize"
                            ) //5.0 6.0 7.0没有
                            //                            totalSize = (long) getPrimaryStorageSize.invoke(storageManager);
                        }

                        //                        long systemSize = 0L;
                        val isMountedReadable = obj.javaClass.getDeclaredMethod(
                            "isMountedReadable"
                        )
                        val readable = isMountedReadable.invoke(obj) as Boolean
                        if (readable) {
                            val file = obj.javaClass.getDeclaredMethod("getPath")
                            val f = file.invoke(obj) as File

                            //                            if (totalSize == 0) {
//                                totalSize = f.getTotalSpace();
//                            }
//                            systemSize = totalSize - f.getTotalSpace();
                            availableSize += f.freeSpace
                            //                            total += totalSize;
                        }
                    } else if (type == 0) { //TYPE_PUBLIC
                        //外置存储
                        val isMountedReadable = obj.javaClass.getDeclaredMethod(
                            "isMountedReadable"
                        )
                        val readable = isMountedReadable.invoke(obj) as Boolean
                        if (readable) {
                            val file = obj.javaClass.getDeclaredMethod("getPath")
                            val f = file.invoke(obj) as File
                            availableSize += f.freeSpace
                            //                            total += f.getTotalSpace();
                        }
                    } else if (type == 2) { //TYPE_EMULATED
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "need permission.PACKAGE_USAGE_STATS")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return abs(availableSize.toDouble()).toLong()
    }

    // 总机身自带存储空间大小
    /**
     * * 获取手机内部总的存储空间
     * *
     * * @return
     */
    @SuppressLint("SoonBlockedPrivateApi")
    fun getTotalInternalMemorySize(context: Context): Long {
//        File path = Environment.getDataDirectory();
//        StatFs stat = new StatFs(path.getPath());
//        long blockSize = stat.getBlockSize();
//        long totalBlocks = stat.getBlockCount();
//        return totalBlocks * blockSize;
        //5.0 查外置存储
        var total: Long = 0
        var availableSize: Long = 0
        val storageManager = context.getSystemService(
            Context.STORAGE_SERVICE
        ) as StorageManager
        var unit = 1000f
        val version = Build.VERSION.SDK_INT
        if (version < Build.VERSION_CODES.M) { //小于6.0
            try {
                val getVolumeList = StorageManager::class.java.getDeclaredMethod("getVolumeList")
                val volumeList = getVolumeList.invoke(storageManager) as Array<StorageVolume>

                if (volumeList != null) {
                    var getPathFile: Method? = null
                    for (volume in volumeList) {
                        if (getPathFile == null) {
                            getPathFile = volume.javaClass.getDeclaredMethod("getPathFile")
                        }
                        val file = getPathFile!!.invoke(volume) as File
                        total += file.totalSpace
                        availableSize += file.usableSpace
                    }
                }
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        } else {
            try {
                val getVolumes = StorageManager::class.java.getDeclaredMethod("getVolumes") //6.0
                val getVolumeInfo = getVolumes.invoke(storageManager) as List<Any>

                for (obj in getVolumeInfo) {
                    val getType = obj.javaClass.getField("type")
                    val type = getType.getInt(obj)

                    Log.d(
                        TAG,
                        "type: $type"
                    )
                    if (type == 1) { //TYPE_PRIVATE

                        var totalSize = 0L

                        //获取内置内存总大小
                        if (version >= Build.VERSION_CODES.O) { //8.0
                            unit = 1000f
                            val getFsUuid = obj.javaClass.getDeclaredMethod("getFsUuid")
                            val fsUuid = getFsUuid.invoke(obj) as String?
                            totalSize = getTotalSize(context, fsUuid) //8.0 以后使用
                        } else if (version >= Build.VERSION_CODES.N_MR1) { //7.1.1
                            val getPrimaryStorageSize = StorageManager::class.java.getMethod(
                                "getPrimaryStorageSize"
                            ) //5.0 6.0 7.0没有
                            totalSize = getPrimaryStorageSize.invoke(storageManager) as Long
                        }
                        var systemSize = 0L

                        val isMountedReadable = obj.javaClass.getDeclaredMethod(
                            "isMountedReadable"
                        )
                        val readable = isMountedReadable.invoke(obj) as Boolean
                        if (readable) {
                            val file = obj.javaClass.getDeclaredMethod("getPath")
                            val f = file.invoke(obj) as File

                            if (totalSize == 0L) {
                                totalSize = f.totalSpace
                            }
                            systemSize = totalSize - f.totalSpace
                            availableSize += f.freeSpace
                            total += totalSize
                        }
                    } else if (type == 0) { //TYPE_PUBLIC
                        //外置存储
                        val isMountedReadable = obj.javaClass.getDeclaredMethod(
                            "isMountedReadable"
                        )
                        val readable = isMountedReadable.invoke(obj) as Boolean
                        if (readable) {
                            val file = obj.javaClass.getDeclaredMethod("getPath")
                            val f = file.invoke(obj) as File
                            availableSize += f.freeSpace
                            total += f.totalSpace
                        }
                    } else if (type == 2) { //TYPE_EMULATED
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "need permission.PACKAGE_USAGE_STATS")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return abs(total.toDouble()).toLong()
    }


    /**
     * API 26 android O
     * 获取总共容量大小，包括系统大小
     */
    @SuppressLint("NewApi")
    fun getTotalSize(context: Context, fsUuid: String?): Long {
        var var3: Long
        try {
            var id: UUID? = null
            val var10000 = if (fsUuid == null) {
                StorageManager.UUID_DEFAULT
            } else {
                UUID.fromString(fsUuid)
            }
            id = var10000
            val stats = context.getSystemService(
                StorageStatsManager::class.java
            )
            var3 = stats.getTotalBytes(id)
        } catch (var6: NoSuchFieldError) {
            var6.printStackTrace()
            var3 = 0L
        } catch (var7: NoClassDefFoundError) {
            var7.printStackTrace()
            var3 = 0L
        } catch (var8: NullPointerException) {
            var8.printStackTrace()
            var3 = 0L
        } catch (var9: IOException) {
            var9.printStackTrace()
            var3 = 0L
        }
        return var3
    }

    // 外接SD卡或U盘总存储空间大小
    /**
     * * 获取SDCARD总的存储空间 注：需要判断sdcard是否存在，存在就获取，不存在传0
     * *
     * * @return
     */
    @SuppressLint("SoonBlockedPrivateApi")
    fun getTotalSdcardMemorySize(context: Context): Long {
        //5.0 查外置存储
        var total: Long = 0
        var availableSize: Long = 0
        val storageManager = context.getSystemService(
            Context.STORAGE_SERVICE
        ) as StorageManager
        val unit = 1000f
        val version = Build.VERSION.SDK_INT
        if (version < Build.VERSION_CODES.M) { //小于6.0
            try {
                val getVolumeList = StorageManager::class.java.getDeclaredMethod("getVolumeList")
                val volumeList = getVolumeList.invoke(storageManager) as Array<StorageVolume>

                if (volumeList != null) {
                    var getPathFile: Method? = null
                    for (volume in volumeList) {
                        if (getPathFile == null) {
                            getPathFile = volume.javaClass.getDeclaredMethod("getPathFile")
                        }
                        val file = getPathFile!!.invoke(volume) as File
                        total += file.totalSpace
                        availableSize += file.usableSpace
                    }
                }
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        } else {
            try {
                val getVolumes = StorageManager::class.java.getDeclaredMethod("getVolumes") //6.0
                val getVolumeInfo = getVolumes.invoke(storageManager) as List<Any>

                for (obj in getVolumeInfo) {
                    val getType = obj.javaClass.getField("type")
                    val type = getType.getInt(obj)

                    Log.d(
                        TAG,
                        "type: $type"
                    )
                    if (type == 0) { //TYPE_PUBLIC
                        //外置存储
                        val isMountedReadable = obj.javaClass.getDeclaredMethod(
                            "isMountedReadable"
                        )
                        val readable = isMountedReadable.invoke(obj) as Boolean
                        if (readable) {
                            val file = obj.javaClass.getDeclaredMethod("getPath")
                            val f = file.invoke(obj) as File
                            availableSize += f.freeSpace
                            total += f.totalSpace
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "need permission.PACKAGE_USAGE_STATS")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return total
    }

    // 外接SD卡或U盘总存储空间已使用量
    /**
     * * 获取SDCARD已用存储空间  注：需要判断sdcard是否存在，存在就获取，不存在传0
     * *
     * * @return
     */
    @SuppressLint("SoonBlockedPrivateApi")
    fun getAvailableSdcardMemorySize(context: Context): Long {
        //5.0 查外置存储

        var total: Long = 0
        var availableSize: Long = 0
        val storageManager = context.getSystemService(
            Context.STORAGE_SERVICE
        ) as StorageManager
        val unit = 1000f
        val version = Build.VERSION.SDK_INT
        if (version < Build.VERSION_CODES.M) { //小于6.0
            try {
                val getVolumeList = StorageManager::class.java.getDeclaredMethod("getVolumeList")
                val volumeList = getVolumeList.invoke(storageManager) as Array<StorageVolume>

                if (volumeList != null) {
                    var getPathFile: Method? = null
                    for (volume in volumeList) {
                        if (getPathFile == null) {
                            getPathFile = volume.javaClass.getDeclaredMethod("getPathFile")
                        }
                        val file = getPathFile!!.invoke(volume) as File
                        total += file.totalSpace
                        availableSize += file.usableSpace
                    }
                }
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        } else {
            try {
                val getVolumes = StorageManager::class.java.getDeclaredMethod("getVolumes") //6.0
                val getVolumeInfo = getVolumes.invoke(storageManager) as List<Any>

                for (obj in getVolumeInfo) {
                    val getType = obj.javaClass.getField("type")
                    val type = getType.getInt(obj)

                    Log.d(
                        TAG,
                        "type: $type"
                    )
                    if (type == 0) { //TYPE_PUBLIC
                        //外置存储
                        val isMountedReadable = obj.javaClass.getDeclaredMethod(
                            "isMountedReadable"
                        )
                        val readable = isMountedReadable.invoke(obj) as Boolean
                        if (readable) {
                            val file = obj.javaClass.getDeclaredMethod("getPath")
                            val f = file.invoke(obj) as File
                            availableSize += f.freeSpace
                            total += f.totalSpace
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "need permission.PACKAGE_USAGE_STATS")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return total - availableSize
    }

    // Environment.getExternalStorageDirectory总的存储空间
    /**
     * * 获取Environment.getExternalStorageDirectory总的存储空间
     * *
     * * @return
     */
    fun getTotalExternalMemorySize(context: Context?): Long {
        if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSize.toLong()
            val totalBlocks = stat.blockCount.toLong()
            return totalBlocks * blockSize
        } else {
            return ERROR
        }
    }

    /**
     * * SDCARD是否存
     */
    fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    // Environment.getExternalStorageDirectory可用存储空间
    /**
     * * 获取Environment.getExternalStorageDirectory可用存储空间
     * *
     * * @return
     */
    private fun getAvailableExternalMemorySize(context: Context?): Long {
        if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSize.toLong()
            val availableBlocks = stat.availableBlocksLong
            return availableBlocks * blockSize
        } else {
            return ERROR
        }
    }
}
