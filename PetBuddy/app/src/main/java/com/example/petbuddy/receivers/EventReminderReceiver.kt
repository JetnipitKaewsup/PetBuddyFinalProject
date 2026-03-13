package com.example.petbuddy.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.petbuddy.notifications.NotificationHelper
import com.example.petbuddy.notifications.ReminderConstants

class EventReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("EventReminder", "=== onReceive ===")
        android.util.Log.d("EventReminder", "intent: $intent")
        android.util.Log.d("EventReminder", "extras: ${intent.extras}")

//        val eventTitle = intent.getStringExtra(ReminderConstants.EXTRA_EVENT_TITLE) ?: return
//        val notificationId = intent.getIntExtra("notification_id", System.currentTimeMillis().toInt())
//
//
//
//        // แสดง notification
//        NotificationHelper.showEventNotification(context, eventTitle, notificationId)
        val eventTitle = intent.getStringExtra(ReminderConstants.EXTRA_EVENT_TITLE)
        android.util.Log.d("EventReminder", "eventTitle: $eventTitle")

        if (eventTitle == null) {
            android.util.Log.e("EventReminder", "eventTitle is null")
            return
        }

        val notificationId = intent.getIntExtra("notification_id", 0)
        android.util.Log.d("EventReminder", "notificationId: $notificationId")

        NotificationHelper.showEventNotification(context, eventTitle, notificationId)
        android.util.Log.d("EventReminder", "notification shown")

    }


}