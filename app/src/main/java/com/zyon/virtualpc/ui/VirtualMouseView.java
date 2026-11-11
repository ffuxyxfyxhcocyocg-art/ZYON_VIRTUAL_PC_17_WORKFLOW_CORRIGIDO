package com.zyon.virtualpc.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

/**
 * Cursor de mouse virtual desenhado na tela.
 *
 * Funciona com:
 * 1. Mouse físico (USB/OTG/Bluetooth) via AXIS_RELATIVE_X/Y — cursor de verdade.
 * 2. Toque na tela — toque move o cursor, tap = clique esquerdo, long press = direito.
 *
 * A setinha tem aparência de cursor de computador (seta padrão branca com borda preta).
 */
public class VirtualMouseView extends View {

    public interface MouseListener {
        void onMove(float x, float y);
        void onLeftClick(float x, float y);
        void onRightClick(float x, float y);
        void onScroll(float x, float y, float delta);
        void onDrag(float x, float y);
    }

    private MouseListener listener;
    private float cursorX, cursorY;
    private float sensitivity = 1.4f;

    // paint do cursor
    private final Paint fill   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path  arrow  = new Path();

    // estado touch
    private float lastTouchX, lastTouchY;
    private boolean dragging = false;
    private long downTime = 0;
    private static final long LONG_PRESS_MS = 500;
    private final android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable longPressRunnable;

    public VirtualMouseView(Context ctx) {
        super(ctx);
        fill.setColor(0xFFFFFFFF);
        fill.setStyle(Paint.Style.FILL);
        stroke.setColor(0xFF000000);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(1.8f);
        setClickable(false);
        setFocusable(false);
    }

    public void setListener(MouseListener l) { listener = l; }
    public void setSensitivity(float s) { sensitivity = s; }

    public void setPosition(float x, float y) {
        cursorX = Math.max(0, Math.min(getWidth(),  x));
        cursorY = Math.max(0, Math.min(getHeight(), y));
        invalidate();
    }

    public float getCursorX() { return cursorX; }
    public float getCursorY() { return cursorY; }

    /** Mover cursor via mouse físico (relativo). */
    public void moveRelative(float dx, float dy) {
        setPosition(cursorX + dx * sensitivity, cursorY + dy * sensitivity);
        if (listener != null) listener.onMove(cursorX, cursorY);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        if (cursorX == 0 && cursorY == 0) {
            cursorX = w / 2f;
            cursorY = h / 2f;
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        float x = cursorX, y = cursorY;
        // seta padrão de PC: ponta em cima-esquerda
        float s = 24f;
        arrow.reset();
        arrow.moveTo(x,              y);
        arrow.lineTo(x,              y + s);
        arrow.lineTo(x + s * 0.35f,  y + s * 0.72f);
        arrow.lineTo(x + s * 0.56f,  y + s * 1.03f);
        arrow.lineTo(x + s * 0.72f,  y + s * 0.88f);
        arrow.lineTo(x + s * 0.50f,  y + s * 0.60f);
        arrow.lineTo(x + s * 0.90f,  y + s * 0.50f);
        arrow.close();
        c.drawPath(arrow, fill);
        c.drawPath(arrow, stroke);
    }

    // ── eventos de toque (alternativa quando não há mouse físico) ──

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downTime = System.currentTimeMillis();
                lastTouchX = e.getX();
                lastTouchY = e.getY();
                dragging = false;
                longPressRunnable = () -> {
                    if (listener != null) listener.onRightClick(cursorX, cursorY);
                };
                h.postDelayed(longPressRunnable, LONG_PRESS_MS);
                return true;

            case MotionEvent.ACTION_MOVE: {
                float dx = (e.getX() - lastTouchX) * sensitivity;
                float dy = (e.getY() - lastTouchY) * sensitivity;
                if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {
                    dragging = true;
                    h.removeCallbacks(longPressRunnable);
                    setPosition(cursorX + dx, cursorY + dy);
                    if (listener != null) {
                        listener.onMove(cursorX, cursorY);
                        listener.onDrag(cursorX, cursorY);
                    }
                    lastTouchX = e.getX();
                    lastTouchY = e.getY();
                }
                return true;
            }
            case MotionEvent.ACTION_POINTER_DOWN:
                // dois dedos: preparar para scroll
                h.removeCallbacks(longPressRunnable);
                return true;

            case MotionEvent.ACTION_UP:
                h.removeCallbacks(longPressRunnable);
                if (!dragging && (System.currentTimeMillis() - downTime) < LONG_PRESS_MS) {
                    if (listener != null) listener.onLeftClick(cursorX, cursorY);
                }
                dragging = false;
                return true;

            default:
                return true;
        }
    }

    // scroll com dois dedos
    @Override
    public boolean onGenericMotionEvent(MotionEvent e) {
        if ((e.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
            if (e.getActionMasked() == MotionEvent.ACTION_SCROLL) {
                float d = e.getAxisValue(MotionEvent.AXIS_VSCROLL);
                if (listener != null) listener.onScroll(cursorX, cursorY, d);
                return true;
            }
        }
        return false;
    }
}
