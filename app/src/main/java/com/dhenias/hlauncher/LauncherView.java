package com.dhenias.hlauncher;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import java.util.*;

public class LauncherView extends FrameLayout {

    private static final int COL = 4;
    private static final int ICON_DP = 56;
    private static final int PADDING_DP = 16;
    private static final int LABEL_HEIGHT_DP = 32;
    private static final int CAT_HEADER_DP = 36;
    private static final int ITEM_SPACING_DP = 12;

    private final List<AppInfo> apps;
    private final Context ctx;
    private ScrollView scrollView;
    private LinearLayout contentLayout;

    // category order + display
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

    public LauncherView(Context context, List<AppInfo> apps) {
        super(context);
        this.ctx = context;
        this.apps = apps;
        setBackgroundColor(Color.parseColor("#0a0a0f"));
        buildUI();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private void buildUI() {
        // clock header
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);

        // clock
        ClockView clock = new ClockView(ctx);
        LinearLayout.LayoutParams clockParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(120));
        clockParams.setMargins(dp(PADDING_DP), dp(48), dp(PADDING_DP), dp(8));
        root.addView(clock, clockParams);

        // scroll content
        scrollView = new ScrollView(ctx);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scrollView.setVerticalScrollBarEnabled(false);
        contentLayout = new LinearLayout(ctx);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(PADDING_DP), 0, dp(PADDING_DP), dp(80));

        // group by category
        Map<String, List<AppInfo>> grouped = new LinkedHashMap<>();
        for (String cat : CAT_ORDER) grouped.put(cat, new ArrayList<>());
        for (AppInfo app : apps) {
            String cat = app.category;
            if (!grouped.containsKey(cat)) grouped.put(cat, new ArrayList<>());
            grouped.get(cat).add(app);
        }

        for (String cat : CAT_ORDER) {
            List<AppInfo> list = grouped.get(cat);
            if (list == null || list.isEmpty()) continue;
            String[] meta = CAT_META.getOrDefault(cat, new String[]{cat, "#94A3B8"});

            // category label
            TextView label = new TextView(ctx);
            label.setText(meta[0].toUpperCase());
            label.setTextColor(Color.parseColor(meta[1]));
            label.setTextSize(10);
            label.setLetterSpacing(0.15f);
            label.setAlpha(0.75f);
            label.setTypeface(Typeface.DEFAULT_BOLD);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(CAT_HEADER_DP));
            lp.setMargins(dp(4), dp(8), 0, 0);
            contentLayout.addView(label, lp);

            // divider
            View divider = new View(ctx);
            divider.setBackgroundColor(Color.parseColor("#1a1a2e"));
            contentLayout.addView(divider, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));

            // grid rows
            int cols = COL;
            for (int i = 0; i < list.size(); i += cols) {
                LinearLayout row = new LinearLayout(ctx);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rowLp.setMargins(0, dp(ITEM_SPACING_DP), 0, 0);

                for (int j = 0; j < cols; j++) {
                    if (i + j < list.size()) {
                        AppInfo app = list.get(i + j);
                        View cell = buildAppCell(app, meta[1]);
                        LinearLayout.LayoutParams cellLp = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        row.addView(cell, cellLp);
                    } else {
                        // empty placeholder
                        View spacer = new View(ctx);
                        row.addView(spacer, new LinearLayout.LayoutParams(0,
                            dp(ICON_DP + LABEL_HEIGHT_DP), 1f));
                    }
                }
                contentLayout.addView(row);
            }
        }

        scrollView.addView(contentLayout);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(scrollView, scrollLp);
        addView(root, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private View buildAppCell(AppInfo app, String accentHex) {
        LinearLayout cell = new LinearLayout(ctx);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER_HORIZONTAL);
        cell.setPadding(dp(4), 0, dp(4), 0);

        // icon container
        FrameLayout iconFrame = new FrameLayout(ctx);
        iconFrame.setLayoutParams(new LinearLayout.LayoutParams(dp(ICON_DP), dp(ICON_DP)));

        // rounded background
        int accent = Color.parseColor(accentHex);
        int bgColor = Color.argb(30, Color.red(accent), Color.green(accent), Color.blue(accent));

        ImageView bg = new ImageView(ctx);
        bg.setBackgroundDrawable(new RoundRectDrawable(bgColor, dp(14)));
        iconFrame.addView(bg, new FrameLayout.LayoutParams(dp(ICON_DP), dp(ICON_DP)));

        // app icon
        ImageView iconView = new ImageView(ctx);
        iconView.setImageDrawable(app.icon);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(dp(40), dp(40));
        iconLp.gravity = Gravity.CENTER;
        iconFrame.addView(iconView, iconLp);

        cell.addView(iconFrame);

        // label
        TextView label = new TextView(ctx);
        label.setText(app.name);
        label.setTextColor(Color.parseColor("#99e8e8f0"));
        label.setTextSize(10);
        label.setMaxLines(1);
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(LABEL_HEIGHT_DP));
        labelLp.setMargins(0, dp(4), 0, 0);
        label.setLayoutParams(labelLp);
        cell.addView(label);

        // click — launch app
        cell.setOnClickListener(v -> {
            if (app.launchIntent != null) {
                app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(app.launchIntent);
            }
        });

        // press effect
        cell.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                iconFrame.animate().scaleX(0.88f).scaleY(0.88f).setDuration(80).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP ||
                       event.getAction() == MotionEvent.ACTION_CANCEL) {
                iconFrame.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
            }
            return false;
        });

        return cell;
    }
}
