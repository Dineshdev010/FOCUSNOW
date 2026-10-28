package com.focusflow.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import com.focusflow.app.util.PermissionHelper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.FocusSoundscape
import com.focusflow.app.ui.components.FocusHourglassFlameIcon
import com.focusflow.app.ui.theme.*
import com.focusflow.app.util.FocusAlarmManager
import com.focusflow.app.viewmodel.FocusFlowViewModel

@Composable
fun FocusNowAnimatedHourglass(
    isRunning: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hourglassSandFlow")
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sandParticlePhase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val halfW = w / 2f
        val halfH = h / 2f
        val neckWidth = w * 0.16f
        val neckHeight = h * 0.08f

        // Glass contour path (hourglass bulbs & waist)
        val glassPath = Path().apply {
            moveTo(w * 0.18f, h * 0.08f)
            lineTo(w * 0.82f, h * 0.08f)
            cubicTo(w * 0.82f, h * 0.32f, halfW + neckWidth, halfH - neckHeight, halfW + neckWidth / 2, halfH)
            cubicTo(halfW + neckWidth, halfH + neckHeight, w * 0.82f, h * 0.68f, w * 0.82f, h * 0.92f)
            lineTo(w * 0.18f, h * 0.92f)
            cubicTo(w * 0.18f, h * 0.68f, halfW - neckWidth, halfH + neckHeight, halfW - neckWidth / 2, halfH)
            cubicTo(halfW - neckWidth, halfH - neckHeight, w * 0.18f, h * 0.32f, w * 0.18f, h * 0.08f)
            close()
        }

        // Soft glowing glass border using theme lime accent
        drawPath(
            path = glassPath,
            color = Color(0xFF84CC16).copy(alpha = 0.55f),
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        )

        // Top & bottom caps matching logo electric lime (#84CC16)
        val capColor = Color(0xFF84CC16)
        drawRoundRect(
            color = capColor,
            topLeft = Offset(w * 0.10f, h * 0.02f),
            size = Size(w * 0.80f, h * 0.07f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
        )
        drawRoundRect(
            color = capColor,
            topLeft = Offset(w * 0.10f, h * 0.91f),
            size = Size(w * 0.80f, h * 0.07f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
        )

        val sandColor = Color(0xFF10B981) // Emerald reservoir matching logo
        val upperFraction = progress.coerceIn(0f, 1f)
        val lowerFraction = (1f - progress).coerceIn(0f, 1f)

        // Upper sand level (decreases with session progress)
        if (upperFraction > 0.04f) {
            val upperTopY = h * 0.10f + (1f - upperFraction) * (halfH - h * 0.12f)
            val upperWidth = w * 0.58f * upperFraction
            val upperSandPath = Path().apply {
                moveTo(halfW - upperWidth / 2, upperTopY)
                lineTo(halfW + upperWidth / 2, upperTopY)
                lineTo(halfW + neckWidth / 3, halfH - neckHeight / 2)
                lineTo(halfW - neckWidth / 3, halfH - neckHeight / 2)
                close()
            }
            drawPath(path = upperSandPath, color = sandColor.copy(alpha = 0.88f))
        }

        // Sand flow stream trickling through the neck
        if (isRunning && upperFraction > 0.02f) {
            drawLine(
                color = Color(0xFFA3E635),
                start = Offset(halfW, halfH - neckHeight),
                end = Offset(halfW, h * 0.88f - lowerFraction * (h * 0.32f)),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Falling sand golden ember particle
            val speckY = halfH + (flowPhase * 0.36f) * h
            if (speckY < h * 0.88f) {
                drawCircle(
                    color = Color(0xFFFBBF24),
                    radius = 1.3.dp.toPx(),
                    center = Offset(halfW, speckY)
                )
            }
        }

        // Lower chamber: Burning focus flame mound (matches logo bottom chamber)
        if (lowerFraction > 0.04f) {
            val moundHeight = lowerFraction * (h * 0.32f)
            val moundWidth = w * 0.58f * lowerFraction
            val lowerMoundPath = Path().apply {
                moveTo(halfW - moundWidth / 2, h * 0.90f)
                quadraticTo(halfW, h * 0.90f - moundHeight, halfW + moundWidth / 2, h * 0.90f)
                close()
            }
            // Emerald flame base
            drawPath(path = lowerMoundPath, color = Color(0xFF10B981).copy(alpha = 0.92f))

            // Burning amber flame tongue when mound forms
            if (lowerFraction > 0.12f) {
                val innerHeight = moundHeight * 0.72f
                val innerWidth = moundWidth * 0.65f
                val innerFlamePath = Path().apply {
                    moveTo(halfW - innerWidth / 2, h * 0.90f)
                    quadraticTo(halfW, h * 0.90f - innerHeight, halfW + innerWidth / 2, h * 0.90f)
                    close()
                }
                drawPath(path = innerFlamePath, color = Color(0xFFF59E0B).copy(alpha = 0.95f))

                // Golden hot flare core
                val coreHeight = innerHeight * 0.50f
                val coreWidth = innerWidth * 0.45f
                val corePath = Path().apply {
                    moveTo(halfW - coreWidth / 2, h * 0.90f)
                    quadraticTo(halfW, h * 0.90f - coreHeight, halfW + coreWidth / 2, h * 0.90f)
                    close()
                }
                drawPath(path = corePath, color = Color(0xFFFBBF24))
            }
        }
    }
}

@Composable
fun FocusScreen(viewModel: FocusFlowViewModel) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var isTestingTimerAlarm by remember { mutableStateOf(false) }

    var hasOverlayPermission by remember { mutableStateOf(PermissionHelper.hasOverlayPermission(context)) }
    var showFloatingPermissionDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = PermissionHelper.hasOverlayPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            FocusAlarmManager.stopAlarm(context)
        }
    }

    val currentMode = viewModel.modes[viewModel.currentModeIndex]
    val totalSeconds = currentMode.durationSeconds
    val remaining = viewModel.timerRemainingSeconds
    val progress = if (totalSeconds > 0) (remaining.toFloat() / totalSeconds.toFloat()) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "timerProgress"
    )

    // Pulse animation when running
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (viewModel.isTimerRunning) 0.5f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val activeColor = if (viewModel.currentModeIndex == 0) FocusAccent else CategoryWork

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header: Leaf Icon + "FocusNow" + "Better Focus • Bigger Goals" + Crown Points Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FocusHourglassFlameIcon(sizeDp = 28)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "FocusNow",
                        color = FocusWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Better Focus • Bigger Goals",
                        color = FocusSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Crown Points Badge Pill (Regain Style)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(FocusPanelLight)
                        .border(1.dp, FocusAccent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showRewardsStoreModal = true
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${viewModel.focusCoins}",
                            color = FocusAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Settings Shortcut Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(FocusPanelLight)
                        .border(1.dp, FocusBorder, CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showSettingsScreen = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚙️", fontSize = 13.sp)
                }
            }
        }

        // Floating Focus Island Permission Banner
        if (viewModel.isFloatingPillEnabled && !hasOverlayPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1B18))
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        PermissionHelper.openOverlaySettings(context)
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🏝️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Floating Screen Permission Needed",
                                color = Color(0xFFFBBF24),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Enable 'Appear on top' to float live timer over other apps",
                                color = FocusSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF59E0B))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Enable",
                            color = FocusBackground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Mode Selector (Focus, Short Break, Long Break - 1:1 Regain Segmented Capsule)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FocusPanel, RoundedCornerShape(22.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val modesData = listOf(
                Triple("🎯 Focus", 0, viewModel.customFocusMinutes),
                Triple("☕ Short Break", 1, viewModel.customShortBreakMinutes),
                Triple("🌙 Long Break", 2, viewModel.customLongBreakMinutes)
            )
            modesData.forEach { (label, index, _) ->
                val isSelected = viewModel.currentModeIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) FocusAccent else Color.Transparent)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (viewModel.isTimerRunning && viewModel.currentModeIndex == 0 && index != 0) {
                                viewModel.openJigsawPuzzle("Focus Session Active: Solve to earn break")
                            } else {
                                viewModel.selectMode(index)
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) FocusBackground else FocusSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Duration Preset Chips (15m, 25m, 35m, 45m, 60m, Custom)
        val activeMinutes = if (viewModel.currentModeIndex == 0) viewModel.customFocusMinutes
            else if (viewModel.currentModeIndex == 1) viewModel.customShortBreakMinutes
            else viewModel.customLongBreakMinutes

        val presetList = listOf(15, 25, 35, 45, 60)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presetList.forEach { mins ->
                val isSelected = activeMinutes == mins
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) FocusAccent else FocusPanel)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (viewModel.currentModeIndex == 0) {
                                viewModel.setFocusDurationMinutes(mins)
                            } else {
                                viewModel.adjustTimerMinutes(mins - activeMinutes)
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${mins}m",
                        color = if (isSelected) FocusBackground else FocusWhite,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                }
            }

            // Custom Duration Chip
            val isCustomActive = !presetList.contains(activeMinutes)
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isCustomActive) FocusAccentSoft else FocusPanel)
                    .border(1.dp, if (isCustomActive) FocusAccent else FocusBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showCustomDurationDialog = true
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎛️", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isCustomActive) "${activeMinutes}m" else "Custom",
                        color = if (isCustomActive) FocusAccent else FocusSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // MIXED TIMER UI: Cyber Neon Gradient Rings + Flowing Hourglass Visualizer
        Box(
            modifier = Modifier.size(264.dp),
            contentAlignment = Alignment.Center
        ) {
            // Dynamic Sweep Gradient Arc Canvas matching Hourglass Flame logo colors
            val neonSweepBrush = remember(FocusAccent) {
                Brush.sweepGradient(
                    listOf(
                        Color(0xFF84CC16), // Electric Lime
                        Color(0xFF10B981), // Emerald
                        Color(0xFFFBBF24), // Golden Amber Flame
                        Color(0xFFF59E0B), // Vibrant Amber
                        Color(0xFF84CC16)  // Electric Lime
                    )
                )
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 13.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                // Background track circular channel
                drawCircle(
                    color = Color(0xFF141C11),
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokeWidth)
                )

                // Inner subtle guidance ring
                drawCircle(
                    color = Color(0xFF1C2B16),
                    radius = radius - 14.dp.toPx(),
                    center = centerOffset,
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Glowing Neon Gradient Sweep Arc
                val sweepAngle = (360f * animatedProgress).coerceAtLeast(1f)
                drawArc(
                    brush = neonSweepBrush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Glowing indicator dot at arc tip
                val angleRad = Math.toRadians((sweepAngle - 90f).toDouble())
                val tipX = centerOffset.x + (radius * Math.cos(angleRad)).toFloat()
                val tipY = centerOffset.y + (radius * Math.sin(angleRad)).toFloat()

                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                    radius = 9.dp.toPx(),
                    center = Offset(tipX, tipY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(tipX, tipY)
                )
            }

            // Glow overlay aura
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(FocusAccent.copy(alpha = pulseAlpha * 0.4f), CircleShape)
            )

            // Inner Timer Display: Session Tag + Time + Flowing Hourglass + Subtext
            val minutes = remaining / 60
            val seconds = remaining % 60
            val timeString = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.clickable(enabled = !viewModel.isTimerRunning) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showCustomDurationDialog = true
                }
            ) {
                // Session title tag
                val modeTitle = viewModel.modes[viewModel.currentModeIndex].title.uppercase()
                Text(
                    text = "$modeTitle SESSION",
                    color = Color(0xFF86EFAC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Giant Digital Countdown Time
                Text(
                    text = timeString,
                    color = FocusWhite,
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                // Animated Flowing Hourglass Sand Visualizer
                FocusNowAnimatedHourglass(
                    isRunning = viewModel.isTimerRunning,
                    progress = animatedProgress,
                    modifier = Modifier.size(width = 34.dp, height = 40.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(if (viewModel.isTimerRunning) FocusAccent else Color(0xFF22C55E), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.isTimerRunning) "Flow active • Sand flowing" else "Ready when you are",
                        color = if (viewModel.isTimerRunning) FocusAccent else FocusSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Duration Capsule below Dial: 🧠 Focus duration • 25 minutes
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(FocusPanelLight)
                .clickable(enabled = !viewModel.isTimerRunning) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showCustomDurationDialog = true
                }
                .padding(horizontal = 16.dp, vertical = 7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🧠", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Focus duration • $activeMinutes minutes",
                    color = Color(0xFFBBF7D0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Main CTA Button: ▶ Start Focus / ⏸ Pause Focus (1:1 Regain Green Capsule)
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (viewModel.isTimerRunning) {
                    viewModel.pauseTimer()
                } else {
                    if (viewModel.isFloatingPillEnabled && !hasOverlayPermission) {
                        showFloatingPermissionDialog = true
                    } else {
                        viewModel.startTimer()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (viewModel.isTimerRunning) FocusPanelLight else FocusAccent
            ),
            shape = RoundedCornerShape(27.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (viewModel.isTimerRunning) "⏸" else "▶",
                    color = if (viewModel.isTimerRunning) FocusWhite else FocusBackground,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (viewModel.isTimerRunning) "Pause Focus" else "Start Focus",
                    color = if (viewModel.isTimerRunning) FocusWhite else FocusBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Minute Adjustment Buttons: — 5 min  |  + 5 min (1:1 Regain Style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(FocusPanel)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.adjustTimerMinutes(-5)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "— 5 min",
                    color = FocusWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(FocusPanel)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.adjustTimerMinutes(5)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ 5 min",
                    color = FocusWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Subtext: 🪙 Complete your session to earn 25 points
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text("🪙", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Complete your session to earn 25 points",
                color = FocusSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Session Completion Alarm Card
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (viewModel.isTimerAlarmEnabled) FocusAccent.copy(alpha = 0.35f) else FocusBorder, RoundedCornerShape(18.dp))
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
                                .background(if (viewModel.isTimerAlarmEnabled) FocusAccent.copy(alpha = 0.2f) else FocusPanelLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (viewModel.isTimerAlarmEnabled) "⏰" else "🔕", fontSize = 17.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Session Completion Alarm",
                                color = FocusWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (viewModel.isTimerAlarmEnabled) "Audio & haptic chime when timer ends" else "Silent completion",
                                color = if (viewModel.isTimerAlarmEnabled) FocusAccent else FocusSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = viewModel.isTimerAlarmEnabled,
                        onCheckedChange = { checked ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.updateTimerAlarmSettings(
                                sound = viewModel.timerAlarmSound,
                                enabled = checked,
                                vibrate = viewModel.isTimerVibrateEnabled
                            )
                            if (!checked && isTestingTimerAlarm) {
                                FocusAlarmManager.stopAlarm(context)
                                isTestingTimerAlarm = false
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

                if (viewModel.isTimerAlarmEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))

                    if (viewModel.isTimerRunning) {
                        val alarmTargetMillis = System.currentTimeMillis() + (viewModel.timerRemainingSeconds * 1000L)
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = alarmTargetMillis }
                        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                        val minute = cal.get(java.util.Calendar.MINUTE)
                        val amPm = if (hour >= 12) "PM" else "AM"
                        val displayHour = when {
                            hour == 0 -> 12
                            hour > 12 -> hour - 12
                            else -> hour
                        }
                        val formattedTime = String.format(java.util.Locale.US, "%d:%02d %s", displayHour, minute, amPm)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FocusAccent.copy(alpha = 0.15f))
                                .border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🟢", fontSize = 9.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Alarm Scheduled for $formattedTime",
                                        color = FocusAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Exact Wake-Up ⚡",
                                    color = Color(0xFFBBF7D0),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    val timerSounds = listOf(
                        Pair(FocusAlarmManager.ALARM_SOUND_GENTLE_CHIME, "🔔 Gentle Chime"),
                        Pair(FocusAlarmManager.ALARM_SOUND_ZEN_GONG, "🧘 Zen Gong"),
                        Pair(FocusAlarmManager.ALARM_SOUND_CLASSIC, "⏰ Classic Alarm"),
                        Pair(FocusAlarmManager.ALARM_SOUND_DIGITAL_BELL, "📟 Digital Bell"),
                        Pair(FocusAlarmManager.ALARM_SOUND_SYSTEM, "📱 System Alarm")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(timerSounds) { (soundKey, soundLabel) ->
                            val isSel = viewModel.timerAlarmSound == soundKey
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) FocusAccent.copy(alpha = 0.25f) else FocusPanelLight)
                                    .border(
                                        1.dp,
                                        if (isSel) FocusAccent else FocusBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.updateTimerAlarmSettings(
                                            sound = soundKey,
                                            enabled = true,
                                            vibrate = viewModel.isTimerVibrateEnabled
                                        )
                                        if (isTestingTimerAlarm) {
                                            FocusAlarmManager.playAlarm(context, soundKey, loop = false, vibrate = viewModel.isTimerVibrateEnabled)
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = soundLabel,
                                    color = if (isSel) FocusAccent else FocusWhite,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.updateTimerAlarmSettings(
                                        sound = viewModel.timerAlarmSound,
                                        enabled = true,
                                        vibrate = !viewModel.isTimerVibrateEnabled
                                    )
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(if (viewModel.isTimerVibrateEnabled) "📳" else "🔕", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (viewModel.isTimerVibrateEnabled) "Vibration ON" else "Vibration OFF",
                                color = if (viewModel.isTimerVibrateEnabled) FocusWhite else FocusSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isTestingTimerAlarm) Color(0xFFEF4444).copy(alpha = 0.2f) else FocusAccent.copy(alpha = 0.15f))
                                .border(1.dp, if (isTestingTimerAlarm) Color(0xFFEF4444) else FocusAccent, RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (isTestingTimerAlarm) {
                                        FocusAlarmManager.stopAlarm(context)
                                        isTestingTimerAlarm = false
                                    } else {
                                        isTestingTimerAlarm = true
                                        FocusAlarmManager.playAlarm(
                                            context = context,
                                            soundName = viewModel.timerAlarmSound,
                                            loop = false,
                                            vibrate = viewModel.isTimerVibrateEnabled
                                        )
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isTestingTimerAlarm) "⏹️ Stop" else "▶️ Test Sound",
                                color = if (isTestingTimerAlarm) Color(0xFFFCA5A5) else FocusAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Card 1: NEED A MENTAL RESET? (Solve a quick puzzle during your break)
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.openJigsawPuzzle("Mental Reset: Solve to earn 5-min break")
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FocusAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧩", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NEED A MENTAL RESET?",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Solve a quick puzzle during your break.",
                        color = FocusWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Play →", color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("›", color = FocusSecondary, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card 2: CURRENT FOCUS (Linked Task with target icon)
        val linkedTask = viewModel.selectedTaskForFocus
        val uncompletedTasks = viewModel.tasks.filter { !it.isCompleted }

        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (uncompletedTasks.isEmpty()) {
                        viewModel.selectedTab = "Tasks"
                    } else {
                        val curIdx = uncompletedTasks.indexOfFirst { it.id == linkedTask?.id }
                        viewModel.selectedTaskForFocus = if (curIdx == -1 || curIdx >= uncompletedTasks.size - 1) {
                            if (curIdx == uncompletedTasks.size - 1) null else uncompletedTasks.firstOrNull()
                        } else {
                            uncompletedTasks[curIdx + 1]
                        }
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Current focus",
                    color = FocusSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FocusAccentSoft)
                            .border(1.5.dp, FocusAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎯", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = linkedTask?.title ?: if (uncompletedTasks.isEmpty()) "No active tasks (tap to create)" else "Link a task to this session",
                            color = if (linkedTask != null) FocusWhite else FocusSecondary,
                            fontSize = 14.sp,
                            fontWeight = if (linkedTask != null) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (linkedTask != null) "${linkedTask.category} • Tap to cycle (${uncompletedTasks.size} tasks)" else "Tap to link • Earn +15 🪙 bonus coins upon finish",
                            color = if (linkedTask != null) FocusAccent else FocusMuted,
                            fontSize = 11.sp
                        )
                    }

                    Text("›", color = FocusSecondary, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Focus Lockdown & Productive Whitelist Card
        val isLockdownActive = viewModel.isTimerRunning && viewModel.currentModeIndex == 0
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isLockdownActive) FocusPanelLight else FocusPanel
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.5.dp,
                    if (isLockdownActive) FocusAccent else FocusAccent.copy(alpha = 0.3f),
                    RoundedCornerShape(18.dp)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("\uD83D\uDEE1\uFE0F", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "STRICT FOCUS LOCKDOWN",
                                color = FocusAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isLockdownActive) "Active: Distractions Locked" else "Calls & Productive Apps Only",
                                color = FocusWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLockdownActive) FocusAccent.copy(alpha = 0.2f) else FocusPanelLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isLockdownActive) "● ACTIVE" else "ARMED",
                            color = if (isLockdownActive) FocusAccent else FocusSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isLockdownActive)
                        "\uD83D\uDD12 All non-productive apps and doomscrolling are blocked. Only voice calls and enabled tools open."
                    else
                        "When Deep Focus starts, incoming calls and productive tools remain accessible. All other apps lock automatically.",
                    color = FocusSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                val enabledProductiveCount = viewModel.productiveAppItems.count { it.isEnabled }
                val isWorkMode = viewModel.isWorkModeEnabled || viewModel.repository.isWorkModeActive()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\uD83D\uDCDE", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calls Allowed", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("•", color = FocusSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$enabledProductiveCount Productive", color = FocusWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(FocusAccent.copy(alpha = 0.15f))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.showProductiveWhitelistModal = true
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Configure \u2699\uFE0F",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Strict Work Mode Row (Teams, Outlook & WhatsApp Only)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isWorkMode) FocusAccent.copy(alpha = 0.12f) else FocusPanelLight)
                        .border(
                            1.dp,
                            if (isWorkMode) FocusAccent.copy(alpha = 0.4f) else FocusPanelBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleWorkMode(!viewModel.isWorkModeEnabled)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text("💼", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Strict Work Mode",
                                color = FocusWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isWorkMode) "Teams, Outlook & WhatsApp only" else "Restrict to office tools",
                                color = if (isWorkMode) FocusAccent else FocusSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = viewModel.isWorkModeEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleWorkMode(it)
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

        Spacer(modifier = Modifier.height(16.dp))

        // Notification Shield & Focus Digest Card
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
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
                                .background(FocusAccentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛡️", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NOTIFICATION SHIELD",
                                color = FocusAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Focus Digest & Silencer",
                                color = FocusWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Switch(
                        checked = viewModel.isNotificationShieldEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleNotificationShield(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FocusBackground,
                            checkedTrackColor = FocusAccent,
                            uncheckedThumbColor = FocusSecondary,
                            uncheckedTrackColor = FocusPanelLight
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Silences distracting alerts and saves them into your Focus Digest. Phone calls and productive apps are never blocked.",
                    color = FocusSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // View Digest Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (viewModel.batchedNotifications.isNotEmpty()) FocusAccent.copy(alpha = 0.2f) else FocusPanelLight)
                            .border(1.dp, if (viewModel.batchedNotifications.isNotEmpty()) FocusAccent else FocusPanelBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.refreshBatchedNotifications()
                                viewModel.showFocusDigestModal = true
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📬", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (viewModel.batchedNotifications.isEmpty()) "Focus Digest" else "Digest (${viewModel.batchedNotifications.size})",
                                color = if (viewModel.batchedNotifications.isNotEmpty()) FocusAccent else FocusWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!viewModel.hasNotificationListenerPermission) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CategoryUrgent.copy(alpha = 0.15f))
                                .border(1.dp, CategoryUrgent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.openNotificationListenerSettings()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Grant Access",
                                    color = CategoryUrgent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Quick preview / test button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(FocusPanelLight)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.addMockBatchedNotificationForPreview()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ Test Alert",
                                color = FocusSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Regain Soundscapes: Ambient Focus Sound Selector
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AMBIENT SOUNDSCAPES",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Auditory Flow Entrainment",
                            color = FocusWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (viewModel.activeSoundscape != FocusSoundscape.SILENT) {
                        Text(
                            text = "\uD83D\uDD0A Playing",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FocusSoundscape.values()) { soundscape ->
                        val isSelected = viewModel.activeSoundscape == soundscape
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                                .border(1.dp, if (isSelected) FocusAccent else FocusBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.selectSoundscape(soundscape)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(soundscape.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = soundscape.title,
                                        color = if (isSelected) FocusAccent else FocusWhite,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = soundscape.subtitle,
                                        color = FocusSecondary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Strict Lockdown Setting Card
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(if (viewModel.isStrictModeEnabled) CategoryUrgent.copy(alpha = 0.2f) else FocusPanelLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("\uD83D\uDD12", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Strict Lockdown Mode",
                        color = FocusWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Locks distracting apps during session. Unlocks require 50 Coins or solving Code Challenges.",
                        color = FocusSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }

                Switch(
                    checked = viewModel.isStrictModeEnabled,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.isStrictModeEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = FocusBackground,
                        checkedTrackColor = FocusAccent,
                        uncheckedThumbColor = FocusSecondary,
                        uncheckedTrackColor = FocusPanelLight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Scheduled Focus Sprints Card
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "SCHEDULED FOCUS SPRINTS",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Automated Focus Blocks",
                            color = FocusWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(FocusAccent.copy(alpha = 0.15f))
                            .border(1.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.showAddScheduleModal = true
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+ New Sprint 📅",
                            color = FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (viewModel.focusSchedules.isEmpty()) {
                    Text(
                        text = "No focus schedules configured. Tap '+ New Sprint' to set automated focus time blocks for deep work or study.",
                        color = FocusSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                } else {
                    viewModel.focusSchedules.forEach { schedule ->
                        val isScheduleActive = schedule.isCurrentlyActive()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isScheduleActive) FocusAccentSoft else FocusPanelLight)
                                .border(1.dp, if (isScheduleActive) FocusAccent else FocusBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.openScheduleForEditing(schedule)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(schedule.emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = schedule.name,
                                            color = FocusWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isScheduleActive) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(FocusAccent)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE NOW",
                                                    color = FocusBackground,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${schedule.formattedTimeRange()} • ${if (schedule.targetAppPackages.isNotEmpty()) "${schedule.targetAppPackages.size} apps blocked" else schedule.daysSummary}",
                                        color = FocusSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = schedule.isEnabled,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.toggleFocusSchedule(schedule.id)
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
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pomodoro Cycle Indicator (4 sessions)
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanelLight),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Daily Flow Cycle", color = FocusSecondary, fontSize = 11.sp)
                    Text("Session ${(viewModel.completedSessions % 4) + 1} of 4", color = FocusWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val currentStep = (viewModel.completedSessions % 4)
                    for (i in 0..3) {
                        val isDone = i < currentStep
                        val isCurrent = i == currentStep
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isDone -> FocusAccent
                                        isCurrent -> FocusAccent.copy(alpha = 0.5f)
                                        else -> FocusBorder
                                    }
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(44.dp))
    }

    // Custom Timer Duration Dialog
    if (showCustomDurationDialog) {
        var inputMins by remember {
            mutableStateOf(
                (if (viewModel.currentModeIndex == 0) viewModel.customFocusMinutes
                else if (viewModel.currentModeIndex == 1) viewModel.customShortBreakMinutes
                else viewModel.customLongBreakMinutes).toString()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FocusBackground.copy(alpha = 0.88f))
                .statusBarsPadding()
                .navigationBarsPadding()
                .clickable { showCustomDurationDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanel),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .border(1.5.dp, FocusBorder, RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱️ SET TIMER DURATION",
                            color = FocusAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(FocusPanelLight)
                                .clickable { showCustomDurationDialog = false }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("✕", color = FocusSecondary, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Customize ${viewModel.modes[viewModel.currentModeIndex].title} duration in minutes:",
                        color = FocusSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stepper Row + Text Field
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                val currentVal = inputMins.toIntOrNull() ?: 25
                                if (currentVal > 5) inputMins = (currentVal - 5).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Text("-5", color = FocusWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        OutlinedTextField(
                            value = inputMins,
                            onValueChange = { newText ->
                                if (newText.isEmpty() || (newText.all { it.isDigit() } && newText.length <= 3)) {
                                    inputMins = newText
                                }
                            },
                            textStyle = TextStyle(
                                color = FocusWhite,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            modifier = Modifier.width(110.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FocusAccent,
                                unfocusedBorderColor = FocusBorder
                            )
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                val currentVal = inputMins.toIntOrNull() ?: 25
                                if (currentVal < 360) inputMins = (currentVal + 5).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Text("+5", color = FocusWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quick Preset Chips inside dialog
                    Text("Or select quick preset:", color = FocusSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(15, 25, 45, 60, 90).forEach { pMins ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (inputMins == pMins.toString()) FocusAccentSoft else FocusPanelLight)
                                    .border(1.dp, if (inputMins == pMins.toString()) FocusAccent else FocusBorder, RoundedCornerShape(10.dp))
                                    .clickable { inputMins = pMins.toString() }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${pMins}m",
                                    color = if (inputMins == pMins.toString()) FocusAccent else FocusWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = {
                            val mins = inputMins.toIntOrNull()?.coerceIn(1, 360) ?: 25
                            if (viewModel.currentModeIndex == 0) {
                                viewModel.setFocusDurationMinutes(mins)
                            } else {
                                val curr = if (viewModel.currentModeIndex == 1) viewModel.customShortBreakMinutes else viewModel.customLongBreakMinutes
                                viewModel.adjustTimerMinutes(mins - curr)
                            }
                            showCustomDurationDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Apply & Set Duration", color = FocusBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (viewModel.showSessionHistoryModal) {
        FocusSessionHistoryDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showSessionHistoryModal = false }
        )
    }

    if (showFloatingPermissionDialog) {
        Dialog(onDismissRequest = { showFloatingPermissionDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusPanel),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, FocusAccent.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏝️", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Enable Floating Screen",
                        color = FocusWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "FocusNow features a floating live countdown island that stays on your screen while you use your allowed apps or do work.\n\nTo float over your screen, Android requires the 'Display over other apps' permission.",
                        color = FocusSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showFloatingPermissionDialog = false
                            PermissionHelper.openOverlaySettings(context)
                            viewModel.startTimer()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Enable in Settings ⚙️",
                            color = FocusBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = {
                            showFloatingPermissionDialog = false
                            viewModel.startTimer()
                        }
                    ) {
                        Text(
                            text = "Start Without Floating",
                            color = FocusSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}