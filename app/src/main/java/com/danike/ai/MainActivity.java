package com.danike.ai;

import java.util.Random;
import java.util.ArrayList;
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




    private String ultimaEquipeHome = "";
    private View pontoIA;
    private View pontoFilmes;
    private View pontoHistorico;
    private View pontoPerfil;
    private View pontoHave;



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

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        prefs = getSharedPreferences("DaNikeAI_Dados", 0);
        escuro = prefs.getBoolean("modo_escuro", true);

        aplicarTemaSistema();

        montarHome();

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

        raiz.addView(botaoMenu, menuParams);

        botaoMenu.setOnClickListener(v -> abrirMenuPrincipal());

        raiz.addView(
                fundo,
                new FrameLayout.LayoutParams(-1, -1)
        );


        // =========================
        // EQUIPE DE DESENVOLVIMENTO & GESTÃO
        // FIXA NO TOPO / ROLAGEM SOMENTE HORIZONTAL
        // =========================

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

                LinearLayout.LayoutParams logoParams =
                        new LinearLayout.LayoutParams(
                                dp(38),
                                dp(38)
                        );
                logoParams.setMargins(
                        dp(2),
                        0,
                        dp(2),
                        dp(5)
                );

                perfilBox.addView(
                        logoInstagram,
                        logoParams
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

        Button adm = null;

        if (ehDono) {

            adm = card(
                    "ADM",
                    "Painel administrativo",
                    "⚡"
            );

            LinearLayout.LayoutParams admParams =
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(135)
                    );

            admParams.setMargins(0, dp(5), 0, dp(10));
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

                String link = "https://github.com/andersonptg/DaNikeAI/releases/latest/download/DaNikeAI.apk";
                        

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

        TextView botaoConexoes =
                txt("👥\nConexões", 12, Color.WHITE);
        botaoConexoes.setGravity(Gravity.CENTER);
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

        TextView[] botoesNav = {
                botaoHome,
                botaoConexoes,
                botaoPerfil,
                botaoMais
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
        botaoConexoes.setElevation(dp(10));

        GradientDrawable fundoPerfil = new GradientDrawable();
        fundoPerfil.setColor(Color.rgb(20, 35, 70));
        fundoPerfil.setCornerRadius(dp(18));
        fundoPerfil.setStroke(dp(1), Color.rgb(70, 145, 255));
        botaoPerfil.setBackground(fundoPerfil);
        botaoPerfil.setElevation(dp(10));

        botaoHome.setOnClickListener(v -> {
            scroll.smoothScrollTo(0, 0);
        });

        botaoConexoes.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            MainActivity.this,
                            SocialActivity.class
                    )
            );
        });

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

        raiz.addView(
                barraNavegacao,
                navParams
        );



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
                    if (!BuildConfig.VERSION_NAME.equals(_vn)) {
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
                pontoHave
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

        if (adm != null) {

            adm.setOnClickListener(v -> {

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

        if (adm != null) {
            animarEntrada(adm, 580);
            animarEntrada(rave, 700);
        } else {
            animarEntrada(rave, 580);
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
            View pontoHave
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
        final android.app.Dialog dialog = new android.app.Dialog(this);

        String tituloExibicao = titulo;
        if (tituloExibicao == null || tituloExibicao.trim().isEmpty()) {
            tituloExibicao = "SISTEMA";
        }
        if (tituloExibicao.equalsIgnoreCase("GERAL")) {
            tituloExibicao = "SISTEMA";
        }

        android.widget.FrameLayout fundo = new android.widget.FrameLayout(this);

        GradientDrawable fundoNeon = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(1, 5, 15),
                        Color.rgb(3, 8, 20),
                        Color.rgb(7, 2, 22),
                        Color.rgb(1, 5, 15)
                }
        );
        fundoNeon.setStroke(dp(2), Color.rgb(0, 170, 255));
        fundo.setBackground(fundoNeon);

        // Fagulhas e partículas animadas.
        android.view.View particulas = new android.view.View(this) {

            final java.util.Random random = new java.util.Random();
            final java.util.ArrayList<float[]> faiscas =
                    new java.util.ArrayList<>();

            {
                for (int i = 0; i < 85; i++) {
                    faiscas.add(new float[]{
                            random.nextFloat(),
                            random.nextFloat(),
                            1.0f + random.nextFloat() * 3.0f,
                            -0.0015f - random.nextFloat() * 0.0045f,
                            random.nextBoolean() ? 1f : 0f
                    });
                }
            }

            @Override
            protected void onDraw(android.graphics.Canvas canvas) {
                super.onDraw(canvas);

                android.graphics.Paint paint =
                        new android.graphics.Paint(
                                android.graphics.Paint.ANTI_ALIAS_FLAG
                        );

                float largura = getWidth();
                float altura = getHeight();

                for (float[] f : faiscas) {

                    f[1] += f[3];

                    if (f[1] < -0.05f) {
                        f[0] = random.nextFloat();
                        f[1] = 1.05f;
                    }

                    float x = f[0] * largura;
                    float y = f[1] * altura;
                    float tamanho = f[2];

                    if (f[4] == 1f) {
                        paint.setColor(Color.rgb(255, 145, 25));
                    } else {
                        paint.setColor(Color.rgb(0, 185, 255));
                    }

                    paint.setStrokeWidth(tamanho);
                    paint.setStrokeCap(
                            android.graphics.Paint.Cap.ROUND
                    );

                    canvas.drawLine(
                            x,
                            y,
                            x - tamanho * 2.5f,
                            y + tamanho * 5f,
                            paint
                    );
                }

                postInvalidateDelayed(35);
            }
        };

        fundo.addView(
                particulas,
                new android.widget.FrameLayout.LayoutParams(-1, -1)
        );

        android.widget.LinearLayout conteudo =
                new android.widget.LinearLayout(this);

        conteudo.setOrientation(
                android.widget.LinearLayout.VERTICAL
        );
        conteudo.setGravity(Gravity.CENTER_HORIZONTAL);
        conteudo.setPadding(
                dp(18),
                dp(20),
                dp(18),
                dp(12)
        );

        // 🔧 Chaves girando.
        android.widget.TextView ferramentas =
                new android.widget.TextView(this);

        ferramentas.setText("🔧  🔧");
        ferramentas.setTextSize(54);
        ferramentas.setGravity(Gravity.CENTER);
        ferramentas.setShadowLayer(
                dp(12),
                0,
                0,
                Color.rgb(0, 190, 255)
        );

        // Título da área.
        android.widget.TextView tituloView =
                new android.widget.TextView(this);

        tituloView.setText(
                tituloExibicao.toUpperCase(
                        java.util.Locale.ROOT
                ) + " EM MANUTENÇÃO"
        );

        tituloView.setTextSize(25);
        tituloView.setGravity(Gravity.CENTER);
        tituloView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        tituloView.setTextColor(Color.WHITE);
        tituloView.setShadowLayer(
                dp(12),
                0,
                0,
                Color.rgb(0, 175, 255)
        );
        tituloView.setPadding(
                0,
                dp(8),
                0,
                dp(10)
        );

        // Mensagem profissional.
        android.widget.TextView mensagem =
                new android.widget.TextView(this);

        mensagem.setText(
                "Estamos realizando melhorias e ajustes nesta área.\n\n" +
                "Nossa equipe está trabalhando para oferecer " +
                "uma experiência mais estável, segura e completa.\n\n" +
                "Esta função estará disponível novamente em breve."
        );

        mensagem.setTextSize(15);
        mensagem.setGravity(Gravity.CENTER);
        mensagem.setTextColor(
                Color.rgb(190, 215, 235)
        );
        mensagem.setLineSpacing(0, 1.12f);
        mensagem.setPadding(
                dp(8),
                dp(5),
                dp(8),
                dp(18)
        );

        // Botão VOLTAR.
        android.widget.Button voltar =
                new android.widget.Button(this);

        voltar.setText("VOLTAR");
        voltar.setTextColor(Color.WHITE);
        voltar.setTextSize(14);
        voltar.setAllCaps(false);
        voltar.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        GradientDrawable botao =
                new GradientDrawable();

        botao.setColor(
                Color.rgb(4, 14, 32)
        );
        botao.setCornerRadius(dp(18));
        botao.setStroke(
                dp(2),
                Color.rgb(0, 200, 255)
        );

        voltar.setBackground(botao);
        voltar.setElevation(dp(8));

        voltar.setOnClickListener(
                v -> dialog.dismiss()
        );

        // Rodapé com nome + selo azul.
        android.widget.LinearLayout assinatura =
                new android.widget.LinearLayout(this);

        assinatura.setOrientation(
                android.widget.LinearLayout.HORIZONTAL
        );
        assinatura.setGravity(Gravity.CENTER);

        android.widget.TextView nome =
                new android.widget.TextView(this);

        nome.setText(
                "Anderson Lopes dos Santos Francisco"
        );
        nome.setTextSize(12);
        nome.setTextColor(
                Color.rgb(205, 220, 235)
        );
        nome.setGravity(Gravity.CENTER_VERTICAL);

        android.widget.TextView selo =
                new android.widget.TextView(this);

        selo.setText("✓");
        selo.setTextSize(9);
        selo.setTextColor(Color.WHITE);
        selo.setGravity(Gravity.CENTER);

        GradientDrawable seloFundo =
                new GradientDrawable();

        seloFundo.setShape(
                GradientDrawable.OVAL
        );
        seloFundo.setColor(
                Color.rgb(25, 145, 245)
        );

        selo.setBackground(seloFundo);
        selo.setElevation(dp(3));

        android.widget.LinearLayout.LayoutParams seloParams =
                new android.widget.LinearLayout.LayoutParams(
                        dp(18),
                        dp(18)
                );

        seloParams.leftMargin = dp(6);

        assinatura.addView(nome);
        assinatura.addView(selo, seloParams);

        // Montagem.
        conteudo.addView(
                ferramentas,
                new android.widget.LinearLayout.LayoutParams(
                        -1,
                        dp(105)
                )
        );

        conteudo.addView(
                tituloView,
                new android.widget.LinearLayout.LayoutParams(
                        -1,
                        dp(78)
                )
        );

        conteudo.addView(
                mensagem,
                new android.widget.LinearLayout.LayoutParams(
                        -1,
                        dp(175)
                )
        );

        conteudo.addView(
                voltar,
                new android.widget.LinearLayout.LayoutParams(
                        dp(230),
                        dp(58)
                )
        );

        android.widget.LinearLayout.LayoutParams assinaturaParams =
                new android.widget.LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                );

        assinaturaParams.topMargin = dp(12);

        conteudo.addView(
                assinatura,
                assinaturaParams
        );

        fundo.addView(
                conteudo,
                new android.widget.FrameLayout.LayoutParams(
                        -1,
                        -2,
                        Gravity.CENTER
                )
        );

        // Giro contínuo das chaves.
        android.view.animation.RotateAnimation giro =
                new android.view.animation.RotateAnimation(
                        0,
                        360,
                        android.view.animation.Animation.RELATIVE_TO_SELF,
                        .5f,
                        android.view.animation.Animation.RELATIVE_TO_SELF,
                        .5f
                );

        giro.setDuration(1800);
        giro.setRepeatCount(
                android.view.animation.Animation.INFINITE
        );
        giro.setInterpolator(
                new android.view.animation.LinearInterpolator()
        );

        ferramentas.startAnimation(giro);

        dialog.

        setContentView(fundo);

        android.view.Window janela = dialog.getWindow();

        if (janela != null) {
            janela.setBackgroundDrawableResource(
                    android.R.color.transparent
            );
            janela.setDimAmount(0.35f);
            janela.addFlags(
                    android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
            );
        }

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
            );

            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }
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
                                "https://github.com/andersonptg/DaNikeAI/releases/latest/download/DaNikeAI.apk";

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

        UsuariosTracker.entrouEmCena();

        if (pontoIA != null) {
            carregarIndicadoresManutencao(
                    pontoIA,
                    pontoFilmes,
                    pontoHistorico,
                    pontoPerfil,
                    pontoHave
            );
        }

        verificarAtualizacaoEquipeHome();
        iniciarSincronizacaoEquipeCloud();
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

    @Override
    protected void onDestroy() {
        UsuariosTracker.saiuDeCena();
        try {
            if (equipeListenerCloud != null) {
                equipeListenerCloud.remove();
                equipeListenerCloud = null;
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
