package com.zyon.virtualpc.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.Locale;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/**
 * Central de notificações do ambiente ZYON — histórico de eventos,
 * controle de processos, alternância de janelas.
 * Pode ser embutida como painel flutuante no desktop.
 */
public class NotificationCenterView extends LinearLayout {

    private static final int MAX_NOTIFS = 50;
    private static final Deque<String[]> history = new ArrayDeque<>(); // [icon, title, body, time]

    private LinearLayout list;

    public NotificationCenterView(Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xF2162547);
        bg.setCornerRadius(dp(ctx, 14));
        bg.setStroke(dp(ctx, 1), 0x33FFFFFF);
        setBackground(bg);
        setPadding(dp(ctx, 14), dp(ctx, 12), dp(ctx, 14), dp(ctx, 12));
        buildContent(ctx);
    }

    private void buildContent(Context ctx) {
        // Header
        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(ctx, 8));

        TextView title = tv(ctx, "🔔  Notificações", 14, 0xFFFFFFFF);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        header.addView(title, new LayoutParams(0, -2, 1f));

        TextView clearBtn = tv(ctx, "Limpar", 11, 0xFF90A4AE);
        clearBtn.setPadding(dp(ctx, 8), dp(ctx, 4), dp(ctx, 8), dp(ctx, 4));
        clearBtn.setBackground(rr(0x22FFFFFF, 10, ctx));
        clearBtn.setOnClickListener(v -> {
            history.clear();
            refresh(ctx);
        });
        header.addView(clearBtn);
        addView(header, new LayoutParams(-1, -2));

        // Divider
        View sep = new View(ctx);
        sep.setBackgroundColor(0x22FFFFFF);
        LayoutParams slp = new LayoutParams(-1, 1);
        slp.bottomMargin = dp(ctx, 8);
        addView(sep, slp);

        // List
        ScrollView sv = new ScrollView(ctx);
        list = new LinearLayout(ctx);
        list.setOrientation(VERTICAL);
        sv.addView(list);
        addView(sv, new LayoutParams(-1, dp(ctx, 240)));

        refresh(ctx);
    }

    private void refresh(Context ctx) {
        list.removeAllViews();
        if (history.isEmpty()) {
            TextView empty = tv(ctx, "Nenhuma notificação", 12, 0xFF90A4AE);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(ctx, 20), 0, dp(ctx, 20));
            list.addView(empty);
            return;
        }
        for (String[] n : history) {
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(VERTICAL);
            row.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8));
            row.setBackground(rr(0x22FFFFFF, 8, ctx));
            LayoutParams rlp = new LayoutParams(-1, -2);
            rlp.bottomMargin = dp(ctx, 4);
            row.setLayoutParams(rlp);

            LinearLayout top = new LinearLayout(ctx);
            top.setOrientation(HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);
            TextView ico = tv(ctx, n[0] + "  " + n[1], 12, 0xFFFFFFFF);
            ico.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            top.addView(ico, new LayoutParams(0, -2, 1f));
            TextView time = tv(ctx, n[3], 10, 0xFF90A4AE);
            top.addView(time);
            row.addView(top);

            if (n[2] != null && !n[2].isEmpty()) {
                TextView body = tv(ctx, n[2], 11, 0xFFCCCCCC);
                body.setPadding(0, dp(ctx, 2), 0, 0);
                row.addView(body);
            }
            list.addView(row, rlp);
        }
    }

    /** Adiciona uma notificação ao histórico estático (acessível de qualquer lugar). */
    public static void post(String icon, String title, String body) {
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        history.addFirst(new String[]{icon, title, body, time});
        while (history.size() > MAX_NOTIFS) history.removeLast();
    }
}
