package com.example.petbuddy.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.petbuddy.notifications.NotificationHelper
import com.example.petbuddy.notifications.ReminderConstants

class FeedingReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.d("FEEDING_REMINDER", "Receiver triggered")

        val petName =
            intent.getStringExtra(ReminderConstants.EXTRA_PET_NAME) ?: "Your pet"

        NotificationHelper.showFeedingNotification(
            context,
            petName
        )
    }
}