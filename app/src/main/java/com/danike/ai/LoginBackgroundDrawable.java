package com.danike.ai;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.PixelFormat;
import android.graphics.ColorFilter;

public class LoginBackgroundDrawable extends Drawable {

    private final Bitmap foto;
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private final RectF destino = new RectF();

    public LoginBackgroundDrawable(Bitmap foto) {
        this.foto = foto;
    }

    @Override
    public void draw(Canvas canvas) {
        int w = getBounds().width();
        int h = getBounds().height();

        if (foto == null || foto.isRecycled() || w <= 0 || h <= 0) {
            canvas.drawColor(android.graphics.Color.BLACK);
            return;
        }

        float escala = Math.max(
                w / (float) foto.getWidth(),
                h / (float) foto.getHeight()
        );

        float largura = foto.getWidth() * escala;
        float altura = foto.getHeight() * escala;
        float esquerda = (w - largura) / 2f;
        float topo = (h - altura) / 2f;

        destino.set(
                esquerda,
                topo,
                esquerda + largura,
                topo + altura
        );

        canvas.drawBitmap(foto, null, destino, paint);
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
