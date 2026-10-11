package com.danike.ai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class DaNikeOrbView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private float fase = 0f;
    private boolean falando = false;

    private final Runnable animacao = new Runnable() {
        @Override public void run() {
            fase += falando ? 0.055f : 0.022f;
            invalidate();
            handler.postDelayed(this, 40);
        }
    };

    public DaNikeOrbView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        handler.post(animacao);
    }

    public void setFalando(boolean valor) {
        falando = valor;
        invalidate();
    }

    public boolean isFalando() {
        return falando;
    }

    private void brilho(Canvas c, float x, float y, float raio, int cor) {
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        p.setColor(cor);
        p.setShadowLayer(raio * 1.5f, 0, 0, cor);
        c.drawCircle(x, y, raio, p);
        p.clearShadowLayer();
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float s = Math.min(getWidth(), getHeight());
        if (s <= 0) return;

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f
                + (float) Math.sin(fase) * s * 0.012f;
        float r = s * 0.29f;
        float pulso = (float) ((Math.sin(fase * 2) + 1) / 2);
        int vermelho = Color.rgb(255, 55, 68);
        int azul = Color.rgb(45, 165, 220);
        int metal = Color.rgb(70, 82, 96);

        // Escudo lateral: linha azul fina e discreta.
        p.setShader(null);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(s * 0.009f);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(Color.argb(155, 55, 170, 225));
        Path escudo = new Path();
        escudo.moveTo(cx + s * 0.23f, cy - s * 0.25f);
        escudo.cubicTo(cx + s * 0.48f, cy - s * 0.20f,
                cx + s * 0.48f, cy + s * 0.15f,
                cx + s * 0.25f, cy + s * 0.31f);
        c.drawPath(escudo, p);

        // Pequenos pontos do campo de força.
        for (int i = 0; i < 3; i++) {
            float yy = cy + (i - 1) * s * 0.13f;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(s * 0.006f);
            p.setColor(Color.argb(100, 75, 190, 235));
            c.drawCircle(cx + s * 0.37f, yy, s * 0.018f, p);
        }

        // Carcaça metálica grafite.
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(
                cx - r * 0.32f, cy - r * 0.4f, r * 1.7f,
                new int[] {
                        Color.rgb(105, 117, 130),
                        Color.rgb(38, 46, 58),
                        Color.rgb(8, 12, 19)
                }, null, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r, p);
        p.setShader(null);

        // Borda metálica.
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(s * 0.012f);
        p.setColor(Color.rgb(92, 115, 135));
        c.drawCircle(cx, cy, r, p);

        // Painel frontal escuro.
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(
                cx - r * 0.2f, cy - r * 0.3f, r * 1.3f,
                new int[] {
                        Color.rgb(35, 45, 59),
                        Color.rgb(12, 17, 26),
                        Color.rgb(4, 7, 12)
                }, null, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r * 0.91f, p);
        p.setShader(null);

        // Olhos vermelhos pequenos, com pulsação suave.
        float olhoY = cy - r * 0.13f;
        float olhoW = r * 0.28f;
        float olhoH = r * (0.075f + pulso * 0.012f);
        for (int lado = -1; lado <= 1; lado += 2) {
            float ex = cx + lado * r * 0.36f;
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(75, 20, 28));
            c.drawRoundRect(new RectF(
                    ex - olhoW * 0.58f, olhoY - olhoH * 1.5f,
                    ex + olhoW * 0.58f, olhoY + olhoH * 1.5f),
                    olhoH, olhoH, p);
            p.setColor(vermelho);
            p.setShadowLayer(s * 0.012f, 0, 0, vermelho);
            c.drawRoundRect(new RectF(
                    ex - olhoW / 2, olhoY - olhoH / 2,
                    ex + olhoW / 2, olhoY + olhoH / 2),
                    olhoH, olhoH, p);
            p.clearShadowLayer();
        }

        // Sensores laterais pequenos.
        brilho(c, cx - r * 0.91f, cy + r * 0.10f,
                s * 0.018f, azul);
        brilho(c, cx + r * 0.91f, cy + r * 0.10f,
                s * 0.018f, falando ? vermelho : azul);

        // Núcleo discreto abaixo dos olhos.
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(s * 0.008f);
        p.setColor(Color.argb(170, 90, 150, 185));
        c.drawArc(new RectF(cx - r * 0.38f, cy + r * 0.02f,
                cx + r * 0.38f, cy + r * 0.66f),
                20, 140, false, p);
        brilho(c, cx, cy + r * 0.38f, s * 0.014f,
                falando ? vermelho : azul);

        // Identificação minimalista.
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        p.setTypeface(android.graphics.Typeface.create(
                "sans-serif-medium", android.graphics.Typeface.BOLD));
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(s * 0.060f);
        p.setColor(Color.rgb(220, 228, 238));
        c.drawText("DA NIKE", cx, cy + r * 0.79f, p);
    }

    @Override protected void onDetachedFromWindow() {
        handler.removeCallbacks(animacao);
        super.onDetachedFromWindow();
    }
}
