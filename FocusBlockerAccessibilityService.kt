package com.focusflow.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.WindowManager
import android.view.Gravity
import android.view.View
import android.view.MotionEvent
import android.widget.TextView
import android.graphics.drawable.GradientDrawable
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import com.focusflow.app.model.*
import com.focusflow.app.MainActivity
import com.focusflow.app.data.FocusFlowRepository
import com.focusflow.app.widget.FocusFlowWidgetProvider
import java.util.ArrayDeque

class FocusBlockerAccessibilityService : AccessibilityService() {
    private lateinit var repository: FocusFlowRepository
    private var lastInterceptionTime = 0L

    // Floating Live Session Countdown Pill (Regain Trademark)
    private var windowManager: WindowManager? = null
    private var floatingPillView: TextView? = null
    private var floatingPillParams: WindowManager.LayoutParams? = null
    private val pillMainHandler = Handler(Looper.getMainLooper())
    private var isPillAttached = false
    private val updateFloatingPillRunnable = object : Runnable {
        override fun run() {
            updateFloatingPillText()
            pillMainHandler.postDelayed(this, 1000L)
        }
    }

    // Reel Swipe Counter (Regain Nudge per X Reels)
    private var reelSwipeCounter = 0
    private var lastReelSwipeTime = 0L

    companion object {
        // Supported Android browsers for URL interception
        private val BROWSER_PACKAGES = setOf(
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.canary",
            "com.chrome.dev",
            "org.chromium.chrome",
            "com.sec.android.app.sbrowser",
            "com.sec.android.app.sbrowser.beta",
            "org.mozilla.firefox",
            "org.mozilla.firefox_beta",
            "org.mozilla.fenix",
            "com.brave.browser",
            "com.opera.browser",
            "com.opera.mini.native",
            "com.opera.touch",
            "com.microsoft.emmx",
            "com.duckduckgo.mobile.android",
            "com.mi.globalbrowser",
            "com.vivo.browser",
            "com.heytap.browser",
            "com.coloros.browser",
            "com.huawei.browser",
            "com.amazon.cloud9"
        )

        // Address bar IDs across browsers
        private val URL_BAR_IDS = listOf(
            "url_bar",
            "location_bar",
            "search_box",
            "url_field",
            "address_bar",
            "omnibox",
            "toolbar_wrapper",
            "title_url_container",
            "url_bar_tint",
            "mozac_browser_toolbar_url_view",
            "display_url",
            "browser_address_bar",
            "custom_tab_toolbar"
        )

        // Instagram Reels view IDs & indicators
        private val INSTAGRAM_REELS_IDS = listOf(
            "clips_viewer",
            "clips_video_container",
            "clips_swipe_refresh_container",
            "clips_item",
            "clips_media_interact_layer",
            "clips_author_name",
            "clips_audio_mix_view",
            "reel_viewer",
            "reel_viewer_tall",
            "reels_tab",
            "reel_feed",
            "root_clips_layout"
        )

        private val INSTAGRAM_REELS_TEXTS = listOf(
            "original audio",
            "original sound",
            "audio by",
            "audio •",
            "remix with",
            "use template",
            "use audio",
            "watch more reels",
            "send reel",
            "share reel",
            "save reel"
        )

        // YouTube Shorts view IDs & indicators (exclusive to full-screen Shorts player)
        private val YOUTUBE_SHORTS_IDS = listOf(
            "reel_recycler",
            "reel_watch_fragment",
            "reel_player",
            "reel_right_action_bar",
            "reel_video_player",
            "shorts_surface_view",
            "reel_player_overlay",
            "reel_comment_button",
            "reel_share_button",
            "reel_pivot_button"
        )

        private val YOUTUBE_SHORTS_TEXTS = listOf(
            "like this short",
            "dislike this short",
            "remix this video",
            "create a short",
            "use this sound"
        )
    }

    override fun onCreate() {
        super.onCreate()
        repository = FocusFlowRepository(applicationContext)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_SCROLLED or
                AccessibilityEvent.TYPE_VIEW_CLICKED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC or AccessibilityServiceInfo.FEEDBACK_HAPTIC
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        info.notificationTimeout = 50L
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return

        // Ignore self
        if (pkg == packageName) return

        // 0a. Never intercept system Phone/Dialer calls
        if (isPhoneOrCallApp(pkg)) return

        // 0b. Never intercept if user has an active Emergency Pass for this app
        if (repository.isEmergencyPassActiveFor(pkg)) return

        val now = System.currentTimeMillis()
        if (now - lastInterceptionTime < 1000L) return

        // 0c. Regain Active Micro-Session Pass check
        if (repository.isMicroSessionActiveFor(pkg)) {
            // User is actively inside their granted 1/5/10/15m pass!
            val session = repository.getActiveMicroSession()
            if (session != null) {
                showFloatingCountdownPill(session)
            }
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                checkReelSwipe(pkg, now)
            }
            checkScrollNudge(pkg, now)
            return
        } else {
            hideFloatingCountdownPill()
        }

        // Check reel swipes during regular usage
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            checkReelSwipe(pkg, now)
        }

        // 0d. Regain Micro-Session Expired: Time's Up! Kick out!
        if (repository.hasMicroSessionJustExpired(pkg)) {
            hideFloatingCountdownPill()
            repository.clearMicroSession()
            lastInterceptionTime = now
            triggerHapticInterception()
            performGlobalAction(GLOBAL_ACTION_HOME)
            launchRegainTimesUp(pkg)
            return
        }

        // 0e. Regain Anti-Tamper: Block entering Android Settings Accessibility during strict lock or curfew
        if (pkg == "com.android.settings") {
            val antiTamper = repository.getAntiTamperConfig()
            if (antiTamper.preventSettingsAccess && (repository.isFocusSessionActive() || repository.isBedtimeCurfewActive())) {
                val className = event.className?.toString()?.lowercase() ?: ""
                val text = event.text.joinToString(" ").lowercase()
                if (className.contains("accessibility") || className.contains("subsettings") || text.contains("accessibility") || text.contains("focusflow")) {
                    lastInterceptionTime = now
                    triggerHapticInterception()
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    launchAntiTamperWarning()
                    return
                }
            }
            return
        }

        if (isSystemEssentialPackage(pkg)) return

        // 0a. STRICT WORK MODE INTERCEPTION
        // When Work Mode is ON, ONLY office products (Teams, Outlook, Office 365), WhatsApp, and phone calls can run.
        // All non-work apps (games, social media, entertainment) are immediately locked.
        if (repository.isWorkModeActive()) {
            if (!isAppAllowedInWorkMode(pkg)) {
                lastInterceptionTime = now
                triggerHapticInterception()
                repository.incrementReelsBlocked()
                FocusFlowWidgetProvider.updateAllWidgets(applicationContext)
                launchFocusLockdown(pkg, isWorkMode = true)
                return
            }
        }

        // 0. DEEP FOCUS LOCKDOWN MODE
        // When Focus session is active in the app, ONLY calls and productive apps are allowed!
        // All other apps and activities are locked immediately.
        if (repository.isFocusSessionActive()) {
            if (!isAppAllowedDuringFocus(pkg)) {
                lastInterceptionTime = now
                triggerHapticInterception()
                repository.incrementReelsBlocked()
                FocusFlowWidgetProvider.updateAllWidgets(applicationContext)
                launchFocusLockdown(pkg, isWorkMode = false)
                return
            }
        }

        val shortFormConfig = repository.getShortFormConfig()

        // Gather all relevant window roots safely
        val rootsToCheck = mutableListOf<AccessibilityNodeInfo>()
        rootInActiveWindow?.let { rootsToCheck.add(it) }
        try {
            for (w in windows) {
                w.root?.let { if (!rootsToCheck.contains(it)) rootsToCheck.add(it) }
            }
        } catch (ignored: Exception) {}

        // 1. Browser Website & Incognito Interception (Chrome, Brave, Firefox, Edge, etc.)
        if (BROWSER_PACKAGES.contains(pkg) && shortFormConfig.blockWebDoomscrolling) {
            val webSettings = repository.getWebShieldSettings()

            // 1a. Incognito / Private Tab Blocker
            if (webSettings.blockIncognitoMode) {
                for (r in rootsToCheck) {
                    if (isIncognitoTabActive(r)) {
                        lastInterceptionTime = now + 1500L
                        triggerHapticInterception()
                        performGlobalAction(GLOBAL_ACTION_BACK)
                        repository.incrementReelsBlocked()
                        FocusFlowWidgetProvider.updateAllWidgets(applicationContext)
                        launchMindfulPause("Incognito Private Browsing 🕵️ (Shield Active)", pkg)
                        return
                    }
                }
            }

            // 1b. Multi-Category Web & Domain Blocker
            for (r in rootsToCheck) {
                val (isBlocked, reason) = detectBlockedWebsiteInBrowserAdvanced(r)
                if (isBlocked && reason != null) {
                    lastInterceptionTime = now + 1500L
                    triggerHapticInterception()
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    repository.incrementReelsBlocked()
                    FocusFlowWidgetProvider.updateAllWidgets(applicationContext)
                    launchMindfulPause(reason, pkg)
                    return
                }
            }
        }

        // 2. Instagram Reels Interception (In-App)
        if ((pkg == "com.instagram.android" || pkg == "com.instagram.lite") && shortFormConfig.blockInstagramReels) {
            val className = event.className?.toString() ?: ""
            val isClipsActivity = className.contains("Clips", ignoreCase = true) ||
                    className.contains("ModalActivity", ignoreCase = true) ||
                    className.contains("TransparentModal", ignoreCase = true)

            if (isClipsActivity || rootsToCheck.any { isInstagramReelsActive(it) }) {
                lastInterceptionTime = now
                triggerHapticInterception()
                performGlobalAction(GLOBAL_ACTION_BACK)
                repository.incrementReelsBlocked()
                FocusFlowWidgetProvider.updateAllWidgets(applicationContext)
                launchMindfulPause("Instagram Reels", pkg)
                return
            }
        }

        // 3. YouTube Shorts Interception (In-App)
        if (pkg == "com.google.android.youtube" && shortFormConfig.blockYouTubeShorts) {
            if (rootsToCheck.any { isYouTubeShortsActive(it) }) {
                lastInterceptionTime = now
                triggerHapticInterception()
                performGlobalAction(GLOBAL_ACTION_BACK)
                repository.incrementReelsBlocked()
                FocusFlowWidgetProvider.updateAllWidgets(applicationContext)
                launchMindfulPause("YouTube Shorts", pkg)
                return
            }
        }

        // 4. Snapchat Spotlight Interception
        if (pkg == "com.snapchat.android" && shortFormConfig.blockSnapchatSpotlight) {
            if (rootsToCheck.any { containsKeyword(it, listOf("spotlight", "spotlight_feed")) }) {
                lastInterceptionTime = now
                triggerHapticInterception()
                performGlobalAction(GLOBAL_ACTION_BACK)
                repository.incrementReelsBlocked()
                FocusFlowWidgetProvider.updateAllWidgets(applicationContext)
                return
            }
        }

        // 5. Whole App Blocking, Regain Morning Shield, Bedtime Curfew & Daily Limits
        val isExplicitlyBlocked = repository.isPackageInBlockedSet(pkg)
        val isCurfewActive = repository.isBedtimeCurfewActive()
        val isMorningActive = repository.isMorningProtectionCurrentlyActive()
        val blockedApps = repository.getBlockedApps()
        val matchedApp = blockedApps.find { it.packageName == pkg }
        val isOverDailyLimit = matchedApp != null && matchedApp.usedMinutesToday >= matchedApp.dailyLimitMinutes
        val activeSchedule = repository.getCurrentlyActiveSchedule()
        val isStrictMode = repository.isStrictModeEnabled()
        val isStrictFocusActive = isStrictMode && repository.isFocusSessionActive()
        val isProductive = repository.isProductiveAppAllowed(pkg)
        val isWorkAllowed = repository.isWorkModeActive() && repository.isWorkAppAllowed(pkg)

        // For YouTube: if NOT explicitly whole-app blocked by user, keep YouTube accessible for long-form study videos (Shorts is handled above)
        val isYouTubeExemptFromWholeBlock = (pkg == "com.google.android.youtube") && !isExplicitlyBlocked

        if (!isYouTubeExemptFromWholeBlock && !isProductive && !isWorkAllowed) {
            val shouldBlock = isExplicitlyBlocked ||
                    (matchedApp != null && matchedApp.isBlocked) ||
                    isCurfewActive ||
                    isMorningActive ||
                    isStrictFocusActive ||
                    activeSchedule != null ||
                    isOverDailyLimit

            if (shouldBlock) {
                repository.recordAppOpen(pkg)
                lastInterceptionTime = now
                triggerHapticInterception()
                performGlobalAction(GLOBAL_ACTION_HOME)
                val appLabel = matchedApp?.name ?: pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
                val reasonMsg = when {
                    isCurfewActive -> "$appLabel (Bedtime Curfew Active 🌙)"
                    isMorningActive -> "$appLabel (Morning Dopamine Shield 🌅)"
                    activeSchedule != null -> "$appLabel (Schedule: ${activeSchedule.name})"
                    isOverDailyLimit -> "$appLabel (Daily ${matchedApp?.dailyLimitMinutes ?: 15}m Limit Reached)"
                    isExplicitlyBlocked -> "$appLabel (Blocked by Shield Rule 🛡️)"
                    else -> appLabel
                }
                FocusFlowWidgetProvider.updateAllWidgets(applicationContext)
                launchMindfulPause(reasonMsg, pkg)
                return
            }
        }
    }

    private var currentForegroundPackage: String? = null
    private var foregroundAppStartTime = 0L
    private var lastScrollNudgeTriggerTime = 0L

    private fun checkScrollNudge(pkg: String, now: Long) {
        val config = repository.getScrollNudgeConfig()
        if (!config.isEnabled) return

        if (currentForegroundPackage != pkg) {
            currentForegroundPackage = pkg
            foregroundAppStartTime = now
            lastScrollNudgeTriggerTime = 0L
            return
        }

        val elapsedMinutes = (now - foregroundAppStartTime) / 60000L
        val interval = config.nudgeIntervalMinutes.coerceAtLeast(5)
        if (elapsedMinutes >= interval && (now - lastScrollNudgeTriggerTime) > (interval * 60000L)) {
            lastScrollNudgeTriggerTime = now
            triggerHapticInterception()
            val appLabel = pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            launchMindfulPause("$appLabel (${elapsedMinutes}m Continuous Scroll Nudge 🧘)", pkg)
        }
    }

    private fun isIncognitoTabActive(root: AccessibilityNodeInfo): Boolean {
        try {
            val queue = ArrayDeque<AccessibilityNodeInfo>()
            queue.add(root)
            var visited = 0
            while (queue.isNotEmpty() && visited < 70) {
                val node = queue.poll() ?: continue
                visited++
                val text = node.text?.toString()?.lowercase() ?: ""
                val desc = node.contentDescription?.toString()?.lowercase() ?: ""
                val viewId = node.viewIdResourceName?.lowercase() ?: ""

                if (text.contains("you've gone incognito") ||
                    text.contains("incognito tab") ||
                    desc.contains("incognito") ||
                    desc.contains("inprivate") ||
                    viewId.contains("incognito_badge") ||
                    viewId.contains("incognito_toggle") ||
                    desc.contains("close incognito")
                ) {
                    return true
                }
                for (i in 0 until node.childCount) {
                    node.getChild(i)?.let { queue.add(it) }
                }
            }
        } catch (_: Exception) {}
        return false
    }

    private fun detectBlockedWebsiteInBrowserAdvanced(root: AccessibilityNodeInfo): Pair<Boolean, String?> {
        try {
            val queue = ArrayDeque<AccessibilityNodeInfo>()
            queue.add(root)
            var visitedCount = 0

            while (queue.isNotEmpty() && visitedCount < 90) {
                val node = queue.poll() ?: continue
                visitedCount++

                val viewId = node.viewIdResourceName?.lowercase() ?: ""
                val text = node.text?.toString()?.lowercase() ?: ""
                val desc = node.contentDescription?.toString()?.lowercase() ?: ""

                val isUrlNode = URL_BAR_IDS.any { viewId.contains(it) }

                if (isUrlNode) {
                    val candidate = "$text $desc".trim()
                    val (blocked, categoryLabel) = repository.isDomainBlocked(candidate)
                    if (blocked && categoryLabel != null) {
                        return Pair(true, "$candidate ($categoryLabel)")
                    }
                }

                val childCount = node.childCount
                for (i in 0 until childCount) {
                    node.getChild(i)?.let { queue.add(it) }
                }
            }
        } catch (_: Exception) {}
        return Pair(false, null)
    }

    private fun detectBlockedWebsiteInBrowser(root: AccessibilityNodeInfo, blockedSites: List<String>): String? {
        val (blocked, reason) = detectBlockedWebsiteInBrowserAdvanced(root)
        return if (blocked) reason else null
    }

    private fun isInstagramReelsActive(root: AccessibilityNodeInfo): Boolean {
        try {
            val queue = ArrayDeque<AccessibilityNodeInfo>()
            queue.add(root)
            var visitedCount = 0

            while (queue.isNotEmpty() && visitedCount < 80) {
                val node = queue.poll() ?: continue
                visitedCount++

                val viewId = node.viewIdResourceName?.lowercase() ?: ""
                val text = node.text?.toString()?.lowercase() ?: ""
                val desc = node.contentDescription?.toString()?.lowercase() ?: ""

                // 1. Matches Instagram Reels specific View IDs
                if (INSTAGRAM_REELS_IDS.any { viewId.contains(it) }) {
                    return true
                }

                // 2. Matches Instagram Reels specific audio/template texts
                if (INSTAGRAM_REELS_TEXTS.any { text.contains(it) || desc.contains(it) }) {
                    return true
                }

                // 3. Reels bottom tab when selected
                if (desc.contains("reels") && node.isSelected) {
                    return true
                }

                val childCount = node.childCount
                for (i in 0 until childCount) {
                    node.getChild(i)?.let { queue.add(it) }
                }
            }
        } catch (ignored: Exception) {}
        return false
    }

    private fun isYouTubeShortsActive(root: AccessibilityNodeInfo): Boolean {
        try {
            val queue = ArrayDeque<AccessibilityNodeInfo>()
            queue.add(root)
            var visitedCount = 0

            while (queue.isNotEmpty() && visitedCount < 80) {
                val node = queue.poll() ?: continue
                visitedCount++

                val viewId = node.viewIdResourceName?.lowercase() ?: ""
                val text = node.text?.toString()?.lowercase() ?: ""
                val desc = node.contentDescription?.toString()?.lowercase() ?: ""

                // 1. Matches YouTube Shorts specific Player View IDs
                if (YOUTUBE_SHORTS_IDS.any { viewId.contains(it) }) {
                    return true
                }

                // 2. Matches YouTube Shorts specific action button descriptions/texts
                if (YOUTUBE_SHORTS_TEXTS.any { text.contains(it) || desc.contains(it) }) {
                    return true
                }

                // 3. Shorts bottom tab in navigation bar when selected
                if ((desc == "shorts" || text == "shorts") && node.isSelected) {
                    return true
                }

                val childCount = node.childCount
                for (i in 0 until childCount) {
                    node.getChild(i)?.let { queue.add(it) }
                }
            }
        } catch (ignored: Exception) {}
        return false
    }

    private fun containsKeyword(root: AccessibilityNodeInfo, keywords: List<String>): Boolean {
        try {
            val queue = ArrayDeque<AccessibilityNodeInfo>()
            queue.add(root)
            var visitedCount = 0

            while (queue.isNotEmpty() && visitedCount < 80) {
                val node = queue.poll() ?: continue
                visitedCount++

                val viewId = node.viewIdResourceName?.lowercase() ?: ""
                val text = node.text?.toString()?.lowercase() ?: ""
                val desc = node.contentDescription?.toString()?.lowercase() ?: ""

                for (kw in keywords) {
                    if (viewId.contains(kw) || text.contains(kw) || desc.contains(kw)) {
                        return true
                    }
                }

                val childCount = node.childCount
                for (i in 0 until childCount) {
                    node.getChild(i)?.let { queue.add(it) }
                }
            }
        } catch (ignored: Exception) {}
        return false
    }

    private fun launchMindfulPause(appName: String, targetPackage: String = "") {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("TRIGGER_MINDFUL_PAUSE", true)
                putExtra("BLOCKED_APP_NAME", appName)
                putExtra("BLOCKED_PACKAGE_NAME", targetPackage)
            }
            startActivity(intent)
        } catch (ignored: Exception) {}
    }

    private fun launchRegainTimesUp(pkg: String) {
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        }
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("TRIGGER_REGAIN_TIMES_UP", true)
                putExtra("TIMES_UP_APP_NAME", appName)
                putExtra("TIMES_UP_PACKAGE_NAME", pkg)
            }
            startActivity(intent)
        } catch (_: Exception) {}
    }

    private fun launchAntiTamperWarning() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("TRIGGER_ANTI_TAMPER_ALERT", true)
            }
            startActivity(intent)
        } catch (_: Exception) {}
    }

    private fun triggerHapticInterception() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80L)
            }
        } catch (ignored: Exception) {}
    }

    private fun isAppAllowedInWorkMode(pkg: String): Boolean {
        // 1. FocusFlow app itself is always allowed
        if (pkg == packageName) return true

        // 2. Active emergency pass for this app
        if (repository.isEmergencyPassActiveFor(pkg)) return true

        // 3. System core essentials (launcher, keyboards, settings)
        if (isSystemEssentialPackage(pkg)) return true

        // 4. Phone & calling apps
        if (isPhoneOrCallApp(pkg)) return true

        // 5. Office Products & WhatsApp (Teams, Outlook, WhatsApp, MS 365, Slack, Docs)
        if (repository.isWorkAppAllowed(pkg)) return true

        return false
    }

    private fun isAppAllowedDuringFocus(pkg: String): Boolean {
        // 1. FocusFlow app itself is always allowed
        if (pkg == packageName) return true

        // 2. Active emergency pass for this app
        if (repository.isEmergencyPassActiveFor(pkg)) return true

        // 3. System core essentials (launcher, keyboards, settings)
        if (isSystemEssentialPackage(pkg)) return true

        // 4. Phone & calling apps
        if (isPhoneOrCallApp(pkg)) return true

        // 5. Work app allowed if Work Mode is active
        if (repository.isWorkModeActive() && repository.isWorkAppAllowed(pkg)) return true

        // 6. Productive whitelist apps
        if (repository.isProductiveAppAllowed(pkg)) return true

        return false
    }

    private fun isSystemEssentialPackage(pkg: String): Boolean {
        if (pkg == "android" || pkg == "com.android.systemui") return true
        if (pkg.contains("inputmethod") || pkg.contains("keyboard") || pkg == "com.google.android.inputmethod.latin") return true
        if (pkg == "com.android.settings" || pkg == "com.google.android.permissioncontroller" || pkg == "com.android.packageinstaller") return true

        // System Launcher / Home Screen
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
        // 1. Active incoming/outgoing telephone call
        try {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            if (telephonyManager != null && telephonyManager.callState != TelephonyManager.CALL_STATE_IDLE) {
                return true
            }
        } catch (ignored: Exception) {}

        // 2. Default system dialer
        try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            val defaultDialer = telecomManager?.defaultDialerPackage
            if (defaultDialer != null && pkg == defaultDialer) {
                return true
            }
        } catch (ignored: Exception) {}

        // 3. Standard dialer and calling packages
        val dialerPackages = setOf(
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.android.phone",
            "com.android.incallui",
            "com.google.android.apps.messaging",
            "com.android.mms"
        )
        if (dialerPackages.contains(pkg)) return true

        // 4. Resolves to dial intent
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            val resolveList = packageManager.queryIntentActivities(dialIntent, 0)
            if (resolveList.any { it.activityInfo.packageName == pkg }) {
                return true
            }
        } catch (ignored: Exception) {}

        return false
    }

    private fun launchFocusLockdown(pkg: String, isWorkMode: Boolean = false) {
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            when {
                pkg.contains("chrome") -> "Chrome Web"
                pkg.contains("instagram") -> "Instagram"
                pkg.contains("youtube") -> "YouTube"
                pkg.contains("tiktok") -> "TikTok"
                pkg.contains("reddit") -> "Reddit"
                pkg.contains("facebook") -> "Facebook"
                else -> pkg
            }
        }

        // Kick the distracting app out
        performGlobalAction(GLOBAL_ACTION_HOME)

        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("TRIGGER_FOCUS_LOCKDOWN", true)
                putExtra("LOCKED_APP_NAME", appName)
                putExtra("LOCKED_PACKAGE_NAME", pkg)
                putExtra("IS_WORK_MODE", isWorkMode)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = ActivityOptions.makeBasic().apply {
                    setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                }
                val pi = PendingIntent.getActivity(
                    this,
                    101,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    options.toBundle()
                )
                pi.send()
            } else {
                startActivity(intent)
            }
        } catch (ignored: Exception) {}
    }

    // --- REGAIN CLONE: FLOATING LIVE SESSION COUNTDOWN PILL ---
    private fun showFloatingCountdownPill(session: ActiveMicroSession) {
        val pillConfig = repository.getFloatingPillConfig()
        if (!pillConfig.isEnabled) return

        pillMainHandler.post {
            try {
                if (windowManager == null) {
                    windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                }
                val wm = windowManager ?: return@post

                if (floatingPillView == null) {
                    val tv = TextView(this).apply {
                        val density = resources.displayMetrics.density
                        val padH = (14 * density).toInt()
                        val padV = (7 * density).toInt()
                        setPadding(padH, padV, padH, padV)
                        setTextColor(Color.WHITE)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                        setTypeface(typeface, android.graphics.Typeface.BOLD)

                        val bg = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 24 * density
                            setColor(Color.parseColor("#E60F172A")) // Slate-900 glass
                            setStroke((1.5f * density).toInt(), Color.parseColor("#38BDF8")) // FocusAccent border
                        }
                        background = bg
                        elevation = 12 * density
                    }

                    val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        WindowManager.LayoutParams.TYPE_PHONE
                    }

                    floatingPillParams = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        overlayType,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        android.graphics.PixelFormat.TRANSLUCENT
                    ).apply {
                        gravity = Gravity.TOP or Gravity.END
                        x = (16 * resources.displayMetrics.density).toInt()
                        y = (80 * resources.displayMetrics.density).toInt()
                    }

                    // Drag to reposition
                    var initialX = 0
                    var initialY = 0
                    var initialTouchX = 0f
                    var initialTouchY = 0f
                    tv.setOnTouchListener { view, motionEvent ->
                        val params = floatingPillParams ?: return@setOnTouchListener false
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                initialX = params.x
                                initialY = params.y
                                initialTouchX = motionEvent.rawX
                                initialTouchY = motionEvent.rawY
                                true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                params.x = initialX - (motionEvent.rawX - initialTouchX).toInt()
                                params.y = initialY + (motionEvent.rawY - initialTouchY).toInt()
                                try {
                                    wm.updateViewLayout(view, params)
                                } catch (_: Exception) {}
                                true
                            }
                            else -> false
                        }
                    }
                    floatingPillView = tv
                }

                val currentSession = repository.getActiveMicroSession()
                if (currentSession != null && !currentSession.isExpired) {
                    floatingPillView?.text = "⏳ ${currentSession.formattedRemaining} • ${currentSession.intention}"
                }

                if (!isPillAttached && floatingPillView != null && floatingPillParams != null) {
                    try {
                        wm.addView(floatingPillView, floatingPillParams)
                        isPillAttached = true
                        pillMainHandler.removeCallbacks(updateFloatingPillRunnable)
                        pillMainHandler.post(updateFloatingPillRunnable)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateFloatingPillText() {
        val currentSession = repository.getActiveMicroSession()
        if (currentSession == null || currentSession.isExpired) {
            hideFloatingCountdownPill()
            return
        }
        floatingPillView?.text = "⏳ ${currentSession.formattedRemaining} • ${currentSession.intention}"
    }

    private fun hideFloatingCountdownPill() {
        pillMainHandler.post {
            pillMainHandler.removeCallbacks(updateFloatingPillRunnable)
            if (isPillAttached && floatingPillView != null && windowManager != null) {
                try {
                    windowManager?.removeView(floatingPillView)
                } catch (_: Exception) {}
                isPillAttached = false
            }
        }
    }

    // --- REGAIN CLONE: REEL SWIPE COUNTER NUDGE ---
    private fun checkReelSwipe(pkg: String, now: Long) {
        val config = repository.getReelSwipeNudgeConfig()
        if (!config.isEnabled) return

        val isShortsApp = pkg == "com.instagram.android" ||
                pkg == "com.google.android.youtube" ||
                pkg == "com.zhiliaoapp.musically" ||
                pkg == "com.ss.android.ugc.trill" ||
                pkg == "com.snapchat.android"

        if (!isShortsApp) return

        if (now - lastReelSwipeTime > 800L) {
            lastReelSwipeTime = now
            reelSwipeCounter++
            if (reelSwipeCounter >= config.swipeInterval) {
                reelSwipeCounter = 0
                triggerHapticInterception()
                val appLabel = if (pkg.contains("instagram")) "Instagram Reels"
                    else if (pkg.contains("youtube")) "YouTube Shorts"
                    else if (pkg.contains("snapchat")) "Snapchat Spotlight"
                    else "Shorts Feeds"
                launchReelSwipeIntervention(appLabel, config.swipeInterval)
            }
        }
    }

    private fun launchReelSwipeIntervention(appName: String, count: Int) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("TRIGGER_REEL_SWIPE_INTERVENTION", true)
                putExtra("REEL_SWIPE_APP_NAME", appName)
                putExtra("REEL_SWIPE_COUNT", count)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = ActivityOptions.makeBasic().apply {
                    setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                }
                val pi = PendingIntent.getActivity(
                    this,
                    1008,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    options.toBundle()
                )
                pi.send()
            } else {
                startActivity(intent)
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        hideFloatingCountdownPill()
    }

    override fun onInterrupt() {
        // Required callback
    }
}