package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MembrosActivity extends Activity {

    int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + .5f);
    }

    GradientDrawable fundo(int cor, float raio) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(cor);
        g.setCornerRadius(dp(raio));
        return g;
    }

    TextView texto(String s, float tamanho, int cor, boolean negrito) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(tamanho);
        t.setTextColor(cor);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (negrito) {
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return t;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(Color.rgb(1,3,12));
        getWindow().setNavigationBarColor(Color.rgb(1,3,12));

        android.widget.FrameLayout raiz =
                new android.widget.FrameLayout(this);

        raiz.addView(
                new FundoMembros(this),
                new android.widget.FrameLayout.LayoutParams(-1,-1));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(dp(14),dp(12),dp(14),dp(20));

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        Button voltar = new Button(this);
        voltar.setText("‹");
        voltar.setTextSize(32);
        voltar.setTextColor(Color.WHITE);
        voltar.setAllCaps(false);
        voltar.setBackground(
                fundo(Color.argb(45,0,190,255),22));

        topo.addView(
                voltar,
                new LinearLayout.LayoutParams(dp(52),dp(52)));

        LinearLayout tituloBox = new LinearLayout(this);
        tituloBox.setOrientation(LinearLayout.VERTICAL);
        tituloBox.setPadding(dp(10),0,0,0);

        tituloBox.addView(
                texto("MEMBROS",23,Color.WHITE,true),
                new LinearLayout.LayoutParams(-1,dp(30)));

        tituloBox.addView(
                texto("SUA ÁREA EXCLUSIVA",
                        9,
                        Color.rgb(0,225,255),
                        true),
                new LinearLayout.LayoutParams(-1,dp(22)));

        topo.addView(
                tituloBox,
                new LinearLayout.LayoutParams(0,dp(56),1));

        TextView online = texto(
                "● ONLINE",
                9,
                Color.rgb(60,255,145),
                true);

        online.setGravity(Gravity.CENTER);

        topo.addView(
                online,
                new LinearLayout.LayoutParams(dp(75),dp(36)));

        conteudo.addView(
                topo,
                new LinearLayout.LayoutParams(-1,dp(60)));

        voltar.setOnClickListener(v -> finish());

        LinearLayout perfil = card();

        TextView nome = texto(
                "Usuário",
                21,
                Color.WHITE,
                true);

        TextView status = texto(
                "● Membro ativo\nSua conta está pronta para usar a DaNikeAI.",
                12,
                Color.rgb(155,205,235),
                false);

        perfil.addView(
                texto("♛",34,Color.rgb(0,225,255),true),
                new LinearLayout.LayoutParams(dp(58),dp(62)));

        LinearLayout dados = new LinearLayout(this);
        dados.setOrientation(LinearLayout.VERTICAL);
        dados.setPadding(dp(10),0,0,0);

        dados.addView(
                nome,
                new LinearLayout.LayoutParams(-1,dp(32)));

        dados.addView(
                status,
                new LinearLayout.LayoutParams(-1,dp(48)));

        perfil.addView(
                dados,
                new LinearLayout.LayoutParams(0,dp(74),1));

        conteudo.addView(
                perfil,
                new LinearLayout.LayoutParams(-1,dp(100)));

        DadosUsuario.nome(
                this,
                nomeAtual -> nome.setText(nomeAtual));

        TextView secao = texto(
                "O QUE VOCÊ PODE FAZER",
                12,
                Color.rgb(0,225,255),
                true);

        secao.setPadding(dp(4),dp(14),0,dp(7));

        conteudo.addView(
                secao,
                new LinearLayout.LayoutParams(-1,dp(42)));

        LinearLayout cerebro = card();

        LinearLayout infoCerebro = new LinearLayout(this);
        infoCerebro.setOrientation(LinearLayout.VERTICAL);

        infoCerebro.addView(
                texto("🧠  Cérebro",
                        17,
                        Color.WHITE,
                        true),
                new LinearLayout.LayoutParams(-1,dp(30)));

        infoCerebro.addView(
                texto(
                        "Grava automaticamente suas conversas no histórico para você consultar depois.",
                        11,
                        Color.rgb(150,195,225),
                        false),
                new LinearLayout.LayoutParams(-1,dp(48)));

        cerebro.addView(
                infoCerebro,
                new LinearLayout.LayoutParams(0,dp(82),1));

        Switch cerebroAtivo = new Switch(this);

        cerebroAtivo.setChecked(
                getSharedPreferences(
                        "danike_config",
                        MODE_PRIVATE)
                        .getBoolean("cerebro_ativo",true));

        cerebro.addView(
                cerebroAtivo,
                new LinearLayout.LayoutParams(dp(58),dp(70)));

        conteudo.addView(
                cerebro,
                new LinearLayout.LayoutParams(-1,dp(96)));

        cerebroAtivo.setOnCheckedChangeListener(
                (button,ativo) ->
                        getSharedPreferences(
                                "danike_config",
                                MODE_PRIVATE)
                                .edit()
                                .putBoolean(
                                        "cerebro_ativo",
                                        ativo)
                                .apply());

        LinearLayout linha1 = new LinearLayout(this);
        linha1.setGravity(Gravity.CENTER);

        Button conta = botao("👤\nMinha conta");
        Button conversas = botao("💬\nConversas");

        linha1.addView(
                conta,
                new LinearLayout.LayoutParams(0,dp(78),1));

        linha1.addView(
                conversas,
                new LinearLayout.LayoutParams(0,dp(78),1));

        conteudo.addView(
                linha1,
                new LinearLayout.LayoutParams(-1,dp(86)));

        conta.setOnClickListener(
                v -> startActivity(
                        new Intent(this,PerfilActivity.class)));

        conversas.setOnClickListener(
                v -> startActivity(
                        new Intent(this,IAActivity.class)));

        LinearLayout linha2 = new LinearLayout(this);
        linha2.setGravity(Gravity.CENTER);

        Button beneficios = botao("✨\nBenefícios");
        Button ajuda = botao("🆘\nAjuda");

        linha2.addView(
                beneficios,
                new LinearLayout.LayoutParams(0,dp(78),1));

        linha2.addView(
                ajuda,
                new LinearLayout.LayoutParams(0,dp(78),1));

        conteudo.addView(
                linha2,
                new LinearLayout.LayoutParams(-1,dp(86)));

        beneficios.setOnClickListener(
                v -> new AlertDialog.Builder(this)
                        .setTitle("✨ DaNikeAI")
                        .setMessage(
                                "Cérebro para histórico automático.\n\n"
                                + "Conversas organizadas.\n\n"
                                + "Memória permanente somente quando você pedir.\n\n"
                                + "Assistente com voz e chat.")
                        .setPositiveButton("FECHAR",null)
                        .show());

        ajuda.setOnClickListener(v -> {
            try {
                Intent i = new Intent(
                        Intent.ACTION_VIEW,
                        android.net.Uri.parse(
                                "https://www.instagram.com/anderson_lopes._ofc"));
                startActivity(i);
            } catch(Exception ignored) {}
        });

        LinearLayout privacidade = card();

        privacidade.addView(
                texto(
                        "🛡  Privacidade\n"
                        + "Seu histórico fica no aparelho. "
                        + "A memória permanente só é criada quando "
                        + "você pede explicitamente para lembrar.",
                        12,
                        Color.rgb(175,215,240),
                        false),
                new LinearLayout.LayoutParams(-1,dp(76)));

        conteudo.addView(
                privacidade,
                new LinearLayout.LayoutParams(-1,dp(92)));

        Button sair = botao("SAIR DA CONTA");

        sair.setTextColor(
                Color.rgb(255,105,130));

        GradientDrawable sairBg =
                fundo(Color.argb(55,80,5,35),22);

        sairBg.setStroke(
                dp(1),
                Color.rgb(255,55,110));

        sair.setBackground(sairBg);

        conteudo.addView(
                sair,
                new LinearLayout.LayoutParams(-1,dp(54)));

        sair.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent i =
                    new Intent(this,LoginActivity.class);

            i.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(i);
            finish();
        });

        TextView rodape = texto(
                "DaNikeAI  •  Área de membros",
                9,
                Color.rgb(90,140,180),
                false);

        rodape.setGravity(Gravity.CENTER);

        conteudo.addView(
                rodape,
                new LinearLayout.LayoutParams(-1,dp(40)));

        scroll.addView(conteudo);

        raiz.addView(
                scroll,
                new android.widget.FrameLayout.LayoutParams(-1,-1));

        setContentView(raiz);
    }

    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this);

        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(
                dp(14),dp(8),dp(14),dp(8));

        GradientDrawable g =
                fundo(
                        Color.argb(125,2,12,30),
                        22);

        g.setStroke(
                dp(1),
                Color.rgb(0,190,255));

        box.setBackground(g);

        return box;
    }

    private Button botao(String texto) {
        Button b = new Button(this);

        b.setText(texto);
        b.setTextSize(12);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD);

        GradientDrawable g =
                fundo(
                        Color.argb(90,10,35,75),
                        20);

        g.setStroke(
                dp(1),
                Color.rgb(80,90,255));

        b.setBackground(g);

        return b;
    }

    class FundoMembros extends View {

        Paint p =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        float fase;

        FundoMembros(
                android.content.Context c) {
            super(c);
            postInvalidateDelayed(30);
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth();
            float h = getHeight();

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(1,3,12));

            c.drawRect(0,0,w,h,p);

            fase += .01f;

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));

            for(int i=0;i<9;i++) {
                float y =
                        (i * dp(100)
                        + fase * dp(180))
                        % (h + dp(100));

                p.setColor(
                        Color.argb(35,0,210,255));

                c.drawLine(
                        0,y,
                        w,y-dp(35),
                        p);
            }

            p.setStyle(Paint.Style.FILL);

            for(int i=0;i<18;i++) {
                float x =
                        (float)((i*71+fase*40)
                        % Math.max(1,w));

                float y =
                        (float)((i*137)
                        % Math.max(1,h));

                p.setColor(
                        Color.argb(120,0,210,255));

                p.setShadowLayer(
                        dp(7),0,0,
                        Color.rgb(0,210,255));

                c.drawCircle(
                        x,y,dp(2),p);

                p.clearShadowLayer();
            }

            postInvalidateDelayed(30);
        }
    }
}
