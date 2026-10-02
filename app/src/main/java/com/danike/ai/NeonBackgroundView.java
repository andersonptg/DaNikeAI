package com.danike.ai;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.Random;

public class NeonBackgroundView extends View {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    private float tempo = 0;
    private final float[] pontosX = new float[18];
    private final float[] pontosY = new float[18];

    public NeonBackgroundView(Context context) {
        super(context);

        for (int i = 0; i < pontosX.length; i++) {
            pontosX[i] = random.nextFloat();
            pontosY[i] = random.nextFloat();
        }

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        postInvalidateDelayed(30);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        float cy = h * 0.38f;

        tempo += 0.018f;

        // Fundo profundo
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(1, 5, 12));
        c.drawRect(0, 0, w, h, p);

        // Pontos neon flutuantes
        for (int i = 0; i < pontosX.length; i++) {
            float x = pontosX[i] * w;
            float y = (pontosY[i] * h + tempo * 35 * (i % 3 + 1)) % h;

            int cor = (i % 4 == 0)
                    ? Color.rgb(190, 40, 255)
                    : Color.rgb(20, 190, 255);

            p.setColor(cor);
            p.setShadowLayer(14, 0, 0, cor);

            float tamanho = 2 + (float)Math.sin(tempo * 3 + i) * 1.5f;
            c.drawCircle(x, y, Math.max(1, tamanho), p);

            p.clearShadowLayer();
        }

        // Linhas tecnológicas laterais
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.5f);
        p.setColor(Color.rgb(15, 120, 255));
        p.setShadowLayer(12, 0, 0, Color.rgb(0, 150, 255));

        Path esquerda = new Path();
        esquerda.moveTo(0, h * .18f);
        esquerda.lineTo(w * .09f, h * .24f);
        esquerda.lineTo(w * .05f, h * .36f);
        esquerda.lineTo(w * .14f, h * .43f);
        esquerda.lineTo(w * .07f, h * .54f);

        c.drawPath(esquerda, p);

        Path direita = new Path();
        direita.moveTo(w, h * .18f);
        direita.lineTo(w * .91f, h * .24f);
        direita.lineTo(w * .95f, h * .36f);
        direita.lineTo(w * .86f, h * .43f);
        direita.lineTo(w * .93f, h * .54f);

        c.drawPath(direita, p);

        p.clearShadowLayer();

        // Órbitas neon atrás do rosto
        float orbit = Math.min(w, h) * .30f;

        p.setStrokeWidth(3.5f);
        p.setColor(Color.rgb(15, 210, 255));
        p.setShadowLayer(20, 0, 0, Color.rgb(0, 180, 255));

        RectF oval1 = new RectF(
                cx - orbit * 1.35f,
                cy - orbit * .55f,
                cx + orbit * 1.35f,
                cy + orbit * .55f
        );

        c.save();
        c.rotate((float)Math.sin(tempo) * 8, cx, cy);
        c.drawOval(oval1, p);
        c.restore();

        p.setColor(Color.rgb(185, 40, 255));

        RectF oval2 = new RectF(
                cx - orbit * 1.2f,
                cy - orbit * .7f,
                cx + orbit * 1.2f,
                cy + orbit * .7f
        );

        c.save();
        c.rotate(-tempo * 18, cx, cy);
        c.drawOval(oval2, p);
        c.restore();

        p.clearShadowLayer();

        // Pequenos segmentos brilhantes das órbitas
        p.setStrokeWidth(7);

        for (int i = 0; i < 3; i++) {
            float angulo = tempo * 70 + i * 120;

            double a = Math.toRadians(angulo);

            float x = cx + (float)Math.cos(a) * orbit * 1.25f;
            float y = cy + (float)Math.sin(a) * orbit * .55f;

            p.setColor(i == 1
                    ? Color.rgb(210, 40, 255)
                    : Color.rgb(20, 220, 255));

            p.setShadowLayer(18, 0, 0, p.getColor());

            c.drawCircle(x, y, 5, p);

            p.clearShadowLayer();
        }

        // Grade futurista no chão
        p.setStrokeWidth(1.5f);
        p.setColor(Color.rgb(8, 65, 110));

        float base = h * .70f;

        for (int i = 0; i < 9; i++) {
            float y = base + i * h * .035f;
            c.drawLine(0, y, w, y, p);
        }

        for (int i = -8; i <= 8; i++) {
            float x = cx + i * w * .10f;
            c.drawLine(cx, base, x, h, p);
        }

        postInvalidateDelayed(30);
    }
}
