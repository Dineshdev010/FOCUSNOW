package com.focusflow.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.JigsawTheme
import com.focusflow.app.model.PuzzleType
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel
import kotlin.random.Random

private fun getThemeIcons(theme: JigsawTheme): List<String> = when (theme) {
    JigsawTheme.AURORA -> listOf("🏔️", "🌌", "❄️", "✨", "🌊", "🌲", "🪐", "☄️", "🛸", "🌟", "🧊", "🌙", "🌠", "🗻", "⛵")
    JigsawTheme.ZEN_GARDEN -> listOf("🎋", "🪨", "🍵", "🌸", "💧", "🪴", "🕊️", "🍂", "🪵", "🌿", "🎐", "🎍", "🌾", "🧘", "🌻")
    JigsawTheme.CYBERPUNK -> listOf("⚡", "💾", "🕹️", "🧬", "🌐", "🛰️", "📡", "🔋", "💿", "🤖", "🏙️", "🔮", "🔌", "💻", "💎")
    JigsawTheme.RUNES -> listOf("🛡️", "⚡", "❄️", "💎", "🌊", "⏳", "🧘", "🎯", "⚔️", "🏹", "🔮", "🗝️", "📜", "👑", "🔥")
    JigsawTheme.COSMIC -> listOf("🚀", "🪐", "🌌", "☄️", "🛸", "🌟", "🌕", "🌠", "👾", "✨", "🔭", "🛰️", "🌞", "🌍", "🌑")
    JigsawTheme.RETRO_ARCADE -> listOf("👾", "🕹️", "🎮", "🪙", "🍄", "⭐", "🏆", "💣", "🍒", "🔥", "🚀", "🛡️", "🎯", "💎", "🎲")
}

private fun getThemeGradient(theme: JigsawTheme, isCorrect: Boolean): Brush = when (theme) {
    JigsawTheme.AURORA -> if (isCorrect) {
        Brush.linearGradient(listOf(Color(0xFF0F3A4A), Color(0xFF1B6B78)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF0B1B29), Color(0xFF142F42)))
    }
    JigsawTheme.ZEN_GARDEN -> if (isCorrect) {
        Brush.linearGradient(listOf(Color(0xFF0F4A2F), Color(0xFF1E754C)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF0A261A), Color(0xFF123B2A)))
    }
    JigsawTheme.CYBERPUNK -> if (isCorrect) {
        Brush.linearGradient(listOf(Color(0xFF4A103D), Color(0xFF821E68)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF260D38), Color(0xFF3F165B)))
    }
    JigsawTheme.RUNES -> if (isCorrect) {
        Brush.linearGradient(listOf(FocusPanelLight, Color(0xFF132A46)))
    } else {
        Brush.linearGradient(listOf(FocusPanelLight, FocusPanel))
    }
    JigsawTheme.COSMIC -> if (isCorrect) {
        Brush.linearGradient(listOf(Color(0xFF1A1040), Color(0xFF2E1B6B)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF100A28), Color(0xFF1D1442)))
    }
    JigsawTheme.RETRO_ARCADE -> if (isCorrect) {
        Brush.linearGradient(listOf(Color(0xFF42210B), Color(0xFF7A3E14)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF261408), Color(0xFF3D210D)))
    }
}

@Composable
fun JigsawPuzzleDialog(
    viewModel: FocusFlowViewModel,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    val haptic = LocalHapticFeedback.current
    var selectedPuzzleType by remember { mutableStateOf(PuzzleType.PHOTO_JIGSAW) }
    var isOverallSolved by remember { mutableStateOf(false) }

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
                .fillMaxHeight(0.92f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .border(1.5.dp, FocusPanelBorder, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FocusAccentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedPuzzleType.emoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MIND PUZZLE ARENA",
                                color = FocusAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = viewModel.jigsawBreakReason,
                                color = FocusSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(FocusPanelLight, CircleShape)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = FocusSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progressive Cognitive Friction & Daily Pass Tracker
                val friction = viewModel.progressiveFrictionStatus
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusAccent.copy(alpha = 0.12f))
                        .border(1.dp, FocusAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${friction.currentTier.badge} • ${friction.currentTier.label}",
                                color = FocusAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${friction.passesUsedToday} unlocked today",
                            color = FocusSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Puzzle Type Selector Tabs - 2 Clean Rows of 4 displaying all 8 games seamlessly
                val row1 = listOf(
                    PuzzleType.PHOTO_JIGSAW to "Jigsaw",
                    PuzzleType.SLIDING_JIGSAW to "Tiles",
                    PuzzleType.FLOW_CONNECT to "Flow",
                    PuzzleType.SUDOKU_MINI to "Sudoku"
                )
                val row2 = listOf(
                    PuzzleType.MEMORY_MATCH to "Memory",
                    PuzzleType.WORD_UNSCRAMBLE to "Words",
                    PuzzleType.MATH_REFLEX to "Math",
                    PuzzleType.TYPING_TEST to "Type"
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(row1, row2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowItems.forEach { (type, label) ->
                                val isSelected = selectedPuzzleType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) FocusAccent else FocusPanelLight)
                                        .border(1.dp, if (isSelected) FocusAccent else FocusPanelBorder, RoundedCornerShape(12.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedPuzzleType = type
                                            isOverallSolved = false
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(type.emoji, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = label,
                                            color = if (isSelected) FocusBackground else FocusWhite,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Render Selected Puzzle
                when (selectedPuzzleType) {
                    PuzzleType.PHOTO_JIGSAW -> {
                        RealPhotoJigsawPuzzleView(
                            viewModel = viewModel,
                            onSolved = { isOverallSolved = true },
                            onReset = { isOverallSolved = false }
                        )
                    }
                    PuzzleType.SLIDING_JIGSAW -> {
                        SlidingTilePuzzleView(
                            viewModel = viewModel,
                            onSolved = { isOverallSolved = true },
                            onReset = { isOverallSolved = false }
                        )
                    }
                    PuzzleType.FLOW_CONNECT -> {
                        ZenFlowConnectView(
                            viewModel = viewModel,
                            onSolved = { isOverallSolved = true },
                            onReset = { isOverallSolved = false }
                        )
                    }
                    PuzzleType.SUDOKU_MINI -> {
                        FocusSudokuView(
                            viewModel = viewModel,
                            onSolved = { isOverallSolved = true },
                            onReset = { isOverallSolved = false }
                        )
                    }
                    PuzzleType.MEMORY_MATCH -> {
                        MemoryCardMatchView(
                            onSolved = { isOverallSolved = true },
                            onReset = { isOverallSolved = false }
                        )
                    }
                    PuzzleType.WORD_UNSCRAMBLE -> {
                        WordUnscramblePuzzleView(
                            onSolved = { isOverallSolved = true },
                            onReset = { isOverallSolved = false }
                        )
                    }
                    PuzzleType.MATH_REFLEX -> {
                        MathReflexSprintView(
                            onSolved = { isOverallSolved = true },
                            onReset = { isOverallSolved = false }
                        )
                    }
                    PuzzleType.TYPING_TEST -> {
                        MindfulTypingSprintView(
                            onSolved = { isOverallSolved = true },
                            onReset = { isOverallSolved = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Victory State Banner
                AnimatedVisibility(visible = isOverallSolved) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E2F1E)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, FocusAccent, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆 PUZZLE SOLVED!", color = FocusAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Mindful Dopamine Earned! +20 Coins 🪙",
                                color = FocusWhite,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onJigsawPuzzleSolved()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
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
            }
        }
    }
}

/* ========================================================
 * 1. SLIDING TILE PUZZLE (2x2 Quick & 3x3 Classic)
 * ======================================================== */
@Composable
private fun SlidingTilePuzzleView(
    viewModel: FocusFlowViewModel,
    onSolved: () -> Unit,
    onReset: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var gridSize by remember { mutableIntStateOf(3) } // 2 for Quick 2x2, 3 for Classic 3x3
    val totalTiles = gridSize * gridSize
    val emptySlot = totalTiles - 1

    var tiles by remember(gridSize) { mutableStateOf((0 until totalTiles).toList()) }
    var moveCount by remember { mutableIntStateOf(0) }
    var hasStarted by remember { mutableStateOf(false) }

    val isSolved = remember(tiles, hasStarted, moveCount) {
        hasStarted && moveCount > 0 && tiles == (0 until totalTiles).toList()
    }

    LaunchedEffect(isSolved) {
        if (isSolved) onSolved()
    }

    fun getAdjacent(index: Int): List<Int> {
        val row = index / gridSize
        val col = index % gridSize
        val adj = mutableListOf<Int>()
        if (row > 0) adj.add(index - gridSize)
        if (row < gridSize - 1) adj.add(index + gridSize)
        if (col > 0) adj.add(index - 1)
        if (col < gridSize - 1) adj.add(index + 1)
        return adj
    }

    fun shuffle() {
        val current = (0 until totalTiles).toMutableList()
        var emptyPos = emptySlot
        val steps = if (gridSize == 2) 8 else 18
        for (step in 0..steps) {
            val valid = getAdjacent(emptyPos)
            val randomNeighbor = valid[Random.nextInt(valid.size)]
            current[emptyPos] = current[randomNeighbor]
            current[randomNeighbor] = emptySlot
            emptyPos = randomNeighbor
        }
        tiles = current
        moveCount = 0
        hasStarted = true
        onReset()
    }

    LaunchedEffect(gridSize) {
        shuffle()
    }

    fun onTileClick(pos: Int) {
        val emptyPos = tiles.indexOf(emptySlot)
        val valid = getAdjacent(emptyPos)
        if (pos in valid) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val updated = tiles.toMutableList()
            updated[emptyPos] = tiles[pos]
            updated[pos] = emptySlot
            tiles = updated
            moveCount++
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Size Selector & Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (gridSize == 2) FocusAccent.copy(alpha = 0.2f) else FocusPanelLight)
                        .border(1.dp, if (gridSize == 2) FocusAccent else FocusPanelBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            if (gridSize != 2) {
                                gridSize = 2
                                onReset()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("2x2 Quick", color = if (gridSize == 2) FocusAccent else FocusSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (gridSize == 3) FocusAccent.copy(alpha = 0.2f) else FocusPanelLight)
                        .border(1.dp, if (gridSize == 3) FocusAccent else FocusPanelBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            if (gridSize != 3) {
                                gridSize = 3
                                onReset()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("3x3 Classic", color = if (gridSize == 3) FocusAccent else FocusSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text("Moves: $moveCount", color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Themes selector row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(JigsawTheme.values()) { theme ->
                val isSelected = viewModel.selectedJigsawTheme == theme
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                        .border(1.dp, if (isSelected) FocusAccent else FocusPanelBorder, RoundedCornerShape(10.dp))
                        .clickable { viewModel.selectJigsawTheme(theme) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("${theme.emoji} ${theme.title}", color = if (isSelected) FocusAccent else FocusSecondary, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tile Grid Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(FocusPanelLight, RoundedCornerShape(20.dp))
                .border(2.dp, FocusPanelBorder, RoundedCornerShape(20.dp))
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (r in 0 until gridSize) {
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (c in 0 until gridSize) {
                            val pos = r * gridSize + c
                            val tileVal = tiles[pos]
                            val isEmpty = tileVal == emptySlot
                            val isCorrect = tileVal == pos

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isEmpty) FocusBackground
                                        else FocusPanel
                                    )
                                    .border(
                                        1.dp,
                                        if (isEmpty) FocusPanelBorder.copy(alpha = 0.3f)
                                        else if (isCorrect) FocusAccent.copy(alpha = 0.5f)
                                        else FocusPanelBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable(!isEmpty) { onTileClick(pos) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!isEmpty) {
                                    val icon = getThemeIcons(viewModel.selectedJigsawTheme).getOrElse(tileVal) { "⭐" }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(icon, fontSize = if (gridSize == 2) 34.sp else 22.sp)
                                        Text("${tileVal + 1}", color = FocusSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons
        Button(
            onClick = { shuffle() },
            colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
            Text("🔄 Reshuffle", color = FocusSecondary, fontSize = 11.sp)
        }
    }
}

/* ========================================================
 * 2. MEMORY CARD MATCH PUZZLE
 * ======================================================== */
@Composable
private fun MemoryCardMatchView(
    onSolved: () -> Unit,
    onReset: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val icons = remember { listOf("🧘", "🌿", "💎", "⚡", "🪐", "☕") }
    var deckSeed by remember { mutableStateOf(System.currentTimeMillis()) }
    // 6 pairs = 12 cards (4 columns x 3 rows)
    val deck = remember(deckSeed) {
        (icons + icons).shuffled(Random(deckSeed))
    }
    val revealed = remember { mutableStateListOf<Int>() }
    val matched = remember { mutableStateListOf<Int>() }
    var flips by remember { mutableIntStateOf(0) }

    val isAllMatched = matched.size == deck.size

    LaunchedEffect(isAllMatched) {
        if (isAllMatched && deck.isNotEmpty()) onSolved()
    }

    fun onCardClick(index: Int) {
        if (index in matched || index in revealed || revealed.size >= 2) return
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        flips++
        revealed.add(index)

        if (revealed.size == 2) {
            val first = revealed[0]
            val second = revealed[1]
            if (deck[first] == deck[second]) {
                matched.add(first)
                matched.add(second)
                revealed.clear()
            }
        }
    }

    LaunchedEffect(revealed.size) {
        if (revealed.size == 2) {
            val first = revealed[0]
            val second = revealed[1]
            if (deck[first] != deck[second]) {
                kotlinx.coroutines.delay(850L)
                revealed.clear()
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pairs: ${matched.size / 2} / ${deck.size / 2}", color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Flips: $flips", color = FocusSecondary, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FocusPanelLight, RoundedCornerShape(20.dp))
                .border(1.5.dp, FocusPanelBorder, RoundedCornerShape(20.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until 4) {
                            val idx = row * 4 + col
                            val isFlipped = idx in revealed || idx in matched
                            val isMatchedCard = idx in matched

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.85f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isMatchedCard) Color(0xFF143825)
                                        else if (isFlipped) FocusPanel
                                        else Color(0xFF1F2432)
                                    )
                                    .border(
                                        1.dp,
                                        if (isMatchedCard) FocusAccent else FocusPanelBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onCardClick(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isFlipped) {
                                    Text(deck[idx], fontSize = 24.sp)
                                } else {
                                    Text("?", color = FocusSecondary.copy(alpha = 0.5f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                revealed.clear()
                matched.clear()
                flips = 0
                deckSeed = System.currentTimeMillis()
                onReset()
            },
            colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
            Text("🔄 Reset Cards", color = FocusSecondary, fontSize = 11.sp)
        }
    }
}

/* ========================================================
 * 3. MINDFUL WORD UNSCRAMBLE PUZZLE
 * ======================================================== */
@Composable
private fun WordUnscramblePuzzleView(
    onSolved: () -> Unit,
    onReset: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val words = remember {
        listOf(
            Pair("FOCUS", "Deep undivided attention"),
            Pair("MINDFUL", "Presence over endless scrolling"),
            Pair("ENERGY", "Recharge your vital stamina"),
            Pair("SERENE", "Untangled calm mental clarity"),
            Pair("HABIT", "Daily routines that shape success"),
            Pair("ZEN", "Effortless tranquil flow state")
        )
    }

    var wordIndex by remember { mutableIntStateOf(Random.nextInt(words.size)) }
    val currentPair = words[wordIndex]
    val targetWord = currentPair.first

    // Shuffled letters
    var scrambledLetters by remember(wordIndex) {
        val chars = targetWord.toList()
        var shuffled = chars.shuffled(Random(wordIndex + System.currentTimeMillis()))
        if (shuffled == chars && chars.size > 1) {
            shuffled = chars.reversed()
        }
        mutableStateOf(shuffled)
    }
    var currentAnswer by remember(wordIndex) { mutableStateOf("") }
    val isCorrect = currentAnswer == targetWord

    LaunchedEffect(isCorrect) {
        if (isCorrect) onSolved()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Word #${wordIndex + 1} of ${words.size}", color = FocusSecondary, fontSize = 11.sp)
        Text(currentPair.second, color = FocusWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(14.dp))

        // Target Slots (tap filled slot to remove letter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in targetWord.indices) {
                val char = currentAnswer.getOrNull(i)?.toString() ?: ""
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (char.isNotEmpty()) FocusPanelLight else FocusBackground)
                        .border(
                            1.5.dp,
                            if (isCorrect) FocusAccent else if (char.isNotEmpty()) FocusAccent.copy(alpha = 0.4f) else FocusPanelBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(enabled = char.isNotEmpty() && !isCorrect) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentAnswer = currentAnswer.removeRange(i, i + 1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(char, color = if (isCorrect) FocusAccent else FocusWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Shuffled Letter Bank
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            scrambledLetters.forEachIndexed { idx, ch ->
                val usedCountInAnswer = currentAnswer.count { it == ch }
                val countInScrambled = scrambledLetters.take(idx + 1).count { it == ch }
                val isUsed = countInScrambled <= usedCountInAnswer

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isUsed) FocusPanel.copy(alpha = 0.4f) else FocusPanelLight)
                        .border(1.dp, if (isUsed) Color.Transparent else FocusAccent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable(!isUsed && !isCorrect) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentAnswer += ch
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(ch.toString(), color = if (isUsed) FocusMutedColor() else FocusWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (currentAnswer.isNotEmpty()) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentAnswer = currentAnswer.dropLast(1)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(42.dp)
            ) {
                Text("⌫ Back", color = FocusWhite, fontSize = 11.sp)
            }

            Button(
                onClick = {
                    wordIndex = (wordIndex + 1) % words.size
                    currentAnswer = ""
                    onReset()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(42.dp)
            ) {
                Text("Next Word 🔀", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ========================================================
 * 4. MATH REFLEX SPRINT PUZZLE
 * ======================================================== */
private data class MathSprintItem(val question: String, val options: List<Int>, val answer: Int)

private fun generateMathSprintProblems(): List<MathSprintItem> {
    val rng = java.util.Random()
    val list = mutableListOf<MathSprintItem>()

    // Problem 1: Addition (two 2-digit numbers)
    val a1 = rng.nextInt(35) + 14
    val b1 = rng.nextInt(35) + 12
    val ans1 = a1 + b1
    val opts1 = mutableSetOf(ans1)
    var attempts = 0
    while (opts1.size < 4 && attempts++ < 30) {
        val delta = (rng.nextInt(6) + 1) * if (rng.nextBoolean()) 1 else -1
        val delta10 = if (rng.nextBoolean()) 10 else 0
        val distractor = ans1 + delta + delta10
        if (distractor > 0) opts1.add(distractor)
    }
    list.add(MathSprintItem("$a1 + $b1 = ?", opts1.toList().shuffled(), ans1))

    // Problem 2: Multiplication (mental math times table)
    val a2 = rng.nextInt(7) + 6 // 6..12
    val b2 = rng.nextInt(7) + 4 // 4..10
    val ans2 = a2 * b2
    val opts2 = mutableSetOf(ans2)
    attempts = 0
    while (opts2.size < 4 && attempts++ < 30) {
        val delta = if (rng.nextBoolean()) a2 else (rng.nextInt(4) + 1) * 2
        val distractor = if (rng.nextBoolean()) ans2 + delta else (ans2 - delta).coerceAtLeast(12)
        opts2.add(distractor)
    }
    list.add(MathSprintItem("$a2 × $b2 = ?", opts2.toList().shuffled(), ans2))

    // Problem 3: Subtraction (2-digit minus 2-digit)
    val a3 = rng.nextInt(45) + 45 // 45..89
    val b3 = rng.nextInt(25) + 14 // 14..38
    val ans3 = a3 - b3
    val opts3 = mutableSetOf(ans3)
    attempts = 0
    while (opts3.size < 4 && attempts++ < 30) {
        val delta = (rng.nextInt(6) + 1) * if (rng.nextBoolean()) 1 else -1
        val distractor = ans3 + delta
        if (distractor > 0) opts3.add(distractor)
    }
    list.add(MathSprintItem("$a3 - $b3 = ?", opts3.toList().shuffled(), ans3))

    return list
}

@Composable
private fun MathReflexSprintView(
    onSolved: () -> Unit,
    onReset: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var sprintSeed by remember { mutableIntStateOf(0) }
    val problems = remember(sprintSeed) { generateMathSprintProblems() }

    var step by remember(sprintSeed) { mutableIntStateOf(0) }
    var streak by remember(sprintSeed) { mutableIntStateOf(0) }
    var wrongOptionSelected by remember(sprintSeed, step) { mutableStateOf<Int?>(null) }
    val isComplete = streak >= problems.size

    LaunchedEffect(isComplete) {
        if (isComplete) onSolved()
    }

    val currentProblem = problems.getOrElse(step) { problems.last() }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Equation ${step + 1} of ${problems.size}", color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Sprint Streak: $streak/${problems.size}", color = FocusSecondary, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FocusPanelLight, RoundedCornerShape(18.dp))
                .border(1.5.dp, FocusPanelBorder, RoundedCornerShape(18.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(currentProblem.question, color = FocusWhite, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Options Grid (2x2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (r in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (c in 0..1) {
                        val index = r * 2 + c
                        if (index < currentProblem.options.size) {
                            val opt = currentProblem.options[index]
                            val isWrong = wrongOptionSelected == opt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isWrong) PriorityHigh.copy(alpha = 0.2f) else FocusPanel)
                                    .border(
                                        1.5.dp,
                                        if (isWrong) PriorityHigh else FocusPanelBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        if (opt == currentProblem.answer) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            wrongOptionSelected = null
                                            streak++
                                            if (step < problems.size - 1) {
                                                step++
                                            } else {
                                                onSolved()
                                            }
                                        } else {
                                            wrongOptionSelected = opt
                                            haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                        }
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$opt",
                                    color = if (isWrong) PriorityHigh else FocusWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                sprintSeed++
                onReset()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(40.dp)
        ) {
            Text("New Equations 🔄", color = FocusSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MindfulTypingSprintView(
    onSolved: () -> Unit,
    onReset: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val quotes = remember {
        listOf(
            "I choose deep focus over endless distractions.",
            "My attention is my most valuable superpower.",
            "Silence the noise and conquer the day.",
            "Discipline is choosing between what you want now and what you want most.",
            "One mindful breath at a time restores my clarity.",
            "Focus is a muscle that grows stronger with every temptation resisted."
        )
    }
    var quoteIndex by remember { mutableIntStateOf(0) }
    val targetQuote = quotes[quoteIndex]
    var typedText by remember { mutableStateOf("") }

    fun normalizeText(s: String): String {
        return s.trim()
            .replace("“", "\"").replace("”", "\"")
            .replace("’", "'").replace("‘", "'")
            .replace("\\s+".toRegex(), " ")
            .removeSuffix(".")
            .lowercase()
    }

    val isMatch = normalizeText(typedText) == normalizeText(targetQuote)

    LaunchedEffect(isMatch) {
        if (isMatch) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSolved()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mindful Affirmation", color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            val matchPercent = if (isMatch) 100 else ((typedText.length.toFloat() / targetQuote.length.toFloat()) * 100).toInt().coerceIn(0, 99)
            Text(
                "Match: ${if (isMatch) "100% ✓" else "$matchPercent%"}",
                color = if (isMatch) FocusAccent else FocusSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Target Affirmation Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FocusPanelLight, RoundedCornerShape(16.dp))
                .border(1.5.dp, if (isMatch) FocusAccent else FocusAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "\"$targetQuote\"",
                color = FocusWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Typing Field
        androidx.compose.material3.OutlinedTextField(
            value = typedText,
            onValueChange = { input ->
                typedText = input
                if (normalizeText(input) == normalizeText(targetQuote)) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSolved()
                }
            },
            placeholder = { Text("Type the quote above...", color = FocusSecondary, fontSize = 13.sp) },
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isMatch) FocusAccent else FocusAccent.copy(alpha = 0.8f),
                unfocusedBorderColor = if (isMatch) FocusAccent else FocusPanelBorder,
                focusedTextColor = FocusWhite,
                unfocusedTextColor = FocusWhite,
                focusedContainerColor = FocusPanel,
                unfocusedContainerColor = FocusPanel
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                quoteIndex = (quoteIndex + 1) % quotes.size
                typedText = ""
                onReset()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
        ) {
            Text("Next Quote 🔀", color = FocusWhite, fontSize = 11.sp)
        }
    }
}

private fun FocusMutedColor(): Color = Color(0xFF555B68)
