from pathlib import Path
from datetime import datetime
import re
import sys

p = Path("app/src/main/java/com/danike/ai/MainActivity.java")
original = p.read_text(encoding="utf-8")
s = original

def sub(padrao, novo, nome):
    global s
    s2, n = re.subn(padrao, lambda m: novo, s, count=1, flags=re.S)
    if n != 1:
        print(f"ERRO em {nome}: {n} ocorrências. Arquivo original preservado.")
        sys.exit(1)
    s = s2

# Botão novo imediatamente antes de Conexões.
sub(
    r'TextView\s+botaoConexoes\s*=\s*txt\("👥\\nConexões",\s*12,\s*Color\.WHITE\);',
    'TextView botaoColaboradores = txt("👥\\\\nColaboradores", 11, Color.WHITE);\n'
    '        botaoColaboradores.setGravity(Gravity.CENTER);\n'
    '        botaoColaboradores.setTypeface(Typeface.DEFAULT, Typeface.BOLD);\n'
    '        TextView botaoConexoes = txt("👥\\\\nConexões", 12, Color.WHITE);',
    "botão Colaboradores"
)

# Inserir o novo botão nos arrays de navegação.
sub(
    r'botaoHome\s*,\s*botaoConexoes\s*,\s*botaoPerfil\s*,\s*botaoMais\s*,',
    'botaoHome, botaoColaboradores, botaoConexoes, botaoPerfil, botaoMais,',
    "navegação do proprietário"
)
sub(
    r'botaoHome\s*,\s*botaoConexoes\s*,\s*botaoPerfil\s*,\s*botaoMais',
    'botaoHome, botaoColaboradores, botaoConexoes, botaoPerfil, botaoMais',
    "navegação comum"
)

# Estilo para o novo botão.
sub(
    r'botaoConexoes\.setBackground\(fundoConexoes\);',
    'botaoConexoes.setBackground(fundoConexoes);\n'
    '        GradientDrawable fundoColaboradores = new GradientDrawable();\n'
    '        fundoColaboradores.setColor(Color.rgb(20, 35, 70));\n'
    '        fundoColaboradores.setCornerRadius(dp(18));\n'
    '        fundoColaboradores.setStroke(dp(1), Color.rgb(70, 145, 255));\n'
    '        botaoColaboradores.setBackground(fundoColaboradores);\n'
    '        botaoColaboradores.setElevation(dp(10));',
    "estilo"
)

# Home e Colaboradores alternam entre tela vazia e conteúdo existente.
sub(
    r'botaoHome\.setOnClickListener\(v\s*->\s*\{\s*scroll\.smoothScrollTo\(0,\s*0\);\s*\}\);',
    'botaoHome.setOnClickListener(v -> {\n'
    '            tela.setVisibility(View.GONE);\n'
    '            equipeTopo.setVisibility(View.GONE);\n'
    '            botaoMenu.setVisibility(View.GONE);\n'
    '            telaHomeVazia.setVisibility(View.VISIBLE);\n'
    '        });\n'
    '        botaoColaboradores.setOnClickListener(v -> {\n'
    '            telaHomeVazia.setVisibility(View.GONE);\n'
    '            tela.setVisibility(View.VISIBLE);\n'
    '            equipeTopo.setVisibility(View.VISIBLE);\n'
    '            botaoMenu.setVisibility(View.VISIBLE);\n'
    '            scroll.smoothScrollTo(0, 0);\n'
    '        });',
    "cliques Home/Colaboradores"
)

# Criar a tela Home vazia sobre o fundo atual.
sub(
    r'raiz\.addView\(\s*fundo,\s*new FrameLayout\.LayoutParams\(-1,\s*-1\)\s*\);',
    'raiz.addView(fundo, new FrameLayout.LayoutParams(-1, -1));\n'
    '        LinearLayout telaHomeVazia = new LinearLayout(this);\n'
    '        telaHomeVazia.setOrientation(LinearLayout.VERTICAL);\n'
    '        telaHomeVazia.setGravity(Gravity.CENTER);\n'
    '        TextView mensagemHome = txt("🏠\\\\nHome", 24, Color.WHITE);\n'
    '        mensagemHome.setGravity(Gravity.CENTER);\n'
    '        mensagemHome.setTypeface(Typeface.DEFAULT, Typeface.BOLD);\n'
    '        telaHomeVazia.addView(mensagemHome, new LinearLayout.LayoutParams(-1, dp(100)));\n'
    '        FrameLayout.LayoutParams homeVaziaParams = new FrameLayout.LayoutParams(-1, -1);\n'
    '        homeVaziaParams.bottomMargin = dp(76);\n'
    '        raiz.addView(telaHomeVazia, homeVaziaParams);',
    "tela Home"
)

# Ocultar o conteúdo antigo ao abrir o app.
sub(
    r'raiz\.addView\(tela,\s*telaParams\);',
    'raiz.addView(tela, telaParams);\n'
    '        tela.setVisibility(View.GONE);\n'
    '        equipeTopo.setVisibility(View.GONE);\n'
    '        botaoMenu.setVisibility(View.GONE);',
    "visibilidade inicial"
)

backup = p.with_name(
    p.name + ".backup-" + datetime.now().strftime("%Y%m%d-%H%M%S")
)
backup.write_text(original, encoding="utf-8")
p.write_text(s, encoding="utf-8")
print("ALTERAÇÃO APLICADA COM SUCESSO.")
print("Backup:", backup)
print("A ação original de Conexões foi mantida.")
