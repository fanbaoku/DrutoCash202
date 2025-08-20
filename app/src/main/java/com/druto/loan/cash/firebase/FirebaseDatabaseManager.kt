package com.druto.loan.cash.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

object FirebaseDatabaseManager {
    val TAG: String = FirebaseDatabaseManager::class.java.simpleName

    fun getHost(listener: OnResultListener) {
        val database = FirebaseDatabase.getInstance(
            "https://fastcashgo718-default-rtdb.firebaseio.com/"
        )
        //地址跟项目经理要
        val myRef = database.reference
        myRef.get().addOnCanceledListener {
            Log.d(TAG, "onCanceled")
            listener.onFail(null)
        }.addOnCompleteListener { task: Task<DataSnapshot> ->
            if (!task.isSuccessful) {
                val exception = task.exception
                Log.e(
                    TAG,
                    "Error getting data",
                    task.exception
                )
                if (exception == null) {
                    listener.onFail(null)
                } else {
                    listener.onFail(exception.message)
                }
            } else {
                //注意获取的数据包含正式域名和测试域名（有test的是测试域名）
                val value = task.result.value
                Log.d(TAG, "value=$value") //firebase返回的数据
                listener.onSuccess(value.toString())
            }
        }.addOnFailureListener { e: Exception ->
            Log.w(TAG, e)
            listener.onFail(e.message)
        }
    }

    interface OnResultListener {
        fun onSuccess(result: String?)
        fun onFail(reason: String?)
    }
}
