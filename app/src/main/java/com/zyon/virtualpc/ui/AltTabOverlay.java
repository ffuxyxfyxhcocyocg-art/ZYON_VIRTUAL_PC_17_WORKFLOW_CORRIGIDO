package com.zyon.virtualpc.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/**
 * Overlay ALT+TAB — alterna entre janelas abertas.
 */
public class AltTabOverlay extends FrameLayout {

    public interface AltTabListener {
        void onWindowSelected(int index);
        void onDismiss();
    }

    private AltTabListener listener;
    private LinearLayout cardRow;

    public AltTabOverlay(Context ctx) {
        super(ctx);
        setBackgroundColor(0x99000000);
        setClickable(true);
        setOnClickListener(v -> { if (listener != null) listener.onDismiss(); });

        LinearLayout panel = new LinearLayout(ctx);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(ctx, 24), dp(ctx, 20), dp(ctx, 24), dp(ctx, 20));

        TextView title = tv(ctx, "ALT + TAB", 13, 0xFF90A4AE);
        title.setGravity(Gravity.CENTER);
        panel.addView(title);

        HorizontalScrollView hsv = new HorizontalScrollView(ctx);
        cardRow = new LinearLayout(ctx);
        cardRow.setOrientation(LinearLayout.HORIZONTAL);
        cardRow.setGravity(Gravity.CENTER_VERTICAL);
        cardRow.setPadding(dp(ctx, 8), dp(ctx, 12), dp(ctx, 8), dp(ctx, 4));
        hsv.addView(cardRow);
        panel.addView(hsv);

        addView(panel, new LayoutParams(-1, -2, Gravity.CENTER));
    }

    public void setListener(AltTabListener l) { listener = l; }

    public void setWindows(List<DesktopActivity.ZWindow> windows) {
        cardRow.removeAllViews();
        for (int i = 0; i < windows.size(); i++) {
            DesktopActivity.ZWindow w = windows.get(i);
            int idx = i;
            LinearLayout card = new LinearLayout(getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER);
            card.setPadding(dp(getContext(), 14), dp(getContext(), 10), dp(getContext(), 14), dp(getContext(), 10));
            card.setBackground(rr(0xCC1A2740, 10, getContext()));
            card.setClickable(true);
            card.setOnClickListener(v -> { if (listener != null) listener.onWindowSelected(idx); });

            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-2, -2);
            clp.setMargins(dp(getContext(), 6), 0, dp(getContext(), 6), 0);

            TextView ico = tv(getContext(), w.emoji, 28, 0xFFFFFFFF);
            ico.setGravity(Gravity.CENTER);
            card.addView(ico);

            TextView lbl = tv(getContext(), w.title, 11, 0xFFCBD5E1);
            lbl.setMaxLines(1);
            card.addView(lbl);

            cardRow.addView(card, clp);
        }
    }
}
