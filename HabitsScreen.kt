package com.focusflow.app.ui.screens

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.HabitItem
import com.focusflow.app.ui.components.CheckmarkIcon
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HabitsScreen(viewModel: FocusFlowViewModel) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val habits = viewModel.habits
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Weekly day abbreviations for the streak summary
    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NEURO-ROUTINES",
                    color = FocusAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Habit Streaks",
                    color = FocusWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.showCreateHabitDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text("+ Add Habit", color = FocusBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Habit Overview Stats Card
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val completedTodayCount = habits.count { it.completedDates.contains(todayStr) }
                val maxStreak = habits.maxOfOrNull { it.currentStreak } ?: 0

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$completedTodayCount / ${habits.size}", color = FocusAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Today's Progress", color = FocusSecondary, fontSize = 11.sp)
                }
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(FocusBorder)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥 $maxStreak d", color = FocusWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Best Active Streak", color = FocusSecondary, fontSize = 11.sp)
                }
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(FocusBorder)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val rate = if (habits.isNotEmpty()) ((completedTodayCount.toFloat() / habits.size) * 100).toInt() else 0
                    Text("$rate%", color = FocusAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Daily Rate", color = FocusSecondary, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Habits List or Empty State
        if (habits.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚡", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Build Your First Keystone Habit",
                        color = FocusWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Consistent mini-routines rewire neural pathways and create lasting prefrontal momentum.",
                        color = FocusSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { viewModel.showCreateHabitDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("+ Create First Habit", color = FocusBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            habits.forEach { habit ->
                val isCompletedToday = habit.completedDates.contains(todayStr)

                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkmark button to toggle completion for today
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isCompletedToday) FocusAccent else FocusPanel)
                                    .border(1.5.dp, if (isCompletedToday) FocusAccent else FocusBorder, CircleShape)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.toggleHabitToday(habit.id)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompletedToday) {
                                    CheckmarkIcon()
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = habit.title,
                                    color = FocusWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (habit.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = habit.description,
                                        color = FocusSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Streak Flame Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (habit.currentStreak > 0) FocusYellow.copy(alpha = 0.15f) else FocusPanel)
                                    .border(1.dp, if (habit.currentStreak > 0) FocusYellow.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "🔥 ${habit.currentStreak}d",
                                    color = if (habit.currentStreak > 0) FocusYellow else FocusSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Delete Habit Action
                            Text(
                                text = "🗑️",
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                        viewModel.deleteHabit(habit.id)
                                    }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Weekly History Dots Row (Last 7 Days)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val cal = Calendar.getInstance()
                            // Get past 7 dates
                            val past7Days = (6 downTo 0).map { daysAgo ->
                                val c = Calendar.getInstance()
                                c.add(Calendar.DAY_OF_YEAR, -daysAgo)
                                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
                                val dayLetter = SimpleDateFormat("E", Locale.US).format(c.time).take(1)
                                val isDone = habit.completedDates.contains(dateStr)
                                val isCurToday = (daysAgo == 0)
                                Triple(dayLetter, isDone, isCurToday)
                            }

                            past7Days.forEach { (dayLetter, isDone, isCurToday) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayLetter,
                                        color = if (isCurToday) FocusAccent else FocusSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = if (isCurToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (isDone) FocusAccent else FocusPanel)
                                            .border(1.dp, if (isCurToday) FocusAccent else Color.Transparent, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(44.dp))
    }

    // Modal: Create Habit Dialog
    if (viewModel.showCreateHabitDialog) {
        CreateHabitDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showCreateHabitDialog = false }
        )
    }
}

@Composable
fun CreateHabitDialog(
    viewModel: FocusFlowViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetDays by remember { mutableIntStateOf(7) }
    var nameError by remember { mutableStateOf<String?>(null) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onDismiss() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .padding(bottom = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("NEW HABIT", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("Form a Neuro-Routine", color = FocusWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(FocusPanelLight)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = FocusSecondary, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text("Habit Name *", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    placeholder = { Text("e.g., Read 20 pages or Meditate", color = FocusSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (nameError != null) FocusRed else FocusAccent,
                        unfocusedBorderColor = if (nameError != null) FocusRed else FocusBorder,
                        focusedTextColor = FocusWhite,
                        unfocusedTextColor = FocusWhite,
                        focusedContainerColor = FocusPanelLight,
                        unfocusedContainerColor = FocusPanelLight
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null
                )
                if (nameError != null) {
                    Text(nameError!!, color = FocusRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Description / Intent (Optional)", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Why this routine matters for your flow...", color = FocusSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusAccent,
                        unfocusedBorderColor = FocusBorder,
                        focusedTextColor = FocusWhite,
                        unfocusedTextColor = FocusWhite,
                        focusedContainerColor = FocusPanelLight,
                        unfocusedContainerColor = FocusPanelLight
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Weekly Frequency", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(7 to "Everyday", 5 to "Weekdays (5d)", 3 to "3x / Week").forEach { (days, label) ->
                        val isSelected = targetDays == days
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                                .border(1.dp, if (isSelected) FocusAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { targetDays = days }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) FocusAccent else FocusSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = "Please enter a habit name"
                            return@Button
                        }
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.addHabit(name, description, targetDays)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("+ Save Routine", color = FocusBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
