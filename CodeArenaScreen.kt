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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun CodeArenaScreen(
    viewModel: FocusFlowViewModel,
    onClose: () -> Unit = {}
) {
    BackHandler { onClose() }
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val challenge = viewModel.challenges[viewModel.currentChallengeIndex]
    var selectedOption by remember { mutableIntStateOf(-1) }
    var submitted by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💻", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("CODE ARENA • LEARN & EARN", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("Programming Challenges", color = FocusWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(FocusPanel)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClose()
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("Close \u2715", color = FocusSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Balance Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Focus Coins Balance", color = FocusSecondary, fontSize = 11.sp)
                    Text("\uD83E\uDE99 ${viewModel.focusCoins} Coins", color = FocusAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(FocusAccentSoft)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "+${challenge.coinReward} Coins Reward",
                        color = FocusAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Question Card
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CategoryWork.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(challenge.language, color = CategoryWork, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FocusPanelLight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(challenge.difficulty, color = FocusSecondary, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = challenge.question,
                    color = FocusWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )

                if (challenge.codeSnippet != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FocusBackground)
                            .border(1.dp, FocusBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = challenge.codeSnippet,
                            color = FocusAccent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Options
                challenge.options.forEachIndexed { index, option ->
                    val isSelected = selectedOption == index
                    val isCorrectOption = index == challenge.correctIndex
                    val showGreen = submitted && isCorrectOption
                    val showRed = submitted && isSelected && !isCorrect

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                showGreen -> FocusAccentSoft
                                showRed -> CategoryUrgent.copy(alpha = 0.15f)
                                isSelected -> FocusAccentSoft
                                else -> FocusPanelLight
                            }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .border(
                                1.dp,
                                when {
                                    showGreen -> FocusAccent
                                    showRed -> CategoryUrgent
                                    isSelected -> FocusAccent
                                    else -> FocusBorder
                                },
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(enabled = !submitted) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedOption = index
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(FocusPanel),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    ('A'.code + index).toChar().toString(),
                                    color = FocusSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = option,
                                color = FocusWhite,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )

                            if (showGreen) {
                                Text("\u2713", color = FocusAccent, fontWeight = FontWeight.Bold)
                            } else if (showRed) {
                                Text("\u2715", color = CategoryUrgent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button: Submit or Next Challenge
                if (!submitted) {
                    Button(
                        onClick = {
                            if (selectedOption != -1) {
                                submitted = true
                                val result = viewModel.submitChallengeAnswer(challenge.id, selectedOption)
                                isCorrect = result
                                if (result) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(14.dp),
                        enabled = selectedOption != -1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Check Answer", color = FocusBackground, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCorrect) FocusAccentSoft else CategoryUrgent.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isCorrect) "\u2705 Correct! +${challenge.coinReward} Coins Added!" else "\u274C Not quite.",
                                    color = if (isCorrect) FocusAccent else CategoryUrgent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = challenge.explanation,
                                    color = FocusWhite,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.nextChallenge()
                                selectedOption = -1
                                submitted = false
                                isCorrect = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Next Challenge \u2192", color = FocusBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}