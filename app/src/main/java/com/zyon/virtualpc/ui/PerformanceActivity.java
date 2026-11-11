package com.zyon.virtualpc.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.zyon.virtualpc.R;
import com.zyon.virtualpc.nativebridge.ZyonNative;

public class PerformanceActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_performance);
        TextView tv = findViewById(R.id.tv_perf);
        try {
            long free = ZyonNative.nativeHostFreeRam();
            tv.setText("RAM livre host: " + (free / (1024*1024)) + " MB\nTeto guest: 16384 MB\nCPU: TCG (sem KVM no Android user)");
        } catch (Throwable t) {
            tv.setText("Native indisponível.\nTeto guest: 16384 MB");
        }
    }
}
