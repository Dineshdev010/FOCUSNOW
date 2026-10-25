package com.focusflow.app.ui.screens

import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.ui.theme.FocusAccent
import com.focusflow.app.ui.theme.FocusAccentSoft
import com.focusflow.app.ui.theme.FocusBackground
import com.focusflow.app.ui.theme.FocusBorder
import com.focusflow.app.ui.theme.FocusPanel
import com.focusflow.app.ui.theme.FocusPanelBorder
import com.focusflow.app.ui.theme.FocusPanelLight
import com.focusflow.app.ui.theme.FocusSecondary
import com.focusflow.app.ui.theme.FocusWhite
import com.focusflow.app.util.FocusAlarmManager
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun AddScheduleDialog(
    viewModel: FocusFlowViewModel,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val editingItem = viewModel.editingScheduleItem
    val isEditing = editingItem != null

    var routineName by remember { mutableStateOf(editingItem?.name ?: "Deep Work Sprint") }
    var selectedEmoji by remember { mutableStateOf(editingItem?.emoji ?: "💼") }
    var startHour by remember { mutableIntStateOf(editingItem?.startHour ?: 9) }
    var startMinute by remember { mutableIntStateOf(editingItem?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(editingItem?.endHour ?: 17) }
    var endMinute by remember { mutableIntStateOf(editingItem?.endMinute ?: 0) }
    var selectedDays by remember { mutableStateOf(editingItem?.daysSummary ?: "Mon - Fri") }

    var isAlarmEnabled by remember { mutableStateOf(editingItem?.isAlarmEnabled ?: true) }
    var selectedAlarmSound by remember { mutableStateOf(editingItem?.alarmSound ?: FocusAlarmManager.ALARM_SOUND_ZEN_GONG) }
    var isVibrateEnabled by remember { mutableStateOf(editingItem?.isVibrateEnabled ?: true) }
    var isTestingAlarm by remember { mutableStateOf(false) }

    var showEmojiPalette by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    DisposableEffect(Unit) {
        onDispose {
            FocusAlarmManager.stopAlarm(context)
        }
    }

    val allRoutineApps = remember { viewModel.getAvailableRoutineApps() }

    // Pre-populate blocked apps
    val initialSelectedApps = remember {
        if (editingItem != null && editingItem.targetAppPackages.isNotEmpty()) {
            editingItem.targetAppPackages.toSet()
        } else {
            allRoutineApps.filter { it.isBlocked && !viewModel.isOfficeOrWorkspaceApp(it.packageName) }
                .map { it.packageName }.toSet()
        }
    }
    var selectedAppPackages by remember { mutableStateOf(initialSelectedApps) }
    var appSearchQuery by remember { mutableStateOf("") }

    val emojis = listOf("💼", "📚", "🌙", "🧘", "💻", "🎨", "⚡", "🎯", "🔬", "🚀", "✍️", "☕")
    val daysPresets = listOf("Mon - Fri", "Daily", "Mon - Sat", "Weekends")

    fun formatTimeParts(h: Int, m: Int): Pair<String, String> {
        val amPm = if (h >= 12) "PM" else "AM"
        val hour12 = if (h == 0) 12 else if (h > 12) h - 12 else h
        val minStr = if (m < 10) "0$m" else "$m"
        return Pair("$hour12:$minStr", amPm)
    }

    // Dynamic duration calculation
    val totalStartMinutes = startHour * 60 + startMinute
    val totalEndMinutes = endHour * 60 + endMinute
    val diffMinutes = if (totalEndMinutes >= totalStartMinutes) {
        totalEndMinutes - totalStartMinutes
    } else {
        (24 * 60 - totalStartMinutes) + totalEndMinutes
    }
    val diffHours = diffMinutes / 60
    val remainingMins = diffMinutes % 60
    val durationSummary = when {
        diffHours > 0 && remainingMins > 0 -> "${diffHours}h ${remainingMins}m"
        diffHours > 0 -> "${diffHours}h"
        else -> "${remainingMins}m"
    }

    // Filter apps based on search & category tab
    val filteredApps = remember(allRoutineApps, appSearchQuery, selectedCategoryFilter, selectedAppPackages) {
        allRoutineApps.filter { app ->
            val matchesSearch = appSearchQuery.isBlank() ||
                app.name.contains(appSearchQuery, ignoreCase = true) ||
                app.category.contains(appSearchQuery, ignoreCase = true) ||
                app.packageName.contains(appSearchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategoryFilter) {
                "All" -> true
                "Blocked" -> selectedAppPackages.contains(app.packageName)
                "Social & Video" -> app.category.contains("Social", ignoreCase = true) ||
                    app.category.contains("Video", ignoreCase = true) ||
                    app.category.contains("Community", ignoreCase = true)
                "Games & Media" -> app.category.contains("Game", ignoreCase = true) ||
                    app.category.contains("Entertainment", ignoreCase = true) ||
                    app.category.contains("Media", ignoreCase = true)
                "Browsers" -> app.category.contains("Browser", ignoreCase = true) ||
                    app.packageName.contains("chrome", ignoreCase = true) ||
                    app.packageName.contains("browser", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesCategory
        }
    }

    // Modern Templates for 1-Tap Automation
    val routineTemplates = listOf(
        Triple("Deep Work Sprint", "💼", Pair(9 to 0, 17 to 0)),
        Triple("Study Marathon", "📚", Pair(14 to 0, 18 to 0)),
        Triple("Sleep Shield", "🌙", Pair(22 to 30, 7 to 0)),
        Triple("Morning Flow", "🧘", Pair(6 to 0, 8 to 30)),
        Triple("Evening Unplug", "⚡", Pair(19 to 0, 22 to 0))
    )

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
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.93f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(FocusAccent.copy(alpha = 0.6f), FocusPanelBorder.copy(alpha = 0.3f))
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Header Bar (Fixed at top)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FocusAccent.copy(alpha = 0.15f))
                                .border(1.dp, FocusAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedEmoji, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FocusAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isEditing) "EDIT ROUTINE" else "NEW ROUTINE",
                                        color = FocusAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• $durationSummary block",
                                    color = FocusSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = if (isEditing) "Edit Focus Block" else "Automate App Lockout",
                                color = FocusWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Close Button
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

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {

                    // --- 1-TAP TEMPLATES BAR ---
                    Text(
                        text = "QUICK TEMPLATES (1-TAP SETUP)",
                        color = FocusSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        routineTemplates.forEach { (tmplName, tmplEmoji, tmplTimes) ->
                            val isCurrentTmpl = routineName == tmplName
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrentTmpl) FocusAccentSoft else FocusPanelLight)
                                    .border(
                                        1.dp,
                                        if (isCurrentTmpl) FocusAccent else FocusBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        routineName = tmplName
                                        selectedEmoji = tmplEmoji
                                        startHour = tmplTimes.first.first
                                        startMinute = tmplTimes.first.second
                                        endHour = tmplTimes.second.first
                                        endMinute = tmplTimes.second.second

                                        // Auto-select distracting apps for convenience
                                        val distractions = allRoutineApps.filter {
                                            (it.category.contains("Social", ignoreCase = true) ||
                                             it.category.contains("Video", ignoreCase = true) ||
                                             it.category.contains("Entertainment", ignoreCase = true)) &&
                                            !viewModel.isOfficeOrWorkspaceApp(it.packageName)
                                        }.map { it.packageName }.toSet()
                                        if (distractions.isNotEmpty()) {
                                            selectedAppPackages = distractions
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tmplEmoji, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tmplName,
                                        color = if (isCurrentTmpl) FocusAccent else FocusWhite,
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrentTmpl) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- ROUTINE NAME & ICON PICKER ---
                    Text("Routine Name & Icon", color = FocusSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji Avatar Button
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(FocusPanelLight)
                                .border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showEmojiPalette = !showEmojiPalette
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedEmoji, fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Text Field
                        OutlinedTextField(
                            value = routineName,
                            onValueChange = { routineName = it },
                            singleLine = true,
                            placeholder = { Text("e.g. Deep Work Sprint", color = FocusSecondary.copy(alpha = 0.5f), fontSize = 13.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FocusAccent,
                                unfocusedBorderColor = FocusBorder,
                                focusedTextColor = FocusWhite,
                                unfocusedTextColor = FocusWhite,
                                focusedContainerColor = FocusPanelLight,
                                unfocusedContainerColor = FocusPanelLight
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Expandable Emoji Palette
                    AnimatedVisibility(
                        visible = showEmojiPalette,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                emojis.forEach { emoji ->
                                    val isSel = selectedEmoji == emoji
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSel) FocusAccentSoft else FocusPanelLight)
                                            .border(
                                                1.dp,
                                                if (isSel) FocusAccent else FocusBorder,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedEmoji = emoji
                                                showEmojiPalette = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // --- SCHEDULE DURATION: VISUAL TIMELINE & DIRECT TIME PICKERS ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCHEDULE DURATION",
                            color = FocusSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FocusAccent.copy(alpha = 0.15f))
                                .border(1.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "⏱️ $durationSummary Lockout",
                                color = FocusAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dual Interactive Time Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val (startDigits, startAmPm) = formatTimeParts(startHour, startMinute)
                        val (endDigits, endAmPm) = formatTimeParts(endHour, endMinute)

                        // START TIME CARD
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            startHour = hourOfDay
                                            startMinute = minute
                                        },
                                        startHour,
                                        startMinute,
                                        false
                                    ).show()
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "START TIME",
                                        color = FocusSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text("🕒", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = startDigits,
                                        color = FocusAccent,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(FocusAccent.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = startAmPm,
                                            color = FocusAccent,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FocusAccent.copy(alpha = 0.12f))
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Tap to Pick Time ✏️",
                                        color = FocusAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // END TIME CARD
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            endHour = hourOfDay
                                            endMinute = minute
                                        },
                                        endHour,
                                        endMinute,
                                        false
                                    ).show()
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "END TIME",
                                        color = FocusSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text("🔔", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = endDigits,
                                        color = FocusAccent,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(FocusAccent.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = endAmPm,
                                            color = FocusAccent,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FocusAccent.copy(alpha = 0.12f))
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Tap to Pick Time ✏️",
                                        color = FocusAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick duration extensions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1 to "+1h", 2 to "+2h", 4 to "+4h", 8 to "+8h").forEach { (hoursToAdd, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FocusPanelLight)
                                    .border(1.dp, FocusPanelBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        endHour = (startHour + hoursToAdd) % 24
                                        endMinute = startMinute
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = FocusSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // --- REPEAT DAYS SELECTOR ---
                    Text("Repeat Days", color = FocusSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        daysPresets.forEach { preset ->
                            val isSelected = selectedDays == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) FocusAccent else FocusPanelLight)
                                    .border(
                                        1.dp,
                                        if (isSelected) FocusAccent else FocusBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedDays = preset
                                    }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset,
                                    color = if (isSelected) FocusBackground else FocusWhite,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // --- ROUTINE ALARM & SOUND ALERT ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isAlarmEnabled) FocusAccent.copy(alpha = 0.5f) else FocusPanelBorder, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isAlarmEnabled) FocusAccent.copy(alpha = 0.2f) else FocusPanel),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isAlarmEnabled) "⏰" else "🔕",
                                            fontSize = 18.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Routine Audio Alarm",
                                            color = FocusWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isAlarmEnabled) "Alerts & wakes device when routine begins" else "Silent start (no audio or vibration)",
                                            color = if (isAlarmEnabled) FocusAccent else FocusSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = isAlarmEnabled,
                                    onCheckedChange = { checked ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isAlarmEnabled = checked
                                        if (!checked && isTestingAlarm) {
                                            FocusAlarmManager.stopAlarm(context)
                                            isTestingAlarm = false
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = FocusBackground,
                                        checkedTrackColor = FocusAccent,
                                        uncheckedThumbColor = FocusSecondary,
                                        uncheckedTrackColor = FocusBorder
                                    )
                                )
                            }

                            if (isAlarmEnabled) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "SELECT ALARM SOUND",
                                    color = FocusSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val alarmSoundOptions = listOf(
                                    Triple(FocusAlarmManager.ALARM_SOUND_ZEN_GONG, "Zen Gong", "🧘"),
                                    Triple(FocusAlarmManager.ALARM_SOUND_GENTLE_CHIME, "Gentle Chime", "🔔"),
                                    Triple(FocusAlarmManager.ALARM_SOUND_CLASSIC, "Classic Alarm", "⏰"),
                                    Triple(FocusAlarmManager.ALARM_SOUND_DIGITAL_BELL, "Digital Bell", "📟"),
                                    Triple(FocusAlarmManager.ALARM_SOUND_SYSTEM, "System Alarm", "📱")
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    alarmSoundOptions.chunked(2).forEach { rowSounds ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowSounds.forEach { (soundKey, soundTitle, soundIcon) ->
                                                val isSelected = selectedAlarmSound == soundKey
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isSelected) FocusAccent.copy(alpha = 0.2f) else FocusPanel)
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) FocusAccent else FocusBorder,
                                                            RoundedCornerShape(10.dp)
                                                        )
                                                        .clickable {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            selectedAlarmSound = soundKey
                                                            if (isTestingAlarm) {
                                                                FocusAlarmManager.playAlarm(context, soundKey, loop = false, vibrate = isVibrateEnabled)
                                                            }
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(soundIcon, fontSize = 14.sp)
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = soundTitle,
                                                                color = if (isSelected) FocusAccent else FocusWhite,
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                            )
                                                        }
                                                        if (isSelected) {
                                                            Text("✓", color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                            if (rowSounds.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Vibration & Preview Control Bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Vibrate Toggle
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                isVibrateEnabled = !isVibrateEnabled
                                            }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(if (isVibrateEnabled) "📳" else "🔕", fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isVibrateEnabled) "Vibration ON" else "Vibration OFF",
                                            color = if (isVibrateEnabled) FocusWhite else FocusSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    // Preview Test Button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isTestingAlarm) Color(0xFFEF4444).copy(alpha = 0.2f) else FocusAccent.copy(alpha = 0.15f))
                                            .border(
                                                1.dp,
                                                if (isTestingAlarm) Color(0xFFEF4444) else FocusAccent,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                if (isTestingAlarm) {
                                                    FocusAlarmManager.stopAlarm(context)
                                                    isTestingAlarm = false
                                                } else {
                                                    isTestingAlarm = true
                                                    FocusAlarmManager.playAlarm(context, selectedAlarmSound, loop = false, vibrate = isVibrateEnabled)
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isTestingAlarm) "⏹️ Stop" else "▶️ Test Audio",
                                                color = if (isTestingAlarm) Color(0xFFFCA5A5) else FocusAccent,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- APPS IN THIS ROUTINE (CATEGORIZED & FILTERED) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📱 APPS IN THIS ROUTINE",
                                color = FocusAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Turn ON to block • Turn OFF to allow",
                                color = FocusSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FocusAccent.copy(alpha = 0.15f))
                                .border(1.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${selectedAppPackages.size} Blocked",
                                color = FocusAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Pills
                    val filterCategories = listOf("All", "Blocked", "Social & Video", "Games & Media", "Browsers")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filterCategories.forEach { category ->
                            val isSel = selectedCategoryFilter == category
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) FocusAccentSoft else FocusPanelLight)
                                    .border(
                                        1.dp,
                                        if (isSel) FocusAccent else FocusBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedCategoryFilter = category
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSel) FocusAccent else FocusSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Select Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FocusPanelLight)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val nonOffice = allRoutineApps.filter { !viewModel.isOfficeOrWorkspaceApp(it.packageName) }.map { it.packageName }
                                    selectedAppPackages = nonOffice.toSet()
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Block All 🛡️", color = FocusWhite, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FocusPanelLight)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val social = allRoutineApps.filter {
                                        (it.category.contains("Social", ignoreCase = true) ||
                                         it.category.contains("Video", ignoreCase = true) ||
                                         it.category.contains("Community", ignoreCase = true)) &&
                                        !viewModel.isOfficeOrWorkspaceApp(it.packageName)
                                    }.map { it.packageName }
                                    selectedAppPackages = social.toSet()
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Social Only 📱", color = FocusWhite, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FocusPanelLight)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedAppPackages = emptySet()
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Unblock All 🟢", color = FocusSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // App Search Bar
                    OutlinedTextField(
                        value = appSearchQuery,
                        onValueChange = { appSearchQuery = it },
                        singleLine = true,
                        placeholder = { Text("Search apps to block/allow...", color = FocusSecondary.copy(alpha = 0.5f), fontSize = 12.sp) },
                        trailingIcon = {
                            if (appSearchQuery.isNotBlank()) {
                                Text(
                                    "✕",
                                    color = FocusSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .clickable { appSearchQuery = "" }
                                        .padding(8.dp)
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Blocked Apps Preview Carousel (if any blocked)
                    if (selectedAppPackages.isNotEmpty()) {
                        val blockedAppsList = allRoutineApps.filter { selectedAppPackages.contains(it.packageName) }
                        Text(
                            text = "CURRENTLY BLOCKED (${blockedAppsList.size})",
                            color = FocusSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            blockedAppsList.forEach { app ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FocusAccent.copy(alpha = 0.15f))
                                        .border(1.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedAppPackages = selectedAppPackages - app.packageName
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(app.iconEmoji, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = app.name,
                                            color = FocusWhite,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("✕", color = FocusAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Apps List
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filteredApps.forEach { app ->
                            val isOfficeApp = viewModel.isOfficeOrWorkspaceApp(app.packageName)
                            val isTurnedOn = selectedAppPackages.contains(app.packageName) && !isOfficeApp

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isTurnedOn) FocusPanelLight else FocusPanel
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isTurnedOn) FocusAccent.copy(alpha = 0.4f) else FocusBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = !isOfficeApp) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedAppPackages = if (isTurnedOn) {
                                            selectedAppPackages - app.packageName
                                        } else {
                                            selectedAppPackages + app.packageName
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    ) {
                                        Text(app.iconEmoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = app.name,
                                                    color = FocusWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (isOfficeApp) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("OFFICE EXEMPT", color = Color(0xFF4ADE80), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                            Text(
                                                text = if (isOfficeApp) "Always active & work exempt" else if (isTurnedOn) "🛡️ Blocked during this routine" else "🟢 Allowed during this routine",
                                                color = if (isOfficeApp) Color(0xFF4ADE80) else if (isTurnedOn) FocusAccent else FocusSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = if (isTurnedOn) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        }
                                    }

                                    if (!isOfficeApp) {
                                        Switch(
                                            checked = isTurnedOn,
                                            onCheckedChange = { checked ->
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedAppPackages = if (checked) {
                                                    selectedAppPackages + app.packageName
                                                } else {
                                                    selectedAppPackages - app.packageName
                                                }
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = FocusBackground,
                                                checkedTrackColor = FocusAccent,
                                                uncheckedThumbColor = FocusBackground,
                                                uncheckedTrackColor = FocusBorder
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Save Action (Fixed at bottom so always visible)
                val ctaLabel = if (isEditing) {
                    "Save Routine Changes ⚡"
                } else {
                    "Activate $durationSummary Routine (${selectedAppPackages.size} Apps Blocked) ⚡"
                }

                Button(
                    onClick = {
                        FocusAlarmManager.stopAlarm(context)
                        val appsList = selectedAppPackages.toList()
                        if (isEditing && editingItem != null) {
                            viewModel.updateFocusScheduleWithApps(
                                scheduleId = editingItem.id,
                                name = routineName,
                                emoji = selectedEmoji,
                                startHour = startHour,
                                startMinute = startMinute,
                                endHour = endHour,
                                endMinute = endMinute,
                                daysSummary = selectedDays,
                                targetAppPackages = appsList,
                                isAlarmEnabled = isAlarmEnabled,
                                alarmSound = selectedAlarmSound,
                                isVibrateEnabled = isVibrateEnabled
                            )
                        } else {
                            viewModel.addCustomSchedule(
                                name = routineName,
                                emoji = selectedEmoji,
                                startHour = startHour,
                                startMinute = startMinute,
                                endHour = endHour,
                                endMinute = endMinute,
                                daysSummary = selectedDays,
                                targetAppPackages = appsList,
                                isAlarmEnabled = isAlarmEnabled,
                                alarmSound = selectedAlarmSound,
                                isVibrateEnabled = isVibrateEnabled
                            )
                        }
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = ctaLabel,
                        color = FocusBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
