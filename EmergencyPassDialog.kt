package com.focusflow.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun EmergencyPassDialog(
    viewModel: FocusFlowViewModel,
    appName: String = "Distracting App",
    onPassGranted: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    val haptic = LocalHapticFeedback.current

    // Generate random math problem: e.g. 24 + 19 = 43
    var num1 by remember { mutableIntStateOf(Random.nextInt(15, 38)) }
    var num2 by remember { mutableIntStateOf(Random.nextInt(12, 35)) }
    val correctAnswer = remember(num1, num2) { num1 + num2 }

    val options = remember(correctAnswer) {
        val distractors = setOf(
            correctAnswer + 10,
            correctAnswer - 1,
            correctAnswer + 2
        )
        (distractors + correctAnswer).shuffled()
    }

    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    // Friction countdown option
    var frictionSecondsLeft by remember { mutableIntStateOf(10) }
    var isCountingDown by remember { mutableStateOf(false) }

    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            while (frictionSecondsLeft > 0) {
                delay(1000L)
                frictionSecondsLeft--
            }
            isSuccess = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(600L)
            viewModel.grantEmergencyPass(appName)
            onPassGranted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground.copy(alpha = 0.95f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .border(1.5.dp, FocusAccentGlow, RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FocusAccentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "EMERGENCY PASS",
                                color = FocusAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "60-Second Temporary Access",
                                color = FocusWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FocusPanelLight)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onClose()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = FocusSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Break mindless autopilot. Solve this quick mental math problem to access $appName for exactly 60 seconds.",
                    color = FocusSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Math Challenge Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$num1  +  $num2  =  ?",
                            color = FocusWhite,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4 Answer Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            options.forEach { opt ->
                                val isSelected = selectedAnswer == opt
                                val isThisCorrect = isSelected && opt == correctAnswer
                                val isThisWrong = isSelected && opt != correctAnswer

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isThisCorrect -> FocusAccent
                                                isThisWrong -> PriorityHigh
                                                else -> FocusPanel
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) FocusAccent else FocusPanelBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedAnswer = opt
                                            if (opt == correctAnswer) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                isSuccess = true
                                                isError = false
                                                viewModel.grantEmergencyPass(appName)
                                                onPassGranted()
                                            } else {
                                                haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                                isError = true
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$opt",
                                        color = if (isThisCorrect) FocusBackground else FocusWhite,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (isError) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Incorrect. Take a breath and try again.",
                                color = PriorityHigh,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Alternative: 10-Second Mindful Wait
                if (!isCountingDown) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isCountingDown = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FocusSecondary)
                    ) {
                        Text("Or wait 10 seconds mindfully ⏳", fontSize = 12.sp)
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Breathe... Unlocking in ${frictionSecondsLeft}s",
                                color = FocusAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
