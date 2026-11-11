package com.zyon.virtualpc.ui;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.zyon.virtualpc.R;
import com.zyon.virtualpc.nativebridge.ZyonNative;

public class VmConsoleActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private String vmName;
    private SurfaceView surfaceView;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);

        vmName = getIntent().getStringExtra("vm_name");
        if (vmName == null) vmName = "default";

        surfaceView = findViewById(R.id.surface_view);
        statusText = findViewById(R.id.status_text);
        Button btnPower = findViewById(R.id.btn_power);
        Button btnPause = findViewById(R.id.btn_pause);

        surfaceView.getHolder().addCallback(this);
        statusText.setText("VM: " + vmName);

        btnPower.setOnClickListener(v -> {
            try {
                ZyonNative.nativeStartVm(vmName);
                statusText.setText("RUNNING: " + vmName);
            } catch (Throwable t) {
                statusText.setText("Error: " + t.getMessage());
            }
        });

        btnPause.setOnClickListener(v -> {
            try {
                ZyonNative.nativePauseVm(vmName);
                statusText.setText("PAUSED: " + vmName);
            } catch (Throwable ignored) {}
        });
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        try {
            ZyonNative.nativeSetSurface(vmName, holder.getSurface());
        } catch (Throwable ignored) {}
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {}
}
