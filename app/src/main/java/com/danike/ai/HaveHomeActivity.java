package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.Settings;
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
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

public class HaveHomeActivity extends Activity {
    private boolean modoConviteHave = false;
    private String salaConviteId = "";
    private String salaConviteNome = "";
    private String salaConviteServico = "";
    private boolean telaAmigosAberta = false;

    private LinearLayout conteudoPerfilHave;


    private LinearLayout heroHave;
    private ImageView imagemHeroHave;
    private TextView tituloHeroHave;
    private TextView provedorHeroHave;
    private TextView descricaoHeroHave;
    private LinearLayout indicadoresHeroHave;
    private JSONArray catalogoHeroHave;
    private EditText campoLinkUniversalHave;
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

        Intent entrada = getIntent();
        modoConviteHave = entrada.getBooleanExtra("modoConviteHave", false);

        if (modoConviteHave) {
            salaConviteId = entrada.getStringExtra("salaId");
            salaConviteNome = entrada.getStringExtra("salaNome");
            salaConviteServico = entrada.getStringExtra("salaServico");

            if (salaConviteId == null) salaConviteId = "";
            if (salaConviteNome == null) salaConviteNome = "Sala HAVE";
            if (salaConviteServico == null) salaConviteServico = "Netflix";
        }

        verificarNomeObrigatorio();
    }

    private void verificarNomeObrigatorio() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            montarHome();
            return;
        }

        String uid = usuario.getUid();
        FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    String nome = doc.exists() ? doc.getString("nome") : null;
                    String foto = doc.exists() ? doc.getString("fotoUrl") : null;

                    if (nome != null && !nome.trim().isEmpty()
                            && !nome.trim().equalsIgnoreCase("Usuário")) {
                        String nomeValido = nome.trim();
                        getSharedPreferences("DaNikeAI_Dados", MODE_PRIVATE)
                                .edit().putString("nome", nomeValido).apply();
                        sincronizarNomeHAVE(uid, nomeValido, foto, this::montarHome);
                    } else {
                        mostrarDialogNomeObrigatorio(uid);
                    }
                })
                .addOnFailureListener(e -> new AlertDialog.Builder(this)
                        .setTitle("NÃO FOI POSSÍVEL VERIFICAR")
                        .setMessage("Não conseguimos consultar seu perfil. Verifique a conexão e tente novamente.")
                        .setCancelable(false)
                        .setPositiveButton("TENTAR NOVAMENTE",
                                (d, w) -> verificarNomeObrigatorio())
                        .show());
    }

    private void mostrarDialogNomeObrigatorio(String uid) {
        LinearLayout painel = new LinearLayout(this);
        painel.setOrientation(LinearLayout.VERTICAL);
        painel.setPadding(dp(24), dp(18), dp(24), dp(12));
        painel.setBackgroundColor(Color.rgb(5, 8, 20));

        TextView titulo = texto("ANTES DE CONTINUAR", 20, Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setGravity(Gravity.CENTER);
        painel.addView(titulo);

        TextView mensagem = texto(
                "Para usar o HAVE, precisamos do seu nome público.",
                15, Color.rgb(220, 230, 245));
        mensagem.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(-1, -2);
        mp.setMargins(0, dp(16), 0, dp(8));
        painel.addView(mensagem, mp);

        TextView dica = texto(
                "Esse nome aparecerá nos seus perfis do DaNikeAI.",
                12, Color.rgb(135, 165, 195));
        dica.setGravity(Gravity.CENTER);
        painel.addView(dica);

        EditText campo = new EditText(this);
        campo.setSingleLine(true);
        campo.setTextSize(16);
        campo.setTextColor(Color.WHITE);
        campo.setHintTextColor(Color.rgb(120, 140, 165));
        campo.setHint("Digite seu nome");
        campo.setPadding(dp(14), dp(10), dp(14), dp(10));

        GradientDrawable borda = fundo(Color.rgb(10, 17, 35), 12);
        borda.setStroke(dp(1), Color.rgb(0, 220, 255));
        campo.setBackground(borda);

        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, dp(20), 0, dp(4));
        painel.addView(campo, cp);
        painel.addView(texto("Campo obrigatório", 11, Color.rgb(0, 220, 255)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(painel)
                .setPositiveButton("SALVAR E CONTINUAR", null)
                .create();

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(ignorado -> {
            Button salvar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            salvar.setTextColor(Color.rgb(0, 180, 230));
            salvar.setOnClickListener(v -> {
                String nome = campo.getText().toString().trim();

                if (nome.length() < 2) {
                    campo.setError("Digite seu nome para continuar");
                    campo.requestFocus();
                    return;
                }

                salvar.setEnabled(false);
                Map<String, Object> dados = new HashMap<>();
                dados.put("nome", nome);

                FirebaseFirestore.getInstance()
                        .collection("usuarios").document(uid)
                        .set(dados, SetOptions.merge())
                        .addOnSuccessListener(ok -> {
                            getSharedPreferences("DaNikeAI_Dados", MODE_PRIVATE)
                                    .edit().putString("nome", nome).apply();

                            FirebaseUser atual =
                                    FirebaseAuth.getInstance().getCurrentUser();
                            String foto = atual != null && atual.getPhotoUrl() != null
                                    ? atual.getPhotoUrl().toString() : null;

                            dialog.dismiss();
                            sincronizarNomeHAVE(uid, nome, foto, this::montarHome);
                        })
                        .addOnFailureListener(e -> {
                            salvar.setEnabled(true);
                            Toast.makeText(this,
                                    "Não foi possível salvar. Tente novamente.",
                                    Toast.LENGTH_LONG).show();
                        });
            });
        });
        dialog.show();
    }

    private void sincronizarNomeHAVE(
            String uid, String nome, String fotoUrl, Runnable concluir) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", nome);
        if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {
            dados.put("fotoUrl", fotoUrl.trim());
        }
        dados.put("atualizadoEm", FieldValue.serverTimestamp());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("have_usuarios").document(uid)
                .set(dados, SetOptions.merge())
                .addOnSuccessListener(ok ->
                        db.collection("have_perfis_publicos").document(uid)
                                .set(dados, SetOptions.merge())
                                .addOnSuccessListener(ok2 -> concluir.run())
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this,
                                            "Perfil público não sincronizado; confira a conexão.",
                                            Toast.LENGTH_LONG).show();
                                    concluir.run();
                                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Perfil HAVE não sincronizado; confira a conexão.",
                            Toast.LENGTH_LONG).show();
                    concluir.run();
                });
    }


    void montarHome() {
        telaAmigosAberta = false;

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(Color.rgb(3, 4, 15));

        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(dp(12), dp(12), dp(12), dp(12));

        raiz.addView(conteudo, new LinearLayout.LayoutParams(
                -1, -1));

        setContentView(raiz);

        criarHero(conteudo);
    }

  void mostrarCentralApps() {
      startActivity(new Intent(this, HaveConnectActivity.class));
  }

  void adicionarServicoCentral(
          LinearLayout pai, String nome, String descricao,
          int cor, String pacote, String site
  ) {
      LinearLayout linha = new LinearLayout(this);
      linha.setGravity(Gravity.CENTER_VERTICAL);
      linha.setPadding(dp(10), dp(9), dp(10), dp(9));

      GradientDrawable bg = fundo(Color.rgb(13, 15, 34), 16);
      bg.setStroke(dp(1), Color.rgb(48, 42, 82));
      linha.setBackground(bg);

      TextView simbolo = texto("▶", 19, Color.WHITE);
      simbolo.setGravity(Gravity.CENTER);
      simbolo.setBackground(fundo(cor, 13));
      linha.addView(simbolo, new LinearLayout.LayoutParams(dp(46), dp(46)));

      LinearLayout textos = new LinearLayout(this);
      textos.setOrientation(LinearLayout.VERTICAL);
      textos.setPadding(dp(12), 0, dp(6), 0);

      TextView titulo = texto(nome, 15, Color.WHITE);
      titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
      TextView detalhe = texto(descricao, 12, Color.rgb(155, 165, 195));
      textos.addView(titulo);
      textos.addView(detalhe);

      linha.addView(textos, new LinearLayout.LayoutParams(0, -2, 1));
      TextView seta = texto("›", 28, Color.rgb(175, 105, 255));
      linha.addView(seta);

      linha.setOnClickListener(v -> abrirAplicativo(pacote, site));

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
              -1, -2
      );
      params.bottomMargin = dp(8);
      pai.addView(linha, params);
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

        informacoes.addView(etiqueta);
        informacoes.addView(tituloHeroHave);
        informacoes.addView(descricaoHeroHave);

        indicadoresHeroHave = new LinearLayout(this);
        indicadoresHeroHave.setOrientation(LinearLayout.HORIZONTAL);
        indicadoresHeroHave.setGravity(Gravity.CENTER_VERTICAL);
        indicadoresHeroHave.setPadding(0, dp(5), 0, dp(8));
        informacoes.addView(
                indicadoresHeroHave,
                new LinearLayout.LayoutParams(-1, dp(20))
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
            atualizarIndicadoresHeroHave();

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

    private void atualizarIndicadoresHeroHave() {
        if (indicadoresHeroHave == null) return;

        indicadoresHeroHave.removeAllViews();

        if (catalogoHeroHave == null || catalogoHeroHave.length() <= 1) {
            return;
        }

        for (int i = 0; i < catalogoHeroHave.length(); i++) {
            GradientDrawable ponto = new GradientDrawable();
            ponto.setShape(GradientDrawable.RECTANGLE);
            ponto.setCornerRadius(dp(10));
            ponto.setColor(i == indiceHeroHave
                    ? Color.rgb(0, 220, 255)
                    : Color.rgb(90, 75, 125));

            View indicador = new View(this);
            indicador.setBackground(ponto);

            int largura = i == indiceHeroHave ? dp(23) : dp(7);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(largura, dp(7));
            lp.setMargins(dp(3), 0, dp(3), 0);
            indicadoresHeroHave.addView(indicador, lp);
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


    void criarBotaoCriarGrupoHave(LinearLayout pai) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(20), dp(18), dp(20), dp(18));

        GradientDrawable fundoCard = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(18, 10, 35),
                        Color.rgb(7, 14, 32)
                }
        );
        fundoCard.setCornerRadius(dp(24));
        fundoCard.setStroke(dp(1), Color.rgb(125, 55, 235));
        card.setBackground(fundoCard);
        card.setElevation(dp(8));

        TextView titulo = texto(
                "＋  CRIAR GRUPO HAVE",
                17,
                Color.WHITE
        );
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titulo.setGravity(Gravity.CENTER);

        TextView subtitulo = texto(
                "Crie uma sala e assista junto. Cada pessoa usa sua própria conta.",
                12,
                Color.rgb(165, 180, 215)
        );
        subtitulo.setGravity(Gravity.CENTER);
        subtitulo.setPadding(0, dp(5), 0, 0);

        card.addView(
                titulo,
                new LinearLayout.LayoutParams(-1, dp(32))
        );

        card.addView(
                subtitulo,
                new LinearLayout.LayoutParams(-1, dp(38))
        );

        card.setOnClickListener(v -> abrirCriarGrupoHave());

        LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(-1, dp(100));
        cp.setMargins(0, dp(2), 0, dp(18));

        pai.addView(card, cp);
    }

    void criarBotaoCriarSala(LinearLayout pai) {
        TextView botao = texto(
                "＋  CRIAR SALA",
                17,
                Color.rgb(255, 255, 255)
        );

        botao.setGravity(Gravity.CENTER);
        botao.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        botao.setLetterSpacing(0.06f);

        GradientDrawable fundoBotao = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.rgb(35, 10, 65),
                        Color.rgb(8, 25, 55)
                }
        );
        fundoBotao.setCornerRadius(dp(18));
        fundoBotao.setStroke(dp(2), Color.rgb(160, 75, 255));
        botao.setBackground(fundoBotao);
        botao.setElevation(dp(5));
        botao.setPadding(dp(12), dp(8), dp(12), dp(8));
        botao.setOnClickListener(v -> abrirNetflixComBalao());

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, dp(62));
        params.setMargins(0, dp(16), 0, dp(18));
        pai.addView(botao, params);
    }

    void abrirCriarGrupoHave() {
        final android.app.Dialog dialog =
                new android.app.Dialog(this);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(22), dp(20), dp(22), dp(22));

        GradientDrawable fundoDialog = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(8, 8, 20),
                        Color.rgb(18, 8, 35)
                }
        );
        fundoDialog.setCornerRadius(dp(28));
        fundoDialog.setStroke(dp(1), Color.rgb(130, 55, 235));
        raiz.setBackground(fundoDialog);

        TextView titulo = texto(
                "CRIAR GRUPO HAVE",
                22,
                Color.WHITE
        );
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView descricao = texto(
                "Monte sua sala e convide pessoas para assistir juntas.",
                13,
                Color.rgb(165, 180, 215)
        );
        descricao.setPadding(0, dp(5), 0, dp(18));

        raiz.addView(titulo);
        raiz.addView(descricao);

        TextView servico = texto(
                "ESCOLHA O SERVIÇO",
                11,
                Color.rgb(190, 105, 255)
        );
        servico.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        raiz.addView(servico);

        HorizontalScrollView appsScroll =
                new HorizontalScrollView(this);
        appsScroll.setHorizontalScrollBarEnabled(false);

        LinearLayout appsLinha =
                new LinearLayout(this);
        appsLinha.setOrientation(LinearLayout.HORIZONTAL);

        final String[] nomes = {
                "Netflix",
                "Prime Video",
                "Disney+",
                "Max",
                "Globoplay"
        };

        final String[] logos = {
                "netflix",
                "primevideo",
                "disneyplus",
                "hbomax",
                "globoplay"
        };

        final TextView[] selecionado = {null};
        final String[] servicoSelecionado = {"Netflix"};

        for (int i = 0; i < nomes.length; i++) {
            TextView item = texto(
                    nomes[i],
                    12,
                    Color.WHITE
            );
            item.setGravity(Gravity.CENTER);
            item.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            GradientDrawable normal = new GradientDrawable();
            normal.setColor(Color.rgb(13, 15, 30));
            normal.setCornerRadius(dp(16));
            normal.setStroke(dp(1), Color.rgb(65, 70, 105));
            item.setBackground(normal);

            final int pos = i;

            item.setOnClickListener(v -> {
                servicoSelecionado[0] = nomes[pos];

                if (selecionado[0] != null) {
                    GradientDrawable antigo =
                            new GradientDrawable();
                    antigo.setColor(Color.rgb(13, 15, 30));
                    antigo.setCornerRadius(dp(16));
                    antigo.setStroke(
                            dp(1),
                            Color.rgb(65, 70, 105)
                    );
                    selecionado[0].setBackground(antigo);
                }

                GradientDrawable ativo =
                        new GradientDrawable();
                ativo.setColor(Color.rgb(30, 12, 55));
                ativo.setCornerRadius(dp(16));
                ativo.setStroke(
                        dp(2),
                        Color.rgb(0, 220, 255)
                );
                item.setBackground(ativo);

                selecionado[0] = item;
            });

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            dp(125),
                            dp(58)
                    );
            lp.setMargins(0, dp(10), dp(8), dp(10));

            appsLinha.addView(item, lp);
        }

        appsScroll.addView(appsLinha);
        raiz.addView(
                appsScroll,
                new LinearLayout.LayoutParams(-1, dp(80))
        );

        EditText nomeSala = new EditText(this);
        nomeSala.setHint("Nome da sala");
        nomeSala.setHintTextColor(Color.rgb(105, 115, 145));
        nomeSala.setTextColor(Color.WHITE);
        nomeSala.setSingleLine(true);

        GradientDrawable campo = new GradientDrawable();
        campo.setColor(Color.rgb(10, 12, 27));
        campo.setCornerRadius(dp(15));
        campo.setStroke(dp(1), Color.rgb(55, 65, 100));
        nomeSala.setBackground(campo);
        nomeSala.setPadding(dp(15), 0, dp(15), 0);

        LinearLayout.LayoutParams campoLp =
                new LinearLayout.LayoutParams(-1, dp(54));
        campoLp.setMargins(0, dp(10), 0, dp(10));
        raiz.addView(nomeSala, campoLp);

        TextView regra = texto(
                "🔒 Cada participante entra com a própria conta. O HAVE não solicita nem armazena senhas.",
                12,
                Color.rgb(155, 170, 205)
        );
        regra.setPadding(0, dp(5), 0, dp(15));
        raiz.addView(regra);

        TextView criar = texto(
                "CRIAR SALA",
                14,
                Color.WHITE
        );
        criar.setGravity(Gravity.CENTER);
        criar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        GradientDrawable botao = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.rgb(120, 0, 255),
                        Color.rgb(0, 125, 255)
                }
        );
        botao.setCornerRadius(dp(30));
        botao.setStroke(dp(1), Color.rgb(0, 220, 255));
        criar.setBackground(botao);

        criar.setOnClickListener(v -> {
            String nome = nomeSala.getText().toString().trim();

            if (nome.isEmpty()) {
                nomeSala.setError("Digite o nome da sala");
                return;
            }

            FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

            if (usuario == null) {
                Toast.makeText(
                        this,
                        "Faça login novamente para criar uma sala.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            criar.setEnabled(false);
            criar.setText("CRIANDO SALA...");

            String meuUid = usuario.getUid();
            String servicoAtual = servicoSelecionado[0];

            Map<String, Object> grupo = new HashMap<>();
            grupo.put("nome", nome);
            grupo.put("descricao", "Sala HAVE • " + servicoAtual);
            grupo.put("administradorUid", meuUid);
            grupo.put("participantes", java.util.Collections.singletonList(meuUid));
            grupo.put("servicoHave", servicoAtual);
            grupo.put("tipo", "HAVE");
            grupo.put("publico", true);
            grupo.put("criadoEm", FieldValue.serverTimestamp());

            FirebaseFirestore.getInstance()
                    .collection("grupos")
                    .add(grupo)
                    .addOnSuccessListener(documento -> {
                        String grupoId = documento.getId();

                        Map<String, Object> dados = new HashMap<>();
                        dados.put("grupoId", grupoId);
                        dados.put("nome", nome);
                        dados.put("nomeGrupo", nome);
                        dados.put("servicoHave", servicoAtual);
                        dados.put("administradorUid", meuUid);
                        dados.put("participantes",
                                java.util.Collections.singletonList(meuUid));
                        dados.put("criadoEm", FieldValue.serverTimestamp());

                        documento.set(dados, SetOptions.merge())
                                .addOnSuccessListener(v2 -> {
                                    dialog.dismiss();

                                String pacote = "com.netflix.mediaclient";
                                String site = "https://www.netflix.com";

                                if ("Prime Video".equals(servicoAtual)) {
                                    pacote = "com.amazon.avod.thirdpartyclient";
                                    site = "https://www.primevideo.com";
                                } else if ("Disney+".equals(servicoAtual)) {
                                    pacote = "com.disney.disneyplus";
                                    site = "https://www.disneyplus.com";
                                } else if ("Max".equals(servicoAtual)) {
                                    pacote = "com.hbo.hbonow";
                                    site = "https://www.max.com";
                                } else if ("Globoplay".equals(servicoAtual)) {
                                    pacote = "com.globo.globoplay";
                                    site = "https://globoplay.globo.com";
                                }

                                Toast.makeText(
                                        this,
                                        "Sala criada! Entrando na sala HAVE...",
                                        Toast.LENGTH_SHORT
                                ).show();

                                Intent salaIntent = new Intent(this, HavePlataformasActivity.class);
                                salaIntent.putExtra("grupoId", grupoId);
                                salaIntent.putExtra("nomeGrupo", nome);
                                salaIntent.putExtra("servicoHave", servicoAtual);
                                startActivity(salaIntent);
                                })
                                .addOnFailureListener(erro -> {
                                    criar.setEnabled(true);
                                    criar.setText("CRIAR SALA");

                                    Toast.makeText(
                                            this,
                                            "Sala criada, mas não foi possível abrir.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                });
                    })
                    .addOnFailureListener(erro -> {
                        criar.setEnabled(true);
                        criar.setText("CRIAR SALA");

                        Toast.makeText(
                                this,
                                "Não foi possível criar a sala.",
                                Toast.LENGTH_LONG
                        ).show();
                    });
        });

        raiz.addView(
                criar,
                new LinearLayout.LayoutParams(-1, dp(54))
        );

        dialog.setContentView(raiz);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
            dialog.getWindow().setLayout(
                    (int)(getResources()
                            .getDisplayMetrics().widthPixels * 0.94f),
                    -2
            );
        }

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int)(getResources()
                            .getDisplayMetrics().widthPixels * 0.94f),
                    -2
            );
        }
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

    void abrirNetflixComBalao() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {

            Toast.makeText(
                    this,
                    "Permita a sobreposição para ativar o balão HAVE.",
                    Toast.LENGTH_LONG
            ).show();

            try {
                Intent permissao = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                );
                startActivity(permissao);
            } catch (Exception e) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            }

            return;
        }

        Intent servico = new Intent(this, HaveOverlayService.class);
        servico.putExtra("grupoId", "");
        servico.putExtra("nomeGrupo", "Sala HAVE");
        servico.putExtra("servicoHave", "Netflix");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(servico);
        } else {
            startService(servico);
        }

        try {
            Intent netflix = getPackageManager()
                    .getLaunchIntentForPackage("com.netflix.mediaclient");

            if (netflix == null) {
                stopService(new Intent(this, HaveOverlayService.class));
                Toast.makeText(
                        this,
                        "O aplicativo oficial da Netflix não foi encontrado. Verifique se está instalado.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            netflix.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(netflix);

        } catch (Exception e) {
            stopService(new Intent(this, HaveOverlayService.class));
            Toast.makeText(
                    this,
                    "Não foi possível abrir o aplicativo Netflix.",
                    Toast.LENGTH_LONG
            ).show();
        }
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
        TextView titulo = texto("HAVE CONNECT  •  SALAS", 19, Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titulo.setPadding(0, dp(20), 0, dp(5));
        pai.addView(titulo);

        TextView subtitulo = texto(
                "Encontre uma sala e participe da experiência.",
                13, Color.rgb(140, 160, 195)
        );
        subtitulo.setPadding(0, 0, 0, dp(14));
        pai.addView(subtitulo);

        TextView avisoLink = texto("🔗 LINK UNIVERSAL • FILME OU SÉRIE", 13, ciano);
        avisoLink.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        avisoLink.setPadding(0, dp(2), 0, dp(8));
        pai.addView(avisoLink);

        campoLinkUniversalHave = new EditText(this);
        campoLinkUniversalHave.setSingleLine(true);
        campoLinkUniversalHave.setTextSize(14);
        campoLinkUniversalHave.setHint("Cole aqui o link direto do título");
        campoLinkUniversalHave.setHintTextColor(Color.rgb(125, 145, 175));
        campoLinkUniversalHave.setTextColor(Color.WHITE);
        campoLinkUniversalHave.setPadding(dp(14), dp(10), dp(14), dp(10));
        campoLinkUniversalHave.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_URI);
        GradientDrawable fundoLinkUniversal = fundo(Color.rgb(8, 14, 28), 14);
        fundoLinkUniversal.setStroke(dp(1), ciano);
        campoLinkUniversalHave.setBackground(fundoLinkUniversal);
        LinearLayout.LayoutParams linkUniversalLp =
                new LinearLayout.LayoutParams(-1, dp(52));
        linkUniversalLp.setMargins(0, 0, 0, dp(8));
        pai.addView(campoLinkUniversalHave, linkUniversalLp);

        TextView dicaLink = texto(
                "Cole o link e toque na sala desejada. O anfitrião precisa salvar o link para compartilhá-lo.",
                11, Color.rgb(145, 165, 195));
        dicaLink.setPadding(dp(2), 0, dp(2), dp(12));
        pai.addView(dicaLink);


        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        pai.addView(lista);

        FirebaseFirestore.getInstance()
                .collection("grupos")
                .whereEqualTo("tipo", "HAVE")
                .whereEqualTo("publico", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    lista.removeAllViews();

                    if (snapshot.isEmpty()) {
                        TextView vazio = texto(
                                "Nenhuma sala pública disponível agora.",
                                13, Color.rgb(140, 155, 195)
                        );
                        vazio.setPadding(dp(4), dp(12), dp(4), dp(18));
                        lista.addView(vazio);
                        return;
                    }

                    java.util.List<com.google.firebase.firestore.DocumentSnapshot> salas =
                            new java.util.ArrayList<>(snapshot.getDocuments());

                    com.google.firebase.auth.FirebaseUser usuarioAtual =
                            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                    String uidAtual = usuarioAtual == null ? null : usuarioAtual.getUid();

                    salas.sort((a, b) -> {
                        Object pa = a.get("participantes");
                        Object pb = b.get("participantes");

                        boolean participaA = uidAtual != null
                                && pa instanceof java.util.List
                                && ((java.util.List<?>) pa).contains(uidAtual);
                        boolean participaB = uidAtual != null
                                && pb instanceof java.util.List
                                && ((java.util.List<?>) pb).contains(uidAtual);

                        return Boolean.compare(participaB, participaA);
                    });

                    for (com.google.firebase.firestore.DocumentSnapshot doc : salas) {
                        String grupoId = doc.getId();
                        String nome = doc.getString("nome");

                        if (nome == null || nome.trim().isEmpty()) {
                            nome = doc.getString("nomeGrupo");
                        }
                        if (nome == null || nome.trim().isEmpty()) {
                            nome = "Sala HAVE";
                        }

                        String servico = doc.getString("servicoHave");
                        if (servico == null || servico.trim().isEmpty()) {
                            servico = "Streaming";
                        }

                        Object participantesObj = doc.get("participantes");
                        int quantidade = 0;
                        if (participantesObj instanceof java.util.List) {
                            quantidade = ((java.util.List<?>) participantesObj).size();
                        }

                        int cor = Color.rgb(125, 55, 235);
                        String simbolo = "▶";

                        if ("Netflix".equalsIgnoreCase(servico)) {
                            cor = Color.rgb(220, 35, 55);
                            simbolo = "N";
                        } else if ("Prime Video".equalsIgnoreCase(servico)) {
                            cor = Color.rgb(35, 125, 245);
                            simbolo = "P";
                        } else if ("Disney+".equalsIgnoreCase(servico)) {
                            cor = Color.rgb(65, 90, 210);
                            simbolo = "D+";
                        } else if ("Max".equalsIgnoreCase(servico)) {
                            cor = Color.rgb(90, 75, 220);
                            simbolo = "M";
                        } else if ("Globoplay".equalsIgnoreCase(servico)) {
                            cor = Color.rgb(190, 45, 175);
                            simbolo = "G";
                        }

                        adicionarSala(
                                lista, simbolo, nome, servico,
                                cor, grupoId, servico, quantidade
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    lista.removeAllViews();
                    TextView erro = texto(
                            "Não foi possível carregar as salas. Tente novamente.",
                            13, Color.rgb(255, 90, 120)
                    );
                    erro.setPadding(0, dp(10), 0, dp(18));
                    lista.addView(erro);
                });
    }

    void adicionarSala(
            LinearLayout pai,
            String imagem,
            String nome,
            String servico,
            int cor,
            String grupoId,
            String servicoHave,
            int quantidade
    ) {
        LinearLayout sala = new LinearLayout(this);
        sala.setOrientation(LinearLayout.VERTICAL);
        sala.setPadding(dp(14), dp(14), dp(14), dp(14));

        GradientDrawable fundoSala = fundo(Color.rgb(8, 12, 29), 20);
        fundoSala.setStroke(dp(1), Color.rgb(45, 65, 105));
        sala.setBackground(fundoSala);
        sala.setElevation(dp(3));

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        TextView poster = texto(imagem, 23, Color.WHITE);
        poster.setGravity(Gravity.CENTER);
        poster.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        poster.setBackground(fundo(cor, 13));
        topo.addView(poster, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout identificacao = new LinearLayout(this);
        identificacao.setOrientation(LinearLayout.VERTICAL);
        identificacao.setPadding(dp(11), 0, 0, 0);

        TextView nomeServico = texto(servico.toUpperCase(java.util.Locale.ROOT),
                10, Color.rgb(0, 220, 255));
        nomeServico.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        identificacao.addView(nomeServico);

        TextView nomeView = texto(nome, 17, Color.WHITE);
        nomeView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        nomeView.setMaxLines(2);
        identificacao.addView(nomeView);

        topo.addView(identificacao,
                new LinearLayout.LayoutParams(0, -2, 1));

        TextView publica = texto("PÚBLICA", 9, Color.rgb(100, 255, 180));
        publica.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        publica.setPadding(dp(8), dp(5), dp(8), dp(5));
        GradientDrawable fundoPublica = fundo(Color.rgb(10, 45, 39), 20);
        fundoPublica.setStroke(dp(1), Color.rgb(30, 120, 90));
        publica.setBackground(fundoPublica);
        topo.addView(publica);

        sala.addView(topo);

        View separador = new View(this);
        separador.setBackgroundColor(Color.rgb(30, 43, 68));
        LinearLayout.LayoutParams separadorLp =
                new LinearLayout.LayoutParams(-1, dp(1));
        separadorLp.setMargins(0, dp(13), 0, dp(11));
        sala.addView(separador, separadorLp);

        LinearLayout rodape = new LinearLayout(this);
        rodape.setGravity(Gravity.CENTER_VERTICAL);

        TextView membros = texto(
                "●  " + quantidade +
                        (quantidade == 1 ? " participante" : " participantes"),
                12, Color.rgb(155, 180, 215)
        );
        rodape.addView(membros,
                new LinearLayout.LayoutParams(0, -2, 1));

        TextView entrar = texto("ENTRAR NA SALA  ›", 11, Color.WHITE);
        entrar.setGravity(Gravity.CENTER);
        entrar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        entrar.setPadding(dp(13), 0, dp(13), 0);

        GradientDrawable fundoEntrar = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.rgb(100, 35, 220),
                        Color.rgb(0, 115, 210)
                }
        );
        fundoEntrar.setCornerRadius(dp(25));
        fundoEntrar.setStroke(dp(1), Color.rgb(0, 210, 255));
        entrar.setBackground(fundoEntrar);

        rodape.addView(entrar,
                new LinearLayout.LayoutParams(-2, dp(42)));
        sala.addView(rodape);

        View.OnClickListener entrarNaSala = v -> {
            Intent intent = new Intent(this, HavePlataformasActivity.class);
            intent.putExtra("grupoId", grupoId);
            intent.putExtra("nomeGrupo", nome);
            intent.putExtra("servicoHave", servicoHave);
                      String linkUniversal = campoLinkUniversalHave == null
                              ? "" : campoLinkUniversalHave.getText().toString().trim();
                      intent.putExtra("linkUniversalHave", linkUniversal);
            startActivity(intent);
        };

        sala.setOnClickListener(entrarNaSala);
        entrar.setOnClickListener(entrarNaSala);

        LinearLayout.LayoutParams cardLp =
                new LinearLayout.LayoutParams(-1, -2);
        cardLp.setMargins(0, 0, 0, dp(12));
        pai.addView(sala, cardLp);
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

    void criarPessoasHave(LinearLayout pai) {

        TextView titulo = texto(
                "👥  Pessoas do HAVE",
                21,
                Color.WHITE
        );

        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titulo.setPadding(0, dp(18), 0, dp(6));
        pai.addView(titulo);

        TextView subtitulo = texto(
                "Veja quem está no HAVE e encontre novas pessoas.",
                14,
                Color.rgb(140, 155, 195)
        );
        subtitulo.setPadding(0, 0, 0, dp(12));
        pai.addView(subtitulo);

        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        pai.addView(lista);

        FirebaseFirestore.getInstance()
                .collection("have_perfis_publicos")
                .orderBy("atualizadoEm", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        TextView vazio = texto(
                                "Nenhuma pessoa disponível no HAVE ainda.",
                                14,
                                Color.rgb(140, 155, 195)
                        );
                        vazio.setPadding(0, dp(8), 0, dp(18));
                        lista.addView(vazio);
                        return;
                    }

                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {

                        String uid = doc.getId();
                        String nome = doc.getString("nome");
                        String haveId = doc.getString("haveId");
                        String status = doc.getString("status");

                        if (nome == null || nome.trim().isEmpty()) {
                            nome = "Usuário HAVE";
                        }

                        if (haveId == null || haveId.trim().isEmpty()) {
                            haveId = "HAVE-SEM-ID";
                        }

                        boolean online = "online".equalsIgnoreCase(status);

                        LinearLayout pessoa = new LinearLayout(this);
                        pessoa.setGravity(Gravity.CENTER_VERTICAL);
                        pessoa.setPadding(
                                dp(12),
                                dp(10),
                                dp(12),
                                dp(10)
                        );

                        GradientDrawable fundoPessoa = fundo(
                                Color.rgb(8, 11, 30),
                                20
                        );

                        fundoPessoa.setStroke(
                                dp(1),
                                online
                                        ? Color.rgb(35, 120, 100)
                                        : Color.rgb(55, 60, 90)
                        );

                        pessoa.setBackground(fundoPessoa);

                        TextView avatar = texto(
                                "●",
                                28,
                                online
                                        ? Color.rgb(40, 255, 145)
                                        : Color.rgb(100, 115, 145)
                        );

                        avatar.setGravity(Gravity.CENTER);

                        pessoa.addView(
                                avatar,
                                new LinearLayout.LayoutParams(
                                        dp(48),
                                        dp(58)
                                )
                        );

                        LinearLayout info = new LinearLayout(this);
                        info.setOrientation(LinearLayout.VERTICAL);
                        info.setPadding(dp(10), 0, dp(8), 0);

                        TextView nomeView = texto(
                                nome,
                                16,
                                Color.WHITE
                        );

                        nomeView.setTypeface(
                                Typeface.DEFAULT,
                                Typeface.BOLD
                        );

                        TextView idView = texto(
                                haveId,
                                12,
                                Color.rgb(80, 210, 255)
                        );

                        TextView statusView = texto(
                                online
                                        ? "● ONLINE"
                                        : "○ OFFLINE",
                                11,
                                online
                                        ? Color.rgb(40, 255, 145)
                                        : Color.rgb(130, 145, 170)
                        );

                        info.addView(nomeView);
                        info.addView(idView);
                        info.addView(statusView);

                        pessoa.addView(
                                info,
                                new LinearLayout.LayoutParams(
                                        0,
                                        -2,
                                        1
                                )
                        );

                        TextView acao = texto(
                                "VER",
                                11,
                                Color.WHITE
                        );

                        acao.setGravity(Gravity.CENTER);

                        GradientDrawable acaoBg = fundo(
                                Color.rgb(20, 35, 65),
                                22
                        );

                        acaoBg.setStroke(
                                dp(1),
                                Color.rgb(0, 210, 255)
                        );

                        acao.setBackground(acaoBg);

                        pessoa.addView(
                                acao,
                                new LinearLayout.LayoutParams(
                                        dp(60),
                                        dp(40)
                                )
                        );

                        final String nomeFinal = nome;
                        final String haveIdFinal = haveId;
                        final String uidFinal = uid;

                        View.OnClickListener abrirPessoa = v -> {

                            Toast.makeText(
                                    this,
                                    nomeFinal + " • " + haveIdFinal,
                                    Toast.LENGTH_SHORT
                            ).show();

                            try {
                                Intent intent = new Intent(
                                        this,
                                        SocialActivity.class
                                );

                                intent.putExtra("uidUsuario", uidFinal);
                                intent.putExtra("haveId", haveIdFinal);
                                intent.putExtra("nomeUsuario", nomeFinal);

                                startActivity(intent);

                            } catch (Exception ignored) {
                                Toast.makeText(
                                        this,
                                        "Perfil disponível pelo HAVE ID: " + haveIdFinal,
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        };

                        pessoa.setOnClickListener(abrirPessoa);
                        acao.setOnClickListener(abrirPessoa);

                        LinearLayout.LayoutParams pessoaParams =
                                new LinearLayout.LayoutParams(
                                        -1,
                                        dp(80)
                                );

                        pessoaParams.setMargins(
                                0,
                                0,
                                0,
                                dp(8)
                        );

                        lista.addView(pessoa, pessoaParams);
                    }
                })
                .addOnFailureListener(e -> {

                    TextView erro = texto(
                            "Não foi possível carregar as pessoas do HAVE.",
                            14,
                            Color.rgb(255, 80, 110)
                    );

                    erro.setPadding(0, dp(8), 0, dp(18));
                    lista.addView(erro);
                });
    }


    void abrirTelaAmigos() {
        if (modoConviteHave) {
            abrirConviteAmigosHave();
            return;
        }
        telaAmigosAberta = true;
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(Color.rgb(3, 4, 15));
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(dp(20), dp(22), dp(20), dp(24));
        criarAmigos(conteudo);
        scroll.addView(conteudo);
        raiz.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        criarNavegacao(raiz);
        setContentView(raiz);
    }

    void criarAmigos(LinearLayout pai) {
        TextView titulo = texto("♧  PESSOAS HAVE", 21, Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titulo.setPadding(0, dp(14), 0, dp(8));
        pai.addView(titulo);
        pai.addView(texto("Pessoas cadastradas no HAVE", 14,
                Color.rgb(140, 155, 195)));

        TextView onlineTitulo = texto("🟢  ONLINE AGORA", 16, Color.WHITE);
        onlineTitulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        onlineTitulo.setPadding(0, dp(22), 0, dp(10));
        pai.addView(onlineTitulo);
        LinearLayout online = new LinearLayout(this);
        online.setOrientation(LinearLayout.VERTICAL);
        pai.addView(online);

        TextView offlineTitulo = texto("⚫  OFFLINE", 16, Color.WHITE);
        offlineTitulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        offlineTitulo.setPadding(0, dp(22), 0, dp(10));
        pai.addView(offlineTitulo);
        LinearLayout offline = new LinearLayout(this);
        offline.setOrientation(LinearLayout.VERTICAL);
        pai.addView(offline);

        online.addView(texto("Carregando pessoas...", 14,
                Color.rgb(140, 155, 195)));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("have_perfis_publicos").get()
          .addOnSuccessListener(resultado -> {
            online.removeAllViews();
            int total = 0;
            String meuUid = FirebaseAuth.getInstance().getUid();
            for (DocumentSnapshot d : resultado.getDocuments()) {
                if (meuUid != null && meuUid.equals(d.getId())) continue;
                total++;
            }
            if (total == 0) {
                online.addView(texto("Nenhuma outra pessoa cadastrada.",
                        14, Color.rgb(140, 155, 195)));
                return;
            }
            final int[] pendentes = {total};
            for (DocumentSnapshot d : resultado.getDocuments()) {
                final String uid = d.getId();
                if (meuUid != null && meuUid.equals(uid)) continue;
                String n = d.getString("nome");
                final String nome = n == null || n.trim().isEmpty()
                        ? "Pessoa HAVE" : n;
                String h = d.getString("haveId");
                final String id = h == null ? "HAVE" : h;

                db.collection("perfis_publicos").document(uid).get()
                  .addOnSuccessListener(p -> {
                    Object t = p.get("ultimoAcesso");
                    boolean ativo = false;
                    if (Boolean.TRUE.equals(p.getBoolean("online"))
                            && t instanceof com.google.firebase.Timestamp) {
                        long idade = System.currentTimeMillis()
                            - ((com.google.firebase.Timestamp)t).toDate().getTime();
                        ativo = idade >= 0 && idade <= 90000;
                    }
                    adicionarPessoaHave(ativo ? online : offline,
                            nome, id, ativo);
                    pendentes[0]--;
                    if (pendentes[0] == 0) {
                        if (online.getChildCount() == 0)
                            online.addView(texto("Ninguém online agora.", 14,
                                    Color.rgb(140, 155, 195)));
                        if (offline.getChildCount() == 0)
                            offline.addView(texto("Nenhuma pessoa offline.", 14,
                                    Color.rgb(140, 155, 195)));
                    }
                  })
                  .addOnFailureListener(e -> {
                    adicionarPessoaHave(offline, nome, id, false);
                    pendentes[0]--;
                  });
            }
          })
          .addOnFailureListener(e -> {
            online.removeAllViews();
            online.addView(texto("Falha ao carregar. Verifique a conexão.",
                    14, Color.rgb(255, 100, 120)));
          });

        Button buscar = pequenoBotao("⌕  BUSCAR PESSOA POR ID HAVE",
                Color.rgb(0, 210, 255));
        LinearLayout.LayoutParams buscaParams =
                new LinearLayout.LayoutParams(-1, dp(48));
        buscaParams.setMargins(0, dp(22), 0, dp(8));
        pai.addView(buscar, buscaParams);
        buscar.setOnClickListener(v -> abrirBuscaHave());
    }

    void adicionarPessoaHave(LinearLayout lista, String nome,
                             String haveId, boolean online) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable fundoPessoa = fundo(Color.rgb(9, 13, 30), 16);
        fundoPessoa.setStroke(dp(1), online
                ? Color.rgb(40, 255, 145) : Color.rgb(65, 75, 105));
        card.setBackground(fundoPessoa);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(9));
        lista.addView(card, params);
        card.addView(texto("●", 18, online
                ? Color.rgb(40, 255, 145) : Color.rgb(105, 115, 135)),
                new LinearLayout.LayoutParams(dp(30), -2));
        LinearLayout coluna = new LinearLayout(this);
        coluna.setOrientation(LinearLayout.VERTICAL);
        TextView nomeView = texto(nome, 15, Color.WHITE);
        nomeView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        coluna.addView(nomeView);
        coluna.addView(texto(haveId + (online ? " • ONLINE" : " • OFFLINE"),
                12, online ? Color.rgb(40, 255, 145)
                           : Color.rgb(140, 155, 195)));
        card.addView(coluna, new LinearLayout.LayoutParams(0, -2, 1));
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

        GradientDrawable fundoCampo = fundo(
                Color.rgb(10, 14, 32),
                16
        );
        fundoCampo.setStroke(
                dp(1),
                Color.rgb(0, 210, 255)
        );
        campo.setBackground(fundoCampo);

        raiz.addView(titulo);
        raiz.addView(subtitulo);
        raiz.addView(
                campo,
                new LinearLayout.LayoutParams(-1, dp(54))
        );

        Button pesquisar = new Button(this);
        pesquisar.setText("🔎 PESQUISAR");
        pesquisar.setTextColor(Color.WHITE);
        pesquisar.setTextSize(14);
        pesquisar.setAllCaps(false);

        GradientDrawable fundoBotao = fundo(
                Color.rgb(35, 18, 65),
                16
        );
        fundoBotao.setStroke(
                dp(1),
                Color.rgb(145, 45, 255)
        );
        pesquisar.setBackground(fundoBotao);

        LinearLayout.LayoutParams botaoParams =
                new LinearLayout.LayoutParams(-1, dp(54));
        botaoParams.topMargin = dp(14);
        raiz.addView(pesquisar, botaoParams);

        LinearLayout resultadoArea = new LinearLayout(this);
        resultadoArea.setOrientation(LinearLayout.VERTICAL);
        resultadoArea.setPadding(dp(16), dp(18), dp(16), dp(18));
        resultadoArea.setVisibility(View.GONE);

        GradientDrawable resultadoBg = fundo(
                Color.rgb(8, 12, 28),
                18
        );
        resultadoBg.setStroke(
                dp(1),
                Color.rgb(55, 75, 110)
        );
        resultadoArea.setBackground(resultadoBg);

        LinearLayout.LayoutParams resultadoParams =
                new LinearLayout.LayoutParams(-1, -2);
        resultadoParams.topMargin = dp(18);

        TextView resultado = texto(
                "",
                15,
                Color.rgb(220, 225, 240)
        );

        resultadoArea.addView(resultado);

        Button adicionar = new Button(this);
        adicionar.setText("➕ ADICIONAR AMIGO");
        adicionar.setTextColor(Color.WHITE);
        adicionar.setTextSize(14);
        adicionar.setAllCaps(false);
        adicionar.setVisibility(View.GONE);

        GradientDrawable adicionarBg = fundo(
                Color.rgb(25, 55, 70),
                16
        );
        adicionarBg.setStroke(
                dp(1),
                Color.rgb(40, 255, 145)
        );
        adicionar.setBackground(adicionarBg);

        LinearLayout.LayoutParams adicionarParams =
                new LinearLayout.LayoutParams(-1, dp(52));
        adicionarParams.topMargin = dp(14);

        resultadoArea.addView(adicionar, adicionarParams);
        raiz.addView(resultadoArea, resultadoParams);

        TextView fechar = texto(
                "← VOLTAR",
                14,
                Color.rgb(0, 210, 255)
        );
        fechar.setGravity(Gravity.CENTER);
        fechar.setPadding(0, dp(18), 0, dp(10));

        raiz.addView(
                fechar,
                new LinearLayout.LayoutParams(-1, dp(54))
        );

        setContentView(raiz);

        final String[] uidEncontrado = {""};
        final String[] nomeEncontrado = {""};
        final String[] haveIdEncontrado = {""};

        pesquisar.setOnClickListener(v -> {
            String id = campo.getText()
                    .toString()
                    .trim()
                    .toUpperCase(Locale.US);

            if (id.isEmpty()) {
                campo.setError("Digite um ID HAVE");
                campo.requestFocus();
                return;
            }

            resultadoArea.setVisibility(View.VISIBLE);
            adicionar.setVisibility(View.GONE);

            resultado.setText("🔎 Procurando " + id + "...");

            FirebaseFirestore.getInstance()
                    .collection("have_perfis_publicos")
                    .whereEqualTo("haveId", id)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        if (snapshot.isEmpty()) {
                            uidEncontrado[0] = "";
                            resultado.setText(
                                    "❌ Usuário não encontrado.\n\n" +
                                    "Confira o ID HAVE e tente novamente."
                            );
                            return;
                        }

                        com.google.firebase.firestore.DocumentSnapshot doc =
                                snapshot.getDocuments().get(0);

                        String uid = doc.getId();
                        String nome = doc.getString("nome");
                        String haveId = doc.getString("haveId");
                        String status = doc.getString("status");

                        if (nome == null || nome.isEmpty()) {
                            nome = "Usuário HAVE";
                        }

                        if (haveId == null || haveId.isEmpty()) {
                            haveId = id;
                        }

                        if (status == null || status.isEmpty()) {
                            status = "offline";
                        }

                        uidEncontrado[0] = uid;
                        nomeEncontrado[0] = nome;
                        haveIdEncontrado[0] = haveId;

                        FirebaseUser atual =
                                FirebaseAuth.getInstance().getCurrentUser();

                        if (atual != null &&
                                atual.getUid().equals(uid)) {

                            resultado.setText(
                                    "🟣 ESTE É SEU PERFIL\n\n" +
                                    "👤 " + nome + "\n" +
                                    "🆔 " + haveId + "\n" +
                                    "● " + status.toUpperCase(Locale.US)
                            );

                            adicionar.setVisibility(View.GONE);
                            return;
                        }

                        resultado.setText(
                                "● PERFIL ENCONTRADO\n\n" +
                                "👤 " + nome + "\n" +
                                "🆔 " + haveId + "\n" +
                                "● " + status.toUpperCase(Locale.US)
                        );

                        adicionar.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e -> {
                        uidEncontrado[0] = "";

                        resultado.setText(
                                "⚠️ Não foi possível realizar a busca.\n\n" +
                                "Verifique sua conexão e tente novamente."
                        );
                    });
        });

        adicionar.setOnClickListener(v -> {

            if (uidEncontrado[0].isEmpty()) {
                Toast.makeText(
                        this,
                        "Pesquise um usuário HAVE primeiro.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Toast.makeText(
                    this,
                    "Usuário encontrado. Próximo passo: criar amizade HAVE.",
                    Toast.LENGTH_SHORT
            ).show();
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



    private void abrirConviteAmigosHave() {
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(18), dp(18), dp(18), dp(18));
        raiz.setBackgroundColor(Color.rgb(3, 4, 15));

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        TextView voltar = texto("‹", 38, Color.WHITE);
        voltar.setGravity(Gravity.CENTER);
        voltar.setOnClickListener(v -> {
            if (modoConviteHave && !salaConviteId.isEmpty()) {
                finish();
            } else {
                montarHome();
            }
        });

        topo.addView(voltar, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout titulos = new LinearLayout(this);
        titulos.setOrientation(LinearLayout.VERTICAL);

        TextView titulo = texto("CONVIDAR AMIGOS", 21, Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);

        TextView subtitulo = texto(
                "AMIGOS HAVE • " + salaConviteServico,
                12,
                Color.rgb(0, 220, 255)
        );

        titulos.addView(titulo);
        titulos.addView(subtitulo);

        topo.addView(titulos, new LinearLayout.LayoutParams(0, -2, 1));
        raiz.addView(topo);

        LinearLayout sala = new LinearLayout(this);
        sala.setOrientation(LinearLayout.VERTICAL);
        sala.setPadding(dp(16), dp(14), dp(16), dp(14));

        GradientDrawable fundoSala = fundo(Color.rgb(10, 17, 35), 18);
        fundoSala.setStroke(dp(1), Color.rgb(145, 40, 255));
        sala.setBackground(fundoSala);

        TextView salaTitulo = texto("● SALA HAVE", 13, Color.rgb(190, 100, 255));
        salaTitulo.setTypeface(Typeface.DEFAULT_BOLD);

        TextView salaNome = texto(
                salaConviteNome,
                17,
                Color.WHITE
        );
        salaNome.setTypeface(Typeface.DEFAULT_BOLD);

        TextView salaServico = texto(
                "Serviço: " + salaConviteServico,
                12,
                Color.rgb(155, 170, 200)
        );

        sala.addView(salaTitulo);
        sala.addView(salaNome);
        sala.addView(salaServico);

        LinearLayout.LayoutParams salaParams =
                new LinearLayout.LayoutParams(-1, -2);
        salaParams.setMargins(0, dp(18), 0, dp(18));
        raiz.addView(sala, salaParams);

        TextView instrucao = texto(
                "Selecione amigos HAVE para convidar para esta sala.",
                14,
                Color.rgb(190, 200, 220)
        );
        instrucao.setPadding(dp(2), 0, dp(2), dp(12));
        raiz.addView(instrucao);

        TextView vazio = texto(
                "Nenhum amigo HAVE carregado ainda.",
                14,
                Color.rgb(130, 145, 175)
        );
        vazio.setGravity(Gravity.CENTER);
        vazio.setPadding(0, dp(35), 0, dp(35));
        raiz.addView(
                vazio,
                new LinearLayout.LayoutParams(-1, -2)
        );

        Button voltarSala = pequenoBotao(
                "←  VOLTAR PARA SALA",
                Color.rgb(0, 220, 255)
        );
        voltarSala.setOnClickListener(v -> finish());

        LinearLayout.LayoutParams voltarParams =
                new LinearLayout.LayoutParams(-1, dp(48));
        voltarParams.topMargin = dp(12);

        raiz.addView(voltarSala, voltarParams);

        setContentView(raiz);
    }

    void criarNavegacao(LinearLayout raiz) {
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(5), dp(8), dp(5));
        GradientDrawable bg = fundo(Color.rgb(5, 7, 22), 22);
        bg.setStroke(dp(1), Color.rgb(45, 50, 100));
        nav.setBackground(bg);
        nav.setElevation(dp(12));
        adicionarNav(nav, "⌂", "HOME", !telaAmigosAberta);
        adicionarNav(nav, "♧", "AMIGOS", telaAmigosAberta);
        adicionarNav(nav, "●", "PERFIL", false);
        nav.getChildAt(0).setOnClickListener(v -> montarHome());
        nav.getChildAt(1).setOnClickListener(v -> {
            if (modoConviteHave) abrirConviteAmigosHave();
            else abrirTelaAmigos();
        });
        nav.getChildAt(2).setOnClickListener(v -> abrirPerfilHave());
        raiz.addView(nav, new LinearLayout.LayoutParams(-1, dp(66)));
    }

    void adicionarNav(
            LinearLayout nav, String icone, String nome, boolean ativo
    ) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(8), dp(3), dp(8), dp(3));

        if (ativo) {
            GradientDrawable selecionado = fundo(Color.rgb(8, 28, 48), 15);
            selecionado.setStroke(dp(1), Color.rgb(0, 220, 255));
            item.setBackground(selecionado);
            item.setElevation(dp(5));
        }

        View desenho = new IconeNav(this, nome, ativo);
        LinearLayout.LayoutParams ip =
                new LinearLayout.LayoutParams(dp(27), dp(27));
        ip.bottomMargin = dp(3);
        item.addView(desenho, ip);

        TextView rotulo = texto(nome, 10,
                ativo ? Color.rgb(105, 235, 255)
                      : Color.rgb(140, 155, 180));
        rotulo.setGravity(Gravity.CENTER);
        rotulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        item.addView(rotulo);

        nav.addView(item, new LinearLayout.LayoutParams(0, -1, 1));
    }

    private class IconeNav extends View {
        private final String tipo;
        private final boolean ativo;
        private final android.graphics.Paint pincel =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);

        IconeNav(Activity contexto, String tipo, boolean ativo) {
            super(contexto);
            this.tipo = tipo;
            this.ativo = ativo;
            pincel.setStyle(android.graphics.Paint.Style.STROKE);
            pincel.setStrokeWidth(2f);
            pincel.setStrokeCap(android.graphics.Paint.Cap.ROUND);
            pincel.setStrokeJoin(android.graphics.Paint.Join.ROUND);
            pincel.setColor(ativo ? Color.rgb(0, 220, 255)
                                  : Color.rgb(140, 165, 195));
        }

        @Override
        protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);
            canvas.save();
            canvas.scale(getWidth() / 28f, getHeight() / 28f);

            android.graphics.Path p = new android.graphics.Path();

            if ("HOME".equals(tipo)) {
                p.moveTo(3, 13);
                p.lineTo(14, 4);
                p.lineTo(25, 13);
                p.moveTo(6, 12);
                p.lineTo(6, 24);
                p.lineTo(22, 24);
                p.lineTo(22, 12);
                p.moveTo(11, 24);
                p.lineTo(11, 16);
                p.lineTo(17, 16);
                p.lineTo(17, 24);
                canvas.drawPath(p, pincel);
            } else if ("AMIGOS".equals(tipo)) {
                canvas.drawCircle(10, 9, 3.2f, pincel);
                canvas.drawCircle(19, 10, 2.8f, pincel);
                p.moveTo(3, 23);
                p.cubicTo(3, 16, 6, 14, 10, 14);
                p.cubicTo(14, 14, 17, 17, 17, 23);
                p.moveTo(16, 16);
                p.cubicTo(22, 14, 25, 18, 25, 23);
                canvas.drawPath(p, pincel);
            } else {
                canvas.drawCircle(14, 8, 4, pincel);
                p.moveTo(5, 24);
                p.cubicTo(5, 17, 9, 14, 14, 14);
                p.cubicTo(19, 14, 23, 17, 23, 24);
                canvas.drawPath(p, pincel);
            }

            canvas.restore();
        }
    }
}