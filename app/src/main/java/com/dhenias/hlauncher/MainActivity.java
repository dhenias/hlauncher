package com.dhenias.hlauncher;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

    private static final int REQ_SETTINGS    = 101;
    private static final int REQ_PERMISSION  = 102;
    private FrameLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        rootLayout = new FrameLayout(this);
        setContentView(rootLayout);

        // request storage permission for wallpaper on Android < 13
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQ_PERMISSION);
                return; // buildLayout called in onRequestPermissionsResult
            }
        }

        buildLayout();

        if (Prefs.getFirstRun(this)) {
            Prefs.setFirstRun(this, false);
            showDefaultLauncherDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        // build regardless of result — wallpaper just won't show if denied
        buildLayout();
        if (Prefs.getFirstRun(this)) {
            Prefs.setFirstRun(this, false);
            showDefaultLauncherDialog();
        }
    }

    public void buildLayout() {
        rootLayout.removeAllViews();
        rootLayout.setBackgroundColor(0xFF0a0a0f);

        // wallpaper layer
        if (Prefs.getUseWallpaper(this)) {
            try {
                WallpaperManager wm = WallpaperManager.getInstance(this);
                Drawable wp = wm.getDrawable();
                if (wp != null) {
                    ImageView wpView = new ImageView(this);
                    wpView.setImageDrawable(wp);
                    wpView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    wpView.setAlpha(0.4f);
                    rootLayout.addView(wpView, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
                }
            } catch (Exception ignored) {}
        }

        // dark overlay so text stays readable
        View overlay = new View(this);
        overlay.setBackgroundColor(0xCC0a0a0f);
        rootLayout.addView(overlay, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        // launcher content
        LauncherView lv = new LauncherView(this, getInstalledApps());
        rootLayout.addView(lv, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void showDefaultLauncherDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Set as Home Launcher?")
            .setMessage("Set hLauncher as your default home screen?")
            .setPositiveButton("Yes", (d, w) -> openHomePicker())
            .setNegativeButton("Later", null)
            .show();
    }

    public void openHomePicker() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivity(i);
    }

    public void openSettings() {
        startActivityForResult(new Intent(this, SettingsActivity.class), REQ_SETTINGS);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_SETTINGS) buildLayout();
    }

    private List<AppInfo> getInstalledApps() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ris = pm.queryIntentActivities(intent, 0);
        Collections.sort(ris, new ResolveInfo.DisplayNameComparator(pm));
        List<AppInfo> list = new ArrayList<>();
        for (ResolveInfo ri : ris) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            String name = ri.loadLabel(pm).toString();
            Drawable icon = ri.loadIcon(pm);
            String cat = CategoryDetector.detect(name, pkg);
            Intent launch = pm.getLaunchIntentForPackage(pkg);
            list.add(new AppInfo(name, pkg, icon, cat, launch));
        }
        return list;
    }

    @Override public void onBackPressed() { /* stay */ }
}
