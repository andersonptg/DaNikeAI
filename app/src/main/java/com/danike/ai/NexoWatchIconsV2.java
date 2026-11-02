package com.danike.ai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

/** Ícones vetoriais NEXO: nenhum emoji, nenhuma fonte/biblioteca externa. */
public final class NexoWatchIconsV2 {
    private NexoWatchIconsV2() {}
    private static int dp(Context c, float v) { return Math.round(v * c.getResources().getDisplayMetrics().density); }
    public static Drawable icon(String tipo, int cor) { return new Glyph(tipo, cor); }

    public static void aplicar(Context c, TextView view, String rotulo) {
        String texto = rotulo == null ? "" : rotulo;
        String k = texto.toLowerCase(java.util.Locale.ROOT);
        String tipo = "play";
        if (k.contains("convid") || k.contains("particip") || k.contains("assistir junto")) tipo="users";
        else if (k.contains("chat") || k.contains("mensag")) tipo="chat";
        else if (k.contains("ajuste") || k.contains("configura")) tipo="gear";
        else if (k.contains("sair")) tipo="exit";
        else if (k.contains("microfone")) tipo="mic";
        else if (k.contains("câmera")) tipo="camera";
        else if (k.contains("reagir") || k.contains("reaç") || k.contains("reac")) tipo="heart";
        else if (k.contains("compartilh")) tipo="share";
        else if (k.contains("sincron")) tipo="sync";
        else if (k.contains("criar")) tipo="plus";
        else if (k.contains("pausar")) tipo="pause";
        else if (k.contains("entrar")) tipo="play";
        else if (k.contains("pronto") || k.contains("assistir")) tipo="play";
        else if (texto.contains("‹")) tipo="back";
        else if (texto.contains("➤")) tipo="send";
        if (tipo.equals("back")) texto="";
        else if (tipo.equals("send") && texto.length()<4) texto="";
        else texto = texto.replaceFirst("^[^\\p{L}\\p{N}]+", "").trim();
        view.setText(texto);
        Drawable d=icon(tipo, Color.WHITE);
        int s=dp(c,20); d.setBounds(0,0,s,s);
        view.setCompoundDrawables(d,null,null,null);
        view.setCompoundDrawablePadding(dp(c,7));
        view.setGravity(android.view.Gravity.CENTER);
        view.setContentDescription(texto.isEmpty() ? (tipo.equals("back")?"Voltar":"Enviar mensagem") : texto);
    }
    private static final class Glyph extends Drawable {
        final String name; final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        final int color;
        Glyph(String name,int color){this.name=name;this.color=color;}
        @Override public void draw(Canvas canvas) {
            int save=canvas.save();
            canvas.translate(getBounds().left,getBounds().top);
            canvas.scale(getBounds().width()/24f,getBounds().height()/24f);
            p.setColor(color);p.setStrokeWidth(2.05f);p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);
            Path path=new Path();
            switch(name){
                case "play": p.setStyle(Paint.Style.FILL);path.moveTo(8,5);path.lineTo(19,12);path.lineTo(8,19);path.close();canvas.drawPath(path,p);break;
                case "pause":p.setStyle(Paint.Style.FILL);canvas.drawRoundRect(new RectF(7,5,10.5f,19),1,1,p);canvas.drawRoundRect(new RectF(14,5,17.5f,19),1,1,p);break;
                case "plus":canvas.drawLine(12,5,12,19,p);canvas.drawLine(5,12,19,12,p);break;
                case "back":canvas.drawLine(19,12,5,12,p);canvas.drawLine(5,12,12,5,p);canvas.drawLine(5,12,12,19,p);break;
                case "users":canvas.drawCircle(9,8,3,p);canvas.drawArc(new RectF(2,14,16,25),195,150,false,p);canvas.drawArc(new RectF(14,5,21,12),280,165,false,p);canvas.drawArc(new RectF(15,13,23,22),230,135,false,p);break;
                case "chat":canvas.drawRoundRect(new RectF(3,4,21,17),4,4,p);path.moveTo(8,17);path.lineTo(6,21);path.lineTo(14,17);canvas.drawPath(path,p);break;
                case "share":canvas.drawLine(8,12,17,7,p);canvas.drawLine(8,12,17,18,p);canvas.drawCircle(6,12,2.1f,p);canvas.drawCircle(19,6,2.1f,p);canvas.drawCircle(19,19,2.1f,p);break;
                case "gear":canvas.drawCircle(12,12,4,p);canvas.drawCircle(12,12,8,p);for(int i=0;i<8;i++){double a=Math.PI*i/4;canvas.drawLine(12+(float)Math.cos(a)*8.3f,12+(float)Math.sin(a)*8.3f,12+(float)Math.cos(a)*10.8f,12+(float)Math.sin(a)*10.8f,p);}break;
                case "exit":canvas.drawRoundRect(new RectF(3,3,14,21),2,2,p);canvas.drawLine(9,12,22,12,p);canvas.drawLine(22,12,17,7,p);canvas.drawLine(22,12,17,17,p);break;
                case "fullscreen":canvas.drawLine(4,10,4,4,p);canvas.drawLine(4,4,10,4,p);canvas.drawLine(20,10,20,4,p);canvas.drawLine(20,4,14,4,p);canvas.drawLine(4,14,4,20,p);canvas.drawLine(4,20,10,20,p);canvas.drawLine(20,14,20,20,p);canvas.drawLine(20,20,14,20,p);break;
                case "sync":canvas.drawArc(new RectF(4,4,20,20),205,205,false,p);canvas.drawArc(new RectF(4,4,20,20),25,150,false,p);canvas.drawLine(19,3,21,10,p);canvas.drawLine(21,10,15,9,p);canvas.drawLine(5,21,3,14,p);canvas.drawLine(3,14,9,15,p);break;
                case "heart":path.moveTo(12,20);path.cubicTo(4,14,2,11,4,7);path.cubicTo(6,3,10,4,12,7);path.cubicTo(15,3,20,4,21,8);path.cubicTo(22,12,18,16,12,20);canvas.drawPath(path,p);break;
                case "mic":canvas.drawRoundRect(new RectF(9,3,15,15),3,3,p);canvas.drawArc(new RectF(6,8,18,19),0,180,false,p);canvas.drawLine(12,19,12,22,p);canvas.drawLine(8,22,16,22,p);break;
                case "camera":canvas.drawRoundRect(new RectF(3,6,16,18),2,2,p);path.moveTo(16,10);path.lineTo(21,7);path.lineTo(21,17);path.lineTo(16,14);canvas.drawPath(path,p);break;
                case "tv":canvas.drawRoundRect(new RectF(3,5,21,18),2,2,p);canvas.drawLine(9,21,15,21,p);canvas.drawLine(12,18,12,21,p);break;
                case "close":canvas.drawLine(5,5,19,19,p);canvas.drawLine(19,5,5,19,p);break;
                case "more":p.setStyle(Paint.Style.FILL);canvas.drawCircle(12,5,1.8f,p);canvas.drawCircle(12,12,1.8f,p);canvas.drawCircle(12,19,1.8f,p);break;
                case "volume":path.moveTo(4,10);path.lineTo(8,10);path.lineTo(13,6);path.lineTo(13,18);path.lineTo(8,14);path.lineTo(4,14);path.close();canvas.drawPath(path,p);canvas.drawArc(new RectF(10,6,22,18),-60,120,false,p);break;
                case "rewind10":
                case "forward10": {
                    boolean frente = name.equals("forward10");
                    canvas.drawArc(new RectF(3.5f,3.5f,20.5f,20.5f), frente? -125 : -55, frente? 275 : -275,false,p);
                    if(frente){canvas.drawLine(20,4,21,11,p);canvas.drawLine(21,11,15,8,p);}
                    else{canvas.drawLine(4,4,3,11,p);canvas.drawLine(3,11,9,8,p);}
                    p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(9.6f);p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                    canvas.drawText("10",12,16,p);p.setTextAlign(Paint.Align.LEFT);p.setTypeface(null);break;
                }
                case "send":p.setStyle(Paint.Style.FILL);path.moveTo(3,11);path.lineTo(21,3);path.lineTo(16,21);path.lineTo(11,14);path.close();canvas.drawPath(path,p);break;
                default:canvas.drawCircle(12,12,8,p);
            }
            canvas.restoreToCount(save);
        }
        @Override public int getIntrinsicWidth(){return 24;}
        @Override public int getIntrinsicHeight(){return 24;}
        @Override public void setAlpha(int alpha){p.setAlpha(alpha);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}
    }
}
