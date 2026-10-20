package com.focusflow.app.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.focusflow.app.MainActivity
import com.focusflow.app.R

class FloatingFocusIslandService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var countDownTimer: CountDownTimer? = null
    private var timerTextView: TextView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val appName = intent?.getStringExtra(EXTRA_APP_NAME) ?: "App"
        val totalSeconds = intent?.getIntExtra(EXTRA_SECONDS, 300) ?: 300

        startForegroundNotification(appName, totalSeconds)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            showFloatingPill(appName, totalSeconds)
        }

        startCountdown(appName, totalSeconds)
        return START_NOT_STICKY
    }

    private fun startForegroundNotification(appName: String, totalSeconds: Int) {
        val channelId = "floating_focus_island"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Focus Island Pass",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live countdown for intentional micro-sessions & puzzle passes"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 99, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏱️ $appName Active")
            .setContentText("Micro-session running • Tap to return to FocusFlow")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIF_ID, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingPill(appName: String, totalSeconds: Int) {
        if (floatingView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 120
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(36, 18, 36, 18)

            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#EE161D26"))
                cornerRadius = 60f
                setStroke(3, Color.parseColor("#443DD598"))
            }
            background = bg
        }

        val iconText = TextView(this).apply {
            text = "⏳ "
            textSize = 14f
        }
        container.addView(iconText)

        timerTextView = TextView(this).apply {
            text = formatTime(totalSeconds)
            setTextColor(Color.parseColor("#3DD598"))
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
        }
        container.addView(timerTextView)

        val appLabel = TextView(this).apply {
            text = " • $appName"
            setTextColor(Color.WHITE)
            textSize = 12f
            setPadding(10, 0, 16, 0)
        }
        container.addView(appLabel)

        val closeBtn = TextView(this).apply {
            text = "✕"
            setTextColor(Color.parseColor("#88A0B2"))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(16, 0, 8, 0)
            setOnClickListener {
                stopSelf()
            }
        }
        container.addView(closeBtn)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    try {
                        windowManager?.updateViewLayout(container, params)
                    } catch (_: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = Math.abs(event.rawX - initialTouchX)
                    val diffY = Math.abs(event.rawY - initialTouchY)
                    if (diffX < 10 && diffY < 10) {
                        // Clicked pill -> bring FocusFlow to front
                        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        if (launchIntent != null) startActivity(launchIntent)
                    }
                    true
                }
                else -> false
            }
        }

        floatingView = container
        try {
            windowManager?.addView(floatingView, params)
        } catch (_: Exception) {}
    }

    private fun startCountdown(appName: String, totalSeconds: Int) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer((totalSeconds * 1000L), 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSeconds = (millisUntilFinished / 1000).toInt()
                timerTextView?.text = formatTime(remainingSeconds)
            }

            override fun onFinish() {
                onTimerFinished(appName)
            }
        }.start()
    }

    private fun onTimerFinished(appName: String) {
        val finishIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TRIGGER_REGAIN_TIMES_UP", true)
            putExtra("LOCKED_APP_NAME", appName)
        }
        startActivity(finishIntent)
        stopSelf()
    }

    private fun formatTime(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return String.format(java.util.Locale.US, "%d:%02d", m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        if (floatingView != null) {
            try {
                windowManager?.removeView(floatingView)
            } catch (_: Exception) {}
            floatingView = null
        }
    }

    companion object {
        private const val NOTIF_ID = 4040
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_SECONDS = "extra_seconds"

        fun start(context: Context, appName: String, seconds: Int) {
            try {
                val intent = Intent(context, FloatingFocusIslandService::class.java).apply {
                    putExtra(EXTRA_APP_NAME, appName)
                    putExtra(EXTRA_SECONDS, seconds)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, FloatingFocusIslandService::class.java)
                context.stopService(intent)
            } catch (_: Exception) {}
        }
    }
}
