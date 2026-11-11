package com.zyon.virtualpc.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
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
 * Configurações de resolução do monitor virtual.
 * Salva em SharedPreferences; a VM lê na próxima inicialização.
 */
public class ResolutionSettingsActivity extends AppCompatActivity {

    private static final String[][] RESOLUTIONS = {
        {"800×600",   "800",  "600"},
        {"1024×768",  "1024", "768"},
        {"1280×720",  "1280", "720"},
        {"1366×768",  "1366", "768"},
        {"1600×900",  "1600", "900"},
        {"1920×1080", "1920", "1080"},
    };

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0D1117);
        root.setPadding(dp(this,24), dp(this,24), dp(this,24), dp(this,24));
        setContentView(root);

        TextView title = tv(this, "Resolução do Monitor Virtual", 18, 0xFFFFFFFF);
        title.setPadding(0, 0, 0, dp(this, 16));
        root.addView(title);

        SharedPreferences prefs = getSharedPreferences("zyon_res", MODE_PRIVATE);
        String current = prefs.getString("res", "1280×720");

        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.VERTICAL);

        for (int i = 0; i < RESOLUTIONS.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(RESOLUTIONS[i][0]);
            rb.setTextColor(0xFFCCCCCC);
            rb.setTextSize(15);
            rb.setTag(i);
            if (RESOLUTIONS[i][0].equals(current)) rb.setChecked(true);
            rg.addView(rb);
        }
        root.addView(rg);

        TextView save = tv(this, "Salvar", 15, 0xFF000000);
        save.setGravity(Gravity.CENTER);
        save.setPadding(dp(this,24), dp(this,12), dp(this,24), dp(this,12));
        save.setBackground(rr(0xFF4A90D9, 10, this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.topMargin = dp(this, 24);
        root.addView(save, lp);

        save.setOnClickListener(v -> {
            int id = rg.getCheckedRadioButtonId();
            RadioButton sel = rg.findViewById(id);
            if (sel == null) { finish(); return; }
            int idx = (int) sel.getTag();
            prefs.edit()
                .putString("res", RESOLUTIONS[idx][0])
                .putInt("res_w", Integer.parseInt(RESOLUTIONS[idx][1]))
                .putInt("res_h", Integer.parseInt(RESOLUTIONS[idx][2]))
                .apply();
            Toast.makeText(this, "Resolução salva: " + RESOLUTIONS[idx][0], Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
