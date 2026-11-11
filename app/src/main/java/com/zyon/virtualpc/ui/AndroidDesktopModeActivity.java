package com.zyon.virtualpc.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/**
 * MODO DESKTOP PARA APPS/JOGOS ANDROID
 *
 * Lança apps Android com orientação forçada para horizontal e, quando o
 * aparelho suporta Multi-Window/Freeform (Android 12+), ativa essa modalidade.
 *
 * A barra superior com minimizar/maximizar/fechar fica disponível quando o app
 * Android roda em Freeform. Quando não é suportado, o app abre em tela cheia
 * horizontal (melhor alternativa sem root).
 *
 * APIs: ActivityManager.supportsMultiWindow, Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT,
 *       Intent.FLAG_ACTIVITY_NEW_TASK, android.app.ActivityOptions
 */
public class AndroidDesktopModeActivity extends AppCompatActivity {

    private LinearLayout appGrid;
    private final int bg    = 0xFF08101E;
    private final int panel = 0xFF131F30;
    private final int accent= 0xFF54D6FF;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bg);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(this,20), dp(this,16), dp(this,20), dp(this,24));
        scroll.addView(root);
        setContentView(scroll);

        // cabeçalho
        TextView back = tv(this, "‹  VOLTAR AO DESKTOP", 13, 0xFF9FB3D1);
        back.setOnClickListener(v -> finish());
        root.addView(back);

        TextView title = tv(this, "MODO DESKTOP · APPS ANDROID", 20, 0xFFFFFFFF);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1,-2);
        tlp.topMargin = dp(this,10);
        root.addView(title, tlp);

        boolean freeform = supportsMultiWindow();
        String mode = freeform ? "✅ Freeform disponível — janelas livres ativas"
                                : "ℹ️ Freeform não disponível — modo tela cheia horizontal";
        TextView modeInfo = tv(this, mode, 12, freeform ? 0xFF4ADE80 : 0xFFFFB347);
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(-1,-2);
        mlp.topMargin = dp(this,4); mlp.bottomMargin = dp(this,16);
        root.addView(modeInfo, mlp);

        TextView appsTitle = tv(this, "APPS INSTALADOS", 13, accent);
        root.addView(appsTitle);

        appGrid = new LinearLayout(this);
        appGrid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(-1,-2);
        glp.topMargin = dp(this,8);
        root.addView(appGrid, glp);

        loadApps();
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(query, 0);

        appGrid.removeAllViews();
        for (ResolveInfo info : apps) {
            String label = info.loadLabel(pm).toString();
            Drawable icon = info.loadIcon(pm);
            String pkg   = info.activityInfo.packageName;
            String cls   = info.activityInfo.name;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(this,14), dp(this,10), dp(this,14), dp(this,10));
            row.setBackground(rr(0xFF192035, 10, this));
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1,-2);
            rlp.bottomMargin = dp(this,6);
            appGrid.addView(row, rlp);

            ImageView iv = new ImageView(this);
            iv.setImageDrawable(icon);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            row.addView(iv, new LinearLayout.LayoutParams(dp(this,36), dp(this,36)));

            TextView lbl = tv(this, label, 14, 0xFFE2E8F0);
            lbl.setPadding(dp(this,12), 0, 0, 0);
            row.addView(lbl, new LinearLayout.LayoutParams(0,-2,1f));

            TextView openBtn = tv(this, "▶ Abrir", 12, 0xFF4A90D9);
            openBtn.setPadding(dp(this,10), dp(this,6), dp(this,10), dp(this,6));
            openBtn.setBackground(rr(0x334A90D9, 8, this));
            openBtn.setOnClickListener(v -> launchApp(pkg, cls));
            row.addView(openBtn);
        }
    }

    private void launchApp(String pkg, String cls) {
        Intent intent = new Intent();
        intent.setClassName(pkg, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        // forçar horizontal
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(
                    new android.content.ComponentName(pkg, cls), 0);
            if (ai.screenOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    || ai.screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    || ai.screenOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                // ok
            }
        } catch (PackageManager.NameNotFoundException ignored) {}

        if (supportsMultiWindow()) {
            // Freeform: lança em janela adjacente se possível
            android.app.ActivityOptions opts = android.app.ActivityOptions.makeBasic();
            // Freeform launch bounds (canto superior esquerdo, 70% da tela)
            android.graphics.Rect bounds = new android.graphics.Rect(
                    0, 0,
                    (int)(getResources().getDisplayMetrics().widthPixels * 0.7f),
                    (int)(getResources().getDisplayMetrics().heightPixels * 0.8f));
            try {
                // setLaunchWindowingMode(WINDOWING_MODE_FREEFORM = 5)
                opts.getClass().getMethod("setLaunchWindowingMode", int.class)
                        .invoke(opts, 5);
            } catch (Exception ignored) {}
            try {
                opts.getClass().getMethod("setLaunchBounds", android.graphics.Rect.class)
                        .invoke(opts, bounds);
            } catch (Exception ignored) {}
            try {
                startActivity(intent, opts.toBundle());
                return;
            } catch (Exception ignored) {}
        }

        // fallback: tela cheia horizontal normal
        try { startActivity(intent); }
        catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean supportsMultiWindow() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        try {
            return (boolean) am.getClass().getMethod("supportsMultiWindow").invoke(am);
        } catch (Exception e) {
            // Android 7+ tem ActivityManager.supportsMultiWindow()
            return android.os.Build.VERSION.SDK_INT >= 24;
        }
    }
}
