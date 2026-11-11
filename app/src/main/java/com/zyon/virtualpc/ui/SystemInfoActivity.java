package com.zyon.virtualpc.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/** Painel completo de informações do sistema — hardware real + status ZYON. */
public class SystemInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);

        ScrollView sv = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0D1117);
        root.setPadding(dp(this,20), dp(this,20), dp(this,20), dp(this,20));
        sv.addView(root);
        setContentView(sv);

        addTitle(root, "🖥️  Sobre este PC — ZYON VIRTUAL PC 17");

        addSection(root, "Sistema");
        addRow(root, "Nome", "ZYON VIRTUAL PC 17 — Master Ultimate");
        addRow(root, "Versão", "17.0.0");
        addRow(root, "Plataforma", "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        addRow(root, "Dispositivo", Build.MANUFACTURER + " " + Build.MODEL);
        addRow(root, "Arquitetura", System.getProperty("os.arch", "arm64"));
        addRow(root, "Build", Build.FINGERPRINT.length() > 40 ? Build.FINGERPRINT.substring(0,40)+"…" : Build.FINGERPRINT);

        addSection(root, "Processador");
        int cores = Runtime.getRuntime().availableProcessors();
        addRow(root, "Núcleos disponíveis", String.valueOf(cores));
        addRow(root, "Hardware", Build.HARDWARE);
        addRow(root, "Suporte KVM", "Não disponível sem root (TCG ativo)");

        addSection(root, "Memória");
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        if (am != null) am.getMemoryInfo(mi);
        long totalMB = mi.totalMem / (1024*1024);
        long availMB = mi.availMem / (1024*1024);
        long usedMB  = totalMB - availMB;
        addRow(root, "RAM total", totalMB + " MB");
        addRow(root, "RAM em uso", usedMB + " MB");
        addRow(root, "RAM disponível", availMB + " MB");
        addRow(root, "Pressão de memória", mi.lowMemory ? "⚠️ BAIXA" : "✅ Normal");

        addSection(root, "Armazenamento");
        try {
            StatFs sf = new StatFs(getFilesDir().getAbsolutePath());
            long blockSize = sf.getBlockSizeLong();
            long totalBlocks = sf.getBlockCountLong();
            long freeBlocks = sf.getAvailableBlocksLong();
            long totalGB = (totalBlocks * blockSize) / (1024*1024*1024);
            long freeGB  = (freeBlocks  * blockSize) / (1024*1024*1024);
            addRow(root, "Armazenamento interno", totalGB + " GB total / " + freeGB + " GB livre");
        } catch (Exception e) {
            addRow(root, "Armazenamento", "Indisponível");
        }

        addSection(root, "Virtualização");
        boolean qemuPresent = AppCatalog.qemuPresent(this);
        addRow(root, "QEMU", qemuPresent ? "✅ Binário presente" : "⚠️ Não encontrado em jniLibs");
        addRow(root, "Firmware OVMF", checkAsset("firmware/OVMF_CODE.fd") ? "✅ Presente" : "⚠️ Não encontrado");
        addRow(root, "Firmware SeaBIOS", checkAsset("bios/bios-256k.bin") ? "✅ Presente" : "⚠️ Não encontrado");
        addRow(root, "Modo sem root", "SLIRP/NAT disponível");

        addSection(root, "Gráficos");
        addRow(root, "OpenGL ES", "3.0+ (requerido)");
        addRow(root, "Renderização VM", "SurfaceView (VmConsoleActivity)");
    }

    private void addTitle(LinearLayout root, String text) {
        TextView t = tv(this, text, 17, 0xFF4FC3F7);
        t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin = dp(this,16);
        root.addView(t, lp);
    }

    private void addSection(LinearLayout root, String name) {
        android.view.View sep = new android.view.View(this);
        sep.setBackgroundColor(0x22FFFFFF);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1,1);
        slp.topMargin = dp(this,12);
        slp.bottomMargin = dp(this,6);
        root.addView(sep, slp);
        TextView t = tv(this, name, 13, 0xFF90A4AE);
        t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(t);
    }

    private void addRow(LinearLayout root, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin = dp(this,4);
        TextView l = tv(this, label, 12, 0xFF90A4AE);
        row.addView(l, new LinearLayout.LayoutParams(dp(this,160),-2));
        TextView v = tv(this, value, 12, 0xFFFFFFFF);
        v.setMaxLines(2);
        row.addView(v, new LinearLayout.LayoutParams(0,-2,1f));
        root.addView(row, lp);
    }

    private boolean checkAsset(String path) {
        try { getAssets().open(path).close(); return true; }
        catch (Exception e) { return false; }
    }
}
