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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PerfilActivity extends Activity {

    private SharedPreferences prefs;
    private boolean escuro;
    private TextView nomeView;
    private ImageView avatar;
    private static final int FOTO = 7001;

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

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView txt(String texto, float tamanho, int cor) {
        TextView t = new TextView(this);
        t.setText(texto);
        t.setTextSize(tamanho);
        t.setTextColor(cor);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private GradientDrawable fundoNeon(int corFundo, int corBorda, int raio) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(corFundo);
        g.setCornerRadius(dp(raio));
        g.setStroke(dp(2), corBorda);
        return g;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        prefs = getSharedPreferences("DaNikeAI_Dados", MODE_PRIVATE);
        escuro = prefs.getBoolean("modo_escuro", true);

        aplicarSistema();
        montar();
    }

    private void aplicarSistema() {
        getWindow().setStatusBarColor(
                escuro ? Color.rgb(2,6,18) : Color.rgb(242,246,252)
        );

        getWindow().setNavigationBarColor(
                escuro ? Color.rgb(2,6,18) : Color.rgb(242,246,252)
        );

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            getWindow().getDecorView().setSystemUiVisibility(
                    escuro ? 0 :
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );
        }
    }

    private void montar() {

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(fundo());
        scroll.setFillViewport(true);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(18), dp(14), dp(18), dp(28));

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        TextView voltar = txt("‹", 38, texto());
        voltar.setBackground(
                fundoNeon(
                        escuro
                                ? Color.argb(80,20,30,60)
                                : Color.argb(150,255,255,255),
                        azul(),
                        18
                )
        );

        topo.addView(
                voltar,
                new LinearLayout.LayoutParams(dp(52), dp(52))
        );

        voltar.setOnClickListener(v -> finish());

        TextView titulo = txt("MEU PERFIL", 23, texto());
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titulo.setShadowLayer(dp(9), 0, 0, azul());

        LinearLayout.LayoutParams tituloParams =
                new LinearLayout.LayoutParams(0, dp(52), 1f);
        tituloParams.setMargins(dp(12), 0, dp(12), 0);
        topo.addView(titulo, tituloParams);

        TextView modo = txt(
                escuro ? "🌙" : "☀️",
                26,
                texto()
        );

        modo.setBackground(
                fundoNeon(
                        escuro
                                ? Color.argb(80,20,30,60)
                                : Color.argb(150,255,255,255),
                        azul(),
                        18
                )
        );

        topo.addView(
                modo,
                new LinearLayout.LayoutParams(dp(58), dp(52))
        );

        modo.setOnClickListener(v -> {
            escuro = !prefs.getBoolean("modo_escuro", true);

            prefs.edit()
                    .putBoolean("modo_escuro", escuro)
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
                new LinearLayout.LayoutParams(-1, dp(35));
        subParams.setMargins(0, dp(8), 0, dp(4));
        raiz.addView(subtitulo, subParams);

        LinearLayout perfilCard = new LinearLayout(this);
        perfilCard.setOrientation(LinearLayout.VERTICAL);
        perfilCard.setGravity(Gravity.CENTER_HORIZONTAL);
        perfilCard.setPadding(dp(16), dp(22), dp(16), dp(22));

        perfilCard.setBackground(
                fundoNeon(painel(), azul(), 30)
        );

        perfilCard.setElevation(dp(12));

        avatar = new ImageView(this);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);

        GradientDrawable avatarFundo =
                fundoNeon(
                        escuro
                                ? Color.rgb(13,18,38)
                                : Color.rgb(235,240,248),
                        Color.rgb(80,180,255),
                        100
                );

        avatar.setBackground(avatarFundo);
        avatar.setPadding(dp(8), dp(8), dp(8), dp(8));

        String fotoSalva = prefs.getString("foto_perfil", "");

        if (!fotoSalva.isEmpty()) {
            try {
                avatar.setImageURI(Uri.parse(fotoSalva));
            } catch (Exception e) {
                avatar.setImageDrawable(null);
            }
        }

        if (fotoSalva.isEmpty()) {
            TextView cabeca = txt("♙", 68, texto());
            cabeca.setBackground(
                    fundoNeon(
                            escuro
                                    ? Color.rgb(13,18,38)
                                    : Color.rgb(235,240,248),
                            Color.rgb(80,180,255),
                            100
                    )
            );

            perfilCard.addView(
                    cabeca,
                    new LinearLayout.LayoutParams(dp(120), dp(120))
            );
        } else {
            perfilCard.addView(
                    avatar,
                    new LinearLayout.LayoutParams(dp(120), dp(120))
            );
        }

        nomeView = txt("Usuário", 23, texto());
        nomeView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        nomeView.setShadowLayer(dp(8), 0, 0, azul());

        LinearLayout.LayoutParams nomeParams =
                new LinearLayout.LayoutParams(-1, dp(45));
        nomeParams.setMargins(0, dp(12), 0, 0);

        perfilCard.addView(nomeView, nomeParams);

        carregarNome();

        LinearLayout botoesFoto = new LinearLayout(this);
        botoesFoto.setOrientation(LinearLayout.HORIZONTAL);
        botoesFoto.setGravity(Gravity.CENTER);
        botoesFoto.setPadding(0, dp(8), 0, 0);

        TextView enviar = txt("📸\nEnviar foto", 13, texto());
        enviar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        enviar.setBackground(
                fundoNeon(
                        escuro
                                ? Color.argb(100,15,35,65)
                                : Color.argb(180,255,255,255),
                        Color.rgb(70,170,255),
                        22
                )
        );

        TextView deletar = txt("🗑️\nDeletar foto", 13, texto());
        deletar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        deletar.setBackground(
                fundoNeon(
                        escuro
                                ? Color.argb(100,35,15,35)
                                : Color.argb(180,255,255,255),
                        Color.rgb(190,80,255),
                        22
                )
        );

        LinearLayout.LayoutParams fotoParams =
                new LinearLayout.LayoutParams(0, dp(68), 1f);
        fotoParams.setMargins(dp(4), dp(4), dp(4), 0);

        botoesFoto.addView(enviar, fotoParams);
        botoesFoto.addView(deletar, fotoParams);

        perfilCard.addView(
                botoesFoto,
                new LinearLayout.LayoutParams(-1, dp(76))
        );

        enviar.setOnClickListener(v -> escolherFoto());

        deletar.setOnClickListener(v -> {
            prefs.edit()
                    .remove("foto_perfil")
                    .apply();

            Toast.makeText(
                    this,
                    "Foto removida.",
                    Toast.LENGTH_SHORT
            ).show();

            recreate();
        });

        raiz.addView(
                perfilCard,
                new LinearLayout.LayoutParams(-1, -2)
        );

        adicionarBotao(
                raiz,
                "✏️  EDITAR NOME",
                "Atualize como seu nome aparece no aplicativo.",
                () -> editarNome()
        );

        FirebaseUser usuario =
                FirebaseAuth.getInstance().getCurrentUser();

        String uid = usuario == null
                ? "Não disponível"
                : usuario.getUid();

        String uidCurto = uid;

        if (uid.length() > 10) {
            uidCurto =
                    uid.substring(0, 4)
                    + " ••••• "
                    + uid.substring(uid.length() - 4);
        }

        adicionarInfo(
                raiz,
                "🆔  ID DA CONTA",
                uidCurto
        );

        adicionarInfo(
                raiz,
                "🟢  STATUS",
                "Conta ativa"
        );

        scroll.addView(raiz);

        setContentView(scroll);
    }

    private void adicionarBotao(
            LinearLayout raiz,
            String titulo,
            String descricao,
            Runnable acao
    ) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(18), dp(10), dp(18), dp(10));

        box.setBackground(
                fundoNeon(
                        painel(),
                        Color.rgb(55,130,245),
                        24
                )
        );

        TextView t = txt(titulo, 17, texto());
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        TextView d = txt(descricao, 12, textoSecundario());
        d.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        box.addView(t, new LinearLayout.LayoutParams(-1, dp(32)));
        box.addView(d, new LinearLayout.LayoutParams(-1, dp(30)));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(78));
        p.setMargins(0, dp(12), 0, 0);

        raiz.addView(box, p);

        box.setOnClickListener(v -> acao.run());
    }

    private void adicionarInfo(
            LinearLayout raiz,
            String titulo,
            String valor
    ) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(8), dp(18), dp(8));

        box.setBackground(
                fundoNeon(
                        painel(),
                        Color.rgb(45,105,205),
                        22
                )
        );

        TextView t = txt(titulo, 15, texto());
        t.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView v = txt(valor, 12, textoSecundario());
        v.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        box.addView(t, new LinearLayout.LayoutParams(-1, dp(30)));
        box.addView(v, new LinearLayout.LayoutParams(-1, dp(28)));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(70));
        p.setMargins(0, dp(10), 0, 0);

        raiz.addView(box, p);
    }

    private void carregarNome() {
        DadosUsuario.nome(this, nome -> runOnUiThread(() ->
                nomeView.setText(nome)
        ));
    }

    private void editarNome() {

        final android.widget.EditText campo =
                new android.widget.EditText(this);

        campo.setSingleLine(true);
        campo.setHint("Seu nome");
        campo.setText(nomeView.getText().toString());

        new AlertDialog.Builder(this)
                .setTitle("Editar nome")
                .setView(campo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novo =
                            campo.getText().toString().trim();

                    if (!novo.isEmpty()) {
                        DadosUsuario.salvarNome(this, novo);
                        nomeView.setText(novo);
                    }
                })
                .show();
    }

    private void escolherFoto() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");

        try {
            startActivityForResult(i, FOTO);
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Não foi possível abrir a galeria.",
                    Toast.LENGTH_SHORT
            ).show();
        }
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

        if (requestCode == FOTO
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            Uri uri = data.getData();

            prefs.edit()
                    .putString(
                            "foto_perfil",
                            uri.toString()
                    )
                    .apply();

            try {
                getContentResolver().takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (Exception ignored) {}

            Toast.makeText(
                    this,
                    "Foto do perfil atualizada.",
                    Toast.LENGTH_SHORT
            ).show();

            recreate();
        }
    }
}
