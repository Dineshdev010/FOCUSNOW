package com.focusflow.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun SettingsScreen(viewModel: FocusFlowViewModel, onClose: () -> Unit = {}) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    BackHandler {
        onClose()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Sticky Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⚙️ Settings",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusWhite,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = "Configure your focus experience",
                    fontSize = 13.sp,
                    color = FocusMuted
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(FocusPanelLight)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClose()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("✕ Close", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FocusSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

        // --- PROFILE & LEVEL CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FocusPanel)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(FocusAccent.copy(alpha = 0.12f), FocusPanel)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Level Avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(FocusAccent.copy(alpha = 0.18f))
                            .border(2.dp, FocusAccent.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.getUserLevel().level.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FocusAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.getUserLevel().title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusWhite
                        )
                        Text(
                            text = "Level ${viewModel.getUserLevel().level} • ${viewModel.getUserLevel().totalXp} XP total",
                            fontSize = 12.sp,
                            color = FocusMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // XP Progress Bar
                        val xpProgress by animateFloatAsState(
                            targetValue = viewModel.getUserLevel().progressPercent,
                            animationSpec = tween(600),
                            label = "xpAnim"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(FocusPanelBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(xpProgress)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(FocusAccent, FocusAccentDark)
                                        )
                                    )
                            )
                        }
                        Text(
                            text = "${viewModel.getUserLevel().currentXp} / ${viewModel.getUserLevel().xpToNextLevel} XP to next level",
                            fontSize = 10.sp,
                            color = FocusMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- THEME SELECTION ---
        SettingsSectionHeader(title = "🎨 App Theme", subtitle = "Choose your visual aesthetic")
        Spacer(modifier = Modifier.height(8.dp))
        
        val themes = listOf(
            Triple("theme_lime", "Lime Zen", Color(0xFF9BE564)),
            Triple("theme_midnight", "Glacier Frost", Color(0xFF38BDF8)),
            Triple("theme_cyberpunk", "Cyberpunk Neon", Color(0xFFEC4899)),
            Triple("theme_sunset", "Solar Amber", Color(0xFFF59E0B))
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            themes.forEach { (id, name, color) ->
                val isSelected = viewModel.activeThemeId == id
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) color else FocusPanelBorder,
                    label = "themeBorder"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.applyTheme(id)
                        }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(color.copy(alpha = if (isSelected) 0.25f else 0.1f))
                            .border(2.dp, borderColor, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = name,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) color else FocusMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- FOCUS TIMER SETTINGS ---
        SettingsSectionHeader(title = "⏱️ Timer Defaults", subtitle = "Configure your Pomodoro durations")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsNumberRow(
                label = "Focus Duration",
                emoji = "🎯",
                value = viewModel.customFocusMinutes,
                unit = "min",
                onIncrease = { viewModel.adjustTimerMinutes(5) },
                onDecrease = { viewModel.adjustTimerMinutes(-5) }
            )
            SettingsDivider()
            SettingsNumberRow(
                label = "Short Break",
                emoji = "☕",
                value = viewModel.customShortBreakMinutes,
                unit = "min",
                onIncrease = {
                    val newVal = (viewModel.customShortBreakMinutes + 1).coerceAtMost(30)
                    viewModel.setShortBreakMinutes(newVal)
                },
                onDecrease = {
                    val newVal = (viewModel.customShortBreakMinutes - 1).coerceAtLeast(1)
                    viewModel.setShortBreakMinutes(newVal)
                }
            )
            SettingsDivider()
            SettingsNumberRow(
                label = "Long Break",
                emoji = "🧘",
                value = viewModel.customLongBreakMinutes,
                unit = "min",
                onIncrease = {
                    val newVal = (viewModel.customLongBreakMinutes + 5).coerceAtMost(60)
                    viewModel.setLongBreakMinutes(newVal)
                },
                onDecrease = {
                    val newVal = (viewModel.customLongBreakMinutes - 5).coerceAtLeast(5)
                    viewModel.setLongBreakMinutes(newVal)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- DAILY FOCUS GOAL ---
        SettingsSectionHeader(title = "🏁 Daily Focus Goal", subtitle = "Set your daily deep focus target")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsNumberRow(
                label = "Daily Goal",
                emoji = "⏳",
                value = viewModel.getDailyFocusGoal(),
                unit = "min",
                onIncrease = { viewModel.setDailyFocusGoal(viewModel.getDailyFocusGoal() + 15) },
                onDecrease = { viewModel.setDailyFocusGoal(viewModel.getDailyFocusGoal() - 15) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- NOTIFICATION PREFERENCES ---
        SettingsSectionHeader(title = "🔔 Notifications", subtitle = "Control what FocusNow sends you")
        Spacer(modifier = Modifier.height(8.dp))

        val notifPrefs = viewModel.getNotificationPreferences()

        SettingsCard {
            SettingsToggleRow(
                label = "Focus Reminders",
                emoji = "🎯",
                subtitle = "Daily reminder to start focusing",
                isChecked = notifPrefs.focusRemindersEnabled,
                onToggle = { viewModel.updateNotificationPref("focus_reminders", it) }
            )
            SettingsDivider()
            SettingsToggleRow(
                label = "Streak Alerts",
                emoji = "🔥",
                subtitle = "Alerts when your streak is at risk",
                isChecked = notifPrefs.streakAlertsEnabled,
                onToggle = { viewModel.updateNotificationPref("streak_alerts", it) }
            )
            SettingsDivider()
            SettingsToggleRow(
                label = "Daily Digest",
                emoji = "📊",
                subtitle = "End-of-day focus summary",
                isChecked = notifPrefs.dailyDigestEnabled,
                onToggle = { viewModel.updateNotificationPref("daily_digest", it) }
            )
            SettingsDivider()
            SettingsToggleRow(
                label = "Motivational Nudges",
                emoji = "💡",
                subtitle = "Inspiring quotes and tips",
                isChecked = notifPrefs.motivationalNudgesEnabled,
                onToggle = { viewModel.updateNotificationPref("motivational_nudges", it) }
            )
            SettingsDivider()
            SettingsToggleRow(
                label = "Achievement Alerts",
                emoji = "🏆",
                subtitle = "Badge unlock notifications",
                isChecked = notifPrefs.achievementAlertsEnabled,
                onToggle = { viewModel.updateNotificationPref("achievement_alerts", it) }
            )
            SettingsDivider()
            SettingsToggleRow(
                label = "Bedtime Reminder",
                emoji = "🌙",
                subtitle = "Wind-down notification at curfew",
                isChecked = notifPrefs.bedtimeReminderEnabled,
                onToggle = { viewModel.updateNotificationPref("bedtime_reminder", it) }
            )
            SettingsDivider()
            SettingsToggleRow(
                label = "Quiet Hours",
                emoji = "🔇",
                subtitle = "Silence notifications 10PM - 7AM",
                isChecked = notifPrefs.quietHoursEnabled,
                onToggle = { viewModel.updateNotificationPref("quiet_hours", it) }
            )
            SettingsDivider()
            val context = LocalContext.current
            SettingsActionRow(
                label = "Test Notification",
                emoji = "🔔",
                subtitle = "Preview focus alert on this device",
                actionLabel = "Send Test",
                onClick = { viewModel.sendTestNotification(context) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- FOCUS BEHAVIOR & SHIELDS ---
        SettingsSectionHeader(title = "🛡️ Focus Behavior & Shields", subtitle = "Fine-tune restrictions, whitelists & bedtime")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsToggleRow(
                label = "Strict Mode",
                emoji = "🔒",
                subtitle = "Lock device during focus sessions",
                isChecked = viewModel.isStrictModeEnabled,
                onToggle = { viewModel.toggleStrictMode(it) }
            )
            SettingsDivider()
            SettingsToggleRow(
                label = "Haptic Feedback",
                emoji = "📳",
                subtitle = "Vibration on button taps",
                isChecked = viewModel.getHapticEnabled(),
                onToggle = { viewModel.setHapticEnabled(it) }
            )
            SettingsDivider()
            SettingsToggleRow(
                label = "Sound Effects",
                emoji = "🔊",
                subtitle = "Timer completion sounds",
                isChecked = viewModel.getSoundEnabled(),
                onToggle = { viewModel.setSoundEnabled(it) }
            )
            SettingsDivider()
            SettingsToggleRow(
                label = "Notification Shield",
                emoji = "🛡️",
                subtitle = "Batch notifications during focus",
                isChecked = viewModel.isNotificationShieldEnabled,
                onToggle = { viewModel.toggleNotificationShield(it) }
            )
            SettingsActionRow(
                label = "Productive Whitelist",
                emoji = "✅",
                subtitle = "Essential apps allowed during focus lockdown",
                actionLabel = "Configure",
                onClick = { viewModel.showProductiveWhitelistModal = true }
            )
            SettingsDivider()
            SettingsToggleRow(
                label = "Strict Work Mode",
                emoji = "💼",
                subtitle = "Only Teams, Outlook, WhatsApp & office tools work",
                isChecked = viewModel.isWorkModeEnabled,
                onToggle = { viewModel.toggleWorkMode(it) }
            )
            SettingsDivider()
            SettingsActionRow(
                label = "App Blocker Rules",
                emoji = "🚫",
                subtitle = "Manage app shields, daily limits & doomscrolling",
                actionLabel = "Manage",
                onClick = { viewModel.showAppBlockerRulesModal = true }
            )
            SettingsDivider()
            SettingsActionRow(
                label = "Sleep & Bedtime Curfew",
                emoji = "🌙",
                subtitle = "Circadian wind-down, curfew hours & sleep stats",
                actionLabel = "Open",
                onClick = { viewModel.showSleepTrackerModal = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- SECURITY ---
        SettingsSectionHeader(title = "🔐 Security", subtitle = "Manage your lock PIN")
        Spacer(modifier = Modifier.height(8.dp))

        var showPinEdit by remember { mutableStateOf(false) }
        var newPin by remember { mutableStateOf("") }

        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔑", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Lock PIN", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = FocusWhite)
                        Text(
                            text = "Current: •••• (SHA-256 Protected)",
                            fontSize = 11.sp,
                            color = FocusMuted
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FocusAccent.copy(alpha = 0.12f))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showPinEdit = !showPinEdit
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (showPinEdit) "Cancel" else "Change",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FocusAccent
                    )
                }
            }

            if (showPinEdit) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // PIN input as 4 boxes
                    (0 until 4).forEach { i ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FocusPanelLight)
                                .border(1.dp, FocusPanelBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    val digit = ((newPin.length) % 10).toString()
                                    if (newPin.length < 4) newPin += digit
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (i < newPin.length) "●" else "",
                                fontSize = 18.sp,
                                color = FocusAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    // Quick digit buttons
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("1", "2", "3").forEach { d ->
                                PinDigitButton(d) { newPin = (newPin + d).take(4) }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("4", "5", "6").forEach { d ->
                                PinDigitButton(d) { newPin = (newPin + d).take(4) }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("7", "8", "9").forEach { d ->
                                PinDigitButton(d) { newPin = (newPin + d).take(4) }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PinDigitButton("⌫") { if (newPin.isNotEmpty()) newPin = newPin.dropLast(1) }
                            PinDigitButton("0") { newPin = (newPin + "0").take(4) }
                            PinDigitButton("✓") {
                                if (newPin.length == 4) {
                                    viewModel.updatePin(newPin)
                                    showPinEdit = false
                                    newPin = ""
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- DATA & ACCOUNT ---
        SettingsSectionHeader(title = "📦 Data", subtitle = "Manage your FocusNow data")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            val context = LocalContext.current
            SettingsActionRow(
                label = "Export Productivity Data",
                emoji = "📤",
                subtitle = "Full JSON backup of tasks, goals, habits & session logs",
                actionLabel = "Export",
                onClick = { viewModel.exportProductivityJson(context) }
            )
            SettingsDivider()
            SettingsActionRow(
                label = "Reset All Local Data",
                emoji = "⚠️",
                subtitle = "Reinitialize tasks, goals, habits and timers to defaults",
                actionLabel = "Reset",
                actionColor = PriorityHigh,
                onClick = { viewModel.resetAllData(context) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- ABOUT ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FocusPanel)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🧠", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("FocusNow", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FocusAccent)
                Text("v2.1.0 · Zen Edition", fontSize = 12.sp, color = FocusMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reclaim your attention. Master your dopamine.",
                    fontSize = 11.sp,
                    color = FocusSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- REUSABLE SETTINGS COMPONENTS ---

@Composable
private fun SettingsSectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = FocusWhite
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = FocusMuted
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FocusPanel)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(1.dp)
            .background(FocusPanelBorder.copy(alpha = 0.5f))
    )
}

@Composable
private fun SettingsToggleRow(
    label: String,
    emoji: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = FocusWhite)
                Text(subtitle, fontSize = 11.sp, color = FocusMuted)
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = FocusAccent,
                checkedTrackColor = FocusAccent.copy(alpha = 0.3f),
                uncheckedThumbColor = FocusMuted,
                uncheckedTrackColor = FocusPanelBorder
            )
        )
    }
}

@Composable
private fun SettingsNumberRow(
    label: String,
    emoji: String,
    value: Int,
    unit: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = FocusWhite)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Minus button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(FocusPanelLight)
                    .border(1.dp, FocusPanelBorder, CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDecrease()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("−", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FocusSecondary)
            }
            Text(
                text = "$value $unit",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = FocusAccent,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center
            )
            // Plus button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(FocusAccent.copy(alpha = 0.12f))
                    .border(1.dp, FocusAccent.copy(alpha = 0.3f), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onIncrease()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FocusAccent)
            }
        }
    }
}

@Composable
private fun SettingsActionRow(
    label: String,
    emoji: String,
    subtitle: String,
    actionLabel: String,
    actionColor: Color = FocusAccent,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = FocusWhite)
                Text(subtitle, fontSize = 11.sp, color = FocusMuted)
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(actionColor.copy(alpha = 0.12f))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = actionLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = actionColor
            )
        }
    }
}

@Composable
private fun PinDigitButton(digit: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(FocusPanelLight)
            .border(1.dp, FocusPanelBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (digit == "✓") FocusAccent else FocusWhite
        )
    }
}
