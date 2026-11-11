package com.zyon.virtualpc.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zyon.virtualpc.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lista apps do celular e permite SALVAR o APK na pasta compartilhada do PC virtual
 * (files/zyon/shared/apks) — acessivel no guest via pasta 9p/shared.
 */
public class HostAppsActivity extends AppCompatActivity {

    static class AppRow {
        String label;
        String pkg;
        Drawable icon;
        Intent launch;
        String apkPath;
    }

    private File apkExportDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_apps);

        apkExportDir = new File(getFilesDir(), "zyon/shared/apks");
        //noinspection ResultOfMethodCallIgnored
        apkExportDir.mkdirs();

        RecyclerView rv = findViewById(R.id.rv_host_apps);
        rv.setLayoutManager(new LinearLayoutManager(this));

        PackageManager pm = getPackageManager();
        List<AppRow> rows = new ArrayList<>();
        for (ApplicationInfo ai : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            Intent launch = pm.getLaunchIntentForPackage(ai.packageName);
            if (launch == null) continue;
            AppRow r = new AppRow();
            r.pkg = ai.packageName;
            r.label = pm.getApplicationLabel(ai).toString();
            r.icon = pm.getApplicationIcon(ai);
            r.launch = launch;
            r.apkPath = ai.sourceDir;
            rows.add(r);
        }
        Collections.sort(rows, (a, b) -> a.label.compareToIgnoreCase(b.label));

        TextView hdr = findViewById(R.id.tv_apps_header);
        hdr.setText(rows.size() + " apps · salvar APK → pasta do PC virtual");

        rv.setAdapter(new RecyclerView.Adapter<VH>() {
            @NonNull
            @Override
            public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_host_app, parent, false);
                return new VH(v);
            }

            @Override
            public void onBindViewHolder(@NonNull VH h, int pos) {
                AppRow r = rows.get(pos);
                h.title.setText(r.label);
                h.sub.setText(r.pkg);
                h.icon.setImageDrawable(r.icon);
                h.itemView.setOnClickListener(v -> {
                    try { startActivity(r.launch); }
                    catch (Exception e) {
                        Toast.makeText(HostAppsActivity.this, "Falha ao abrir", Toast.LENGTH_SHORT).show();
                    }
                });
                h.btnSave.setOnClickListener(v -> saveApkToVmFolder(r));
            }

            @Override
            public int getItemCount() { return rows.size(); }
        });
    }

    private void saveApkToVmFolder(AppRow r) {
        try {
            File src = new File(r.apkPath);
            if (!src.exists()) {
                Toast.makeText(this, "APK não legível (app sistema)", Toast.LENGTH_SHORT).show();
                return;
            }
            String safe = r.pkg.replaceAll("[^a-zA-Z0-9._-]", "_") + ".apk";
            File dst = new File(apkExportDir, safe);
            try (FileChannel in = new FileInputStream(src).getChannel();
                 FileChannel out = new FileOutputStream(dst).getChannel()) {
                out.transferFrom(in, 0, in.size());
            }
            Toast.makeText(this,
                    "Salvo no PC virtual:\nshared/apks/" + safe,
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, sub;
        Button btnSave;
        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.img_app_icon);
            title = v.findViewById(R.id.tv_app_title);
            sub = v.findViewById(R.id.tv_app_pkg);
            btnSave = v.findViewById(R.id.btn_save_apk);
        }
    }
}
