package com.focusflow.app.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import com.focusflow.app.data.FocusFlowRepository
import com.focusflow.app.model.BatchedNotification
import java.util.UUID

/**
 * FocusNotificationListenerService
 *
 * Intercepts and silences distracting push notifications in real-time during active Deep Focus sessions.
 * Batches them into the FocusFlowRepository so the user can review them safely in the Focus Digest
 * after completing their focus block.
 *
 * Emergency calls, alarms, system alerts, and whitelisted productive apps are NEVER silenced.
 */
class FocusNotificationListenerService : NotificationListenerService() {

    private lateinit var repository: FocusFlowRepository

    override fun onCreate() {
        super.onCreate()
        repository = FocusFlowRepository(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pkg = sbn.packageName ?: return

        // 1. Never intercept self
        if (pkg == packageName) return

        // 2. Only active when Deep Focus session is running and Shield is enabled
        if (!repository.isFocusSessionActive() || !repository.isNotificationShieldEnabled()) {
            return
        }

        // 3. Never silence ongoing notifications (active calls, turn-by-turn navigation, audio players)
        if (sbn.isOngoing) return

        val notification = sbn.notification ?: return

        // 4. Never silence alarms, phone calls, or emergency notifications
        if (notification.category == Notification.CATEGORY_CALL ||
            notification.category == Notification.CATEGORY_ALARM ||
            notification.category == Notification.CATEGORY_REMINDER ||
            notification.category == Notification.CATEGORY_EVENT
        ) {
            return
        }

        // 5. Never silence system core packages, launchers, dialers
        if (isSystemEssentialPackage(pkg) || isPhoneOrCallApp(pkg)) {
            return
        }

        // 5b. Never silence educational & learning apps (Duolingo study reminders, etc.)
        if (repository.isEducationalApp(pkg)) {
            return
        }

        // 6. Never silence office apps, workspace notifications, or productive apps
        if (repository.isOfficeOrWorkspaceApp(pkg) || repository.isProductiveAppAllowed(pkg) || repository.isWorkAppAllowed(pkg)) {
            return
        }

        // 7. Never silence if user has an active emergency pass for this app
        if (repository.isEmergencyPassActiveFor(pkg)) {
            return
        }

        // ==========================================
        // DISTRACTION INTERCEPTED: Silence & Batch!
        // ==========================================
        try {
            // Dismiss from notification drawer so it doesn't disturb the user
            cancelNotification(sbn.key)
        } catch (e: Exception) {
            try {
                cancelNotification(sbn.packageName, sbn.tag, sbn.id)
            } catch (ignored: Exception) {}
        }

        // Extract metadata
        val extras = notification.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras?.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
            ?: "New Notification"

        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: "Content received during focus session"

        val (appName, emoji, category) = resolveAppInfo(pkg)

        val batchedItem = BatchedNotification(
            id = UUID.randomUUID().toString(),
            packageName = pkg,
            appName = appName,
            title = title.take(90),
            text = text.take(160),
            timestamp = System.currentTimeMillis(),
            emoji = emoji,
            category = category
        )

        // Store into repository for Focus Digest
        repository.addBatchedNotification(batchedItem)
        repository.incrementReelsBlocked() // Increases protected interventions count
    }

    private fun resolveAppInfo(pkg: String): Triple<String, String, String> {
        return when (pkg) {
            "com.microsoft.teams", "com.microsoft.teams.v2" -> Triple("Microsoft Teams", "👥", "Work")
            "com.microsoft.office.outlook", "com.microsoft.outlook" -> Triple("Microsoft Outlook", "📧", "Work")
            "com.whatsapp", "com.whatsapp.w4b" -> Triple("WhatsApp", "💬", "Work")
            "com.instagram.android" -> Triple("Instagram", "📸", "Social")
            "com.google.android.youtube" -> Triple("YouTube", "▶️", "Media")
            "com.zhiliaoapp.musically", "com.ss.android.ugc.trill" -> Triple("TikTok", "🎵", "Social")
            "com.snapchat.android" -> Triple("Snapchat", "👻", "Social")
            "com.twitter.android" -> Triple("X (Twitter)", "🐦", "Social")
            "com.facebook.katana" -> Triple("Facebook", "👥", "Social")
            "com.reddit.frontpage" -> Triple("Reddit", "🤖", "Social")
            "org.telegram.messenger" -> Triple("Telegram", "✈️", "Chat")
            "com.discord" -> Triple("Discord", "🎮", "Community")
            "com.pinterest" -> Triple("Pinterest", "📌", "Inspiration")
            "com.netflix.mediaclient" -> Triple("Netflix", "🍿", "Entertainment")
            "com.spotify.music" -> Triple("Spotify", "🎧", "Music")
            "com.amazon.mShop.android.shopping" -> Triple("Amazon", "🛒", "Shopping")
            else -> {
                val appLabel = try {
                    val appInfo = packageManager.getApplicationInfo(pkg, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    pkg.substringAfterLast('.').capitalize()
                }
                Triple(appLabel, "🔔", "Notification")
            }
        }
    }

    private fun isSystemEssentialPackage(pkg: String): Boolean {
        if (pkg == "android" || pkg == "com.android.systemui") return true
        if (pkg.contains("inputmethod") || pkg.contains("keyboard") || pkg == "com.google.android.inputmethod.latin") return true
        if (pkg == "com.android.settings" || pkg == "com.google.android.permissioncontroller" || pkg == "com.android.packageinstaller") return true

        // Check if launcher
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val defaultLauncher = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
            if (pkg == defaultLauncher) return true
        } catch (ignored: Exception) {}

        if (pkg == "com.google.android.apps.nexuslauncher" || pkg == "com.android.launcher3" || pkg == "com.sec.android.app.launcher") {
            return true
        }
        return false
    }

    private fun isPhoneOrCallApp(pkg: String): Boolean {
        try {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            if (telephonyManager != null && telephonyManager.callState != TelephonyManager.CALL_STATE_IDLE) {
                return true
            }
        } catch (ignored: Exception) {}

        try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            val defaultDialer = telecomManager?.defaultDialerPackage
            if (defaultDialer != null && pkg == defaultDialer) {
                return true
            }
        } catch (ignored: Exception) {}

        val dialerPackages = setOf(
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.android.phone",
            "com.android.incallui",
            "com.google.android.apps.messaging",
            "com.android.mms"
        )
        return dialerPackages.contains(pkg)
    }
}
