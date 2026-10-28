package com.danike.ai;

import com.danike.ai.NexoAppObserver;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.view.View;

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
                    splash.atualizar();
                    splash.invalidate();
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void observarInternet() {

        cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {
            internetOk = false;
            return;
        }

        atualizarInternet();

        callback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> {
                    atualizarInternet();
                });
            }

            @Override
            public void onCapabilitiesChanged(
                    Network network,
                    NetworkCapabilities capabilities) {

                runOnUiThread(() -> {
                    internetOk =
                            capabilities != null &&
                            capabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            capabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_VALIDATED);

                    if (splash != null) {
                        splash.invalidate();
                    }
                });
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> {
                    atualizarInternet();
                });
            }
        };

        try {
            NetworkRequest request =
                    new NetworkRequest.Builder()
                            .addCapability(
                                    NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build();

            cm.registerNetworkCallback(request, callback);

        } catch (Exception ignored) {
        }
    }

    private void atualizarInternet() {

        if (cm == null) {
            internetOk = false;
            return;
        }

        try {
            Network network = cm.getActiveNetwork();

            if (network == null) {
                internetOk = false;
                return;
            }

            NetworkCapabilities caps =
                    cm.getNetworkCapabilities(network);

            internetOk =
                    caps != null &&
                    caps.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_VALIDATED);

        } catch (Exception e) {
            internetOk = false;
        }

        if (splash != null) {
            splash.invalidate();
        }
    }

    private void abrirLogin() {

        if (loginAberto) {
            return;
        }

        loginAberto = true;

        try {
            if (cm != null && callback != null) {
                cm.unregisterNetworkCallback(callback);
            }
        } catch (Exception ignored) {
        }

        Intent intent =
                new Intent(SplashActivity.this, LoginActivity.class);

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {

        try {
            if (cm != null && callback != null) {
                cm.unregisterNetworkCallback(callback);
            }
        } catch (Exception ignored) {
        }

        super.onDestroy();
    }

    // ============================================================
    // SPLASH PREMIUM
    // Visual: preto absoluto + logo central + barra prata

    private class SplashView extends View {

        private final Paint paint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Paint logoPaint =
                new Paint(Paint.ANTI_ALIAS_FLAG |
                        Paint.FILTER_BITMAP_FLAG);

        private final Paint progressPaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Bitmap logo;

        private final float density;

        private float anim = 0f;
        private float progresso = 0f;

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
                    R.drawable.nexo_halloween
            );

            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        private float d(float valor) {
            return valor * density;
        }

        void atualizar() {

            anim += 0.016f;

            long decorrido =
                    System.currentTimeMillis() - inicio;

            float alvo =
                    Math.min(
                            1f,
                            decorrido / 6200f
                    );

            progresso +=
                    (alvo - progresso) * 0.075f;

            if (internetOk && progresso > 0.985f) {
                progresso = 1f;
            }

            if (internetOk && progresso >= 1f) {

                if (!finalizando) {
                    finalizando = true;
                    finalInicio =
                            System.currentTimeMillis();
                }

                if (System.currentTimeMillis()
                        - finalInicio >= 650) {

                    abrirLogin();
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            float w = getWidth();
            float h = getHeight();

            // Fundo totalmente preto
            // Fundo premium azul-marinho, sem efeitos em movimento
            canvas.drawColor(Color.BLACK);


            desenharLogo(canvas, w, h);
            desenharHalloween(canvas, w, h);
            desenharBarra(canvas, w, h);
        }

        // ========================================================
        // LOGO CENTRAL
        // ========================================================

        private void desenharLogo(
                Canvas canvas,
                float w,
                float h) {

            if (logo == null) {
                return;
            }

            float centroX = w / 2f;
            float centroY = h * 0.43f;

            // Mantém a proporção original da logo
            float limiteLargura = w * 0.52f;
            float limiteAltura = h * 0.28f;

            float escala = Math.min(
                    limiteLargura / logo.getWidth(),
                    limiteAltura / logo.getHeight()
            );

            float larguraLogo = logo.getWidth() * escala;
            float alturaLogo = logo.getHeight() * escala;

            float left = centroX - larguraLogo / 2f;
            float top = centroY - alturaLogo / 2f;

            canvas.drawBitmap(
                    logo,
                    null,
                    new android.graphics.RectF(
                            left,
                            top,
                            left + larguraLogo,
                            top + alturaLogo
                    ),
                    logoPaint
            );

        // ========================================================

        }


        // ========================================================
        // HALLOWEEN NEXO: teias, aranha e morcegos
        // Desenho local, sem imagens ou dependências externas.
        // ========================================================

        private void linhaHalloween(
                Canvas c, float x1, float y1,
                float x2, float y2, Paint p) {
            c.drawLine(x1, y1, x2, y2, p);
        }

        private void desenharTeia(
                Canvas c, float ox, float oy,
                float sx, float sy, Paint p) {

            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(d(0.8f));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(Color.argb(145, 150, 175, 195));

            Path raio = new Path();
            raio.moveTo(ox, oy);
            raio.lineTo(ox + sx, oy);
            raio.moveTo(ox, oy);
            raio.lineTo(ox, oy + sy);
            raio.moveTo(ox, oy);
            raio.lineTo(ox + sx * 0.72f, oy + sy * 0.72f);
            raio.moveTo(ox, oy);
            raio.lineTo(ox + sx * 0.35f, oy + sy);
            c.drawPath(raio, p);

            for (int i = 1; i <= 4; i++) {
                float t = i / 5f;
                Path arco = new Path();
                arco.moveTo(ox + sx * t, oy);
                arco.quadTo(
                        ox + sx * t * 0.90f,
                        oy + sy * t * 0.25f,
                        ox + sx * t * 0.72f,
                        oy + sy * t * 0.72f);
                arco.quadTo(
                        ox + sx * t * 0.25f,
                        oy + sy * t * 0.90f,
                        ox,
                        oy + sy * t);
                c.drawPath(arco, p);
            }
            p.setStyle(Paint.Style.FILL);
        }

        private void desenharAranha(
                Canvas c, float x, float y, Paint p) {

            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(d(1.25f));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(Color.rgb(190, 200, 215));

            // Fio suspenso
            c.drawLine(x, y - d(25), x, y - d(4), p);

            // Oito patas
            for (int lado = -1; lado <= 1; lado += 2) {
                for (int i = 0; i < 4; i++) {
                    float yy = y - d(4) + i * d(2.2f);
                    Path pata = new Path();
                    pata.moveTo(x + lado * d(2), yy);
                    pata.lineTo(x + lado * d(6), yy - d(3));
                    pata.lineTo(x + lado * d(8), yy + d(1));
                    c.drawPath(pata, p);
                }
            }

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(20, 23, 30));
            c.drawCircle(x, y, d(4.2f), p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(d(0.8f));
            p.setColor(Color.rgb(0, 205, 245));
            c.drawCircle(x, y, d(4.2f), p);
            p.setStyle(Paint.Style.FILL);
        }

        private void desenharMorcego(
                Canvas c, float x, float y, float tamanho, Paint p) {

            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(205, 18, 20, 29));

            Path asa = new Path();
            asa.moveTo(x, y);
            asa.cubicTo(
                    x - tamanho * 0.25f, y - tamanho * 0.75f,
                    x - tamanho * 0.72f, y - tamanho * 0.48f,
                    x - tamanho, y - tamanho * 0.18f);
            asa.lineTo(x - tamanho * 0.72f, y + tamanho * 0.22f);
            asa.lineTo(x - tamanho * 0.42f, y + tamanho * 0.05f);
            asa.lineTo(x, y + tamanho * 0.35f);
            asa.lineTo(x + tamanho * 0.42f, y + tamanho * 0.05f);
            asa.lineTo(x + tamanho * 0.72f, y + tamanho * 0.22f);
            asa.lineTo(x + tamanho, y - tamanho * 0.18f);
            asa.cubicTo(
                    x + tamanho * 0.72f, y - tamanho * 0.48f,
                    x + tamanho * 0.25f, y - tamanho * 0.75f,
                    x, y);
            asa.close();
            c.drawPath(asa, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(d(0.7f));
            p.setColor(Color.argb(180, 0, 190, 230));
            c.drawPath(asa, p);
            p.setStyle(Paint.Style.FILL);
        }

        private void desenharHalloween(Canvas c, float w, float h) {
            Paint p = paint;
            p.setShader(null);

            // Teias discretas nos dois cantos superiores
            desenharTeia(c, 0, 0, w * 0.25f, h * 0.16f, p);

            c.save();
            c.scale(-1, 1, w / 2f, 0);
            desenharTeia(c, 0, 0, w * 0.25f, h * 0.16f, p);
            c.restore();

            // Aranha próxima à logo NEXO, sem cobrir o centro
            desenharAranha(c, w * 0.73f, h * 0.345f, p);

            // Morcegos afastados dos elementos principais
            desenharMorcego(c, w * 0.16f, h * 0.30f, d(13), p);
            desenharMorcego(c, w * 0.84f, h * 0.57f, d(10), p);

            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
        }

// BARRA PRATA
        // ========================================================

        private void desenharBarra(
                Canvas canvas,
                float w,
                float h) {

            float largura = w * 0.58f;
            float left = (w - largura) / 2f;
            float right = left + largura;

            float y = h * 0.73f;
            float altura = d(4);
            float raio = altura / 2f;

            // Trilho
            progressPaint.setShader(null);
            progressPaint.setStyle(Paint.Style.FILL);
            progressPaint.setColor(
                    Color.rgb(38, 40, 44)
            );

            canvas.drawRoundRect(
                    left,
                    y,
                    right,
                    y + altura,
                    raio,
                    raio,
                    progressPaint
            );

            // Progresso prata
            float fim =
                    left +
                    (right - left) * progresso;

            LinearGradient prata =
                    new LinearGradient(
                            left,
                            y,
                            right,
                            y,
                            new int[]{
                                    Color.rgb(95, 98, 102),
                                    Color.rgb(190, 193, 197),
                                    Color.rgb(245, 245, 245)
                            },
                            null,
                            Shader.TileMode.CLAMP
                    );

            progressPaint.setShader(prata);

            canvas.drawRoundRect(
                    left,
                    y,
                    Math.max(
                            fim,
                            left + d(2)
                    ),
                    y + altura,
                    raio,
                    raio,
                    progressPaint
            );

            progressPaint.setShader(null);

            // Porcentagem sincronizada com a barra de progresso
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            paint.setTextSize(d(12));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.rgb(190, 193, 197));
            int percentual = Math.min(100, Math.max(0, Math.round(progresso * 100f)));
            canvas.drawText(percentual + "%", w / 2f, y + d(27), paint);
}
    }
    }
