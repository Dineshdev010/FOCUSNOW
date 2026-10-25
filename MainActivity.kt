package com.focusflow.app

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.focusflow.app.model.AppStage
import com.focusflow.app.ui.components.NavFocusIcon
import com.focusflow.app.ui.components.NavHomeIcon
import com.focusflow.app.ui.components.NavProgressIcon
import com.focusflow.app.ui.components.NavTasksIcon
import com.focusflow.app.ui.components.NavHabitsIcon
import com.focusflow.app.ui.components.AtmosphericZenBackground
import com.focusflow.app.ui.components.AntiTamperAlertDialog
import com.focusflow.app.ui.screens.AchievementsScreen
import com.focusflow.app.ui.screens.AddScheduleDialog
import com.focusflow.app.ui.screens.AlarmRingingDialog
import com.focusflow.app.ui.screens.AppLimitsAndSchedulesScreen
import com.focusflow.app.ui.screens.CodeArenaScreen
import com.focusflow.app.ui.screens.EmergencyPassDialog
import com.focusflow.app.ui.screens.FocusDigestDialog
import com.focusflow.app.ui.screens.FocusLockdownDialog
import com.focusflow.app.ui.screens.FocusScreen
import com.focusflow.app.ui.screens.HomeScreen
import com.focusflow.app.ui.screens.MindfulPauseDialog
import com.focusflow.app.ui.screens.JigsawPuzzleDialog
import com.focusflow.app.ui.screens.OnboardingScreen
import com.focusflow.app.ui.screens.ProductiveWhitelistDialog
import com.focusflow.app.ui.screens.ProfileScreen
import com.focusflow.app.ui.screens.ProgressScreen
import com.focusflow.app.ui.screens.ReelsBlockerDialog
import com.focusflow.app.ui.screens.RewardsStoreDialog
import com.focusflow.app.ui.screens.SettingsScreen
import com.focusflow.app.ui.screens.AppBlockerRulesDialog
import com.focusflow.app.ui.screens.SleepTrackerDialog
import com.focusflow.app.ui.screens.SplashScreen
import com.focusflow.app.ui.screens.StrictLockScreen
import com.focusflow.app.ui.screens.GlobalSearchDialog
import com.focusflow.app.ui.screens.HabitsAndGoalsScreen
import com.focusflow.app.ui.screens.TaskEditorDialog
import com.focusflow.app.ui.screens.TasksScreen
import com.focusflow.app.ui.theme.FocusAccent
import com.focusflow.app.ui.theme.FocusBackground
import com.focusflow.app.ui.theme.FocusBorder
import com.focusflow.app.ui.theme.FocusFlowTheme
import com.focusflow.app.ui.theme.FocusPanel
import com.focusflow.app.ui.theme.FocusSecondary
import com.focusflow.app.viewmodel.FocusFlowViewModel
import com.focusflow.app.widget.FocusFlowWidgetProvider

class MainActivity : ComponentActivity() {
    private val viewModel by lazy {
        ViewModelProvider(this)[FocusFlowViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setupDynamicShortcuts()
        handleInterceptionIntent(intent)
        viewModel.initializeNotifications()

        setContent {
            FocusFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FocusBackground
                ) {
                    Crossfade(targetState = viewModel.appStage, label = "stageCrossfade") { stage ->
                        when (stage) {
                            AppStage.SPLASH -> SplashScreen(viewModel)
                            AppStage.ONBOARDING -> OnboardingScreen(viewModel)
                            AppStage.MAIN -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    FocusFlowMainApp(viewModel)

                                    if (viewModel.isStrictLockActive) {
                                        StrictLockScreen(viewModel)
                                    }

                                        // Overlay Dialogs
                                        if (viewModel.showCodeChallengeModal) {
                                            CodeArenaScreen(
                                                viewModel = viewModel,
                                                onClose = { viewModel.showCodeChallengeModal = false }
                                            )
                                        }

                                        if (viewModel.showMindfulPauseModal) {
                                            MindfulPauseDialog(
                                                viewModel = viewModel,
                                                appName = viewModel.mindfulPauseAppName,
                                                packageName = viewModel.mindfulPausePackageName,
                                                isTimesUpMode = false,
                                                onClose = { viewModel.showMindfulPauseModal = false }
                                            )
                                        }

                                        if (viewModel.showRegainTimesUpModal) {
                                            MindfulPauseDialog(
                                                viewModel = viewModel,
                                                appName = viewModel.regainTimesUpAppName,
                                                packageName = viewModel.regainTimesUpPackageName,
                                                isTimesUpMode = true,
                                                onClose = { viewModel.showRegainTimesUpModal = false }
                                            )
                                        }

                                        if (viewModel.showAntiTamperAlertModal) {
                                            AntiTamperAlertDialog(
                                                onClose = { viewModel.showAntiTamperAlertModal = false }
                                            )
                                        }

                                        if (viewModel.showReelSwipeInterventionModal) {
                                            MindfulPauseDialog(
                                                viewModel = viewModel,
                                                appName = "${viewModel.reelSwipeInterventionAppName} (${viewModel.reelSwipeInterventionCount} videos)",
                                                packageName = "",
                                                isTimesUpMode = false,
                                                onClose = { viewModel.showReelSwipeInterventionModal = false }
                                            )
                                        }

                                        if (viewModel.showReelsBlockerSheet) {
                                            ReelsBlockerDialog(
                                                viewModel = viewModel,
                                                onClose = { viewModel.showReelsBlockerSheet = false }
                                            )
                                        }

                                        if (viewModel.showAchievementsScreen) {
                                            AchievementsScreen(
                                                viewModel = viewModel,
                                                onClose = { viewModel.showAchievementsScreen = false }
                                            )
                                        }

                                        if (viewModel.showProfileScreen) {
                                            ProfileScreen(
                                                viewModel = viewModel,
                                                onClose = { viewModel.showProfileScreen = false }
                                            )
                                        }

                                        if (viewModel.showAppLimitsAndSchedulesModal) {
                                            AppLimitsAndSchedulesScreen(
                                                viewModel = viewModel,
                                                onClose = { viewModel.showAppLimitsAndSchedulesModal = false }
                                            )
                                        }

                                        if (viewModel.showSettingsScreen) {
                                            SettingsScreen(
                                                viewModel = viewModel,
                                                onClose = { viewModel.showSettingsScreen = false }
                                            )
                                        }

                                        if (viewModel.showAddScheduleModal) {
                                            AddScheduleDialog(
                                                viewModel = viewModel,
                                                onClose = {
                                                    viewModel.showAddScheduleModal = false
                                                    viewModel.editingScheduleItem = null
                                                }
                                            )
                                        }

                                        if (viewModel.showRewardsStoreModal) {
                                            RewardsStoreDialog(
                                                viewModel = viewModel,
                                                onClose = { viewModel.showRewardsStoreModal = false }
                                            )
                                        }

                                        if (viewModel.showEmergencyPassModal) {
                                            EmergencyPassDialog(
                                                viewModel = viewModel,
                                                appName = viewModel.emergencyPassApp,
                                                onPassGranted = {
                                                    viewModel.showEmergencyPassModal = false
                                                },
                                                onClose = { viewModel.showEmergencyPassModal = false }
                                            )
                                        }

                                        if (viewModel.showFocusLockdownModal) {
                                            FocusLockdownDialog(
                                                viewModel = viewModel,
                                                appName = viewModel.focusLockdownAppName,
                                                onReturnToFocus = {
                                                    viewModel.showFocusLockdownModal = false
                                                    viewModel.selectedTab = "Focus"
                                                },
                                                onOpenEmergencyPass = {
                                                    viewModel.showFocusLockdownModal = false
                                                    viewModel.emergencyPassApp = viewModel.focusLockdownAppName
                                                    viewModel.showEmergencyPassModal = true
                                                },
                                                onOpenWhitelist = {
                                                    viewModel.showProductiveWhitelistModal = true
                                                },
                                                onGiveUpSession = {
                                                    viewModel.showFocusLockdownModal = false
                                                    viewModel.pauseTimer()
                                                }
                                            )
                                        }

                                        if (viewModel.showProductiveWhitelistModal) {
                                            ProductiveWhitelistDialog(
                                                viewModel = viewModel,
                                                onClose = { viewModel.showProductiveWhitelistModal = false }
                                            )
                                        }

                                        if (viewModel.showAlwaysAllowedModal) {
                                            com.focusflow.app.ui.screens.AlwaysAllowedAppsDialog(
                                                viewModel = viewModel,
                                                onClose = { viewModel.showAlwaysAllowedModal = false }
                                            )
                                        }

                                        if (viewModel.showFocusDigestModal) {
                                            FocusDigestDialog(
                                                viewModel = viewModel,
                                                onClose = { viewModel.showFocusDigestModal = false }
                                            )
                                        }

                                        if (viewModel.showGoalModal) {
                                            com.focusflow.app.ui.components.GoalSelectorDialog(
                                                currentGoal = viewModel.userGoal,
                                                onGoalSelected = { viewModel.updateUserGoal(it) },
                                                onDismiss = { viewModel.showGoalModal = false }
                                            )
                                        }

                                        if (viewModel.showAppBlockerRulesModal) {
                                            AppBlockerRulesDialog(
                                                viewModel = viewModel,
                                                onClose = { viewModel.showAppBlockerRulesModal = false }
                                            )
                                        }

                                        if (viewModel.showSleepTrackerModal) {
                                            SleepTrackerDialog(
                                                viewModel = viewModel,
                                                onClose = { viewModel.showSleepTrackerModal = false }
                                            )
                                        }

                                        if (viewModel.showTaskEditorModal) {
                                            TaskEditorDialog(
                                                task = viewModel.editingTaskItem,
                                                viewModel = viewModel,
                                                onDismiss = {
                                                    viewModel.showTaskEditorModal = false
                                                    viewModel.editingTaskItem = null
                                                }
                                            )
                                        }

                                        if (viewModel.showGlobalSearchModal) {
                                            GlobalSearchDialog(
                                                viewModel = viewModel,
                                                onDismiss = { viewModel.showGlobalSearchModal = false }
                                            )
                                        }

                                        // Mind Puzzle Arena (Highest priority overlay - renders above all lockdowns and pass dialogs)
                                        if (viewModel.showJigsawPuzzleModal) {
                                            JigsawPuzzleDialog(
                                                viewModel = viewModel,
                                                onClose = { viewModel.closeJigsawPuzzle() }
                                            )
                                        }

                                        // Full-Screen Focus & Routine Alarm Ringing Alert
                                        if (viewModel.isAlarmRinging) {
                                            AlarmRingingDialog(
                                                viewModel = viewModel,
                                                onDismiss = { viewModel.dismissRingingAlarm() }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleInterceptionIntent(intent)
    }

    private fun setupDynamicShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            try {
                val shortcutManager = getSystemService(ShortcutManager::class.java) ?: return
                
                val shortcutFocus = ShortcutInfo.Builder(this, "dyn_deep_focus")
                    .setShortLabel(getString(R.string.shortcut_focus_short))
                    .setLongLabel(getString(R.string.shortcut_focus_long))
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                    .setIntent(Intent(this, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("START_FOCUS_SESSION", true)
                        putExtra("NAVIGATE_TAB", "Focus")
                    })
                    .build()

                val shortcutPuzzles = ShortcutInfo.Builder(this, "dyn_puzzles")
                    .setShortLabel(getString(R.string.shortcut_puzzles_short))
                    .setLongLabel(getString(R.string.shortcut_puzzles_long))
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                    .setIntent(Intent(this, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("OPEN_PUZZLES", true)
                    })
                    .build()

                val shortcutShield = ShortcutInfo.Builder(this, "dyn_reels_shield")
                    .setShortLabel(getString(R.string.shortcut_shield_short))
                    .setLongLabel(getString(R.string.shortcut_shield_long))
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                    .setIntent(Intent(this, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("OPEN_REELS_SHIELD", true)
                    })
                    .build()

                val shortcutPause = ShortcutInfo.Builder(this, "dyn_mindful_pause")
                    .setShortLabel(getString(R.string.shortcut_pause_short))
                    .setLongLabel(getString(R.string.shortcut_pause_long))
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                    .setIntent(Intent(this, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("TRIGGER_MINDFUL_PAUSE", true)
                    })
                    .build()

                shortcutManager.dynamicShortcuts = listOf(shortcutFocus, shortcutPuzzles, shortcutShield, shortcutPause)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun Intent.hasExtraFlag(key: String): Boolean {
        return try {
            if (getBooleanExtra(key, false)) return true
            val str = getStringExtra(key)
            str != null && (str.equals("true", ignoreCase = true) || str == "1")
        } catch (_: Exception) {
            getStringExtra(key).equals("true", ignoreCase = true)
        }
    }

    private fun handleInterceptionIntent(intent: Intent?) {
        if (intent == null) return
        val triggerAlarm = intent.hasExtraFlag("TRIGGER_ALARM_RINGING")
        if (triggerAlarm) {
            viewModel.appStage = AppStage.MAIN
            viewModel.isAlarmRinging = true
            val title = intent.getStringExtra("ALARM_TITLE") ?: "⏰ Focus Complete!"
            val sub = intent.getStringExtra("ALARM_SUBTITLE") ?: "Your focus session is done."
            val sound = intent.getStringExtra("ALARM_SOUND") ?: viewModel.timerAlarmSound
            viewModel.alarmRingingTitle = title
            viewModel.alarmRingingSubtitle = sub
            viewModel.alarmRingingSound = sound
        }
        val navTab = intent.getStringExtra("NAVIGATE_TAB")
        if (navTab != null) {
            viewModel.appStage = AppStage.MAIN
            viewModel.selectedTab = navTab
        }
        val triggerLockdown = intent.hasExtraFlag("TRIGGER_FOCUS_LOCKDOWN")
        if (triggerLockdown) {
            viewModel.appStage = AppStage.MAIN
            val appName = intent.getStringExtra("LOCKED_APP_NAME") ?: "Distracting App"
            val pkgName = intent.getStringExtra("LOCKED_PACKAGE_NAME") ?: ""
            val isWorkMode = intent.getBooleanExtra("IS_WORK_MODE", false)
            viewModel.triggerFocusLockdownInterception(appName, pkgName, isWorkMode)
        }
        val triggerRegainTimesUp = intent.hasExtraFlag("TRIGGER_REGAIN_TIMES_UP")
        if (triggerRegainTimesUp) {
            viewModel.appStage = AppStage.MAIN
            val appName = intent.getStringExtra("TIMES_UP_APP_NAME")
                ?: intent.getStringExtra("LOCKED_APP_NAME")
                ?: "App"
            val pkgName = intent.getStringExtra("TIMES_UP_PACKAGE_NAME") ?: ""
            viewModel.triggerRegainTimesUp(appName, pkgName)
        }
        val triggerAntiTamper = intent.hasExtraFlag("TRIGGER_ANTI_TAMPER_ALERT")
        if (triggerAntiTamper) {
            viewModel.appStage = AppStage.MAIN
            viewModel.showAntiTamperAlertModal = true
        }
        val triggerReelSwipe = intent.hasExtraFlag("TRIGGER_REEL_SWIPE_INTERVENTION")
        if (triggerReelSwipe) {
            viewModel.appStage = AppStage.MAIN
            val appName = intent.getStringExtra("REEL_SWIPE_APP_NAME") ?: "Reels & Shorts"
            val count = intent.getIntExtra("REEL_SWIPE_COUNT", 10)
            viewModel.triggerReelSwipeIntervention(appName, count)
        }
        val triggerPause = intent.hasExtraFlag("TRIGGER_MINDFUL_PAUSE")
        if (triggerPause) {
            viewModel.appStage = AppStage.MAIN
            val appName = intent.getStringExtra("BLOCKED_APP_NAME") ?: "Mindful Pause"
            val pkgName = intent.getStringExtra("BLOCKED_PACKAGE_NAME") ?: ""
            viewModel.testMindfulIntervention(appName, pkgName)
        }
        val startFocus = intent.hasExtraFlag("START_FOCUS_SESSION")
        if (startFocus) {
            viewModel.appStage = AppStage.MAIN
            viewModel.selectedTab = "Focus"
            if (!viewModel.isTimerRunning) {
                viewModel.startTimer()
            }
        }
        val openPuzzles = intent.hasExtraFlag("OPEN_PUZZLES")
        if (openPuzzles) {
            viewModel.appStage = AppStage.MAIN
            viewModel.showJigsawModal = true
        }
        val openShield = intent.hasExtraFlag("OPEN_REELS_SHIELD")
        if (openShield) {
            viewModel.appStage = AppStage.MAIN
            viewModel.showReelsBlockerSheet = true
        }

        // Quick timer notification actions
        if (intent.action == "com.focusflow.ACTION_PAUSE_RESUME_TIMER") {
            viewModel.appStage = AppStage.MAIN
            viewModel.selectedTab = "Focus"
            if (viewModel.isTimerRunning) {
                viewModel.pauseTimer()
            } else {
                viewModel.startTimer()
            }
        }
        if (intent.action == "com.focusflow.ACTION_ADD_FIVE_MINS") {
            viewModel.appStage = AppStage.MAIN
            viewModel.selectedTab = "Focus"
            viewModel.adjustTimerMinutes(5)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkAndRefreshDeviceUsage()
        FocusFlowWidgetProvider.updateAllWidgets(this)
        if (viewModel.isTimerRunning && viewModel.isFloatingPillEnabled && com.focusflow.app.util.PermissionHelper.hasOverlayPermission(this)) {
            val modeTitle = viewModel.modes.getOrNull(viewModel.currentModeIndex)?.title ?: "Focus"
            com.focusflow.app.service.FloatingFocusIslandService.start(this, modeTitle, viewModel.timerRemainingSeconds)
        }
    }
}

@Composable
fun FocusFlowMainApp(viewModel: FocusFlowViewModel) {
    val haptic = LocalHapticFeedback.current

    AtmosphericZenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            if (viewModel.isEmergencyPassActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color(0xFF261907))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Emergency Pass Active: ${viewModel.emergencyPassApp}",
                            color = androidx.compose.ui.graphics.Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${viewModel.emergencyPassSecondsLeft}s left",
                            color = androidx.compose.ui.graphics.Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Active Screen Content (floats seamlessly over animated ambient background)
            Box(modifier = Modifier.weight(1f)) {
                when (viewModel.selectedTab) {
                    "Home" -> HomeScreen(viewModel)
                    "Focus" -> FocusScreen(viewModel)
                    "Tasks" -> TasksScreen(viewModel)
                    "Habits & Goals", "Habits" -> HabitsAndGoalsScreen(viewModel)
                    "Progress" -> ProgressScreen(viewModel)
                    else -> HomeScreen(viewModel)
                }
            }

            // Floating Frosted Glass Dock with Active Glow Indicators
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 14.dp, end = 14.dp, bottom = 8.dp, top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(FocusPanel.copy(alpha = 0.90f))
                        .border(
                            width = 1.dp,
                            color = FocusBorder.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            "Home" to "Home",
                            "Focus" to "Focus",
                            "Tasks" to "Tasks",
                            "Habits & Goals" to "Habits",
                            "Progress" to "Progress"
                        )

                        tabs.forEach { (tabKey, displayLabel) ->
                            val isSelected = viewModel.selectedTab == tabKey || (tabKey == "Habits & Goals" && viewModel.selectedTab == "Habits")
                            val itemColor by animateColorAsState(
                                targetValue = if (isSelected) FocusAccent else FocusSecondary,
                                label = "tabColor"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isSelected) FocusAccent.copy(alpha = 0.14f) else androidx.compose.ui.graphics.Color.Transparent)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.selectedTab = tabKey
                                    }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    when (tabKey) {
                                        "Home" -> NavHomeIcon(tint = itemColor)
                                        "Focus" -> NavFocusIcon(tint = itemColor)
                                        "Tasks" -> NavTasksIcon(tint = itemColor)
                                        "Habits & Goals" -> NavHabitsIcon(tint = itemColor)
                                        "Progress" -> NavProgressIcon(tint = itemColor)
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = displayLabel,
                                        color = itemColor,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )

                                    if (isSelected) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(width = 12.dp, height = 2.dp)
                                                .clip(RoundedCornerShape(1.dp))
                                                .background(FocusAccent)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}