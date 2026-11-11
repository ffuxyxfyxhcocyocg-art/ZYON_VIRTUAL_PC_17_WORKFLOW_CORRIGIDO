package com.zyon.virtualpc.ui;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/**
 * Central de ações rápidas — abre a partir do canto direito da barra de tarefas.
 * Mostra conexão, controle de volume, brilho, modo avião, Bluetooth.
 */
public class QuickActionsView extends LinearLayout {

    private final AudioManager audio;
    private final Context ctx;

    public QuickActionsView(Context context) {
        super(context);
        this.ctx = context;
        this.audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        setOrientation(VERTICAL);
        setBackground(rr(0xF2162547, 14, context));
        setPadding(dp(ctx, 16), dp(ctx, 12), dp(ctx, 16), dp(ctx, 16));

        android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
        border.setColor(0xF2162547);
        border.setCornerRadius(dp(ctx, 14));
        border.setStroke(dp(ctx, 1), 0x33FFFFFF);
        setBackground(border);

        buildContent();
    }

    private void buildContent() {
        // Título
        TextView title = tv(ctx, "Central de Ações", 13, 0xFFFFFFFF);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setPadding(0, 0, 0, dp(ctx, 10));
        addView(title);

        // Rede
        addToggleRow("🌐", "Rede / Wi-Fi", isConnected(), () -> {
            try { ctx.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
            catch (Exception e) { Toast.makeText(ctx, "Configurações de rede indisponíveis", Toast.LENGTH_SHORT).show(); }
        });

        // Bluetooth
        addToggleRow("🔵", "Bluetooth", false, () -> {
            try { ctx.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
            catch (Exception e) { Toast.makeText(ctx, "Configurações Bluetooth indisponíveis", Toast.LENGTH_SHORT).show(); }
        });

        // Modo avião
        addToggleRow("✈️", "Modo Avião", false, () -> {
            try { ctx.startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
            catch (Exception e) {}
        });

        // Separador
        View sep = new View(ctx);
        sep.setBackgroundColor(0x22FFFFFF);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, 1);
        slp.topMargin = dp(ctx, 8);
        slp.bottomMargin = dp(ctx, 8);
        addView(sep, slp);

        // Volume
        TextView volLabel = tv(ctx, "🔊  Volume", 12, 0xFFCCCCCC);
        addView(volLabel);
        SeekBar vol = new SeekBar(ctx);
        int maxVol = audio != null ? audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) : 15;
        int curVol = audio != null ? audio.getStreamVolume(AudioManager.STREAM_MUSIC) : 8;
        vol.setMax(maxVol);
        vol.setProgress(curVol);
        vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean u) {
                if (audio != null) audio.setStreamVolume(AudioManager.STREAM_MUSIC, p, 0);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(-1, -2);
        vlp.topMargin = dp(ctx, 4);
        vlp.bottomMargin = dp(ctx, 10);
        addView(vol, vlp);

        // Brilho
        TextView briLabel = tv(ctx, "☀️  Brilho", 12, 0xFFCCCCCC);
        addView(briLabel);
        SeekBar bri = new SeekBar(ctx);
        bri.setMax(255);
        try {
            bri.setProgress(Settings.System.getInt(ctx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 128));
        } catch (Exception ignored) { bri.setProgress(128); }
        bri.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean u) {
                try {
                    Settings.System.putInt(ctx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, Math.max(1, p));
                    android.view.WindowManager.LayoutParams lp2 =
                        ((android.app.Activity) ctx).getWindow().getAttributes();
                    lp2.screenBrightness = p / 255f;
                    ((android.app.Activity) ctx).getWindow().setAttributes(lp2);
                } catch (Exception ignored) {}
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, -2);
        blp.topMargin = dp(ctx, 4);
        addView(bri, blp);
    }

    private void addToggleRow(String icon, String label, boolean active, Runnable action) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8));
        row.setBackground(rr(active ? 0x334A90D9 : 0x22FFFFFF, 10, ctx));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1, -2);
        rlp.bottomMargin = dp(ctx, 6);
        row.setLayoutParams(rlp);

        TextView ico = tv(ctx, icon + "  " + label, 13, 0xFFFFFFFF);
        row.addView(ico, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView state = tv(ctx, active ? "Ativo" : "Desligado", 11,
            active ? 0xFF4FC3F7 : 0xFF90A4AE);
        row.addView(state);

        row.setOnClickListener(v -> action.run());
        addView(row, rlp);
    }

    private boolean isConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        } catch (Exception e) { return false; }
    }
}
