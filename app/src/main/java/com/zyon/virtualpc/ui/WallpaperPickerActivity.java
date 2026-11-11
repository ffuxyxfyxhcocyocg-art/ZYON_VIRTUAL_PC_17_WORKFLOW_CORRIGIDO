package com.zyon.virtualpc.ui;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/** Escolha de papel de parede — 8 gradientes integrados, sem arquivos externos. */
public class WallpaperPickerActivity extends AppCompatActivity {

    private static final int[][] WALLPAPERS = {
        {0xFF020617, 0xFF0B2468},  // 0 Azul noite (padrão)
        {0xFF0D0D0D, 0xFF1A1A2E},  // 1 Preto gamer
        {0xFF0B2F1A, 0xFF144D2E},  // 2 Verde hacker
        {0xFF1A0B2E, 0xFF3D1A6B},  // 3 Roxo neon
        {0xFF1A0F00, 0xFF4D2800},  // 4 Laranja sunset
        {0xFF001A2E, 0xFF004080},  // 5 Azul oceano
        {0xFF1A001A, 0xFF4D004D},  // 6 Rosa magenta
        {0xFF0A0A0A, 0xFF2D2D2D},  // 7 Cinza escuro
    };
    private static final String[] WP_NAMES = {
        "Azul Noite (Padrão)", "Preto Gamer", "Verde Hacker", "Roxo Neon",
        "Laranja Sunset", "Azul Oceano", "Rosa Magenta", "Cinza Escuro"
    };

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0D1117);
        setContentView(root);

        TextView title = tv(this, "🖼️  Papel de Parede", 18, 0xFFFFFFFF);
        title.setPadding(dp(this, 20), dp(this, 20), dp(this, 20), dp(this, 16));
        root.addView(title);

        ScrollView sv = new ScrollView(this);
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        grid.setPadding(dp(this, 12), 0, dp(this, 12), dp(this, 12));

        SharedPreferences prefs = getSharedPreferences("zyon_wallpaper", MODE_PRIVATE);
        int current = prefs.getInt("index", 0);

        for (int i = 0; i < WALLPAPERS.length; i++) {
            final int idx = i;
            final int c0 = WALLPAPERS[i][0], c1 = WALLPAPERS[i][1];

            FrameLayout card = new FrameLayout(this);
            int cardW = (getResources().getDisplayMetrics().widthPixels - dp(this, 36)) / 2;
            int cardH = cardW * 9 / 16;

            View preview = new View(this) {
                final Paint p = new Paint();
                @Override protected void onDraw(Canvas c) {
                    p.setShader(new LinearGradient(0, 0, getWidth(), getHeight(), c0, c1, Shader.TileMode.CLAMP));
                    c.drawRect(0, 0, getWidth(), getHeight(), p);
                    // orb
                    p.setShader(new RadialGradient(getWidth()*0.6f, getHeight()*0.5f,
                        getWidth()*0.5f, 0x441E5BFF, 0x00000000, Shader.TileMode.CLAMP));
                    c.drawRect(0, 0, getWidth(), getHeight(), p);
                }
            };
            preview.setBackground(rr(0, 10, this));
            card.addView(preview, new FrameLayout.LayoutParams(cardW, cardH));

            if (i == current) {
                View sel = new View(this);
                sel.setBackground(rr(0, 10, this));
                android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
                gd.setColor(0x00000000);
                gd.setStroke(dp(this, 3), 0xFF4A90D9);
                gd.setCornerRadius(dp(this, 10));
                sel.setBackground(gd);
                card.addView(sel, new FrameLayout.LayoutParams(cardW, cardH));
            }

            TextView label = tv(this, WP_NAMES[i], 11, 0xFFCCCCCC);
            label.setGravity(Gravity.CENTER);
            label.setPadding(0, dp(this, 4), 0, 0);
            FrameLayout.LayoutParams llp = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            llp.bottomMargin = dp(this, 4);
            card.addView(label, llp);

            GridLayout.LayoutParams glp = new GridLayout.LayoutParams();
            glp.setMargins(dp(this, 4), dp(this, 4), dp(this, 4), dp(this, 4));
            card.setLayoutParams(glp);

            card.setOnClickListener(v -> {
                prefs.edit().putInt("index", idx).putInt("c0", c0).putInt("c1", c1).apply();
                Toast.makeText(this, "Papel de parede: " + WP_NAMES[idx], Toast.LENGTH_SHORT).show();
                finish();
            });
            grid.addView(card);
        }
        sv.addView(grid);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));
    }
}
