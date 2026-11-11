package com.zyon.virtualpc.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.zyon.virtualpc.R;

public class DiskManagerActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_disks);
        Spinner fmt = findViewById(R.id.sp_disk_fmt);
        fmt.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
            new String[]{"qcow2", "raw", "vmdk", "vdi", "vhdx"}));
        EditText size = findViewById(R.id.et_disk_size);
        size.setText("32");
        findViewById(R.id.btn_disk_create).setOnClickListener(v ->
            Toast.makeText(this, "Criar disco " + size.getText() + "GB " + fmt.getSelectedItem(), Toast.LENGTH_SHORT).show());
    }
}
