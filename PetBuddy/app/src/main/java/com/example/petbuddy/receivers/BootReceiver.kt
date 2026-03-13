package com.example.petbuddy.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.petbuddy.notifications.ReminderManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            Log.d("BootReceiver", "Device rebooted - restoring alarms")

            restoreFeedingReminders(context)
            restoreEventReminders(context)

        }
    }

    private fun restoreFeedingReminders(context: Context) {

        // TODO: โหลด feeding schedule จาก database
        // ตัวอย่าง structure

        Log.d("BootReceiver", "Restoring feeding reminders")

        /*

        val schedules = FeedingRepository.getAllSchedules()

        schedules.forEach { schedule ->

            ReminderManager.scheduleFeedingReminder(
                context = context,
                petId = schedule.petId,
                petName = schedule.petName,
                hour = schedule.hour,
                minute = schedule.minute,
                repeatType = schedule.repeatType,
                days = schedule.days
            )
        }
        */

    }

    private fun restoreEventReminders(context: Context) {

        Log.d("BootReceiver", "Restoring event reminders")

        /*

        val events = EventRepository.getUpcomingEvents()

        events.forEach { event ->

            ReminderManager.scheduleEventReminder(
                context = context,
                eventId = event.id,
                eventTitle = event.title,
                eventTimeInMillis = event.time,
                reminderBeforeMinutes = event.reminderBefore,
                notificationId = event.notificationId
            )
        }
        */

    }
}