package com.danike.ai;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

public class NeonCardDrawable extends Drawable {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String tipo;

    private final long inicio = SystemClock.uptimeMillis();

    public NeonCardDrawable(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public void draw(Canvas canvas) {

        float w = getBounds().width();
        float h = getBounds().height();

        if (w <= 0 || h <= 0) return;

        float t = (SystemClock.uptimeMillis() - inicio) / 1000f;

        RectF area = new RectF(0, 0, w, h);

        canvas.save();
        canvas.clipRect(area);

        int[] cores = coresDoCard();

        // =========================================================
        // FUNDO COLORIDO FORTE
        // =========================================================

        float deslocamento = (float) Math.sin(t * 0.45f) * w * 0.35f;

        LinearGradient fundo = new LinearGradient(
                -w + deslocamento,
                0,
                w + deslocamento,
                h,
                new int[]{
                        cores[0],
                        cores[1],
                        cores[2],
                        cores[0]
                },
                null,
                Shader.TileMode.MIRROR
        );

        p.setShader(fundo);
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, w, h, p);
        p.setShader(null);

        // =========================================================
        // MANCHAS DE LUZ GRANDES
        // =========================================================

        float movimento1 = (float) Math.sin(t * 0.8f);
        float movimento2 = (float) Math.cos(t * 0.65f);

        desenharLuz(
                canvas,
                w * 0.20f + movimento1 * w * 0.15f,
                h * 0.25f,
                h * 0.70f,
                cores[3],
                120
        );

        desenharLuz(
                canvas,
                w * 0.82f + movimento2 * w * 0.12f,
                h * 0.72f,
                h * 0.65f,
                cores[4],
                105
        );

        // =========================================================
        // ONDAS DE LUZ
        // =========================================================

        p.setStyle(Paint.Style.STROKE);

        for (int i = 0; i < 4; i++) {

            float fase = t * (0.7f + i * 0.08f) + i * 1.4f;

            Path onda = new Path();

            float baseY = h * (0.30f + i * 0.18f);

            onda.moveTo(-30, baseY);

            for (int x = -30; x <= (int) w + 30; x += 12) {

                float y =
                        baseY
                        + (float) Math.sin((x / w) * Math.PI * 2.4f + fase)
                        * (12 + i * 5);

                onda.lineTo(x, y);
            }

            p.setStrokeWidth(2.0f + i * 0.5f);
            p.setColor(Color.argb(
                    45 + i * 18,
                    Color.red(cores[3]),
                    Color.green(cores[3]),
                    Color.blue(cores[3])
            ));

            canvas.drawPath(onda, p);
        }

        // =========================================================
        // PARTÍCULAS
        // =========================================================

        p.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 18; i++) {

            float px =
                    (float) ((i * 83 + t * (18 + i)) % (w + 80)) - 40;

            float py =
                    (float) (
                            h * 0.10f
                            + ((i * 47) % 80) / 100f * h * 0.85f
                    );

            float pulso =
                    2.0f
                    + (float) Math.sin(t * 2.5f + i) * 1.8f;

            p.setColor(Color.argb(
                    100 + (i % 4) * 30,
                    Color.red(cores[3]),
                    Color.green(cores[3]),
                    Color.blue(cores[3])
            ));

            canvas.drawCircle(px, py, Math.max(1.5f, pulso), p);
        }

        // =========================================================
        // ELEMENTO CENTRAL ESPECÍFICO
        // =========================================================

        if (tipo.equals("IA")) {
            desenharIA(canvas, w, h, t, cores);
        } else if (tipo.equals("Filmes")) {
            desenharFilmes(canvas, w, h, t, cores);
        } else if (tipo.equals("Histórico")) {
            desenharHistorico(canvas, w, h, t, cores);
        } else if (tipo.equals("Perfil")) {
            desenharPerfil(canvas, w, h, t, cores);
        } else if (tipo.equals("ADM")) {
            desenharADM(canvas, w, h, t, cores);
        } else if (tipo.equals("Rave")) {
            desenharRave(canvas, w, h, t, cores);
        }

        // =========================================================
        // REFLEXO PASSANDO PELO BOTÃO
        // =========================================================

        float reflexoX =
                ((t * 170f) % (w + 280f)) - 140f;

        LinearGradient reflexo = new LinearGradient(
                reflexoX - 90,
                0,
                reflexoX + 90,
                0,
                Color.TRANSPARENT,
                Color.argb(85, 255, 255, 255),
                Shader.TileMode.CLAMP
        );

        p.setShader(reflexo);
        p.setStyle(Paint.Style.FILL);

        canvas.drawRect(0, 0, w, h, p);

        p.setShader(null);

        // =========================================================
        // BORDA NEON
        // =========================================================

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.5f);
        p.setColor(Color.argb(
                210,
                Color.red(cores[3]),
                Color.green(cores[3]),
                Color.blue(cores[3])
        ));

        canvas.drawRoundRect(
                2,
                2,
                w - 2,
                h - 2,
                28,
                28,
                p
        );

        // segunda borda pulsante

        float pulso =
                (float) ((Math.sin(t * 2.0f) + 1.0) / 2.0);

        p.setStrokeWidth(1.2f);
        p.setColor(Color.argb(
                (int) (70 + pulso * 100),
                Color.red(cores[4]),
                Color.green(cores[4]),
                Color.blue(cores[4])
        ));

        canvas.drawRoundRect(
                7,
                7,
                w - 7,
                h - 7,
                24,
                24,
                p
        );

        canvas.restore();

        invalidateSelf();
    }

    // =============================================================
    // PALETAS
    // =============================================================

    private int[] coresDoCard() {

        if (tipo.equals("IA")) {
            return new int[]{
                    Color.rgb(0, 30, 65),
                    Color.rgb(0, 110, 160),
                    Color.rgb(75, 25, 150),
                    Color.rgb(0, 245, 255),
                    Color.rgb(170, 60, 255)
            };
        }

        if (tipo.equals("Filmes")) {
            return new int[]{
                    Color.rgb(55, 5, 70),
                    Color.rgb(150, 20, 100),
                    Color.rgb(245, 65, 100),
                    Color.rgb(255, 100, 40),
                    Color.rgb(180, 40, 255)
            };
        }

        if (tipo.equals("Histórico")) {
            return new int[]{
                    Color.rgb(3, 25, 75),
                    Color.rgb(10, 75, 150),
                    Color.rgb(70, 30, 170),
                    Color.rgb(0, 220, 255),
                    Color.rgb(120, 80, 255)
            };
        }

        if (tipo.equals("Perfil")) {
            return new int[]{
                    Color.rgb(0, 35, 60),
                    Color.rgb(0, 120, 150),
                    Color.rgb(90, 30, 150),
                    Color.rgb(0, 255, 230),
                    Color.rgb(210, 50, 255)
            };
        }

        if (tipo.equals("ADM")) {
            return new int[]{
                    Color.rgb(2, 45, 45),
                    Color.rgb(0, 120, 105),
                    Color.rgb(20, 70, 150),
                    Color.rgb(0, 255, 190),
                    Color.rgb(60, 140, 255)
            };
        }

        // RAVE
        return new int[]{
                Color.rgb(40, 0, 80),
                Color.rgb(110, 0, 160),
                Color.rgb(220, 0, 120),
                Color.rgb(255, 0, 180),
                Color.rgb(80, 30, 255)
        };
    }

    // =============================================================
    // LUZ
    // =============================================================

    private void desenharLuz(
            Canvas canvas,
            float x,
            float y,
            float raio,
            int cor,
            int alpha
    ) {

        RadialGradient grad = new RadialGradient(
                x,
                y,
                raio,
                Color.argb(
                        alpha,
                        Color.red(cor),
                        Color.green(cor),
                        Color.blue(cor)
                ),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
        );

        glow.setShader(grad);
        glow.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x, y, raio, glow);

        glow.setShader(null);
    }

    // =============================================================
    // IA
    // =============================================================

    private void desenharIA(
            Canvas canvas,
            float w,
            float h,
            float t,
            int[] cores
    ) {

        float cx = w * 0.82f;
        float cy = h * 0.50f;

        float pulso =
                1f + (float) Math.sin(t * 2.2f) * 0.06f;

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);

        p.setColor(Color.argb(210, 0, 245, 255));

        canvas.drawCircle(cx, cy, 55 * pulso, p);
        canvas.drawCircle(cx, cy, 38 * pulso, p);

        p.setColor(Color.argb(160, 180, 70, 255));

        canvas.drawCircle(cx, cy, 20 * pulso, p);

        p.setStyle(Paint.Style.FILL);

        p.setColor(Color.WHITE);

        canvas.drawCircle(cx - 8, cy - 5, 3, p);
        canvas.drawCircle(cx + 8, cy - 5, 3, p);
    }

    // =============================================================
    // FILMES
    // =============================================================

    private void desenharFilmes(
            Canvas canvas,
            float w,
            float h,
            float t,
            int[] cores
    ) {

        float cx = w * 0.80f;
        float cy = h * 0.50f;

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4);

        p.setColor(Color.argb(220, 255, 120, 80));

        canvas.drawRect(
                cx - 58,
                cy - 38,
                cx + 58,
                cy + 38,
                p
        );

        p.setStyle(Paint.Style.FILL);

        p.setColor(Color.argb(180, 210, 50, 255));

        Path play = new Path();

        play.moveTo(cx - 12, cy - 23);
        play.lineTo(cx + 28, cy);
        play.lineTo(cx - 12, cy + 23);
        play.close();

        canvas.drawPath(play, p);

        p.setColor(Color.argb(220, 255, 110, 80));

        for (int i = -2; i <= 2; i++) {
            canvas.drawCircle(cx - 45, cy + i * 14, 4, p);
            canvas.drawCircle(cx + 45, cy + i * 14, 4, p);
        }
    }

    // =============================================================
    // HISTÓRICO
    // =============================================================

    private void desenharHistorico(
            Canvas canvas,
            float w,
            float h,
            float t,
            int[] cores
    ) {

        float cx = w * 0.80f;
        float cy = h * 0.50f;
        float r = 52;

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4);

        p.setColor(Color.argb(220, 0, 235, 255));

        canvas.drawCircle(cx, cy, r, p);

        float angulo = t * 0.9f;

        canvas.drawLine(
                cx,
                cy,
                cx + (float)Math.cos(angulo) * 30,
                cy + (float)Math.sin(angulo) * 30,
                p
        );

        canvas.drawLine(
                cx,
                cy,
                cx,
                cy - 30,
                p
        );

        p.setStyle(Paint.Style.FILL);

        p.setColor(Color.argb(220, 150, 70, 255));

        canvas.drawCircle(cx, cy, 6, p);
    }

    // =============================================================
    // PERFIL
    // =============================================================

    private void desenharPerfil(
            Canvas canvas,
            float w,
            float h,
            float t,
            int[] cores
    ) {

        float cx = w * 0.80f;
        float cy = h * 0.48f;

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4);

        p.setColor(Color.argb(220, 0, 255, 235));

        canvas.drawCircle(cx, cy - 28, 25, p);

        RectF corpo = new RectF(
                cx - 58,
                cy + 8,
                cx + 58,
                cy + 70
        );

        canvas.drawOval(corpo, p);

        p.setStyle(Paint.Style.FILL);

        p.setColor(Color.argb(130, 210, 50, 255));

        canvas.drawCircle(cx, cy - 28, 19, p);
    }

    // =============================================================
    // ADM
    // =============================================================

    private void desenharADM(
            Canvas canvas,
            float w,
            float h,
            float t,
            int[] cores
    ) {

        float cx = w * 0.80f;
        float cy = h * 0.50f;

        float rot =
                (float)Math.sin(t * 0.8f) * 5f;

        canvas.save();
        canvas.rotate(rot, cx, cy);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4);

        p.setColor(Color.argb(230, 0, 255, 190));

        Path escudo = new Path();

        escudo.moveTo(cx, cy - 58);
        escudo.lineTo(cx + 48, cy - 30);
        escudo.lineTo(cx + 38, cy + 35);
        escudo.lineTo(cx, cy + 60);
        escudo.lineTo(cx - 38, cy + 35);
        escudo.lineTo(cx - 48, cy - 30);
        escudo.close();

        canvas.drawPath(escudo, p);

        canvas.restore();

        p.setStyle(Paint.Style.FILL);

        p.setColor(Color.argb(220, 0, 255, 190));

        canvas.drawCircle(
                cx,
                cy,
                10 + (float)Math.sin(t * 2.5f) * 3,
                p
        );
    }

    // =============================================================
    // RAVE
    // =============================================================

    private void desenharRave(
            Canvas canvas,
            float w,
            float h,
            float t,
            int[] cores
    ) {

        float base = h * 0.82f;

        p.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 18; i++) {

            float x = w * 0.55f + i * 18;

            float altura =
                    20
                    + Math.abs((float)Math.sin(
                            t * 4.0f + i * 0.7f
                    )) * 90;

            int cor =
                    (i % 2 == 0)
                    ? Color.rgb(255, 0, 190)
                    : Color.rgb(40, 120, 255);

            p.setColor(Color.argb(180, Color.red(cor),
                    Color.green(cor), Color.blue(cor)));

            canvas.drawRect(
                    x,
                    base - altura,
                    x + 10,
                    base,
                    p
            );
        }

        float raio =
                35 + (float)Math.sin(t * 3) * 12;

        RadialGradient raveGlow = new RadialGradient(
                w * 0.78f,
                h * 0.30f,
                raio * 2,
                Color.argb(210, 255, 0, 190),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
        );

        glow.setShader(raveGlow);

        canvas.drawCircle(
                w * 0.78f,
                h * 0.30f,
                raio * 2,
                glow
        );

        glow.setShader(null);
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }
}
