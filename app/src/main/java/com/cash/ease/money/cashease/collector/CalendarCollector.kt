package com.cash.ease.money.cashease.collector

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.util.Log
import androidx.annotation.WorkerThread
import com.cash.ease.money.cashease.BuildConfig
import com.cash.ease.money.cashease.utils.CashEaseHelper
import com.cash.ease.money.cashease.utils.ThreadUtil.getSimpleDateFormat
import java.util.Date
import java.util.TimeZone

/**
 * 1. 获取所有事件
 * 2. 用事件ID查询reminders
 */
object CalendarCollector {
    const val TAG: String = "COLLECTOR-CALENDAR"
    private const val CALENDAR_NAME = "Default-Account"
    private const val CALENDAR_ACCOUNT_NAME = "Default-Account"
    private const val CALENDAR_DISPLAY_NAME = "Default-Account"

    @WorkerThread
    fun fetchCalendarEvents(context: Context): List<List<Any?>> {
        val lists: MutableList<List<Any?>> = ArrayList()
        if (!CashEaseHelper.hasPermission(context, Manifest.permission.READ_CALENDAR)) {
            return lists
        }
        val contentResolver = context.contentResolver
        val sdf = getSimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        // 日历事件的URI
        val eventsUri = CalendarContract.Events.CONTENT_URI
        Log.d(TAG, "eventsUri=$eventsUri")
        Log.d(TAG, "remindersUri=" + CalendarContract.Reminders.CONTENT_URI)

        // 查询日历事件
        try {
            contentResolver.query(eventsUri, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        val list: MutableList<Any?> = ArrayList()
                        // 获取字段内容
                        val eventId = cursor.getLong(
                            cursor.getColumnIndexOrThrow(CalendarContract.Events._ID)
                        )
                        val title = cursor.getString(
                            cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                        )
                        val description = cursor.getString(
                            cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION)
                        )
                        val location = cursor.getString(
                            cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION)
                        )
                        val startTime = cursor.getLong(
                            cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
                        )
                        val endTime = cursor.getLong(
                            cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND)
                        )
                        val hasAlarm = cursor.getInt(
                            cursor.getColumnIndexOrThrow(CalendarContract.Events.HAS_ALARM)
                        )
                        val reminder = fetchEventReminders(contentResolver, eventId)
                        val date = sdf.format(Date(startTime))
                        if (BuildConfig.DEBUG) {
                            val content =
                                ("Event ID: " + eventId + ", Title: " + title + ", Description: "
                                        + description + ", Start Time: " + date
                                        + ", End Time: " + sdf.format(Date(endTime))
                                        + ", Location: " + location + ", Has Alarm: " + hasAlarm
                                        + ", reminder: " + reminder)
                            Log.d(TAG, "calendar=$content")
                        }
                        list.add(eventId)
                        list.add(title)
                        list.add(description)
                        list.add(location)
                        list.add(startTime)
                        list.add(endTime)
                        list.add(hasAlarm)
                        list.add(reminder)
                        lists.add(list)
                        if (!reminder.isEmpty()) {
                            Log.w(TAG, "eventId=" + eventId + ", reminderSize=" + reminder.size)
                        }
                    } while (cursor.moveToNext())
                } else {
                    Log.d(TAG, "No upcoming calendar events found.")
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, e)
        }
        //        lists.sort((o1, o2) -> {
//            long id1 = (long) o1.get(0);
//            long id2 = (long) o2.get(0);
//            return Long.compare(id1, id2);
//        });
        return lists
    }

    private fun fetchEventReminders(contentResolver: ContentResolver, eventId: Long): List<Any> {
        val lists: MutableList<Any> = ArrayList()
        // 提醒表的 URI
        val remindersUri = CalendarContract.Reminders.CONTENT_URI
        // 查询条件（例如仅查询未来的事件）
        val selection = CalendarContract.Reminders.EVENT_ID + " = ?"
        val selectionArgs = arrayOf(eventId.toString())
        // 查询字段
        val projection = arrayOf(
            CalendarContract.Reminders._ID,  // Reminder ID
            CalendarContract.Reminders.EVENT_ID,  // 事件 ID
            CalendarContract.Reminders.METHOD,  // 提醒方式
            CalendarContract.Reminders.MINUTES // 提前时间（分钟）
        )

        contentResolver.query(
            remindersUri, projection, selection,
            selectionArgs, null
        ).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val list: MutableList<Any?> = ArrayList()
                    val reminderId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(CalendarContract.Reminders._ID)
                    )
                    val method = cursor.getInt(
                        cursor.getColumnIndexOrThrow(CalendarContract.Reminders.METHOD)
                    )
                    val minutes = cursor.getInt(
                        cursor.getColumnIndexOrThrow(CalendarContract.Reminders.MINUTES)
                    )

                    if (BuildConfig.DEBUG) {
                        val content = ("Reminder ID: " + reminderId + ", Method: " + method
                                + ", Minutes before: " + minutes)
                        Log.d(TAG, "reminder=$content")
                    }

                    list.add(reminderId)
                    list.add(method)
                    list.add(minutes)
                    lists.add(list)
                } while (cursor.moveToNext())
            }
        }
        return lists
    }

    /**
     * 添加系统日历事件并设置提醒
     *
     * @param context          上下文
     * @param eventId          事件ID（用于查询或创建）
     * @param title            事件标题
     * @param description      事件描述
     * @param dtstart          开始时间（毫秒时间戳）
     * @param dtend            结束时间（毫秒时间戳）
     * @param reminderMinutes  提前提醒时间（分钟）
     * @return 返回事件ID
     */
    fun addOrUpdateCalendarEvent(
        context: Context, eventId: Long,
        title: String?, description: String?, dtstart: Long, dtend: Long, reminderMinutes: Int
    ): Long {
        val cr = context.contentResolver
        val calendarId = obtainCalendarAccountID(context)

        if (calendarId == -1L) {
            throw RuntimeException("No calendar account available.")
        }

        // 查询是否已存在相同标题和日期的事件
        val cursor = cr.query(
            CalendarContract.Events.CONTENT_URI,
            arrayOf(CalendarContract.Events._ID),
            CalendarContract.Events._ID + " = ?",
            arrayOf(eventId.toString()),
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            // 事件已存在，返回其ID
            val existingEventId = cursor.getLong(0)
            cursor.close()
            if (eventId == existingEventId) {
                Log.w(
                    TAG,
                    "calendar event $eventId exists, request ignore"
                )
                return existingEventId
            }
        }
        cursor?.close()

        // 如果不存在，创建新的事件
        val values = ContentValues()
        values.put(CalendarContract.Events._ID, eventId)
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId)
        values.put(CalendarContract.Events.TITLE, title)
        values.put(CalendarContract.Events.DESCRIPTION, description)
        values.put(CalendarContract.Events.DTSTART, dtstart)
        values.put(CalendarContract.Events.DTEND, dtend)
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")

        var newId = eventId
        val eventUri = cr.insert(CalendarContract.Events.CONTENT_URI, values)
        if (eventUri != null) {
            newId = eventUri.lastPathSegment!!.toLong()
        } else {
            Log.w(TAG, "Failed to insert event.")
        }

        // 设置提醒
        val reminderValues = ContentValues()
        reminderValues.put(CalendarContract.Reminders.EVENT_ID, newId)
        reminderValues.put(CalendarContract.Reminders.MINUTES, reminderMinutes)
        reminderValues.put(
            CalendarContract.Reminders.METHOD,
            CalendarContract.Reminders.METHOD_ALERT
        )

        val newReminderUri = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
        if (newReminderUri == null) {
            Log.w(TAG, "Failed to add reminder.")
        }

        return newId
    }

    private fun obtainCalendarAccountID(context: Context): Long {
        val calID = checkCalendarAccount(context)
        if (calID >= 0) {
            return calID
        }
        return createCalendarAccount(context)
    }

    private fun createCalendarAccount(context: Context): Long {
        val accountUri: Uri?
        val checkSelfPermission: Int
        val uri = CalendarContract.Calendars.CONTENT_URI
        val account = ContentValues()
        account.put("account_type", "LOCAL")
        account.put("name", CALENDAR_NAME)
        account.put("account_name", CALENDAR_ACCOUNT_NAME)
        account.put("calendar_displayName", CALENDAR_DISPLAY_NAME)
        account.put("calendar_color", Color.parseColor("#515bd4"))
        account.put("calendar_access_level", 700)
        account.put("visible", 1)
        account.put("calendar_timezone", TimeZone.getDefault().id)
        account.put("canModifyTimeZone", 1)
        account.put("sync_events", 1)
        account.put("ownerAccount", CALENDAR_ACCOUNT_NAME)
        account.put("canOrganizerRespond", 1)
        account.put("maxReminders", 8)
        account.put("allowedReminders", "0,1,2,3,4")
        account.put("allowedAvailability", "0,1,2")
        account.put("allowedAttendeeTypes", "0,1,2")
        val uri2 = uri.buildUpon()
            .appendQueryParameter("caller_is_syncadapter", "true")
            .appendQueryParameter("account_name", CALENDAR_ACCOUNT_NAME)
            .appendQueryParameter("account_type", "calendar_location")
            .build()
        if (Build.VERSION.SDK_INT >= 23) {
            checkSelfPermission = context.checkSelfPermission("android.permission.WRITE_CALENDAR")
            if (checkSelfPermission == 0) {
                accountUri = context.contentResolver.insert(uri2, account)
            } else {
                return -2L
            }
        } else {
            accountUri = context.contentResolver.insert(uri2, account)
        }
        if (accountUri == null) {
            return -1L
        }
        return ContentUris.parseId(accountUri)
    }

    private fun checkCalendarAccount(context: Context): Long {
        val cursor =
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                null,
                null,
                null,
                null
            )
                ?: return -1L
        try {
            val count = cursor.count
            if (count > 0) {
                cursor.moveToFirst()
                val id =
                    cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                        .toLong()
                cursor.close()
                return id
            }
            cursor.close()
            return -1L
        } catch (t: Throwable) {
            try {
                cursor.close()
            } catch (t2: Throwable) {
                t.addSuppressed(t2)
                Log.w(TAG, t2)
            }
        }
        return -1L
    }

    class EventParam {
        var id: Long = 0
        var title: String? = null
        var description: String? = null
        var dtstart: Long = 0
        var dtend: Long = 0
        var reminderMinutes: Int = 0

        override fun toString(): String {
            return "EventParam{" +
                    "eventId=" + id +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", dtstart=" + dtstart +
                    ", dtend=" + dtend +
                    ", reminderMinutes=" + reminderMinutes +
                    '}'
        }
    }
}
