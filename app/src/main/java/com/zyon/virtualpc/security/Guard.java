package com.zyon.virtualpc.security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Debug;

import com.zyon.virtualpc.BuildConfig;

/** Proteção básica contra depuração/adulteração em build release. */
public final class Guard {
    private Guard() {}

    public static boolean tampered(Context c) {
        if (BuildConfig.DEBUG) return false;
        boolean debuggable = (c.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        return debuggable || Debug.isDebuggerConnected() || Debug.waitingForDebugger();
    }
}
