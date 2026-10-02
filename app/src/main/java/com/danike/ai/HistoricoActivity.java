package com.danike.ai;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class HistoricoActivity extends Activity {

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
        raiz.setBackgroundColor(Color.rgb(2, 10, 18));
        raiz.setPadding(dp(16), dp(20), dp(16), 0);

        TextView titulo = txt("HISTÓRICO", 25, Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        raiz.addView(titulo,
            new LinearLayout.LayoutParams(-1, dp(50)));

        TextView subtitulo = txt(
            "Conversas e atividades recentes",
            14,
            Color.rgb(150, 200, 225)
        );
        raiz.addView(subtitulo,
            new LinearLayout.LayoutParams(-1, dp(35)));

        TextView vazio = txt(
            "◷\n\nSeu histórico aparecerá aqui.\n\n"
            + "As conversas, respostas salvas e atividades "
            + "da DaNikeAI poderão ser consultadas nesta área.",
            16,
            Color.rgb(180, 220, 235)
        );

        raiz.addView(vazio,
            new LinearLayout.LayoutParams(-1, 0, 1));

        raiz.addView(criarNavegacao(),
            new LinearLayout.LayoutParams(-1, dp(70)));

        setContentView(raiz);
    }

    LinearLayout criarNavegacao() {

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setBackgroundColor(Color.rgb(5, 20, 32));

        Button inicio = botao("⌂\nInício");
        Button ia = botao("🧠\nIA");
        Button historico = botao("◷\nHistórico");
        Button perfil = botao("♙\nPerfil");

        historico.setTextColor(Color.rgb(0, 225, 255));

        nav.addView(inicio, new LinearLayout.LayoutParams(0, dp(65), 1));
        nav.addView(ia, new LinearLayout.LayoutParams(0, dp(65), 1));
        nav.addView(historico, new LinearLayout.LayoutParams(0, dp(65), 1));
        nav.addView(perfil, new LinearLayout.LayoutParams(0, dp(65), 1));

        inicio.setOnClickListener(v ->
            startActivity(new android.content.Intent(this, MainActivity.class)));

        ia.setOnClickListener(v ->
            startActivity(new android.content.Intent(this, IAActivity.class)));

        perfil.setOnClickListener(v ->
            startActivity(new android.content.Intent(this, PerfilActivity.class)));

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
