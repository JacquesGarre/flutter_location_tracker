package com.example.location_tracker

import android.app.*
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object Notification {

    fun show(context: Context, title: String, content: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channelId = "location_channel"
        val channelName = "Location Tracking"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(content) // TODO: Parameterize
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
        if (context is Service) {
            context.startForeground(1, notification)
        }
        println("[LocationTrackerPlugin] NOTIFICATION DISPLAYED")
    }
}