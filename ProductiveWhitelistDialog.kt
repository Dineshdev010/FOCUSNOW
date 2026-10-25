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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.ProductiveAppItem
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun ProductiveWhitelistDialog(
    viewModel: FocusFlowViewModel,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val items = viewModel.productiveAppItems

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground.copy(alpha = 0.95f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .border(1.5.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header (Fixed at top)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "FOCUS LOCKDOWN",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Productive Whitelist",
                            color = FocusWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
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
                        Text("✕", color = FocusSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {

                // Work Mode Quick Preset & Toggle Card
                val isWorkMode = viewModel.isWorkModeEnabled || viewModel.repository.isWorkModeActive()
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isWorkMode) FocusAccent.copy(alpha = 0.12f) else FocusPanelLight
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isWorkMode) FocusAccent.copy(alpha = 0.6f) else FocusPanelBorder,
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text("💼", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Strict Work Mode",
                                        color = FocusWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Teams, Outlook & WhatsApp only",
                                        color = if (isWorkMode) FocusAccent else FocusSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Switch(
                                checked = viewModel.isWorkModeEnabled,
                                onCheckedChange = { enabled ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.toggleWorkMode(enabled)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FocusBackground,
                                    checkedTrackColor = FocusAccent,
                                    uncheckedThumbColor = FocusSecondary,
                                    uncheckedTrackColor = FocusPanel
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Preset Button: Teams, Outlook & WhatsApp Only
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isWorkMode) FocusAccent.copy(alpha = 0.2f) else FocusPanel)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.applyStrictWorkModePreset()
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚡ Set Office Preset (Teams + Outlook + WhatsApp)",
                                color = FocusAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Auto-Schedule Office Hours
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "⏰ Auto-Schedule Office Hours",
                                    color = FocusWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Mon–Fri (${viewModel.workModeScheduleConfig.formattedRange()})",
                                    color = if (viewModel.workModeScheduleConfig.isAutoScheduleEnabled) FocusAccent else FocusSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = viewModel.workModeScheduleConfig.isAutoScheduleEnabled,
                                onCheckedChange = { enabled ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.toggleWorkModeAutoSchedule(enabled)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FocusBackground,
                                    checkedTrackColor = FocusAccent,
                                    uncheckedThumbColor = FocusSecondary,
                                    uncheckedTrackColor = FocusPanel
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Floating Focus Island Overlay
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🏝️ Floating Focus Island",
                                    color = FocusWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Floating countdown pill over apps during passes",
                                    color = FocusSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = viewModel.isFloatingPillEnabled,
                                onCheckedChange = { enabled ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.toggleFloatingPill(enabled)
                                    if (enabled && !com.focusflow.app.util.PermissionHelper.hasOverlayPermission(context)) {
                                        com.focusflow.app.util.PermissionHelper.openOverlaySettings(context)
                                    }
                                },
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

                Spacer(modifier = Modifier.height(10.dp))

                // Explanatory Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FocusPanelLight)
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (isWorkMode)
                            "🔒 Work Mode Active: Only office products (Teams, Outlook, WhatsApp) & vital voice calls remain accessible. All social media and entertainment apps lock automatically."
                        else
                            "🛡️ When Deep Focus starts, only incoming/outgoing calls and enabled productive tools below remain unlocked. All games, social media, and web doomscrolling are intercepted.",
                        color = FocusSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // App Items List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { item ->
                        ProductiveAppRow(
                            item = item,
                            isWorkModeActive = isWorkMode,
                            onToggle = { enabled ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.toggleProductiveApp(item.id, enabled)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Done Button (Fixed at bottom so always visible)
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Save & Keep Active 🛡️",
                    color = FocusBackground,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
}

@Composable
private fun ProductiveAppRow(
    item: ProductiveAppItem,
    isWorkModeActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusBackground),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(item.emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            color = FocusWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        val isEdu = item.category.contains("Education", ignoreCase = true) || item.id.contains("duolingo", ignoreCase = true)
                        if (isEdu) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(FocusAccent.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "IMMUNE 🎓",
                                    color = FocusAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        } else if (item.isWorkModeOfficeApp || item.isEssential) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CategoryWork.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ALWAYS UNBLOCKED 💼",
                                    color = CategoryWork,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.category,
                            color = FocusSecondary,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEdu) "• 🎓 Educational (Never Blocked)"
                                else if (item.isWorkModeOfficeApp || item.isEssential) "• 🟢 Office & Workspace (Always Unblocked)"
                                else if (isWorkModeActive) "• 🔒 Restricted in Work Mode"
                                else "• 🛡️ Whitelisted",
                            color = if (isEdu || item.isWorkModeOfficeApp || item.isEssential) FocusAccent else FocusSecondary.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Switch(
                checked = item.isEnabled,
                onCheckedChange = { if (!item.isEssential) onToggle(it) },
                enabled = !item.isEssential,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = FocusBackground,
                    checkedTrackColor = FocusAccent,
                    uncheckedThumbColor = FocusSecondary,
                    uncheckedTrackColor = FocusPanelLight,
                    disabledCheckedThumbColor = FocusBackground,
                    disabledCheckedTrackColor = FocusAccent.copy(alpha = 0.6f)
                )
            )
        }
    }
}
