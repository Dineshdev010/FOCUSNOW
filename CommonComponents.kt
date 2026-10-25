package com.focusflow.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.model.TaskPriority
import com.focusflow.app.ui.theme.*

@Composable
fun FocusHourglassFlameIcon(
    sizeDp: Int = 24,
    accentColor: Color = FocusAccent
) {
    Canvas(modifier = Modifier.size(sizeDp.dp)) {
        val w = size.width
        val h = size.height

        // Top & bottom cap bars
        drawLine(
            color = accentColor,
            start = Offset(w * 0.15f, h * 0.12f),
            end = Offset(w * 0.85f, h * 0.12f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = accentColor,
            start = Offset(w * 0.15f, h * 0.88f),
            end = Offset(w * 0.85f, h * 0.88f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Hourglass outer glass outline
        val glassPath = Path().apply {
            moveTo(w * 0.22f, h * 0.15f)
            lineTo(w * 0.46f, h * 0.48f)
            lineTo(w * 0.46f, h * 0.52f)
            lineTo(w * 0.22f, h * 0.85f)

            moveTo(w * 0.78f, h * 0.15f)
            lineTo(w * 0.54f, h * 0.48f)
            lineTo(w * 0.54f, h * 0.52f)
            lineTo(w * 0.78f, h * 0.85f)
        }
        drawPath(
            path = glassPath,
            color = accentColor.copy(alpha = 0.7f),
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        )

        // Top sand level
        val topSand = Path().apply {
            moveTo(w * 0.28f, h * 0.24f)
            lineTo(w * 0.72f, h * 0.24f)
            lineTo(w * 0.52f, h * 0.46f)
            lineTo(w * 0.48f, h * 0.46f)
            close()
        }
        drawPath(
            path = topSand,
            color = accentColor.copy(alpha = 0.45f)
        )

        // Falling sand drops
        drawCircle(
            color = accentColor,
            radius = 1.dp.toPx(),
            center = Offset(w * 0.50f, h * 0.50f)
        )
        drawCircle(
            color = Color(0xFFFBBF24),
            radius = 1.dp.toPx(),
            center = Offset(w * 0.50f, h * 0.57f)
        )

        // Bottom flame
        val flamePath = Path().apply {
            moveTo(w * 0.50f, h * 0.60f)
            cubicTo(w * 0.62f, h * 0.68f, w * 0.68f, h * 0.76f, w * 0.62f, h * 0.83f)
            cubicTo(w * 0.56f, h * 0.86f, w * 0.44f, h * 0.86f, w * 0.38f, h * 0.83f)
            cubicTo(w * 0.32f, h * 0.76f, w * 0.38f, h * 0.68f, w * 0.50f, h * 0.60f)
            close()
        }
        drawPath(
            path = flamePath,
            color = Color(0xFFF59E0B)
        )

        // Inner flame core
        val innerFlame = Path().apply {
            moveTo(w * 0.50f, h * 0.68f)
            cubicTo(w * 0.56f, h * 0.73f, w * 0.58f, h * 0.78f, w * 0.54f, h * 0.82f)
            cubicTo(w * 0.51f, h * 0.84f, w * 0.49f, h * 0.84f, w * 0.46f, h * 0.82f)
            cubicTo(w * 0.42f, h * 0.78f, w * 0.44f, h * 0.73f, w * 0.50f, h * 0.68f)
            close()
        }
        drawPath(
            path = innerFlame,
            color = Color(0xFFFEF08A)
        )
    }
}

@Composable
fun FocusBrandBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(36.dp)
            .background(FocusAccentSoft, RoundedCornerShape(12.dp))
            .padding(horizontal = 11.dp)
    ) {
        FocusHourglassFlameIcon(sizeDp = 18)
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = "FOCUSNOW",
            color = FocusAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FocusPanel)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                color = FocusSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = FocusWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = FocusMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun CategoryBadge(category: String) {
    val (bg, textColor) = when (category.lowercase()) {
        "work", "dev" -> CategoryWork.copy(alpha = 0.2f) to CategoryWork
        "study" -> CategoryStudy.copy(alpha = 0.2f) to CategoryStudy
        "personal" -> CategoryPersonal.copy(alpha = 0.2f) to CategoryPersonal
        "health", "habit" -> CategoryHealth.copy(alpha = 0.2f) to CategoryHealth
        else -> FocusPanelLight to FocusSecondary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = category,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PriorityBadge(priority: TaskPriority) {
    val (color, text) = when (priority) {
        TaskPriority.HIGH -> PriorityHigh to "High"
        TaskPriority.MEDIUM -> PriorityMedium to "Med"
        TaskPriority.LOW -> PriorityLow to "Low"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CheckmarkIcon(tint: Color = FocusBackground) {
    Canvas(modifier = Modifier.size(12.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.5f)
            lineTo(size.width * 0.42f, size.height * 0.78f)
            lineTo(size.width * 0.88f, size.height * 0.22f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun NavHomeIcon(tint: Color = FocusSecondary) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.15f)
            lineTo(size.width * 0.88f, size.height * 0.48f)
            lineTo(size.width * 0.88f, size.height * 0.88f)
            lineTo(size.width * 0.12f, size.height * 0.88f)
            lineTo(size.width * 0.12f, size.height * 0.48f)
            close()
        }
        drawPath(path = path, color = tint, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun NavFocusIcon(tint: Color = FocusSecondary) {
    Canvas(modifier = Modifier.size(20.dp)) {
        drawCircle(
            color = tint,
            radius = size.minDimension * 0.38f,
            style = Stroke(width = 1.8.dp.toPx())
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.5f, size.height * 0.05f),
            end = Offset(size.width * 0.5f, size.height * 0.18f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.5f, size.height * 0.5f),
            end = Offset(size.width * 0.65f, size.height * 0.4f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun NavTasksIcon(tint: Color = FocusSecondary) {
    Canvas(modifier = Modifier.size(20.dp)) {
        drawLine(
            color = tint,
            start = Offset(size.width * 0.38f, size.height * 0.28f),
            end = Offset(size.width * 0.88f, size.height * 0.28f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.38f, size.height * 0.52f),
            end = Offset(size.width * 0.88f, size.height * 0.52f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.38f, size.height * 0.76f),
            end = Offset(size.width * 0.88f, size.height * 0.76f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(color = tint, radius = 1.8.dp.toPx(), center = Offset(size.width * 0.18f, size.height * 0.28f))
        drawCircle(color = tint, radius = 1.8.dp.toPx(), center = Offset(size.width * 0.18f, size.height * 0.52f))
        drawCircle(color = tint, radius = 1.8.dp.toPx(), center = Offset(size.width * 0.18f, size.height * 0.76f))
    }
}

@Composable
fun NavProgressIcon(tint: Color = FocusSecondary) {
    Canvas(modifier = Modifier.size(20.dp)) {
        drawLine(
            color = tint,
            start = Offset(size.width * 0.25f, size.height * 0.85f),
            end = Offset(size.width * 0.25f, size.height * 0.55f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.52f, size.height * 0.85f),
            end = Offset(size.width * 0.52f, size.height * 0.25f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.79f, size.height * 0.85f),
            end = Offset(size.width * 0.79f, size.height * 0.40f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun NavHabitsIcon(tint: Color = FocusSecondary) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.58f, size.height * 0.10f)
            lineTo(size.width * 0.22f, size.height * 0.52f)
            lineTo(size.width * 0.50f, size.height * 0.52f)
            lineTo(size.width * 0.42f, size.height * 0.90f)
            lineTo(size.width * 0.78f, size.height * 0.46f)
            lineTo(size.width * 0.52f, size.height * 0.46f)
            close()
        }
        drawPath(path = path, color = tint)
    }
}

@Composable
fun GoalSelectorDialog(
    currentGoal: String,
    onGoalSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val presetGoals = listOf(
        "Master Focus & Eliminate Distractions",
        "Deep Coding & Software Mastery",
        "Exam Prep & Intensive Study Flow",
        "Zero Doomscrolling & Reels Detox",
        "Complete 3 Priority Tasks Daily"
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "🎯 Set Focus Objective",
                    color = FocusAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pick your main dopamine shield mission for today:",
                    color = FocusSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                presetGoals.forEach { goal ->
                    val isSelected = goal == currentGoal
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) FocusAccentSoft else FocusPanelLight)
                            .then(
                                if (isSelected) Modifier.border(1.dp, FocusAccent, RoundedCornerShape(14.dp))
                                else Modifier
                            )
                            .clickable {
                                onGoalSelected(goal)
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = goal,
                                color = if (isSelected) FocusAccent else FocusWhite,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            if (isSelected) {
                                Text("✓", color = FocusAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusPanelLight)
                        .clickable { onDismiss() }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Close", color = FocusSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AntiTamperAlertDialog(onClose: () -> Unit) {
    androidx.activity.compose.BackHandler { onClose() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground.copy(alpha = 0.96f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .border(1.5.dp, PriorityHigh, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(10.dp)
            ) {
                Text("🔒", fontSize = 42.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Anti-Tamper Shield Active",
                    color = FocusWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Regain Strict Mode has locked Android Accessibility Settings during active Deep Focus / Bedtime Curfew to preserve your dopamine detox. Settings cannot be modified right now.",
                    color = FocusSecondary,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
                androidx.compose.material3.Button(
                    onClick = onClose,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = FocusAccent),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        "Understood — Stay in Flow",
                        color = FocusBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * Atmospheric Zen Canvas Background
 * Implements living ambient gradient orbs, cosmic stardust twinkling,
 * sacred geometry focus ripples, and luxury cinema vignette.
 * Pure Compose Canvas hardware-accelerated rendering at 60/120fps.
 */
@Composable
fun AtmosphericZenBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AtmosphericAura")

    // Slow ambient breathing cycle for primary energy aura (6.5s smooth pulse)
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    // Gentle horizontal drift for secondary ambient aura (9s loop)
    val driftOffset by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftOffset"
    )

    // Continuous smooth rotation for sacred geometry zen ripples (16s cycle)
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Twinkle alpha for cosmic tranquility stardust
    val twinkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkleAlpha"
    )

    // Stable stardust coordinates (relative positions: X, Y, base size in dp)
    val stars = remember {
        listOf(
            Triple(0.12f, 0.08f, 1.8f),
            Triple(0.88f, 0.12f, 2.2f),
            Triple(0.24f, 0.22f, 1.4f),
            Triple(0.76f, 0.28f, 2.0f),
            Triple(0.15f, 0.42f, 1.6f),
            Triple(0.92f, 0.48f, 1.5f),
            Triple(0.35f, 0.62f, 1.9f),
            Triple(0.82f, 0.68f, 2.3f),
            Triple(0.08f, 0.78f, 1.7f),
            Triple(0.68f, 0.84f, 1.5f),
            Triple(0.28f, 0.92f, 2.1f),
            Triple(0.85f, 0.94f, 1.6f)
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Deep luxury obsidian base
            drawRect(color = FocusBackground)

            // 2. Immersive radial vignette - deepens outer corners
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x70000000)
                    ),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = (w.coerceAtLeast(h)) * 0.85f
                )
            )

            // 3. Primary Glowing Brand Aura Orb (Top-Left / Center)
            val orb1Radius = (w * 0.72f) * breathScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FocusAccent.copy(alpha = 0.13f),
                        FocusAccent.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.35f + driftOffset, h * 0.18f),
                    radius = orb1Radius
                ),
                radius = orb1Radius,
                center = Offset(w * 0.35f + driftOffset, h * 0.18f)
            )

            // 4. Secondary Emerald Zen Aura Orb (Right-Center)
            val orb2Radius = (w * 0.62f) * (2.1f - breathScale)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LogoEmerald.copy(alpha = 0.09f),
                        LogoEmerald.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.82f - driftOffset * 0.6f, h * 0.52f),
                    radius = orb2Radius
                ),
                radius = orb2Radius,
                center = Offset(w * 0.82f - driftOffset * 0.6f, h * 0.52f)
            )

            // 5. Warm Ember Grounding Glow (Bottom-Left)
            val orb3Radius = w * 0.55f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LogoGold.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.88f),
                    radius = orb3Radius
                ),
                radius = orb3Radius,
                center = Offset(w * 0.15f, h * 0.88f)
            )

            // 6. Sacred Geometry Concentric Focus Ripple Rings
            val waveCenter = Offset(w * 0.5f, h * 0.20f)
            val waveDash = PathEffect.dashPathEffect(floatArrayOf(5f, 15f), phase = wavePhase)
            val ringRadii = listOf(w * 0.35f, w * 0.55f, w * 0.80f)
            ringRadii.forEachIndexed { idx, radius ->
                val ringAlpha = (0.040f - idx * 0.01f).coerceAtLeast(0.012f)
                drawCircle(
                    color = FocusAccent.copy(alpha = ringAlpha),
                    radius = radius * (0.95f + (breathScale - 0.88f) * 0.12f),
                    center = waveCenter,
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = waveDash
                    )
                )
            }

            // 7. Twinkling Tranquility Stardust Constellation
            stars.forEachIndexed { i, (normX, normY, starSize) ->
                val starAlpha = if (i % 2 == 0) twinkleAlpha else (1.04f - twinkleAlpha)
                val effectiveAlpha = (starAlpha * 0.65f).coerceIn(0.12f, 0.75f)
                drawCircle(
                    color = FocusWhite.copy(alpha = effectiveAlpha),
                    radius = starSize.dp.toPx(),
                    center = Offset(normX * w, normY * h)
                )
                if (starSize > 2.0f) {
                    drawCircle(
                        color = FocusAccent.copy(alpha = effectiveAlpha * 0.35f),
                        radius = (starSize * 2.2f).dp.toPx(),
                        center = Offset(normX * w, normY * h)
                    )
                }
            }
        }

        // Host foreground UI content with full transparency support
        content()
    }
}