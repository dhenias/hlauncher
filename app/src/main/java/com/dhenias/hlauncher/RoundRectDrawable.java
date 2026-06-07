package com.dhenias.hlauncher;

import android.graphics.*;
import android.graphics.drawable.Drawable;

public class RoundRectDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float radius;

    public RoundRectDrawable(int color, float radius) {
        paint.setColor(color);
        this.radius = radius;
    }

    @Override
    public void draw(Canvas canvas) {
        RectF rect = new RectF(getBounds());
        canvas.drawRoundRect(rect, radius, radius, paint);
    }

    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
    @Override public void setColorFilter(ColorFilter cf) { paint.setColorFilter(cf); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
