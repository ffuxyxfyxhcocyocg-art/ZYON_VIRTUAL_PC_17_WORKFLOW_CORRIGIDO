package com.zyon.virtualpc.nativebridge;

import android.view.Surface;

public final class ZyonNative {
    static {
        try {
            System.loadLibrary("zyon_core");
        } catch (UnsatisfiedLinkError e) {
            // QEMU/native not built yet — UI still runs
        }
    }

    private ZyonNative() {}

    public static native int nativeInit(String dataRoot);
    public static native String nativeCreateVm(String name, int arch, int cores, long ramMb, String workDir);
    public static native int nativeStartVm(String name);
    public static native int nativeStopVm(String name, boolean force);
    public static native int nativePauseVm(String name);
    public static native int nativeResumeVm(String name);
    public static native String[] nativeListVms();
    public static native int nativeGetState(String name);
    public static native void nativeSetSurface(String name, Surface surface);
    public static native String nativeVersion();
    public static native long nativeHostFreeRam();
}
