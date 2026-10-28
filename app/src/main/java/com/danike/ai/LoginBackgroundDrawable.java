package com.danike.ai;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

public class LoginBackgroundDrawable extends Drawable {

    private final Bitmap foto;
    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private final Paint fumaçaPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint morcegoPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint teiaPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF rect = new RectF();

    private long inicioAnimacao;

    public LoginBackgroundDrawable(Bitmap foto) {
        this.foto = foto;
        inicioAnimacao = SystemClock.uptimeMillis();
    }

    @Override
    public void draw(Canvas canvas) {

        int w = getBounds().width();
        int h = getBounds().height();

        if (w <= 0 || h <= 0) {
            return;
        }

        // =========================================================
        // FUNDO PRETO PURO
        // =========================================================
        canvas.drawColor(Color.BLACK);

        float tempo =
                (SystemClock.uptimeMillis() - inicioAnimacao) / 1000f;

        // =========================================================
        // FUMAÇA ANIMADA
        // =========================================================
        // Fumaça removida do Login.

        // =========================================================
        // TEIAS
        // =========================================================
        desenharTeias(canvas, w, h);

        // =========================================================
        // MORCEGOS BRANCOS ANIMADOS
        // =========================================================
        desenharMorcegos(canvas, w, h, tempo);

        // Mantém a animação viva.
        invalidateSelf();
    }

    // =============================================================
    // FUMAÇA
    // =============================================================

    private void desenharFumaca(
            Canvas canvas,
            int w,
            int h,
            float tempo
    ) {

        fumaçaPaint.setStyle(Paint.Style.FILL);
        fumaçaPaint.setColor(Color.WHITE);

        // Névoa muito suave, para não atrapalhar o login.
        fumaçaPaint.setAlpha(16);

        float movimento1 =
                (float) Math.sin(tempo * 0.32f);

        float movimento2 =
                (float) Math.sin(tempo * 0.21f + 2.0f);

        float movimento3 =
                (float) Math.sin(tempo * 0.16f + 4.0f);

        float baseY = h * 0.82f;

        desenharNuvem(
                canvas,
                w * 0.18f + movimento1 * w * 0.035f,
                baseY,
                w * 0.34f,
                h * 0.16f
        );

        desenharNuvem(
                canvas,
                w * 0.58f + movimento2 * w * 0.045f,
                h * 0.73f,
                w * 0.42f,
                h * 0.18f
        );

        fumaçaPaint.setAlpha(11);

        desenharNuvem(
                canvas,
                w * 0.38f + movimento3 * w * 0.05f,
                h * 0.48f,
                w * 0.46f,
                h * 0.14f
        );

        fumaçaPaint.setAlpha(8);

        desenharNuvem(
                canvas,
                w * 0.78f - movimento1 * w * 0.04f,
                h * 0.30f,
                w * 0.32f,
                h * 0.12f
        );
    }

    private void desenharNuvem(
            Canvas canvas,
            float x,
            float y,
            float largura,
            float altura
    ) {

        canvas.drawOval(
                new RectF(
                        x - largura * 0.50f,
                        y - altura * 0.45f,
                        x + largura * 0.50f,
                        y + altura * 0.45f
                ),
                fumaçaPaint
        );

        canvas.drawOval(
                new RectF(
                        x - largura * 0.35f,
                        y - altura * 0.70f,
                        x + largura * 0.05f,
                        y + altura * 0.25f
                ),
                fumaçaPaint
        );

        canvas.drawOval(
                new RectF(
                        x - largura * 0.05f,
                        y - altura * 0.62f,
                        x + largura * 0.42f,
                        y + altura * 0.30f
                ),
                fumaçaPaint
        );
    }

    // =============================================================
    // TEIAS
    // =============================================================

    private void desenharTeias(
            Canvas canvas,
            int w,
            int h
    ) {

        teiaPaint.setStyle(Paint.Style.STROKE);
        teiaPaint.setStrokeWidth(Math.max(1f, w / 650f));
        teiaPaint.setColor(Color.WHITE);
        teiaPaint.setAlpha(105);

        float r = Math.min(w, h) * 0.20f;

        desenharTeia(
                canvas,
                teiaPaint,
                0,
                0,
                r
        );

        canvas.save();

        canvas.rotate(
                180f,
                w / 2f,
                h / 2f
        );

        desenharTeia(
                canvas,
                teiaPaint,
                0,
                0,
                r * 0.85f
        );

        canvas.restore();
    }

    private void desenharTeia(
            Canvas canvas,
            Paint p,
            float x,
            float y,
            float r
    ) {

        p.setAlpha(105);

        for (int i = 0; i <= 6; i++) {

            double angulo =
                    Math.PI * 0.5 * i / 6.0;

            float ex =
                    x + (float) Math.cos(angulo) * r;

            float ey =
                    y + (float) Math.sin(angulo) * r;

            canvas.drawLine(
                    x,
                    y,
                    ex,
                    ey,
                    p
            );
        }

        for (int i = 1; i <= 5; i++) {

            float rr =
                    r * i / 5f;

            RectF oval =
                    new RectF(
                            x - rr,
                            y - rr,
                            x + rr,
                            y + rr
                    );

            canvas.drawArc(
                    oval,
                    0f,
                    90f,
                    false,
                    p
            );
        }
    }

    // =============================================================
    // MORCEGOS
    // =============================================================

    private void desenharMorcegos(
            Canvas canvas,
            int w,
            int h,
            float tempo
    ) {

        morcegoPaint.setStyle(Paint.Style.FILL);
        morcegoPaint.setColor(Color.WHITE);

        // Morcego 1
        desenharMorcego(
                canvas,
                w * 0.17f +
                        (float) Math.sin(tempo * 0.65f) * w * 0.025f,
                h * 0.25f +
                        (float) Math.sin(tempo * 0.90f) * h * 0.018f,
                Math.max(8f, w * 0.020f),
                tempo * 1.2f
        );

        // Morcego 2
        desenharMorcego(
                canvas,
                w * 0.82f +
                        (float) Math.sin(tempo * 0.48f + 2f) * w * 0.035f,
                h * 0.38f +
                        (float) Math.sin(tempo * 0.72f + 1f) * h * 0.025f,
                Math.max(7f, w * 0.017f),
                tempo * 1.5f
        );

        // Morcego 3 menor e mais distante
        desenharMorcego(
                canvas,
                w * 0.68f +
                        (float) Math.sin(tempo * 0.37f + 4f) * w * 0.045f,
                h * 0.18f +
                        (float) Math.sin(tempo * 0.55f) * h * 0.015f,
                Math.max(5f, w * 0.011f),
                tempo
        );
    }

    private void desenharMorcego(
            Canvas canvas,
            float x,
            float y,
            float tamanho,
            float movimento
    ) {

        canvas.save();

        float inclinacao =
                (float) Math.sin(movimento * 0.7f) * 5f;

        canvas.rotate(
                inclinacao,
                x,
                y
        );

        Path asa = new Path();

        // Asa esquerda.
        asa.moveTo(x, y);

        asa.cubicTo(
                x - tamanho * 1.2f,
                y - tamanho * 0.85f,
                x - tamanho * 2.1f,
                y - tamanho * 0.95f,
                x - tamanho * 2.8f,
                y - tamanho * 0.20f
        );

        asa.cubicTo(
                x - tamanho * 2.35f,
                y - tamanho * 0.35f,
                x - tamanho * 2.15f,
                y + tamanho * 0.10f,
                x - tamanho * 1.70f,
                y + tamanho * 0.30f
        );

        asa.cubicTo(
                x - tamanho * 1.35f,
                y + tamanho * 0.02f,
                x - tamanho * 0.75f,
                y + tamanho * 0.20f,
                x,
                y + tamanho * 0.12f
        );

        asa.close();

        canvas.drawPath(
                asa,
                morcegoPaint
        );

        // Asa direita.
        canvas.save();

        canvas.scale(
                -1f,
                1f,
                x,
                y
        );

        canvas.drawPath(
                asa,
                morcegoPaint
        );

        canvas.restore();

        // Corpo.
        canvas.drawOval(
                new RectF(
                        x - tamanho * 0.34f,
                        y - tamanho * 0.55f,
                        x + tamanho * 0.34f,
                        y + tamanho * 0.62f
                ),
                morcegoPaint
        );

        // Cabeça.
        canvas.drawCircle(
                x,
                y - tamanho * 0.55f,
                tamanho * 0.35f,
                morcegoPaint
        );

        // Orelhas.
        Path orelhas = new Path();

        orelhas.moveTo(
                x - tamanho * 0.28f,
                y - tamanho * 0.68f
        );

        orelhas.lineTo(
                x - tamanho * 0.38f,
                y - tamanho * 1.10f
        );

        orelhas.lineTo(
                x - tamanho * 0.05f,
                y - tamanho * 0.78f
        );

        orelhas.close();

        canvas.drawPath(
                orelhas,
                morcegoPaint
        );

        canvas.save();

        canvas.scale(
                -1f,
                1f,
                x,
                y
        );

        canvas.drawPath(
                orelhas,
                morcegoPaint
        );

        canvas.restore();

        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        paint.setColorFilter(filter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
