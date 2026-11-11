package com.zyon.virtualpc.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Teclado virtual de PC completo — ESC, F1-F12, CTRL, ALT, SHIFT, setas,
 * teclas especiais, combinações. Envia eventos via VirtualKeyboardListener.
 */
public class VirtualKeyboardView extends View {

    public interface VirtualKeyboardListener {
        void onKey(int keyCode, String label, boolean isDown);
        void onChar(String chars);
    }

    private static final class Key {
        final String label, label2;
        final int keyCode;
        final float col, row, widthU, heightU;
        boolean pressed = false;
        Key(String l, String l2, int kc, float c, float r, float w, float h) {
            label = l; label2 = l2; keyCode = kc; col = c; row = r; widthU = w; heightU = h;
        }
    }

    private final List<Key> keys = new ArrayList<>();
    private VirtualKeyboardListener listener;
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint keyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint smallTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private float unitW, unitH;
    private static final float ROWS = 6f;
    private static final float COLS = 15f;
    private static final float GAP = 2f;

    // modifier state
    private boolean ctrlDown = false, altDown = false, shiftDown = false;

    public VirtualKeyboardView(Context ctx) { super(ctx); init(); }
    public VirtualKeyboardView(Context ctx, AttributeSet a) { super(ctx, a); init(); }

    public void setListener(VirtualKeyboardListener l) { listener = l; }

    private void init() {
        bgPaint.setColor(0xFF1A1A2E);
        keyPaint.setColor(0xFF2D3561);
        pressedPaint.setColor(0xFF4A90D9);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        smallTextPaint.setColor(0xFFAAAAAA);
        smallTextPaint.setTextAlign(Paint.Align.CENTER);

        buildLayout();
    }

    private void buildLayout() {
        // Row 0: ESC, F1-F12
        addKey("ESC", "", android.view.KeyEvent.KEYCODE_ESCAPE, 0, 0, 1, 1);
        for (int i = 1; i <= 12; i++) {
            addKey("F" + i, "", android.view.KeyEvent.KEYCODE_F1 + (i - 1),
                    i + (i > 4 ? 0.3f : 0) + (i > 8 ? 0.3f : 0), 0, 1, 1);
        }

        // Row 1: ` 1-9 0 - = BKSP
        String[] r1l = {"`","1","2","3","4","5","6","7","8","9","0","-","="};
        String[] r1l2 = {"~","!","@","#","$","%","^","&","*","(",")","_","+"};
        int[] r1kc = {android.view.KeyEvent.KEYCODE_GRAVE,
                android.view.KeyEvent.KEYCODE_1, android.view.KeyEvent.KEYCODE_2,
                android.view.KeyEvent.KEYCODE_3, android.view.KeyEvent.KEYCODE_4,
                android.view.KeyEvent.KEYCODE_5, android.view.KeyEvent.KEYCODE_6,
                android.view.KeyEvent.KEYCODE_7, android.view.KeyEvent.KEYCODE_8,
                android.view.KeyEvent.KEYCODE_9, android.view.KeyEvent.KEYCODE_0,
                android.view.KeyEvent.KEYCODE_MINUS, android.view.KeyEvent.KEYCODE_EQUALS};
        for (int i = 0; i < r1l.length; i++) addKey(r1l[i], r1l2[i], r1kc[i], i, 1, 1, 1);
        addKey("BKSP", "", android.view.KeyEvent.KEYCODE_DEL, 13, 1, 2, 1);

        // Row 2: TAB Q-P [ ] \
        addKey("TAB", "", android.view.KeyEvent.KEYCODE_TAB, 0, 2, 1.5f, 1);
        String qwerty = "QWERTYUIOP";
        for (int i = 0; i < qwerty.length(); i++) {
            int kc = android.view.KeyEvent.KEYCODE_Q + i;
            addKey(String.valueOf(qwerty.charAt(i)), "", kc, 1.5f + i, 2, 1, 1);
        }
        addKey("[", "{", android.view.KeyEvent.KEYCODE_LEFT_BRACKET, 11.5f, 2, 1, 1);
        addKey("]", "}", android.view.KeyEvent.KEYCODE_RIGHT_BRACKET, 12.5f, 2, 1, 1);
        addKey("\\", "|", android.view.KeyEvent.KEYCODE_BACKSLASH, 13.5f, 2, 1.5f, 1);

        // Row 3: CAPS A-L ; ' ENTER
        addKey("CAPS", "", android.view.KeyEvent.KEYCODE_CAPS_LOCK, 0, 3, 1.75f, 1);
        String asdf = "ASDFGHJKL";
        for (int i = 0; i < asdf.length(); i++) {
            int kc = android.view.KeyEvent.KEYCODE_A + i;
            addKey(String.valueOf(asdf.charAt(i)), "", kc, 1.75f + i, 3, 1, 1);
        }
        addKey(";", ":", android.view.KeyEvent.KEYCODE_SEMICOLON, 10.75f, 3, 1, 1);
        addKey("'", "\"", android.view.KeyEvent.KEYCODE_APOSTROPHE, 11.75f, 3, 1, 1);
        addKey("ENTER", "", android.view.KeyEvent.KEYCODE_ENTER, 12.75f, 3, 2.25f, 1);

        // Row 4: SHIFT Z-M , . / SHIFT
        addKey("SHIFT", "", android.view.KeyEvent.KEYCODE_SHIFT_LEFT, 0, 4, 2.25f, 1);
        String zxcv = "ZXCVBNM";
        for (int i = 0; i < zxcv.length(); i++) {
            int kc = android.view.KeyEvent.KEYCODE_Z + i;
            addKey(String.valueOf(zxcv.charAt(i)), "", kc, 2.25f + i, 4, 1, 1);
        }
        addKey(",", "<", android.view.KeyEvent.KEYCODE_COMMA, 9.25f, 4, 1, 1);
        addKey(".", ">", android.view.KeyEvent.KEYCODE_PERIOD, 10.25f, 4, 1, 1);
        addKey("/", "?", android.view.KeyEvent.KEYCODE_SLASH, 11.25f, 4, 1, 1);
        addKey("SHIFT", "", android.view.KeyEvent.KEYCODE_SHIFT_RIGHT, 12.25f, 4, 2.75f, 1);

        // Row 5: CTRL WIN ALT SPACE ALT CTRL ← ↑ ↓ →
        addKey("CTRL", "", android.view.KeyEvent.KEYCODE_CTRL_LEFT, 0, 5, 1.5f, 1);
        addKey("WIN", "", android.view.KeyEvent.KEYCODE_META_LEFT, 1.5f, 5, 1, 1);
        addKey("ALT", "", android.view.KeyEvent.KEYCODE_ALT_LEFT, 2.5f, 5, 1, 1);
        addKey("", "", android.view.KeyEvent.KEYCODE_SPACE, 3.5f, 5, 6, 1);
        addKey("ALT", "", android.view.KeyEvent.KEYCODE_ALT_RIGHT, 9.5f, 5, 1, 1);
        addKey("CTRL", "", android.view.KeyEvent.KEYCODE_CTRL_RIGHT, 10.5f, 5, 1.5f, 1);
        addKey("←", "", android.view.KeyEvent.KEYCODE_DPAD_LEFT, 12f, 5, 1, 1);
        addKey("↑", "", android.view.KeyEvent.KEYCODE_DPAD_UP, 13f, 5, 1, 1);
        addKey("↓", "", android.view.KeyEvent.KEYCODE_DPAD_DOWN, 13f, 5, 1, 1);
        addKey("→", "", android.view.KeyEvent.KEYCODE_DPAD_RIGHT, 14f, 5, 1, 1);
    }

    private void addKey(String l, String l2, int kc, float c, float r, float w, float h) {
        keys.add(new Key(l, l2, kc, c, r, w, h));
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        unitW = (w - GAP) / COLS;
        unitH = (h - GAP) / ROWS;
        textPaint.setTextSize(unitH * 0.38f);
        smallTextPaint.setTextSize(unitH * 0.28f);
    }

    @Override
    protected void onDraw(Canvas c) {
        c.drawColor(0xFF111122);
        for (Key k : keys) {
            float x = GAP + k.col * unitW;
            float y = GAP + k.row * unitH;
            float w = k.widthU * unitW - GAP;
            float h = k.heightU * unitH - GAP;
            rect.set(x, y, x + w, y + h);
            boolean mod = isModifier(k.keyCode);
            boolean active = mod && isModifierActive(k.keyCode);
            c.drawRoundRect(rect, 6, 6, active ? pressedPaint : (k.pressed ? pressedPaint : keyPaint));
            float tx = x + w / 2f;
            float ty = y + h / 2f + textPaint.getTextSize() * 0.35f;
            c.drawText(k.label, tx, ty, textPaint);
            if (k.label2 != null && !k.label2.isEmpty()) {
                c.drawText(k.label2, tx, y + smallTextPaint.getTextSize() * 1.2f, smallTextPaint);
            }
        }
    }

    private boolean isModifier(int kc) {
        return kc == android.view.KeyEvent.KEYCODE_SHIFT_LEFT
                || kc == android.view.KeyEvent.KEYCODE_SHIFT_RIGHT
                || kc == android.view.KeyEvent.KEYCODE_CTRL_LEFT
                || kc == android.view.KeyEvent.KEYCODE_CTRL_RIGHT
                || kc == android.view.KeyEvent.KEYCODE_ALT_LEFT
                || kc == android.view.KeyEvent.KEYCODE_ALT_RIGHT;
    }

    private boolean isModifierActive(int kc) {
        if (kc == android.view.KeyEvent.KEYCODE_SHIFT_LEFT || kc == android.view.KeyEvent.KEYCODE_SHIFT_RIGHT) return shiftDown;
        if (kc == android.view.KeyEvent.KEYCODE_CTRL_LEFT || kc == android.view.KeyEvent.KEYCODE_CTRL_RIGHT) return ctrlDown;
        if (kc == android.view.KeyEvent.KEYCODE_ALT_LEFT || kc == android.view.KeyEvent.KEYCODE_ALT_RIGHT) return altDown;
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        int idx = ev.getActionIndex();
        float tx = ev.getX(idx), ty = ev.getY(idx);

        Key hit = hitTest(tx, ty);
        if (hit == null) return true;

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            hit.pressed = true;
            handleModifier(hit.keyCode, true);
            if (listener != null) listener.onKey(hit.keyCode, hit.label, true);
            invalidate();
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP
                || action == MotionEvent.ACTION_CANCEL) {
            hit.pressed = false;
            if (!isModifier(hit.keyCode)) {
                // send char
                String ch = resolveChar(hit, shiftDown);
                if (!ch.isEmpty() && listener != null) listener.onChar(ch);
            } else {
                handleModifier(hit.keyCode, false);
            }
            if (listener != null) listener.onKey(hit.keyCode, hit.label, false);
            invalidate();
        }
        return true;
    }

    private void handleModifier(int kc, boolean down) {
        if (kc == android.view.KeyEvent.KEYCODE_SHIFT_LEFT || kc == android.view.KeyEvent.KEYCODE_SHIFT_RIGHT) shiftDown = down;
        if (kc == android.view.KeyEvent.KEYCODE_CTRL_LEFT || kc == android.view.KeyEvent.KEYCODE_CTRL_RIGHT) ctrlDown = down;
        if (kc == android.view.KeyEvent.KEYCODE_ALT_LEFT || kc == android.view.KeyEvent.KEYCODE_ALT_RIGHT) altDown = down;
    }

    private String resolveChar(Key k, boolean shift) {
        if (k.label.isEmpty()) return " ";
        if (k.label.length() == 1 && Character.isLetter(k.label.charAt(0))) {
            return shift ? k.label.toUpperCase() : k.label.toLowerCase();
        }
        if (shift && k.label2 != null && !k.label2.isEmpty()) return k.label2;
        return k.label.length() == 1 ? k.label : "";
    }

    private Key hitTest(float px, float py) {
        for (Key k : keys) {
            float x = GAP + k.col * unitW;
            float y = GAP + k.row * unitH;
            float w = k.widthU * unitW - GAP;
            float h = k.heightU * unitH - GAP;
            if (px >= x && px <= x + w && py >= y && py <= y + h) return k;
        }
        return null;
    }
}
