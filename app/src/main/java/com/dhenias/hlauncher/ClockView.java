package com.dhenias.hlauncher;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClockView extends View {

    private final Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler();
    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            invalidate();
            handler.postDelayed(this, 1000);
        }
    };

    public ClockView(Context context) {
        super(context);
        timePaint.setColor(Color.WHITE);
        timePaint.setAlpha(230);
        timePaint.setTextSize(sp(52));
        timePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        timePaint.setLetterSpacing(-0.05f);

        datePaint.setColor(Color.parseColor("#99e8e8f0"));
        datePaint.setTextSize(sp(13));
        datePaint.setTypeface(Typeface.DEFAULT);
        datePaint.setLetterSpacing(0.05f);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.post(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(ticker);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Date now = new Date();
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now);
        String date = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(now);

        int x = dp(4);
        canvas.drawText(date, x, dp(22), datePaint);
        canvas.drawText(time, x - dp(2), dp(80), timePaint);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
    private float sp(int v) {
        return v * getResources().getDisplayMetrics().scaledDensity;
    }
}
