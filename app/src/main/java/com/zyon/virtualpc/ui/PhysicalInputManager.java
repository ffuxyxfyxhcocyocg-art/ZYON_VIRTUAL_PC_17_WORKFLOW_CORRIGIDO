package com.zyon.virtualpc.ui;

import android.content.Context;
import android.hardware.input.InputManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Gerencia mouse e teclado físicos (USB, OTG, Bluetooth).
 * Detecta dispositivos de entrada conectados, rastreia cursor real,
 * e envia eventos para a VM ou apps Android.
 *
 * APIs usadas: InputManager, InputDevice (oficiais Android, sem root).
 */
public class PhysicalInputManager implements InputManager.InputDeviceListener {

    public interface PhysicalMouseListener {
        void onMouseMove(float dx, float dy);
        void onMouseButton(int button, boolean pressed);  // MotionEvent.BUTTON_*
        void onMouseScroll(float vScroll, float hScroll);
    }

    public interface PhysicalKeyboardListener {
        void onKey(int keyCode, int scanCode, boolean pressed, int metaState);
    }

    private final InputManager im;
    private PhysicalMouseListener mouseListener;
    private PhysicalKeyboardListener keyboardListener;

    // sensibilidade do cursor (1.0 = normal)
    private float sensitivity = 1.0f;

    public PhysicalInputManager(Context ctx) {
        im = (InputManager) ctx.getSystemService(Context.INPUT_SERVICE);
        im.registerInputDeviceListener(this, null);
    }

    public void setMouseListener(PhysicalMouseListener l) { mouseListener = l; }
    public void setKeyboardListener(PhysicalKeyboardListener l) { keyboardListener = l; }
    public void setSensitivity(float s) { sensitivity = s; }

    /** Chame no dispatchGenericMotionEvent da Activity/View para capturar mouse físico. */
    public boolean handleMotionEvent(MotionEvent e) {
        if (e.getSource() == InputDevice.SOURCE_MOUSE ||
            (e.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {

            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_HOVER_MOVE:
                case MotionEvent.ACTION_MOVE: {
                    float dx = e.getAxisValue(MotionEvent.AXIS_RELATIVE_X) * sensitivity;
                    float dy = e.getAxisValue(MotionEvent.AXIS_RELATIVE_Y) * sensitivity;
                    if (mouseListener != null && (dx != 0 || dy != 0))
                        mouseListener.onMouseMove(dx, dy);
                    return true;
                }
                case MotionEvent.ACTION_SCROLL: {
                    float v = e.getAxisValue(MotionEvent.AXIS_VSCROLL);
                    float h = e.getAxisValue(MotionEvent.AXIS_HSCROLL);
                    if (mouseListener != null) mouseListener.onMouseScroll(v, h);
                    return true;
                }
                case MotionEvent.ACTION_BUTTON_PRESS:
                case MotionEvent.ACTION_BUTTON_RELEASE: {
                    boolean pressed = e.getActionMasked() == MotionEvent.ACTION_BUTTON_PRESS;
                    if (mouseListener != null) mouseListener.onMouseButton(e.getActionButton(), pressed);
                    return true;
                }
            }
        }
        return false;
    }

    /** Chame no dispatchKeyEvent da Activity para capturar teclado físico. */
    public boolean handleKeyEvent(KeyEvent e) {
        InputDevice dev = e.getDevice();
        if (dev == null) return false;
        int src = dev.getSources();
        boolean isKeyboard = (src & InputDevice.SOURCE_KEYBOARD) != 0
                          || (src & InputDevice.SOURCE_GAMEPAD) == 0;
        if (!isKeyboard) return false;

        boolean pressed = e.getAction() == KeyEvent.ACTION_DOWN;
        if (keyboardListener != null)
            keyboardListener.onKey(e.getKeyCode(), e.getScanCode(), pressed, e.getMetaState());
        return false; // não consumir; deixar o sistema processar também
    }

    /** Lista todos os mouses físicos conectados agora. */
    public List<String> listMice() {
        List<String> names = new ArrayList<>();
        for (int id : im.getInputDeviceIds()) {
            InputDevice d = im.getInputDevice(id);
            if (d == null) continue;
            if ((d.getSources() & InputDevice.SOURCE_MOUSE) != 0 ||
                (d.getSources() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
                names.add(d.getName() + " [id=" + id + "]");
            }
        }
        return names;
    }

    /** Lista todos os teclados físicos conectados agora. */
    public List<String> listKeyboards() {
        List<String> names = new ArrayList<>();
        for (int id : im.getInputDeviceIds()) {
            InputDevice d = im.getInputDevice(id);
            if (d == null) continue;
            if ((d.getSources() & InputDevice.SOURCE_KEYBOARD) != 0
                    && d.getKeyboardType() == InputDevice.KEYBOARD_TYPE_ALPHABETIC) {
                names.add(d.getName() + " [id=" + id + "]");
            }
        }
        return names;
    }

    public boolean hasPhysicalMouse() { return !listMice().isEmpty(); }
    public boolean hasPhysicalKeyboard() { return !listKeyboards().isEmpty(); }

    @Override public void onInputDeviceAdded(int deviceId) {}
    @Override public void onInputDeviceRemoved(int deviceId) {}
    @Override public void onInputDeviceChanged(int deviceId) {}

    public void destroy() {
        im.unregisterInputDeviceListener(this);
    }
}
