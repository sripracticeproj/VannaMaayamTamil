package com.vannamaayam.tamil.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vannamaayam.tamil.ui.theme.*
import com.vannamaayam.tamil.viewmodel.VannaMaayamViewModel
import kotlin.math.sin

@Composable
fun VannaMaayamDashboard(
    hasRecordPermission: Boolean,
    onRequestPermission: () -> Unit,
    viewModel: VannaMaayamViewModel = viewModel()
) {
    val currentAnimal by viewModel.currentAnimal.collectAsState()
    val targetColorTamil by viewModel.targetColorTamil.collectAsState()
    val guessedColorHex by viewModel.guessedColorHex.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()
    val transcript by viewModel.transcript.collectAsState()
    val debugLogs by viewModel.debugLogs.collectAsState()
    val thuliAnimationState by viewModel.thuliAnimationState.collectAsState()
    val gameFinished by viewModel.gameFinished.collectAsState()

    // Smooth gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isListening) {
                        listOf(DarkPurpleBg, Color(0xFF2C1E45))
                    } else {
                        listOf(SoftPurple, PlayfulPink)
                    }
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (gameFinished) {
            GameFinishedScreen(onRestart = { viewModel.restartGame() })
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header / round indicators (visual progress tracker)
                TopProgressHeader(currentRound = if (currentAnimal == "singam") 0 else if (currentAnimal == "thavalai") 1 else if (currentAnimal == "paravai") 2 else 3)

                // Debug Logs Overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    debugLogs.forEach { log ->
                        Text(text = log, color = Color.Yellow, fontSize = 12.sp)
                    }
                }

                // Main Character Canvas: Thuli and the Target Animal side-by-side or stacked
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side: Cute Agent "Thuli"
                    ThuliCharacterView(
                        state = thuliAnimationState,
                        modifier = Modifier
                            .weight(1f)
                            .height(260.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right Side: UNIQUE "Living Canvas" (Vector-based, Reactive)
                    LivingCanvas(
                        animalType = currentAnimal,
                        fillColor = guessedColorHex?.let { Color(it) } ?: Color.Transparent,
                        isListening = isListening,
                        audioRms = audioRms,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(320.dp)
                            .graphicsLayer {
                                // Add a floating effect
                                translationY = sin(System.currentTimeMillis() / 500f) * 10f
                            }
                    )
                }

                // Bottom Mic Trigger and transcript
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    if (transcript.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 16.dp).shadow(8.dp, RoundedCornerShape(24.dp))
                        ) {
                            Text(
                                text = transcript,
                                color = PurplePrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                    }
                    BottomControlArea(
                        hasRecordPermission = hasRecordPermission,
                        isListening = isListening,
                        audioRms = audioRms,
                        onRequestPermission = onRequestPermission,
                        onStartMic = { viewModel.startListening() },
                        onStopMic = { viewModel.stopListening() }
                    )
                }
            }
        }
    }
}

@Composable
fun TopProgressHeader(currentRound: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 4) {
            val isCompleted = i < currentRound
            val isCurrent = i == currentRound
            val starColor = when {
                isCompleted -> SunshineYellow
                isCurrent -> CoralOrange
                else -> Color.White.copy(alpha = 0.5f)
            }
            val size = if (isCurrent) 32.dp else 24.dp
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(size)
                    .graphicsLayer {
                        if (isCurrent) {
                            rotationZ = 15f
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path().apply {
                        val centerX = size.toPx() / 2f
                        val centerY = size.toPx() / 2f
                        val numPoints = 5
                        val outerRadius = size.toPx() / 2f
                        val innerRadius = outerRadius / 2f
                        var angle = Math.PI / 2 * 3
                        val step = Math.PI / numPoints

                        moveTo(
                            (centerX + outerRadius * Math.cos(angle)).toFloat(),
                            (centerY + outerRadius * Math.sin(angle)).toFloat()
                        )
                        for (j in 0 until numPoints * 2) {
                            val r = if (j % 2 == 0) innerRadius else outerRadius
                            angle += step
                            lineTo(
                                (centerX + r * Math.cos(angle)).toFloat(),
                                (centerY + r * Math.sin(angle)).toFloat()
                            )
                        }
                        close()
                    }
                    drawPath(path, color = starColor)
                }
            }
        }
    }
}

@Composable
fun ThuliCharacterView(state: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "thuli")
    
    // Smooth breathing or bouncing animations based on state
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (state == "celebrate") -30f else -10f,
        animationSpec = infiniteTransitionSpec(if (state == "celebrate") 400 else 1200),
        label = "bounce"
    )

    val scaleX by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == "talking") 1.08f else 1.02f,
        animationSpec = infiniteTransitionSpec(if (state == "talking") 300 else 1500),
        label = "scale"
    )

    // Eye look angle for "thinking"
    val eyeOffsetX by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = if (state == "thinking") 5f else -5f,
        animationSpec = infiniteTransitionSpec(2000),
        label = "eye"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                this.translationY = bounceY
                this.scaleX = scaleX
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f + 20f

            // 1. Draw Thuli's Body (Droplet Shape)
            val bodyPath = Path().apply {
                moveTo(centerX, centerY - 100f)
                cubicTo(
                    centerX - 90f, centerY - 20f,
                    centerX - 90f, centerY + 90f,
                    centerX, centerY + 95f
                )
                cubicTo(
                    centerX + 90f, centerY + 90f,
                    centerX + 90f, centerY - 20f,
                    centerX, centerY - 100f
                )
                close()
            }
            
            // Draw drop glow/body with gradient
            val thuliColor = when (state) {
                "celebrate" -> Brush.radialGradient(listOf(SunshineYellow, CoralOrange), center = Offset(centerX, centerY))
                "thinking" -> Brush.radialGradient(listOf(SoftPurple, PurpleSecondary), center = Offset(centerX, centerY))
                else -> Brush.radialGradient(listOf(SkyBlue, PurplePrimary), center = Offset(centerX, centerY))
            }
            drawPath(bodyPath, brush = thuliColor)

            // 2. Draw Eyes
            val eyeRadius = 12f
            val eyeSpacing = 35f
            val eyeY = centerY - 10f

            // Left Eye
            drawCircle(
                color = Color.Black,
                radius = eyeRadius,
                center = Offset(centerX - eyeSpacing + if (state == "thinking") eyeOffsetX else 0f, eyeY)
            )
            // Left Eye Pupil Glow
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(centerX - eyeSpacing - 4f + if (state == "thinking") eyeOffsetX else 0f, eyeY - 4f)
            )

            // Right Eye
            drawCircle(
                color = Color.Black,
                radius = eyeRadius,
                center = Offset(centerX + eyeSpacing + if (state == "thinking") eyeOffsetX else 0f, eyeY)
            )
            // Right Eye Pupil Glow
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(centerX + eyeSpacing - 4f + if (state == "thinking") eyeOffsetX else 0f, eyeY - 4f)
            )

            // 3. Draw Mouth (dynamic based on speech/state)
            val mouthPath = Path()
            if (state == "talking") {
                // Wide open talking oval
                mouthPath.addOval(
                    androidx.compose.ui.geometry.Rect(
                        centerX - 12f, centerY + 20f,
                        centerX + 12f, centerY + 45f
                    )
                )
                drawPath(mouthPath, color = Color(0xFFC62828))
            } else if (state == "celebrate") {
                // Cute happy smile arc
                mouthPath.moveTo(centerX - 15f, centerY + 20f)
                mouthPath.quadraticBezierTo(centerX, centerY + 38f, centerX + 15f, centerY + 20f)
                drawPath(mouthPath, color = Color.Black, style = Stroke(width = 5f))
            } else {
                // Simple happy smile arc
                mouthPath.moveTo(centerX - 10f, centerY + 22f)
                mouthPath.quadraticBezierTo(centerX, centerY + 30f, centerX + 10f, centerY + 22f)
                drawPath(mouthPath, color = Color.Black, style = Stroke(width = 5f))
            }

            // 4. Rosy Cheek Circles
            drawCircle(
                color = PlayfulPink.copy(alpha = 0.6f),
                radius = 10f,
                center = Offset(centerX - eyeSpacing - 18f, centerY + 10f)
            )
            drawCircle(
                color = PlayfulPink.copy(alpha = 0.6f),
                radius = 10f,
                center = Offset(centerX + eyeSpacing + 18f, centerY + 10f)
            )
        }
    }
}

@Composable
fun BottomControlArea(
    hasRecordPermission: Boolean,
    isListening: Boolean,
    audioRms: Float,
    onRequestPermission: () -> Unit,
    onStartMic: () -> Unit,
    onStopMic: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!hasRecordPermission) {
            // Big warning permission key (zero text UX fallback)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
                    .clickable { onRequestPermission() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            // Ripple effects when listening
            val rmsScale = remember { Animatable(1f) }
            LaunchedEffect(audioRms) {
                // Map RMS scale smoothly (speech feedback)
                val target = 1f + (audioRms.coerceIn(0f, 15f) / 15f) * 0.7f
                rmsScale.animateTo(
                    target,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                if (isListening) {
                    // Pulsing background rings representing sound volume
                    Box(
                        modifier = Modifier
                            .size(100.dp * rmsScale.value)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                    Box(
                        modifier = Modifier
                            .size(120.dp * rmsScale.value)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                }

                // Interactive mic button
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                if (isListening) {
                                    listOf(GradientSunnyStart, GradientSunnyEnd)
                                } else {
                                    listOf(GradientPurpleStart, GradientPurpleEnd)
                                }
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (isListening) onStopMic() else onStartMic()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(45.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GameFinishedScreen(onRestart: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "finish")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -35f,
        animationSpec = infiniteTransitionSpec(500),
        label = "bounce"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Trophy/Celebration display
        Box(
            modifier = Modifier
                .size(180.dp)
                .graphicsLayer { translationY = bounceY },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Draw Gold Star / Trophy Vector
                drawCircle(color = SunshineYellow, radius = 75f, center = Offset(cx, cy))
                drawCircle(color = CoralOrange, radius = 75f, center = Offset(cx, cy), style = Stroke(width = 8f))
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(72.dp).graphicsLayer { rotationZ = -90f } // Pointing up
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Play again trigger button (Zero text UX)
        Box(
            modifier = Modifier
                .size(100.dp)
                .shadow(16.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(GradientSunnyStart, GradientSunnyEnd)))
                .clickable { onRestart() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(54.dp)
            )
        }
    }
}

private fun <T> infiniteTransitionSpec(duration: Int): InfiniteRepeatableSpec<T> {
    return infiniteRepeatable(
        animation = tween(durationMillis = duration, easing = LinearOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    )
}
