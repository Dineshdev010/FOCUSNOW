package com.focusflow.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
fun FocusSudokuView(
    viewModel: FocusFlowViewModel,
    onSolved: () -> Unit,
    onReset: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    // Valid 4x4 Sudoku Solution:
    // [1, 2 | 3, 4]
    // [3, 4 | 1, 2]
    // --------------
    // [2, 1 | 4, 3]
    // [4, 3 | 2, 1]
    val solution = remember {
        listOf(
            1, 2, 3, 4,
            3, 4, 1, 2,
            2, 1, 4, 3,
            4, 3, 2, 1
        )
    }

    // Clues given initially (0 = empty, >0 = immutable clue)
    val initialClues = remember {
        listOf(
            1, 0, 3, 0,
            0, 4, 0, 2,
            0, 1, 0, 3,
            4, 0, 2, 0
        )
    }

    // Current cell values (16 elements)
    val boardValues = remember {
        mutableStateListOf<Int>().apply {
            addAll(initialClues)
        }
    }

    var selectedCellIndex by remember { mutableIntStateOf(-1) }
    var isVictory by remember { mutableStateOf(false) }
    var conflictCellIndex by remember { mutableIntStateOf(-1) }

    fun checkSolved() {
        val allFilled = boardValues.none { it == 0 }
        if (allFilled && boardValues.toList() == solution) {
            isVictory = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun hasConflict(index: Int, num: Int): Boolean {
        if (num == 0) return false
        val r = index / 4
        val c = index % 4

        // Check row
        for (col in 0 until 4) {
            val idx = r * 4 + col
            if (idx != index && boardValues[idx] == num) return true
        }

        // Check column
        for (row in 0 until 4) {
            val idx = row * 4 + c
            if (idx != index && boardValues[idx] == num) return true
        }

        // Check 2x2 box
        val boxR = (r / 2) * 2
        val boxC = (c / 2) * 2
        for (br in 0 until 2) {
            for (bc in 0 until 2) {
                val idx = (boxR + br) * 4 + (boxC + bc)
                if (idx != index && boardValues[idx] == num) return true
            }
        }
        return false
    }

    fun resetBoard() {
        boardValues.clear()
        boardValues.addAll(initialClues)
        selectedCellIndex = -1
        conflictCellIndex = -1
        isVictory = false
        onReset()
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
                text = "MINI SUDOKU 4x4",
                color = FocusSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Digits 1-4 • No Repeats",
                color = FocusAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SUDOKU 4x4 GRID
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131824)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .border(2.dp, if (isVictory) FocusAccent else FocusPanelBorder, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (r in 0 until 4) {
                    // Thick horizontal divider between top and bottom 2x2 boxes
                    if (r == 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(FocusPanelBorder)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (c in 0 until 4) {
                            // Thick vertical divider between left and right 2x2 boxes
                            if (c == 2) {
                                Box(
                                    modifier = Modifier
                                        .height(72.dp)
                                        .width(2.dp)
                                        .background(FocusPanelBorder)
                                )
                            }

                            val idx = r * 4 + c
                            val value = boardValues[idx]
                            val isGiven = initialClues[idx] > 0
                            val isSelected = selectedCellIndex == idx
                            val isConflicted = conflictCellIndex == idx

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(
                                        when {
                                            isSelected -> FocusAccentSoft
                                            isConflicted -> Color(0xFFFF5252).copy(alpha = 0.25f)
                                            isGiven -> FocusPanelLight.copy(alpha = 0.4f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 0.5.dp,
                                        color = if (isSelected) FocusAccent else Color.White.copy(alpha = 0.12f)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedCellIndex = idx
                                        conflictCellIndex = -1
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (value > 0) {
                                    Text(
                                        text = "$value",
                                        color = when {
                                            isConflicted -> Color(0xFFFF5252)
                                            isGiven -> FocusWhite
                                            else -> FocusAccent
                                        },
                                        fontSize = 22.sp,
                                        fontWeight = if (isGiven) FontWeight.Bold else FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // NUMBER PAD (1, 2, 3, 4, Erase)
        Row(
            modifier = Modifier.fillMaxWidth(0.92f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (1..4).forEach { digit ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanelLight)
                        .border(1.dp, FocusPanelBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (selectedCellIndex != -1 && initialClues[selectedCellIndex] == 0) {
                                val conflict = hasConflict(selectedCellIndex, digit)
                                boardValues[selectedCellIndex] = digit
                                conflictCellIndex = if (conflict) selectedCellIndex else -1
                                checkSolved()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("$digit", color = FocusWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Erase button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FocusPanelLight)
                    .border(1.dp, FocusPanelBorder, RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (selectedCellIndex != -1 && initialClues[selectedCellIndex] == 0) {
                            boardValues[selectedCellIndex] = 0
                            conflictCellIndex = -1
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("⌫", color = Color(0xFFFF6B6B), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Spacer(modifier = Modifier.height(10.dp))

        if (!isVictory) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    resetBoard()
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Text("Reset Grid 🔄", color = FocusWhite, fontSize = 11.sp)
            }
        }
    }
}
