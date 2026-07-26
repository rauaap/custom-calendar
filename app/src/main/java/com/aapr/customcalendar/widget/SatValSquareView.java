package com.aapr.customcalendar.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

/** Classic saturation (x-axis) / value (y-axis) square for a fixed hue. */
public final class SatValSquareView extends View {

    public interface OnSatValChangedListener {
        void onSatValChanged(float sat, float val);
    }

    private float hue = 0f;
    private float sat = 1f;
    private float val = 1f;
    private OnSatValChangedListener listener;

    private final Paint satPaint = new Paint();
    private final Paint valPaint = new Paint();
    private final Paint indicatorPaint = new Paint();

    public SatValSquareView(Context context) {
        super(context);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setStrokeWidth(4f);
        indicatorPaint.setAntiAlias(true);
    }

    public void setListener(OnSatValChangedListener listener) {
        this.listener = listener;
    }

    public void setHue(float hue) {
        this.hue = hue;
        invalidate();
    }

    public void setSatVal(float sat, float val) {
        this.sat = sat;
        this.val = val;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }

        int hueColor = Color.HSVToColor(new float[]{hue, 1f, 1f});
        satPaint.setShader(new LinearGradient(0, 0, w, 0, Color.WHITE, hueColor, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, satPaint);

        valPaint.setShader(new LinearGradient(0, 0, 0, h, 0x00000000, 0xFF000000, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, valPaint);

        float cx = sat * w;
        float cy = (1f - val) * h;
        indicatorPaint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, 12f, indicatorPaint);
        indicatorPaint.setColor(Color.BLACK);
        canvas.drawCircle(cx, cy, 9f, indicatorPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            int w = getWidth();
            int h = getHeight();
            sat = clamp(event.getX() / w);
            val = 1f - clamp(event.getY() / h);
            invalidate();
            if (listener != null) {
                listener.onSatValChanged(sat, val);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
