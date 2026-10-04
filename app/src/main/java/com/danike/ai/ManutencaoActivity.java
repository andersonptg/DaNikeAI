package com.danike.ai;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Typeface;

public class ManutencaoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout tela = new LinearLayout(this);
        tela.setOrientation(LinearLayout.VERTICAL);
        tela.setGravity(android.view.Gravity.CENTER);
        tela.setPadding(dp(24), dp(20), dp(24), dp(20));
        tela.setBackgroundColor(Color.BLACK);

        TextView logo = new TextView(this);
        logo.setText("DANIKEAI");
        logo.setTextColor(Color.rgb(0, 230, 255));
        logo.setTextSize(25);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        logo.setGravity(android.view.Gravity.CENTER);

        OficinaView oficina = new OficinaView();

        TextView titulo = new TextView(this);
        titulo.setText("ESTAMOS MELHORANDO");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(23);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setGravity(android.view.Gravity.CENTER);
        titulo.setPadding(0, dp(18), 0, dp(10));

        TextView mensagem = new TextView(this);
        mensagem.setText(
                "O DaNikeAI está passando por uma atualização.\n\n"
                + "Volte em alguns instantes."
        );
        mensagem.setTextColor(Color.rgb(190, 200, 215));
        mensagem.setTextSize(16);
        mensagem.setGravity(android.view.Gravity.CENTER);
        mensagem.setLineSpacing(dp(4), 1f);

        LinearLayout statusLinha = new LinearLayout(this);
        statusLinha.setGravity(android.view.Gravity.CENTER);
        statusLinha.setPadding(0, dp(22), 0, 0);

        TextView bolinha = new TextView(this);
        bolinha.setText("●");
        bolinha.setTextColor(Color.RED);
        bolinha.setTextSize(20);

        TextView status = new TextView(this);
        status.setText("  SISTEMA EM MANUTENÇÃO");
        status.setTextColor(Color.rgb(255, 70, 70));
        status.setTextSize(13);
        status.setTypeface(Typeface.DEFAULT_BOLD);

        statusLinha.addView(bolinha);
        statusLinha.addView(status);

        tela.addView(
                logo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(45)
                )
        );

        tela.addView(
                oficina,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(300)
                )
        );

        tela.addView(
                titulo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        tela.addView(
                mensagem,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(90)
                )
        );

        tela.addView(statusLinha);

        setContentView(tela);

        iniciarPulso(bolinha);
    }

    private void iniciarPulso(TextView bolinha) {
        ValueAnimator animador = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.rgb(100, 0, 0),
                Color.RED
        );

        animador.setDuration(650);
        animador.setRepeatMode(ValueAnimator.REVERSE);
        animador.setRepeatCount(ValueAnimator.INFINITE);

        animador.addUpdateListener(animation -> {
            bolinha.setTextColor((Integer) animation.getAnimatedValue());

            float escala =
                    1.0f
                    + 0.28f
                    * (animation.getAnimatedFraction());

            bolinha.setScaleX(escala);
            bolinha.setScaleY(escala);
        });

        animador.start();
    }

    private int dp(int valor) {
        return (int) (
                valor
                * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private class OficinaView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        OficinaView() {
            super(ManutencaoActivity.this);
            paint.setStrokeCap(Paint.Cap.ROUND);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;

            float raio = Math.min(getWidth(), getHeight()) * 0.36f;

            // Campo de força externo
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3));
            paint.setColor(Color.rgb(0, 220, 255));
            paint.setShadowLayer(dp(16), 0, 0, Color.rgb(0, 220, 255));

            canvas.drawCircle(cx, cy, raio, paint);

            paint.clearShadowLayer();

            // Segundo anel neon
            paint.setStrokeWidth(dp(1));
            paint.setColor(Color.rgb(80, 245, 255));
            canvas.drawCircle(cx, cy, raio - dp(13), paint);

            // Pequenos pontos do campo de força
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(0, 220, 255));

            for (int i = 0; i < 8; i++) {
                double angulo = Math.toRadians(i * 45);
                float x = cx + (float) Math.cos(angulo) * raio;
                float y = cy + (float) Math.sin(angulo) * raio;
                canvas.drawCircle(x, y, dp(3), paint);
            }

            // Ferramentas cruzadas
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(15));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(Color.rgb(0, 180, 255));
            paint.setShadowLayer(dp(14), 0, 0, Color.rgb(0, 220, 255));

            canvas.save();
            canvas.rotate(45, cx, cy);

            canvas.drawLine(
                    cx,
                    cy - dp(70),
                    cx,
                    cy + dp(70),
                    paint
            );

            canvas.restore();

            canvas.save();
            canvas.rotate(-45, cx, cy);

            canvas.drawLine(
                    cx,
                    cy - dp(70),
                    cx,
                    cy + dp(70),
                    paint
            );

            canvas.restore();

            paint.clearShadowLayer();

            // Cabeças das ferramentas
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(7));
            paint.setColor(Color.rgb(90, 235, 255));

            canvas.save();
            canvas.rotate(45, cx, cy);

            RectF topo = new RectF(
                    cx - dp(20),
                    cy - dp(95),
                    cx + dp(20),
                    cy - dp(55)
            );

            canvas.drawArc(topo, 180, 180, false, paint);

            canvas.restore();

            canvas.save();
            canvas.rotate(-45, cx, cy);

            RectF topo2 = new RectF(
                    cx - dp(20),
                    cy - dp(95),
                    cx + dp(20),
                    cy - dp(55)
            );

            canvas.drawArc(topo2, 180, 180, false, paint);

            canvas.restore();

            // Núcleo central
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setShadowLayer(dp(20), 0, 0, Color.CYAN);
            canvas.drawCircle(cx, cy, dp(8), paint);
            paint.clearShadowLayer();
        }
    }
}
