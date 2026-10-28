package com.focusflow.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

    Box(modifier = Modifier.fillMaxSize()) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NEURO-ROUTINES",
                    color = FocusAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Habit Streaks",
                    color = FocusWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.showCreateHabitDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("+ Add Habit", color = FocusBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (habit.emoji.isNotBlank()) {
                                        Text(
                                            text = habit.emoji,
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                    }
                                    Text(
                                        text = habit.title,
                                        color = FocusWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
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
            if (habits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusPanel.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, FocusBorder.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showCreateHabitDialog = true
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("+ Add Another Habit Routine", color = FocusAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }

    if (viewModel.showCreateHabitDialog) {
        CreateHabitDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showCreateHabitDialog = false }
        )
    }
}
}

@Composable
fun CreateHabitDialog(
    viewModel: FocusFlowViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    BackHandler { onDismiss() }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("⚡") }
    var targetDays by remember { mutableIntStateOf(7) }
    var nameError by remember { mutableStateOf<String?>(null) }

    val emojis = listOf("⚡", "💧", "📚", "🏃", "🧘", "💻", "🍎", "💤", "🎯", "🏋️", "✍️", "🌿")

    val quickSuggestions = listOf(
        Triple("💧", "Drink 2L Water", 7),
        Triple("📚", "Read 15 Mins", 7),
        Triple("🧘", "5m Mindful Breath", 7),
        Triple("🏃", "Morning Walk / Run", 5),
        Triple("💻", "Deep Work Sprint", 5),
        Triple("💤", "Sleep Before 11 PM", 7)
    )

    fun performSave() {
        if (name.isBlank()) {
            nameError = "Please enter a habit name"
            return
        }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.addHabit(
            title = name.trim(),
            description = description.trim(),
            targetDaysPerWeek = targetDays,
            emoji = selectedEmoji
        )
        Toast.makeText(context, "Routine \"${name.trim()}\" added! ⚡", Toast.LENGTH_SHORT).show()
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, FocusBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* consume clicks */ }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header (Fixed at top)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("NEW HABIT ROUTINE", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("Form a Daily Ritual", color = FocusWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Area (gracefully contracts when keyboard is open)
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Quick Suggestions Chips
                    Text("Quick Suggestions", color = FocusSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickSuggestions.size) { idx ->
                            val (sugEmoji, sugTitle, sugDays) = quickSuggestions[idx]
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FocusPanelLight)
                                    .border(1.dp, FocusBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedEmoji = sugEmoji
                                        name = sugTitle
                                        targetDays = sugDays
                                        nameError = null
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text("$sugEmoji $sugTitle", color = FocusWhite, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Routine Icon", color = FocusWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(emojis.size) { idx ->
                            val emo = emojis[idx]
                            val isSelected = selectedEmoji == emo
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                                    .border(
                                        1.5.dp,
                                        if (isSelected) FocusAccent else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedEmoji = emo
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emo, fontSize = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Habit Name *", color = FocusWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = null
                        },
                        placeholder = { Text("e.g., Read 20 pages or Meditate", color = FocusSecondary, fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { performSave() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (nameError != null) FocusRed else FocusAccent,
                            unfocusedBorderColor = if (nameError != null) FocusRed else FocusBorder,
                            focusedTextColor = FocusWhite,
                            unfocusedTextColor = FocusWhite,
                            focusedContainerColor = FocusPanelLight,
                            unfocusedContainerColor = FocusPanelLight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError != null,
                        singleLine = true
                    )
                    if (nameError != null) {
                        Text(nameError!!, color = FocusRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Description / Intent (Optional)", color = FocusWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Why this routine matters...", color = FocusSecondary, fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { performSave() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FocusAccent,
                            unfocusedBorderColor = FocusBorder,
                            focusedTextColor = FocusWhite,
                            unfocusedTextColor = FocusWhite,
                            focusedContainerColor = FocusPanelLight,
                            unfocusedContainerColor = FocusPanelLight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Weekly Frequency", color = FocusWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(7 to "Everyday", 5 to "Weekdays (5d)", 3 to "3x / Week").forEach { (days, label) ->
                            val isSelected = targetDays == days
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                                    .border(1.dp, if (isSelected) FocusAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        targetDays = days
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
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

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Button: Fixed at bottom, always reachable and visible!
                Button(
                    onClick = { performSave() },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("+ Save Routine", color = FocusBackground, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
