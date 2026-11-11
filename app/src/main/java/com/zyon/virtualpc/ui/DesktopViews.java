package com.zyon.virtualpc.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/** Views desenhadas por código: papel de parede, logo do Iniciar e gráfico. */
final class DesktopViews {
    private DesktopViews() {}

    /** Papel de parede — lê cor do SharedPreferences (WallpaperPickerActivity). */
    static final class WallpaperView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int c0 = 0xFF020617, c1 = 0xFF0B2468;

        WallpaperView(Context ctx) {
            super(ctx);
            android.content.SharedPreferences prefs =
                ctx.getSharedPreferences("zyon_wallpaper", android.content.Context.MODE_PRIVATE);
            c0 = prefs.getInt("c0", 0xFF020617);
            c1 = prefs.getInt("c1", 0xFF0B2468);
        }

        @Override
        protected void onDraw(Canvas c) {
            int w = getWidth(), h = getHeight();
            if (w == 0 || h == 0) return;
            p.setShader(new LinearGradient(0, 0, w, h, c0, c1, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, p);

            float cx = w * 0.60f, cy = h * 0.55f, s = Math.min(w, h);
            p.setShader(new RadialGradient(cx, cy, s * 0.95f, 0x881E5BFF, 0x00000000, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, p);

            // pétalas externas
            for (int k = 0; k < 7; k++) {
                c.save();
                c.rotate(k * 51.4f + 8f, cx, cy);
                RectF r = new RectF(cx - s * 0.17f, cy - s * 0.70f, cx + s * 0.17f, cy + s * 0.02f);
                p.setShader(new LinearGradient(cx, r.top, cx, r.bottom,
                        0xC03D8BFF, 0xC00A2A9A, Shader.TileMode.CLAMP));
                c.drawOval(r, p);
                c.restore();
            }
            // pétalas internas
            for (int k = 0; k < 6; k++) {
                c.save();
                c.rotate(k * 60f + 30f, cx, cy);
                RectF r = new RectF(cx - s * 0.11f, cy - s * 0.46f, cx + s * 0.11f, cy + s * 0.02f);
                p.setShader(new LinearGradient(cx, r.top, cx, r.bottom,
                        0xD05AA9FF, 0xD01238C8, Shader.TileMode.CLAMP));
                c.drawOval(r, p);
                c.restore();
            }
            p.setShader(new RadialGradient(cx, cy, s * 0.14f, 0xFFB3D7FF, 0x00000000, Shader.TileMode.CLAMP));
            c.drawCircle(cx, cy, s * 0.14f, p);
            p.setShader(null);
        }
    }

    /** Quatro quadrados do botão Iniciar. */
    static final class WinLogoView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        WinLogoView(Context c) {
            super(c);
            p.setColor(0xFF4FC3F7);
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight();
            float side = Math.min(w, h) * 0.56f;
            float gap = side * 0.10f;
            float sq = (side - gap) / 2f;
            float x0 = (w - side) / 2f, y0 = (h - side) / 2f;
            for (int r = 0; r < 2; r++)
                for (int col = 0; col < 2; col++) {
                    float x = x0 + col * (sq + gap), y = y0 + r * (sq + gap);
                    c.drawRoundRect(new RectF(x, y, x + sq, y + sq), 2f, 2f, p);
                }
        }
    }

    /** Gráfico de linha preenchido (0–100). */
    static final class GraphView extends View {
        private final float[] data = new float[60];
        private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint grid = new Paint();
        private final int color;

        GraphView(Context c, int color) {
            super(c);
            this.color = color;
            line.setColor(color);
            line.setStyle(Paint.Style.STROKE);
            line.setStrokeWidth(3f);
            fill.setStyle(Paint.Style.FILL);
            grid.setColor(0x22FFFFFF);
            grid.setStrokeWidth(1f);
        }

        void push(float v) {
            System.arraycopy(data, 1, data, 0, data.length - 1);
            data[data.length - 1] = Math.max(0f, Math.min(100f, v));
            invalidate();
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight();
            for (int i = 1; i < 4; i++) c.drawLine(0, h * i / 4f, w, h * i / 4f, grid);
            for (int i = 1; i < 8; i++) c.drawLine(w * i / 8f, 0, w * i / 8f, h, grid);
            fill.setShader(new LinearGradient(0, 0, 0, h, (color & 0x00FFFFFF) | 0x66000000,
                    (color & 0x00FFFFFF) | 0x08000000, Shader.TileMode.CLAMP));
            Path path = new Path();
            Path area = new Path();
            area.moveTo(0, h);
            for (int i = 0; i < data.length; i++) {
                float x = w * i / (data.length - 1f);
                float y = h - (data[i] / 100f) * (h - 4f) - 2f;
                if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
                area.lineTo(x, y);
            }
            area.lineTo(w, h);
            area.close();
            c.drawPath(area, fill);
            c.drawPath(path, line);
        }
    }
}
