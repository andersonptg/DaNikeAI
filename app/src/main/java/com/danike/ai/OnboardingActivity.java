package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.content.*;
import java.util.*;

public class OnboardingActivity extends Activity {

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
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8), dp(5), dp(8), dp(5));

        if (negrito)
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        return t;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(Color.rgb(1, 4, 12));
        getWindow().setNavigationBarColor(Color.rgb(1, 4, 12));

        mostrarNome();
    }

    LinearLayout criarBase() {

        FrameLayout frame = new FrameLayout(this);

        NeonManualBackground fundo =
                new NeonManualBackground(this);

        frame.addView(
                fundo,
                new FrameLayout.LayoutParams(-1, -1)
        );

        LinearLayout conteudo =
                new LinearLayout(this);

        conteudo.setOrientation(
                LinearLayout.VERTICAL
        );

        conteudo.setPadding(
                dp(18),
                dp(20),
                dp(18),
                dp(18)
        );

        frame.addView(
                conteudo,
                new FrameLayout.LayoutParams(-1, -1)
        );

        setContentView(frame);

        return conteudo;
    }

    void mostrarNome() {

        LinearLayout raiz = criarBase();

        Space topo = new Space(this);
        raiz.addView(
                topo,
                new LinearLayout.LayoutParams(1, dp(25))
        );

        TextView logo = texto(
                "✦ DaNikeAI ✦",
                32,
                Color.rgb(0, 235, 255),
                true
        );

        raiz.addView(
                logo,
                new LinearLayout.LayoutParams(-1, dp(65))
        );

        TextView subtitulo = texto(
                "Sua inteligência artificial pessoal",
                16,
                Color.rgb(180, 205, 230),
                false
        );

        raiz.addView(
                subtitulo,
                new LinearLayout.LayoutParams(-1, dp(45))
        );

        Space espaco = new Space(this);
        raiz.addView(
                espaco,
                new LinearLayout.LayoutParams(1, 0, 1)
        );

        LinearLayout vidro = new LinearLayout(this);
        vidro.setOrientation(
                LinearLayout.VERTICAL
        );
        vidro.setPadding(
                dp(18),
                dp(22),
                dp(18),
                dp(22)
        );

        GradientDrawable vidroFundo =
                fundo(
                        Color.argb(125, 4, 25, 50),
                        26
                );

        vidroFundo.setStroke(
                dp(1.5f),
                Color.argb(190, 0, 225, 255)
        );

        vidro.setBackground(vidroFundo);

        TextView izy = texto(
                "🤖",
                42,
                Color.WHITE,
                false
        );

        vidro.addView(
                izy,
                new LinearLayout.LayoutParams(-1, dp(60))
        );

        TextView titulo = texto(
                "Como você quer que eu te chame?",
                22,
                Color.WHITE,
                true
        );

        vidro.addView(
                titulo,
                new LinearLayout.LayoutParams(-1, dp(60))
        );

        TextView explicacao = texto(
                "Esse nome será usado pela IZy durante suas conversas e na tela inicial.",
                14,
                Color.rgb(175, 205, 230),
                false
        );

        vidro.addView(
                explicacao,
                new LinearLayout.LayoutParams(-1, dp(70))
        );

        EditText nome = new EditText(this);
        nome.setHint("Digite o nome...");
        nome.setHintTextColor(
                Color.rgb(110, 160, 190)
        );
        nome.setTextColor(Color.WHITE);
        nome.setTextSize(18);
        nome.setSingleLine(true);
        nome.setPadding(
                dp(16),
                0,
                dp(16),
                0
        );

        GradientDrawable campo =
                fundo(
                        Color.argb(150, 0, 15, 32),
                        18
                );

        campo.setStroke(
                dp(1),
                Color.rgb(0, 190, 255)
        );

        nome.setBackground(campo);

        vidro.addView(
                nome,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        Button continuar =
                botaoNeon("CONTINUAR  →");

        continuar.setOnClickListener(v -> {

            String valor =
                    nome.getText().toString().trim();

            if (valor.isEmpty()) {
                nome.setError(
                        "Digite o nome que devo usar."
                );
                nome.requestFocus();
                return;
            }

            DadosUsuario.salvarNome(
                    this,
                    valor
            );

            getSharedPreferences(
                    "DaNikeAI_Onboarding",
                    MODE_PRIVATE
            ).edit()
                    .putBoolean("nome_definido", true)
                    .apply();

            mostrarManual(valor);
        });

        LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(56)
                );

        cp.setMargins(
                0,
                dp(18),
                0,
                0
        );

        vidro.addView(
                continuar,
                cp
        );

        raiz.addView(
                vidro,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        Space baixo = new Space(this);

        raiz.addView(
                baixo,
                new LinearLayout.LayoutParams(
                        1,
                        0,
                        1
                )
        );

        TextView rodape = texto(
                "Primeiro acesso • DaNikeAI",
                11,
                Color.rgb(90, 145, 185),
                false
        );

        raiz.addView(
                rodape,
                new LinearLayout.LayoutParams(-1, dp(35))
        );
    }

    void mostrarManual(String nome) {

        LinearLayout raiz = criarBase();

        TextView topo = texto(
                "✦  MANUAL COMPLETO DA DANIKΕAI  ✦",
                21,
                Color.rgb(0, 235, 255),
                true
        );

        raiz.addView(
                topo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        TextView boasVindas = texto(
                "Olá, " + nome + "! 👋\n" +
                "Conheça todos os recursos da DaNikeAI.",
                15,
                Color.WHITE,
                true
        );

        raiz.addView(
                boasVindas,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                )
        );

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        LinearLayout lista =
                new LinearLayout(this);

        lista.setOrientation(
                LinearLayout.VERTICAL
        );

        lista.setPadding(
                0,
                dp(4),
                0,
                dp(20)
        );

        // Manual removido: o usuário entra direto na Home.

        scroll.addView(lista);

        raiz.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        Button entrar =
                botaoNeon("ENTRAR NA DANIKΕAI  →");

        entrar.setOnClickListener(v -> {

            getSharedPreferences(
                    "DaNikeAI_Onboarding",
                    MODE_PRIVATE
            ).edit()
                    .putBoolean("manual_visto", true)
                    .putBoolean("concluido", true)
                    .apply();

            Intent i =
                    new Intent(
                            this,
                            IAActivity.class
                    );

            startActivity(i);
            finish();
        });

        raiz.addView(
                entrar,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );
    }

    void manual(LinearLayout lista) {

        card(
                lista,
                "🏠  01 • TELA INICIAL",
                "A Home é o centro da DaNikeAI. Nela você encontra a IZy e os principais caminhos do aplicativo."
        );

        card(
                lista,
                "🤖  02 • IZy — SUA ASSISTENTE",
                "A IZy é a inteligência artificial da DaNikeAI. Você pode conversar por voz ou escrever suas perguntas."
        );

        card(
                lista,
                "🎙️  03 • MICROFONE E VOZ",
                "Com a permissão do microfone, você pode falar com a IZy. A resposta também pode ser reproduzida em voz."
        );

        card(
                lista,
                "⌨️  04 • DIGITAÇÃO",
                "Não quer falar? Toque na área de conversa e digite. A IZy responde normalmente pelo texto."
        );

        card(
                lista,
                "🧠  05 • INTELIGÊNCIA ARTIFICIAL",
                "A IZy pode explicar assuntos, responder perguntas, organizar ideias, resumir conteúdos e ajudar em tarefas."
        );

        card(
                lista,
                "📚  06 • ESTUDOS E LIÇÕES",
                "Ajuda com matérias escolares, exercícios, trabalhos, pesquisas, explicações e preparação para provas."
        );

        card(
                lista,
                "📝  07 • QUESTÕES E PROVAS",
                "Você pode enviar uma questão e pedir resolução, explicação passo a passo ou ajuda para entender o conteúdo."
        );

        card(
                lista,
                "🎬  08 • FILMES",
                "Área dedicada à exploração de filmes e conteúdos relacionados ao catálogo disponível no aplicativo."
        );

        card(
                lista,
                "🕘  09 • HISTÓRICO",
                "Mostra atividades e conversas anteriores para facilitar a continuidade do uso da DaNikeAI."
        );

        card(
                lista,
                "👤  10 • PERFIL",
                "Área com seus dados e informações pessoais utilizadas para personalizar a experiência."
        );

        card(
                lista,
                "✏️  11 • SEU NOME",
                "Você pode alterar o nome pelo qual deseja ser chamado. A IZy passa a utilizar o novo nome."
        );

        card(
                lista,
                "⚙️  12 • CONFIGURAÇÕES",
                "Controle recursos da IZy, permissões, voz, aparência, memória, efeitos e outras opções do aplicativo."
        );

        card(
                lista,
                "🔊  13 • VOZ DA IZy",
                "Controle a reprodução das respostas faladas e a interação por áudio."
        );

        card(
                lista,
                "🧠  14 • MEMÓRIA",
                "Recursos de memória permitem personalizar a experiência quando informações forem salvas de maneira apropriada."
        );

        card(
                lista,
                "📅  15 • DATA E HORA",
                "A IZy pode responder informações de data, horário e ajudar com perguntas relacionadas ao tempo."
        );

        card(
                lista,
                "🗓️  16 • CALENDÁRIO",
                "Quando autorizado, recursos do calendário podem ser utilizados para informações e organização."
        );

        card(
                lista,
                "📍  17 • LOCALIZAÇÃO",
                "Quando você conceder permissão, a localização poderá ser utilizada nos recursos que precisarem dela."
        );

        card(
                lista,
                "📷  18 • CÂMERA",
                "Permissões de câmera permitem recursos que dependam da câmera do aparelho."
        );

        card(
                lista,
                "🖼️  19 • FOTOS E ARQUIVOS",
                "Permissões de arquivos permitem trabalhar com conteúdos necessários aos recursos compatíveis."
        );

        card(
                lista,
                "👥  20 • CONTATOS",
                "Quando autorizado, o aplicativo poderá acessar recursos que dependam dos contatos."
        );

        card(
                lista,
                "🔔  21 • NOTIFICAÇÕES",
                "Notificações podem informar atualizações, atividades e eventos importantes do aplicativo."
        );

        card(
                lista,
                "✨  22 • EFEITOS VISUAIS",
                "A DaNikeAI possui elementos neon, animações e efeitos visuais para deixar a experiência mais dinâmica."
        );

        status(
                lista,
                "🟢  23 • RECURSOS DISPONÍVEIS",
                "Recursos funcionando normalmente na versão instalada."
        );

        status(
                lista,
                "🟡  24 • RECURSOS EM MELHORIA",
                "Algumas funções podem estar recebendo melhorias. Quando isso acontecer, o manual poderá indicar essa situação."
        );

        status(
                lista,
                "🔴  25 • MANUTENÇÃO",
                "Recursos temporariamente indisponíveis podem ser identificados como manutenção."
        );

        status(
                lista,
                "🔵  26 • NOVOS RECURSOS",
                "Novidades adicionadas nas atualizações podem aparecer no manual para orientar o usuário."
        );

        card(
                lista,
                "🔄  27 • ATUALIZAÇÕES",
                "A DaNikeAI possui mecanismo de atualização. Novas versões podem trazer correções, melhorias, novos recursos e mudanças no manual."
        );

        card(
                lista,
                "⚡  28 • ADM",
                "O Painel Administrativo é uma área restrita ao proprietário autorizado para gerenciamento administrativo do aplicativo."
        );

        card(
                lista,
                "🔐  29 • PERMISSÕES",
                "Alguns recursos dependem de permissões do Android. Você decide quais permissões conceder."
        );

        card(
                lista,
                "🌐  30 • CONEXÃO",
                "Alguns recursos de inteligência e serviços dependem de conexão com a internet."
        );

        card(
                lista,
                "🛡️  31 • SEGURANÇA",
                "O aplicativo separa recursos administrativos das funções normais do usuário."
        );

        card(
                lista,
                "📖  32 • MANUAL VIVO",
                "Este manual acompanha a evolução do aplicativo. Conforme novas funções forem liberadas, melhoradas ou colocadas em manutenção, as informações poderão ser atualizadas."
        );

        LinearLayout versao =
                new LinearLayout(this);

        versao.setOrientation(
                LinearLayout.VERTICAL
        );

        versao.setGravity(Gravity.CENTER);
        versao.setPadding(
                dp(12),
                dp(18),
                dp(12),
                dp(18)
        );

        GradientDrawable vf =
                fundo(
                        Color.argb(100, 0, 220, 255),
                        20
                );

        vf.setStroke(
                dp(1),
                Color.argb(170, 0, 225, 255)
        );

        versao.setBackground(vf);

        TextView v1 = texto(
                "DaNikeAI",
                22,
                Color.WHITE,
                true
        );

        TextView v2 = texto(
                "Versão " + BuildConfig.VERSION_NAME +
                "  •  Código " +
                BuildConfig.VERSION_CODE,
                15,
                Color.rgb(0, 235, 255),
                true
        );

        TextView v3 = texto(
                "Manual inicial • atualizado junto com o aplicativo",
                12,
                Color.rgb(150, 190, 220),
                false
        );

        versao.addView(v1);
        versao.addView(v2);
        versao.addView(v3);

        LinearLayout.LayoutParams vp =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        vp.setMargins(
                0,
                dp(8),
                0,
                dp(20)
        );

        lista.addView(versao, vp);
    }

    void card(
            LinearLayout lista,
            String titulo,
            String descricao
    ) {

        LinearLayout c =
                new LinearLayout(this);

        c.setOrientation(
                LinearLayout.VERTICAL
        );

        c.setPadding(
                dp(15),
                dp(13),
                dp(15),
                dp(13)
        );

        GradientDrawable g =
                fundo(
                        Color.argb(105, 4, 27, 50),
                        20
                );

        g.setStroke(
                dp(1),
                Color.argb(150, 0, 190, 255)
        );

        c.setBackground(g);

        TextView t =
                texto(
                        titulo,
                        16,
                        Color.rgb(0, 230, 255),
                        true
                );

        t.setGravity(Gravity.LEFT);

        TextView d =
                texto(
                        descricao,
                        13,
                        Color.rgb(195, 220, 240),
                        false
                );

        d.setGravity(Gravity.LEFT);

        c.addView(t);
        c.addView(d);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        p.setMargins(
                0,
                0,
                0,
                dp(8)
        );

        lista.addView(c, p);
    }

    void status(
            LinearLayout lista,
            String titulo,
            String descricao
    ) {
        card(
                lista,
                titulo,
                descricao
        );
    }

    Button botaoNeon(String texto) {

        Button b =
                new Button(this);

        b.setText(texto);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        GradientDrawable g =
                fundo(
                        Color.rgb(7, 75, 125),
                        24
                );

        g.setStroke(
                dp(1.5f),
                Color.rgb(0, 235, 255)
        );

        b.setBackground(g);

        return b;
    }

    class NeonManualBackground extends View {

        Paint p =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        float movimento = 0;

        NeonManualBackground(Context c) {
            super(c);

            setLayerType(
                    View.LAYER_TYPE_SOFTWARE,
                    null
            );

            postInvalidateDelayed(30);
        }

        @Override
        protected void onDraw(Canvas c) {

            super.onDraw(c);

            int w = getWidth();
            int h = getHeight();

            c.drawColor(
                    Color.rgb(1, 5, 15)
            );

            movimento += .018f;

            // brilho superior
            p.setShader(
                    new RadialGradient(
                            w * .5f,
                            h * .18f,
                            w * .75f,
                            new int[]{
                                    Color.argb(80, 0, 225, 255),
                                    Color.argb(25, 80, 50, 255),
                                    Color.TRANSPARENT
                            },
                            null,
                            Shader.TileMode.CLAMP
                    )
            );

            c.drawRect(
                    0,
                    0,
                    w,
                    h,
                    p
            );

            p.setShader(null);

            // linhas verticais neon
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));

            for (int x = -w; x < w * 2; x += dp(75)) {

                float deslocamento =
                        (float)Math.sin(
                                movimento + x * .01
                        ) * dp(10);

                p.setColor(
                        Color.argb(
                                45,
                                0,
                                210,
                                255
                        )
                );

                c.drawLine(
                        x + deslocamento,
                        h,
                        x + deslocamento + dp(120),
                        0,
                        p
                );
            }

            // grade inferior
            for (int y = h - dp(220);
                 y < h;
                 y += dp(28)) {

                p.setColor(
                        Color.argb(
                                35,
                                0,
                                220,
                                255
                        )
                );

                c.drawLine(
                        0,
                        y,
                        w,
                        y,
                        p
                );
            }

            for (int x = 0;
                 x < w;
                 x += dp(55)) {

                p.setColor(
                        Color.argb(
                                35,
                                120,
                                70,
                                255
                        )
                );

                c.drawLine(
                        x,
                        h - dp(220),
                        x - dp(100),
                        h,
                        p
                );
            }

            // pequenos pontos
            p.setStyle(Paint.Style.FILL);

            for (int i = 0; i < 18; i++) {

                float x =
                        (float)(
                                (i * 137) % Math.max(w, 1)
                        );

                float y =
                        (float)(
                                (i * 211) % Math.max(h, 1)
                        );

                int alpha =
                        (int)(
                                30 +
                                25 *
                                (
                                  (Math.sin(
                                    movimento * 2 + i
                                  ) + 1) / 2
                                )
                        );

                p.setColor(
                        Color.argb(
                                alpha,
                                0,
                                225,
                                255
                        )
                );

                c.drawCircle(
                        x,
                        y,
                        dp(1.5f),
                        p
                );
            }

            postInvalidateDelayed(30);
        }
    }
}
