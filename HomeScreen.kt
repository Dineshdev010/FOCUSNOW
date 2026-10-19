package com.focusflow.app.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.focusflow.app.model.FocusSchedule
import com.focusflow.app.model.BlockedApp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.ui.components.CategoryBadge
import com.focusflow.app.ui.components.CheckmarkIcon
import com.focusflow.app.ui.components.FocusBrandBadge
import com.focusflow.app.ui.components.PriorityBadge
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel
import java.util.Calendar

@Composable
fun HomeScreen(viewModel: FocusFlowViewModel) {
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()

    val toastMsg = viewModel.showBonsaiToastMessage
    androidx.compose.runtime.LaunchedEffect(toastMsg) {
        if (toastMsg != null) {
            android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.showBonsaiToastMessage = null
        }
    }

    val totalCount = viewModel.tasks.size
    val completedCount = viewModel.tasks.count { it.isCompleted }
    val progressRatio = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )
    val percent = (animatedProgress * 100).toInt()

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    val savedHours = String.format("%.1f", viewModel.savedScreenTimeMinutes / 60f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Header with Brand and Coin Balance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FocusBrandBadge()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Streak Header Badge -> Opens Progress / Streak Details
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanel)
                        .border(1.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.selectedTab = "Progress"
                        }
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${viewModel.streakDays}d",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Coin Balance Badge -> Opens Rewards Store
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanel)
                        .border(1.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showRewardsStoreModal = true
                        }
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${viewModel.focusCoins}",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Achievements Badge -> Opens Achievements & Challenges Screen
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanel)
                        .border(1.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showAchievementsScreen = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏆", fontSize = 14.sp)
                }

                // Universal Search Badge -> Opens Global Search Modal
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanel)
                        .border(1.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showGlobalSearchModal = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔍", fontSize = 14.sp)
                }

                // Settings Badge -> Opens Full Settings Screen
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanel)
                        .border(1.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showSettingsScreen = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚙️", fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "$greeting.",
            color = FocusSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Make today count.",
            color = FocusWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        // Goal & Shield Pills
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(FocusPanelLight)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.showGoalModal = true
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("🎯 ${viewModel.userGoal}", color = FocusWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(FocusAccentSoft)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.showReelsBlockerSheet = true
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    "\uD83D\uDEE1\uFE0F Reels & Shorts Shield",
                    color = FocusAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Regain Highlight Card: Screen Time Saved & Mindful Pause
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.testMindfulIntervention("Instagram Reels")
                }
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(FocusAccentSoft, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("\u23F3", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Regained Today:", color = FocusSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$savedHours hrs saved", color = FocusAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Mindful Pause Active \u2022 Tap to practice breathwork",
                            color = FocusWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text("\u2192", color = FocusAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dopamine Shield Score & Traps Neutralized Matrix
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(FocusPanel, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Dopamine Health", color = FocusSecondary, fontSize = 10.sp)
                            Text("${viewModel.dopamineScore}% Zen", color = FocusAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(FocusPanel, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Reels Intercepted", color = FocusSecondary, fontSize = 10.sp)
                            Text("${viewModel.reelsBlockedCount} Blocked", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(FocusPanel, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Interventions", color = FocusSecondary, fontSize = 10.sp)
                            Text("${viewModel.interventionsAccepted} Resisted", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Progress Card
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DAILY FOCUS GOAL",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$completedCount of $totalCount tasks done",
                            color = FocusWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "$percent%",
                        color = FocusAccent,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(FocusBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FocusAccent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Living Zen Bonsai Garden Card (Focus Gamification)
        val bonsai = viewModel.bonsaiStatus
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(FocusAccentSoft)
                                .border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(bonsai.emoji, fontSize = 26.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ZEN BONSAI",
                                    color = FocusAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FocusPanel)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Stage ${bonsai.stage}/5",
                                        color = FocusWhite,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = bonsai.title,
                                color = FocusWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    // Water / Nurture Action Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(FocusAccent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (bonsai.stage >= 5 && bonsai.waterPoints >= 100) {
                                    viewModel.harvestAndPlantInSanctuary()
                                } else {
                                    viewModel.waterBonsaiPlant()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (bonsai.stage >= 5) "🌸 Plant" else "💧 Nurture", color = FocusBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (bonsai.stage >= 5) "+50🪙" else "15🪙", color = FocusBackground.copy(alpha = 0.85f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Growth progress & Sanctuary count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Growth: ${bonsai.waterPoints}% (${100 - bonsai.waterPoints}% to next stage)",
                        color = FocusSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "🌳 ${bonsai.totalTreesGrown} in Sanctuary",
                        color = FocusWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Animated Growth Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(FocusPanel)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(bonsai.waterPoints / 100f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FocusAccent)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🌱 Grown by deep focus sessions (+35%) and resisting doomscroll temptations (+15%).",
                    color = FocusSecondary,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions Grid (Deep Focus / Mind Puzzles / Code Arena / Reels Shield)
        Text(
            text = "QUICK SHORTCUTS",
            color = FocusAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Start Focus Session Shortcut
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.selectedTab = "Focus"
                        if (!viewModel.isTimerRunning) {
                            viewModel.startTimer()
                        }
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("⚡", fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Deep Focus", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Start 25m Timer", color = FocusAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Mind Puzzle Arena Shortcut
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.showJigsawModal = true
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🧩", fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Mind Puzzles", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("8 Brain Puzzles", color = FocusSecondary, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Code Arena Shortcut
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.showCodeChallengeModal = true
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💻", fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Code Arena", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Earn Focus Coins", color = FocusSecondary, fontSize = 10.sp)
                }
            }

            // Reels Shield Shortcut
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.showReelsBlockerSheet = true
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🛡️", fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Reels Shield", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${viewModel.reelsBlockedCount} blocked", color = FocusSecondary, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Every App Blocker Rules Shortcut
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.showAppBlockerRulesModal = true
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🛡️", fontSize = 22.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PriorityHigh.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${viewModel.allAppShieldRules.count { it.isBlocked }} Blocked",
                                color = PriorityHigh,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("App Shield", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Every App Toggle", color = FocusSecondary, fontSize = 10.sp)
                }
            }

            // Sleep Tracker Shortcut
            val sleep = viewModel.sleepTrackingState
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.showSleepTrackerModal = true
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌙", fontSize = 22.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(androidx.compose.ui.graphics.Color(0xFF8B5CF6).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${sleep.lastNightScore} Score",
                                color = androidx.compose.ui.graphics.Color(0xFFA78BFA),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Sleep Tracker", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${sleep.lastNightDuration} • Curfew", color = FocusSecondary, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1-Tap Pin Shortcut to Launcher Banner
        val context = androidx.compose.ui.platform.LocalContext.current
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FocusAccent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.pinFocusShortcut(context)
                    android.widget.Toast.makeText(context, "📌 Added ⚡ Quick Focus shortcut to Home Screen!", android.widget.Toast.LENGTH_SHORT).show()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📲", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Add Shortcut to Home Screen",
                            color = FocusWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Instant 1-tap 25m Focus from your desktop",
                            color = FocusSecondary,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .background(FocusAccent, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Add +",
                        color = FocusBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1-Tap Pin Widget to Home Screen
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FocusAccent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val pinned = viewModel.pinFocusWidget(context)
                    val msg = if (pinned) "📌 FocusNow Widget pinned to your Home Screen!" else "📌 Widget requested! Add from your launcher's widget menu if prompt doesn't appear."
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Live Home Screen Widget",
                            color = FocusWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Track streak, stats & 1-tap focus",
                            color = FocusSecondary,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .background(FocusAccent, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Pin +",
                        color = FocusBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Focus Schedules Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SCHEDULED FOCUS BLOCKS",
                    color = FocusAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Distraction Shields",
                    color = FocusWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val hasActiveSchedule = viewModel.focusSchedules.any { it.isCurrentlyActive() }
                if (hasActiveSchedule) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(FocusAccentSoft)
                            .border(1.dp, FocusAccent, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(FocusAccent)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ACTIVE",
                                color = FocusAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Add Schedule Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanelLight)
                        .border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showAddScheduleModal = true
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+ Add Block",
                        color = FocusAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Schedules List
        viewModel.focusSchedules.forEach { schedule ->
            val isActiveNow = schedule.isCurrentlyActive()
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isActiveNow) FocusPanelLight else FocusPanel
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .then(
                        if (isActiveNow) {
                            Modifier.border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        } else {
                            Modifier
                        }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActiveNow) FocusAccentSoft else FocusPanelLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                schedule.name.contains("Bedtime", ignoreCase = true) -> "🌙"
                                schedule.name.contains("Study", ignoreCase = true) -> "📚"
                                else -> "💼"
                            },
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = schedule.name,
                                color = FocusWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            if (isActiveNow) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FocusAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Active",
                                        color = FocusAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${schedule.formattedTimeRange()} • ${schedule.daysSummary}",
                            color = FocusSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = schedule.isEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleFocusSchedule(schedule.id)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FocusBackground,
                            checkedTrackColor = FocusAccent,
                            uncheckedThumbColor = FocusSecondary,
                            uncheckedTrackColor = FocusBorder
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Daily Limits & Screen Time Budget
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "APP TIME LIMITS & BUDGET",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Per-App Rules & Toggles",
                        color = FocusWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanelLight)
                        .border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showAppBlockerRulesModal = true
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Manage All Apps 🛡️",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            viewModel.blockedApps.forEach { app ->
                val usedRatio = (app.usedMinutesToday.toFloat() / app.dailyLimitMinutes.toFloat()).coerceIn(0f, 1f)
                val isLimitExceeded = app.usedMinutesToday >= app.dailyLimitMinutes

                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusPanel),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FocusPanelLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when {
                                        app.name.contains("Instagram", ignoreCase = true) -> "📸"
                                        app.name.contains("YouTube", ignoreCase = true) -> "▶️"
                                        app.name.contains("Snapchat", ignoreCase = true) -> "👻"
                                        app.name.contains("Chrome", ignoreCase = true) -> "🌐"
                                        app.name.contains("TikTok", ignoreCase = true) -> "🎵"
                                        app.name.contains("X", ignoreCase = true) || app.name.contains("Twitter", ignoreCase = true) -> "🐦"
                                        else -> "📱"
                                    },
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.name,
                                    color = FocusWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isLimitExceeded) {
                                        "Limit reached (${app.usedMinutesToday}m / ${app.dailyLimitMinutes}m)"
                                    } else {
                                        "${app.usedMinutesToday}m used today • ${app.dailyLimitMinutes - app.usedMinutesToday}m remaining"
                                    },
                                    color = if (isLimitExceeded) PriorityHigh else FocusSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isLimitExceeded) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Switch(
                                    checked = app.isBlocked,
                                    onCheckedChange = { isBlockedNow ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.toggleAppBlockedState(app.packageName, isBlockedNow)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = FocusBackground,
                                        checkedTrackColor = PriorityHigh,
                                        uncheckedThumbColor = FocusBackground,
                                        uncheckedTrackColor = FocusBorder
                                    )
                                )
                                Text(
                                    text = if (app.isBlocked) "Blocked 🔴" else "Allowed 🟢",
                                    color = if (app.isBlocked) PriorityHigh else FocusAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(FocusBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(usedRatio)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isLimitExceeded) PriorityHigh else FocusAccent)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Limit selector chips: 15m, 30m, 45m, 60m
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Cap:",
                                color = FocusMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )

                            listOf(15, 30, 45, 60).forEach { limit ->
                                val isSelected = app.dailyLimitMinutes == limit
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) FocusAccent else FocusPanelLight)
                                        .border(
                                            1.dp,
                                            if (isSelected) FocusAccent else FocusBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.updateAppDailyLimit(app.packageName, limit)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${limit}m",
                                        color = if (isSelected) FocusBackground else FocusWhite,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Habits Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Habit Routines",
                color = FocusWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Manage ⚡",
                color = FocusAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { viewModel.selectedTab = "Habits & Goals" }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        if (viewModel.habits.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.selectedTab = "Habits & Goals" }
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("No habits set yet. Tap to configure daily routines.", color = FocusSecondary, fontSize = 12.sp)
                }
            }
        } else {
            viewModel.habits.take(3).forEach { habit ->
                val isDone = habit.completedDates.contains(todayDateStr)
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (isDone) FocusAccent else FocusPanel)
                                .border(1.5.dp, if (isDone) FocusAccent else FocusBorder, CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleHabitToday(habit.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                CheckmarkIcon()
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(habit.title, color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            if (habit.description.isNotBlank()) {
                                Text(habit.description, color = FocusSecondary, fontSize = 10.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (habit.currentStreak > 0) FocusYellow.copy(alpha = 0.2f) else FocusPanel)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🔥 ${habit.currentStreak}d",
                                color = if (habit.currentStreak > 0) FocusYellow else FocusSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tasks Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Tasks",
                color = FocusWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "See All",
                color = FocusAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { viewModel.selectedTab = "Tasks" }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tasks list
        viewModel.tasks.forEach { task ->
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleTask(task.id)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (task.isCompleted) FocusAccent else FocusPanel)
                            .border(1.5.dp, if (task.isCompleted) FocusAccent else FocusBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isCompleted) {
                            CheckmarkIcon()
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            color = if (task.isCompleted) FocusSecondary else FocusWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CategoryBadge(category = task.category)
                            PriorityBadge(priority = task.priority)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(135.dp))
    }
}