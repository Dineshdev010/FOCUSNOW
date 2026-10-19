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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.TaskItem
import com.focusflow.app.model.TaskPriority
import com.focusflow.app.ui.components.CategoryBadge
import com.focusflow.app.ui.components.CheckmarkIcon
import com.focusflow.app.ui.components.PriorityBadge
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TasksScreen(viewModel: FocusFlowViewModel) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val categories = listOf("All", "Work", "Study", "Personal", "Health", "Side Projects")
    val taskTabs = listOf("All", "Today", "Inbox", "Upcoming", "Completed")
    val sortOptions = listOf("Priority" to "Priority", "DueDate" to "Due Date", "Title" to "Name")

    val tasksList = viewModel.getFilteredAndSortedTasks()
    val pendingCount = viewModel.tasks.count { !it.isCompleted }
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Header with "+ New Task" Action Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACTION PLAN",
                    color = FocusAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tasks & Projects",
                    color = FocusWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.editingTaskItem = null
                    viewModel.showTaskEditorModal = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text("+ New Task", color = FocusBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Search Input
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            placeholder = { Text("Search title, description...", color = FocusSecondary) },
            leadingIcon = { Text("🔍", fontSize = 15.sp) },
            trailingIcon = {
                if (viewModel.searchQuery.isNotBlank()) {
                    Text(
                        text = "✕",
                        color = FocusSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { viewModel.searchQuery = "" }
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FocusAccent,
                unfocusedBorderColor = FocusBorder,
                focusedTextColor = FocusWhite,
                unfocusedTextColor = FocusWhite,
                focusedContainerColor = FocusPanel,
                unfocusedContainerColor = FocusPanel
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Primary View Filter Tabs (Inbox, Today, Upcoming, Completed, All)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(taskTabs) { tab ->
                val isSelected = viewModel.selectedTaskTab == tab
                val tabCount = when (tab) {
                    "Today" -> viewModel.tasks.count { it.dueDateFormatted == todayStr || (!it.isCompleted && it.dueDateFormatted == null) }
                    "Inbox" -> viewModel.tasks.count { it.category.equals("Inbox", ignoreCase = true) || it.category.isBlank() }
                    "Upcoming" -> viewModel.tasks.count { it.dueDateFormatted != null && it.dueDateFormatted!! > todayStr }
                    "Completed" -> viewModel.tasks.count { it.isCompleted }
                    else -> viewModel.tasks.size
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) FocusAccent else FocusPanel)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.selectedTaskTab = tab
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "$tab ($tabCount)",
                        color = if (isSelected) FocusBackground else FocusSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary Filters: Category & Sort Order
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category scroll
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = viewModel.selectedCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                            .border(1.dp, if (isSelected) FocusAccent else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.selectedCategoryFilter = cat
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) FocusAccent else FocusWhite,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Sort Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(FocusPanelLight)
                    .clickable {
                        val next = when (viewModel.taskSortOrder) {
                            "Priority" -> "DueDate"
                            "DueDate" -> "Title"
                            else -> "Priority"
                        }
                        viewModel.taskSortOrder = next
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                val sortLabel = sortOptions.find { it.first == viewModel.taskSortOrder }?.second ?: "Priority"
                Text(
                    text = "↕ $sortLabel",
                    color = FocusAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tasks List or Empty State
        if (tasksList.isEmpty()) {
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
                    Text("📋", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (viewModel.searchQuery.isNotBlank()) "No Matching Tasks" else "No Tasks in this View",
                        color = FocusWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (viewModel.searchQuery.isNotBlank()) "Try refining your search terms or filters." else "Clear mind, clear workspace! Add your next priority action.",
                        color = FocusSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.editingTaskItem = null
                            viewModel.showTaskEditorModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ Create Task", color = FocusBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            tasksList.forEach { task ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            // Open task editor modal on tap
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.editingTaskItem = task
                            viewModel.showTaskEditorModal = true
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (task.isCompleted) FocusAccent else FocusPanel)
                                .border(1.5.dp, if (task.isCompleted) FocusAccent else FocusBorder, CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleTask(task.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (task.isCompleted) {
                                CheckmarkIcon()
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Task Title, Description, and Badges
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                color = if (task.isCompleted) FocusSecondary else FocusWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (task.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = task.description,
                                    color = FocusSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryBadge(category = task.category)
                                PriorityBadge(priority = task.priority)
                                val dateFormatted = task.dueDateFormatted
                                if (!dateFormatted.isNullOrBlank()) {
                                    val isToday = dateFormatted == todayStr
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isToday) FocusAccentSoft else FocusPanel)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isToday) "📅 Today" else "📅 $dateFormatted",
                                            color = if (isToday) FocusAccent else FocusSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                if (!task.linkedGoalId.isNullOrBlank()) {
                                    Text("🎯", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Delete Quick Action
                        Text(
                            text = "🗑️",
                            fontSize = 15.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                    viewModel.deleteTask(task.id)
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(135.dp))
    }
}