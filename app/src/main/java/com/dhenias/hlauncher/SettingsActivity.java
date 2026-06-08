package com.dhenias.hlauncher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class SettingsActivity extends Activity {

    private static final int BG      = 0xFF0d0d16;
    private static final int SURFACE = 0x18ffffff;
    private static final int TEXT    = 0xFFe8e8f0;
    private static final int MUTED   = 0x88e8e8f0;

    private LinearLayout rootLayout;

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
        sv.setOverScrollMode(View.OVER_SCROLL_NEVER);

        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(dp(20), dp(52), dp(20), dp(40));

        buildContent();

        sv.addView(rootLayout, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);
    }

    private void buildContent() {
        rootLayout.removeAllViews();

        // title + back
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView backBtn = new TextView(this);
        backBtn.setText("←");
        backBtn.setTextSize(22);
        backBtn.setTextColor(TEXT);
        backBtn.setPadding(0, 0, dp(12), 0);
        backBtn.setOnClickListener(v -> { setResult(RESULT_OK); finish(); });
        titleRow.addView(backBtn);

        TextView title = new TextView(this);
        title.setText("Settings");
        title.setTextSize(24);
        title.setTextColor(TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        titleRow.addView(title);
        add(titleRow, ViewGroup.LayoutParams.MATCH_PARENT, dp(56));

        // ── APPEARANCE ──
        addSection("Appearance");

        addToggle("Use system wallpaper",
            "Show wallpaper behind the launcher",
            Prefs.getUseWallpaper(this),
            v -> Prefs.setUseWallpaper(this, v));

        addToggle("Show app labels",
            "Display name under each icon",
            Prefs.getShowLabels(this),
            v -> Prefs.setShowLabels(this, v));

        addSlider("Card transparency",
            "Background opacity behind icons",
            0f, 1f, Prefs.getCardAlpha(this),
            v -> Prefs.setCardAlpha(this, v));

        // ── GRID ──
        addSection("Grid");

        addStepper("Grid columns", "Icons per row",
            3, 6, Prefs.getColumns(this),
            v -> Prefs.setColumns(this, v));

        addStepper("Icon size", "Size in dp (40–72)",
            40, 72, Prefs.getIconSize(this),
            v -> Prefs.setIconSize(this, v));

        // ── ACCENT COLOR ──
        addSection("Accent color");
        addColorPicker();

        // ── LAUNCHER ──
        addSection("Launcher");
        addSetDefaultRow();

        // ── APPLY BUTTON ──
        Button apply = new Button(this);
        apply.setText("Apply & Close");
        apply.setTextColor(Color.WHITE);
        apply.setTypeface(Typeface.DEFAULT_BOLD);
        apply.setTextSize(15);
        apply.setBackgroundColor(Color.parseColor(Prefs.getAccentColor(this)));
        LinearLayout.LayoutParams applyLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        applyLp.setMargins(0, dp(24), 0, 0);
        apply.setOnClickListener(v -> { setResult(RESULT_OK); finish(); });
        rootLayout.addView(apply, applyLp);
    }

    // ── ROW BUILDERS ──

    private void addToggle(String label, String sub, boolean init, BoolCb cb) {
        LinearLayout row = makeCard();
        LinearLayout texts = vStack();
        texts.addView(labelText(label));
        texts.addView(subText(sub));
        Switch sw = new Switch(this);
        sw.setChecked(init);
        sw.setOnCheckedChangeListener((b, v) -> cb.on(v));
        row.addView(texts, new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(sw);
        rootLayout.addView(row, cardLp());
    }

    private void addSlider(String label, String sub, float min, float max, float init, FloatCb cb) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setBackgroundColor(SURFACE);
        col.setPadding(dp(16), dp(14), dp(16), dp(14));
        col.addView(labelText(label));
        col.addView(subText(sub));
        SeekBar sb = new SeekBar(this);
        sb.setMax(100);
        sb.setProgress(Math.round((init - min) / (max - min) * 100));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) { cb.on(min + (max-min)*p/100f); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        LinearLayout.LayoutParams sbLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sbLp.setMargins(0, dp(10), 0, 0);
        col.addView(sb, sbLp);
        rootLayout.addView(col, cardLp());
    }

    private void addStepper(String label, String sub, int min, int max, int init, IntCb cb) {
        LinearLayout row = makeCard();
        LinearLayout texts = vStack();
        texts.addView(labelText(label));
        texts.addView(subText(sub));
        row.addView(texts, new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        int[] val = {init};
        TextView valTv = new TextView(this);
        valTv.setText(String.valueOf(init));
        valTv.setTextSize(16);
        valTv.setTextColor(TEXT);
        valTv.setTypeface(Typeface.DEFAULT_BOLD);
        valTv.setGravity(Gravity.CENTER);
        valTv.setMinWidth(dp(36));

        Button minus = stepBtn("−");
        Button plus  = stepBtn("+");
        minus.setOnClickListener(v -> {
            if (val[0] > min) { val[0]--; valTv.setText(String.valueOf(val[0])); cb.on(val[0]); }
        });
        plus.setOnClickListener(v -> {
            if (val[0] < max) { val[0]++; valTv.setText(String.valueOf(val[0])); cb.on(val[0]); }
        });

        LinearLayout stepper = new LinearLayout(this);
        stepper.setOrientation(LinearLayout.HORIZONTAL);
        stepper.setGravity(Gravity.CENTER_VERTICAL);
        stepper.addView(minus);
        stepper.addView(valTv, new LinearLayout.LayoutParams(dp(40),
            ViewGroup.LayoutParams.WRAP_CONTENT));
        stepper.addView(plus);
        row.addView(stepper);
        rootLayout.addView(row, cardLp());
    }

    private void addColorPicker() {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.HORIZONTAL);
        grid.setPadding(dp(16), dp(14), dp(16), dp(14));
        grid.setBackgroundColor(SURFACE);
        grid.setWeightSum(6f);

        String[] colors = {"#7c6af7","#f76ac8","#22c55e","#fb923c","#0ea5e9","#ef4444"};
        String cur = Prefs.getAccentColor(this);
        View[] dots = new View[colors.length];

        for (int i = 0; i < colors.length; i++) {
            final String hex = colors[i];
            final int idx = i;
            View dot = new View(this);
            dot.setBackgroundColor(Color.parseColor(hex));
            dot.setAlpha(hex.equals(cur) ? 1f : 0.45f);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(38), 1f);
            lp.setMargins(dp(4), dp(2), dp(4), dp(2));
            dots[i] = dot;
            dot.setOnClickListener(v -> {
                Prefs.setAccentColor(this, hex);
                for (View d : dots) d.setAlpha(0.45f);
                dot.setAlpha(1f);
            });
            grid.addView(dot, lp);
        }
        rootLayout.addView(grid, cardLp());
    }

    private void addSetDefaultRow() {
        LinearLayout row = makeCard();
        LinearLayout texts = vStack();
        texts.addView(labelText("Set as default launcher"));
        texts.addView(subText("Open system prompt to set hLauncher as home"));
        row.addView(texts, new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button btn = new Button(this);
        btn.setText("Set");
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(Color.parseColor(Prefs.getAccentColor(this)));
        btn.setPadding(dp(16), dp(4), dp(16), dp(4));
        btn.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(i);
        });
        row.addView(btn);
        rootLayout.addView(row, cardLp());
    }

    // ── SECTION LABEL ──
    private void addSection(String text) {
        TextView tv = new TextView(this);
        tv.setText(text.toUpperCase());
        tv.setTextSize(10);
        tv.setTextColor(MUTED);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setLetterSpacing(0.15f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(4), dp(22), 0, dp(6));
        rootLayout.addView(tv, lp);
    }

    // ── HELPERS ──
    private LinearLayout makeCard() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(SURFACE);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));
        return row;
    }

    private LinearLayout vStack() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private TextView labelText(String t) {
        TextView tv = new TextView(this);
        tv.setText(t); tv.setTextSize(15); tv.setTextColor(TEXT);
        return tv;
    }

    private TextView subText(String t) {
        TextView tv = new TextView(this);
        tv.setText(t); tv.setTextSize(12); tv.setTextColor(MUTED);
        return tv;
    }

    private Button stepBtn(String t) {
        Button b = new Button(this);
        b.setText(t); b.setTextColor(TEXT);
        b.setBackgroundColor(0x22ffffff);
        b.setPadding(dp(14), dp(4), dp(14), dp(4));
        return b;
    }

    private void add(View v, int w, int h) {
        rootLayout.addView(v, new LinearLayout.LayoutParams(w, h));
    }

    private LinearLayout.LayoutParams cardLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(4), 0, 0);
        return lp;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    interface BoolCb  { void on(boolean v); }
    interface FloatCb { void on(float v); }
    interface IntCb   { void on(int v); }
}
