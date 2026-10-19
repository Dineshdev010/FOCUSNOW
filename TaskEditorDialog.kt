package com.focusflow.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.TaskItem
import com.focusflow.app.model.TaskPriority
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TaskEditorDialog(
    task: TaskItem?,
    viewModel: FocusFlowViewModel,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    BackHandler { onDismiss() }

    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var category by remember { mutableStateOf(task?.category ?: "Work") }
    var priority by remember { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf(task?.dueDateFormatted ?: "") }
    var linkedGoalId by remember { mutableStateOf(task?.linkedGoalId ?: "") }
    var titleError by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Work", "Study", "Personal", "Health", "Side Projects")
    val priorities = listOf(TaskPriority.HIGH, TaskPriority.MEDIUM, TaskPriority.LOW)

    // Quick Date Helpers
    val cal = Calendar.getInstance()
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    cal.add(Calendar.DAY_OF_YEAR, 1)
    val tomorrowStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    cal.add(Calendar.DAY_OF_YEAR, 6)
    val nextWeekStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (task == null) "NEW TASK" else "EDIT TASK",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (task == null) "Create Focused Action" else "Refine Task Details",
                            color = FocusWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
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

                // Title Input
                Text("Task Title *", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = null
                    },
                    placeholder = { Text("e.g., Implement navigation architecture", color = FocusSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (titleError != null) FocusRed else FocusAccent,
                        unfocusedBorderColor = if (titleError != null) FocusRed else FocusBorder,
                        focusedTextColor = FocusWhite,
                        unfocusedTextColor = FocusWhite,
                        focusedContainerColor = FocusPanelLight,
                        unfocusedContainerColor = FocusPanelLight
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    isError = titleError != null
                )
                if (titleError != null) {
                    Text(titleError!!, color = FocusRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description Input
                Text("Notes & Details (Optional)", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Add acceptance criteria, sub-steps, or links...", color = FocusSecondary) },
                    minLines = 3,
                    maxLines = 5,
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

                Spacer(modifier = Modifier.height(14.dp))

                // Priority Selection
                Text("Priority Level", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { p ->
                        val isSelected = priority == p
                        val pColor = when (p) {
                            TaskPriority.HIGH -> FocusRed
                            TaskPriority.MEDIUM -> FocusYellow
                            TaskPriority.LOW -> FocusAccent
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) pColor.copy(alpha = 0.2f) else FocusPanelLight)
                                .border(1.dp, if (isSelected) pColor else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { priority = p }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = p.name,
                                color = if (isSelected) pColor else FocusSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text("Project / Category", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                                .border(1.dp, if (isSelected) FocusAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) FocusAccent else FocusSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Due Date Selection
                Text("Due Date", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Today" to todayStr,
                        "Tomorrow" to tomorrowStr,
                        "Next Week" to nextWeekStr,
                        "No Date" to ""
                    ).forEach { (label, dateVal) ->
                        val isSelected = dueDate == dateVal
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                                .border(1.dp, if (isSelected) FocusAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { dueDate = dateVal }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
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

                // Linked Goal (Optional)
                if (viewModel.goals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Link to Active Goal (Optional)", color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(viewModel.goals) { goal ->
                            val isSelected = linkedGoalId == goal.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                                    .border(1.dp, if (isSelected) FocusAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable {
                                        linkedGoalId = if (isSelected) "" else goal.id
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "🎯 ${goal.title}",
                                    color = if (isSelected) FocusAccent else FocusSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (task != null) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                viewModel.deleteTask(task.id)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FocusRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("Delete", color = FocusRed, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = "Please enter a task title"
                                return@Button
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.saveOrUpdateTask(
                                id = task?.id,
                                title = title,
                                description = description,
                                category = category,
                                priority = priority,
                                dueDate = dueDate.ifBlank { null },
                                linkedGoalId = linkedGoalId.ifBlank { null }
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(2f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = if (task == null) "Create Task" else "Save Changes",
                            color = FocusBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
