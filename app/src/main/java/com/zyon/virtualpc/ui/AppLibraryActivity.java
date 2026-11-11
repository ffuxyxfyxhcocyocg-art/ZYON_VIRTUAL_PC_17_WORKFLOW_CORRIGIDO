package com.zyon.virtualpc.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/**
 * Biblioteca unificada de aplicativos — separa em três categorias:
 * Android (apps do celular), Windows (executam na VM), Linux (VM Linux).
 */
public class AppLibraryActivity extends AppCompatActivity {

    private LinearLayout contentArea;
    private int activeTab = 0;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0D1117);
        setContentView(root);

        // Título
        TextView title = tv(this, "📚  Biblioteca de Aplicativos", 17, 0xFFFFFFFF);
        title.setPadding(dp(this, 20), dp(this, 20), dp(this, 20), dp(this, 8));
        root.addView(title);

        // Tabs
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setBackgroundColor(0xFF111827);
        tabs.setPadding(dp(this, 8), dp(this, 6), dp(this, 8), dp(this, 6));

        String[] tabLabels = {"📱 Android", "🪟 Windows", "🐧 Linux"};
        TextView[] tabViews = new TextView[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            tabViews[i] = tv(this, tabLabels[i], 13, 0xFFCCCCCC);
            tabViews[i].setPadding(dp(this, 16), dp(this, 8), dp(this, 16), dp(this, 8));
            tabViews[i].setBackground(rr(i == 0 ? 0xFF1565C0 : 0x22FFFFFF, 20, this));
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-2, -2);
            tlp.rightMargin = dp(this, 6);
            tabViews[i].setOnClickListener(v -> {
                for (TextView t : tabViews) t.setBackground(rr(0x22FFFFFF, 20, this));
                tabViews[idx].setBackground(rr(0xFF1565C0, 20, this));
                activeTab = idx;
                loadTab(idx);
            });
            tabs.addView(tabViews[i], tlp);
        }
        root.addView(tabs);

        // Content
        ScrollView sv = new ScrollView(this);
        contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setPadding(dp(this, 16), dp(this, 12), dp(this, 16), dp(this, 16));
        sv.addView(contentArea);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));

        loadTab(0);
    }

    private void loadTab(int tab) {
        contentArea.removeAllViews();
        switch (tab) {
            case 0: loadAndroid(); break;
            case 1: loadWindows(); break;
            case 2: loadLinux(); break;
        }
    }

    private void loadAndroid() {
        List<AppCatalog.Entry> apps = AppCatalog.get(this);
        if (apps.isEmpty()) {
            addInfo("Carregando apps instalados…");
            new Thread(() -> {
                AppCatalog.get(getApplicationContext());
                runOnUiThread(() -> { if (!isFinishing()) loadAndroid(); });
            }).start();
            return;
        }
        addSectionHeader("Apps instalados no Android (" + apps.size() + ")");
        for (AppCatalog.Entry e : apps) {
            addAppRow(e.icon, e.label, "Android", () -> {
                try { startActivity(e.launch); } catch (Exception ex) {
                    Toast.makeText(this, "Não foi possível abrir " + e.label, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadWindows() {
        addSectionHeader("Programas Windows");
        addInfo("Programas Windows são executados dentro da máquina virtual Windows.");
        addInfo("Inicie a VM primeiro em Máquinas Virtuais.");
        addAppRow(null, "Iniciar Máquina Virtual Windows", "VM", () ->
            startActivity(new Intent(this, MainActivity.class)));
        addAppRow(null, "Console da VM", "VM", () ->
            startActivity(new Intent(this, VmConsoleActivity.class)));
        addAppRow(null, "Gerenciador de Discos", "VM", () ->
            startActivity(new Intent(this, DiskManagerActivity.class)));
    }

    private void loadLinux() {
        addSectionHeader("Ambientes Linux");
        addInfo("Linux requer uma VM com ISO de distribuição Linux importada.");
        addInfo("Use o Gerenciador de Discos para importar uma ISO Linux.");
        addAppRow(null, "Criar VM Linux", "VM", () ->
            startActivity(new Intent(this, CreateVmActivity.class)));
        addAppRow(null, "Importar ISO", "Disco", () ->
            startActivity(new Intent(this, DiskManagerActivity.class)));
    }

    private void addSectionHeader(String text) {
        TextView h = tv(this, text, 14, 0xFF4FC3F7);
        h.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(this, 4);
        lp.bottomMargin = dp(this, 8);
        contentArea.addView(h, lp);
    }

    private void addInfo(String text) {
        TextView t = tv(this, text, 12, 0xFF90A4AE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(this, 6);
        contentArea.addView(t, lp);
    }

    private void addAppRow(android.graphics.drawable.Drawable icon, String label, String type, Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(this, 12), dp(this, 10), dp(this, 12), dp(this, 10));
        row.setBackground(rr(0x22FFFFFF, 8, this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(this, 4);
        row.setLayoutParams(lp);

        if (icon != null) {
            android.widget.ImageView iv = new android.widget.ImageView(this);
            iv.setImageDrawable(icon);
            row.addView(iv, new LinearLayout.LayoutParams(dp(this, 32), dp(this, 32)));
            android.view.View spacer = new android.view.View(this);
            row.addView(spacer, new LinearLayout.LayoutParams(dp(this, 10), 1));
        } else {
            TextView ico = tv(this, "▶", 16, 0xFF4A90D9);
            row.addView(ico, new LinearLayout.LayoutParams(dp(this, 32), -2));
        }

        TextView lbl = tv(this, label, 13, 0xFFFFFFFF);
        row.addView(lbl, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView tag = tv(this, type, 10, 0xFF90A4AE);
        tag.setPadding(dp(this, 6), dp(this, 2), dp(this, 6), dp(this, 2));
        tag.setBackground(rr(0x33FFFFFF, 10, this));
        row.addView(tag);

        row.setOnClickListener(v -> action.run());
        contentArea.addView(row, lp);
    }
}
