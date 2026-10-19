package com.focusflow.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.*
import com.focusflow.app.ui.components.StatCard
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun ProgressScreen(viewModel: FocusFlowViewModel) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val totalHours = String.format("%.1f", viewModel.totalFocusMinutes / 60f)
    val savedHours = String.format("%.1f", viewModel.savedScreenTimeMinutes / 60f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "DOPAMINE DETOX & METRICS",
            color = FocusAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your Reclaimed Time",
            color = FocusWhite,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Date Range Filter Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(FocusPanel)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Today", "This Week", "This Month").forEach { range ->
                val isSelected = viewModel.analyticsDateRange == range
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) FocusAccent else Color.Transparent)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.analyticsDateRange = range
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = range,
                        color = if (isSelected) FocusBackground else FocusSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Executive Productivity Breakdown (Tasks, Habits, Goals & Focus Logs)
        val completedTasksCount = viewModel.tasks.count { it.isCompleted }
        val totalTasksCount = viewModel.tasks.size
        val taskCompletionRate = if (totalTasksCount > 0) (completedTasksCount * 100 / totalTasksCount) else 0
        val overdueTasksCount = viewModel.tasks.count { !it.isCompleted && it.dueDate != null && it.dueDate < System.currentTimeMillis() }

        val activeHabitsCount = viewModel.habits.size
        val habitsDoneToday = viewModel.habits.count { it.isCompletedToday }
        val habitCompletionRate = if (activeHabitsCount > 0) (habitsDoneToday * 100 / activeHabitsCount) else 0
        val bestHabitStreak = viewModel.habits.maxOfOrNull { it.bestStreak } ?: 0

        val completedGoalsCount = viewModel.goals.count { it.isCompleted }
        val activeGoalsCount = viewModel.goals.count { !it.isCompleted }
        val avgGoalProgress = if (viewModel.goals.isNotEmpty()) {
            (viewModel.goals.map { it.progressPercentage }.average()).toInt()
        } else 0

        val sessionLogs = viewModel.focusSessionLogs
        val totalSessionMinutes = if (sessionLogs.isNotEmpty()) sessionLogs.sumOf { it.actualMinutes } else viewModel.totalFocusMinutes
        val avgSessionMinutes = if (sessionLogs.isNotEmpty()) (totalSessionMinutes / sessionLogs.size) else viewModel.customFocusMinutes

        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(22.dp),
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
                            text = "EXECUTIVE SUMMARY (${viewModel.analyticsDateRange.uppercase()})",
                            color = FocusAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Productivity Health Index",
                            color = FocusWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(FocusAccentSoft)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${taskCompletionRate}% Velocity",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4-Column Grid for Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Task Velocity
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(FocusPanelLight)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Tasks", color = FocusSecondary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$completedTasksCount/$totalTasksCount",
                                color = FocusWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (overdueTasksCount > 0) "$overdueTasksCount Overdue" else "0 Overdue",
                                color = if (overdueTasksCount > 0) CategoryUrgent else FocusAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Habit Consistency
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(FocusPanelLight)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Habits", color = FocusSecondary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$habitsDoneToday/$activeHabitsCount",
                                color = FocusWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🔥 $bestHabitStreak Best",
                                color = FocusAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Goal Milestones
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(FocusPanelLight)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Goals", color = FocusSecondary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$completedGoalsCount Done",
                                color = FocusWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$avgGoalProgress% Avg",
                                color = FocusAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Deep Focus Sprints
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(FocusPanelLight)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Sprints", color = FocusSecondary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${sessionLogs.size.coerceAtLeast(viewModel.completedSessions)} Logged",
                                color = FocusWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${avgSessionMinutes}m Avg",
                                color = FocusAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Toward Daily Target Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Target Progress: ${viewModel.totalFocusMinutes} of ${viewModel.dailyFocusGoalMinutes} min",
                        color = FocusSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${((viewModel.totalFocusMinutes.toFloat() / viewModel.dailyFocusGoalMinutes) * 100).toInt().coerceAtMost(100)}%",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val targetRatio = (viewModel.totalFocusMinutes.toFloat() / viewModel.dailyFocusGoalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(FocusPanelLight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(targetRatio)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FocusAccent)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• Task Completion Rate = (Completed Tasks ÷ Total Tasks) × 100%\n• Session durations are verified from local storage records.",
                    color = FocusSecondary.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hero Streak Arena Card (Duolingo & Opal style)
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Streak Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(FocusAccentSoft, CircleShape)
                                .border(1.5.dp, FocusAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔥", fontSize = 26.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "${viewModel.streakDays} Day Streak",
                                color = FocusWhite,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Personal Best: ${viewModel.longestStreakDays} Days",
                                color = FocusSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Streak Freeze Protection Badge
                    Box(
                        modifier = Modifier
                            .background(FocusAccentSoft, RoundedCornerShape(12.dp))
                            .border(1.dp, FocusAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${viewModel.streakFreezes} Freeze",
                                color = FocusAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 7-Day Consistency Visualizer
                val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                val activeDays = viewModel.activeDaysOfWeek
                val todayDayIndex = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK).let {
                    if (it == java.util.Calendar.SUNDAY) 6 else it - 2
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dayNames.forEachIndexed { index, dayLetter ->
                        val isActive = activeDays.getOrElse(index) { false }
                        val isToday = index == todayDayIndex

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayLetter,
                                color = if (isToday) FocusAccent else FocusSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isActive -> FocusAccent
                                            isToday -> FocusAccentSoft
                                            else -> FocusPanelLight
                                        }
                                    )
                                    .then(
                                        if (isToday) Modifier.border(2.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), CircleShape)
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isActive) {
                                    Text("✓", color = FocusBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                } else if (isToday) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(FocusAccent, CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(FocusSecondary.copy(alpha = 0.4f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Streak Dopamine Milestone Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weekly Target: ${minOf(viewModel.streakDays, 7)} of 7 Days Active",
                        color = FocusSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "+15 🪙 Daily Bonus",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(FocusPanelLight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((viewModel.streakDays % 7).let { if (it == 0) 1f else it / 7f })
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(FocusAccent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Focus Flow Index & Dopamine Equilibrium Gauge
        FocusFlowGaugeCard(viewModel.getFocusFlowIndexMetric())

        Spacer(modifier = Modifier.height(18.dp))

        // 4-Stat Metric Matrix
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Regained Time",
                value = "$savedHours hrs",
                subtitle = "Screen time saved",
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Focus Time",
                value = "${viewModel.totalFocusMinutes} min",
                subtitle = "Deep work logged",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Reels Traps",
                value = "${viewModel.reelsBlockedCount}",
                subtitle = "Feeds intercepted",
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Focus Coins",
                value = "${viewModel.focusCoins} \uD83E\uDE99",
                subtitle = "Earned through code",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Screen Time Composition & Category Distribution
        ScreenTimeCompositionCard(viewModel.getScreenTimeComposition())

        Spacer(modifier = Modifier.height(20.dp))

        // Regain Habit Resistance Breakdown: Impulsive vs. Intentional Opens
        RegainHabitResistanceCard(viewModel.habitResistanceStats)

        Spacer(modifier = Modifier.height(20.dp))

        // Weekly Reclaimed Time Bar Chart (Regain & Opal style)
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(22.dp),
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
                            text = "WEEKLY RECLAIMED HOURS",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Screen Time Subtracted",
                            color = FocusWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Avg 1.8h / day",
                        color = FocusSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Chart Bars
                val maxHour = viewModel.weeklyActivity.maxOfOrNull { it.second }?.coerceAtLeast(2.0f) ?: 2.0f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    viewModel.weeklyActivity.forEach { (day, hours) ->
                        val ratio = (hours / maxHour).coerceIn(0.12f, 1f)
                        val isToday = day == "Mon" || day == "Tue"

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${hours}h",
                                color = if (isToday) FocusAccent else FocusSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height((90 * ratio).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(if (isToday) FocusAccent else FocusPanelLight)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = day,
                                color = if (isToday) FocusWhite else FocusSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Advanced Analytic Charts Suite ---
        // 1. Dual-Bar Focus vs Distraction Comparison
        FocusVsDistractionChartCard(viewModel.getWeeklyFocusVsDistraction())

        Spacer(modifier = Modifier.height(20.dp))

        // 2. 7-Day Neurochemical Equilibrium Spline Curve
        DopamineTrendLineChartCard(viewModel.getDopamineTrendHistory())

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Category Attention Donut Allocation Chart
        AppCategoryDonutChartCard(viewModel.getCategoryDistribution())

        Spacer(modifier = Modifier.height(20.dp))

        // 4. 28-Day Habit Consistency Punchcard Heatmap
        FocusHeatmapMatrixCard(viewModel.get28DayFocusHeatmap())

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Short-form Doomscroll Interception Defense Meters
        DoomscrollDefenseRadarCard(viewModel.getDoomscrollDefenseStats())

        Spacer(modifier = Modifier.height(20.dp))

        // Circadian Chronotype & 24h Cognitive Energy Rhythm
        ChronotypeRhythmCard(viewModel.getChronotypeHourlyRhythm())

        Spacer(modifier = Modifier.height(20.dp))

        // Sleep Architecture & Bedtime Curfew Tracking
        SleepArchitectureCard(
            sleepState = viewModel.sleepTrackingState,
            onOpenTracker = { viewModel.showSleepTrackerModal = true }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Distraction Resistance Breakdown (Regain style)
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "DISTRACTION RESISTANCE LOG",
                    color = FocusAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    Triple("📸 Instagram Reels", "7 Interventions", "+21m Reclaimed"),
                    Triple("▶️ YouTube Shorts", "5 Interventions", "+15m Reclaimed"),
                    Triple("👻 Snapchat Spotlight", "4 Interventions", "+12m Reclaimed"),
                    Triple("🌐 Web Doomscrolling", "3 Interventions", "+9m Reclaimed")
                ).forEach { (app, interventions, saved) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(app, color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f).padding(end = 8.dp), maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(interventions, color = FocusSecondary, fontSize = 11.sp, softWrap = false, maxLines = 1)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(saved, color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, softWrap = false, maxLines = 1)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lifetime Reclaimed Impact Matrix
        LifetimeImpactMatrixCard(viewModel.getLifetimeImpactStats())

        Spacer(modifier = Modifier.height(20.dp))

        // Reclaimed Life Value Matrix (Opportunity Cost Calculator)
        val opportunityMetrics = viewModel.getOpportunityCostMetrics()
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FocusAccent.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "OPPORTUNITY COST EQUIVALENT",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "What Your Saved Time Bought You",
                            color = FocusWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("💎 Reclaimed", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    opportunityMetrics.forEach { metric ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(FocusPanelLight)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(metric.emoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = metric.domain,
                                            color = FocusWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = metric.description,
                                            color = FocusSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = metric.value,
                                        color = FocusAccent,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = metric.unit,
                                        color = FocusSecondary,
                                        fontSize = 10.sp,
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Danger Hours Temptation Heatmap
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TEMPTATION RADAR",
                                color = CategoryUrgent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Danger Hours Prediction",
                                color = FocusWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CategoryUrgent.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Peak Alert", color = CategoryUrgent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Based on your open logs, 10:30 PM - 12:30 AM is your highest doomscroll vulnerability window.",
                    color = FocusSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val timeSlots = listOf(
                        Triple("Morning (8-11am)", "Low 🟢", FocusAccent),
                        Triple("Slump (2-4pm)", "Med 🟡", CategoryWork),
                        Triple("Night (10pm-1am)", "High 🔴", CategoryUrgent)
                    )
                    timeSlots.forEach { (slot, risk, color) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FocusPanelLight)
                                .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(slot, color = FocusSecondary, fontSize = 9.sp, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(risk, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Real Device App Screen Time (UsageStatsManager)
        val context = androidx.compose.ui.platform.LocalContext.current
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(22.dp),
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
                            text = "REAL DEVICE USAGE",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Daily Screen Time per App",
                            color = FocusWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!viewModel.hasUsageAccessPermission) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(FocusAccentSoft)
                                .border(1.dp, FocusAccent, RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    com.focusflow.app.util.PermissionHelper.openUsageAccessSettings(context)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Connect 📲", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!viewModel.hasUsageAccessPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FocusAccentSoft)
                            .clickable {
                                com.focusflow.app.util.PermissionHelper.openUsageAccessSettings(context)
                            }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📲 Tap to Grant Usage Access for Real Screen Time",
                            color = FocusAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (viewModel.deviceUsageStats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✨ Zero screen time logged yet today. Pure focus!",
                            color = FocusSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    viewModel.deviceUsageStats.take(6).forEach { stat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stat.iconEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(stat.appName, color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                    Text(stat.category, color = FocusSecondary, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                            Text(stat.formattedDuration, color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, softWrap = false, maxLines = 1)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Focus Coach Real-Time Insights
        AIFocusCoachCard(viewModel.getAIFocusInsights())

        Spacer(modifier = Modifier.height(20.dp))

        // Science-backed Focus Tip Card
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("\uD83E\uDDE0", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Dopamine Rewiring Tip",
                        color = FocusWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A simple 5-second mindful pause interrupts the compulsive habit loop between cue and action, reactivating prefrontal cortex executive control.",
                    color = FocusSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun FocusFlowGaugeCard(metric: FocusFlowIndexMetric) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FocusAccent.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FOCUSNOW INDEX",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Dopamine Equilibrium",
                        color = FocusWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusAccentSoft)
                        .border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "🌿 ${metric.statusTitle}",
                        color = FocusAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(FocusAccentSoft, CircleShape)
                        .border(2.5.dp, FocusAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${metric.overallScore}",
                            color = FocusWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "/ 100",
                            color = FocusSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = metric.statusSubtitle,
                    color = FocusSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            val pillars = listOf(
                Triple("🛡️ Distraction Resistance", metric.distractionResistance, FocusAccent),
                Triple("⚡ Deep Work Intensity", metric.deepWorkIntensity, CategoryWork),
                Triple("🎯 Goal Execution", metric.goalExecution, CategoryStudy)
            )

            pillars.forEach { (label, percent, color) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, color = FocusWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text("$percent%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(FocusPanelLight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(percent / 100f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RegainHabitResistanceCard(stats: HabitResistanceStats) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(22.dp),
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
                        text = "REGAIN HABIT RESISTANCE",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Impulsive vs. Intentional Opens",
                        color = FocusWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(FocusAccentSoft)
                        .border(1.dp, FocusAccent, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${stats.resistanceRate}% Resisted",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dual Progress Bar
            val total = stats.totalAttempts.coerceAtLeast(1)
            val impulseRatio = stats.impulseOpensIntercepted.toFloat() / total
            val intentionalRatio = stats.intentionalPassesGranted.toFloat() / total

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(FocusPanelLight)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (stats.impulseOpensIntercepted > 0) {
                        Box(
                            modifier = Modifier
                                .weight(impulseRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(FocusAccent)
                        )
                    }
                    if (stats.intentionalPassesGranted > 0) {
                        Box(
                            modifier = Modifier
                                .weight(intentionalRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(PriorityMedium)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Split Metric Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FocusPanelLight)
                        .border(1.dp, FocusBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(FocusAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Impulses Stopped", color = FocusSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${stats.impulseOpensIntercepted}", color = FocusWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Walked away at wait screen", color = FocusSecondary, fontSize = 9.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FocusPanelLight)
                        .border(1.dp, FocusBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PriorityMedium)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Intentional Passes", color = FocusSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${stats.intentionalPassesGranted}", color = FocusWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Completed conscious pause", color = FocusSecondary, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Neuro-Insight Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FocusBackground)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🧠", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (stats.totalAttempts == 0) {
                            "Mindful interventions will track your subconscious app opening impulses here."
                        } else if (stats.resistanceRate >= 65) {
                            "Strong dopamine defense! You walked away from ${stats.impulseOpensIntercepted} unconscious habit loops today."
                        } else {
                            "Mindful awareness growing. Every conscious pause trains impulse resistance in your brain."
                        },
                        color = FocusSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenTimeCompositionCard(composition: ScreenTimeComposition) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(22.dp),
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
                        text = "SCREEN TIME COMPOSITION",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Category Distribution",
                        color = FocusWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "↓ ${composition.doomscrollReductionPercent}% Distraction",
                    color = FocusAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(composition.productivePercent)
                        .fillMaxHeight()
                        .background(FocusAccent)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .weight(composition.communicationPercent)
                        .fillMaxHeight()
                        .background(CategoryWork)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .weight(composition.distractingPercent)
                        .fillMaxHeight()
                        .background(CategoryUrgent)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(FocusAccent, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Productive: ${(composition.productivePercent * 100).toInt()}%", color = FocusWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(composition.productiveHours, color = FocusSecondary, fontSize = 10.sp, modifier = Modifier.padding(start = 14.dp))
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(CategoryWork, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Comm: ${(composition.communicationPercent * 100).toInt()}%", color = FocusWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Slack & Docs", color = FocusSecondary, fontSize = 10.sp, modifier = Modifier.padding(start = 14.dp))
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(CategoryUrgent, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Feeds: ${(composition.distractingPercent * 100).toInt()}%", color = FocusWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Saved: ${composition.savedHours}", color = FocusAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 14.dp))
                }
            }
        }
    }
}

@Composable
fun ChronotypeRhythmCard(slots: List<ChronotypeHourlySlot>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(22.dp),
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
                        text = "CIRCADIAN CHRONOTYPE",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "24h Energy & Focus Rhythm",
                        color = FocusWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FocusAccentSoft)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("⚡ 9a - 12p Peak", color = FocusAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val displaySlots = slots.filterIndexed { index, _ -> index % 2 == 0 }
                displaySlots.forEach { slot ->
                    val barHeight = (slot.intensityRatio * 55).coerceAtLeast(4f)
                    val barColor = when {
                        slot.isPeak -> FocusAccent
                        slot.isVulnerable -> CategoryUrgent.copy(alpha = 0.7f)
                        else -> FocusPanelLight
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(barHeight.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(barColor)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = slot.label,
                            color = if (slot.isPeak) FocusAccent else FocusSecondary,
                            fontSize = 9.sp,
                            fontWeight = if (slot.isPeak) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FocusPanelLight)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🧠", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Optimal Focus Window: 9:00 AM – 12:30 PM. Cortisol is naturally elevated. Reserve mornings for deep problem solving.",
                        color = FocusSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LifetimeImpactMatrixCard(stats: LifetimeImpactStats) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "LIFETIME RECLAIMED IMPACT",
                color = FocusAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Cumulative Sanctuary Milestones",
                color = FocusWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FocusPanelLight)
                        .padding(12.dp)
                ) {
                    Column {
                        Text("⏳", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${stats.totalReclaimedHours} hrs", color = FocusAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Reclaimed Focus", color = FocusSecondary, fontSize = 10.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FocusPanelLight)
                        .padding(12.dp)
                ) {
                    Column {
                        Text("🛡️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${stats.shortFormInterceptions}", color = FocusAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Feeds Intercepted", color = FocusSecondary, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FocusPanelLight)
                        .padding(12.dp)
                ) {
                    Column {
                        Text("🪴", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${stats.zenTreesGrown} Bonsais", color = FocusAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Sanctuary Trees", color = FocusSecondary, fontSize = 10.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FocusPanelLight)
                        .padding(12.dp)
                ) {
                    Column {
                        Text("🧠", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${stats.puzzlesAndCodeSolved} Solved", color = FocusAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Code & Arena Puzzles", color = FocusSecondary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AIFocusCoachCard(insights: List<AIFocusInsight>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FocusAccent.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI FOCUS COACH",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Real-Time Behavioral Intelligence",
                        color = FocusWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text("✨ Active", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                insights.forEach { insight ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(FocusPanelLight)
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(insight.emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = insight.title,
                                        color = FocusWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FocusAccentSoft)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = insight.category,
                                        color = FocusAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        softWrap = false
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = insight.message,
                                color = FocusSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FocusPanel)
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎯 Action: ", color = FocusAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = insight.actionableTip,
                                        color = FocusWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
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

@Composable
fun SleepArchitectureCard(
    sleepState: com.focusflow.app.model.SleepTrackingState,
    onOpenTracker: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val stages = sleepState.stages
    val isCurfewActive = sleepState.isCurfewCurrentlyActive()

    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, androidx.compose.ui.graphics.Color(0xFF8B5CF6).copy(alpha = 0.45f), RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "CIRCADIAN SLEEP TRACKING",
                        color = androidx.compose.ui.graphics.Color(0xFFA78BFA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sleep Architecture & Curfew",
                        color = FocusWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFF8B5CF6).copy(alpha = 0.2f))
                        .border(1.dp, androidx.compose.ui.graphics.Color(0xFF8B5CF6).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${sleepState.lastNightScore} Restorative",
                        color = androidx.compose.ui.graphics.Color(0xFFA78BFA),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Duration & Melatonin Defense Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Duration
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanelLight)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Duration", color = FocusSecondary, fontSize = 10.sp)
                        Text(sleepState.lastNightDuration, color = FocusWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Target: 8.0h", color = FocusAccent, fontSize = 9.sp)
                    }
                }

                // Melatonin Protection
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanelLight)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Curfew Shield", color = FocusSecondary, fontSize = 10.sp)
                        Text(if (isCurfewActive) "Active 🌙" else "Guarded 🛡️", color = androidx.compose.ui.graphics.Color(0xFFA78BFA), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(sleepState.formattedCurfewRange(), color = FocusSecondary, fontSize = 9.sp, maxLines = 1)
                    }
                }

                // Doomscroll Traps Blocked
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanelLight)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Traps Intercepted", color = FocusSecondary, fontSize = 10.sp)
                        Text("${sleepState.doomscrollInterceptionsPast11Pm} Night", color = FocusAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Late doomscrolls", color = FocusSecondary, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sleep Stages Stacked Bar
            Text(
                text = "Stages Breakdown: Deep (22%) • REM (26%) • Light (45%) • Awake (7%)",
                color = FocusSecondary,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            val totalMins = stages.totalInBedMinutes.toFloat().coerceAtLeast(1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
            ) {
                Box(modifier = Modifier.weight((stages.deepMinutes / totalMins).coerceAtLeast(0.05f)).background(androidx.compose.ui.graphics.Color(0xFF7C3AED)))
                Box(modifier = Modifier.weight((stages.remMinutes / totalMins).coerceAtLeast(0.05f)).background(androidx.compose.ui.graphics.Color(0xFF3B82F6)))
                Box(modifier = Modifier.weight((stages.lightMinutes / totalMins).coerceAtLeast(0.05f)).background(androidx.compose.ui.graphics.Color(0xFF10B981)))
                Box(modifier = Modifier.weight((stages.awakeMinutes / totalMins).coerceAtLeast(0.05f)).background(androidx.compose.ui.graphics.Color(0xFFF59E0B)))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7-Day Sleep Duration Consistency Mini Chart
            Text(
                text = "7-DAY DURATION CONSISTENCY",
                color = FocusMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                sleepState.weeklySleepHistory.forEach { dayRecord ->
                    val ratio = (dayRecord.durationHours / 9.0f).coerceIn(0.15f, 1f)
                    val isOptimal = dayRecord.durationHours >= 7.5f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = String.format(java.util.Locale.US, "%.1fh", dayRecord.durationHours),
                            color = if (isOptimal) androidx.compose.ui.graphics.Color(0xFFA78BFA) else FocusSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height((45 * ratio).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (isOptimal) androidx.compose.ui.graphics.Color(0xFF8B5CF6) else FocusPanelLight
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayRecord.dayOfWeek.take(1),
                            color = FocusSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button: Open Sleep Tracker & Curfew Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(androidx.compose.ui.graphics.Color(0xFF8B5CF6).copy(alpha = 0.15f))
                    .border(1.dp, androidx.compose.ui.graphics.Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenTracker()
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Adjust Curfew & Log Sleep Session",
                        color = androidx.compose.ui.graphics.Color(0xFFA78BFA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// ADVANCED ANALYTIC CHARTS COMPOSABLES
// ==========================================

@Composable
fun FocusVsDistractionChartCard(data: List<DailyFocusComparison>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FocusAccent.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FOCUS VS DISTRACTION RATIO",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Weekly Dual-Bar Comparison",
                        color = FocusWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(FocusAccent, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Focus", color = FocusSecondary, fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF5C5C), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Distract", color = FocusSecondary, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val maxVal = data.maxOfOrNull { maxOf(it.focusHours, it.distractionHours) }?.coerceAtLeast(6f) ?: 6f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    val focusRatio = (item.focusHours / maxVal).coerceIn(0.08f, 1f)
                    val distractRatio = (item.distractionHours / maxVal).coerceIn(0.08f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Focus Bar
                            Box(
                                modifier = Modifier
                                    .width(9.dp)
                                    .height((90 * focusRatio).dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(FocusAccent, FocusAccentDark)
                                        )
                                    )
                            )
                            // Distraction Bar
                            Box(
                                modifier = Modifier
                                    .width(9.dp)
                                    .height((90 * distractRatio).dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFFF5C5C), Color(0xFF991B1B))
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.day,
                            color = if (item.day == "Sun") FocusWhite else FocusSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (item.day == "Sun") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val totalFocus = data.sumOf { it.focusHours.toDouble() }
            val totalDistract = data.sumOf { it.distractionHours.toDouble() }
            val ratioPercent = if (totalFocus + totalDistract > 0) ((totalFocus / (totalFocus + totalDistract)) * 100).toInt() else 80

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FocusPanelLight)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Net Flow Ratio: $ratioPercent% deep work focus",
                        color = FocusSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "+${String.format(java.util.Locale.US, "%.1fh", totalFocus - totalDistract)} surplus",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DopamineTrendLineChartCard(points: List<DopamineTrendPoint>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FocusAccent.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEUROCHEMICAL RESILIENCE",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "7-Day Dopamine Equilibrium",
                        color = FocusWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FocusAccentSoft)
                        .border(1.dp, FocusAccent, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚡ ${points.lastOrNull()?.score ?: 94}/100",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val accentColor = FocusAccent
            val gridColor = FocusPanelLight

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (points.isEmpty()) return@Canvas
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 20.dp.toPx()
                    val chartHeight = height - paddingBottom
                    val stepX = width / (points.size - 1).coerceAtLeast(1)

                    // Baseline 50 reference line
                    val baselineY = chartHeight * 0.5f
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, baselineY),
                        end = Offset(width, baselineY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Compute points
                    val screenPoints = points.mapIndexed { idx, pt ->
                        val normY = ((pt.score - 40f) / 60f).coerceIn(0.05f, 0.95f)
                        val x = idx * stepX
                        val y = chartHeight * (1f - normY)
                        Offset(x, y)
                    }

                    // Build path for smooth curve
                    val linePath = Path().apply {
                        moveTo(screenPoints.first().x, screenPoints.first().y)
                        for (i in 0 until screenPoints.size - 1) {
                            val p0 = screenPoints[i]
                            val p1 = screenPoints[i + 1]
                            val controlX1 = (p0.x + p1.x) / 2f
                            cubicTo(controlX1, p0.y, controlX1, p1.y, p1.x, p1.y)
                        }
                    }

                    // Build gradient fill path
                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(screenPoints.last().x, chartHeight)
                        lineTo(screenPoints.first().x, chartHeight)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    // Draw stroke
                    drawPath(
                        path = linePath,
                        color = accentColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw points
                    screenPoints.forEachIndexed { i, pt ->
                        val isLast = i == screenPoints.lastIndex
                        drawCircle(
                            color = FocusPanel,
                            radius = if (isLast) 6.dp.toPx() else 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = accentColor,
                            radius = if (isLast) 4.5.dp.toPx() else 2.5.dp.toPx(),
                            center = pt
                        )
                    }
                }

                // X-Axis Labels Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    points.forEach { pt ->
                        Text(
                            text = pt.label,
                            color = if (pt.label == "Sun") FocusWhite else FocusSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (pt.label == "Sun") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "📈 Baseline sensitivity is up +36% this week as compulsive short-form cravings stabilized.",
                color = FocusSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun AppCategoryDonutChartCard(categories: List<CategoryShare>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FocusAccent.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "ATTENTION ALLOCATION",
                color = FocusAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Time by Life Category",
                color = FocusWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Canvas
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        val arcSize = size.width - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        var startAngle = -90f

                        categories.forEach { cat ->
                            val sweepAngle = cat.percentage * 360f
                            drawArc(
                                color = Color(cat.colorHex),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle - 2f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("100%", color = FocusWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Flow", color = FocusSecondary, fontSize = 9.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Breakdown list
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(cat.colorHex), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${cat.iconEmoji} ${cat.name}",
                                    color = FocusWhite,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "${(cat.percentage * 100).toInt()}%",
                                color = FocusSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FocusHeatmapMatrixCard(days: List<HeatmapDay>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FocusAccent.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HABIT CONSISTENCY PUNCHCARD",
                        color = FocusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "28-Day Deep Flow Matrix",
                        color = FocusWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text("🔥 24/28 Active", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Weeks x 7 Days Grid
            val weeks = days.chunked(7)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                weeks.forEachIndexed { weekIdx, weekDays ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { day ->
                            val cellColor = when (day.intensityLevel) {
                                4 -> Color(0xFF10B981) // Deep emerald
                                3 -> Color(0xFF34D399) // Medium emerald
                                2 -> Color(0xFF6EE7B7).copy(alpha = 0.7f)
                                1 -> Color(0xFFA7F3D0).copy(alpha = 0.4f)
                                else -> FocusPanelLight
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellColor)
                                    .then(
                                        if (day.isToday) Modifier.border(2.dp, FocusWhite, RoundedCornerShape(8.dp))
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${day.dayOfMonth}",
                                    color = if (day.intensityLevel >= 2) Color(0xFF064E3B) else FocusSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (day.isToday) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less Focus", color = FocusMuted, fontSize = 10.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf(
                        FocusPanelLight,
                        Color(0xFFA7F3D0).copy(alpha = 0.4f),
                        Color(0xFF6EE7B7).copy(alpha = 0.7f),
                        Color(0xFF34D399),
                        Color(0xFF10B981)
                    ).forEach { c ->
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(c))
                    }
                }
                Text("Heroic Flow (4h+)", color = FocusAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DoomscrollDefenseRadarCard(stats: List<DoomscrollDefenseStat>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanel),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FocusAccent.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "INTERCEPTION DEFENSE METERS",
                            color = FocusAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Algorithm Defense by App",
                            color = FocusWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FocusAccentSoft)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Active Shields", color = FocusAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                stats.forEach { stat ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stat.iconEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stat.platform,
                                    color = FocusWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${stat.count} blocks",
                                    color = FocusSecondary,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stat.savedHours,
                                    color = FocusAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Progress track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(FocusPanelLight)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(stat.percent.coerceIn(0.08f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(stat.colorHex))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(135.dp))
    }
}