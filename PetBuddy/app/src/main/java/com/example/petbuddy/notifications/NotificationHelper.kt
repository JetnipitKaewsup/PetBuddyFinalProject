package com.example.petbuddy.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.petbuddy.R

object NotificationHelper {

    fun showFeedingNotification(
        context: Context,
        petName: String
    ) {
        showNotification(
            context = context,
            channelId = ReminderConstants.CHANNEL_FEEDING,
            channelName = "Feeding Reminder",
            notificationId = generateNotificationId(),
            title = "Feeding Time",
            content = "Time to feed $petName",
            icon = R.drawable.ic_launcher_foreground
        )
    }

    fun showEventNotification(
        context: Context,
        eventTitle: String,
        notificationId: Int = generateNotificationId()
    ) {
        showNotification(
            context = context,
            channelId = ReminderConstants.CHANNEL_EVENT,
            channelName = "Event Reminder",
            notificationId = notificationId,
            title = "Upcoming Event",
            content = "$eventTitle is coming up soon",
            icon = R.drawable.ic_notification
        )
    }

    private fun showNotification(
        context: Context,
        channelId: String,
        channelName: String,
        notificationId: Int,
        title: String,
        content: String,
        icon: Int
    ) {

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminder notification for $channelName"
                enableLights(true)
                enableVibration(true)
            }

            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }

    private fun generateNotificationId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }
}