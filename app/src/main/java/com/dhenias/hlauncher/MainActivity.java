package com.dhenias.hlauncher;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ParcelFileDescriptor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private static final int REQ_SETTINGS   = 101;
    private static final int REQ_PERMISSION = 102;
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

        // request storage permission for wallpaper (Android <= 12)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQ_PERMISSION);
                return;
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
        buildLayout();
        if (Prefs.getFirstRun(this)) {
            Prefs.setFirstRun(this, false);
            showDefaultLauncherDialog();
        }
    }

    public void buildLayout() {
        rootLayout.removeAllViews();
        rootLayout.setBackgroundColor(0xFF0a0a0f);

        // ── wallpaper ──
        if (Prefs.getUseWallpaper(this)) {
            Drawable wpDrawable = loadWallpaper();
            if (wpDrawable != null) {
                ImageView wpView = new ImageView(this);
                wpView.setImageDrawable(wpDrawable);
                wpView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                rootLayout.addView(wpView, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            }
        }

        // dark scrim so content is readable
        View scrim = new View(this);
        scrim.setBackgroundColor(0xBB0a0a0f);
        rootLayout.addView(scrim, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        // launcher
        LauncherView lv = new LauncherView(this, getInstalledApps());
        rootLayout.addView(lv, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private Drawable loadWallpaper() {
        try {
            WallpaperManager wm = WallpaperManager.getInstance(this);

            // Android 8.1+ — getWallpaperFile gives the raw file
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                ParcelFileDescriptor pfd = wm.getWallpaperFile(WallpaperManager.FLAG_SYSTEM);
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    Bitmap bmp = BitmapFactory.decodeFileDescriptor(fd);
                    pfd.close();
                    if (bmp != null) return new BitmapDrawable(getResources(), bmp);
                }
            }

            // fallback — getDrawable (works on older Android)
            Drawable d = wm.getDrawable();
            if (d != null) return d;

        } catch (Exception ignored) {}
        return null;
    }

    private void showDefaultLauncherDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Set as Default Launcher")
            .setMessage("To use hLauncher as your home screen, set it as the default launcher in your device settings.")
            .setPositiveButton("Open Settings", (d, w) -> openDefaultAppsSettings())
            .setNegativeButton("Later", null)
            .show();
    }

    public void openDefaultAppsSettings() {
        try {
            // direct to default apps / home app settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Intent i = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                startActivity(i);
            } else {
                // older Android — show home chooser so user can pick + set always
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_HOME);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(Intent.createChooser(i, "Select Home App"));
            }
        } catch (Exception e) {
            // last resort fallback
            try {
                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } catch (Exception ignored) {}
        }
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

    @Override public void onBackPressed() {}
}
