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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.MorningProtectionConfig
import com.focusflow.app.model.RegainAntiTamperConfig
import com.focusflow.app.model.ScrollNudgeConfig
import com.focusflow.app.model.WebBlockCategory
import com.focusflow.app.model.RegainFrictionMode
import com.focusflow.app.model.ReelSwipeNudgeConfig
import com.focusflow.app.model.FloatingPillConfig
import com.focusflow.app.model.AppOpenQuota
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun AppBlockerRulesDialog(
    viewModel: FocusFlowViewModel,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    val haptic = LocalHapticFeedback.current
    var currentTab by remember { mutableStateOf("apps") } // "apps" or "websites"
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var newWebsiteInput by remember { mutableStateOf("") }

    val allRules = viewModel.allAppShieldRules
    val categories = remember(allRules) {
        val cats = mutableListOf("All", "Blocked", "Allowed")
        val dynamicCats = allRules.map { it.category }.distinct()
        cats.addAll(dynamicCats)
        cats.distinct()
    }

    val blockedCount = allRules.count { it.isBlocked }
    val allowedCount = allRules.size - blockedCount

    val filteredRules = remember(allRules, searchQuery, selectedCategory) {
        allRules.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.appName.contains(searchQuery, ignoreCase = true) ||
                    item.packageName.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Blocked" -> item.isBlocked
                "Allowed" -> !item.isBlocked
                else -> item.category.equals(selectedCategory, ignoreCase = true)
            }

            matchesSearch && matchesCategory
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground.copy(alpha = 0.96f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .border(1.5.dp, FocusAccent.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
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
                                text = "SHIELD RULES & TARGETED BLOCKING",
                                color = FocusAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (currentTab) {
                                "apps" -> "Every App Control"
                                "websites" -> "Website & Domain Shield"
                                else -> "Regain Dopamine Hub"
                            },
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

                Spacer(modifier = Modifier.height(10.dp))

                // TOP NAVIGATION TABS: Apps Shield vs Websites vs Regain Clone Hub
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusBackground)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (currentTab == "apps") FocusAccent else FocusPanelLight)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentTab = "apps"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📱 Apps (${allRules.size})",
                            color = if (currentTab == "apps") FocusBackground else FocusWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (currentTab == "websites") FocusAccent else FocusPanelLight)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentTab = "websites"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌐 Web Shield",
                            color = if (currentTab == "websites") FocusBackground else FocusWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (currentTab == "regain") FocusAccent else FocusPanelLight)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentTab = "regain"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡ Regain Hub",
                            color = if (currentTab == "regain") FocusBackground else FocusWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (currentTab == "apps") {
                    // --- APPS SHIELD CONTENT ---
                    // Stats Overview Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FocusPanelLight)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PriorityHigh)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$blockedCount Blocked",
                                color = FocusWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(FocusAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$allowedCount Allowed",
                                color = FocusAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${allRules.size} Total Apps",
                            color = FocusSecondary,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Instant Search Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FocusBackground)
                            .border(1.dp, FocusBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🔍", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search YouTube, Chrome, Instagram, etc...",
                                    color = FocusMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = FocusWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(FocusAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp)
                        )

                        if (searchQuery.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(FocusPanelLight)
                                    .clickable { searchQuery = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", color = FocusSecondary, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Filter Pills
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            val countLabel = when (cat) {
                                "All" -> "(${allRules.size})"
                                "Blocked" -> "($blockedCount)"
                                "Allowed" -> "($allowedCount)"
                                else -> "(${allRules.count { it.category.equals(cat, ignoreCase = true) }})"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) FocusAccent else FocusPanelLight)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedCategory = cat
                                    }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "$cat $countLabel",
                                    color = if (isSelected) FocusBackground else FocusWhite,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Batch Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PriorityHigh.copy(alpha = 0.15f))
                                .border(1.dp, PriorityHigh.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.batchSetAppCategoryBlocked("Social Media", true)
                                    viewModel.batchSetAppCategoryBlocked("Short Video", true)
                                    viewModel.batchSetAppCategoryBlocked("Entertainment", true)
                                    viewModel.batchSetAppCategoryBlocked("Games", true)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🛑 Shield Distractions",
                                color = PriorityHigh,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FocusAccent.copy(alpha = 0.15f))
                                .border(1.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    allRules.forEach {
                                        viewModel.toggleAppBlockedState(it.packageName, false)
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🟢 Allow All Apps",
                                color = FocusAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // List of Every App with Direct Toggle
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (filteredRules.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No apps found matching '$searchQuery'",
                                        color = FocusSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        items(filteredRules, key = { it.packageName }) { appItem ->
                            if (appItem.packageName == "com.google.android.youtube") {
                                // SPECIALIZED YOUTUBE CARD WITH TARGETED SHORTS TOGGLE
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.5.dp, FocusAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(PriorityHigh.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("▶️", fontSize = 20.sp)
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "YouTube",
                                                        color = FocusWhite,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(FocusAccent.copy(alpha = 0.2f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "Study Shield",
                                                            color = FocusAccent,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = if (viewModel.shortFormConfig.blockYouTubeShorts) "Shorts Filter Active • Study Videos Open" else "All YouTube Videos Allowed",
                                                    color = if (viewModel.shortFormConfig.blockYouTubeShorts) FocusAccent else FocusSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Explainer note
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(FocusBackground)
                                                .border(1.dp, FocusBorder, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 7.dp)
                                        ) {
                                            Text(
                                                text = "💡 Educational tutorials, lectures & playlists remain open! Use the toggle below to block only dopamine-inducing Shorts.",
                                                color = FocusSecondary,
                                                fontSize = 10.sp,
                                                lineHeight = 14.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // PRIMARY TOGGLE: BLOCK YOUTUBE SHORTS
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(FocusBackground)
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Block YouTube Shorts",
                                                        color = FocusWhite,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(FocusAccent)
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = "RECOMMENDED",
                                                            color = FocusBackground,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.ExtraBold
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Filters infinite Shorts feed & player • Normal videos stay open",
                                                    color = FocusSecondary,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            Switch(
                                                checked = viewModel.shortFormConfig.blockYouTubeShorts,
                                                onCheckedChange = { isShortsBlocked ->
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    viewModel.toggleShortsBlocker(isShortsBlocked)
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = FocusBackground,
                                                    checkedTrackColor = FocusAccent,
                                                    uncheckedThumbColor = FocusSecondary,
                                                    uncheckedTrackColor = FocusBorder
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // SECONDARY TOGGLE: BLOCK ENTIRE YOUTUBE APP
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                                Text(
                                                    text = "Block Entire YouTube App",
                                                    color = if (appItem.isBlocked) PriorityHigh else FocusMuted,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Completely blocks YouTube app on open (including long-form)",
                                                    color = FocusSecondary,
                                                    fontSize = 9.sp
                                                )
                                            }

                                            Switch(
                                                checked = appItem.isBlocked,
                                                onCheckedChange = { isWholeBlocked ->
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    viewModel.toggleAppBlockedState(appItem.packageName, isWholeBlocked)
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = FocusBackground,
                                                    checkedTrackColor = PriorityHigh,
                                                    uncheckedThumbColor = FocusSecondary,
                                                    uncheckedTrackColor = FocusBorder
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                // STANDARD APP CARD
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (appItem.isBlocked) FocusPanelLight else FocusBackground
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            if (appItem.isBlocked) PriorityHigh.copy(alpha = 0.35f) else FocusBorder,
                                            RoundedCornerShape(14.dp)
                                        )
                                ) {
                                    val isEdu = viewModel.repository.isEducationalApp(appItem.packageName)
                                    val isOfficeOrWorkspace = viewModel.isOfficeOrWorkspaceApp(appItem.packageName)
                                    val isImmune = isEdu || isOfficeOrWorkspace
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // App Icon Emoji
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isImmune) FocusAccent.copy(alpha = 0.2f)
                                                    else if (appItem.isBlocked) PriorityHigh.copy(alpha = 0.15f)
                                                    else FocusPanelLight
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(appItem.iconEmoji, fontSize = 20.sp)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // App Name & Category
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = appItem.appName,
                                                    color = FocusWhite,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f, fill = false),
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isImmune) FocusAccent.copy(alpha = 0.2f) else FocusPanelLight)
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (isOfficeOrWorkspace) "OFFICE & WORKSPACE 💼" else if (isEdu) "EDUCATION 🎓" else appItem.category,
                                                        color = if (isImmune) FocusAccent else FocusSecondary,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isImmune) FocusAccent else if (appItem.isBlocked) PriorityHigh else FocusAccent)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isOfficeOrWorkspace) "PROTECTED (Office & Workspace • Always Unblocked)"
                                                        else if (isEdu) "PROTECTED (Always Allowed • Never Blocked)"
                                                        else if (appItem.isBlocked) "BLOCKED (Locked by Shield)"
                                                        else "ALLOWED (Active)",
                                                    color = if (isImmune) FocusAccent else if (appItem.isBlocked) PriorityHigh else FocusAccent,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        // Toggle Switch
                                        Switch(
                                            checked = if (isImmune) false else appItem.isBlocked,
                                            onCheckedChange = if (isImmune) null else { isBlockedNow ->
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.toggleAppBlockedState(appItem.packageName, isBlockedNow)
                                            },
                                            enabled = !isImmune,
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = FocusBackground,
                                                checkedTrackColor = PriorityHigh,
                                                uncheckedThumbColor = if (isImmune) FocusAccent else FocusBackground,
                                                uncheckedTrackColor = if (isImmune) FocusAccent.copy(alpha = 0.3f) else FocusBorder,
                                                disabledUncheckedThumbColor = FocusAccent,
                                                disabledUncheckedTrackColor = FocusAccent.copy(alpha = 0.25f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (currentTab == "websites") {
                    // --- WEBSITES & DOMAINS SHIELD CONTENT ---
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Explainer banner
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, FocusAccent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🌐", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Targeted Browser Web & Incognito Shield",
                                        color = FocusAccent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Inspects address bars in Chrome, Brave, Firefox, Edge & 20+ browsers. Blocks adult content, social web & custom domains without locking normal research.",
                                    color = FocusWhite,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        // Check if Chrome was mistakenly blocked as a whole app
                        val isChromeAppBlocked = allRules.any { it.packageName == "com.android.chrome" && it.isBlocked }
                        if (isChromeAppBlocked) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CategoryUrgent.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CategoryUrgent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "⚠️ Chrome Browser is currently blocked as a whole app!",
                                        color = CategoryUrgent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Because Chrome was toggled off, the entire browser exits on launch. Tap below to unblock Chrome while keeping domain filters active!",
                                        color = FocusWhite,
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.unblockChromeBrowser()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("🟢 Unblock Chrome Browser (Keep web filters active)", color = FocusBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Master Web Shield Switch
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Browser Web Shield",
                                        color = FocusWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Master switch for address bar scanning across 20+ Android browsers",
                                        color = FocusSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                                Switch(
                                    checked = viewModel.shortFormConfig.blockWebDoomscrolling,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.toggleWebDoomscroll(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = FocusBackground,
                                        checkedTrackColor = FocusAccent,
                                        uncheckedThumbColor = FocusSecondary,
                                        uncheckedTrackColor = FocusBorder
                                    )
                                )
                            }
                        }

                        // PRE-BUILT CATEGORY FILTERS
                        Text(
                            text = "PRE-BUILT SMART CATEGORY FILTERS",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        val categoriesList = listOf(
                            Triple(WebBlockCategory.ADULT_NSFW, "18+ Adult & NSFW Shield", viewModel.webShieldSettings.blockAdultNsfw),
                            Triple(WebBlockCategory.SOCIAL_WEB, "Social Media Web (Instagram, TikTok, X, Reddit)", viewModel.webShieldSettings.blockSocialWeb),
                            Triple(WebBlockCategory.DOOMSCROLL_NEWS, "Doomscroll & Sensational News Media", viewModel.webShieldSettings.blockDoomscrollNews),
                            Triple(WebBlockCategory.GAMING_STREAMING, "Gaming & Live Streams (Twitch, Roblox)", viewModel.webShieldSettings.blockGamingStreaming)
                        )

                        categoriesList.forEach { (cat, title, isChecked) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(cat.emoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = title,
                                                color = FocusWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = cat.description,
                                                color = FocusSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.toggleWebCategory(cat, it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = FocusBackground,
                                            checkedTrackColor = FocusAccent,
                                            uncheckedThumbColor = FocusSecondary,
                                            uncheckedTrackColor = FocusBorder
                                        )
                                    )
                                }
                            }
                        }

                        // INCOGNITO / PRIVATE BROWSING BLOCKER
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, FocusAccent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🕵️", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Incognito Tab Blocker",
                                            color = FocusWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Closes/intercepts private tabs in Chrome, Firefox & Brave to prevent bypass",
                                            color = FocusSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Switch(
                                    checked = viewModel.webShieldSettings.blockIncognitoMode,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.toggleIncognitoBlocker(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = FocusBackground,
                                        checkedTrackColor = FocusAccent,
                                        uncheckedThumbColor = FocusSecondary,
                                        uncheckedTrackColor = FocusBorder
                                    )
                                )
                            }
                        }

                        // CUSTOM DOMAIN BLACKLIST
                        Text(
                            text = "CUSTOM BLOCKED DOMAINS (${viewModel.blockedWebsites.size})",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // Add website bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FocusBackground)
                                    .border(1.dp, FocusBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (newWebsiteInput.isEmpty()) {
                                    Text(
                                        text = "Add domain (e.g. reddit.com)...",
                                        color = FocusMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                BasicTextField(
                                    value = newWebsiteInput,
                                    onValueChange = { newWebsiteInput = it },
                                    singleLine = true,
                                    textStyle = TextStyle(color = FocusWhite, fontSize = 11.sp),
                                    cursorBrush = SolidColor(FocusAccent),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = {
                                    if (newWebsiteInput.isNotBlank()) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.addCustomBlockedWebsite(newWebsiteInput)
                                        newWebsiteInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text("+ Add Site", color = FocusBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // List of Blocked Domains
                        viewModel.blockedWebsites.forEach { site ->
                            val emoji = when {
                                site.contains("instagram") -> "📸"
                                site.contains("tiktok") -> "🎵"
                                site.contains("youtube") -> "▶️"
                                site.contains("reddit") -> "🤖"
                                site.contains("twitter") || site.contains("x.com") -> "🐦"
                                site.contains("facebook") -> "📘"
                                else -> "🌐"
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (site.contains("instagram")) FocusAccent.copy(alpha = 0.5f) else FocusBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(FocusBackground),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 16.sp)
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = site,
                                            color = FocusWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Scanned in browser address bar",
                                            color = FocusSecondary,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(FocusBackground)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.removeCustomBlockedWebsite(site)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✕", color = PriorityHigh, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // --- REGAIN CLONE ARCHITECTURE HUB ---
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. REGAIN 1:1 MICRO-SESSION ENGINE CARD
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.2.dp, FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚡", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "1:1 Regain Micro-Session Engine",
                                            color = FocusWhite,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Intentional friction replaces cold-turkey impulse blocks",
                                            color = FocusAccent,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "1. Mindful Pause: 5s unskippable breathing countdown breaks the dopamine habit loop.\n" +
                                            "2. Intention Selection: Pick your purpose (Work, Chat, Quick Check).\n" +
                                            "3. Session Pass: Choose 1m, 5m, 10m, or 15m.\n" +
                                            "4. Time's Up Interceptor: When time expires, app immediately closes & prompts you to return to life.",
                                    color = FocusSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.testMindfulIntervention("Instagram", "com.instagram.android")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FocusAccentSoft),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .border(1.dp, FocusAccent, RoundedCornerShape(10.dp))
                                ) {
                                    Text("🧪 Test Regain Mindful Wait Screen", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // 2. MORNING DOPAMINE SHIELD CARD
                        val morning = viewModel.morningProtectionConfig
                        val isMorningActive = morning.isMorningShieldActive()

                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (isMorningActive) FocusAccent else FocusBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🌅", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Morning Dopamine Shield",
                                                    color = FocusWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (isMorningActive) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(FocusAccent)
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("ACTIVE NOW", color = FocusBackground, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "Auto-blocks social media for 30-60 mins after waking up",
                                                color = FocusSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = morning.isEnabled,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.updateMorningProtection(morning.copy(isEnabled = it))
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = FocusBackground,
                                            checkedTrackColor = FocusAccent,
                                            uncheckedThumbColor = FocusSecondary,
                                            uncheckedTrackColor = FocusBorder
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FocusBackground)
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Protected Morning Window:", color = FocusSecondary, fontSize = 11.sp)
                                    Text(
                                        text = morning.formattedMorningWindow(),
                                        color = FocusAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Shield Duration after wake-up:", color = FocusSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(30, 45, 60).forEach { mins ->
                                        val isSel = morning.shieldDurationMinutes == mins
                                        Button(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.updateMorningProtection(morning.copy(durationMinutes = mins))
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSel) FocusAccent else FocusBackground
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp)
                                                .border(1.dp, if (isSel) FocusAccent else FocusBorder, RoundedCornerShape(8.dp))
                                        ) {
                                            Text(
                                                text = "$mins Mins",
                                                color = if (isSel) FocusBackground else FocusWhite,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. CONTINUOUS SCROLL NUDGE CARD
                        val scrollNudge = viewModel.scrollNudgeConfig

                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🧘", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Continuous Scroll Nudge",
                                                color = FocusWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Interrupts unmindful endless scrolling inside apps",
                                                color = FocusSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = scrollNudge.isEnabled,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.updateScrollNudge(scrollNudge.copy(isEnabled = it))
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = FocusBackground,
                                            checkedTrackColor = FocusAccent,
                                            uncheckedThumbColor = FocusSecondary,
                                            uncheckedTrackColor = FocusBorder
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Nudge Trigger Interval:", color = FocusSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(5, 10, 15).forEach { mins ->
                                        val isSel = scrollNudge.nudgeIntervalMinutes == mins
                                        Button(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.updateScrollNudge(scrollNudge.copy(nudgeIntervalMinutes = mins))
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSel) FocusAccent else FocusBackground
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp)
                                                .border(1.dp, if (isSel) FocusAccent else FocusBorder, RoundedCornerShape(8.dp))
                                        ) {
                                            Text(
                                                text = "Every $mins min",
                                                color = if (isSel) FocusBackground else FocusWhite,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. STRICT ANTI-TAMPER & SETTINGS LOCKOUT CARD
                        val antiTamper = viewModel.antiTamperConfig

                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, PriorityHigh.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🔒", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Strict Anti-Tamper & Settings Lockout",
                                                color = FocusWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Blocks Settings Accessibility access during active focus & curfew",
                                                color = PriorityHigh,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = antiTamper.preventSettingsAccess,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.updateAntiTamper(antiTamper.copy(preventSettingsAccess = it))
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = FocusBackground,
                                            checkedTrackColor = PriorityHigh,
                                            uncheckedThumbColor = FocusSecondary,
                                            uncheckedTrackColor = FocusBorder
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FocusBackground)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "🛡️ Prevents impulsive deactivation of the Accessibility Service while Deep Focus or Curfew is active. If opened, user is redirected back to flow.",
                                        color = FocusSecondary,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }

                        // 5. REGAIN MINDFUL FRICTION CHALLENGE MODE
                        val currentFriction = viewModel.regainFrictionMode

                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🫁", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Mindful Friction Challenge Mode",
                                            color = FocusWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Choose the cognitive friction before unlocking apps",
                                            color = FocusSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    RegainFrictionMode.values().forEach { mode ->
                                        val isSel = currentFriction == mode
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSel) FocusAccentSoft else FocusBackground)
                                                .border(1.dp, if (isSel) FocusAccent else FocusBorder, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    viewModel.updateFrictionMode(mode)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(mode.emoji, fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = mode.title,
                                                            color = if (isSel) FocusAccent else FocusWhite,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = mode.description,
                                                            color = FocusSecondary,
                                                            fontSize = 9.sp
                                                        )
                                                    }
                                                }
                                                if (isSel) {
                                                    Text("✓", color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 6. FLOATING LIVE COUNTDOWN PILL (SYSTEM_ALERT_WINDOW)
                        val floatingPill = viewModel.floatingPillConfig

                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("⏳", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Floating In-App Live Timer Pill",
                                                color = FocusWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Live countdown capsule stays visible inside apps",
                                                color = FocusSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = floatingPill.isEnabled,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.updateFloatingPillConfig(it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = FocusBackground,
                                            checkedTrackColor = FocusAccent,
                                            uncheckedThumbColor = FocusSecondary,
                                            uncheckedTrackColor = FocusBorder
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FocusBackground)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Draggable floating bubble showing '⏳ 04:32 • Quick Task' while scrolling inside unlocked apps. Keeps temporal awareness sharp.",
                                        color = FocusSecondary,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }

                        // 7. REEL SWIPE COUNTER NUDGE CARD
                        val swipeNudge = viewModel.reelSwipeNudgeConfig

                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🎬", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Reel Swipe Counter Nudges",
                                                color = FocusWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Nudges after every X short videos swiped",
                                                color = FocusSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = swipeNudge.isEnabled,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.updateReelSwipeNudge(it, swipeNudge.swipeInterval)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = FocusBackground,
                                            checkedTrackColor = FocusAccent,
                                            uncheckedThumbColor = FocusSecondary,
                                            uncheckedTrackColor = FocusBorder
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Swipe Trigger Interval:", color = FocusSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(5, 10, 15, 20).forEach { count ->
                                        val isSel = swipeNudge.swipeInterval == count
                                        Button(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.updateReelSwipeNudge(swipeNudge.isEnabled, count)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSel) FocusAccent else FocusBackground
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp)
                                                .border(1.dp, if (isSel) FocusAccent else FocusBorder, RoundedCornerShape(8.dp))
                                        ) {
                                            Text(
                                                text = "$count Reels",
                                                color = if (isSel) FocusBackground else FocusWhite,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 8. DAILY APP OPEN BUDGET / QUOTA CARD
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎯", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Daily App Open Budget",
                                            color = FocusWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Hard cap on intentional app launches per day",
                                            color = FocusSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FocusBackground)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Apps default to 5 intentional opens per day. Once exhausted, the Mindful Wait Screen locks passes to prevent taking consecutive micro-sessions.",
                                        color = FocusSecondary,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Done Button
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Done & Apply Shield Rules",
                        color = FocusBackground,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
