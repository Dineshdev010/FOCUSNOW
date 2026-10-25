package com.focusflow.app.util

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.focusflow.app.MainActivity
import com.focusflow.app.model.FocusSchedule
import java.util.Calendar
import kotlin.concurrent.thread
import kotlin.math.sin

object FocusAlarmManager {
    @Volatile
    private var isPlaying = false
    private var audioTrack: AudioTrack? = null
    private var alarmThread: Thread? = null
    private var systemRingtone: Ringtone? = null

    val ALARM_SOUND_ZEN_GONG = "Zen Gong"
    val ALARM_SOUND_GENTLE_CHIME = "Gentle Chime"
    val ALARM_SOUND_CLASSIC = "Classic Alarm"
    val ALARM_SOUND_DIGITAL_BELL = "Digital Bell"
    val ALARM_SOUND_SYSTEM = "System Alarm"

    val AVAILABLE_ALARM_SOUNDS = listOf(
        ALARM_SOUND_ZEN_GONG,
        ALARM_SOUND_GENTLE_CHIME,
        ALARM_SOUND_CLASSIC,
        ALARM_SOUND_DIGITAL_BELL,
        ALARM_SOUND_SYSTEM
    )

    fun isAlarmActive(): Boolean = isPlaying

    fun playAlarm(context: Context, soundName: String = ALARM_SOUND_ZEN_GONG, loop: Boolean = false, vibrate: Boolean = true) {
        stopAlarm(context)
        isPlaying = true

        if (vibrate) {
            triggerVibration(context)
        }

        if (soundName == ALARM_SOUND_SYSTEM) {
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                systemRingtone = RingtoneManager.getRingtone(context, uri).apply {
                    play()
                }
            } catch (_: Exception) {}
            return
        }

        alarmThread = thread(start = true, isDaemon = true, name = "FocusAlarmThread") {
            try {
                val sampleRate = 44100
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = (minBufferSize * 2).coerceAtLeast(8192)

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()

                val buffer = ShortArray(bufferSize)
                var sampleIndex = 0L
                var iterations = 0
                val maxIterations = if (loop) 30 else 3 // Play 3 repeats if preview, or up to 30 if ringing alarm

                while (isPlaying && iterations < maxIterations) {
                    when (soundName) {
                        ALARM_SOUND_ZEN_GONG -> {
                            // Tibetan singing bowl gong (136.1 Hz + gentle 272.2 Hz octave) with exponential decay
                            val durationSecs = 2.4
                            val totalSamples = (sampleRate * durationSecs).toInt()
                            for (s in 0 until totalSamples step buffer.size) {
                                if (!isPlaying) break
                                val chunk = minOf(buffer.size, totalSamples - s)
                                for (i in 0 until chunk) {
                                    val t = (s + i).toDouble() / sampleRate
                                    val decay = Math.exp(-1.4 * t)
                                    val sampleVal = (sin(2.0 * Math.PI * 136.1 * t) * 0.7 +
                                            sin(2.0 * Math.PI * 272.2 * t) * 0.3) * decay * 22000.0
                                    buffer[i] = sampleVal.toInt().coerceIn(-32768, 32767).toShort()
                                }
                                audioTrack?.write(buffer, 0, chunk)
                            }
                        }
                        ALARM_SOUND_GENTLE_CHIME -> {
                            // Upbeat harmonic arpeggio: 528 Hz (Mi - transformation), 660 Hz (E5), 792 Hz (G5)
                            val notes = listOf(528.0, 660.0, 792.0)
                            val noteDuration = 0.35
                            val noteSamples = (sampleRate * noteDuration).toInt()
                            for (freq in notes) {
                                if (!isPlaying) break
                                for (s in 0 until noteSamples step buffer.size) {
                                    if (!isPlaying) break
                                    val chunk = minOf(buffer.size, noteSamples - s)
                                    for (i in 0 until chunk) {
                                        val t = (s + i).toDouble() / sampleRate
                                        val decay = Math.exp(-2.5 * t)
                                        val sampleVal = sin(2.0 * Math.PI * freq * t) * decay * 20000.0
                                        buffer[i] = sampleVal.toInt().coerceIn(-32768, 32767).toShort()
                                    }
                                    audioTrack?.write(buffer, 0, chunk)
                                }
                            }
                            // Brief pause between repeats
                            Thread.sleep(400L)
                        }
                        ALARM_SOUND_CLASSIC -> {
                            // Dual-tone alternating alarm beeps (880 Hz / 1174 Hz)
                            val beepSamples = (sampleRate * 0.12).toInt()
                            for (beep in 0 until 4) {
                                if (!isPlaying) break
                                val freq = if (beep % 2 == 0) 880.0 else 1174.0
                                for (s in 0 until beepSamples step buffer.size) {
                                    if (!isPlaying) break
                                    val chunk = minOf(buffer.size, beepSamples - s)
                                    for (i in 0 until chunk) {
                                        val t = (s + i).toDouble() / sampleRate
                                        val sampleVal = sin(2.0 * Math.PI * freq * t) * 24000.0
                                        buffer[i] = sampleVal.toInt().coerceIn(-32768, 32767).toShort()
                                    }
                                    audioTrack?.write(buffer, 0, chunk)
                                }
                                Thread.sleep(80L)
                            }
                            Thread.sleep(400L)
                        }
                        ALARM_SOUND_DIGITAL_BELL -> {
                            // Digital bell ring: 1046.5 Hz (High C) with metallic harmonics
                            val durationSecs = 1.2
                            val totalSamples = (sampleRate * durationSecs).toInt()
                            for (s in 0 until totalSamples step buffer.size) {
                                if (!isPlaying) break
                                val chunk = minOf(buffer.size, totalSamples - s)
                                for (i in 0 until chunk) {
                                    val t = (s + i).toDouble() / sampleRate
                                    val decay = Math.exp(-3.0 * t)
                                    val sampleVal = (sin(2.0 * Math.PI * 1046.5 * t) * 0.6 +
                                            sin(2.0 * Math.PI * 2093.0 * t) * 0.4) * decay * 22000.0
                                    buffer[i] = sampleVal.toInt().coerceIn(-32768, 32767).toShort()
                                }
                                audioTrack?.write(buffer, 0, chunk)
                            }
                            Thread.sleep(300L)
                        }
                        else -> {
                            Thread.sleep(500L)
                        }
                    }
                    iterations++
                }
            } catch (_: Exception) {
            } finally {
                stopAudioTrack()
                isPlaying = false
            }
        }
    }

    fun stopAlarm(context: Context? = null) {
        isPlaying = false
        stopAudioTrack()
        try {
            systemRingtone?.stop()
            systemRingtone = null
        } catch (_: Exception) {}

        context?.let { ctx ->
            try {
                val vibrator = getVibrator(ctx)
                vibrator?.cancel()
            } catch (_: Exception) {}
        }
    }

    private fun stopAudioTrack() {
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (_: Exception) {}
        audioTrack = null
        alarmThread = null
    }

    private fun triggerVibration(context: Context) {
        try {
            val vibrator = getVibrator(context) ?: return
            val pattern = longArrayOf(0, 350, 150, 350, 150, 600)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun scheduleRoutineAlarm(context: Context, schedule: FocusSchedule) {
        if (!schedule.isEnabled || !schedule.isAlarmEnabled) {
            cancelRoutineAlarm(context, schedule.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, FocusAlarmReceiver::class.java).apply {
            action = "com.focusflow.ROUTINE_ALARM"
            putExtra("schedule_id", schedule.id)
            putExtra("schedule_name", schedule.name)
            putExtra("schedule_emoji", schedule.emoji)
            putExtra("schedule_sound", schedule.alarmSound)
        }

        val requestCode = schedule.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = computeNextTriggerMillis(schedule.startHour, schedule.startMinute, schedule.daysSummary)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (_: Exception) {
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } catch (_: Exception) {}
        }
    }

    fun cancelRoutineAlarm(context: Context, scheduleId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, FocusAlarmReceiver::class.java).apply {
            action = "com.focusflow.ROUTINE_ALARM"
        }
        val requestCode = scheduleId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}
    }

    fun scheduleTimerAlarm(
        context: Context,
        durationSeconds: Int,
        modeTitle: String,
        soundName: String = ALARM_SOUND_GENTLE_CHIME,
        vibrate: Boolean = true
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, FocusAlarmReceiver::class.java).apply {
            action = "com.focusflow.TIMER_ALARM"
            putExtra("mode_title", modeTitle)
            putExtra("sound_name", soundName)
            putExtra("vibrate", vibrate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            8801,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (durationSeconds * 1000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (_: Exception) {
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } catch (_: Exception) {}
        }
    }

    fun cancelTimerAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, FocusAlarmReceiver::class.java).apply {
            action = "com.focusflow.TIMER_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            8801,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}
    }

    fun playTimerCompletion(context: Context) {
        playAlarm(context, ALARM_SOUND_GENTLE_CHIME, loop = false, vibrate = true)
    }

    private fun computeNextTriggerMillis(hour: Int, minute: Int, daysSummary: String): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time already passed today, advance by at least 1 day
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Adjust for matching repeat days
        for (i in 0..7) {
            val dayOfWeek = target.get(Calendar.DAY_OF_WEEK)
            val matches = when (daysSummary) {
                "Mon - Fri" -> dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
                "Mon - Sat" -> dayOfWeek != Calendar.SUNDAY
                "Weekends" -> dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
                "Daily" -> true
                else -> true
            }
            if (matches) {
                break
            }
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis
    }
}

class FocusAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        when (intent?.action) {
            "com.focusflow.DISMISS_ALARM" -> {
                FocusAlarmManager.stopAlarm(context)
                val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notifManager?.cancel(9901)
                notifManager?.cancel(9904)
            }
            "com.focusflow.SNOOZE_ALARM" -> {
                FocusAlarmManager.stopAlarm(context)
                val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notifManager?.cancel(9901)
                notifManager?.cancel(9904)

                val soundName = intent.getStringExtra("sound_name") ?: FocusAlarmManager.ALARM_SOUND_GENTLE_CHIME
                val vibrate = intent.getBooleanExtra("vibrate", true)

                // Schedule 5-minute snooze alarm
                FocusAlarmManager.scheduleTimerAlarm(context, 300, "5m Break", soundName, vibrate)

                // Bring app forward to show break timer
                try {
                    val openIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        action = "com.focusflow.ACTION_ADD_FIVE_MINS"
                    }
                    context.startActivity(openIntent)
                } catch (_: Exception) {}
            }
            "com.focusflow.TIMER_ALARM" -> {
                val modeTitle = intent.getStringExtra("mode_title") ?: "Focus"
                val soundName = intent.getStringExtra("sound_name") ?: FocusAlarmManager.ALARM_SOUND_GENTLE_CHIME
                val vibrate = intent.getBooleanExtra("vibrate", true)

                // Ring alarm continuously in a loop until dismissed!
                FocusAlarmManager.playAlarm(context, soundName, loop = true, vibrate = vibrate)

                // High-priority heads up notification with Stop & Snooze actions
                showTimerAlarmNotification(context, modeTitle, soundName, vibrate)

                // Open ringing alert modal in app
                try {
                    val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("TRIGGER_ALARM_RINGING", true)
                        putExtra("ALARM_TITLE", "⏰ $modeTitle Complete!")
                        putExtra("ALARM_SUBTITLE", "Your focus session is done. Great job!")
                        putExtra("ALARM_SOUND", soundName)
                    }
                    context.startActivity(fullScreenIntent)
                } catch (_: Exception) {}
            }
            "com.focusflow.ROUTINE_ALARM" -> {
                val scheduleName = intent.getStringExtra("schedule_name") ?: "Focus Routine"
                val scheduleEmoji = intent.getStringExtra("schedule_emoji") ?: "⚡"
                val soundName = intent.getStringExtra("schedule_sound") ?: FocusAlarmManager.ALARM_SOUND_ZEN_GONG

                // Ring the alarm audio and haptics
                FocusAlarmManager.playAlarm(context, soundName, loop = true, vibrate = true)

                // Show Heads-Up Alert Notification with Dismiss Action
                showAlarmNotification(context, scheduleName, scheduleEmoji)

                // Open ringing alert modal in app
                try {
                    val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("TRIGGER_ALARM_RINGING", true)
                        putExtra("ALARM_TITLE", "⏰ $scheduleEmoji $scheduleName Active!")
                        putExtra("ALARM_SUBTITLE", "Focus routine is starting. Distracting apps are locked.")
                        putExtra("ALARM_SOUND", soundName)
                    }
                    context.startActivity(fullScreenIntent)
                } catch (_: Exception) {}

                // Re-schedule for next recurrence
                val scheduleId = intent.getStringExtra("schedule_id")
                if (scheduleId != null) {
                    val repo = com.focusflow.app.data.FocusFlowRepository(context)
                    val sched = repo.getFocusSchedules().find { it.id == scheduleId }
                    if (sched != null && sched.isEnabled && sched.isAlarmEnabled) {
                        FocusAlarmManager.scheduleRoutineAlarm(context, sched)
                    }
                }
            }
        }
    }

    private fun showTimerAlarmNotification(context: Context, modeTitle: String, soundName: String, vibrate: Boolean) {
        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        FocusNotificationHelper.createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TRIGGER_ALARM_RINGING", true)
            putExtra("ALARM_TITLE", "⏰ $modeTitle Complete!")
            putExtra("ALARM_SUBTITLE", "Your focus session is done. Great job!")
            putExtra("ALARM_SOUND", soundName)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            9904,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, FocusAlarmReceiver::class.java).apply {
            action = "com.focusflow.DISMISS_ALARM"
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            9905,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, FocusAlarmReceiver::class.java).apply {
            action = "com.focusflow.SNOOZE_ALARM"
            putExtra("sound_name", soundName)
            putExtra("vibrate", vibrate)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            9906,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, FocusNotificationHelper.CHANNEL_FOCUS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $modeTitle Session Complete!")
            .setContentText("Focus time is done. Tap to dismiss or snooze.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Alarm ⏹️", dismissPendingIntent)
            .addAction(android.R.drawable.ic_media_play, "☕ Snooze (+5m)", snoozePendingIntent)
            .build()

        try {
            notifManager.notify(9904, notif)
        } catch (_: Exception) {}
    }

    private fun showAlarmNotification(context: Context, scheduleName: String, emoji: String) {
        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        FocusNotificationHelper.createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            9902,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, FocusAlarmReceiver::class.java).apply {
            action = "com.focusflow.DISMISS_ALARM"
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            9903,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, FocusNotificationHelper.CHANNEL_FOCUS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $emoji $scheduleName Started!")
            .setContentText("Focus routine is active. Distracting apps are now locked.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Alarm ⏹️", dismissPendingIntent)
            .addAction(android.R.drawable.ic_media_play, "Open App 🚀", openAppPendingIntent)
            .build()

        try {
            notifManager.notify(9901, notif)
        } catch (_: Exception) {}
    }
}
