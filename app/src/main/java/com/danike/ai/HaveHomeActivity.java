package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.FrameLayout;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

public class HaveHomeActivity extends Activity {
    private LinearLayout conteudoPerfilHave;


    private LinearLayout heroHave;
    private ImageView imagemHeroHave;
    private TextView tituloHeroHave;
    private TextView provedorHeroHave;
    private TextView descricaoHeroHave;
    private JSONArray catalogoHeroHave;
    private int indiceHeroHave = 0;
    private final Handler handlerHeroHave =
            new Handler(Looper.getMainLooper());



    int roxo = Color.rgb(145, 40, 255);
    int azul = Color.rgb(35, 100, 255);
    int ciano = Color.rgb(0, 220, 255);

    int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    TextView texto(String s, float tamanho, int cor) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(tamanho);
        t.setTextColor(cor);
        return t;
    }

    GradientDrawable fundo(int cor, float raio) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(cor);
        g.setCornerRadius(dp(raio));
        return g;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        montarHome();
    }

    void montarHome() {

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(Color.rgb(3, 4, 15));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(dp(20), dp(22), dp(20), dp(120));

        criarTopo(conteudo);
        criarHero(conteudo);
        criarApps(conteudo);
        criarSalas(conteudo);
        criarAmigos(conteudo);

        scroll.addView(conteudo);

        raiz.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1
        ));

        criarNavegacao(raiz);

        setContentView(raiz);
    }

    void criarTopo(LinearLayout pai) {

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        TextView logo = texto("▶ HAVE", 31, Color.WHITE);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
        logo.setShadowLayer(dp(15), 0, 0, roxo);

        TextView subtitulo = texto(
                "  WATCH TOGETHER",
                11,
                Color.rgb(170, 180, 215)
        );

        LinearLayout marca = new LinearLayout(this);
        marca.setOrientation(LinearLayout.VERTICAL);
        marca.addView(logo);
        marca.addView(subtitulo);

        topo.addView(marca, new LinearLayout.LayoutParams(
                0, -2, 1
        ));

        TextView busca = botaoTopo("⌕");

        LinearLayout perfilTopo = new LinearLayout(this);
        perfilTopo.setOrientation(LinearLayout.VERTICAL);
        perfilTopo.setGravity(Gravity.CENTER);
        perfilTopo.setPadding(dp(2), dp(2), dp(2), 0);

        TextView avatar = texto("●", 25, Color.rgb(190, 100, 255));
        avatar.setGravity(Gravity.CENTER);

        GradientDrawable fundoAvatar = fundo(
                Color.argb(170, 12, 13, 35),
                50
        );
        fundoAvatar.setStroke(
                dp(2),
                Color.rgb(145, 45, 255)
        );
        avatar.setBackground(fundoAvatar);

        LinearLayout.LayoutParams avatarParams =
                new LinearLayout.LayoutParams(dp(50), dp(50));
        perfilTopo.addView(avatar, avatarParams);

        TextView nomeTopo = texto(
                "Perfil",
                10,
                Color.rgb(210, 200, 240)
        );
        nomeTopo.setGravity(Gravity.CENTER);
        nomeTopo.setMaxLines(1);
        perfilTopo.addView(nomeTopo, new LinearLayout.LayoutParams(
                dp(62), dp(18)
        ));

        busca.setOnClickListener(v -> abrirBuscaHave());

        perfilTopo.setOnClickListener(v -> abrirPerfilHave());

        FirebaseUser usuarioTopo =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioTopo != null) {
            FirebaseFirestore.getInstance()
                    .collection("have_usuarios")
                    .document(usuarioTopo.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nome = doc.getString("nome");

                            if (nome != null && !nome.trim().isEmpty()) {
                                nomeTopo.setText(nome.trim());
                                String inicial = nome.trim()
                                        .substring(0, 1)
                                        .toUpperCase();
                                avatar.setText(inicial);
                            }
                        }
                    });
        }

        topo.addView(busca);

        LinearLayout.LayoutParams perfilParams =
                new LinearLayout.LayoutParams(dp(68), dp(76));
        perfilParams.setMargins(dp(4), 0, 0, 0);
        topo.addView(perfilTopo, perfilParams);

        pai.addView(topo);
    }

    TextView botaoTopo(String s) {

        TextView t = texto(s, 25, Color.WHITE);
        t.setGravity(Gravity.CENTER);

        GradientDrawable g = fundo(
                Color.argb(150, 12, 13, 35),
                50
        );

        g.setStroke(
                dp(1),
                Color.argb(110, 130, 50, 255)
        );

        t.setBackground(g);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        dp(50), dp(50)
                );

        p.setMargins(dp(5), 0, 0, 0);

        t.setLayoutParams(p);

        return t;
    }

    private void carregarCatalogoHave() {
        new Thread(() -> {
            try {
                URL url = new URL(
                        "https://danikeai.onrender.com/have/catalog"
                );

                HttpURLConnection conexao =
                        (HttpURLConnection) url.openConnection();

                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(15000);
                conexao.setReadTimeout(20000);

                InputStream entrada =
                        conexao.getInputStream();

                java.io.ByteArrayOutputStream buffer =
                        new java.io.ByteArrayOutputStream();

                byte[] dados = new byte[4096];
                int quantidade;

                while ((quantidade =
                        entrada.read(dados)) != -1) {
                    buffer.write(dados, 0, quantidade);
                }

                entrada.close();
                conexao.disconnect();

                JSONObject resposta =
                        new JSONObject(
                                buffer.toString("UTF-8")
                        );

                JSONArray filmes =
                        resposta.optJSONArray("results");

                if (filmes == null ||
                        filmes.length() == 0) {
                    return;
                }

                catalogoHeroHave = filmes;
                indiceHeroHave = 0;

                runOnUiThread(() -> {
                    try {
                        atualizarHeroHave(
                                catalogoHeroHave.getJSONObject(0)
                        );
                        iniciarCarrosselHeroHave();
                    } catch (Exception ignored) {
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Catálogo temporariamente indisponível",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        }).start();
    }


    void criarHero(LinearLayout pai) {

        heroHave = new LinearLayout(this);
        heroHave.setOrientation(LinearLayout.VERTICAL);
        heroHave.setPadding(dp(0), dp(0), dp(0), dp(0));
        heroHave.setClipToOutline(true);

        GradientDrawable fundoHero = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(32, 7, 58),
                        Color.rgb(7, 13, 35),
                        Color.rgb(3, 25, 45)
                }
        );
        fundoHero.setCornerRadius(dp(28));
        fundoHero.setStroke(dp(1), Color.rgb(105, 45, 220));
        heroHave.setBackground(fundoHero);
        heroHave.setElevation(dp(8));

        FrameLayout imagemArea = new FrameLayout(this);

        imagemHeroHave = new ImageView(this);
        imagemHeroHave.setScaleType(ImageView.ScaleType.CENTER_CROP);

        imagemArea.addView(
                imagemHeroHave,
                new FrameLayout.LayoutParams(-1, dp(245))
        );

        GradientDrawable degradado = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        Color.TRANSPARENT,
                        Color.argb(225, 5, 8, 20)
                }
        );

        View camada = new View(this);
        camada.setBackground(degradado);

        imagemArea.addView(
                camada,
                new FrameLayout.LayoutParams(-1, dp(245))
        );

        provedorHeroHave = texto(
                "HAVE",
                12,
                Color.WHITE
        );
        provedorHeroHave.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        provedorHeroHave.setGravity(Gravity.CENTER);
        provedorHeroHave.setPadding(dp(12), dp(5), dp(12), dp(5));

        GradientDrawable fundoProvedor = new GradientDrawable();
        fundoProvedor.setColor(Color.argb(210, 0, 0, 0));
        fundoProvedor.setCornerRadius(dp(20));
        provedorHeroHave.setBackground(fundoProvedor);

        FrameLayout.LayoutParams provedorParams =
                new FrameLayout.LayoutParams(
                        -2,
                        dp(34),
                        Gravity.TOP | Gravity.START
                );
        provedorParams.setMargins(dp(15), dp(15), 0, 0);

        imagemArea.addView(provedorHeroHave, provedorParams);

        heroHave.addView(
                imagemArea,
                new LinearLayout.LayoutParams(-1, dp(245))
        );

        LinearLayout informacoes = new LinearLayout(this);
        informacoes.setOrientation(LinearLayout.VERTICAL);
        informacoes.setPadding(
                dp(20),
                dp(14),
                dp(20),
                dp(20)
        );

        TextView etiqueta = texto(
                "EM DESTAQUE",
                11,
                Color.rgb(190, 105, 255)
        );
        etiqueta.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        tituloHeroHave = texto(
                "Filmes, séries e muita resenha.",
                25,
                Color.WHITE
        );
        tituloHeroHave.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tituloHeroHave.setPadding(0, dp(5), 0, dp(6));

        descricaoHeroHave = texto(
                "Descobrindo novidades para assistir juntos.",
                14,
                Color.rgb(190, 205, 235)
        );

        TextView entrar = texto(
                "▶   ENTRAR EM UMA SALA",
                14,
                Color.WHITE
        );
        entrar.setGravity(Gravity.CENTER);
        entrar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        GradientDrawable botao = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.rgb(130, 0, 255),
                        Color.rgb(20, 95, 255)
                }
        );
        botao.setCornerRadius(dp(40));
        botao.setStroke(dp(1), Color.rgb(180, 90, 255));
        entrar.setBackground(botao);

        entrar.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Salas em breve",
                        Toast.LENGTH_SHORT
                ).show()
        );

        informacoes.addView(etiqueta);
        informacoes.addView(tituloHeroHave);
        informacoes.addView(descricaoHeroHave);
        informacoes.addView(entrar,
                new LinearLayout.LayoutParams(
                        dp(230),
                        dp(52)
                )
        );

        heroHave.addView(informacoes);

        LinearLayout.LayoutParams hp =
                new LinearLayout.LayoutParams(-1, -2);

        hp.setMargins(0, dp(20), 0, dp(15));

        pai.addView(heroHave, hp);

        carregarCatalogoHave();
    }

    private void atualizarHeroHave(JSONObject filme) {

        try {

            String titulo = filme.optString(
                    "title",
                    filme.optString(
                            "name",
                            "Filme em destaque"
                    )
            );

            String descricao = filme.optString(
                    "overview",
                    "Descubra este título e assista com seus amigos."
            );

            String provedor = filme.optString(
                    "have_provider",
                    "Streaming"
            );

            String imagem = filme.optString(
                    "backdrop_path",
                    ""
            );

            if (imagem == null || imagem.trim().isEmpty()) {

                imagem = filme.optString(
                        "poster_path",
                        ""
                );
            }

            tituloHeroHave.setText(titulo);

            descricaoHeroHave.setText(
                    descricao == null ||
                    descricao.trim().isEmpty()
                            ? "Descubra este título e assista com seus amigos."
                            : descricao
            );

            provedorHeroHave.setText(provedor);

            if (imagem != null &&
                    !imagem.trim().isEmpty()) {

                String endereco;

                if (imagem.startsWith("http://") ||
                        imagem.startsWith("https://")) {

                    endereco = imagem;

                } else {

                    endereco =
                            "https://image.tmdb.org/t/p/w1280"
                            + imagem;
                }

                carregarImagemHero(endereco);
            }

        } catch (Exception e) {

            android.util.Log.e(
                    "HAVE_HERO",
                    "Erro ao atualizar hero",
                    e
            );
        }
    }

    private void carregarImagemHero(
            String endereco
    ) {

        new Thread(() -> {

            HttpURLConnection conexao = null;
            InputStream entrada = null;

            try {

                URL url = new URL(endereco);

                conexao =
                        (HttpURLConnection)
                                url.openConnection();

                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(15000);
                conexao.setReadTimeout(20000);
                conexao.setInstanceFollowRedirects(true);

                int codigo =
                        conexao.getResponseCode();

                if (codigo < 200 || codigo >= 300) {

                    throw new Exception(
                            "Imagem HTTP " + codigo
                    );
                }

                entrada =
                        conexao.getInputStream();

                Bitmap bitmap =
                        BitmapFactory.decodeStream(
                                entrada
                        );

                if (bitmap == null) {

                    throw new Exception(
                            "Bitmap nulo"
                    );
                }

                runOnUiThread(() -> {

                    imagemHeroHave
                            .setImageBitmap(bitmap);

                    imagemHeroHave
                            .setAlpha(0f);

                    imagemHeroHave
                            .animate()
                            .alpha(1f)
                            .setDuration(450)
                            .start();
                });

            } catch (Exception e) {

                android.util.Log.e(
                        "HAVE_HERO",
                        "Falha ao carregar imagem: "
                                + endereco,
                        e
                );

            } finally {

                try {

                    if (entrada != null) {
                        entrada.close();
                    }

                } catch (Exception ignored) {}

                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        }).start();
    }

    private void iniciarCarrosselHeroHave() {
        handlerHeroHave.removeCallbacksAndMessages(null);

        handlerHeroHave.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {

                        if (catalogoHeroHave != null &&
                                catalogoHeroHave.length() > 0) {

                            indiceHeroHave++;

                            if (indiceHeroHave >=
                                    catalogoHeroHave.length()) {
                                indiceHeroHave = 0;
                            }

                            try {
                                atualizarHeroHave(
                                        catalogoHeroHave.getJSONObject(
                                                indiceHeroHave
                                        )
                                );
                            } catch (Exception ignored) {
                            }
                        }

                        handlerHeroHave.postDelayed(
                                this,
                                6500
                        );
                    }
                },
                6500
        );
    }


    void criarApps(LinearLayout pai) {

        TextView titulo = texto(
                "▦  Seus Apps",
                21,
                Color.WHITE
        );

        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        pai.addView(titulo);

        TextView subtitulo = texto(
                "Seus serviços de streaming, em um só lugar.",
                14,
                Color.rgb(155, 170, 205)
        );

        subtitulo.setPadding(0, dp(3), 0, dp(12));
        pai.addView(subtitulo);

        HorizontalScrollView scroll =
                new HorizontalScrollView(this);

        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout apps = new LinearLayout(this);
        apps.setOrientation(LinearLayout.HORIZONTAL);

        adicionarApp(
                apps,
                "Netflix",
                Color.rgb(230, 0, 25),
                "com.netflix.mediaclient",
                "https://www.netflix.com",
                "LOCAL_NETFLIX"
        );

        adicionarApp(
                apps,
                "YouTube",
                Color.RED,
                "com.google.android.youtube",
                "https://www.youtube.com",
                "LOCAL_YOUTUBE"
        );

        adicionarApp(
                apps,
                "Prime Video",
                Color.rgb(30, 140, 240),
                "com.amazon.avod.thirdpartyclient",
                "https://www.primevideo.com",
                "LOCAL_PRIMEVIDEO"
        );

        adicionarApp(
                apps,
                "Disney+",
                Color.rgb(45, 85, 220),
                "com.disney.disneyplus",
                "https://www.disneyplus.com",
                "LOCAL_DISNEYPLUS"
        );

        adicionarApp(
                apps,
                "Max",
                Color.rgb(70, 55, 255),
                "com.hbo.hbonow",
                "https://www.max.com",
                "LOCAL_HBOMAX"
        );

        adicionarApp(
                apps,
                "Globoplay",
                Color.rgb(255, 80, 30),
                "com.globo.globoplay",
                "https://globoplay.globo.com",
                "LOCAL_GLOBOPLAY"
        );

        scroll.addView(apps);

        pai.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(160)
                )
        );
    }

    void adicionarApp(
            LinearLayout pai,
            String nome,
            int cor,
            String pacote,
            String site,
            String logoUrl
    ) {

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView logo = new ImageView(this);
        if ("LOCAL_PRIMEVIDEO".equals(logoUrl)) {
            logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
            logo.setAdjustViewBounds(true);
            logo.setPadding(dp(0), dp(18), dp(0), dp(18));
        } else {
            logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
            logo.setAdjustViewBounds(true);
            logo.setPadding(dp(3), dp(3), dp(3), dp(3));
        }

        GradientDrawable fundoLogo = fundo(
                Color.rgb(7, 9, 25),
                20
        );
        fundoLogo.setStroke(dp(3), cor);
        logo.setBackground(fundoLogo);
        logo.setElevation(dp(14));
        logo.setTranslationZ(dp(4));

        item.addView(
                logo,
                new LinearLayout.LayoutParams(
                        dp(112),
                        dp(112)
                )
        );

        if ("LOCAL_NETFLIX".equals(logoUrl)) {
            logo.setImageResource(R.drawable.netflix);
        } else if ("LOCAL_YOUTUBE".equals(logoUrl)) {
            logo.setImageResource(R.drawable.youtube);
        } else if ("LOCAL_PRIMEVIDEO".equals(logoUrl)) {
            logo.setImageResource(R.drawable.primevideo);
        } else if ("LOCAL_DISNEYPLUS".equals(logoUrl)) {
            logo.setImageResource(R.drawable.disneyplus);
        } else if ("LOCAL_HBOMAX".equals(logoUrl)) {
            logo.setImageResource(R.drawable.hbomax);
        } else if ("LOCAL_GLOBOPLAY".equals(logoUrl)) {
            logo.setImageResource(R.drawable.globoplay);
        } else {
            carregarLogoApp(logo, logoUrl);
        }

        TextView nomeView = texto(
                nome,
                13,
                Color.WHITE
        );

        nomeView.setGravity(Gravity.CENTER);
        nomeView.setMaxLines(2);

        item.addView(
                nomeView,
                new LinearLayout.LayoutParams(
                        dp(105),
                        dp(38)
                )
        );

        item.setOnClickListener(
                v -> abrirAplicativo(pacote, site)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        dp(112),
                        dp(145)
                );

        params.setMargins(
                0,
                0,
                dp(8),
                0
        );

        pai.addView(item, params);
    }

    void carregarLogoApp(
            ImageView imagem,
            String endereco
    ) {

        new Thread(() -> {

            try {

                URL url = new URL(endereco);

                HttpURLConnection conexao =
                        (HttpURLConnection) url.openConnection();

                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(10000);
                conexao.setReadTimeout(10000);
                conexao.setInstanceFollowRedirects(true);

                int codigo =
                        conexao.getResponseCode();

                if (codigo < 200 || codigo >= 300) {
                    throw new Exception(
                            "Logo HTTP " + codigo
                    );
                }

                InputStream entrada =
                        conexao.getInputStream();

                Bitmap bitmap =
                        BitmapFactory.decodeStream(entrada);

                entrada.close();
                conexao.disconnect();

                if (bitmap != null) {

                    runOnUiThread(() ->
                            imagem.setImageBitmap(bitmap)
                    );
                }

            } catch (Exception e) {

                android.util.Log.e(
                        "HAVE_LOGO",
                        "Falha ao carregar logo: "
                                + endereco,
                        e
                );
            }

        }).start();
    }

    void abrirAplicativo(String pacote, String site) {

        try {

            Intent intent =
                    getPackageManager()
                            .getLaunchIntentForPackage(pacote);

            if (intent != null) {
                startActivity(intent);
                return;
            }

        } catch (Exception ignored) {}

        try {

            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(site)
            ));

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Não foi possível abrir o serviço.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    void criarSalas(LinearLayout pai) {

        TextView titulo = texto(
                "🔥  Salas em Destaque",
                21,
                Color.WHITE
        );

        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titulo.setPadding(0, dp(18), 0, dp(10));

        pai.addView(titulo);

        TextView vazio = texto(
                "Nenhuma sala pública disponível no momento.",
                14,
                Color.rgb(140, 155, 195)
        );
        vazio.setPadding(0, dp(8), 0, dp(12));
        pai.addView(vazio);
    }

    void adicionarSala(
            LinearLayout pai,
            String imagem,
            String nome,
            String detalhes,
            int cor
    ) {

        LinearLayout sala = new LinearLayout(this);
        sala.setGravity(Gravity.CENTER_VERTICAL);
        sala.setPadding(dp(10), dp(10), dp(10), dp(10));

        GradientDrawable g = fundo(
                Color.rgb(8, 11, 30),
                22
        );

        g.setStroke(
                dp(1),
                Color.rgb(65, 45, 130)
        );

        sala.setBackground(g);

        TextView poster = texto(
                imagem,
                40,
                Color.WHITE
        );

        poster.setGravity(Gravity.CENTER);
        poster.setBackground(fundo(cor, 16));

        sala.addView(poster, new LinearLayout.LayoutParams(
                dp(80), dp(85)
        ));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(12), 0, dp(4), 0);

        TextView nomeView = texto(
                nome,
                17,
                Color.WHITE
        );

        nomeView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView detalhe = texto(
                detalhes,
                13,
                Color.rgb(160, 175, 210)
        );

        info.addView(nomeView);
        info.addView(detalhe);

        sala.addView(info, new LinearLayout.LayoutParams(
                0, -2, 1
        ));

        TextView entrar = texto(
                "ENTRAR",
                12,
                Color.WHITE
        );

        entrar.setGravity(Gravity.CENTER);

        GradientDrawable eb = fundo(
                Color.rgb(95, 20, 180),
                30
        );

        eb.setStroke(
                dp(1),
                Color.rgb(160, 70, 255)
        );

        entrar.setBackground(eb);

        sala.addView(entrar, new LinearLayout.LayoutParams(
                dp(85), dp(45)
        ));

        View.OnClickListener entrarNaSala = v -> {
            Toast.makeText(
                    this,
                    "🎬 Entrando na sala: " + nome,
                    Toast.LENGTH_SHORT
            ).show();
        };

        sala.setOnClickListener(entrarNaSala);
        entrar.setOnClickListener(entrarNaSala);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1, dp(105)
                );

        p.setMargins(0, 0, 0, dp(10));

        pai.addView(sala, p);
    }

    Button pequenoBotao(String textoBotao, int cor) {
        Button b = new Button(this);
        b.setText(textoBotao);
        b.setTextSize(12);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        GradientDrawable g = fundo(Color.rgb(10, 14, 32), 14);
        g.setStroke(dp(2), cor);
        b.setBackground(g);
        return b;
    }

    void criarAmigos(LinearLayout pai) {

        TextView titulo = texto(
                "♧  Pessoas",
                21,
                Color.WHITE
        );
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titulo.setPadding(0, dp(14), 0, dp(8));
        pai.addView(titulo);

        TextView subtitulo = texto(
                "Encontre pessoas reais pelo ID HAVE.",
                14,
                Color.rgb(140, 155, 195)
        );
        subtitulo.setPadding(0, 0, 0, dp(12));
        pai.addView(subtitulo);

        LinearLayout adicionarBox = new LinearLayout(this);
        adicionarBox.setOrientation(LinearLayout.HORIZONTAL);
        adicionarBox.setGravity(Gravity.CENTER_VERTICAL);

        EditText idBusca = new EditText(this);
        idBusca.setHint("Digite o ID HAVE");
        idBusca.setHintTextColor(Color.rgb(110, 125, 150));
        idBusca.setTextColor(Color.WHITE);
        idBusca.setSingleLine(true);
        idBusca.setTextSize(14);
        idBusca.setPadding(dp(14), 0, dp(10), 0);

        GradientDrawable fundoBusca = fundo(Color.rgb(10, 14, 32), 16);
        fundoBusca.setStroke(dp(2), Color.rgb(0, 210, 255));
        idBusca.setBackground(fundoBusca);

        adicionarBox.addView(
                idBusca,
                new LinearLayout.LayoutParams(0, dp(54), 1)
        );

        Button adicionar = new Button(this);
        adicionar.setText("+");
        adicionar.setTextSize(22);
        adicionar.setTextColor(Color.WHITE);
        adicionar.setAllCaps(false);

        GradientDrawable fundoAdicionar =
                fundo(Color.rgb(35, 12, 65), 16);
        fundoAdicionar.setStroke(
                dp(2),
                Color.rgb(190, 70, 255)
        );
        adicionar.setBackground(fundoAdicionar);

        LinearLayout.LayoutParams addParams =
                new LinearLayout.LayoutParams(dp(58), dp(54));
        addParams.setMargins(dp(8), 0, 0, 0);

        adicionarBox.addView(adicionar, addParams);
        pai.addView(
                adicionarBox,
                new LinearLayout.LayoutParams(-1, dp(54))
        );

        LinearLayout botoes = new LinearLayout(this);
        botoes.setOrientation(LinearLayout.HORIZONTAL);
        botoes.setPadding(0, dp(10), 0, dp(10));

        Button grupo = pequenoBotao("👥  CRIAR GRUPO", Color.rgb(190, 70, 255));
        Button conversa = pequenoBotao("💬  CONVERSAS", Color.rgb(0, 210, 255));

        botoes.addView(
                grupo,
                new LinearLayout.LayoutParams(0, dp(44), 1)
        );

        LinearLayout.LayoutParams conversaParams =
                new LinearLayout.LayoutParams(0, dp(44), 1);
        conversaParams.setMargins(dp(8), 0, 0, 0);

        botoes.addView(conversa, conversaParams);
        pai.addView(botoes);

        TextView onlineTitulo = texto(
                "🟢  Amigos online",
                16,
                Color.WHITE
        );
        onlineTitulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        pai.addView(onlineTitulo);

        TextView onlineVazio = texto(
                "Nenhum amigo online.",
                14,
                Color.rgb(140, 155, 195)
        );
        onlineVazio.setPadding(0, dp(5), 0, dp(12));
        pai.addView(onlineVazio);

        TextView offlineTitulo = texto(
                "⚫  Amigos offline",
                16,
                Color.WHITE
        );
        offlineTitulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        pai.addView(offlineTitulo);

        TextView offlineVazio = texto(
                "Nenhum amigo offline.",
                14,
                Color.rgb(140, 155, 195)
        );
        offlineVazio.setPadding(0, dp(5), 0, dp(12));
        pai.addView(offlineVazio);

        adicionar.setOnClickListener(v -> {
            String id = idBusca.getText().toString().trim();

            if (id.isEmpty()) {
                idBusca.setError("Digite um ID HAVE");
                idBusca.requestFocus();
                return;
            }

            Toast.makeText(
                    this,
                    "Pesquisa pelo ID HAVE será conectada ao Firestore.",
                    Toast.LENGTH_SHORT
            ).show();
        });

        grupo.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Criador de grupos HAVE será aberto aqui.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        conversa.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Suas conversas HAVE serão abertas aqui.",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    void adicionarAmigo(
            LinearLayout pai,
            String inicial,
            String nome
    ) {

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView avatar = texto(
                inicial,
                22,
                Color.WHITE
        );

        avatar.setGravity(Gravity.CENTER);

        GradientDrawable g = fundo(
                Color.rgb(42, 20, 75),
                50
        );

        g.setStroke(
                dp(2),
                Color.rgb(130, 55, 255)
        );

        avatar.setBackground(g);

        item.addView(avatar, new LinearLayout.LayoutParams(
                dp(65), dp(65)
        ));

        TextView n = texto(
                nome,
                12,
                Color.WHITE
        );

        n.setGravity(Gravity.CENTER);

        item.addView(n);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        dp(82), dp(105)
                );

        p.setMargins(0, 0, dp(6), 0);

        pai.addView(item, p);
    }



    void abrirBuscaHave() {
        final LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(20), dp(24), dp(20), dp(24));
        raiz.setBackgroundColor(Color.rgb(3, 4, 15));

        TextView titulo = texto("⌕ BUSCAR NO HAVE", 24, Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);

        TextView subtitulo = texto(
                "Encontre pessoas pelo ID HAVE",
                14,
                Color.rgb(150, 165, 190)
        );
        subtitulo.setPadding(0, dp(6), 0, dp(22));

        EditText campo = new EditText(this);
        campo.setHint("Ex.: HAVE-AVN4SX12");
        campo.setHintTextColor(Color.rgb(100, 115, 140));
        campo.setTextColor(Color.WHITE);
        campo.setSingleLine(true);
        campo.setTextSize(15);
        campo.setPadding(dp(16), 0, dp(16), 0);

        GradientDrawable fundoCampo = fundo(Color.rgb(10, 14, 32), 16);
        fundoCampo.setStroke(dp(1), Color.rgb(0, 210, 255));
        campo.setBackground(fundoCampo);

        raiz.addView(titulo);
        raiz.addView(subtitulo);
        raiz.addView(campo, new LinearLayout.LayoutParams(-1, dp(54)));

        Button pesquisar = new Button(this);
        pesquisar.setText("🔎 PESQUISAR");
        pesquisar.setTextColor(Color.WHITE);
        pesquisar.setTextSize(14);
        pesquisar.setAllCaps(false);

        GradientDrawable fundoBotao = fundo(Color.rgb(35, 18, 65), 16);
        fundoBotao.setStroke(dp(1), Color.rgb(145, 45, 255));
        pesquisar.setBackground(fundoBotao);

        LinearLayout.LayoutParams botaoParams =
                new LinearLayout.LayoutParams(-1, dp(54));
        botaoParams.topMargin = dp(14);
        raiz.addView(pesquisar, botaoParams);

        TextView resultado = texto("", 15, Color.rgb(220, 225, 240));
        resultado.setPadding(dp(16), dp(18), dp(16), dp(18));
        resultado.setVisibility(View.GONE);

        LinearLayout.LayoutParams resultadoParams =
                new LinearLayout.LayoutParams(-1, -2);
        resultadoParams.topMargin = dp(18);
        raiz.addView(resultado, resultadoParams);

        TextView fechar = texto("← VOLTAR", 14, Color.rgb(0, 210, 255));
        fechar.setGravity(Gravity.CENTER);
        fechar.setPadding(0, dp(18), 0, dp(10));
        raiz.addView(fechar, new LinearLayout.LayoutParams(-1, dp(54)));

        setContentView(raiz);

        pesquisar.setOnClickListener(v -> {
            String id = campo.getText().toString().trim().toUpperCase(Locale.US);

            if (id.isEmpty()) {
                campo.setError("Digite um ID HAVE");
                campo.requestFocus();
                return;
            }

            resultado.setVisibility(View.VISIBLE);
            resultado.setText("🔎 Procurando " + id + "...");

            FirebaseFirestore.getInstance()
                    .collection("have_perfis_publicos")
                    .whereEqualTo("haveId", id)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.isEmpty()) {
                            resultado.setText(
                                    "❌ Usuário não encontrado.\n\n" +
                                    "Confira o ID HAVE e tente novamente."
                            );
                            return;
                        }

                        com.google.firebase.firestore.DocumentSnapshot doc =
                                snapshot.getDocuments().get(0);

                        String nome = doc.getString("nome");
                        String haveId = doc.getString("haveId");
                        String status = doc.getString("status");

                        if (nome == null || nome.isEmpty()) nome = "Usuário HAVE";
                        if (haveId == null || haveId.isEmpty()) haveId = id;
                        if (status == null || status.isEmpty()) status = "offline";

                        resultado.setText(
                                "● PERFIL ENCONTRADO\n\n" +
                                "👤 " + nome + "\n" +
                                "🆔 " + haveId + "\n" +
                                "● " + status.toUpperCase(Locale.US)
                        );
                    })
                    .addOnFailureListener(e ->
                            resultado.setText(
                                    "⚠️ Não foi possível realizar a busca.\n\n" +
                                    "Verifique sua conexão e tente novamente."
                            )
                    );
        });

        fechar.setOnClickListener(v -> montarHome());
    }

    void abrirPerfilHave() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Toast.makeText(this, "Faça login no HAVE primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = usuario.getUid();

        db.collection("have_usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        exibirPerfilHave(
                                doc.getString("nome"),
                                doc.getString("haveId"),
                                usuario.getEmail()
                        );
                        sincronizarPerfilPublico(
                                uid,
                                doc.getString("nome"),
                                doc.getString("haveId")
                        );
                        return;
                    }

                    String email = usuario.getEmail();
                    String nome = usuario.getDisplayName();

                    if (nome == null || nome.trim().isEmpty()) {
                        nome = "Usuário HAVE";
                    }

                    String haveId = "HAVE-" +
                            UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 8)
                                    .toUpperCase(Locale.US);

                    Map<String, Object> dados = new HashMap<>();
                    dados.put("nome", nome);
                    dados.put("email", email == null ? "" : email);
                    dados.put("haveId", haveId);
                    dados.put("criadoEm", FieldValue.serverTimestamp());

                    final String nomeFinal = nome;
                    final String haveIdFinal = haveId;
                    final String emailFinal = email == null ? "" : email;

                    db.collection("have_usuarios")
                            .document(uid)
                            .set(dados)
                            .addOnSuccessListener(v -> {
                                sincronizarPerfilPublico(
                                        uid,
                                        nomeFinal,
                                        haveIdFinal
                                );

                                Toast.makeText(
                                        this,
                                        "Perfil HAVE recuperado com sucesso.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                exibirPerfilHave(
                                        nomeFinal,
                                        haveIdFinal,
                                        emailFinal
                                );
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Não foi possível criar seu perfil: "
                                                    + e.getLocalizedMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Erro ao carregar perfil: "
                                        + e.getLocalizedMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void sincronizarPerfilPublico(
            String uid,
            String nome,
            String haveId
    ) {
        if (nome == null || nome.trim().isEmpty()) {
            nome = "Usuário HAVE";
        }

        if (haveId == null || haveId.trim().isEmpty()) {
            return;
        }

        Map<String, Object> publico = new HashMap<>();
        publico.put("nome", nome);
        publico.put("haveId", haveId);
        publico.put("status", "online");
        publico.put("atualizadoEm", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("have_perfis_publicos")
                .document(uid)
                .set(publico, SetOptions.merge());
    }

    private void exibirPerfilHave(
        String nome,
        String haveId,
        String email
) {
    if (nome == null || nome.trim().isEmpty()) nome = "Usuário HAVE";
    if (haveId == null || haveId.trim().isEmpty()) haveId = "HAVE-SEM-ID";
    if (email == null) email = "";

    final String nomeInicial = nome;
    final String emailInicial = email;
    final String haveIdCopia = haveId;

    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(true);
    scroll.setBackgroundColor(Color.rgb(2, 4, 15));

    LinearLayout tela = new LinearLayout(this);
    tela.setOrientation(LinearLayout.VERTICAL);
    tela.setPadding(dp(22), dp(24), dp(22), dp(30));

    TextView titulo = texto("◉  PERFIL HAVE", 25, Color.WHITE);
    titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    tela.addView(titulo);

    TextView subtitulo = texto(
            "Seu espaço pessoal • seguro • privado",
            13,
            Color.rgb(115, 210, 255)
    );
    subtitulo.setPadding(0, dp(5), 0, dp(24));
    tela.addView(subtitulo);

    TextView avatar = texto("◉", 52, Color.rgb(90, 220, 255));
    avatar.setGravity(Gravity.CENTER);
    GradientDrawable avatarBg = new GradientDrawable();
    avatarBg.setShape(GradientDrawable.OVAL);
    avatarBg.setColor(Color.TRANSPARENT);
    avatarBg.setStroke(dp(2), Color.rgb(70, 210, 255));
    avatar.setBackground(avatarBg);

    LinearLayout.LayoutParams avatarParams =
            new LinearLayout.LayoutParams(dp(100), dp(100));
    avatarParams.gravity = Gravity.CENTER_HORIZONTAL;
    tela.addView(avatar, avatarParams);

    TextView trocarFoto = itemConfiguracaoHave(
            "📷",
            "Trocar foto",
            "Adicionar ou alterar sua foto de perfil"
    );
    trocarFoto.setOnClickListener(v ->
            Toast.makeText(
                    this,
                    "A foto será conectada ao armazenamento na próxima etapa.",
                    Toast.LENGTH_SHORT
            ).show()
    );
    tela.addView(trocarFoto);

    TextView nomeView = texto(nome, 23, Color.WHITE);
    nomeView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    nomeView.setGravity(Gravity.CENTER);
    nomeView.setPadding(0, dp(10), 0, dp(2));
    tela.addView(nomeView);

    TextView editarNome = itemConfiguracaoHave(
            "✎",
            "Editar nome",
            "Alterar o nome exibido no seu perfil"
    );
    editarNome.setOnClickListener(v -> {
        final EditText campoNome = new EditText(this);
        campoNome.setSingleLine(true);
        campoNome.setText(nomeInicial);
        campoNome.setTextColor(Color.WHITE);
        campoNome.setHintTextColor(Color.rgb(120, 140, 175));
        campoNome.setHint("Novo nome");

        new android.app.AlertDialog.Builder(this)
                .setTitle("✎ EDITAR NOME")
                .setView(campoNome)
                .setPositiveButton("SALVAR", (dialog, which) -> {
                    String novoNome = campoNome.getText().toString().trim();

                    if (novoNome.isEmpty()) {
                        Toast.makeText(this, "Digite um nome válido.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser usuarioAtual =
                            FirebaseAuth.getInstance().getCurrentUser();

                    if (usuarioAtual == null) {
                        Toast.makeText(this, "Sessão HAVE não encontrada.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = usuarioAtual.getUid();

                    Map<String, Object> dadosNome = new HashMap<>();
                    dadosNome.put("nome", novoNome);

                    FirebaseFirestore.getInstance()
                            .collection("have_usuarios")
                            .document(uid)
                            .update(dadosNome)
                            .addOnSuccessListener(v2 -> {
                                sincronizarPerfilPublico(uid, novoNome, haveIdCopia);
                                Toast.makeText(
                                        this,
                                        "Nome atualizado com sucesso.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                exibirPerfilHave(
                                        novoNome,
                                        haveIdCopia,
                                        emailInicial
                                );
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Não foi possível atualizar o nome.",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    });
    tela.addView(editarNome);

    TextView status = texto(
            "● ONLINE",
            13,
            Color.rgb(55, 235, 155)
    );
    status.setGravity(Gravity.CENTER);
    status.setPadding(0, dp(8), 0, dp(20));
    tela.addView(status);

    TextView config = texto("⚙  CONFIGURAÇÕES", 16, Color.WHITE);
    config.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    config.setPadding(0, dp(8), 0, dp(5));
    tela.addView(config);

    TextView privacidade = itemConfiguracaoHave(
            "🔐",
            "Privacidade",
            "Controle o que outras pessoas podem ver"
    );
    privacidade.setOnClickListener(v -> abrirPrivacidadeHave());
    tela.addView(privacidade);

    TextView seguranca = itemConfiguracaoHave(
            "🛡",
            "Segurança",
            "Proteção e segurança da sua conta"
    );
    seguranca.setOnClickListener(v -> abrirSegurancaHave());
    tela.addView(seguranca);

    TextView aparencia = itemConfiguracaoHave(
            "🎨",
            "Aparência",
            "Personalize o visual do HAVE"
    );
    aparencia.setOnClickListener(v -> abrirAparenciaHave());
    tela.addView(aparencia);

    TextView notificacoes = itemConfiguracaoHave(
            "🔔",
            "Notificações",
            "Controle os avisos do HAVE"
    );
    notificacoes.setOnClickListener(v -> abrirNotificacoesHave());
    tela.addView(notificacoes);

    TextView conexoes = texto("👥  CONEXÕES", 16, Color.WHITE);
    conexoes.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    conexoes.setPadding(0, dp(26), 0, dp(5));
    tela.addView(conexoes);

    TextView meuId = itemConfiguracaoHave(
            "🔗",
            "Meu HAVE ID",
            "Copiar, compartilhar ou revogar seu ID"
    );
    meuId.setOnClickListener(v -> abrirMeuHaveId(nomeInicial, haveIdCopia));
    tela.addView(meuId);

    TextView bloqueados = itemConfiguracaoHave(
            "🚫",
            "Usuários bloqueados",
            "Gerenciar pessoas que você bloqueou"
    );
    bloqueados.setOnClickListener(v -> abrirBloqueadosHave());
    tela.addView(bloqueados);

    TextView conta = texto("✉  E-mail privado", 13, Color.rgb(115, 130, 165));
    conta.setPadding(0, dp(26), 0, dp(4));
    tela.addView(conta);

    TextView emailPrivado = texto(
            "Este e-mail não aparece no perfil público.",
            12,
            Color.rgb(90, 105, 140)
    );
    tela.addView(emailPrivado);

    TextView voltar = itemConfiguracaoHave(
            "←",
            "Voltar",
            "Retornar ao início do HAVE"
    );
    voltar.setOnClickListener(v -> montarHome());
    tela.addView(voltar);

    scroll.addView(tela);
    setContentView(scroll);
}

private void abrirMeuHaveId(String nome, String haveId) {
    TextView id = texto(haveId, 22, Color.rgb(75, 220, 255));
    id.setGravity(Gravity.CENTER);
    id.setPadding(0, dp(18), 0, dp(18));

    LinearLayout box = new LinearLayout(this);
    box.setOrientation(LinearLayout.VERTICAL);
    box.setPadding(dp(18), dp(8), dp(18), dp(4));
    box.addView(id);

    Button copiar = pequenoBotao("📋  COPIAR ID", Color.rgb(70, 190, 255));
    copiar.setOnClickListener(v -> {
        android.content.ClipboardManager cm =
                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(
                android.content.ClipData.newPlainText("HAVE ID", haveId)
        );
        Toast.makeText(this, "HAVE ID copiado.", Toast.LENGTH_SHORT).show();
    });
    box.addView(copiar);

    Button compartilhar = pequenoBotao(
            "📤  COMPARTILHAR",
            Color.rgb(170, 75, 255)
    );
    compartilhar.setOnClickListener(v -> {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(
                Intent.EXTRA_TEXT,
                "Meu HAVE ID é " + haveId
        );
        startActivity(Intent.createChooser(intent, "Compartilhar HAVE ID"));
    });
    box.addView(compartilhar);

    Button revogar = pequenoBotao(
            "🔄  REVOGAR ID",
            Color.rgb(255, 80, 120)
    );
    revogar.setOnClickListener(v -> {
        new android.app.AlertDialog.Builder(this)
                .setTitle("REVOGAR HAVE ID")
                .setMessage(
                        "O ID atual deixará de funcionar para novas buscas. " +
                        "Um novo HAVE ID será criado."
                )
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("REVOGAR", (dialog, which) -> {
                    FirebaseUser usuario =
                            FirebaseAuth.getInstance().getCurrentUser();

                    if (usuario == null) {
                        Toast.makeText(
                                this,
                                "Sessão HAVE não encontrada.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    String novoId =
                            "HAVE-" +
                            UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 8)
                                    .toUpperCase(Locale.US);

                    String uid = usuario.getUid();

                    Map<String, Object> dados = new HashMap<>();
                    dados.put("haveId", novoId);

                    FirebaseFirestore db =
                            FirebaseFirestore.getInstance();

                    db.collection("have_usuarios")
                            .document(uid)
                            .update(dados)
                            .addOnSuccessListener(v2 -> {
                                sincronizarPerfilPublico(
                                        uid,
                                        nome,
                                        novoId
                                );

                                Toast.makeText(
                                        this,
                                        "Novo HAVE ID criado com sucesso.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                exibirPerfilHave(
                                        nome,
                                        novoId,
                                        usuario.getEmail()
                                );
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Não foi possível revogar o ID.",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                })
                .show();
    });
    box.addView(revogar);

    new android.app.AlertDialog.Builder(this)
            .setTitle("🔗 MEU HAVE ID")
            .setView(box)
            .setNegativeButton("FECHAR", null)
            .show();
}

private void abrirBloqueadosHave() {
    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

    if (usuario == null) {
        Toast.makeText(this, "Sessão HAVE não encontrada.", Toast.LENGTH_SHORT).show();
        return;
    }

    TextView info = texto(
            "Aqui aparecerão os usuários que você bloquear.\n\n" +
            "O bloqueio ficará vinculado à sua conta HAVE.",
            14,
            Color.rgb(185, 195, 220)
    );
    info.setPadding(dp(8), dp(10), dp(8), dp(10));

    new android.app.AlertDialog.Builder(this)
            .setTitle("🚫 USUÁRIOS BLOQUEADOS")
            .setView(info)
            .setPositiveButton("FECHAR", null)
            .show();
}

    private TextView itemConfiguracaoHave(
            String icone,
            String titulo,
            String descricao
    ) {
        TextView item = texto(
                icone + "  " + titulo + "\n" + descricao,
                15,
                Color.WHITE
        );

        item.setPadding(dp(4), dp(14), dp(4), dp(14));
        item.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable linha = new GradientDrawable();
        linha.setColor(Color.TRANSPARENT);
        linha.setStroke(dp(1), Color.rgb(35, 70, 105));
        linha.setCornerRadius(dp(14));
        item.setBackground(linha);

        item.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                item.setAlpha(0.65f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP
                    || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                item.setAlpha(1f);
            }
            return false;
        });

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(5);
        params.bottomMargin = dp(5);
        item.setLayoutParams(params);

        return item;
    }

    private void abrirPrivacidadeHave() {
        android.app.AlertDialog.Builder builder =
                new android.app.AlertDialog.Builder(this);

        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(dp(24), dp(10), dp(24), dp(10));

        TextView titulo = texto(
                "🔒 PRIVACIDADE",
                21,
                Color.WHITE
        );
        titulo.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView explicacao = texto(
                "Escolha quais informações do seu perfil podem "
                        + "ser exibidas para outras pessoas.",
                13,
                Color.rgb(155, 165, 195)
        );
        explicacao.setPadding(0, dp(8), 0, dp(18));

        android.widget.Switch foto = new android.widget.Switch(this);
        foto.setText("📷 Mostrar minha foto");
        foto.setTextColor(Color.WHITE);
        foto.setChecked(getPreferences(0)
                .getBoolean("priv_foto", true));

        android.widget.Switch status = new android.widget.Switch(this);
        status.setText("🟢 Mostrar status online");
        status.setTextColor(Color.WHITE);
        status.setChecked(getPreferences(0)
                .getBoolean("priv_status", true));

        TextView idInfo = texto(
                "🆔 HAVE ID\n"
                        + "Seu HAVE ID permanece público para "
                        + "permitir que outras pessoas encontrem você.",
                13,
                Color.rgb(195, 205, 225)
        );
        idInfo.setPadding(0, dp(12), 0, dp(12));

        TextView emailInfo = texto(
                "📧 E-mail\n"
                        + "Seu e-mail é sempre privado e não aparece "
                        + "no perfil público.",
                13,
                Color.rgb(195, 205, 225)
        );
        emailInfo.setPadding(0, dp(8), 0, dp(12));

        conteudo.addView(titulo);
        conteudo.addView(explicacao);
        conteudo.addView(foto);
        conteudo.addView(status);
        conteudo.addView(idInfo);
        conteudo.addView(emailInfo);

        android.app.AlertDialog dialog = builder
                .setView(conteudo)
                .setPositiveButton("SALVAR", null)
                .setNegativeButton("CANCELAR", null)
                .create();

        dialog.setOnShowListener(v -> {
            dialog.getButton(
                    android.app.AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(x -> {
                getPreferences(0)
                        .edit()
                        .putBoolean("priv_foto", foto.isChecked())
                        .putBoolean("priv_status", status.isChecked())
                        .apply();

                Toast.makeText(
                        this,
                        "Privacidade atualizada.",
                        Toast.LENGTH_SHORT
                ).show();

                dialog.dismiss();
            });
        });

        dialog.show();
    }
    private void abrirSegurancaHave() {
    final EditText email = new EditText(this);
    email.setSingleLine(true);
    email.setHint("E-mail da conta HAVE");
    email.setTextColor(Color.WHITE);
    email.setHintTextColor(Color.rgb(120, 140, 175));
    email.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
            android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    );

    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    if (usuario != null && usuario.getEmail() != null) {
        email.setText(usuario.getEmail());
    }

    new android.app.AlertDialog.Builder(this)
            .setTitle("🛡 SEGURANÇA")
            .setMessage(
                    "Digite o e-mail da sua conta. " +
                    "O Firebase enviará o link oficial para criar uma nova senha."
            )
            .setView(email)
            .setNegativeButton("FECHAR", null)
            .setPositiveButton("ENVIAR", (dialog, which) -> {
                String endereco = email.getText().toString().trim();

                if (endereco.isEmpty() ||
                        !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(endereco).matches()) {
                    Toast.makeText(
                            this,
                            "Digite um e-mail válido.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                FirebaseAuth.getInstance()
                        .sendPasswordResetEmail(endereco)
                        .addOnSuccessListener(v ->
                                Toast.makeText(
                                        this,
                                        "E-mail de redefinição enviado. Verifique também o spam.",
                                        Toast.LENGTH_LONG
                                ).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(
                                        this,
                                        "Não foi possível enviar o e-mail.",
                                        Toast.LENGTH_LONG
                                ).show()
                        );
            })
            .show();
}
    private void abrirAparenciaHave() {
    final String[] opcoes = {
            "☀  Modo claro",
            "🌙  Modo escuro",
            "⚙  Automático"
    };

    android.content.SharedPreferences pref =
            getSharedPreferences("have_config", MODE_PRIVATE);

    String atual = pref.getString("tema", "claro");

    int marcado = 0;
    if ("escuro".equals(atual)) marcado = 1;
    if ("automatico".equals(atual)) marcado = 2;

    final int[] escolha = {marcado};

    new android.app.AlertDialog.Builder(this)
            .setTitle("🎨 APARÊNCIA")
            .setSingleChoiceItems(
                    opcoes,
                    marcado,
                    (dialog, which) -> escolha[0] = which
            )
            .setNegativeButton("CANCELAR", null)
            .setPositiveButton("APLICAR", (dialog, which) -> {
                String tema =
                        escolha[0] == 1
                                ? "escuro"
                                : escolha[0] == 2
                                ? "automatico"
                                : "claro";

                pref.edit()
                        .putString("tema", tema)
                        .apply();

                Toast.makeText(
                        this,
                        "Tema salvo: " +
                                (tema.equals("claro")
                                        ? "Modo claro"
                                        : tema.equals("escuro")
                                        ? "Modo escuro"
                                        : "Automático"),
                        Toast.LENGTH_SHORT
                ).show();

                aplicarTemaHave(tema);
            })
            .show();
}


        private void aplicarTemaHave(String tema) {
    boolean escuro = "escuro".equals(tema);

    if ("automatico".equals(tema)) {
        int modo =
                getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        escuro =
                modo ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    if (escuro) {
        getWindow().setStatusBarColor(Color.rgb(2, 4, 15));
        getWindow().setNavigationBarColor(Color.rgb(2, 4, 15));
    } else {
        getWindow().setStatusBarColor(Color.rgb(245, 248, 255));
        getWindow().setNavigationBarColor(Color.rgb(245, 248, 255));
    }
}

private void abrirNotificacoesHave() {
        android.widget.Switch avisos =
                new android.widget.Switch(this);

        avisos.setText("🔔 Permitir notificações do HAVE");
        avisos.setTextColor(Color.WHITE);
        avisos.setChecked(
                getPreferences(0)
                        .getBoolean("notificacoes_have", true)
        );

        new android.app.AlertDialog.Builder(this)
                .setTitle("🔔 NOTIFICAÇÕES")
                .setView(avisos)
                .setPositiveButton("SALVAR", (dialog, which) -> {
                    getPreferences(0)
                            .edit()
                            .putBoolean(
                                    "notificacoes_have",
                                    avisos.isChecked()
                            )
                            .apply();

                    Toast.makeText(
                            this,
                            "Preferência de notificações salva.",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    }



    void criarNavegacao(LinearLayout raiz) {

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(
                dp(5),
                dp(8),
                dp(5),
                dp(8)
        );

        GradientDrawable g = fundo(
                Color.rgb(5, 7, 22),
                26
        );

        g.setStroke(
                dp(1),
                Color.rgb(45, 50, 100)
        );

        nav.setBackground(g);
        nav.setElevation(dp(20));

        adicionarNav(nav, "⌂", "Início", true);
        adicionarNav(nav, "♧", "Salas", false);
        adicionarNav(nav, "+", "Criar", false);
        adicionarNav(nav, "●", "Amigos", false);
        adicionarNav(nav, "🎧", "Suporte", false);
        nav.getChildAt(4).setOnClickListener(v ->
                Toast.makeText(this,
                        "Suporte HAVE em breve.",
                        Toast.LENGTH_SHORT).show()
        );

        raiz.addView(nav, new LinearLayout.LayoutParams(
                -1, dp(82)
        ));
    }

    void adicionarNav(
            LinearLayout nav,
            String icone,
            String nome,
            boolean ativo
    ) {

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);

        TextView i = texto(
                icone,
                26,
                ativo
                        ? Color.rgb(195, 60, 255)
                        : Color.rgb(140, 155, 195)
        );

        i.setGravity(Gravity.CENTER);

        TextView n = texto(
                nome,
                11,
                ativo
                        ? Color.rgb(205, 80, 255)
                        : Color.rgb(140, 155, 195)
        );

        n.setGravity(Gravity.CENTER);

        item.addView(i);
        item.addView(n);

        nav.addView(item, new LinearLayout.LayoutParams(
                0, -1, 1
        ));
    }
}
