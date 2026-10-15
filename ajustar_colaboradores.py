from pathlib import Path
from datetime import datetime
import sys

p = Path("app/src/main/java/com/danike/ai/MainActivity.java")
original = p.read_text(encoding="utf-8")
s = original

def trocar(antigo, novo, nome):
    global s
    n = s.count(antigo)
    if n != 1:
        print(f"ERRO em {nome}: trecho encontrado {n} vezes. Nada foi salvo.")
        sys.exit(1)
    s = s.replace(antigo, novo, 1)

trocar(
'''        TextView botaoConexoes =                                    txt("👥\\nConexões", 12, Color.WHITE);''',
'''        TextView botaoColaboradores =
                txt("👥\\nColaboradores", 11, Color.WHITE);
        botaoColaboradores.setGravity(Gravity.CENTER);
        botaoColaboradores.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView botaoConexoes =                                    txt("👥\\nConexões", 12, Color.WHITE);''',
"criar botão"
)

trocar(
'''                        botaoHome,                                          botaoConexoes,                                      botaoPerfil,                                        botaoMais,''',
'''                        botaoHome, botaoColaboradores, botaoConexoes,
                        botaoPerfil, botaoMais,''',
"navegação do proprietário"
)

trocar(
'''                        botaoHome,
                        botaoConexoes,                                      botaoPerfil,''',
'''                        botaoHome,
                        botaoColaboradores,
                        botaoConexoes,                                      botaoPerfil,''',
"navegação comum"
)

trocar(
'''        botaoConexoes.setBackground(fundoConexoes);''',
'''        botaoConexoes.setBackground(fundoConexoes);
        GradientDrawable fundoColaboradores = new GradientDrawable();
        fundoColaboradores.setColor(Color.rgb(20, 35, 70));
        fundoColaboradores.setCornerRadius(dp(18));
        fundoColaboradores.setStroke(dp(1), Color.rgb(70, 145, 255));
        botaoColaboradores.setBackground(fundoColaboradores);
        botaoColaboradores.setElevation(dp(10));''',
"estilo do botão"
)

trocar(
'''        botaoHome.setOnClickListener(v -> {
            scroll.smoothScrollTo(0, 0);
        });''',
'''        botaoHome.setOnClickListener(v -> {
            tela.setVisibility(View.GONE);
            equipeTopo.setVisibility(View.GONE);
            botaoMenu.setVisibility(View.GONE);
            telaHomeVazia.setVisibility(View.VISIBLE);
        });

        botaoColaboradores.setOnClickListener(v -> {
            telaHomeVazia.setVisibility(View.GONE);
            tela.setVisibility(View.VISIBLE);
            equipeTopo.setVisibility(View.VISIBLE);
            botaoMenu.setVisibility(View.VISIBLE);
            scroll.smoothScrollTo(0, 0);
        });''',
"cliques da navegação"
)

trocar(
'''        raiz.addView(
                fundo,                                              new FrameLayout.LayoutParams(-1, -1)        );''',
'''        raiz.addView(
                fundo,                                              new FrameLayout.LayoutParams(-1, -1)        );

        LinearLayout telaHomeVazia = new LinearLayout(this);
        telaHomeVazia.setOrientation(LinearLayout.VERTICAL);
        telaHomeVazia.setGravity(Gravity.CENTER);
        TextView mensagemHome = txt("🏠\\\\nHome", 24, Color.WHITE);
        mensagemHome.setGravity(Gravity.CENTER);
        mensagemHome.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        telaHomeVazia.addView(mensagemHome,
                new LinearLayout.LayoutParams(-1, dp(100)));
        FrameLayout.LayoutParams homeVaziaParams =
                new FrameLayout.LayoutParams(-1, -1);
        homeVaziaParams.bottomMargin = dp(76);
        raiz.addView(telaHomeVazia, homeVaziaParams);''',
"tela Home vazia"
)

trocar(
'''        raiz.addView(tela, telaParams);''',
'''        raiz.addView(tela, telaParams);
        tela.setVisibility(View.GONE);
        equipeTopo.setVisibility(View.GONE);
        botaoMenu.setVisibility(View.GONE);''',
"tela inicial"
)

backup = p.with_name(p.name + ".backup-" + datetime.now().strftime("%Y%m%d-%H%M%S"))
backup.write_text(original, encoding="utf-8")
p.write_text(s, encoding="utf-8")
print("ALTERAÇÃO APLICADA COM SUCESSO.")
print("Backup:", backup)
print("A ação original de Conexões foi preservada.")
