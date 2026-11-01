package com.danike.ai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.TextView;

/** Estilo autônomo do catálogo NEXO; não altera tema global nem funções da HAVE. */
public final class NexoCatalogVisual {
    public static final int BG = Color.rgb(4, 8, 22);
    public static final int PANEL = Color.rgb(15, 20, 42);
    public static final int BLUE = Color.rgb(23, 146, 244);
    public static final int PURPLE = Color.rgb(91, 43, 221);
    public static final int VIOLET = Color.rgb(180, 57, 242);
    public static final int MUTED = Color.rgb(172, 185, 207);
    private NexoCatalogVisual() { }

    private static int dp(Context c, float x) {
        return Math.round(x * c.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable gradient(Context c, int a, int b, int stroke,
                                            int radius) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {a, b});
        d.setCornerRadius(dp(c, radius));
        if (stroke != Color.TRANSPARENT) d.setStroke(Math.max(1, dp(c, 1)), stroke);
        return d;
    }

    public static GradientDrawable solid(Context c, int color, int stroke,
                                         int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(c, radius));
        if (stroke != Color.TRANSPARENT) d.setStroke(Math.max(1, dp(c, 1)), stroke);
        return d;
    }

    public static void button(Context c, TextView v, String icon, boolean primary) {
        v.setGravity(Gravity.CENTER);
        v.setTextSize(primary ? 15 : 13);
        v.setTextColor(Color.WHITE);
        v.setTypeface(android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD);
        v.setCompoundDrawablePadding(dp(c, 9));
        v.setCompoundDrawablesWithIntrinsicBounds(glyph(c, icon, Color.WHITE, 18), null, null, null);
        v.setBackground(primary
                ? gradient(c, Color.rgb(154, 41, 236), Color.rgb(57, 43, 208),
                        Color.rgb(167, 107, 255), 15)
                : gradient(c, Color.rgb(27, 24, 70), Color.rgb(7, 37, 72),
                        Color.rgb(41, 170, 246), 24));
        v.setClickable(true);
        v.setFocusable(true);
        v.setPadding(dp(c, 13), 0, dp(c, 13), 0);
    }

    public static void action(Context c, TextView v, String icon) {
        v.setTextSize(12);
        v.setTextColor(Color.rgb(231, 236, 252));
        v.setGravity(Gravity.CENTER);
        v.setCompoundDrawablePadding(dp(c, 7));
        v.setCompoundDrawablesWithIntrinsicBounds(null, glyph(c, icon, Color.WHITE, 23), null, null);
        v.setBackground(solid(c, Color.TRANSPARENT, Color.TRANSPARENT, 12));
        v.setClickable(true);
        v.setFocusable(true);
    }

    public static Drawable glyph(Context c, String kind, int color, int sizeDp) {
        return new Icon(kind, color, dp(c, sizeDp));
    }

    private static final class Icon extends Drawable {
        private final String kind;
        private final int color, px;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Icon(String kind, int color, int px) {
            this.kind = kind; this.color = color; this.px = px;
        }
        @Override public int getIntrinsicWidth() { return px; }
        @Override public int getIntrinsicHeight() { return px; }
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            if (b.width() <= 0 || b.height() <= 0) return;
            c.save();
            c.translate(b.left, b.top);
            c.scale(b.width() / 24f, b.height() / 24f);
            p.setColor(color);
            p.setAlpha(255);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.9f);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            Path a = new Path();
            switch (kind) {
                case "play":
                    a.moveTo(7, 4); a.lineTo(20, 12); a.lineTo(7, 20); a.close();
                    p.setStyle(Paint.Style.FILL); c.drawPath(a, p); break;
                case "download":
                    c.drawLine(12, 3, 12, 16, p);
                    c.drawLine(7, 11, 12, 16, p); c.drawLine(17, 11, 12, 16, p);
                    c.drawLine(4, 20, 20, 20, p); break;
                case "group":
                    c.drawCircle(9, 8, 3, p); c.drawCircle(18, 9, 2, p);
                    c.drawArc(new RectF(2, 14, 16, 25), 188, 164, false, p);
                    c.drawArc(new RectF(14, 14, 23, 23), 210, 116, false, p);
                    break;
                case "plus":
                    c.drawLine(12, 4, 12, 20, p); c.drawLine(4, 12, 20, 12, p); break;
                case "star":
                    for (int i = 0; i < 10; i++) {
                        double angle = -Math.PI / 2 + i * Math.PI / 5;
                        float r = i % 2 == 0 ? 9f : 4.5f;
                        float x = 12 + (float)Math.cos(angle) * r;
                        float y = 12 + (float)Math.sin(angle) * r;
                        if (i == 0) a.moveTo(x, y); else a.lineTo(x, y);
                    }
                    a.close(); c.drawPath(a, p); break;
                case "share":
                    c.drawLine(8, 12, 17, 6, p); c.drawLine(8, 12, 17, 18, p);
                    c.drawCircle(6, 12, 2.3f, p); c.drawCircle(19, 5, 2.3f, p);
                    c.drawCircle(19, 19, 2.3f, p); break;
                case "info":
                    c.drawCircle(12, 12, 9, p); c.drawLine(12, 10, 12, 17, p);
                    p.setStyle(Paint.Style.FILL); c.drawCircle(12, 7, 1.1f, p); break;
                case "back":
                    c.drawLine(16, 5, 9, 12, p); c.drawLine(9, 12, 16, 19, p); break;
                case "search":
                    c.drawCircle(10, 10, 6, p); c.drawLine(15, 15, 21, 21, p); break;
                default:
                    c.drawCircle(12, 12, 8, p);
            }
            c.restore();
        }
        @Override public void setAlpha(int alpha) { p.setAlpha(alpha); invalidateSelf(); }
        @Override public void setColorFilter(ColorFilter filter) { p.setColorFilter(filter); invalidateSelf(); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
