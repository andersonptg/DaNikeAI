package com.danike.ai;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

public class LoginBackgroundDrawable extends Drawable {

    private final Bitmap foto;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ponto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF destino = new RectF();

    private final float[] px = {
            .12f, .27f, .43f, .61f, .78f, .91f,
            .20f, .36f, .53f, .69f, .84f
    };

    private final float[] py = {
            .18f, .31f, .12f, .26f, .16f, .38f,
            .52f, .67f, .47f, .72f, .58f
    };

    public LoginBackgroundDrawable(Bitmap foto) {
        this.foto = foto;
        ponto.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(Canvas canvas) {
        int w = getBounds().width();
        int h = getBounds().height();

        if (w <= 0 || h <= 0 || foto == null) return;

        float escala = Math.max(
                w / (float) foto.getWidth(),
                h / (float) foto.getHeight()
        );

        float largura = foto.getWidth() * escala;
        float altura = foto.getHeight() * escala;

        float esquerda = (w - largura) / 2f;
        float topo = (h - altura) / 2f;

        destino.set(esquerda, topo, esquerda + largura, topo + altura);
        canvas.drawBitmap(foto, null, destino, paint);

        // Escurece a fotografia para dar aparência cinematográfica.

        // Pequena camada extra nas bordas.

        long tempo = SystemClock.uptimeMillis();

        for (int i = 0; i < px.length; i++) {
            float movimento =
                    ((tempo / 35f) + i * 57f) % (h + 160);

            float x = px[i] * w;
            float y = py[i] * h - movimento % (h + 180) + 90;

            if (y < -30) y += h + 60;

            float pulso =
                    0.65f + 0.35f *
                    (float)Math.sin(tempo / 420.0 + i);

            int alpha = (int)(150 * pulso);

            // halo externo
            ponto.setColor(Color.argb(alpha / 5, 0, 220, 255));
            canvas.drawCircle(x, y, 13, ponto);

            ponto.setColor(Color.argb(alpha / 3, 80, 180, 255));
            canvas.drawCircle(x, y, 7, ponto);

            // núcleo luminoso
            ponto.setColor(Color.argb(alpha, 190, 245, 255));
            canvas.drawCircle(x, y, 2.4f, ponto);
        }

        // Pontos pequenos adicionais.
        for (int i = 0; i < 9; i++) {
            float x = ((i * 137) % 1000) / 1000f * w;
            float y = ((tempo / 55f + i * 113) % (h + 100)) - 50;

            ponto.setColor(Color.argb(115, 70, 210, 255));
            canvas.drawCircle(x, y, 1.5f, ponto);
        }

        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(android.graphics.ColorFilter filter) {}

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }
}
