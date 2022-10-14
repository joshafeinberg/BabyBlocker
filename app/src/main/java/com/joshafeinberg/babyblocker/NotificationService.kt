package com.joshafeinberg.babyblocker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NotificationService : Service() {

    private var builder: NotificationCompat.Builder? = null
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var stopBlockingIntent: Intent

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val startBlockingIntent = Intent(this, LayoverService::class.java)
        val startBlockingPendingIntent = PendingIntent.getService(this, 0, startBlockingIntent, PendingIntent.FLAG_IMMUTABLE)
        val startBlockingNotificationAction = NotificationCompat.Action.Builder(null, getString(R.string.notification_action_start), startBlockingPendingIntent).build()

        stopBlockingIntent = Intent(this, LayoverService::class.java).apply {
            action = LayoverService.ACTION_CLOSE
        }
        val stopBlockingPendingIntent = PendingIntent.getService(this, 0, stopBlockingIntent, PendingIntent.FLAG_IMMUTABLE)
        val stopBlockingNotificationAction = NotificationCompat.Action.Builder(null, getString(R.string.notification_action_stop), stopBlockingPendingIntent).build()

        val closeBabyBlockerIntent = Intent(this, NotificationService::class.java).apply {
            action = ACTION_CLOSE
        }
        val closeBabyBlockerPendingIntent = PendingIntent.getService(this, 0, closeBabyBlockerIntent, PendingIntent.FLAG_IMMUTABLE)
        val closeNotificationAction = NotificationCompat.Action.Builder(null, getString(R.string.notification_action_close), closeBabyBlockerPendingIntent).build()


        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.setSound(null, null)
        notificationManager!!.createNotificationChannel(notificationChannel)
        builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content_small))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.notification_content))
            )
            .setSmallIcon(R.drawable.baseline_child_care_24)
            .setOngoing(false)

        serviceScope.launch {
            BabyBlockerStatus.toggleNotification(isActive = true)

            BabyBlockerStatus.babyBlockerStatus.collect { isActive ->
                builder?.clearActions()
                    ?.addAction(if (isActive) stopBlockingNotificationAction else startBlockingNotificationAction)
                    ?.addAction(closeNotificationAction)

                startForeground(NOTIFICATION_ID, builder?.build())
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CLOSE -> stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.cancel(NOTIFICATION_ID)

        startService(stopBlockingIntent)

        serviceScope.launch {
            BabyBlockerStatus.toggleNotification(isActive = false)
            serviceJob.cancel()
        }

        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "overlay_notification_channel"
        private const val NOTIFICATION_ID = 1

        private const val ACTION_CLOSE = "ACTION_CLOSE"
    }
}