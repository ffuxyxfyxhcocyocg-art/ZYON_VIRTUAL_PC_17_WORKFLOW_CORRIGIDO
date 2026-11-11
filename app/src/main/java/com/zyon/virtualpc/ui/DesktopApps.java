package com.zyon.virtualpc.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.StatFs;
import android.os.SystemClock;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.zyon.virtualpc.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Conteúdo das janelas do desktop. */
final class DesktopApps {
    private DesktopApps() {}

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    static TextView tv(Context c, String s, float sp, int color) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        return t;
    }

    static GradientDrawable rr(int fill, float radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    static String fmt(long b) {
        if (b < 1024) return b + " B";
        double v = b / 1024.0;
        String[] u = {"KB", "MB", "GB", "TB"};
        int i = 0;
        while (v >= 1024 && i < u.length - 1) { v /= 1024; i++; }
        return String.format(Locale.US, "%.1f %s", v, u[i]);
    }

    // ───────────────────────── Gerenciador de Tarefas ─────────────────────────
    static final class TaskManagerView extends LinearLayout implements WinContent {
        private final DesktopViews.GraphView cpuG, memG, dskG;
        private final TextView cpuT, memT, dskT, info;
        private final Handler h = new Handler(Looper.getMainLooper());
        private final int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        private long lastCpu = Process.getElapsedCpuTime();
        private long lastWall = SystemClock.elapsedRealtime();
        private boolean running = true;
        private final long startWall = SystemClock.elapsedRealtime();

        private final Runnable tick = new Runnable() {
            @Override public void run() {
                if (!running) return;
                update();
                h.postDelayed(this, 1000);
            }
        };

        TaskManagerView(Context c) {
            super(c);
            setOrientation(VERTICAL);
            setPadding(dp(c, 12), dp(c, 8), dp(c, 12), dp(c, 8));
            ScrollView sv = new ScrollView(c);
            LinearLayout col = new LinearLayout(c);
            col.setOrientation(VERTICAL);
            sv.addView(col);
            addView(sv, new LayoutParams(-1, -1));

            cpuG = new DesktopViews.GraphView(c, 0xFF4FC3F7);
            memG = new DesktopViews.GraphView(c, 0xFFB388FF);
            dskG = new DesktopViews.GraphView(c, 0xFF69F0AE);
            cpuT = tv(c, "0%", 13, 0xFFFFFFFF);
            memT = tv(c, "-", 13, 0xFFFFFFFF);
            dskT = tv(c, "-", 13, 0xFFFFFFFF);
            metric(col, "CPU (processo Zyon)", cpuG, cpuT);
            metric(col, "Memória do aparelho", memG, memT);
            metric(col, "Armazenamento (PC virtual)", dskG, dskT);
            info = tv(c, "", 12, 0xFFB0BEC5);
            info.setPadding(0, dp(c, 10), 0, 0);
            col.addView(info);
            update();
            h.postDelayed(tick, 1000);
        }

        private void metric(LinearLayout col, String name, DesktopViews.GraphView g, TextView val) {
            Context c = getContext();
            LinearLayout row = new LinearLayout(c);
            row.setOrientation(HORIZONTAL);
            TextView n = tv(c, name, 13, 0xFFE0E0E0);
            n.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(n, new LayoutParams(0, -2, 1f));
            row.addView(val);
            row.setPadding(0, dp(c, 8), 0, dp(c, 4));
            col.addView(row);
            col.addView(g, new LayoutParams(-1, dp(c, 64)));
        }

        private void update() {
            Context c = getContext();
            long cpu = Process.getElapsedCpuTime();
            long wall = SystemClock.elapsedRealtime();
            long dW = wall - lastWall;
            float pct = dW > 0 ? (cpu - lastCpu) * 100f / (dW * cores) : 0f;
            lastCpu = cpu;
            lastWall = wall;
            cpuG.push(pct);
            cpuT.setText(String.format(Locale.US, "%.0f%%", Math.min(100f, pct)));

            ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            long used = mi.totalMem - mi.availMem;
            float mp = mi.totalMem > 0 ? used * 100f / mi.totalMem : 0f;
            memG.push(mp);
            memT.setText(fmt(used) + " / " + fmt(mi.totalMem) + String.format(Locale.US, " (%.0f%%)", mp));

            try {
                StatFs fs = new StatFs(c.getFilesDir().getPath());
                long tot = fs.getTotalBytes(), free = fs.getAvailableBytes();
                float dp = tot > 0 ? (tot - free) * 100f / tot : 0f;
                dskG.push(dp);
                dskT.setText(fmt(tot - free) + " / " + fmt(tot));
            } catch (Exception ignored) {}

            long up = (SystemClock.elapsedRealtime() - startWall) / 1000;
            info.setText("Núcleos: " + cores + "    Threads: " + Thread.activeCount()
                    + "\nModelo: " + Build.MANUFACTURER + " " + Build.MODEL
                    + "\nTempo de atividade: " + String.format(Locale.US, "%d:%02d:%02d", up / 3600, (up / 60) % 60, up % 60));
        }

        @Override public void onClose() { running = false; h.removeCallbacksAndMessages(null); }
    }

    // ───────────────────────── Explorador de Arquivos ─────────────────────────
    static final class FileExplorerView extends LinearLayout implements WinContent {
        private final File root;
        private File cur;
        private final TextView path;
        private final LinearLayout list;

        FileExplorerView(Context c, File root) {
            super(c);
            this.root = root;
            this.cur = root;
            //noinspection ResultOfMethodCallIgnored
            root.mkdirs();
            setOrientation(VERTICAL);

            LinearLayout bar = new LinearLayout(c);
            bar.setOrientation(HORIZONTAL);
            bar.setGravity(Gravity.CENTER_VERTICAL);
            bar.setPadding(dp(c, 8), dp(c, 6), dp(c, 8), dp(c, 6));
            TextView back = chip(c, "⬅");
            TextView mk = chip(c, "➕ Pasta");
            path = tv(c, "", 12, 0xFFB3E5FC);
            path.setPadding(dp(c, 10), 0, 0, 0);
            path.setSingleLine(true);
            bar.addView(back);
            bar.addView(mk);
            bar.addView(path, new LayoutParams(0, -2, 1f));
            addView(bar);

            ScrollView sv = new ScrollView(c);
            list = new LinearLayout(c);
            list.setOrientation(VERTICAL);
            sv.addView(list);
            addView(sv, new LayoutParams(-1, 0, 1f));

            back.setOnClickListener(v -> {
                File p = cur.getParentFile();
                if (!cur.equals(root) && p != null) { cur = p; refresh(); }
            });
            mk.setOnClickListener(v -> newFolder());
            refresh();
        }

        private TextView chip(Context c, String s) {
            TextView t = tv(c, s, 13, 0xFFFFFFFF);
            t.setPadding(dp(c, 12), dp(c, 8), dp(c, 12), dp(c, 8));
            t.setBackground(rr(0x33FFFFFF, 8, c));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.rightMargin = dp(c, 6);
            t.setLayoutParams(lp);
            return t;
        }

        private void refresh() {
            Context c = getContext();
            String rel = cur.equals(root) ? "" : cur.getAbsolutePath().substring(root.getAbsolutePath().length());
            path.setText("PC virtual > Arquivos" + rel.replace("/", " > "));
            list.removeAllViews();
            File[] fs = cur.listFiles();
            if (fs == null || fs.length == 0) {
                TextView e = tv(c, "Esta pasta está vazia.", 13, 0xFF90A4AE);
                e.setPadding(dp(c, 16), dp(c, 16), 0, 0);
                list.addView(e);
                return;
            }
            Arrays.sort(fs, (a, b) -> {
                if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });
            for (File f : fs) {
                LinearLayout row = new LinearLayout(c);
                row.setOrientation(HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(c, 14), dp(c, 10), dp(c, 14), dp(c, 10));
                TextView ic = tv(c, f.isDirectory() ? "📁" : "📄", 20, 0xFFFFFFFF);
                LinearLayout txt = new LinearLayout(c);
                txt.setOrientation(VERTICAL);
                txt.setPadding(dp(c, 12), 0, 0, 0);
                txt.addView(tv(c, f.getName(), 14, 0xFFFFFFFF));
                String sub = f.isDirectory()
                        ? (f.list() == null ? 0 : f.list().length) + " itens"
                        : fmt(f.length()) + "  ·  " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(f.lastModified()));
                txt.addView(tv(c, sub, 11, 0xFF90A4AE));
                row.addView(ic);
                row.addView(txt);
                row.setOnClickListener(v -> {
                    if (f.isDirectory()) { cur = f; refresh(); }
                    else Toast.makeText(c, f.getName() + " — " + fmt(f.length()), Toast.LENGTH_SHORT).show();
                });
                row.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(c)
                            .setTitle("Excluir \"" + f.getName() + "\"?")
                            .setPositiveButton("Excluir", (d, w) -> { deleteRec(f); refresh(); })
                            .setNegativeButton("Cancelar", null)
                            .show();
                    return true;
                });
                list.addView(row);
            }
        }

        private void newFolder() {
            Context c = getContext();
            EditText et = new EditText(c);
            et.setHint("Nome da pasta");
            et.setInputType(InputType.TYPE_CLASS_TEXT);
            new AlertDialog.Builder(c)
                    .setTitle("Nova pasta")
                    .setView(et)
                    .setPositiveButton("Criar", (d, w) -> {
                        String n = et.getText().toString().trim().replaceAll("[/\\\\:*?\"<>|]", "_");
                        if (!n.isEmpty()) {
                            //noinspection ResultOfMethodCallIgnored
                            new File(cur, n).mkdirs();
                            refresh();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }

        private static void deleteRec(File f) {
            File[] k = f.listFiles();
            if (k != null) for (File x : k) deleteRec(x);
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    // ───────────────────────── Bloco de Notas ─────────────────────────
    static final class NotepadView extends LinearLayout implements WinContent {
        private final EditText et;
        private final File file;

        NotepadView(Context c, File docsDir) {
            super(c);
            //noinspection ResultOfMethodCallIgnored
            docsDir.mkdirs();
            file = new File(docsDir, "notas.txt");
            et = new EditText(c);
            et.setBackground(null);
            et.setTextColor(0xFFFFFFFF);
            et.setHint("Escreva aqui… (salva sozinho)");
            et.setHintTextColor(0xFF607D8B);
            et.setGravity(Gravity.TOP | Gravity.START);
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            et.setPadding(dp(c, 12), dp(c, 10), dp(c, 12), dp(c, 10));
            addView(et, new LayoutParams(-1, -1));
            if (file.exists()) {
                try (FileInputStream in = new FileInputStream(file)) {
                    byte[] b = new byte[(int) Math.min(file.length(), 1_000_000)];
                    int n = in.read(b);
                    et.setText(new String(b, 0, Math.max(n, 0), StandardCharsets.UTF_8));
                } catch (Exception ignored) {}
            }
        }

        @Override public void onSave() {
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(et.getText().toString().getBytes(StandardCharsets.UTF_8));
            } catch (Exception ignored) {}
        }

        @Override public void onClose() { onSave(); }
    }

    // ───────────────────────── Sobre este PC ─────────────────────────
    static final class AboutView extends ScrollView implements WinContent {
        AboutView(Context c) {
            super(c);
            ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            boolean q = AppCatalog.qemuPresent(c);
            String s = "ZYON VIRTUAL PC " + BuildConfig.VERSION_NAME
                    + "\n\nAparelho:  " + Build.MANUFACTURER + " " + Build.MODEL
                    + "\nAndroid:  " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")"
                    + "\nArquitetura:  " + Build.SUPPORTED_ABIS[0]
                    + "\nNúcleos:  " + Runtime.getRuntime().availableProcessors()
                    + "\nRAM total:  " + fmt(mi.totalMem)
                    + "\n\nAcesso:  livre (sem Key/licença)"
                    + "\n\nMotor QEMU:  " + (q ? "presente" : "não empacotado")
                    + (q ? "" : "\nPra rodar Windows/Linux de verdade falta colocar os binários do QEMU\nem jniLibs/arm64-v8a, o firmware (OVMF/SeaBIOS) e uma ISO.\nSem isso o PC virtual roda como desktop com seus apps do celular.");
            TextView t = tv(c, s, 13, 0xFFE0E0E0);
            t.setTypeface(Typeface.MONOSPACE);
            t.setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14));
            addView(t);
        }
    }

    // ──────────────────────────────── Lixeira ────────────────────────────────
    static final class TrashView extends LinearLayout implements WinContent {
        private final File trashDir;
        private LinearLayout list;

        TrashView(Context c) {
            super(c);
            trashDir = new File(c.getFilesDir(), "zyon/trash");
            //noinspection ResultOfMethodCallIgnored
            trashDir.mkdirs();
            setOrientation(VERTICAL);
            setPadding(dp(c, 12), dp(c, 8), dp(c, 12), dp(c, 8));
            build(c);
        }

        private void build(Context c) {
            LinearLayout header = new LinearLayout(c);
            header.setOrientation(HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);
            header.setPadding(0, 0, 0, dp(c, 8));
            TextView t = tv(c, "🗑️  Lixeira", 14, 0xFFFFFFFF);
            t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            header.addView(t, new LayoutParams(0, -2, 1f));
            TextView empty = tv(c, "Esvaziar", 12, 0xFFFF5252);
            empty.setPadding(dp(c, 10), dp(c, 4), dp(c, 10), dp(c, 4));
            empty.setBackground(rr(0x22FF5252, 8, c));
            empty.setOnClickListener(v -> emptyTrash(c));
            header.addView(empty);
            addView(header, new LayoutParams(-1, -2));

            android.view.View sep = new android.view.View(c);
            sep.setBackgroundColor(0x22FFFFFF);
            LayoutParams slp = new LayoutParams(-1, 1);
            slp.bottomMargin = dp(c, 6);
            addView(sep, slp);

            ScrollView sv = new ScrollView(c);
            list = new LinearLayout(c);
            list.setOrientation(VERTICAL);
            sv.addView(list);
            addView(sv, new LayoutParams(-1, 0, 1f));
            refresh(c);
        }

        private void refresh(Context c) {
            list.removeAllViews();
            File[] files = trashDir.listFiles();
            if (files == null || files.length == 0) {
                TextView empty = tv(c, "Lixeira vazia", 12, 0xFF90A4AE);
                empty.setGravity(Gravity.CENTER);
                empty.setPadding(0, dp(c, 20), 0, 0);
                list.addView(empty);
                return;
            }
            for (File f : files) {
                LinearLayout row = new LinearLayout(c);
                row.setOrientation(HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(c, 8), dp(c, 8), dp(c, 8), dp(c, 8));
                row.setBackground(rr(0x22FFFFFF, 6, c));
                LayoutParams rlp = new LayoutParams(-1, -2);
                rlp.bottomMargin = dp(c, 3);

                TextView name = tv(c, (f.isDirectory() ? "📁 " : "📄 ") + f.getName(), 12, 0xFFCCCCCC);
                row.addView(name, new LayoutParams(0, -2, 1f));

                TextView restore = tv(c, "↩", 16, 0xFF4FC3F7);
                restore.setPadding(dp(c, 8), 0, dp(c, 4), 0);
                restore.setOnClickListener(v -> {
                    File dest = new File(c.getFilesDir(), "zyon/shared/" + f.getName());
                    if (f.renameTo(dest)) refresh(c);
                });
                row.addView(restore);

                TextView del = tv(c, "✕", 14, 0xFFFF5252);
                del.setPadding(dp(c, 4), 0, 0, 0);
                del.setOnClickListener(v -> { deleteRecursive(f); refresh(c); });
                row.addView(del);

                list.addView(row, rlp);
            }
        }

        private void emptyTrash(Context c) {
            File[] files = trashDir.listFiles();
            if (files != null) for (File f : files) deleteRecursive(f);
            refresh(c);
            android.widget.Toast.makeText(c, "Lixeira esvaziada", android.widget.Toast.LENGTH_SHORT).show();
        }

        private void deleteRecursive(File f) {
            if (f.isDirectory()) { File[] ch = f.listFiles(); if (ch != null) for (File c2 : ch) deleteRecursive(c2); }
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }

        @Override public void onSave() {}
        @Override public void onClose() {}
    }

    // ───────────────────────── Apps do celular (grade) ─────────────────────────
    static final class AppsGridView extends ScrollView implements WinContent {
        AppsGridView(Context c, List<AppCatalog.Entry> apps, int cols, Consumer<AppCatalog.Entry> launch) {
            super(c);
            GridLayout g = new GridLayout(c);
            g.setColumnCount(cols);
            g.setPadding(dp(c, 8), dp(c, 8), dp(c, 8), dp(c, 8));
            for (AppCatalog.Entry e : apps) {
                LinearLayout cell = new LinearLayout(c);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setGravity(Gravity.CENTER_HORIZONTAL);
                cell.setPadding(dp(c, 4), dp(c, 8), dp(c, 4), dp(c, 8));
                ImageView iv = new ImageView(c);
                iv.setImageDrawable(e.icon);
                cell.addView(iv, new LinearLayout.LayoutParams(dp(c, 44), dp(c, 44)));
                TextView l = tv(c, e.label, 11, 0xFFFFFFFF);
                l.setGravity(Gravity.CENTER);
                l.setMaxLines(2);
                l.setEllipsize(android.text.TextUtils.TruncateAt.END);
                cell.addView(l, new LinearLayout.LayoutParams(dp(c, 76), ViewGroup.LayoutParams.WRAP_CONTENT));
                cell.setOnClickListener(v -> launch.accept(e));
                g.addView(cell);
            }
            addView(g);
        }
    }

    /** Alias para compatibilidade com DesktopActivity */
    static FileExplorerView FileManagerView(android.content.Context c, java.io.File root) {
        return new FileExplorerView(c, root);
    }
}
