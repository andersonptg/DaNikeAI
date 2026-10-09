package com.danike.ai;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CriarGrupoActivity extends Activity {

    private static final int ESCOLHER_FOTO = 7001;

    private EditText campoNome;
    private EditText campoDescricao;
    private ImageView fotoGrupo;
    private LinearLayout listaPessoas;
    private Button botaoCriar;

    private Uri fotoSelecionada;
    private final List<String> participantesSelecionados = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private int azul = Color.rgb(0, 170, 255);
    private int fundo = Color.rgb(5, 7, 12);
    private int card = Color.rgb(13, 17, 25);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        montarTela();
        carregarPessoas();
    }

    private void montarTela() {

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(fundo);

        LinearLayout principal = new LinearLayout(this);
        principal.setOrientation(LinearLayout.VERTICAL);
        principal.setPadding(dp(18), dp(18), dp(18), dp(30));

        scroll.addView(principal);

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        TextView voltar = texto("‹", 38, Color.WHITE);
        voltar.setGravity(Gravity.CENTER);
        voltar.setOnClickListener(v -> finish());

        topo.addView(voltar,
                new LinearLayout.LayoutParams(dp(55), dp(55)));

        TextView titulo = texto("Criar grupo", 23, Color.WHITE);
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);

        topo.addView(titulo,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                ));

        principal.addView(topo);

        Space espacoTopo = new Space(this);
        principal.addView(
                espacoTopo,
                new LinearLayout.LayoutParams(
                        1,
                        dp(10)
                )
        );

        fotoGrupo = new ImageView(this);
        fotoGrupo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        fotoGrupo.setImageResource(android.R.drawable.ic_menu_camera);
        fotoGrupo.setPadding(dp(28), dp(28), dp(28), dp(28));
        fotoGrupo.setBackground(circulo(azul, fundo));
        fotoGrupo.setOnClickListener(v -> escolherFoto());

        LinearLayout.LayoutParams fotoParams =
                new LinearLayout.LayoutParams(dp(110), dp(110));
        fotoParams.gravity = Gravity.CENTER_HORIZONTAL;

        principal.addView(fotoGrupo, fotoParams);

        TextView toqueFoto = texto(
                "Toque para adicionar uma foto",
                13,
                Color.LTGRAY
        );
        toqueFoto.setGravity(Gravity.CENTER);

        principal.addView(
                toqueFoto,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

        campoNome = campo("Nome do grupo");
        principal.addView(
                campoNome,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        Space espacoNome = new Space(this);
        principal.addView(
                espacoNome,
                new LinearLayout.LayoutParams(1, dp(12))
        );

        campoDescricao = campo("Descrição do grupo");
        campoDescricao.setMinLines(3);
        campoDescricao.setGravity(
                Gravity.TOP | Gravity.START
        );

        principal.addView(
                campoDescricao,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(95)
                )
        );

        Space espacoDescricao = new Space(this);
        principal.addView(
                espacoDescricao,
                new LinearLayout.LayoutParams(1, dp(22))
        );

        TextView participantes = texto(
                "PARTICIPANTES",
                12,
                azul
        );
        participantes.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        principal.addView(participantes);

        TextView admin = texto(
                "👑 Você será o administrador inicial",
                14,
                Color.WHITE
        );
        admin.setPadding(0, dp(10), 0, dp(12));

        principal.addView(admin);

        listaPessoas = new LinearLayout(this);
        listaPessoas.setOrientation(LinearLayout.VERTICAL);

        principal.addView(listaPessoas);

        botaoCriar = new Button(this);
        botaoCriar.setText("CRIAR GRUPO");
        botaoCriar.setTextColor(Color.WHITE);
        botaoCriar.setTextSize(14);
        botaoCriar.setAllCaps(false);
        botaoCriar.setBackground(
                arredondado(azul, 22)
        );

        botaoCriar.setOnClickListener(v -> criarGrupo());

        LinearLayout.LayoutParams botaoParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(56)
                );

        botaoParams.topMargin = dp(24);

        principal.addView(botaoCriar, botaoParams);

        setContentView(scroll);
    }

    private void carregarPessoas() {

        db.collection("perfis_publicos")
                .orderBy("nome")
                .get()
                .addOnSuccessListener(resultado -> {

                    listaPessoas.removeAllViews();

                    String meuUid =
                            auth.getCurrentUser() == null
                                    ? ""
                                    : auth.getCurrentUser().getUid();

                    for (QueryDocumentSnapshot doc : resultado) {

                        String uid = doc.getId();

                        if (uid.equals(meuUid)) {
                            continue;
                        }

                        String nome = doc.getString("nome");

                        if (nome == null ||
                                nome.trim().isEmpty()) {
                            nome = "Usuário";
                        }

                        String fotoUrl =
                                doc.getString("fotoUrl");

                        adicionarPessoa(
                                uid,
                                nome,
                                fotoUrl
                        );
                    }
                });
    }

    private void adicionarPessoa(
            String uid,
            String nome,
            String fotoUrl
    ) {

        LinearLayout linha = new LinearLayout(this);
        linha.setGravity(Gravity.CENTER_VERTICAL);
        linha.setPadding(
                dp(12),
                dp(8),
                dp(12),
                dp(8)
        );

        linha.setBackground(
                arredondado(card, 22)
        );

        ImageView foto = new ImageView(this);
        foto.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        foto.setImageResource(
                android.R.drawable.ic_menu_gallery
        );

        LinearLayout.LayoutParams fotoParams =
                new LinearLayout.LayoutParams(
                        dp(52),
                        dp(52)
                );

        linha.addView(foto, fotoParams);

        TextView nomeView =
                texto(nome, 16, Color.WHITE);

        nomeView.setPadding(
                dp(14),
                0,
                dp(8),
                0
        );

        linha.addView(
                nomeView,
                new LinearLayout.LayoutParams(
                        0,
                        dp(60),
                        1
                )
        );

        CheckBox check = new CheckBox(this);
        check.setButtonTintList(
                new android.content.res.ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_checked},
                                new int[]{}
                        },
                        new int[]{
                                azul,
                                Color.GRAY
                        }
                )
        );

        check.setOnCheckedChangeListener(
                (buttonView, marcado) -> {

                    if (marcado) {
                        if (!participantesSelecionados.contains(uid)) {
                            participantesSelecionados.add(uid);
                        }
                    } else {
                        participantesSelecionados.remove(uid);
                    }
                }
        );

        linha.addView(
                check,
                new LinearLayout.LayoutParams(
                        dp(55),
                        dp(60)
                )
        );

        LinearLayout.LayoutParams linhaParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(76)
                );

        linhaParams.bottomMargin = dp(9);

        listaPessoas.addView(
                linha,
                linhaParams
        );

        if (fotoUrl != null &&
                !fotoUrl.trim().isEmpty()) {

            new Thread(() -> {

                try {

                    java.net.URL url =
                            new java.net.URL(fotoUrl);

                    java.net.HttpURLConnection conexao =
                            (java.net.HttpURLConnection)
                                    url.openConnection();

                    conexao.setConnectTimeout(6000);
                    conexao.setReadTimeout(6000);

                    java.io.InputStream entrada =
                            conexao.getInputStream();

                    android.graphics.Bitmap bitmap =
                            android.graphics.BitmapFactory
                                    .decodeStream(entrada);

                    entrada.close();

                    if (bitmap != null) {

                        runOnUiThread(() ->
                                foto.setImageBitmap(
                                        bitmap
                                )
                        );
                    }

                } catch (Exception ignored) {
                }

            }).start();
        }
    }

    private void escolherFoto() {

        Intent intent =
                new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                );

        startActivityForResult(
                intent,
                ESCOLHER_FOTO
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

        if (requestCode == ESCOLHER_FOTO &&
                resultCode == RESULT_OK &&
                data != null) {

            fotoSelecionada =
                    data.getData();

            fotoGrupo.setImageURI(
                    fotoSelecionada
            );

            fotoGrupo.setPadding(0, 0, 0, 0);
        }
    }

    private void criarGrupo() {

        String nome =
                campoNome.getText()
                        .toString()
                        .trim();

        String descricao =
                campoDescricao.getText()
                        .toString()
                        .trim();

        if (nome.isEmpty()) {

            campoNome.setError(
                    "Digite o nome do grupo"
            );

            campoNome.requestFocus();

            return;
        }

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Faça login novamente.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String meuUid =
                auth.getCurrentUser().getUid();

        botaoCriar.setEnabled(false);
        botaoCriar.setText("CRIANDO...");

        List<String> membros =
                new ArrayList<>(
                        participantesSelecionados
                );

        if (!membros.contains(meuUid)) {
            membros.add(0, meuUid);
        }

        Map<String, Object> grupo =
                GrupoModelo.novoGrupo(
                        nome,
                        descricao,
                        "",
                        meuUid,
                        membros
                );

        db.collection("grupos")
                .add(grupo)
                .addOnSuccessListener(
                        documento -> {

                            String grupoId =
                                    documento.getId();

                            Map<String, Object>
                                    dadosCriacao =
                                    new HashMap<>();

                            dadosCriacao.put(
                                    "grupoId",
                                    grupoId
                            );

                            dadosCriacao.put(
                                    "nome",
                                    nome
                            );

                            dadosCriacao.put(
                                    "descricao",
                                    descricao
                            );

                            dadosCriacao.put(
                                    "administradorUid",
                                    meuUid
                            );

                            dadosCriacao.put(
                                    "participantes",
                                    membros
                            );

                            dadosCriacao.put(
                                    "criadoEm",
                                    com.google.firebase.firestore
                                            .FieldValue
                                            .serverTimestamp()
                            );

                            documento.set(
                                    dadosCriacao,
                                    com.google.firebase.firestore
                                            .SetOptions
                                            .merge()
                            ).addOnSuccessListener(
                                    v -> {

                                        Toast.makeText(
                                                this,
                                                "Grupo criado com sucesso!",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        finish();
                                    }
                            );
                        }
                )
                .addOnFailureListener(
                        erro -> {

                            botaoCriar.setEnabled(true);
                            botaoCriar.setText(
                                    "CRIAR GRUPO"
                            );

                            Toast.makeText(
                                    this,
                                    "Não foi possível criar o grupo.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private EditText campo(String hint) {

        EditText campo =
                new EditText(this);

        campo.setHint(hint);
        campo.setHintTextColor(
                Color.rgb(150, 155, 165)
        );
        campo.setTextColor(Color.WHITE);
        campo.setTextSize(15);
        campo.setSingleLine(false);
        campo.setPadding(
                dp(18),
                dp(10),
                dp(18),
                dp(10)
        );

        campo.setBackground(
                arredondado(card, 20)
        );

        return campo;
    }

    private TextView texto(
            String texto,
            float tamanho,
            int cor
    ) {

        TextView view =
                new TextView(this);

        view.setText(texto);
        view.setTextSize(tamanho);
        view.setTextColor(cor);

        return view;
    }

    private GradientDrawable arredondado(
            int cor,
            float raio
    ) {

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(cor);
        g.setCornerRadius(
                dp((int) raio)
        );

        return g;
    }

    private GradientDrawable circulo(
            int cor,
            int fundo
    ) {

        GradientDrawable g =
                new GradientDrawable();

        g.setShape(
                GradientDrawable.OVAL
        );

        g.setColor(fundo);
        g.setStroke(
                dp(2),
                cor
        );

        return g;
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
