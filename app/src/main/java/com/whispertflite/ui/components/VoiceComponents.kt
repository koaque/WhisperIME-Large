package com.whispertflite.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.whispertflite.ui.theme.*
import kotlin.math.*

/**
 * Animated voice wave visualization component
 */
@Composable
fun VoiceWaveAnimation(
    modifier: Modifier = Modifier,
    isAnimating: Boolean = false,
    audioLevel: Float = 0f,
    waveCount: Int = 5,
    baseHeight: Dp = 4.dp,
    maxHeight: Dp = 40.dp,
    waveWidth: Dp = 3.dp,
    spacing: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val density = LocalDensity.current
    
    // Animation for wave heights
    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave")
    val animationValues = remember { mutableListOf<Animatable<Float, AnimationVector1D>>() }
    
    // Initialize animatables if needed
    LaunchedEffect(waveCount) {
        if (animationValues.size != waveCount) {
            animationValues.clear()
            repeat(waveCount) { index ->
                animationValues.add(Animatable(0f))
            }
        }
    }
    
    // Control animations based on isAnimating state
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            // Start staggered wave animations
            animationValues.forEachIndexed { index, animatable ->
                launch {
                    animatable.animateTo(
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 800 + (index * 100),
                                easing = FastOutSlowInEasing
                            ),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(index * 100)
                        )
                    )
                }
            }
        } else {
            // Stop animations
            animationValues.forEach { animatable ->
                launch {
                    animatable.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(300)
                    )
                }
            }
        }
    }
    
    Canvas(
        modifier = modifier
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val baseHeightPx = with(density) { baseHeight.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val waveWidthPx = with(density) { waveWidth.toPx() }
        val spacingPx = with(density) { spacing.toPx() }
        
        val totalWidth = (waveCount * waveWidthPx) + ((waveCount - 1) * spacingPx)
        val startX = (canvasWidth - totalWidth) / 2
        
        animationValues.forEachIndexed { index, animatable ->
            val x = startX + (index * (waveWidthPx + spacingPx))
            val animatedValue = animatable.value
            
            // Combine animation with audio level
            val heightFactor = if (isAnimating) {
                maxOf(animatedValue, audioLevel * 0.5f)
            } else {
                audioLevel * 0.3f
            }
            
            val waveHeight = baseHeightPx + (heightFactor * (maxHeightPx - baseHeightPx))
            val centerY = canvasHeight / 2
            
            // Draw wave bar
            drawRoundRect(
                color = color,
                topLeft = Offset(x, centerY - waveHeight / 2),
                size = androidx.compose.ui.geometry.Size(waveWidthPx, waveHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(waveWidthPx / 2)
            )
            
            // Add glow effect for active waves
            if (heightFactor > 0.5f) {
                drawRoundRect(
                    color = color.copy(alpha = 0.3f),
                    topLeft = Offset(x - 1, centerY - waveHeight / 2 - 1),
                    size = androidx.compose.ui.geometry.Size(waveWidthPx + 2, waveHeight + 2),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius((waveWidthPx + 2) / 2)
                )
            }
        }
    }
}

/**
 * Circular VU meter component
 */
@Composable
fun CircularVUMeter(
    modifier: Modifier = Modifier,
    level: Float = 0f,
    maxLevel: Float = 1f,
    size: Dp = 60.dp,
    strokeWidth: Dp = 6.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    val density = LocalDensity.current
    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0f, maxLevel),
        animationSpec = tween(durationMillis = 100),
        label = "vu_level"
    )
    
    Canvas(
        modifier = modifier.size(size)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = (minOf(size.width, size.height) / 2) - with(density) { strokeWidth.toPx() / 2 }
        val strokeWidthPx = with(density) { strokeWidth.toPx() }
        
        // Background circle
        drawCircle(
            color = backgroundColor,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = strokeWidthPx)
        )
        
        // Active arc based on level
        val sweepAngle = (animatedLevel / maxLevel) * 360f
        drawArc(
            color = AudioColors.getAudioLevelColor(animatedLevel),
            startAngle = -90f, // Start from top
            sweepAngle = sweepAngle,
            useCenter = false,
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            topLeft = Offset(centerX - radius, centerY - radius),
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * Linear VU meter bars
 */
@Composable
fun LinearVUMeter(
    modifier: Modifier = Modifier,
    level: Float = 0f,
    barCount: Int = 10,
    barHeight: Dp = WhisperDesignTokens.VUMeterHeightLarge,
    barWidth: Dp = 3.dp,
    spacing: Dp = 2.dp,
    inactiveColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 100),
        label = "linear_vu_level"
    )
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            val barThreshold = (index + 1f) / barCount
            val isActive = animatedLevel >= barThreshold
            
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(barHeight)
                    .clip(RoundedCornerShape(barWidth / 2))
                    .background(
                        if (isActive) {
                            AudioColors.getAudioLevelColor(barThreshold)
                        } else {
                            inactiveColor
                        }
                    )
            )
        }
    }
}

/**
 * Microphone button with recording state visualization
 */
@Composable
fun MicrophoneButton(
    modifier: Modifier = Modifier,
    isRecording: Boolean = false,
    audioLevel: Float = 0f,
    size: Dp = WhisperDesignTokens.MicButtonSize,
    onClick: () -> Unit,
    onLongClick: () -> Unit = onClick,
    enabled: Boolean = true
) {
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = if (isRecording) {
            AudioColors.voiceActive
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        contentColor = if (isRecording) {
            Color.White
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isRecording) 1f + (audioLevel * 0.1f) else 1f,
        animationSpec = tween(100),
        label = "mic_scale"
    )
    
    Box(
        contentAlignment = Alignment.Center
    ) {
        // Pulse effect when recording
        if (isRecording) {
            val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )
            
            Box(
                modifier = Modifier
                    .size(size * pulseScale)
                    .clip(CircleShape)
                    .background(
                        AudioColors.voiceActive.copy(alpha = 0.2f)
                    )
            )
        }
        
        // Main button
        Button(
            onClick = onClick,
            modifier = modifier
                .size(size)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                },
            enabled = enabled,
            colors = buttonColors,
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = if (isRecording) {
                    androidx.compose.material.icons.Icons.Default.Stop
                } else {
                    androidx.compose.material.icons.Icons.Default.Mic
                },
                contentDescription = if (isRecording) "Stop recording" else "Start recording",
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}