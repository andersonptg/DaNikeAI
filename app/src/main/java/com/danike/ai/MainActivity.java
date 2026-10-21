package com.danike.ai;



import java.util.Random;

import java.util.ArrayList;

import java.io.InputStream;

import java.io.BufferedReader;

import java.io.InputStreamReader;

import org.json.JSONArray;

import org.json.JSONObject;



import android.animation.ValueAnimator;

import android.view.animation.LinearInterpolator;

import android.app.Activity;

import android.os.Bundle;



import android.content.Intent;

import android.graphics.*;

import android.graphics.drawable.*;

import android.view.*;

import android.view.animation.*;

import android.widget.*;



public class MainActivity extends Activity {





    // ============================================================

    // HALO NEON ATRÁS DAS FOTOS

    // ============================================================

    private class NeonFotoDrawable extends Drawable {



        private final Paint linha = new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Paint brilho = new Paint(Paint.ANTI_ALIAS_FLAG);

        private float movimento = 0f;



        NeonFotoDrawable() {



            ValueAnimator anim =

                    ValueAnimator.ofFloat(0f, 1f);



            anim.setDuration(2600);

            anim.setRepeatCount(ValueAnimator.INFINITE);

            anim.setInterpolator(new LinearInterpolator());



            anim.addUpdateListener(v -> {

                movimento =

                        (float) v.getAnimatedValue();

                invalidateSelf();

            });



            anim.start();

        }



        @Override

        public void draw(Canvas canvas) {



            Rect b = getBounds();



            if (b.width() <= 0 || b.height() <= 0)

                return;



            float cx = b.centerX();

            float cy = b.centerY();



            // ====================================================

            // BRILHO AZUL CENTRAL

            // ====================================================

            brilho.setShader(new RadialGradient(

                    cx,

                    cy,

                    Math.max(b.width(), b.height()) * 0.75f,

                    new int[] {

                            Color.argb(120, 0, 210, 255),

                            Color.argb(70, 0, 120, 255),

                            Color.TRANSPARENT

                    },

                    new float[] {

                            0f,

                            0.55f,

                            1f

                    },

                    Shader.TileMode.CLAMP

            ));



            canvas.drawRect(

                    b.left,

                    b.top,

                    b.right,

                    b.bottom,

                    brilho

            );



            brilho.setShader(null);



            // ====================================================

            // RAIOS NEON AZUIS SAINDO DA FOTO

            // ====================================================

            linha.setStyle(Paint.Style.STROKE);

            linha.setStrokeCap(Paint.Cap.ROUND);



            int totalRaios = 34;



            for (int i = 0; i < totalRaios; i++) {



                double angulo =

                        (Math.PI * 2.0 * i / totalRaios)

                        + (movimento * Math.PI * 2.0);



                float interno =

                        Math.min(

                                b.width(),

                                b.height()

                        ) * 0.34f;



                float externo =

                        Math.max(

                                b.width(),

                                b.height()

                        ) * (0.48f + ((i % 4) * 0.08f));



                float x1 =

                        cx + (float)Math.cos(angulo) * interno;



                float y1 =

                        cy + (float)Math.sin(angulo) * interno;



                float x2 =

                        cx + (float)Math.cos(angulo) * externo;



                float y2 =

                        cy + (float)Math.sin(angulo) * externo;



                linha.setStrokeWidth(

                        dp(i % 5 == 0 ? 2.2f : 1.2f)

                );



                linha.setColor(

                        Color.argb(

                                i % 5 == 0 ? 210 : 125,

                                0,

                                205,

                                255

                        )

                );



                linha.setShadowLayer(

                        dp(7),

                        0,

                        0,

                        Color.rgb(0, 190, 255)

                );



                canvas.drawLine(

                        x1,

                        y1,

                        x2,

                        y2,

                        linha

                );

            }



            linha.clearShadowLayer();



            // ====================================================

            // SEGUNDO HALO AZUL ATRÁS DA FOTO

            // ====================================================

            Paint halo = new Paint(Paint.ANTI_ALIAS_FLAG);



            halo.setStyle(Paint.Style.STROKE);

            halo.setStrokeWidth(dp(3));

            halo.setColor(Color.rgb(0, 220, 255));



            halo.setShadowLayer(

                    dp(12),

                    0,

                    0,

                    Color.rgb(0, 200, 255)

            );



            float raio =

                    Math.min(

                            b.width(),

                            b.height()

                    ) * 0.35f;



            canvas.drawCircle(

                    cx,

                    cy,

                    raio,

                    halo

            );



            halo.clearShadowLayer();

        }



        @Override

        public void setAlpha(int alpha) {}



        @Override

        public void setColorFilter(

                android.graphics.ColorFilter filter) {}



        @Override

        public int getOpacity() {

            return android.graphics.PixelFormat.TRANSLUCENT;

        }

    }





    // ============================================================

    // NEON INTERNO DOS BALÕES — BRILHO + FRAGMENTOS ANIMADOS

    // ============================================================

    private class NeonBalaoDrawable extends Drawable {



        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Paint brilho = new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Random random = new Random();



        private float movimento = 0f;

        private final ArrayList<Float> fragmentosX = new ArrayList<>();

        private final ArrayList<Float> fragmentosY = new ArrayList<>();

        private final ArrayList<Float> fragmentosTamanho = new ArrayList<>();

        private final ValueAnimator animator;



        NeonBalaoDrawable() {

            for (int i = 0; i < 22; i++) {

                fragmentosX.add(random.nextFloat());

                fragmentosY.add(random.nextFloat());

                fragmentosTamanho.add(1.5f + random.nextFloat() * 3.5f);

            }



            animator = ValueAnimator.ofFloat(0f, 1f);

            animator.setDuration(3600);

            animator.setRepeatCount(ValueAnimator.INFINITE);

            animator.setInterpolator(new LinearInterpolator());



            animator.addUpdateListener(animation -> {

                movimento = (float) animation.getAnimatedValue();

                invalidateSelf();

            });



            animator.start();

        }



        @Override

        public void draw(Canvas canvas) {

            Rect b = getBounds();



            if (b.width() <= 0 || b.height() <= 0) return;



            LinearGradient fundo = new LinearGradient(

                    b.left, b.top,

                    b.right, b.bottom,

                    new int[] {

                            Color.rgb(3, 5, 16),

                            Color.rgb(38, 4, 28),

                            Color.rgb(8, 12, 42),

                            Color.rgb(35, 3, 25),

                            Color.rgb(3, 5, 16)

                    },

                    null,

                    Shader.TileMode.CLAMP

            );



            paint.setShader(fundo);

            canvas.drawRoundRect(

                    b.left, b.top, b.right, b.bottom,

                    dp(24), dp(24), paint

            );

            paint.setShader(null);



            brilho.setShader(new RadialGradient(

                    b.left + b.width() * (0.18f + movimento * 0.55f),

                    b.top + b.height() * 0.25f,

                    Math.max(b.width(), b.height()) * 0.65f,

                    new int[] {

                            Color.argb(115, 255, 20, 75),

                            Color.argb(55, 255, 20, 100),

                            Color.TRANSPARENT

                    },

                    null,

                    Shader.TileMode.CLAMP

            ));



            canvas.drawRoundRect(

                    b.left, b.top, b.right, b.bottom,

                    dp(24), dp(24), brilho

            );



            brilho.setShader(null);



            for (int i = 0; i < fragmentosX.size(); i++) {



                float x = b.left + fragmentosX.get(i) * b.width();

                float yBase = b.top + fragmentosY.get(i) * b.height();



                float deslocamento =

                        (movimento * (18f + i * 1.7f)) % b.height();



                float y = yBase + deslocamento;



                if (y > b.bottom) y -= b.height();



                float tamanho = fragmentosTamanho.get(i);



                brilho.setColor(

                        i % 3 == 0

                                ? Color.argb(210, 255, 30, 90)

                                : i % 3 == 1

                                    ? Color.argb(180, 40, 180, 255)

                                    : Color.argb(160, 190, 40, 255)

                );



                brilho.setShadowLayer(

                        dp(7), 0, 0, brilho.getColor()

                );



                canvas.save();

                canvas.rotate(

                        (movimento * 180f + i * 23f) % 360f,

                        x, y

                );



                Path fragmento = new Path();

                fragmento.moveTo(x, y - tamanho * 2.5f);

                fragmento.lineTo(x + tamanho, y);

                fragmento.lineTo(x, y + tamanho * 2.5f);

                fragmento.lineTo(x - tamanho, y);

                fragmento.close();



                canvas.drawPath(fragmento, brilho);

                canvas.restore();

            }



            brilho.clearShadowLayer();



            float faixaX =

                    b.left - b.height()

                    + (b.width() + b.height()) * movimento;



            Paint faixa = new Paint(Paint.ANTI_ALIAS_FLAG);

            faixa.setShader(new LinearGradient(

                    faixaX - dp(45), b.top,

                    faixaX + dp(45), b.bottom,

                    Color.TRANSPARENT,

                    Color.argb(95, 255, 45, 100),

                    Shader.TileMode.CLAMP

            ));



            canvas.save();

            canvas.clipRect(b.left, b.top, b.right, b.bottom);

            canvas.drawRect(

                    b.left, b.top, b.right, b.bottom, faixa

            );

            canvas.restore();

        }



        @Override

        public void setAlpha(int alpha) {}



        @Override

        public void setColorFilter(

                android.graphics.ColorFilter filter) {}



        @Override

        public int getOpacity() {

            return android.graphics.PixelFormat.TRANSLUCENT;

        }

    }







    private FrameLayout raiz;

    private FrameLayout telaMenu;





    // Controle da equipe exibida na Home

    private String equipeJsonExibida = null;



    private com.google.firebase.firestore.ListenerRegistration equipeListenerCloud;

    // Controle do carrossel de anúncios na Home

    private com.google.firebase.firestore.ListenerRegistration anunciosListenerHome;

    private LinearLayout faixaAnunciosHome;

    private HorizontalScrollView scrollAnunciosHome;

    private android.os.Handler handlerCarrosselAnuncios;

    private Runnable tarefaCarrosselAnuncios;

    private int indiceCarrosselAnuncios = 0;











    private String ultimaEquipeHome = "";

    private View pontoIA;

    private View pontoFilmes;

    private View pontoHistorico;

    private View pontoPerfil;

    private View pontoHave;

    private View pontoConexoes;







    LinearLayout tela;

    boolean escuro = true;

    android.content.SharedPreferences prefs;



    int dp(float v) {

        return (int)(v * getResources().getDisplayMetrics().density + .5f);

    }



    GradientDrawable bg(int cor, float raio) {

        GradientDrawable g = new GradientDrawable();

        g.setColor(cor);

        g.setCornerRadius(dp(raio));

        return g;

    }



    TextView txt(String s, float tam, int cor) {

        TextView t = new TextView(this);

        t.setText(s);

        t.setTextSize(tam);



        // Cores neon continuam recebidas normalmente.

        // No modo claro, apenas branco puro de textos neutros

        // vira uma cor escura para manter a leitura.

        int corFinal = cor;



        if (!escuro && cor == Color.WHITE) {

            corFinal = Color.rgb(25, 30, 42);

        }



        t.setTextColor(corFinal);

        t.setGravity(Gravity.CENTER);

        return t;

    }



    Button card(String titulo, String subtitulo, String emoji) {

        Button b = new Button(this);



        b.setText(emoji + "\n" + titulo + "\n" + subtitulo);

        b.setTextSize(15);

        b.setTextColor(Color.WHITE);

        b.setAllCaps(false);

        b.setGravity(Gravity.CENTER);

        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        b.setPadding(dp(18), dp(12), dp(18), dp(12));



        NeonCardDrawable fundo = new NeonCardDrawable(titulo);

        b.setBackground(fundo);



        if (android.os.Build.VERSION.SDK_INT >= 21) {

            b.setElevation(dp(7));

        }



        b.setStateListAnimator(null);



        b.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                v.animate()

                        .scaleX(.97f)

                        .scaleY(.97f)

                        .alpha(.88f)

                        .setDuration(100)

                        .start();

            } else if (event.getAction() == MotionEvent.ACTION_UP

                    || event.getAction() == MotionEvent.ACTION_CANCEL) {

                v.animate()

                        .scaleX(1f)

                        .scaleY(1f)

                        .alpha(1f)

                        .setDuration(160)

                        .start();

            }

            return false;

        });



        return b;

    }



    private JSONArray carregarCatalogoNexo() {

        try (InputStream entrada = getAssets().open(

                "catalogo_filmes/catalogo.json")) {



            BufferedReader leitor = new BufferedReader(

                    new InputStreamReader(entrada, "UTF-8"));



            StringBuilder texto = new StringBuilder();

            String linha;



            while ((linha = leitor.readLine()) != null) {

                texto.append(linha);

            }



            JSONObject catalogo = new JSONObject(texto.toString());

            return catalogo.getJSONArray("filmes");



        } catch (Exception e) {

            android.util.Log.e(

                    "NEXO", "Erro ao carregar catálogo", e);

            return new JSONArray();

        }

    }



    protected void onCreate(Bundle b) {

        super.onCreate(b);



        prefs = getSharedPreferences("DaNikeAI_Dados", 0);

        escuro = prefs.getBoolean("modo_escuro", true);



        aplicarTemaSistema();



        montarHome();

        DaNikeNotificacoes.registrarTokenFCM(this);



        AtualizacaoApp.verificar(this);

    }



    void aplicarTemaSistema() {

        android.view.Window janela = getWindow();



        if (escuro) {

            janela.setStatusBarColor(Color.rgb(2, 6, 18));

            janela.setNavigationBarColor(Color.rgb(2, 6, 18));



            if (android.os.Build.VERSION.SDK_INT >= 23) {

                janela.getDecorView().setSystemUiVisibility(0);

            }



        } else {

            janela.setStatusBarColor(Color.rgb(242, 246, 252));

            janela.setNavigationBarColor(Color.rgb(242, 246, 252));



            if (android.os.Build.VERSION.SDK_INT >= 23) {

                janela.getDecorView().setSystemUiVisibility(

                        android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

                        | android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

                );

            }

        }

    }



    void montarHome() {



        tela = new LinearLayout(this);

        tela.setOrientation(LinearLayout.VERTICAL);

        tela.setPadding(

                dp(14),

                dp(12),

                dp(14),

                dp(14)

        );



        NeonBackgroundView fundo =

                new NeonBackgroundView(this);



        // O fundo estrutural acompanha o tema.

        // Elementos neon continuam independentes e preservados.

        if (!escuro) {

            fundo.setAlpha(0.92f);

        } else {

            fundo.setAlpha(1.0f);

        }



        raiz = new FrameLayout(this);



        TextView botaoMenu = txt("☰  MENU", 14, Color.WHITE);

        botaoMenu.setGravity(Gravity.CENTER);

        botaoMenu.setTypeface(Typeface.DEFAULT, Typeface.BOLD);



        GradientDrawable fundoMenu = new GradientDrawable(

                GradientDrawable.Orientation.LEFT_RIGHT,

                new int[] {

                        Color.rgb(25, 10, 55),

                        Color.rgb(8, 35, 75)

                }

        );

        fundoMenu.setCornerRadius(dp(26));

        fundoMenu.setStroke(dp(2), Color.rgb(75, 145, 255));



        botaoMenu.setBackground(fundoMenu);

        botaoMenu.setElevation(dp(10));

        botaoMenu.setPadding(dp(18), 0, dp(18), 0);



        FrameLayout.LayoutParams menuParams =

                new FrameLayout.LayoutParams(

                        dp(130),

                        dp(48),

                        Gravity.TOP | Gravity.END

                );



        menuParams.setMargins(0, dp(350), dp(14), 0);

raiz.addView(fundo, new FrameLayout.LayoutParams(-1, -1));



// CABEÇALHO PREMIUM DA HOME

LinearLayout cabecalhoNexo = new LinearLayout(this);

cabecalhoNexo.setOrientation(LinearLayout.HORIZONTAL);

cabecalhoNexo.setGravity(Gravity.CENTER_VERTICAL);

cabecalhoNexo.setPadding(dp(14), dp(8), dp(10), dp(8));



GradientDrawable fundoCabecalho = new GradientDrawable(

        GradientDrawable.Orientation.LEFT_RIGHT,

        new int[] {

                Color.rgb(5, 12, 28),

                Color.rgb(12, 8, 32)

        }

);

fundoCabecalho.setCornerRadius(dp(22));

fundoCabecalho.setStroke(dp(1), Color.rgb(0, 190, 255));

cabecalhoNexo.setBackground(fundoCabecalho);

cabecalhoNexo.setElevation(dp(12));



ImageView logoNexoTopo = new ImageView(this);

logoNexoTopo.setImageResource(R.drawable.nexo_logo);

logoNexoTopo.setScaleType(ImageView.ScaleType.FIT_CENTER);

logoNexoTopo.setAdjustViewBounds(true);



LinearLayout.LayoutParams logoTopoParams =

        new LinearLayout.LayoutParams(0, dp(62), 1f);

cabecalhoNexo.addView(logoNexoTopo, logoTopoParams);



LinearLayout perfilTopoBox = new LinearLayout(this);

perfilTopoBox.setOrientation(LinearLayout.VERTICAL);

perfilTopoBox.setGravity(Gravity.CENTER_HORIZONTAL);



TextView botaoPerfilTopo = txt("👤", 20, Color.WHITE);

botaoPerfilTopo.setGravity(Gravity.CENTER);



GradientDrawable fundoPerfilTopo = new GradientDrawable();

fundoPerfilTopo.setShape(GradientDrawable.OVAL);

fundoPerfilTopo.setColor(Color.rgb(10, 24, 48));

fundoPerfilTopo.setStroke(dp(1), Color.rgb(70, 145, 255));

botaoPerfilTopo.setBackground(fundoPerfilTopo);

botaoPerfilTopo.setElevation(dp(10));



perfilTopoBox.addView(

        botaoPerfilTopo,

        new LinearLayout.LayoutParams(dp(42), dp(42))

);



TextView textoPerfilTopo = txt("PERFIL", 9, Color.WHITE);

textoPerfilTopo.setGravity(Gravity.CENTER);

textoPerfilTopo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

textoPerfilTopo.setLetterSpacing(0.08f);



LinearLayout.LayoutParams perfilTextoParams =

        new LinearLayout.LayoutParams(dp(58), dp(20));

perfilTextoParams.setMargins(0, dp(2), 0, 0);

perfilTopoBox.addView(textoPerfilTopo, perfilTextoParams);



LinearLayout.LayoutParams perfilBoxParams =

        new LinearLayout.LayoutParams(dp(70), dp(68));

cabecalhoNexo.addView(perfilTopoBox, perfilBoxParams);



FrameLayout.LayoutParams cabecalhoParams =

        new FrameLayout.LayoutParams(-1, dp(86), Gravity.TOP);

cabecalhoParams.setMargins(dp(12), dp(18), dp(12), 0);

raiz.addView(cabecalhoNexo, cabecalhoParams);



botaoPerfilTopo.setOnClickListener(v ->

        startActivity(new Intent(MainActivity.this, PerfilActivity.class))

);



// =========================

        // HOME LIMPA — LOGO NEXO + BOTÃO JF

        // =========================

        LinearLayout telaHomeVazia = new LinearLayout(this);

        telaHomeVazia.setOrientation(LinearLayout.VERTICAL);

        telaHomeVazia.setGravity(Gravity.START);

        telaHomeVazia.setPadding(dp(14), dp(112), dp(14), dp(90));

        telaHomeVazia.setBackgroundColor(Color.rgb(3, 6, 15));



        FrameLayout.LayoutParams homeVaziaParams =

                new FrameLayout.LayoutParams(-1, -1);

        homeVaziaParams.bottomMargin = dp(76);

        raiz.addView(telaHomeVazia, homeVaziaParams);



        // Faixa horizontal reservada para anúncios publicados

        faixaAnunciosHome = new LinearLayout(this);

        faixaAnunciosHome.setOrientation(LinearLayout.HORIZONTAL);

        faixaAnunciosHome.setGravity(Gravity.CENTER_VERTICAL);

        faixaAnunciosHome.setPadding(dp(2), dp(4), dp(2), dp(4));



        scrollAnunciosHome = new HorizontalScrollView(this);

        scrollAnunciosHome.setHorizontalScrollBarEnabled(false);

        scrollAnunciosHome.setFillViewport(false);

        scrollAnunciosHome.addView(

                faixaAnunciosHome,

                new HorizontalScrollView.LayoutParams(

                        ViewGroup.LayoutParams.WRAP_CONTENT,

                        ViewGroup.LayoutParams.WRAP_CONTENT

                )

        );

        LinearLayout.LayoutParams anunciosHomeParams =

                new LinearLayout.LayoutParams(

                        ViewGroup.LayoutParams.MATCH_PARENT,

                        ViewGroup.LayoutParams.WRAP_CONTENT

                );

        anunciosHomeParams.bottomMargin = dp(12);

        telaHomeVazia.addView(scrollAnunciosHome, anunciosHomeParams);



        LinearLayout equipeTopo = new LinearLayout(this);

        equipeTopo.setOrientation(LinearLayout.VERTICAL);

        equipeTopo.setGravity(Gravity.CENTER_HORIZONTAL);



        TextView tituloEquipe = txt(

                "👥  EQUIPE DE DESENVOLVIMENTO & GESTÃO",

                15,

                Color.WHITE

        );

        tituloEquipe.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        tituloEquipe.setGravity(Gravity.CENTER);



        GradientDrawable fundoTituloEquipe = new GradientDrawable();

        fundoTituloEquipe.setShape(GradientDrawable.RECTANGLE);

        fundoTituloEquipe.setColor(Color.rgb(8, 10, 28));

        fundoTituloEquipe.setCornerRadius(dp(30));

        fundoTituloEquipe.setStroke(

                dp(2),

                Color.rgb(190, 40, 255)

        );

        tituloEquipe.setBackground(fundoTituloEquipe);

        tituloEquipe.setPadding(

                dp(14),

                dp(5),

                dp(14),

                dp(5)

        );



        LinearLayout.LayoutParams tituloParams =

                new LinearLayout.LayoutParams(

                        -2,

                        dp(44)

                );

        tituloParams.setMargins(

                0,

                dp(4),

                0,

                dp(5)

        );

        equipeTopo.addView(

                tituloEquipe,

                tituloParams

        );



        HorizontalScrollView equipeScroll =

                new HorizontalScrollView(this);

        equipeScroll.setHorizontalScrollBarEnabled(false);

        equipeScroll.setVerticalScrollBarEnabled(false);

        equipeScroll.setOverScrollMode(

                View.OVER_SCROLL_NEVER

        );

        equipeScroll.setFillViewport(false);



        LinearLayout equipeLinha =

                new LinearLayout(this);

        equipeLinha.setOrientation(

                LinearLayout.HORIZONTAL

        );

        equipeLinha.setGravity(

                Gravity.CENTER_VERTICAL

        );

        equipeLinha.setPadding(

                dp(20),

                0,

                dp(20),

                0

        );



        android.content.SharedPreferences equipePrefs =

                getSharedPreferences(

                        "DaNikeAI_ADM",

                        MODE_PRIVATE

                );



        String equipeJson =

                equipePrefs.getString(

                        "equipe_perfis",

                        "[]"

                );



        boolean temEquipe = false;



        try {

            org.json.JSONArray lista =

                    new org.json.JSONArray(equipeJson);



            for (int i = 0; i < lista.length(); i++) {



                org.json.JSONObject pessoa =

                        lista.getJSONObject(i);



                String nome =

                        pessoa.optString(

                                "nome",

                                ""

                        ).trim();



                String cargo =

                        pessoa.optString(

                                "cargo",

                                ""

                        ).trim();



                String fotoUri =

                                pessoa.optString(

                                        "fotoUri",

                                        ""

                                ).trim();



                        String fotoUrl =

                                pessoa.optString(

                                        "fotoUrl",

                                        ""

                                ).trim();



                        if (fotoUri.isEmpty() && !fotoUrl.isEmpty()) {

                            fotoUri = fotoUrl;

                        }



                        if (nome.isEmpty()

                        || cargo.isEmpty()

                        || fotoUri.isEmpty()) {

                    continue;

                }



                java.io.File arquivoFoto = null;



                boolean fotoRemota =

                        fotoUri.startsWith("http://")

                                || fotoUri.startsWith("https://");



                if (!fotoUri.startsWith("content://") && !fotoRemota) {

                    arquivoFoto = new java.io.File(fotoUri);

                    if (!arquivoFoto.exists()) {

                        continue;

                    }

                }



                temEquipe = true;



                String coroa =

                        nome.equalsIgnoreCase("Anderson") ? "👑" : "";



                    String instagramUsuario =

                        pessoa.optString("instagramUsuario", "").trim();



                    String instagramLink =

                        pessoa.optString("instagramLink", "").trim();



                    adicionarMembroEquipe(

                        equipeLinha,

                        nome,

                        cargo,

                        coroa,

                        fotoUri,

                        instagramUsuario,

                        instagramLink

                    );

            }



        } catch (Exception ignored) {

        }



        equipeJsonExibida = equipeJson;



        int quantidadeEquipe = 0;

        try {

            quantidadeEquipe = new org.json.JSONArray(equipeJson).length();

        } catch (Exception ignored) {

        }



        if (quantidadeEquipe <= 3) {

            equipeLinha.setGravity(Gravity.CENTER);

            equipeScroll.setFillViewport(true);

        } else {

            equipeLinha.setGravity(Gravity.CENTER_VERTICAL);

            equipeScroll.setFillViewport(false);

        }



        equipeScroll.addView(

                equipeLinha,

                new HorizontalScrollView.LayoutParams(

                        quantidadeEquipe <= 3 ? -1 : -2,

                        dp(300)

                )

        );



        equipeTopo.addView(

                equipeScroll,

                new LinearLayout.LayoutParams(

                        -1,

                        dp(300)

                )

        );



        // =========================

        // PAINEL NEON / ÁGUA DA EQUIPE

        // =========================



        equipeTopo.setVisibility(

                temEquipe

                        ? View.VISIBLE

                        : View.GONE

        );



        GradientDrawable painelEquipe =

                new GradientDrawable(

                        GradientDrawable.Orientation.TL_BR,

                        new int[] {

                                Color.rgb(4, 18, 38),

                                Color.rgb(12, 8, 42),

                                Color.rgb(5, 25, 48)

                        }

                );



        painelEquipe.setCornerRadius(dp(32));



        painelEquipe.setStroke(

                dp(3),

                Color.rgb(255, 25, 70)

        );

        equipeTopo.setBackground(painelEquipe);



        // Fundo neon removido: classe EquipeNeonDrawable nao existe



        equipeTopo.setPadding(

                dp(4),

                dp(2),

                dp(4),

                dp(6)

        );



        FrameLayout.LayoutParams equipeParams =

                new FrameLayout.LayoutParams(

                        -1,

                        dp(340)

                );



        equipeParams.setMargins(

                dp(8),

                dp(4),

                dp(8),

                0

        );



        raiz.addView(

                equipeTopo,

                equipeParams

        );



        equipeTopo.setElevation(dp(20));

        equipeTopo.bringToFront();



        ScrollView scroll = new ScrollView(this);

        scroll.setFillViewport(true);

        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);



        LinearLayout conteudo = new LinearLayout(this);

        conteudo.setOrientation(LinearLayout.VERTICAL);

        conteudo.setPadding(0, dp(5), 0, dp(20));



        // =========================

        // CABEÇALHO

        // =========================



        TextView titulo = txt(

                "DaNikeAI",

                30,

                Color.WHITE

        );



        titulo.setTypeface(

                Typeface.DEFAULT,

                Typeface.BOLD

        );



        conteudo.addView(

                titulo,

                new LinearLayout.LayoutParams(

                        -1,

                        dp(52)

                )

        );



        String nomeUsuario = prefs.getString("nome", "Usuário").trim();

        if (nomeUsuario.isEmpty()) nomeUsuario = "Usuário";



        TextView descricao = txt(

                "Escolha uma área para continuar",

                14,

                Color.rgb(125, 165, 190)

        );



        conteudo.addView(

                descricao,

                new LinearLayout.LayoutParams(

                        -1,

                        dp(34)

                )

        );



        // =========================

        // NOSSA PÁGINA — VISUAL

        // =========================



        LinearLayout paginaOficial = new LinearLayout(this);

        paginaOficial.setOrientation(LinearLayout.HORIZONTAL);

        paginaOficial.setGravity(Gravity.CENTER_VERTICAL);

        paginaOficial.setPadding(dp(14), dp(10), dp(14), dp(10));



        GradientDrawable fundoPagina = new GradientDrawable(

                GradientDrawable.Orientation.LEFT_RIGHT,

                new int[] {

                        Color.rgb(8, 10, 30),

                        Color.rgb(24, 8, 45),

                        Color.rgb(5, 28, 48)

                }

        );



        fundoPagina.setCornerRadius(dp(24));

        fundoPagina.setStroke(dp(3), Color.rgb(255, 25, 70));



        paginaOficial.setBackground(fundoPagina);

        paginaOficial.setElevation(dp(12));



        // Logo oficial do Instagram

        android.widget.ImageView instagramIcone =

                new android.widget.ImageView(this);

        instagramIcone.setImageResource(

                R.drawable.instagram_oficial

        );

        instagramIcone.setScaleType(

                android.widget.ImageView.ScaleType.CENTER_INSIDE

        );



        LinearLayout.LayoutParams instagramParams =

                new LinearLayout.LayoutParams(

                        dp(72),

                        dp(72)

                );



        paginaOficial.addView(

                instagramIcone,

                instagramParams

        );



        LinearLayout textosPagina = new LinearLayout(this);

        textosPagina.setOrientation(LinearLayout.VERTICAL);

        textosPagina.setGravity(Gravity.CENTER_VERTICAL);



        TextView nomePagina = txt(

                "DaNikeAI  ✓",

                19,

                Color.WHITE

        );



        nomePagina.setTypeface(

                Typeface.DEFAULT,

                Typeface.BOLD

        );



        TextView subtituloPagina = txt(

                "Nossa página oficial",

                13,

                Color.rgb(190, 225, 245)

        );



        TextView acaoPagina = txt(

                "Em breve • Página oficial",

                12,

                Color.rgb(100, 200, 255)

        );



        textosPagina.addView(nomePagina);

        textosPagina.addView(subtituloPagina);

        textosPagina.addView(acaoPagina);



        LinearLayout.LayoutParams textosPaginaParams =

                new LinearLayout.LayoutParams(

                        0,

                        -2,

                        1f

                );



        textosPaginaParams.setMargins(

                dp(14),

                0,

                dp(8),

                0

        );



        paginaOficial.addView(

                textosPagina,

                textosPaginaParams

        );



        TextView setaPagina = txt(

                "›",

                34,

                Color.rgb(90, 210, 255)

        );



        setaPagina.setGravity(Gravity.CENTER);



        paginaOficial.addView(

                setaPagina,

                new LinearLayout.LayoutParams(

                        dp(35),

                        dp(60)

                )

        );



        LinearLayout.LayoutParams paginaParams =

                new LinearLayout.LayoutParams(

                        -1,

                        dp(98)

                );



        paginaParams.setMargins(

                dp(4),

                dp(14),

                dp(4),

                dp(18)

        );



        conteudo.addView(

                paginaOficial,

                paginaParams

        );







        // =========================================================

        // INSTAGRAM DA EQUIPE — PAINEL NEON

        // =========================================================

        LinearLayout painelInstagramEquipe =

                new LinearLayout(this);

        painelInstagramEquipe.setOrientation(

                LinearLayout.VERTICAL

        );

        painelInstagramEquipe.setGravity(

                Gravity.CENTER_VERTICAL

        );



        GradientDrawable fundoPainelInstagram =

                new GradientDrawable(

                        GradientDrawable.Orientation.TL_BR,

                        new int[] {

                                Color.rgb(10, 7, 24),

                                Color.rgb(18, 8, 38),

                                Color.rgb(8, 18, 38)

                        }

                );

        fundoPainelInstagram.setCornerRadius(

                dp(20)

        );

        fundoPainelInstagram.setStroke(

                dp(2),

                Color.rgb(80, 130, 255)

        );



        painelInstagramEquipe.setBackground(

                fundoPainelInstagram

        );

        painelInstagramEquipe.setElevation(

                dp(10)

        );

        painelInstagramEquipe.setPadding(

                dp(8),

                dp(7),

                dp(8),

                dp(7)

        );



        HorizontalScrollView instagramScroll =

                new HorizontalScrollView(this);

        instagramScroll.setHorizontalScrollBarEnabled(

                false

        );

        instagramScroll.setOverScrollMode(

                View.OVER_SCROLL_NEVER

        );



        LinearLayout instagramLinha =

                new LinearLayout(this);

        instagramLinha.setOrientation(

                LinearLayout.HORIZONTAL

        );

        instagramLinha.setGravity(

                Gravity.CENTER_VERTICAL

        );



        try {

            org.json.JSONArray perfisInstagram =

                    new org.json.JSONArray(equipeJson);



            for (int i = 0;

                    i < perfisInstagram.length();

                    i++) {



                org.json.JSONObject perfilInstagram =

                        perfisInstagram.getJSONObject(i);



                String usuarioInstagram =

                        perfilInstagram.optString(

                                "instagramUsuario",

                                ""

                        ).trim();



                String linkInstagram =

                        perfilInstagram.optString(

                                "instagramLink",

                                ""

                        ).trim();



                if (usuarioInstagram.isEmpty()) {

                    continue;

                }



                if (linkInstagram.isEmpty()) {

                    linkInstagram =

                            "https://instagram.com/" +

                            usuarioInstagram;

                }



                if (!linkInstagram.startsWith("http://") &&

                        !linkInstagram.startsWith("https://")) {

                    linkInstagram =

                            "https://" + linkInstagram;

                }



                final String linkFinalInstagram =

                        linkInstagram;



                LinearLayout perfilBox =

                        new LinearLayout(this);

                perfilBox.setOrientation(

                        LinearLayout.VERTICAL

                );

                perfilBox.setGravity(

                        Gravity.CENTER

                );

                perfilBox.setPadding(

                        dp(4),

                        0,

                        dp(4),

                        0

                );



                // Logo oficial do Instagram

                android.widget.ImageView logoInstagram =

                        new android.widget.ImageView(this);

                logoInstagram.setImageResource(

                        R.drawable.instagram_oficial

                );

                logoInstagram.setScaleType(

                        android.widget.ImageView.ScaleType.CENTER_INSIDE

                );



                LinearLayout.LayoutParams logoInstagramParams =

                        new LinearLayout.LayoutParams(

                                dp(38),

                                dp(38)

                        );

                logoInstagramParams.setMargins(

                        dp(2),

                        0,

                        dp(2),

                        dp(5)

                );



                perfilBox.addView(

                        logoInstagram,

                        logoInstagramParams

                );



                // Nome + verificação

                LinearLayout nomeBox =

                        new LinearLayout(this);

                nomeBox.setOrientation(

                        LinearLayout.HORIZONTAL

                );

                nomeBox.setGravity(

                        Gravity.CENTER

                );

                nomeBox.setPadding(

                        dp(10),

                        dp(6),

                        dp(10),

                        dp(6)

                );



                GradientDrawable fundoNome =

                        new GradientDrawable();

                fundoNome.setShape(

                        GradientDrawable.RECTANGLE

                );

                fundoNome.setColor(

                        Color.rgb(12, 10, 28)

                );

                fundoNome.setCornerRadius(

                        dp(22)

                );

                fundoNome.setStroke(

                        dp(1),

                        Color.rgb(70, 120, 255)

                );



                nomeBox.setBackground(

                        fundoNome

                );

                nomeBox.setElevation(

                        dp(8)

                );



                TextView nomeInstagram =

                        txt(

                                "@" + usuarioInstagram,

                                11,

                                Color.WHITE

                        );

                nomeInstagram.setTypeface(

                        Typeface.DEFAULT,

                        Typeface.BOLD

                );

                nomeInstagram.setSingleLine(

                        true

                );



                nomeBox.addView(

                        nomeInstagram,

                        new LinearLayout.LayoutParams(

                                -2,

                                dp(24)

                        )

                );



                TextView seloInstagram =

                        txt(

                                "✓",

                                9,

                                Color.WHITE

                        );

                seloInstagram.setGravity(

                        Gravity.CENTER

                );

                seloInstagram.setTypeface(

                        Typeface.DEFAULT,

                        Typeface.BOLD

                );



                GradientDrawable fundoSelo =

                        new GradientDrawable();

                fundoSelo.setShape(

                        GradientDrawable.OVAL

                );

                fundoSelo.setColor(

                        Color.rgb(35, 135, 255)

                );

                fundoSelo.setStroke(

                        dp(1),

                        Color.rgb(105, 190, 255)

                );



                seloInstagram.setBackground(

                        fundoSelo

                );



                LinearLayout.LayoutParams seloParams =

                        new LinearLayout.LayoutParams(

                                dp(16),

                                dp(16)

                        );

                seloParams.setMargins(

                        dp(6),

                        0,

                        0,

                        0

                );



                nomeBox.addView(

                        seloInstagram,

                        seloParams

                );



                perfilBox.addView(

                        nomeBox,

                        new LinearLayout.LayoutParams(

                                -2,

                                dp(38)

                        )

                );



                // =========================================================

                // GLOW PREMIUM DO BOTÃO — ACENDE E APAGA LENTAMENTE

                // =========================================================

                android.animation.ValueAnimator glow =

                        android.animation.ValueAnimator.ofFloat(

                                0.55f,

                                1.0f

                        );



                glow.setDuration(1500);



                glow.setRepeatMode(

                        android.animation.ValueAnimator.REVERSE

                );



                glow.setRepeatCount(

                        android.animation.ValueAnimator.INFINITE

                );



                glow.addUpdateListener(animation -> {

                    float intensidade =

                            (float) animation.getAnimatedValue();



                    nomeBox.setAlpha(intensidade);



                    nomeBox.setElevation(

                            dp((int)(8 + (intensidade * 14)))

                    );

                });



                glow.start();



                // Abre o perfil do Instagram

                perfilBox.setOnClickListener(v -> {

                    try {

                        startActivity(

                                new Intent(

                                        Intent.ACTION_VIEW,

                                        android.net.Uri.parse(

                                                linkFinalInstagram

                                        )

                                )

                        );

                    } catch (Exception e) {

                        Toast.makeText(

                                this,

                                "Não foi possível abrir o Instagram.",

                                Toast.LENGTH_SHORT

                        ).show();

                    }

                });



                instagramLinha.addView(

                        perfilBox,

                        new LinearLayout.LayoutParams(

                                -2,

                                -1

                        )

                );

            }



        } catch (Exception e) {

            e.printStackTrace();

        }



        instagramScroll.addView(

                instagramLinha,

                new HorizontalScrollView.LayoutParams(

                        -2,

                        -1

                )

        );



        painelInstagramEquipe.addView(

                instagramScroll,

                new LinearLayout.LayoutParams(

                        -1,

                        dp(108)

                )

        );



        LinearLayout.LayoutParams painelInstagramParams =

                new LinearLayout.LayoutParams(

                        -1,

                        dp(122)

                );

        painelInstagramParams.setMargins(

                dp(4),

                0,

                dp(4),

                dp(10)

        );



        conteudo.addView(

                painelInstagramEquipe,

                painelInstagramParams

        );



        // =========================

        // CAPA IA

        // =========================



        Button ia = card(

                "IA",

                "Sua assistente IZy",

                "🤖"

        );



        LinearLayout.LayoutParams iaParams =

                new LinearLayout.LayoutParams(

                        -1,

                        dp(155)

                );



        iaParams.setMargins(0, dp(10), 0, dp(16));



        FrameLayout containerIA = new FrameLayout(this);

        containerIA.setClipChildren(false);

        containerIA.addView(ia, new FrameLayout.LayoutParams(-1, -1));



        pontoIA = criarIndicadorManutencao();



        FrameLayout.LayoutParams pontoIAParams =

                new FrameLayout.LayoutParams(dp(14), dp(14),

                        Gravity.TOP | Gravity.END);

        pontoIAParams.setMargins(0, dp(2), dp(2), 0);

        containerIA.addView(pontoIA, pontoIAParams);



        // =========================

        // CAPA FILMES

        // =========================



        Button filmes = card(

                "Filmes",

                "Explore seus filmes",

                "🎬"

        );



        LinearLayout.LayoutParams filmesParams =

                new LinearLayout.LayoutParams(

                        -1,

                        dp(155)

                );



        filmesParams.setMargins(0, 0, 0, dp(16));



        FrameLayout containerFilmes = new FrameLayout(this);

        containerFilmes.setClipChildren(false);

        containerFilmes.addView(filmes, new FrameLayout.LayoutParams(-1, -1));



        pontoFilmes = criarIndicadorManutencao();



        FrameLayout.LayoutParams pontoFilmesParams =

                new FrameLayout.LayoutParams(dp(14), dp(14),

                        Gravity.TOP | Gravity.END);

        pontoFilmesParams.setMargins(0, dp(2), dp(2), 0);

        containerFilmes.addView(pontoFilmes, pontoFilmesParams);



        // =========================

        // CAPA HISTÓRICO

        // =========================



        Button historico = card(

                "Histórico",

                "Veja suas atividades",

                "🕘"

        );



        LinearLayout.LayoutParams historicoParams =

                new LinearLayout.LayoutParams(

                        -1,

                        dp(155)

                );



        historicoParams.setMargins(0, 0, 0, dp(16));



        FrameLayout containerHistorico = new FrameLayout(this);

        containerHistorico.setClipChildren(false);

        containerHistorico.addView(historico, new FrameLayout.LayoutParams(-1, -1));



        pontoHistorico = criarIndicadorManutencao();



        FrameLayout.LayoutParams pontoHistoricoParams =

                new FrameLayout.LayoutParams(dp(14), dp(14),

                        Gravity.TOP | Gravity.END);

        pontoHistoricoParams.setMargins(0, dp(2), dp(2), 0);

        containerHistorico.addView(pontoHistorico, pontoHistoricoParams);



        // =========================

        // CAPA PERFIL

        // =========================



        Button perfil = card(

                "Perfil",

                "Seus dados e informações",

                "👤"

        );



        LinearLayout.LayoutParams perfilParams =

                new LinearLayout.LayoutParams(

                        -1,

                        dp(155)

                );



        perfilParams.setMargins(0, 0, 0, dp(16));



        FrameLayout containerPerfil = new FrameLayout(this);

        containerPerfil.setClipChildren(false);

        containerPerfil.addView(perfil, new FrameLayout.LayoutParams(-1, -1));



        pontoPerfil = criarIndicadorManutencao();



        FrameLayout.LayoutParams pontoPerfilParams =

                new FrameLayout.LayoutParams(dp(14), dp(14),

                        Gravity.TOP | Gravity.END);

        pontoPerfilParams.setMargins(0, dp(2), dp(2), 0);

        containerPerfil.addView(pontoPerfil, pontoPerfilParams);



        // =========================

        // ADM — SOMENTE DONO

        // =========================



        com.google.firebase.auth.FirebaseUser usuarioAtual = null;



        try {

            usuarioAtual =

                    com.google.firebase.auth.FirebaseAuth

                            .getInstance()

                            .getCurrentUser();

        } catch (Exception ignored) {}



        boolean ehDono =

                usuarioAtual != null

                && "lipesanderson@gmail.com".equalsIgnoreCase(

                        String.valueOf(usuarioAtual.getEmail())

                );





        if (ehDono) {



        }



        // =========================

        // RAVE — MANUTENÇÃO

        // =========================



        Button rave = card(

                "Have",

                "Acessar",

                "▶"

        );



        LinearLayout.LayoutParams raveParams =

                new LinearLayout.LayoutParams(

                        -1,

                        dp(135)

                );



        raveParams.setMargins(0, 0, 0, dp(20));



        FrameLayout containerHave = new FrameLayout(this);

        containerHave.setClipChildren(false);

        containerHave.addView(rave, new FrameLayout.LayoutParams(-1, -1));



        pontoHave = criarIndicadorManutencao();



        FrameLayout.LayoutParams pontoHaveParams =

                new FrameLayout.LayoutParams(dp(14), dp(14),

                        Gravity.TOP | Gravity.END);

        pontoHaveParams.setMargins(0, dp(2), dp(2), 0);

        containerHave.addView(pontoHave, pontoHaveParams);



        // ==============================

        // COMPARTILHAR APP

        // ==============================

        Button compartilharApp = card(

                "Compartilhar App",

                "Envie para seus amigos",

                "📲"

        );



        LinearLayout.LayoutParams compartilharParams =

                new LinearLayout.LayoutParams(-1, dp(135));

        compartilharParams.setMargins(0, 0, 0, dp(20));



        compartilharApp.setOnClickListener(v -> {

            try {

                String versao = getPackageManager()

                        .getPackageInfo(getPackageName(), 0)

                        .versionName;



                String link = "https://github.com/andersonptg/DaNikeAI/releases/latest/download/NEXO.apk";





                String mensagem =

                        "🚀 Baixe o DaNikeAI!\n\n"

                        + "Versão atual: " + versao + "\n\n"

                        + link;



                Intent compartilhar = new Intent(Intent.ACTION_SEND);

                compartilhar.setType("text/plain");

                compartilhar.putExtra(Intent.EXTRA_TEXT, mensagem);



                startActivity(Intent.createChooser(

                        compartilhar,

                        "Compartilhar DaNikeAI"

                ));



            } catch (Exception e) {

                Toast.makeText(

                        MainActivity.this,

                        "Não foi possível compartilhar o app.",

                        Toast.LENGTH_SHORT

                ).show();

            }

        });



        scroll.addView(conteudo);



        tela.addView(

                scroll,

                new LinearLayout.LayoutParams(

                        -1,

                        0,

                        1

                )

        );



        FrameLayout.LayoutParams telaParams =

                new FrameLayout.LayoutParams(-1, -1);

        telaParams.topMargin =

                temEquipe ? dp(260) : dp(5);

        telaParams.bottomMargin = dp(76);

        raiz.addView(tela, telaParams);

        tela.setVisibility(View.GONE);

        equipeTopo.setVisibility(View.GONE);

        botaoMenu.setVisibility(View.GONE);



        // =========================================================

        // BARRA DE NAVEGAÇÃO FIXA — HOME / CONEXÕES / PERFIL

        // =========================================================

        LinearLayout barraNavegacao =

                new LinearLayout(this);

        barraNavegacao.setOrientation(

                LinearLayout.HORIZONTAL

        );

        barraNavegacao.setGravity(

                Gravity.CENTER

        );

        barraNavegacao.setPadding(

                dp(10),

                dp(5),

                dp(10),

                dp(5)

        );



        GradientDrawable fundoBarra =

                new GradientDrawable(

                        GradientDrawable.Orientation.TL_BR,

                        new int[] {

                                Color.rgb(7, 7, 18),

                                Color.rgb(13, 8, 28),

                                Color.rgb(7, 12, 24)

                        }

                );

        fundoBarra.setCornerRadius(dp(22));

        fundoBarra.setStroke(

                dp(1),

                Color.rgb(55, 100, 180)

        );

        barraNavegacao.setBackground(fundoBarra);

        barraNavegacao.setElevation(dp(22));



        TextView botaoHome =

                txt("🏠\nHome", 12, Color.WHITE);

        botaoHome.setGravity(Gravity.CENTER);

        botaoHome.setTypeface(

                Typeface.DEFAULT,

                Typeface.BOLD

        );



        TextView botaoColaboradores = txt("👥\\nColaboradores", 11, Color.WHITE);

        botaoColaboradores.setGravity(Gravity.CENTER);

        botaoColaboradores.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView botaoConexoes = txt("👥\\nConexões", 12, Color.WHITE);

        botaoConexoes.setGravity(Gravity.CENTER);



        pontoConexoes = criarIndicadorManutencao();

        pontoConexoes.setVisibility(View.VISIBLE);

        botaoConexoes.setTypeface(

                Typeface.DEFAULT,

                Typeface.BOLD

        );



        TextView botaoPerfil =

                txt("👤\nPerfil", 12, Color.WHITE);



        TextView botaoMais =

                txt("✦\nAPPS", 12, Color.WHITE);

        botaoMais.setGravity(Gravity.CENTER);

        botaoMais.setTypeface(

                Typeface.DEFAULT,

                Typeface.BOLD

        );



        GradientDrawable fundoAppsNeon =

                new GradientDrawable(

                        GradientDrawable.Orientation.TL_BR,

                        new int[]{

                                Color.rgb(25, 8, 65),

                                Color.rgb(8, 35, 78),

                                Color.rgb(20, 8, 55)

                        }

                );



        fundoAppsNeon.setCornerRadius(dp(18));

        fundoAppsNeon.setStroke(

                dp(2),

                Color.rgb(75, 170, 255)

        );



        botaoMais.setBackground(fundoAppsNeon);

        botaoMais.setElevation(dp(18));



        android.animation.ObjectAnimator pulsoApps =

                android.animation.ObjectAnimator.ofFloat(

                        botaoMais,

                        "alpha",

                        0.72f,

                        1.0f

                );



        pulsoApps.setDuration(1100);

        pulsoApps.setRepeatMode(

                android.animation.ValueAnimator.REVERSE

        );

        pulsoApps.setRepeatCount(

                android.animation.ValueAnimator.INFINITE

        );

        pulsoApps.start();

        botaoPerfil.setGravity(Gravity.CENTER);

        botaoPerfil.setTypeface(

                Typeface.DEFAULT,

                Typeface.BOLD

        );



        // =========================

        // ADM VIP — SOMENTE DONO

        // =========================

        TextView botaoAdm = null;



        if (ehDono) {

            botaoAdm = txt("ADM", 12, Color.rgb(255, 235, 150));

            botaoAdm.setGravity(Gravity.CENTER);

            botaoAdm.setTypeface(Typeface.DEFAULT, Typeface.BOLD);



            GradientDrawable fundoAdmGold =

                    new GradientDrawable(

                            GradientDrawable.Orientation.TL_BR,

                            new int[]{

                                    Color.rgb(70, 45, 5),

                                    Color.rgb(210, 155, 35),

                                    Color.rgb(85, 50, 5)

                            }

                    );



            fundoAdmGold.setCornerRadius(dp(18));

            fundoAdmGold.setStroke(

                    dp(2),

                    Color.rgb(255, 215, 70)

            );



            botaoAdm.setBackground(fundoAdmGold);

            botaoAdm.setElevation(dp(24));



            android.animation.ObjectAnimator pulsoAdm =

                    android.animation.ObjectAnimator.ofFloat(

                            botaoAdm,

                            "alpha",

                            0.78f,

                            1.0f

                    );



            pulsoAdm.setDuration(900);

            pulsoAdm.setRepeatMode(

                    android.animation.ValueAnimator.REVERSE

            );

            pulsoAdm.setRepeatCount(

                    android.animation.ValueAnimator.INFINITE

            );

            pulsoAdm.start();

        }



        TextView[] botoesNav = ehDono

                ? new TextView[]{

                        botaoHome, botaoColaboradores, botaoConexoes, botaoMais,

                        botaoAdm

                }

                : new TextView[]{

                        botaoHome, botaoColaboradores, botaoConexoes, botaoMais

                };



        for (TextView botao : botoesNav) {

            botao.setPadding(

                    dp(8),

                    dp(3),

                    dp(8),

                    dp(3)

            );



            LinearLayout.LayoutParams bp =

                    new LinearLayout.LayoutParams(

                            0,

                            dp(62),

                            1

                    );

            bp.setMargins(

                    dp(3),

                    0,

                    dp(3),

                    0

            );



            barraNavegacao.addView(

                    botao,

                    bp

            );

        }



        // Home fica destacada porque é a tela inicial

        GradientDrawable fundoHome =

                new GradientDrawable();

        fundoHome.setColor(

                Color.rgb(20, 35, 70)

        );

        fundoHome.setCornerRadius(dp(18));

        fundoHome.setStroke(

                dp(1),

                Color.rgb(70, 145, 255)

        );

        botaoHome.setBackground(fundoHome);

        botaoHome.setElevation(dp(10));



        GradientDrawable fundoConexoes = new GradientDrawable();

        fundoConexoes.setColor(Color.rgb(20, 35, 70));

        fundoConexoes.setCornerRadius(dp(18));

        fundoConexoes.setStroke(dp(1), Color.rgb(70, 145, 255));

        botaoConexoes.setBackground(fundoConexoes);

        GradientDrawable fundoColaboradores = new GradientDrawable();

        fundoColaboradores.setColor(Color.rgb(20, 35, 70));

        fundoColaboradores.setCornerRadius(dp(18));

        fundoColaboradores.setStroke(dp(1), Color.rgb(70, 145, 255));

        botaoColaboradores.setBackground(fundoColaboradores);

        botaoColaboradores.setElevation(dp(10));

        botaoConexoes.setElevation(dp(10));



        GradientDrawable fundoPerfil = new GradientDrawable();

        fundoPerfil.setColor(Color.rgb(20, 35, 70));

        fundoPerfil.setCornerRadius(dp(18));

        fundoPerfil.setStroke(dp(1), Color.rgb(70, 145, 255));

        botaoPerfil.setBackground(fundoPerfil);

        botaoPerfil.setElevation(dp(10));



        botaoHome.setOnClickListener(v -> {

            botaoPerfilTopo.setVisibility(View.VISIBLE);

            tela.setVisibility(View.GONE);

            equipeTopo.setVisibility(View.GONE);

            botaoMenu.setVisibility(View.GONE);

            telaHomeVazia.setVisibility(View.VISIBLE);

        });

        botaoColaboradores.setOnClickListener(v -> {

            botaoPerfilTopo.setVisibility(View.GONE);

            telaHomeVazia.setVisibility(View.GONE);

            tela.setVisibility(View.VISIBLE);

            equipeTopo.setVisibility(View.VISIBLE);

            botaoMenu.setVisibility(View.VISIBLE);

            scroll.smoothScrollTo(0, 0);

        });



        botaoConexoes.setOnClickListener(v ->

        abrirAreaComManutencao(

            "conexoes",

            "CONEXÕES",

            () -> startActivity(

                new Intent(

                    MainActivity.this,

                    SocialActivity.class

                )

            )

        )

    );



    botaoPerfil.setOnClickListener(v -> {

            startActivity(

                    new Intent(

                            MainActivity.this,

                            PerfilActivity.class

                    )

            );

        });



        botaoMais.setOnClickListener(v -> abrirMenuPrincipal());



        FrameLayout.LayoutParams navParams =

                new FrameLayout.LayoutParams(

                        -1,

                        dp(76),

                        Gravity.BOTTOM

                );

        navParams.setMargins(

                dp(8),

                0,

                dp(8),

                dp(8)

        );



        raiz.addView(barraNavegacao, navParams);



        botaoPerfilTopo.bringToFront();







        setContentView(raiz);



        // Sincronizado com PUBLICAR v1.8.6

        try {

            String _u = "https://raw.githubusercontent.com/andersonptg/DaNikeAI/main/versao.json";

            new java.lang.Thread(() -> {

                try {

                    java.net.HttpURLConnection _c = (java.net.HttpURLConnection) new java.net.URL(_u).openConnection();

                    _c.connect();

                    String _j = new java.io.BufferedReader(new java.io.InputStreamReader(_c.getInputStream())).lines().collect(java.util.stream.Collectors.joining());

                    org.json.JSONObject _o = new org.json.JSONObject(_j);

                    String _vn = _o.getString("versao");

                    String _apk = _o.getString("apkUrl");

                    String _ch = _o.getString("changelog");

                    if (!BuildConfig.VERSION_NAME.equals(_vn) && _vn != null && !_vn.isEmpty()) {

                        runOnUiThread(() -> AtualizacaoTelefoneDialog.show(this, _vn, _apk, _ch));

                    }

                } catch (Exception ignored) {}

            }).start();

        } catch (Exception ignored) {}



        carregarIndicadoresManutencao(

                pontoIA,

                pontoFilmes,

                pontoHistorico,

                pontoPerfil,

                pontoHave,

                pontoConexoes

        );



        // =========================

        // CLIQUES

        // =========================



        ia.setOnClickListener(v -> {

            abrirAreaComManutencao(

                    "ia",

                    "IA",

                    () -> {

                        Intent intent =

                                new Intent(

                                        MainActivity.this,

                                        IAActivity.class

                                );

                        startActivity(intent);

                    }

            );

        });



        filmes.setOnClickListener(v -> {

            abrirAreaComManutencao(

                    "filmes",

                    "FILMES",

                    () -> {

                        Intent intent =

                                new Intent(

                                        MainActivity.this,

                                        com.danike.ai.filmes.FilmesActivity.class

                                );

                        startActivity(intent);

                    }

            );

        });



        historico.setOnClickListener(v -> {

            abrirAreaComManutencao(

                    "historico",

                    "HISTÓRICO",

                    () -> {

                        Intent intent =

                                new Intent(

                                        MainActivity.this,

                                        HistoricoActivity.class

                                );

                        startActivity(intent);

                    }

            );

        });



        perfil.setOnClickListener(v -> {

            abrirAreaComManutencao(

                    "perfil",

                    "PERFIL",

                    () -> {

                        Intent intent =

                                new Intent(

                                        MainActivity.this,

                                        PerfilActivity.class

                                );

                        startActivity(intent);

                    }

            );

        });



        rave.setOnClickListener(v -> {

            abrirAreaComManutencao(

                    "have",

                    "HAVE",

                    () -> {

                        Intent intent =

                                new Intent(

                                        MainActivity.this,

                                        HaveLoginActivity.class

                                );

                        startActivity(intent);

                    }

            );

        });



        // ADM — acesso exclusivo do proprietário

        if (botaoAdm != null) {

            botaoAdm.setOnClickListener(v -> {

                com.google.firebase.auth.FirebaseUser dono =

                        com.google.firebase.auth.FirebaseAuth

                                .getInstance()

                                .getCurrentUser();



                if (dono != null &&

                        "lipesanderson@gmail.com".equalsIgnoreCase(

                                String.valueOf(dono.getEmail())

                        )) {



                    startActivity(

                            new Intent(

                                    MainActivity.this,

                                    AdmActivity.class

                            )

                    );



                } else {

                    Toast.makeText(

                            MainActivity.this,

                            "Acesso administrativo não autorizado.",

                            Toast.LENGTH_SHORT

                    ).show();

                }

            });

        }



        // =========================

        // ANIMAÇÃO DE ENTRADA

        // =========================



        animarEntrada(ia, 100);

        animarEntrada(filmes, 220);

        animarEntrada(historico, 340);

        animarEntrada(perfil, 460);



        animarEntrada(rave, 580);



        if (botaoAdm != null) {

            animarEntrada(botaoAdm, 700);

        }

    }



    // =========================

    // MANUTENÇÃO INDIVIDUAL

    // =========================



    private boolean ehDonoDaNike() {

        try {

            com.google.firebase.auth.FirebaseUser u =

                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();



            return u != null

                    && "lipesanderson@gmail.com".equalsIgnoreCase(

                            String.valueOf(u.getEmail())

                    );

        } catch (Exception e) {

            return false;

        }

    }



    private void adicionarMembroEquipe(

            LinearLayout equipeLinha,

            String nomeTexto,

            String cargoTexto,

            String coroaTexto,

            String fotoUri,

            String instagramUsuario,

            String instagramLink

    ) {



        LinearLayout membro = new LinearLayout(this);

        membro.setOrientation(LinearLayout.VERTICAL);

        membro.setGravity(Gravity.CENTER_HORIZONTAL);

        membro.setPadding(

                dp(8),

                dp(2),

                dp(8),

                dp(4)

        );



        TextView coroa = txt(

                coroaTexto,

                24,

                Color.rgb(255, 215, 0)

        );

        coroa.setGravity(Gravity.CENTER);



        membro.addView(

                coroa,

                new LinearLayout.LayoutParams(

                        dp(100),

                        dp(30)

                )

        );



        FrameLayout fotoContainer = new FrameLayout(this);



        GradientDrawable aroFoto = new GradientDrawable();

        aroFoto.setShape(GradientDrawable.OVAL);

        aroFoto.setColor(Color.rgb(3, 12, 28));

        aroFoto.setStroke(

                dp(2),

                Color.rgb(0, 220, 255)

        );



        fotoContainer.setBackground(aroFoto);



        ImageView foto = new ImageView(this);

        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);

        foto.setClipToOutline(true);



        FrameLayout.LayoutParams fotoParams =

                new FrameLayout.LayoutParams(

                        dp(88),

                        dp(88),

                        Gravity.CENTER

                );



        if (!fotoUri.isEmpty()) {

            try {

                if (fotoUri.startsWith("content://")) {

                    foto.setImageURI(

                            android.net.Uri.parse(fotoUri)

                    );

                } else if (fotoUri.startsWith("http://")

                        || fotoUri.startsWith("https://")) {



                    new Thread(() -> {

                        try {

                            java.net.URL url = new java.net.URL(fotoUri);

                            java.net.HttpURLConnection conexao =

                                    (java.net.HttpURLConnection) url.openConnection();

                            conexao.setConnectTimeout(10000);

                            conexao.setReadTimeout(15000);

                            conexao.setDoInput(true);

                            conexao.connect();



                            java.io.InputStream entrada = conexao.getInputStream();

                            android.graphics.Bitmap bitmap =

                                    android.graphics.BitmapFactory.decodeStream(entrada);

                            entrada.close();

                            conexao.disconnect();



                            if (bitmap != null) {

                                foto.post(() -> foto.setImageBitmap(bitmap));

                            }

                        } catch (Exception ignored) {

                        }

                    }).start();



                } else {

                    android.graphics.Bitmap bitmap =

                            android.graphics.BitmapFactory

                                    .decodeFile(fotoUri);



                    if (bitmap != null) {

                        foto.setImageBitmap(bitmap);

                    }

                }

            } catch (Exception ignored) {

            }

        }



        fotoContainer.addView(

                foto,

                fotoParams

        );



        membro.addView(

                fotoContainer,

                new LinearLayout.LayoutParams(

                        dp(96),

                        dp(96)

                )

        );



        LinearLayout nomeBox = new LinearLayout(this);

        nomeBox.setOrientation(LinearLayout.HORIZONTAL);

        nomeBox.setGravity(Gravity.CENTER);



        GradientDrawable fundoNome = new GradientDrawable(

                GradientDrawable.Orientation.LEFT_RIGHT,

                new int[] {

                        Color.rgb(2, 5, 12),

                        Color.rgb(18, 5, 25),

                        Color.rgb(2, 5, 12)

                }

        );

        fundoNome.setCornerRadius(dp(24));

        fundoNome.setStroke(dp(2), Color.rgb(0, 225, 255));

        nomeBox.setBackground(fundoNome);

        nomeBox.setPadding(dp(8), dp(3), dp(8), dp(3));



        TextView nome = txt(

                nomeTexto,

                14,

                Color.WHITE

        );

        nome.setTypeface(

                Typeface.DEFAULT,

                Typeface.BOLD

        );

        nome.setGravity(Gravity.CENTER);

        nome.setSingleLine(true);

        nome.setEllipsize(android.text.TextUtils.TruncateAt.END);



        nomeBox.addView(

                nome,

                new LinearLayout.LayoutParams(

                        0,

                        -1,

                        1f

                )

        );



        TextView verificadoNome = txt(

                "✓",

                8,

                Color.WHITE

        );

        verificadoNome.setGravity(Gravity.CENTER);



        GradientDrawable seloNome = new GradientDrawable();

        seloNome.setShape(GradientDrawable.OVAL);

        seloNome.setColor(Color.rgb(45, 120, 255));

        seloNome.setStroke(dp(1), Color.WHITE);

        verificadoNome.setBackground(seloNome);



        nomeBox.addView(

                verificadoNome,

                new LinearLayout.LayoutParams(

                        dp(15),

                        dp(15)

                )

        );



        LinearLayout.LayoutParams nomeParams =

                new LinearLayout.LayoutParams(

                        dp(140),

                        dp(42)

                );

        nomeParams.setMargins(

                0,

                dp(6),

                0,

                dp(4)

        );



        membro.addView(

                nomeBox,

                nomeParams

        );



        TextView cargo = txt(

                cargoTexto,

                11,

                Color.rgb(215, 230, 255)

        );



        cargo.setGravity(Gravity.CENTER);

        cargo.setSingleLine(false);

        cargo.setMaxLines(2);



        GradientDrawable fundoCargo =

                new GradientDrawable();



        fundoCargo.setShape(

                GradientDrawable.RECTANGLE

        );



        fundoCargo.setColor(

                Color.rgb(2, 5, 12)

        );



        fundoCargo.setCornerRadius(

                dp(22)

        );



        fundoCargo.setStroke(

                dp(2),

                Color.rgb(185, 45, 255)

        );



        cargo.setBackground(fundoCargo);



        cargo.setPadding(

                dp(8),

                dp(3),

                dp(8),

                dp(3)

        );



        membro.addView(

                cargo,

                new LinearLayout.LayoutParams(

                        dp(112),

                        dp(34)

                )

        );



        equipeLinha.addView(

                membro,

                new LinearLayout.LayoutParams(

                        -2,

                        dp(260)

                )

        );

    }



    private void abrirAreaComManutencao(

            String area,

            String titulo,

            Runnable abrirNormal

    ) {

        com.google.firebase.firestore.FirebaseFirestore.getInstance()

                .collection("config")

                .document("areas")

                .get()

                .addOnSuccessListener(doc -> {



                    Boolean valor = doc.getBoolean("manutencao_" + area);

                    boolean emManutencao = valor != null && valor;



                    if (emManutencao && !ehDonoDaNike()) {

                        mostrarTelaManutencao(titulo);

                    } else {

                        abrirNormal.run();

                    }

                })

                .addOnFailureListener(e -> {

                    // Se o Firebase não responder, não bloqueia o usuário.

                    abrirNormal.run();

                });

    }



    private View criarIndicadorManutencao() {

        View ponto = new View(this);



        GradientDrawable fundo = new GradientDrawable();

        fundo.setShape(GradientDrawable.OVAL);

        fundo.setColor(Color.rgb(0, 255, 100));

        fundo.setStroke(dp(2), Color.WHITE);



        ponto.setBackground(fundo);

        ponto.setElevation(dp(12));



        android.animation.ObjectAnimator pulsar =

                android.animation.ObjectAnimator.ofFloat(

                        ponto,

                        "alpha",

                        1f, 0.35f, 1f

                );



        pulsar.setDuration(900);

        pulsar.setRepeatCount(android.animation.ValueAnimator.INFINITE);

        pulsar.setInterpolator(

                new android.view.animation.AccelerateDecelerateInterpolator()

        );

        pulsar.start();



        return ponto;

    }



    private void definirCorIndicador(View ponto, boolean manutencao) {

        if (ponto == null) return;



        GradientDrawable fundo = new GradientDrawable();

        fundo.setShape(GradientDrawable.OVAL);



        if (manutencao) {

            fundo.setColor(Color.rgb(255, 30, 55));

        } else {

            fundo.setColor(Color.rgb(0, 255, 100));

        }



        fundo.setStroke(dp(2), Color.WHITE);

        ponto.setBackground(fundo);

    }



    private void carregarIndicadoresManutencao(

            View pontoIA,

            View pontoFilmes,

            View pontoHistorico,

            View pontoPerfil,

            View pontoHave,

            View pontoConexoes

    ) {

        com.google.firebase.firestore.FirebaseFirestore.getInstance()

                .collection("config")

                .document("areas")

                .get()

                .addOnSuccessListener(doc -> {

                    definirCorIndicador(

                            pontoIA,

                            Boolean.TRUE.equals(doc.getBoolean("manutencao_ia"))

                    );



                    definirCorIndicador(

                            pontoFilmes,

                            Boolean.TRUE.equals(doc.getBoolean("manutencao_filmes"))

                    );



                    definirCorIndicador(

                            pontoHistorico,

                            Boolean.TRUE.equals(doc.getBoolean("manutencao_historico"))

                    );



                    definirCorIndicador(

                            pontoPerfil,

                            Boolean.TRUE.equals(doc.getBoolean("manutencao_perfil"))

                    );



                    definirCorIndicador(

                            pontoHave,

                            Boolean.TRUE.equals(doc.getBoolean("manutencao_have"))

                    );

                })

                .addOnFailureListener(e -> {

                    // Sem resposta do Firebase, mantém os indicadores verdes.

                });

    }



    private void mostrarTelaManutencao(String titulo) {



        final android.app.Dialog dialog =

                new android.app.Dialog(this);



        String tituloExibicao = titulo;



        if (tituloExibicao == null ||

                tituloExibicao.trim().isEmpty()) {

            tituloExibicao = "SISTEMA";

        }



        if (tituloExibicao.equalsIgnoreCase("GERAL")) {

            tituloExibicao = "SISTEMA";

        }



        final String areaFinal =

                tituloExibicao.toUpperCase(

                        java.util.Locale.ROOT

                );



        android.widget.FrameLayout fundo =

                new android.widget.FrameLayout(this);



        fundo.setBackgroundColor(

                Color.rgb(1, 3, 7)

        );



        /*

         * FUNDO HUD

         */

        android.view.View fundoHud =

                new android.view.View(this) {



            private final android.graphics.Paint paint =

                    new android.graphics.Paint(

                            android.graphics.Paint.ANTI_ALIAS_FLAG

                    );



            @Override

            protected void onDraw(

                    android.graphics.Canvas canvas

            ) {

                float w = getWidth();

                float h = getHeight();



                android.graphics.RadialGradient grad =

                        new android.graphics.RadialGradient(

                                w / 2f,

                                h * .34f,

                                Math.max(w, h) * .62f,

                                new int[]{

                                        Color.rgb(7, 22, 38),

                                        Color.rgb(2, 8, 15),

                                        Color.rgb(1, 3, 7)

                                },

                                new float[]{

                                        0f,

                                        .42f,

                                        1f

                                },

                                android.graphics.Shader.TileMode.CLAMP

                        );



                paint.setShader(grad);



                canvas.drawRect(

                        0,

                        0,

                        w,

                        h,

                        paint

                );



                paint.setShader(null);



                paint.setStyle(

                        android.graphics.Paint.Style.STROKE

                );



                paint.setStrokeWidth(1);



                paint.setColor(

                        Color.argb(22, 255, 20, 35)

                );



                int passo = dp(38);



                for (int x = 0; x < w; x += passo) {

                    canvas.drawLine(

                            x,

                            0,

                            x,

                            h,

                            paint

                    );

                }



                for (int y = 0; y < h; y += passo) {

                    canvas.drawLine(

                            0,

                            y,

                            w,

                            y,

                            paint

                    );

                }



                /*

                 * Cantos HUD

                 */

                paint.setColor(

                        Color.argb(150, 255, 0, 25)

                );



                paint.setStrokeWidth(dp(1));



                float c = dp(24);



                canvas.drawLine(

                        0, c,

                        c, c,

                        paint

                );



                canvas.drawLine(

                        c, c,

                        c, 0,

                        paint

                );



                canvas.drawLine(

                        w, c,

                        w - c, c,

                        paint

                );



                canvas.drawLine(

                        w - c, c,

                        w - c, 0,

                        paint

                );



                canvas.drawLine(

                        0, h - c,

                        c, h - c,

                        paint

                );



                canvas.drawLine(

                        c, h - c,

                        c, h,

                        paint

                );



                canvas.drawLine(

                        w, h - c,

                        w - c, h - c,

                        paint

                );



                canvas.drawLine(

                        w - c, h - c,

                        w - c, h,

                        paint

                );



                paint.setStyle(

                        android.graphics.Paint.Style.FILL

                );

            }

        };



        fundo.addView(

                fundoHud,

                new android.widget.FrameLayout.LayoutParams(

                        -1,

                        -1

                )

        );



        /*

         * CONTEÚDO

         */

        android.widget.LinearLayout conteudo =

                new android.widget.LinearLayout(this);



        conteudo.setOrientation(

                android.widget.LinearLayout.VERTICAL

        );



        conteudo.setGravity(

                Gravity.CENTER_HORIZONTAL

        );



        conteudo.setPadding(

                dp(18),

                dp(18),

                dp(18),

                dp(10)

        );



        /*

         * MARCA

         */

        android.widget.TextView marca =

                new android.widget.TextView(this);



        marca.setText(

                "DANIKEAI  //  SYSTEM CONTROL"

        );



        marca.setTextSize(9);

        marca.setLetterSpacing(.30f);

        marca.setGravity(Gravity.CENTER);

        marca.setTypeface(

                Typeface.DEFAULT,

                Typeface.BOLD

        );



        marca.setTextColor(

                Color.rgb(255, 120, 130)

        );



        conteudo.addView(

                marca,

                new android.widget.LinearLayout.LayoutParams(

                        -1,

                        dp(34)

                )

        );



        /*

         * ÁREA DO REATOR

         */

        android.widget.FrameLayout reator =

                new android.widget.FrameLayout(this);



        int tamanho = dp(305);



        reator.setClipChildren(false);



        /*

         * HALO AZUL

         */

        android.view.View halo =

                new android.view.View(this) {



            private final android.graphics.Paint p =

                    new android.graphics.Paint(

                            android.graphics.Paint.ANTI_ALIAS_FLAG

                    );



            @Override

            protected void onDraw(

                    android.graphics.Canvas canvas

            ) {

                float cx = getWidth() / 2f;

                float cy = getHeight() / 2f;



                android.graphics.RadialGradient g =

                        new android.graphics.RadialGradient(

                                cx,

                                cy,

                                getWidth() * .47f,

                                new int[]{

                                        Color.argb(

                                                80, 255, 0, 25

                                        ),

                                        Color.argb(

                                                20, 180, 0, 15

                                        ),

                                        Color.TRANSPARENT

                                },

                                null,

                                android.graphics.Shader.TileMode.CLAMP

                        );



                p.setShader(g);



                canvas.drawCircle(

                        cx,

                        cy,

                        getWidth() * .47f,

                        p

                );



                p.setShader(null);

            }

        };



        reator.addView(

                halo,

                centralizarParams(tamanho)

        );



        /*

         * ANEL AZUL EXTERNO

         */

        android.view.View azul =

                criarAnelManutencao(

                        tamanho,

                        Color.rgb(255, 0, 25),

                        1

                );



        reator.addView(

                azul,

                centralizarParams(tamanho)

        );



        /*

         * ANEL AZUL INTERNO

         */

        android.view.View azul2 =

                criarAnelManutencao(

                        dp(245),

                        Color.rgb(255, 35, 55),

                        -1

                );



        reator.addView(

                azul2,

                centralizarParams(dp(245))

        );



        /*

         * ANEL VERMELHO

         */

        android.view.View vermelho =

                criarAnelManutencao(

                        dp(190),

                        Color.rgb(255, 25, 50),

                        1

                );



        reator.addView(

                vermelho,

                centralizarParams(dp(190))

        );



        /*

         * ÓRBITA AZUL

         */

        android.view.View orbitaAzul =

                criarOrbitaManutencao(

                        dp(270),

                        Color.rgb(255, 0, 25),

                        1

                );



        reator.addView(

                orbitaAzul,

                centralizarParams(dp(270))

        );



        /*

         * ÓRBITA VERMELHA

         */

        android.view.View orbitaVermelha =

                criarOrbitaManutencao(

                        dp(255),

                        Color.rgb(255, 30, 55),

                        -1

                );



        reator.addView(

                orbitaVermelha,

                centralizarParams(dp(255))

        );



        /*

         * NÚCLEO CENTRAL VIP

         * Duas chaves cruzadas + núcleo energético vermelho

         */

        android.view.View nucleo =

                new android.view.View(this) {



            private final android.graphics.Paint p =

                    new android.graphics.Paint(

                            android.graphics.Paint.ANTI_ALIAS_FLAG

                    );



            @Override

            protected void onDraw(

                    android.graphics.Canvas canvas

            ) {

                float cx = getWidth() / 2f;

                float cy = getHeight() / 2f;



                /*

                 * NÚCLEO ENERGÉTICO

                 */

                android.graphics.RadialGradient energia =

                        new android.graphics.RadialGradient(

                                cx,

                                cy,

                                getWidth() * .50f,

                                new int[]{

                                        Color.argb(210, 255, 0, 25),

                                        Color.argb(150, 150, 0, 15),

                                        Color.rgb(18, 2, 6),

                                        Color.rgb(3, 3, 5)

                                },

                                new float[]{

                                        0f, .22f, .65f, 1f

                                },

                                android.graphics.Shader.TileMode.CLAMP

                        );



                p.setShader(energia);

                p.setStyle(android.graphics.Paint.Style.FILL);



                canvas.drawCircle(

                        cx,

                        cy,

                        getWidth() * .48f,

                        p

                );



                p.setShader(null);



                /*

                 * ANÉIS INTERNOS

                 */

                p.setStyle(android.graphics.Paint.Style.STROKE);



                p.setStrokeWidth(dp(3));

                p.setColor(Color.rgb(255, 0, 25));



                canvas.drawCircle(

                        cx,

                        cy,

                        getWidth() * .42f,

                        p

                );



                p.setStrokeWidth(dp(2));

                p.setColor(Color.argb(230, 255, 55, 70));



                canvas.drawCircle(

                        cx,

                        cy,

                        getWidth() * .34f,

                        p

                );



                p.setStrokeWidth(dp(1));



                p.setColor(

                        Color.argb(190, 255, 130, 140)

                );



                canvas.drawCircle(

                        cx,

                        cy,

                        getWidth() * .27f,

                        p

                );



                /*

                 * DUAS CHAVES CRUZADAS

                 */

                desenharChaveNeon(

                        canvas,

                        cx,

                        cy,

                        getWidth() * .18f,

                        -1

                );



                desenharChaveNeon(

                        canvas,

                        cx,

                        cy,

                        getWidth() * .18f,

                        1

                );



                /*

                 * NÚCLEO CENTRAL

                 */

                p.setStyle(android.graphics.Paint.Style.FILL);



                android.graphics.RadialGradient centro =

                        new android.graphics.RadialGradient(

                                cx,

                                cy,

                                getWidth() * .13f,

                                new int[]{

                                        Color.WHITE,

                                        Color.rgb(255, 80, 90),

                                        Color.rgb(255, 0, 20),

                                        Color.TRANSPARENT

                                },

                                null,

                                android.graphics.Shader.TileMode.CLAMP

                        );



                p.setShader(centro);



                canvas.drawCircle(

                        cx,

                        cy,

                        getWidth() * .13f,

                        p

                );



                p.setShader(null);



                /*

                 * PONTO CENTRAL

                 */

                p.setColor(Color.WHITE);



                canvas.drawCircle(

                        cx,

                        cy,

                        dp(3),

                        p

                );

            }

        };



        android.animation.ObjectAnimator pulsoNucleo =

                android.animation.ObjectAnimator.ofPropertyValuesHolder(

                        nucleo,



                        android.animation.PropertyValuesHolder.ofFloat(

                                "scaleX",

                                .94f,

                                1.03f,

                                .94f

                        ),



                        android.animation.PropertyValuesHolder.ofFloat(

                                "scaleY",

                                .94f,

                                1.03f,

                                .94f

                        ),



                        android.animation.PropertyValuesHolder.ofFloat(

                                "alpha",

                                .82f,

                                1f,

                                .82f

                        )

                );



        pulsoNucleo.setDuration(1500);



        pulsoNucleo.setRepeatCount(

                android.animation.ValueAnimator.INFINITE

        );



        pulsoNucleo.start();



        reator.addView(

                nucleo,

                centralizarParams(dp(145))

        );



        conteudo.addView(

                reator,

                new android.widget.LinearLayout.LayoutParams(

                        -1,

                        tamanho

                )

        );



        /*

         * STATUS VERMELHO

         */

        android.widget.TextView status =

                new android.widget.TextView(this);



        status.setText(

                "●  MANUTENÇÃO EM ANDAMENTO"

        );



        status.setTextSize(9);

        status.setLetterSpacing(.22f);

        status.setGravity(Gravity.CENTER);

        status.setTypeface(

                Typeface.DEFAULT,

                Typeface.BOLD

        );



        status.setTextColor(

                Color.rgb(255, 70, 85)

        );



        android.animation.ObjectAnimator pulsoStatus =

                android.animation.ObjectAnimator.ofFloat(

                        status,

                        "alpha",

                        .45f,

                        1f,

                        .45f

                );



        pulsoStatus.setDuration(1000);



        pulsoStatus.setRepeatCount(

                android.animation.ValueAnimator.INFINITE

        );



        pulsoStatus.start();



        conteudo.addView(

                status,

                new android.widget.LinearLayout.LayoutParams(

                        -1,

                        dp(32)

                )

        );



        /*

         * TÍTULO

         */

        android.widget.TextView tituloView =

                new android.widget.TextView(this);



        tituloView.setText(

                areaFinal + " EM MANUTENÇÃO"

        );



        tituloView.setTextSize(25);

        tituloView.setGravity(Gravity.CENTER);

        tituloView.setTypeface(

                Typeface.DEFAULT,

                Typeface.BOLD

        );



        tituloView.setLetterSpacing(.06f);



        tituloView.setTextColor(

                Color.rgb(245, 245, 248)

        );



        tituloView.setShadowLayer(

                dp(12),

                0,

                0,

                Color.rgb(255, 20, 45)

        );



        conteudo.addView(

                tituloView,

                new android.widget.LinearLayout.LayoutParams(

                        -1,

                        dp(58)

                )

        );



        /*

         * LINHA NEON

         */

        android.view.View linha =

                new android.view.View(this);



        GradientDrawable linhaFundo =

                new GradientDrawable(

                        GradientDrawable.Orientation.LEFT_RIGHT,

                        new int[]{

                                Color.TRANSPARENT,

                                Color.rgb(255, 0, 25),

                                Color.rgb(255, 30, 50),

                                Color.rgb(255, 0, 25),

                                Color.TRANSPARENT

                        }

                );



        linha.setBackground(linhaFundo);



        conteudo.addView(

                linha,

                new android.widget.LinearLayout.LayoutParams(

                        dp(190),

                        dp(2)

                )

        );



        /*

         * MENSAGEM

         */

        android.widget.TextView mensagem =

                new android.widget.TextView(this);



        mensagem.setText(

                "ESTA ÁREA ESTÁ TEMPORARIAMENTE\n" +

                "INDISPONÍVEL. TENTE NOVAMENTE\n" +

                "MAIS TARDE."

        );



        mensagem.setTextSize(11);

        mensagem.setGravity(Gravity.CENTER);

        mensagem.setLetterSpacing(.16f);

        mensagem.setLineSpacing(0, 1.25f);



        mensagem.setTextColor(

                Color.rgb(210, 210, 215)

        );



        conteudo.addView(

                mensagem,

                new android.widget.LinearLayout.LayoutParams(

                        -1,

                        dp(82)

                )

        );



        /*

         * BOTÃO

         */

        android.widget.Button voltar =

                new android.widget.Button(this);



        voltar.setText("VOLTAR");



        voltar.setTextSize(11);

        voltar.setLetterSpacing(.15f);

        voltar.setTextColor(Color.WHITE);



        voltar.setTypeface(

                Typeface.DEFAULT,

                Typeface.BOLD

        );



        GradientDrawable botao =

                new GradientDrawable();



        botao.setColor(

                Color.rgb(4, 10, 18)

        );



        botao.setCornerRadius(

                dp(18)

        );



        botao.setStroke(

                dp(1),

                Color.rgb(255, 0, 25)

        );



        voltar.setBackground(botao);



        voltar.setOnClickListener(

                v -> dialog.dismiss()

        );



        conteudo.addView(

                voltar,

                new android.widget.LinearLayout.LayoutParams(

                        dp(190),

                        dp(48)

                )

        );



        /*

         * RODAPÉ

         */

        android.widget.TextView rodape =

                new android.widget.TextView(this);



        rodape.setText(

                "DANIKEAI  •  SEMPRE EVOLUINDO"

        );



        rodape.setTextSize(7);

        rodape.setLetterSpacing(.22f);

        rodape.setGravity(Gravity.CENTER);



        rodape.setTextColor(

                Color.rgb(70, 120, 150)

        );



        android.widget.LinearLayout.LayoutParams rodapeParams =

                new android.widget.LinearLayout.LayoutParams(

                        -1,

                        dp(34)

                );



        rodapeParams.topMargin = dp(6);



        conteudo.addView(

                rodape,

                rodapeParams

        );



        fundo.addView(

                conteudo,

                new android.widget.FrameLayout.LayoutParams(

                        -1,

                        -2,

                        Gravity.CENTER

                )

        );



        dialog.setContentView(fundo);



        android.view.Window window =

                dialog.getWindow();



        if (window != null) {



            window.setBackgroundDrawable(

                    new android.graphics.drawable.ColorDrawable(

                            Color.TRANSPARENT

                    )

            );



            android.view.WindowManager.LayoutParams params =

                    window.getAttributes();



            params.width =

                    android.view.WindowManager.LayoutParams.MATCH_PARENT;



            params.height =

                    android.view.WindowManager.LayoutParams.MATCH_PARENT;



            params.dimAmount = .80f;



            window.setAttributes(params);

        }



        dialog.setCancelable(true);



        dialog.show();



        if (dialog.getWindow() != null) {



            android.view.WindowManager.LayoutParams params =

                    dialog.getWindow().getAttributes();



            params.width =

                    android.view.WindowManager.LayoutParams.MATCH_PARENT;



            params.height =

                    android.view.WindowManager.LayoutParams.MATCH_PARENT;



            params.dimAmount = .80f;



            dialog.getWindow().setAttributes(params);

        }

    }



    private void desenharChaveNeon(

            android.graphics.Canvas canvas,

            float cx,

            float cy,

            float r,

            int sentido

    ) {

        android.graphics.Paint p =

                new android.graphics.Paint(

                        android.graphics.Paint.ANTI_ALIAS_FLAG

                );



        p.setStyle(android.graphics.Paint.Style.STROKE);

        p.setStrokeCap(android.graphics.Paint.Cap.ROUND);

        p.setStrokeJoin(android.graphics.Paint.Join.ROUND);



        /*

         * HALO DA CHAVE

         */

        p.setStrokeWidth(dp(11));

        p.setColor(Color.argb(35, 255, 0, 25));



        desenharFormaChave(

                canvas,

                p,

                cx,

                cy,

                r,

                sentido

        );



        /*

         * CORPO NEON

         */

        p.setStrokeWidth(dp(6));

        p.setColor(Color.rgb(255, 0, 25));



        desenharFormaChave(

                canvas,

                p,

                cx,

                cy,

                r,

                sentido

        );



        /*

         * BRILHO INTERNO

         */

        p.setStrokeWidth(dp(2));

        p.setColor(Color.rgb(255, 150, 155));



        desenharFormaChave(

                canvas,

                p,

                cx,

                cy,

                r,

                sentido

        );

    }



    private void desenharFormaChave(

            android.graphics.Canvas canvas,

            android.graphics.Paint p,

            float cx,

            float cy,

            float r,

            int sentido

    ) {

        float s = sentido;



        canvas.drawLine(

                cx - r * .70f,

                cy + s * r * .70f,

                cx + r * .55f,

                cy - s * r * .55f,

                p

        );



        p.setStyle(android.graphics.Paint.Style.STROKE);



        android.graphics.RectF cabeca =

                new android.graphics.RectF(

                        cx - r * .98f,

                        cy + s * r * .48f - r * .32f,

                        cx - r * .36f,

                        cy + s * r * .48f + r * .32f

                );



        if (sentido < 0) {

            cabeca.set(

                    cx + r * .36f,

                    cy - r * .80f,

                    cx + r * .98f,

                    cy - r * .16f

            );

        }



        canvas.drawArc(

                cabeca,

                sentido < 0 ? -45 : 135,

                250,

                false,

                p

        );

    }



    private android.widget.FrameLayout.LayoutParams centralizarParams(

            int tamanho

    ) {

        return new android.widget.FrameLayout.LayoutParams(

                tamanho,

                tamanho,

                Gravity.CENTER

        );

    }



    private android.view.View criarAnelManutencao(

            int tamanho,

            int cor,

            int sentido

    ) {



        android.view.View anel =

                new android.view.View(this) {



            private final android.graphics.Paint paint =

                    new android.graphics.Paint(

                            android.graphics.Paint.ANTI_ALIAS_FLAG

                    );



            @Override

            protected void onDraw(

                    android.graphics.Canvas canvas

            ) {



                float cx = getWidth() / 2f;

                float cy = getHeight() / 2f;

                float r = Math.min(

                        getWidth(),

                        getHeight()

                ) * .46f;



                paint.setStyle(

                        android.graphics.Paint.Style.STROKE

                );



                paint.setStrokeCap(

                        android.graphics.Paint.Cap.ROUND

                );



                /*

                 * Halo

                 */

                paint.setStrokeWidth(dp(14));



                paint.setColor(

                        Color.argb(

                                35,

                                Color.red(cor),

                                Color.green(cor),

                                Color.blue(cor)

                        )

                );



                canvas.drawCircle(

                        cx,

                        cy,

                        r,

                        paint

                );



                /*

                 * Anel principal

                 */

                paint.setStrokeWidth(dp(3));



                paint.setColor(cor);



                canvas.drawCircle(

                        cx,

                        cy,

                        r,

                        paint

                );



                /*

                 * Segmentos luminosos

                 */

                paint.setStrokeWidth(dp(10));



                android.graphics.RectF oval =

                        new android.graphics.RectF(

                                cx - r,

                                cy - r,

                                cx + r,

                                cy + r

                        );



                for (int i = 0; i < 4; i++) {



                    canvas.drawArc(

                            oval,

                            i * 90f + 10f,

                            34f,

                            false,

                            paint

                    );

                }



                /*

                 * Pontos técnicos

                 */

                paint.setStyle(

                        android.graphics.Paint.Style.FILL

                );



                for (int i = 0; i < 8; i++) {



                    double a =

                            Math.toRadians(i * 45);



                    float x =

                            cx + (float)Math.cos(a) * r;



                    float y =

                            cy + (float)Math.sin(a) * r;



                    canvas.drawCircle(

                            x,

                            y,

                            dp(3),

                            paint

                    );

                }

            }

        };



        android.animation.ObjectAnimator giro =

                android.animation.ObjectAnimator.ofFloat(

                        anel,

                        "rotation",

                        0f,

                        sentido > 0 ? 360f : -360f

                );



        giro.setDuration(

                sentido > 0 ? 11500 : 8500

        );



        giro.setRepeatCount(

                android.animation.ValueAnimator.INFINITE

        );



        giro.setInterpolator(

                new android.view.animation.LinearInterpolator()

        );



        giro.start();



        return anel;

    }



    private android.view.View criarOrbitaManutencao(

            int tamanho,

            int cor,

            int sentido

    ) {



        android.view.View orbita =

                new android.view.View(this) {



            private final android.graphics.Paint paint =

                    new android.graphics.Paint(

                            android.graphics.Paint.ANTI_ALIAS_FLAG

                    );



            @Override

            protected void onDraw(

                    android.graphics.Canvas canvas

            ) {



                float cx = getWidth() / 2f;

                float cy = getHeight() / 2f;



                float rx = getWidth() * .47f;

                float ry = getHeight() * .22f;



                paint.setStyle(

                        android.graphics.Paint.Style.STROKE

                );



                paint.setStrokeWidth(dp(3));

                paint.setColor(cor);



                android.graphics.RectF oval =

                        new android.graphics.RectF(

                                cx - rx,

                                cy - ry,

                                cx + rx,

                                cy + ry

                        );



                canvas.drawOval(

                        oval,

                        paint

                );



                paint.setStyle(

                        android.graphics.Paint.Style.FILL

                );



                paint.setShadowLayer(

                        dp(8),

                        0,

                        0,

                        cor

                );



                canvas.drawCircle(

                        cx + rx,

                        cy,

                        dp(6),

                        paint

                );



                canvas.drawCircle(

                        cx - rx,

                        cy,

                        dp(4),

                        paint

                );



                paint.clearShadowLayer();

            }

        };



        android.animation.ObjectAnimator giro =

                android.animation.ObjectAnimator.ofFloat(

                        orbita,

                        "rotation",

                        0f,

                        sentido > 0 ? 360f : -360f

                );



        giro.setDuration(

                sentido > 0 ? 9000 : 12000

        );



        giro.setRepeatCount(

                android.animation.ValueAnimator.INFINITE

        );



        giro.setInterpolator(

                new android.view.animation.LinearInterpolator()

        );



        giro.start();



        return orbita;

    }



    private void abrirMenuPrincipal() {



        telaMenu = new FrameLayout(this);

        telaMenu.setBackgroundColor(Color.rgb(2, 4, 14));

        telaMenu.setElevation(dp(100));



        NeonBackgroundView fundoMenu = new NeonBackgroundView(this);

        telaMenu.addView(

                fundoMenu,

                new FrameLayout.LayoutParams(-1, -1)

        );



        TextView tituloMenu = txt("✦  APPS", 30, Color.WHITE);

        tituloMenu.setGravity(Gravity.CENTER);

        tituloMenu.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        tituloMenu.setShadowLayer(dp(14), 0, 0, Color.rgb(70, 160, 255));



        FrameLayout.LayoutParams tituloParams =

                new FrameLayout.LayoutParams(

                        -1,

                        dp(70),

                        Gravity.TOP

                );

        tituloParams.setMargins(dp(20), dp(5), dp(70), 0);



        telaMenu.addView(tituloMenu, tituloParams);



        TextView fechar = txt("✕", 26, Color.WHITE);

        fechar.setGravity(Gravity.CENTER);

        fechar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);



        GradientDrawable fundoFechar = new GradientDrawable();

        fundoFechar.setColor(Color.rgb(25, 10, 55));

        fundoFechar.setCornerRadius(dp(25));

        fundoFechar.setStroke(dp(2), Color.rgb(75, 145, 255));

        fechar.setBackground(fundoFechar);



        FrameLayout.LayoutParams fecharParams =

                new FrameLayout.LayoutParams(

                        dp(52),

                        dp(52),

                        Gravity.TOP | Gravity.END

                );

        fecharParams.setMargins(0, dp(10), dp(12), 0);



        telaMenu.addView(fechar, fecharParams);



        ScrollView scrollMenu = new ScrollView(this);

        scrollMenu.setFillViewport(true);



        LinearLayout listaMenu = new LinearLayout(this);

        listaMenu.setOrientation(LinearLayout.VERTICAL);

        listaMenu.setPadding(dp(12), dp(92), dp(12), dp(28));



        GradientDrawable fundoApps = new GradientDrawable(

                GradientDrawable.Orientation.TL_BR,

                new int[] {

                        Color.argb(238, 5, 8, 24),

                        Color.argb(235, 12, 8, 35),

                        Color.argb(238, 5, 18, 38)

                }

        );

        fundoApps.setCornerRadius(dp(30));

        fundoApps.setStroke(dp(2), Color.rgb(45, 125, 245));

        listaMenu.setBackground(fundoApps);

        listaMenu.setElevation(dp(18));



        scrollMenu.addView(

                listaMenu,

                new ScrollView.LayoutParams(-1, -2)

        );



        FrameLayout.LayoutParams scrollParams =

                new FrameLayout.LayoutParams(-1, -1);

        scrollParams.setMargins(0, 0, 0, 0);



        telaMenu.addView(scrollMenu, scrollParams);

        fechar.bringToFront();



        adicionarItemMenu(

                listaMenu,

                "🤖",

                "IA",

                "Assistente inteligente",

                () -> {

                    abrirAreaComManutencao(

                            "ia",

                            "IA",

                            () -> startActivity(

                                    new Intent(

                                            MainActivity.this,

                                            IAActivity.class

                                    )

                            )

                    );

                }

        );



        adicionarItemMenu(

                listaMenu,

                "🎬",

                "FILMES",

                "Filmes e conteúdo",

                () -> {

                    abrirAreaComManutencao(

                            "filmes",

                            "FILMES",

                            () -> startActivity(

                                    new Intent(

                                            MainActivity.this,

                                            com.danike.ai.filmes.FilmesActivity.class

                                    )

                            )

                    );

                }

        );



        adicionarItemMenu(

                listaMenu,

                "📚",

                "HISTÓRICO",

                "Suas conversas e registros",

                () -> {

                    abrirAreaComManutencao(

                            "historico",

                            "HISTÓRICO",

                            () -> startActivity(

                                    new Intent(

                                            MainActivity.this,

                                            HistoricoActivity.class

                                    )

                            )

                    );

                }

        );



        adicionarItemMenu(

                listaMenu,

                "🎮",

                "HAVE",

                "Entre no seu espaço Have",

                () -> {

                    abrirAreaComManutencao(

                            "have",

                            "HAVE",

                            () -> startActivity(

                                    new Intent(

                                            MainActivity.this,

                                            HaveLoginActivity.class

                                    )

                            )

                    );

                }

        );



        adicionarItemMenu(

                listaMenu,

                "📤",

                "COMPARTILHAR APP",

                "Convide alguém para usar o DaNikeAI",

                () -> {

                    try {

                        String versao =

                                getPackageManager()

                                        .getPackageInfo(

                                                getPackageName(),

                                                0

                                        ).versionName;



                        String link =

                                "https://github.com/andersonptg/DaNikeAI/releases/latest/download/NEXO.apk";



                        String mensagem =

                                "🚀 Baixe o DaNikeAI!\n\n"

                                + "Versão atual: " + versao + "\n\n"

                                + link;



                        Intent compartilhar =

                                new Intent(Intent.ACTION_SEND);



                        compartilhar.setType("text/plain");

                        compartilhar.putExtra(

                                Intent.EXTRA_TEXT,

                                mensagem

                        );



                        startActivity(

                                Intent.createChooser(

                                        compartilhar,

                                        "Compartilhar DaNikeAI"

                                )

                        );



                    } catch (Exception e) {

                        Toast.makeText(

                                MainActivity.this,

                                "Não foi possível compartilhar o app.",

                                Toast.LENGTH_SHORT

                        ).show();

                    }

                }

        );



        com.google.firebase.auth.FirebaseUser donoMenu =

                com.google.firebase.auth.FirebaseAuth

                        .getInstance()

                        .getCurrentUser();



        boolean ehDonoMenu =

                donoMenu != null &&

                "lipesanderson@gmail.com".equalsIgnoreCase(

                        String.valueOf(donoMenu.getEmail())

                );



        if (ehDonoMenu) {

            adicionarItemMenu(

                    listaMenu,

                    "⚡",

                    "ADM",

                    "Painel administrativo",

                    () -> {

                        com.google.firebase.auth.FirebaseUser dono =

                                com.google.firebase.auth.FirebaseAuth

                                        .getInstance()

                                        .getCurrentUser();



                        if (dono != null &&

                                "lipesanderson@gmail.com"

                                        .equalsIgnoreCase(

                                                String.valueOf(

                                                        dono.getEmail()

                                                )

                                        )) {



                            startActivity(

                                    new Intent(

                                            MainActivity.this,

                                            AdmActivity.class

                                    )

                            );



                        } else {

                            Toast.makeText(

                                    MainActivity.this,

                                    "Acesso administrativo não autorizado.",

                                    Toast.LENGTH_SHORT

                            ).show();

                        }

                    }

            );

        }



        fechar.setOnClickListener(v -> {

            telaMenu.setVisibility(View.GONE);

        });



        raiz.addView(

                telaMenu,

                new FrameLayout.LayoutParams(-1, -1)

        );



        telaMenu.setVisibility(View.VISIBLE);

        telaMenu.bringToFront();

    }



    private void adicionarItemMenu(

            LinearLayout lista,

            String icone,

            String titulo,

            String descricao,

            Runnable acao

    ) {

        LinearLayout item = new LinearLayout(this);

        item.setOrientation(LinearLayout.HORIZONTAL);

        item.setGravity(Gravity.CENTER_VERTICAL);

        item.setPadding(dp(14), dp(8), dp(10), dp(8));



        GradientDrawable fundo = new GradientDrawable(

                GradientDrawable.Orientation.LEFT_RIGHT,

                new int[] {

                        Color.rgb(10, 12, 32),

                        Color.rgb(13, 25, 58),

                        Color.rgb(8, 14, 35)

                }

        );

        fundo.setCornerRadius(dp(24));

        fundo.setStroke(dp(2), Color.rgb(45, 125, 245));

        item.setBackground(fundo);

        item.setElevation(dp(12));



        TextView iconeView = txt(icone, 29, Color.WHITE);

        iconeView.setGravity(Gravity.CENTER);



        GradientDrawable fundoIcone = new GradientDrawable(

                GradientDrawable.Orientation.TL_BR,

                new int[] {

                        Color.rgb(24, 12, 58),

                        Color.rgb(8, 45, 85)

                }

        );

        fundoIcone.setShape(GradientDrawable.RECTANGLE);

        fundoIcone.setCornerRadius(dp(18));

        fundoIcone.setStroke(dp(1), Color.rgb(70, 155, 255));

        iconeView.setBackground(fundoIcone);

        iconeView.setElevation(dp(8));



        LinearLayout.LayoutParams iconeParams =

                new LinearLayout.LayoutParams(dp(58), dp(58));

        item.addView(iconeView, iconeParams);



        LinearLayout textos = new LinearLayout(this);

        textos.setOrientation(LinearLayout.VERTICAL);

        textos.setGravity(Gravity.CENTER_VERTICAL);



        TextView tituloView = txt(titulo, 18, Color.WHITE);

        tituloView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        tituloView.setShadowLayer(dp(8), 0, 0, Color.rgb(50, 140, 255));



        TextView descricaoView = txt(descricao, 12, Color.rgb(190, 205, 225));



        textos.addView(tituloView);

        textos.addView(descricaoView);



        LinearLayout.LayoutParams textoParams =

                new LinearLayout.LayoutParams(0, -2, 1f);

        textoParams.setMargins(dp(12), 0, dp(8), 0);

        item.addView(textos, textoParams);



        TextView seta = txt("›", 32, Color.rgb(85, 175, 255));

        seta.setGravity(Gravity.CENTER);

        seta.setShadowLayer(dp(10), 0, 0, Color.rgb(50, 140, 255));



        item.addView(

                seta,

                new LinearLayout.LayoutParams(dp(34), dp(58))

        );



        LinearLayout.LayoutParams itemParams =

                new LinearLayout.LayoutParams(-1, dp(88));

        itemParams.setMargins(0, 0, 0, dp(13));



        lista.addView(item, itemParams);



        item.setOnClickListener(v -> {

            if (acao != null) {

                acao.run();

            }

        });



        item.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    v.setAlpha(0.82f);

                    v.setScaleX(0.985f);

                    v.setScaleY(0.985f);

                    break;



                case MotionEvent.ACTION_UP:

                case MotionEvent.ACTION_CANCEL:

                    v.setAlpha(1f);

                    v.setScaleX(1f);

                    v.setScaleY(1f);

                    break;

            }

            return false;

        });

    }



    @Override

    public void onBackPressed() {

        // Menu aberto: volta para a Home

        if (telaMenu != null && telaMenu.getVisibility() == View.VISIBLE) {

            telaMenu.setVisibility(View.GONE);

            return;

        }



        // Home: volta para o Login

        Intent login = new Intent(MainActivity.this, LoginActivity.class);

        login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(login);

        finish();

    }



    @Override

    protected void onResume() {

        super.onResume();







        if (pontoIA != null) {

            carregarIndicadoresManutencao(

                    pontoIA,

                    pontoFilmes,

                    pontoHistorico,

                    pontoPerfil,

                    pontoHave,

                    pontoConexoes

            );

        }



        verificarAtualizacaoEquipeHome();

        iniciarSincronizacaoEquipeCloud();

        iniciarSincronizacaoAnunciosHome();

    }



    private void iniciarSincronizacaoEquipeCloud() {

        try {

            if (equipeListenerCloud != null) {

                equipeListenerCloud.remove();

            }



            equipeListenerCloud =

                    com.google.firebase.firestore.FirebaseFirestore

                            .getInstance()

                            .collection("equipe")

                            .addSnapshotListener((snapshot, error) -> {



                                if (error != null || snapshot == null) {

                                    return;

                                }



                                try {

                                    org.json.JSONArray listaOnline =

                                            new org.json.JSONArray();



                                    java.util.ArrayList<

                                            com.google.firebase.firestore.DocumentSnapshot

                                    > documentos =

                                            new java.util.ArrayList<>(

                                                    snapshot.getDocuments()

                                            );



                                    java.util.Collections.sort(

                                            documentos,

                                            (a, b) ->

                                                    a.getString("nome") == null

                                                            ? 1

                                                            : b.getString("nome") == null

                                                            ? -1

                                                            : a.getString("nome")

                                                                    .compareToIgnoreCase(

                                                                            b.getString("nome")

                                                                    )

                                    );



                                    for (com.google.firebase.firestore.DocumentSnapshot doc

                                            : documentos) {



                                        String nome =

                                                doc.getString("nome");



                                        String cargo =

                                                doc.getString("cargo");



                                        String fotoUrl =

                                                doc.getString("fotoUrl");



                                        if (nome == null || cargo == null

                                                || fotoUrl == null

                                                || nome.trim().isEmpty()

                                                || cargo.trim().isEmpty()

                                                || fotoUrl.trim().isEmpty()) {

                                            continue;

                                        }



                                        org.json.JSONObject pessoa =

                                                new org.json.JSONObject();



                                        pessoa.put(

                                                "id",

                                                doc.getId()

                                        );

                                        pessoa.put(

                                                "nome",

                                                nome

                                        );

                                        pessoa.put(

                                                "cargo",

                                                cargo

                                        );

                                        pessoa.put(

                                                "fotoUri",

                                                fotoUrl

                                        );

                                        pessoa.put(

                                                "fotoUrl",

                                                fotoUrl

                                        );

                                        String coroa = doc.getString("coroa");

                                        if (coroa == null) coroa = "";



                                        pessoa.put(

                                                "coroa",

                                                coroa

                                        );



                                        String instagramUsuario = doc.getString("instagramUsuario");

if (instagramUsuario == null) instagramUsuario = "";



String instagramLink = doc.getString("instagramLink");

if (instagramLink == null) instagramLink = "";



if (instagramUsuario.trim().isEmpty()) {

    try {

        org.json.JSONArray localLista = new org.json.JSONArray(

            getSharedPreferences("DaNikeAI_ADM", MODE_PRIVATE)

                .getString("equipe_perfis", "[]")

        );



        for (int i = 0; i < localLista.length(); i++) {

            org.json.JSONObject localPessoa = localLista.getJSONObject(i);



            if (nome.equalsIgnoreCase(

                    localPessoa.optString("nome", "").trim())) {



                instagramUsuario =

                    localPessoa.optString("instagramUsuario", "").trim();



                instagramLink =

                    localPessoa.optString("instagramLink", "").trim();



                break;

            }

        }

    } catch (Exception ignored) {}

}



pessoa.put("instagramUsuario", instagramUsuario);

pessoa.put("instagramLink", instagramLink);



                                        listaOnline.put(pessoa);

                                    }



                                    String novoJson =

                                            listaOnline.toString();



                                    android.content.SharedPreferences prefs =

                                            getSharedPreferences(

                                                    "DaNikeAI_ADM",

                                                    MODE_PRIVATE

                                            );



                                    String antigoJson =

                                            prefs.getString(

                                                    "equipe_perfis",

                                                    "[]"

                                            );



                                    if (!novoJson.equals(antigoJson)) {



                                        prefs.edit()

                                                .putString(

                                                        "equipe_perfis",

                                                        novoJson

                                                )

                                                .apply();



                                        runOnUiThread(() -> {

                                            equipeJsonExibida =

                                                    novoJson;



                                            recreate();

                                        });

                                    }



                                } catch (Exception ignored) {

                                }

                            });



        } catch (Exception ignored) {

        }

    }





    // =========================

    // ANÚNCIOS PUBLICADOS NA HOME

    // =========================



    private void iniciarSincronizacaoAnunciosHome() {

        try {

            if (anunciosListenerHome != null) {

                anunciosListenerHome.remove();

                anunciosListenerHome = null;

            }



            anunciosListenerHome =

                    com.google.firebase.firestore.FirebaseFirestore

                            .getInstance()

                            .collection("anuncios")

                            .addSnapshotListener((snapshot, error) -> {

                                if (error != null || snapshot == null

                                        || isFinishing() || isDestroyed()) {

                                    return;

                                }



                                java.util.ArrayList<

                                        com.google.firebase.firestore.DocumentSnapshot

                                > publicados = new java.util.ArrayList<>();



                                for (com.google.firebase.firestore.DocumentSnapshot doc

                                        : snapshot.getDocuments()) {

                                    Boolean publicado = doc.getBoolean("publicado");

                                    String imagem = doc.getString("imagemUrl");



                                    if (Boolean.TRUE.equals(publicado)

                                            && imagem != null

                                            && !imagem.trim().isEmpty()) {

                                        publicados.add(doc);

                                    }

                                }



                                if (faixaAnunciosHome == null

                                        || scrollAnunciosHome == null) {

                                    return;

                                }



                                if (handlerCarrosselAnuncios == null) {

                                    handlerCarrosselAnuncios =

                                            new android.os.Handler(

                                                    android.os.Looper.getMainLooper()

                                            );

                                }



                                handlerCarrosselAnuncios.removeCallbacksAndMessages(null);



                                faixaAnunciosHome.removeAllViews();

                                indiceCarrosselAnuncios = 0;



                                if (publicados.isEmpty()) {

                                    scrollAnunciosHome.setVisibility(View.GONE);

                                    return;

                                }



                                scrollAnunciosHome.setVisibility(View.VISIBLE);



                                int larguraTela =

                                        getResources().getDisplayMetrics().widthPixels;

                                int larguraCard = Math.max(dp(240), larguraTela - dp(56));

                                int alturaImagem = Math.round(larguraCard * 9f / 16f);

                                int alturaConteudoCard = dp(200);
                                int alturaBotaoCard = dp(52);
                                int alturaTextoCard = alturaConteudoCard - alturaBotaoCard;

                                int alturaCard = alturaImagem + alturaConteudoCard;



                                for (com.google.firebase.firestore.DocumentSnapshot doc

                                        : publicados) {

                                    String tipo = doc.getString("tipo");



                                    // Compatibilidade com anúncios antigos:

                                    // se não houver tipo, tratamos como venda.

                                    if (tipo == null || tipo.trim().isEmpty()) {

                                        tipo = "venda";

                                    }



                                    String imagemUrl = doc.getString("imagemUrl");
                                    boolean anuncioVip = Boolean.TRUE.equals(doc.getBoolean("vip"));



                                    LinearLayout card = new LinearLayout(this);

                                    card.setOrientation(LinearLayout.VERTICAL);

                                    card.setGravity(Gravity.TOP);

                                    card.setBackgroundColor(Color.rgb(8, 14, 28));

                                    card.setClipToOutline(true);



                                    LinearLayout.LayoutParams cardParams =

                                            new LinearLayout.LayoutParams(

                                                    larguraCard,

                                                    alturaCard

                                            );

                                    cardParams.rightMargin = dp(10);

                                    faixaAnunciosHome.addView(card, cardParams);



                                    ImageView banner = new ImageView(this);

                                    banner.setScaleType(ImageView.ScaleType.FIT_CENTER);

                                    banner.setAdjustViewBounds(false);

                                    banner.setBackgroundColor(Color.rgb(3, 6, 15));

                                    banner.setContentDescription("Imagem do anúncio NEXO");



                                    android.widget.FrameLayout molduraImagem = new android.widget.FrameLayout(this);

                                    android.graphics.drawable.GradientDrawable fundoMolduraVip =
                                            new android.graphics.drawable.GradientDrawable();
                                    fundoMolduraVip.setColor(Color.rgb(3, 6, 15));
                                    fundoMolduraVip.setCornerRadius(dp(5));
                                    fundoMolduraVip.setStroke(dp(1), Color.rgb(185, 198, 215));
                                    molduraImagem.setBackground(fundoMolduraVip);
                                    molduraImagem.setPadding(dp(1), dp(1), dp(1), dp(1));

                                    molduraImagem.addView(
                                            banner,
                                            new android.widget.FrameLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                    );

                                    card.addView(
                                            molduraImagem,
                                            new LinearLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    alturaImagem
                                            )
                                    );

                                    if (anuncioVip) {
                                        TextView seloVip = new TextView(this);
                                        seloVip.setText("♛ VIP");
                                        seloVip.setTextColor(Color.rgb(45, 29, 0));
                                        seloVip.setTextSize(12);
                                        seloVip.setTypeface(
                                                android.graphics.Typeface.DEFAULT,
                                                android.graphics.Typeface.BOLD
                                        );
                                        seloVip.setPadding(dp(12), dp(5), dp(12), dp(5));

                                        android.graphics.drawable.GradientDrawable fundoVip =
                                                new android.graphics.drawable.GradientDrawable();
                                        fundoVip.setColor(Color.rgb(255, 204, 70));
                                        fundoVip.setCornerRadius(dp(5));
                                        fundoVip.setStroke(dp(1), Color.rgb(255, 235, 160));
                                        seloVip.setBackground(fundoVip);

                                        android.widget.FrameLayout.LayoutParams seloParams =
                                                new android.widget.FrameLayout.LayoutParams(
                                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                                        Gravity.TOP | Gravity.CENTER_HORIZONTAL
                                                );
                                        seloParams.setMargins(0, dp(2), 0, 0);
                                        molduraImagem.addView(seloVip, seloParams);
                                    }

                                    carregarImagemAnuncioHome(banner, imagemUrl);





                                    banner.setOnTouchListener((v, evento) -> {

                                        if (handlerCarrosselAnuncios != null

                                                && tarefaCarrosselAnuncios != null) {

                                            int acao = evento.getAction();

                                            if (acao == android.view.MotionEvent.ACTION_DOWN) {

                                                handlerCarrosselAnuncios.removeCallbacks(

                                                        tarefaCarrosselAnuncios

                                                );

                                            } else if (acao == android.view.MotionEvent.ACTION_UP

                                                    || acao == android.view.MotionEvent.ACTION_CANCEL) {

                                                handlerCarrosselAnuncios.removeCallbacks(

                                                        tarefaCarrosselAnuncios

                                                );

                                                handlerCarrosselAnuncios.postDelayed(

                                                        tarefaCarrosselAnuncios, 5000

                                                );

                                            }

                                        }

                                        return false;

                                    });

if ("divulgacao_nexo".equalsIgnoreCase(tipo)) {

                                        // Mantém a mesma altura dos cartões de venda.

                                        View espacoInferior = new View(this);

                                        card.addView(

                                                espacoInferior,

                                                new LinearLayout.LayoutParams(

                                                        ViewGroup.LayoutParams.MATCH_PARENT,

                                                        alturaConteudoCard

                                                )

                                        );

                                        continue;

                                    }



                                    LinearLayout conteudo = new LinearLayout(this);

                                    conteudo.setOrientation(LinearLayout.VERTICAL);

                                    conteudo.setPadding(dp(12), dp(10), dp(12), dp(12));

                                    ScrollView rolagemConteudo = new ScrollView(this);

                                    rolagemConteudo.setFillViewport(false);

                                    rolagemConteudo.setVerticalScrollBarEnabled(true);

                                    rolagemConteudo.setOverScrollMode(

                                            View.OVER_SCROLL_IF_CONTENT_SCROLLS

                                    );

                                    card.addView(

                                            rolagemConteudo,

                                            new LinearLayout.LayoutParams(

                                                    ViewGroup.LayoutParams.MATCH_PARENT,

                                                    alturaTextoCard

                                            )

                                    );

                                    rolagemConteudo.addView(

                                            conteudo,

                                            new ScrollView.LayoutParams(

                                                    ViewGroup.LayoutParams.MATCH_PARENT,

                                                    ViewGroup.LayoutParams.WRAP_CONTENT

                                            )

                                    );



                                    String tituloAnuncio = doc.getString("titulo");

                                    String descricaoAnuncio = doc.getString("descricao");

                                    String preco = doc.getString("preco");

                                    String promocional = doc.getString("precoPromocional");

                                    String whatsapp = doc.getString("whatsapp");



                                    if (tituloAnuncio != null

                                            && !tituloAnuncio.trim().isEmpty()) {

                                        TextView tituloView = new TextView(this);

                                        tituloView.setText(tituloAnuncio);

                                        tituloView.setTextColor(Color.WHITE);

                                        tituloView.setTextSize(18);

                                        tituloView.setTypeface(

                                                android.graphics.Typeface.DEFAULT,

                                                android.graphics.Typeface.BOLD

                                        );

                                        conteudo.addView(tituloView);

                                    }



                                    if (descricaoAnuncio != null

                                            && !descricaoAnuncio.trim().isEmpty()) {

                                        TextView descricaoView = new TextView(this);

                                        descricaoView.setText(descricaoAnuncio);

                                        descricaoView.setTextColor(Color.rgb(205, 215, 230));

                                        descricaoView.setTextSize(14);

                                        descricaoView.setPadding(0, dp(5), 0, dp(7));

                                        conteudo.addView(descricaoView);

                                    }



                                    boolean temPromocao = promocional != null

                                            && !promocional.trim().isEmpty()

                                            && !promocional.trim().equals("0")

                                            && !promocional.trim().equals("0.0")

                                            && !promocional.trim().equals("0.00");



                                    if (temPromocao) {

                                        TextView etiqueta = new TextView(this);

                                        etiqueta.setText("🔥 OFERTA");

                                        etiqueta.setTextColor(Color.rgb(255, 100, 100));

                                        etiqueta.setTypeface(

                                                android.graphics.Typeface.DEFAULT,

                                                android.graphics.Typeface.BOLD

                                        );

                                        conteudo.addView(etiqueta);



                                        String precoFormatado = formatarPrecoAnuncioHome(preco);

                                        if (!precoFormatado.isEmpty()) {

                                            TextView precoAntigo = new TextView(this);

                                            precoAntigo.setText(precoFormatado);

                                            precoAntigo.setTextColor(Color.rgb(160, 170, 190));

                                            precoAntigo.setTextSize(13);

                                            precoAntigo.setPaintFlags(

                                                    precoAntigo.getPaintFlags()

                                                            | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG

                                            );

                                            conteudo.addView(precoAntigo);

                                        }



                                        String promoFormatado =

                                                formatarPrecoAnuncioHome(promocional);

                                        if (!promoFormatado.isEmpty()) {

                                            TextView precoNovo = new TextView(this);

                                            precoNovo.setText(promoFormatado);

                                            precoNovo.setTextColor(Color.rgb(0, 235, 180));

                                            precoNovo.setTextSize(21);

                                            precoNovo.setTypeface(

                                                    android.graphics.Typeface.DEFAULT,

                                                    android.graphics.Typeface.BOLD

                                            );

                                            conteudo.addView(precoNovo);

                                        }

                                    } else {

                                        String precoFormatado = formatarPrecoAnuncioHome(preco);

                                        if (!precoFormatado.isEmpty()) {

                                            TextView precoView = new TextView(this);

                                            precoView.setText(precoFormatado);

                                            precoView.setTextColor(Color.rgb(0, 235, 180));

                                            precoView.setTextSize(20);

                                            precoView.setTypeface(

                                                    android.graphics.Typeface.DEFAULT,

                                                    android.graphics.Typeface.BOLD

                                            );

                                            conteudo.addView(precoView);

                                        }

                                    }



                                    String numero = whatsapp == null

                                            ? "" : whatsapp.replaceAll("[^0-9]", "");



                                    if (numero.length() >= 10) {

                                        if ((numero.length() == 10 || numero.length() == 11)

                                                && !numero.startsWith("55")) {

                                            numero = "55" + numero;

                                        }



                                        final String numeroFinal = numero;

                                        Button botaoWhatsApp = new Button(this);

                                        botaoWhatsApp.setText("CHAMAR NO WHATSAPP");

                                        botaoWhatsApp.setAllCaps(false);

                                        botaoWhatsApp.setTextColor(Color.WHITE);

                                        botaoWhatsApp.setBackgroundTintList(

                                                android.content.res.ColorStateList.valueOf(

                                                        Color.rgb(0, 145, 95)

                                                )

                                        );



                                        LinearLayout.LayoutParams botaoParams =

                                                new LinearLayout.LayoutParams(

                                                        ViewGroup.LayoutParams.MATCH_PARENT,

                                                        dp(44)

                                                );

                                        botaoParams.topMargin = dp(4);

                                        card.addView(botaoWhatsApp, botaoParams);



                                        botaoWhatsApp.setOnClickListener(v -> {

                                            try {

                                                String mensagem = android.net.Uri.encode(

                                                        "Olá! Vi seu anúncio no NEXO e gostaria de saber mais."

                                                );

                                                Intent intent = new Intent(

                                                        Intent.ACTION_VIEW,

                                                        android.net.Uri.parse(

                                                                "https://wa.me/"

                                                                        + numeroFinal

                                                                        + "?text=" + mensagem

                                                        )

                                                );

                                                startActivity(intent);

                                            } catch (Exception ignored) {

                                                Toast.makeText(

                                                        MainActivity.this,

                                                        "Não foi possível abrir o WhatsApp.",

                                                        Toast.LENGTH_SHORT

                                                ).show();

                                            }

                                        });

                                    }

                                }



                                if (publicados.size() > 1) {

                                    tarefaCarrosselAnuncios = new Runnable() {

                                        @Override

                                        public void run() {

                                            if (isFinishing() || isDestroyed()

                                                    || scrollAnunciosHome == null

                                                    || faixaAnunciosHome == null

                                                    || faixaAnunciosHome.getChildCount() < 2) {

                                                return;

                                            }



                                            indiceCarrosselAnuncios++;

                                            if (indiceCarrosselAnuncios

                                                    >= faixaAnunciosHome.getChildCount()) {

                                                indiceCarrosselAnuncios = 0;

                                            }



                                            View proximo = faixaAnunciosHome.getChildAt(

                                                    indiceCarrosselAnuncios

                                            );

                                            if (proximo != null) {

                                                scrollAnunciosHome.smoothScrollTo(

                                                        proximo.getLeft(), 0

                                                );

                                            }



                                            handlerCarrosselAnuncios.postDelayed(this, 5000);

                                        }

                                    };



                                    handlerCarrosselAnuncios.postDelayed(

                                            tarefaCarrosselAnuncios, 5000

                                    );

                                } else {

                                    scrollAnunciosHome.scrollTo(0, 0);

                                }

                            });

        } catch (Exception ignored) {

        }

    }



    private void carregarImagemAnuncioHome(

            ImageView imagem,

            String urlImagem

    ) {

        if (imagem == null || urlImagem == null

                || urlImagem.trim().isEmpty()) {

            return;

        }



        imagem.setTag(urlImagem);



        new Thread(() -> {

            java.net.HttpURLConnection conexao = null;

            java.io.InputStream entrada = null;



            try {

                java.net.URL url = new java.net.URL(urlImagem);

                conexao = (java.net.HttpURLConnection) url.openConnection();

                conexao.setConnectTimeout(10000);

                conexao.setReadTimeout(15000);

                conexao.setDoInput(true);

                conexao.connect();



                entrada = conexao.getInputStream();

                android.graphics.Bitmap bitmap =

                        android.graphics.BitmapFactory.decodeStream(entrada);



                if (bitmap != null) {

                    imagem.post(() -> {

                        if (urlImagem.equals(imagem.getTag())) {

                            imagem.setImageBitmap(bitmap);

                        }

                    });

                }

            } catch (Exception ignored) {

            } finally {

                try {

                    if (entrada != null) entrada.close();

                } catch (Exception ignored) {

                }

                if (conexao != null) conexao.disconnect();

            }

        }).start();

    }



    private String formatarPrecoAnuncioHome(String valor) {

        if (valor == null || valor.trim().isEmpty()) {

            return "";

        }



        try {

            String normalizado = valor.trim().replace("R$", "").trim();

            if (normalizado.contains(",") && normalizado.contains(".")) {

                normalizado = normalizado.replace(".", "").replace(",", ".");

            } else {

                normalizado = normalizado.replace(",", ".");

            }



            java.math.BigDecimal numero = new java.math.BigDecimal(normalizado);

            java.text.NumberFormat formato =

                    java.text.NumberFormat.getCurrencyInstance(

                            new java.util.Locale("pt", "BR")

                    );

            return formato.format(numero);

        } catch (Exception ignored) {

            return "";

        }

    }



    @Override

    protected void onDestroy() {



        try {

            if (equipeListenerCloud != null) {

                equipeListenerCloud.remove();

                equipeListenerCloud = null;

            }

        } catch (Exception ignored) {

        }



        try {

            if (anunciosListenerHome != null) {

                anunciosListenerHome.remove();

                anunciosListenerHome = null;

            }

            if (handlerCarrosselAnuncios != null) {

                handlerCarrosselAnuncios.removeCallbacksAndMessages(null);

            }

        } catch (Exception ignored) {

        }



        super.onDestroy();

    }



    private void verificarAtualizacaoEquipeHome() {

        try {

            android.content.SharedPreferences prefs =

                    getSharedPreferences("DaNikeAI_ADM", MODE_PRIVATE);



            String atual = prefs.getString("equipe_perfis", "[]");



            if (equipeJsonExibida == null || !equipeJsonExibida.equals(atual)) {

                equipeJsonExibida = atual;

                recreate();

            }

        } catch (Exception ignored) {

        }

    }



    void animarEntrada(View view, long atraso) {



        view.setAlpha(0f);

        view.setTranslationY(dp(35));

        view.setScaleX(.96f);

        view.setScaleY(.96f);



        view.animate()

                .alpha(1f)

                .translationY(0)

                .scaleX(1f)

                .scaleY(1f)

                .setStartDelay(atraso)

                .setDuration(420)

                .setInterpolator(

                        new DecelerateInterpolator()

                )

                .start();

    }

}
