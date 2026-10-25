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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    var showCustomTimePickerFor by remember { mutableStateOf<String?>(null) } // "start", "end", or null

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

                    // Presets for rapid 1-tap scheduling
                    val startPresets = listOf(
                        Pair(8, 0) to "8:00",
                        Pair(9, 0) to "9:00",
                        Pair(14, 0) to "14:00",
                        Pair(22, 0) to "22:00"
                    )
                    val isStartPreset = startPresets.any { it.first.first == startHour && it.first.second == startMinute }

                    val endPresets = listOf(
                        Pair(12, 0) to "12:00",
                        Pair(17, 0) to "17:00",
                        Pair(20, 0) to "20:00",
                        Pair(23, 0) to "23:00"
                    )
                    val isEndPreset = endPresets.any { it.first.first == endHour && it.first.second == endMinute }

                    val (startDigits, startAmPm) = formatTimeParts(startHour, startMinute)
                    val (endDigits, endAmPm) = formatTimeParts(endHour, endMinute)

                    // Dual Interactive Time Cards with Presets + Custom Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                    showCustomTimePickerFor = "start"
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
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

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = startDigits,
                                        color = FocusAccent,
                                        fontSize = 20.sp,
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

                                Spacer(modifier = Modifier.height(8.dp))

                                // Presets 2x2
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(startPresets[0], startPresets[1]).forEach { (timePair, label) ->
                                        val isSel = startHour == timePair.first && startMinute == timePair.second
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) FocusAccent else FocusPanel)
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    startHour = timePair.first
                                                    startMinute = timePair.second
                                                }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSel) FocusBackground else FocusSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(startPresets[2], startPresets[3]).forEach { (timePair, label) ->
                                        val isSel = startHour == timePair.first && startMinute == timePair.second
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) FocusAccent else FocusPanel)
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    startHour = timePair.first
                                                    startMinute = timePair.second
                                                }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSel) FocusBackground else FocusSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Prominent Custom Time Chip
                                val isCustomActive = !isStartPreset
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCustomActive) FocusAccentSoft else FocusPanel)
                                        .border(
                                            1.dp,
                                            if (isCustomActive) FocusAccent else FocusPanelBorder.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            showCustomTimePickerFor = "start"
                                        }
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isCustomActive) "⏱️ $startDigits $startAmPm ✏️" else "+ Custom Time ⏱️",
                                        color = if (isCustomActive) FocusAccent else FocusWhite,
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
                                    showCustomTimePickerFor = "end"
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
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

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = endDigits,
                                        color = FocusAccent,
                                        fontSize = 20.sp,
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

                                Spacer(modifier = Modifier.height(8.dp))

                                // Presets 2x2
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(endPresets[0], endPresets[1]).forEach { (timePair, label) ->
                                        val isSel = endHour == timePair.first && endMinute == timePair.second
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) FocusAccent else FocusPanel)
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    endHour = timePair.first
                                                    endMinute = timePair.second
                                                }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSel) FocusBackground else FocusSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(endPresets[2], endPresets[3]).forEach { (timePair, label) ->
                                        val isSel = endHour == timePair.first && endMinute == timePair.second
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) FocusAccent else FocusPanel)
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    endHour = timePair.first
                                                    endMinute = timePair.second
                                                }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSel) FocusBackground else FocusSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Prominent Custom Time Chip
                                val isCustomActive = !isEndPreset
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCustomActive) FocusAccentSoft else FocusPanel)
                                        .border(
                                            1.dp,
                                            if (isCustomActive) FocusAccent else FocusPanelBorder.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            showCustomTimePickerFor = "end"
                                        }
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isCustomActive) "⏱️ $endDigits $endAmPm ✏️" else "+ Custom Time ⏱️",
                                        color = if (isCustomActive) FocusAccent else FocusWhite,
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
                        listOf(30 to "+30m", 60 to "+1h", 120 to "+2h", 240 to "+4h", 480 to "+8h").forEach { (minsToAdd, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FocusPanelLight)
                                    .border(1.dp, FocusPanelBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val totalStart = startHour * 60 + startMinute
                                        val targetMin = (totalStart + minsToAdd) % (24 * 60)
                                        endHour = targetMin / 60
                                        endMinute = targetMin % 60
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

        // --- CUSTOM TIME PICKER MODAL ---
        if (showCustomTimePickerFor != null) {
            val isStart = showCustomTimePickerFor == "start"
            ScheduleCustomTimePickerDialog(
                title = if (isStart) "Custom Start Time" else "Custom End Time",
                initialHour = if (isStart) startHour else endHour,
                initialMinute = if (isStart) startMinute else endMinute,
                referenceStartHour = if (!isStart) startHour else null,
                referenceStartMinute = if (!isStart) startMinute else null,
                onConfirm = { h, m ->
                    if (isStart) {
                        startHour = h
                        startMinute = m
                    } else {
                        endHour = h
                        endMinute = m
                    }
                    showCustomTimePickerFor = null
                },
                onDismiss = { showCustomTimePickerFor = null }
            )
        }
    }
}

/**
 * High-tech glassmorphic custom time picker allowing precise hour/minute input,
 * AM/PM toggling, steppers, quick chips, and live duration calculation.
 */
@Composable
fun ScheduleCustomTimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    referenceStartHour: Int? = null,
    referenceStartMinute: Int? = null,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var selectedHour24 by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    // Derive 12-hour values
    val isPm = selectedHour24 >= 12
    val hour12 = when {
        selectedHour24 == 0 -> 12
        selectedHour24 > 12 -> selectedHour24 - 12
        else -> selectedHour24
    }

    var hourInput by remember { mutableStateOf(hour12.toString()) }
    var minuteInput by remember { mutableStateOf(if (selectedMinute < 10) "0$selectedMinute" else selectedMinute.toString()) }

    fun setHour12AndAmPm(newHour12: Int, pm: Boolean) {
        val safeHour12 = ((newHour12 - 1) % 12 + 12) % 12 + 1
        selectedHour24 = when {
            pm && safeHour12 == 12 -> 12
            pm -> safeHour12 + 12
            !pm && safeHour12 == 12 -> 0
            else -> safeHour12
        }
        hourInput = safeHour12.toString()
    }

    fun setMinuteSafe(newMin: Int) {
        val safeMin = ((newMin % 60) + 60) % 60
        selectedMinute = safeMin
        minuteInput = if (safeMin < 10) "0$safeMin" else safeMin.toString()
    }

    // Calculated Duration if this is an End Time picker
    val calculatedDuration = remember(selectedHour24, selectedMinute, referenceStartHour, referenceStartMinute) {
        if (referenceStartHour != null && referenceStartMinute != null) {
            val startTotal = referenceStartHour * 60 + referenceStartMinute
            val endTotal = selectedHour24 * 60 + selectedMinute
            val diff = if (endTotal >= startTotal) endTotal - startTotal else (24 * 60 - startTotal) + endTotal
            val h = diff / 60
            val m = diff % 60
            when {
                h > 0 && m > 0 -> "${h}h ${m}m"
                h > 0 -> "${h}h"
                else -> "${m}m"
            }
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground.copy(alpha = 0.90f))
            .clickable { onDismiss() },
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
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(FocusAccent.copy(alpha = 0.7f), FocusPanelBorder.copy(alpha = 0.3f))
                        )
                    ),
                    RoundedCornerShape(26.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FocusAccent.copy(alpha = 0.15f))
                                .border(1.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⏱️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = title.uppercase(),
                                color = FocusWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Set exact hours & minutes",
                                color = FocusSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(FocusPanelLight)
                            .border(1.dp, FocusPanelBorder.copy(alpha = 0.5f), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = FocusSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Digital Clock Display
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, FocusAccent.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hour Display with Steppers
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(FocusPanel)
                                        .border(1.dp, FocusPanelBorder, CircleShape)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            setHour12AndAmPm(if (hour12 == 12) 1 else hour12 + 1, isPm)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("▲", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (hour12 < 10) "0$hour12" else "$hour12",
                                    color = FocusAccent,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(FocusPanel)
                                        .border(1.dp, FocusPanelBorder, CircleShape)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            setHour12AndAmPm(if (hour12 == 1) 12 else hour12 - 1, isPm)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("▼", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text("HOUR", color = FocusSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            // Colon Divider
                            Text(
                                text = ":",
                                color = FocusAccent,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )

                            // Minute Display with Steppers
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(FocusPanel)
                                        .border(1.dp, FocusPanelBorder, CircleShape)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            setMinuteSafe(selectedMinute + 1)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("▲", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (selectedMinute < 10) "0$selectedMinute" else "$selectedMinute",
                                    color = FocusAccent,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(FocusPanel)
                                        .border(1.dp, FocusPanelBorder, CircleShape)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            setMinuteSafe(selectedMinute - 1)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("▼", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text("MINUTE", color = FocusSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // AM / PM Toggle Stack
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (!isPm) FocusAccent else FocusPanel)
                                        .border(1.dp, if (!isPm) FocusAccent else FocusBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            setHour12AndAmPm(hour12, false)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "AM",
                                        color = if (!isPm) FocusBackground else FocusSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isPm) FocusAccent else FocusPanel)
                                        .border(1.dp, if (isPm) FocusAccent else FocusBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            setHour12AndAmPm(hour12, true)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "PM",
                                        color = if (isPm) FocusBackground else FocusSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Direct Numeric Typing Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Type Exact Digits:",
                        color = FocusSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = hourInput,
                            onValueChange = { newVal ->
                                if (newVal.isEmpty() || (newVal.all { it.isDigit() } && newVal.length <= 2)) {
                                    hourInput = newVal
                                    val h = newVal.toIntOrNull()
                                    if (h != null && h in 1..12) {
                                        setHour12AndAmPm(h, isPm)
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(
                                color = FocusWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FocusAccent,
                                unfocusedBorderColor = FocusBorder,
                                focusedContainerColor = FocusPanelLight,
                                unfocusedContainerColor = FocusPanelLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(60.dp).height(44.dp)
                        )

                        Text(":", color = FocusSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = minuteInput,
                            onValueChange = { newVal ->
                                if (newVal.isEmpty() || (newVal.all { it.isDigit() } && newVal.length <= 2)) {
                                    minuteInput = newVal
                                    val m = newVal.toIntOrNull()
                                    if (m != null && m in 0..59) {
                                        selectedMinute = m
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(
                                color = FocusWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FocusAccent,
                                unfocusedBorderColor = FocusBorder,
                                focusedContainerColor = FocusPanelLight,
                                unfocusedContainerColor = FocusPanelLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(60.dp).height(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Minute Stepper Pills (-15m, -5m, +5m, +15m)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(-15 to "-15m", -5 to "-5m", 5 to "+5m", 15 to "+15m").forEach { (offset, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FocusPanelLight)
                                .border(1.dp, FocusPanelBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    setMinuteSafe(selectedMinute + offset)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = FocusWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Hour Chips (1 through 12)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SELECT HOUR (1 - 12):",
                        color = FocusSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..12).forEach { h ->
                            val isSel = hour12 == h
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) FocusAccent else FocusPanelLight)
                                    .border(1.dp, if (isSel) FocusAccent else FocusBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        setHour12AndAmPm(h, isPm)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$h",
                                    color = if (isSel) FocusBackground else FocusWhite,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Minute Chips (:00, :15, :30, :45)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "QUICK MINUTE PRESETS:",
                        color = FocusSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0 to ":00", 15 to ":15", 30 to ":30", 45 to ":45").forEach { (m, label) ->
                            val isSel = selectedMinute == m
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) FocusAccent else FocusPanelLight)
                                    .border(1.dp, if (isSel) FocusAccent else FocusBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        setMinuteSafe(m)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) FocusBackground else FocusWhite,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Relative Duration Helpers (If setting End Time)
                if (calculatedDuration != null && referenceStartHour != null && referenceStartMinute != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FocusAccent.copy(alpha = 0.12f))
                            .border(1.dp, FocusAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CALCULATED DURATION",
                                color = FocusSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "⚡ $calculatedDuration Lockout",
                                color = FocusAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Or Set Duration from Start Time:",
                            color = FocusSecondary,
                            fontSize = 9.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                30 to "+30m",
                                60 to "+1h",
                                90 to "+1.5h",
                                120 to "+2h",
                                180 to "+3h",
                                240 to "+4h",
                                360 to "+6h",
                                480 to "+8h"
                            ).forEach { (minsToAdd, label) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FocusPanelLight)
                                        .border(1.dp, FocusPanelBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            val totalStart = referenceStartHour * 60 + referenceStartMinute
                                            val targetMin = (totalStart + minsToAdd) % (24 * 60)
                                            selectedHour24 = targetMin / 60
                                            selectedMinute = targetMin % 60
                                            val newH12 = if (selectedHour24 == 0) 12 else if (selectedHour24 > 12) selectedHour24 - 12 else selectedHour24
                                            hourInput = newH12.toString()
                                            minuteInput = if (selectedMinute < 10) "0$selectedMinute" else selectedMinute.toString()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = FocusAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fallback System Clock Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FocusPanelLight)
                        .border(1.dp, FocusBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    selectedHour24 = hourOfDay
                                    selectedMinute = minute
                                    onConfirm(hourOfDay, minute)
                                },
                                selectedHour24,
                                selectedMinute,
                                false
                            ).show()
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Open System Clock Dial ⏰",
                        color = FocusSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Cancel", color = FocusSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onConfirm(selectedHour24, selectedMinute)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = "Apply Custom Time ⚡",
                            color = FocusBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
