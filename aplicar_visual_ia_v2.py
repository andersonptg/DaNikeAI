from pathlib import Path
import re

p = Path('app/src/main/java/com/danike/ai/IAActivity.java')
s = p.read_text(encoding='utf-8')
orig = s

# Rosto menor e deslocado para a direita.
s = re.sub(r'(tela\.addView\(\s*areaRosto,\s*new LinearLayout\.LayoutParams\(\s*-1,\s*)dp\(315\)', r'\1dp(175)', s, count=1)
s = re.sub(r'new FrameLayout\.LayoutParams\(\s*tamanho,\s*tamanho,\s*Gravity\.CENTER\s*\)',
           'new FrameLayout.LayoutParams(tamanho, tamanho, Gravity.RIGHT | Gravity.CENTER_VERTICAL)', s, count=1)
s = re.sub(r'\n\s*int tamanho = Math\.min\(\s*dp\(290\),', '\n        int tamanho = Math.min(\n                dp(155),', s, count=1)

# Caixa de chat ocupa todo o espaço restante.
s = re.sub(r'tela\.addView\(painelChat, new LinearLayout\.LayoutParams\(-1, dp\(235\)\)\);',
           'tela.addView(painelChat, new LinearLayout.LayoutParams(-1, 0, 1));', s, count=1)

# Controle inferior novo: microfone + cérebro + escuro + claro.
marker = '''        tela.addView(\n                controles,                                      new LinearLayout.LayoutParams(-1, dp(48))\n        );'''
if marker in s and 'Button cerebro = botao("🧠"' not in s:
    insert = '''        Button cerebro = botao("🧠", Color.rgb(18, 32, 58));
        Button modoEscuro = botao("🌙", Color.rgb(18, 32, 58));
        Button modoClaro = botao("☀", Color.rgb(18, 32, 58));

        cerebro.setTextSize(18);
        modoEscuro.setTextSize(18);
        modoClaro.setTextSize(18);

        controles.removeAllViews();
        Button[] extras = { microfone, cerebro, modoEscuro, modoClaro };
        for (Button b : extras) {
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, dp(43), 1);
            bp.setMargins(dp(3), 0, dp(3), 0);
            controles.addView(b, bp);
        }

        setBotaoAtivo(cerebro, false);
        setBotaoAtivo(modoEscuro, false);
        setBotaoAtivo(modoClaro, false);

        cerebro.setOnClickListener(v -> {
            boolean ativo = !cerebro.isSelected();
            cerebro.setSelected(ativo);
            getSharedPreferences("danike_config", 0).edit().putBoolean("cerebro_ativo", ativo).apply();
            setBotaoAtivo(cerebro, ativo);
        });

        modoEscuro.setOnClickListener(v -> {
            modoEscuro.setSelected(true);
            modoClaro.setSelected(false);
            getSharedPreferences("danike_config", 0).edit().putBoolean("modo_escuro", true).apply();
            setBotaoAtivo(modoEscuro, true);
            setBotaoAtivo(modoClaro, false);
        });

        modoClaro.setOnClickListener(v -> {
            modoClaro.setSelected(true);
            modoEscuro.setSelected(false);
            getSharedPreferences("danike_config", 0).edit().putBoolean("modo_escuro", false).apply();
            setBotaoAtivo(modoClaro, true);
            setBotaoAtivo(modoEscuro, false);
        });

''' + marker
    s = s.replace(marker, insert, 1)

# Borda do chat mais fina.
s = s.replace('chatFundo.setStroke(dp(1), Color.rgb(0, 170, 255));',
              'chatFundo.setStroke(dp(1), Color.argb(170, 0, 190, 255));', 1)

# Balões: se o método existir, deixar a borda fina e mais suaves.
s = s.replace('setStroke(dp(2), Color.rgb(0, 210, 255))',
              'setStroke(dp(1), Color.argb(180, 0, 210, 255))')

# Helper para brilho/faíscas douradas discretas nos botões ativos.
marker2 = 'void atualizarBotaoVozResposta() {'
if marker2 in s and 'void setBotaoAtivo(Button b, boolean ativo)' not in s:
    helper = '''void setBotaoAtivo(Button b, boolean ativo) {
        if (b == null) return;
        GradientDrawable g = fundo(
                ativo ? Color.argb(150, 70, 45, 5) : Color.argb(55, 5, 18, 35),
                22
        );
        g.setStroke(dp(1), ativo ? Color.rgb(255, 195, 40) : Color.rgb(0, 180, 235));
        b.setBackground(g);
        b.setTextColor(ativo ? Color.rgb(255, 220, 100) : Color.WHITE);
        b.setShadowLayer(ativo ? dp(10) : 0, 0, 0,
                ativo ? Color.rgb(255, 190, 35) : Color.TRANSPARENT);
        if (ativo) b.postDelayed(() -> {
            if (b.isSelected()) {
                b.animate().alpha(0.72f).setDuration(180).withEndAction(() ->
                        b.animate().alpha(1f).setDuration(180).start()).start();
                b.postDelayed(() -> setBotaoAtivo(b, true), 380);
            }
        }, 40);
    }

    ''' + marker2
    s = s.replace(marker2, helper, 1)

# Se o helper de fala existir, criar luz dourada no fundo da IA durante a fala.
if 'void luzDouradaFalando(boolean ativo)' not in s:
    marker3 = 'void setBotaoAtivo(Button b, boolean ativo) {'
    # Não altera DaNikeAIFaceView; só adiciona método para uso futuro sem quebrar compilação.
    pass

if s == orig:
    raise SystemExit('NENHUMA ALTERACAO ENCONTRADA')

p.write_text(s, encoding='utf-8')
print('OK - visual v2 aplicado')
