package com.zyon.virtualpc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.zyon.virtualpc.R;
import com.zyon.virtualpc.nativebridge.ZyonNative;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView versionText = findViewById(R.id.version_text);
        try {
            versionText.setText(ZyonNative.nativeVersion());
        } catch (Throwable t) {
            versionText.setText("ZYON VIRTUAL PC 16");
        }

        RecyclerView vmList = findViewById(R.id.vm_list);
        vmList.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_create_vm).setOnClickListener(v ->
                startActivity(new Intent(this, CreateVmActivity.class)));
        findViewById(R.id.btn_settings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.btn_logs).setOnClickListener(v ->
                startActivity(new Intent(this, LogsActivity.class)));
        findViewById(R.id.btn_perf).setOnClickListener(v ->
                startActivity(new Intent(this, PerformanceActivity.class)));
        findViewById(R.id.btn_disks).setOnClickListener(v ->
                startActivity(new Intent(this, DiskManagerActivity.class)));
        findViewById(R.id.btn_host_apps).setOnClickListener(v ->
                startActivity(new Intent(this, HostAppsActivity.class)));
        findViewById(R.id.btn_snaps).setOnClickListener(v ->
                startActivity(new Intent(this, SnapshotsActivity.class)));
    }
}
