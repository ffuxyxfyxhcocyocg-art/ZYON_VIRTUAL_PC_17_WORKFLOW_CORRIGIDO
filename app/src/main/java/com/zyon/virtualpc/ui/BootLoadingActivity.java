package com.zyon.virtualpc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zyon.virtualpc.R;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Boot: revalida licença, indexa apps do celular, prepara pastas e abre o DESKTOP do PC virtual. */
public class BootLoadingActivity extends AppCompatActivity {

    private TextView status;
    private TextView detail;
    private ProgressBar bar;
    private final Handler main = new Handler(Looper.getMainLooper());
    private ExecutorService ex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot_loading);
        status = findViewById(R.id.tv_boot_status);
        detail = findViewById(R.id.tv_boot_detail);
        bar = findViewById(R.id.progress_boot);
        bar.setIndeterminate(false);
        bar.setMax(100);

        ex = Executors.newSingleThreadExecutor();
        ex.execute(this::bootSequence);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ex != null) ex.shutdownNow();
    }

    private void bootSequence() {
        ui("Núcleo", "Inicializando ZYON…", 6);
        sleep(250);

        ui("Acesso", "Inicialização sem sistema de licença ou chave", 15);
        sleep(200);

        ui("Pastas do PC virtual", "Criando Documentos / Downloads / Imagens…", 35);
        File shared = new File(getFilesDir(), "zyon/shared");
        for (String d : new String[]{"Documentos", "Downloads", "Imagens", "apks"}) {
            //noinspection ResultOfMethodCallIgnored
            new File(shared, d).mkdirs();
        }
        sleep(200);

        ui("Apps do celular", "Indexando aplicativos…", 55);
        int n = AppCatalog.get(this).size();
        ui("Apps do celular", n + " aplicativos prontos no menu Iniciar", 78);
        sleep(250);

        ui("Motor de virtualização", AppCatalog.qemuPresent(this)
                ? "QEMU encontrado" : "QEMU não empacotado — modo desktop", 92);
        sleep(250);

        ui("Pronto", "Abrindo o PC virtual…", 100);
        // post startup notifications so they appear in the notification center
        NotificationCenterView.post("🖥️", "ZYON iniciado", "Desktop pronto. Bem-vindo.");
        if (!AppCatalog.qemuPresent(this))
            NotificationCenterView.post("⚠️", "QEMU não encontrado", "Coloque os binários em jniLibs/arm64-v8a/ para usar VMs.");
        sleep(350);

        main.post(() -> {
            boolean welcomed = getSharedPreferences("zyon_welcome", MODE_PRIVATE)
                    .getBoolean("done", false);
            Class<?> target = welcomed ? DesktopActivity.class : WelcomeActivity.class;
            startActivity(new Intent(BootLoadingActivity.this, target));
            finish();
        });
    }

    private void ui(String s, String d, int p) {
        main.post(() -> {
            status.setText(s);
            detail.setText(d);
            bar.setProgress(p);
        });
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
