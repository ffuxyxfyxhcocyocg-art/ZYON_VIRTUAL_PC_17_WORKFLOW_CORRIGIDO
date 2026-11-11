package com.zyon.virtualpc.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/**
 * Perfis de controle por jogo — mapeamento de botões físicos/virtuais
 * para ações do jogo. Salvo em SharedPreferences por nome de jogo.
 */
public class GameControllerProfileActivity extends AppCompatActivity {

    private static final String[] BUTTONS = {"A","B","X","Y","L1","R1","L2","R2","START","SELECT","↑","↓","←","→"};
    private final EditText[] mappings = new EditText[BUTTONS.length];
    private EditText gameName;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0D1117);
        setContentView(root);

        TextView title = tv(this, "🎮  Perfil de Controle", 18, 0xFFFFFFFF);
        title.setPadding(dp(this,20), dp(this,20), dp(this,20), dp(this,8));
        root.addView(title);

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);
        nameRow.setPadding(dp(this,16), dp(this,8), dp(this,16), dp(this,8));
        TextView nl = tv(this, "Jogo:", 13, 0xFF90A4AE);
        nameRow.addView(nl, new LinearLayout.LayoutParams(dp(this,60), -2));
        gameName = new EditText(this);
        gameName.setHint("Nome do jogo");
        gameName.setHintTextColor(0xFF555555);
        gameName.setTextColor(0xFFFFFFFF);
        gameName.setBackground(rr(0x22FFFFFF, 8, this));
        gameName.setPadding(dp(this,10), dp(this,8), dp(this,10), dp(this,8));
        nameRow.addView(gameName, new LinearLayout.LayoutParams(0, -2, 1f));
        root.addView(nameRow);

        // Load button
        TextView loadBtn = tv(this, "Carregar perfil", 13, 0xFFFFFFFF);
        loadBtn.setPadding(dp(this,16), dp(this,8), dp(this,16), dp(this,8));
        loadBtn.setBackground(rr(0xFF1565C0, 8, this));
        LinearLayout.LayoutParams lbp = new LinearLayout.LayoutParams(-2, -2);
        lbp.leftMargin = dp(this,16);
        lbp.bottomMargin = dp(this,8);
        root.addView(loadBtn, lbp);
        loadBtn.setOnClickListener(v -> loadProfile());

        ScrollView sv = new ScrollView(this);
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(dp(this,16), dp(this,8), dp(this,16), dp(this,16));

        for (int i = 0; i < BUTTONS.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1, -2);
            rlp.bottomMargin = dp(this,6);

            TextView lbl = tv(this, BUTTONS[i], 14, 0xFF4FC3F7);
            lbl.setMinWidth(dp(this, 52));
            row.addView(lbl, new LinearLayout.LayoutParams(dp(this,52), -2));

            mappings[i] = new EditText(this);
            mappings[i].setHint("ação / tecla");
            mappings[i].setHintTextColor(0xFF444444);
            mappings[i].setTextColor(0xFFFFFFFF);
            mappings[i].setTextSize(13);
            mappings[i].setBackground(rr(0x22FFFFFF, 8, this));
            mappings[i].setPadding(dp(this,8), dp(this,6), dp(this,8), dp(this,6));
            row.addView(mappings[i], new LinearLayout.LayoutParams(0, -2, 1f));

            grid.addView(row, rlp);
        }
        sv.addView(grid);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));

        // Save
        TextView saveBtn = tv(this, "💾  Salvar Perfil", 15, 0xFFFFFFFF);
        saveBtn.setGravity(Gravity.CENTER);
        saveBtn.setPadding(dp(this,24), dp(this,14), dp(this,24), dp(this,14));
        saveBtn.setBackground(rr(0xFF1B5E20, 12, this));
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(-1, -2);
        sbp.setMargins(dp(this,16), dp(this,8), dp(this,16), dp(this,16));
        root.addView(saveBtn, sbp);
        saveBtn.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String game = gameName.getText().toString().trim();
        if (game.isEmpty()) { Toast.makeText(this,"Digite o nome do jogo",Toast.LENGTH_SHORT).show(); return; }
        SharedPreferences.Editor ed = getSharedPreferences("zyon_ctrl_" + game, MODE_PRIVATE).edit();
        for (int i = 0; i < BUTTONS.length; i++)
            ed.putString("btn_" + BUTTONS[i], mappings[i].getText().toString().trim());
        ed.apply();
        Toast.makeText(this, "Perfil salvo para: " + game, Toast.LENGTH_SHORT).show();
    }

    private void loadProfile() {
        String game = gameName.getText().toString().trim();
        if (game.isEmpty()) return;
        SharedPreferences p = getSharedPreferences("zyon_ctrl_" + game, MODE_PRIVATE);
        for (int i = 0; i < BUTTONS.length; i++)
            mappings[i].setText(p.getString("btn_" + BUTTONS[i], ""));
    }
}
