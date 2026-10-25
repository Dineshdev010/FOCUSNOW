package com.focusflow.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.focusflow.app.model.BlockedApp
import com.focusflow.app.model.CategoryBudget
import com.focusflow.app.model.FocusSchedule
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun AppLimitsAndSchedulesScreen(
    viewModel: FocusFlowViewModel,
    initialTab: Int = 0,
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddAppDialog by remember { mutableStateOf(false) }

    val activeSchedule = viewModel.getCurrentlyActiveSchedule()
    val totalBudgetMinutes = viewModel.blockedApps.sumOf { it.dailyLimitMinutes }
    val totalUsedMinutes = viewModel.blockedApps.sumOf { it.usedMinutesToday }
    val overallBudgetRatio = if (totalBudgetMinutes > 0) {
        (totalUsedMinutes.toFloat() / totalBudgetMinutes.toFloat()).coerceIn(0f, 1f)
    } else 0f

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
        // --- TOP NAVIGATION HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "App Budgets & Schedules",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusWhite
                )
                Text(
                    text = "Enforce boundaries & automated focus",
                    fontSize = 11.sp,
                    color = FocusMuted
                )
            }

            // Quick Add Action
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FocusAccent.copy(alpha = 0.16f))
                    .border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (selectedTabIndex == 1) {
                            viewModel.showAddScheduleModal = true
                        } else {
                            showAddAppDialog = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 20.sp, color = FocusAccent, fontWeight = FontWeight.Bold)
            }
        }

        // --- SCROLLABLE CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // --- HERO OVERVIEW BUDGET CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FocusPanel)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(FocusAccent.copy(alpha = 0.14f), FocusPanel)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Today's App Time Budget",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FocusWhite
                                )
                                Text(
                                    text = "${totalUsedMinutes}m used • ${totalBudgetMinutes}m total daily cap",
                                    fontSize = 12.sp,
                                    color = FocusSecondary
                                )
                            }

                            // Radial Percentage Gauge
                            Box(
                                modifier = Modifier.size(54.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(50.dp)) {
                                    val strokeWidth = 5.dp.toPx()
                                    drawCircle(
                                        color = FocusPanelBorder,
                                        style = Stroke(strokeWidth)
                                    )
                                    val sweep = overallBudgetRatio * 360f
                                    drawArc(
                                        brush = Brush.sweepGradient(
                                            listOf(FocusAccent, PriorityMedium, PriorityHigh)
                                        ),
                                        startAngle = -90f,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                    )
                                }
                                Text(
                                    text = "${(overallBudgetRatio * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FocusWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Active Schedule Banner
                        if (activeSchedule != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FocusAccent.copy(alpha = 0.18f))
                                    .border(1.dp, FocusAccent, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(activeSchedule.emoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Active: ${activeSchedule.name}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FocusAccent
                                            )
                                            Text(
                                                text = "${activeSchedule.formattedTimeRange()} (${activeSchedule.daysSummary})",
                                                fontSize = 10.sp,
                                                color = FocusWhite.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "BLOCKING ON 🛡️",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FocusAccent
                                    )
                                }
                            }
                        } else {
                            val nextSched = viewModel.focusSchedules.firstOrNull { it.isEnabled }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FocusPanelLight)
                                    .border(1.dp, FocusPanelBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(nextSched?.emoji ?: "⏰", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (nextSched != null) {
                                            "Next focus window: ${nextSched.name} (${nextSched.formattedTimeRange()})"
                                        } else {
                                            "No active focus window right now. Tap + to schedule one!"
                                        },
                                        fontSize = 11.sp,
                                        color = FocusSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3-TAB SEGMENT CONTROLLER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FocusPanel)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    "📱 App Limits (${viewModel.blockedApps.size})",
                    "⏰ Windows (${viewModel.focusSchedules.size})",
                    "📊 Categories"
                )
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) FocusAccent else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedTabIndex = index
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) FocusBackground else FocusSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- TAB CONTENT ---
            when (selectedTabIndex) {
                0 -> {
                    // TAB 1: PER-APP TIME LIMITS & RADIAL USAGE RINGS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily App Time Caps",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusWhite
                        )

                        Text(
                            text = "+ Add App",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusAccent,
                            modifier = Modifier.clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showAddAppDialog = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search tracked app...", color = FocusMuted, fontSize = 12.sp) },
                        singleLine = true,
                        leadingIcon = { Text("🔍", fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FocusWhite,
                            unfocusedTextColor = FocusWhite,
                            focusedBorderColor = FocusAccent,
                            unfocusedBorderColor = FocusPanelBorder,
                            cursorColor = FocusAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredApps = viewModel.blockedApps.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredApps.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = FocusPanel)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📱", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No apps found",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FocusWhite
                                )
                                Text(
                                    text = "Tap '+ Add App' to track custom screen time limits",
                                    fontSize = 11.sp,
                                    color = FocusMuted
                                )
                            }
                        }
                    } else {
                        filteredApps.forEach { app ->
                            AppLimitGaugeCard(
                                app = app,
                                onEdit = {
                                    viewModel.editingAppLimitItem = app
                                },
                                onToggleBlock = { isBlocked ->
                                    viewModel.toggleAppBlockedState(app.packageName, isBlocked)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                1 -> {
                    // TAB 2: SCHEDULED FOCUS WINDOWS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Automated Focus Windows",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusWhite
                        )

                        Text(
                            text = "+ New Window",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusAccent,
                            modifier = Modifier.clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.showAddScheduleModal = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Smart Automation Presets Carousel
                    Text(
                        text = "⚡ Quick Preset Windows",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FocusSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            Triple("Deep Work", "💼", "Mon-Fri • 09:00 - 17:00"),
                            Triple("Study Sprint", "📚", "Daily • 18:30 - 21:00"),
                            Triple("Digital Curfew", "🌙", "Daily • 22:30 - 07:00"),
                            Triple("Morning Detox", "🧘", "Mon-Sat • 06:00 - 08:30")
                        )

                        presets.forEach { (name, emoji, timing) ->
                            Card(
                                modifier = Modifier
                                    .width(160.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        when (name) {
                                            "Deep Work" -> viewModel.addCustomSchedule(name, emoji, 9, 0, 17, 0, "Mon - Fri")
                                            "Study Sprint" -> viewModel.addCustomSchedule(name, emoji, 18, 30, 21, 0, "Daily")
                                            "Digital Curfew" -> viewModel.addCustomSchedule(name, emoji, 22, 30, 7, 0, "Daily")
                                            "Morning Detox" -> viewModel.addCustomSchedule(name, emoji, 6, 0, 8, 30, "Mon - Sat")
                                        }
                                        Toast.makeText(context, "Added $name schedule! ✨", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = FocusPanel)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(emoji, fontSize = 20.sp)
                                        Text("+ Add", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FocusAccent)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FocusWhite
                                    )
                                    Text(
                                        text = timing,
                                        fontSize = 10.sp,
                                        color = FocusMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Active Schedule Windows",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FocusWhite
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    viewModel.focusSchedules.forEach { schedule ->
                        val isActive = schedule.isCurrentlyActive()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) FocusPanelLight else FocusPanel
                            ),
                            border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, FocusAccent.copy(alpha = 0.6f)) else null
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isActive) FocusAccentSoft else FocusPanelLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(schedule.emoji, fontSize = 20.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = schedule.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FocusWhite
                                                )
                                                if (isActive) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(FocusAccent)
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("ACTIVE", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = FocusBackground)
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${schedule.formattedTimeRange()} • ${schedule.daysSummary}",
                                                fontSize = 11.sp,
                                                color = FocusSecondary
                                            )
                                        }
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
                                            uncheckedThumbColor = FocusBackground,
                                            uncheckedTrackColor = FocusBorder
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val appsCountText = if (schedule.targetAppPackages.isNotEmpty()) {
                                        "📱 ${schedule.targetAppPackages.size} Apps Turned ON"
                                    } else {
                                        "📱 All Distractions Blocked"
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = appsCountText,
                                            fontSize = 10.sp,
                                            color = FocusAccent
                                        )
                                        if (schedule.isAlarmEnabled) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(FocusAccent.copy(alpha = 0.15f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "⏰ ${schedule.alarmSound}",
                                                    fontSize = 9.sp,
                                                    color = FocusAccent,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = "Edit ⚙️",
                                            fontSize = 11.sp,
                                            color = FocusAccent,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.openScheduleForEditing(schedule)
                                            }
                                        )
                                        Text(
                                            text = "Delete",
                                            fontSize = 11.sp,
                                            color = PriorityHigh,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.deleteFocusSchedule(schedule.id)
                                                Toast.makeText(context, "Schedule removed", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 3: CATEGORY-LEVEL SHARED BUDGETS
                    Text(
                        text = "Category Shared Time Budgets",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FocusWhite
                    )
                    Text(
                        text = "Set global daily caps shared across entire groups of apps",
                        fontSize = 12.sp,
                        color = FocusMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    viewModel.categoryBudgets.forEach { budget ->
                        val matchingApps = viewModel.blockedApps.filter {
                            it.category.contains(budget.category, ignoreCase = true) ||
                            (budget.category.contains("Social", ignoreCase = true) && it.category.contains("Community", ignoreCase = true))
                        }
                        val categoryUsed = matchingApps.sumOf { it.usedMinutesToday }
                        val ratio = (categoryUsed.toFloat() / budget.dailyLimitMinutes.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = FocusPanel)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(budget.emoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = budget.category,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FocusWhite
                                            )
                                            Text(
                                                text = "${matchingApps.size} apps connected",
                                                fontSize = 11.sp,
                                                color = FocusSecondary
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = budget.isEnabled,
                                        onCheckedChange = { enabled ->
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.updateCategoryBudget(budget.category, budget.dailyLimitMinutes, enabled)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = FocusBackground,
                                            checkedTrackColor = FocusAccent,
                                            uncheckedThumbColor = FocusBackground,
                                            uncheckedTrackColor = FocusBorder
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Usage Progress
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(FocusPanelLight)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(ratio)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                if (categoryUsed >= budget.dailyLimitMinutes) PriorityHigh
                                                else FocusAccent
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${categoryUsed}m / ${budget.dailyLimitMinutes}m used",
                                        fontSize = 11.sp,
                                        color = if (categoryUsed >= budget.dailyLimitMinutes) PriorityHigh else FocusWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    // Limit selector buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(30, 45, 60, 90).forEach { mins ->
                                            val isSel = budget.dailyLimitMinutes == mins
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) FocusAccent else FocusPanelLight)
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        viewModel.updateCategoryBudget(budget.category, mins, budget.isEnabled)
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "${mins}m",
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSel) FocusBackground else FocusWhite
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

            Spacer(modifier = Modifier.height(44.dp))
        }
    }

    // --- CUSTOM APP TIME LIMIT DIALOG ---
    val appToEdit = viewModel.editingAppLimitItem
    if (appToEdit != null) {
        CustomAppLimitDialog(
            app = appToEdit,
            onSave = { newLimit, maxOpens, isHardLock ->
                viewModel.updateAppDailyLimit(appToEdit.packageName, newLimit, maxOpens, isHardLock)
                viewModel.editingAppLimitItem = null
                Toast.makeText(context, "Saved ${newLimit}m cap for ${appToEdit.name}! ⏱️", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                viewModel.editingAppLimitItem = null
            }
        )
    }

    // --- ADD APP DIALOG ---
    if (showAddAppDialog) {
        AddAppToLimitsDialog(
            existingPackages = viewModel.blockedApps.map { it.packageName }.toSet(),
            onAddApp = { newApp ->
                viewModel.addOrUpdateBlockedApp(newApp)
                showAddAppDialog = false
                Toast.makeText(context, "Added ${newApp.name} to tracked limits! 🎯", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAddAppDialog = false }
        )
    }

    // --- ADD SCHEDULE DIALOG ---
    if (viewModel.showAddScheduleModal) {
        AddScheduleDialog(
            viewModel = viewModel,
            onClose = {
                viewModel.showAddScheduleModal = false
                viewModel.editingScheduleItem = null
            }
        )
    }
}

// --- APP LIMIT GAUGE CARD COMPONENT ---
@Composable
private fun AppLimitGaugeCard(
    app: BlockedApp,
    onEdit: () -> Unit,
    onToggleBlock: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val usedRatio = (app.usedMinutesToday.toFloat() / app.dailyLimitMinutes.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val isExceeded = app.usedMinutesToday >= app.dailyLimitMinutes
    val remainingMins = (app.dailyLimitMinutes - app.usedMinutesToday).coerceAtLeast(0)

    val gaugeColor = when {
        isExceeded -> PriorityHigh
        usedRatio >= 0.75f -> PriorityMedium
        else -> FocusAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onEdit()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FocusPanel)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Radial Usage Ring
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(48.dp)) {
                    val stroke = 4.dp.toPx()
                    drawCircle(color = FocusPanelBorder, style = Stroke(stroke))
                    drawArc(
                        color = gaugeColor,
                        startAngle = -90f,
                        sweepAngle = usedRatio * 360f,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
                Text(app.iconEmoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // App Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FocusWhite
                    )
                    if (app.isHardLock) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PriorityHigh.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("STRICT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = PriorityHigh)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${app.usedMinutesToday}m / ${app.dailyLimitMinutes}m used • ${(usedRatio * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = gaugeColor
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isExceeded) "Limit Exceeded" else "${remainingMins}m left",
                        fontSize = 10.sp,
                        color = if (isExceeded) PriorityHigh else FocusSecondary
                    )
                    Text("•", fontSize = 10.sp, color = FocusMuted)
                    Text(
                        text = "${app.opensToday} opens today",
                        fontSize = 10.sp,
                        color = FocusMuted
                    )
                }
            }

            // Quick Edit Chip + Toggle Switch
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Switch(
                    checked = app.isBlocked,
                    onCheckedChange = { onToggleBlock(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = FocusBackground,
                        checkedTrackColor = if (isExceeded) PriorityHigh else FocusAccent,
                        uncheckedThumbColor = FocusBackground,
                        uncheckedTrackColor = FocusBorder
                    )
                )
                Text(
                    text = "Edit Cap ⚙️",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusAccent
                )
            }
        }
    }
}

// --- CUSTOM APP TIME LIMIT DIALOG ---
@Composable
fun CustomAppLimitDialog(
    app: BlockedApp,
    onSave: (Int, Int, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var limitMinutes by remember { mutableIntStateOf(app.dailyLimitMinutes) }
    var maxOpens by remember { mutableIntStateOf(app.maxOpensPerDay) }
    var isHardLock by remember { mutableStateOf(app.isHardLock) }

    val presetChips = listOf(10, 15, 20, 30, 45, 60, 90, 120)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = FocusPanel)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(app.iconEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${app.name} Limit",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusWhite
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FocusPanelLight)
                            .clickable { onDismiss() }
                            .padding(6.dp)
                    ) {
                        Text("✕", fontSize = 12.sp, color = FocusMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Stepper for Limit Minutes
                Text(
                    text = "${limitMinutes} minutes",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusAccent
                )
                Text(
                    text = "Daily Screen Time Allowance",
                    fontSize = 11.sp,
                    color = FocusSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stepper Buttons (-5m / +5m / -15m / +15m)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(FocusPanelLight)
                            .clickable { limitMinutes = (limitMinutes - 5).coerceAtLeast(5) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("-5m", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FocusWhite)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(FocusPanelLight)
                            .clickable { limitMinutes = (limitMinutes - 15).coerceAtLeast(5) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("-15m", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FocusWhite)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(FocusPanelLight)
                            .clickable { limitMinutes = (limitMinutes + 5).coerceAtMost(360) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("+5m", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FocusWhite)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(FocusPanelLight)
                            .clickable { limitMinutes = (limitMinutes + 15).coerceAtMost(360) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("+15m", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FocusWhite)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetChips.forEach { mins ->
                        val isSel = limitMinutes == mins
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) FocusAccent else FocusPanelLight)
                                .clickable { limitMinutes = mins }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${mins}m",
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) FocusBackground else FocusWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Max Opens Quota
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanelLight)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Max Daily Opens", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FocusWhite)
                        Text("Prevent compulsive checking", fontSize = 10.sp, color = FocusMuted)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "-",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusAccent,
                            modifier = Modifier
                                .clickable { maxOpens = (maxOpens - 5).coerceAtLeast(5) }
                                .padding(horizontal = 8.dp)
                        )
                        Text("$maxOpens", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FocusWhite)
                        Text(
                            text = "+",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusAccent,
                            modifier = Modifier
                                .clickable { maxOpens = (maxOpens + 5).coerceAtMost(100) }
                                .padding(horizontal = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Strict Hard Lock Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanelLight)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Strict Hard Lockdown", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FocusWhite)
                        Text("Zero extra time once limit is reached", fontSize = 10.sp, color = FocusMuted)
                    }
                    Switch(
                        checked = isHardLock,
                        onCheckedChange = { isHardLock = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FocusBackground,
                            checkedTrackColor = PriorityHigh,
                            uncheckedThumbColor = FocusBackground,
                            uncheckedTrackColor = FocusBorder
                        )
                    )
                }

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
                        onClick = { onSave(limitMinutes, maxOpens, isHardLock) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Limit", color = FocusBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- ADD APP TO LIMITS DIALOG ---
@Composable
fun AddAppToLimitsDialog(
    existingPackages: Set<String>,
    onAddApp: (BlockedApp) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val candidates = remember {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveList = try { pm.queryIntentActivities(launcherIntent, 0) } catch (_: Exception) { emptyList() }
        val resolvePkgs = resolveList.mapNotNull { it.activityInfo?.packageName?.lowercase() }.toSet()

        val popular = listOf(
            BlockedApp("Instagram", "com.instagram.android", "Social Media", "📸", dailyLimitMinutes = 20),
            BlockedApp("YouTube", "com.google.android.youtube", "Video", "▶️", dailyLimitMinutes = 45),
            BlockedApp("TikTok", "com.zhiliaoapp.musically", "Short Video", "🎵", dailyLimitMinutes = 15),
            BlockedApp("Twitter / X", "com.twitter.android", "Social Media", "🐦", dailyLimitMinutes = 20),
            BlockedApp("Snapchat", "com.snapchat.android", "Social Media", "👻", dailyLimitMinutes = 15),
            BlockedApp("Reddit", "com.reddit.frontpage", "Community", "🤖", dailyLimitMinutes = 25),
            BlockedApp("Netflix", "com.netflix.mediaclient", "Video", "🍿", dailyLimitMinutes = 60),
            BlockedApp("Discord", "com.discord", "Chat", "💬", dailyLimitMinutes = 30),
            BlockedApp("Pinterest", "com.pinterest", "Media", "📌", dailyLimitMinutes = 20),
            BlockedApp("Twitch", "tv.twitch.android.app", "Streaming", "🎮", dailyLimitMinutes = 30),
            BlockedApp("Chrome Browser", "com.android.chrome", "Web", "🌐", dailyLimitMinutes = 45),
            BlockedApp("Spotify", "com.spotify.music", "Audio", "🎧", dailyLimitMinutes = 90)
        ).filter { app ->
            resolvePkgs.contains(app.packageName.lowercase())
        }

        val foundInstalled = mutableListOf<BlockedApp>()
        val popularPkgs = popular.map { it.packageName.lowercase() }.toSet()

        for (info in resolveList) {
            val pkg = info.activityInfo?.packageName ?: continue
            if (pkg == context.packageName || popularPkgs.contains(pkg.lowercase()) || existingPackages.contains(pkg)) continue
            val label = try { info.loadLabel(pm)?.toString() ?: pkg } catch (_: Exception) { pkg }
            val emoji = when {
                label.contains("camera", true) -> "📷"
                label.contains("gallery", true) || label.contains("photos", true) -> "🖼️"
                label.contains("clock", true) -> "⏰"
                label.contains("browser", true) -> "🌐"
                label.contains("game", true) -> "🎮"
                label.contains("music", true) -> "🎵"
                label.contains("mail", true) -> "✉️"
                else -> "📱"
            }
            foundInstalled.add(
                BlockedApp(
                    name = label,
                    packageName = pkg,
                    category = "Installed Apps",
                    iconEmoji = emoji,
                    dailyLimitMinutes = 30
                )
            )
        }
        (popular + foundInstalled).filterNot { existingPackages.contains(it.packageName) }
    }

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
                    .padding(20.dp)
            ) {
                Text(
                    text = "Add App to Daily Limits",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusWhite
                )
                Text(
                    text = "Choose an app to start enforcing screen time caps",
                    fontSize = 11.sp,
                    color = FocusMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (candidates.isEmpty()) {
                        Text(
                            text = "All popular apps are already being tracked!",
                            color = FocusSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        candidates.forEach { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FocusPanelLight)
                                    .clickable { onAddApp(app) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(app.iconEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(app.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FocusWhite)
                                        Text("${app.category} • Default ${app.dailyLimitMinutes}m", fontSize = 10.sp, color = FocusMuted)
                                    }
                                }
                                Text("+ Track", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FocusAccent)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", color = FocusSecondary)
                }
            }
        }
    }
}
