package com.aapr.customcalendar.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

/** Horizontal 0-255 alpha slider over a checkerboard, gradient tinted to the current opaque color. */
public final class AlphaSliderView extends View {

    public interface OnAlphaChangedListener {
        void onAlphaChanged(int alpha);
    }

    private int alpha = 255;
    private int opaqueColor = Color.WHITE;
    private OnAlphaChangedListener listener;

    private final Paint checkerLight = new Paint();
    private final Paint checkerDark = new Paint();
    private final Paint gradientPaint = new Paint();
    private final Paint indicatorPaint = new Paint();

    public AlphaSliderView(Context context) {
        super(context);
        checkerLight.setColor(Color.WHITE);
        checkerDark.setColor(Color.LTGRAY);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setAntiAlias(true);
    }

    public void setListener(OnAlphaChangedListener listener) {
        this.listener = listener;
    }

    public void setAlpha255(int alpha) {
        this.alpha = alpha;
        invalidate();
    }

    public void setOpaqueColor(int opaqueColor) {
        this.opaqueColor = opaqueColor;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }

        int squares = 10;
        float squareW = w / (float) squares;
        for (int i = 0; i < squares; i++) {
            Paint p = (i % 2 == 0) ? checkerLight : checkerDark;
            canvas.drawRect(i * squareW, 0, (i + 1) * squareW, h, p);
        }

        int transparent = opaqueColor & 0x00FFFFFF;
        int opaque = opaqueColor | 0xFF000000;
        gradientPaint.setShader(new LinearGradient(0, 0, w, 0, transparent, opaque, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, gradientPaint);

        float cx = (alpha / 255f) * w;
        indicatorPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(cx - 6f, 0, cx + 6f, h, 4f, 4f, indicatorPaint);
        indicatorPaint.setColor(Color.BLACK);
        canvas.drawRoundRect(cx - 4f, 2f, cx + 4f, h - 2f, 4f, 4f, indicatorPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            int w = getWidth();
            alpha = Math.max(0, Math.min(255, Math.round((event.getX() / w) * 255f)));
            // Snap to the extremes near the edges — hitting the exact edge pixel is hard, and
            // "fully opaque" / "fully transparent" are the two values users most rely on landing on.
            if (alpha <= 12) {
                alpha = 0;
            } else if (alpha >= 243) {
                alpha = 255;
            }
            invalidate();
            if (listener != null) {
                listener.onAlphaChanged(alpha);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
