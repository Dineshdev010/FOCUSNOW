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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay

enum class PhotoJigsawTheme(val title: String, val emoji: String, val desc: String) {
    SUNSET_FREEDOM("Sunset Freedom", "🌅", "Friends celebrating evening freedom"),
    COSMIC_AURORA("Cosmic Aurora", "🌌", "Starry nebula over arctic peak"),
    ZEN_SAKURA("Zen Sakura", "🌸", "Mount Fuji tranquility & blossom")
}

data class JigsawPieceSpec(
    val id: Int,
    val row: Int,
    val col: Int,
    val topEdge: Int,    // 0 = flat, +1 = tab out, -1 = blank in
    val rightEdge: Int,
    val bottomEdge: Int,
    val leftEdge: Int,
    val isBorder: Boolean
)

@Composable
fun RealPhotoJigsawPuzzleView(
    viewModel: FocusFlowViewModel,
    onSolved: () -> Unit,
    onReset: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var selectedTheme by remember { mutableStateOf(PhotoJigsawTheme.SUNSET_FREEDOM) }

    // Grid configuration: Default 4 columns x 3 rows = 12 pieces (true landscape photo aspect)
    var numRows by remember { mutableIntStateOf(3) }
    var numCols by remember { mutableIntStateOf(4) }
    val totalPieces = numRows * numCols

    // Piece filter in tray: "ALL", "BORDER", "CENTER"
    var trayFilter by remember { mutableStateOf("ALL") }
    val trayListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Live timer for deep focus tracking
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // Generate interlocking jigsaw piece specifications
    val pieceSpecs = remember(numRows, numCols) {
        val list = mutableListOf<JigsawPieceSpec>()

        // vEdges[r][c] = right edge of (r, c)
        // hEdges[r][c] = bottom edge of (r, c)
        val vEdges = Array(numRows) { r ->
            IntArray(numCols) { c ->
                if ((r + c) % 2 == 0) 1 else -1
            }
        }
        val hEdges = Array(numRows) { r ->
            IntArray(numCols) { c ->
                if ((r * 2 + c) % 2 == 0) -1 else 1
            }
        }

        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                val id = r * numCols + c
                val top = if (r == 0) 0 else -hEdges[r - 1][c]
                val right = if (c == numCols - 1) 0 else vEdges[r][c]
                val bottom = if (r == numRows - 1) 0 else hEdges[r][c]
                val left = if (c == 0) 0 else -vEdges[r][c - 1]
                val isBorder = (top == 0 || right == 0 || bottom == 0 || left == 0)
                list.add(JigsawPieceSpec(id, r, c, top, right, bottom, left, isBorder))
            }
        }
        list
    }

    // STRICT PUZZLE RULES: ZERO PRE-LOADED PIECES!
    // Board starts 100% empty! (all false)
    val placedStatus = remember(numRows, numCols) {
        mutableStateListOf<Boolean>().apply {
            repeat(totalPieces) { add(false) }
        }
    }

    // ALL pieces start in the tray! (shuffled)
    val trayPieces = remember(numRows, numCols) {
        mutableStateListOf<Int>().apply {
            val allIds = (0 until totalPieces).toList().shuffled()
            addAll(allIds)
        }
    }

    var selectedTrayPieceId by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVictory by remember { mutableStateOf(false) }
    var showPeekPreview by remember { mutableStateOf(false) }

    fun checkVictory() {
        if (placedStatus.all { it }) {
            isVictory = true
            isTimerRunning = false
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSolved()
        }
    }

    fun restartPuzzle() {
        placedStatus.clear()
        repeat(totalPieces) { placedStatus.add(false) }
        trayPieces.clear()
        trayPieces.addAll((0 until totalPieces).toList().shuffled())
        selectedTrayPieceId = null
        errorMessage = null
        isVictory = false
        elapsedSeconds = 0
        isTimerRunning = true
        showPeekPreview = false
        onReset()
    }

    val placedCount = placedStatus.count { it }
    val borderPiecesInTray = trayPieces.count { id -> pieceSpecs.firstOrNull { it.id == id }?.isBorder == true }
    val centerPiecesInTray = trayPieces.size - borderPiecesInTray

    val filteredTrayPieces = when (trayFilter) {
        "BORDER" -> trayPieces.filter { id -> pieceSpecs.firstOrNull { it.id == id }?.isBorder == true }
        "CENTER" -> trayPieces.filter { id -> pieceSpecs.firstOrNull { it.id == id }?.isBorder == false }
        else -> trayPieces
    }

    val formattedTime = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header: Focus Timer & Genuine Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⏱️ $formattedTime",
                    color = FocusAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• Placed: $placedCount / $totalPieces",
                    color = FocusSecondary,
                    fontSize = 11.sp
                )
            }

            // Grid Complexity Toggles: 3x3 (9 pcs), 4x3 (12 pcs), 4x4 (16 pcs)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    Triple(3, 3, "9 Pcs"),
                    Triple(3, 4, "12 Pcs"),
                    Triple(4, 4, "16 Pcs")
                ).forEach { (r, c, label) ->
                    val isSel = (numRows == r && numCols == c)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) FocusAccent else FocusPanelLight)
                            .clickable {
                                if (numRows != r || numCols != c) {
                                    numRows = r
                                    numCols = c
                                    selectedTrayPieceId = null
                                    errorMessage = null
                                    isVictory = false
                                    elapsedSeconds = 0
                                    isTimerRunning = true
                                    showPeekPreview = false
                                    onReset()
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = if (isSel) FocusBackground else FocusWhite,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scenic Theme Selector & Ghost Peek Guide
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PhotoJigsawTheme.values().forEach { theme ->
                    val isSel = selectedTheme == theme
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) FocusAccentSoft else FocusPanelLight)
                            .border(1.dp, if (isSel) FocusAccent else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                selectedTheme = theme
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${theme.emoji} ${theme.title}",
                            fontSize = 9.5.sp,
                            color = if (isSel) FocusAccent else FocusWhite,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (showPeekPreview) FocusAccent else FocusPanelLight)
                    .border(1.dp, if (showPeekPreview) FocusAccent else FocusPanelBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        showPeekPreview = !showPeekPreview
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (showPeekPreview) "👁️ Hide Guide" else "👁️ Guide",
                    fontSize = 9.5.sp,
                    color = if (showPeekPreview) FocusBackground else FocusAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // THE JIGSAW BOARD CANVAS (Starts 100% EMPTY!)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10141E)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(numCols.toFloat() / numRows.toFloat())
                .border(2.dp, if (isVictory) FocusAccent else FocusPanelBorder, RoundedCornerShape(18.dp))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val boardWidth = size.width
                    val boardHeight = size.height
                    val pieceW = boardWidth / numCols
                    val pieceH = boardHeight / numRows

                    // Draw all pieces on the board
                    pieceSpecs.forEach { spec ->
                        val isPlaced = placedStatus[spec.id]
                        val startX = spec.col * pieceW
                        val startY = spec.row * pieceH

                        val path = buildJigsawPath(
                            spec = spec,
                            x = startX,
                            y = startY,
                            w = pieceW,
                            h = pieceH
                        )

                        if (isPlaced) {
                            // Full artwork slice for genuinely assembled piece
                            clipPath(path) {
                                drawScenicArtwork(
                                    theme = selectedTheme,
                                    boardSize = Size(boardWidth, boardHeight)
                                )
                            }
                            // 3D interlocking seam edge
                            drawPath(
                                path = path,
                                color = Color.Black.copy(alpha = 0.55f),
                                style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            drawPath(
                                path = path,
                                color = Color.White.copy(alpha = 0.18f),
                                style = Stroke(width = 1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        } else {
                            // EMPTY CUTOUT SLOT with physical interlocking tabs & blanks outline
                            clipPath(path) {
                                if (showPeekPreview) {
                                    // Ghost guide artwork to help position pieces
                                    drawContext.canvas.save()
                                    drawScenicArtwork(
                                        theme = selectedTheme,
                                        boardSize = Size(boardWidth, boardHeight)
                                    )
                                    drawRect(Color.Black.copy(alpha = 0.65f))
                                    drawContext.canvas.restore()
                                } else {
                                    drawRect(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFF1E2533), Color(0xFF0D1017)),
                                            center = Offset(startX + pieceW / 2, startY + pieceH / 2),
                                            radius = pieceW * 0.85f
                                        )
                                    )
                                }
                            }

                            // Distinct jigsaw slot guideline
                            drawPath(
                                path = path,
                                color = if (showPeekPreview) FocusAccent.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.22f),
                                style = Stroke(
                                    width = 1.2f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                // Interactive Board Slots (Tap to attempt placing selected piece, or tap placed piece to return to tray)
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until numRows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            for (c in 0 until numCols) {
                                val slotId = r * numCols + c
                                val isPlaced = placedStatus[slotId]

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clickable {
                                            if (isPlaced) {
                                                // User tapped an already-placed piece: return it to tray
                                                placedStatus[slotId] = false
                                                trayPieces.add(slotId)
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                errorMessage = null
                                            } else {
                                                val sel = selectedTrayPieceId
                                                if (sel != null) {
                                                    if (sel == slotId) {
                                                        // GENUINE FIT! SNAP INTO PLACE!
                                                        placedStatus[slotId] = true
                                                        trayPieces.remove(sel)
                                                        selectedTrayPieceId = null
                                                        errorMessage = null
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        checkVictory()
                                                    } else {
                                                        // MISMATCH: No cheat, rejected!
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        errorMessage = "Edge mismatch! This piece does not fit here."
                                                    }
                                                } else {
                                                    errorMessage = "Select a piece from the tray below first!"
                                                }
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rejection / Error Message
        if (errorMessage != null && !isVictory) {
            Text(
                text = errorMessage!!,
                color = Color(0xFFFF6B6B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // TRAY / CAROUSEL AREA (Contains ALL pieces, zero pre-placed)
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FocusPanelBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tray Header with Real Puzzle Strategy Filters: All / Borders / Center
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🧺 TRAY (${trayPieces.size})",
                            color = FocusSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF141923))
                                .border(0.5.dp, FocusPanelBorder, CircleShape)
                                .clickable {
                                    coroutineScope.launch {
                                        val cur = trayListState.firstVisibleItemIndex
                                        trayListState.animateScrollToItem(maxOf(0, cur - 2))
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("◀", fontSize = 11.sp, color = FocusWhite)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF141923))
                                .border(0.5.dp, FocusPanelBorder, CircleShape)
                                .clickable {
                                    coroutineScope.launch {
                                        val cur = trayListState.firstVisibleItemIndex
                                        val maxIdx = maxOf(0, filteredTrayPieces.size - 1)
                                        trayListState.animateScrollToItem(minOf(maxIdx, cur + 2))
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▶", fontSize = 11.sp, color = FocusWhite)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "ALL" to "All (${trayPieces.size})",
                            "BORDER" to "🔲 Edge ($borderPiecesInTray)",
                            "CENTER" to "🧩 Inner ($centerPiecesInTray)"
                        ).forEach { (fKey, label) ->
                            val active = trayFilter == fKey
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (active) FocusAccentSoft else Color(0xFF141923))
                                    .border(0.5.dp, if (active) FocusAccent else FocusPanelBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        trayFilter = fKey
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    color = if (active) FocusAccent else FocusWhite,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (trayPieces.isEmpty() && !isVictory) {
                    Text(
                        text = "All pieces placed! Checking alignment...",
                        color = FocusSecondary,
                        fontSize = 12.sp
                    )
                } else if (isVictory) {
                    Text(
                        text = "🎉 Masterpiece completely assembled without hints!",
                        color = FocusAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    // LazyRow showing loose unnumbered jigsaw pieces with authentic contours
                    LazyRow(
                        state = trayListState,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredTrayPieces) { pieceId ->
                            val spec = pieceSpecs.firstOrNull { it.id == pieceId }
                            val isSelected = selectedTrayPieceId == pieceId

                            if (spec != null) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) FocusAccentSoft else Color(0xFF121622))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) FocusAccent else FocusPanelBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedTrayPieceId = if (isSelected) null else pieceId
                                            errorMessage = null
                                        }
                                        .padding(5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // True jigsaw contour preview with artwork slice (NO NUMBERS!)
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val cW = size.width
                                        val cH = size.height
                                        val marginX = cW * 0.18f
                                        val marginY = cH * 0.18f
                                        val pieceW = cW - 2 * marginX
                                        val pieceH = cH - 2 * marginY
                                        val miniPath = buildJigsawPath(
                                            spec = spec,
                                            x = marginX,
                                            y = marginY,
                                            w = pieceW,
                                            h = pieceH
                                        )

                                        clipPath(miniPath) {
                                            val fullW = pieceW * numCols
                                            val fullH = pieceH * numRows
                                            val offsetX = marginX - spec.col * pieceW
                                            val offsetY = marginY - spec.row * pieceH

                                            drawContext.canvas.save()
                                            drawContext.canvas.translate(offsetX, offsetY)
                                            drawScenicArtwork(
                                                theme = selectedTheme,
                                                boardSize = Size(fullW, fullH)
                                            )
                                            drawContext.canvas.restore()
                                        }

                                        drawPath(
                                            path = miniPath,
                                            color = if (isSelected) FocusAccent else Color.White.copy(alpha = 0.55f),
                                            style = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!isVictory) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        restartPuzzle()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Restart Puzzle 🔄", color = FocusWhite, fontSize = 12.sp)
                }
            }
        }
    }
}

// Builds an authentic interlocking jigsaw piece path with Bezier tabs and blanks
private fun buildJigsawPath(
    spec: JigsawPieceSpec,
    x: Float,
    y: Float,
    w: Float,
    h: Float
): Path {
    val path = Path()
    path.moveTo(x, y)

    // TOP EDGE: (x, y) -> (x+w, y)
    addEdgeSegment(path, x, y, x + w, y, spec.topEdge, isHorizontal = true)

    // RIGHT EDGE: (x+w, y) -> (x+w, y+h)
    addEdgeSegment(path, x + w, y, x + w, y + h, spec.rightEdge, isHorizontal = false)

    // BOTTOM EDGE: (x+w, y+h) -> (x, y+h)
    addEdgeSegment(path, x + w, y + h, x, y + h, -spec.bottomEdge, isHorizontal = true)

    // LEFT EDGE: (x, y+h) -> (x, y)
    addEdgeSegment(path, x, y + h, x, y, -spec.leftEdge, isHorizontal = false)

    path.close()
    return path
}

private fun addEdgeSegment(
    path: Path,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    direction: Int,
    isHorizontal: Boolean
) {
    if (direction == 0) {
        path.lineTo(x2, y2)
        return
    }

    val length = if (isHorizontal) (x2 - x1) else (y2 - y1)
    val dir = direction.toFloat()

    if (isHorizontal) {
        val sign = if (length >= 0) 1f else -1f
        val absL = kotlin.math.abs(length)
        val p1x = x1 + sign * absL * 0.35f
        val p2x = x1 + sign * absL * 0.40f
        val p3x = x1 + sign * absL * 0.50f
        val p4x = x1 + sign * absL * 0.60f
        val p5x = x1 + sign * absL * 0.65f

        val neckDepth = -dir * absL * 0.04f
        val knobDepth = -dir * absL * 0.18f
        val knobWidth = absL * 0.16f

        path.lineTo(p1x, y1)
        path.cubicTo(
            p1x + sign * absL * 0.03f, y1 + neckDepth,
            p2x - sign * knobWidth * 0.5f, y1 + knobDepth,
            p3x, y1 + knobDepth
        )
        path.cubicTo(
            p4x + sign * knobWidth * 0.5f, y1 + knobDepth,
            p5x - sign * absL * 0.03f, y1 + neckDepth,
            p5x, y1
        )
        path.lineTo(x2, y2)
    } else {
        val sign = if (length >= 0) 1f else -1f
        val absL = kotlin.math.abs(length)
        val p1y = y1 + sign * absL * 0.35f
        val p2y = y1 + sign * absL * 0.40f
        val p3y = y1 + sign * absL * 0.50f
        val p4y = y1 + sign * absL * 0.60f
        val p5y = y1 + sign * absL * 0.65f

        val neckDepth = dir * absL * 0.04f
        val knobDepth = dir * absL * 0.18f
        val knobWidth = absL * 0.16f

        path.lineTo(x1, p1y)
        path.cubicTo(
            x1 + neckDepth, p1y + sign * absL * 0.03f,
            x1 + knobDepth, p2y - sign * knobWidth * 0.5f,
            x1 + knobDepth, p3y
        )
        path.cubicTo(
            x1 + knobDepth, p4y + sign * knobWidth * 0.5f,
            x1 + neckDepth, p5y - sign * absL * 0.03f,
            x1, p5y
        )
        path.lineTo(x2, y2)
    }
}

// Draws the rich scenic picture artwork across the full board canvas
private fun DrawScope.drawScenicArtwork(theme: PhotoJigsawTheme, boardSize: Size) {
    val w = boardSize.width
    val h = boardSize.height

    when (theme) {
        PhotoJigsawTheme.SUNSET_FREEDOM -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF5E1408),
                        Color(0xFF9E2A0A),
                        Color(0xFFD9480F),
                        Color(0xFFF76707),
                        Color(0xFFFFA919),
                        Color(0xFFFFD43B)
                    ),
                    startY = 0f,
                    endY = h * 0.85f
                ),
                size = boardSize
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF3BF), Color(0xFFFF922B).copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(w * 0.62f, h * 0.45f),
                    radius = w * 0.28f
                ),
                radius = w * 0.28f,
                center = Offset(w * 0.62f, h * 0.45f)
            )

            val hillPath = Path().apply {
                moveTo(0f, h * 0.72f)
                cubicTo(w * 0.25f, h * 0.69f, w * 0.55f, h * 0.75f, w * 0.78f, h * 0.71f)
                cubicTo(w * 0.9f, h * 0.70f, w * 0.95f, h * 0.73f, w, h * 0.72f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = hillPath, color = Color(0xFF140804))

            // 3 Celebrating friends silhouettes
            drawCelebrationSilhouette(this, w * 0.28f, h * 0.82f, w * 0.0022f, 0)
            drawCelebrationSilhouette(this, w * 0.48f, h * 0.80f, w * 0.0024f, 1)
            drawCelebrationSilhouette(this, w * 0.68f, h * 0.81f, w * 0.0023f, 2)
        }

        PhotoJigsawTheme.COSMIC_AURORA -> {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF09061A), Color(0xFF140E36), Color(0xFF1A1A4A), Color(0xFF0D253A))
                ),
                size = boardSize
            )

            val auroraPath = Path().apply {
                moveTo(0f, h * 0.25f)
                cubicTo(w * 0.3f, h * 0.15f, w * 0.6f, h * 0.38f, w, h * 0.20f)
                lineTo(w, h * 0.42f)
                cubicTo(w * 0.6f, h * 0.55f, w * 0.3f, h * 0.32f, 0f, h * 0.45f)
                close()
            }
            drawPath(
                path = auroraPath,
                brush = Brush.linearGradient(
                    listOf(Color(0xFF00FFB2).copy(alpha = 0.6f), Color(0xFF00C3FF).copy(alpha = 0.4f)),
                    start = Offset(0f, h * 0.2f),
                    end = Offset(w, h * 0.4f)
                )
            )

            val starCoords = listOf(
                0.12f to 0.15f, 0.22f to 0.08f, 0.45f to 0.12f, 0.75f to 0.09f,
                0.88f to 0.22f, 0.34f to 0.28f, 0.65f to 0.18f, 0.18f to 0.35f
            )
            starCoords.forEach { (sx, sy) ->
                drawCircle(Color.White, radius = 2.5f, center = Offset(w * sx, h * sy))
            }

            val mountainPath = Path().apply {
                moveTo(0f, h * 0.75f)
                lineTo(w * 0.25f, h * 0.52f)
                lineTo(w * 0.42f, h * 0.68f)
                lineTo(w * 0.65f, h * 0.48f)
                lineTo(w * 0.85f, h * 0.65f)
                lineTo(w, h * 0.58f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = mountainPath, color = Color(0xFF070B14))
        }

        PhotoJigsawTheme.ZEN_SAKURA -> {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF1A2238), Color(0xFF433D60), Color(0xFF8B5D7A), Color(0xFFD4929D))
                ),
                size = boardSize
            )

            drawCircle(
                color = Color(0xFFFFE3E8).copy(alpha = 0.85f),
                radius = w * 0.2f,
                center = Offset(w * 0.5f, h * 0.45f)
            )

            val fujiPath = Path().apply {
                moveTo(w * 0.15f, h * 0.85f)
                lineTo(w * 0.44f, h * 0.48f)
                lineTo(w * 0.56f, h * 0.48f)
                lineTo(w * 0.85f, h * 0.85f)
                lineTo(w, h * 0.85f)
                lineTo(w, h)
                lineTo(0f, h)
                lineTo(0f, h * 0.85f)
                close()
            }
            drawPath(path = fujiPath, color = Color(0xFF171324))

            drawCircle(Color(0xFFFF9EAA), radius = 6f, center = Offset(w * 0.85f, h * 0.2f))
            drawCircle(Color(0xFFFF9EAA), radius = 8f, center = Offset(w * 0.78f, h * 0.25f))
            drawCircle(Color(0xFFFFB8C2), radius = 5f, center = Offset(w * 0.82f, h * 0.28f))
            drawCircle(Color(0xFFFF9EAA), radius = 7f, center = Offset(w * 0.90f, h * 0.26f))
        }
    }
}

private fun drawCelebrationSilhouette(
    drawScope: DrawScope,
    centerX: Float,
    baseY: Float,
    scale: Float,
    pose: Int
) {
    val silhouetteColor = Color(0xFF0D0603)
    val headRadius = 13f * scale

    drawScope.drawCircle(
        color = silhouetteColor,
        radius = headRadius,
        center = Offset(centerX, baseY - 130f * scale)
    )

    val bodyPath = Path().apply {
        when (pose) {
            0 -> {
                moveTo(centerX - 16f * scale, baseY)
                lineTo(centerX - 14f * scale, baseY - 65f * scale)
                lineTo(centerX - 24f * scale, baseY - 105f * scale)
                lineTo(centerX - 50f * scale, baseY - 150f * scale)
                lineTo(centerX - 42f * scale, baseY - 152f * scale)
                lineTo(centerX - 18f * scale, baseY - 110f * scale)
                lineTo(centerX - 6f * scale, baseY - 118f * scale)
                lineTo(centerX + 6f * scale, baseY - 118f * scale)
                lineTo(centerX + 22f * scale, baseY - 105f * scale)
                lineTo(centerX + 38f * scale, baseY - 165f * scale)
                lineTo(centerX + 46f * scale, baseY - 160f * scale)
                lineTo(centerX + 18f * scale, baseY - 95f * scale)
                lineTo(centerX + 14f * scale, baseY - 65f * scale)
                lineTo(centerX + 16f * scale, baseY)
                close()
            }
            1 -> {
                moveTo(centerX - 18f * scale, baseY)
                lineTo(centerX - 15f * scale, baseY - 60f * scale)
                lineTo(centerX - 22f * scale, baseY - 100f * scale)
                lineTo(centerX - 42f * scale, baseY - 170f * scale)
                lineTo(centerX - 34f * scale, baseY - 174f * scale)
                lineTo(centerX - 14f * scale, baseY - 108f * scale)
                lineTo(centerX - 6f * scale, baseY - 115f * scale)
                lineTo(centerX + 6f * scale, baseY - 115f * scale)
                lineTo(centerX + 14f * scale, baseY - 108f * scale)
                lineTo(centerX + 34f * scale, baseY - 174f * scale)
                lineTo(centerX + 42f * scale, baseY - 170f * scale)
                lineTo(centerX + 22f * scale, baseY - 100f * scale)
                lineTo(centerX + 15f * scale, baseY - 60f * scale)
                lineTo(centerX + 18f * scale, baseY)
                close()
            }
            else -> {
                moveTo(centerX - 16f * scale, baseY)
                lineTo(centerX - 12f * scale, baseY - 62f * scale)
                lineTo(centerX - 18f * scale, baseY - 102f * scale)
                lineTo(centerX - 30f * scale, baseY - 162f * scale)
                lineTo(centerX - 22f * scale, baseY - 165f * scale)
                lineTo(centerX - 10f * scale, baseY - 106f * scale)
                lineTo(centerX - 5f * scale, baseY - 116f * scale)
                lineTo(centerX + 7f * scale, baseY - 116f * scale)
                lineTo(centerX + 25f * scale, baseY - 102f * scale)
                lineTo(centerX + 52f * scale, baseY - 155f * scale)
                lineTo(centerX + 58f * scale, baseY - 148f * scale)
                lineTo(centerX + 22f * scale, baseY - 92f * scale)
                lineTo(centerX + 16f * scale, baseY - 62f * scale)
                lineTo(centerX + 18f * scale, baseY)
                close()
            }
        }
    }
    drawScope.drawPath(path = bodyPath, color = silhouetteColor)
}
