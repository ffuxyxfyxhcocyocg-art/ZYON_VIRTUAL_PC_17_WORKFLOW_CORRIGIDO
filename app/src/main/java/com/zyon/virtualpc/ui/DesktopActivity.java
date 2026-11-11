package com.zyon.virtualpc.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.zyon.virtualpc.ui.DesktopApps.dp;
import static com.zyon.virtualpc.ui.DesktopApps.rr;
import static com.zyon.virtualpc.ui.DesktopApps.tv;

/**
 * DESKTOP do ZYON VIRTUAL PC 17 — interface estilo Windows 11.
 * - Área de trabalho com ícones e papel de parede
 * - Menu Iniciar com busca e apps do celular
 * - Barra de tarefas com relógio, notificações, bateria
 * - Janelas com minimizar/maximizar/fechar (botões reais ─ □ ✕)
 * - Suporte a mouse físico via PhysicalInputManager
 * - Suporte a teclado físico (atalhos, WASD, F-keys)
 * - ALT+TAB, Snap Layout
 * - Mouse virtual com cursor de PC (seta padrão)
 */
public class DesktopActivity extends AppCompatActivity {

    private static final class Tool {
        final String emoji, label;
        final Runnable run;
        Tool(String e, String l, Runnable r) { emoji = e; label = l; run = r; }
    }

    private final Handler h = new Handler(Looper.getMainLooper());
    private final List<ZWindow> windows = new ArrayList<>();
    private final List<Tool> tools = new ArrayList<>();

    private FrameLayout root, windowLayer;
    private LinearLayout taskbarApps, startList, startPanel;
    private View startScrim;
    private EditText startSearch;
    private TextView clock;
    private boolean startOpen = false;
    private int screenW, screenH, taskbarH;
    private File sharedRoot;

    // overlays
    private AltTabOverlay altTabOverlay;
    private SnapLayoutOverlay snapOverlay;
    private VirtualMouseView mouseOverlay;
    private VirtualKeyboardView keyboardView;
    private LinearLayout keyboardContainer;
    private boolean keyboardVisible = false;
    private boolean altKeyDown = false;
    private ZWindow pendingSnapWindow = null;
    private QuickActionsView quickActionsPanel = null;
    private boolean quickActionsVisible = false;

    // mouse físico
    private PhysicalInputManager physicalInput;
    private boolean physicalMouseActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(wlp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideBars();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenW = dm.widthPixels;
        screenH = dm.heightPixels;
        taskbarH = dp(this, 48);
        sharedRoot = new File(getFilesDir(), "zyon/shared");
        for (String d : new String[]{"Documentos", "Downloads", "Imagens", "apks"})
            new File(sharedRoot, d).mkdirs();

        // mouse físico
        physicalInput = new PhysicalInputManager(this);
        physicalMouseActive = physicalInput.hasPhysicalMouse();

        buildTools();
        buildUi();
        buildOverlays();
        setupPhysicalInput();
        refreshStart("");
        tickClock();

        if (!AppCatalog.isLoaded()) {
            new Thread(() -> {
                AppCatalog.get(getApplicationContext());
                h.post(() -> { if (!isFinishing()) refreshStart(startSearch.getText().toString()); });
            }).start();
        }
        root.post(() -> openStart(false));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (startOpen) { closeStart(); return; }
                if (!windows.isEmpty()) { windows.get(windows.size() - 1).close(); return; }
                new AlertDialog.Builder(DesktopActivity.this)
                        .setTitle("Desligar o PC virtual?")
                        .setPositiveButton("Desligar", (d, w) -> finishAffinity())
                        .setNegativeButton("Continuar", null)
                        .show();
            }
        });
    }

    // ── mouse e teclado físico ──────────────────────────────────────────────

    private void setupPhysicalInput() {
        physicalInput.setMouseListener(new PhysicalInputManager.PhysicalMouseListener() {
            @Override public void onMouseMove(float dx, float dy) {
                runOnUiThread(() -> {
                    mouseOverlay.setVisibility(View.VISIBLE);
                    mouseOverlay.moveRelative(dx, dy);
                });
            }
            @Override public void onMouseButton(int button, boolean pressed) {
                if (!pressed) return;
                runOnUiThread(() -> {
                    float cx = mouseOverlay.getCursorX();
                    float cy = mouseOverlay.getCursorY();
                    if (button == MotionEvent.BUTTON_PRIMARY) {
                        // simular toque na coordenada do cursor
                        dispatchTouchAtCursor(cx, cy);
                    } else if (button == MotionEvent.BUTTON_SECONDARY) {
                        showContextMenu(cx, cy);
                    }
                });
            }
            @Override public void onMouseScroll(float vScroll, float hScroll) {}
        });

        physicalInput.setKeyboardListener((keyCode, scanCode, pressed, metaState) -> {
            if (!pressed) return;
            runOnUiThread(() -> handlePhysicalKey(keyCode, metaState));
        });

        // mostrar ou esconder cursor conforme mouse presente
        if (physicalMouseActive) {
            mouseOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void dispatchTouchAtCursor(float cx, float cy) {
        // Encontrar a view mais à frente na posição e "clicar" nela
        View hit = findTopViewAt((int)cx, (int)cy, root);
        if (hit != null && hit.isClickable()) hit.performClick();
    }

    private View findTopViewAt(int x, int y, ViewGroup parent) {
        // percorre do topo para a base
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            View child = parent.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;
            int[] loc = new int[2]; child.getLocationOnScreen(loc);
            if (x >= loc[0] && x <= loc[0] + child.getWidth()
                    && y >= loc[1] && y <= loc[1] + child.getHeight()) {
                if (child instanceof ViewGroup) {
                    View deeper = findTopViewAt(x, y, (ViewGroup) child);
                    if (deeper != null) return deeper;
                }
                return child;
            }
        }
        return null;
    }

    private void showContextMenu(float cx, float cy) {
        // menu de contexto simples no desktop
        new AlertDialog.Builder(this)
                .setTitle("Menu de contexto")
                .setItems(new String[]{
                        "🖥️  Propriedades do desktop",
                        "🖼️  Trocar papel de parede",
                        "🔄  Atualizar desktop"
                }, (d, w) -> {
                    if (w == 1) startActivity(new Intent(this, WallpaperPickerActivity.class));
                })
                .show();
    }

    private void handlePhysicalKey(int keyCode, int meta) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ESCAPE:
                if (startOpen) closeStart(); break;
            case KeyEvent.KEYCODE_TAB:
                if (altKeyDown) showAltTab(); break;
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
                altKeyDown = true; break;
            case KeyEvent.KEYCODE_META_LEFT:
                // tecla Windows: abrir/fechar menu Iniciar
                if (startOpen) closeStart(); else openStart(false);
                break;
        }
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        if (physicalInput != null && physicalInput.handleMotionEvent(ev)) return true;
        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent ev) {
        if (physicalInput != null) physicalInput.handleKeyEvent(ev);
        if (ev.getKeyCode() == KeyEvent.KEYCODE_ALT_LEFT
                || ev.getKeyCode() == KeyEvent.KEYCODE_ALT_RIGHT) {
            altKeyDown = (ev.getAction() == KeyEvent.ACTION_DOWN);
        }
        return super.dispatchKeyEvent(ev);
    }

    // ── métodos de UI básicos ──────────────────────────────────────────────

    private void hideBars() {
        WindowInsetsControllerCompat c = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        c.hide(WindowInsetsCompat.Type.systemBars());
        c.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    @Override protected void onPause() {
        super.onPause();
        for (ZWindow w : windows) if (w.content instanceof WinContent) ((WinContent) w.content).onSave();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        h.removeCallbacksAndMessages(null);
        if (physicalInput != null) physicalInput.destroy();
        for (ZWindow w : new ArrayList<>(windows))
            if (w.content instanceof WinContent) ((WinContent) w.content).onClose();
    }

    // ── build tools (ícones do menu Iniciar) ──────────────────────────────

    private void buildTools() {
        tools.add(new Tool("🖥️", "PC Virtual", () -> startActivity(new Intent(this, VmConsoleActivity.class))));
        tools.add(new Tool("🎮", "Game Center", () -> startActivity(new Intent(this, GameLauncherActivity.class))));
        tools.add(new Tool("📱", "Apps no Desktop", () -> startActivity(new Intent(this, AndroidDesktopModeActivity.class))));
        tools.add(new Tool("🖱️", "Mouse", () -> startActivity(new Intent(this, MouseSettingsActivity.class))));
        tools.add(new Tool("⌨️", "Teclado / Mapeamento", () -> startActivity(new Intent(this, KeyboardMappingActivity.class))));
        tools.add(new Tool("📁", "Arquivos", () -> openWindow("📁 Arquivos", "📁",
                new DesktopApps.FileExplorerView(this, sharedRoot), 460, 400)));
        tools.add(new Tool("📊", "Gerenciador de Tarefas", () -> openWindow("📊 Gerenciador", "📊",
                new DesktopApps.TaskManagerView(this), 400, 350)));
        tools.add(new Tool("⚙️", "Configurações", () -> startActivity(new Intent(this, SettingsActivity.class))));
        tools.add(new Tool("🖼️", "Papel de Parede", () -> startActivity(new Intent(this, WallpaperPickerActivity.class))));
        tools.add(new Tool("💾", "Discos / ISO", () -> startActivity(new Intent(this, DiskManagerActivity.class))));
        tools.add(new Tool("📸", "Snapshots", () -> startActivity(new Intent(this, SnapshotsActivity.class))));
        tools.add(new Tool("📋", "Logs", () -> startActivity(new Intent(this, LogsActivity.class))));
        tools.add(new Tool("ℹ️", "Informações do sistema", () -> startActivity(new Intent(this, SystemInfoActivity.class))));
        tools.add(new Tool("🖥️", "Resolução", () -> startActivity(new Intent(this, ResolutionSettingsActivity.class))));
        tools.add(new Tool("⚡", "Desempenho", () -> startActivity(new Intent(this, PerformanceActivity.class))));
    }

    // ── build UI ──────────────────────────────────────────────────────────

    private void buildUi() {
        root = new FrameLayout(this);
        root.setBackgroundColor(0xFF0D1B2A);
        setContentView(root);

        // papel de parede (placeholder azul escuro gradiente)
        View wallpaper = new View(this);
        wallpaper.setBackground(new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF0D1B2A, 0xFF162032, 0xFF0A1628}));
        root.addView(wallpaper, new FrameLayout.LayoutParams(-1, -1));

        // camada de janelas
        windowLayer = new FrameLayout(this);
        FrameLayout.LayoutParams wlLP = new FrameLayout.LayoutParams(-1, -1);
        wlLP.bottomMargin = taskbarH;
        root.addView(windowLayer, wlLP);

        // ícones do desktop
        buildDesktopIcons();

        // barra de tarefas
        buildTaskbar();

        // menu Iniciar
        buildStartMenu();
    }

    private void buildDesktopIcons() {
        // ícones rápidos no canto superior esquerdo
        LinearLayout icons = new LinearLayout(this);
        icons.setOrientation(LinearLayout.VERTICAL);
        icons.setPadding(dp(this, 16), dp(this, 20), 0, 0);
        FrameLayout.LayoutParams ilp = new FrameLayout.LayoutParams(-2, -2);
        root.addView(icons, ilp);

        String[][] desktopIcons = {
            {"🖥️", "PC Virtual"},
            {"📁", "Arquivos"},
            {"🎮", "Jogos"},
            {"⚙️", "Configurações"},
        };
        for (String[] ic : desktopIcons) {
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            cell.setPadding(dp(this,8), dp(this,6), dp(this,8), dp(this,6));
            cell.setClickable(true);
            cell.setFocusable(true);

            TextView ico = tv(this, ic[0], 28, 0xFFFFFFFF);
            ico.setGravity(Gravity.CENTER);
            cell.addView(ico);

            TextView lbl = tv(this, ic[1], 10, 0xFFFFFFFF);
            lbl.setGravity(Gravity.CENTER);
            lbl.setShadowLayer(2, 1, 1, 0xFF000000);
            cell.addView(lbl);

            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-2, -2);
            clp.bottomMargin = dp(this, 4);
            icons.addView(cell, clp);

            String name = ic[1];
            cell.setOnClickListener(v -> handleDesktopIconClick(name));
        }
    }

    private void handleDesktopIconClick(String name) {
        switch (name) {
            case "PC Virtual":    startActivity(new Intent(this, VmConsoleActivity.class)); break;
            case "Arquivos":      openWindow("📁 Arquivos", "📁", new DesktopApps.FileExplorerView(this, sharedRoot), 460, 400); break;
            case "Jogos":         startActivity(new Intent(this, GameLauncherActivity.class)); break;
            case "Configurações": startActivity(new Intent(this, SettingsActivity.class)); break;
        }
    }

    private void buildTaskbar() {
        LinearLayout taskbar = new LinearLayout(this);
        taskbar.setOrientation(LinearLayout.HORIZONTAL);
        taskbar.setGravity(Gravity.CENTER_VERTICAL);
        taskbar.setBackgroundColor(0xE6050C1A);
        taskbar.setPadding(dp(this, 6), 0, dp(this, 10), 0);

        FrameLayout.LayoutParams tblp = new FrameLayout.LayoutParams(-1, taskbarH, Gravity.BOTTOM);
        root.addView(taskbar, tblp);

        // botão iniciar
        TextView startBtn = tv(this, "🖥️", 22, 0xFFFFFFFF);
        startBtn.setGravity(Gravity.CENTER);
        startBtn.setPadding(dp(this, 10), 0, dp(this, 10), 0);
        startBtn.setOnClickListener(v -> { if (startOpen) closeStart(); else openStart(true); });
        taskbar.addView(startBtn, new LinearLayout.LayoutParams(-2, -1));

        // apps abertos (barra de tarefas)
        taskbarApps = new LinearLayout(this);
        taskbarApps.setOrientation(LinearLayout.HORIZONTAL);
        taskbarApps.setGravity(Gravity.CENTER_VERTICAL);
        taskbar.addView(taskbarApps, new LinearLayout.LayoutParams(0, -1, 1f));

        // área de notificação direita
        LinearLayout sysArea = new LinearLayout(this);
        sysArea.setOrientation(LinearLayout.HORIZONTAL);
        sysArea.setGravity(Gravity.CENTER_VERTICAL);

        // indicador de mouse físico
        TextView mouseInd = tv(this, physicalMouseActive ? "🖱️" : "👆", 16, physicalMouseActive ? 0xFF4ADE80 : 0xFF64748B);
        mouseInd.setPadding(dp(this, 6), 0, dp(this, 6), 0);
        sysArea.addView(mouseInd);

        // relógio
        clock = tv(this, "00:00", 12, 0xFFFFFFFF);
        clock.setGravity(Gravity.CENTER);
        clock.setPadding(dp(this, 8), 0, dp(this, 4), 0);
        sysArea.addView(clock);

        taskbar.addView(sysArea);
    }

    private void tickClock() {
        h.postDelayed(() -> {
            if (clock != null && !isFinishing()) {
                clock.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
                tickClock();
            }
        }, 10_000);
        if (clock != null)
            clock.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
    }

    private void buildStartMenu() {
        startScrim = new View(this);
        startScrim.setBackgroundColor(0x66000000);
        startScrim.setVisibility(View.GONE);
        startScrim.setOnClickListener(v -> closeStart());
        root.addView(startScrim, new FrameLayout.LayoutParams(-1, -1));

        int menuW = Math.min(dp(this, 640), screenW - dp(this, 24));
        startPanel = new LinearLayout(this);
        startPanel.setOrientation(LinearLayout.VERTICAL);
        GradientDrawableHolder.apply(startPanel, this);
        startPanel.setVisibility(View.GONE);
        FrameLayout.LayoutParams splp = new FrameLayout.LayoutParams(menuW,
                screenH - taskbarH - dp(this, 12), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        splp.bottomMargin = taskbarH + dp(this, 4);
        root.addView(startPanel, splp);

        // título do menu
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(this, 20), dp(this, 14), dp(this, 20), dp(this, 8));
        TextView menuTitle = tv(this, "🖥️  ZYON VIRTUAL PC 17", 14, 0xFF4A90D9);
        menuTitle.setTypeface(null, Typeface.BOLD);
        header.addView(menuTitle, new LinearLayout.LayoutParams(0, -2, 1f));
        startPanel.addView(header);

        startSearch = new EditText(this);
        startSearch.setHint("🔍  Pesquisar aplicativos e configurações");
        startSearch.setHintTextColor(0xFF90A4AE);
        startSearch.setTextColor(0xFFFFFFFF);
        startSearch.setTextSize(14);
        startSearch.setSingleLine(true);
        startSearch.setPadding(dp(this, 16), dp(this, 10), dp(this, 16), dp(this, 10));
        startSearch.setBackground(rr(0x33FFFFFF, 20, this));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.setMargins(dp(this, 20), 0, dp(this, 20), dp(this, 6));
        startPanel.addView(startSearch, sp);
        startSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable e) { refreshStart(e.toString()); }
        });

        ScrollView sv = new ScrollView(this);
        startList = new LinearLayout(this);
        startList.setOrientation(LinearLayout.VERTICAL);
        startList.setPadding(dp(this, 14), 0, dp(this, 14), dp(this, 8));
        sv.addView(startList);
        startPanel.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));

        // rodapé
        LinearLayout foot = new LinearLayout(this);
        foot.setOrientation(LinearLayout.HORIZONTAL);
        foot.setGravity(Gravity.CENTER_VERTICAL);
        foot.setBackgroundColor(0x33000000);
        foot.setPadding(dp(this, 20), dp(this, 10), dp(this, 20), dp(this, 10));
        TextView user = tv(this, "👤  Usuário do ZYON", 13, 0xFFFFFFFF);
        foot.addView(user, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView power = tv(this, "⏻", 20, 0xFFFFFFFF);
        power.setPadding(dp(this, 12), 0, dp(this, 4), 0);
        power.setOnClickListener(v -> powerMenu(v));
        foot.addView(power);
        startPanel.addView(foot, new LinearLayout.LayoutParams(-1, -2));
    }

    private void refreshStart(String q) {
        String f = q == null ? "" : q.trim().toLowerCase(Locale.getDefault());
        startList.removeAllViews();
        int cellW = dp(this, 88);
        int menuW = Math.min(dp(this, 640), screenW - dp(this, 24)) - dp(this, 28);
        int cols = Math.max(3, menuW / cellW);

        List<Tool> pinned = new ArrayList<>();
        for (Tool t : tools)
            if (f.isEmpty() || t.label.toLowerCase(Locale.getDefault()).contains(f)) pinned.add(t);

        if (!pinned.isEmpty()) {
            startList.addView(header("Fixado"));
            GridLayout g = new GridLayout(this);
            g.setColumnCount(cols);
            for (Tool t : pinned) g.addView(cell(t.emoji, null, t.label, cellW, t.run, true));
            startList.addView(g);
        }

        List<AppCatalog.Entry> apps = new ArrayList<>();
        boolean loaded = AppCatalog.isLoaded();
        if (loaded)
            for (AppCatalog.Entry e : AppCatalog.get(this))
                if (f.isEmpty() || e.label.toLowerCase(Locale.getDefault()).contains(f)) apps.add(e);

        startList.addView(header(loaded ? "Apps do celular (" + apps.size() + ")" : "Apps do celular"));
        if (!loaded) {
            startList.addView(tv(this, "Carregando apps…", 12, 0xFF90A4AE));
        } else if (apps.isEmpty()) {
            startList.addView(tv(this, "Nenhum app encontrado.", 12, 0xFF90A4AE));
        } else {
            GridLayout g = new GridLayout(this);
            g.setColumnCount(cols);
            for (AppCatalog.Entry e : apps)
                g.addView(cell(null, e.icon, e.label, cellW, () -> launchApp(e), true));
            startList.addView(g);
        }
    }

    private TextView header(String s) {
        TextView t = tv(this, s, 13, 0xFFFFFFFF);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(dp(this, 6), dp(this, 12), 0, dp(this, 6));
        return t;
    }

    private View cell(String emoji, android.graphics.drawable.Drawable icon, String label,
                      int cellW, Runnable action, boolean wrap) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER_HORIZONTAL);
        cell.setPadding(dp(this, 6), dp(this, 8), dp(this, 6), dp(this, 8));
        GridLayout.LayoutParams glp = new GridLayout.LayoutParams();
        glp.width = cellW;
        cell.setLayoutParams(glp);
        cell.setClickable(true);
        cell.setFocusable(true);
        cell.setOnClickListener(v -> { closeStart(); action.run(); });

        if (icon != null) {
            ImageView iv = new ImageView(this);
            iv.setImageDrawable(icon);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            cell.addView(iv, new LinearLayout.LayoutParams(dp(this, 36), dp(this, 36)));
        } else {
            TextView t = tv(this, emoji, 26, 0xFFFFFFFF);
            t.setGravity(Gravity.CENTER);
            cell.addView(t);
        }
        TextView lbl = tv(this, label, 10, 0xFFCBD5E1);
        lbl.setGravity(Gravity.CENTER);
        lbl.setMaxLines(2);
        lbl.setEllipsize(TextUtils.TruncateAt.END);
        cell.addView(lbl);
        return cell;
    }

    private void launchApp(AppCatalog.Entry e) {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(e.pkg);
            if (intent != null) startActivity(intent);
            else toast("App não pode ser aberto.");
        } catch (Exception ex) { toast("Erro: " + ex.getMessage()); }
    }

    private void openStart(boolean focusSearch) {
        startOpen = true;
        startScrim.setVisibility(View.VISIBLE);
        startPanel.setAlpha(0f);
        startPanel.setVisibility(View.VISIBLE);
        startPanel.animate().alpha(1f).setDuration(120).start();
        if (focusSearch) {
            startSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(startSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void closeStart() {
        if (!startOpen) return;
        startOpen = false;
        startScrim.setVisibility(View.GONE);
        startPanel.setVisibility(View.GONE);
        startSearch.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(startSearch.getWindowToken(), 0);
        hideBars();
    }

    private void powerMenu(View anchor) {
        PopupMenu m = new PopupMenu(this, anchor);
        m.getMenu().add("🔄  Reiniciar");
        m.getMenu().add("⏻  Desligar");
        m.setOnMenuItemClickListener(i -> {
            String title = i.getTitle().toString();
            if (title.contains("Reiniciar")) {
                startActivity(new Intent(this, BootLoadingActivity.class));
                finish();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Desligar?")
                        .setMessage("Dados não salvos podem ser perdidos.")
                        .setPositiveButton("Desligar", (d, w) -> finishAffinity())
                        .setNegativeButton("Cancelar", null).show();
            }
            return true;
        });
        m.show();
    }

    private void showAltTab() {
        altTabOverlay.setWindows(windows);
        altTabOverlay.setVisibility(View.VISIBLE);
        altTabOverlay.bringToFront();
    }

    // ── overlays ──────────────────────────────────────────────────────────

    private void buildOverlays() {
        mouseOverlay = new VirtualMouseView(this);
        mouseOverlay.setListener(new VirtualMouseView.MouseListener() {
            @Override public void onMove(float x, float y) {}
            @Override public void onLeftClick(float x, float y) { dispatchTouchAtCursor(x, y); }
            @Override public void onRightClick(float x, float y) { showContextMenu(x, y); }
            @Override public void onScroll(float x, float y, float d) {}
            @Override public void onDrag(float x, float y) {}
        });
        FrameLayout.LayoutParams mlp = new FrameLayout.LayoutParams(-1, -1);
        mlp.bottomMargin = taskbarH;
        root.addView(mouseOverlay, mlp);
        mouseOverlay.setVisibility(View.GONE);

        altTabOverlay = new AltTabOverlay(this);
        altTabOverlay.setVisibility(View.GONE);
        altTabOverlay.setListener(new AltTabOverlay.AltTabListener() {
            @Override public void onWindowSelected(int index) {
                altTabOverlay.setVisibility(View.GONE);
                if (index >= 0 && index < windows.size()) windows.get(index).focus();
            }
            @Override public void onDismiss() { altTabOverlay.setVisibility(View.GONE); }
        });
        root.addView(altTabOverlay, new FrameLayout.LayoutParams(-1, -1));

        snapOverlay = new SnapLayoutOverlay(this);
        snapOverlay.setVisibility(View.GONE);
        snapOverlay.setListener(zone -> {
            snapOverlay.setVisibility(View.GONE);
            if (pendingSnapWindow != null) { pendingSnapWindow.applySnap(zone); pendingSnapWindow = null; }
        });
        root.addView(snapOverlay, new FrameLayout.LayoutParams(-1, -1));
    }

    // ── janelas ───────────────────────────────────────────────────────────

    private void openWindow(String title, String emoji, View content, int wDp, int hDp) {
        closeStart();
        for (ZWindow w : windows) {
            if (w.title.equals(title)) {
                if (content instanceof WinContent) ((WinContent) content).onClose();
                w.restoreAndFocus(); return;
            }
        }
        int n = windows.size();
        int w = Math.min(dp(this, wDp), screenW - dp(this, 16));
        int hgt = Math.min(dp(this, hDp), screenH - taskbarH - dp(this, 16));
        ZWindow win = new ZWindow(title, emoji, content, w, hgt);
        windowLayer.addView(win, new FrameLayout.LayoutParams(w, hgt, Gravity.TOP | Gravity.START));
        win.setX(Math.min(dp(this, 90) + n * dp(this, 28), Math.max(0, screenW - w)));
        win.setY(Math.min(dp(this, 14) + n * dp(this, 24), Math.max(0, screenH - taskbarH - hgt)));
        windows.add(win);
        win.addTaskButton();
        win.focus();
    }

    // ── ZWindow (janela do desktop) ────────────────────────────────────────

    final class ZWindow extends LinearLayout {
        final String title;
        final String emoji;
        final View content;
        final TextView taskBtn;
        private boolean minimized = false, maximized = false;
        private float nx, ny;
        private int nw, nh;

        ZWindow(String title, String emoji, View content, int w, int hgt) {
            super(DesktopActivity.this);
            this.title = title;
            this.emoji = emoji;
            this.content = content;
            setOrientation(VERTICAL);
            setBackground(rr(0xF2141C2E, 10, getContext()));
            setElevation(dp(getContext(), 8));
            setClickable(true);

            // barra de título
            LinearLayout bar = new LinearLayout(getContext());
            bar.setOrientation(HORIZONTAL);
            bar.setGravity(Gravity.CENTER_VERTICAL);
            bar.setBackground(rr(0xFF1A2740, 10, getContext()));

            TextView t = tv(getContext(), emoji + "  " + title, 12, 0xFFFFFFFF);
            t.setSingleLine(true);
            t.setPadding(dp(getContext(), 12), 0, 0, 0);
            bar.addView(t, new LayoutParams(0, -2, 1f));

            // botões — □ são os de verdade
            bar.addView(winBtn("─", 0x00000000, 0xFFFFFFFF, () -> minimize()));
            View maxBtn = winBtn("□", 0x00000000, 0xFFFFFFFF, () -> toggleMax());
            maxBtn.setOnLongClickListener(v -> {
                pendingSnapWindow = this;
                snapOverlay.setVisibility(View.VISIBLE);
                snapOverlay.bringToFront();
                return true;
            });
            bar.addView(maxBtn);
            bar.addView(winBtn("✕", 0xFFCC3333, 0xFFFFFFFF, () -> close()));

            addView(bar, new LayoutParams(-1, dp(getContext(), 36)));
            addView(content, new LayoutParams(-1, 0, 1f));

            // arrastar barra de título
            bar.setOnTouchListener(new OnTouchListener() {
                float dx, dy;
                @Override public boolean onTouch(View v, MotionEvent e) {
                    switch (e.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            focus(); dx = getX() - e.getRawX(); dy = getY() - e.getRawY(); return true;
                        case MotionEvent.ACTION_MOVE:
                            if (maximized) return true;
                            float x = e.getRawX() + dx, y = e.getRawY() + dy;
                            setX(Math.max(-getWidth() + dp(getContext(), 90),
                                    Math.min(screenW - dp(getContext(), 90), x)));
                            setY(Math.max(0, Math.min(screenH - taskbarH - dp(getContext(), 36), y)));
                            return true;
                        default: return true;
                    }
                }
            });
            setOnTouchListener((v, e) -> { if (e.getActionMasked() == MotionEvent.ACTION_DOWN) focus(); return false; });

            taskBtn = tv(getContext(), emoji + " " + title, 11, 0xFFFFFFFF);
            taskBtn.setSingleLine(true);
            taskBtn.setEllipsize(TextUtils.TruncateAt.END);
            taskBtn.setMaxWidth(dp(getContext(), 130));
            taskBtn.setGravity(Gravity.CENTER);
            taskBtn.setPadding(dp(getContext(), 10), dp(getContext(), 6), dp(getContext(), 10), dp(getContext(), 6));
            taskBtn.setOnClickListener(v -> {
                if (minimized) restoreAndFocus();
                else if (windows.get(windows.size() - 1) == this) minimize();
                else focus();
            });
        }

        private View winBtn(String s, int bg, int color, Runnable r) {
            TextView b = tv(getContext(), s, 13, color);
            b.setGravity(Gravity.CENTER);
            b.setBackground(rr(bg, 0, getContext()));
            b.setOnClickListener(v -> r.run());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(getContext(), 40), -1);
            b.setLayoutParams(lp);
            return b;
        }

        void addTaskButton() {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.setMargins(dp(getContext(), 2), 0, dp(getContext(), 2), 0);
            taskbarApps.addView(taskBtn, lp);
        }

        void focus() {
            minimized = false;
            setVisibility(VISIBLE);
            bringToFront();
            windows.remove(this); windows.add(this);
            for (ZWindow w : windows)
                w.taskBtn.setBackground(rr(w == this ? 0x40FFFFFF : 0x14FFFFFF, 6, getContext()));
        }

        void restoreAndFocus() { focus(); }

        void minimize() {
            minimized = true;
            setVisibility(GONE);
            taskBtn.setBackground(rr(0x14FFFFFF, 6, getContext()));
        }

        void toggleMax() {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) getLayoutParams();
            if (!maximized) {
                nx = getX(); ny = getY(); nw = lp.width; nh = lp.height;
                lp.width = -1; lp.height = -1;
                setX(0); setY(0); maximized = true;
            } else {
                lp.width = nw; lp.height = nh;
                setX(nx); setY(ny); maximized = false;
            }
            setLayoutParams(lp);
        }

        void applySnap(int zone) {
            int sw = screenW, sh = screenH - taskbarH;
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) getLayoutParams();
            maximized = false;
            switch (zone) {
                case 0: lp.width = sw/2; lp.height = sh;   setX(0);     setY(0); break;
                case 1: lp.width = sw/2; lp.height = sh;   setX(sw/2f); setY(0); break;
                case 2: lp.width = sw/2; lp.height = sh/2; setX(0);     setY(0); break;
                case 3: lp.width = sw/2; lp.height = sh/2; setX(sw/2f); setY(0); break;
                case 4: lp.width = sw/2; lp.height = sh/2; setX(0);     setY(sh/2f); break;
                case 5: lp.width = sw/2; lp.height = sh/2; setX(sw/2f); setY(sh/2f); break;
                case 6: lp.width = -1;   lp.height = -1;   setX(0);     setY(0); maximized = true; break;
            }
            setLayoutParams(lp);
        }

        void close() {
            if (content instanceof WinContent) ((WinContent) content).onClose();
            windowLayer.removeView(this);
            taskbarApps.removeView(taskBtn);
            windows.remove(this);
            if (!windows.isEmpty()) windows.get(windows.size() - 1).focus();
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private static final class GradientDrawableHolder {
        static void apply(View v, Context c) {
            android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
            g.setColor(0xF2162547);
            g.setCornerRadius(dp(c, 14));
            g.setStroke(dp(c, 1), 0x33FFFFFF);
            v.setBackground(g);
        }
        static int dp(Context c, float v) {
            return Math.round(v * c.getResources().getDisplayMetrics().density);
        }
    }
}
