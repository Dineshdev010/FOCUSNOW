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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.GoalItem
import com.focusflow.app.model.GoalType
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun GoalsScreen(viewModel: FocusFlowViewModel) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val goals = viewModel.goals
    val activeGoals = goals.filter { !it.isCompleted }
    val completedGoals = goals.filter { it.isCompleted }

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
            Column {
                Text(
                    text = "STRATEGIC VISION",
                    color = FocusAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Milestones & Goals",
                    color = FocusWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.showCreateGoalDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text("+ Set Goal", color = FocusBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Goals Executive Metric Card
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${activeGoals.size}", color = FocusAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("In Flight", color = FocusSecondary, fontSize = 11.sp)
                }
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(FocusBorder)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${completedGoals.size}", color = FocusWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Achieved", color = FocusSecondary, fontSize = 11.sp)
                }
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(FocusBorder)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val avgProgress = if (goals.isNotEmpty()) {
                        ((goals.sumOf { it.progressPercentage.toDouble() } / goals.size) * 100).toInt()
                    } else 0
                    Text("$avgProgress%", color = FocusAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Avg Velocity", color = FocusSecondary, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Active Goals Section
        Text(
            text = "ACTIVE MILESTONES (${activeGoals.size})",
            color = FocusSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (activeGoals.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎯", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No Active Goals Yet",
                        color = FocusWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Set a high-impact target (like 600 focus minutes or 25 completed tasks) to orient your daily work.",
                        color = FocusSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.showCreateGoalDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ Set First Milestone", color = FocusBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            activeGoals.forEach { goal ->
                GoalCard(goal = goal, viewModel = viewModel)
            }
        }

        // Completed Goals Section
        if (completedGoals.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "ACHIEVED MILESTONES (${completedGoals.size})",
                color = FocusAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            completedGoals.forEach { goal ->
                GoalCard(goal = goal, viewModel = viewModel)
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }

    if (viewModel.showCreateGoalDialog) {
        CreateGoalDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showCreateGoalDialog = false }
        )
    }
}
}

@Composable
fun GoalCard(
    goal: GoalItem,
    viewModel: FocusFlowViewModel
) {
    val haptic = LocalHapticFeedback.current
    val unitStr = when (goal.type) {
        GoalType.FOCUS_TIME -> "min"
        GoalType.TASKS_COMPLETED -> "tasks"
        GoalType.HABIT_STREAK -> "days streak"
    }

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
                // Type Icon
                val typeEmoji = when (goal.type) {
                    GoalType.FOCUS_TIME -> "⏱️"
                    GoalType.TASKS_COMPLETED -> "✅"
                    GoalType.HABIT_STREAK -> "🔥"
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FocusAccentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(typeEmoji, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        color = FocusWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (goal.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = goal.description,
                            color = FocusSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete Button
                Text(
                    text = "🗑️",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.Reject)
                            viewModel.deleteGoal(goal.id)
                        }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { goal.progressPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (goal.isCompleted) FocusGreen else FocusAccent,
                trackColor = FocusPanel
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats Sub-row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${goal.currentValue} / ${goal.targetValue} $unitStr (${(goal.progressPercentage * 100).toInt()}%)",
                    color = if (goal.isCompleted) FocusGreen else FocusAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val deadlineStr = goal.deadlineTimestamp?.let {
                    java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(java.util.Date(it))
                }
                if (!deadlineStr.isNullOrBlank()) {
                    Text(
                        text = "Target: $deadlineStr",
                        color = FocusSecondary,
                        fontSize = 11.sp
                    )
                } else if (goal.isCompleted) {
                    Text(
                        text = "🏆 Completed",
                        color = FocusGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CreateGoalDialog(
    viewModel: FocusFlowViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    BackHandler { onDismiss() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var goalType by remember { mutableStateOf(GoalType.FOCUS_TIME) }
    var targetValueStr by remember { mutableStateOf("300") }
    var deadline by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var targetError by remember { mutableStateOf<String?>(null) }

    fun performSave() {
        if (title.isBlank()) {
            titleError = "Please enter a goal title"
            return
        }
        val target = targetValueStr.toIntOrNull() ?: 0
        if (target <= 0) {
            targetError = "Enter a valid positive number"
            return
        }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.addGoal(
            title = title.trim(),
            description = description.trim(),
            type = goalType,
            targetValue = target,
            deadline = deadline.trim()
        )
        Toast.makeText(context, "Goal \"${title.trim()}\" launched! 🎯", Toast.LENGTH_SHORT).show()
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
                        Text("NEW STRATEGIC TARGET", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("Set Productivity Goal", color = FocusWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Goal Type Selector
                    Text("Goal Type", color = FocusWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            GoalType.FOCUS_TIME to "⏱️ Focus Time",
                            GoalType.TASKS_COMPLETED to "✅ Tasks",
                            GoalType.HABIT_STREAK to "🔥 Streak"
                        ).forEach { (type, label) ->
                            val isSelected = goalType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                                    .border(1.dp, if (isSelected) FocusAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        goalType = type
                                        targetValueStr = when (type) {
                                            GoalType.FOCUS_TIME -> "300"
                                            GoalType.TASKS_COMPLETED -> "10"
                                            GoalType.HABIT_STREAK -> "7"
                                        }
                                        targetError = null
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Goal Title *", color = FocusWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (it.isNotBlank()) titleError = null
                        },
                        placeholder = { Text("e.g., Master Kotlin Coroutines", color = FocusSecondary, fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (titleError != null) FocusRed else FocusAccent,
                            unfocusedBorderColor = if (titleError != null) FocusRed else FocusBorder,
                            focusedTextColor = FocusWhite,
                            unfocusedTextColor = FocusWhite,
                            focusedContainerColor = FocusPanelLight,
                            unfocusedContainerColor = FocusPanelLight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = titleError != null,
                        singleLine = true
                    )
                    if (titleError != null) {
                        Text(titleError!!, color = FocusRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Description / Why (Optional)", color = FocusWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("What milestone will you unlock?", color = FocusSecondary, fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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

                    Spacer(modifier = Modifier.height(10.dp))

                    val unitLabel = when (goalType) {
                        GoalType.FOCUS_TIME -> "Target Focus Minutes *"
                        GoalType.TASKS_COMPLETED -> "Target Completed Tasks *"
                        GoalType.HABIT_STREAK -> "Target Streak Days *"
                    }
                    Text(unitLabel, color = FocusWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = targetValueStr,
                        onValueChange = {
                            targetValueStr = it.filter { ch -> ch.isDigit() }
                            if (it.isNotBlank()) targetError = null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { performSave() }),
                        placeholder = { Text("e.g., 300", color = FocusSecondary, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (targetError != null) FocusRed else FocusAccent,
                            unfocusedBorderColor = if (targetError != null) FocusRed else FocusBorder,
                            focusedTextColor = FocusWhite,
                            unfocusedTextColor = FocusWhite,
                            focusedContainerColor = FocusPanelLight,
                            unfocusedContainerColor = FocusPanelLight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = targetError != null,
                        singleLine = true
                    )
                    if (targetError != null) {
                        Text(targetError!!, color = FocusRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Target Deadline (Optional)", color = FocusWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        placeholder = { Text("e.g., End of Month or Oct 31", color = FocusSecondary, fontSize = 13.sp) },
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
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action: Fixed & always accessible!
                Button(
                    onClick = { performSave() },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("+ Launch Goal", color = FocusBackground, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
