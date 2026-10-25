package com.focusflow.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.StoreCategory
import com.focusflow.app.ui.theme.FocusAccent
import com.focusflow.app.ui.theme.FocusAccentSoft
import com.focusflow.app.ui.theme.FocusBackground
import com.focusflow.app.ui.theme.FocusBorder
import com.focusflow.app.ui.theme.FocusPanel
import com.focusflow.app.ui.theme.FocusPanelBorder
import com.focusflow.app.ui.theme.FocusPanelLight
import com.focusflow.app.ui.theme.FocusSecondary
import com.focusflow.app.ui.theme.FocusWhite
import com.focusflow.app.ui.theme.PriorityMedium
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun RewardsStoreDialog(
    viewModel: FocusFlowViewModel,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    val haptic = LocalHapticFeedback.current
    var selectedCategory by remember { mutableStateOf(StoreCategory.THEMES) }

    val filteredItems = viewModel.storeItems.filter { it.category == selectedCategory }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground.copy(alpha = 0.94f))
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
                .border(1.5.dp, FocusPanelBorder, RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Header & Coin Balance (Fixed at top)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "FOCUS REWARDS STORE",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Dopamine Rewards",
                            color = FocusWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Coin Counter Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(FocusPanelLight)
                                .border(1.dp, PriorityMedium, RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪙", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${viewModel.focusCoins} Coins",
                                    color = PriorityMedium,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Close Button Pill
                        Box(
                            modifier = Modifier
                                .size(34.dp)
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
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Tabs (Fixed at top)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        StoreCategory.THEMES to "🎨 Themes",
                        StoreCategory.PUZZLES to "🧩 Puzzles",
                        StoreCategory.SOUNDS to "🎵 Sounds"
                    ).forEach { (category, title) ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                                .border(
                                    1.dp,
                                    if (isSelected) FocusAccent else FocusBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedCategory = category
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) FocusAccent else FocusSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content Area: Store Items List + Footer Tip
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredItems.forEach { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (item.isEquipped) {
                                        Modifier.border(1.dp, FocusAccent, RoundedCornerShape(18.dp))
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(FocusPanel),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item.iconEmoji, fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        color = FocusWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.description,
                                        color = FocusSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Action Button (Equipped / Equip / Unlock)
                                when {
                                    item.isEquipped -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(FocusAccentSoft)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("✓ Equipped", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    item.isUnlocked -> {
                                        Button(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.equipStoreItem(item.id)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = FocusPanel),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text("Equip", color = FocusWhite, fontSize = 11.sp)
                                        }
                                    }
                                    else -> {
                                        val canAfford = viewModel.focusCoins >= item.cost
                                        Button(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.purchaseStoreItem(item.id)
                                            },
                                            enabled = canAfford,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (canAfford) FocusAccent else FocusPanelBorder,
                                                contentColor = if (canAfford) FocusBackground else FocusSecondary
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text("${item.cost} 🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Footer Tip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FocusPanelLight, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            "💡 Solve Coding Challenges & Jigsaw Puzzles during breaks to earn more Focus Coins!",
                            color = FocusSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { onClose() },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Close", color = FocusSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}
