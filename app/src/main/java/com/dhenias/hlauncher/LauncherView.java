package com.dhenias.hlauncher;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import java.util.*;

public class LauncherView extends FrameLayout {

    private static final String[] CAT_ORDER = {
        "social","games","media","tools","finance","health","education","travel","shopping","other"
    };
    private static final Map<String, String[]> CAT_META = new LinkedHashMap<>();
    static {
        CAT_META.put("social",    new String[]{"Social",    "#1DA1F2"});
        CAT_META.put("games",     new String[]{"Games",     "#A855F7"});
        CAT_META.put("media",     new String[]{"Media",     "#FB923C"});
        CAT_META.put("tools",     new String[]{"Tools",     "#22C55E"});
        CAT_META.put("finance",   new String[]{"Finance",   "#34D399"});
        CAT_META.put("health",    new String[]{"Health",    "#EF4444"});
        CAT_META.put("education", new String[]{"Education", "#FBB724"});
        CAT_META.put("travel",    new String[]{"Travel",    "#0EA5E9"});
        CAT_META.put("shopping",  new String[]{"Shopping",  "#F43F5E"});
        CAT_META.put("other",     new String[]{"Other",     "#94A3B8"});
    }

    private final List<AppInfo> apps;
    private final Context ctx;

    public LauncherView(Context context, List<AppInfo> apps) {
        super(context);
        this.ctx = context;
        this.apps = apps;
        setBackgroundColor(Color.TRANSPARENT);
        buildUI();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private void buildUI() {
        removeAllViews();

        int cols      = Prefs.getColumns(ctx);
        int iconSizeDp= Prefs.getIconSize(ctx);
        boolean labels= Prefs.getShowLabels(ctx);
        float alpha   = Prefs.getCardAlpha(ctx);
        String accent = Prefs.getAccentColor(ctx);

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);

        // ── top bar: clock + settings button ──
        FrameLayout topBar = new FrameLayout(ctx);
        ClockView clock = new ClockView(ctx);
        FrameLayout.LayoutParams clockLp = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, dp(120));
        clockLp.setMargins(dp(20), dp(48), dp(20), 0);
        topBar.addView(clock, clockLp);

        // settings gear button
        TextView settingsBtn = new TextView(ctx);
        settingsBtn.setText("⚙");
        settingsBtn.setTextSize(22);
        settingsBtn.setTextColor(Color.parseColor("#99ffffff"));
        settingsBtn.setPadding(dp(12), dp(12), dp(12), dp(12));
        FrameLayout.LayoutParams gearLp = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
        gearLp.gravity = Gravity.TOP | Gravity.END;
        gearLp.setMargins(0, dp(48), dp(8), 0);
        settingsBtn.setOnClickListener(v -> {
            if (ctx instanceof MainActivity) {
                ((MainActivity) ctx).openSettings();
            }
        });
        topBar.addView(settingsBtn, gearLp);

        LinearLayout.LayoutParams topLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(168));
        root.addView(topBar, topLp);

        // ── scrollable app grid ──
        ScrollView sv = new ScrollView(ctx);
        sv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        sv.setVerticalScrollBarEnabled(false);

        LinearLayout content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), 0, dp(16), dp(90));

        // group apps
        Map<String, List<AppInfo>> grouped = new LinkedHashMap<>();
        for (String cat : CAT_ORDER) grouped.put(cat, new ArrayList<>());
        for (AppInfo app : apps) {
            String cat = app.category;
            if (!grouped.containsKey(cat)) grouped.put(cat, new ArrayList<>());
            Objects.requireNonNull(grouped.get(cat)).add(app);
        }

        for (String cat : CAT_ORDER) {
            List<AppInfo> list = grouped.get(cat);
            if (list == null || list.isEmpty()) continue;
            String[] meta = CAT_META.getOrDefault(cat, new String[]{cat, "#94A3B8"});
            int accentColor = Color.parseColor(meta[1]);

            // category section container
            LinearLayout section = new LinearLayout(ctx);
            section.setOrientation(LinearLayout.VERTICAL);
            int bgAlpha = (int)(alpha * 255);
            section.setBackgroundColor(Color.argb(bgAlpha,
                (int)(Color.red(accentColor) * 0.1f),
                (int)(Color.green(accentColor) * 0.1f),
                (int)(Color.blue(accentColor) * 0.1f)));
            section.setPadding(dp(12), dp(10), dp(12), dp(12));
            LinearLayout.LayoutParams secLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            secLp.setMargins(0, dp(12), 0, 0);

            // category label row
            LinearLayout labelRow = new LinearLayout(ctx);
            labelRow.setOrientation(LinearLayout.HORIZONTAL);
            labelRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView catLabel = new TextView(ctx);
            catLabel.setText(meta[0].toUpperCase());
            catLabel.setTextColor(accentColor);
            catLabel.setTextSize(10);
            catLabel.setAlpha(0.85f);
            catLabel.setTypeface(Typeface.DEFAULT_BOLD);
            catLabel.setLetterSpacing(0.12f);
            labelRow.addView(catLabel, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            // divider line
            View div = new View(ctx);
            div.setBackgroundColor(accentColor);
            div.setAlpha(0.2f);
            LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(0, dp(1), 1f);
            divLp.setMargins(dp(8), 0, 0, 0);
            labelRow.addView(div, divLp);
            section.addView(labelRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(28)));

            // icon grid rows
            for (int i = 0; i < list.size(); i += cols) {
                LinearLayout row = new LinearLayout(ctx);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rowLp.setMargins(0, dp(8), 0, 0);

                for (int j = 0; j < cols; j++) {
                    if (i + j < list.size()) {
                        row.addView(buildAppCell(list.get(i+j), accentColor, iconSizeDp, labels),
                            new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    } else {
                        row.addView(new View(ctx),
                            new LinearLayout.LayoutParams(0, dp(iconSizeDp + (labels ? 32 : 0)), 1f));
                    }
                }
                section.addView(row, rowLp);
            }
            content.addView(section, secLp);
        }

        sv.addView(content);
        root.addView(sv, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        addView(root, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private View buildAppCell(AppInfo app, int accentColor, int iconSizeDp, boolean showLabel) {
        LinearLayout cell = new LinearLayout(ctx);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER_HORIZONTAL);
        cell.setPadding(dp(4), 0, dp(4), 0);

        FrameLayout iconFrame = new FrameLayout(ctx);
        int sz = dp(iconSizeDp);
        iconFrame.setLayoutParams(new LinearLayout.LayoutParams(sz, sz));

        // tinted bg
        int bgC = Color.argb(40,
            Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor));
        ImageView bg = new ImageView(ctx);
        bg.setBackgroundDrawable(new RoundRectDrawable(bgC, dp(14)));
        iconFrame.addView(bg, new FrameLayout.LayoutParams(sz, sz));

        // icon
        ImageView iconView = new ImageView(ctx);
        iconView.setImageDrawable(app.icon);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int iconInner = (int)(sz * 0.72f);
        FrameLayout.LayoutParams iLp = new FrameLayout.LayoutParams(iconInner, iconInner);
        iLp.gravity = Gravity.CENTER;
        iconFrame.addView(iconView, iLp);
        cell.addView(iconFrame);

        if (showLabel) {
            TextView label = new TextView(ctx);
            label.setText(app.name);
            label.setTextColor(Color.parseColor("#cce8e8f0"));
            label.setTextSize(9.5f);
            label.setMaxLines(1);
            label.setEllipsize(TextUtils.TruncateAt.END);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(26));
            lLp.setMargins(0, dp(3), 0, 0);
            cell.addView(label, lLp);
        }

        cell.setOnClickListener(v -> {
            if (app.launchIntent != null) {
                app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(app.launchIntent);
            }
        });

        cell.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN)
                iconFrame.animate().scaleX(0.87f).scaleY(0.87f).setDuration(70).start();
            else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL)
                iconFrame.animate().scaleX(1f).scaleY(1f).setDuration(110).start();
            return false;
        });

        return cell;
    }
}
