package com.danike.ai;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ManutencaoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout tela = new LinearLayout(this);
        tela.setOrientation(LinearLayout.VERTICAL);
        tela.setGravity(Gravity.CENTER);
        tela.setPadding(dp(32), dp(32), dp(32), dp(32));
        tela.setBackgroundColor(Color.rgb(5, 7, 12));

        TextView logo = new TextView(this);
        logo.setText("◈");
        logo.setTextColor(Color.rgb(0, 220, 255));
        logo.setTextSize(56);
        logo.setGravity(Gravity.CENTER);

        TextView titulo = new TextView(this);
        titulo.setText("DaNikeAI");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(28);
        titulo.setGravity(Gravity.CENTER);
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView icone = new TextView(this);
        icone.setText("🔧");
        icone.setTextSize(42);
        icone.setGravity(Gravity.CENTER);
        icone.setPadding(0, dp(28), 0, dp(12));

        TextView mensagem = new TextView(this);
        mensagem.setText(
                "Estamos realizando melhorias no aplicativo.\n\n" +
                "O DaNikeAI estará disponível novamente em breve."
        );
        mensagem.setTextColor(Color.LTGRAY);
        mensagem.setTextSize(17);
        mensagem.setGravity(Gravity.CENTER);
        mensagem.setLineSpacing(dp(4), 1f);

        TextView status = new TextView(this);
        status.setText("● SISTEMA EM MANUTENÇÃO");
        status.setTextColor(Color.rgb(255, 190, 50));
        status.setTextSize(13);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(28), 0, 0);

        tela.addView(logo);
        tela.addView(titulo);
        tela.addView(icone);
        tela.addView(mensagem);
        tela.addView(status);

        setContentView(tela);
    }

    private int dp(int valor) {
        return (int) (
                valor * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
