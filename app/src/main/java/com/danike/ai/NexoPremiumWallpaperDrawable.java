package com.danike.ai;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/** Fundo leve e vetorial: escala sem pixelizar em telefones, tablets ou janela Android. */
public final class NexoPremiumWallpaperDrawable extends Drawable {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int alpha = 255;
    @Override public void draw(Canvas canvas) {
        Rect r=getBounds(); if(r.width()<=0||r.height()<=0)return;
        int w=r.width(),h=r.height();
        p.setShader(new LinearGradient(0,0,w,h,
            new int[]{Color.rgb(2,8,22),Color.rgb(6,22,47),Color.rgb(1,6,18)},null,Shader.TileMode.CLAMP));
        p.setAlpha(alpha);canvas.drawRect(r,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);
        float d=w/360f;
        for(int i=0;i<6;i++) {
            p.setColor(Color.argb(24+i*2,19,112,221));p.setStrokeWidth((.7f+i*.12f)*d);
            Path path=new Path();path.moveTo(-w*.15f,h*(.12f+i*.20f));
            path.cubicTo(w*.22f,h*(.05f+i*.15f),w*.61f,h*(.30f+i*.13f),w*1.12f,h*(.15f+i*.18f));
            canvas.drawPath(path,p);
        }
        p.setStyle(Paint.Style.FILL);
        p.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(Math.min(w*.085f,72f*d));
        p.setColor(Color.argb(19,50,151,255));
        canvas.save();canvas.rotate(-17,w*.67f,h*.51f);
        canvas.drawText("N E X O",w*.67f,h*.51f,p);
        canvas.restore();
        p.setShader(null);
    }
    @Override public void setAlpha(int a){alpha=a;invalidateSelf();}
    @Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.OPAQUE;}
}
