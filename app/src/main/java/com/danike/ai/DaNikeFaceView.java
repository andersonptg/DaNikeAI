package com.danike.ai;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class DaNikeFaceView extends View {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float pulse = 0f;
    private float blink = 1f;

    private boolean thinking = false;
    private boolean listening = false;
    private boolean speaking = false;

    private final int BLUE = Color.rgb(20, 220, 255);
    private final int CYAN = Color.rgb(0, 150, 255);
    private final int GOLD = Color.rgb(255, 190, 40);
    private final int DARK = Color.rgb(2, 6, 16);

    public DaNikeFaceView(Context context) {
        super(context);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(2400);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());

        animator.addUpdateListener(a -> {
            pulse = (float) a.getAnimatedValue();
            invalidate();
        });

        animator.start();

        iniciarPiscar();
    }

    private void iniciarPiscar() {
        postDelayed(new Runnable() {
            @Override
            public void run() {

                if (!thinking && !listening) {

                    blink = 0.08f;
                    invalidate();

                    postDelayed(() -> {
                        blink = 1f;
                        invalidate();
                    }, 120);

                    postDelayed(
                            this,
                            3000 + (long)(Math.random() * 3000)
                    );
                } else {
                    postDelayed(this, 1800);
                }
            }
        }, 3000);
    }

    public void setThinking(boolean value) {
        thinking = value;
        invalidate();
    }

    public void setListening(boolean value) {
        listening = value;
        invalidate();
    }

    public void setSpeaking(boolean value) {
        speaking = value;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        float w = getWidth();
        float h = getHeight();

        float cx = w / 2f;
        float cy = h / 2f;

        float r = Math.min(w, h) * 0.40f;

        float anim = (float)Math.sin(pulse * Math.PI * 2.0);

        int neon = thinking ? GOLD : BLUE;

        /*
         * FUNDO
         */
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(1, 5, 13));
        c.drawRect(0, 0, w, h, p);

        /*
         * BRILHO CENTRAL
         */
        p.setColor(Color.rgb(3, 18, 32));
        p.setShadowLayer(
                55 + anim * 10,
                0,
                0,
                neon
        );

        c.drawCircle(
                cx,
                cy,
                r * 1.05f,
                p
        );

        p.clearShadowLayer();

        /*
         * CABELO EXTERNO
         */
        desenharCabelo(
                c,
                cx,
                cy,
                r,
                neon,
                anim
        );

        /*
         * ORELHAS / LATERAIS NEON
         */
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(neon);
        p.setShadowLayer(
                listening ? 28 : 16,
                0,
                0,
                neon
        );

        c.drawArc(
                new RectF(
                        cx - r * 1.12f,
                        cy - r * .62f,
                        cx - r * .78f,
                        cy + r * .72f
                ),
                80,
                220,
                false,
                p
        );

        c.drawArc(
                new RectF(
                        cx + r * .78f,
                        cy - r * .62f,
                        cx + r * 1.12f,
                        cy + r * .72f
                ),
                -120,
                220,
                false,
                p
        );

        p.clearShadowLayer();

        /*
         * PESCOÇO
         */
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(7, 17, 29));

        Path pescoco = new Path();

        pescoco.moveTo(
                cx - r * .30f,
                cy + r * .62f
        );

        pescoco.lineTo(
                cx - r * .22f,
                cy + r * .95f
        );

        pescoco.lineTo(
                cx + r * .22f,
                cy + r * .95f
        );

        pescoco.lineTo(
                cx + r * .30f,
                cy + r * .62f
        );

        pescoco.close();

        c.drawPath(pescoco, p);

        /*
         * ROSTO
         */
        p.setColor(Color.rgb(8, 18, 30));

        p.setShadowLayer(
                22,
                0,
                4,
                neon
        );

        Path rosto = new Path();

        rosto.moveTo(
                cx,
                cy - r * .78f
        );

        rosto.cubicTo(
                cx - r * .52f,
                cy - r * .76f,
                cx - r * .70f,
                cy - r * .25f,
                cx - r * .60f,
                cy + r * .34f
        );

        rosto.cubicTo(
                cx - r * .50f,
                cy + r * .70f,
                cx - r * .20f,
                cy + r * .83f,
                cx,
                cy + r * .88f
        );

        rosto.cubicTo(
                cx + r * .20f,
                cy + r * .83f,
                cx + r * .50f,
                cy + r * .70f,
                cx + r * .60f,
                cy + r * .34f
        );

        rosto.cubicTo(
                cx + r * .70f,
                cy - r * .25f,
                cx + r * .52f,
                cy - r * .76f,
                cx,
                cy - r * .78f
        );

        rosto.close();

        c.drawPath(rosto, p);

        p.clearShadowLayer();

        /*
         * CONTORNO DO ROSTO
         */
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3.5f);
        p.setColor(neon);

        p.setShadowLayer(
                listening ? 25 : 13,
                0,
                0,
                neon
        );

        c.drawPath(rosto, p);

        p.clearShadowLayer();

        /*
         * TESTA / LINHAS TECNOLÓGICAS
         */
        p.setStrokeWidth(2);

        c.drawLine(
                cx - r * .23f,
                cy - r * .60f,
                cx - r * .08f,
                cy - r * .67f,
                p
        );

        c.drawLine(
                cx + r * .23f,
                cy - r * .60f,
                cx + r * .08f,
                cy - r * .67f,
                p
        );

        /*
         * SOBRANCELHAS
         */
        desenharSobrancelha(
                c,
                cx - r * .27f,
                cy - r * .18f,
                r,
                neon
        );

        desenharSobrancelha(
                c,
                cx + r * .27f,
                cy - r * .18f,
                r,
                neon
        );

        /*
         * OLHOS
         */
        float olhoY = cy - r * .07f;
        float olhoX = r * .27f;

        desenharOlho(
                c,
                cx - olhoX,
                olhoY,
                r * .135f,
                neon
        );

        desenharOlho(
                c,
                cx + olhoX,
                olhoY,
                r * .135f,
                neon
        );

        /*
         * NARIZ
         */
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.5f);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(Color.rgb(50, 170, 205));

        Path nariz = new Path();

        nariz.moveTo(
                cx,
                cy + r * .04f
        );

        nariz.lineTo(
                cx - r * .055f,
                cy + r * .29f
        );

        nariz.quadTo(
                cx,
                cy + r * .34f,
                cx + r * .055f,
                cy + r * .29f
        );

        c.drawPath(nariz, p);

        /*
         * BOCA
         */
        desenharBoca(
                c,
                cx,
                cy + r * .49f,
                r,
                neon
        );

        /*
         * MARCAS LATERAIS
         */
        desenharMarca(
                c,
                cx - r * .58f,
                cy + r * .20f,
                neon,
                false
        );

        desenharMarca(
                c,
                cx + r * .58f,
                cy + r * .20f,
                neon,
                true
        );

        /*
         * ANEL EXTERNO
         */
        desenharAneis(
                c,
                cx,
                cy,
                r,
                neon,
                anim
        );

        /*
         * INDICADOR DE ESCUTA
         */
        if (listening) {

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);

            float onda = r * (
                    1.15f +
                    0.08f *
                    (float)Math.sin(pulse * Math.PI * 2)
            );

            p.setColor(BLUE);
            p.setShadowLayer(25, 0, 0, BLUE);

            c.drawCircle(
                    cx,
                    cy,
                    onda,
                    p
            );

            p.clearShadowLayer();
        }

        /*
         * INDICADOR DE PENSAMENTO
         */
        if (thinking) {

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(4);
            p.setColor(GOLD);

            p.setShadowLayer(
                    22,
                    0,
                    0,
                    GOLD
            );

            RectF pensar = new RectF(
                    cx - r * 1.28f,
                    cy - r * 1.28f,
                    cx + r * 1.28f,
                    cy + r * 1.28f
            );

            c.drawArc(
                    pensar,
                    pulse * 360f,
                    105,
                    false,
                    p
            );

            p.clearShadowLayer();
        }

        /*
         * ANIMAÇÃO DA FALA
         */
        if (speaking) {
            postInvalidateDelayed(35);
        }
    }

    private void desenharCabelo(
            Canvas c,
            float cx,
            float cy,
            float r,
            int neon,
            float anim
    ) {

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);

        /*
         * MASSA PRINCIPAL DO CABELO
         */
        p.setStrokeWidth(r * .20f);
        p.setColor(Color.rgb(4, 13, 27));

        Path cabelo = new Path();

        cabelo.moveTo(
                cx - r * .62f,
                cy + r * .58f
        );

        cabelo.cubicTo(
                cx - r * 1.18f,
                cy + r * .20f,
                cx - r * 1.05f,
                cy - r * .88f,
                cx - r * .35f,
                cy - r * 1.02f
        );

        cabelo.cubicTo(
                cx,
                cy - r * 1.18f,
                cx + r * .70f,
                cy - r * 1.03f,
                cx + r * 1.05f,
                cy - r * .50f
        );

        cabelo.cubicTo(
                cx + r * 1.16f,
                cy - r * .08f,
                cx + r * 1.05f,
                cy + r * .50f,
                cx + r * .64f,
                cy + r * .67f
        );

        c.drawPath(cabelo, p);

        /*
         * FIOS NEON
         */
        p.setStrokeWidth(3);
        p.setColor(neon);
        p.setShadowLayer(
                15 + anim * 8,
                0,
                0,
                neon
        );

        for (int i = 0; i < 7; i++) {

            float lado = i % 2 == 0 ? -1f : 1f;

            float x = cx + lado * r * (
                    .48f + (i % 3) * .16f
            );

            Path fio = new Path();

            fio.moveTo(
                    x,
                    cy - r * .70f
            );

            fio.cubicTo(
                    x + lado * r * .30f,
                    cy - r * .40f,
                    x + lado * r * .25f,
                    cy + r * .25f,
                    x + lado * r * .45f,
                    cy + r * .75f
            );

            c.drawPath(fio, p);
        }

        p.clearShadowLayer();
    }

    private void desenharSobrancelha(
            Canvas c,
            float x,
            float y,
            float r,
            int cor
    ) {

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(5);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(cor);

        p.setShadowLayer(
                12,
                0,
                0,
                cor
        );

        boolean esquerda = x < getWidth() / 2f;

        RectF rect;

        if (esquerda) {
            rect = new RectF(
                    x - r * .23f,
                    y - r * .05f,
                    x + r * .23f,
                    y + r * .12f
            );
        } else {
            rect = new RectF(
                    x - r * .23f,
                    y + r * .05f,
                    x + r * .23f,
                    y - r * .12f
            );
        }

        c.drawArc(
                rect,
                200,
                140,
                false,
                p
        );

        p.clearShadowLayer();
    }

    private void desenharOlho(
            Canvas c,
            float x,
            float y,
            float r,
            int cor
    ) {

        p.setStyle(Paint.Style.FILL);

        /*
         * OLHO ESCURO
         */
        p.setColor(Color.rgb(2, 12, 24));

        p.setShadowLayer(
                28,
                0,
                0,
                cor
        );

        c.drawOval(
                new RectF(
                        x - r * 1.65f,
                        y - r * blink,
                        x + r * 1.65f,
                        y + r * blink
                ),
                p
        );

        /*
         * ÍRIS
         */
        p.setColor(cor);

        p.setShadowLayer(
                22,
                0,
                0,
                cor
        );

        float iris = r * (.95f + .12f *
                (float)Math.sin(pulse * Math.PI * 2));

        c.drawCircle(
                x,
                y,
                iris,
                p
        );

        p.clearShadowLayer();

        /*
         * CENTRO
         */
        p.setColor(Color.rgb(0, 22, 35));

        c.drawCircle(
                x,
                y,
                r * .45f,
                p
        );

        /*
         * REFLEXO
         */
        p.setColor(Color.WHITE);

        c.drawCircle(
                x - r * .32f,
                y - r * .32f,
                r * .20f,
                p
        );

        /*
         * ANEL
         */
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.5f);
        p.setColor(cor);

        c.drawCircle(
                x,
                y,
                r * 1.30f,
                p
        );
    }

    private void desenharBoca(
            Canvas c,
            float x,
            float y,
            float r,
            int cor
    ) {

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);

        p.setColor(cor);

        if (speaking) {

            float abertura =
                    r * (
                            .045f +
                            .040f *
                            (float)Math.abs(
                                    Math.sin(
                                            System.currentTimeMillis()
                                                    / 90.0
                                    )
                            )
                    );

            p.setStrokeWidth(5);

            p.setShadowLayer(
                    16,
                    0,
                    0,
                    cor
            );

            c.drawOval(
                    new RectF(
                            x - r * .18f,
                            y - abertura,
                            x + r * .18f,
                            y + abertura
                    ),
                    p
            );

            p.clearShadowLayer();

            postInvalidateDelayed(40);

        } else {

            p.setStrokeWidth(3);

            Path boca = new Path();

            boca.moveTo(
                    x - r * .16f,
                    y
            );

            boca.quadTo(
                    x,
                    y + r * .055f,
                    x + r * .16f,
                    y
            );

            c.drawPath(
                    boca,
                    p
            );
        }
    }

    private void desenharMarca(
            Canvas c,
            float x,
            float y,
            int cor,
            boolean direita
    ) {

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.5f);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(cor);

        float sinal = direita ? 1f : -1f;

        c.drawLine(
                x,
                y,
                x + sinal * 18,
                y,
                p
        );

        c.drawLine(
                x + sinal * 7,
                y + 7,
                x + sinal * 25,
                y + 7,
                p
        );

        c.drawCircle(
                x + sinal * 31,
                y + 3,
                3,
                p
        );
    }

    private void desenharAneis(
            Canvas c,
            float cx,
            float cy,
            float r,
            int cor,
            float anim
    ) {

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);

        p.setColor(cor);

        p.setStrokeWidth(3);

        p.setShadowLayer(
                18 + anim * 8,
                0,
                0,
                cor
        );

        RectF anel = new RectF(
                cx - r * 1.16f,
                cy - r * 1.16f,
                cx + r * 1.16f,
                cy + r * 1.16f
        );

        c.drawArc(
                anel,
                -65 + anim * 15,
                70,
                false,
                p
        );

        c.drawArc(
                anel,
                115 + anim * 15,
                70,
                false,
                p
        );

        p.clearShadowLayer();

        /*
         * PEQUENOS PONTOS DE CIRCUITO
         */
        p.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 4; i++) {

            double ang =
                    (pulse * Math.PI * 2)
                    + i * Math.PI / 2;

            float x =
                    cx +
                    (float)Math.cos(ang) *
                    r * 1.16f;

            float y =
                    cy +
                    (float)Math.sin(ang) *
                    r * 1.16f;

            c.drawCircle(
                    x,
                    y,
                    4,
                    p
            );
        }
    }
}
