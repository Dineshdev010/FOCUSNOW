package com.focusflow.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.focusflow.app.model.RegainFrictionMode
import com.focusflow.app.ui.theme.*
import com.focusflow.app.viewmodel.FocusFlowViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun MindfulPauseDialog(
    viewModel: FocusFlowViewModel,
    appName: String = viewModel.mindfulPauseAppName,
    packageName: String = viewModel.mindfulPausePackageName,
    isTimesUpMode: Boolean = viewModel.showRegainTimesUpModal,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val frictionMode = viewModel.regainFrictionMode
    val quota = if (packageName.isNotBlank()) viewModel.getAppOpenQuota(packageName) else null
    val isQuotaExceeded = quota?.isExceeded ?: false

    var selectedIntention by remember { mutableStateOf<String?>("Habit / Boredom") }
    var breathPhase by remember { mutableStateOf("Inhale slowly...") }

    // Regain Trademark: 5s Unskippable Mindful Friction Delay (Breathing)
    var waitSecondsLeft by remember { mutableIntStateOf(if (isTimesUpMode) 0 else 5) }
    var delayChallengeActive by remember { mutableStateOf(false) }
    var delaySecondsLeft by remember { mutableIntStateOf(30) }
    var delayChallengeCompleted by remember { mutableStateOf(false) }

    // Pledge Typing Friction Mode
    val pledgeTarget = remember(appName) { "I will use $appName with conscious purpose and leave when done." }
    var userPledgeInput by remember { mutableStateOf("") }
    val isPledgeComplete = userPledgeInput.trim().equals(pledgeTarget.trim(), ignoreCase = true) ||
            userPledgeInput.trim().length >= (pledgeTarget.length * 0.85).toInt()

    // Phone Flip Friction Mode (Sensors)
    var isPhoneFaceDown by remember { mutableStateOf(false) }
    var flipSecondsLeft by remember { mutableIntStateOf(5) }
    var isFlipComplete by remember { mutableStateOf(false) }

    DisposableEffect(frictionMode) {
        if (frictionMode == RegainFrictionMode.PHONE_FLIP && !isTimesUpMode) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null) return
                    // Z < -6.5 m/s^2 means screen is flat against a table/downwards
                    val z = event.values[2]
                    isPhoneFaceDown = z < -6.5f
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            if (sensorManager != null && accel != null) {
                sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
            }
            onDispose {
                sensorManager?.unregisterListener(listener)
            }
        } else {
            onDispose {}
        }
    }

    LaunchedEffect(isPhoneFaceDown, frictionMode) {
        if (frictionMode == RegainFrictionMode.PHONE_FLIP && isPhoneFaceDown && !isFlipComplete) {
            while (flipSecondsLeft > 0 && isPhoneFaceDown) {
                delay(1000L)
                if (isPhoneFaceDown) {
                    flipSecondsLeft--
                }
            }
            if (flipSecondsLeft == 0) {
                isFlipComplete = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    val isFrictionPassed = isTimesUpMode || when (frictionMode) {
        RegainFrictionMode.BREATHING -> waitSecondsLeft <= 0
        RegainFrictionMode.PLEDGE_TYPING -> isPledgeComplete
        RegainFrictionMode.PHONE_FLIP -> isFlipComplete
    }
    val canTakePass = isFrictionPassed && !isQuotaExceeded

    LaunchedEffect(Unit) {
        if (!isTimesUpMode && waitSecondsLeft > 0) {
            while (waitSecondsLeft > 0) {
                delay(1000L)
                waitSecondsLeft--
            }
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    LaunchedEffect(delayChallengeActive) {
        if (delayChallengeActive) {
            delaySecondsLeft = 30
            while (delaySecondsLeft > 0) {
                delay(1000L)
                delaySecondsLeft--
            }
            delayChallengeCompleted = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.recordWalkAwayMindfulChoice(minutesSaved = 20, coinReward = 15)
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            breathPhase = "Inhale slowly..."
            delay(4000L)
            breathPhase = "Hold gently..."
            delay(4000L)
            breathPhase = "Exhale peacefully..."
            delay(4000L)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathTransition")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    val savedHours = String.format("%.1f", viewModel.savedScreenTimeMinutes / 60f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground.copy(alpha = 0.96f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusPanel),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.92f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .border(1.5.dp, if (isTimesUpMode) PriorityHigh.copy(alpha = 0.6f) else FocusAccent.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isTimesUpMode) PriorityHigh else FocusAccent, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isTimesUpMode) "TIME'S UP • REGAIN CONTROL" else "REGAIN • MINDFUL INTERVENTION",
                            color = if (isTimesUpMode) PriorityHigh else FocusAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
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
                        Text(
                            "✕",
                            color = FocusSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isTimesUpMode || frictionMode == RegainFrictionMode.BREATHING) {
                    // 1. Breathing Circle Animation with 4-7-8 Phase & Wait Countdown
                    Box(
                        modifier = Modifier
                            .size(125.dp)
                            .scale(breathScale)
                            .background(if (isTimesUpMode) PriorityHigh.copy(alpha = 0.15f) else FocusAccentSoft, CircleShape)
                            .border(2.dp, if (isTimesUpMode) PriorityHigh else FocusAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isTimesUpMode) "⌛" else "🌿", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                if (isTimesUpMode) "Time Expired" else if (waitSecondsLeft > 0) "Breathe (${waitSecondsLeft}s)" else breathPhase,
                                color = FocusWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (frictionMode == RegainFrictionMode.PLEDGE_TYPING) {
                    // 2. Intentional Pledge Typing Challenge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(FocusPanelLight)
                            .border(1.dp, if (isPledgeComplete) FocusAccent else FocusBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✍️ Intentional Pledge", color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (isPledgeComplete) "✅ Anchored" else "${userPledgeInput.length}/${pledgeTarget.length}",
                                    color = if (isPledgeComplete) FocusAccent else FocusSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\"$pledgeTarget\"",
                                color = FocusSecondary,
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            BasicTextField(
                                value = userPledgeInput,
                                onValueChange = { userPledgeInput = it },
                                textStyle = TextStyle(color = FocusWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                                cursorBrush = SolidColor(FocusAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FocusBackground)
                                    .border(1.dp, if (isPledgeComplete) FocusAccent else FocusBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                decorationBox = { innerTextField ->
                                    if (userPledgeInput.isEmpty()) {
                                        Text("Type the pledge above to unlock...", color = FocusSecondary.copy(alpha = 0.6f), fontSize = 11.sp)
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                } else if (frictionMode == RegainFrictionMode.PHONE_FLIP) {
                    // 3. Physical Phone Flip Challenge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isFlipComplete) FocusAccentSoft else FocusPanelLight)
                            .border(1.dp, if (isFlipComplete) FocusAccent else if (isPhoneFaceDown) FocusAccent else FocusBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isFlipComplete) "✅" else if (isPhoneFaceDown) "⏳" else "📱", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isFlipComplete) "Mindful Rest Complete!" else if (isPhoneFaceDown) "Phone Face Down: ${flipSecondsLeft}s left" else "Place Phone Face Down",
                                color = if (isFlipComplete) FocusAccent else FocusWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isFlipComplete) "Dopamine urge successfully interrupted." else if (isPhoneFaceDown) "Hold still. Let gravity break the stimulus loop." else "Rest phone screen-down on flat surface for 5s to unlock.",
                                color = FocusSecondary,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Daily Open Quota Badge / Warning
                if (isQuotaExceeded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PriorityHigh.copy(alpha = 0.15f))
                            .border(1.dp, PriorityHigh, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛑", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Daily Open Budget Reached (${quota?.currentOpensToday}/${quota?.maxDailyOpens})",
                                    color = PriorityHigh,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "You have exhausted your daily opens for $appName. Step away to preserve your focus.",
                                    color = FocusWhite,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else if (quota != null && quota.maxDailyOpens > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📊 Daily Open Budget:", color = FocusSecondary, fontSize = 10.sp)
                        Text("${quota.currentOpensToday} of ${quota.maxDailyOpens} used today", color = FocusAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (isTimesUpMode) {
                    Text(
                        text = "Your Micro-Session for $appName is Over",
                        color = FocusWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Did you complete what you intended? Break the algorithmic loop now.",
                        color = FocusSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                } else {
                    Text(
                        text = "Pause before opening $appName",
                        color = FocusWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You've launched this app multiple times today. Why are you opening it now?",
                        color = FocusSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isTimesUpMode) {
                    // Intention Selector Chips (Regain & One Sec style)
                    val intentions = listOf(
                        "\uD83D\uDCAC Important Chat" to "Important Chat",
                        "\uD83D\uDD0D Quick Search" to "Quick Search",
                        "\uD83D\uDCBC Work Related" to "Work Related",
                        "\uD83E\uDD71 Habit / Boredom" to "Habit / Boredom"
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        intentions.forEach { (display, value) ->
                            val isSelected = selectedIntention == value
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) FocusAccentSoft else FocusPanelLight,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) FocusAccent else FocusBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedIntention = value
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = display,
                                        color = if (isSelected) FocusAccent else FocusWhite,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) {
                                        Text("\u2713", color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Regained Time Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FocusPanelLight, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("\u23F3 Screen time saved today:", color = FocusSecondary, fontSize = 11.sp)
                        Text("$savedHours hrs saved", color = FocusAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isTimesUpMode) {
                    // TIME'S UP PRIMARY ACTION: I'm Done (Close App & go Home)
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.recordWalkAwayMindfulChoice(minutesSaved = 10, coinReward = 15)
                            viewModel.endMicroSession()
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            "✅ I'm Done — Exit App (+15 🪙)",
                            color = FocusBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Priority: Solve Mind Puzzle to Earn +5 Extra Minutes
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.openJigsawPuzzle(
                                reason = "Time's up for $appName! Solve a mindful puzzle to earn 5 extra minutes.",
                                onSolved = {
                                    viewModel.startMicroSession(packageName, appName, 5, "Puzzle Time Unlock")
                                    onClose()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.5.dp, FocusAccent, RoundedCornerShape(16.dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🧩", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Solve Puzzle to Unlock Time (+5m)",
                                color = FocusAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary: Extend 1 Minute with 15s delay penalty
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.extendMicroSessionOneMinute(packageName, appName)
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPanelLight),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .border(1.dp, FocusBorder, RoundedCornerShape(14.dp))
                    ) {
                        Text("⏱️ Need 1 More Minute (Quick Wrap-up)", color = FocusSecondary, fontSize = 11.sp)
                    }
                } else {
                    // 30-Second Dopamine Delay Challenge
                    if (delayChallengeCompleted) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(FocusAccentSoft)
                                .border(1.5.dp, FocusAccent, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✨ Urge Neutralized! (+15 🪙)", color = FocusAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Your prefrontal cortex restored conscious control.", color = FocusWhite, fontSize = 10.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    } else if (delayChallengeActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(FocusPanelLight)
                                .border(1.5.dp, FocusAccent, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⏳ Waiting out the urge...", color = FocusAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${delaySecondsLeft}s",
                                    color = FocusWhite,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Neuroscience: Dopamine cravings peak at 20-30s. Breathe deeply.",
                                    color = FocusSecondary,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Primary Choice: Walk Away & Save Time (+10 Coins!)
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.recordWalkAwayMindfulChoice(minutesSaved = 15, coinReward = 10)
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusAccent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            "\uD83C\uDF3F Walk Away & Regain Time (+10 \uD83E\uDE99)",
                            color = FocusBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // REGAIN SIGNATURE: MICRO-SESSION PASS BUTTONS
                    Text(
                        text = if (isQuotaExceeded) "Daily open limit reached for this app." else if (!isFrictionPassed) "Complete the mindful friction above to unlock passes..." else "Or take an intentional micro-session pass:",
                        color = if (isQuotaExceeded) PriorityHigh else FocusSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val sessionOptions = listOf(
                            "1 Min" to 1,
                            "5 Min" to 5,
                            "10 Min" to 10,
                            "15 Min" to 15
                        )

                        sessionOptions.forEach { (label, mins) ->
                            val isEnabled = canTakePass
                            Button(
                                onClick = {
                                    if (isEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.startMicroSession(
                                            packageName = packageName,
                                            appName = appName,
                                            minutes = mins,
                                            intention = selectedIntention ?: "Quick Task"
                                        )
                                        onClose()
                                    }
                                },
                                enabled = isEnabled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (mins == 5) FocusAccentSoft else FocusPanelLight,
                                    disabledContainerColor = FocusPanelLight.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .border(1.dp, if (mins == 5) FocusAccent else FocusBorder, RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    text = label,
                                    color = if (mins == 5) FocusAccent else FocusWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}