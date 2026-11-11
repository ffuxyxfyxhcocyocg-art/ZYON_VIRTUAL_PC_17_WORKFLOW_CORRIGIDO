package com.zyon.virtualpc.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/**
 * Configurações de mouse: sensibilidade, velocidade, lista de dispositivos conectados.
 */
public class MouseSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFF080F1A);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(this,20), dp(this,16), dp(this,20), dp(this,24));
        scroll.addView(root);
        setContentView(scroll);

        TextView back = tv(this, "‹  VOLTAR", 13, 0xFF9FB3D1);
        back.setOnClickListener(v -> finish());
        root.addView(back);

        TextView title = tv(this, "🖱️  CONFIGURAÇÕES DE MOUSE", 18, 0xFFFFFFFF);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1,-2);
        tlp.topMargin = dp(this,10); tlp.bottomMargin = dp(this,16);
        root.addView(title, tlp);

        SharedPreferences prefs = getSharedPreferences("zyon_mouse", MODE_PRIVATE);

        // Dispositivos conectados
        addSection(root, "DISPOSITIVOS CONECTADOS");
        PhysicalInputManager pim = new PhysicalInputManager(this);
        java.util.List<String> mice = pim.listMice();
        if (mice.isEmpty()) {
            root.addView(tv(this, "Nenhum mouse detectado. Conecte via USB/OTG/Bluetooth.", 13, 0xFF64748B));
        } else {
            for (String m : mice) {
                TextView mt = tv(this, "✅  " + m, 13, 0xFF4ADE80);
                root.addView(mt);
            }
        }
        pim.destroy();

        // Sensibilidade
        addSection(root, "SENSIBILIDADE DO CURSOR");
        SeekBar sens = new SeekBar(this);
        sens.setMax(200);
        int savedSens = prefs.getInt("sensitivity", 100);
        sens.setProgress(savedSens);
        root.addView(sens);
        TextView sensVal = tv(this, savedSens + "%", 13, 0xFF94A3B8);
        root.addView(sensVal);
        sens.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean u) {
                sensVal.setText(p + "%");
                prefs.edit().putInt("sensitivity", p).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // Velocidade
        addSection(root, "VELOCIDADE DO CURSOR");
        SeekBar speed = new SeekBar(this);
        speed.setMax(10);
        int savedSpeed = prefs.getInt("speed", 5);
        speed.setProgress(savedSpeed);
        root.addView(speed);
        TextView speedVal = tv(this, savedSpeed + "/10", 13, 0xFF94A3B8);
        root.addView(speedVal);
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean u) {
                speedVal.setText(p + "/10");
                prefs.edit().putInt("speed", p).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // Info sobre OTG
        addSection(root, "INFORMAÇÕES");
        String info = "• Mouse USB: conecte via cabo OTG\n" +
                      "• Mouse Bluetooth: pareie nas configurações do Android\n" +
                      "• Hub USB: suportado quando o Android reconhece o hub\n" +
                      "• DPI: lido automaticamente quando o dispositivo informa\n" +
                      "• O cursor aparece automaticamente ao conectar o mouse";
        TextView infoTv = tv(this, info, 13, 0xFFCBD5E1);
        infoTv.setPadding(dp(this,14), dp(this,10), dp(this,14), dp(this,10));
        infoTv.setBackground(rr(0xFF131E30, 8, this));
        root.addView(infoTv);
    }

    private void addSection(LinearLayout root, String label) {
        TextView t = tv(this, label, 11, 0xFF4A90D9);
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin = dp(this,18); lp.bottomMargin = dp(this,6);
        root.addView(t, lp);
    }
}
