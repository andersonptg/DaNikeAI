package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

public class MainActivity extends Activity {
    
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
        t.setTextColor(cor);
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

        android.content.SharedPreferences onboardingPrefs =
                getSharedPreferences("DaNikeAI_Onboarding", MODE_PRIVATE);

        boolean onboardingConcluido =
                onboardingPrefs.getBoolean("concluido", false);

        boolean apresentacaoNomeV2 =
                onboardingPrefs.getBoolean("apresentacao_nome_v2", false);

        if (!apresentacaoNomeV2) {
            startActivity(
                    new Intent(this, OnboardingActivity.class)
            );
            finish();
            return;
        }

        if (!onboardingConcluido) {
            startActivity(
                    new Intent(this, OnboardingActivity.class)
            );
            finish();
            return;
        }

        montarHome();

        AtualizacaoApp.verificar(this);
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

        FrameLayout raiz = new FrameLayout(this);

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
                        dp(245)
                )
        );

        equipeTopo.addView(
                equipeScroll,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(245)
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
                dp(2),
                Color.rgb(0, 220, 255)
        );

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
                        dp(285)
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

        TextView saudacao = txt(
                "Olá, " + nomeUsuario + " 👋",
                19,
                Color.rgb(190, 225, 245)
        );

        conteudo.addView(
                saudacao,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

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

        conteudo.addView(containerIA, iaParams);

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

        conteudo.addView(containerFilmes, filmesParams);

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

        conteudo.addView(containerHistorico, historicoParams);

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

        conteudo.addView(containerPerfil, perfilParams);

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

            conteudo.addView(adm, admParams);
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

        conteudo.addView(containerHave, raveParams);

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
        conteudo.addView(compartilharApp, compartilharParams);

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
        raiz.addView(tela, telaParams);

        setContentView(raiz);
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
        nome.setSingleLine(false);
        nome.setMaxLines(2);

        GradientDrawable fundoNome =
                new GradientDrawable();

        fundoNome.setShape(
                GradientDrawable.RECTANGLE
        );

        fundoNome.setColor(
                Color.rgb(2, 5, 12)
        );

        fundoNome.setCornerRadius(
                dp(24)
        );

        fundoNome.setStroke(
                dp(2),
                Color.rgb(0, 225, 255)
        );

        nome.setBackground(fundoNome);

        nome.setPadding(
                dp(8),
                dp(3),
                dp(8),
                dp(3)
        );

        LinearLayout.LayoutParams nomeParams =
                new LinearLayout.LayoutParams(
                        dp(112),
                        dp(38)
                );

        nomeParams.setMargins(
                0,
                dp(6),
                0,
                dp(4)
        );

        membro.addView(
                nome,
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

        // Instagram publicado pelo ADM
        if (instagramUsuario != null &&
            !instagramUsuario.trim().isEmpty()) {

            TextView instagram = txt(
                    "◎  @" + instagramUsuario + "  ✓",
                    10,
                    Color.WHITE
            );

            instagram.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            instagram.setSingleLine(true);
            instagram.setGravity(Gravity.CENTER);

            GradientDrawable fundoInstagram =
                    new GradientDrawable();

            fundoInstagram.setShape(
                    GradientDrawable.RECTANGLE
            );

            fundoInstagram.setColor(
                    Color.rgb(18, 2, 8)
            );

            fundoInstagram.setCornerRadius(dp(20));

            fundoInstagram.setStroke(
                    dp(2),
                    Color.rgb(255, 35, 85)
            );

            instagram.setBackground(fundoInstagram);

            instagram.setPadding(
                    dp(6),
                    dp(2),
                    dp(6),
                    dp(2)
            );

            instagram.setOnClickListener(v -> {
                try {
                    String link = instagramLink;

                    if (link == null ||
                        link.trim().isEmpty()) {
                        link = "https://instagram.com/"
                                + instagramUsuario;
                    }

                    if (!link.startsWith("http://") &&
                        !link.startsWith("https://")) {
                        link = "https://" + link;
                    }

                    startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse(link)
                    ));

                } catch (Exception e) {
                    Toast.makeText(
                            this,
                            "Não foi possível abrir o Instagram.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });

            membro.addView(
                    instagram,
                    new LinearLayout.LayoutParams(
                            dp(116),
                            dp(34)
                    )
            );
        }

        equipeLinha.addView(
                membro,
                new LinearLayout.LayoutParams(
                        dp(120),
                        dp(214)
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

        dialog.setContentView(fundo);

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

    @Override
    protected void onResume() {
        super.onResume();

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
