package com.zyon.virtualpc.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.zyon.virtualpc.R;

public class LogsActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_logs);
        TextView tv = findViewById(R.id.tv_logs);
        tv.setText("ZYON VIRTUAL PC 16\nLogs do QEMU e do host aparecem no logcat (tag ZyonPC).\n\nUse: adb logcat -s ZyonPC ZyonApp");
    }
}
