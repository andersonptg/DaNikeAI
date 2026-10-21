package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.File;
import java.io.InputStream;

public class PerfilActivity extends Activity {

    private SharedPreferences prefs;
    private boolean escuro;

    private TextView nomeView;
    private TextView identificadorView, handleView, descricaoView;
    private TextView statusView;
    private TextView ultimoAcessoView;
    private String identificadorPublicoReal = "";
    private boolean mostrarIdentificadorPublico = false;
    private ImageView avatar;

    private static final int FOTO = 7001;
    private static final int FOTO_CAMERA = 7002;
    private Uri uriFotoCamera;
    private File arquivoFotoCamera;

    private int azul() {
        return Color.rgb(65, 155, 255);
    }

    private int fundo() {
        return escuro
                ? Color.rgb(5, 8, 24)
                : Color.rgb(244, 247, 252);
    }

    private int painel() {
        return escuro
                ? Color.argb(135, 10, 18, 42)
                : Color.argb(190, 255, 255, 255);
    }

    private int texto() {
        return escuro
                ? Color.WHITE
                : Color.rgb(25, 30, 42);
    }

    private int textoSecundario() {
        return escuro
                ? Color.rgb(190, 205, 225)
                : Color.rgb(80, 90, 105);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(
                "DaNikeAI_Dados",
                MODE_PRIVATE
        );

        escuro = prefs.getBoolean(
                "modo_escuro",
                true
        );

        aplicarSistema();
        montar();
        carregarPerfilFirebase();
    }

    private void aplicarSistema() {
        getWindow().setStatusBarColor(fundo());

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();

            if (!escuro) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }

            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void montar() {

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(fundo());

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(28)
        );

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        TextView voltar = txt(
                "‹",
                38,
                texto()
        );

        voltar.setGravity(Gravity.CENTER);
        voltar.setBackground(
                fundoNeon(
                        escuro
                                ? Color.argb(80, 20, 30, 60)
                                : Color.argb(150, 255, 255, 255),
                        azul(),
                        18
                )
        );

        topo.addView(
                voltar,
                new LinearLayout.LayoutParams(
                        dp(52),
                        dp(52)
                )
        );

        voltar.setOnClickListener(
                v -> finish()
        );

        TextView titulo = txt(
                "MEU PERFIL",
                23,
                texto()
        );

        titulo.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        titulo.setShadowLayer(
                dp(9),
                0,
                0,
                azul()
        );

        LinearLayout.LayoutParams tituloParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1f
                );

        tituloParams.setMargins(
                dp(12),
                0,
                dp(12),
                0
        );

        topo.addView(
                titulo,
                tituloParams
        );

        TextView modo = txt(
                escuro ? "🌙" : "☀️",
                26,
                texto()
        );

        modo.setGravity(Gravity.CENTER);

        modo.setBackground(
                fundoNeon(
                        escuro
                                ? Color.argb(80, 20, 30, 60)
                                : Color.argb(150, 255, 255, 255),
                        azul(),
                        18
                )
        );

        topo.addView(
                modo,
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(52)
                )
        );

        modo.setOnClickListener(v -> {

            escuro = !escuro;

            prefs.edit()
                    .putBoolean(
                            "modo_escuro",
                            escuro
                    )
                    .apply();

            recreate();
        });

        raiz.addView(topo);

        TextView subtitulo = txt(
                "Sua conta no DaNikeAI",
                13,
                textoSecundario()
        );

        LinearLayout.LayoutParams subParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(35)
                );

        subParams.setMargins(
                0,
                dp(8),
                0,
                dp(4)
        );

        raiz.addView(
                subtitulo,
                subParams
        );

        LinearLayout perfilCard =
                new LinearLayout(this);

        perfilCard.setOrientation(
                LinearLayout.VERTICAL
        );

        perfilCard.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        perfilCard.setPadding(
                dp(16),
                dp(22),
                dp(16),
                dp(22)
        );

        perfilCard.setBackground(
                fundoNeon(
                        painel(),
                        azul(),
                        30
                )
        );

        perfilCard.setElevation(
                dp(12)
        );

        avatar = new ImageView(this);

        avatar.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        avatar.setBackground(
                fundoNeon(
                        escuro
                                ? Color.rgb(13, 18, 38)
                                : Color.rgb(235, 240, 248),
                        Color.rgb(80, 180, 255),
                        100
                )
        );

        avatar.setPadding(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        mostrarIconeCamera();

        avatar.setOnClickListener(
                v -> mostrarMenuFoto()
        );

        perfilCard.addView(
                avatar,
                new LinearLayout.LayoutParams(
                        dp(128),
                        dp(128)
                )
        );

        nomeView = txt(
                "Usuário",
                23,
                texto()
        );

        nomeView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        nomeView.setGravity(
                Gravity.CENTER
        );

        nomeView.setShadowLayer(
                dp(8),
                0,
                0,
                azul()
        );

        LinearLayout.LayoutParams nomeParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(45)
                );

        nomeParams.setMargins(
                0,
                dp(12),
                0,
                0
        );

        perfilCard.addView(
                nomeView,
                nomeParams
        );

        identificadorView = txt(
                "DNK-------",
                13,
                azul()
        );

        identificadorView.setGravity(
                Gravity.CENTER
        );

        identificadorView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        perfilCard.addView(
                identificadorView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );
        identificadorView.setOnClickListener(v -> {
            mostrarIdentificadorPublico = !mostrarIdentificadorPublico;
            atualizarIdentificadorPublico();
        });


        handleView = txt(
            "Adicione seu @usuário",
            13,
            azul()
        );
        handleView.setGravity(Gravity.CENTER);
        handleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams handleParams =
            new LinearLayout.LayoutParams(-1, dp(28));
        handleParams.setMargins(0, dp(2), 0, dp(2));
        perfilCard.addView(handleView, handleParams);

        descricaoView = txt(
            "Adicione uma descrição",
            12,
            textoSecundario()
        );
        descricaoView.setGravity(Gravity.CENTER);
        descricaoView.setPadding(dp(12), dp(2), dp(12), dp(2));
        descricaoView.setMaxLines(4);
        LinearLayout.LayoutParams descricaoParams =
            new LinearLayout.LayoutParams(-1, -2);
        descricaoParams.setMargins(0, dp(2), 0, dp(8));
        perfilCard.addView(descricaoView, descricaoParams);
        descricaoView.setOnClickListener(v -> editarDescricao());

statusView = txt(
                "⚪ Offline",
                13,
                textoSecundario()
        );

        statusView.setGravity(
                Gravity.CENTER
        );

        perfilCard.addView(
                statusView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(30)
                )
        );

        ultimoAcessoView = txt(
                "Visto por último: —",
                11,
                textoSecundario()
        );

        ultimoAcessoView.setGravity(
                Gravity.CENTER
        );

        perfilCard.addView(
                ultimoAcessoView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(25)
                )
        );

        raiz.addView(
                perfilCard,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        adicionarBotao(
                raiz,
                "✏️  EDITAR NOME",
                "Atualize como seu nome aparece no aplicativo.",
                () -> editarNome()
        );

        adicionarInfo(raiz,
                "🪪  IDENTIFICADOR PÚBLICO",
                "Toque no código acima para revelar ou ocultar.");
        adicionarBotao(raiz,
                "❕  SOBRE O IDENTIFICADOR",
                "Código público para ser encontrado nas Conexões.",
                () -> new AlertDialog.Builder(this)
                    .setTitle("Identificador público")
                    .setMessage("Este código identifica seu perfil nas Conexões. "
                        + "Ele é diferente do ID interno da conta, que não deve "
                        + "ser compartilhado publicamente.")
                    .setPositiveButton("Entendi", null)
                    .show());

        adicionarInfo(raiz,
                "🔒  ID INTERNO DA CONTA",
                "••••••••••  Protegido; não é público.");
        adicionarInfo(raiz,
                "🛡️  PRIVACIDADE",
                "O ID interno fica oculto nesta tela e não deve ser divulgado.");


adicionarBotao(
    raiz,
    "✏️  EDITAR @USUÁRIO",
    "Seu nome de usuário público exclusivo.",
    () -> editarHandle()
);

adicionarBotao(
    raiz,
    "📝  EDITAR DESCRIÇÃO",
    "Escreva uma breve apresentação sobre você.",
    () -> editarDescricao()
);

adicionarBotao(
    raiz,
    "🔐  VISIBILIDADE DO PERFIL",
    "Escolha se seu perfil pode aparecer no GLOBAL.",
    () -> editarVisibilidade()
);

scroll.addView(raiz);

        setContentView(scroll);
    }

    private void carregarPerfilFirebase() {

        if (!PerfilFirebase.estaLogado()) {
            carregarNome();
            return;
        }

        PerfilFirebase.garantirPerfil(
                this,
                prefs.getString(
                        "nome",
                        ""
                ),
                new PerfilFirebase.Callback() {

                    @Override
                    public void sucesso() {
                        PerfilFirebase.carregarMeuPerfil(
                                new PerfilFirebase.PerfilCallback() {

                                    @Override
                                    public void sucesso(
                                            DocumentSnapshot documento
                                    ) {
                                        runOnUiThread(
                                                () -> aplicarPerfil(
                                                        documento
                                                )
                                        );
                                    }

                                    @Override
                                    public void erro(
                                            String mensagem
                                    ) {
                                        runOnUiThread(
                                                () -> carregarNome()
                                        );
                                    }
                                }
                        );
                    }

                    @Override
                    public void erro(
                            String mensagem
                    ) {
                        runOnUiThread(
                                () -> carregarNome()
                        );
                    }
                }
        );
    }

    private void aplicarPerfil(
            DocumentSnapshot doc
    ) {

        if (doc == null || !doc.exists()) {
            carregarNome();
            return;
        }

        String nome =
                doc.getString("nome");

        if (nome != null
                && !nome.trim().isEmpty()) {

            nomeView.setText(
                    nome.trim()
            );

            prefs.edit()
                    .putString(
                            "nome",
                            nome.trim()
                    )
                    .apply();
        }

        String identificador =
                doc.getString(
                        "identificadorPublico"
                );

        if (identificador != null
                && !identificador.trim().isEmpty()) {

            identificadorPublicoReal = identificador.trim();
            atualizarIdentificadorPublico();
        }


        String handlePerfil = doc.getString("handle");
        if (handlePerfil != null && !handlePerfil.trim().isEmpty()) {
            handleView.setText("@" + handlePerfil.trim().replaceFirst("^@", ""));
        } else {
            handleView.setText("Adicione seu @usuário");
        }

        String descricaoPerfil = doc.getString("descricao");
        if (descricaoPerfil != null && !descricaoPerfil.trim().isEmpty()) {
            descricaoView.setText(descricaoPerfil.trim());
        } else {
            descricaoView.setText("Adicione uma descrição");
        }

Boolean online =
                doc.getBoolean("online");

        if (Boolean.TRUE.equals(online)) {

            statusView.setText(
                    "🟢 Online agora"
            );

            statusView.setTextColor(
                    Color.rgb(80, 230, 125)
            );

        } else {

            statusView.setText(
                    "⚪ Offline"
            );

            statusView.setTextColor(
                    textoSecundario()
            );

            String ultimo =
                    formatarTimestamp(
                            doc.getTimestamp(
                                    "ultimoAcesso"
                            )
                    );

            ultimoAcessoView.setText(
                    "Visto por último: "
                            + ultimo
            );
        }


String handleSalvo = doc.getString("handle");
if (handleSalvo != null) {
    prefs.edit().putString("handle", handleSalvo).apply();
}
String descricaoSalva = doc.getString("descricao");
if (descricaoSalva != null) {
    prefs.edit().putString("descricao", descricaoSalva).apply();
}
Boolean publicoSalvo = doc.getBoolean("perfilPublico");
if (publicoSalvo != null) {
    prefs.edit().putBoolean("perfilPublico", publicoSalvo).apply();
}

String fotoUrl =
                doc.getString(
                        "fotoUrl"
                );

        if (fotoUrl != null
                && !fotoUrl.trim().isEmpty()) {

            carregarImagemUrl(
                    fotoUrl.trim()
            );
        }
    }

    private void atualizarIdentificadorPublico() {
        if (identificadorPublicoReal == null
                || identificadorPublicoReal.trim().isEmpty()) {
            identificadorView.setText("DNK-*********  👁️");
            return;
        }
        identificadorView.setText(
            mostrarIdentificadorPublico
                ? identificadorPublicoReal + "  🙈"
                : "DNK-*********  👁️");
    }

    private void carregarImagemUrl(
            String url
    ) {

        new Thread(() -> {

            try {

                java.net.URL endereco =
                        new java.net.URL(url);

                java.net.HttpURLConnection conexao =
                        (java.net.HttpURLConnection)
                                endereco.openConnection();

                conexao.setConnectTimeout(
                        10000
                );

                conexao.setReadTimeout(
                        15000
                );

                conexao.setDoInput(true);
                conexao.connect();

                InputStream entrada =
                        conexao.getInputStream();

                Bitmap bitmap =
                        BitmapFactory.decodeStream(
                                entrada
                        );

                entrada.close();
                conexao.disconnect();

                if (bitmap != null) {

                    Bitmap circular =
                            circularizar(
                                    bitmap
                            );

                    runOnUiThread(() -> {
                        avatar.clearColorFilter();
                        avatar.setPadding(0, 0, 0, 0);
                        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        avatar.setImageBitmap(circular);
                    });
                }

            } catch (Exception ignored) {
            }

        }).start();
    }

    private Bitmap circularizar(
            Bitmap original
    ) {

        int tamanho =
                Math.min(
                        original.getWidth(),
                        original.getHeight()
                );

        int esquerda =
                (original.getWidth() - tamanho)
                        / 2;

        int topo =
                (original.getHeight() - tamanho)
                        / 2;

        Bitmap quadrado =
                Bitmap.createBitmap(
                        original,
                        esquerda,
                        topo,
                        tamanho,
                        tamanho
                );

        Bitmap resultado =
                Bitmap.createBitmap(
                        tamanho,
                        tamanho,
                        Bitmap.Config.ARGB_8888
                );

        Canvas canvas =
                new Canvas(resultado);

        Paint paint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );

        canvas.drawCircle(
                tamanho / 2f,
                tamanho / 2f,
                tamanho / 2f,
                paint
        );

        paint.setXfermode(
                new PorterDuffXfermode(
                        PorterDuff.Mode.SRC_IN
                )
        );

        canvas.drawBitmap(
                quadrado,
                0,
                0,
                paint
        );

        paint.setXfermode(null);

        return resultado;
    }

    private String formatarTimestamp(
            com.google.firebase.Timestamp timestamp
    ) {

        if (timestamp == null) {
            return "ainda não disponível";
        }

        java.text.SimpleDateFormat formato =
                new java.text.SimpleDateFormat(
                        "dd/MM/yyyy 'às' HH:mm",
                        java.util.Locale.getDefault()
                );

        return formato.format(
                timestamp.toDate()
        );
    }

    private void adicionarBotao(
            LinearLayout raiz,
            String titulo,
            String descricao,
            Runnable acao
    ) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setGravity(
                Gravity.CENTER_VERTICAL
        );

        box.setPadding(
                dp(18),
                dp(10),
                dp(18),
                dp(10)
        );

        box.setBackground(
                fundoNeon(
                        painel(),
                        Color.rgb(55, 130, 245),
                        24
                )
        );

        TextView t =
                txt(
                        titulo,
                        17,
                        texto()
                );

        t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView d =
                txt(
                        descricao,
                        12,
                        textoSecundario()
                );

        box.addView(
                t,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(32)
                )
        );

        box.addView(
                d,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(30)
                )
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(78)
                );

        p.setMargins(
                0,
                dp(12),
                0,
                0
        );

        raiz.addView(
                box,
                p
        );

        box.setOnClickListener(
                v -> acao.run()
        );
    }

    private void adicionarInfo(
            LinearLayout raiz,
            String titulo,
            String valor
    ) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                dp(18),
                dp(8),
                dp(18),
                dp(8)
        );

        box.setBackground(
                fundoNeon(
                        painel(),
                        Color.rgb(45, 105, 205),
                        22
                )
        );

        TextView t =
                txt(
                        titulo,
                        15,
                        texto()
                );

        t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView v =
                txt(
                        valor,
                        12,
                        textoSecundario()
                );

        box.addView(
                t,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(30)
                )
        );

        box.addView(
                v,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                );

        p.setMargins(
                0,
                dp(10),
                0,
                0
        );

        raiz.addView(
                box,
                p
        );
    }

    private void carregarNome() {

        DadosUsuario.nome(
                this,
                nome -> runOnUiThread(
                        () -> nomeView.setText(
                                nome
                        )
                )
        );
    }


private void editarHandle() {
    EditText campo = new EditText(this);
    campo.setSingleLine(true);
    campo.setHint("@seuusuario");
    campo.setText(prefs.getString("handle", ""));
    campo.setSelection(campo.length());

    new AlertDialog.Builder(this)
        .setTitle("Seu @usuário")
        .setMessage("Use de 3 a 20 letras, números ou pontos. O @ é opcional.")
        .setView(campo)
        .setNegativeButton("Cancelar", null)
        .setPositiveButton("Salvar", (dialog, which) -> {
            String valor = campo.getText().toString().trim()
                .replaceFirst("^@", "")
                .toLowerCase(java.util.Locale.ROOT);

            if (!valor.matches("[a-z0-9.]{3,20}")
                    || valor.startsWith(".")
                    || valor.endsWith(".")
                    || valor.contains("..")) {
                Toast.makeText(this,
                    "Use 3 a 20 caracteres: letras, números ou pontos.",
                    Toast.LENGTH_LONG).show();
                return;
            }

            salvarCamposPerfil(valor,
                prefs.getString("descricao", ""),
                prefs.getBoolean("perfilPublico", true));
        })
        .show();
}

private void editarDescricao() {
    EditText campo = new EditText(this);
    campo.setHint("Conte um pouco sobre você");
    campo.setMinLines(3);
    campo.setMaxLines(5);
    campo.setText(prefs.getString("descricao", ""));

    new AlertDialog.Builder(this)
        .setTitle("Descrição do perfil")
        .setMessage("Máximo de 160 caracteres.")
        .setView(campo)
        .setNegativeButton("Cancelar", null)
        .setPositiveButton("Salvar", (dialog, which) -> {
            String descricao = campo.getText().toString().trim();
            if (descricao.length() > 160) {
                Toast.makeText(this,
                    "A descrição pode ter no máximo 160 caracteres.",
                    Toast.LENGTH_LONG).show();
                return;
            }
            salvarCamposPerfil(
                prefs.getString("handle", ""),
                descricao,
                prefs.getBoolean("perfilPublico", true));
        })
        .show();
}

private void editarVisibilidade() {
    String[] opcoes = {"Público", "Privado"};
    boolean atual = prefs.getBoolean("perfilPublico", true);

    new AlertDialog.Builder(this)
        .setTitle("Visibilidade do perfil")
        .setSingleChoiceItems(opcoes, atual ? 0 : 1, (dialog, escolha) -> {
            boolean publico = escolha == 0;
            dialog.dismiss();
            salvarCamposPerfil(
                prefs.getString("handle", ""),
                prefs.getString("descricao", ""),
                publico);
        })
        .setNegativeButton("Cancelar", null)
        .show();
}

private void salvarCamposPerfil(
        String handle, String descricao, boolean publico) {
    Toast.makeText(this, "Salvando perfil...", Toast.LENGTH_SHORT).show();

    PerfilFirebase.salvarDadosPerfil(
        this, handle, descricao, publico,
        new PerfilFirebase.Callback() {
            @Override
            public void sucesso() {
                prefs.edit()
                    .putString("handle", handle)
                    .putString("descricao", descricao)
                    .putBoolean("perfilPublico", publico)
                    .apply();

                Toast.makeText(PerfilActivity.this,
                    "Perfil atualizado.", Toast.LENGTH_SHORT).show();
                carregarPerfilFirebase();
            }

            @Override
            public void erro(String mensagem) {
                Toast.makeText(PerfilActivity.this,
                    "Não foi possível salvar: " + mensagem,
                    Toast.LENGTH_LONG).show();
            }
        });
}

private void editarNome() {

        final EditText campo =
                new EditText(this);

        campo.setSingleLine(true);
        campo.setHint(
                "Seu nome"
        );

        campo.setText(
                nomeView
                        .getText()
                        .toString()
        );

        new AlertDialog.Builder(this)
                .setTitle(
                        "Editar nome"
                )
                .setView(campo)
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Salvar",
                        (dialog, which) -> {

                            String novo =
                                    campo.getText()
                                            .toString()
                                            .trim();

                            if (novo.isEmpty()) {
                                Toast.makeText(
                                        this,
                                        "Digite um nome.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            DadosUsuario.salvarNome(
                                    this,
                                    novo
                            );

                            nomeView.setText(
                                    novo
                            );

                            PerfilFirebase.atualizarNome(
                                    PerfilActivity.this,
                                    novo,
                                    new PerfilFirebase.Callback() {

                                        @Override
                                        public void sucesso() {
                                            Toast.makeText(
                                                    PerfilActivity.this,
                                                    "Nome atualizado.",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }

                                        @Override
                                        public void erro(
                                                String mensagem
                                        ) {
                                            Toast.makeText(
                                                    PerfilActivity.this,
                                                    "Nome salvo localmente.",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    }
                            );
                        }
                )
                .show();
    }


    private void mostrarIconeCamera() {
        avatar.setImageResource(android.R.drawable.ic_menu_camera);
        avatar.setColorFilter(Color.rgb(80, 180, 255));
        avatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        avatar.setPadding(dp(28), dp(28), dp(28), dp(28));
    }

    private void mostrarMenuFoto() {
    String[] opcoes = {
        "Galeria",
        "Câmera",
        "Arquivos",
        "Remover foto"
    };

    new AlertDialog.Builder(this)
        .setTitle("Foto do perfil")
        .setItems(opcoes, (dialog, which) -> {
            if (which == 0) {
                escolherFoto();
            } else if (which == 1) {
                tirarFoto();
            } else if (which == 2) {
                escolherArquivoFoto();
            } else if (which == 3) {
                confirmarRemocaoFoto();
            }
        })
        .show();
}

private void tirarFoto() {
        try {
            File pastaFotos = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (pastaFotos == null) {
                throw new IllegalStateException("Pasta de fotos indisponível.");
            }

            if (!pastaFotos.exists() && !pastaFotos.mkdirs()) {
                throw new IllegalStateException("Não foi possível criar a pasta de fotos.");
            }

            arquivoFotoCamera = new File(
                    pastaFotos,
                    "danike_perfil_" + System.currentTimeMillis() + ".jpg"
            );

            uriFotoCamera = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    arquivoFotoCamera
            );

            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            camera.putExtra(MediaStore.EXTRA_OUTPUT, uriFotoCamera);
            camera.setClipData(ClipData.newRawUri("foto", uriFotoCamera));
            camera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            camera.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (camera.resolveActivity(getPackageManager()) == null) {
                arquivoFotoCamera.delete();
                arquivoFotoCamera = null;
                uriFotoCamera = null;
                Toast.makeText(this, "Nenhum aplicativo de câmera disponível.", Toast.LENGTH_LONG).show();
                return;
            }

            startActivityForResult(camera, FOTO_CAMERA);

        } catch (Exception e) {
            if (arquivoFotoCamera != null) {
                arquivoFotoCamera.delete();
            }
            arquivoFotoCamera = null;
            uriFotoCamera = null;

            Toast.makeText(
                    this,
                    "Falha ao abrir a câmera: " +
                            (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void processarFoto(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "Não foi possível acessar a foto.", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit().putString("foto_perfil", uri.toString()).apply();

        try (InputStream entrada = getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(entrada);
            if (bitmap != null) {
                avatar.clearColorFilter();
                avatar.setPadding(0, 0, 0, 0);
                avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                avatar.setImageBitmap(circularizar(bitmap));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível exibir a foto.", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "Enviando foto para o perfil...", Toast.LENGTH_SHORT).show();

        PerfilFirebase.enviarFoto(uri, new PerfilFirebase.Callback() {
            @Override
            public void sucesso() {
                Toast.makeText(
                        PerfilActivity.this,
                        "Foto do perfil atualizada.",
                        Toast.LENGTH_SHORT
                ).show();
                carregarPerfilFirebase();
            }

            @Override
            public void erro(String mensagem) {
                Toast.makeText(
                        PerfilActivity.this,
                        "Erro ao salvar a foto: " + mensagem,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void escolherArquivoFoto() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("image/*");
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

    try {
        startActivityForResult(intent, FOTO);
    } catch (Exception e) {
        Toast.makeText(
            this,
            "Não foi possível abrir os arquivos.",
            Toast.LENGTH_LONG
        ).show();
    }
}

private void escolherFoto() {

        Intent i =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        i.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        i.setType(
                "image/*"
        );

        i.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        try {

            startActivityForResult(
                    i,
                    FOTO
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Não foi possível abrir a galeria.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void confirmarRemocaoFoto() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Remover foto?"
                )
                .setMessage(
                        "A foto será removida do seu perfil público."
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Remover",
                        (dialog, which) ->
                                removerFotoFirebase()
                )
                .show();
    }

    private void removerFotoFirebase() {

        PerfilFirebase.removerFoto(
                new PerfilFirebase.Callback() {

                    @Override
                    public void sucesso() {

                        prefs.edit()
                                .remove(
                                        "foto_perfil"
                                )
                                .apply();

                        mostrarIconeCamera();

                        Toast.makeText(
                                PerfilActivity.this,
                                "Foto removida.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void erro(
                            String mensagem
                    ) {

                        Toast.makeText(
                                PerfilActivity.this,
                                "Não foi possível remover a foto.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FOTO_CAMERA) {
            Uri foto = uriFotoCamera;
            uriFotoCamera = null;

            if (resultCode == RESULT_OK && foto != null) {
                processarFoto(foto);
            } else {
                if (arquivoFotoCamera != null) {
                    arquivoFotoCamera.delete();
                }
            }
            arquivoFotoCamera = null;
            return;
        }

        if (requestCode != FOTO
                || resultCode != RESULT_OK
                || data == null
                || data.getData() == null) {
            return;
        }

        Uri uri = data.getData();

        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {
        }

        processarFoto(uri);
    }

    private TextView txt(
            String texto,
            int tamanho,
            int cor
    ) {

        TextView t =
                new TextView(this);

        t.setText(texto);
        t.setTextSize(tamanho);
        t.setTextColor(cor);

        t.setGravity(
                Gravity.CENTER_VERTICAL
        );

        return t;
    }

    private GradientDrawable fundoNeon(
            int corFundo,
            int corBorda,
            int raio
    ) {

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(corFundo);

        g.setCornerRadius(
                dp(raio)
        );

        g.setStroke(
                dp(1),
                corBorda
        );

        return g;
    }

    private int dp(int valor) {

        return Math.round(
                valor
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
