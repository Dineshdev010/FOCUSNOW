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

    private const val CHANNEL_FOCUS = "focus_reminders"
    private const val CHANNEL_STREAK = "streak_alerts"
    private const val CHANNEL_DIGEST = "daily_digest"
    private const val CHANNEL_ACHIEVEMENT = "achievements"
    private const val CHANNEL_MOTIVATION = "motivation"

    private const val NOTIF_ID_FOCUS_REMINDER = 1001
    private const val NOTIF_ID_STREAK_ALERT = 1002
    private const val NOTIF_ID_DAILY_DIGEST = 1003
    private const val NOTIF_ID_ACHIEVEMENT = 1004
    private const val NOTIF_ID_MOTIVATION = 1005
    private const val NOTIF_ID_BEDTIME = 1006

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
                NotificationChannel(CHANNEL_DIGEST, "Daily Digest", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "End-of-day focus summary and stats"
                },
                NotificationChannel(CHANNEL_ACHIEVEMENT, "Achievements", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Badge and achievement unlock notifications"
                },
                NotificationChannel(CHANNEL_MOTIVATION, "Motivational Nudges", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Encouraging tips and motivational messages"
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

    fun showDailyDigest(context: Context, focusMinutes: Int, sessionsCompleted: Int, screenTimeSaved: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TAB", "Progress")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hours = focusMinutes / 60
        val mins = focusMinutes % 60
        val focusStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
        val savedStr = "${screenTimeSaved / 60}h ${screenTimeSaved % 60}m"

        val notif = NotificationCompat.Builder(context, CHANNEL_DIGEST)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📊 Your Daily Focus Digest")
            .setContentText("$focusStr focused · $sessionsCompleted sessions · $savedStr screen time reclaimed")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Today's Focus Report:\n" +
                "⏱️ Total Focus Time: $focusStr\n" +
                "📈 Sessions Completed: $sessionsCompleted\n" +
                "🛡️ Screen Time Saved: $savedStr\n\n" +
                "Keep pushing towards your daily goal! 💪"
            ))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        try {
            val notifManager = context.getSystemService(NotificationManager::class.java)
            notifManager?.notify(NOTIF_ID_DAILY_DIGEST, notif)
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
            "com.focusflow.FOCUS_REMINDER" -> {
                FocusNotificationHelper.showFocusReminder(context)
            }
            "com.focusflow.DAILY_DIGEST" -> {
                val prefs = context.getSharedPreferences("focusflow_preferences", Context.MODE_PRIVATE)
                val focusMins = prefs.getInt("key_total_minutes", 0)
                val sessions = prefs.getInt("key_completed_sessions", 0)
                val saved = prefs.getInt("key_saved_screentime", 0)
                FocusNotificationHelper.showDailyDigest(context, focusMins, sessions, saved)
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
