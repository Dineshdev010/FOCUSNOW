package com.focusflow.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun SleepTrackerDialog(
    viewModel: FocusFlowViewModel,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val sleepState = viewModel.sleepTrackingState
    val isCurfewNow = sleepState.isCurfewCurrentlyActive()

    var selectedDuration by remember { mutableFloatStateOf(7.8f) }
    var selectedScore by remember { mutableIntStateOf(88) }
    var selectedBedtime by remember { mutableStateOf("10:45 PM") }
    var selectedWakeTime by remember { mutableStateOf("06:33 AM") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground.copy(alpha = 0.96f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .border(1.5.dp, androidx.compose.ui.graphics.Color(0xFF8B5CF6).copy(alpha = 0.5f), RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CIRCADIAN RECOVERY & CURFEW",
                                color = androidx.compose.ui.graphics.Color(0xFFA78BFA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sleep Architecture",
                            color = FocusWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FocusPanelLight)
                            .border(1.dp, FocusBorder, CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onClose()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = FocusSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    // Bedtime Curfew & Wind-down Shield
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (sleepState.bedtimeCurfewEnabled) FocusPanelLight else FocusBackground
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isCurfewNow) androidx.compose.ui.graphics.Color(0xFF8B5CF6) else FocusBorder,
                                RoundedCornerShape(18.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(androidx.compose.ui.graphics.Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🌙", fontSize = 20.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Bedtime Screen Curfew",
                                                color = FocusWhite,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isCurfewNow) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(androidx.compose.ui.graphics.Color(0xFF8B5CF6).copy(alpha = 0.3f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        "ACTIVE NOW",
                                                        color = androidx.compose.ui.graphics.Color(0xFFA78BFA),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${sleepState.formattedCurfewRange()} • Daily",
                                            color = androidx.compose.ui.graphics.Color(0xFFA78BFA),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Switch(
                                    checked = sleepState.bedtimeCurfewEnabled,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.toggleBedtimeCurfew(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = FocusBackground,
                                        checkedTrackColor = androidx.compose.ui.graphics.Color(0xFF8B5CF6),
                                        uncheckedThumbColor = FocusSecondary,
                                        uncheckedTrackColor = FocusBorder
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Locks dopamine triggers & doomscroll feeds past bedtime to guard natural melatonin release and deep REM sleep cycles.",
                                color = FocusSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Preset Bedtime Selectors
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bedtime:",
                                    color = FocusMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(end = 4.dp)
                                )

                                listOf(
                                    Triple(22, 0, "10:00 PM"),
                                    Triple(22, 30, "10:30 PM"),
                                    Triple(23, 0, "11:00 PM"),
                                    Triple(23, 30, "11:30 PM")
                                ).forEach { (h, m, label) ->
                                    val isSelected = sleepState.curfewStartHour == h && sleepState.curfewStartMinute == m
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) androidx.compose.ui.graphics.Color(0xFF8B5CF6) else FocusPanel
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.updateBedtimeCurfewTimes(
                                                    h,
                                                    m,
                                                    sleepState.curfewEndHour,
                                                    sleepState.curfewEndMinute
                                                )
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) FocusWhite else FocusSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sleep Architecture Stages Overview
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "LAST NIGHT ARCHITECTURE",
                                        color = androidx.compose.ui.graphics.Color(0xFFA78BFA),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${sleepState.lastNightDuration} (${sleepState.lastNightScore}/100 Restorative)",
                                        color = FocusWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "${sleepState.lastBedtime} - ${sleepState.lastWakeTime}",
                                    color = FocusSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Stacked Stages Bar
                            val stages = sleepState.stages
                            val totalMins = stages.totalInBedMinutes.toFloat().coerceAtLeast(1f)
                            val deepRatio = stages.deepMinutes / totalMins
                            val remRatio = stages.remMinutes / totalMins
                            val lightRatio = stages.lightMinutes / totalMins
                            val awakeRatio = stages.awakeMinutes / totalMins

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(deepRatio.coerceAtLeast(0.05f))
                                        .background(androidx.compose.ui.graphics.Color(0xFF7C3AED))
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(remRatio.coerceAtLeast(0.05f))
                                        .background(androidx.compose.ui.graphics.Color(0xFF3B82F6))
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(lightRatio.coerceAtLeast(0.05f))
                                        .background(androidx.compose.ui.graphics.Color(0xFF10B981))
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(awakeRatio.coerceAtLeast(0.05f))
                                        .background(androidx.compose.ui.graphics.Color(0xFFF59E0B))
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Stages Legend
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(
                                    Triple("Deep", "${stages.deepMinutes}m (22%)", androidx.compose.ui.graphics.Color(0xFF7C3AED)),
                                    Triple("REM", "${stages.remMinutes}m (26%)", androidx.compose.ui.graphics.Color(0xFF3B82F6)),
                                    Triple("Light", "${stages.lightMinutes}m (45%)", androidx.compose.ui.graphics.Color(0xFF10B981)),
                                    Triple("Awake", "${stages.awakeMinutes}m (7%)", androidx.compose.ui.graphics.Color(0xFFF59E0B))
                                ).forEach { (title, subtitle, color) ->
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(title, color = FocusWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text(subtitle, color = FocusSecondary, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Log / Adjust Sleep Session
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusPanel),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FocusBorder, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "LOG LAST NIGHT'S SLEEP",
                                color = androidx.compose.ui.graphics.Color(0xFFA78BFA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Duration Quick Selector
                            Text("Duration Slept:", color = FocusSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(6.5f, 7.0f, 7.5f, 7.8f, 8.0f, 8.5f).forEach { hours ->
                                    val isSelected = selectedDuration == hours
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) androidx.compose.ui.graphics.Color(0xFF8B5CF6) else FocusPanelLight
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedDuration = hours
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${hours}h",
                                            color = if (isSelected) FocusWhite else FocusSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Score Selector
                            Text("Restorative Score:", color = FocusSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    75 to "Fair",
                                    84 to "Good",
                                    90 to "Great",
                                    96 to "Optimal"
                                ).forEach { (score, label) ->
                                    val isSelected = selectedScore == score
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) FocusAccent else FocusPanelLight
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedScore = score
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$score",
                                                color = if (isSelected) FocusBackground else FocusWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = label,
                                                color = if (isSelected) FocusBackground.copy(alpha = 0.8f) else FocusSecondary,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val deep = (selectedDuration * 60 * 0.22f).toInt()
                                    val rem = (selectedDuration * 60 * 0.26f).toInt()
                                    val light = (selectedDuration * 60 * 0.45f).toInt()
                                    val awake = 30
                                    viewModel.logSleepRecord(
                                        durationHours = selectedDuration,
                                        score = selectedScore,
                                        bedtime = selectedBedtime,
                                        wakeTime = selectedWakeTime,
                                        deepMins = deep,
                                        remMins = rem,
                                        lightMins = light,
                                        awakeMins = awake
                                    )
                                    android.widget.Toast.makeText(context, "🌙 Sleep session logged! Restorative Score: $selectedScore", android.widget.Toast.LENGTH_SHORT).show()
                                    onClose()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color(0xFF8B5CF6)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Save Sleep Log",
                                    color = FocusWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circadian Protocol Science Tips
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "CIRCADIAN WIND-DOWN PROTOCOL",
                                color = androidx.compose.ui.graphics.Color(0xFFA78BFA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            listOf(
                                "📱 Zero doomscrolling 60m before bed preserves 38% deeper REM cycles.",
                                "💡 Blue light suppresses pineal melatonin by up to 50% within 15 minutes of screen exposure.",
                                "🛡️ FocusNow Bedtime Curfew intercepts nighttime notifications and dopamine triggers automatically."
                            ).forEach { tip ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("•", color = androidx.compose.ui.graphics.Color(0xFFA78BFA), fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
                                    Text(tip, color = FocusSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
