package com.danike.ai.nexo;

import com.danike.ai.NexoDiagnostic;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.content.Intent;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class NexoFilmesActivity extends Activity {
    private android.app.ProgressDialog progressoUploadNexo;


    private Uri uriVideoSelecionado;
    private Uri uriPosterSelecionado;
    private Uri uriBannerSelecionado;
    private static final String URL_RENDER_NEXO =
            "https://danikeai-brain.onrender.com/nexo/publicar-filme";

    private boolean publicacaoEmAndamento = false;


    private TextView textoVideoSelecionado;
    private TextView textoPosterSelecionado;
    private TextView textoBannerSelecionado;

    private static final int FUNDO = Color.rgb(3, 5, 15);
    private static final int PAINEL = Color.rgb(7, 14, 29);
    private static final int AZUL = Color.rgb(0, 190, 255);
    private static final int ROSA = Color.rgb(255, 45, 150);
    private static final int BRANCO = Color.WHITE;
    private static final int CINZA = Color.rgb(160, 170, 190);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // NEXO_OWNER_GUARD: validar acesso também ao abrir a Activity diretamente.
        com.google.firebase.auth.FirebaseUser usuario =
                com.google.firebase.auth.FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        boolean proprietario = usuario != null
                && usuario.getEmail() != null
                && "lipesanderson@gmail.com".equalsIgnoreCase(
                        usuario.getEmail().trim()
                );

        if (!proprietario) {
            android.widget.Toast.makeText(
                    this,
                    "Área exclusiva do proprietário.",
                    android.widget.Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        montarTela();
    }

    private void montarTela() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(FUNDO);
        scroll.setFillViewport(true);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(18), dp(22), dp(18), dp(30));

        scroll.addView(raiz, new ScrollView.LayoutParams(-1, -2));

        TextView etiqueta = texto(
                "NEXO  /  CENTRAL DE CONTEÚDO",
                12, AZUL, true
        );
        etiqueta.setPadding(dp(2), dp(2), dp(2), dp(8));
        raiz.addView(etiqueta);

        TextView titulo = texto(
                "Sua biblioteca",
                29, BRANCO, true
        );
        raiz.addView(titulo);

        TextView subtitulo = texto(
                "Gerencie seus filmes e conteúdos em um só lugar.",
                14, CINZA, false
        );
        subtitulo.setPadding(0, dp(5), 0, dp(22));
        raiz.addView(subtitulo);

        LinearLayout abas = new LinearLayout(this);
        abas.setOrientation(LinearLayout.HORIZONTAL);
        abas.setPadding(dp(4), dp(4), dp(4), dp(4));

        android.graphics.drawable.GradientDrawable fundoAbas =
                new android.graphics.drawable.GradientDrawable();
        fundoAbas.setColor(PAINEL);
        fundoAbas.setCornerRadius(dp(16));
        abas.setBackground(fundoAbas);

        Button abaCatalogo = botao("Catálogo", AZUL);
        Button abaPublicar = botao("＋ Publicar", AZUL);
        Button abaGerenciar = botao("Gerenciar", AZUL);

        Button[] botoesAbas = {
                abaCatalogo, abaPublicar, abaGerenciar
        };

        for (Button item : botoesAbas) {
            item.setTextSize(12);
            item.setAllCaps(false);
            item.setMinHeight(dp(44));
            item.setPadding(dp(3), 0, dp(3), 0);
            abas.addView(item, new LinearLayout.LayoutParams(
                    0, dp(44), 1f
            ));
        }

        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams conteudoParams =
                new LinearLayout.LayoutParams(-1, -2);
        conteudoParams.setMargins(0, dp(18), 0, 0);

        raiz.addView(abas);
        raiz.addView(conteudo, conteudoParams);

        Runnable[] mostrar = new Runnable[3];

        mostrar[0] = () -> {
            conteudo.removeAllViews();
            botoesAbas[0].setAlpha(1f);
            botoesAbas[1].setAlpha(0.65f);
            botoesAbas[2].setAlpha(0.65f);

            TextView tituloCatalogo = texto(
                    "CATÁLOGO NEXO", 19, BRANCO, true
            );
            tituloCatalogo.setPadding(0, dp(5), 0, dp(10));
            conteudo.addView(tituloCatalogo);

            LinearLayout cartao = painel();
            cartao.setOrientation(LinearLayout.VERTICAL);
            cartao.setPadding(dp(20), dp(24), dp(20), dp(24));

            TextView icone = texto("🎬", 38, BRANCO, false);
            icone.setGravity(Gravity.CENTER);
            cartao.addView(icone);

            TextView mensagem = texto(
                    "Sua biblioteca de conteúdo",
                    17, BRANCO, true
            );
            mensagem.setGravity(Gravity.CENTER);
            mensagem.setPadding(0, dp(12), 0, dp(8));
            cartao.addView(mensagem);

            TextView detalhe = texto(
                    "A listagem automática será conectada à fonte real "
                    + "do catálogo. Nenhum filme fictício será exibido.",
                    13, CINZA, false
            );
            detalhe.setGravity(Gravity.CENTER);
            cartao.addView(detalhe);

            conteudo.addView(cartao);
        };

        mostrar[1] = () -> {
            botoesAbas[0].setAlpha(0.65f);
            botoesAbas[1].setAlpha(1f);
            botoesAbas[2].setAlpha(0.65f);
            abrirPublicacao();
        };

        mostrar[2] = () -> {
            conteudo.removeAllViews();
            botoesAbas[0].setAlpha(0.65f);
            botoesAbas[1].setAlpha(0.65f);
            botoesAbas[2].setAlpha(1f);

            TextView tituloGerenciar = texto(
                    "GERENCIAR CONTEÚDO", 19, BRANCO, true
            );
            tituloGerenciar.setPadding(0, dp(5), 0, dp(10));
            conteudo.addView(tituloGerenciar);

            LinearLayout cartao = painel();
            cartao.setOrientation(LinearLayout.VERTICAL);
            cartao.setPadding(dp(18), dp(20), dp(18), dp(20));

            TextView estado = texto(
                    "Área de gerenciamento",
                    17, BRANCO, true
            );
            cartao.addView(estado);

            TextView detalhe = texto(
                    "As ações de editar, ocultar e excluir serão "
                    + "habilitadas quando a consulta e as operações "
                    + "reais do backend estiverem confirmadas.",
                    13, CINZA, false
            );
            detalhe.setPadding(0, dp(8), 0, 0);
            cartao.addView(detalhe);
            conteudo.addView(cartao);

            Button publicarAgora = botao(
                    "＋  Publicar novo conteúdo", ROSA
            );
            publicarAgora.setOnClickListener(v -> abrirPublicacao());

            LinearLayout.LayoutParams botaoParams =
                    new LinearLayout.LayoutParams(-1, dp(52));
            botaoParams.setMargins(0, dp(16), 0, 0);
            conteudo.addView(publicarAgora, botaoParams);
        };

        abaCatalogo.setOnClickListener(v -> mostrar[0].run());
        abaPublicar.setOnClickListener(v -> mostrar[1].run());
        abaGerenciar.setOnClickListener(v -> mostrar[2].run());

        mostrar[0].run();
        setContentView(scroll);
    }

    private void abrirPublicacao() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(FUNDO);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(18), dp(18), dp(18), dp(30));
        scroll.addView(raiz, new ScrollView.LayoutParams(-1, -2));

        // Cabeçalho premium
        LinearLayout cabecalho = new LinearLayout(this);
        cabecalho.setOrientation(LinearLayout.VERTICAL);
        cabecalho.setPadding(dp(18), dp(18), dp(18), dp(18));

        GradientDrawable fundoCabecalho = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[] { Color.rgb(8, 35, 65), Color.rgb(5, 15, 30) }
        );
        fundoCabecalho.setCornerRadius(dp(20));
        fundoCabecalho.setStroke(dp(1), AZUL);
        cabecalho.setBackground(fundoCabecalho);

        cabecalho.addView(texto(
                "NEXO  /  ESTÚDIO DE CONTEÚDO", 10, AZUL, true
        ));

        TextView titulo = texto("Publicar filme", 27, BRANCO, true);
        titulo.setPadding(0, dp(8), 0, dp(4));
        cabecalho.addView(titulo);

        cabecalho.addView(texto(
                "Cadastre os dados e envie os arquivos para a nuvem.",
                13, CINZA, false
        ));
        raiz.addView(cabecalho);

        // Cartão de informações
        LinearLayout dados = criarCartaoNexo();
        TextView tituloDados = texto(
                "INFORMAÇÕES DO CONTEÚDO", 11, AZUL, true
        );
        tituloDados.setPadding(dp(2), 0, 0, dp(10));
        dados.addView(tituloDados);

        dados.addView(rotulo("TÍTULO DO FILME"));
        EditText campoTitulo = campo("Ex.: O Grande Filme", false);
        dados.addView(campoTitulo);

        dados.addView(rotulo("DESCRIÇÃO / SINOPSE"));
        EditText campoDescricao = campo(
                "Escreva a sinopse e as informações do filme...", true
        );
        campoDescricao.setMinLines(4);
        campoDescricao.setGravity(Gravity.TOP);
        dados.addView(campoDescricao);

        dados.addView(rotulo("ANO DE LANÇAMENTO"));
        EditText campoAno = campo("Ex.: 2026", false);
        campoAno.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );
        dados.addView(campoAno);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(-1, -2);
        cardParams.setMargins(0, dp(16), 0, dp(12));
        dados.setLayoutParams(cardParams);
        raiz.addView(dados);

        // Categorias
        LinearLayout cardCategorias = criarCartaoNexo();
        cardCategorias.addView(texto("CATEGORIAS", 11, AZUL, true));

        TextView dicaCategorias = texto(
                "Selecione todas as categorias aplicáveis.",
                12, CINZA, false
        );
        dicaCategorias.setPadding(0, dp(5), 0, dp(8));
        cardCategorias.addView(dicaCategorias);

        LinearLayout painelCategorias = new LinearLayout(this);
        painelCategorias.setOrientation(LinearLayout.VERTICAL);
        painelCategorias.setPadding(dp(8), dp(5), dp(8), dp(5));

        GradientDrawable fundoLista = new GradientDrawable();
        fundoLista.setColor(Color.rgb(5, 12, 25));
        fundoLista.setCornerRadius(dp(14));
        fundoLista.setStroke(dp(1), Color.rgb(25, 55, 85));
        painelCategorias.setBackground(fundoLista);

        String[] categorias = {
                "Ação", "Aventura", "Comédia", "Drama",
                "Terror", "Romance", "Ficção", "Suspense",
                "Animação", "Anime", "Infantil", "Documentário"
        };

        java.util.ArrayList<CheckBox> seletoresCategorias =
                new java.util.ArrayList<>();

        for (String nomeCategoria : categorias) {
            CheckBox item = new CheckBox(this);
            item.setText(nomeCategoria);
            item.setTextColor(BRANCO);
            item.setTextSize(14);
            item.setButtonTintList(
                    android.content.res.ColorStateList.valueOf(AZUL)
            );
            item.setPadding(dp(4), dp(3), dp(4), dp(3));
            painelCategorias.addView(item);
            seletoresCategorias.add(item);
        }

        cardCategorias.addView(painelCategorias);
        LinearLayout.LayoutParams catParams =
                new LinearLayout.LayoutParams(-1, -2);
        catParams.setMargins(0, 0, 0, dp(12));
        cardCategorias.setLayoutParams(catParams);
        raiz.addView(cardCategorias);

        // Arquivos
        LinearLayout cardArquivos = criarCartaoNexo();
        cardArquivos.addView(texto("ARQUIVOS DE MÍDIA", 11, AZUL, true));

        cardArquivos.addView(rotulo("VÍDEO DO FILME"));
        Button selecionarVideo = botao("🎞️  Selecionar vídeo", AZUL);
        textoVideoSelecionado = texto(
                "Nenhum vídeo selecionado", 12, CINZA, false
        );
        textoVideoSelecionado.setPadding(dp(4), dp(2), dp(4), dp(10));

        selecionarVideo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
            startActivityForResult(intent, 1001);
        });
        cardArquivos.addView(selecionarVideo);
        cardArquivos.addView(textoVideoSelecionado);

        cardArquivos.addView(rotulo("PÔSTER / CAPA"));
        Button selecionarPoster = botao("🖼️  Selecionar pôster", AZUL);
        textoPosterSelecionado = texto(
                "Nenhum pôster selecionado", 12, CINZA, false
        );
        textoPosterSelecionado.setPadding(dp(4), dp(2), dp(4), dp(10));

        selecionarPoster.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, 1002);
        });
        cardArquivos.addView(selecionarPoster);
        cardArquivos.addView(textoPosterSelecionado);

        cardArquivos.addView(rotulo("BANNER"));
        Button selecionarBanner = botao("🌄  Selecionar banner", AZUL);
        textoBannerSelecionado = texto(
                "Nenhum banner selecionado", 12, CINZA, false
        );
        textoBannerSelecionado.setPadding(dp(4), dp(2), dp(4), dp(10));

        selecionarBanner.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, 1003);
        });
        cardArquivos.addView(selecionarBanner);
        cardArquivos.addView(textoBannerSelecionado);

        LinearLayout.LayoutParams midiaParams =
                new LinearLayout.LayoutParams(-1, -2);
        midiaParams.setMargins(0, 0, 0, dp(12));
        cardArquivos.setLayoutParams(midiaParams);
        raiz.addView(cardArquivos);

        // Opções de publicação
        LinearLayout cardOpcoes = criarCartaoNexo();
        cardOpcoes.addView(texto("VISIBILIDADE", 11, AZUL, true));

        CheckBox destaque = new CheckBox(this);
        destaque.setText("⭐ Colocar filme em DESTAQUES");
        destaque.setTextColor(BRANCO);
        destaque.setTextSize(14);
        destaque.setButtonTintList(
                android.content.res.ColorStateList.valueOf(AZUL)
        );
        destaque.setPadding(0, dp(8), 0, dp(8));
        cardOpcoes.addView(destaque);

        CheckBox publicado = new CheckBox(this);
        publicado.setText("🟢 Publicar imediatamente");
        publicado.setTextColor(BRANCO);
        publicado.setTextSize(14);
        publicado.setButtonTintList(
                android.content.res.ColorStateList.valueOf(AZUL)
        );
        publicado.setChecked(true);
        cardOpcoes.addView(publicado);
        raiz.addView(cardOpcoes);

        Button publicar = botao(
                "🚀  PUBLICAR NA NUVEM NEXO", ROSA
        );
        publicar.setTextSize(14);

        LinearLayout.LayoutParams publicarParams =
                new LinearLayout.LayoutParams(-1, dp(56));
        publicarParams.setMargins(0, dp(16), 0, dp(8));
        publicar.setLayoutParams(publicarParams);

        publicar.setOnClickListener(v -> {
            String tituloFilme = campoTitulo.getText().toString().trim();

            if (tituloFilme.isEmpty()) {
                campoTitulo.setError("Informe o título do filme");
                campoTitulo.requestFocus();
                return;
            }

            publicarNoRender(
                    tituloFilme,
                    campoDescricao.getText().toString().trim(),
                    campoAno.getText().toString().trim(),
                    juntarCategorias(seletoresCategorias),
                    destaque.isChecked(),
                    publicado.isChecked(),
                    publicar
            );
        });

        raiz.addView(publicar);

        TextView nota = texto(
                "Confira os dados antes de publicar.",
                11, CINZA, false
        );
        nota.setGravity(Gravity.CENTER);
        nota.setPadding(0, dp(2), 0, dp(8));
        raiz.addView(nota);

        setContentView(scroll);
    }

    private LinearLayout criarCartaoNexo() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(12));

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(PAINEL);
        fundo.setCornerRadius(dp(18));
        fundo.setStroke(dp(1), Color.rgb(25, 55, 85));
        card.setBackground(fundo);
        return card;
    }

    private String juntarCategorias(
            java.util.ArrayList<CheckBox> seletores
    ) {
        java.util.ArrayList<String> escolhidas = new java.util.ArrayList<>();

        for (CheckBox item : seletores) {
            if (item.isChecked()) {
                escolhidas.add(item.getText().toString());
            }
        }

        return android.text.TextUtils.join(", ", escolhidas);
    }

    private void publicarNoRender(
            final String tituloFilme,
            final String descricaoFilme,
            final String anoTexto,
            final String categoriaFilme,
            final boolean destaqueFilme,
            final boolean ativoFilme,
            final Button botaoPublicar
    ) {
        if (publicacaoEmAndamento) {
            return;
        }

        if (uriVideoSelecionado == null) {
            publicacaoFalhou(
                    botaoPublicar,
                    "Selecione o arquivo de vídeo antes de publicar."
            );
            return;
        }

        com.google.firebase.auth.FirebaseUser usuario =
                com.google.firebase.auth.FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (usuario == null) {
            publicacaoFalhou(
                    botaoPublicar,
                    "Entre na sua conta antes de publicar no NEXO."
            );
            return;
        }

        publicacaoEmAndamento = true;
        if (botaoPublicar != null) {
            botaoPublicar.setEnabled(false);
            botaoPublicar.setText("⏳ PREPARANDO PUBLICAÇÃO...");
        }

        usuario.getIdToken(true)
                .addOnSuccessListener(resultado -> {
                    String token = resultado.getToken();
                    if (token == null || token.trim().isEmpty()) {
                        publicacaoFalhou(
                                botaoPublicar,
                                "Não foi possível obter o token de autenticação."
                        );
                        return;
                    }

                    fazerUploadArquivosNexo(
                            usuario.getUid(),
                            tituloFilme,
                            descricaoFilme,
                            anoTexto,
                            categoriaFilme,
                            destaqueFilme,
                            ativoFilme,
                            token,
                            botaoPublicar
                    );
                })
                .addOnFailureListener(erro ->
                        publicacaoFalhou(
                                botaoPublicar,
                                "Falha na autenticação Firebase: "
                                        + mensagemErro(erro)
                        )
                );
    }

    private void fazerUploadArquivosNexo(
            final String uid,
            final String tituloFilme,
            final String descricaoFilme,
            final String anoTexto,
            final String categoriaFilme,
            final boolean destaqueFilme,
            final boolean ativoFilme,
            final String token,
            final Button botaoPublicar
    ) {
        if (uriVideoSelecionado != null) {
            if (botaoPublicar != null) {
                botaoPublicar.setText("⏳ ENVIANDO VÍDEO...");
            }

            uploadArquivoNexo(
                    uid,
                    uriVideoSelecionado,
                    "video",
                    urlVideo -> uploadPosterNexo(
                            uid,
                            urlVideo,
                            tituloFilme,
                            descricaoFilme,
                            anoTexto,
                            categoriaFilme,
                            destaqueFilme,
                            ativoFilme,
                            token,
                            botaoPublicar
                    ),
                    erro -> publicacaoFalhou(botaoPublicar, erro)
            );
        } else {
            uploadPosterNexo(
                    uid,
                    "",
                    tituloFilme,
                    descricaoFilme,
                    anoTexto,
                    categoriaFilme,
                    destaqueFilme,
                    ativoFilme,
                    token,
                    botaoPublicar
            );
        }
    }

    private void uploadPosterNexo(
            final String uid,
            final String urlVideo,
            final String tituloFilme,
            final String descricaoFilme,
            final String anoTexto,
            final String categoriaFilme,
            final boolean destaqueFilme,
            final boolean ativoFilme,
            final String token,
            final Button botaoPublicar
    ) {
        if (uriPosterSelecionado != null) {
            if (botaoPublicar != null) {
                botaoPublicar.setText("⏳ ENVIANDO POSTER...");
            }

            uploadArquivoNexo(
                    uid,
                    uriPosterSelecionado,
                    "capa",
                    urlPoster -> uploadBannerNexo(
                            uid,
                            urlVideo,
                            urlPoster,
                            tituloFilme,
                            descricaoFilme,
                            anoTexto,
                            categoriaFilme,
                            destaqueFilme,
                            ativoFilme,
                            token,
                            botaoPublicar
                    ),
                    erro -> publicacaoFalhou(botaoPublicar, erro)
            );
        } else {
            uploadBannerNexo(
                    uid,
                    urlVideo,
                    "",
                    tituloFilme,
                    descricaoFilme,
                    anoTexto,
                    categoriaFilme,
                    destaqueFilme,
                    ativoFilme,
                    token,
                    botaoPublicar
            );
        }
    }

    private void uploadBannerNexo(
            final String uid,
            final String urlVideo,
            final String urlPoster,
            final String tituloFilme,
            final String descricaoFilme,
            final String anoTexto,
            final String categoriaFilme,
            final boolean destaqueFilme,
            final boolean ativoFilme,
            final String token,
            final Button botaoPublicar
    ) {
        if (uriBannerSelecionado != null) {
            if (botaoPublicar != null) {
                botaoPublicar.setText("⏳ ENVIANDO BANNER...");
            }

            uploadArquivoNexo(
                    uid,
                    uriBannerSelecionado,
                    "banner",
                    urlBanner -> enviarPublicacaoNexo(
                            urlVideo,
                            urlPoster,
                            urlBanner,
                            tituloFilme,
                            descricaoFilme,
                            anoTexto,
                            categoriaFilme,
                            destaqueFilme,
                            ativoFilme,
                            token,
                            botaoPublicar
                    ),
                    erro -> publicacaoFalhou(botaoPublicar, erro)
            );
        } else {
            enviarPublicacaoNexo(
                    urlVideo,
                    urlPoster,
                    "",
                    tituloFilme,
                    descricaoFilme,
                    anoTexto,
                    categoriaFilme,
                    destaqueFilme,
                    ativoFilme,
                    token,
                    botaoPublicar
            );
        }
    }


    private void mostrarProgressoUploadNexo(
            String tipo,
            int percentual
    ) {
        final String etapa;
        if ("video".equalsIgnoreCase(tipo)) {
            etapa = "vídeo";
        } else if ("capa".equalsIgnoreCase(tipo)
                || "poster".equalsIgnoreCase(tipo)) {
            etapa = "capa";
        } else if ("banner".equalsIgnoreCase(tipo)) {
            etapa = "banner";
        } else {
            etapa = tipo == null ? "arquivo" : tipo;
        }

        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            if (progressoUploadNexo == null) {
                progressoUploadNexo =
                        new android.app.ProgressDialog(
                                NexoFilmesActivity.this
                        );
                progressoUploadNexo.setProgressStyle(
                        android.app.ProgressDialog.STYLE_HORIZONTAL
                );
                progressoUploadNexo.setMax(100);
                progressoUploadNexo.setCancelable(false);
            }

            progressoUploadNexo.setTitle("Enviando " + etapa);
            progressoUploadNexo.setMessage(
                    "Transferindo arquivo para a nuvem NEXO..."
            );
            progressoUploadNexo.setIndeterminate(false);
            progressoUploadNexo.setProgress(
                    Math.max(0, Math.min(100, percentual))
            );

            if (!progressoUploadNexo.isShowing()) {
                progressoUploadNexo.show();
                progressoUploadNexo.setProgress(
                        Math.max(0, Math.min(100, percentual))
                );
            }
        });
    }

    private void finalizarProgressoUploadNexo() {
        runOnUiThread(() -> {
            if (progressoUploadNexo != null
                    && progressoUploadNexo.isShowing()) {
                progressoUploadNexo.dismiss();
            }
        });
    }

    private interface UploadSucesso {
        void executar(String url);
    }

    private interface UploadFalha {
        void executar(String mensagem);
    }

    private void uploadArquivoNexo(
            final String uid,
            final Uri uri,
            final String tipo,
            final UploadSucesso sucesso,
            final UploadFalha falha
    ) {
        try {
            String mime = getContentResolver().getType(uri);

            if (mime == null || mime.trim().isEmpty()) {
                mime = "application/octet-stream";
            }

            String nomeArquivo = "arquivo";
            android.database.Cursor cursor =
                    getContentResolver().query(
                            uri,
                            new String[]{android.provider.OpenableColumns.DISPLAY_NAME},
                            null,
                            null,
                            null
                    );

            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int indice = cursor.getColumnIndex(
                                android.provider.OpenableColumns.DISPLAY_NAME
                        );

                        if (indice >= 0) {
                            String nome = cursor.getString(indice);
                            if (nome != null && !nome.trim().isEmpty()) {
                                nomeArquivo = nome.trim();
                            }
                        }
                    }
                } finally {
                    cursor.close();
                }
            }

            final String mimeFinal = mime;
            final String nomeArquivoFinal = nomeArquivo;

            com.google.firebase.auth.FirebaseUser usuario =
                    com.google.firebase.auth.FirebaseAuth
                            .getInstance()
                            .getCurrentUser();

            if (usuario == null) {
                falha.executar("Usuário não autenticado.");
                return;
            }

            usuario.getIdToken(true)
                    .addOnSuccessListener(resultadoToken -> {
                        new Thread(() -> {
                            try {
                            String token = resultadoToken.getToken();

                            if (token == null || token.trim().isEmpty()) {
                                falha.executar("Não foi possível obter o token Firebase.");
                                return;
                            }

                            org.json.JSONObject dados =
                                    new org.json.JSONObject();

                            dados.put("nome", nomeArquivoFinal);
                            dados.put("mime", mimeFinal);
                            dados.put("tipo", tipo);

                            java.net.URL url =
                                    new java.net.URL(
                                            "https://danikeai-brain.onrender.com/nexo/r2/upload-url"
                                    );

                            java.net.HttpURLConnection conexao =
                                    (java.net.HttpURLConnection) url.openConnection();

                            conexao.setRequestMethod("POST");
                            conexao.setConnectTimeout(15000);
                            conexao.setReadTimeout(30000);
                            conexao.setDoOutput(true);
                            conexao.setRequestProperty(
                                    "Authorization",
                                    "Bearer " + token
                            );
                            conexao.setRequestProperty(
                                    "Content-Type",
                                    "application/json"
                            );
                            conexao.setRequestProperty(
                                    "Accept",
                                    "application/json"
                            );

                            byte[] corpo = dados.toString()
                                    .getBytes(java.nio.charset.StandardCharsets.UTF_8);

                            try (java.io.OutputStream saida =
                                         conexao.getOutputStream()) {
                                saida.write(corpo);
                                saida.flush();
                            }

                            int codigo = conexao.getResponseCode();

                            java.io.InputStream respostaStream =
                                    codigo >= 200 && codigo < 300
                                            ? conexao.getInputStream()
                                            : conexao.getErrorStream();

                            String resposta = "";

                            if (respostaStream != null) {
                                try (java.io.BufferedReader leitor =
                                             new java.io.BufferedReader(
                                                     new java.io.InputStreamReader(
                                                             respostaStream,
                                                             java.nio.charset.StandardCharsets.UTF_8
                                                     )
                                             )) {
                                    StringBuilder texto =
                                            new StringBuilder();
                                    String linha;

                                    while ((linha = leitor.readLine()) != null) {
                                        texto.append(linha);
                                    }

                                    resposta = texto.toString();
                                }
                            }

                            conexao.disconnect();

                            if (codigo < 200 || codigo >= 300) {
                                final String mensagem =
                                        resposta == null || resposta.trim().isEmpty()
                                                ? "Servidor retornou HTTP " + codigo
                                                : resposta;

                                runOnUiThread(() ->
                                        falha.executar(
                                                "Não foi possível preparar o upload do "
                                                        + tipo
                                                        + ": "
                                                        + mensagem
                                        )
                                );
                                return;
                            }

                            org.json.JSONObject retorno =
                                    new org.json.JSONObject(resposta);

                            boolean ok =
                                    retorno.optBoolean("sucesso", false);

                            if (!ok) {
                                final String erro =
                                        retorno.optString(
                                                "erro",
                                                "Servidor recusou o upload."
                                        );

                                runOnUiThread(() ->
                                        falha.executar(erro)
                                );
                                return;
                            }

                            String uploadUrl =
                                    retorno.optString("upload_url", "");

                            String objectKey =
                                    retorno.optString("object_key", "");

                            if (uploadUrl.trim().isEmpty()) {
                                runOnUiThread(() ->
                                        falha.executar(
                                                "O servidor não retornou a URL de upload do "
                                                        + tipo
                                        )
                                );
                                return;
                            }

                            long tamanhoArquivo = -1L;
                            try (android.content.res.AssetFileDescriptor afd =
                                         getContentResolver()
                                                 .openAssetFileDescriptor(uri, "r")) {
                                if (afd != null) {
                                    tamanhoArquivo = afd.getLength();
                                }
                            } catch (Exception ignorado) {
                                tamanhoArquivo = -1L;
                            }
                            final long tamanhoTotalUpload = tamanhoArquivo;

                            java.io.InputStream entrada =
                                    getContentResolver()
                                            .openInputStream(uri);

                            if (entrada == null) {
                                runOnUiThread(() ->
                                        falha.executar(
                                                "Não foi possível abrir o arquivo selecionado para o upload do "
                                                        + tipo
                                                        + "."
                                        )
                                );
                                return;
                            }

                            java.net.HttpURLConnection upload =
                                    (java.net.HttpURLConnection)
                                            new java.net.URL(uploadUrl)
                                                    .openConnection();

                            upload.setRequestMethod("PUT");
                            upload.setDoOutput(true);
                            upload.setConnectTimeout(30000);
                            upload.setReadTimeout(120000);
                            upload.setRequestProperty(
                                    "Content-Type",
                                    mimeFinal
                            );

                            mostrarProgressoUploadNexo(tipo, 0);

                            try (
                                    java.io.InputStream origem = entrada;
                                    java.io.OutputStream destino =
                                            upload.getOutputStream()
                            ) {
                                byte[] buffer = new byte[64 * 1024];
                                int lidos;
                                long bytesEnviados = 0L;
                                int ultimoPercentual = -1;

                                while ((lidos = origem.read(buffer)) != -1) {
                                    destino.write(buffer, 0, lidos);
                                    bytesEnviados += lidos;

                                    if (tamanhoTotalUpload > 0L) {
                                        int percentual = (int) Math.min(
                                                99L,
                                                bytesEnviados * 100L
                                                        / tamanhoTotalUpload
                                        );

                                        if (percentual != ultimoPercentual) {
                                            ultimoPercentual = percentual;
                                            mostrarProgressoUploadNexo(
                                                    tipo, percentual
                                            );
                                        }
                                    }
                                }

                                destino.flush();
                            }

                            int codigoUpload =
                                    upload.getResponseCode();

                            String respostaUpload = "";

                            java.io.InputStream respostaUploadStream =
                                    codigoUpload >= 200
                                            && codigoUpload < 300
                                            ? upload.getInputStream()
                                            : upload.getErrorStream();

                            if (respostaUploadStream != null) {
                                try (java.io.BufferedReader leitor =
                                             new java.io.BufferedReader(
                                                     new java.io.InputStreamReader(
                                                             respostaUploadStream,
                                                             java.nio.charset.StandardCharsets.UTF_8
                                                     )
                                             )) {
                                    StringBuilder texto =
                                            new StringBuilder();
                                    String linha;

                                    while ((linha = leitor.readLine()) != null) {
                                        texto.append(linha);
                                    }

                                    respostaUpload = texto.toString();
                                }
                            }

                            upload.disconnect();

                            if (codigoUpload < 200
                                    || codigoUpload >= 300) {

                                finalizarProgressoUploadNexo();

                                final String mensagem =
                                        respostaUpload == null
                                                || respostaUpload.trim().isEmpty()
                                                ? "HTTP " + codigoUpload
                                                : respostaUpload;

                                runOnUiThread(() ->
                                        falha.executar(
                                                "Falha ao enviar o "
                                                        + tipo
                                                        + " para o Cloudflare R2: "
                                                        + mensagem
                                        )
                                );
                                return;
                            }

                            final String chaveFinal = objectKey.trim();

                            runOnUiThread(() -> {
                                if (chaveFinal.isEmpty()) {
                                    falha.executar(
                                            "O R2 recebeu o arquivo, mas o servidor não retornou a chave do objeto "
                                                    + tipo
                                    );
                                } else {
                                    sucesso.executar(chaveFinal);
                                }
                            });

                        } catch (Exception erro) {
                            finalizarProgressoUploadNexo();
                            runOnUiThread(() ->
                                    falha.executar(
                                            "Erro no upload do "
                                                    + tipo
                                                    + ": "
                                                    + mensagemErro(erro)
                                    )
                            );
                        }
                        }).start();
                    })
                    .addOnFailureListener(erro ->
                            falha.executar(
                                    "Não foi possível obter o token Firebase: "
                                            + mensagemErro(erro)
                            )
                    );

        } catch (Exception erro) {
            falha.executar(
                    "Erro ao preparar o upload do "
                            + tipo
                            + ": "
                            + mensagemErro(erro)
            );
        }
    }

    private String extensaoParaMime(
            String mime,
            String tipo
    ) {
        String m = mime.toLowerCase();

        if (m.contains("mp4")) return ".mp4";
        if (m.contains("webm")) return ".webm";
        if (m.contains("quicktime")) return ".mov";
        if (m.contains("png")) return ".png";
        if (m.contains("webp")) return ".webp";
        if (m.contains("gif")) return ".gif";
        if (m.contains("jpeg") || m.contains("jpg")) return ".jpg";

        if ("video".equals(tipo)) return ".mp4";
        return ".jpg";
    }

    private String mensagemErro(Exception erro) {
        String mensagem = erro.getMessage();

        return mensagem == null || mensagem.trim().isEmpty()
                ? erro.getClass().getSimpleName()
                : mensagem;
    }

    private void enviarPublicacaoNexo(
            final String urlVideo,
            final String urlPoster,
            final String urlBanner,
            final String tituloFilme,
            final String descricaoFilme,
            final String anoTexto,
            final String categoriaFilme,
            final boolean destaqueFilme,
            final boolean ativoFilme,
            final String token,
            final Button botaoPublicar
    ) {
        if (botaoPublicar != null) {
            botaoPublicar.setText("⏳ PUBLICANDO NO NEXO...");
        }

        runOnUiThread(() -> {
            if (progressoUploadNexo != null
                    && progressoUploadNexo.isShowing()) {
                progressoUploadNexo.setTitle("Publicando no NEXO");
                progressoUploadNexo.setMessage(
                        "Arquivos enviados. Aguardando confirmação do servidor..."
                );
                progressoUploadNexo.setIndeterminate(true);
            }
        });

        new Thread(() -> {
            java.net.HttpURLConnection conexao = null;

            try {
                org.json.JSONObject corpo = new org.json.JSONObject();

                corpo.put("titulo", tituloFilme);
                corpo.put("descricao", descricaoFilme);
                corpo.put("categoria", categoriaFilme);
                corpo.put("ano", anoTexto == null ? "" : anoTexto);
                corpo.put("destaque", destaqueFilme);
                corpo.put("ativo", ativoFilme);

                if (urlVideo != null && !urlVideo.trim().isEmpty()) {
                    corpo.put("video_key", urlVideo);
                }

                if (urlPoster != null && !urlPoster.trim().isEmpty()) {
                    corpo.put("capa_key", urlPoster);
                }

                if (urlBanner != null && !urlBanner.trim().isEmpty()) {
                    corpo.put("banner_key", urlBanner);
                }

                byte[] dados =
                        corpo.toString().getBytes(
                                java.nio.charset.StandardCharsets.UTF_8
                        );

                java.net.URL url =
                        new java.net.URL(URL_RENDER_NEXO);

                conexao =
                        (java.net.HttpURLConnection) url.openConnection();

                conexao.setRequestMethod("POST");
                conexao.setConnectTimeout(15000);
                conexao.setReadTimeout(30000);
                conexao.setDoOutput(true);

                conexao.setRequestProperty(
                        "Authorization",
                        "Bearer " + token
                );

                conexao.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                conexao.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                conexao.setFixedLengthStreamingMode(dados.length);

                try (java.io.OutputStream saida =
                             conexao.getOutputStream()) {
                    saida.write(dados);
                    saida.flush();
                }

                int codigo = conexao.getResponseCode();

                java.io.InputStream entrada =
                        codigo >= 400
                                ? conexao.getErrorStream()
                                : conexao.getInputStream();

                String resposta = "";

                if (entrada != null) {
                    try (
                            java.io.BufferedReader leitor =
                                    new java.io.BufferedReader(
                                            new java.io.InputStreamReader(
                                                    entrada,
                                                    java.nio.charset.StandardCharsets.UTF_8
                                            )
                                    )
                    ) {
                        StringBuilder texto =
                                new StringBuilder();

                        String linha;

                        while ((linha = leitor.readLine()) != null) {
                            texto.append(linha);
                        }

                        resposta = texto.toString();
                    }
                }

                final int codigoFinal = codigo;
                final String respostaFinal = resposta;

                runOnUiThread(() -> {
                    finalizarProgressoUploadNexo();
                    publicacaoEmAndamento = false;

                    if (botaoPublicar != null) {
                        botaoPublicar.setEnabled(true);
                        botaoPublicar.setText(
                                "🚀  PUBLICAR NA NUVEM NEXO"
                        );
                    }

                    if (codigoFinal >= 200 && codigoFinal < 300) {
                        NexoDiagnostic.registrar(
                                NexoFilmesActivity.this,
                                "NexoFilmesActivity",
                                "PUBLISH_SUCCESS",
                                "INFO",
                                "NEXO",
                                "publicarNoRender",
                                codigoFinal,
                                "",
                                "Arquivos enviados ao Cloudflare R2 e publicação confirmada pelo servidor.",
                                0
                        );

                        new android.app.AlertDialog.Builder(this)
                                .setTitle("✅ Filme publicado no NEXO")
                                .setMessage(
                                        "Os arquivos foram enviados ao Cloudflare R2 e o servidor respondeu à publicação.\n\n"
                                                + "Resposta do servidor:\n"
                                                + respostaFinal
                                )
                                .setPositiveButton("OK", null)
                                .show();

                    } else {
                        NexoDiagnostic.registrar(
                                NexoFilmesActivity.this,
                                "NexoFilmesActivity",
                                "PUBLISH_ERROR",
                                "ERROR",
                                "NEXO",
                                "publicarNoRender",
                                codigoFinal,
                                "HTTP_" + codigoFinal,
                                "Falha HTTP na publicação NEXO.",
                                0
                        );

                        publicacaoFalhou(
                                botaoPublicar,
                                "HTTP " + codigoFinal
                                        + "\n\n"
                                        + respostaFinal
                        );
                    }
                });

            } catch (Exception erro) {
                final String mensagem = mensagemErro(erro);

                runOnUiThread(() ->
                        publicacaoFalhou(
                                botaoPublicar,
                                mensagem
                        )
                );

            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        }).start();
    }

    private void publicacaoFalhou(
            Button botaoPublicar,
            String mensagem
    ) {
        finalizarProgressoUploadNexo();

        NexoDiagnostic.registrar(
                this,
                "NexoFilmesActivity",
                "PUBLISH_ERROR",
                "ERROR",
                "NEXO",
                "publicarNoRender",
                null,
                "CLIENT_ERROR",
                "Falha no cliente durante a publicação NEXO.",
                0
        );

        publicacaoEmAndamento = false;

        if (botaoPublicar != null) {
            botaoPublicar.setEnabled(true);
            botaoPublicar.setText(
                    "🚀  PUBLICAR NA NUVEM NEXO"
            );
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("❌ Publicação não concluída")
                .setMessage(mensagem)
                .setPositiveButton("OK", null)
                .show();
    }

    private TextView rotulo(String texto) {

        TextView t = texto(
                texto,
                11,
                AZUL,
                true
        );

        t.setPadding(
                dp(2),
                dp(8),
                0,
                dp(2)
        );

        return t;
    }

    private EditText campo(
            String hint,
            boolean multiline
    ) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setHintTextColor(CINZA);
        e.setTextColor(BRANCO);
        e.setTextSize(14);
        e.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
        );

        if (multiline) {
            e.setSingleLine(false);
        } else {
            e.setSingleLine(true);
        }

        GradientDrawable fundo =
                new GradientDrawable();

        fundo.setColor(
                Color.rgb(5, 12, 25)
        );

        fundo.setCornerRadius(
                dp(14)
        );

        fundo.setStroke(
                dp(1),
                Color.rgb(25, 65, 100)
        );

        e.setBackground(fundo);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        multiline ? dp(110) : dp(52)
                );

        p.setMargins(
                0,
                dp(4),
                0,
                dp(8)
        );

        e.setLayoutParams(p);

        return e;
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (resultCode != RESULT_OK ||
                data == null ||
                data.getData() == null) {
            return;
        }

        Uri uri = data.getData();

        try {

            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

        } catch (Exception ignored) {
            // Alguns provedores não oferecem permissão persistente.
        }

        String nome = nomeArquivo(uri);

        if (nome == null || nome.trim().isEmpty()) {
            nome = "Arquivo selecionado";
        }

        if (requestCode == 1001) {

            uriVideoSelecionado = uri;

            if (textoVideoSelecionado != null) {
                textoVideoSelecionado.setText(
                        "✅ " + nome
                );
                textoVideoSelecionado.setTextColor(
                        Color.rgb(80, 230, 150)
                );
            }

        } else if (requestCode == 1002) {

            uriPosterSelecionado = uri;

            if (textoPosterSelecionado != null) {
                textoPosterSelecionado.setText(
                        "✅ " + nome
                );
                textoPosterSelecionado.setTextColor(
                        Color.rgb(80, 230, 150)
                );
            }

        } else if (requestCode == 1003) {

            uriBannerSelecionado = uri;

            if (textoBannerSelecionado != null) {
                textoBannerSelecionado.setText(
                        "✅ " + nome
                );
                textoBannerSelecionado.setTextColor(
                        Color.rgb(80, 230, 150)
                );
            }
        }
    }

    private String nomeArquivo(Uri uri) {

        if (uri == null) {
            return null;
        }

        Cursor cursor = null;

        try {

            cursor = getContentResolver().query(
                    uri,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null &&
                    cursor.moveToFirst()) {

                int indice =
                        cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME
                        );

                if (indice >= 0) {
                    return cursor.getString(indice);
                }
            }

        } catch (Exception e) {

            android.util.Log.e(
                    "NEXO_FILMES",
                    "Erro ao obter nome do arquivo",
                    e
            );

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return uri.getLastPathSegment();
    }

    private void adicionarBotaoFiltro(
            LinearLayout raiz,
            String texto
    ) {

        Button b = botao(texto, AZUL);

        raiz.addView(
                b,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                )
        );
    }

    private LinearLayout painel() {

        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.HORIZONTAL);
        p.setGravity(Gravity.CENTER);
        p.setPadding(
                dp(8), dp(12), dp(8), dp(12)
        );

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(PAINEL);
        fundo.setCornerRadius(dp(18));
        fundo.setStroke(dp(1), Color.rgb(25, 65, 100));

        p.setBackground(fundo);

        return p;
    }

    private LinearLayout estatistica(
            String icone,
            String nome,
            String valor
    ) {

        LinearLayout bloco = new LinearLayout(this);
        bloco.setOrientation(LinearLayout.VERTICAL);
        bloco.setGravity(Gravity.CENTER);

        TextView i = texto(
                icone,
                21,
                BRANCO,
                false
        );
        i.setGravity(Gravity.CENTER);

        TextView v = texto(
                valor,
                20,
                BRANCO,
                true
        );
        v.setGravity(Gravity.CENTER);

        TextView n = texto(
                nome,
                9,
                CINZA,
                true
        );
        n.setGravity(Gravity.CENTER);

        bloco.addView(i);
        bloco.addView(v);
        bloco.addView(n);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                );

        bloco.setLayoutParams(p);

        return bloco;
    }

    private Button botao(
            String texto,
            int cor
    ) {

        Button b = new Button(this);

        b.setText(texto);
        b.setTextColor(BRANCO);
        b.setTextSize(13);
        b.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);

        GradientDrawable fundo =
                new GradientDrawable();

        fundo.setColor(Color.rgb(5, 16, 32));
        fundo.setCornerRadius(dp(16));
        fundo.setStroke(dp(1), cor);

        b.setBackground(fundo);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                );

        p.setMargins(
                dp(2), dp(5),
                dp(2), dp(5)
        );

        b.setLayoutParams(p);

        return b;
    }

    private TextView texto(
            String texto,
            float tamanho,
            int cor,
            boolean negrito
    ) {

        TextView t = new TextView(this);

        t.setText(texto);
        t.setTextColor(cor);
        t.setTextSize(tamanho);

        if (negrito) {
            t.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return t;
    }

    private int dp(int valor) {
        return (int) (
                valor *
                getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
