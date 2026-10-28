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
    private var appLabelTextView: TextView? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val appName = intent?.getStringExtra(EXTRA_APP_NAME) ?: "App"
        val totalSeconds = intent?.getIntExtra(EXTRA_SECONDS, 300) ?: 300

        val hasOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)
        startForegroundNotification(appName, totalSeconds, hasOverlay)

        if (hasOverlay) {
            showFloatingPill(appName, totalSeconds)
        } else {
            // Overlay permission is required to float over other apps on Android
            try {
                val overlayIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(overlayIntent)
            } catch (_: Exception) {
                try {
                    val overlayIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(overlayIntent)
                } catch (_: Exception) {}
            }
        }

        startCountdown(appName, totalSeconds)
        return START_NOT_STICKY
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else dpToPx(32)
    }

    private fun startForegroundNotification(appName: String, totalSeconds: Int, hasOverlayPermission: Boolean) {
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
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 99, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (!hasOverlayPermission) {
            val overlayIntent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val overlayPendingIntent = PendingIntent.getActivity(
                this, 101, overlayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notifBuilder
                .setContentTitle("🏝️ Enable Floating Screen ($appName)")
                .setContentText("Tap to allow FocusNow to float on screen over your apps")
                .setContentIntent(overlayPendingIntent)
                .addAction(0, "Enable Permission ⚙️", overlayPendingIntent)
        } else {
            notifBuilder
                .setContentTitle("⏱️ $appName Active")
                .setContentText("Floating session running • Tap to return to FocusNow")
                .setContentIntent(pendingIntent)
        }

        val notification = notifBuilder.build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIF_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIF_ID, notification)
            }
        } catch (_: Exception) {}
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingPill(appName: String, totalSeconds: Int) {
        if (floatingView != null) {
            // Already floating on top of screen / used app: update label and restart countdown
            appLabelTextView?.text = " • $appName"
            timerTextView?.text = formatTime(totalSeconds)
            startCountdown(appName, totalSeconds)
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val statusBarHeight = getStatusBarHeight()
        val topY = (statusBarHeight + dpToPx(16)).coerceAtLeast(dpToPx(60))
        val initialEstimatedWidth = dpToPx(200)

        val windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = ((screenWidth - initialEstimatedWidth) / 2).coerceAtLeast(dpToPx(16))
            y = topY
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        this.params = windowParams

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(16), dpToPx(8), dpToPx(14), dpToPx(8))

            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#F50F1014")) // Obsidian Luxury Dark
                cornerRadius = dpToPx(24).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#9BE564")) // Lime Zen Glow Rim
            }
            background = bg
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = dpToPx(10).toFloat()
            }
        }

        val iconText = TextView(this).apply {
            text = "⏳ "
            textSize = 14f
        }
        container.addView(iconText)

        timerTextView = TextView(this).apply {
            text = formatTime(totalSeconds)
            setTextColor(Color.parseColor("#9BE564"))
            textSize = 13f
            typeface = Typeface.MONOSPACE
        }
        container.addView(timerTextView)

        val appLabel = TextView(this).apply {
            text = " • $appName"
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            maxWidth = dpToPx(160)
            setPadding(dpToPx(4), 0, dpToPx(10), 0)
        }
        appLabelTextView = appLabel
        container.addView(appLabel)

        // Subtle vertical separator
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(1), dpToPx(16)).apply {
                setMargins(0, 0, dpToPx(8), 0)
            }
            setBackgroundColor(Color.parseColor("#33FFFFFF"))
        }
        container.addView(divider)

        val closeBtn = TextView(this).apply {
            text = "✕"
            setTextColor(Color.parseColor("#9CA3AF"))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dpToPx(6), dpToPx(4), dpToPx(4), dpToPx(4))
        }
        container.addView(closeBtn)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = windowParams.x
                    initialY = windowParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(deltaX) > 8 || Math.abs(deltaY) > 8) {
                        isDragging = true
                    }
                    val metrics = resources.displayMetrics
                    val viewWidth = container.width.takeIf { it > 0 } ?: dpToPx(180)
                    val viewHeight = container.height.takeIf { it > 0 } ?: dpToPx(44)
                    val maxX = (metrics.widthPixels - viewWidth).coerceAtLeast(0)
                    val maxY = (metrics.heightPixels - viewHeight).coerceAtLeast(0)

                    windowParams.x = (initialX + deltaX).coerceIn(0, maxX)
                    windowParams.y = (initialY + deltaY).coerceIn(getStatusBarHeight(), maxY)
                    try {
                        windowManager?.updateViewLayout(container, windowParams)
                    } catch (_: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = Math.abs(event.rawX - initialTouchX)
                    val diffY = Math.abs(event.rawY - initialTouchY)
                    if (!isDragging && diffX < 14 && diffY < 14) {
                        // Check if tap was on close button
                        val closeLocation = IntArray(2)
                        closeBtn.getLocationOnScreen(closeLocation)
                        val closeLeft = closeLocation[0]
                        val closeTop = closeLocation[1]
                        val closeRight = closeLeft + closeBtn.width
                        val closeBottom = closeTop + closeBtn.height

                        val rawX = event.rawX.toInt()
                        val rawY = event.rawY.toInt()

                        if (rawX in (closeLeft - dpToPx(10))..(closeRight + dpToPx(10)) &&
                            rawY in (closeTop - dpToPx(10))..(closeBottom + dpToPx(10))) {
                            stopSelf()
                        } else {
                            // Clicked pill body -> bring FocusNow to front
                            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            if (launchIntent != null) startActivity(launchIntent)
                        }
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager?.addView(container, windowParams)
            floatingView = container
        } catch (e: Exception) {
            e.printStackTrace()
            floatingView = null
        }
    }

    private fun startCountdown(appName: String, totalSeconds: Int) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer((totalSeconds * 1000L), 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSeconds = (millisUntilFinished / 1000).toInt()
                timerTextView?.text = formatTime(remainingSeconds)

                // If overlay permission was granted while service is running, auto-attach pill
                if (floatingView == null && Settings.canDrawOverlays(this@FloatingFocusIslandService)) {
                    showFloatingPill(appName, remainingSeconds)
                }
            }

            override fun onFinish() {
                onTimerFinished(appName)
            }
        }.start()
    }

    private fun onTimerFinished(appName: String) {
        val finishIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("TRIGGER_REGAIN_TIMES_UP", true)
            putExtra("TIMES_UP_APP_NAME", appName)
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
