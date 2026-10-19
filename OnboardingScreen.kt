package com.focusflow.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.BlockedApp
import com.focusflow.app.ui.theme.*
import com.focusflow.app.util.PermissionHelper
import com.focusflow.app.viewmodel.FocusFlowViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(viewModel: FocusFlowViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 5

    // Preferences State
    var dailyTargetMinutes by remember { mutableIntStateOf(120) }
    var focusDurationMinutes by remember { mutableIntStateOf(25) }
    var shortBreakMinutes by remember { mutableIntStateOf(5) }
    var longBreakMinutes by remember { mutableIntStateOf(15) }
    var selectedTheme by remember { mutableStateOf("Dark Modern") }

    // Goal & Apps State
    var selectedGoal by remember { mutableStateOf("Eliminate Reels & Doomscrolling") }
    var selectedApps by remember { mutableStateOf(viewModel.blockedApps) }
    var pin by remember { mutableStateOf("1234") }

    val goals = listOf(
        "🛡️ Eliminate Reels & Doomscrolling",
        "📚 Master Deep Study & Coding Focus",
        "⏳ Save 2+ Hours of Daily Screen Time",
        "🧠 Build Consistent Habit Streaks"
    )

    fun finishWithDefaults() {
        viewModel.completeOnboarding(
            goal = selectedGoal,
            selectedApps = selectedApps,
            pin = pin,
            focusMinutes = focusDurationMinutes,
            shortBreakMinutes = shortBreakMinutes,
            longBreakMinutes = longBreakMinutes,
            dailyTargetMinutes = dailyTargetMinutes
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .statusBarsPadding()
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                Text(
                    text = "← Back",
                    color = FocusSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        step--
                    }
                )
            } else {
                Text(
                    text = "FOCUSNOW",
                    color = FocusAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }

            // Step Progress Dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0 until totalSteps) {
                    Box(
                        modifier = Modifier
                            .width(if (i == step) 22.dp else 7.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (i <= step) FocusAccent else FocusBorder)
                    )
                }
            }

            // Quick Skip Button
            Text(
                text = "Skip",
                color = FocusSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    finishWithDefaults()
                }
            )
        }

        // Scrollable Body Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(targetState = step, label = "onboardingStepTransition") { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    when (currentStep) {
                        0 -> {
                            // Screen 1: Welcome & Value Proposition
                            Spacer(modifier = Modifier.height(20.dp))
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(FocusAccentSoft)
                                    .border(1.5.dp, FocusAccent, RoundedCornerShape(22.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🛡️", fontSize = 36.sp)
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Welcome to FocusNow",
                                color = FocusWhite,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Reclaim your attention, conquer digital addictions, and master deep flow sessions with precision.",
                                color = FocusSecondary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Highlight 1: Mindful Shield
                            OnboardingValueCard(
                                emoji = "⚡",
                                title = "Mindful Shield & Shorts Blocker",
                                description = "Pause before doomscrolling. Block Instagram Reels and YouTube Shorts without losing whole apps."
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Highlight 2: Pomodoro Focus Engine
                            OnboardingValueCard(
                                emoji = "⏱️",
                                title = "Deep Work Focus Engine",
                                description = "Time-tested Pomodoro intervals with strict lockdown mode, ambient soundscapes, and emergency passcodes."
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Highlight 3: Habits & Milestones
                            OnboardingValueCard(
                                emoji = "🎯",
                                title = "Habit Streaks & Milestone Goals",
                                description = "Build non-negotiable daily routines, track your longest active streaks, and measure true time reclaimed."
                            )

                            Spacer(modifier = Modifier.height(30.dp))
                        }

                        1 -> {
                            // Screen 2: User Preferences
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Customize Your Focus Rhythm",
                                color = FocusWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Set your personal daily targets and interval lengths. You can adjust these anytime in Settings.",
                                color = FocusSecondary,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            // 1. Daily Focus Target
                            Text("DAILY FOCUS TARGET", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(60 to "1 Hour (Gentle)", 120 to "2 Hours (Optimal)", 180 to "3 Hours (Deep)", 240 to "4 Hours (Intense)").forEach { (mins, label) ->
                                    val isSelected = dailyTargetMinutes == mins
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) FocusAccentSoft else FocusPanel)
                                            .border(1.dp, if (isSelected) FocusAccent else FocusBorder, RoundedCornerShape(12.dp))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                dailyTargetMinutes = mins
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) FocusAccent else FocusWhite,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 2. Focus Sprint Duration
                            Text("DEFAULT FOCUS SPRINT", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(25 to "25 min\nPomodoro", 45 to "45 min\nDeep Flow", 50 to "50 min\nMastery").forEach { (mins, label) ->
                                    val isSelected = focusDurationMinutes == mins
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) FocusAccentSoft else FocusPanel)
                                            .border(1.dp, if (isSelected) FocusAccent else FocusBorder, RoundedCornerShape(14.dp))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                focusDurationMinutes = mins
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) FocusAccent else FocusWhite,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 3. Break Durations
                            Text("BREAK RECOVERY INTERVALS", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(FocusPanel)
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text("Short Break", color = FocusSecondary, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf(5, 10).forEach { mins ->
                                                val isSelected = shortBreakMinutes == mins
                                                Text(
                                                    text = "${mins}m",
                                                    color = if (isSelected) FocusAccent else FocusWhite,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                                                        .clickable { shortBreakMinutes = mins }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(FocusPanel)
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text("Long Break", color = FocusSecondary, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf(15, 30).forEach { mins ->
                                                val isSelected = longBreakMinutes == mins
                                                Text(
                                                    text = "${mins}m",
                                                    color = if (isSelected) FocusAccent else FocusWhite,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                                                        .clickable { longBreakMinutes = mins }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        2 -> {
                            // Screen 3: Goals
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "What is your main focus goal?",
                                color = FocusWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "We'll personalize your dashboard metrics and dopamine detox routines around this purpose.",
                                color = FocusSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            goals.forEach { goal ->
                                val isSelected = selectedGoal == goal
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) FocusAccentSoft else FocusPanel
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .border(
                                            1.dp,
                                            if (isSelected) FocusAccent else FocusBorder,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedGoal = goal
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(18.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = goal,
                                            color = if (isSelected) FocusAccent else FocusWhite,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isSelected) {
                                            Text("✓", color = FocusAccent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        3 -> {
                            // Screen 4: Distracting Apps Shield
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Select distracting apps",
                                color = FocusWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "These apps will have mindful pause delays and Reels/Shorts shields activated.",
                                color = FocusSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            selectedApps.forEach { app ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = FocusPanel),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedApps = selectedApps.map {
                                                if (it.name == app.name) it.copy(isBlocked = !it.isBlocked) else it
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(app.iconEmoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(app.name, color = FocusWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(app.category, color = FocusSecondary, fontSize = 11.sp)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (app.isBlocked) FocusAccent else FocusPanelLight)
                                                .border(1.5.dp, if (app.isBlocked) FocusAccent else FocusBorder, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (app.isBlocked) {
                                                Text("✓", color = FocusBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        4 -> {
                            // Screen 5: Shield Permissions & PIN
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Grant shield permissions",
                                color = FocusWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Android requires explicit user permissions to monitor screen time and intercept Reels in third-party apps.",
                                color = FocusSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // Permission 1: Usage Stats
                            val hasUsage = PermissionHelper.hasUsageStatsPermission(context)
                            PermissionCard(
                                title = "Usage Access",
                                subtitle = "Tracks screen time and calculates hours reclaimed",
                                isGranted = hasUsage,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    PermissionHelper.openUsageAccessSettings(context)
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Permission 2: Overlay
                            val hasOverlay = PermissionHelper.hasOverlayPermission(context)
                            PermissionCard(
                                title = "Display Over Other Apps",
                                subtitle = "Shows the Mindful Pause breathing screen when opening shielded apps",
                                isGranted = hasOverlay,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    PermissionHelper.openOverlaySettings(context)
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Permission 3: Accessibility Service (Regain style Reels Shield)
                            val hasAccessibility = PermissionHelper.isAccessibilityServiceEnabled(context)
                            PermissionCard(
                                title = "Accessibility Shield (Reels & Shorts)",
                                subtitle = "Intercepts Instagram Reels & YouTube Shorts without crashing your apps",
                                isGranted = hasAccessibility,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    PermissionHelper.openAccessibilitySettings(context)
                                }
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            // PIN Security
                            Text("SECURITY & OVERRIDE PIN", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = pin,
                                onValueChange = { if (it.length <= 4) pin = it },
                                label = { Text("4-Digit Security PIN", color = FocusSecondary) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FocusAccent,
                                    unfocusedBorderColor = FocusBorder,
                                    focusedTextColor = FocusWhite,
                                    unfocusedTextColor = FocusWhite
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(FocusAccentSoft, RoundedCornerShape(14.dp))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎉", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Welcome Bonus", color = FocusAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("You start with +100 Focus Coins to kick off your streak!", color = FocusWhite, fontSize = 11.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        // Pinned Bottom Action Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FocusPanel)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (step < totalSteps - 1) {
                        step++
                    } else {
                        finishWithDefaults()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = when {
                        step == 0 -> "Get Started →"
                        step < totalSteps - 1 -> "Continue →"
                        else -> "Enter FocusNow ✨"
                    },
                    color = FocusBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun OnboardingValueCard(
    emoji: String,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FocusAccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = FocusWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = FocusSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = FocusSecondary, fontSize = 11.sp, lineHeight = 14.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isGranted) FocusAccentSoft else CategoryUrgent.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isGranted) "✓ Granted" else "Grant →",
                    color = if (isGranted) FocusAccent else CategoryUrgent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}