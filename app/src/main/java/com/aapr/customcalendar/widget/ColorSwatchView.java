package com.aapr.customcalendar.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/** Small tappable square showing the current color, with a checkerboard behind it so alpha is visible. */
public final class ColorSwatchView extends View {

    private final Paint checkerLight = new Paint();
    private final Paint checkerDark = new Paint();
    private final Paint colorPaint = new Paint();
    private final Paint borderPaint = new Paint();

    public ColorSwatchView(Context context) {
        this(context, null);
    }

    public ColorSwatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        checkerLight.setColor(Color.WHITE);
        checkerDark.setColor(Color.LTGRAY);
        borderPaint.setColor(Color.DKGRAY);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        colorPaint.setColor(Color.WHITE);
    }

    public void setColor(int color) {
        colorPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        int half = w / 2;
        canvas.drawRect(0, 0, half, half, checkerLight);
        canvas.drawRect(half, 0, w, half, checkerDark);
        canvas.drawRect(0, half, half, h, checkerDark);
        canvas.drawRect(half, half, w, h, checkerLight);
        canvas.drawRect(0, 0, w, h, colorPaint);
        canvas.drawRect(1, 1, w - 1, h - 1, borderPaint);
    }
}
