package com.danike.ai;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class JFDemoActivity extends Activity {

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView texto(String valor, int tamanho, int cor) {
        TextView t = new TextView(this);
        t.setText(valor);
        t.setTextSize(tamanho);
        t.setTextColor(cor);
        return t;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setGravity(Gravity.CENTER_HORIZONTAL);
        raiz.setPadding(dp(22), dp(40), dp(22), dp(30));
        raiz.setBackgroundColor(Color.BLACK);

        TextView icone = texto("💈", 42, Color.WHITE);
        icone.setGravity(Gravity.CENTER);

        raiz.addView(icone,
                new LinearLayout.LayoutParams(-1, dp(60)));

        TextView titulo = texto("JF", 34, Color.WHITE);
        titulo.setGravity(Gravity.CENTER);
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        raiz.addView(titulo,
                new LinearLayout.LayoutParams(-1, dp(52)));

        TextView subtitulo =
                texto("BARBER EXPERIENCE", 11, Color.rgb(0, 190, 255));
        subtitulo.setGravity(Gravity.CENTER);
        subtitulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        raiz.addView(subtitulo,
                new LinearLayout.LayoutParams(-1, dp(32)));

        TextView card = texto(
                "JF BARBER\n\n"
                + "Estilo. Precisão. Identidade.\n\n"
                + "Uma experiência moderna,\n"
                + "premium e personalizada.",
                17,
                Color.WHITE
        );

        card.setGravity(Gravity.CENTER);
        card.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.setPadding(dp(22), dp(22), dp(22), dp(22));

        GradientDrawable fundoCard = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[] {
                        Color.rgb(7, 18, 35),
                        Color.rgb(18, 7, 38)
                }
        );

        fundoCard.setCornerRadius(dp(24));
        fundoCard.setStroke(dp(1), Color.rgb(0, 190, 255));
        card.setBackground(fundoCard);
        card.setElevation(dp(14));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(-1, dp(250));
        cardParams.setMargins(0, dp(35), 0, dp(25));

        raiz.addView(card, cardParams);

        TextView entrar = texto("ENTRAR NA EXPERIÊNCIA", 14, Color.WHITE);
        entrar.setGravity(Gravity.CENTER);
        entrar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        GradientDrawable fundoBotao = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {
                        Color.rgb(0, 105, 255),
                        Color.rgb(120, 45, 255)
                }
        );

        fundoBotao.setCornerRadius(dp(26));
        fundoBotao.setStroke(dp(1), Color.rgb(80, 210, 255));
        entrar.setBackground(fundoBotao);
        entrar.setElevation(dp(12));

        raiz.addView(entrar,
                new LinearLayout.LayoutParams(dp(240), dp(54)));

        setContentView(raiz);
    }
}
