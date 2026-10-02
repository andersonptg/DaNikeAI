
package com.danike.ai;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.view.View;

import java.util.Random;

public class DaNikeAIFaceView extends View {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint soft = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Handler handler = new Handler();
    private final Random random = new Random();

    private boolean falando = false;
    private boolean piscando = false;

    private float boca = 0f;
    private float brilho = 0.7f;

    private float fase = 0f;

    private final Runnable animacao = new Runnable() {
        @Override
        public void run() {
            fase += 0.035f;

            brilho = 0.72f
                    + 0.22f * (float)Math.sin(System.currentTimeMillis() / 300.0);

            invalidate();
            handler.postDelayed(this, 30);
        }
    };

    private final Runnable piscarRunnable = new Runnable() {
        @Override
        public void run() {

            piscando = true;
            invalidate();

            handler.postDelayed(() -> {

                piscando = false;
                invalidate();

                long proxima = 2500 + random.nextInt(3500);
                handler.postDelayed(this, proxima);

            }, 130);
        }
    };

    public DaNikeAIFaceView(Context context) {
        super(context);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        handler.post(animacao);
        handler.postDelayed(piscarRunnable, 3000);
    }

    public void setFalando(boolean valor) {
        falando = valor;

        if (!falando) {
            boca = 0f;
        }

        invalidate();
    }

    public boolean isFalando() {
        return falando;
    }

    public void setBoca(float valor) {
        boca = Math.max(0f, Math.min(1f, valor));
        invalidate();
    }

    private int alpha(int a) {
        return Math.max(0, Math.min(255, a));
    }

    private void neonStroke(Canvas c, float largura, int cor) {
        glow.setStyle(Paint.Style.STROKE);
        glow.setStrokeWidth(largura);
        glow.setColor(Color.argb(150,
                Color.red(cor),
                Color.green(cor),
                Color.blue(cor)));
        glow.setShadowLayer(largura * 3.5f, 0, 0, cor);
        c.drawPath(new Path(), glow);
        glow.clearShadowLayer();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        float w = getWidth();
        float h = getHeight();

        float cx = w / 2f;
        float cy = h / 2f;

        float menor = Math.min(w, h);

        /*
         * A cabeça ocupa aproximadamente 60% da menor dimensão.
         */
        float rosto = menor * 0.285f;

        /*
         * =========================================================
         * FUNDO / AURA DO ROSTO
         * =========================================================
         */

        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(
                cx,
                cy,
                menor * 0.47f,
                new int[]{
                        Color.argb(80, 0, 220, 255),
                        Color.argb(35, 0, 90, 180),
                        Color.argb(0, 0, 0, 0)
                },
                null,
                Shader.TileMode.CLAMP
        ));

        c.drawCircle(cx, cy, menor * 0.48f, p);
        p.setShader(null);

        /*
         * =========================================================
         * ANÉIS HOLOGRÁFICOS
         * =========================================================
         */

        float rot = fase * 22f;

        c.save();
        c.rotate(rot, cx, cy);

        for (int i = 0; i < 4; i++) {

            float raio = menor * (0.345f + i * 0.038f);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(menor * 0.006f);

            int cor;

            if (i % 2 == 0) {
                cor = Color.rgb(0, 225, 255);
            } else {
                cor = Color.rgb(145, 45, 255);
            }

            p.setColor(Color.argb(145 - i * 20,
                    Color.red(cor),
                    Color.green(cor),
                    Color.blue(cor)));

            RectF r = new RectF(
                    cx - raio,
                    cy - raio,
                    cx + raio,
                    cy + raio
            );

            c.drawArc(r, 12 + i * 20, 105, false, p);
            c.drawArc(r, 205 + i * 15, 75, false, p);
        }

        c.restore();

        /*
         * Anéis dourados pequenos.
         */
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(menor * 0.004f);
        p.setColor(Color.argb(180, 255, 205, 55));

        float ouro = menor * 0.39f;

        c.drawArc(
                new RectF(cx - ouro, cy - ouro,
                        cx + ouro, cy + ouro),
                300,
                45,
                false,
                p
        );

        c.drawArc(
                new RectF(cx - ouro, cy - ouro,
                        cx + ouro, cy + ouro),
                110,
                35,
                false,
                p
        );

        /*
         * =========================================================
         * CABELO — FORMA ARTIFICIAL / NEON
         * =========================================================
         */

        Path cabelo = new Path();

        cabelo.moveTo(
                cx - rosto * 0.82f,
                cy - rosto * 0.95f
        );

        cabelo.cubicTo(
                cx - rosto * 1.55f,
                cy - rosto * 1.05f,
                cx - rosto * 1.58f,
                cy + rosto * 0.25f,
                cx - rosto * 1.30f,
                cy + rosto * 1.55f
        );

        cabelo.cubicTo(
                cx - rosto * 1.05f,
                cy + rosto * 1.82f,
                cx - rosto * 0.72f,
                cy + rosto * 1.45f,
                cx - rosto * 0.64f,
                cy + rosto * 0.72f
        );

        cabelo.lineTo(
                cx + rosto * 0.64f,
                cy + rosto * 0.72f
        );

        cabelo.cubicTo(
                cx + rosto * 0.72f,
                cy + rosto * 1.45f,
                cx + rosto * 1.05f,
                cy + rosto * 1.82f,
                cx + rosto * 1.30f,
                cy + rosto * 1.55f
        );

        cabelo.cubicTo(
                cx + rosto * 1.58f,
                cy + rosto * 0.25f,
                cx + rosto * 1.55f,
                cy - rosto * 1.05f,
                cx + rosto * 0.82f,
                cy - rosto * 0.95f
        );

        cabelo.close();

        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(2, 12, 28));
        p.setShadowLayer(
                menor * 0.055f,
                0,
                0,
                Color.rgb(0, 185, 255)
        );

        c.drawPath(cabelo, p);
        p.clearShadowLayer();

        /*
         * Fios luminosos do cabelo.
         */
        for (int i = 0; i < 7; i++) {

            float lado = (i % 2 == 0) ? -1f : 1f;

            float x = cx + lado * rosto *
                    (0.68f + (i / 2) * 0.13f);

            Path fio = new Path();

            fio.moveTo(
                    x,
                    cy - rosto * 0.83f
            );

            fio.cubicTo(
                    x - lado * rosto * 0.20f,
                    cy - rosto * 0.20f,
                    x + lado * rosto * 0.18f,
                    cy + rosto * 0.72f,
                    x + lado * rosto * 0.05f,
                    cy + rosto * 1.48f
            );

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(menor * 0.008f);

            if (i % 3 == 0) {
                p.setColor(Color.rgb(0, 220, 255));
            } else if (i % 3 == 1) {
                p.setColor(Color.rgb(45, 110, 255));
            } else {
                p.setColor(Color.rgb(180, 45, 255));
            }

            p.setShadowLayer(
                    menor * 0.018f,
                    0,
                    0,
                    p.getColor()
            );

            c.drawPath(fio, p);
            p.clearShadowLayer();
        }

        /*
         * =========================================================
         * ROSTO ARTIFICIAL
         * =========================================================
         */

        Path face = new Path();

        face.moveTo(
                cx,
                cy - rosto * 0.92f
        );

        face.cubicTo(
                cx - rosto * 0.70f,
                cy - rosto * 0.98f,
                cx - rosto * 0.98f,
                cy - rosto * 0.50f,
                cx - rosto * 0.88f,
                cy + rosto * 0.25f
        );

        face.cubicTo(
                cx - rosto * 0.82f,
                cy + rosto * 0.82f,
                cx - rosto * 0.40f,
                cy + rosto * 1.08f,
                cx,
                cy + rosto * 1.15f
        );

        face.cubicTo(
                cx + rosto * 0.40f,
                cy + rosto * 1.08f,
                cx + rosto * 0.82f,
                cy + rosto * 0.82f,
                cx + rosto * 0.88f,
                cy + rosto * 0.25f
        );

        face.cubicTo(
                cx + rosto * 0.98f,
                cy - rosto * 0.50f,
                cx + rosto * 0.70f,
                cy - rosto * 0.98f,
                cx,
                cy - rosto * 0.92f
        );

        face.close();

        p.setStyle(Paint.Style.FILL);

        p.setShader(new LinearGradient(
                cx,
                cy - rosto,
                cx,
                cy + rosto,
                Color.rgb(24, 55, 85),
                Color.rgb(5, 17, 31),
                Shader.TileMode.CLAMP
        ));

        p.setShadowLayer(
                menor * 0.035f,
                0,
                0,
                Color.rgb(0, 210, 255)
        );

        c.drawPath(face, p);

        p.clearShadowLayer();
        p.setShader(null);

        /*
         * Contorno luminoso do rosto.
         */
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(menor * 0.009f);
        p.setColor(Color.rgb(0, 220, 255));

        p.setShadowLayer(
                menor * 0.025f,
                0,
                0,
                Color.rgb(0, 220, 255)
        );

        c.drawPath(face, p);
        p.clearShadowLayer();

        /*
         * =========================================================
         * LATERAIS TECNOLÓGICAS DO ROSTO
         * =========================================================
         */

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(menor * 0.006f);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(Color.rgb(0, 180, 255));

        Path circuitoL = new Path();

        circuitoL.moveTo(
                cx - rosto * 0.82f,
                cy - rosto * 0.05f
        );

        circuitoL.lineTo(
                cx - rosto * 0.67f,
                cy + rosto * 0.02f
        );

        circuitoL.lineTo(
                cx - rosto * 0.74f,
                cy + rosto * 0.18f
        );

        circuitoL.lineTo(
                cx - rosto * 0.61f,
                cy + rosto * 0.26f
        );

        c.drawPath(circuitoL, p);

        Path circuitoR = new Path();

        circuitoR.moveTo(
                cx + rosto * 0.82f,
                cy - rosto * 0.05f
        );

        circuitoR.lineTo(
                cx + rosto * 0.67f,
                cy + rosto * 0.02f
        );

        circuitoR.lineTo(
                cx + rosto * 0.74f,
                cy + rosto * 0.18f
        );

        circuitoR.lineTo(
                cx + rosto * 0.61f,
                cy + rosto * 0.26f
        );

        c.drawPath(circuitoR, p);

        /*
         * Pontos dos circuitos.
         */
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(0, 240, 255));

        c.drawCircle(
                cx - rosto * 0.61f,
                cy + rosto * 0.26f,
                menor * 0.010f,
                p
        );

        c.drawCircle(
                cx + rosto * 0.61f,
                cy + rosto * 0.26f,
                menor * 0.010f,
                p
        );

        /*
         * =========================================================
         * FONE / INTERFACE NEURAL
         * =========================================================
         */

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(menor * 0.015f);
        p.setColor(Color.rgb(0, 225, 255));

        RectF foneL = new RectF(
                cx - rosto * 1.02f,
                cy - rosto * 0.38f,
                cx - rosto * 0.68f,
                cy + rosto * 0.38f
        );

        RectF foneR = new RectF(
                cx + rosto * 0.68f,
                cy - rosto * 0.38f,
                cx + rosto * 1.02f,
                cy + rosto * 0.38f
        );

        p.setShadowLayer(
                menor * 0.025f,
                0,
                0,
                Color.rgb(0, 220, 255)
        );

        c.drawArc(foneL, 70, 220, false, p);
        c.drawArc(foneR, -110, 220, false, p);

        p.clearShadowLayer();

        /*
         * Núcleos dourados dos fones.
         */
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(menor * 0.009f);
        p.setColor(Color.rgb(255, 205, 70));

        c.drawOval(
                new RectF(
                        cx - rosto * 0.98f,
                        cy - rosto * 0.22f,
                        cx - rosto * 0.78f,
                        cy + rosto * 0.22f
                ),
                p
        );

        c.drawOval(
                new RectF(
                        cx + rosto * 0.78f,
                        cy - rosto * 0.22f,
                        cx + rosto * 0.98f,
                        cy + rosto * 0.22f
                ),
                p
        );

        /*
         * =========================================================
         * OLHOS NEON
         * =========================================================
         */

        float olhoX = rosto * 0.40f;
        float olhoY = cy - rosto * 0.13f;

        if (piscando) {

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(menor * 0.009f);
            p.setColor(Color.rgb(0, 220, 255));

            c.drawLine(
                    cx - olhoX - rosto * 0.17f,
                    olhoY,
                    cx - olhoX + rosto * 0.17f,
                    olhoY,
                    p
            );

            c.drawLine(
                    cx + olhoX - rosto * 0.17f,
                    olhoY,
                    cx + olhoX + rosto * 0.17f,
                    olhoY,
                    p
            );

        } else {

            int corOlho = Color.rgb(0, 225, 255);

            /*
             * Aura.
             */
            glow.setStyle(Paint.Style.FILL);
            glow.setColor(Color.argb(
                    alpha((int)(65 + brilho * 65)),
                    0,
                    210,
                    255
            ));

            glow.setShadowLayer(
                    menor * 0.065f,
                    0,
                    0,
                    Color.rgb(0, 220, 255)
            );

            c.drawOval(
                    new RectF(
                            cx - olhoX - rosto * 0.23f,
                            olhoY - rosto * 0.12f,
                            cx - olhoX + rosto * 0.23f,
                            olhoY + rosto * 0.12f
                    ),
                    glow
            );

            c.drawOval(
                    new RectF(
                            cx + olhoX - rosto * 0.23f,
                            olhoY - rosto * 0.12f,
                            cx + olhoX + rosto * 0.23f,
                            olhoY + rosto * 0.12f
                    ),
                    glow
            );

            glow.clearShadowLayer();

            /*
             * Íris.
             */
            p.setStyle(Paint.Style.FILL);
            p.setColor(corOlho);

            c.drawOval(
                    new RectF(
                            cx - olhoX - rosto * 0.18f,
                            olhoY - rosto * 0.085f,
                            cx - olhoX + rosto * 0.18f,
                            olhoY + rosto * 0.085f
                    ),
                    p
            );

            c.drawOval(
                    new RectF(
                            cx + olhoX - rosto * 0.18f,
                            olhoY - rosto * 0.085f,
                            cx + olhoX + rosto * 0.18f,
                            olhoY + rosto * 0.085f
                    ),
                    p
            );

            /*
             * Pupilas artificiais.
             */
            p.setColor(Color.rgb(0, 12, 25));

            c.drawCircle(
                    cx - olhoX,
                    olhoY,
                    rosto * 0.075f,
                    p
            );

            c.drawCircle(
                    cx + olhoX,
                    olhoY,
                    rosto * 0.075f,
                    p
            );

            /*
             * Reflexo branco.
             */
            p.setColor(Color.WHITE);

            c.drawCircle(
                    cx - olhoX - rosto * 0.035f,
                    olhoY - rosto * 0.035f,
                    rosto * 0.022f,
                    p
            );

            c.drawCircle(
                    cx + olhoX - rosto * 0.035f,
                    olhoY - rosto * 0.035f,
                    rosto * 0.022f,
                    p
            );
        }

        /*
         * =========================================================
         * SOBRANCELHAS / LINHAS DE EXPRESSÃO
         * =========================================================
         */

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(menor * 0.008f);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(Color.rgb(60, 160, 210));

        RectF sobrE = new RectF(
                cx - rosto * 0.73f,
                cy - rosto * 0.62f,
                cx - rosto * 0.08f,
                cy - rosto * 0.27f
        );

        RectF sobrD = new RectF(
                cx + rosto * 0.08f,
                cy - rosto * 0.62f,
                cx + rosto * 0.73f,
                cy - rosto * 0.27f
        );

        c.drawArc(sobrE, 200, 140, false, p);
        c.drawArc(sobrD, 200, 140, false, p);

        /*
         * =========================================================
         * NARIZ DIGITAL
         * =========================================================
         */

        p.setColor(Color.rgb(45, 130, 170));
        p.setStrokeWidth(menor * 0.006f);

        Path nariz = new Path();

        nariz.moveTo(
                cx,
                cy + rosto * 0.01f
        );

        nariz.lineTo(
                cx - rosto * 0.09f,
                cy + rosto * 0.29f
        );

        nariz.lineTo(
                cx + rosto * 0.08f,
                cy + rosto * 0.29f
        );

        c.drawPath(nariz, p);

        /*
         * Pequenas linhas de circuito no nariz.
         */
        p.setColor(Color.rgb(0, 215, 255));

        c.drawCircle(
                cx - rosto * 0.09f,
                cy + rosto * 0.29f,
                menor * 0.008f,
                p
        );

        c.drawCircle(
                cx + rosto * 0.08f,
                cy + rosto * 0.29f,
                menor * 0.008f,
                p
        );

        /*
         * =========================================================
         * BOCA NEON
         * =========================================================
         */

        float bocaY = cy + rosto * 0.59f;

        float abertura;

        if (falando) {
            abertura = rosto * (0.025f + boca * 0.20f);
        } else {
            abertura = rosto * 0.018f;
        }

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(menor * 0.010f);
        p.setColor(
                falando
                        ? Color.rgb(255, 205, 70)
                        : Color.rgb(0, 225, 255)
        );

        p.setShadowLayer(
                menor * 0.018f,
                0,
                0,
                p.getColor()
        );

        RectF bocaRect = new RectF(
                cx - rosto * 0.30f,
                bocaY - abertura,
                cx + rosto * 0.30f,
                bocaY + abertura
        );

        c.drawOval(bocaRect, p);

        p.clearShadowLayer();

        /*
         * =========================================================
         * DETALHES DOURADOS / CHIP CENTRAL
         * =========================================================
         */

        float chipY = cy + rosto * 1.08f;

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(menor * 0.008f);
        p.setColor(Color.rgb(255, 205, 65));

        Path chip = new Path();

        chip.moveTo(cx, chipY - rosto * 0.13f);
        chip.lineTo(cx + rosto * 0.11f, chipY);
        chip.lineTo(cx, chipY + rosto * 0.13f);
        chip.lineTo(cx - rosto * 0.11f, chipY);
        chip.close();

        p.setShadowLayer(
                menor * 0.020f,
                0,
                0,
                Color.rgb(255, 190, 40)
        );

        c.drawPath(chip, p);

        p.clearShadowLayer();

        /*
         * =========================================================
         * PONTOS DE ENERGIA FLUTUANDO
         * =========================================================
         */

        for (int i = 0; i < 8; i++) {

            double a =
                    fase * (0.8 + i * 0.05)
                    + i * 0.9;

            float raio = menor * (0.31f + (i % 3) * 0.045f);

            float px = cx + (float)Math.cos(a) * raio;
            float py = cy + (float)Math.sin(a) * raio;

            int cor;

            if (i % 3 == 0) {
                cor = Color.rgb(0, 230, 255);
            } else if (i % 3 == 1) {
                cor = Color.rgb(170, 50, 255);
            } else {
                cor = Color.rgb(255, 205, 65);
            }

            glow.setStyle(Paint.Style.FILL);
            glow.setColor(Color.argb(
                    170,
                    Color.red(cor),
                    Color.green(cor),
                    Color.blue(cor)
            ));

            glow.setShadowLayer(
                    menor * 0.020f,
                    0,
                    0,
                    cor
            );

            c.drawCircle(
                    px,
                    py,
                    menor * 0.010f,
                    glow
            );

            glow.clearShadowLayer();
        }
    }

    @Override
    protected void onDetachedFromWindow() {

        handler.removeCallbacksAndMessages(null);

        super.onDetachedFromWindow();
    }
}
