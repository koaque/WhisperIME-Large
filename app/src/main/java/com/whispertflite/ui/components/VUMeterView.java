package com.whispertflite.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Simple VU Meter View for audio level visualization
 */
public class VUMeterView extends View {
    
    private Paint paint;
    private float audioLevel = 0.0f;
    
    public VUMeterView(Context context) {
        super(context);
        init();
    }
    
    public VUMeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public VUMeterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
    }
    
    public void setAudioLevel(float level) {
        this.audioLevel = Math.max(0.0f, Math.min(1.0f, level));
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw background
        paint.setColor(Color.LTGRAY);
        canvas.drawRect(0, 0, width, height, paint);
        
        // Draw audio level
        paint.setColor(Color.GREEN);
        float barWidth = width * audioLevel;
        canvas.drawRect(0, 0, barWidth, height, paint);
    }
}