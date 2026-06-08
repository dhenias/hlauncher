package com.dhenias.hlauncher;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String FILE = "hlauncher_prefs";

    public static SharedPreferences get(Context ctx) {
        return ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static int getIconSize(Context ctx) {
        return get(ctx).getInt("icon_size", 56);
    }
    public static void setIconSize(Context ctx, int v) {
        get(ctx).edit().putInt("icon_size", v).apply();
    }

    public static int getColumns(Context ctx) {
        return get(ctx).getInt("columns", 4);
    }
    public static void setColumns(Context ctx, int v) {
        get(ctx).edit().putInt("columns", v).apply();
    }

    public static boolean getShowLabels(Context ctx) {
        return get(ctx).getBoolean("show_labels", true);
    }
    public static void setShowLabels(Context ctx, boolean v) {
        get(ctx).edit().putBoolean("show_labels", v).apply();
    }

    public static float getCardAlpha(Context ctx) {
        return get(ctx).getFloat("card_alpha", 0.15f);
    }
    public static void setCardAlpha(Context ctx, float v) {
        get(ctx).edit().putFloat("card_alpha", v).apply();
    }

    public static String getAccentColor(Context ctx) {
        return get(ctx).getString("accent_color", "#7c6af7");
    }
    public static void setAccentColor(Context ctx, String v) {
        get(ctx).edit().putString("accent_color", v).apply();
    }

    public static boolean getUseWallpaper(Context ctx) {
        return get(ctx).getBoolean("use_wallpaper", true);
    }
    public static void setUseWallpaper(Context ctx, boolean v) {
        get(ctx).edit().putBoolean("use_wallpaper", v).apply();
    }

    public static boolean getFirstRun(Context ctx) {
        return get(ctx).getBoolean("first_run", true);
    }
    public static void setFirstRun(Context ctx, boolean v) {
        get(ctx).edit().putBoolean("first_run", v).apply();
    }
}
