package com.zyon.virtualpc.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.zyon.virtualpc.R;

public class SnapshotsActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_snapshots);
        String vm = getIntent().getStringExtra("vm_name");
        if (vm == null) vm = "";
        EditText name = findViewById(R.id.et_snap_name);
        Button create = findViewById(R.id.btn_snap_create);
        Button restore = findViewById(R.id.btn_snap_restore);
        final String vmName = vm;
        create.setOnClickListener(v ->
            Toast.makeText(this, "Snapshot: " + name.getText() + " @ " + vmName, Toast.LENGTH_SHORT).show());
        restore.setOnClickListener(v ->
            Toast.makeText(this, "Restore snapshot @ " + vmName, Toast.LENGTH_SHORT).show());
    }
}
