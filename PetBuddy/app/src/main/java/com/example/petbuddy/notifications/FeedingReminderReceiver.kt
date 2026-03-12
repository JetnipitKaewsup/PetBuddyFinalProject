package com.example.petbuddy.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FeedingReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val petName = intent.getStringExtra("pet_name") ?: return

        NotificationHelper.showFeedingNotification(
            context,
            petName
        )

    }
}