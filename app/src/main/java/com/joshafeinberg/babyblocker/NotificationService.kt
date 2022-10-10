package com.joshafeinberg.babyblocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class NotificationService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val intent = Intent(this, LayoverService::class.java)
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.setSound(null, null)
        notificationManager!!.createNotificationChannel(notificationChannel)
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content_small))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.notification_content))
            )
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setOngoing(false)

        startForeground(NOTIFICATION_ID, builder.build())
    }

    override fun onDestroy() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.cancel(NOTIFICATION_ID)

        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "overlay_notification_channel"
        private const val NOTIFICATION_ID = 1
    }
}