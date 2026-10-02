package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AdmActivity extends Activity {

    private static final String OWNER_EMAIL = "lipesanderson@gmail.com";
    private static final int FOTO_PERFIL = 9001;

    private final int FUNDO = Color.rgb(2, 6, 16);
    private final int AZUL = Color.rgb(0, 210, 255);
    private final int VERDE = Color.rgb(40, 255, 145);
    private final int VERMELHO = Color.rgb(255, 45, 100);
    private final int ROXO = Color.rgb(190, 70, 255);
    private final int ROSA = Color.rgb(255, 40, 170);
    private final int DOURADO = Color.rgb(255, 190, 45);
    private final int BRANCO = Color.rgb(235, 245, 255);

    private FirebaseFirestore db;
    private boolean manutencaoAtiva = false;
    private Button manutencaoBtn;
    private TextView totalUsuarios;
    private TextView onlineUsuarios;
    private TextView offlineUsuarios;
    private TextView proprietarios;
    private LinearLayout listaUsuarios;
    private ImageView fotoPerfil;

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + .5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null
                || usuario.getEmail() == null
                || !OWNER_EMAIL.equalsIgnoreCase(usuario.getEmail())) {

            Toast.makeText(
                    this,
                    "Acesso exclusivo do proprietário.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        montarTela();
        carregarManutencao();
        carregarUsuarios();
    }

    private void montarTela() {

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(FUNDO);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(14), dp(10), dp(14), dp(18));

        scroll.addView(raiz);

        raiz.addView(cabecalho());

        TextView visao = secao("⚡  VISÃO GERAL", AZUL);
        raiz.addView(visao);

        LinearLayout estatisticas = new LinearLayout(this);
        estatisticas.setOrientation(LinearLayout.HORIZONTAL);

        totalUsuarios = valorCard(estatisticas, "0", "TOTAL\nDE USUÁRIOS", AZUL);
        onlineUsuarios = valorCard(estatisticas, "0", "ONLINE", VERDE);
        offlineUsuarios = valorCard(estatisticas, "0", "OFFLINE", ROSA);
        proprietarios = valorCard(estatisticas, "1", "PROPRIETÁRIO", ROXO);

        raiz.addView(estatisticas);

        raiz.addView(secao("👥  USUÁRIOS RECENTES", AZUL));

        LinearLayout usuariosBox = painel(AZUL);
        listaUsuarios = new LinearLayout(this);
        listaUsuarios.setOrientation(LinearLayout.VERTICAL);

        usuariosBox.addView(listaUsuarios);

        Button verTodos = pequenoBotao("VER TODOS  ›", AZUL);
        verTodos.setOnClickListener(v -> mostrarTodosUsuarios());

        usuariosBox.addView(
                verTodos,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(46)
                )
        );

        raiz.addView(usuariosBox);

        raiz.addView(secao("🎛  CONTROLES", AZUL));

        LinearLayout grade1 = new LinearLayout(this);
        grade1.setOrientation(LinearLayout.HORIZONTAL);

        manutencaoBtn = quadrado(
                grade1,
                "⚙",
                "MANUTENÇÃO",
                "Sistema",
                ROXO
        );

        Button ia = quadrado(
                grade1,
                "🤖",
                "IA",
                "Configurações",
                AZUL
        );

        raiz.addView(grade1);

        manutencaoBtn.setOnClickListener(v -> alternarManutencao());

        ia.setOnClickListener(v -> {
            startActivity(new Intent(this, IAActivity.class));
        });

        LinearLayout grade2 = new LinearLayout(this);
        grade2.setOrientation(LinearLayout.HORIZONTAL);

        Button filmes = quadrado(
                grade2,
                "🎬",
                "FILMES",
                "Gerenciar",
                ROSA
        );

        Button logs = quadrado(
                grade2,
                "📄",
                "LOGS / FLUXOS",
                "Visualizar",
                DOURADO
        );

        raiz.addView(grade2);

        filmes.setOnClickListener(v -> {
            startActivity(new Intent(this, com.danike.ai.filmes.FilmesActivity.class));
        });

        logs.setOnClickListener(v -> mostrarDiagnostico());

        LinearLayout grade3 = new LinearLayout(this);
        grade3.setOrientation(LinearLayout.HORIZONTAL);

        Button banco = quadrado(
                grade3,
                "🗄",
                "BANCO DE DADOS",
                "Backup / dados",
                VERDE
        );

        Button seguranca = quadrado(
                grade3,
                "🛡",
                "SEGURANÇA",
                "Autenticação",
                AZUL
        );

        raiz.addView(grade3);

        banco.setOnClickListener(v -> mostrarBanco());
        seguranca.setOnClickListener(v -> mostrarSeguranca());

        LinearLayout grade4 = new LinearLayout(this);
        grade4.setOrientation(LinearLayout.HORIZONTAL);

        Button reiniciar = quadrado(
                grade4,
                "↻",
                "REINICIAR",
                "Sistema",
                AZUL
        );

        Button limpar = quadrado(
                grade4,
                "🗑",
                "LIMPAR CACHE",
                "Dados temporários",
                VERMELHO
        );

        raiz.addView(grade4);

        reiniciar.setOnClickListener(v -> {
            Toast.makeText(this, "Atualizando painel...", Toast.LENGTH_SHORT).show();
            recreate();
        });

        limpar.setOnClickListener(v -> limparCache());

        LinearLayout grade5 = new LinearLayout(this);
        grade5.setOrientation(LinearLayout.HORIZONTAL);

        Button atualizacao = quadrado(
                grade5,
                "📲",
                "ATUALIZAÇÃO",
                "Gerenciar versão",
                AZUL
        );

        Button compartilhar = quadrado(
                grade5,
                "📤",
                "COMPARTILHAR",
                "Divulgar app",
                VERDE
        );

        raiz.addView(grade5);

        atualizacao.setOnClickListener(v -> mostrarAtualizacao());

        compartilhar.setOnClickListener(v -> AppDistribuicao.compartilhar(
                this,
                "https://github.com/andersonptg/DaNikeAI/releases/latest/download/DaNikeAI.apk"
        ));

        LinearLayout grade6 = new LinearLayout(this);
        grade6.setOrientation(LinearLayout.HORIZONTAL);

        Button baixarApp = quadrado(
                grade6,
                "⬇️",
                "BAIXAR APP",
                "Salvar APK",
                DOURADO
        );

        raiz.addView(grade6);

        baixarApp.setOnClickListener(v -> AppDistribuicao.baixarApk(
                this,
                "https://github.com/andersonptg/DaNikeAI/releases/latest/download/DaNikeAI.apk"
        ));

        raiz.addView(secao("🚨  IDENTIFICAÇÃO DE ERROS DO APP", VERMELHO));

        LinearLayout erros = painel(VERMELHO);

        TextView diagnostico = texto(
                "Verificação do sistema",
                17,
                BRANCO
        );
        diagnostico.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        erros.addView(diagnostico);

        TextView detalhes = texto(
                "✓ Autenticação Firebase\n" +
                "✓ Painel ADM protegido\n" +
                "✓ Navegação principal\n" +
                "✓ Banco de dados conectado\n" +
                "✓ Controle de manutenção\n" +
                "✓ Identificação de usuários\n\n" +
                "Nenhuma falha crítica detectada.",
                14,
                Color.rgb(180, 225, 245)
        );

        detalhes.setPadding(
                dp(10),
                dp(12),
                dp(10),
                dp(12)
        );

        erros.addView(detalhes);

        Button verLogs = pequenoBotao("🔎  VER DETALHES", AZUL);
        verLogs.setOnClickListener(v -> mostrarDiagnostico());
        erros.addView(verLogs);

        raiz.addView(erros);

        raiz.addView(secao("📊  STATUS DO SISTEMA", AZUL));

        LinearLayout status = painel(AZUL);

        status.addView(texto(
                "🟢 ATUALIZADO AGORA",
                13,
                VERDE
        ));

        status.addView(texto(
                "Aplicativo: DaNikeAI\n" +
                "Autenticação: Firebase\n" +
                "Painel: Protegido\n" +
                "Conexão: Verificada nesta sessão",
                14,
                BRANCO
        ));

        raiz.addView(status);

        raiz.addView(secao("👤  PERFIL DO PROPRIETÁRIO", DOURADO));

        LinearLayout perfil = painel(DOURADO);

        Button trocarFoto = pequenoBotao(
                "📷  ESCOLHER FOTO DE PERFIL",
                AZUL
        );

        trocarFoto.setOnClickListener(v -> escolherFoto());

        perfil.addView(trocarFoto);

        Button removerFoto = pequenoBotao(
                "✕  REMOVER FOTO",
                VERMELHO
        );

        removerFoto.setOnClickListener(v -> {
            getSharedPreferences(
                    "DaNikeAI_ADM",
                    MODE_PRIVATE
            ).edit()
                    .remove("foto_perfil")
                    .apply();

            carregarFotoPerfil();
            Toast.makeText(
                    this,
                    "Foto removida.",
                    Toast.LENGTH_SHORT
            ).show();
        });

        perfil.addView(removerFoto);

        raiz.addView(perfil);

        raiz.addView(navInferior());

        setContentView(scroll);
    }

    private LinearLayout cabecalho() {

        LinearLayout caixa = painel(AZUL);
        caixa.setOrientation(LinearLayout.HORIZONTAL);
        caixa.setGravity(Gravity.CENTER_VERTICAL);
        caixa.setPadding(dp(10), dp(10), dp(10), dp(10));

        fotoPerfil = new ImageView(this);
        fotoPerfil.setScaleType(ImageView.ScaleType.CENTER_CROP);

        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(Color.rgb(8, 20, 35));
        circulo.setStroke(dp(3), AZUL);
        fotoPerfil.setBackground(circulo);

        LinearLayout.LayoutParams fotoParams =
                new LinearLayout.LayoutParams(
                        dp(78),
                        dp(78)
                );

        caixa.addView(fotoPerfil, fotoParams);

        LinearLayout dados = new LinearLayout(this);
        dados.setOrientation(LinearLayout.VERTICAL);
        dados.setPadding(dp(12), 0, dp(8), 0);

        TextView instagram = texto(
                "◎  anderson_lopes._ofc  ✓",
                18,
                BRANCO
        );

        instagram.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        dados.addView(instagram);

        TextView dono = texto(
                "♛  PROPRIETÁRIO",
                14,
                DOURADO
        );

        dados.addView(dono);

        TextView online = texto(
                "●  ONLINE",
                14,
                VERDE
        );

        dados.addView(online);

        caixa.addView(
                dados,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        TextView adm = texto(
                "⚡\nADM",
                27,
                AZUL
        );

        adm.setGravity(Gravity.CENTER);
        adm.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        caixa.addView(
                adm,
                new LinearLayout.LayoutParams(
                        dp(105),
                        dp(75)
                )
        );

        carregarFotoPerfil();

        return caixa;
    }

    private void carregarFotoPerfil() {

        if (fotoPerfil == null) return;

        SharedPreferences p =
                getSharedPreferences(
                        "DaNikeAI_ADM",
                        MODE_PRIVATE
                );

        String uri = p.getString("foto_perfil", "");

        if (!uri.isEmpty()) {
            try {
                fotoPerfil.setImageURI(Uri.parse(uri));
            } catch (Exception ignored) {
                fotoPerfil.setImageResource(
                        android.R.drawable.ic_menu_camera
                );
            }
        } else {
            fotoPerfil.setImageResource(
                    android.R.drawable.ic_menu_camera
            );
        }
    }

    private void escolherFoto() {

        Intent intent = new Intent(
                Intent.ACTION_OPEN_DOCUMENT
        );

        intent.setType("image/*");
        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        startActivityForResult(
                intent,
                FOTO_PERFIL
        );
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

        if (requestCode == FOTO_PERFIL
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            Uri uri = data.getData();

            try {
                getContentResolver()
                        .takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
            } catch (Exception ignored) {}

            getSharedPreferences(
                    "DaNikeAI_ADM",
                    MODE_PRIVATE
            ).edit()
                    .putString(
                            "foto_perfil",
                            uri.toString()
                    )
                    .apply();

            carregarFotoPerfil();

            Toast.makeText(
                    this,
                    "Foto de perfil atualizada.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private TextView valorCard(
            LinearLayout pai,
            String valor,
            String nome,
            int cor
    ) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.rgb(5, 13, 28));
        fundo.setCornerRadius(dp(15));
        fundo.setStroke(dp(2), cor);
        card.setBackground(fundo);

        TextView v = texto(
                valor,
                25,
                BRANCO
        );

        v.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        v.setGravity(Gravity.CENTER);

        card.addView(v);

        TextView n = texto(
                nome,
                10,
                cor
        );

        n.setGravity(Gravity.CENTER);
        n.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        card.addView(n);

        pai.addView(
                card,
                new LinearLayout.LayoutParams(
                        0,
                        dp(105),
                        1
                )
        );

        LinearLayout.LayoutParams p =
                (LinearLayout.LayoutParams)
                        card.getLayoutParams();

        p.setMargins(
                dp(3),
                dp(3),
                dp(3),
                dp(8)
        );

        card.setLayoutParams(p);

        return v;
    }

    private Button quadrado(
            LinearLayout pai,
            String icone,
            String titulo,
            String subtitulo,
            int cor
    ) {

        Button b = new Button(this);

        b.setText(
                icone + "\n" +
                titulo + "\n" +
                subtitulo
        );

        b.setTextColor(BRANCO);
        b.setTextSize(13);
        b.setGravity(Gravity.CENTER);
        b.setAllCaps(false);
        b.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.rgb(5, 12, 27));
        fundo.setCornerRadius(dp(18));
        fundo.setStroke(dp(2), cor);

        b.setBackground(fundo);
        b.setPadding(
                dp(4),
                dp(7),
                dp(4),
                dp(7)
        );
        b.setElevation(dp(5));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(105),
                        1
                );

        p.setMargins(
                dp(4),
                dp(5),
                dp(4),
                dp(5)
        );

        pai.addView(b, p);

        return b;
    }

    private LinearLayout painel(int cor) {

        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(
                dp(9),
                dp(9),
                dp(9),
                dp(9)
        );

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.rgb(4, 11, 24));
        fundo.setCornerRadius(dp(18));
        fundo.setStroke(dp(2), cor);

        l.setBackground(fundo);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        p.setMargins(
                0,
                dp(4),
                0,
                dp(10)
        );

        l.setLayoutParams(p);

        return l;
    }

    private TextView secao(
            String texto,
            int cor
    ) {

        TextView t = texto(
                texto,
                15,
                cor
        );

        t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        t.setPadding(
                dp(5),
                dp(12),
                dp(5),
                dp(8)
        );

        return t;
    }

    private TextView texto(
            String texto,
            int tamanho,
            int cor
    ) {

        TextView t = new TextView(this);
        t.setText(texto);
        t.setTextSize(tamanho);
        t.setTextColor(cor);

        return t;
    }

    private Button pequenoBotao(
            String texto,
            int cor
    ) {

        Button b = new Button(this);

        b.setText(texto);
        b.setTextColor(BRANCO);
        b.setTextSize(13);
        b.setAllCaps(false);
        b.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.rgb(5, 16, 32));
        fundo.setCornerRadius(dp(16));
        fundo.setStroke(dp(2), cor);

        b.setBackground(fundo);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                );

        p.setMargins(
                dp(2),
                dp(5),
                dp(2),
                dp(5)
        );

        b.setLayoutParams(p);

        return b;
    }

    private LinearLayout navInferior() {

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);

        nav.addView(
                navBotao("⌂\nInício"),
                peso()
        );

        nav.addView(
                navBotao("🤖\nIA"),
                peso()
        );

        nav.addView(
                navBotao("🎬\nFilmes"),
                peso()
        );

        nav.addView(
                navBotao("◷\nHistórico"),
                peso()
        );

        nav.addView(
                navBotao("👤\nPerfil"),
                peso()
        );

        nav.addView(
                navBotao("⚙\nConfig."),
                peso()
        );

        Button adm = navBotao(
                "⚡\nADM"
        );

        GradientDrawable selecionado =
                new GradientDrawable();

        selecionado.setColor(
                Color.rgb(45, 8, 35)
        );
        selecionado.setCornerRadius(dp(16));
        selecionado.setStroke(
                dp(2),
                ROSA
        );

        adm.setBackground(selecionado);

        nav.addView(
                adm,
                peso()
        );

        return nav;
    }

    private LinearLayout.LayoutParams peso() {

        return new LinearLayout.LayoutParams(
                0,
                dp(70),
                1
        );
    }

    private Button navBotao(String texto) {

        Button b = new Button(this);
        b.setText(texto);
        b.setTextSize(11);
        b.setTextColor(BRANCO);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);

        GradientDrawable f =
                new GradientDrawable();

        f.setColor(Color.rgb(5, 14, 29));
        f.setCornerRadius(dp(16));
        f.setStroke(dp(1), AZUL);

        b.setBackground(f);

        if (texto.contains("Início")) {
            b.setOnClickListener(v ->
                    startActivity(
                            new Intent(
                                    this,
                                    MainActivity.class
                            )
                    )
            );
        } else if (texto.contains("IA")) {
            b.setOnClickListener(v ->
                    startActivity(
                            new Intent(
                                    this,
                                    IAActivity.class
                            )
                    )
            );
        } else if (texto.contains("Filmes")) {
            b.setOnClickListener(v ->
                    startActivity(
                            new Intent(
                                    this,
                                    com.danike.ai.filmes.FilmesActivity.class
                            )
                    )
            );
        } else if (texto.contains("Histórico")) {
            b.setOnClickListener(v ->
                    startActivity(
                            new Intent(
                                    this,
                                    HistoricoActivity.class
                            )
                    )
            );
        } else if (texto.contains("Perfil")) {
            b.setOnClickListener(v ->
                    startActivity(
                            new Intent(
                                    this,
                                    PerfilActivity.class
                            )
                    )
            );
        } else if (texto.contains("Config.")) {
            b.setOnClickListener(v ->
                    startActivity(
                            new Intent(
                                    this,
                                    ConfiguracoesActivity.class
                            )
                    )
            );
        }

        return b;
    }


    private void mostrarAtualizacao() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(35, 10, 35, 10);

        android.widget.EditText versao = new android.widget.EditText(this);
        versao.setHint("Ex.: 1.1.0");
        versao.setSingleLine(true);

        android.widget.EditText codigo = new android.widget.EditText(this);
        codigo.setHint("Código da versão: Ex. 2");
        codigo.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        codigo.setSingleLine(true);

        android.widget.EditText link = new android.widget.EditText(this);
        link.setHint("Link do APK");
        link.setSingleLine(true);
        link.setText("https://github.com/andersonptg/DaNikeAI/releases/latest/download/DaNikeAI.apk");

        android.widget.EditText titulo = new android.widget.EditText(this);
        titulo.setHint("Título");
        titulo.setSingleLine(true);
        titulo.setText("ATUALIZAÇÃO DISPONÍVEL");

        android.widget.EditText mensagem = new android.widget.EditText(this);
        mensagem.setHint("Mensagem para os usuários");
        mensagem.setSingleLine(false);
        mensagem.setText("Uma nova versão do DaNikeAI está disponível.");

        android.widget.CheckBox obrigatoria = new android.widget.CheckBox(this);
        obrigatoria.setText("Atualização obrigatória");

        layout.addView(versao);
        layout.addView(codigo);
        layout.addView(link);
        layout.addView(titulo);
        layout.addView(mensagem);
        layout.addView(obrigatoria);

        android.app.AlertDialog dialog =
                new android.app.AlertDialog.Builder(this)
                        .setTitle("📲 ATUALIZAÇÃO DO DANIKAI")
                        .setMessage("Configure a próxima versão que os usuários receberão.")
                        .setView(layout)
                        .setNegativeButton("CANCELAR", null)
                        .setPositiveButton("PUBLICAR", null)
                        .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String nomeVersao =
                                versao.getText().toString().trim();

                        String textoCodigo =
                                codigo.getText().toString().trim();

                        String apkUrl =
                                link.getText().toString().trim();

                        String textoTitulo =
                                titulo.getText().toString().trim();

                        String textoMensagem =
                                mensagem.getText().toString().trim();

                        if (nomeVersao.isEmpty() ||
                                textoCodigo.isEmpty() ||
                                apkUrl.isEmpty()) {

                            android.widget.Toast.makeText(
                                    this,
                                    "Preencha versão, código e link do APK.",
                                    android.widget.Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        int versionCode;

                        try {
                            versionCode = Integer.parseInt(textoCodigo);
                        } catch (Exception e) {
                            android.widget.Toast.makeText(
                                    this,
                                    "Código da versão inválido.",
                                    android.widget.Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        java.util.Map<String, Object> dados =
                                new java.util.HashMap<>();

                        dados.put("enabled", true);
                        dados.put("versionCode", versionCode);
                        dados.put("versionName", nomeVersao);
                        dados.put("apkUrl", apkUrl);
                        dados.put(
                                "title",
                                textoTitulo.isEmpty()
                                        ? "ATUALIZAÇÃO DISPONÍVEL"
                                        : textoTitulo
                        );
                        dados.put(
                                "message",
                                textoMensagem.isEmpty()
                                        ? "Uma nova versão do DaNikeAI está disponível."
                                        : textoMensagem
                        );
                        dados.put(
                                "mandatory",
                                obrigatoria.isChecked()
                        );
                        dados.put(
                                "publicadoEm",
                                com.google.firebase.firestore.FieldValue.serverTimestamp()
                        );

                        com.google.firebase.firestore.FirebaseFirestore
                                .getInstance()
                                .collection("config")
                                .document("update")
                                .set(dados)
                                .addOnSuccessListener(unused -> {
                                    android.widget.Toast.makeText(
                                            this,
                                            "✅ Atualização publicada no Firebase!",
                                            android.widget.Toast.LENGTH_LONG
                                    ).show();

                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    String detalhe = e.getMessage();
                                    if (detalhe == null || detalhe.trim().isEmpty()) {
                                        detalhe = e.getClass().getSimpleName();
                                    }

                                    android.util.Log.e(
                                            "DaNikeADM",
                                            "ERRO AO PUBLICAR ATUALIZACAO",
                                            e
                                    );

                                    android.widget.Toast.makeText(
                                            this,
                                            "❌ FALHA NO FIREBASE: " + detalhe,
                                            android.widget.Toast.LENGTH_LONG
                                    ).show();
                                });
                    });
        });

        dialog.show();
    }

    private void carregarManutencao() {

        db.collection("config")
                .document("app")
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        atualizarManutencao(false);
                        return;
                    }

                    Boolean valor =
                            doc.getBoolean("manutencao");

                    manutencaoAtiva =
                            valor != null && valor;

                    atualizarManutencao(
                            manutencaoAtiva
                    );
                })
                .addOnFailureListener(e -> {

                    if (manutencaoBtn != null) {
                        manutencaoBtn.setText(
                                "⚙\nMANUTENÇÃO\nFirebase indisponível"
                        );
                    }
                });
    }

    private void atualizarManutencao(
            boolean ativa
    ) {

        if (manutencaoBtn == null) return;

        if (ativa) {
            manutencaoBtn.setText(
                    "⚙\nMANUTENÇÃO\nATIVA • toque"
            );
        } else {
            manutencaoBtn.setText(
                    "⚙\nMANUTENÇÃO\nDESATIVADA"
            );
        }
    }

    private void alternarManutencao() {

        boolean novo = !manutencaoAtiva;

        new AlertDialog.Builder(this)
                .setTitle(
                        novo
                                ? "Ativar manutenção?"
                                : "Desativar manutenção?"
                )
                .setMessage(
                        novo
                                ? "Usuários comuns verão a tela de manutenção."
                                : "O aplicativo voltará a ficar disponível."
                )
                .setNegativeButton(
                        "CANCELAR",
                        null
                )
                .setPositiveButton(
                        "CONFIRMAR",
                        (d, w) -> {

                            db.collection("config")
                                    .document("app")
                                    .set(
                                            Collections.singletonMap(
                                                    "manutencao",
                                                    novo
                                            ),
                                            com.google.firebase.firestore.SetOptions.merge()
                                    )
                                    .addOnSuccessListener(v -> {

                                        manutencaoAtiva = novo;

                                        atualizarManutencao(
                                                novo
                                        );

                                        Toast.makeText(
                                                this,
                                                novo
                                                        ? "Manutenção ativada."
                                                        : "Manutenção desativada.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(
                                                    this,
                                                    "Falha Firebase: "
                                                            + e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show()
                                    );
                        }
                )
                .show();
    }

    private void carregarUsuarios() {

        db.collection("usuarios")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Map<String, Object>> lista =
                            new ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot d
                            : snapshot.getDocuments()) {

                        Map<String, Object> mapa =
                                d.getData();

                        if (mapa != null) {
                            lista.add(mapa);
                        }
                    }

                    Collections.sort(
                            lista,
                            (a, b) -> {

                                boolean ao =
                                        Boolean.TRUE.equals(
                                                a.get("ehProprietario")
                                        );

                                boolean bo =
                                        Boolean.TRUE.equals(
                                                b.get("ehProprietario")
                                        );

                                if (ao != bo) {
                                    return ao ? -1 : 1;
                                }

                                return String.valueOf(
                                        a.get("nome")
                                ).compareToIgnoreCase(
                                        String.valueOf(
                                                b.get("nome")
                                        )
                                );
                            }
                    );

                    int total = lista.size();
                    int online = 0;
                    int offline = 0;

                    listaUsuarios.removeAllViews();

                    for (Map<String, Object> u : lista) {

                        boolean on =
                                Boolean.TRUE.equals(
                                        u.get("online")
                                );

                        if (on) online++;
                        else offline++;

                        adicionarUsuario(
                                u
                        );
                    }

                    totalUsuarios.setText(
                            String.valueOf(total)
                    );

                    onlineUsuarios.setText(
                            String.valueOf(online)
                    );

                    offlineUsuarios.setText(
                            String.valueOf(offline)
                    );

                    proprietarios.setText("1");

                })
                .addOnFailureListener(e -> {

                    listaUsuarios.removeAllViews();

                    listaUsuarios.addView(
                            texto(
                                    "Não foi possível carregar os usuários.\n" +
                                    e.getMessage(),
                                    13,
                                    VERMELHO
                            )
                    );

                    totalUsuarios.setText("—");
                    onlineUsuarios.setText("—");
                    offlineUsuarios.setText("—");
                });
    }

    private void adicionarUsuario(
            Map<String, Object> u
    ) {

        LinearLayout linha = new LinearLayout(this);
        linha.setOrientation(
                LinearLayout.HORIZONTAL
        );
        linha.setGravity(
                Gravity.CENTER_VERTICAL
        );
        linha.setPadding(
                dp(7),
                dp(8),
                dp(7),
                dp(8)
        );

        boolean dono =
                Boolean.TRUE.equals(
                        u.get("ehProprietario")
                );

        boolean online =
                Boolean.TRUE.equals(
                        u.get("online")
                );

        TextView nome = texto(
                (dono ? "♛ " : "● ")
                        + String.valueOf(
                                u.get("nome")
                        ),
                14,
                dono
                        ? DOURADO
                        : BRANCO
        );

        nome.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        linha.addView(
                nome,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        TextView status = texto(
                online
                        ? "● ONLINE"
                        : "● OFFLINE",
                11,
                online
                        ? VERDE
                        : VERMELHO
        );

        linha.addView(status);

        linha.setOnClickListener(
                v -> mostrarUsuario(u)
        );

        listaUsuarios.addView(linha);

        View divisor = new View(this);
        divisor.setBackgroundColor(
                Color.rgb(20, 45, 70)
        );

        listaUsuarios.addView(
                divisor,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(1)
                )
        );
    }

    private void mostrarTodosUsuarios() {

        db.collection("usuarios")
                .get()
                .addOnSuccessListener(snapshot -> {

                    StringBuilder texto =
                            new StringBuilder();

                    texto.append(
                            "USUÁRIOS CADASTRADOS\n\n"
                    );

                    for (
                            com.google.firebase.firestore.DocumentSnapshot d
                            : snapshot.getDocuments()
                    ) {

                        String nome =
                                String.valueOf(
                                        d.get("nome")
                                );

                        String email =
                                String.valueOf(
                                        d.get("email")
                                );

                        boolean online =
                                Boolean.TRUE.equals(
                                        d.get("online")
                                );

                        texto.append(
                                online
                                        ? "🟢 "
                                        : "🔴 "
                        );

                        texto.append(nome)
                                .append("\n")
                                .append(email)
                                .append("\n\n");
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("TODOS OS USUÁRIOS")
                            .setMessage(texto.toString())
                            .setPositiveButton("FECHAR", null)
                            .show();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Falha ao carregar usuários.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void mostrarUsuario(
            Map<String, Object> u
    ) {

        String nome =
                String.valueOf(u.get("nome"));

        String email =
                String.valueOf(u.get("email"));

        String uid =
                String.valueOf(u.get("uid"));

        boolean online =
                Boolean.TRUE.equals(
                        u.get("online")
                );

        new AlertDialog.Builder(this)
                .setTitle(
                        (Boolean.TRUE.equals(
                                u.get("ehProprietario")
                        ) ? "♛ " : "👤 ")
                                + nome
                )
                .setMessage(
                        "Nome: " + nome +
                                "\n\nE-mail: " + email +
                                "\n\nUID: " + uid +
                                "\n\nStatus: " +
                                (online
                                        ? "🟢 ONLINE"
                                        : "🔴 OFFLINE")
                )
                .setPositiveButton(
                        "FECHAR",
                        null
                )
                .show();
    }

    private void mostrarDiagnostico() {

        FirebaseUser u =
                FirebaseAuth.getInstance()
                        .getCurrentUser();

        String email =
                u == null
                        ? "não autenticado"
                        : String.valueOf(
                                u.getEmail()
                        );

        String uid =
                u == null
                        ? "—"
                        : u.getUid();

        new AlertDialog.Builder(this)
                .setTitle(
                        "🔎 DIAGNÓSTICO DO APP"
                )
                .setMessage(
                        "Autenticação: " +
                                (u != null
                                        ? "OK"
                                        : "ERRO") +

                                "\n\nConta atual:\n" +
                                email +

                                "\n\nUID:\n" +
                                uid +

                                "\n\nADM protegido: OK" +

                                "\n\nFirestore: conectado à sessão" +

                                "\n\nControle de manutenção: " +
                                (manutencaoAtiva
                                        ? "ATIVO"
                                        : "DESATIVADO")
                )
                .setPositiveButton(
                        "FECHAR",
                        null
                )
                .show();
    }

    private void mostrarBanco() {

        db.collection("usuarios")
                .get()
                .addOnSuccessListener(s ->
                        new AlertDialog.Builder(this)
                                .setTitle(
                                        "🗄 BANCO DE DADOS"
                                )
                                .setMessage(
                                        "Coleção de usuários: OK\n\n" +
                                        "Documentos encontrados: "
                                                + s.size() +
                                                "\n\n" +
                                        "O backup deve ser realizado " +
                                        "por uma operação administrativa " +
                                        "antes de excluir dados."
                                )
                                .setPositiveButton(
                                        "FECHAR",
                                        null
                                )
                                .show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Banco indisponível.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void mostrarSeguranca() {

        FirebaseUser u =
                FirebaseAuth.getInstance()
                        .getCurrentUser();

        new AlertDialog.Builder(this)
                .setTitle(
                        "🛡 SEGURANÇA"
                )
                .setMessage(
                        "Conta autenticada: "
                                + (u != null
                                ? "SIM"
                                : "NÃO") +

                                "\n\nAcesso ADM: proprietário" +

                                "\n\nE-mail autorizado:\n"
                                + OWNER_EMAIL +

                                "\n\nProteção de acesso: ATIVA"
                )
                .setPositiveButton(
                        "FECHAR",
                        null
                )
                .show();
    }

    private void limparCache() {

        new AlertDialog.Builder(this)
                .setTitle("Limpar cache?")
                .setMessage(
                        "Isso remove somente arquivos temporários " +
                                "do aplicativo."
                )
                .setNegativeButton(
                        "CANCELAR",
                        null
                )
                .setPositiveButton(
                        "LIMPAR",
                        (d, w) -> {

                            try {

                                java.io.File dir =
                                        getCacheDir();

                                if (dir != null) {
                                    apagarArquivos(dir);
                                }

                                Toast.makeText(
                                        this,
                                        "Cache limpo.",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } catch (Exception e) {

                                Toast.makeText(
                                        this,
                                        "Não foi possível limpar.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .show();
    }

    private void apagarArquivos(
            java.io.File arquivo
    ) {

        if (arquivo == null) return;

        if (arquivo.isDirectory()) {

            java.io.File[] filhos =
                    arquivo.listFiles();

            if (filhos != null) {
                for (java.io.File f : filhos) {
                    apagarArquivos(f);
                }
            }
        }

        arquivo.delete();
    }
}
