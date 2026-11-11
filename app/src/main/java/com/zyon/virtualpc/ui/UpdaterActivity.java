package com.zyon.virtualpc.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Build;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdaterActivity extends AppCompatActivity {
    private static final String RELEASE_API = "https://api.github.com/repos/ffuxyxfyxhcocyocg-art/ZYON_VIRTUAL_PC_17_WORKFLOW_CORRIGIDO/releases/latest";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private TextView status;
    private ProgressBar progress;
    private String apkUrl;
    private String checksumUrl;
    private long latestVersionCode = -1;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(DesktopApps.dp(this, 22), DesktopApps.dp(this, 24), DesktopApps.dp(this, 22), DesktopApps.dp(this, 22));
        root.setBackgroundColor(0xFF0D1117);
        setContentView(root);

        root.addView(DesktopApps.tv(this, "☁️ Atualizações do ZYON", 21, 0xFFFFFFFF));
        TextView installed = DesktopApps.tv(this, "Versão instalada: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")", 14, 0xFFAAAAAA);
        installed.setPadding(0, DesktopApps.dp(this, 12), 0, DesktopApps.dp(this, 18));
        root.addView(installed);
        status = DesktopApps.tv(this, "Verificando atualizações...", 15, 0xFFE6EDF3);
        root.addView(status);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        progress.setVisibility(ProgressBar.GONE);
        root.addView(progress, new LinearLayout.LayoutParams(-1, DesktopApps.dp(this, 5)));
        addAction(root, "🔎  Verificar novamente", this::checkRelease);
        addAction(root, "⬇️  Baixar e instalar atualização", this::downloadAndInstall);
        TextView note = DesktopApps.tv(this,
            "O ZYON verifica automaticamente ao abrir esta tela. Quando houver versão nova, baixe e confirme a instalação solicitada pelo Android. O sistema Android não permite instalação silenciosa normal por um app comum.",
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
            try {
                JSONObject release = new JSONObject(readUrl(RELEASE_API));
                if (release.optBoolean("draft") || release.optBoolean("prerelease"))
                    throw new Exception("A última Release está marcada como rascunho/pré-lançamento.");
                String tag = release.optString("tag_name", "");
                long remoteCode = versionCodeFromTag(tag);
                String foundApk = null, foundSha = null;
                JSONArray assets = release.optJSONArray("assets");
                if (assets != null) for (int i = 0; i < assets.length(); i++) {
                    JSONObject asset = assets.getJSONObject(i);
                    String name = asset.optString("name", "");
                    String url = asset.optString("browser_download_url", "");
                    if (name.toLowerCase(Locale.ROOT).endsWith(".apk")) foundApk = url;
                    if (name.toLowerCase(Locale.ROOT).endsWith(".apk.sha256")) foundSha = url;
                }
                if (foundApk == null || foundApk.isEmpty()) throw new Exception("Release encontrada, mas não contém APK.");
                if (foundSha == null || foundSha.isEmpty()) throw new Exception("Release sem arquivo SHA-256. Não vou instalar um APK sem validação.");
                final String fApk = foundApk, fSha = foundSha;
                final long fCode = remoteCode;
                main.post(() -> {
                    apkUrl = fApk;
                    checksumUrl = fSha;
                    latestVersionCode = fCode;
                    if (remoteCode <= BuildConfig.VERSION_CODE) {
                        setBusy(false, "ZYON atualizado. Versão mais recente: " + tag + ". Nenhuma atualização necessária.");
                    } else {
                        setBusy(false, "Atualização disponível: " + tag + ". Toque em Baixar e instalar.");
                    }
                });
            } catch (Exception e) {
                main.post(() -> setBusy(false, "Falha ao verificar: " + e.getMessage()));
            }
        });
    }

    private void downloadAndInstall() {
        final String url = apkUrl;
        final String shaUrl = checksumUrl;
        final long expectedCode = latestVersionCode;
        if (url == null || shaUrl == null || expectedCode <= BuildConfig.VERSION_CODE) {
            Toast.makeText(this, "Não há atualização nova validada. Verifique novamente.", Toast.LENGTH_LONG).show();
            checkRelease();
            return;
        }
        setBusy(true, "Baixando APK e checksum oficial...");
        executor.execute(() -> {
            try {
                File apk = new File(getCacheDir(), "zyon-update.apk");
                downloadToFile(url, apk);
                String checksumText = readUrl(shaUrl).trim();
                String expectedSha = checksumText.split("\\s+")[0].toLowerCase(Locale.ROOT);
                if (!expectedSha.matches("[a-f0-9]{64}")) throw new Exception("Arquivo SHA-256 inválido.");
                String actualSha = sha256(apk);
                if (!MessageDigest.isEqual(expectedSha.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                        actualSha.getBytes(java.nio.charset.StandardCharsets.US_ASCII))) {
                    throw new Exception("SHA-256 não confere. APK rejeitado por segurança.");
                }
                PackageInfo archive = getPackageManager().getPackageArchiveInfo(apk.getAbsolutePath(), 0);
                if (archive == null || !BuildConfig.APPLICATION_ID.equals(archive.packageName))
                    throw new Exception("O APK baixado não pertence ao ZYON.");
                long apkCode = Build.VERSION.SDK_INT >= 28 ? archive.getLongVersionCode() : archive.versionCode;
                if (apkCode != expectedCode || apkCode <= BuildConfig.VERSION_CODE)
                    throw new Exception("Versão do APK não corresponde à versão publicada.");
                main.post(() -> installApk(apk));
            } catch (Exception e) {
                main.post(() -> setBusy(false, "Atualização cancelada: " + e.getMessage()));
            }
        });
    }

    private static long versionCodeFromTag(String tag) throws Exception {
        String value = tag.startsWith("v") || tag.startsWith("V") ? tag.substring(1) : tag;
        String[] parts = value.split("[.-]");
        if (parts.length == 0) throw new Exception("Tag de versão inválida: " + tag);
        try { return Long.parseLong(parts[0]); }
        catch (NumberFormatException e) { throw new Exception("Tag deve começar com o versionCode, exemplo v20.0.0."); }
    }

    private String readUrl(String address) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(address).openConnection();
        try {
            c.setConnectTimeout(15000); c.setReadTimeout(30000);
            c.setRequestProperty("Accept", "application/vnd.github+json");
            c.setRequestProperty("User-Agent", "ZYON-Virtual-PC-Updater");
            int code = c.getResponseCode();
            if (code != 200) throw new Exception("GitHub respondeu HTTP " + code);
            try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] b = new byte[4096]; int n;
                while ((n = in.read(b)) != -1) out.write(b, 0, n);
                return out.toString("UTF-8");
            }
        } finally { c.disconnect(); }
    }

    private void downloadToFile(String address, File file) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(address).openConnection();
        try {
            c.setConnectTimeout(15000); c.setReadTimeout(60000);
            c.setRequestProperty("User-Agent", "ZYON-Virtual-PC-Updater");
            if (c.getResponseCode() != 200) throw new Exception("Download HTTP " + c.getResponseCode());
            try (InputStream in = c.getInputStream(); FileOutputStream out = new FileOutputStream(file)) {
                byte[] b = new byte[8192]; int n;
                while ((n = in.read(b)) != -1) out.write(b, 0, n);
                out.flush();
            }
            if (file.length() < 100 * 1024) throw new Exception("APK baixado parece incompleto.");
        } finally { c.disconnect(); }
    }

    private static String sha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new FileInputStream(file)) {
            byte[] b = new byte[8192]; int n;
            while ((n = in.read(b)) != -1) digest.update(b, 0, n);
        }
        StringBuilder result = new StringBuilder();
        for (byte value : digest.digest()) result.append(String.format(Locale.ROOT, "%02x", value & 0xff));
        return result.toString();
    }

    private void installApk(File apk) {
        try {
            PackageInstaller installer = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int id = installer.createSession(params);
            try (PackageInstaller.Session session = installer.openSession(id); InputStream in = new FileInputStream(apk)) {
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
            setBusy(false, "APK validado. Confirme a instalação na tela do Android.");
        } catch (Exception e) {
            setBusy(false, "Não foi possível iniciar a instalação: " + e.getMessage());
        }
    }

    private void setBusy(boolean busy, String message) {
        status.setText(message);
        progress.setVisibility(busy ? ProgressBar.VISIBLE : ProgressBar.GONE);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
