package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.content.Context;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(new SplashProfissionalView(this));
    }

    private class SplashProfissionalView extends View {

        private Bitmap fundo;
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private long inicio;
        private boolean terminou = false;

        SplashProfissionalView(Context context) {
            super(context);

            fundo = BitmapFactory.decodeResource(
                    getResources(),
                    R.drawable.splash_profissional
            );

            paint.setFilterBitmap(true);
            inicio = System.currentTimeMillis();

            postInvalidateOnAnimation();
        }

        private float limitar(float v) {
            return Math.max(0f, Math.min(1f, v));
        }

        private float suave(float v) {
            v = limitar(v);
            return v * v * (3f - 2f * v);
        }

        private int dp(float valor) {
            return (int)(
                    valor *
                    getResources().getDisplayMetrics().density
                    + 0.5f
            );
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int w = getWidth();
            int h = getHeight();

            canvas.drawColor(Color.BLACK);

            long tempo = System.currentTimeMillis() - inicio;

            // Imagem ocupando toda a tela, preservando proporção.
            if (fundo != null) {

                float escala = Math.max(
                        (float) w / fundo.getWidth(),
                        (float) h / fundo.getHeight()
                );

                float largura = fundo.getWidth() * escala;
                float altura = fundo.getHeight() * escala;

                float esquerda = (w - largura) / 2f;
                float topo = (h - altura) / 2f;

                RectF destino = new RectF(
                        esquerda,
                        topo,
                        esquerda + largura,
                        topo + altura
                );

                paint.setAlpha(255);
                canvas.drawBitmap(
                        fundo,
                        null,
                        destino,
                        paint
                );
            }

            // Entrada suave.
            float entrada = suave(
                    Math.min(1f, tempo / 900f)
            );

            paint.setColor(Color.BLACK);
            paint.setAlpha(
                    (int)(70f * (1f - entrada))
            );

            canvas.drawRect(
                    0,
                    0,
                    w,
                    h,
                    paint
            );

            // Brilho azul discreto na parte inferior.
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(0, 190, 255));

            float pulso =
                    0.5f +
                    0.5f *
                    (float)Math.sin(tempo / 280.0);

            paint.setAlpha(
                    (int)(25 + 25 * pulso)
            );

            canvas.drawCircle(
                    w / 2f,
                    h * 0.91f,
                    dp(75),
                    paint
            );

            // Carregamento moderno.
            float progresso = limitar(
                    (tempo - 350f) / 2600f
            );

            float cx = w / 2f;
            float cy = h * 0.91f;

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3));
            paint.setStrokeCap(Paint.Cap.ROUND);

            // círculo externo
            paint.setColor(Color.argb(80, 255, 255, 255));

            canvas.drawCircle(
                    cx,
                    cy,
                    dp(19),
                    paint
            );

            // progresso azul
            paint.setColor(Color.rgb(0, 220, 255));
            paint.setAlpha(255);

            RectF arco = new RectF(
                    cx - dp(19),
                    cy - dp(19),
                    cx + dp(19),
                    cy + dp(19)
            );

            canvas.drawArc(
                    arco,
                    -90,
                    360f * progresso,
                    false,
                    paint
            );

            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);

            // Pequeno texto de carregamento.
            paint.setTypeface(Typeface.create(
                    Typeface.SANS_SERIF,
                    Typeface.BOLD
            ));

            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(dp(11));
            paint.setColor(Color.WHITE);
            paint.setAlpha(210);

            canvas.drawText(
                    "INICIANDO",
                    cx,
                    cy + dp(42),
                    paint
            );

            // Transição elegante para o Login.
            if (tempo > 2850) {

                float fade = limitar(
                        (tempo - 2850f) / 550f
                );

                paint.setColor(Color.BLACK);
                paint.setAlpha(
                        (int)(255f * fade)
                );

                canvas.drawRect(
                        0,
                        0,
                        w,
                        h,
                        paint
                );
            }

            // Ir para Login.
            if (tempo >= 3400 && !terminou) {

                terminou = true;

                Intent intent = new Intent(
                        SplashActivity.this,
                        LoginActivity.class
                );

                startActivity(intent);
                overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );

                finish();
            }

            postInvalidateOnAnimation();
        }
    }
}
