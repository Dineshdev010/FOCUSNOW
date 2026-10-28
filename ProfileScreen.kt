package com.focusflow.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.focusflow.app.model.Achievement
import com.focusflow.app.ui.components.AtmosphericZenBackground
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: FocusFlowViewModel,
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    var showEditProfileDialog by remember { mutableStateOf(false) }

    val userLevel = viewModel.getUserLevel()
    val achievements = viewModel.getAchievements()
    val unlockedAchievements = achievements.filter { it.isUnlocked }
    val focusHours = viewModel.totalFocusMinutes / 60
    val focusMins = viewModel.totalFocusMinutes % 60
    val savedHours = String.format(Locale.US, "%.1f", viewModel.savedScreenTimeMinutes / 60f)

    BackHandler {
        onClose()
    }

    AtmosphericZenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
        // --- TOP NAVIGATION APP BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FocusPanel)
                    .border(1.dp, FocusPanelBorder, RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClose()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("←", fontSize = 18.sp, color = FocusWhite, fontWeight = FontWeight.Bold)
            }

            // Screen Header Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusWhite
                )
                Text(
                    text = "@${viewModel.userName.lowercase().replace(" ", "")}",
                    fontSize = 12.sp,
                    color = FocusMuted
                )
            }

            // Action Buttons (Share & Settings)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share Profile
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanel)
                        .border(1.dp, FocusPanelBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "My FocusFlow Profile")
                                putExtra(Intent.EXTRA_TEXT, viewModel.getProfileShareSummary())
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Focus Stats"))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("📤", fontSize = 15.sp)
                }

                // Settings Shortcut
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanel)
                        .border(1.dp, FocusPanelBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showSettingsScreen = true
                            onClose()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚙️", fontSize = 15.sp)
                }
            }
        }

        // --- SCROLLABLE PROFILE CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // --- HERO IDENTITY CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = FocusPanel)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(FocusAccent.copy(alpha = 0.15f), FocusPanel)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with glowing ring and edit indicator
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(FocusAccent.copy(alpha = 0.28f), FocusPanelLight)
                                        )
                                    )
                                    .border(2.5.dp, FocusAccent, CircleShape)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showEditProfileDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = viewModel.userAvatar,
                                    fontSize = 42.sp
                                )
                            }

                            // Edit pencil pill
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(FocusAccent)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showEditProfileDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✏️", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // User Name
                        Text(
                            text = viewModel.userName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FocusWhite
                        )

                        // Title & Handle
                        Text(
                            text = "${userLevel.title} • Level ${userLevel.level}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FocusAccent
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // User Bio
                        Text(
                            text = "\"${viewModel.userBio}\"",
                            fontSize = 13.sp,
                            color = FocusSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Metadata Pill Row (Joined Date & Shield Status)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FocusPanelLight)
                                    .border(1.dp, FocusPanelBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "📅 ${viewModel.userJoinedDate}",
                                    fontSize = 11.sp,
                                    color = FocusSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FocusPanelLight)
                                    .border(
                                        1.dp,
                                        if (viewModel.isMasterShieldEnabled) FocusAccent.copy(alpha = 0.5f) else FocusPanelBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (viewModel.isMasterShieldEnabled) "🛡️ Shield Active" else "🛡️ Shield Paused",
                                    fontSize = 11.sp,
                                    color = if (viewModel.isMasterShieldEnabled) FocusAccent else FocusMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Edit Profile Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FocusPanelLight)
                                .border(1.dp, FocusPanelBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showEditProfileDialog = true
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✏️ Edit Profile & Avatar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FocusWhite
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- LEVEL & XP PROGRESSION CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FocusPanel)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Level ${userLevel.level} Progression",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = FocusWhite
                            )
                        }
                        Text(
                            text = "${userLevel.totalXp} Total XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FocusAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Animated XP Bar
                    val xpProgress by animateFloatAsState(
                        targetValue = userLevel.progressPercent,
                        animationSpec = tween(600),
                        label = "profileXpAnim"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FocusPanelLight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(xpProgress)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(FocusAccent, FocusAccentDark)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${userLevel.currentXp} / ${userLevel.xpToNextLevel} XP to Level ${userLevel.level + 1}",
                            fontSize = 11.sp,
                            color = FocusMuted
                        )
                        Text(
                            text = "${(xpProgress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4-STAT METRIC MATRIX (Focus Showcase) ---
            Text(
                text = "📊 Focus Statistics",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = FocusWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Streak Card
                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🔥",
                    value = "${viewModel.streakDays}d",
                    title = "Daily Streak",
                    subtitle = "Best: ${viewModel.longestStreakDays}d",
                    tint = Color(0xFFFF7A00),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.selectedTab = "Progress"
                        onClose()
                    }
                )

                // Total Focus Time Card
                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "⏱️",
                    value = if (focusHours > 0) "${focusHours}h ${focusMins}m" else "${focusMins}m",
                    title = "Deep Focus",
                    subtitle = "${viewModel.completedSessions} sessions",
                    tint = FocusAccent,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.selectedTab = "Progress"
                        onClose()
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Reclaimed Screen Time Card
                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🛡️",
                    value = "${savedHours}h",
                    title = "Time Saved",
                    subtitle = "${viewModel.reelsBlockedCount} temptations cut",
                    tint = Color(0xFF38BDF8),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.selectedTab = "Home"
                        onClose()
                    }
                )

                // Focus Coins Card
                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🪙",
                    value = "${viewModel.focusCoins}",
                    title = "Focus Coins",
                    subtitle = "Tap to spend",
                    tint = Color(0xFFFBBF24),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.showRewardsStoreModal = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- ACHIEVEMENTS SHOWCASE SHELF ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆 Achievements (${unlockedAchievements.size}/${achievements.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusWhite
                )
                Text(
                    text = "View All →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusAccent,
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.showAchievementsScreen = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (unlockedAchievements.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FocusPanel)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌱", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Your Journey Has Just Begun",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FocusWhite
                            )
                            Text(
                                text = "Complete focus sessions and maintain streaks to unlock badges!",
                                fontSize = 11.sp,
                                color = FocusMuted
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    unlockedAchievements.take(5).forEach { ach ->
                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.showAchievementsScreen = true
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = FocusPanel)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(FocusAccent.copy(alpha = 0.16f))
                                        .border(1.5.dp, FocusAccent.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(ach.emoji, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ach.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FocusWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = ach.tier.title,
                                    fontSize = 9.sp,
                                    color = FocusAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- FOCUS IDENTITY & EQUIPPED LOADOUT ---
            Text(
                text = "⚡ Equipped Focus Loadout",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = FocusWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FocusPanel)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Equipped Theme Row
                    ProfileLoadoutRow(
                        emoji = "🎨",
                        title = "App Theme",
                        value = when (viewModel.activeThemeId) {
                            "theme_midnight" -> "Glacier Frost"
                            "theme_cyberpunk" -> "Cyberpunk Neon"
                            "theme_sunset" -> "Solar Amber"
                            else -> "Lime Zen"
                        },
                        trailingAction = "Change",
                        onAction = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showSettingsScreen = true
                            onClose()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bonsai Tree Health
                    ProfileLoadoutRow(
                        emoji = "🪴",
                        title = "Focus Bonsai",
                        value = "${viewModel.bonsaiStatus.title} • ${viewModel.bonsaiStatus.healthPercent}% Health",
                        trailingAction = "Nurture",
                        onAction = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.selectedTab = "Home"
                            onClose()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Curfew & Sleep Protection
                    ProfileLoadoutRow(
                        emoji = "🌙",
                        title = "Night Curfew",
                        value = if (viewModel.sleepTrackingState.bedtimeCurfewEnabled) {
                            "Active (${viewModel.sleepTrackingState.curfewStartHour}:00 - ${viewModel.sleepTrackingState.curfewEndHour}:00)"
                        } else {
                            "Disabled"
                        },
                        trailingAction = "Configure",
                        onAction = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showSleepTrackerModal = true
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Jigsaw Mind Arena Theme
                    ProfileLoadoutRow(
                        emoji = "🧩",
                        title = "Mind Puzzle Theme",
                        value = viewModel.selectedJigsawTheme.name.lowercase().replaceFirstChar { it.uppercase() },
                        trailingAction = "Practice",
                        onAction = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.openJigsawPuzzle("Practice brain workout")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- ACCOUNT ACTIONS & PREFERENCES ---
            Text(
                text = "⚙️ Preferences & Quick Actions",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = FocusWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FocusPanel)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Daily Focus Goal
                    ProfileMenuRow(
                        emoji = "🎯",
                        title = "Daily Focus Goal",
                        subtitle = "${viewModel.dailyFocusGoalMinutes} minutes / day",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showGoalModal = true
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // App Limits & Focus Schedules
                    ProfileMenuRow(
                        emoji = "⏱️",
                        title = "App Budgets & Focus Windows",
                        subtitle = "${viewModel.blockedApps.size} apps capped • ${viewModel.focusSchedules.size} auto windows",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showAppLimitsAndSchedulesModal = true
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Always Allowed Apps & Master Shield
                    ProfileMenuRow(
                        emoji = "🛡️",
                        title = "Always Allowed Apps",
                        subtitle = "${viewModel.alwaysAllowedPackages.size} apps always unblocked",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showAlwaysAllowedModal = true
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rewards & Perk Shop
                    ProfileMenuRow(
                        emoji = "🏪",
                        title = "Perk Shop & Themes",
                        subtitle = "Exchange coins for perks and visual styles",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showRewardsStoreModal = true
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Copy Stats
                    ProfileMenuRow(
                        emoji = "📋",
                        title = "Copy Profile Summary",
                        subtitle = "Copy formatted stats to clipboard",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("FocusFlow Profile", viewModel.getProfileShareSummary())
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied profile stats to clipboard! 📋", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Version Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FocusFlow Pro • Version 2.4",
                    fontSize = 11.sp,
                    color = FocusMuted
                )
                Text(
                    text = "Reclaiming human attention, one session at a time",
                    fontSize = 10.sp,
                    color = FocusMuted.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(44.dp))
        }
    }
    }

    // --- INTERACTIVE EDIT PROFILE DIALOG ---
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = viewModel.userName,
            currentBio = viewModel.userBio,
            currentAvatar = viewModel.userAvatar,
            onSave = { newName, newBio, newAvatar ->
                viewModel.updateProfile(newName, newBio, newAvatar)
                showEditProfileDialog = false
                Toast.makeText(context, "Profile updated successfully! ✨", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }
}

// --- STAT CARD COMPONENT ---
@Composable
private fun ProfileStatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FocusPanel)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 20.sp)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(tint)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FocusWhite
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = FocusSecondary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = FocusMuted
            )
        }
    }
}

// --- LOADOUT ITEM ROW ---
@Composable
private fun ProfileLoadoutRow(
    emoji: String,
    title: String,
    value: String,
    trailingAction: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusWhite
                )
                Text(
                    text = value,
                    fontSize = 11.sp,
                    color = FocusMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(FocusPanelLight)
                .clickable { onAction() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = trailingAction,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = FocusAccent
            )
        }
    }
}

// --- MENU ROW ---
@Composable
private fun ProfileMenuRow(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusWhite
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = FocusMuted
                )
            }
        }
        Text("›", fontSize = 20.sp, color = FocusMuted)
    }
}

// --- EDIT PROFILE DIALOG ---
@Composable
fun EditProfileDialog(
    currentName: String,
    currentBio: String,
    currentAvatar: String,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }
    var selectedAvatar by remember { mutableStateOf(currentAvatar) }

    val presetAvatars = listOf(
        "⚡", "🧘", "🦊", "🦁", "🚀", "👑", "🐉", "🦉", "💎", "🔥", "🪐", "🛡️"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FocusPanel)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusWhite
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Avatar Preview
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(FocusPanelLight)
                        .border(2.dp, FocusAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedAvatar, fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Choose Your Avatar Emoji",
                    fontSize = 11.sp,
                    color = FocusMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Preset Avatar Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAvatars.forEach { emoji ->
                        val isSelected = selectedAvatar == emoji
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) FocusAccent.copy(alpha = 0.25f) else FocusPanelLight)
                                .border(
                                    1.5.dp,
                                    if (isSelected) FocusAccent else FocusPanelBorder,
                                    CircleShape
                                )
                                .clickable { selectedAvatar = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 25) name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = FocusWhite,
                        unfocusedTextColor = FocusWhite,
                        focusedBorderColor = FocusAccent,
                        unfocusedBorderColor = FocusPanelBorder,
                        focusedLabelColor = FocusAccent,
                        unfocusedLabelColor = FocusSecondary,
                        cursorColor = FocusAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bio Input
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 80) bio = it },
                    label = { Text("Personal Bio / Mission") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = FocusWhite,
                        unfocusedTextColor = FocusWhite,
                        focusedBorderColor = FocusAccent,
                        unfocusedBorderColor = FocusPanelBorder,
                        focusedLabelColor = FocusAccent,
                        unfocusedLabelColor = FocusSecondary,
                        cursorColor = FocusAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = FocusSecondary)
                    }

                    Button(
                        onClick = { onSave(name, bio, selectedAvatar) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", color = FocusBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
