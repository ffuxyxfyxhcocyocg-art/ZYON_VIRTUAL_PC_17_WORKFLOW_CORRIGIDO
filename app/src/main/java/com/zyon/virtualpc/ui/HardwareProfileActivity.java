package com.zyon.virtualpc.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/**
 * Dimensionamento de hardware — detecta RAM disponível real,
 * oferece três perfis e mostra limites seguros para a VM.
 */
public class HardwareProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0D1117);
        root.setPadding(dp(this, 20), dp(this, 20), dp(this, 20), dp(this, 20));
        setContentView(root);

        // Detectar RAM
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        if (am != null) am.getMemoryInfo(mi);
        long totalMB = mi.totalMem / (1024 * 1024);
        long availMB = mi.availMem / (1024 * 1024);

        // Limites por perfil
        long ecoMB   = Math.max(512, Math.min(1024, totalMB / 6));
        long balMB   = Math.max(1024, Math.min(3072, totalMB / 3));
        long perfMB  = Math.max(2048, Math.min(6144, totalMB / 2));

        TextView title = tv(this, "⚡  Dimensionamento de Hardware", 18, 0xFFFFFFFF);
        root.addView(title);

        addInfo(root, "RAM total detectada", totalMB + " MB");
        addInfo(root, "RAM disponível agora", availMB + " MB");
        addInfo(root, "Dispositivo", android.os.Build.MODEL);

        addDivider(root);

        TextView profLabel = tv(this, "Perfil de desempenho:", 15, 0xFFCCCCCC);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(-1, -2);
        plp.topMargin = dp(this, 12);
        plp.bottomMargin = dp(this, 8);
        root.addView(profLabel, plp);

        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.VERTICAL);

        String[] labels = {
            "🔋 Econômico — " + ecoMB + " MB RAM para VM (bateria, temperatura baixa)",
            "⚖️ Equilibrado — " + balMB + " MB RAM para VM (uso geral)",
            "🚀 Desempenho — " + perfMB + " MB RAM para VM (máximo, pode aquecer)"
        };
        String[] ids = {"eco", "balanced", "perf"};
        RadioButton[] btns = new RadioButton[3];
        SharedPreferences prefs = getSharedPreferences("zyon_hw", MODE_PRIVATE);
        String currentProfile = prefs.getString("profile", "balanced");

        for (int i = 0; i < 3; i++) {
            btns[i] = new RadioButton(this);
            btns[i].setText(labels[i]);
            btns[i].setTextColor(0xFFCCCCCC);
            btns[i].setTag(ids[i]);
            if (ids[i].equals(currentProfile)) btns[i].setChecked(true);
            rg.addView(btns[i]);
        }
        root.addView(rg);

        addDivider(root);

        // Núcleos
        int cores = Runtime.getRuntime().availableProcessors();
        addInfo(root, "CPUs disponíveis", cores + " núcleos");
        addInfo(root, "Núcleos seguros para VM", Math.max(1, cores - 2) + " (reserva 2 para Android)");

        addDivider(root);

        // Salvar
        TextView save = tv(this, "Salvar Perfil", 15, 0xFFFFFFFF);
        save.setGravity(Gravity.CENTER);
        save.setPadding(dp(this, 24), dp(this, 14), dp(this, 24), dp(this, 14));
        save.setBackground(rr(0xFF1565C0, 12, this));
        LinearLayout.LayoutParams slp2 = new LinearLayout.LayoutParams(-2, -2);
        slp2.topMargin = dp(this, 16);
        root.addView(save, slp2);

        long[] ramByProfile = {ecoMB, balMB, perfMB};
        save.setOnClickListener(v -> {
            int sel = -1;
            for (int i = 0; i < btns.length; i++) if (btns[i].isChecked()) { sel = i; break; }
            if (sel < 0) sel = 1;
            prefs.edit()
                .putString("profile", ids[sel])
                .putLong("vm_ram_mb", ramByProfile[sel])
                .putInt("vm_cores", Math.max(1, cores - 2))
                .apply();
            Toast.makeText(this, "Perfil salvo: " + ids[sel] + " — " + ramByProfile[sel] + " MB", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void addInfo(LinearLayout root, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(this, 5), 0, dp(this, 5));
        TextView l = tv(this, label, 13, 0xFF90A4AE);
        TextView v = tv(this, value, 13, 0xFFFFFFFF);
        row.addView(l, new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(v);
        root.addView(row);
    }

    private void addDivider(LinearLayout root) {
        android.view.View line = new android.view.View(this);
        line.setBackgroundColor(0x22FFFFFF);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 1);
        lp.topMargin = dp(this, 10);
        lp.bottomMargin = dp(this, 10);
        root.addView(line, lp);
    }
}
