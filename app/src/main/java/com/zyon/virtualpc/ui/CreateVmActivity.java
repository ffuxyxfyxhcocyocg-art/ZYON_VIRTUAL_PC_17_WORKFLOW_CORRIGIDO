package com.zyon.virtualpc.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zyon.virtualpc.R;
import com.zyon.virtualpc.nativebridge.ZyonNative;

import java.io.File;

public class CreateVmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_vm);

        EditText nameEt = findViewById(R.id.et_name);
        Spinner archSp = findViewById(R.id.sp_arch);
        EditText coresEt = findViewById(R.id.et_cores);
        EditText ramEt = findViewById(R.id.et_ram_mb);
        Button createBtn = findViewById(R.id.btn_create);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.arch_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        archSp.setAdapter(adapter);

        coresEt.setText("2");
        ramEt.setText("2048");

        createBtn.setOnClickListener(v -> {
            String name = nameEt.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Nome obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }
            int arch = archSp.getSelectedItemPosition(); // 0=x86_64, 1=arm64
            int cores = Integer.parseInt(coresEt.getText().toString());
            long ramMb = Long.parseLong(ramEt.getText().toString());
            if (ramMb > 16384) ramMb = 16384; // 16 GB hard cap

            File work = new File(getFilesDir(), "zyon/vms/" + name);
            work.mkdirs();

            try {
                String result = ZyonNative.nativeCreateVm(name, arch, cores, ramMb, work.getAbsolutePath());
                Toast.makeText(this, "VM criada: " + result, Toast.LENGTH_SHORT).show();
                finish();
            } catch (Throwable t) {
                Toast.makeText(this, "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
