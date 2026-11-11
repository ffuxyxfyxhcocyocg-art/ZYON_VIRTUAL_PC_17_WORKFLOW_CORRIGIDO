package com.zyon.virtualpc.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/**
 * Tela inicial do ZYON VIRTUAL PC 17.
 * Tema e desempenho são grupos independentes.
 * Botão ABRIR PC VIRTUAL sempre visível fora da rolagem.
 */
public class WelcomeActivity extends AppCompatActivity {

    private int colorText = 0xFFE2E8F0;
    private int colorMuted = 0xFF94A3B8;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF020617);
        root.setPadding(dp(this, 20), dp(this, 12), dp(this, 20), dp(this, 10));
        setContentView(root);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(this, 4), dp(this, 2), dp(this, 4), dp(this, 10));
        scroll.addView(content);

        // ícone de PC de verdade
        TextView logo = tv(this, "🖥", 46, 0xFFFFFFFF);
        logo.setGravity(Gravity.CENTER);
        content.addView(logo);

        TextView title = tv(this, "ZYON VIRTUAL PC 17", 24, 0xFF4A90D9);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = dp(this, 3);
        content.addView(title, titleLp);

        TextView sub = tv(this, "Master Ultimate · Windows 11 · PC Gamer", 12, colorMuted);
        sub.setGravity(Gravity.CENTER);
        content.addView(sub);

        TextView sub2 = tv(this, "Mouse · Teclado · QEMU · ARM64", 11, 0xFF64748B);
        sub2.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sub2Lp = new LinearLayout.LayoutParams(-1, -2);
        sub2Lp.topMargin = dp(this, 2);
        content.addView(sub2, sub2Lp);

        View line = new View(this);
        line.setBackgroundColor(0x22FFFFFF);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(-1, dp(this, 1));
        lineLp.topMargin = dp(this, 14);
        lineLp.bottomMargin = dp(this, 12);
        content.addView(line, lineLp);

        // Tema
        TextView themeLabel = tv(this, "1. Escolha o tema inicial", 14, colorText);
        content.addView(themeLabel);

        RadioGroup themeGroup = new RadioGroup(this);
        themeGroup.setOrientation(RadioGroup.HORIZONTAL);
        themeGroup.setGravity(Gravity.CENTER_VERTICAL);
        themeGroup.setPadding(0, dp(this, 2), 0, dp(this, 8));

        RadioButton dark = radio("🌙  Escuro");
        RadioButton light = radio("☀️  Claro");
        themeGroup.addView(dark);
        themeGroup.addView(light);
        dark.setChecked(true);
        content.addView(themeGroup);

        // Desempenho
        TextView perfLabel = tv(this, "2. Perfil de desempenho inicial", 14, colorText);
        LinearLayout.LayoutParams perfLabelLp = new LinearLayout.LayoutParams(-1, -2);
        perfLabelLp.topMargin = dp(this, 4);
        content.addView(perfLabel, perfLabelLp);

        RadioGroup perfGroup = new RadioGroup(this);
        perfGroup.setOrientation(RadioGroup.HORIZONTAL);
        perfGroup.setGravity(Gravity.CENTER_VERTICAL);
        perfGroup.setPadding(0, dp(this, 2), 0, dp(this, 4));

        String[] profiles = {"🔋 Econômico", "⚖️ Equilibrado", "⚡ Desempenho"};
        RadioButton[] profileButtons = new RadioButton[profiles.length];
        for (int i = 0; i < profiles.length; i++) {
            profileButtons[i] = radio(profiles[i]);
            perfGroup.addView(profileButtons[i]);
        }
        profileButtons[1].setChecked(true);
        content.addView(perfGroup);

        TextView hint = tv(this, "Você pode alterar essas opções depois nas configurações.", 11, colorMuted);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(-1, -2);
        hintLp.topMargin = dp(this, 4);
        content.addView(hint, hintLp);

        // Botão sempre visível fora da área de rolagem
        TextView btn = tv(this, "🖥️  ABRIR PC VIRTUAL", 16, 0xFFFFFFFF);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(this, 20), dp(this, 14), dp(this, 20), dp(this, 14));
        btn.setBackground(rr(0xFF1565C0, 14, this));
        btn.setClickable(true);
        btn.setFocusable(true);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, -2);
        btnLp.topMargin = dp(this, 8);
        root.addView(btn, btnLp);

        btn.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("zyon_welcome", MODE_PRIVATE);
            prefs.edit()
                    .putBoolean("done", true)
                    .putBoolean("dark", dark.isChecked())
                    .putString("perf_profile", profileButtons[0].isChecked() ? "eco"
                            : profileButtons[2].isChecked() ? "perf" : "balanced")
                    .apply();
            startActivity(new Intent(this, BootLoadingActivity.class));
            finish();
        });
    }

    private RadioButton radio(String label) {
        RadioButton button = new RadioButton(this);
        button.setText(label);
        button.setTextColor(colorText);
        button.setTextSize(13);
        button.setMinHeight(dp(this, 42));
        button.setPadding(dp(this, 2), 0, dp(this, 8), 0);
        return button;
    }
}
