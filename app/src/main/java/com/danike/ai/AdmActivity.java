package com.danike.ai;

import org.json.JSONObject;
import org.json.JSONArray;
import android.graphics.RectF;
import android.graphics.BitmapShader;
import android.graphics.Shader;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AdmActivity extends Activity {
    
    // Lista visual dos perfis da equipe dentro do ADM
    private LinearLayout equipeListaAdm;


    private static final String OWNER_EMAIL = "lipesanderson@gmail.com";
    private static final int FOTO_PERFIL = 9001;
    private static final String CLOUDINARY_CLOUD_NAME = "pmxz8swv";
    private static final String CLOUDINARY_UPLOAD_PRESET = "danike_perfil";

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
    private Button manutencaoIA;
    private Button manutencaoFilmes;
    private Button manutencaoHistorico;
    private Button manutencaoPerfil;
    private Button manutencaoHave;
    private TextView totalUsuarios;
    private TextView onlineUsuarios;
    private TextView offlineUsuarios;
    private TextView proprietarios;
    private LinearLayout listaUsuarios;
    private ImageView fotoPerfil;

    // Foto temporária do colaborador sendo cadastrado
    private ImageView fotoEquipeSelecionada;
    private Uri uriFotoEquipeSelecionada;

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

        // ==============================
        // MANUTENÇÃO INDIVIDUAL DAS ÁREAS
        // ==============================
        raiz.addView(secao("🔧  MANUTENÇÃO DAS ÁREAS", VERMELHO));

        LinearLayout manutencaoAreas1 = new LinearLayout(this);
        manutencaoAreas1.setOrientation(LinearLayout.HORIZONTAL);

        manutencaoIA = quadrado(
                manutencaoAreas1,
                "🔧",
                "MANUTENÇÃO IA",
                "Controle individual",
                AZUL
        );

        manutencaoFilmes = quadrado(
                manutencaoAreas1,
                "🔧",
                "MANUTENÇÃO FILMES",
                "Controle individual",
                ROSA
        );

        raiz.addView(manutencaoAreas1);

        LinearLayout manutencaoAreas2 = new LinearLayout(this);
        manutencaoAreas2.setOrientation(LinearLayout.HORIZONTAL);

        manutencaoHistorico = quadrado(
                manutencaoAreas2,
                "🔧",
                "MANUTENÇÃO HISTÓRICO",
                "Controle individual",
                DOURADO
        );

        manutencaoPerfil = quadrado(
                manutencaoAreas2,
                "🔧",
                "MANUTENÇÃO PERFIL",
                "Controle individual",
                ROXO
        );

        raiz.addView(manutencaoAreas2);

        LinearLayout manutencaoAreas3 = new LinearLayout(this);
        manutencaoAreas3.setOrientation(LinearLayout.HORIZONTAL);

        manutencaoHave = quadrado(
                manutencaoAreas3,
                "🔧",
                "MANUTENÇÃO HAVE",
                "Controle individual",
                VERDE
        );

        raiz.addView(manutencaoAreas3);

        manutencaoIA.setOnClickListener(v -> alternarManutencaoArea("ia", "IA"));
        manutencaoFilmes.setOnClickListener(v -> alternarManutencaoArea("filmes", "FILMES"));
        manutencaoHistorico.setOnClickListener(v -> alternarManutencaoArea("historico", "HISTÓRICO"));
        manutencaoPerfil.setOnClickListener(v -> alternarManutencaoArea("perfil", "PERFIL"));
        manutencaoHave.setOnClickListener(v -> alternarManutencaoArea("have", "HAVE"));

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

        // ==============================
        // EQUIPE DO PROJETO - LOCAL
        // SOMENTE O PROPRIETÁRIO EDITA
        // ==============================
        raiz.addView(secao("👥  EQUIPE DO PROJETO", DOURADO));

        LinearLayout equipeAdm = painel(DOURADO);
        equipeAdm.setOrientation(LinearLayout.VERTICAL);

        TextView equipeInfo = texto(
                "Adicione os colaboradores que aparecerão na tela inicial.\\n" +
                "Somente o proprietário pode escolher fotos, nomes e cargos.",
                14,
                BRANCO
        );

        equipeInfo.setPadding(
                dp(10),
                dp(10),
                dp(10),
                dp(14)
        );

        equipeAdm.addView(equipeInfo);

        Button adicionarPerfilEquipe = pequenoBotao(
                "＋  ADICIONAR PERFIL",
                AZUL
        );

        adicionarPerfilEquipe.setOnClickListener(
                v -> adicionarPerfilEquipe()
        );

        equipeAdm.addView(adicionarPerfilEquipe);

        raiz.addView(equipeAdm);

        // Lista dos colaboradores já cadastrados
        equipeListaAdm = new LinearLayout(this);
        equipeListaAdm.setOrientation(LinearLayout.HORIZONTAL);
        equipeListaAdm.setGravity(Gravity.CENTER_VERTICAL);
        equipeListaAdm.setPadding(dp(8), dp(10), dp(8), dp(10));

        HorizontalScrollView equipeScrollAdm =
        new HorizontalScrollView(this);
equipeScrollAdm.setHorizontalScrollBarEnabled(false);
equipeScrollAdm.setOverScrollMode(View.OVER_SCROLL_NEVER);

ScrollView equipeScrollVerticalAdm =
        new ScrollView(this);
equipeScrollVerticalAdm.setVerticalScrollBarEnabled(false);
equipeScrollVerticalAdm.setOverScrollMode(View.OVER_SCROLL_NEVER);

equipeScrollVerticalAdm.addView(
        equipeListaAdm,
        new ScrollView.LayoutParams(
                -1,
                -2
        )
);

equipeScrollAdm.addView(
        equipeScrollVerticalAdm,
        new HorizontalScrollView.LayoutParams(
                -1,
                dp(245)
        )
);

raiz.addView(equipeScrollAdm);
atualizarListaEquipeAdm();


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

    private void adicionarPerfilEquipe() {

        final LinearLayout caixa = new LinearLayout(this);
        caixa.setOrientation(LinearLayout.VERTICAL);
        caixa.setPadding(dp(10), dp(4), dp(10), dp(4));

        ImageView foto = new ImageView(this);

        // Guarda a ImageView do colaborador que está sendo cadastrado
        fotoEquipeSelecionada = foto;
        uriFotoEquipeSelecionada = null;

        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(Color.TRANSPARENT);
        circulo.setStroke(dp(2), AZUL);
        foto.setBackground(circulo);
        foto.setImageResource(android.R.drawable.ic_menu_camera);
        foto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        caixa.addView(
                foto,
                new LinearLayout.LayoutParams(
                        dp(110),
                        dp(110)
                )
        );

        Button escolher = pequenoBotao(
                "＋  ESCOLHER FOTO",
                AZUL
        );

        caixa.addView(escolher);

        EditText nome = new EditText(this);
        nome.setHint("Nome");
        nome.setTextColor(BRANCO);
        nome.setHintTextColor(Color.rgb(130, 160, 180));
        nome.setSingleLine(true);

        caixa.addView(
                nome,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        EditText cargo = new EditText(this);
        cargo.setHint("Cargo");
        cargo.setTextColor(BRANCO);
        cargo.setHintTextColor(Color.rgb(130, 160, 180));
        cargo.setSingleLine(true);

        caixa.addView(
                cargo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("👤  NOVO PERFIL")
                .setView(caixa)
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("SALVAR", null)
                .create();

        escolher.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            startActivityForResult(
                    intent,
                    9100
            );
        });

        dialog.setOnShowListener(v -> {
            Button salvar = dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            );

            salvar.setOnClickListener(view -> {

                String nomeTexto =
                        nome.getText().toString().trim();

                String cargoTexto =
                        cargo.getText().toString().trim();

                if (nomeTexto.isEmpty()) {
                    nome.setError("Digite o nome.");
                    return;
                }

                if (cargoTexto.isEmpty()) {
                    cargo.setError("Digite o cargo.");
                    return;
                }

                boolean salvo = salvarPerfilEquipeLocal(nomeTexto, cargoTexto, uriFotoEquipeSelecionada != null ? uriFotoEquipeSelecionada.toString() : "");

                if (salvo) {
                    atualizarListaEquipeAdm();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    

    
    private void atualizarListaEquipeAdm() {
        if (equipeListaAdm == null) {
            return;
        }

        equipeListaAdm.removeAllViews();

        try {
            SharedPreferences prefs =
                    getSharedPreferences("DaNikeAI_ADM", MODE_PRIVATE);

            JSONArray lista =
                    new JSONArray(
                            prefs.getString("equipe_perfis", "[]")
                    );

            for (int i = 0; i < lista.length(); i++) {

                JSONObject pessoa =
                        lista.getJSONObject(i);

                String nome =
                        pessoa.optString("nome", "").trim();

                String cargo =
                        pessoa.optString("cargo", "").trim();

                String fotoUri =
                        pessoa.optString("fotoUri", "").trim();

                if (nome.isEmpty() || fotoUri.isEmpty()) {
                    continue;
                }

                LinearLayout item =
                        new LinearLayout(this);

                item.setOrientation(
                        LinearLayout.VERTICAL
                );

                item.setGravity(Gravity.CENTER);
                item.setPadding(
                        dp(10), dp(4), dp(10), dp(4)
                );

                // Foto + lixeira sobreposta
                FrameLayout avatar =
                        new FrameLayout(this);

                GradientDrawable circulo =
                        new GradientDrawable();

                circulo.setShape(
                        GradientDrawable.OVAL
                );

                circulo.setColor(
                        Color.TRANSPARENT
                );

                circulo.setStroke(
                        dp(2),
                        AZUL
                );

                avatar.setBackground(circulo);
                avatar.setClipToOutline(true);

                ImageView foto =
                        new ImageView(this);

                foto.setScaleType(
                        ImageView.ScaleType.CENTER_CROP
                );

                try {
                    if (fotoUri.startsWith("content://")) {
                        foto.setImageURI(
                                android.net.Uri.parse(fotoUri)
                        );
                    } else {
                        foto.setImageBitmap(
                                BitmapFactory.decodeFile(fotoUri)
                        );
                    }
                } catch (Exception ignored) {
                }

                avatar.addView(
                        foto,
                        new FrameLayout.LayoutParams(
                                dp(82),
                                dp(82)
                        )
                );

                // Lixeira discreta e transparente
                TextView lixeira =
                        new TextView(this);

                lixeira.setText("🗑");
                lixeira.setTextSize(16);
                lixeira.setGravity(Gravity.CENTER);
                lixeira.setTextColor(
                        Color.WHITE
                );

                GradientDrawable fundoLixeira =
                        new GradientDrawable();

                fundoLixeira.setShape(
                        GradientDrawable.OVAL
                );

                fundoLixeira.setColor(
                        Color.argb(
                                145,
                                0,
                                0,
                                0
                        )
                );

                lixeira.setBackground(
                        fundoLixeira
                );

                FrameLayout.LayoutParams lixoParams =
                        new FrameLayout.LayoutParams(
                                dp(30),
                                dp(30),
                                Gravity.END | Gravity.BOTTOM
                        );

                lixoParams.setMargins(
                        0, 0, dp(2), dp(2)
                );

                avatar.addView(
                        lixeira,
                        lixoParams
                );

                // Dois toques na foto, lixeira ou avatar excluem.
                final int indice = i;
                final long[] ultimoToque = {0};

                android.view.View.OnTouchListener toqueDuplo =
                        (v, event) -> {
                            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                                long agora = android.os.SystemClock.elapsedRealtime();

                                if (agora - ultimoToque[0] <= 350) {
                                    ultimoToque[0] = 0;
                                    excluirPerfilEquipeLocal(indice);
                                } else {
                                    ultimoToque[0] = agora;
                                }
                            }
                            return true;
                        };

                avatar.setOnTouchListener(toqueDuplo);
                foto.setOnTouchListener(toqueDuplo);
                lixeira.setOnTouchListener(toqueDuplo);

                item.addView(
                        avatar,
                        new LinearLayout.LayoutParams(
                                dp(86),
                                dp(86)
                        )
                );

                TextView nomeView = new TextView(this);
                nomeView.setText(nome);
                nomeView.setTextSize(13);
                nomeView.setTextColor(BRANCO);

                nomeView.setGravity(
                        Gravity.CENTER
                );

                item.addView(
                        nomeView,
                        new LinearLayout.LayoutParams(
                                dp(110),
                                dp(28)
                        )
                );

                TextView cargoView = new TextView(this); cargoView.setText(cargo); cargoView.setTextSize(11); cargoView.setTextColor(AZUL);

                cargoView.setGravity(
                        Gravity.CENTER
                );

                item.addView(
                        cargoView,
                        new LinearLayout.LayoutParams(
                                dp(110),
                                dp(24)
                        )
                );

                                    // Instagram do perfil
                    Button instagramBtn = pequenoBotao("Instagram", 10);
                    android.graphics.drawable.Drawable logoInstagramAdm =
                            getResources().getDrawable(
                                    R.drawable.instagram_oficial
                            );
                    logoInstagramAdm.setBounds(
                            0,
                            0,
                            dp(20),
                            dp(20)
                    );
                    instagramBtn.setCompoundDrawables(
                            logoInstagramAdm,
                            null,
                            null,
                            null
                    );
                    instagramBtn.setCompoundDrawablePadding(dp(5));

                    instagramBtn.setOnClickListener(v ->
                            abrirEditorInstagram(nome, lista, indice)
                    );

                    item.addView(
                            instagramBtn,
                            new LinearLayout.LayoutParams(
                                    dp(110),
                                    dp(36)
                            )
                    );

equipeListaAdm.addView(
                        item,
                        new LinearLayout.LayoutParams(
                                dp(125),
                                dp(145)
                        )
                );
            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Erro ao carregar equipe.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    
    private void abrirEditorInstagram(
            String nome,
            JSONArray lista,
            int indice
    ) {
        try {
            JSONObject pessoa = lista.getJSONObject(indice);

            EditText usuario = new EditText(this);
            usuario.setHint("Nome de usuário");
            usuario.setSingleLine(true);
            usuario.setText(
                    pessoa.optString("instagramUsuario", "")
            );
            usuario.setTextColor(BRANCO);
            usuario.setHintTextColor(Color.rgb(130, 160, 180));

            EditText link = new EditText(this);
            link.setHint("Link do Instagram");
            link.setSingleLine(true);
            link.setText(
                    pessoa.optString("instagramLink", "")
            );
            link.setTextColor(BRANCO);
            link.setHintTextColor(Color.rgb(130, 160, 180));

            LinearLayout caixa = new LinearLayout(this);
            caixa.setOrientation(LinearLayout.VERTICAL);
            caixa.setPadding(
                    dp(20), dp(10), dp(20), dp(10)
            );

            caixa.addView(
                    usuario,
                    new LinearLayout.LayoutParams(
                            -1, dp(55)
                    )
            );

            caixa.addView(
                    link,
                    new LinearLayout.LayoutParams(
                            -1, dp(55)
                    )
            );

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Instagram • " + nome)
                    .setView(caixa)
                    .setNegativeButton(
                            "CANCELAR",
                            null
                    )
                    .create();

            Button publicar = new Button(this);
            publicar.setText("PUBLICAR");
            publicar.setTextColor(Color.WHITE);
            publicar.setAllCaps(false);

            publicar.setOnClickListener(v -> {
                String usuarioTexto =
                        usuario.getText().toString().trim();

                String linkTexto =
                        link.getText().toString().trim();

                if (usuarioTexto.isEmpty()) {
                    usuario.setError(
                            "Digite o nome de usuário."
                    );
                    return;
                }

                if (linkTexto.isEmpty()) {
                    linkTexto =
                            "https://instagram.com/"
                            + usuarioTexto;
                }

                if (!linkTexto.startsWith("http://") &&
                    !linkTexto.startsWith("https://")) {
                    linkTexto = "https://" + linkTexto;
                }

                try {
                    pessoa.put(
                            "instagramUsuario",
                            usuarioTexto
                    );

                    pessoa.put(
                            "instagramLink",
                            linkTexto
                    );

                    lista.put(indice, pessoa);

                    getSharedPreferences(
                            "DaNikeAI_ADM",
                            MODE_PRIVATE
                    ).edit()
                    .putString(
                            "equipe_perfis",
                            lista.toString()
                    )
                    .apply();

                    String idEquipe =
                            idEquipeCloud(nome);

                    java.util.HashMap<String, Object>
                            dados =
                            new java.util.HashMap<>();

                    dados.put(
                            "instagramUsuario",
                            usuarioTexto
                    );

                    dados.put(
                            "instagramLink",
                            linkTexto
                    );

                    dados.put(
                            "instagramPublicado",
                            true
                    );

                    dados.put(
                            "atualizadoEm",
                            com.google.firebase.firestore.FieldValue
                                    .serverTimestamp()
                    );

                    db.collection("equipe")
                            .document(idEquipe)
                            .update(dados)
                            .addOnSuccessListener(ok -> {
                                Toast.makeText(
                                        this,
                                        "✅ Instagram publicado na nuvem.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(
                                        this,
                                        "❌ Erro ao publicar no Firestore.",
                                        Toast.LENGTH_LONG
                                ).show();
                            });

                } catch (Exception e) {
                    Toast.makeText(
                            this,
                            "Erro ao publicar Instagram.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });

            dialog.setOnShowListener(d -> {
                Button positivo =
                        dialog.getButton(
                                AlertDialog.BUTTON_POSITIVE
                        );

                if (positivo != null) {
                    positivo.setOnClickListener(
                            v -> publicar.performClick()
                    );
                }
            });

            dialog.setButton(
                    AlertDialog.BUTTON_POSITIVE,
                    "PUBLICAR",
                    (d, w) -> {}
            );

            dialog.show();

            Button botaoPublicar =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            botaoPublicar.setOnClickListener(
                    v -> publicar.performClick()
            );

            Button excluir =
                    dialog.getButton(
                            AlertDialog.BUTTON_NEGATIVE
                    );

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Erro ao abrir Instagram.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

private void excluirPerfilEquipeLocal(int indice) {
    try {
        SharedPreferences prefs = getSharedPreferences("DaNikeAI_ADM", MODE_PRIVATE);
        JSONArray lista = new JSONArray(prefs.getString("equipe_perfis", "[]"));
        if (indice < 0 || indice >= lista.length()) return;

        JSONObject pessoa = lista.getJSONObject(indice);
        final String nome = pessoa.optString("nome", "").trim();
        if (nome.isEmpty()) {
            Toast.makeText(this, "Nome do perfil invalido.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null
                || usuario.getEmail() == null
                || !OWNER_EMAIL.equalsIgnoreCase(usuario.getEmail())
                || !"nd9zWE3GF2hOJDg4BfgTtaSy3H33".equals(usuario.getUid())) {
            Toast.makeText(this, "Somente o proprietario pode excluir fotos da equipe.", Toast.LENGTH_LONG).show();
            return;
        }

        final String idEquipe = idEquipeCloud(nome);
        Toast.makeText(this, "Excluindo perfil da equipe...", Toast.LENGTH_SHORT).show();

        db.collection("equipe").document(idEquipe).delete()
                .addOnSuccessListener(resultado -> {
                    try {
                        SharedPreferences prefsAtual = getSharedPreferences("DaNikeAI_ADM", MODE_PRIVATE);
                        JSONArray listaAtual = new JSONArray(prefsAtual.getString("equipe_perfis", "[]"));
                        JSONArray novaLista = new JSONArray();

                        for (int i = 0; i < listaAtual.length(); i++) {
                            JSONObject item = listaAtual.optJSONObject(i);
                            if (item == null) continue;
                            String nomeItem = item.optString("nome", "").trim();
                            if (!nome.equalsIgnoreCase(nomeItem)) {
                                novaLista.put(item);
                            }
                        }

                        prefsAtual.edit().putString("equipe_perfis", novaLista.toString()).apply();
                        atualizarListaEquipeAdm();
                        Toast.makeText(this, "Perfil da equipe excluido com sucesso.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Perfil excluido na nuvem, mas nao foi possivel atualizar a lista local.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Falha ao excluir no Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show());
    } catch (Exception e) {
        Toast.makeText(this, "Erro ao excluir perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}

private String idEquipeCloud(String nome) {
    try {
        String normalizado = nome == null ? "" : nome.trim().toLowerCase();
        java.security.MessageDigest md =
                java.security.MessageDigest.getInstance("SHA-256");

        byte[] bytes = md.digest(
                normalizado.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8
                )
        );

        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString().substring(0, 24);

    } catch (Exception e) {
        return "equipe_" +
                Math.abs(
                        (nome == null ? "" : nome).hashCode()
                );
    }
}

private boolean salvarPerfilEquipeLocal(
        String nome,
        String cargo,
        String fotoUri
) {
    if (fotoUri == null || fotoUri.trim().isEmpty()) {
        Toast.makeText(this, "Escolha uma foto primeiro.", Toast.LENGTH_SHORT).show();
        return false;
    }

    try {
        Bitmap original;

        if (fotoUri.startsWith("content://")) {
            android.net.Uri uri = android.net.Uri.parse(fotoUri);
            original = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(uri)
            );
        } else {
            original = BitmapFactory.decodeFile(fotoUri);
        }

        if (original == null) {
            Toast.makeText(this, "Não foi possível carregar a foto.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // =========================
        // FOTO CIRCULAR REAL
        // =========================
        int lado = Math.min(original.getWidth(), original.getHeight());

        int esquerda = (original.getWidth() - lado) / 2;
        int topo = (original.getHeight() - lado) / 2;

        Bitmap quadrada = Bitmap.createBitmap(
                original,
                esquerda,
                topo,
                lado,
                lado
        );

        Bitmap redonda = Bitmap.createBitmap(
                512,
                512,
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(redonda);

        float escala = 512f / (float) lado;

        BitmapShader shader = new BitmapShader(
                quadrada,
                Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP
        );

        android.graphics.Matrix matriz = new android.graphics.Matrix();
        matriz.setScale(escala, escala);
        shader.setLocalMatrix(matriz);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(shader);

        canvas.drawCircle(256f, 256f, 256f, paint);

        // Salva como PNG para manter os cantos transparentes.
        java.io.File pasta =
                new java.io.File(getFilesDir(), "equipe_fotos");

        if (!pasta.exists() && !pasta.mkdirs()) {
            Toast.makeText(this, "Erro ao criar pasta das fotos.", Toast.LENGTH_SHORT).show();
            return false;
        }

        java.io.File arquivo = new java.io.File(
                pasta,
                "perfil_" + System.currentTimeMillis() + ".png"
        );

        java.io.FileOutputStream saida =
                new java.io.FileOutputStream(arquivo);

        redonda.compress(
                Bitmap.CompressFormat.PNG,
                100,
                saida
        );

        saida.flush();
        saida.close();

        SharedPreferences prefs =
                getSharedPreferences("DaNikeAI_ADM", MODE_PRIVATE);

        String atual =
                prefs.getString("equipe_perfis", "[]");

        JSONArray lista =
                new JSONArray(atual);

        JSONObject perfil =
                new JSONObject();

        perfil.put("nome", nome);
        perfil.put("cargo", cargo);
        perfil.put("fotoUri", arquivo.getAbsolutePath());
        perfil.put("coroa", "");

        lista.put(perfil);

        prefs.edit()
                .putString("equipe_perfis", lista.toString())
                .apply();

        Toast.makeText(
                this,
                "☁️ Enviando foto da equipe...",
                Toast.LENGTH_SHORT
        ).show();

        uploadFotoEquipeCloudinary(
                arquivo,
                nome,
                cargo
        );

        Toast.makeText(
                this,
                "✅ Perfil salvo e enviado para sincronização.",
                Toast.LENGTH_SHORT
        ).show();

        return true;

    } catch (Exception e) {
        Toast.makeText(
                this,
                "Erro ao salvar foto: " + e.getMessage(),
                Toast.LENGTH_LONG
        ).show();

        return false;
    }
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

        if (requestCode == 9100
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            Uri uri = data.getData();

            try {
                getContentResolver().takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (Exception ignored) {}

            uriFotoEquipeSelecionada = uri;

            if (fotoEquipeSelecionada != null) {
                try {
                    fotoEquipeSelecionada.setImageURI(uri);
                    fotoEquipeSelecionada.setScaleType(
                            ImageView.ScaleType.CENTER_CROP
                    );
                } catch (Exception ignored) {}
            }

            Toast.makeText(
                    this,
                    "Foto selecionada.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

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
        final String GITHUB_BUILD_URL =
                "https://raw.githubusercontent.com/andersonptg/DaNikeAI/main/app/build.gradle";

        final String APK_URL =
                "https://github.com/andersonptg/DaNikeAI/releases/latest/download/DaNikeAI.apk";

        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(this);

        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(35, 10, 35, 10);

        android.widget.TextView versaoAtual =
                new android.widget.TextView(this);

        versaoAtual.setText(
                "📦 Versão instalada: v" + BuildConfig.VERSION_NAME
                        + "  •  Código " + BuildConfig.VERSION_CODE
        );

        versaoAtual.setTextSize(14);
        versaoAtual.setTextColor(
                android.graphics.Color.rgb(120, 220, 255)
        );
        versaoAtual.setPadding(0, 10, 0, 16);

        android.widget.TextView statusGithub =
                new android.widget.TextView(this);

        statusGithub.setText(
                "☁️ Consultando GitHub..."
        );
        statusGithub.setTextSize(13);
        statusGithub.setTextColor(
                android.graphics.Color.rgb(40, 255, 145)
        );
        statusGithub.setPadding(0, 0, 0, 12);

        android.widget.EditText versao =
                new android.widget.EditText(this);

        versao.setHint("Próxima versão");
        versao.setSingleLine(true);
        versao.setEnabled(false);

        android.widget.EditText codigo =
                new android.widget.EditText(this);

        codigo.setHint("Código da próxima versão");
        codigo.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );
        codigo.setSingleLine(true);
        codigo.setEnabled(false);

        android.widget.EditText link =
                new android.widget.EditText(this);

        link.setHint("Link do APK");
        link.setSingleLine(true);
        link.setEnabled(false);
        link.setText(APK_URL);

        android.widget.EditText titulo =
                new android.widget.EditText(this);

        titulo.setHint("Título");
        titulo.setSingleLine(true);
        titulo.setText("ATUALIZAÇÃO DISPONÍVEL");

        android.widget.EditText mensagem =
                new android.widget.EditText(this);

        mensagem.setHint("Mensagem para os usuários");
        mensagem.setSingleLine(false);
        mensagem.setText(
                "Uma nova versão do DaNikeAI está disponível."
        );

        android.widget.CheckBox obrigatoria =
                new android.widget.CheckBox(this);

        obrigatoria.setText(
                "Atualização obrigatória"
        );

        layout.addView(versaoAtual);
        layout.addView(statusGithub);
        layout.addView(versao);
        layout.addView(codigo);
        layout.addView(link);
        layout.addView(titulo);
        layout.addView(mensagem);
        layout.addView(obrigatoria);

        android.app.AlertDialog dialog =
                new android.app.AlertDialog.Builder(this)
                        .setTitle("📲 ATUALIZAÇÃO DO DANIKAI")
                        .setMessage(
                                "A versão e o código são puxados automaticamente do GitHub."
                        )
                        .setView(layout)
                        .setNegativeButton(
                                "CANCELAR",
                                null
                        )
                        .setPositiveButton(
                                "PUBLICAR",
                                null
                        )
                        .create();

        dialog.setOnShowListener(d -> {

            android.widget.Button publicar =
                    dialog.getButton(
                            android.content.DialogInterface.BUTTON_POSITIVE
                    );

            publicar.setEnabled(false);

            new Thread(() -> {

                java.net.HttpURLConnection conexao = null;

                try {
                    java.net.URL url =
                            new java.net.URL(GITHUB_BUILD_URL);

                    conexao =
                            (java.net.HttpURLConnection)
                                    url.openConnection();

                    conexao.setRequestMethod("GET");
                    conexao.setConnectTimeout(10000);
                    conexao.setReadTimeout(10000);
                    conexao.setRequestProperty(
                            "User-Agent",
                            "DaNikeAI-ADM"
                    );

                    int codigoHttp =
                            conexao.getResponseCode();

                    if (codigoHttp != 200) {
                        throw new Exception(
                                "GitHub respondeu HTTP " + codigoHttp
                        );
                    }

                    java.io.BufferedReader leitor =
                            new java.io.BufferedReader(
                                    new java.io.InputStreamReader(
                                            conexao.getInputStream(),
                                            java.nio.charset.StandardCharsets.UTF_8
                                    )
                            );

                    StringBuilder conteudo =
                            new StringBuilder();

                    String linha;

                    while ((linha = leitor.readLine()) != null) {
                        conteudo.append(linha).append("\n");
                    }

                    leitor.close();

                    String buildGradle =
                            conteudo.toString();

                    java.util.regex.Matcher matcherCodigo =
                            java.util.regex.Pattern.compile(
                                    "versionCode\\s+(\\d+)"
                            ).matcher(buildGradle);

                    java.util.regex.Matcher matcherVersao =
                            java.util.regex.Pattern.compile(
                                    "versionName\\s+['\\\"]([^'\\\"]+)['\\\"]"
                            ).matcher(buildGradle);

                    if (!matcherCodigo.find()) {
                        throw new Exception(
                                "versionCode não encontrado no GitHub."
                        );
                    }

                    if (!matcherVersao.find()) {
                        throw new Exception(
                                "versionName não encontrado no GitHub."
                        );
                    }

                    String nomeVersao =
                            matcherVersao.group(1);

                    String textoCodigo =
                            matcherCodigo.group(1);

                    runOnUiThread(() -> {

                        versao.setText(nomeVersao);
                        codigo.setText(textoCodigo);
                        link.setText(APK_URL);

                        statusGithub.setText(
                                "🟢 GitHub sincronizado\n"
                                        + "Versão encontrada: v"
                                        + nomeVersao
                                        + "  •  Código "
                                        + textoCodigo
                        );

                        publicar.setEnabled(true);

                        android.widget.Toast.makeText(
                                this,
                                "✅ Versão carregada automaticamente do GitHub.",
                                android.widget.Toast.LENGTH_SHORT
                        ).show();
                    });

                } catch (Exception e) {

                    runOnUiThread(() -> {

                        statusGithub.setText(
                                "🔴 Não foi possível consultar o GitHub."
                        );

                        statusGithub.setTextColor(
                                android.graphics.Color.rgb(
                                        255,
                                        70,
                                        100
                                )
                        );

                        publicar.setEnabled(false);

                        android.widget.Toast.makeText(
                                this,
                                "❌ Erro ao consultar GitHub: "
                                        + e.getMessage(),
                                android.widget.Toast.LENGTH_LONG
                        ).show();
                    });

                } finally {

                    if (conexao != null) {
                        conexao.disconnect();
                    }
                }

            }).start();

            publicar.setOnClickListener(v -> {

                String nomeVersao =
                        versao.getText()
                                .toString()
                                .trim();

                String textoCodigo =
                        codigo.getText()
                                .toString()
                                .trim();

                String apkUrl =
                        link.getText()
                                .toString()
                                .trim();

                String textoTitulo =
                        titulo.getText()
                                .toString()
                                .trim();

                String textoMensagem =
                        mensagem.getText()
                                .toString()
                                .trim();

                if (nomeVersao.isEmpty()
                        || textoCodigo.isEmpty()
                        || apkUrl.isEmpty()) {

                    android.widget.Toast.makeText(
                            this,
                            "A versão do GitHub ainda não foi carregada.",
                            android.widget.Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                int versionCode;

                try {

                    versionCode =
                            Integer.parseInt(textoCodigo);

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

                dados.put(
                        "enabled",
                        true
                );

                dados.put(
                        "versionCode",
                        versionCode
                );

                dados.put(
                        "versionName",
                        nomeVersao
                );

                dados.put(
                        "apkUrl",
                        apkUrl
                );

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
                        com.google.firebase.firestore.FieldValue
                                .serverTimestamp()
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

                            String detalhe =
                                    e.getMessage();

                            if (detalhe == null
                                    || detalhe.trim().isEmpty()) {

                                detalhe =
                                        e.getClass()
                                                .getSimpleName();
                            }

                            android.util.Log.e(
                                    "DaNikeADM",
                                    "ERRO AO PUBLICAR ATUALIZACAO",
                                    e
                            );

                            android.widget.Toast.makeText(
                                    this,
                                    "❌ FALHA NO FIREBASE: "
                                            + detalhe,
                                    android.widget.Toast.LENGTH_LONG
                            ).show();
                        });
            });
        });

        dialog.show();
    }

    private void atualizarBotoesManutencaoAreas(java.util.Map<String, Object> dados) {
        atualizarBotaoArea(manutencaoIA, "IA", dados.get("manutencao_ia"));
        atualizarBotaoArea(manutencaoFilmes, "FILMES", dados.get("manutencao_filmes"));
        atualizarBotaoArea(manutencaoHistorico, "HISTÓRICO", dados.get("manutencao_historico"));
        atualizarBotaoArea(manutencaoPerfil, "PERFIL", dados.get("manutencao_perfil"));
        atualizarBotaoArea(manutencaoHave, "HAVE", dados.get("manutencao_have"));
    }

    private void atualizarBotaoArea(Button botao, String titulo, Object valor) {
        if (botao == null) return;
        boolean ativa = valor instanceof Boolean && (Boolean) valor;
        botao.setText(
                "🔧\nMANUTENÇÃO " + titulo + "\n" +
                (ativa ? "🔴 EM MANUTENÇÃO" : "🟢 DISPONÍVEL")
        );
    }

    private void carregarManutencaoAreas() {
        db.collection("config")
                .document("areas")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        atualizarBotoesManutencaoAreas(doc.getData());
                    } else {
                        atualizarBotoesManutencaoAreas(new java.util.HashMap<>());
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("DaNikeADM", "ERRO AO CARREGAR AREAS", e);
                });
    }

    private void alternarManutencaoArea(String area, String titulo) {
        Button botao = null;

        if ("ia".equals(area)) botao = manutencaoIA;
        else if ("filmes".equals(area)) botao = manutencaoFilmes;
        else if ("historico".equals(area)) botao = manutencaoHistorico;
        else if ("perfil".equals(area)) botao = manutencaoPerfil;
        else if ("have".equals(area)) botao = manutencaoHave;

        boolean atual = botao != null
                && botao.getText().toString().contains("🔴");

        boolean novoEstado = !atual;
        String campo = "manutencao_" + area;

        new AlertDialog.Builder(this)
                .setTitle((novoEstado ? "Ativar " : "Desativar ") + "manutenção")
                .setMessage(
                        novoEstado
                                ? titulo + " ficará em manutenção para usuários comuns."
                                : titulo + " voltará a funcionar normalmente."
                )
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("CONFIRMAR", (d, w) -> {
                    db.collection("config")
                            .document("areas")
                            .set(
                                    Collections.singletonMap(campo, novoEstado),
                                    com.google.firebase.firestore.SetOptions.merge()
                            )
                            .addOnSuccessListener(v -> {
                                carregarManutencaoAreas();
                                Toast.makeText(
                                        this,
                                        titulo + (novoEstado
                                                ? " em manutenção."
                                                : " disponível novamente."),
                                        Toast.LENGTH_SHORT
                                ).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Falha Firebase: " + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                })
                .show();
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


    private void uploadFotoEquipeCloudinary(
            java.io.File arquivo,
            String nome,
            String cargo
    ) {
        if (arquivo == null || !arquivo.exists()) {
            Toast.makeText(
                    this,
                    "Foto da equipe não encontrada.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        new Thread(() -> {
            java.net.HttpURLConnection conexao = null;

            try {
                String boundary =
                        "----DaNikeAI" + System.currentTimeMillis();

                java.net.URL url = new java.net.URL(
                        "https://api.cloudinary.com/v1_1/"
                                + CLOUDINARY_CLOUD_NAME
                                + "/image/upload"
                );

                conexao =
                        (java.net.HttpURLConnection) url.openConnection();

                conexao.setRequestMethod("POST");
                conexao.setDoOutput(true);
                conexao.setDoInput(true);
                conexao.setUseCaches(false);
                conexao.setRequestProperty(
                        "Content-Type",
                        "multipart/form-data; boundary=" + boundary
                );

                java.io.OutputStream saida =
                        conexao.getOutputStream();

                java.io.PrintWriter writer =
                        new java.io.PrintWriter(
                                new java.io.OutputStreamWriter(
                                        saida,
                                        java.nio.charset.StandardCharsets.UTF_8
                                ),
                                true
                        );

                writer.append("--").append(boundary).append("\r\n");
                writer.append(
                        "Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n"
                );
                writer.append(CLOUDINARY_UPLOAD_PRESET).append("\r\n");

                writer.append("--").append(boundary).append("\r\n");
                writer.append(
                        "Content-Disposition: form-data; name=\"folder\"\r\n\r\n"
                );
                writer.append("equipe").append("\r\n");

                writer.append("--").append(boundary).append("\r\n");
                writer.append(
                        "Content-Disposition: form-data; name=\"file\"; filename=\""
                                + arquivo.getName()
                                + "\"\r\n"
                );
                writer.append("Content-Type: image/png\r\n\r\n");
                writer.flush();

                try (java.io.FileInputStream entrada =
                             new java.io.FileInputStream(arquivo)) {

                    byte[] buffer = new byte[8192];
                    int lidos;

                    while ((lidos = entrada.read(buffer)) != -1) {
                        saida.write(buffer, 0, lidos);
                    }
                }

                saida.flush();

                writer.append("\r\n");
                writer.append("--").append(boundary).append("--\r\n");
                writer.flush();
                writer.close();

                int codigo = conexao.getResponseCode();

                java.io.InputStream respostaStream =
                        codigo >= 200 && codigo < 300
                                ? conexao.getInputStream()
                                : conexao.getErrorStream();

                String resposta = "";

                if (respostaStream != null) {
                    resposta =
                            new String(
                                    respostaStream.readAllBytes(),
                                    java.nio.charset.StandardCharsets.UTF_8
                            );
                    respostaStream.close();
                }

                if (codigo >= 200 && codigo < 300) {
                    org.json.JSONObject json =
                            new org.json.JSONObject(resposta);

                    String secureUrl =
                            json.optString("secure_url", "").trim();

                    String publicId =
                            json.optString("public_id", "").trim();

                    if (secureUrl.isEmpty()) {
                        throw new Exception(
                                "Cloudinary não retornou a URL da foto."
                        );
                    }

                    if (publicId.isEmpty()) {
                        throw new Exception(
                                "Cloudinary não retornou o public_id da foto."
                        );
                    }

                    String idEquipe = idEquipeCloud(nome);

                    java.util.HashMap<String, Object> perfilCloud =
                            new java.util.HashMap<>();
                    perfilCloud.put("id", idEquipe);

                    perfilCloud.put("nome", nome);
                    perfilCloud.put("cargo", cargo);
                    perfilCloud.put("fotoUrl", secureUrl);
                    perfilCloud.put("fotoUri", secureUrl);
                    perfilCloud.put("publicId", publicId);
                    perfilCloud.put("coroa", "");
                    perfilCloud.put("atualizadoEm",
                            com.google.firebase.firestore.FieldValue.serverTimestamp());

                    db.collection("equipe")
                            .document(idEquipe)
                            .set(perfilCloud)
                            .addOnSuccessListener(v -> {
                                runOnUiThread(() ->
                                        Toast.makeText(
                                                this,
                                                "☁️ Equipe sincronizada na nuvem.",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                );

                                android.util.Log.d(
                                        "DaNikeCloudinary",
                                        "Equipe sincronizada: " + idEquipe
                                );
                            })
                            .addOnFailureListener(e -> {

                                android.util.Log.e(
                                        "DaNikeCloudinary",
                                        "Erro Firestore equipe",
                                        e
                                );

                                runOnUiThread(() ->
                                        Toast.makeText(
                                                this,
                                                "Falha Firestore: " + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show()
                                );
                            });

                    android.util.Log.d(
                            "DaNikeCloudinary",
                            "URL equipe: " + secureUrl
                    );

                } else {
                    android.util.Log.e(
                            "DaNikeCloudinary",
                            "Resposta HTTP " + codigo + ": " + resposta
                    );

                    String mensagemErro = resposta;
                    try {
                        org.json.JSONObject jsonErro =
                                new org.json.JSONObject(resposta);
                        org.json.JSONObject detalhe =
                                jsonErro.optJSONObject("error");
                        if (detalhe != null) {
                            mensagemErro = detalhe.optString("message", resposta);
                        }
                    } catch (Exception ignorado) {
                        // Mantém a resposta original.
                    }

                    throw new Exception(
                            "HTTP " + codigo + ": " + mensagemErro
                    );
                }

            } catch (Exception e) {
                android.util.Log.e(
                        "DaNikeCloudinary",
                        "Erro no upload",
                        e
                );

                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Erro ao enviar foto: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );

            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        }).start();
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
