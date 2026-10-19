package com.focusflow.app.util

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import java.util.Calendar

data class AppUsageStat(
    val packageName: String,
    val appName: String,
    val minutesToday: Long,
    val secondsToday: Long,
    val formattedDuration: String,
    val iconEmoji: String,
    val category: String
)

object DeviceUsageTracker {

    // Common apps mapping for recognizable emojis and categories
    private val appMetadata = mapOf(
        "com.instagram.android" to ("📸" to "Social"),
        "com.google.android.youtube" to ("▶️" to "Video"),
        "com.snapchat.android" to ("👻" to "Social"),
        "com.zhiliaoapp.musically" to ("🎵" to "Social"),
        "com.reddit.frontpage" to ("🤖" to "Entertainment"),
        "com.facebook.katana" to ("📘" to "Social"),
        "com.twitter.android" to ("🐦" to "Social"),
        "com.android.chrome" to ("🌐" to "Browsing"),
        "com.google.android.dialer" to ("📞" to "Communication"),
        "com.google.android.apps.messaging" to ("💬" to "Communication"),
        "com.google.android.gm" to ("✉️" to "Productivity"),
        "com.android.settings" to ("⚙️" to "System Tools"),
        "com.focusflow.app" to ("🛡️" to "Focus & Wellbeing"),
        "com.google.android.apps.photos" to ("🖼️" to "Media"),
        "com.android.vending" to ("🛍️" to "App Store"),
        "com.android.camera2" to ("📷" to "Media")
    )

    // Ignore internal OS daemons that are not user-facing apps
    private val ignoredSystemPackages = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.gms",
        "com.google.android.googlequicksearchbox",
        "com.android.launcher3",
        "com.google.android.apps.nexuslauncher",
        "com.google.android.inputmethod.latin",
        "com.android.keychain",
        "com.android.providers.media.module"
    )

    fun getRealDailyAppUsage(context: Context): List<AppUsageStat> {
        if (!PermissionHelper.hasUsageStatsPermission(context)) {
            return emptyList()
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        // Use queryAndAggregateUsageStats to combine multiple sessions into today's accurate totals
        val statsMap = try {
            usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        } catch (e: Exception) {
            emptyMap()
        }

        if (statsMap.isNullOrEmpty()) {
            return emptyList()
        }

        val packageManager = context.packageManager
        val appUsageList = mutableListOf<AppUsageStat>()

        for ((pkgName, usage) in statsMap) {
            val totalMillis = usage.totalTimeInForeground
            // Only list apps that were actually in the foreground today
            if (totalMillis > 1000 && !ignoredSystemPackages.contains(pkgName)) {
                val appLabel = try {
                    val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    pkgName.substringAfterLast('.')
                }

                val totalSeconds = totalMillis / 1000
                val totalMinutes = totalSeconds / 60
                val remSeconds = totalSeconds % 60

                val formatted = if (totalMinutes > 0) {
                    "${totalMinutes}m ${remSeconds}s"
                } else {
                    "${totalSeconds}s"
                }

                val meta = appMetadata[pkgName]
                val emoji = meta?.first ?: deriveEmoji(pkgName, appLabel)
                val category = meta?.second ?: "Application"

                appUsageList.add(
                    AppUsageStat(
                        packageName = pkgName,
                        appName = appLabel,
                        minutesToday = totalMinutes,
                        secondsToday = totalSeconds,
                        formattedDuration = formatted,
                        iconEmoji = emoji,
                        category = category
                    )
                )
            }
        }

        // Return real apps sorted strictly by foreground duration descending
        return appUsageList.sortedByDescending { it.secondsToday }
    }

    fun getTotalTrackedScreenTimeMinutes(context: Context): Long {
        val usageList = getRealDailyAppUsage(context)
        return usageList.sumOf { it.minutesToday }
    }

    fun getTotalTrackedScreenTimeSeconds(context: Context): Long {
        val usageList = getRealDailyAppUsage(context)
        return usageList.sumOf { it.secondsToday }
    }

    private fun deriveEmoji(packageName: String, label: String): String {
        val lower = (packageName + label).lowercase()
        return when {
            lower.contains("social") || lower.contains("chat") || lower.contains("talk") -> "💬"
            lower.contains("video") || lower.contains("tube") || lower.contains("tv") -> "▶️"
            lower.contains("music") || lower.contains("audio") -> "🎵"
            lower.contains("game") || lower.contains("play") -> "🎮"
            lower.contains("mail") || lower.contains("inbox") -> "✉️"
            lower.contains("browser") || lower.contains("web") -> "🌐"
            lower.contains("note") || lower.contains("task") || lower.contains("todo") -> "📝"
            lower.contains("map") || lower.contains("nav") -> "🗺️"
            else -> "📱"
        }
    }
}
