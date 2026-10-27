package com.danike.ai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

public final class HalloweenHomeOverlay extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private long start;

    public HalloweenHomeOverlay(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        start = System.currentTimeMillis();
    }

    private float d(float v) { return v * density; }

    private void glow(Canvas c, float x, float y, float r,
                      int color, int alpha) {
        p.setShader(new RadialGradient(
                x, y, r,
                new int[] {
                    Color.argb(alpha, Color.red(color),
                        Color.green(color), Color.blue(color)),
                    Color.argb(alpha / 3, Color.red(color),
                        Color.green(color), Color.blue(color)),
                    Color.TRANSPARENT
                },
                new float[] {0f, 0.48f, 1f},
                Shader.TileMode.CLAMP
        ));
        p.setStyle(Paint.Style.FILL);
        c.drawCircle(x, y, r, p);
        p.setShader(null);
    }

    private void line(Canvas c, float x1, float y1, float x2, float y2,
                      int color, float width, int alpha) {
        p.setShader(null);
        p.setColor(Color.argb(alpha, Color.red(color),
                Color.green(color), Color.blue(color)));
        p.setStrokeWidth(d(width));
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStyle(Paint.Style.STROKE);
        c.drawLine(x1, y1, x2, y2, p);
        p.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        float t = (System.currentTimeMillis() - start) / 1000f;
        int purple = Color.rgb(132, 62, 255);
        int cyan = Color.rgb(37, 210, 255);
        int orange = Color.rgb(255, 112, 47);

        // Névoa suave em movimento, sempre atrás dos cartões.
        canvas.save();
        canvas.clipRect(0, 0, w, h);

        for (int i = 0; i < 5; i++) {
            float phase = t * (0.18f + i * 0.025f) + i * 1.7f;
            float x = (float) (
                ((phase * d(90) + i * w / 3f) % (w + d(180))) - d(90)
            );
            float y = h * (0.16f + i * 0.16f)
                    + (float) Math.sin(t * 0.42f + i) * d(22);
            float r = d(75 + (i % 3) * 24);
            int color = (i % 2 == 0) ? purple : cyan;
            glow(canvas, x, y, r, color, 15);
            glow(canvas, x + d(38), y + d(14), r * 0.65f,
                    i % 2 == 0 ? cyan : purple, 9);
        }

        // Fios de teia discretos no canto superior direito.
        float wx = w - d(8);
        float wy = d(7);
        for (int i = 1; i <= 5; i++) {
            float endX = wx - d(i * 14);
            float endY = wy + d(i * 17);
            line(canvas, wx, wy, endX, endY, cyan, 0.65f, 48);
        }
        for (int j = 1; j <= 3; j++) {
            Path web = new Path();
            web.moveTo(wx - d(j * 12), wy + d(j * 4));
            web.quadTo(wx - d(j * 19), wy + d(j * 14),
                    wx - d(j * 14), wy + d(j * 25));
            p.setColor(Color.argb(38, 130, 190, 255));
            p.setStrokeWidth(d(0.65f));
            p.setStyle(Paint.Style.STROKE);
            canvas.drawPath(web, p);
            p.setStyle(Paint.Style.FILL);
        }

        // Aranha suspensa por um fio, com balanço lento.
        float sx = w * 0.84f
                + (float) Math.sin(t * 0.8f) * d(7);
        float sy = d(72) + (float) Math.sin(t * 1.1f) * d(5);
        line(canvas, sx, 0, sx, sy - d(12), purple, 0.9f, 115);
        glow(canvas, sx, sy, d(24), purple, 25);

        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(220, 24, 19, 36));
        canvas.drawCircle(sx, sy, d(8), p);
        p.setColor(Color.argb(235, 105, 62, 148));
        canvas.drawCircle(sx, sy - d(1), d(6), p);
        p.setColor(Color.argb(230, 255, 91, 110));
        canvas.drawCircle(sx - d(2.3f), sy - d(1), d(0.9f), p);
        canvas.drawCircle(sx + d(2.3f), sy - d(1), d(0.9f), p);

        // Oito pernas finas, levemente anguladas.
        for (int side : new int[] {-1, 1}) {
            for (int leg = 0; leg < 4; leg++) {
                float y1 = sy - d(4) + d(leg * 2.5f);
                float x1 = sx + side * d(5);
                float x2 = sx + side * d(10 + (leg % 2) * 2);
                float y2 = y1 - d(4);
                float x3 = sx + side * d(13 + (leg % 2) * 2);
                float y3 = y1 + d(3);
                line(canvas, x1, y1, x2, y2, purple, 1f, 190);
                line(canvas, x2, y2, x3, y3, purple, 1f, 190);
            }
        }

        // Pequeno brilho âmbar no rodapé, sem cobrir textos.
        float emberX = w * (0.2f + 0.6f *
                (0.5f + 0.5f * (float) Math.sin(t * 0.23f)));
        glow(canvas, emberX, h - d(20), d(50), orange, 10);

        canvas.restore();
        postInvalidateDelayed(55);
    }
}
