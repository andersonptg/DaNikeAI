package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.net.*;
import android.view.*;

public class SplashActivity extends Activity {

    private SplashView splash;
    private ConnectivityManager cm;
    private ConnectivityManager.NetworkCallback callback;
    private boolean internetOk = false;
    private boolean loginAberto = false;
    private long inicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        splash = new SplashView(this);
        setContentView(splash);

        inicio = System.currentTimeMillis();

        observarInternet();

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (splash != null) {
                    splash.invalidate();
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void observarInternet() {

        cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return;

        atualizarInternet();

        callback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onCapabilitiesChanged(
                    Network network,
                    NetworkCapabilities caps) {

                internetOk =
                        caps.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        &&
                        caps.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_VALIDATED);

                runOnUiThread(() -> splash.invalidate());
            }

            @Override
            public void onLost(Network network) {
                internetOk = false;
                runOnUiThread(() -> splash.invalidate());
            }
        };

        try {
            cm.registerDefaultNetworkCallback(callback);
        } catch (Exception ignored) {
        }
    }

    private void atualizarInternet() {

        try {
            Network network = cm.getActiveNetwork();

            if (network == null) {
                internetOk = false;
                return;
            }

            NetworkCapabilities caps =
                    cm.getNetworkCapabilities(network);

            internetOk =
                    caps != null
                    &&
                    caps.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    &&
                    caps.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_VALIDATED);

        } catch (Exception ignored) {
            internetOk = false;
        }
    }

    private void abrirLogin() {

        if (loginAberto) {
            return;
        }

        loginAberto = true;

        try {
            if (callback != null && cm != null) {
                cm.unregisterNetworkCallback(callback);
            }
        } catch (Exception ignored) {
        }

        Intent intent =
                new Intent(
                        SplashActivity.this,
                        LoginActivity.class
                );

        startActivity(intent);

        finish();
    }

    @Override
    protected void onDestroy() {

        try {
            if (callback != null && cm != null) {
                cm.unregisterNetworkCallback(callback);
            }
        } catch (Exception ignored) {
        }

        super.onDestroy();
    }

    private class SplashView extends View {

        private final Paint paint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Paint barPaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Bitmap logo;

        private final float density;

        private float anim = 0f;

        private boolean finalizando = false;
        private long finalInicio = 0;

        SplashView(Context context) {
            super(context);

            density =
                    getResources()
                            .getDisplayMetrics()
                            .density;

            logo = BitmapFactory.decodeResource(
                    getResources(),
                    R.drawable.danikeai_logo
            );
        }

        private float d(float valor) {
            return valor * density;
        }

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            float w = getWidth();
            float h = getHeight();

            anim += 0.016f;

            // FUNDO PRETO PURO
            canvas.drawColor(Color.BLACK);

            desenharDetalhesExternos(canvas, w, h);
            desenharLogoOriginal(canvas, w, h);
            desenharFrase(canvas, w, h);
            desenharUsuario(canvas, w, h);
            desenharBarra(canvas, w, h);
        }

        private void desenharDetalhesExternos(
                Canvas canvas,
                float w,
                float h) {

            // FEIXES NEON PREMIUM — azul, vermelho e branco.
            // Ficam atrás do logo.

            float movimento = (anim * d(28)) % (w * 0.65f);

            desenharFeixeNeon(
                    canvas,
                    -w * 0.30f + movimento,
                    h * 0.31f,
                    w * 0.72f + movimento,
                    h * 0.10f,
                    Color.rgb(35, 125, 255),
                    d(3.2f)
            );

            desenharFeixeNeon(
                    canvas,
                    w * 0.28f - movimento,
                    h * 0.50f,
                    w * 1.30f - movimento,
                    h * 0.24f,
                    Color.rgb(255, 45, 55),
                    d(3.0f)
            );

            desenharFeixeNeon(
                    canvas,
                    -w * 0.16f + movimento * 0.55f,
                    h * 0.58f,
                    w * 0.78f + movimento * 0.55f,
                    h * 0.37f,
                    Color.rgb(235, 245, 255),
                    d(2.4f)
            );

            // Pequenas partículas douradas somente no fundo.
            for (int i = 0; i < 22; i++) {

                float x =
                        ((i * 97) % 1000) / 1000f * w;

                float baseY =
                        ((i * 173) % 1000) / 1000f;

                float y =
                        ((baseY + anim * 0.008f) % 1f) * h;

                float brilho =
                        50f
                        +
                        55f *
                        (float)Math.abs(
                                Math.sin(anim * 2.0f + i)
                        );

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(
                        Color.argb(
                                (int) brilho,
                                220,
                                160,
                                45
                        )
                );

                canvas.drawCircle(
                        x,
                        y,
                        d(0.6f + (i % 2) * 0.4f),
                        paint
                );
            }
        }

        private void desenharFeixeNeon(
                Canvas canvas,
                float x1,
                float y1,
                float x2,
                float y2,
                int cor,
                float espessura) {

            int r = Color.red(cor);
            int g = Color.green(cor);
            int b = Color.blue(cor);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);

            // Halo externo.
            paint.setStrokeWidth(espessura * 3.8f);
            paint.setColor(Color.argb(28, r, g, b));
            canvas.drawLine(x1, y1, x2, y2, paint);

            // Brilho intermediário.
            paint.setStrokeWidth(espessura * 2.1f);
            paint.setColor(Color.argb(65, r, g, b));
            canvas.drawLine(x1, y1, x2, y2, paint);

            // Corpo principal.
            paint.setStrokeWidth(espessura);
            paint.setColor(Color.argb(185, r, g, b));
            canvas.drawLine(x1, y1, x2, y2, paint);

            // Núcleo luminoso.
            paint.setStrokeWidth(espessura * 0.38f);
            paint.setColor(Color.argb(255, r, g, b));
            canvas.drawLine(x1, y1, x2, y2, paint);

            paint.setStrokeCap(Paint.Cap.BUTT);
        }

        private void desenharLogoOriginal(
                Canvas canvas,
                float w,
                float h) {

            if (logo == null) return;

            /*
             * IMPORTANTE:
             * A imagem é desenhada preservando EXATAMENTE
             * sua proporção original.
             *
             * Nenhuma deformação.
             * Nenhum brilho sobre a imagem.
             * Nenhuma extrusão.
             * Nenhum shader.
             */

            float margem = w * 0.08f;

            float maxW = w - margem * 2f;
            float maxH = h * 0.43f;

            float escalaW =
                    maxW / logo.getWidth();

            float escalaH =
                    maxH / logo.getHeight();

            float escala =
                    Math.min(escalaW, escalaH);

            float dw =
                    logo.getWidth() * escala;

            float dh =
                    logo.getHeight() * escala;

            float left =
                    (w - dw) / 2f;

            float top =
                    h * 0.16f;

            RectF destino =
                    new RectF(
                            left,
                            top,
                            left + dw,
                            top + dh
                    );

            paint.setAlpha(255);
            paint.setFilterBitmap(true);
            paint.setDither(true);

            canvas.drawBitmap(
                    logo,
                    null,
                    destino,
                    paint
            );
        }

        private void desenharFrase(
                Canvas canvas,
                float w,
                float h) {

            float y = h * 0.635f;

            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(
                    Typeface.create(
                            "sans-serif",
                            Typeface.BOLD
                    )
            );

            // Primeira linha: destaque elegante, sem exagero.
            paint.setTextSize(d(12.5f));
            paint.setLetterSpacing(0.035f);
            paint.setColor(Color.WHITE);
            paint.setShadowLayer(
                    d(3f),
                    0f,
                    0f,
                    Color.argb(70, 255, 255, 255)
            );

            canvas.drawText(
                    "GRANDES RESULTADOS COMEÇAM",
                    w / 2f,
                    y,
                    paint
            );

            // Segunda linha: menor e mais leve.
            paint.setTypeface(
                    Typeface.create(
                            "sans-serif",
                            Typeface.NORMAL
                    )
            );

            paint.setTextSize(d(10.8f));
            paint.setLetterSpacing(0.045f);
            paint.setColor(Color.argb(235, 255, 255, 255));

            canvas.drawText(
                    "COM PEQUENOS PASSOS.",
                    w / 2f,
                    y + d(21),
                    paint
            );

            paint.clearShadowLayer();
            paint.setLetterSpacing(0f);
        }

        private void desenharUsuario(
                Canvas canvas,
                float w,
                float h) {

            String usuario =
                    "anderson_lopes._ofc";

            paint.setTypeface(
                    Typeface.create(
                            "sans-serif-medium",
                            Typeface.NORMAL
                    )
            );

            paint.setTextSize(d(11));
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(Color.WHITE);

            float textoW =
                    paint.measureText(usuario);

            float selo =
                    d(16);

            float espaco =
                    d(7);

            float total =
                    textoW + espaco + selo;

            float inicio =
                    (w - total) / 2f;

            float y =
                    h * 0.725f;

            canvas.drawText(
                    usuario,
                    inicio,
                    y,
                    paint
            );

            // SELO AZUL
            float cx =
                    inicio
                    + textoW
                    + espaco
                    + selo / 2f;

            float cy =
                    y - d(4);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(
                    Color.rgb(35, 125, 255)
            );

            canvas.drawCircle(
                    cx,
                    cy,
                    selo / 2f,
                    paint
            );

            // CHECK BRANCO
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(d(1.8f));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(Color.WHITE);

            Path check = new Path();

            check.moveTo(
                    cx - d(4),
                    cy
            );

            check.lineTo(
                    cx - d(1),
                    cy + d(3)
            );

            check.lineTo(
                    cx + d(4),
                    cy - d(3)
            );

            canvas.drawPath(
                    check,
                    paint
            );

            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);
        }

        private void desenharBarra(
                Canvas canvas,
                float w,
                float h) {

            long decorrido =
                    System.currentTimeMillis()
                    - inicio;

            float progresso =
                    Math.min(
                            1f,
                            decorrido / 8500f
                    );

            if (!internetOk) {
                progresso =
                        Math.min(
                                0.78f,
                                progresso * 0.92f
                        );
            }

            float left =
                    w * 0.12f;

            float right =
                    w * 0.88f;

            float y =
                    h * 0.835f;

            float altura =
                    d(7);

            float raio =
                    altura / 2f;

            // FUNDO DA BARRA
            barPaint.setStyle(Paint.Style.FILL);
            barPaint.setColor(
                    Color.rgb(30, 30, 30)
            );

            canvas.drawRoundRect(
                    left,
                    y,
                    right,
                    y + altura,
                    raio,
                    raio,
                    barPaint
            );

            // PREENCHIMENTO
            float fim =
                    left
                    + (right - left)
                    * progresso;

            LinearGradient gradiente =
                    new LinearGradient(
                            left,
                            y,
                            right,
                            y,
                            new int[]{
                                    Color.rgb(120, 65, 5),
                                    Color.rgb(220, 145, 25),
                                    Color.rgb(255, 210, 75)
                            },
                            null,
                            Shader.TileMode.CLAMP
                    );

            barPaint.setShader(gradiente);

            canvas.drawRoundRect(
                    left,
                    y,
                    Math.max(fim, left + d(2)),
                    y + altura,
                    raio,
                    raio,
                    barPaint
            );

            barPaint.setShader(null);

            // Pequeno brilho que corre somente dentro da barra
            if (progresso > 0.02f) {

                float brilhoX =
                        left
                        + ((anim * d(95))
                        % Math.max(
                                d(20),
                                fim - left
                        ));

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(
                        Color.argb(
                                170,
                                255,
                                240,
                                180
                        )
                );

                canvas.drawCircle(
                        brilhoX,
                        y + altura / 2f,
                        d(1.8f),
                        paint
                );
            }

            // PORCENTAGEM
            paint.setTypeface(
                    Typeface.create(
                            "sans-serif-medium",
                            Typeface.NORMAL
                    )
            );

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setTextSize(d(9));

            paint.setColor(
                    Color.rgb(190, 190, 190)
            );

            canvas.drawText(
                    Math.round(progresso * 100) + "%",
                    w / 2f,
                    y + d(27),
                    paint
            );

            // ENTRA NO LOGIN QUANDO INTERNET ESTIVER OK
            if (internetOk && progresso >= 1.0f) {

                if (!finalizando) {
                    finalizando = true;
                    finalInicio =
                            System.currentTimeMillis();
                }

                if (
                        System.currentTimeMillis()
                        - finalInicio >= 0
                ) {
                    abrirLogin();
                }
            }
        }
    }
}
