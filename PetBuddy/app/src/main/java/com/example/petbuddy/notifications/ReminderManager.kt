package com.example.petbuddy.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderManager {

    fun scheduleFeedingReminder(
        context: Context,
        petName: String,
        hour: Int,
        minute: Int
    ) {

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        val intent = Intent(context, FeedingReminderReceiver::class.java)
        intent.putExtra("pet_name", petName)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            petName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}