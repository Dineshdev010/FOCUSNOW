package com.focusflow.app.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import com.focusflow.app.MainActivity
import com.focusflow.app.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.data.FocusFlowRepository
import com.focusflow.app.model.*
import com.focusflow.app.util.FocusSoundManager
import com.focusflow.app.util.PermissionHelper
import com.focusflow.app.widget.FocusFlowWidgetProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TimerMode(
    val title: String,
    val durationSeconds: Int
)

class FocusFlowViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FocusFlowRepository(application.applicationContext)

    // Flow Stage (Splash -> Onboarding -> Main)
    var appStage by mutableStateOf(AppStage.SPLASH)
    var selectedTab by mutableStateOf("Home")

    // Gamification & Security
    var focusCoins by mutableIntStateOf(100)
        private set
    var lockPin by mutableStateOf("1234")
        private set
    var userGoal by mutableStateOf("Master Focus & Eliminate Distractions")
        private set
    var blockedApps by mutableStateOf<List<BlockedApp>>(emptyList())
        private set
    var blockedWebsites by mutableStateOf<List<String>>(emptyList())
        private set
    var focusSchedules by mutableStateOf<List<FocusSchedule>>(emptyList())
        private set

    // Regain Digital Wellbeing Highlights
    var savedScreenTimeMinutes by mutableIntStateOf(0)
        private set
    var reelsBlockedCount by mutableIntStateOf(19)
        private set
    var interventionsAccepted by mutableIntStateOf(14)
        private set
    var dopamineScore by mutableIntStateOf(92)
        private set
    var shortFormConfig by mutableStateOf(ShortFormBlockerConfig())
        private set
    var activeSoundscape by mutableStateOf(FocusSoundscape.SILENT)
        private set
    var bonsaiStatus by mutableStateOf(repository.getBonsaiStatus())
        private set
    var showBonsaiToastMessage by mutableStateOf<String?>(null)

    // Modals
    var showMindfulPauseModal by mutableStateOf(false)
    var mindfulPauseAppName by mutableStateOf("Instagram Reels")
    var mindfulPausePackageName by mutableStateOf("")
    var showReelsBlockerSheet by mutableStateOf(false)
    var showJigsawPuzzleModal by mutableStateOf(false)
    var jigsawBreakReason by mutableStateOf("Solve puzzle to unlock a 5-minute break")
    var showAddScheduleModal by mutableStateOf(false)
    var showRewardsStoreModal by mutableStateOf(false)
    var showEmergencyPassModal by mutableStateOf(false)
    var emergencyPassApp by mutableStateOf("Instagram Reels")
    var emergencyPassSecondsLeft by mutableIntStateOf(0)
    var isEmergencyPassActive by mutableStateOf(false)

    // Regain Clone: Advanced Web Shield, Micro-Sessions & Morning Shield
    var webShieldSettings by mutableStateOf(WebShieldSettings())
    var activeMicroSession by mutableStateOf<ActiveMicroSession?>(null)
    var morningProtectionConfig by mutableStateOf(MorningProtectionConfig())
    var scrollNudgeConfig by mutableStateOf(ScrollNudgeConfig())
    var antiTamperConfig by mutableStateOf(RegainAntiTamperConfig())
    var regainFrictionMode by mutableStateOf(RegainFrictionMode.BREATHING)
    var reelSwipeNudgeConfig by mutableStateOf(ReelSwipeNudgeConfig())
    var habitResistanceStats by mutableStateOf(HabitResistanceStats())
    var floatingPillConfig by mutableStateOf(FloatingPillConfig())
    var showReelSwipeInterventionModal by mutableStateOf(false)
    var reelSwipeInterventionCount by mutableStateOf(10)
    var reelSwipeInterventionAppName by mutableStateOf("Reels & Shorts")

    var showRegainTimesUpModal by mutableStateOf(false)
    var regainTimesUpAppName by mutableStateOf("App")
    var regainTimesUpPackageName by mutableStateOf("")
    var showAntiTamperAlertModal by mutableStateOf(false)

    // Deep Focus Lockdown Mode States
    var showFocusLockdownModal by mutableStateOf(false)
    var focusLockdownAppName by mutableStateOf("Distracting App")
    var focusLockdownPackageName by mutableStateOf("")
    var isWorkModeInterception by mutableStateOf(false)
    var isWorkModeEnabled by mutableStateOf(false)
    var showProductiveWhitelistModal by mutableStateOf(false)
    var productiveAppItems by mutableStateOf<List<ProductiveAppItem>>(emptyList())
        private set

    // Focus Notification Shield & Digest States
    var showFocusDigestModal by mutableStateOf(false)
    var batchedNotifications by mutableStateOf<List<BatchedNotification>>(emptyList())
        private set
    var isNotificationShieldEnabled by mutableStateOf(true)
        private set
    var hasNotificationListenerPermission by mutableStateOf(false)
        private set

    // Real Device Screen Time Stats
    var deviceUsageStats by mutableStateOf<List<com.focusflow.app.util.AppUsageStat>>(emptyList())
        private set
    var hasUsageAccessPermission by mutableStateOf(false)
        private set

    // Rewards Store & Themes
    var storeItems by mutableStateOf<List<StoreItem>>(emptyList())
        private set
    var activeThemeId by mutableStateOf("theme_lime")
        private set
    var selectedJigsawTheme by mutableStateOf(JigsawTheme.AURORA)
        private set

    // Strict Lock Mode During Focus
    var isStrictModeEnabled by mutableStateOf(true)
    var isStrictLockActive by mutableStateOf(false)
    var showEmergencyUnlockModal by mutableStateOf(false)
    var showCodeChallengeModal by mutableStateOf(false)

    // Sleep Tracking & Curfew State
    var sleepTrackingState by mutableStateOf(repository.getSleepTrackingState())
        private set
    var showSleepTrackerModal by mutableStateOf(false)

    // All-Apps Shield & Every App Blocker State
    var showAppBlockerRulesModal by mutableStateOf(false)
    var allAppShieldRules by mutableStateOf<List<AppShieldRuleItem>>(emptyList())
        private set

    // Coding Challenges / Learn to Earn
    val challenges: List<CodingChallenge> = CodingChallengeCatalog.sampleChallenges
    var solvedChallengeIds by mutableStateOf<Set<Int>>(emptySet())
        private set
    var currentChallengeIndex by mutableIntStateOf(0)

    // Tasks State
    var tasks by mutableStateOf<List<TaskItem>>(emptyList())
        private set
    var searchQuery by mutableStateOf("")
    var selectedCategoryFilter by mutableStateOf("All")
    var selectedStatusFilter by mutableStateOf("All")
    var selectedTaskTab by mutableStateOf("All") // "All", "Inbox", "Today", "Upcoming", "Completed"
    var taskSortOrder by mutableStateOf("Priority") // "Priority", "DueDate", "Title"
    var showTaskEditorModal by mutableStateOf(false)
    var editingTaskItem by mutableStateOf<TaskItem?>(null)

    // Goals State
    var goals by mutableStateOf<List<GoalItem>>(emptyList())
        private set
    var showCreateGoalDialog by mutableStateOf(false)

    // Habits State
    var habits by mutableStateOf<List<HabitItem>>(emptyList())
        private set
    var showCreateHabitDialog by mutableStateOf(false)

    // Focus Session History Log
    var focusSessionLogs by mutableStateOf<List<FocusSessionRecord>>(emptyList())
        private set
    var showSessionHistoryModal by mutableStateOf(false)
    var selectedTaskForFocus by mutableStateOf<TaskItem?>(null)

    // Global Search Modal
    var showGlobalSearchModal by mutableStateOf(false)
    var globalSearchQuery by mutableStateOf("")

    // Stats State
    var totalFocusMinutes by mutableIntStateOf(45)
        private set
    var completedSessions by mutableIntStateOf(2)
        private set
    var streakDays by mutableIntStateOf(5)
        private set
    var longestStreakDays by mutableIntStateOf(14)
        private set
    var streakFreezes by mutableIntStateOf(1)
        private set
    var activeDaysOfWeek by mutableStateOf<List<Boolean>>(listOf(true, true, true, true, false, false, false))
        private set
    var showStreakModal by mutableStateOf(false)
    var showGoalModal by mutableStateOf(false)
    var showJigsawModal: Boolean
        get() = showJigsawPuzzleModal
        set(value) { showJigsawPuzzleModal = value }
    var weeklyActivity by mutableStateOf<List<Pair<String, Float>>>(emptyList())
        private set

    // Timer State
    var customFocusMinutes by mutableIntStateOf(25)
        private set
    var customShortBreakMinutes by mutableIntStateOf(5)
        private set
    var customLongBreakMinutes by mutableIntStateOf(15)
        private set
    var dailyFocusGoalMinutes by mutableIntStateOf(120)
        private set
    var analyticsDateRange by mutableStateOf("This Week")

    val modes: List<TimerMode>
        get() = listOf(
            TimerMode("Focus (${customFocusMinutes}m)", customFocusMinutes * 60),
            TimerMode("Short Break (${customShortBreakMinutes}m)", customShortBreakMinutes * 60),
            TimerMode("Long Break (${customLongBreakMinutes}m)", customLongBreakMinutes * 60)
        )

    var currentModeIndex by mutableIntStateOf(0)
        private set
    var timerRemainingSeconds by mutableIntStateOf(25 * 60)
        private set
    var isTimerRunning by mutableStateOf(false)
    private var timerJob: Job? = null

    fun setFocusDurationMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(1, 360)
        customFocusMinutes = clamped
        repository.setCustomFocusMinutes(clamped)
        if (currentModeIndex == 0 && !isTimerRunning) {
            timerRemainingSeconds = clamped * 60
        }
    }

    fun setShortBreakDurationMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(1, 60)
        customShortBreakMinutes = clamped
        repository.setCustomBreakMinutes(clamped)
        if (currentModeIndex == 1 && !isTimerRunning) {
            timerRemainingSeconds = clamped * 60
        }
    }

    fun setLongBreakDurationMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(1, 120)
        customLongBreakMinutes = clamped
        repository.setCustomLongBreakMinutes(clamped)
        if (currentModeIndex == 2 && !isTimerRunning) {
            timerRemainingSeconds = clamped * 60
        }
    }

    @JvmName("updateDailyFocusGoalMinutes")
    fun setDailyFocusGoalMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(15, 480)
        dailyFocusGoalMinutes = clamped
        repository.setDailyFocusGoal(clamped)
    }

    fun adjustTimerMinutes(deltaMinutes: Int) {
        if (!isTimerRunning) {
            when (currentModeIndex) {
                0 -> {
                    val clamped = (customFocusMinutes + deltaMinutes).coerceIn(1, 360)
                    setFocusDurationMinutes(clamped)
                }
                1 -> {
                    val clamped = (customShortBreakMinutes + deltaMinutes).coerceIn(1, 60)
                    customShortBreakMinutes = clamped
                    repository.setCustomBreakMinutes(clamped)
                    timerRemainingSeconds = clamped * 60
                }
                2 -> {
                    val clamped = (customLongBreakMinutes + deltaMinutes).coerceIn(1, 120)
                    customLongBreakMinutes = clamped
                    timerRemainingSeconds = clamped * 60
                }
            }
        }
    }

    init {
        refreshStateFromRepository()
    }

    fun onSplashFinished() {
        appStage = if (repository.isOnboardingCompleted()) {
            AppStage.MAIN
        } else {
            AppStage.ONBOARDING
        }
    }

    fun refreshStateFromRepository() {
        focusCoins = repository.getFocusCoins()
        lockPin = repository.getLockPin()
        userGoal = repository.getUserGoal()
        blockedApps = repository.getBlockedApps()
        blockedWebsites = repository.getBlockedWebsites()
        isStrictModeEnabled = repository.isStrictModeEnabled()
        savedScreenTimeMinutes = repository.getSavedScreenTimeMinutes()
        reelsBlockedCount = repository.getReelsBlockedCount()
        interventionsAccepted = repository.getInterventionsAccepted()
        dopamineScore = repository.getDopamineScore()
        shortFormConfig = repository.getShortFormConfig()
        activeSoundscape = repository.getActiveSoundscape()
        tasks = repository.loadTasks()
        goals = repository.loadGoals()
        habits = repository.loadHabits()
        focusSessionLogs = repository.loadFocusSessions()
        recalculateGoalsProgress()
        totalFocusMinutes = repository.getTotalFocusMinutes()
        completedSessions = repository.getCompletedSessions()
        streakDays = repository.checkInDailyStreak()
        longestStreakDays = repository.getLongestStreakDays()
        streakFreezes = repository.getStreakFreezes()
        activeDaysOfWeek = repository.getActiveDaysOfWeek()
        weeklyActivity = repository.getWeeklyActivityHours()
        solvedChallengeIds = repository.getSolvedChallengeIds()
        focusSchedules = repository.getFocusSchedules()
        storeItems = repository.getStoreItems()
        val equippedTheme = repository.getActiveThemeId()
        activeThemeId = equippedTheme
        com.focusflow.app.ui.theme.applyThemeById(equippedTheme)
        selectedJigsawTheme = repository.getSelectedJigsawTheme()
        productiveAppItems = repository.getProductiveAppItems()
        isWorkModeEnabled = repository.isWorkModeEnabled()
        isNotificationShieldEnabled = repository.isNotificationShieldEnabled()
        checkAndRefreshDeviceUsage()
        batchedNotifications = repository.getBatchedNotifications()
        bonsaiStatus = repository.getBonsaiStatus()
        sleepTrackingState = repository.getSleepTrackingState()
        webShieldSettings = repository.getWebShieldSettings()
        activeMicroSession = repository.getActiveMicroSession()
        morningProtectionConfig = repository.getMorningProtectionConfig()
        scrollNudgeConfig = repository.getScrollNudgeConfig()
        antiTamperConfig = repository.getAntiTamperConfig()
        regainFrictionMode = repository.getRegainFrictionMode()
        reelSwipeNudgeConfig = repository.getReelSwipeNudgeConfig()
        habitResistanceStats = repository.getHabitResistanceStats()
        floatingPillConfig = repository.getFloatingPillConfig()
        allAppShieldRules = repository.getAllAppShieldRules()
        customFocusMinutes = repository.getCustomFocusMinutes()
        customShortBreakMinutes = repository.getCustomBreakMinutes()
        customLongBreakMinutes = repository.getCustomLongBreakMinutes()
        dailyFocusGoalMinutes = repository.getDailyFocusGoal()
        if (!isTimerRunning && currentModeIndex == 0) {
            timerRemainingSeconds = customFocusMinutes * 60
        }
        checkNotificationListenerPermission()
        checkAndRefreshDeviceUsage()
        FocusFlowWidgetProvider.updateAllWidgets(getApplication())
    }

    // Timer Controls
    fun startTimer() {
        if (isTimerRunning) return
        isTimerRunning = true

        if (currentModeIndex == 0) {
            // Activate deep focus lockdown across the device
            repository.setFocusSessionActive(true, System.currentTimeMillis() + (timerRemainingSeconds * 1000L))
        }

        if (activeSoundscape != FocusSoundscape.SILENT) {
            FocusSoundManager.play(activeSoundscape)
        }

        timerJob = viewModelScope.launch {
            while (isActive && timerRemainingSeconds > 0) {
                delay(1000L)
                timerRemainingSeconds--
            }

            if (timerRemainingSeconds <= 0) {
                onTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        isTimerRunning = false
        timerJob?.cancel()
        FocusSoundManager.stop()
        repository.setFocusSessionActive(false)
    }

    fun resetTimer() {
        pauseTimer()
        timerRemainingSeconds = modes[currentModeIndex].durationSeconds
        isStrictLockActive = false
        repository.setFocusSessionActive(false)
    }

    fun selectMode(index: Int) {
        if (index in modes.indices) {
            pauseTimer()
            currentModeIndex = index
            timerRemainingSeconds = modes[index].durationSeconds
            isStrictLockActive = false
            repository.setFocusSessionActive(false)
        }
    }

    private fun onTimerFinished() {
        isTimerRunning = false
        timerJob?.cancel()
        FocusSoundManager.stop()
        repository.setFocusSessionActive(false)

        try {
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
        } catch (ignored: Exception) {}

        if (currentModeIndex == 0) {
            val focusMins = customFocusMinutes
            repository.addFocusMinutes(focusMins)
            repository.incrementCompletedSessions()
            repository.addFocusCoins(25)
            repository.addBonsaiWater(35) // Water Bonsai on completing deep focus session!

            // Record focus session history
            val sessionRecord = FocusSessionRecord(
                sessionType = "Focus",
                plannedMinutes = focusMins,
                actualMinutes = focusMins,
                startTimestamp = System.currentTimeMillis() - (focusMins * 60 * 1000L),
                endTimestamp = System.currentTimeMillis(),
                status = "COMPLETED",
                associatedTaskTitle = selectedTaskForFocus?.title
            )
            repository.recordFocusSession(sessionRecord)
            focusSessionLogs = repository.loadFocusSessions()

            // Update associated task if any
            selectedTaskForFocus?.let { task ->
                val updatedTask = task.copy(
                    updatedAt = System.currentTimeMillis()
                )
                val current = tasks.map { if (it.id == task.id) updatedTask else it }
                tasks = current
                repository.saveTasks(current)
            }

            recalculateGoalsProgress()

            // Auto-trigger Focus Digest if notifications were shielded during session!
            refreshBatchedNotifications()
            if (batchedNotifications.isNotEmpty()) {
                showFocusDigestModal = true
            }
        }

        isStrictLockActive = false
        refreshStateFromRepository()
        selectMode(if (currentModeIndex == 0) 1 else 0)
    }

    // Regain Ambient Soundscapes
    fun selectSoundscape(soundscape: FocusSoundscape) {
        activeSoundscape = soundscape
        repository.saveActiveSoundscape(soundscape)
        if (isTimerRunning) {
            FocusSoundManager.play(soundscape)
        } else {
            FocusSoundManager.stop()
        }
    }

    // Regain Reels & Shorts Shield Toggles
    fun toggleReelsBlocker(enabled: Boolean) {
        val updated = shortFormConfig.copy(blockInstagramReels = enabled)
        shortFormConfig = updated
        repository.saveShortFormConfig(updated)
    }

    fun toggleShortsBlocker(enabled: Boolean) {
        val updated = shortFormConfig.copy(blockYouTubeShorts = enabled)
        shortFormConfig = updated
        repository.saveShortFormConfig(updated)
    }

    fun toggleSpotlightBlocker(enabled: Boolean) {
        val updated = shortFormConfig.copy(blockSnapchatSpotlight = enabled)
        shortFormConfig = updated
        repository.saveShortFormConfig(updated)
    }

    fun toggleWebDoomscroll(enabled: Boolean) {
        val updated = shortFormConfig.copy(blockWebDoomscrolling = enabled)
        shortFormConfig = updated
        repository.saveShortFormConfig(updated)
    }

    // Scheduled Focus Blocks & App Time Limits
    fun toggleFocusSchedule(scheduleId: String) {
        repository.toggleFocusSchedule(scheduleId)
        focusSchedules = repository.getFocusSchedules()
    }

    fun updateAppDailyLimit(packageName: String, limitMinutes: Int) {
        repository.updateAppDailyLimit(packageName, limitMinutes)
        blockedApps = repository.getBlockedApps()
    }

    fun getCurrentlyActiveSchedule(): FocusSchedule? {
        return repository.getCurrentlyActiveSchedule()
    }

    fun addCustomSchedule(name: String, emoji: String, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, daysSummary: String) {
        val newSchedule = FocusSchedule(
            id = "sched_${System.currentTimeMillis()}",
            name = name.trim().ifEmpty { "Focus Block" },
            emoji = emoji.ifEmpty { "💼" },
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            daysSummary = daysSummary.ifEmpty { "Daily" },
            isEnabled = true
        )
        repository.addFocusSchedule(newSchedule)
        focusSchedules = repository.getFocusSchedules()
        showAddScheduleModal = false
    }

    fun deleteFocusSchedule(id: String) {
        repository.deleteFocusSchedule(id)
        focusSchedules = repository.getFocusSchedules()
    }

    // Rewards Store & Themes
    fun purchaseStoreItem(itemId: String): Boolean {
        val success = repository.purchaseStoreItem(itemId)
        if (success) {
            refreshStateFromRepository()
        }
        return success
    }

    fun equipStoreItem(itemId: String) {
        repository.equipStoreItem(itemId)
        refreshStateFromRepository()
    }

    fun selectJigsawTheme(theme: JigsawTheme) {
        repository.setSelectedJigsawTheme(theme)
        selectedJigsawTheme = theme
    }

    // Mindful Pause Interventions (Regain / One Sec core loop)
    fun testMindfulIntervention(appName: String = "Instagram Reels", packageName: String = "") {
        mindfulPauseAppName = appName
        mindfulPausePackageName = packageName
        showMindfulPauseModal = true
    }

    fun recordWalkAwayMindfulChoice(minutesSaved: Int = 15, coinReward: Int = 10) {
        repository.incrementInterventionsAccepted()
        repository.recordImpulseIntercepted()
        repository.addSavedScreenTime(minutesSaved)
        repository.addFocusCoins(coinReward)
        repository.addBonsaiWater(15) // Walking away nourishes the bonsai!
        habitResistanceStats = repository.getHabitResistanceStats()
        refreshStateFromRepository()
    }

    // --- REGAIN CLONE: MICRO-SESSION PASS ENGINE ---
    fun startMicroSession(packageName: String, appName: String, minutes: Int, intention: String = "Quick Task") {
        repository.startMicroSession(packageName, appName, minutes, intention)
        repository.recordIntentionalPassGranted()
        if (packageName.isNotBlank()) {
            repository.incrementTodayAppOpenCount(packageName)
        }
        activeMicroSession = repository.getActiveMicroSession()
        habitResistanceStats = repository.getHabitResistanceStats()
        showMindfulPauseModal = false
        showRegainTimesUpModal = false

        if (packageName.isNotBlank()) {
            try {
                val context = getApplication<Application>().applicationContext
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    context.startActivity(launchIntent)
                }
                if (repository.isFloatingPillEnabled()) {
                    com.focusflow.app.service.FloatingFocusIslandService.start(context, appName.ifEmpty { "App" }, minutes * 60)
                }
            } catch (_: Exception) {}
        }
    }

    fun endMicroSession() {
        repository.clearMicroSession()
        activeMicroSession = null
        showRegainTimesUpModal = false
        try {
            val context = getApplication<Application>().applicationContext
            com.focusflow.app.service.FloatingFocusIslandService.stop(context)
        } catch (_: Exception) {}
    }

    fun extendMicroSessionOneMinute(packageName: String, appName: String) {
        startMicroSession(packageName, appName, 1, "1 Min Extension")
    }

    fun triggerRegainTimesUp(appName: String, packageName: String) {
        regainTimesUpAppName = appName
        regainTimesUpPackageName = packageName
        showRegainTimesUpModal = true
    }

    // --- REGAIN CLONE: ADVANCED WEB SHIELD CATEGORIES ---
    fun toggleWebCategory(category: WebBlockCategory, isEnabled: Boolean) {
        val current = repository.getWebShieldSettings()
        val updated = when (category) {
            WebBlockCategory.ADULT_NSFW -> current.copy(blockAdultNsfw = isEnabled)
            WebBlockCategory.SOCIAL_WEB -> current.copy(blockSocialWeb = isEnabled)
            WebBlockCategory.DOOMSCROLL_NEWS -> current.copy(blockDoomscrollNews = isEnabled)
            WebBlockCategory.GAMING_STREAMING -> current.copy(blockGamingStreaming = isEnabled)
        }
        repository.saveWebShieldSettings(updated)
        webShieldSettings = updated
        blockedWebsites = updated.customBlockedDomains
    }

    fun toggleIncognitoBlocker(isEnabled: Boolean) {
        val current = repository.getWebShieldSettings()
        val updated = current.copy(blockIncognitoMode = isEnabled)
        repository.saveWebShieldSettings(updated)
        webShieldSettings = updated
    }

    fun addCustomBlockedWebsite(domain: String) {
        repository.addBlockedWebsite(domain)
        webShieldSettings = repository.getWebShieldSettings()
        blockedWebsites = webShieldSettings.customBlockedDomains
    }

    fun removeCustomBlockedWebsite(domain: String) {
        repository.removeBlockedWebsite(domain)
        webShieldSettings = repository.getWebShieldSettings()
        blockedWebsites = webShieldSettings.customBlockedDomains
    }

    // --- REGAIN CLONE: MORNING PROTECTION & ANTI-TAMPER ---
    fun updateMorningProtection(isEnabled: Boolean, wakeH: Int, wakeM: Int, durationMins: Int) {
        val config = MorningProtectionConfig(isEnabled, wakeH, wakeM, durationMins)
        repository.saveMorningProtectionConfig(config)
        morningProtectionConfig = config
    }

    fun updateMorningProtection(config: MorningProtectionConfig) {
        repository.saveMorningProtectionConfig(config)
        morningProtectionConfig = config
    }

    fun updateScrollNudge(isEnabled: Boolean, intervalMinutes: Int) {
        val config = ScrollNudgeConfig(isEnabled, intervalMinutes)
        repository.saveScrollNudgeConfig(config)
        scrollNudgeConfig = config
    }

    fun updateScrollNudge(config: ScrollNudgeConfig) {
        repository.saveScrollNudgeConfig(config)
        scrollNudgeConfig = config
    }

    fun updateAntiTamper(preventSettings: Boolean, cooldownMinutes: Int) {
        val config = RegainAntiTamperConfig(preventSettings, cooldownMinutes)
        repository.saveAntiTamperConfig(config)
        antiTamperConfig = config
    }

    fun updateAntiTamper(config: RegainAntiTamperConfig) {
        repository.saveAntiTamperConfig(config)
        antiTamperConfig = config
    }

    // --- REGAIN CLONE: ADVANCED CONTROLS ---
    fun updateFrictionMode(mode: RegainFrictionMode) {
        repository.saveRegainFrictionMode(mode)
        regainFrictionMode = mode
    }

    fun updateReelSwipeNudge(isEnabled: Boolean, interval: Int) {
        val config = ReelSwipeNudgeConfig(isEnabled, interval)
        repository.saveReelSwipeNudgeConfig(config)
        reelSwipeNudgeConfig = config
    }

    fun updateFloatingPillConfig(isEnabled: Boolean) {
        val config = FloatingPillConfig(isEnabled, true)
        repository.saveFloatingPillConfig(config)
        floatingPillConfig = config
    }

    fun saveAppOpenQuota(packageName: String, maxOpens: Int) {
        repository.saveAppOpenQuota(packageName, maxOpens)
    }

    fun getAppOpenQuota(packageName: String): AppOpenQuota {
        return repository.getAppOpenQuotaModel(packageName)
    }

    fun triggerReelSwipeIntervention(appName: String = "Reels & Shorts", count: Int = 10) {
        reelSwipeInterventionAppName = appName
        reelSwipeInterventionCount = count
        showReelSwipeInterventionModal = true
    }

    fun waterBonsaiPlant() {
        if (focusCoins >= 15) {
            if (repository.deductFocusCoins(15)) {
                val leveledUp = repository.addBonsaiWater(25)
                refreshStateFromRepository()
                showBonsaiToastMessage = if (leveledUp) {
                    "🎉 Level Up! Bonsai grew into Stage ${bonsaiStatus.stage} (${bonsaiStatus.title})!"
                } else {
                    "💧 Nourished your Bonsai! (+25% Growth)"
                }
            }
        }
    }

    fun harvestAndPlantInSanctuary() {
        val harvested = repository.harvestAndPlantBonsai()
        if (harvested) {
            refreshStateFromRepository()
            showBonsaiToastMessage = "🌸 Planted in your Sanctuary! (+50 🪙 Harvest Bonus)"
        }
    }

    fun getOpportunityCostMetrics(): List<com.focusflow.app.model.OpportunityCostMetric> {
        val mins = savedScreenTimeMinutes
        return listOf(
            com.focusflow.app.model.OpportunityCostMetric(
                domain = "Knowledge & Books",
                emoji = "📖",
                value = "${(mins * 0.5).toInt()}",
                unit = "pages read",
                description = "Equivalent to ~${String.format(java.util.Locale.US, "%.1f", mins / 250f)} books completed"
            ),
            com.focusflow.app.model.OpportunityCostMetric(
                domain = "Health & Movement",
                emoji = "🏃",
                value = String.format(java.util.Locale.US, "%.1f", mins * 0.08),
                unit = "km walked",
                description = "Burned ~${mins * 4} active calories instead of sitting"
            ),
            com.focusflow.app.model.OpportunityCostMetric(
                domain = "Code & Mastery",
                emoji = "💻",
                value = "${(mins * 2.5).toInt()}",
                unit = "lines of code",
                description = "Deep technical craft and problem solving"
            ),
            com.focusflow.app.model.OpportunityCostMetric(
                domain = "Mindfulness & Peace",
                emoji = "🧘",
                value = "${mins * 5}",
                unit = "deep breaths",
                description = "Restored parasympathetic nervous calm"
            )
        )
    }

    fun getFocusFlowIndexMetric(): FocusFlowIndexMetric {
        val completedTaskCount = tasks.count { it.isCompleted }
        val totalTaskCount = tasks.size.coerceAtLeast(1)
        val taskRate = ((completedTaskCount.toFloat() / totalTaskCount) * 100).toInt().coerceIn(0, 100)

        val resistance = if (reelsBlockedCount > 0) {
            val totalAttempts = reelsBlockedCount + 2
            ((reelsBlockedCount.toFloat() / totalAttempts) * 100).toInt().coerceIn(80, 99)
        } else 95

        val deepWork = ((totalFocusMinutes / 120f) * 100).toInt().coerceIn(70, 98)
        val overall = ((resistance * 0.4f) + (deepWork * 0.35f) + (taskRate * 0.25f)).toInt().coerceIn(50, 100)

        val (title, sub) = when {
            overall >= 90 -> "Peak Dopamine Equilibrium" to "Your prefrontal cortex executive control is operating at optimal neurochemical flow."
            overall >= 75 -> "Strong Focus Momentum" to "High cognitive resilience; minimal distraction friction observed today."
            else -> "Rebuilding Focus Capacity" to "Use short 15-minute sprints and mindful pauses to steady attention spans."
        }

        return FocusFlowIndexMetric(
            overallScore = overall,
            distractionResistance = resistance,
            deepWorkIntensity = deepWork,
            goalExecution = taskRate,
            statusTitle = title,
            statusSubtitle = sub
        )
    }

    fun getScreenTimeComposition(): ScreenTimeComposition {
        val productiveMins = totalFocusMinutes + (savedScreenTimeMinutes * 0.6f).toInt()
        val savedMins = savedScreenTimeMinutes
        val prodHoursStr = "${productiveMins / 60}h ${productiveMins % 60}m"
        val savedHoursStr = "${savedMins / 60}h ${savedMins % 60}m"

        return ScreenTimeComposition(
            productivePercent = 0.68f,
            communicationPercent = 0.20f,
            distractingPercent = 0.12f,
            productiveHours = prodHoursStr,
            savedHours = savedHoursStr,
            doomscrollReductionPercent = 74
        )
    }

    fun getChronotypeHourlyRhythm(): List<ChronotypeHourlySlot> {
        return listOf(
            ChronotypeHourlySlot(0, "12a", 0.05f),
            ChronotypeHourlySlot(1, "1a", 0.02f),
            ChronotypeHourlySlot(2, "2a", 0.01f),
            ChronotypeHourlySlot(3, "3a", 0.01f),
            ChronotypeHourlySlot(4, "4a", 0.02f),
            ChronotypeHourlySlot(5, "5a", 0.08f),
            ChronotypeHourlySlot(6, "6a", 0.25f),
            ChronotypeHourlySlot(7, "7a", 0.45f),
            ChronotypeHourlySlot(8, "8a", 0.70f),
            ChronotypeHourlySlot(9, "9a", 0.95f, isPeak = true),
            ChronotypeHourlySlot(10, "10a", 1.00f, isPeak = true),
            ChronotypeHourlySlot(11, "11a", 0.92f, isPeak = true),
            ChronotypeHourlySlot(12, "12p", 0.65f),
            ChronotypeHourlySlot(13, "1p", 0.50f),
            ChronotypeHourlySlot(14, "2p", 0.38f, isVulnerable = true),
            ChronotypeHourlySlot(15, "3p", 0.42f, isVulnerable = true),
            ChronotypeHourlySlot(16, "4p", 0.75f),
            ChronotypeHourlySlot(17, "5p", 0.82f),
            ChronotypeHourlySlot(18, "6p", 0.60f),
            ChronotypeHourlySlot(19, "7p", 0.52f),
            ChronotypeHourlySlot(20, "8p", 0.48f),
            ChronotypeHourlySlot(21, "9p", 0.35f),
            ChronotypeHourlySlot(22, "10p", 0.25f, isVulnerable = true),
            ChronotypeHourlySlot(23, "11p", 0.12f, isVulnerable = true)
        )
    }

    fun getLifetimeImpactStats(): LifetimeImpactStats {
        val totalHours = (totalFocusMinutes + savedScreenTimeMinutes) / 60f + 32.8f
        val solvedCount = solvedChallengeIds.size + 14
        return LifetimeImpactStats(
            totalReclaimedHours = String.format(java.util.Locale.US, "%.1f", totalHours).toFloat(),
            shortFormInterceptions = reelsBlockedCount + 103,
            zenTreesGrown = bonsaiStatus.totalTreesGrown,
            puzzlesAndCodeSolved = solvedCount
        )
    }

    fun getAIFocusInsights(): List<AIFocusInsight> {
        return listOf(
            AIFocusInsight(
                id = "insight_peak_window",
                emoji = "⚡",
                category = "Circadian Peak",
                title = "Optimal Flow Window: 9:00 AM – 12:00 PM",
                message = "Your cortisol and prefrontal alertness are naturally elevated during morning hours.",
                actionableTip = "Schedule complex architecture & deep coding blocks before midday."
            ),
            AIFocusInsight(
                id = "insight_slump_defense",
                emoji = "🛡️",
                category = "Vulnerability Radar",
                title = "Afternoon Dip Shielded",
                message = "Around 2:30 PM, circadian body temperature drops, spiking compulsive phone pickups.",
                actionableTip = "Take a 5-minute Mind Puzzle break or water your Zen Bonsai instead of opening feeds."
            ),
            AIFocusInsight(
                id = "insight_night_curfew",
                emoji = "🌙",
                category = "Sleep Restoration",
                title = "Melatonin Protection Intact",
                message = "Reels Shield intercepted short-form doomscrolling past 10:30 PM, preserving REM sleep.",
                actionableTip = "Maintain Strict Focus Lockdown from 10:30 PM to wake refreshed tomorrow."
            )
        )
    }

    fun getWeeklyFocusVsDistraction(): List<DailyFocusComparison> {
        val todayFocus = (totalFocusMinutes / 60f).coerceAtLeast(0.5f)
        val todayDistraction = (savedScreenTimeMinutes / 60f * 0.4f).coerceAtLeast(0.3f)
        return listOf(
            DailyFocusComparison("Mon", 4.2f, 1.8f),
            DailyFocusComparison("Tue", 5.0f, 1.2f),
            DailyFocusComparison("Wed", 3.8f, 2.1f),
            DailyFocusComparison("Thu", 6.1f, 0.9f),
            DailyFocusComparison("Fri", 5.5f, 1.4f),
            DailyFocusComparison("Sat", 4.0f, 2.4f),
            DailyFocusComparison("Sun", todayFocus, todayDistraction)
        )
    }

    fun getCategoryDistribution(): List<CategoryShare> {
        val totalMins = (totalFocusMinutes + savedScreenTimeMinutes).coerceAtLeast(60)
        return listOf(
            CategoryShare("Deep Coding & Dev", 0.42f, "${(totalMins * 0.42f).toInt()}m", 0xFF6366F1, "💻"),
            CategoryShare("Learning & Research", 0.28f, "${(totalMins * 0.28f).toInt()}m", 0xFF10B981, "📚"),
            CategoryShare("System Architecture", 0.16f, "${(totalMins * 0.16f).toInt()}m", 0xFF3B82F6, "🏛️"),
            CategoryShare("Communication & Mail", 0.09f, "${(totalMins * 0.09f).toInt()}m", 0xFFF59E0B, "💬"),
            CategoryShare("Mindful Zen & Rest", 0.05f, "${(totalMins * 0.05f).toInt()}m", 0xFFA855F7, "🌿")
        )
    }

    fun get28DayFocusHeatmap(): List<HeatmapDay> {
        val pattern = listOf(
            3, 4, 2, 4, 3, 1, 0,
            2, 3, 4, 4, 3, 2, 1,
            4, 4, 3, 4, 4, 2, 0,
            3, 4, 4, 3, 4, 3, 4
        )
        return pattern.mapIndexed { index, level ->
            val day = index + 1
            val isToday = (index == pattern.lastIndex)
            val minutes = when (level) {
                4 -> 240 + (index * 7 % 60)
                3 -> 160 + (index * 5 % 40)
                2 -> 90 + (index * 3 % 30)
                1 -> 45
                else -> 0
            }
            HeatmapDay(dayOfMonth = day, intensityLevel = level, focusMinutes = minutes, isToday = isToday)
        }
    }

    fun getDoomscrollDefenseStats(): List<DoomscrollDefenseStat> {
        val blocked = reelsBlockedCount.coerceAtLeast(1)
        val insta = blocked + 12
        val yt = (blocked * 0.7f).toInt() + 8
        val snap = (blocked * 0.4f).toInt() + 5
        val xfeed = (blocked * 0.3f).toInt() + 3
        val total = (insta + yt + snap + xfeed).coerceAtLeast(1).toFloat()

        return listOf(
            DoomscrollDefenseStat("Instagram Reels", "📸", insta, insta / total, "${(insta * 3.5f).toInt()}m saved", 0xFFE1306C),
            DoomscrollDefenseStat("YouTube Shorts", "▶️", yt, yt / total, "${(yt * 3.0f).toInt()}m saved", 0xFFFF0000),
            DoomscrollDefenseStat("Snapchat Spotlight", "👻", snap, snap / total, "${(snap * 2.5f).toInt()}m saved", 0xFFFFFC00),
            DoomscrollDefenseStat("X / Twitter Feeds", "𝕏", xfeed, xfeed / total, "${(xfeed * 2.0f).toInt()}m saved", 0xFF1DA1F2)
        )
    }

    fun getDopamineTrendHistory(): List<DopamineTrendPoint> {
        return listOf(
            DopamineTrendPoint("Mon", 58),
            DopamineTrendPoint("Tue", 64),
            DopamineTrendPoint("Wed", 61),
            DopamineTrendPoint("Thu", 75),
            DopamineTrendPoint("Fri", 82),
            DopamineTrendPoint("Sat", 88),
            DopamineTrendPoint("Sun", 94)
        )
    }



    fun checkAndRefreshDeviceUsage() {
        val context = getApplication<Application>().applicationContext
        hasUsageAccessPermission = com.focusflow.app.util.PermissionHelper.hasUsageStatsPermission(context)
        deviceUsageStats = com.focusflow.app.util.DeviceUsageTracker.getRealDailyAppUsage(context)
    }

    fun grantEmergencyPass(appName: String = "Instagram Reels", durationSeconds: Int = 60) {
        emergencyPassApp = appName
        isEmergencyPassActive = true
        emergencyPassSecondsLeft = durationSeconds
        showEmergencyPassModal = false
        showMindfulPauseModal = false
        showFocusLockdownModal = false
        repository.setEmergencyPass(appName, durationSeconds)
        viewModelScope.launch {
            while (emergencyPassSecondsLeft > 0) {
                delay(1000L)
                emergencyPassSecondsLeft--
            }
            isEmergencyPassActive = false
            try {
                val context = getApplication<Application>().applicationContext
                com.focusflow.app.service.FloatingFocusIslandService.stop(context)
            } catch (_: Exception) {}
        }
        val targetPkg = if (appName.contains(".")) appName else focusLockdownPackageName
        if (targetPkg.isNotBlank()) {
            try {
                val context = getApplication<Application>().applicationContext
                val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPkg)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    context.startActivity(launchIntent)
                }
            } catch (_: Exception) {}
        }
        if (repository.isFloatingPillEnabled()) {
            try {
                val context = getApplication<Application>().applicationContext
                com.focusflow.app.service.FloatingFocusIslandService.start(context, appName.ifEmpty { "Pass" }, durationSeconds)
            } catch (_: Exception) {}
        }
    }

    fun launchAppByPackage(pkg: String) {
        try {
            val context = getApplication<Application>().applicationContext
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                context.startActivity(launchIntent)
            }
        } catch (_: Exception) {}
    }

    fun triggerFocusLockdownInterception(appName: String, packageName: String, isWorkMode: Boolean = false) {
        focusLockdownAppName = appName
        focusLockdownPackageName = packageName
        isWorkModeInterception = isWorkMode || repository.isWorkModeActive()
        showFocusLockdownModal = true
    }

    var workModeScheduleConfig by mutableStateOf(repository.getWorkModeScheduleConfig())
    var progressiveFrictionStatus by mutableStateOf(repository.getProgressiveFrictionStatus())
    var isFloatingPillEnabled by mutableStateOf(repository.isFloatingPillEnabled())

    fun toggleWorkMode(enabled: Boolean) {
        repository.setWorkModeEnabled(enabled)
        isWorkModeEnabled = enabled
        if (enabled) {
            repository.applyStrictWorkModePreset()
            productiveAppItems = repository.getProductiveAppItems()
        }
    }

    fun applyStrictWorkModePreset() {
        repository.applyStrictWorkModePreset()
        isWorkModeEnabled = true
        productiveAppItems = repository.getProductiveAppItems()
    }

    fun toggleWorkModeAutoSchedule(enabled: Boolean) {
        val updated = workModeScheduleConfig.copy(isAutoScheduleEnabled = enabled)
        repository.saveWorkModeScheduleConfig(updated)
        workModeScheduleConfig = updated
        isWorkModeEnabled = repository.isWorkModeActive()
    }

    fun updateWorkModeScheduleTimes(startH: Int, startM: Int, endH: Int, endM: Int) {
        val updated = workModeScheduleConfig.copy(
            startHour = startH,
            startMinute = startM,
            endHour = endH,
            endMinute = endM
        )
        repository.saveWorkModeScheduleConfig(updated)
        workModeScheduleConfig = updated
        isWorkModeEnabled = repository.isWorkModeActive()
    }

    fun toggleFloatingPill(enabled: Boolean) {
        repository.setFloatingPillEnabled(enabled)
        isFloatingPillEnabled = enabled
        if (!enabled) {
            try {
                val context = getApplication<Application>().applicationContext
                com.focusflow.app.service.FloatingFocusIslandService.stop(context)
            } catch (_: Exception) {}
        }
    }

    fun toggleProductiveApp(appId: String, enabled: Boolean) {
        repository.setProductiveAppEnabled(appId, enabled)
        productiveAppItems = repository.getProductiveAppItems()
    }

    // Notification Shield & Focus Digest Controls
    fun refreshBatchedNotifications() {
        batchedNotifications = repository.getBatchedNotifications()
    }

    fun clearBatchedNotifications() {
        repository.clearBatchedNotifications()
        batchedNotifications = emptyList()
    }

    fun toggleNotificationShield(enabled: Boolean) {
        repository.setNotificationShieldEnabled(enabled)
        isNotificationShieldEnabled = enabled
    }

    fun checkNotificationListenerPermission() {
        val context = getApplication<Application>().applicationContext
        hasNotificationListenerPermission = PermissionHelper.isNotificationListenerEnabled(context)
    }

    fun openNotificationListenerSettings() {
        val context = getApplication<Application>().applicationContext
        PermissionHelper.openNotificationListenerSettings(context)
    }

    fun addMockBatchedNotificationForPreview() {
        val sample = BatchedNotification(
            id = java.util.UUID.randomUUID().toString(),
            packageName = "com.instagram.android",
            appName = "Instagram",
            title = "alex_walker sent a Reel",
            text = "Check out this video clip before it gets deleted! 🔥",
            timestamp = System.currentTimeMillis() - 120000L,
            emoji = "📸",
            category = "Social"
        )
        repository.addBatchedNotification(sample)
        refreshBatchedNotifications()
    }

    // Gamified Coin Economy
    fun addCoins(amount: Int) {
        repository.addFocusCoins(amount)
        focusCoins = repository.getFocusCoins()
    }

    fun emergencyUnlockWithCoins(): Boolean {
        val success = repository.deductFocusCoins(50)
        if (success) {
            focusCoins = repository.getFocusCoins()
            isStrictLockActive = false
            pauseTimer()
        }
        return success
    }

    fun emergencyUnlockWithPin(pinInput: String): Boolean {
        if (repository.verifyLockPin(pinInput)) {
            isStrictLockActive = false
            pauseTimer()
            return true
        }
        return false
    }

    // Coding Challenges
    fun submitChallengeAnswer(challengeId: Int, selectedIndex: Int): Boolean {
        val challenge = challenges.find { it.id == challengeId } ?: return false
        val isCorrect = challenge.correctIndex == selectedIndex
        if (isCorrect) {
            repository.recordSolvedChallenge(challengeId)
            repository.addFocusCoins(challenge.coinReward)
            solvedChallengeIds = repository.getSolvedChallengeIds()
            focusCoins = repository.getFocusCoins()

            if (isStrictLockActive) {
                isStrictLockActive = false
                pauseTimer()
            }
        }
        return isCorrect
    }

    fun nextChallenge() {
        currentChallengeIndex = (currentChallengeIndex + 1) % challenges.size
    }

    // Task Management
    fun getFilteredTasks(): List<TaskItem> {
        return getFilteredAndSortedTasks()
    }

    fun getFilteredAndSortedTasks(): List<TaskItem> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val filtered = tasks.filter { task ->
            val matchesQuery = searchQuery.isBlank() || 
                task.title.contains(searchQuery, ignoreCase = true) || 
                task.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == "All" || task.category.equals(selectedCategoryFilter, ignoreCase = true)
            val taskDueStr = task.dueDateFormatted
            val matchesTab = when (selectedTaskTab) {
                "Inbox" -> task.category.equals("Inbox", ignoreCase = true) || task.category.isBlank()
                "Today" -> taskDueStr == todayStr || (!task.isCompleted && taskDueStr == null)
                "Upcoming" -> taskDueStr != null && taskDueStr > todayStr
                "Completed" -> task.isCompleted
                else -> true // "All"
            }
            matchesQuery && matchesCategory && matchesTab
        }

        return when (taskSortOrder) {
            "Priority" -> filtered.sortedByDescending { it.priority.ordinal }
            "DueDate" -> filtered.sortedWith(compareBy(nullsLast()) { it.dueDate })
            "Title" -> filtered.sortedBy { it.title.lowercase() }
            else -> filtered
        }
    }

    fun addTask(title: String, category: String = "Work", priority: TaskPriority = TaskPriority.MEDIUM) {
        saveOrUpdateTask(
            id = null,
            title = title,
            description = "",
            category = category,
            priority = priority,
            dueDate = null,
            linkedGoalId = null
        )
    }

    fun saveOrUpdateTask(
        id: String?,
        title: String,
        description: String,
        category: String,
        priority: TaskPriority,
        dueDate: String?,
        linkedGoalId: String?
    ) {
        if (title.isBlank()) return
        val current = tasks.toMutableList()
        val now = System.currentTimeMillis()
        val parsedDueDate = try {
            if (!dueDate.isNullOrBlank()) SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dueDate.trim())?.time else null
        } catch (_: Exception) { null }

        if (id != null) {
            val index = current.indexOfFirst { it.id == id }
            if (index != -1) {
                val existing = current[index]
                current[index] = existing.copy(
                    title = title.trim(),
                    description = description.trim(),
                    category = category.trim().ifEmpty { "General" },
                    priority = priority,
                    dueDate = parsedDueDate,
                    linkedGoalId = linkedGoalId?.trim()?.ifEmpty { null },
                    updatedAt = now
                )
            }
        } else {
            val newTask = TaskItem(
                id = "task_$now",
                title = title.trim(),
                description = description.trim(),
                category = category.trim().ifEmpty { "General" },
                priority = priority,
                dueDate = parsedDueDate,
                linkedGoalId = linkedGoalId?.trim()?.ifEmpty { null },
                createdAt = now,
                updatedAt = now
            )
            current.add(0, newTask)
        }
        tasks = current
        repository.saveTasks(current)
        recalculateGoalsProgress()
        showTaskEditorModal = false
        editingTaskItem = null
    }

    fun toggleTask(id: String) {
        val now = System.currentTimeMillis()
        val current = tasks.map {
            if (it.id == id) {
                val willBeCompleted = !it.isCompleted
                it.copy(
                    isCompleted = willBeCompleted,
                    completedAt = if (willBeCompleted) now else null,
                    updatedAt = now
                )
            } else it
        }
        tasks = current
        repository.saveTasks(current)
        recalculateGoalsProgress()
    }

    fun deleteTask(id: String) {
        val current = tasks.filter { it.id != id }
        tasks = current
        repository.saveTasks(current)
        recalculateGoalsProgress()
    }

    // --- GOAL MANAGEMENT ---
    fun recalculateGoalsProgress() {
        val totalMins = repository.getTotalFocusMinutes()
        val completedTaskCount = tasks.count { it.isCompleted }
        val maxHabitStreak = habits.maxOfOrNull { it.currentStreak } ?: streakDays
        
        val updatedGoals = goals.map { goal ->
            val progress = when (goal.type) {
                GoalType.FOCUS_TIME -> totalMins
                GoalType.TASKS_COMPLETED -> completedTaskCount
                GoalType.HABIT_STREAK -> maxHabitStreak
            }
            val isComplete = progress >= goal.targetValue
            goal.copy(
                currentValue = progress,
                isCompleted = isComplete,
                completedAt = if (isComplete && goal.completedAt == null) System.currentTimeMillis() else goal.completedAt
            )
        }
        goals = updatedGoals
        repository.saveGoals(updatedGoals)
    }

    fun addGoal(title: String, description: String, type: GoalType, targetValue: Int, deadline: String = "") {
        if (title.isBlank() || targetValue <= 0) return
        val parsedDeadline = try {
            if (deadline.isNotBlank()) SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(deadline.trim())?.time else null
        } catch (_: Exception) { null }

        val newGoal = GoalItem(
            id = "goal_${System.currentTimeMillis()}",
            title = title.trim(),
            description = description.trim(),
            type = type,
            targetValue = targetValue,
            currentValue = 0,
            deadlineTimestamp = parsedDeadline
        )
        val current = goals.toMutableList()
        current.add(0, newGoal)
        goals = current
        repository.saveGoals(current)
        recalculateGoalsProgress()
        showCreateGoalDialog = false
    }

    fun deleteGoal(id: String) {
        val current = goals.filter { it.id != id }
        goals = current
        repository.saveGoals(current)
    }

    // --- HABIT MANAGEMENT ---
    fun addHabit(title: String, description: String = "", targetDaysPerWeek: Int = 7) {
        if (title.isBlank()) return
        val daysList = (1..targetDaysPerWeek.coerceIn(1, 7)).toList()
        val newHabit = HabitItem(
            id = "habit_${System.currentTimeMillis()}",
            title = title.trim(),
            description = description.trim(),
            frequencySummary = if (targetDaysPerWeek >= 7) "Daily" else "${targetDaysPerWeek}x / Week",
            scheduledDays = daysList
        )
        val current = habits.toMutableList()
        current.add(0, newHabit)
        habits = current
        repository.saveHabits(current)
        showCreateHabitDialog = false
    }

    fun deleteHabit(id: String) {
        val current = habits.filter { it.id != id }
        habits = current
        repository.saveHabits(current)
        recalculateGoalsProgress()
    }

    fun toggleHabitToday(id: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val updatedHabits = habits.map { habit ->
            if (habit.id == id) {
                val dates = habit.completedDates.toMutableList()
                val isCompletedToday = dates.contains(todayStr)
                if (isCompletedToday) {
                    dates.remove(todayStr)
                } else {
                    dates.add(todayStr)
                }
                
                // Calculate streak: consecutive days ending today or yesterday
                val sorted = dates.distinct().sortedDescending()
                var streak = 0
                val cal = Calendar.getInstance()
                var checkDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                if (sorted.contains(checkDateStr)) {
                    streak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    checkDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                    while (sorted.contains(checkDateStr)) {
                        streak++
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        checkDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                    }
                }
                val best = maxOf(habit.bestStreak, streak)
                habit.copy(
                    completedDates = dates,
                    currentStreak = streak,
                    bestStreak = best,
                    totalCompletions = dates.size
                )
            } else habit
        }
        habits = updatedHabits
        repository.saveHabits(updatedHabits)
        recalculateGoalsProgress()
    }

    // --- FOCUS SESSION HISTORY ---
    fun deleteFocusSessionRecord(id: String) {
        val updated = focusSessionLogs.filter { it.id != id }
        focusSessionLogs = updated
        repository.saveFocusSessions(updated)
    }

    // --- GLOBAL SEARCH ---
    data class GlobalSearchResult(
        val type: String,
        val title: String,
        val subtitle: String,
        val icon: String
    )

    fun getGlobalSearchResults(): List<GlobalSearchResult> {
        val q = globalSearchQuery.trim()
        if (q.isBlank()) return emptyList()
        val results = mutableListOf<GlobalSearchResult>()
        
        tasks.filter { it.title.contains(q, ignoreCase = true) || it.description.contains(q, ignoreCase = true) }
            .forEach {
                results.add(GlobalSearchResult("Task", it.title, "${it.category} • Priority ${it.priority.name}", "✅"))
            }

        goals.filter { it.title.contains(q, ignoreCase = true) || it.description.contains(q, ignoreCase = true) }
            .forEach {
                results.add(GlobalSearchResult("Goal", it.title, "${it.currentValue}/${it.targetValue} (${it.type.displayName})", it.type.emoji))
            }

        habits.filter { it.title.contains(q, ignoreCase = true) || it.description.contains(q, ignoreCase = true) }
            .forEach {
                results.add(GlobalSearchResult("Habit", it.title, "Streak: ${it.currentStreak} days", it.emoji))
            }

        focusSessionLogs.filter { (it.associatedTaskTitle ?: "").contains(q, ignoreCase = true) || it.sessionType.contains(q, ignoreCase = true) }
            .forEach {
                results.add(GlobalSearchResult("Focus Log", "${it.plannedMinutes}m ${it.sessionType}", it.associatedTaskTitle ?: "General Focus", "⏱️"))
            }

        return results
    }

    // --- DATA BACKUP & RESET ---
    fun exportProductivityJson(context: Context) {
        try {
            val jsonString = repository.exportProductivityDataJson()
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, jsonString)
                putExtra(Intent.EXTRA_SUBJECT, "FocusFlow_Full_Backup_${System.currentTimeMillis()}.json")
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = Intent.createChooser(sendIntent, "Export Productivity Data")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Export error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun resetAllData(context: Context) {
        repository.resetAllLocalData()
        refreshStateFromRepository()
        android.widget.Toast.makeText(context, "All local productivity data reset to defaults", android.widget.Toast.LENGTH_SHORT).show()
    }

    fun refreshStreak() {
        streakDays = repository.checkInDailyStreak()
        longestStreakDays = repository.getLongestStreakDays()
        streakFreezes = repository.getStreakFreezes()
        activeDaysOfWeek = repository.getActiveDaysOfWeek()
    }

    fun updateUserGoal(goal: String) {
        userGoal = goal
        repository.setUserGoal(goal)
    }

    fun pinFocusShortcut(context: Context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val shortcutManager = context.getSystemService(android.content.pm.ShortcutManager::class.java)
                if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("START_FOCUS_SESSION", true)
                        putExtra("NAVIGATE_TAB", "Focus")
                    }
                    val pinShortcutInfo = android.content.pm.ShortcutInfo.Builder(context, "shortcut_pinned_focus")
                        .setShortLabel("⚡ Quick Focus")
                        .setLongLabel("Start 25m Focus Session")
                        .setIcon(android.graphics.drawable.Icon.createWithResource(context, R.mipmap.ic_launcher))
                        .setIntent(intent)
                        .build()
                    shortcutManager.requestPinShortcut(pinShortcutInfo, null)
                }
            }
        } catch (_: Exception) {}
    }

    fun pinFocusWidget(context: Context): Boolean {
        val success = FocusFlowWidgetProvider.pinWidgetToHomeScreen(context)
        FocusFlowWidgetProvider.updateAllWidgets(context)
        return success
    }

    // Onboarding
    fun completeOnboarding(
        goal: String,
        selectedApps: List<BlockedApp>,
        pin: String,
        focusMinutes: Int = 25,
        shortBreakMinutes: Int = 5,
        longBreakMinutes: Int = 15,
        dailyTargetMinutes: Int = 120
    ) {
        userGoal = goal
        lockPin = pin
        blockedApps = selectedApps

        setFocusDurationMinutes(focusMinutes)
        setShortBreakDurationMinutes(shortBreakMinutes)
        setLongBreakDurationMinutes(longBreakMinutes)
        setDailyFocusGoalMinutes(dailyTargetMinutes)

        repository.setUserGoal(goal)
        repository.setLockPin(pin)
        repository.saveBlockedApps(selectedApps)
        repository.setOnboardingCompleted(true)
        appStage = AppStage.MAIN
    }

    fun resetOnboardingForTesting() {
        repository.setOnboardingCompleted(false)
        appStage = AppStage.ONBOARDING
    }

    var puzzleSolvedCallback: (() -> Unit)? = null

    // Jigsaw Puzzle Break Challenge
    fun openJigsawPuzzle(reason: String = "Solve puzzle to unlock a 5-minute break", onSolved: (() -> Unit)? = null) {
        jigsawBreakReason = reason
        puzzleSolvedCallback = onSolved
        showJigsawPuzzleModal = true
    }

    fun closeJigsawPuzzle() {
        showJigsawPuzzleModal = false
        puzzleSolvedCallback = null
    }

    fun onJigsawPuzzleSolved() {
        showJigsawPuzzleModal = false
        showMindfulPauseModal = false
        showFocusLockdownModal = false
        showEmergencyPassModal = false
        isStrictLockActive = false
        repository.addFocusCoins(20)
        repository.incrementInterventionsAccepted()
        repository.addSavedScreenTime(5)
        repository.incrementDailyPuzzlePassCount()
        progressiveFrictionStatus = repository.getProgressiveFrictionStatus()
        val callback = puzzleSolvedCallback
        puzzleSolvedCallback = null
        if (callback != null) {
            callback.invoke()
        } else {
            selectMode(1) // 5-minute short break
            startTimer()
        }
        refreshStateFromRepository()
    }

    // --- EVERY APP TOGGLE & SHIELD RULES ---
    fun toggleAppBlockedState(packageName: String, isBlocked: Boolean) {
        repository.setAppBlockedState(packageName, isBlocked)
        allAppShieldRules = repository.getAllAppShieldRules()
        blockedApps = repository.getBlockedApps()
    }

    fun batchSetAppCategoryBlocked(category: String, isBlocked: Boolean) {
        repository.batchSetCategoryBlocked(category, isBlocked)
        allAppShieldRules = repository.getAllAppShieldRules()
        blockedApps = repository.getBlockedApps()
    }

    fun refreshAppShieldRules() {
        allAppShieldRules = repository.getAllAppShieldRules()
    }

    // --- WEBSITE DOMAIN SHIELD & BROWSER CONTROLS ---
    fun toggleWebsiteBlocked(domain: String, isBlocked: Boolean) {
        repository.toggleWebsiteBlocked(domain, isBlocked)
        blockedWebsites = repository.getBlockedWebsites()
    }

    fun isWebsiteBlocked(domain: String): Boolean {
        return repository.isWebsiteBlocked(domain)
    }

    fun addBlockedWebsite(domain: String) {
        repository.addBlockedWebsite(domain)
        blockedWebsites = repository.getBlockedWebsites()
    }

    fun removeBlockedWebsite(domain: String) {
        repository.removeBlockedWebsite(domain)
        blockedWebsites = repository.getBlockedWebsites()
    }

    fun unblockChromeBrowser() {
        repository.unblockBrowserPackage("com.android.chrome")
        allAppShieldRules = repository.getAllAppShieldRules()
        blockedApps = repository.getBlockedApps()
    }

    // --- SLEEP ARCHITECTURE & BEDTIME CURFEW ---
    fun toggleBedtimeCurfew(enabled: Boolean) {
        repository.toggleBedtimeCurfew(enabled)
        sleepTrackingState = repository.getSleepTrackingState()
    }

    fun updateBedtimeCurfewTimes(startH: Int, startM: Int, endH: Int, endM: Int) {
        repository.updateBedtimeCurfewTimes(startH, startM, endH, endM)
        sleepTrackingState = repository.getSleepTrackingState()
    }

    fun logSleepRecord(
        durationHours: Float,
        score: Int,
        bedtime: String,
        wakeTime: String,
        deepMins: Int,
        remMins: Int,
        lightMins: Int,
        awakeMins: Int
    ) {
        repository.logManualSleepRecord(
            durationHours = durationHours,
            score = score,
            bedtime = bedtime,
            wakeTime = wakeTime,
            deepMins = deepMins,
            remMins = remMins,
            lightMins = lightMins,
            awakeMins = awakeMins
        )
        sleepTrackingState = repository.getSleepTrackingState()
    }

    // --- ACHIEVEMENTS SYSTEM ---
    var showAchievementsScreen by mutableStateOf(false)
    var showSettingsScreen by mutableStateOf(false)
    var recentlyUnlockedAchievement by mutableStateOf<com.focusflow.app.model.Achievement?>(null)

    fun getAchievements(): List<com.focusflow.app.model.Achievement> {
        val unlockedIds = repository.getUnlockedAchievementIds()
        return com.focusflow.app.model.AchievementCatalog.allAchievements.map { ach ->
            val currentValue = getAchievementProgress(ach.id)
            ach.copy(
                currentValue = currentValue,
                isUnlocked = unlockedIds.contains(ach.id)
            )
        }
    }

    private fun getAchievementProgress(achievementId: String): Int {
        return when {
            achievementId.startsWith("ach_first_focus") || achievementId.startsWith("ach_focus_") ->
                completedSessions
            achievementId.startsWith("ach_streak_") ->
                longestStreakDays
            achievementId.startsWith("ach_block_") ->
                reelsBlockedCount + interventionsAccepted
            achievementId.startsWith("ach_save_") ->
                savedScreenTimeMinutes
            achievementId.startsWith("ach_puzzle_") ->
                solvedChallengeIds.size + 14 // Include prior puzzles solved
            achievementId.startsWith("ach_bonsai_") ->
                bonsaiStatus.totalTreesGrown
            achievementId.startsWith("ach_coins_") ->
                repository.getTotalCoinsEarned()
            achievementId.startsWith("ach_walkaway_") ->
                interventionsAccepted
            else -> 0
        }
    }

    fun checkAndUnlockAchievements() {
        val unlockedIds = repository.getUnlockedAchievementIds()
        val context = getApplication<Application>().applicationContext

        com.focusflow.app.model.AchievementCatalog.allAchievements.forEach { ach ->
            if (!unlockedIds.contains(ach.id)) {
                val progress = getAchievementProgress(ach.id)
                if (progress >= ach.targetValue) {
                    repository.unlockAchievement(ach.id)
                    repository.addFocusCoins(ach.coinReward)
                    repository.addXp(ach.coinReward / 2)
                    focusCoins = repository.getFocusCoins()
                    recentlyUnlockedAchievement = ach.copy(isUnlocked = true, currentValue = progress)

                    // Send notification if enabled
                    val notifPrefs = repository.getNotificationPreferences()
                    if (notifPrefs.achievementAlertsEnabled) {
                        com.focusflow.app.util.FocusNotificationHelper.showAchievementUnlocked(
                            context, ach.title, ach.emoji, ach.coinReward
                        )
                    }
                }
            }
        }
    }

    // --- USER LEVEL ---
    fun getUserLevel(): com.focusflow.app.model.UserLevel {
        val level = repository.getUserLevel()
        val xp = repository.getUserXp()
        val totalXp = repository.getUserTotalXp()
        val xpNeeded = repository.getXpForLevel(level)
        val title = repository.getLevelTitle(level)
        return com.focusflow.app.model.UserLevel(
            level = level,
            title = title,
            currentXp = xp,
            xpToNextLevel = xpNeeded,
            totalXp = totalXp
        )
    }

    fun addXp(amount: Int) {
        val leveledUp = repository.addXp(amount)
        if (leveledUp) {
            val newLevel = repository.getUserLevel()
            showBonsaiToastMessage = "🎉 Level Up! You're now Level $newLevel: ${repository.getLevelTitle(newLevel)}!"
        }
    }

    // --- DAILY & WEEKLY CHALLENGES ---
    fun getDailyChallenges(): List<com.focusflow.app.model.DailyChallenge> {
        return listOf(
            com.focusflow.app.model.DailyChallenge(
                id = "dc_focus_session",
                title = "Focus Sprinter",
                description = "Complete 2 focus sessions today",
                emoji = "🎯",
                targetValue = 2,
                currentValue = completedSessions.coerceAtMost(2),
                xpReward = 50,
                coinReward = 25,
                isCompleted = completedSessions >= 2
            ),
            com.focusflow.app.model.DailyChallenge(
                id = "dc_block_distraction",
                title = "Shield Activation",
                description = "Block 3 distraction attempts",
                emoji = "🛡️",
                targetValue = 3,
                currentValue = (reelsBlockedCount % 10).coerceAtMost(3),
                xpReward = 30,
                coinReward = 15,
                isCompleted = reelsBlockedCount >= 3
            ),
            com.focusflow.app.model.DailyChallenge(
                id = "dc_water_bonsai",
                title = "Zen Gardener",
                description = "Water your Bonsai once",
                emoji = "💧",
                targetValue = 1,
                currentValue = if (bonsaiStatus.waterPoints > 30) 1 else 0,
                xpReward = 20,
                coinReward = 10,
                isCompleted = bonsaiStatus.waterPoints > 30
            )
        )
    }

    fun getWeeklyChallenges(): List<com.focusflow.app.model.WeeklyChallenge> {
        return listOf(
            com.focusflow.app.model.WeeklyChallenge(
                id = "wc_focus_hours",
                title = "Deep Work Week",
                description = "Accumulate 5 hours of focus time",
                emoji = "⏱️",
                targetValue = 300,
                currentValue = totalFocusMinutes.coerceAtMost(300),
                xpReward = 200,
                coinReward = 100,
                isCompleted = totalFocusMinutes >= 300
            ),
            com.focusflow.app.model.WeeklyChallenge(
                id = "wc_streak_week",
                title = "Consistency King",
                description = "Maintain a 7-day streak",
                emoji = "🔥",
                targetValue = 7,
                currentValue = streakDays.coerceAtMost(7),
                xpReward = 250,
                coinReward = 150,
                isCompleted = streakDays >= 7
            ),
            com.focusflow.app.model.WeeklyChallenge(
                id = "wc_save_time",
                title = "Time Reclaimer",
                description = "Save 2 hours of screen time",
                emoji = "⏰",
                targetValue = 120,
                currentValue = savedScreenTimeMinutes.coerceAtMost(120),
                xpReward = 150,
                coinReward = 75,
                isCompleted = savedScreenTimeMinutes >= 120
            )
        )
    }

    // --- NOTIFICATION PREFERENCES ---
    fun getNotificationPreferences(): com.focusflow.app.model.NotificationPreferences {
        return repository.getNotificationPreferences()
    }

    fun updateNotificationPref(key: String, value: Boolean) {
        repository.updateNotificationPref(key, value)

        val context = getApplication<Application>().applicationContext
        val prefs = repository.getNotificationPreferences()

        // Schedule or cancel alarms based on preference changes
        when (key) {
            "focus_reminders" -> {
                if (value) {
                    com.focusflow.app.util.FocusNotificationHelper.scheduleDailyFocusReminder(
                        context, prefs.focusReminderHour, prefs.focusReminderMinute
                    )
                }
            }
            "daily_digest" -> {
                if (value) {
                    com.focusflow.app.util.FocusNotificationHelper.scheduleDailyDigest(
                        context, prefs.dailyDigestHour, prefs.dailyDigestMinute
                    )
                }
            }
        }
    }

    // --- SETTINGS ---
    fun applyTheme(themeId: String) {
        repository.equipStoreItem(themeId)
        activeThemeId = themeId
        com.focusflow.app.ui.theme.applyThemeById(themeId)
    }

    fun getDailyFocusGoal(): Int = repository.getDailyFocusGoal()

    fun setDailyFocusGoal(minutes: Int) {
        repository.setDailyFocusGoal(minutes)
    }

    fun getHapticEnabled(): Boolean = repository.getHapticEnabled()
    fun setHapticEnabled(enabled: Boolean) { repository.setHapticEnabled(enabled) }

    fun getSoundEnabled(): Boolean = repository.getSoundEnabled()
    fun setSoundEnabled(enabled: Boolean) { repository.setSoundEnabled(enabled) }

    fun toggleStrictMode(enabled: Boolean) {
        isStrictModeEnabled = enabled
        repository.setStrictModeEnabled(enabled)
    }

    fun setShortBreakMinutes(minutes: Int) {
        customShortBreakMinutes = minutes
        repository.setCustomBreakMinutes(minutes)
        if (currentModeIndex == 1 && !isTimerRunning) {
            timerRemainingSeconds = minutes * 60
        }
    }

    fun setLongBreakMinutes(minutes: Int) {
        customLongBreakMinutes = minutes
        repository.setCustomLongBreakMinutes(minutes)
        if (currentModeIndex == 2 && !isTimerRunning) {
            timerRemainingSeconds = minutes * 60
        }
    }

    fun updatePin(newPin: String) {
        lockPin = newPin
        repository.setLockPin(newPin)
    }

    fun initializeNotifications() {
        val context = getApplication<Application>().applicationContext
        com.focusflow.app.util.FocusNotificationHelper.createNotificationChannels(context)

        val prefs = repository.getNotificationPreferences()
        if (prefs.focusRemindersEnabled) {
            com.focusflow.app.util.FocusNotificationHelper.scheduleDailyFocusReminder(
                context, prefs.focusReminderHour, prefs.focusReminderMinute
            )
        }
        if (prefs.dailyDigestEnabled) {
            com.focusflow.app.util.FocusNotificationHelper.scheduleDailyDigest(
                context, prefs.dailyDigestHour, prefs.dailyDigestMinute
            )
        }
    }

    fun sendTestNotification(context: Context) {
        com.focusflow.app.util.FocusNotificationHelper.createNotificationChannels(context)
        com.focusflow.app.util.FocusNotificationHelper.showFocusReminder(context)
    }

    fun exportFocusData(context: Context) {
        try {
            val root = JSONObject().apply {
                put("app", "FocusFlow")
                put("version", "2.1.0")
                put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

                put("stats", JSONObject().apply {
                    put("totalFocusMinutes", totalFocusMinutes)
                    put("completedSessions", completedSessions)
                    put("streakDays", streakDays)
                    put("longestStreakDays", longestStreakDays)
                    put("savedScreenTimeMinutes", savedScreenTimeMinutes)
                    put("reelsBlockedCount", reelsBlockedCount)
                    put("focusCoins", focusCoins)
                    put("dopamineScore", dopamineScore)
                })

                val userLevel = getUserLevel()
                put("userLevel", JSONObject().apply {
                    put("level", userLevel.level)
                    put("title", userLevel.title)
                    put("currentXp", userLevel.currentXp)
                    put("totalXp", userLevel.totalXp)
                })

                put("sleep", JSONObject().apply {
                    put("lastNightScore", sleepTrackingState.lastNightScore)
                    put("lastNightDuration", sleepTrackingState.lastNightDuration)
                    put("lastBedtime", sleepTrackingState.lastBedtime)
                    put("lastWakeTime", sleepTrackingState.lastWakeTime)
                    put("bedtimeCurfewEnabled", sleepTrackingState.bedtimeCurfewEnabled)
                })
            }

            val jsonString = root.toString(2)
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, jsonString)
                putExtra(Intent.EXTRA_SUBJECT, "FocusFlow_Backup_${System.currentTimeMillis()}.json")
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = Intent.createChooser(sendIntent, "Export Focus Data")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Export error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        FocusSoundManager.stop()
    }
}