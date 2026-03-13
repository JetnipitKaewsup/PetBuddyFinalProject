package com.example.petbuddy.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
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

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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

                scheduleAlarm(
                    context,
                    alarmManager,
                    petId,
                    petName,
                    hour,
                    minute,
                    calendar
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

                    scheduleAlarm(
                        context,
                        alarmManager,
                        petId,
                        petName,
                        hour,
                        minute,
                        calendar,
                        day
                    )
                }
            }
        }
    }

    private fun scheduleAlarm(
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

            putExtra("pet_name", petName)
            putExtra("pet_id", petId)

        }

        val requestCode = (petId + hour + minute + day).hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d(
            "ReminderManager",
            "Feeding alarm scheduled for $petName at ${Date(calendar.timeInMillis)}"
        )

        setExactAlarm(
            alarmManager,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelFeedingReminder(
        context: Context,
        petId: String,
        hour: Int,
        minute: Int,
        days: List<String>
    ) {

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (days.isEmpty()) {

            cancelAlarm(
                context,
                alarmManager,
                petId + hour + minute
            )

        } else {

            days.forEach { day ->

                cancelAlarm(
                    context,
                    alarmManager,
                    petId + hour + minute + day
                )
            }
        }
    }


    fun scheduleEventReminder(
        context: Context,
        eventId: String,
        eventTitle: String,
        eventTimeInMillis: Long,
        reminderBeforeMinutes: Int,
        notificationId: Int
    ) {

        if (reminderBeforeMinutes <= 0) return

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val reminderTime =
            eventTimeInMillis - (reminderBeforeMinutes * 60 * 1000)

        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(context, EventReminderReceiver::class.java).apply {

            putExtra("event_title", eventTitle)
            putExtra("notification_id", notificationId)

        }

        val requestCode = (eventId + reminderBeforeMinutes).hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d(
            "ReminderManager",
            "Event reminder scheduled at ${Date(reminderTime)}"
        )

        setExactAlarm(
            alarmManager,
            reminderTime,
            pendingIntent
        )
    }

    fun cancelEventReminder(
        context: Context,
        eventId: String,
        reminderBeforeMinutes: Int
    ) {

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, EventReminderReceiver::class.java)

        val requestCode = (eventId + reminderBeforeMinutes).hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        Log.d("ReminderManager", "Event reminder cancelled")
    }

    /*
    -------------------------
    COMMON FUNCTIONS
    -------------------------
     */

    private fun cancelAlarm(
        context: Context,
        alarmManager: AlarmManager,
        key: String
    ) {

        val intent = Intent(context, FeedingReminderReceiver::class.java)

        val requestCode = key.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        Log.d("ReminderManager", "Alarm cancelled: $key")
    }

    private fun setExactAlarm(
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

        } catch (e: Exception) {

            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
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