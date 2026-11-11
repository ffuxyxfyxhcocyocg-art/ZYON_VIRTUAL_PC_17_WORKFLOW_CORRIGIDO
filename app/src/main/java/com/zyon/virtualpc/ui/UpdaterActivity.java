package com.zyon.virtualpc.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zyon.virtualpc.BuildConfig;
import com.zyon.virtualpc.ui.DesktopApps;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdaterActivity extends AppCompatActivity {
    private static final String RELEASE_API = "https://api.github.com/repos/ffuxyxfyxhcocyocg-art/ZYON_VIRTUAL_PC_17_WORKFLOW_CORRIGIDO/releases/latest";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private TextView status;
    private TextView version;
    private ProgressBar progress;
    private String apkUrl;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(DesktopApps.dp(this, 22), DesktopApps.dp(this, 24), DesktopApps.dp(this, 22), DesktopApps.dp(this, 22));
        root.setBackgroundColor(0xFF0D1117);
        setContentView(root);

        TextView title = DesktopApps.tv(this, "☁️ Atualizações do ZYON", 21, 0xFFFFFFFF);
        root.addView(title);
        version = DesktopApps.tv(this, "Versão instalada: " + BuildConfig.VERSION_NAME, 14, 0xFFAAAAAA);
        version.setPadding(0, DesktopApps.dp(this, 12), 0, DesktopApps.dp(this, 18));
        root.addView(version);
        status = DesktopApps.tv(this, "Pronto para verificar o GitHub.", 15, 0xFFE6EDF3);
        root.addView(status);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        progress.setVisibility(ProgressBar.GONE);
        root.addView(progress, new LinearLayout.LayoutParams(-1, DesktopApps.dp(this, 5)));
        addAction(root, "🔎  Verificar atualizações", this::checkRelease);
        addAction(root, "⬇️  Baixar e instalar atualização", this::downloadAndInstall);
        TextView note = DesktopApps.tv(this,
            "As versões precisam ser publicadas em Releases no GitHub e assinadas com a mesma chave do aplicativo. O Android sempre pede confirmação para instalar. Seus dados do app não são apagados por uma atualização normal.",
            12, 0xFF9AA4AF);
        note.setPadding(0, DesktopApps.dp(this, 20), 0, 0);
        root.addView(note);
        checkRelease();
    }

    private void addAction(LinearLayout root, String label, Runnable action) {
        TextView item = DesktopApps.tv(this, label, 16, 0xFFFFFFFF);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(DesktopApps.dp(this, 4), DesktopApps.dp(this, 18), 0, DesktopApps.dp(this, 18));
        item.setOnClickListener(v -> action.run());
        root.addView(item);
    }

    private void checkRelease() {
        setBusy(true, "Consultando a versão mais recente no GitHub...");
        executor.execute(() -> {
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) new URL(RELEASE_API).openConnection();
                c.setConnectTimeout(12000);
                c.setReadTimeout(12000);
                c.setRequestProperty("Accept", "application/vnd.github+json");
                c.setRequestProperty("User-Agent", "ZYON-Virtual-PC-Updater");
                int code = c.getResponseCode();
                if (code != 200) throw new Exception("GitHub respondeu HTTP " + code + ". Ainda não há uma Release publicada?");
                StringBuilder body = new StringBuilder();
                try (InputStream in = c.getInputStream()) {
                    byte[] b = new byte[4096]; int n;
                    while ((n = in.read(b)) != -1) body.append(new String(b, 0, n, java.nio.charset.StandardCharsets.UTF_8));
                }
                JSONObject release = new JSONObject(body.toString());
                String tag = release.optString("tag_name", "versão desconhecida");
                String url = "";
                org.json.JSONArray assets = release.optJSONArray("assets");
                if (assets != null) for (int i=0; i<assets.length(); i++) {
                    JSONObject asset = assets.getJSONObject(i);
                    if (asset.optString("name").toLowerCase().endsWith(".apk")) { url = asset.optString("browser_download_url"); break; }
                }
                final String foundUrl = url;
                main.post(() -> {
                    apkUrl = foundUrl;
                    setBusy(false, foundUrl.isEmpty()
                        ? "Release " + tag + " encontrada, mas sem APK anexado."
                        : "Nova versão disponível: " + tag + ". Toque em Baixar e instalar.");
                });
            } catch (Exception e) {
                main.post(() -> setBusy(false, "Não foi possível verificar: " + e.getMessage()));
            } finally { if (c != null) c.disconnect(); }
        });
    }

    private void downloadAndInstall() {
        final String url = apkUrl;
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "Verifique se existe uma Release com APK.", Toast.LENGTH_LONG).show();
            checkRelease(); return;
        }
        setBusy(true, "Baixando APK oficial...");
        executor.execute(() -> {
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) new URL(url).openConnection();
                c.setConnectTimeout(15000); c.setReadTimeout(30000);
                c.setRequestProperty("User-Agent", "ZYON-Virtual-PC-Updater");
                if (c.getResponseCode() != 200) throw new Exception("Falha HTTP " + c.getResponseCode());
                File apk = new File(getCacheDir(), "zyon-update.apk");
                try (InputStream in = c.getInputStream(); FileOutputStream out = new FileOutputStream(apk)) {
                    byte[] b = new byte[8192]; int n;
                    while ((n = in.read(b)) != -1) out.write(b, 0, n);
                }
                if (apk.length() < 1024 * 100) throw new Exception("APK baixado parece incompleto.");
                main.post(() -> installApk(apk));
            } catch (Exception e) {
                main.post(() -> setBusy(false, "Erro no download: " + e.getMessage()));
            } finally { if (c != null) c.disconnect(); }
        });
    }

    private void installApk(File apk) {
        try {
            PackageInstaller installer = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int id = installer.createSession(params);
            try (PackageInstaller.Session session = installer.openSession(id); InputStream in = new java.io.FileInputStream(apk)) {
                try (java.io.OutputStream out = session.openWrite("zyon-update.apk", 0, apk.length())) {
                    byte[] b = new byte[8192]; int n;
                    while ((n = in.read(b)) != -1) out.write(b, 0, n);
                    session.fsync(out);
                }
                Intent result = new Intent(this, UpdaterActivity.class);
                PendingIntent pending = PendingIntent.getActivity(this, id, result,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                session.commit(pending.getIntentSender());
            }
            setBusy(false, "APK pronto. Confirme a instalação na tela do Android.");
        } catch (Exception e) {
            setBusy(false, "Não foi possível iniciar a instalação: " + e.getMessage());
        }
    }

    private void setBusy(boolean busy, String message) {
        status.setText(message);
        progress.setVisibility(busy ? ProgressBar.VISIBLE : ProgressBar.GONE);
    }

    @Override protected void onDestroy() { super.onDestroy(); executor.shutdownNow(); }
}
