package com.focusflow.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun GlobalSearchDialog(
    viewModel: FocusFlowViewModel,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    BackHandler { onDismiss() }

    val results = viewModel.getGlobalSearchResults()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.90f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable { onDismiss() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .padding(vertical = 8.dp)
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
                        Text("COMMAND CENTER", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("Universal Search", color = FocusWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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

                Spacer(modifier = Modifier.height(16.dp))

                // Search Input
                OutlinedTextField(
                    value = viewModel.globalSearchQuery,
                    onValueChange = { viewModel.globalSearchQuery = it },
                    placeholder = { Text("Search tasks, goals, habits, focus logs...", color = FocusSecondary) },
                    leadingIcon = { Text("🔍", fontSize = 16.sp) },
                    trailingIcon = {
                        if (viewModel.globalSearchQuery.isNotBlank()) {
                            Text(
                                text = "✕",
                                color = FocusSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable { viewModel.globalSearchQuery = "" }
                            )
                        }
                    },
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

                Spacer(modifier = Modifier.height(18.dp))

                if (viewModel.globalSearchQuery.isBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚡", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Type anything to search across FocusFlow", color = FocusSecondary, fontSize = 13.sp)
                    }
                } else if (results.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🕵️", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching records found for \"${viewModel.globalSearchQuery}\"", color = FocusWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Check your spelling or try searching by category or priority", color = FocusSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                } else {
                    Text(
                        text = "MATCHING RESULTS (${results.size})",
                        color = FocusSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    results.forEach { res ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onDismiss()
                                    // Navigate to relevant tab
                                    when (res.type) {
                                        "Task" -> viewModel.selectedTab = "Tasks"
                                        "Goal", "Habit" -> viewModel.selectedTab = "Habits & Goals"
                                        "Focus Log" -> viewModel.selectedTab = "Focus"
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(FocusAccentSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(res.icon, fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = res.title,
                                        color = FocusWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = res.subtitle,
                                        color = FocusSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FocusPanel)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = res.type,
                                        color = FocusAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
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
