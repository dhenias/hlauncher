package com.dhenias.hlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private LauncherView launcherView;
    private static final int REQ_SETTINGS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        buildLayout();

        // first run — ask to set as default launcher
        if (Prefs.getFirstRun(this)) {
            Prefs.setFirstRun(this, false);
            showDefaultLauncherDialog();
        }
    }

    private void buildLayout() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF0a0a0f);

        // wallpaper layer
        if (Prefs.getUseWallpaper(this)) {
            try {
                WallpaperManager wm = WallpaperManager.getInstance(this);
                Drawable wallpaper = wm.getDrawable();
                if (wallpaper != null) {
                    ImageView wpView = new ImageView(this);
                    wpView.setImageDrawable(wallpaper);
                    wpView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    wpView.setAlpha(0.35f); // subtle blend
                    root.addView(wpView, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
                }
            } catch (Exception e) {
                // wallpaper permission not granted, skip
            }
        }

        // launcher content
        List<AppInfo> apps = getInstalledApps();
        launcherView = new LauncherView(this, apps);
        root.addView(launcherView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);
    }

    private void showDefaultLauncherDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Set as Home Launcher?")
            .setMessage("Would you like to set hLauncher as your default home screen?")
            .setPositiveButton("Yes", (d, w) -> promptSetDefault())
            .setNegativeButton("Later", null)
            .show();
    }

    private void promptSetDefault() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivity(intent);
    }

    public void openSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivityForResult(i, REQ_SETTINGS);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_SETTINGS && res == RESULT_OK) {
            // rebuild UI with new settings
            buildLayout();
        }
    }

    private List<AppInfo> getInstalledApps() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ris = pm.queryIntentActivities(intent, 0);
        Collections.sort(ris, new ResolveInfo.DisplayNameComparator(pm));
        List<AppInfo> apps = new ArrayList<>();
        for (ResolveInfo ri : ris) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            String name = ri.loadLabel(pm).toString();
            Drawable icon = ri.loadIcon(pm);
            String category = CategoryDetector.detect(name, pkg);
            Intent launch = pm.getLaunchIntentForPackage(pkg);
            apps.add(new AppInfo(name, pkg, icon, category, launch));
        }
        return apps;
    }

    @Override
    public void onBackPressed() { /* stay on launcher */ }
}
