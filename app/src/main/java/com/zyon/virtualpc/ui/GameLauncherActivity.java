package com.zyon.virtualpc.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * CENTRAL DE JOGOS — PC GAMER
 * Exibe jogos Android instalados com interface de PC.
 * Windows: explica que precisa de QEMU e ISO.
 * NÃO apresenta jogos Android como jogos Windows.
 */
public class GameLauncherActivity extends AppCompatActivity {

    private LinearLayout gameList;
    private final int bg    = 0xFF080D19;
    private final int panel = 0xFF141E31;
    private final int accent= 0xFF54D6FF;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(20), dp(22), dp(24));
        scroll.addView(root);
        setContentView(scroll);

        // navegação
        TextView back = text("‹  VOLTAR AO DESKTOP", 13, 0xFF9FB3D1);
        back.setPadding(0, 0, 0, dp(16));
        back.setOnClickListener(v -> finish());
        root.addView(back);

        root.addView(text("🖥️  ZYON  /  GAME CENTER", 12, accent));
        TextView title = text("🎮  PC GAMER", 30, 0xFFFFFFFF);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title);
        root.addView(text("Sua biblioteca de jogos Android com interface de computador.", 13, 0xFFB5C4DA));

        // seção: modo desktop para apps
        addSectionTitle(root, "⬛  MODO DESKTOP · FREEFORM");
        TextView desktopInfo = text("Abra qualquer app Android em modo desktop com orientação forçada horizontal. Em dispositivos com suporte Freeform, o app abre em janela.", 13, 0xFFCBD5E1);
        desktopInfo.setBackground(round(panel));
        desktopInfo.setPadding(dp(14), dp(12), dp(14), dp(12));
        root.addView(desktopInfo);
        TextView desktopBtn = text("🖥️  ABRIR MODO DESKTOP PARA APPS", 14, 0xFFFFFFFF);
        desktopBtn.setGravity(Gravity.CENTER);
        desktopBtn.setPadding(dp(16), dp(12), dp(16), dp(12));
        desktopBtn.setBackground(round(0xFF1565C0));
        desktopBtn.setClickable(true);
        LinearLayout.LayoutParams dblp = new LinearLayout.LayoutParams(-1, -2);
        dblp.topMargin = dp(8);
        root.addView(desktopBtn, dblp);
        desktopBtn.setOnClickListener(v -> startActivity(new Intent(this, AndroidDesktopModeActivity.class)));

        // seção: jogos Android detectados
        addSectionTitle(root, "🎮  JOGOS ANDROID INSTALADOS");
        gameList = new LinearLayout(this);
        gameList.setOrientation(LinearLayout.VERTICAL);
        root.addView(gameList);
        loadGames();

        // seção: Windows virtual
        addSectionTitle(root, "🪟  WINDOWS VIRTUAL (QEMU)");
        TextView winInfo = text(
            "Para jogos e aplicativos Windows de verdade, configure uma máquina virtual:\n\n" +
            "1. Obtenha uma ISO do Windows 10 ou 11 (ARM ou x86_64)\n" +
            "2. Configure uma máquina virtual com disco virtual\n" +
            "3. O motor QEMU (libqemu-arm64.so) precisa estar em jniLibs/arm64-v8a/\n" +
            "4. O firmware OVMF para UEFI precisa estar em assets/firmware/\n\n" +
            "⚠️ Esta versão do projeto não inclui os binários QEMU por tamanho. " +
            "Compilar o QEMU para Android ARM64 requer NDK r25+ e a flag --target=aarch64-linux-android.",
            13, 0xFFCBD5E1);
        winInfo.setBackground(round(panel));
        winInfo.setPadding(dp(16), dp(14), dp(16), dp(14));
        root.addView(winInfo);

        TextView vmBtn = text("⚙️  CONFIGURAR MÁQUINA VIRTUAL", 14, 0xFFFFFFFF);
        vmBtn.setGravity(Gravity.CENTER);
        vmBtn.setPadding(dp(16), dp(12), dp(16), dp(12));
        vmBtn.setBackground(round(0xFF233A5C));
        vmBtn.setClickable(true);
        LinearLayout.LayoutParams vblp = new LinearLayout.LayoutParams(-1, -2);
        vblp.topMargin = dp(8); vblp.bottomMargin = dp(8);
        root.addView(vmBtn, vblp);
        vmBtn.setOnClickListener(v -> startActivity(new Intent(this, CreateVmActivity.class)));

        // mapeamento de teclado
        addSectionTitle(root, "⌨️  MAPEAMENTO DE TECLADO / MOUSE");
        TextView mapInfo = text("Configure mouse e teclado físicos para seus jogos Android. WASD, mira, câmera e atalhos personalizáveis por jogo.", 13, 0xFFCBD5E1);
        root.addView(mapInfo);
        TextView mapBtn = text("⌨️  ABRIR EDITOR DE MAPEAMENTO", 14, 0xFFFFFFFF);
        mapBtn.setGravity(Gravity.CENTER);
        mapBtn.setPadding(dp(16), dp(12), dp(16), dp(12));
        mapBtn.setBackground(round(0xFF1A3050));
        mapBtn.setClickable(true);
        LinearLayout.LayoutParams mblp = new LinearLayout.LayoutParams(-1, -2);
        mblp.topMargin = dp(8);
        root.addView(mapBtn, mblp);
        mapBtn.setOnClickListener(v -> startActivity(new Intent(this, KeyboardMappingActivity.class)));
    }

    private void addSectionTitle(LinearLayout root, String label) {
        TextView t = text(label, 14, accent);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(24); lp.bottomMargin = dp(8);
        root.addView(t, lp);
    }

    private void loadGames() {
        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> all = pm.queryIntentActivities(query, 0);

        List<ResolveInfo> games = new ArrayList<>();
        for (ResolveInfo r : all) {
            ApplicationInfo ai = r.activityInfo.applicationInfo;
            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                games.add(r);
        }

        gameList.removeAllViews();
        if (games.isEmpty()) {
            gameList.addView(text("Nenhum app instalado detectado.", 13, 0xFF64748B));
            return;
        }
        for (ResolveInfo info : games) {
            String label = info.loadLabel(pm).toString();
            Drawable icon = info.loadIcon(pm);
            String pkg = info.activityInfo.packageName;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), dp(10), dp(14), dp(10));
            row.setBackground(round(panel));
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1, -2);
            rlp.bottomMargin = dp(5);
            gameList.addView(row, rlp);

            ImageView iv = new ImageView(this);
            iv.setImageDrawable(icon);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            row.addView(iv, new LinearLayout.LayoutParams(dp(40), dp(40)));

            LinearLayout info2 = new LinearLayout(this);
            info2.setOrientation(LinearLayout.VERTICAL);
            info2.setPadding(dp(12), 0, 0, 0);
            TextView lbl = text(label, 14, 0xFFE2E8F0);
            TextView pkgTv = text(pkg, 11, 0xFF64748B);
            info2.addView(lbl);
            info2.addView(pkgTv);
            row.addView(info2, new LinearLayout.LayoutParams(0, -2, 1f));

            TextView btn = text("▶", 18, 0xFF4A90D9);
            btn.setPadding(dp(12), dp(8), dp(12), dp(8));
            btn.setClickable(true);
            btn.setOnClickListener(v -> {
                Intent i = pm.getLaunchIntentForPackage(pkg);
                if (i != null) startActivity(i);
                else Toast.makeText(this, "Não foi possível abrir.", Toast.LENGTH_SHORT).show();
            });
            row.addView(btn);
        }
    }

    private TextView text(String s, float sp, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        return t;
    }

    private android.graphics.drawable.GradientDrawable round(int color) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(10));
        return g;
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
