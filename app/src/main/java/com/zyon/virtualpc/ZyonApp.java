package com.zyon.virtualpc;

import android.app.Application;
import android.util.Log;

import com.zyon.virtualpc.nativebridge.ZyonNative;

import java.io.File;

public class ZyonApp extends Application {
    private static final String TAG = "ZyonApp";
    private static ZyonApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        File data = new File(getFilesDir(), "zyon");
        if (!data.exists()) data.mkdirs();
        try {
            int rc = ZyonNative.nativeInit(data.getAbsolutePath());
            Log.i(TAG, "Native init rc=" + rc + " version=" + ZyonNative.nativeVersion());
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Native library not loaded yet (expected until QEMU binaries are built)", e);
        }
    }

    public static ZyonApp get() {
        return instance;
    }
}
