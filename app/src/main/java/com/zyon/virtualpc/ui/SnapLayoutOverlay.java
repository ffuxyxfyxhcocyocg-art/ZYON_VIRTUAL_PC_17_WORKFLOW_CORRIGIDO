package com.zyon.virtualpc.ui;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/**
 * Snap Layout — aparece ao segurar o botão maximizar.
 * 6 zonas + maximizado completo.
 */
public class SnapLayoutOverlay extends FrameLayout {

    public interface SnapListener { void onZone(int zone); }

    private SnapListener listener;

    public SnapLayoutOverlay(Context ctx) {
        super(ctx);
        setBackgroundColor(0x88000000);
        setClickable(true);
        setOnClickListener(v -> setVisibility(GONE));

        LinearLayout panel = new LinearLayout(ctx);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(ctx, 20), dp(ctx, 16), dp(ctx, 20), dp(ctx, 16));
        panel.setBackground(rr(0xE6162032, 14, ctx));

        TextView title = tv(ctx, "⬛ Snap Layout", 14, 0xFF90A4AE);
        title.setGravity(Gravity.CENTER);
        panel.addView(title);

        // 6 zonas em grid
        GridLayout grid = new GridLayout(ctx);
        grid.setColumnCount(3);
        grid.setRowCount(2);
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(-2, -2);
        glp.topMargin = dp(ctx, 10);

        String[] labels = {"◧ Esq 50%", "◨ Dir 50%", "◱ TL", "◲ TR", "◳ BL", "◴ BR"};
        for (int i = 0; i < 6; i++) {
            int zone = i;
            TextView btn = tv(ctx, labels[i], 12, 0xFFFFFFFF);
            btn.setGravity(Gravity.CENTER);
            btn.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8));
            btn.setBackground(rr(0xFF1E3050, 8, ctx));
            btn.setClickable(true);
            btn.setOnClickListener(v -> { if (listener != null) listener.onZone(zone); setVisibility(GONE); });
            GridLayout.LayoutParams glp2 = new GridLayout.LayoutParams();
            glp2.setMargins(dp(ctx, 4), dp(ctx, 4), dp(ctx, 4), dp(ctx, 4));
            glp2.width = dp(ctx, 90);
            glp2.height = dp(ctx, 50);
            grid.addView(btn, glp2);
        }
        panel.addView(grid, glp);

        // Maximizar completo
        TextView full = tv(ctx, "⬜ Maximizado completo", 13, 0xFFFFFFFF);
        full.setGravity(Gravity.CENTER);
        full.setPadding(dp(ctx, 16), dp(ctx, 10), dp(ctx, 16), dp(ctx, 10));
        full.setBackground(rr(0xFF1565C0, 8, ctx));
        full.setClickable(true);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(-1, -2);
        flp.topMargin = dp(ctx, 8);
        full.setOnClickListener(v -> { if (listener != null) listener.onZone(6); setVisibility(GONE); });
        panel.addView(full, flp);

        addView(panel, new LayoutParams(-2, -2, Gravity.CENTER));
    }

    public void setListener(SnapListener l) { listener = l; }
}
