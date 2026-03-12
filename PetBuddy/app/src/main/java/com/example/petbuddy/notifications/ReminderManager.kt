package com.example.petbuddy.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

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

            // ---------- ONCE ----------
            "once" -> {

                val calendar = Calendar.getInstance()

                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                scheduleExactAlarm(
                    context,
                    alarmManager,
                    petId,
                    petName,
                    hour,
                    minute,
                    calendar
                )
            }

            // ---------- EVERYDAY ----------
            "everyday" -> {

                val calendar = Calendar.getInstance()

                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                scheduleExactAlarm(
                    context,
                    alarmManager,
                    petId,
                    petName,
                    hour,
                    minute,
                    calendar
                )
            }

            // ---------- CUSTOM DAYS ----------
            "custom" -> {

                days.forEach { day ->

                    val calendar = Calendar.getInstance()

                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val targetDay = getDayOfWeek(day)
                    val today = calendar.get(Calendar.DAY_OF_WEEK)

                    var diff = targetDay - today
                    if (diff < 0) diff += 7

                    calendar.add(Calendar.DAY_OF_MONTH, diff)

                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                        calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    }

                    scheduleExactAlarm(
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

    private fun scheduleExactAlarm(
        context: Context,
        alarmManager: AlarmManager,
        petId: String,
        petName: String,
        hour: Int,
        minute: Int,
        calendar: Calendar,
        day: String = ""
    ) {

        val intent = Intent(context, FeedingReminderReceiver::class.java)
        intent.putExtra("pet_name", petName)

        val requestCode = (petId + hour + minute + day).hashCode()

        val existingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (existingIntent != null) return

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                if (alarmManager.canScheduleExactAlarms()) {

                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )

                } else {

                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )

                }

            } else {

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )

            }

        } catch (_: SecurityException) {

            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
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

    fun cancelFeedingReminder(
        context: Context,
        petId: String,
        hour: Int,
        minute: Int,
        days: List<String>
    ) {

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ถ้าไม่มี custom days
        if (days.isEmpty()) {

            val intent = Intent(context, FeedingReminderReceiver::class.java)

            val requestCode = (petId + hour + minute).hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)

            return
        }

        // ถ้ามีหลายวัน
        days.forEach { day ->

            val intent = Intent(context, FeedingReminderReceiver::class.java)

            val requestCode = (petId + hour + minute + day).hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
        }
    }
}