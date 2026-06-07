package com.dhenias.hlauncher;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class AppInfo {
    public final String name;
    public final String packageName;
    public final Drawable icon;
    public final String category;
    public final Intent launchIntent;

    public AppInfo(String name, String packageName, Drawable icon, String category, Intent launchIntent) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.category = category;
        this.launchIntent = launchIntent;
    }
}
