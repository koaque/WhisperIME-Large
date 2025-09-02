package com.whispertflite.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * WhisperIME Design System - Inspired by modern voice apps like Google Recorder, Otter.ai
 * Features: Dynamic Material 3, sophisticated audio-visual elements, professional gradients
 */

// Primary brand colors - Deep voice/audio inspired palette
private val WhisperBlue = Color(0xFF1565C0)
private val WhisperBlueVariant = Color(0xFF0D47A1)
private val WhisperTeal = Color(0xFF00838F)
private val WhisperTealVariant = Color(0xFF006064)

// Accent colors for audio visualization
private val VoiceGreen = Color(0xFF2E7D32)
private val VoiceGreenLight = Color(0xFF4CAF50)
private val VoiceOrange = Color(0xFFFF8F00)
private val VoiceRed = Color(0xFFD32F2F)

// Neutral sophisticated palette
private val WhisperGray = Color(0xFF37474F)
private val WhisperGrayLight = Color(0xFF62727B)
private val WhisperGrayDark = Color(0xFF263238)

private val LightColorScheme = lightColorScheme(
    primary = WhisperBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    
    secondary = WhisperTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2F1),
    onSecondaryContainer = Color(0xFF004D40),
    
    tertiary = VoiceGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8F5E8),
    onTertiaryContainer = Color(0xFF1B5E20),
    
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410E0B),
    
    background = Color(0xFFFEFBFF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFFEFBFF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFF90CAF9)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1976D2),
    onPrimaryContainer = Color(0xFFE3F2FD),
    
    secondary = Color(0xFF4DD0E1),
    onSecondary = Color(0xFF006064),
    secondaryContainer = Color(0xFF00838F),
    onSecondaryContainer = Color(0xFFE0F2F1),
    
    tertiary = Color(0xFF81C784),
    onTertiary = Color(0xFF1B5E20),
    tertiaryContainer = Color(0xFF2E7D32),
    onTertiaryContainer = Color(0xFFE8F5E8),
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = Color(0xFF101214),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF101214),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Color(0xFF1565C0)
)

// Audio visualization colors
object AudioColors {
    val voiceActive = VoiceGreenLight
    val voiceInactive = Color(0xFF81C784)
    val audioLevel1 = VoiceGreen
    val audioLevel2 = Color(0xFF66BB6A)
    val audioLevel3 = Color(0xFF4CAF50)
    val audioLevel4 = VoiceOrange
    val audioLevel5 = VoiceRed
    
    @Composable
    fun getAudioLevelColor(level: Float): Color {
        return when {
            level < 0.2f -> audioLevel1
            level < 0.4f -> audioLevel2
            level < 0.6f -> audioLevel3
            level < 0.8f -> audioLevel4
            else -> audioLevel5
        }
    }
}

// Model download status colors
object ModelColors {
    val downloading = Color(0xFF2196F3)
    val downloaded = VoiceGreen
    val error = VoiceRed
    val notDownloaded = Color(0xFF757575)
    val verifying = VoiceOrange
}

// Typography - Professional and readable
private val WhisperTypography = Typography().let { default ->
    default.copy(
        displayLarge = default.displayLarge.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = androidx.compose.ui.unit.sp(-0.25)
        ),
        displayMedium = default.displayMedium.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default
        ),
        displaySmall = default.displaySmall.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default
        ),
        headlineLarge = default.headlineLarge.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = androidx.compose.ui.unit.sp(-0.25)
        ),
        headlineMedium = default.headlineMedium.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = androidx.compose.ui.unit.sp(0)
        ),
        headlineSmall = default.headlineSmall.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default
        ),
        titleLarge = default.titleLarge.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = androidx.compose.ui.unit.sp(0)
        ),
        titleMedium = default.titleMedium.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = androidx.compose.ui.unit.sp(0.15)
        ),
        titleSmall = default.titleSmall.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = androidx.compose.ui.unit.sp(0.1)
        ),
        bodyLarge = default.bodyLarge.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = androidx.compose.ui.unit.sp(0.5)
        ),
        bodyMedium = default.bodyMedium.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = androidx.compose.ui.unit.sp(0.25)
        ),
        bodySmall = default.bodySmall.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = androidx.compose.ui.unit.sp(0.4)
        ),
        labelLarge = default.labelLarge.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = androidx.compose.ui.unit.sp(0.1)
        ),
        labelMedium = default.labelMedium.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = androidx.compose.ui.unit.sp(0.5)
        ),
        labelSmall = default.labelSmall.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = androidx.compose.ui.unit.sp(0.5)
        )
    )
}

// Shapes - Sophisticated, modern with subtle roundings
private val WhisperShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(androidx.compose.ui.unit.dp(4)),
    small = androidx.compose.foundation.shape.RoundedCornerShape(androidx.compose.ui.unit.dp(8)),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(androidx.compose.ui.unit.dp(12)),
    large = androidx.compose.foundation.shape.RoundedCornerShape(androidx.compose.ui.unit.dp(16)),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(androidx.compose.ui.unit.dp(24))
)

@Composable
fun WhisperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WhisperTypography,
        shapes = WhisperShapes,
        content = content
    )
}

// Custom design tokens for WhisperIME specific components
object WhisperDesignTokens {
    // Spacing system
    val SpacingXXS = androidx.compose.ui.unit.dp(2)
    val SpacingXS = androidx.compose.ui.unit.dp(4)
    val SpacingS = androidx.compose.ui.unit.dp(8)
    val SpacingM = androidx.compose.ui.unit.dp(16)
    val SpacingL = androidx.compose.ui.unit.dp(24)
    val SpacingXL = androidx.compose.ui.unit.dp(32)
    val SpacingXXL = androidx.compose.ui.unit.dp(48)
    
    // Component specific measurements
    val MicButtonSize = androidx.compose.ui.unit.dp(56)
    val MicButtonSizeSmall = androidx.compose.ui.unit.dp(40)
    val VUMeterHeight = androidx.compose.ui.unit.dp(4)
    val VUMeterHeightLarge = androidx.compose.ui.unit.dp(8)
    val ModelCardHeight = androidx.compose.ui.unit.dp(120)
    val TranscriptMinHeight = androidx.compose.ui.unit.dp(200)
    
    // Animation durations
    const val AnimationFast = 150
    const val AnimationNormal = 300
    const val AnimationSlow = 500
    
    // Audio level thresholds
    const val AudioLevelLow = 0.2f
    const val AudioLevelMedium = 0.5f
    const val AudioLevelHigh = 0.8f
}

// Extension functions for theme-aware components
@Composable
fun ColorScheme.audioVisualizationColors(): List<Color> {
    return listOf(
        AudioColors.audioLevel1,
        AudioColors.audioLevel2,
        AudioColors.audioLevel3,
        AudioColors.audioLevel4,
        AudioColors.audioLevel5
    )
}

@Composable  
fun ColorScheme.modelStatusColor(isDownloaded: Boolean, isDownloading: Boolean, hasError: Boolean): Color {
    return when {
        hasError -> ModelColors.error
        isDownloading -> ModelColors.downloading
        isDownloaded -> ModelColors.downloaded
        else -> ModelColors.notDownloaded
    }
}