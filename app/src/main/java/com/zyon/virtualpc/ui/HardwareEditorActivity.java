package com.zyon.virtualpc.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zyon.virtualpc.R;

public class HardwareEditorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware_editor);

        Spinner gpu = findViewById(R.id.sp_gpu);
        Spinner nic = findViewById(R.id.sp_nic);
        Spinner audio = findViewById(R.id.sp_audio);
        EditText cores = findViewById(R.id.et_hw_cores);
        EditText ram = findViewById(R.id.et_hw_ram);
        Button save = findViewById(R.id.btn_hw_save);

        gpu.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"virtio-gpu", "qxl", "cirrus", "std"}));
        nic.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"virtio-net", "e1000", "rtl8139"}));
        audio.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"hda", "ac97", "sb16"}));

        String vm = getIntent().getStringExtra("vm_name");
        if (vm == null) vm = "";

        final String vmName = vm;
        save.setOnClickListener(v -> {
            Toast.makeText(this, "Hardware salvo: " + vmName +
                    " CPU=" + cores.getText() + " RAM=" + ram.getText() + "MB", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
