package com.danike.ai;

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
    // ============================================================

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
                    R.drawable.danike_splash_logo
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

            /*
             * Progresso da inicialização do aplicativo.
             * Não representa velocidade ou qualidade da internet.
             */
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

            desenharFundo(canvas, w, h);

            desenharLogo(canvas, w, h);

            desenharNome(canvas, w, h);

            desenharSubtitulo(canvas, w, h);

            desenharStatus(canvas, w, h);

            desenharBarra(canvas, w, h);

            desenharRodape(canvas, w, h);

            if (finalizando) {
                desenharFinalizacao(canvas, w, h);
            }
        }

        // ========================================================
        // FUNDO
        // ========================================================

        private void desenharFundo(
                Canvas canvas,
                float w,
                float h) {

            canvas.drawColor(
                    Color.rgb(3, 4, 8)
            );

            Paint fundo =
                    new Paint(Paint.ANTI_ALIAS_FLAG);

            float centroX = w * 0.50f;
            float centroY = h * 0.40f;

            RadialGradient glow =
                    new RadialGradient(
                            centroX,
                            centroY,
                            Math.max(w, h) * 0.62f,
                            new int[]{
                                    Color.rgb(20, 32, 55),
                                    Color.rgb(7, 10, 18),
                                    Color.rgb(3, 4, 8)
                            },
                            new float[]{
                                    0f,
                                    0.48f,
                                    1f
                            },
                            Shader.TileMode.CLAMP
                    );

            fundo.setShader(glow);

            canvas.drawRect(
                    0,
                    0,
                    w,
                    h,
                    fundo
            );

            fundo.setShader(null);

            // brilho muito sutil no centro
            Paint halo =
                    new Paint(Paint.ANTI_ALIAS_FLAG);

            RadialGradient centro =
                    new RadialGradient(
                            w * 0.50f,
                            h * 0.39f,
                            w * 0.36f,
                            Color.argb(
                                    30,
                                    55,
                                    125,
                                    255
                            ),
                            Color.TRANSPARENT,
                            Shader.TileMode.CLAMP
                    );

            halo.setShader(centro);

            canvas.drawCircle(
                    w * 0.50f,
                    h * 0.39f,
                    w * 0.36f,
                    halo
            );

            halo.setShader(null);

            // linha superior discreta
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(
                    Color.argb(
                            65,
                            255,
                            255,
                            255
                    )
            );

            canvas.drawRect(
                    w * 0.18f,
                    h * 0.075f,
                    w * 0.82f,
                    h * 0.076f,
                    paint
            );
        }

        // ========================================================
        // LOGO
        // ========================================================

        private void desenharLogo(
                Canvas canvas,
                float w,
                float h) {

            if (logo == null) {
                return;
            }

            float maxW = w * 0.60f;
            float maxH = h * 0.34f;

            float escala =
                    Math.min(
                            maxW / logo.getWidth(),
                            maxH / logo.getHeight()
                    );

            float largura =
                    logo.getWidth() * escala;

            float altura =
                    logo.getHeight() * escala;

            float left =
                    (w - largura) / 2f;

            float top =
                    h * 0.12f;

            float pulso =
                    1f +
                    (float)
                    Math.sin(anim * 1.7f)
                    * 0.008f;

            float cx =
                    w / 2f;

            float cy =
                    top + altura / 2f;

            canvas.save();

            canvas.scale(
                    pulso,
                    pulso,
                    cx,
                    cy
            );

            // halo discreto atrás da logo
            Paint halo =
                    new Paint(Paint.ANTI_ALIAS_FLAG);

            RadialGradient brilho =
                    new RadialGradient(
                            cx,
                            cy,
                            largura * 0.62f,
                            Color.argb(
                                    38,
                                    90,
                                    145,
                                    255
                            ),
                            Color.TRANSPARENT,
                            Shader.TileMode.CLAMP
                    );

            halo.setShader(brilho);

            canvas.drawCircle(
                    cx,
                    cy,
                    largura * 0.62f,
                    halo
            );

            halo.setShader(null);

            canvas.drawBitmap(
                    logo,
                    null,
                    new android.graphics.RectF(
                            left,
                            top,
                            left + largura,
                            top + altura
                    ),
                    logoPaint
            );

            canvas.restore();
        }

        // ========================================================
        // NOME
        // ========================================================

        private void desenharNome(
                Canvas canvas,
                float w,
                float h) {

            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);

            paint.setTypeface(
                    Typeface.create(
                            "sans-serif",
                            Typeface.BOLD
                    )
            );

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setTextSize(
                    d(23)
            );

            paint.setLetterSpacing(
                    0.04f
            );

            paint.setColor(
                    Color.rgb(
                            245,
                            247,
                            252
                    )
            );

            canvas.drawText(
                    "DaNikeAI",
                    w / 2f,
                    h * 0.535f,
                    paint
            );

            paint.setLetterSpacing(0f);
        }

        // ========================================================
        // SUBTÍTULO
        // ========================================================

        private void desenharSubtitulo(
                Canvas canvas,
                float w,
                float h) {

            paint.setTypeface(
                    Typeface.create(
                            "sans-serif",
                            Typeface.NORMAL
                    )
            );

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setTextSize(
                    d(9)
            );

            paint.setLetterSpacing(
                    0.18f
            );

            paint.setColor(
                    Color.rgb(
                            155,
                            163,
                            178
                    )
            );

            canvas.drawText(
                    "INTELIGÊNCIA  •  TECNOLOGIA  •  CONEXÃO",
                    w / 2f,
                    h * 0.575f,
                    paint
            );

            paint.setLetterSpacing(0f);
        }

        // ========================================================
        // STATUS REAL DA INTERNET
        // ========================================================

        private void desenharStatus(
                Canvas canvas,
                float w,
                float h) {

            float y =
                    h * 0.665f;

            float centroX =
                    w / 2f;

            String texto;

            if (internetOk) {

                texto =
                        finalizando
                                ? "Sistema pronto"
                                : "Internet conectada";

            } else {

                texto =
                        "Aguardando conexão...";
            }

            paint.setTypeface(
                    Typeface.create(
                            "sans-serif-medium",
                            Typeface.NORMAL
                    )
            );

            paint.setTextSize(
                    d(11)
            );

            paint.setTextAlign(
                    Paint.Align.LEFT
            );

            float larguraTexto =
                    paint.measureText(
                            texto
                    );

            float raio =
                    d(4);

            float espacamento =
                    d(10);

            float total =
                    raio * 2f
                    + espacamento
                    + larguraTexto;

            float inicioX =
                    centroX
                    - total / 2f;

            paint.setStyle(
                    Paint.Style.FILL
            );

            if (internetOk) {

                paint.setColor(
                        Color.rgb(
                                50,
                                210,
                                125
                        )
                );

            } else {

                paint.setColor(
                        Color.rgb(
                                180,
                                180,
                                185
                        )
                );
            }

            canvas.drawCircle(
                    inicioX + raio,
                    y - d(4),
                    raio,
                    paint
            );

            paint.setColor(
                    Color.rgb(
                            190,
                            195,
                            205
                    )
            );

            canvas.drawText(
                    texto,
                    inicioX
                            + raio * 2f
                            + espacamento,
                    y,
                    paint
            );
        }

        // ========================================================
        // BARRA
        // ========================================================

        private void desenharBarra(
                Canvas canvas,
                float w,
                float h) {

            float left =
                    w * 0.18f;

            float right =
                    w * 0.82f;

            float y =
                    h * 0.735f;

            float altura =
                    d(5);

            float raio =
                    altura / 2f;

            // fundo
            progressPaint.setShader(null);
            progressPaint.setStyle(
                    Paint.Style.FILL
            );

            progressPaint.setColor(
                    Color.rgb(
                            25,
                            27,
                            34
                    )
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

            // progresso
            float fim =
                    left +
                    (right - left)
                    * progresso;

            LinearGradient gradiente =
                    new LinearGradient(
                            left,
                            y,
                            right,
                            y,
                            new int[]{
                                    Color.rgb(
                                            75,
                                            125,
                                            220
                                    ),
                                    Color.rgb(
                                            115,
                                            165,
                                            255
                                    ),
                                    Color.rgb(
                                            220,
                                            230,
                                            255
                                    )
                            },
                            null,
                            Shader.TileMode.CLAMP
                    );

            progressPaint.setShader(
                    gradiente
            );

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

            // porcentagem
            paint.setTypeface(
                    Typeface.create(
                            "sans-serif-medium",
                            Typeface.NORMAL
                    )
            );

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setTextSize(
                    d(10)
            );

            paint.setLetterSpacing(
                    0.03f
            );

            paint.setColor(
                    Color.rgb(
                            160,
                            166,
                            178
                    )
            );

            canvas.drawText(
                    Math.round(
                            progresso * 100f
                    ) + "%",
                    w / 2f,
                    y + d(28),
                    paint
            );

            paint.setLetterSpacing(0f);
        }

        // ========================================================
        // RODAPÉ
        // ========================================================

        private void desenharRodape(
                Canvas canvas,
                float w,
                float h) {

            paint.setTypeface(
                    Typeface.create(
                            "sans-serif",
                            Typeface.NORMAL
                    )
            );

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setTextSize(
                    d(8)
            );

            paint.setLetterSpacing(
                    0.12f
            );

            paint.setColor(
                    Color.rgb(
                            85,
                            90,
                            102
                    )
            );

            canvas.drawText(
                    "DA NIKE AI",
                    w / 2f,
                    h * 0.915f,
                    paint
            );

            paint.setLetterSpacing(0f);
        }

        // ========================================================
        // FINALIZAÇÃO
        // ========================================================

        private void desenharFinalizacao(
                Canvas canvas,
                float w,
                float h) {

            long tempo =
                    System.currentTimeMillis()
                    - finalInicio;

            float alpha =
                    Math.min(
                            1f,
                            tempo / 300f
                    );

            paint.setAlpha(
                    (int)
                    (255 * alpha)
            );

            paint.setTypeface(
                    Typeface.create(
                            "sans-serif-medium",
                            Typeface.NORMAL
                    )
            );

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setTextSize(
                    d(12)
            );

            paint.setLetterSpacing(
                    0.08f
            );

            paint.setColor(
                    Color.rgb(
                            110,
                            220,
                            150
                    )
            );

            canvas.drawText(
                    "✓  SISTEMA PRONTO",
                    w / 2f,
                    h * 0.80f,
                    paint
            );

            paint.setAlpha(255);
            paint.setLetterSpacing(0f);
        }
    }
}
