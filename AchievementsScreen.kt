package com.focusflow.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.Achievement
import com.focusflow.app.model.AchievementTier
import com.focusflow.app.model.DailyChallenge
import com.focusflow.app.model.WeeklyChallenge
import com.focusflow.app.ui.components.AtmosphericZenBackground
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementsScreen(viewModel: FocusFlowViewModel, onClose: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val achievements = viewModel.getAchievements()
    val dailyChallenges = viewModel.getDailyChallenges()
    val weeklyChallenges = viewModel.getWeeklyChallenges()
    val userLevel = viewModel.getUserLevel()

    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCount = achievements.size

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Focus", "Streaks", "Defense", "Time", "Puzzles", "Bonsai", "Mindful")

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
        // Sticky Header with close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🏆 Achievements",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusWhite
                )
                Text(
                    text = "$unlockedCount / $totalCount unlocked",
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

            // --- DAILY CHALLENGES ---
            Text(
                text = "⚡ Daily Challenges",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = FocusWhite
            )
            Text("Complete before midnight to earn XP & Coins", fontSize = 11.sp, color = FocusMuted)
            Spacer(modifier = Modifier.height(8.dp))

            dailyChallenges.forEach { challenge ->
                DailyChallengeCard(challenge = challenge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- WEEKLY CHALLENGES ---
            Text(
                text = "📅 Weekly Challenges",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = FocusWhite
            )
            Text("Bonus rewards for consistent progress", fontSize = 11.sp, color = FocusMuted)
            Spacer(modifier = Modifier.height(8.dp))

            weeklyChallenges.forEach { challenge ->
                WeeklyChallengeCard(challenge = challenge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BADGE COLLECTION ---
            Text(
                text = "🎖️ Badge Collection",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = FocusWhite
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val scrollStateHoriz = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(5).forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) FocusAccent.copy(alpha = 0.18f)
                                    else FocusPanelLight
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) FocusAccent.copy(alpha = 0.4f) else FocusPanelBorder,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedCategory = cat
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) FocusAccent else FocusSecondary
                            )
                        }
                    }
                }
            }

            // Achievement Grid
            val filteredAchievements = if (selectedCategory == "All") achievements
            else achievements.filter { it.category == selectedCategory }

            // Show unlocked first, then locked sorted by progress
            val sortedAchievements = filteredAchievements.sortedWith(
                compareByDescending<Achievement> { it.isUnlocked }
                    .thenByDescending { it.progressPercent }
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sortedAchievements.forEach { achievement ->
                    AchievementBadgeCard(achievement = achievement)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- TIER PROGRESS SUMMARY ---
            Text(
                text = "📊 Tier Progress",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = FocusWhite
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FocusPanel)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    AchievementTier.entries.forEach { tier ->
                        val tierAchievements = achievements.filter { it.tier == tier }
                        val tierUnlocked = tierAchievements.count { it.isUnlocked }
                        val tierTotal = tierAchievements.size
                        if (tierTotal > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = tier.emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tier.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FocusWhite
                                    )
                                }
                                Text(
                                    text = "$tierUnlocked / $tierTotal",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tierUnlocked == tierTotal) FocusAccent else FocusSecondary
                                )
                            }
                            // Mini progress bar
                            val progress by animateFloatAsState(
                                targetValue = tierUnlocked.toFloat() / tierTotal.coerceAtLeast(1),
                                animationSpec = tween(600),
                                label = "tierProgress"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(FocusPanelBorder)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(getTierColor(tier))
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
    }
}

@Composable
private fun DailyChallengeCard(challenge: DailyChallenge) {
    val progress by animateFloatAsState(
        targetValue = challenge.progressPercent,
        animationSpec = tween(600),
        label = "dailyProgress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (challenge.isCompleted) FocusAccent.copy(alpha = 0.08f) else FocusPanel
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress indicator
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(44.dp)) {
                    drawArc(
                        color = FocusPanelBorder,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 4f, cap = StrokeCap.Round),
                        topLeft = Offset(4f, 4f),
                        size = Size(size.width - 8f, size.height - 8f)
                    )
                    drawArc(
                        color = if (challenge.isCompleted) Color(0xFF9BE564) else Color(0xFF3B82F6),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 4f, cap = StrokeCap.Round),
                        topLeft = Offset(4f, 4f),
                        size = Size(size.width - 8f, size.height - 8f)
                    )
                }
                Text(
                    text = if (challenge.isCompleted) "✓" else challenge.emoji,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (challenge.isCompleted) FocusAccent else FocusWhite
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusWhite
                )
                Text(
                    text = challenge.description,
                    fontSize = 11.sp,
                    color = FocusMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(FocusPanelBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (challenge.isCompleted) FocusAccent
                                else Color(0xFF3B82F6)
                            )
                    )
                }
                Text(
                    text = "${challenge.currentValue} / ${challenge.targetValue}",
                    fontSize = 10.sp,
                    color = FocusMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${challenge.xpReward} XP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B82F6)
                )
                Text(
                    text = "+${challenge.coinReward} 🪙",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusAccent
                )
            }
        }
    }
}

@Composable
private fun WeeklyChallengeCard(challenge: WeeklyChallenge) {
    val progress by animateFloatAsState(
        targetValue = challenge.progressPercent,
        animationSpec = tween(600),
        label = "weeklyProgress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (challenge.isCompleted) Color(0xFFA855F7).copy(alpha = 0.08f) else FocusPanel
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = challenge.emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(challenge.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FocusWhite)
                Text(challenge.description, fontSize = 11.sp, color = FocusMuted, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(FocusPanelBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFA855F7))
                    )
                }
                Text(
                    text = "${challenge.currentValue} / ${challenge.targetValue}",
                    fontSize = 10.sp,
                    color = FocusMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("+${challenge.xpReward} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA855F7))
                Text("+${challenge.coinReward} 🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FocusAccent)
            }
        }
    }
}

@Composable
private fun AchievementBadgeCard(achievement: Achievement) {
    val isUnlocked = achievement.isUnlocked
    val progress by animateFloatAsState(
        targetValue = achievement.progressPercent,
        animationSpec = tween(600),
        label = "achProgress"
    )
    val tierColor = getTierColor(achievement.tier)

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(105.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) tierColor.copy(alpha = 0.08f) else FocusPanel
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) tierColor.copy(alpha = 0.15f)
                        else FocusPanelLight
                    )
                    .border(
                        2.dp,
                        if (isUnlocked) tierColor.copy(alpha = 0.5f) else FocusPanelBorder,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isUnlocked) achievement.emoji else "🔒",
                    fontSize = if (isUnlocked) 22.sp else 18.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = achievement.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) FocusWhite else FocusMuted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp
            )

            // Tier badge
            Text(
                text = "${achievement.tier.emoji} ${achievement.tier.title}",
                fontSize = 9.sp,
                color = tierColor,
                fontWeight = FontWeight.SemiBold
            )

            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(4.dp))
                // Progress mini bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(FocusPanelBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(tierColor.copy(alpha = 0.7f))
                    )
                }
                Text(
                    text = "${(achievement.progressPercent * 100).toInt()}%",
                    fontSize = 9.sp,
                    color = FocusMuted,
                    modifier = Modifier.padding(top = 1.dp)
                )
            } else {
                Text(
                    text = "+${achievement.coinReward} 🪙",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusAccent,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun getTierColor(tier: AchievementTier): Color {
    return when (tier) {
        AchievementTier.BRONZE -> Color(0xFFCD7F32)
        AchievementTier.SILVER -> Color(0xFFC0C0C0)
        AchievementTier.GOLD -> Color(0xFFFFD700)
        AchievementTier.DIAMOND -> Color(0xFF00BFFF)
        AchievementTier.LEGENDARY -> Color(0xFFFF6B35)
    }
}
