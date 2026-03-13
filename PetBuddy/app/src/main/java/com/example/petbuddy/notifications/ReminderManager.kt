package com.example.petbuddy.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.petbuddy.receivers.EventReminderReceiver
import com.example.petbuddy.receivers.FeedingReminderReceiver
import java.util.Calendar
import java.util.Date

object ReminderManager {

    fun scheduleFeedingReminder(
        context: Context,
        petId: String,
        petName: String,
        hour: Int,
        minute: Int,
        repeatType: String,
        days: List<String>
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        when (repeatType) {
            "once", "everyday" -> {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                scheduleFeedingAlarm(
                    context = context,
                    alarmManager = alarmManager,
                    petId = petId,
                    petName = petName,
                    hour = hour,
                    minute = minute,
                    calendar = calendar
                )
            }

            "custom" -> {
                days.forEach { day ->
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val targetDay = getDayOfWeek(day)
                    val today = calendar.get(Calendar.DAY_OF_WEEK)
                    var diff = targetDay - today
                    if (diff < 0) diff += 7

                    calendar.add(Calendar.DAY_OF_MONTH, diff)

                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                        calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    }

                    scheduleFeedingAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        petId = petId,
                        petName = petName,
                        hour = hour,
                        minute = minute,
                        calendar = calendar,
                        day = day
                    )
                }
            }
        }
    }

    private fun scheduleFeedingAlarm(
        context: Context,
        alarmManager: AlarmManager,
        petId: String,
        petName: String,
        hour: Int,
        minute: Int,
        calendar: Calendar,
        day: String = ""
    ) {
        val intent = Intent(context, FeedingReminderReceiver::class.java).apply {
            putExtra(ReminderConstants.EXTRA_TYPE, ReminderConstants.TYPE_FEEDING)
            putExtra(ReminderConstants.EXTRA_PET_ID, petId)
            putExtra(ReminderConstants.EXTRA_PET_NAME, petName)
        }

        val requestCode = (petId + hour + minute + day).hashCode()

        if (isAlarmScheduled(context, requestCode, FeedingReminderReceiver::class.java)) {
            return
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactAlarm(alarmManager, calendar.timeInMillis, pendingIntent)
    }

    fun cancelFeedingReminder(
        context: Context,
        petId: String,
        hour: Int,
        minute: Int,
        days: List<String>
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (days.isEmpty()) {
            cancelAlarm(context, alarmManager, petId + hour + minute, FeedingReminderReceiver::class.java)
        } else {
            days.forEach { day ->
                cancelAlarm(context, alarmManager, petId + hour + minute + day, FeedingReminderReceiver::class.java)
            }
        }
    }

    // ========== ฟังก์ชันใหม่สำหรับ Event ==========

    fun scheduleEventReminder(
        context: Context,
        eventId: String,
        eventTitle: String,
        eventTimeInMillis: Long,
        reminderBeforeMinutes: Int,
        notificationId: Int
    ) {

        if (reminderBeforeMinutes <= 0) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // คำนวณเวลาที่จะแจ้งเตือน
        val reminderTime = eventTimeInMillis - (reminderBeforeMinutes * 60 * 1000)

        // ถ้าเวลาผ่านไปแล้ว ไม่ต้องตั้ง
        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra(ReminderConstants.EXTRA_TYPE, ReminderConstants.TYPE_EVENT)
            putExtra(ReminderConstants.EXTRA_EVENT_ID, eventId)
            putExtra(ReminderConstants.EXTRA_EVENT_TITLE, eventTitle)
            putExtra("notification_id", notificationId)
        }

        val requestCode = (eventId + reminderBeforeMinutes).hashCode()

        if (isAlarmScheduled(context, requestCode, EventReminderReceiver::class.java)) {
            return
        }

        android.util.Log.d("ReminderManager", "=== scheduleEventReminder ===")
        android.util.Log.d("ReminderManager", "eventId: $eventId")
        android.util.Log.d("ReminderManager", "eventTitle: $eventTitle")
        android.util.Log.d("ReminderManager", "eventTime: ${Date(eventTimeInMillis)}")
        android.util.Log.d("ReminderManager", "reminderBefore: $reminderBeforeMinutes minutes")
        android.util.Log.d("ReminderManager", "reminderTime: ${Date(reminderTime)}")
        android.util.Log.d("ReminderManager", "notificationId: $notificationId")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactAlarm(alarmManager, reminderTime, pendingIntent)


    }

    fun cancelEventReminder(
        context: Context,
        eventId: String,
        reminderBeforeMinutes: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = (eventId + reminderBeforeMinutes).hashCode()

        cancelAlarm(context, alarmManager, requestCode.toString(), EventReminderReceiver::class.java)
    }

    // ========== ฟังก์ชันร่วม ==========

    private fun scheduleExactAlarm(
        alarmManager: AlarmManager,
        triggerTime: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    private fun <T> isAlarmScheduled(
        context: Context,
        requestCode: Int,
        receiverClass: Class<T>
    ): Boolean {
        val intent = Intent(context, receiverClass)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) != null
    }

    private fun cancelAlarm(
        context: Context,
        alarmManager: AlarmManager,
        key: String,
        receiverClass: Class<*>
    ) {
        val intent = Intent(context, receiverClass)
        val requestCode = key.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    private fun getDayOfWeek(day: String): Int {
        return when (day) {
            "Mon" -> Calendar.MONDAY
            "Tue" -> Calendar.TUESDAY
            "Wed" -> Calendar.WEDNESDAY
            "Thu" -> Calendar.THURSDAY
            "Fri" -> Calendar.FRIDAY
            "Sat" -> Calendar.SATURDAY
            "Sun" -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
    }
}