package com.vannamaayam.tamil.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun LivingCanvas(
    animalType: String,
    fillColor: Color,
    isListening: Boolean,
    audioRms: Float,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = fillColor,
        animationSpec = tween(1500),
        label = "color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.05f + (audioRms / 30f) else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(modifier = modifier.padding(16.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            val width = size.width
            val height = size.height
            val cx = width / 2f
            val cy = height / 2f

            when (animalType) {
                "singam" -> drawLion(this, cx, cy, width, height, animatedColor)
                "thavalai" -> drawFrog(this, cx, cy, width, height, animatedColor)
                "paravai" -> drawBird(this, cx, cy, width, height, animatedColor)
                "muyal" -> drawRabbit(this, cx, cy, width, height, animatedColor)
            }
        }
    }
}

private fun drawLion(scope: androidx.compose.ui.graphics.drawscope.DrawScope, cx: Float, cy: Float, w: Float, h: Float, color: Color) {
    val manePath = Path().apply {
        moveTo(cx, cy - h*0.45f)
        cubicTo(cx - w*0.5f, cy - h*0.45f, cx - w*0.55f, cy + h*0.15f, cx - w*0.2f, cy + h*0.45f)
        lineTo(cx + w*0.2f, cy + h*0.45f)
        cubicTo(cx + w*0.55f, cy + h*0.15f, cx + w*0.5f, cy - h*0.45f, cx, cy - h*0.45f)
        close()
    }
    scope.drawPath(manePath, color = color.copy(alpha = 0.8f))
    scope.drawPath(manePath, color = Color.White, style = Stroke(width = 8f))

    val facePath = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(cx - w*0.22f, cy - h*0.25f, cx + w*0.22f, cy + h*0.25f))
    }
    scope.drawPath(facePath, color = color)
    scope.drawPath(facePath, color = Color.White, style = Stroke(width = 6f))

    // Eyes
    scope.drawCircle(Color.Black, radius = 10f, center = androidx.compose.ui.geometry.Offset(cx - w*0.08f, cy - h*0.05f))
    scope.drawCircle(Color.Black, radius = 10f, center = androidx.compose.ui.geometry.Offset(cx + w*0.08f, cy - h*0.05f))
    // Nose
    scope.drawCircle(Color(0xFF4E342E), radius = 12f, center = androidx.compose.ui.geometry.Offset(cx, cy + h*0.05f))
    // Smile
    val smile = Path().apply {
        moveTo(cx - 20f, cy + h*0.12f)
        quadraticBezierTo(cx, cy + h*0.18f, cx + 20f, cy + h*0.12f)
    }
    scope.drawPath(smile, Color.Black, style = Stroke(width = 4f))
}

private fun drawFrog(scope: androidx.compose.ui.graphics.drawscope.DrawScope, cx: Float, cy: Float, w: Float, h: Float, color: Color) {
    val body = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(cx - w*0.35f, cy - h*0.15f, cx + w*0.35f, cy + h*0.35f))
    }
    scope.drawPath(body, color = color)
    scope.drawPath(body, color = Color.White, style = Stroke(width = 8f))

    // Eyes
    val eyeL = androidx.compose.ui.geometry.Rect(cx - w*0.28f, cy - h*0.35f, cx - w*0.08f, cy - h*0.1f)
    val eyeR = androidx.compose.ui.geometry.Rect(cx + w*0.08f, cy - h*0.35f, cx + w*0.28f, cy - h*0.1f)
    scope.drawOval(color, topLeft = eyeL.topLeft, size = eyeL.size)
    scope.drawOval(color, topLeft = eyeR.topLeft, size = eyeR.size)
    scope.drawOval(Color.White, topLeft = eyeL.topLeft, size = eyeL.size, style = Stroke(width = 6f))
    scope.drawOval(Color.White, topLeft = eyeR.topLeft, size = eyeR.size, style = Stroke(width = 6f))
    
    scope.drawCircle(Color.Black, radius = 12f, center = androidx.compose.ui.geometry.Offset(cx - w*0.18f, cy - h*0.22f))
    scope.drawCircle(Color.Black, radius = 12f, center = androidx.compose.ui.geometry.Offset(cx + w*0.18f, cy - h*0.22f))

    // Mouth line
    val mouth = Path().apply {
        moveTo(cx - w*0.15f, cy + h*0.15f)
        quadraticBezierTo(cx, cy + h*0.25f, cx + w*0.15f, cy + h*0.15f)
    }
    scope.drawPath(mouth, Color.Black, style = Stroke(width = 5f))
}

private fun drawBird(scope: androidx.compose.ui.graphics.drawscope.DrawScope, cx: Float, cy: Float, w: Float, h: Float, color: Color) {
    val body = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(cx - w*0.3f, cy - h*0.25f, cx + w*0.3f, cy + h*0.25f))
    }
    scope.drawPath(body, color = color)
    scope.drawPath(body, color = Color.White, style = Stroke(width = 8f))

    // Wing
    val wing = Path().apply {
        moveTo(cx - w*0.05f, cy - h*0.05f)
        cubicTo(cx - w*0.4f, cy - h*0.2f, cx - w*0.4f, cy + h*0.2f, cx - w*0.05f, cy + h*0.05f)
    }
    scope.drawPath(wing, color = color.copy(alpha = 0.6f))
    scope.drawPath(wing, color = Color.White, style = Stroke(width = 4f))

    // Eye
    scope.drawCircle(Color.Black, radius = 10f, center = androidx.compose.ui.geometry.Offset(cx + w*0.1f, cy - h*0.08f))
    
    // Beak
    val beak = Path().apply {
        moveTo(cx + w*0.28f, cy - h*0.02f)
        lineTo(cx + w*0.45f, cy + h*0.05f)
        lineTo(cx + w*0.28f, cy + h*0.12f)
        close()
    }
    scope.drawPath(beak, Color(0xFFFFD54F), style = Fill)
    scope.drawPath(beak, Color.White, style = Stroke(width = 4f))
}

private fun drawRabbit(scope: androidx.compose.ui.graphics.drawscope.DrawScope, cx: Float, cy: Float, w: Float, h: Float, color: Color) {
    val head = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(cx - w*0.25f, cy - h*0.15f, cx + w*0.25f, cy + h*0.35f))
    }
    scope.drawPath(head, color = color)
    scope.drawPath(head, color = Color.White, style = Stroke(width = 8f))

    // Ears
    val earL = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(cx - w*0.22f, cy - h*0.5f, cx - w*0.05f, cy))
    }
    val earR = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(cx + w*0.05f, cy - h*0.5f, cx + w*0.22f, cy))
    }
    scope.drawPath(earL, color = color)
    scope.drawPath(earR, color = color)
    scope.drawPath(earL, color = Color.White, style = Stroke(width = 6f))
    scope.drawPath(earR, color = Color.White, style = Stroke(width = 6f))

    // Eyes
    scope.drawCircle(Color.Black, radius = 9f, center = androidx.compose.ui.geometry.Offset(cx - w*0.1f, cy + h*0.05f))
    scope.drawCircle(Color.Black, radius = 9f, center = androidx.compose.ui.geometry.Offset(cx + w*0.1f, cy + h*0.05f))
    // Nose
    scope.drawCircle(Color(0xFFF06292), radius = 8f, center = androidx.compose.ui.geometry.Offset(cx, cy + h*0.15f))
}
