package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SocialTesteActivity extends Activity {

    private FrameLayout raiz;
    private FrameLayout conteudo;
    private TextView titulo;
    private TextView[] botoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        raiz = new FrameLayout(this);
        raiz.setBackgroundColor(Color.rgb(2, 3, 8));

        montarInterface();

        setContentView(raiz);
        selecionar(0);
    }

    private void montarInterface() {

        // TOPO
        LinearLayout topo = new LinearLayout(this);
        topo.setOrientation(LinearLayout.HORIZONTAL);
        topo.setGravity(Gravity.CENTER_VERTICAL);
        topo.setPadding(dp(16), dp(10), dp(16), dp(10));

        GradientDrawable fundoTopo = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(8, 12, 28),
                        Color.rgb(12, 7, 25),
                        Color.rgb(5, 12, 22)
                }
        );

        fundoTopo.setCornerRadius(dp(20));
        fundoTopo.setStroke(dp(1), Color.rgb(45, 100, 180));
        topo.setBackground(fundoTopo);

        TextView voltar = texto("‹", 34, Color.WHITE);
        voltar.setGravity(Gravity.CENTER);
        voltar.setOnClickListener(v -> finish());

        topo.addView(
                voltar,
                new LinearLayout.LayoutParams(dp(50), dp(60))
        );

        titulo = texto("GLOBAL", 21, Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        topo.addView(
                titulo,
                new LinearLayout.LayoutParams(0, dp(60), 1)
        );

        FrameLayout.LayoutParams topoParams =
                new FrameLayout.LayoutParams(
                        -1,
                        dp(80),
                        Gravity.TOP
                );

        topoParams.setMargins(dp(10), dp(10), dp(10), 0);
        raiz.addView(topo, topoParams);

        // CONTEÚDO
        conteudo = new FrameLayout(this);

        FrameLayout.LayoutParams conteudoParams =
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                );

        conteudoParams.setMargins(
                0,
                dp(95),
                0,
                dp(100)
        );

        raiz.addView(conteudo, conteudoParams);

        // BARRA INFERIOR
        LinearLayout barraInferior =
                new LinearLayout(this);

        barraInferior.setOrientation(
                LinearLayout.HORIZONTAL
        );

        barraInferior.setGravity(
                Gravity.CENTER
        );

        barraInferior.setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
        );

        GradientDrawable fundoBarra =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{
                                Color.rgb(7, 8, 18),
                                Color.rgb(15, 8, 28),
                                Color.rgb(6, 13, 24)
                        }
                );

        fundoBarra.setCornerRadius(dp(22));
        fundoBarra.setStroke(
                dp(1),
                Color.rgb(55, 100, 180)
        );

        barraInferior.setBackground(fundoBarra);
        barraInferior.setElevation(dp(20));

        botoes = new TextView[]{
                botao("🌎\nGlobal"),
                botao("👥\nConexões"),
                botao("💌\nSolicitações"),
                botao("💬\nMensagens"),
                botao("👥\nGrupos"),
                botao("⭕\nStatus")
        };

        for (int i = 0; i < botoes.length; i++) {

            final int posicao = i;

            FrameLayout aba =
                    new FrameLayout(this);

            FrameLayout.LayoutParams abaParams =
                    new FrameLayout.LayoutParams(
                            0,
                            dp(68),
                            1
                    );

            TextView botaoAtual = botoes[i];

            aba.addView(
                    botaoAtual,
                    new FrameLayout.LayoutParams(
                            -1,
                            -1
                    )
            );

            barraInferior.addView(
                    aba,
                    abaParams
            );

            botaoAtual.setOnClickListener(
                    v -> selecionar(posicao)
            );
        }

        FrameLayout.LayoutParams barraParams =
                new FrameLayout.LayoutParams(
                        -1,
                        dp(78),
                        Gravity.BOTTOM
                );

        barraParams.setMargins(
                dp(8),
                0,
                dp(8),
                dp(12)
        );

        raiz.addView(
                barraInferior,
                barraParams
        );

        barraInferior.bringToFront();
        barraInferior.setZ(1000f);
    }

    private void selecionar(int posicao) {

        String[] titulos = {
                "GLOBAL",
                "CONEXÕES",
                "SOLICITAÇÕES",
                "MENSAGENS",
                "GRUPOS",
                "STATUS"
        };

        titulo.setText(titulos[posicao]);

        conteudo.removeAllViews();

        for (int i = 0; i < botoes.length; i++) {

            GradientDrawable fundo =
                    new GradientDrawable();

            fundo.setCornerRadius(dp(18));

            if (i == posicao) {

                fundo.setColor(
                        Color.rgb(18, 42, 78)
                );

                fundo.setStroke(
                        dp(1),
                        Color.rgb(70, 160, 255)
                );

                botoes[i].setTextColor(
                        Color.WHITE
                );

            } else {

                fundo.setColor(
                        Color.TRANSPARENT
                );

                botoes[i].setTextColor(
                        Color.rgb(145, 160, 185)
                );
            }

            botoes[i].setBackground(fundo);
        }

        LinearLayout painel =
                new LinearLayout(this);

        painel.setOrientation(
                LinearLayout.VERTICAL
        );

        painel.setGravity(
                Gravity.CENTER
        );

        TextView icone =
                texto(
                        titulos[posicao].equals("GLOBAL")
                                ? "🌎"
                                : posicao == 1
                                ? "👥"
                                : posicao == 2
                                ? "💌"
                                : posicao == 3
                                ? "💬"
                                : posicao == 4
                                ? "👥"
                                : "⭕",
                        48,
                        Color.WHITE
                );

        icone.setGravity(Gravity.CENTER);

        TextView mensagem =
                texto(
                        titulos[posicao],
                        20,
                        Color.rgb(100, 190, 255)
                );

        mensagem.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        mensagem.setGravity(Gravity.CENTER);

        TextView teste =
                texto(
                        "TELA DE TESTE\n\n" +
                        "Os botões inferiores estão funcionando.",
                        14,
                        Color.rgb(145, 160, 185)
                );

        teste.setGravity(Gravity.CENTER);

        painel.addView(icone);
        painel.addView(
                mensagem,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(45)
                )
        );
        painel.addView(teste);

        conteudo.addView(
                painel,
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                )
        );
    }

    private TextView botao(
            String texto
    ) {

        TextView t =
                new TextView(this);

        t.setText(texto);
        t.setTextSize(11);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        t.setPadding(
                dp(2),
                dp(2),
                dp(2),
                dp(2)
        );

        return t;
    }

    private TextView texto(
            String texto,
            int tamanho,
            int cor
    ) {

        TextView t =
                new TextView(this);

        t.setText(texto);
        t.setTextSize(tamanho);
        t.setTextColor(cor);

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
