package com.focusflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun HabitsAndGoalsScreen(viewModel: FocusFlowViewModel) {
    var activeSubTab by remember { mutableStateOf("Habits") } // "Habits" or "Goals"
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Segmented Switcher Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(FocusPanel)
                    .padding(4.dp)
            ) {
                // Habits Pill
                val isHabitsSelected = activeSubTab == "Habits"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isHabitsSelected) FocusAccent else FocusPanel)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            activeSubTab = "Habits"
                        }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "⚡ Habits & Streaks",
                        color = if (isHabitsSelected) FocusBackground else FocusSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isHabitsSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Goals Pill
                val isGoalsSelected = activeSubTab == "Goals"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isGoalsSelected) FocusAccent else FocusPanel)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            activeSubTab = "Goals"
                        }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "🎯 Milestones & Goals",
                        color = if (isGoalsSelected) FocusBackground else FocusSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isGoalsSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Sub-screen rendering
        if (activeSubTab == "Habits") {
            HabitsScreen(viewModel = viewModel)
        } else {
            GoalsScreen(viewModel = viewModel)
        }
    }
}
