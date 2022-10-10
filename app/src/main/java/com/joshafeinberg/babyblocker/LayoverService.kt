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
import java.util.*


class LayoverService : Service() {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(layoutView)
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
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    }
}