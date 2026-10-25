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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun StrictLockScreen(viewModel: FocusFlowViewModel) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val remaining = viewModel.timerRemainingSeconds
    val minutes = remaining / 60
    val seconds = remaining % 60
    val timeFormatted = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

    BackHandler {
        haptic.performHapticFeedback(HapticFeedbackType.Reject)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lock Hero Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(CategoryUrgent.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, CategoryUrgent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83D\uDD12", fontSize = 42.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "STRICT LOCKDOWN ACTIVE",
                color = CategoryUrgent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Session Locked Until",
                color = FocusSecondary,
                fontSize = 14.sp
            )

            Text(
                text = timeFormatted,
                color = FocusWhite,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Distracting apps and short-form feeds are locked to protect your dopamine baseline.",
                color = FocusSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Option: Solve Jigsaw Puzzle to Earn Break
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanel),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, FocusAccentSoft, RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("\uD83E\uDDE9", fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Jigsaw Puzzle Break", color = FocusAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Solve 3x3 puzzle to earn 5-min break", color = FocusSecondary, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.openJigsawPuzzle("Strict Lock: Solve to unlock 5-min break") {
                                viewModel.grantEmergencyPass(viewModel.focusLockdownTargetApp ?: "", 300)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Play \u2192", color = FocusBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Option 1: Unlock with 50 Focus Coins
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanel),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("\uD83E\uDE99", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Emergency Pass", color = FocusWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Costs 50 Coins (Balance: ${viewModel.focusCoins})", color = FocusSecondary, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val ok = viewModel.emergencyUnlockWithCoins()
                            if (!ok) {
                                haptic.performHapticFeedback(HapticFeedbackType.Reject)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(12.dp),
                        enabled = viewModel.focusCoins >= 50
                    ) {
                        Text("Pay 50", color = FocusBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Option 2: Solve 2 Code Challenges
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanel),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("\uD83D\uDCBB", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Code Arena Challenge", color = FocusWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Solve 1 quiz problem to unlock free", color = FocusSecondary, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showCodeChallengeModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Solve \u2192", color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option 3: Emergency PIN
            if (!showPinDialog) {
                Text(
                    text = "Unlock with Emergency PIN",
                    color = FocusSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { showPinDialog = true }
                )
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Enter 4-Digit Security PIN", color = FocusWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = {
                                if (it.length <= 4) enteredPin = it
                                pinError = false
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FocusAccent,
                                unfocusedBorderColor = FocusBorder,
                                focusedTextColor = FocusWhite,
                                unfocusedTextColor = FocusWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (pinError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Incorrect PIN.", color = CategoryUrgent, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val ok = viewModel.emergencyUnlockWithPin(enteredPin)
                                if (!ok) {
                                    pinError = true
                                    haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CategoryUrgent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Override Lockdown", color = FocusWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}