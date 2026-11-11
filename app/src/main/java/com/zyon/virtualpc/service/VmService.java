package com.zyon.virtualpc.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.zyon.virtualpc.R;

public class VmService extends Service {
    private static final String CHANNEL_ID = "zyon_vm_channel";
    private static final int NOTIF_ID = 1601;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ZYON VIRTUAL PC 16")
                .setContentText("Máquina virtual em execução")
                .setSmallIcon(R.drawable.ic_pc)
                .setOngoing(true)
                .build();
        startForeground(NOTIF_ID, n);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "ZYON VM Runtime", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }
}
