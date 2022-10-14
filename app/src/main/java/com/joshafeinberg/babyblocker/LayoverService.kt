package com.joshafeinberg.babyblocker

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class LayoverService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var layoutView: View
    private lateinit var windowManager: WindowManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            LayoutParamFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager? ?: error("Window Manager Must Not Be Null")
        layoutView = TimedRemovalLayout(this)
        windowManager.addView(layoutView, params)

        serviceScope.launch {
            BabyBlockerStatus.updateStatus(isActive = true)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CLOSE -> stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        windowManager.removeView(layoutView)
        serviceScope.launch {
            BabyBlockerStatus.updateStatus(isActive = false)
            serviceJob.cancel()
        }
        super.onDestroy()
    }

    private class TimedRemovalLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
    ) : LinearLayout(context, attrs) {

        var timer: Timer? = null
        var clickCount = 0

        init {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

            setOnClickListener {
                timer?.cancel()

                if (++clickCount >= 5) {
                    context.stopService(Intent(context, LayoverService::class.java))
                    return@setOnClickListener
                }

                timer = Timer()
                timer!!.schedule(object : TimerTask() {
                    override fun run() {
                        clickCount = 0
                    }
                }, 5000)
            }
        }
    }

    companion object {
        private const val LayoutParamFlags = (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        const val ACTION_CLOSE = "ACTION_CLOSE"
    }
}