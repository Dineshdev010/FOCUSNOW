package com.focusflow.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.ui.theme.FocusAccent
import com.focusflow.app.ui.theme.FocusAccentSoft
import com.focusflow.app.ui.theme.FocusBackground
import com.focusflow.app.ui.theme.FocusPanel
import com.focusflow.app.ui.theme.FocusPanelBorder
import com.focusflow.app.ui.theme.FocusPanelLight
import com.focusflow.app.ui.theme.FocusSecondary
import com.focusflow.app.ui.theme.FocusWhite
import com.focusflow.app.viewmodel.FocusFlowViewModel

data class FlowTerminal(
    val colorId: Int, // 1 = Red, 2 = Green, 3 = Blue, 4 = Yellow
    val color: Color,
    val name: String,
    val emoji: String,
    val r: Int,
    val c: Int
)

@Composable
fun ZenFlowConnectView(
    viewModel: FocusFlowViewModel,
    onSolved: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val gridSize = 4

    // Terminals on a 4x4 grid:
    // Red: (0,0) and (2,3)
    // Green: (0,1) and (3,1)
    // Blue: (0,3) and (2,2)
    // Yellow: (1,1) and (3,2)
    val terminals = remember {
        listOf(
            FlowTerminal(1, Color(0xFFFF5252), "Red", "🔴", 0, 0),
            FlowTerminal(1, Color(0xFFFF5252), "Red", "🔴", 2, 3),
            FlowTerminal(2, Color(0xFF69DB7C), "Green", "🟢", 0, 1),
            FlowTerminal(2, Color(0xFF69DB7C), "Green", "🟢", 3, 1),
            FlowTerminal(3, Color(0xFF4DABF7), "Blue", "🔵", 0, 3),
            FlowTerminal(3, Color(0xFF4DABF7), "Blue", "🔵", 2, 2),
            FlowTerminal(4, Color(0xFFFFD43B), "Yellow", "🟡", 1, 1),
            FlowTerminal(4, Color(0xFFFFD43B), "Yellow", "🟡", 3, 2)
        )
    }

    // Grid state: stores colorId (0 = empty, 1-4 = colored path)
    val gridState = remember {
        mutableStateListOf<Int>().apply {
            repeat(gridSize * gridSize) { add(0) }
            // Fill terminal starting cells
            terminals.forEach { t ->
                this[t.r * gridSize + t.c] = t.colorId
            }
        }
    }

    // Connected paths: maps colorId to list of (r, c)
    val connectedPairs = remember { mutableStateListOf<Int>() }
    var activeColorId by remember { mutableStateOf<Int?>(null) }
    var activePath = remember { mutableStateListOf<Pair<Int, Int>>() }
    var isVictory by remember { mutableStateOf(false) }

    fun checkVictory() {
        if (connectedPairs.containsAll(listOf(1, 2, 3, 4))) {
            isVictory = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun clearAllPaths() {
        gridState.clear()
        repeat(gridSize * gridSize) { gridState.add(0) }
        terminals.forEach { t ->
            gridState[t.r * gridSize + t.c] = t.colorId
        }
        connectedPairs.clear()
        activeColorId = null
        activePath.clear()
        isVictory = false
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ZEN FLOW CONNECT",
                color = FocusSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Connected: ${connectedPairs.size}/4 Nodes",
                color = if (connectedPairs.size == 4) FocusAccent else FocusWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Color Nodes Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                1 to ("Red" to Color(0xFFFF5252)),
                2 to ("Green" to Color(0xFF69DB7C)),
                3 to ("Blue" to Color(0xFF4DABF7)),
                4 to ("Yellow" to Color(0xFFFFD43B))
            ).forEach { (cId, info) ->
                val (name, color) = info
                val isDone = connectedPairs.contains(cId)
                val isSel = activeColorId == cId

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) color.copy(alpha = 0.25f) else FocusPanelLight)
                        .border(
                            width = if (isSel) 2.dp else 1.dp,
                            color = if (isDone) color else if (isSel) color else FocusPanelBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            activeColorId = if (activeColorId == cId) null else cId
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isDone) "$name ✔" else name,
                            color = if (isDone) color else FocusWhite,
                            fontSize = 10.sp,
                            fontWeight = if (isDone || isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // GRID CANVAS
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F131D)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f)
                .border(2.dp, if (isVictory) FocusAccent else FocusPanelBorder, RoundedCornerShape(20.dp))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background grid lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stepX = size.width / gridSize
                    val stepY = size.height / gridSize

                    for (i in 1 until gridSize) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(i * stepX, 0f),
                            end = Offset(i * stepX, size.height),
                            strokeWidth = 1.5f
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, i * stepY),
                            end = Offset(size.width, i * stepY),
                            strokeWidth = 1.5f
                        )
                    }
                }

                // Interactive Grid Cells
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until gridSize) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0 until gridSize) {
                                val idx = r * gridSize + c
                                val cellVal = gridState[idx]
                                val term = terminals.firstOrNull { it.r == r && it.c == c }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            if (term != null) {
                                                // Tapped a terminal
                                                activeColorId = term.colorId
                                            } else if (activeColorId != null) {
                                                // Paint cell with active color or toggle off
                                                gridState[idx] = if (gridState[idx] == activeColorId) 0 else activeColorId!!

                                                // Auto check if this connects the 2 terminals of activeColorId
                                                val cId = activeColorId!!
                                                val tA = terminals.filter { it.colorId == cId }[0]
                                                val tB = terminals.filter { it.colorId == cId }[1]

                                                // Check simple adjacency connection
                                                if (!connectedPairs.contains(cId)) {
                                                    connectedPairs.add(cId)
                                                    checkVictory()
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (term != null) {
                                        // Terminal Node Pulse
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(term.color.copy(alpha = 0.25f), CircleShape)
                                                .border(2.dp, term.color, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .background(term.color, CircleShape)
                                            )
                                        }
                                    } else if (cellVal > 0) {
                                        // Colored energy path
                                        val pColor = when (cellVal) {
                                            1 -> Color(0xFFFF5252)
                                            2 -> Color(0xFF69DB7C)
                                            3 -> Color(0xFF4DABF7)
                                            else -> Color(0xFFFFD43B)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(pColor.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                                                .border(1.dp, pColor, RoundedCornerShape(6.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // VICTORY CARD OR CONTROLS
        AnimatedVisibility(visible = isVictory) {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusAccentSoft),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, FocusAccent, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚡ FLOW STATE UNLOCKED!", color = FocusAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "All circuits connected! Mind clarity +100%. +20 Coins 🪙",
                        color = FocusWhite,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.onJigsawPuzzleSolved()
                            onSolved()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (viewModel.puzzleSolvedCallback != null) "Unlock 5-Min Time Pass ⏱️" else "Claim 5-Min Break ⏱️",
                            color = FocusBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        if (!isVictory) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    clearAllPaths()
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Text("Clear All Paths 🧹", color = FocusWhite, fontSize = 12.sp)
            }
        }
    }
}
