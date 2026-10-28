package com.focusflow.app.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.focusflow.app.MainActivity
import com.focusflow.app.R
import java.util.Calendar

object FocusNotificationHelper {

    const val CHANNEL_FOCUS = "focus_reminders"
    private const val CHANNEL_STREAK = "streak_alerts"
    private const val CHANNEL_DIGEST = "daily_digest"
    private const val CHANNEL_ACHIEVEMENT = "achievements"
    private const val CHANNEL_MOTIVATION = "motivation"
    private const val CHANNEL_APP_LIMITS = "app_limits"
    private const val CHANNEL_ACTIVE_TIMER = "active_focus_timer"

    private const val NOTIF_ID_FOCUS_REMINDER = 1001
    private const val NOTIF_ID_STREAK_ALERT = 1002
    private const val NOTIF_ID_DAILY_DIGEST = 1003
    private const val NOTIF_ID_ACHIEVEMENT = 1004
    private const val NOTIF_ID_MOTIVATION = 1005
    private const val NOTIF_ID_BEDTIME = 1006
    private const val NOTIF_ID_LIMIT_WARNING = 1007
    private const val NOTIF_ID_LIMIT_EXCEEDED = 1008
    private const val NOTIF_ID_ACTIVE_TIMER = 1009

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifManager = context.getSystemService(NotificationManager::class.java) ?: return

            val channels = listOf(
                NotificationChannel(CHANNEL_FOCUS, "Focus Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Scheduled reminders to start focus sessions"
                },
                NotificationChannel(CHANNEL_STREAK, "Streak Alerts", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Alerts about your focus streak status"
                },
                NotificationChannel(CHANNEL_DIGEST, "Daily Focus & Screen Time Report", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "End-of-day summary of focus time, screen time, and blocked distractions"
                    enableVibration(true)
                },
                NotificationChannel(CHANNEL_ACHIEVEMENT, "Achievements", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Badge and achievement unlock notifications"
                },
                NotificationChannel(CHANNEL_MOTIVATION, "Motivational Nudges", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Encouraging tips and motivational messages"
                },
                NotificationChannel(CHANNEL_APP_LIMITS, "App Limits & Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Alerts when apps approach or reach their daily screen time limits"
                },
                NotificationChannel(CHANNEL_ACTIVE_TIMER, "Active Focus Timer", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Live countdown timer and quick controls during focus sessions"
                    setShowBadge(false)
                }
            )
            channels.forEach { notifManager.createNotificationChannel(it) }
        }
    }

    fun showFocusReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TAB", "Focus")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messages = listOf(
            "🎯 Time to focus!" to "Your peak performance window is open. Start a deep focus session now.",
            "⚡ Ready to reclaim your time?" to "A 25-minute focus sprint can transform your day.",
            "🧠 Your brain is warmed up!" to "Channel your energy into deep work before distractions creep in.",
            "🔥 Focus Mode awaits!" to "The best time to start is now. Hit that timer!"
        )
        val (title, text) = messages.random()

        val notif = NotificationCompat.Builder(context, CHANNEL_FOCUS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            val notifManager = context.getSystemService(NotificationManager::class.java)
            notifManager?.notify(NOTIF_ID_FOCUS_REMINDER, notif)
        } catch (_: Exception) {}
    }

    fun showStreakAlert(context: Context, streakDays: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TAB", "Home")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, text) = when {
            streakDays >= 30 -> "🏆 ${streakDays}-Day Streak!" to "You're unstoppable! Complete a focus session to keep your legendary streak alive."
            streakDays >= 7 -> "🔥 ${streakDays}-Day Streak!" to "One week strong! Don't break the chain — start a session today."
            streakDays >= 3 -> "📅 ${streakDays}-Day Streak!" to "Momentum is building! Keep it going with a quick focus sprint."
            else -> "🔗 Streak Alert!" to "You have a ${streakDays}-day streak. Complete a focus session to extend it!"
        }

        val notif = NotificationCompat.Builder(context, CHANNEL_STREAK)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            val notifManager = context.getSystemService(NotificationManager::class.java)
            notifManager?.notify(NOTIF_ID_STREAK_ALERT, notif)
        } catch (_: Exception) {}
    }

    fun showDailyDigest(
        context: Context,
        focusMinutes: Int,
        sessionsCompleted: Int,
        screenTimeSaved: Int,
        trackedScreenTimeMins: Long = 0L,
        streakDays: Int = 0,
        coinsEarned: Int = 0,
        reelsBlocked: Int = 0,
        topApps: List<Pair<String, Long>> = emptyList()
    ) {
        val prefs = context.getSharedPreferences("focusflow_preferences", Context.MODE_PRIVATE)
        val streak = if (streakDays > 0) streakDays else prefs.getInt("key_streak_days", 1)
        val coins = if (coinsEarned > 0) coinsEarned else prefs.getInt("key_total_coins_earned", 0)
        val blockedCount = if (reelsBlocked > 0) reelsBlocked else prefs.getInt("key_reels_blocked_today", 0)

        val realScreenMins = if (trackedScreenTimeMins > 0) trackedScreenTimeMins else {
            try {
                DeviceUsageTracker.getTotalTrackedScreenTimeMinutes(context)
            } catch (_: Exception) { 0L }
        }

        val apps = if (topApps.isNotEmpty()) topApps else {
            try {
                DeviceUsageTracker.getRealDailyAppUsage(context).take(3).map {
                    "${it.iconEmoji} ${it.appName}" to it.minutesToday
                }
            } catch (_: Exception) { emptyList() }
        }

        val progressIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TAB", "Progress")
        }
        val progressPendingIntent = PendingIntent.getActivity(
            context, 2, progressIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val focusIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TAB", "Focus")
            putExtra("START_FOCUS_SESSION", true)
        }
        val focusPendingIntent = PendingIntent.getActivity(
            context, 3, focusIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hours = focusMinutes / 60
        val mins = focusMinutes % 60
        val focusStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

        val sHours = screenTimeSaved / 60
        val sMins = screenTimeSaved % 60
        val savedStr = if (sHours > 0) "${sHours}h ${sMins}m" else "${sMins}m"

        val tHours = realScreenMins / 60
        val tMins = realScreenMins % 60
        val screenStr = if (tHours > 0) "${tHours}h ${tMins}m" else "${tMins}m"

        val title = when {
            focusMinutes >= 120 -> "🏆 Legendary Day! $focusStr Focused · Daily Report"
            focusMinutes >= 60 -> "🌟 Strong Focus Today! $focusStr · Daily Report"
            focusMinutes > 0 -> "📊 Daily Focus & Screen Time Report · $focusStr"
            else -> "📊 Daily Screen Time & Focus Report"
        }

        val summaryLine = "⏱️ $focusStr focused · 📱 $screenStr screen · 🛡️ $savedStr reclaimed"

        val bigText = buildString {
            append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("📊 TODAY'S FOCUS & SCREEN TIME REPORT\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("⏱️ Deep Focus Time:    $focusStr ($sessionsCompleted session${if (sessionsCompleted == 1) "" else "s"})\n")
            if (realScreenMins > 0) {
                append("📱 Phone Screen Time:   $screenStr\n")
            }
            append("🛡️ Screen Time Reclaimed: $savedStr\n")
            if (streak > 0) {
                append("🔥 Active Focus Streak: $streak Day${if (streak == 1) "" else "s"}\n")
            }
            if (coins > 0) {
                append("🪙 Total Focus Coins:   +$coins Coins\n")
            }
            if (blockedCount > 0) {
                append("🚫 Distractions Blocked: $blockedCount Interceptions\n")
            }

            if (apps.isNotEmpty()) {
                append("\n📱 Most Used Apps Today:\n")
                apps.take(3).forEach { (app, usedMins) ->
                    val appDuration = if (usedMins >= 60) "${usedMins / 60}h ${usedMins % 60}m" else "${usedMins}m"
                    append("  • $app: $appDuration\n")
                }
            }

            append("\n💡 ")
            append(when {
                focusMinutes >= 120 -> "Incredible mastery and self-control today! Rest well and conquer tomorrow."
                focusMinutes >= 60 -> "Great momentum built today! Tomorrow is a new chance to reach peak flow."
                focusMinutes > 0 -> "Good progress on deep work! Keep showing up consistently."
                else -> "Tomorrow is a fresh canvas. Kick off your morning with an intentional 25-minute focus session!"
            })
        }

        val notif = NotificationCompat.Builder(context, CHANNEL_DIGEST)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(summaryLine)
            .setSubText("Daily Report")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setAutoCancel(true)
            .setContentIntent(progressPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .addAction(0, "📊 View Full Analytics", progressPendingIntent)
            .addAction(0, "⚡ Start Focus Sprint", focusPendingIntent)
            .build()

        try {
            val notifManager = context.getSystemService(NotificationManager::class.java)
            notifManager?.notify(NOTIF_ID_DAILY_DIGEST, notif)
        } catch (_: Exception) {}
    }

    fun showAppLimitWarning(context: Context, appName: String, remainingMinutes: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TAB", "Home")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 5, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_APP_LIMITS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚠️ $appName Limit Warning")
            .setContentText("Only $remainingMinutes min remaining today before daily focus cutoff.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            val notifManager = context.getSystemService(NotificationManager::class.java)
            notifManager?.notify(NOTIF_ID_LIMIT_WARNING + appName.hashCode() % 100, notif)
        } catch (_: Exception) {}
    }

    fun showAppLimitExceeded(context: Context, appName: String, limitMinutes: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TAB", "Home")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 6, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_APP_LIMITS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🛑 $appName Limit Reached")
            .setContentText("You reached your ${limitMinutes}m daily limit. Mindful shield is active.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            val notifManager = context.getSystemService(NotificationManager::class.java)
            notifManager?.notify(NOTIF_ID_LIMIT_EXCEEDED + appName.hashCode() % 100, notif)
        } catch (_: Exception) {}
    }

    fun showAchievementUnlocked(context: Context, achievementTitle: String, achievementEmoji: String, coinReward: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TAB", "Progress")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 3, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🏆 Achievement Unlocked!")
            .setContentText("$achievementEmoji $achievementTitle — +$coinReward 🪙 Focus Coins")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            val notifManager = context.getSystemService(NotificationManager::class.java)
            notifManager?.notify(NOTIF_ID_ACHIEVEMENT + achievementTitle.hashCode() % 1000, notif)
        } catch (_: Exception) {}
    }

    fun showMotivationalNudge(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 4, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val quotes = listOf(
            "💡 \"The successful warrior is the average man, with laser-like focus.\" — Bruce Lee",
            "🌟 \"Concentrate all your thoughts on the task at hand.\" — Alexander Graham Bell",
            "🧠 \"Where focus goes, energy flows.\" — Tony Robbins",
            "🎯 \"It is during our darkest moments that we must focus to see the light.\" — Aristotle",
            "⚡ \"The key to success is to focus on goals, not obstacles.\"",
            "🔥 \"Starve your distractions, feed your focus.\"",
            "🌱 \"Small daily improvements lead to stunning results over time.\""
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_MOTIVATION)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("FocusFlow Thought of the Day")
            .setContentText(quotes.random())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(quotes.random()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            val notifManager = context.getSystemService(NotificationManager::class.java)
            notifManager?.notify(NOTIF_ID_MOTIVATION, notif)
        } catch (_: Exception) {}
    }

    fun showBedtimeReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 5, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_FOCUS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🌙 Bedtime Wind-Down")
            .setContentText("Put your phone down and protect your sleep. Blue light exposure now will reduce REM quality by 23%.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            val notifManager = context.getSystemService(NotificationManager::class.java)
            notifManager?.notify(NOTIF_ID_BEDTIME, notif)
        } catch (_: Exception) {}
    }

        fun scheduleStreakCheck(context: Context, hour: Int = 20, minute: Int = 0) {
        scheduleDailyFocusReminder(context, hour, minute)
    }

    fun scheduleDailyFocusReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, FocusReminderReceiver::class.java).apply {
            action = "com.focusflow.FOCUS_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 100, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (_: Exception) {}
    }

    fun scheduleDailyDigest(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, FocusReminderReceiver::class.java).apply {
            action = "com.focusflow.DAILY_DIGEST"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 101, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (_: Exception) {}
        } catch (_: Exception) {}
    }

    fun cancelDailyDigest(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, FocusReminderReceiver::class.java).apply {
            action = "com.focusflow.DAILY_DIGEST"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 101, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}
    }

    fun showActiveTimerNotification(
        context: Context,
        remainingSeconds: Int,
        modeTitle: String,
        isRunning: Boolean,
        taskTitle: String? = null
    ) {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeStr = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TAB", "Focus")
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, 200, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.focusflow.ACTION_PAUSE_RESUME_TIMER"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pauseResumePendingIntent = PendingIntent.getActivity(
            context, 201, pauseResumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val addFiveIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.focusflow.ACTION_ADD_FIVE_MINS"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val addFivePendingIntent = PendingIntent.getActivity(
            context, 202, addFiveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (!taskTitle.isNullOrBlank()) "🎯 $taskTitle ($timeStr)" else "⏱️ $modeTitle Session ($timeStr)"
        val text = if (isRunning) "Deep focus in progress • Tap to open" else "Session Paused • Tap to resume"

        val notif = NotificationCompat.Builder(context, CHANNEL_ACTIVE_TIMER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(isRunning)
            .setAutoCancel(false)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .addAction(0, if (isRunning) "⏸️ Pause" else "▶️ Resume", pauseResumePendingIntent)
            .addAction(0, "⏱️ +5 Min", addFivePendingIntent)
            .build()

        try {
            val notifManager = context.getSystemService(NotificationManager::class.java)
            notifManager?.notify(NOTIF_ID_ACTIVE_TIMER, notif)
        } catch (_: Exception) {}
    }

    fun dismissActiveTimerNotification(context: Context) {
        try {
            val notifManager = context.getSystemService(NotificationManager::class.java)
            notifManager?.cancel(NOTIF_ID_ACTIVE_TIMER)
        } catch (_: Exception) {}
    }

    fun cancelAllScheduledNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        listOf(100, 101, 102).forEach { requestCode ->
            val intent = Intent(context, FocusReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try { alarmManager.cancel(pendingIntent) } catch (_: Exception) {}
        }
    }
}

class FocusReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        FocusNotificationHelper.createNotificationChannels(context)

        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.BOOT_COMPLETED",
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.MY_PACKAGE_REPLACED" -> {
                try {
                    val repo = com.focusflow.app.data.FocusFlowRepository(context)
                    val prefs = repo.getNotificationPreferences()
                    // Restore daily focus reminder
                    if (prefs.focusRemindersEnabled) {
                        FocusNotificationHelper.scheduleDailyFocusReminder(context, prefs.focusReminderHour, prefs.focusReminderMinute)
                    }
                    // Restore daily report notification
                    if (prefs.dailyDigestEnabled) {
                        FocusNotificationHelper.scheduleDailyDigest(context, prefs.dailyDigestHour, prefs.dailyDigestMinute)
                    }
                    if (prefs.streakAlertsEnabled) {
                        FocusNotificationHelper.scheduleStreakCheck(context, 20, 0)
                    }

                    // Re-register all enabled routine alarms with AlarmManager
                    val schedules = repo.getFocusSchedules()
                    schedules.filter { it.isEnabled && it.isAlarmEnabled }.forEach { sched ->
                        FocusAlarmManager.scheduleRoutineAlarm(context, sched)
                    }
                } catch (_: Exception) {}
            }
            "com.focusflow.FOCUS_REMINDER" -> {
                FocusNotificationHelper.showFocusReminder(context)
            }
            "com.focusflow.DAILY_DIGEST" -> {
                val prefs = context.getSharedPreferences("focusflow_preferences", Context.MODE_PRIVATE)
                val isEnabled = prefs.getBoolean("key_notif_daily_digest", true)
                val reportHour = prefs.getInt("key_notif_digest_hour", 21)
                val reportMinute = prefs.getInt("key_notif_digest_minute", 0)

                if (isEnabled) {
                    val focusMins = prefs.getInt("key_total_minutes", 0)
                    val sessions = prefs.getInt("key_completed_sessions", 0)
                    val saved = prefs.getInt("key_saved_screentime", 0)
                    val realScreenMins = try {
                        DeviceUsageTracker.getTotalTrackedScreenTimeMinutes(context)
                    } catch (_: Exception) { 0L }
                    val streak = prefs.getInt("key_streak_days", 1)
                    val coins = prefs.getInt("key_total_coins_earned", 0)
                    val reelsBlocked = prefs.getInt("key_reels_blocked_today", 0)
                    val topApps = try {
                        DeviceUsageTracker.getRealDailyAppUsage(context).take(3).map {
                            "${it.iconEmoji} ${it.appName}" to it.minutesToday
                        }
                    } catch (_: Exception) { emptyList() }

                    FocusNotificationHelper.showDailyDigest(
                        context = context,
                        focusMinutes = focusMins,
                        sessionsCompleted = sessions,
                        screenTimeSaved = saved,
                        trackedScreenTimeMins = realScreenMins,
                        streakDays = streak,
                        coinsEarned = coins,
                        reelsBlocked = reelsBlocked,
                        topApps = topApps
                    )
                    
                    // Reschedule for next day at the user's preferred report time
                    FocusNotificationHelper.scheduleDailyDigest(context, reportHour, reportMinute)
                }
            }
            "com.focusflow.STREAK_CHECK" -> {
                val prefs = context.getSharedPreferences("focusflow_preferences", Context.MODE_PRIVATE)
                val streak = prefs.getInt("key_streak_days", 0)
                if (streak > 0) {
                    FocusNotificationHelper.showStreakAlert(context, streak)
                }
            }
        }
    }
}
