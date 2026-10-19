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
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.ui.theme.*
import com.focusflow.app.util.PermissionHelper
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun ReelsBlockerDialog(
    viewModel: FocusFlowViewModel,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val config = viewModel.shortFormConfig
    val isAccessibilityActive = PermissionHelper.isAccessibilityServiceEnabled(context)

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
                .fillMaxHeight(0.88f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
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
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            "REELS & SHORTS SHIELD",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Targeted Feed Blocker",
                            color = FocusWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                        Text(
                            "✕",
                            color = FocusSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Regain control: Block addictive infinite-scroll video feeds while keeping messages, search, and tutorials accessible.",
                    color = FocusSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Accessibility Service Status Banner
                if (isAccessibilityActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FocusAccentSoft, RoundedCornerShape(12.dp))
                            .border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(FocusAccent, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "\uD83D\uDEE1\uFE0F Live Shield Active \u2022 Intercepting Reels in Real Time",
                                color = FocusAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CategoryUrgent.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "\u26A0\uFE0F Accessibility Service Required",
                                color = CategoryUrgent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "To detect and kick out of Reels & Shorts automatically on your phone, enable FocusNow in Accessibility Settings.",
                                color = FocusWhite,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    PermissionHelper.openAccessibilitySettings(context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CategoryUrgent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                            ) {
                                Text("Enable Accessibility Shield \u2192", color = FocusWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Shield Reclaimed Stats
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FocusPanelLight, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Traps Intercepted", color = FocusSecondary, fontSize = 11.sp)
                            Text("${viewModel.reelsBlockedCount} Reels Blocked", color = FocusWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Screen Time Reclaimed", color = FocusSecondary, fontSize = 11.sp)
                            Text("+${viewModel.reelsBlockedCount * 3} Mins Saved", color = FocusAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle 1: Instagram Reels
                ReelsBlockItem(
                    emoji = "\uD83D\uDCF8",
                    title = "Block Instagram Reels",
                    subtitle = "Blocks Reels feed, keeps DMs & posts open",
                    checked = config.blockInstagramReels,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleReelsBlocker(it)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle 2: YouTube Shorts
                ReelsBlockItem(
                    emoji = "\u25B6\uFE0F",
                    title = "Block YouTube Shorts",
                    subtitle = "Filters algorithmic shorts, keeps study videos",
                    checked = config.blockYouTubeShorts,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleShortsBlocker(it)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle 3: Snapchat Spotlight
                ReelsBlockItem(
                    emoji = "\uD83D\uDC7B",
                    title = "Block Snapchat Spotlight",
                    subtitle = "Eliminates endless swipe video tab",
                    checked = config.blockSnapchatSpotlight,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleSpotlightBlocker(it)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle 4: Web Doomscrolling
                ReelsBlockItem(
                    emoji = "\uD83C\uDF10",
                    title = "Block Web Doomscrolling",
                    subtitle = "Blocks instagram.com & web feeds (browsers stay open)",
                    checked = config.blockWebDoomscrolling,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleWebDoomscroll(it)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Test Shield Interception Button
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.testMindfulIntervention("Instagram Reels")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("\u2728 Test Interception Simulation", color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ReelsBlockItem(
    emoji: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = FocusWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = FocusSecondary, fontSize = 10.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = FocusBackground,
                    checkedTrackColor = FocusAccent,
                    uncheckedThumbColor = FocusSecondary,
                    uncheckedTrackColor = FocusPanel
                )
            )
        }
    }
}