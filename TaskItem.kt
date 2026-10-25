package com.focusflow.app.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val category: String = "Work",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val linkedGoalId: String? = null
) {
    val dueDateFormatted: String?
        get() = dueDate?.let {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(it))
        }
}

enum class GoalType(val displayName: String, val unit: String, val emoji: String) {
    FOCUS_TIME("Focus Duration", "mins", "⏱️"),
    TASKS_COMPLETED("Tasks Completed", "tasks", "✅"),
    HABIT_STREAK("Habit Streak", "days", "🔥")
}

data class GoalItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val type: GoalType = GoalType.FOCUS_TIME,
    val targetValue: Int = 300,
    val currentValue: Int = 0,
    val deadlineTimestamp: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    val progressPercentage: Float
        get() = if (targetValue > 0) (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f) else 0f
}

data class HabitItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val emoji: String = "⚡",
    val frequencySummary: String = "Daily",
    val scheduledDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // Calendar day of week
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val completedDates: List<String> = emptyList(), // "yyyy-MM-dd" date strings
    val createdAt: Long = System.currentTimeMillis()
) {
    val name: String get() = title
    fun isCompletedOnDate(dateStr: String): Boolean = completedDates.contains(dateStr)
    val isCompletedToday: Boolean
        get() {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            return completedDates.contains(todayStr)
        }
}

data class FocusSessionRecord(
    val id: String = UUID.randomUUID().toString(),
    val sessionType: String = "Focus", // "Focus", "Short Break", "Long Break"
    val plannedMinutes: Int = 25,
    val actualMinutes: Int = 25,
    val startTimestamp: Long = System.currentTimeMillis(),
    val endTimestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED", // "COMPLETED", "INTERRUPTED", "CANCELLED"
    val associatedTaskTitle: String? = null
)

data class ProductivityExportData(
    val appVersion: String = "2.4.0",
    val exportedAt: Long = System.currentTimeMillis(),
    val totalFocusMinutes: Int,
    val completedSessions: Int,
    val streakDays: Int,
    val tasks: List<TaskItem>,
    val goals: List<GoalItem>,
    val habits: List<HabitItem>,
    val focusSessions: List<FocusSessionRecord>
)
