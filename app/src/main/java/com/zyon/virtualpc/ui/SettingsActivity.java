package com.zyon.virtualpc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0D1117);
        root.setPadding(dp(this, 20), dp(this, 20), dp(this, 20), dp(this, 20));
        setContentView(root);

        TextView title = tv(this, "⚙️  Configurações do ZYON", 18, 0xFFFFFFFF);
        title.setPadding(0, 0, 0, dp(this, 20));
        root.addView(title);

        // Tema escuro/claro
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);
        row1.setPadding(0, dp(this, 8), 0, dp(this, 8));
        TextView t1 = tv(this, "Modo escuro", 15, 0xFFCCCCCC);
        row1.addView(t1, new LinearLayout.LayoutParams(0, -2, 1f));
        Switch dark = new Switch(this);
        dark.setOnCheckedChangeListener((btn, on) -> {
            AppCompatDelegate.setDefaultNightMode(on
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, on ? "Modo escuro" : "Modo claro", Toast.LENGTH_SHORT).show();
        });
        row1.addView(dark);
        root.addView(row1);

        addDivider(root);

        // Resolução
        addButton(root, "🖥️  Resolução do monitor virtual", () ->
            startActivity(new Intent(this, ResolutionSettingsActivity.class)));

        addDivider(root);

        // VM
        addButton(root, "🪟  Máquinas Virtuais", () ->
            startActivity(new Intent(this, MainActivity.class)));

        addDivider(root);

        // Discos
        addButton(root, "💾  Gerenciador de Discos", () ->
            startActivity(new Intent(this, DiskManagerActivity.class)));

        addDivider(root);

        // Logs
        addButton(root, "📋  Logs do Sistema", () ->
            startActivity(new Intent(this, LogsActivity.class)));

        addDivider(root);

        // Snapshots
        addButton(root, "📸  Snapshots", () ->
            startActivity(new Intent(this, SnapshotsActivity.class)));

        addDivider(root);

        addButton(root, "📊  Desempenho", () ->
            startActivity(new Intent(this, PerformanceActivity.class)));
    }

    private void addDivider(LinearLayout root) {
        android.view.View line = new android.view.View(this);
        line.setBackgroundColor(0x22FFFFFF);
        root.addView(line, new LinearLayout.LayoutParams(-1, 1));
    }

    private void addButton(LinearLayout root, String label, Runnable action) {
        TextView btn = tv(this, label, 15, 0xFFCCCCCC);
        btn.setPadding(0, dp(this, 14), 0, dp(this, 14));
        btn.setOnClickListener(v -> action.run());
        root.addView(btn, new LinearLayout.LayoutParams(-1, -2));

        // Separador
        View sep = new View(this);
        sep.setBackgroundColor(0x22FFFFFF);
        LinearLayout.LayoutParams seplp = new LinearLayout.LayoutParams(-1, dp(this,1));
        seplp.topMargin = dp(this,16); seplp.bottomMargin = dp(this,8);
        root.addView(sep, seplp);

        // Mouse
        TextView mouseRow = tv(this, "🖱️  Configurações de mouse (USB / OTG / Bluetooth)", 15, 0xFFCCCCCC);
        mouseRow.setPadding(0, dp(this,10), 0, dp(this,10));
        mouseRow.setClickable(true);
        mouseRow.setOnClickListener(v -> startActivity(new Intent(this, MouseSettingsActivity.class)));
        root.addView(mouseRow);

        // Teclado
        TextView kbRow = tv(this, "⌨️  Mapeamento de teclado por jogo", 15, 0xFFCCCCCC);
        kbRow.setPadding(0, dp(this,10), 0, dp(this,10));
        kbRow.setClickable(true);
        kbRow.setOnClickListener(v -> startActivity(new Intent(this, KeyboardMappingActivity.class)));
        root.addView(kbRow);

        // Resolução
        TextView resRow = tv(this, "🖥️  Resolução do monitor virtual", 15, 0xFFCCCCCC);
        resRow.setPadding(0, dp(this,10), 0, dp(this,10));
        resRow.setClickable(true);
        resRow.setOnClickListener(v -> startActivity(new Intent(this, ResolutionSettingsActivity.class)));
        root.addView(resRow);

        // Modo Desktop Android
        TextView desktopRow = tv(this, "📱  Modo desktop para apps Android", 15, 0xFFCCCCCC);
        desktopRow.setPadding(0, dp(this,10), 0, dp(this,10));
        desktopRow.setClickable(true);
        desktopRow.setOnClickListener(v -> startActivity(new Intent(this, AndroidDesktopModeActivity.class)));
        root.addView(desktopRow);

    }
}
