package com.focusflow.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun AlwaysAllowedAppsDialog(
    viewModel: FocusFlowViewModel,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var customPackageInput by remember { mutableStateOf("") }
    var showAddCustomPackageInput by remember { mutableStateOf(false) }

    val allItems = viewModel.getAlwaysAllowedAppItems()
    val categories = remember(allItems) {
        listOf("All", "Office & Workspace 💼", "Allowed 🟢", "Communication", "Utilities", "Media")
    }

    val filteredItems = remember(allItems, searchQuery, selectedCategory) {
        allItems.filter { item ->
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                    item.packageName.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Allowed 🟢" -> item.isAlwaysAllowed
                else -> item.category.equals(selectedCategory, ignoreCase = true)
            }

            matchesSearch && matchesCategory
        }
    }

    val masterShieldEnabled = viewModel.isMasterShieldEnabled

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground.copy(alpha = 0.95f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClose() },
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
                .border(1.5.dp, FocusAccent.copy(alpha = 0.35f), RoundedCornerShape(26.dp))
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
                        Text(
                            text = "SHIELD MASTER CONTROL",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Always Allowed Apps",
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

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Card: Master Shield Power Switch
                val cardBgColor by animateColorAsState(
                    targetValue = if (masterShieldEnabled) FocusAccent.copy(alpha = 0.12f) else Color(0xFF261907),
                    label = "cardBgColor"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (masterShieldEnabled) FocusAccent.copy(alpha = 0.5f) else Color(0xFFF59E0B).copy(alpha = 0.4f),
                    label = "borderColor"
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, borderColor, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f).padding(end = 10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (masterShieldEnabled) FocusAccent.copy(alpha = 0.2f) else Color(0x33F59E0B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (masterShieldEnabled) "🛡️" else "⏸️", fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = if (masterShieldEnabled) "Distraction Shield: ON" else "Distraction Shield: OFF",
                                        color = if (masterShieldEnabled) FocusWhite else Color(0xFFF59E0B),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (masterShieldEnabled) "Active • Apps & Reels guarded" else "Paused • No apps or feeds blocked",
                                        color = if (masterShieldEnabled) FocusAccent else FocusSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = masterShieldEnabled,
                                onCheckedChange = { checked ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleMasterShield(checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FocusBackground,
                                    checkedTrackColor = FocusAccent,
                                    uncheckedThumbColor = FocusSecondary,
                                    uncheckedTrackColor = FocusPanelLight
                                )
                            )
                        }

                        if (!masterShieldEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x33F59E0B))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "⚠️ Protection is currently turned off. Turn on whenever you're ready to block distractions.",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ALWAYS ALLOWED APPS",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Apps You Can Use All The Time",
                            color = FocusWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${allItems.count { it.isAlwaysAllowed }} allowed",
                        color = FocusSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Allowed apps bypass all daily limits, bedtime curfews, and mindful pauses. Accessible 24/7.",
                    color = FocusSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanelLight)
                        .border(1.dp, FocusPanelBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔍", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = FocusWhite, fontSize = 13.sp),
                            cursorBrush = SolidColor(FocusAccent),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text("Search apps by name or category...", color = FocusSecondary, fontSize = 12.sp)
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "✕",
                                color = FocusSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { searchQuery = "" }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FocusAccent else FocusPanelLight)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedCategory = cat
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) FocusBackground else FocusWhite,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add Custom Package Row
                if (showAddCustomPackageInput) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FocusPanelLight)
                            .border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Add Any App by Package Name:", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BasicTextField(
                                    value = customPackageInput,
                                    onValueChange = { customPackageInput = it },
                                    textStyle = TextStyle(color = FocusWhite, fontSize = 12.sp),
                                    cursorBrush = SolidColor(FocusAccent),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { inner ->
                                        if (customPackageInput.isEmpty()) {
                                            Text("e.g. com.example.myapp", color = FocusSecondary, fontSize = 11.sp)
                                        }
                                        inner()
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (customPackageInput.isNotBlank()) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.toggleAlwaysAllowedApp(customPackageInput.trim(), true)
                                            customPackageInput = ""
                                            showAddCustomPackageInput = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Add", color = FocusBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // App List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredItems, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FocusPanelLight)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(app.iconEmoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = app.name,
                                            color = FocusWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                        if (app.isEssential) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Always Unblocked 💼",
                                                color = FocusAccent,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${app.category} • ${if (app.isEssential || app.isAlwaysAllowed) "Always Allowed 🟢" else "Shielded 🛡️"}",
                                        color = if (app.isAlwaysAllowed) FocusAccent else FocusSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Switch(
                                checked = app.isAlwaysAllowed,
                                onCheckedChange = { allowed ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.toggleAlwaysAllowedApp(app.packageName, allowed)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FocusBackground,
                                    checkedTrackColor = FocusAccent,
                                    uncheckedThumbColor = FocusSecondary,
                                    uncheckedTrackColor = FocusPanel
                                ),
                                enabled = !app.isEssential
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showAddCustomPackageInput = !showAddCustomPackageInput
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("+ Add Custom App", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("Done", color = FocusBackground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
