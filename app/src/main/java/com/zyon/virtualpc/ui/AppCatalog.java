package com.zyon.virtualpc.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Catálogo dos apps do celular (carregado na tela de boot e usado pelo desktop). */
public final class AppCatalog {
    private AppCatalog() {}

    public static final class Entry {
        public String label;
        public String pkg;
        public Drawable icon;
        public Intent launch;
    }

    private static final List<Entry> CACHE = new ArrayList<>();
    private static boolean loaded = false;

    public static synchronized boolean isLoaded() { return loaded; }

    public static synchronized List<Entry> get(Context c) {
        if (!loaded) load(c);
        return CACHE;
    }

    public static synchronized void load(Context c) {
        PackageManager pm = c.getPackageManager();
        List<Entry> out = new ArrayList<>();
        for (ApplicationInfo ai : pm.getInstalledApplications(0)) {
            if (ai.packageName.equals(c.getPackageName())) continue;
            Intent launch = pm.getLaunchIntentForPackage(ai.packageName);
            if (launch == null) continue;
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Entry e = new Entry();
            e.pkg = ai.packageName;
            try {
                e.label = String.valueOf(pm.getApplicationLabel(ai));
                e.icon = pm.getApplicationIcon(ai);
            } catch (Exception ex) {
                continue;
            }
            e.launch = launch;
            out.add(e);
        }
        Collections.sort(out, (a, b) -> a.label.compareToIgnoreCase(b.label));
        CACHE.clear();
        CACHE.addAll(out);
        loaded = true;
    }

    /** true se existem binários QEMU empacotados (libqemu*.so) no APK. */
    public static boolean qemuPresent(Context c) {
        File dir = new File(c.getApplicationInfo().nativeLibraryDir);
        String[] names = dir.list();
        if (names == null) return false;
        for (String n : names) if (n.startsWith("libqemu")) return true;
        return false;
    }
}
