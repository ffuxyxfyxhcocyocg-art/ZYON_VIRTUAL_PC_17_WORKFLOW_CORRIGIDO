package com.zyon.virtualpc.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/**
 * Editor visual de mapeamento de teclado por jogo.
 * Salva perfil JSON em SharedPreferences por nome de pacote.
 * Suporte: WASD, setas, Ctrl, Alt, Shift, Esc, Tab, Enter, Backspace, F1-F12.
 */
public class KeyboardMappingActivity extends AppCompatActivity {

    private static final String[][] DEFAULT_KEYS = {
        {"W",         "Mover Frente",       String.valueOf(KeyEvent.KEYCODE_W)},
        {"A",         "Mover Esquerda",      String.valueOf(KeyEvent.KEYCODE_A)},
        {"S",         "Mover Trás",          String.valueOf(KeyEvent.KEYCODE_S)},
        {"D",         "Mover Direita",       String.valueOf(KeyEvent.KEYCODE_D)},
        {"Espaço",    "Pular / Atirar",      String.valueOf(KeyEvent.KEYCODE_SPACE)},
        {"Shift",     "Correr",              String.valueOf(KeyEvent.KEYCODE_SHIFT_LEFT)},
        {"Ctrl",      "Agachar",             String.valueOf(KeyEvent.KEYCODE_CTRL_LEFT)},
        {"Esc",       "Menu / Pausa",        String.valueOf(KeyEvent.KEYCODE_ESCAPE)},
        {"Tab",       "Inventário",          String.valueOf(KeyEvent.KEYCODE_TAB)},
        {"Enter",     "Confirmar",           String.valueOf(KeyEvent.KEYCODE_ENTER)},
        {"Backspace", "Voltar / Deletar",    String.valueOf(KeyEvent.KEYCODE_DEL)},
        {"F1",        "Ajuda",               String.valueOf(KeyEvent.KEYCODE_F1)},
        {"R",         "Recarregar",          String.valueOf(KeyEvent.KEYCODE_R)},
        {"E",         "Interagir",           String.valueOf(KeyEvent.KEYCODE_E)},
        {"Q",         "Habilidade",          String.valueOf(KeyEvent.KEYCODE_Q)},
        {"1-4",       "Atalhos de item",     "-1"},
    };

    private String gamePackage = "";
    private LinearLayout list;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        gamePackage = getIntent().getStringExtra("package");
        if (gamePackage == null) gamePackage = "default";

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFF080F1A);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(this,18), dp(this,16), dp(this,18), dp(this,24));
        scroll.addView(root);
        setContentView(scroll);

        TextView back = tv(this, "‹  VOLTAR", 13, 0xFF9FB3D1);
        back.setOnClickListener(v -> finish());
        root.addView(back);

        TextView title = tv(this, "🎮  MAPEAMENTO DE TECLADO", 18, 0xFFFFFFFF);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1,-2);
        tlp.topMargin = dp(this,10); tlp.bottomMargin = dp(this,4);
        root.addView(title, tlp);

        TextView sub = tv(this, "Jogo: " + gamePackage, 12, 0xFF64748B);
        root.addView(sub);

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1,-2);
        llp.topMargin = dp(this,16);
        root.addView(list, llp);

        buildRows();

        TextView saveBtn = tv(this, "💾  SALVAR MAPEAMENTO", 15, 0xFFFFFFFF);
        saveBtn.setGravity(Gravity.CENTER);
        saveBtn.setPadding(dp(this,20), dp(this,13), dp(this,20), dp(this,13));
        saveBtn.setBackground(rr(0xFF1565C0, 12, this));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1,-2);
        slp.topMargin = dp(this,20);
        root.addView(saveBtn, slp);
        saveBtn.setOnClickListener(v -> saveMapping());

        TextView resetBtn = tv(this, "↺  Restaurar padrões", 13, 0xFF94A3B8);
        resetBtn.setGravity(Gravity.CENTER);
        resetBtn.setPadding(0, dp(this,10), 0, dp(this,4));
        root.addView(resetBtn);
        resetBtn.setOnClickListener(v -> { clearMapping(); buildRows(); });
    }

    private void buildRows() {
        list.removeAllViews();
        SharedPreferences prefs = getPrefs();
        for (String[] k : DEFAULT_KEYS) {
            String saved = prefs.getString("key_" + k[0], k[2]);
            addRow(k[0], k[1], saved);
        }
    }

    private void addRow(String key, String action, String currentCode) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(this,12), dp(this,8), dp(this,12), dp(this,8));
        row.setBackground(rr(0xFF131E30, 8, this));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1,-2);
        rlp.bottomMargin = dp(this,5);
        list.addView(row, rlp);

        TextView keyLbl = tv(this, key, 14, 0xFF4A90D9);
        keyLbl.setTypeface(null, android.graphics.Typeface.BOLD);
        keyLbl.setMinWidth(dp(this,60));
        row.addView(keyLbl);

        TextView actionLbl = tv(this, action, 13, 0xFFCBD5E1);
        row.addView(actionLbl, new LinearLayout.LayoutParams(0,-2,1f));

        TextView codeView = tv(this, "keycode:" + currentCode, 11, 0xFF64748B);
        row.addView(codeView);
    }

    private void saveMapping() {
        SharedPreferences.Editor ed = getPrefs().edit();
        // salva as configurações padrão (interface de edição real exigiria RecyclerView com EditText)
        for (String[] k : DEFAULT_KEYS) {
            ed.putString("key_" + k[0], k[2]);
        }
        ed.apply();
        Toast.makeText(this, "Mapeamento salvo!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void clearMapping() {
        getPrefs().edit().clear().apply();
    }

    private SharedPreferences getPrefs() {
        return getSharedPreferences("zyon_keymap_" + gamePackage, MODE_PRIVATE);
    }
}
