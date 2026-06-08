package com.dhenias.hlauncher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class SettingsActivity extends Activity {

    private static final int BG = 0xFF0d0d14;
    private static final int SURFACE = 0x14ffffff;
    private static final int TEXT = 0xFFe8e8f0;
    private static final int MUTED = 0x77e8e8f0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BG);
        sv.setVerticalScrollBarEnabled(false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(52), dp(20), dp(40));

        // title
        TextView title = makeText("Settings", 24, TEXT, true);
        root.addView(title, lp(0, dp(48)));

        // ── SECTION: Appearance ──
        root.addView(sectionLabel("Appearance"));

        // Use wallpaper toggle
        root.addView(makeToggleRow(
            "Use system wallpaper",
            "Show your wallpaper behind the launcher",
            Prefs.getUseWallpaper(this),
            v -> Prefs.setUseWallpaper(this, v)
        ));

        // Show labels toggle
        root.addView(makeToggleRow(
            "Show app labels",
            "Display app name under icons",
            Prefs.getShowLabels(this),
            v -> Prefs.setShowLabels(this, v)
        ));

        // Card transparency slider
        root.addView(makeSliderRow(
            "Card transparency",
            "Background behind icons",
            0f, 1f, Prefs.getCardAlpha(this),
            v -> Prefs.setCardAlpha(this, v)
        ));

        // ── SECTION: Grid ──
        root.addView(sectionLabel("Grid"));

        // Columns
        root.addView(makeStepperRow(
            "Grid columns",
            "Icons per row",
            3, 6, Prefs.getColumns(this),
            v -> Prefs.setColumns(this, v)
        ));

        // Icon size
        root.addView(makeStepperRow(
            "Icon size",
            "Size in dp",
            40, 72, Prefs.getIconSize(this),
            v -> Prefs.setIconSize(this, v)
        ));

        // ── SECTION: Accent Color ──
        root.addView(sectionLabel("Accent color"));
        root.addView(makeColorPicker());

        // ── SECTION: Launcher ──
        root.addView(sectionLabel("Launcher"));

        // Set as default button
        LinearLayout setDefaultRow = makeCardRow();
        TextView setDefaultLabel = makeText("Set as default launcher", 15, TEXT, false);
        TextView setDefaultSub = makeText("Open system prompt to set hLauncher as home", 12, MUTED, false);
        LinearLayout setDefaultTexts = new LinearLayout(this);
        setDefaultTexts.setOrientation(LinearLayout.VERTICAL);
        setDefaultTexts.addView(setDefaultLabel);
        setDefaultTexts.addView(setDefaultSub);
        Button setDefaultBtn = new Button(this);
        setDefaultBtn.setText("Set");
        setDefaultBtn.setTextColor(TEXT);
        setDefaultBtn.setBackgroundColor(Color.parseColor(Prefs.getAccentColor(this)));
        setDefaultBtn.setPadding(dp(16), dp(4), dp(16), dp(4));
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnLp.gravity = Gravity.CENTER_VERTICAL;
        setDefaultRow.addView(setDefaultTexts, new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        setDefaultRow.addView(setDefaultBtn, btnLp);
        setDefaultBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        });
        root.addView(setDefaultRow, cardLp());

        // Done button
        Button doneBtn = new Button(this);
        doneBtn.setText("Apply & Close");
        doneBtn.setTextColor(Color.WHITE);
        doneBtn.setTypeface(Typeface.DEFAULT_BOLD);
        doneBtn.setBackgroundColor(Color.parseColor(Prefs.getAccentColor(this)));
        LinearLayout.LayoutParams doneLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        doneLp.setMargins(0, dp(24), 0, 0);
        doneBtn.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
        root.addView(doneBtn, doneLp);

        sv.addView(root);
        setContentView(sv);
    }

    // ── UI helpers ──

    interface BoolConsumer { void accept(boolean v); }
    interface FloatConsumer { void accept(float v); }
    interface IntConsumer  { void accept(int v); }

    private LinearLayout makeToggleRow(String label, String sub, boolean initial, BoolConsumer onChange) {
        LinearLayout row = makeCardRow();
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(makeText(label, 15, 0xFFe8e8f0, false));
        texts.addView(makeText(sub, 12, MUTED, false));
        Switch sw = new Switch(this);
        sw.setChecked(initial);
        sw.setOnCheckedChangeListener((b, v) -> onChange.accept(v));
        row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(sw);
        return row;
    }

    private LinearLayout makeSliderRow(String label, String sub, float min, float max, float initial, FloatConsumer onChange) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setBackgroundColor(SURFACE);
        col.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams colLp = cardLp();
        col.addView(makeText(label, 15, TEXT, false));
        col.addView(makeText(sub, 12, MUTED, false));
        SeekBar sb = new SeekBar(this);
        sb.setMax(100);
        sb.setProgress((int)((initial - min) / (max - min) * 100));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) {
                onChange.accept(min + (max - min) * p / 100f);
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        LinearLayout.LayoutParams sbLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sbLp.setMargins(0, dp(8), 0, 0);
        col.addView(sb, sbLp);
        root().addView(col, colLp); // will be added by caller
        return col;
    }

    private LinearLayout makeStepperRow(String label, String sub, int min, int max, int initial, IntConsumer onChange) {
        LinearLayout row = makeCardRow();
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(makeText(label, 15, TEXT, false));
        texts.addView(makeText(sub, 12, MUTED, false));
        row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        int[] val = {initial};
        TextView valTv = makeText(String.valueOf(initial), 16, TEXT, true);
        valTv.setGravity(Gravity.CENTER);
        valTv.setMinWidth(dp(32));

        Button minus = new Button(this);
        minus.setText("−");
        minus.setTextColor(TEXT);
        minus.setBackgroundColor(SURFACE);
        minus.setPadding(dp(12), dp(4), dp(12), dp(4));

        Button plus = new Button(this);
        plus.setText("+");
        plus.setTextColor(TEXT);
        plus.setBackgroundColor(SURFACE);
        plus.setPadding(dp(12), dp(4), dp(12), dp(4));

        minus.setOnClickListener(v -> {
            if (val[0] > min) { val[0]--; valTv.setText(String.valueOf(val[0])); onChange.accept(val[0]); }
        });
        plus.setOnClickListener(v -> {
            if (val[0] < max) { val[0]++; valTv.setText(String.valueOf(val[0])); onChange.accept(val[0]); }
        });

        LinearLayout stepper = new LinearLayout(this);
        stepper.setOrientation(LinearLayout.HORIZONTAL);
        stepper.setGravity(Gravity.CENTER_VERTICAL);
        stepper.addView(minus);
        stepper.addView(valTv, new LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.WRAP_CONTENT));
        stepper.addView(plus);
        row.addView(stepper);
        return row;
    }

    private LinearLayout makeColorPicker() {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.HORIZONTAL);
        grid.setPadding(dp(16), dp(12), dp(16), dp(12));
        grid.setBackgroundColor(SURFACE);
        grid.setWeightSum(6);
        LinearLayout.LayoutParams gridLp = cardLp();

        String[] colors = {"#7c6af7","#f76ac8","#22c55e","#fb923c","#0ea5e9","#ef4444"};
        String current = Prefs.getAccentColor(this);

        for (String hex : colors) {
            View dot = new View(this);
            int c = Color.parseColor(hex);
            dot.setBackgroundColor(c);
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(0, dp(36), 1f);
            dotLp.setMargins(dp(4), dp(4), dp(4), dp(4));
            if (hex.equals(current)) {
                dot.setAlpha(1f);
                dot.setScaleX(1.2f); dot.setScaleY(1.2f);
            } else {
                dot.setAlpha(0.6f);
            }
            dot.setOnClickListener(v -> {
                Prefs.setAccentColor(this, hex);
                for (int i = 0; i < grid.getChildCount(); i++) grid.getChildAt(i).setAlpha(0.6f);
                dot.setAlpha(1f);
            });
            grid.addView(dot, dotLp);
        }
        root().addView(grid, gridLp);
        return grid;
    }

    private LinearLayout makeCardRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(SURFACE);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));
        return row;
    }

    private TextView makeText(String t, int sp, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(t);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        return tv;
    }

    private View sectionLabel(String text) {
        TextView tv = makeText(text.toUpperCase(), 10, 0x99e8e8f0, true);
        tv.setLetterSpacing(0.15f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(4), dp(20), 0, dp(6));
        tv.setLayoutParams(lp);
        return tv;
    }

    private LinearLayout.LayoutParams cardLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(4), 0, 0);
        return lp;
    }

    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(
            w == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : w, h);
    }

    private ViewGroup root() {
        return (ViewGroup) ((ScrollView) getWindow().getDecorView()
            .findViewById(android.R.id.content)).getChildAt(0);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
