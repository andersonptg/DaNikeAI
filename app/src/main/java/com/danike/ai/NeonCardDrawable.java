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

        RectF area = new RectF(0, 0, w, h);
        canvas.save();
        canvas.clipRect(area);

        int[] cores = coresDoCard();

        // Fundo escuro premium
        p.setShader(new LinearGradient(
                0, 0, w, h,
                cores[0],
                cores[1],
                Shader.TileMode.CLAMP
        ));
        p.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(area, 24, 24, p);
        p.setShader(null);

        // Brilho discreto
        RadialGradient brilho = new RadialGradient(
                w * 0.82f,
                h * 0.50f,
                h * 0.85f,
                Color.argb(55, Color.red(cores[3]), Color.green(cores[3]), Color.blue(cores[3])),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
        );
        glow.setShader(brilho);
        canvas.drawRoundRect(area, 24, 24, glow);
        glow.setShader(null);

        // Ícone/arte temática à esquerda
        float cx = 58;
        float cy = h * 0.50f;
        float raio = Math.min(30, h * 0.25f);

        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(35, 255, 255, 255));
        canvas.drawRoundRect(
                cx - raio - 10,
                cy - raio - 10,
                cx + raio + 10,
                cy + raio + 10,
                18,
                18,
                p
        );

        desenharIcone(canvas, cx, cy, raio, cores);

        // Arte temática no lado direito do cartão
        float tempo = (SystemClock.uptimeMillis() - inicio) / 1000f;

        if (tipo.equals("IA")) {
            desenharIA(canvas, w, h, tempo, cores);
        } else if (tipo.equals("Filmes") || tipo.equals("Rave")) {
            desenharFilmes(canvas, w, h, tempo, cores);
        } else if (tipo.equals("Histórico")) {
            desenharHistorico(canvas, w, h, tempo, cores);
        } else if (tipo.equals("Perfil")) {
            desenharPerfil(canvas, w, h, tempo, cores);
        } else if (tipo.equals("ADM")) {
            desenharADM(canvas, w, h, tempo, cores);
        }

        // Seta circular à direita
        float sx = w - 42;
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(42, 255, 255, 255));
        canvas.drawCircle(sx, cy, 19, p);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.5f);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(Color.WHITE);

        Path seta = new Path();
        seta.moveTo(sx - 6, cy - 7);
        seta.lineTo(sx + 2, cy);
        seta.lineTo(sx - 6, cy + 7);
        canvas.drawPath(seta, p);

        // Borda neon elegante
        p.setStrokeWidth(1.8f);
        p.setColor(Color.argb(
                210,
                Color.red(cores[3]),
                Color.green(cores[3]),
                Color.blue(cores[3])
        ));
        canvas.drawRoundRect(
                1,
                1,
                w - 1,
                h - 1,
                24,
                24,
                p
        );

        canvas.restore();
    }

    private void desenharIcone(
            Canvas canvas,
            float cx,
            float cy,
            float r,
            int[] cores
    ) {

        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);

        if (tipo.equals("IA")) {

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setColor(Color.rgb(80, 225, 255));

            canvas.drawCircle(cx, cy, r * 0.72f, p);
            canvas.drawCircle(cx, cy, r * 0.34f, p);

            canvas.drawLine(cx - r, cy, cx - r * 0.72f, cy, p);
            canvas.drawLine(cx + r * 0.72f, cy, cx + r, cy, p);
            canvas.drawLine(cx, cy - r, cx, cy - r * 0.72f, p);
            canvas.drawLine(cx, cy + r * 0.72f, cx, cy + r, p);

        } else if (tipo.equals("Filmes")) {

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setColor(Color.rgb(255, 140, 65));

            canvas.drawRect(
                    cx - r * 0.72f,
                    cy - r * 0.48f,
                    cx + r * 0.72f,
                    cy + r * 0.48f,
                    p
            );

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(255, 140, 65));

            Path play = new Path();
            play.moveTo(cx - 7, cy - 11);
            play.lineTo(cx + 12, cy);
            play.lineTo(cx - 7, cy + 11);
            play.close();
            canvas.drawPath(play, p);

        } else if (tipo.equals("Histórico")) {

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setColor(Color.rgb(90, 220, 255));

            canvas.drawCircle(cx, cy, r * 0.72f, p);
            canvas.drawLine(cx, cy, cx, cy - r * 0.40f, p);
            canvas.drawLine(cx, cy, cx + r * 0.32f, cy + r * 0.20f, p);

        } else if (tipo.equals("Rave")) {

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setColor(Color.rgb(220, 100, 255));

            canvas.drawCircle(cx, cy, r * 0.70f, p);

            p.setStyle(Paint.Style.FILL);
            Path play = new Path();
            play.moveTo(cx - 7, cy - 11);
            play.lineTo(cx + 12, cy);
            play.lineTo(cx - 7, cy + 11);
            play.close();
            canvas.drawPath(play, p);

        } else if (tipo.equals("ADM")) {

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setColor(Color.rgb(80, 255, 195));

            Path escudo = new Path();
            escudo.moveTo(cx, cy - r * 0.78f);
            escudo.lineTo(cx + r * 0.62f, cy - r * 0.38f);
            escudo.lineTo(cx + r * 0.48f, cy + r * 0.58f);
            escudo.lineTo(cx, cy + r * 0.78f);
            escudo.lineTo(cx - r * 0.48f, cy + r * 0.58f);
            escudo.lineTo(cx - r * 0.62f, cy - r * 0.38f);
            escudo.close();
            canvas.drawPath(escudo, p);

        } else {

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setColor(Color.rgb(255, 125, 70));
            canvas.drawCircle(cx, cy, r * 0.65f, p);
        }
    }

    // =============================================================
    // PALETAS
    // =============================================================

    private int[] coresDoCard() {

        // Paleta Halloween Premium: base escura, laranja e violeta.
        if (tipo.equals("IA")) {
            return new int[]{
                    Color.rgb(13, 10, 25),
                    Color.rgb(31, 13, 37),
                    Color.rgb(43, 18, 30),
                    Color.rgb(255, 112, 47),
                    Color.rgb(151, 72, 220)
            };
        }

        if (tipo.equals("Filmes")) {
            return new int[]{
                    Color.rgb(16, 9, 24),
                    Color.rgb(37, 13, 35),
                    Color.rgb(48, 17, 29),
                    Color.rgb(255, 126, 48),
                    Color.rgb(166, 72, 220)
            };
        }

        if (tipo.equals("Histórico")) {
            return new int[]{
                    Color.rgb(11, 12, 28),
                    Color.rgb(25, 15, 38),
                    Color.rgb(39, 17, 34),
                    Color.rgb(255, 112, 47),
                    Color.rgb(135, 75, 210)
            };
        }

        if (tipo.equals("Perfil")) {
            return new int[]{
                    Color.rgb(12, 11, 24),
                    Color.rgb(27, 13, 34),
                    Color.rgb(42, 17, 30),
                    Color.rgb(255, 132, 55),
                    Color.rgb(170, 82, 225)
            };
        }

        if (tipo.equals("ADM")) {
            return new int[]{
                    Color.rgb(10, 12, 24),
                    Color.rgb(22, 14, 32),
                    Color.rgb(37, 17, 32),
                    Color.rgb(255, 112, 47),
                    Color.rgb(145, 75, 215)
            };
        }

        // HAVE / RAVE
        return new int[]{
                Color.rgb(15, 9, 27),
                Color.rgb(33, 12, 39),
                Color.rgb(47, 14, 34),
                Color.rgb(255, 112, 47),
                Color.rgb(158, 68, 220)
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
