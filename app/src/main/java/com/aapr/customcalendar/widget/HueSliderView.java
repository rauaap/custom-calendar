package com.aapr.customcalendar.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

/** Horizontal 0-360 hue spectrum slider. */
public final class HueSliderView extends View {

    public interface OnHueChangedListener {
        void onHueChanged(float hue);
    }

    private float hue = 0f;
    private OnHueChangedListener listener;
    private final Paint gradientPaint = new Paint();
    private final Paint indicatorPaint = new Paint();

    public HueSliderView(Context context) {
        super(context);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setAntiAlias(true);
    }

    public void setListener(OnHueChangedListener listener) {
        this.listener = listener;
    }

    public void setHue(float hue) {
        this.hue = hue;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }

        int[] colors = new int[13];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = Color.HSVToColor(new float[]{i * 30f, 1f, 1f});
        }
        gradientPaint.setShader(new LinearGradient(0, 0, w, 0, colors, null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, gradientPaint);

        float cx = (hue / 360f) * w;
        indicatorPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(cx - 6f, 0, cx + 6f, h, 4f, 4f, indicatorPaint);
        indicatorPaint.setColor(Color.BLACK);
        canvas.drawRoundRect(cx - 4f, 2f, cx + 4f, h - 2f, 4f, 4f, indicatorPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            int w = getWidth();
            hue = Math.max(0f, Math.min(359.999f, (event.getX() / w) * 360f));
            invalidate();
            if (listener != null) {
                listener.onHueChanged(hue);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
