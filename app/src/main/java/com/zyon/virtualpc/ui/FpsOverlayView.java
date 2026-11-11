package com.zyon.virtualpc.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * Overlay de FPS + RAM/CPU para o Game Center.
 * Renderizado via Canvas — zero dependências externas.
 * Calculado pela taxa de invalidate() = frames reais do Android.
 */
public class FpsOverlayView extends View {

    private final Paint bgPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Handler h = new Handler(Looper.getMainLooper());
    private final ActivityManager am;

    private int fps = 0, frameCount = 0;
    private long lastFpsTime = System.currentTimeMillis();
    private float cpuUsage = 0f, ramUsage = 0f;
    private boolean running = false;

    public FpsOverlayView(Context ctx) {
        super(ctx);
        am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        bgPaint.setColor(0xCC000000);
        txtPaint.setColor(0xFF00FF88);
        txtPaint.setTextSize(11 * ctx.getResources().getDisplayMetrics().density);
        txtPaint.setTypeface(android.graphics.Typeface.MONOSPACE);
        barPaint.setColor(0xFF00FF88);
        setClickable(false);
        setFocusable(false);
    }

    public void start() {
        running = true;
        scheduleSample();
        scheduleFrame();
    }

    public void stop() {
        running = false;
        h.removeCallbacksAndMessages(null);
    }

    private void scheduleFrame() {
        if (!running) return;
        postInvalidateOnAnimation();
        h.postDelayed(this::scheduleFrame, 16);
    }

    private void scheduleSample() {
        if (!running) return;
        sampleStats();
        h.postDelayed(this::scheduleSample, 1000);
    }

    private void sampleStats() {
        // FPS: count frames per second
        long now = System.currentTimeMillis();
        long elapsed = now - lastFpsTime;
        if (elapsed >= 900) {
            fps = (int)(frameCount * 1000f / elapsed);
            frameCount = 0;
            lastFpsTime = now;
        }

        // RAM
        if (am != null) {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            long used = mi.totalMem - mi.availMem;
            ramUsage = (float) used / mi.totalMem * 100f;
        }

        // CPU (approximate from /proc/stat)
        try {
            java.io.RandomAccessFile reader = new java.io.RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            reader.close();
            String[] toks = load.split("\\s+");
            if (toks.length >= 5) {
                long idle  = Long.parseLong(toks[4]);
                long total = 0;
                for (int i = 1; i < Math.min(toks.length, 9); i++) total += Long.parseLong(toks[i]);
                cpuUsage = total > 0 ? (1f - (float) idle / total) * 100f : 0f;
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDraw(Canvas c) {
        frameCount++;
        float d = getResources().getDisplayMetrics().density;
        float pad = 8 * d, w = 90 * d, h = 54 * d;
        rect.set(4 * d, 4 * d, 4 * d + w, 4 * d + h);
        c.drawRoundRect(rect, 6 * d, 6 * d, bgPaint);

        float ts = txtPaint.getTextSize();
        c.drawText("FPS: " + fps, 10 * d, 4 * d + ts, txtPaint);
        c.drawText(String.format("CPU: %.0f%%", cpuUsage), 10 * d, 4 * d + ts * 2.1f, txtPaint);
        c.drawText(String.format("RAM: %.0f%%", ramUsage), 10 * d, 4 * d + ts * 3.2f, txtPaint);

        // mini bar CPU
        float barW = (w - 16 * d) * (cpuUsage / 100f);
        barPaint.setColor(cpuUsage > 85f ? 0xFFFF5252 : cpuUsage > 60f ? 0xFFFFD740 : 0xFF00FF88);
        c.drawRect(10 * d, 4 * d + ts * 3.6f, 10 * d + barW, 4 * d + ts * 3.6f + 3 * d, barPaint);
    }
}
