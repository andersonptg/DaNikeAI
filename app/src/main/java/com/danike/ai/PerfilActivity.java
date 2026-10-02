package com.danike.ai;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

public class PerfilActivity extends Activity {

    int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + .5f);
    }

    TextView txt(String s, float tamanho, int cor) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(tamanho);
        t.setTextColor(cor);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        montar();
    }

    void montar() {

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(Color.rgb(2,10,18));
        raiz.setPadding(dp(18), dp(20), dp(18), 0);

        TextView titulo = txt("PERFIL", 25, Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        raiz.addView(titulo,
            new LinearLayout.LayoutParams(-1, dp(50)));

        TextView avatar = txt(
            "♙",
            55,
            Color.rgb(0,225,255)
        );

        raiz.addView(avatar,
            new LinearLayout.LayoutParams(-1, dp(90)));

        TextView nome = txt(
            "Anderson",
            23,
            Color.WHITE
        );
        nome.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        raiz.addView(nome,
            new LinearLayout.LayoutParams(-1, dp(40)));

        TextView status = txt(
            "●  CONTA ATIVA",
            13,
            Color.rgb(60,255,130)
        );

        raiz.addView(status,
            new LinearLayout.LayoutParams(-1, dp(35)));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(15),dp(10),dp(15),dp(10));

        info.addView(txt(
            "DANIKE AI",
            18,
            Color.rgb(0,225,255)
        ));

        info.addView(txt(
            "\nAssistente pessoal de inteligência artificial\n"
            + "Memória: disponível\n"
            + "Modo de voz: configurável\n"
            + "Sistema: Android\n"
            + "IA: Gemini",
            15,
            Color.rgb(190,220,235)
        ));

        raiz.addView(info,
            new LinearLayout.LayoutParams(-1, 0, 1));

        Button adm = botao("⚡\nPAINEL ADM");
        adm.setTextColor(Color.rgb(255,190,45));

        adm.setOnClickListener(v -> {

            com.google.firebase.auth.FirebaseUser usuario =
                    com.google.firebase.auth.FirebaseAuth
                            .getInstance()
                            .getCurrentUser();

            boolean autorizado = usuario != null && (
                    "Fo3JCu1NEjPglVyY4I9pB5yiV112".equals(usuario.getUid())
                    || "lipesanderson@gmail.com".equalsIgnoreCase(usuario.getEmail())
            );

            if (!autorizado) {

                Toast.makeText(
                        this,
                        "Acesso administrativo não autorizado.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            startActivity(new android.content.Intent(
                    this,
                    AdmActivity.class
            ));
        });

        raiz.addView(adm,
            new LinearLayout.LayoutParams(-1, dp(58)));

        raiz.addView(criarNavegacao(),
            new LinearLayout.LayoutParams(-1, dp(70)));

        setContentView(raiz);
    }

    LinearLayout criarNavegacao() {

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setBackgroundColor(Color.rgb(5,20,32));

        Button inicio = botao("⌂\nInício");
        Button ia = botao("🧠\nIA");
        Button historico = botao("◷\nHistórico");
        Button perfil = botao("♙\nPerfil");

        perfil.setTextColor(Color.rgb(0,225,255));

        nav.addView(inicio, new LinearLayout.LayoutParams(0, dp(65), 1));
        nav.addView(ia, new LinearLayout.LayoutParams(0, dp(65), 1));
        nav.addView(historico, new LinearLayout.LayoutParams(0, dp(65), 1));
        nav.addView(perfil, new LinearLayout.LayoutParams(0, dp(65), 1));

        inicio.setOnClickListener(v ->
            startActivity(new android.content.Intent(this, MainActivity.class)));

        ia.setOnClickListener(v ->
            startActivity(new android.content.Intent(this, IAActivity.class)));

        historico.setOnClickListener(v ->
            startActivity(new android.content.Intent(this, HistoricoActivity.class)));

        return nav;
    }

    Button botao(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(12);
        b.setTextColor(Color.rgb(170,210,240));
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        return b;
    }
}
