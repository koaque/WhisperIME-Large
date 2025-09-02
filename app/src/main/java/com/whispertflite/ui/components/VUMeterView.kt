package com.whispertflite.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.whispertflite.R
import kotlin.math.*

/**
 * Custom VU Meter view for displaying audio levels in the IME
 * Supports both linear bar and waveform visualizations
 */
class VUMeterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_BAR_COUNT = 10
        private const val DEFAULT_BAR_SPACING = 4f
        private const val DEFAULT_CORNER_RADIUS = 2f
        private const val ANIMATION_DURATION = 100L
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var currentLevel = 0f
    private var targetLevel = 0f
    private var barCount = DEFAULT_BAR_COUNT
    private var barSpacing = DEFAULT_BAR_SPACING
    private var cornerRadius = DEFAULT_CORNER_RADIUS
    
    // Colors
    private var activeColorLow = 0
    private var activeColorMid = 0
    private var activeColorHigh = 0
    private var inactiveColor = 0
    
    // Animation
    private var lastUpdateTime = 0L
    
    init {
        initColors()
        setupPaint()
    }
    
    private fun initColors() {
        activeColorLow = ContextCompat.getColor(context, android.R.color.holo_green_light)
        activeColorMid = ContextCompat.getColor(context, android.R.color.holo_orange_light)
        activeColorHigh = ContextCompat.getColor(context, android.R.color.holo_red_light)
        inactiveColor = ContextCompat.getColor(context, android.R.color.darker_gray)
    }
    
    private fun setupPaint() {
        paint.style = Paint.Style.FILL
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = inactiveColor
        backgroundPaint.alpha = 80
    }
    
    fun setLevel(level: Float) {
        targetLevel = level.coerceIn(0f, 1f)
        
        // Smooth animation towards target
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime >= 16) { // ~60fps
            val diff = targetLevel - currentLevel
            currentLevel += diff * 0.3f // Smooth interpolation
            lastUpdateTime = currentTime
            invalidate()
        }
    }
    
    fun setBarCount(count: Int) {
        barCount = count.coerceIn(3, 20)
        invalidate()
    }
    
    fun setBarSpacing(spacing: Float) {
        barSpacing = spacing.coerceAtLeast(0f)
        invalidate()
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (24 * resources.displayMetrics.density).toInt()
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        
        setMeasuredDimension(width, height)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (width <= 0 || height <= 0) return
        
        drawLinearBars(canvas)
    }
    
    private fun drawLinearBars(canvas: Canvas) {
        val totalSpacing = barSpacing * (barCount - 1)
        val availableWidth = width - paddingLeft - paddingRight - totalSpacing
        val barWidth = availableWidth / barCount.toFloat()
        
        val barHeight = height - paddingTop - paddingBottom
        val baseY = paddingTop.toFloat()
        
        for (i in 0 until barCount) {
            val barThreshold = (i + 1f) / barCount
            val isActive = currentLevel >= barThreshold
            
            val left = paddingLeft + (i * (barWidth + barSpacing))
            val right = left + barWidth
            val top = baseY
            val bottom = baseY + barHeight
            
            val rect = RectF(left, top, right, bottom)
            
            // Draw background bar
            backgroundPaint.alpha = 60
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint)
            
            // Draw active bar with color based on level
            if (isActive) {
                paint.color = getColorForLevel(barThreshold)
                paint.alpha = (255 * (0.3f + 0.7f * min(currentLevel / barThreshold, 1f))).toInt()
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
                
                // Add glow effect for high levels
                if (barThreshold > 0.8f && currentLevel > 0.8f) {
                    paint.alpha = 40
                    val glowRect = RectF(
                        rect.left - 1,
                        rect.top - 1,
                        rect.right + 1,
                        rect.bottom + 1
                    )
                    canvas.drawRoundRect(glowRect, cornerRadius + 1, cornerRadius + 1, paint)
                }
            }
        }
    }
    
    private fun getColorForLevel(level: Float): Int {
        return when {
            level <= 0.4f -> activeColorLow
            level <= 0.7f -> {
                // Interpolate between low and mid
                val factor = (level - 0.4f) / 0.3f
                interpolateColor(activeColorLow, activeColorMid, factor)
            }
            level <= 0.9f -> {
                // Interpolate between mid and high
                val factor = (level - 0.7f) / 0.2f
                interpolateColor(activeColorMid, activeColorHigh, factor)
            }
            else -> activeColorHigh
        }
    }
    
    private fun interpolateColor(colorA: Int, colorB: Int, factor: Float): Int {
        val f = factor.coerceIn(0f, 1f)
        val invF = 1f - f
        
        val r = (Color.red(colorA) * invF + Color.red(colorB) * f).toInt()
        val g = (Color.green(colorA) * invF + Color.green(colorB) * f).toInt()
        val b = (Color.blue(colorA) * invF + Color.blue(colorB) * f).toInt()
        val a = (Color.alpha(colorA) * invF + Color.alpha(colorB) * f).toInt()
        
        return Color.argb(a, r, g, b)
    }
    
    /**
     * Reset the meter to zero
     */
    fun reset() {
        currentLevel = 0f
        targetLevel = 0f
        invalidate()
    }
    
    /**
     * Set colors for different level ranges
     */
    fun setLevelColors(low: Int, mid: Int, high: Int, inactive: Int) {
        activeColorLow = low
        activeColorMid = mid
        activeColorHigh = high
        inactiveColor = inactive
        backgroundPaint.color = inactive
        invalidate()
    }
    
    /**
     * Enable/disable smooth animation
     */
    fun setSmoothAnimation(enabled: Boolean) {
        // Could add animation control here
    }
}