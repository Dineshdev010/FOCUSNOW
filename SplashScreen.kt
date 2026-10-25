package com.focusflow.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.ui.components.AtmosphericZenBackground
import com.focusflow.app.ui.theme.FocusAccent
import com.focusflow.app.ui.theme.FocusAccentGlow
import com.focusflow.app.ui.theme.LogoEmerald
import com.focusflow.app.ui.theme.FocusBackground
import com.focusflow.app.ui.theme.FocusPanelLight
import com.focusflow.app.ui.theme.FocusSecondary
import com.focusflow.app.viewmodel.FocusFlowViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: FocusFlowViewModel) {
    val scale = remember { Animatable(0.7f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
        )
        delay(300)
        viewModel.onSplashFinished()
    }

    AtmosphericZenBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated Hourglass Flame Logo with Radiant Glowing Aura Halo
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(scale.value),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                FocusAccent.copy(alpha = 0.40f),
                                LogoEmerald.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        )
                    )
                }
                Box(
                    modifier = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Deep obsidian backlight circles matching launcher background
                    drawCircle(
                        color = Color(0xFF141C11),
                        radius = size.minDimension * 0.48f
                    )
                    drawCircle(
                        color = Color(0xFF1C2B16),
                        radius = size.minDimension * 0.38f
                    )
                    drawCircle(
                        color = Color(0xFF29401F),
                        radius = size.minDimension * 0.48f,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Ambient Lime Glow
                    drawCircle(
                        color = Color(0x3584CC16),
                        radius = size.minDimension * 0.42f
                    )

                    // Top & Bottom Cap Bars (Electric Lime #84CC16)
                    val capBarHeight = h * 0.045f
                    drawRoundRect(
                        color = Color(0xFF84CC16),
                        topLeft = Offset(w * 0.20f, h * 0.16f),
                        size = androidx.compose.ui.geometry.Size(w * 0.60f, capBarHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFF84CC16),
                        topLeft = Offset(w * 0.20f, h * 0.795f),
                        size = androidx.compose.ui.geometry.Size(w * 0.60f, capBarHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                    )

                    // Hourglass Glass Frame Outline Path
                    val glassPath = Path().apply {
                        moveTo(w * 0.24f, h * 0.205f)
                        lineTo(w * 0.45f, h * 0.48f)
                        lineTo(w * 0.45f, h * 0.52f)
                        lineTo(w * 0.24f, h * 0.795f)

                        moveTo(w * 0.76f, h * 0.205f)
                        lineTo(w * 0.55f, h * 0.48f)
                        lineTo(w * 0.55f, h * 0.52f)
                        lineTo(w * 0.76f, h * 0.795f)
                    }
                    drawPath(
                        path = glassPath,
                        color = Color(0xFF84CC16),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Top Chamber: Emerald Sand Reservoir (#10B981)
                    val topSandPath = Path().apply {
                        moveTo(w * 0.28f, h * 0.25f)
                        lineTo(w * 0.72f, h * 0.25f)
                        lineTo(w * 0.55f, h * 0.47f)
                        lineTo(w * 0.45f, h * 0.47f)
                        close()
                    }
                    drawPath(path = topSandPath, color = Color(0xFF10B981).copy(alpha = 0.85f))

                    // Top Sand Wave Highlight (#84CC16)
                    val waveHighlight = Path().apply {
                        moveTo(w * 0.32f, h * 0.31f)
                        quadraticTo(w * 0.50f, h * 0.26f, w * 0.68f, h * 0.33f)
                        lineTo(w * 0.54f, h * 0.46f)
                        lineTo(w * 0.46f, h * 0.46f)
                        close()
                    }
                    drawPath(path = waveHighlight, color = Color(0xFF84CC16).copy(alpha = 0.90f))

                    // Falling Sand Granules Stream
                    drawCircle(color = Color(0xFFA3E635), radius = 2.dp.toPx(), center = Offset(w * 0.50f, h * 0.50f))
                    drawCircle(color = Color(0xFF84CC16), radius = 1.8.dp.toPx(), center = Offset(w * 0.50f, h * 0.54f))
                    drawCircle(color = Color(0xFFFBBF24), radius = 2.2.dp.toPx(), center = Offset(w * 0.50f, h * 0.58f))

                    // Bottom Chamber: Burning Focus Flame
                    // Emerald Flame Base
                    val flameBase = Path().apply {
                        moveTo(w * 0.50f, h * 0.59f)
                        cubicTo(w * 0.62f, h * 0.67f, w * 0.68f, h * 0.73f, w * 0.65f, h * 0.78f)
                        cubicTo(w * 0.60f, h * 0.80f, w * 0.40f, h * 0.80f, w * 0.35f, h * 0.78f)
                        cubicTo(w * 0.32f, h * 0.73f, w * 0.38f, h * 0.67f, w * 0.50f, h * 0.59f)
                        close()
                    }
                    drawPath(path = flameBase, color = Color(0xFF10B981).copy(alpha = 0.95f))

                    // Mid Vibrant Amber Flame Tongue (#F59E0B)
                    val midFlame = Path().apply {
                        moveTo(w * 0.50f, h * 0.62f)
                        cubicTo(w * 0.58f, h * 0.68f, w * 0.63f, h * 0.72f, w * 0.60f, h * 0.77f)
                        cubicTo(w * 0.56f, h * 0.79f, w * 0.44f, h * 0.79f, w * 0.40f, h * 0.77f)
                        cubicTo(w * 0.37f, h * 0.72f, w * 0.42f, h * 0.68f, w * 0.50f, h * 0.62f)
                        close()
                    }
                    drawPath(path = midFlame, color = Color(0xFFF59E0B))

                    // Inner Golden Hot Flame Core (#FBBF24)
                    val innerFlame = Path().apply {
                        moveTo(w * 0.50f, h * 0.66f)
                        cubicTo(w * 0.55f, h * 0.70f, w * 0.58f, h * 0.73f, w * 0.56f, h * 0.76f)
                        cubicTo(w * 0.53f, h * 0.775f, w * 0.47f, h * 0.775f, w * 0.44f, h * 0.76f)
                        cubicTo(w * 0.42f, h * 0.73f, w * 0.45f, h * 0.70f, w * 0.50f, h * 0.66f)
                        close()
                    }
                    drawPath(path = innerFlame, color = Color(0xFFFBBF24))

                    // Intense White-Hot Center Flare (#FEF08A)
                    val coreFlare = Path().apply {
                        moveTo(w * 0.50f, h * 0.70f)
                        cubicTo(w * 0.53f, h * 0.72f, w * 0.54f, h * 0.74f, w * 0.52f, h * 0.755f)
                        cubicTo(w * 0.51f, h * 0.76f, w * 0.49f, h * 0.76f, w * 0.48f, h * 0.755f)
                        cubicTo(w * 0.46f, h * 0.74f, w * 0.47f, h * 0.72f, w * 0.50f, h * 0.70f)
                        close()
                    }
                    drawPath(path = coreFlare, color = Color(0xFFFEF08A))

                    // Left Glass Specular Sheen Reflection
                    val sheenPath = Path().apply {
                        moveTo(w * 0.27f, h * 0.23f)
                        lineTo(w * 0.42f, h * 0.44f)
                        moveTo(w * 0.42f, h * 0.56f)
                        lineTo(w * 0.28f, h * 0.75f)
                    }
                    drawPath(
                        path = sheenPath,
                        color = Color(0x60FFFFFF),
                        style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "FOCUSNOW",
                color = FocusAccent,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dopamine Shield & Productivity Engine",
                color = FocusSecondary,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(5.dp)
                    .background(FocusPanelLight, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .height(5.dp)
                        .background(FocusAccent, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Activating Reels & Distraction Shield...",
                color = FocusSecondary.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}
}