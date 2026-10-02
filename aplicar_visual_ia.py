from pathlib import Path

p = Path('app/src/main/java/com/danike/ai/IAActivity.java')
s = p.read_text()

start = s.index('    void montarTela() {')
end = s.index('    void atualizarBotaoVozResposta()', start)

novo = r'''    void montarTela() {

        FrameLayout raiz = new FrameLayout(this);

        NeonBackground fundoNeon = new NeonBackground(this);
        raiz.addView(fundoNeon, new FrameLayout.LayoutParams(-1, -1));

        tela = new LinearLayout(this);
        tela.setOrientation(LinearLayout.VERTICAL);
        tela.setPadding(dp(10), dp(7), dp(10), dp(8));
        raiz.addView(tela, new FrameLayout.LayoutParams(-1, -1));

        // TOPO — mantido compacto
        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout tituloBox = new LinearLayout(this);
        tituloBox.setOrientation(LinearLayout.VERTICAL);

        TextView titulo = texto("DaNike AI", 25, Color.WHITE, true);
        TextView subtitulo = texto("SEU ASSISTENTE INTELIGENTE", 9, Color.rgb(0,225,255), true);
        tituloBox.addView(titulo, new LinearLayout.LayoutParams(-1, dp(34)));
        tituloBox.addView(subtitulo, new LinearLayout.LayoutParams(-1, dp(20)));
        topo.addView(tituloBox, new LinearLayout.LayoutParams(0, dp(58), 1));

        TextView instagram = texto("◎  anderson_lopes._ofc", 11, Color.WHITE, true);
        GradientDrawable instaFundo = fundo(Color.argb(35, 0, 120, 255), 18);
        instaFundo.setStroke(dp(1), Color.rgb(0,225,255));
        instagram.setGravity(Gravity.CENTER);
        topo.addView(instagram, new LinearLayout.LayoutParams(dp(155), dp(42)));

        Button configuracoes = botao("⚙", Color.TRANSPARENT);
        configuracoes.setTextSize(23);
        GradientDrawable configFundo = fundo(Color.rgb(3,15,30), 25);
        configFundo.setStroke(dp(2), Color.rgb(0,225,255));
        configuracoes.setBackground(configFundo);
        topo.addView(configuracoes, new LinearLayout.LayoutParams(dp(55), dp(55)));
        configuracoes.setOnClickListener(v -> startActivity(new Intent(IAActivity.this, ConfiguracoesActivity.class)));

        tela.addView(topo, new LinearLayout.LayoutParams(-1, dp(60)));

        statusIA = texto("●  IA ONLINE", 12, Color.rgb(60,255,130), true);
        statusIA.setGravity(Gravity.CENTER);
        tela.addView(statusIA, new LinearLayout.LayoutParams(-1, dp(25)));

        // ÁREA PRINCIPAL: caixa ocupa praticamente toda a tela
        FrameLayout area = new FrameLayout(this);

        LinearLayout painelChat = new LinearLayout(this);
        painelChat.setOrientation(LinearLayout.VERTICAL);
        painelChat.setPadding(dp(6), dp(5), dp(6), dp(5));

        GradientDrawable chatFundo = fundo(Color.argb(145, 1, 9, 25), 18);
        chatFundo.setStroke(dp(1), Color.rgb(0,170,255));
        painelChat.setBackground(chatFundo);

        LinearLayout cabecalhoChat = new LinearLayout(this);
        cabecalhoChat.setGravity(Gravity.CENTER_VERTICAL);
        tituloAssunto = texto("💬  Nova conversa", 11, Color.rgb(255,195,40), true);
        tituloAssunto.setSingleLine(true);
        cabecalhoChat.addView(tituloAssunto, new LinearLayout.LayoutParams(0, dp(27), 1));

        botaoVozResposta = botao("🔊", Color.TRANSPARENT);
        botaoVozResposta.setTextSize(12);
        botaoVozResposta.setMinWidth(0);
        botaoVozResposta.setMinimumWidth(0);
        botaoVozResposta.setPadding(0,0,0,0);
        botaoVozResposta.setBackground(fundo(Color.argb(40,0,210,255), 15));
        cabecalhoChat.addView(botaoVozResposta, new LinearLayout.LayoutParams(dp(38), dp(28)));
        painelChat.addView(cabecalhoChat, new LinearLayout.LayoutParams(-1, dp(30)));

        chatScroll = new ScrollView(this);
        chatScroll.setFillViewport(true);
        chatScroll.setVerticalScrollBarEnabled(true);
        chatScroll.setScrollbarFadingEnabled(false);
        chatScroll.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);

        chatLista = new LinearLayout(this);
        chatLista.setOrientation(LinearLayout.VERTICAL);
        chatLista.setPadding(dp(5), dp(4), dp(5), dp(4));
        chatScroll.addView(chatLista, new ScrollView.LayoutParams(-1, -2));
        painelChat.addView(chatScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout linhaMensagem = new LinearLayout(this);
        linhaMensagem.setGravity(Gravity.CENTER_VERTICAL);

        entrada = new EditText(this);
        entrada.setHint("Digite sua mensagem para a DaNikeAI...");
        entrada.setHintTextColor(Color.rgb(90,130,165));
        entrada.setTextColor(Color.WHITE);
        entrada.setTextSize(14);
        entrada.setSingleLine(true);
        entrada.setImeOptions(EditorInfo.IME_ACTION_SEND);
        entrada.setPadding(dp(14),0,dp(9),0);
        GradientDrawable campoFundo = fundo(Color.rgb(2,12,27), 25);
        campoFundo.setStroke(dp(1), Color.rgb(0,205,255));
        entrada.setBackground(campoFundo);
        linhaMensagem.addView(entrada, new LinearLayout.LayoutParams(0, dp(53), 1));

        Button enviar = botao("➤", Color.TRANSPARENT);
        enviar.setTextSize(21);
        GradientDrawable enviarFundo = fundo(Color.rgb(3,65,125), 27);
        enviarFundo.setStroke(dp(1), Color.rgb(0,225,255));
        enviar.setBackground(enviarFundo);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(dp(55), dp(53));
        ep.setMargins(dp(6),0,0,0);
        linhaMensagem.addView(enviar, ep);
        painelChat.addView(linhaMensagem, new LinearLayout.LayoutParams(-1, dp(58)));

        area.addView(painelChat, new FrameLayout.LayoutParams(-1, -1));

        // Rosto menor no lado direito — usa a mesma DaNikeAIFaceView
        rostoIA = new DaNikeAIFaceView(this);
        int tamanhoRosto = dp(128);
        FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(tamanhoRosto, tamanhoRosto, Gravity.RIGHT | Gravity.TOP);
        rp.setMargins(0, dp(2), dp(8), 0);
        area.addView(rostoIA, rp);

        // Luz dourada que aparece somente quando a IA fala
        luzFala = new View(this);
        GradientDrawable luz = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.argb(150,255,185,0), Color.argb(45,255,130,0), Color.TRANSPARENT});
        luz.setShape(GradientDrawable.OVAL);
        luzFala.setBackground(luz);
        luzFala.setVisibility(View.GONE);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(170), dp(170), Gravity.RIGHT | Gravity.TOP);
        lp.setMargins(0, -dp(12), -dp(10), 0);
        area.addView(luzFala, lp);
        luzFala.bringToFront();
        rostoIA.bringToFront();

        // Voz sempre discreta na borda da caixa
        botaoVozResposta.bringToFront();

        LinearLayout.LayoutParams areaParams = new LinearLayout.LayoutParams(-1, 0, 1);
        tela.addView(area, areaParams);

        // CONTROLES DA IA — substituem a navegação inferior dentro da IA
        LinearLayout controles = new LinearLayout(this);
        controles.setGravity(Gravity.CENTER);
        controles.setPadding(0, dp(5), 0, 0);

        Button cerebro = criarControleIA("🧠", "CÉREBRO", true);
        Button microfone = criarControleIA("🎙", "MICROFONE", true);
        Button escuro = criarControleIA("☾", "MODO ESCURO", true);
        Button claro = criarControleIA("☀", "MODO CLARO", false);

        controles.addView(cerebro, new LinearLayout.LayoutParams(0, dp(63), 1));
        controles.addView(microfone, new LinearLayout.LayoutParams(0, dp(63), 1));
        controles.addView(escuro, new LinearLayout.LayoutParams(0, dp(63), 1));
        controles.addView(claro, new LinearLayout.LayoutParams(0, dp(63), 1));
        tela.addView(controles, new LinearLayout.LayoutParams(-1, dp(70)));

        falaAutomatica = getSharedPreferences("danike_config", 0).getBoolean("fala_automatica", true);
        atualizarBotaoVozResposta();

        cerebro.setOnClickListener(v -> alternarControle(cerebro, "cerebro_ativo"));
        microfone.setOnClickListener(v -> {
            alternarControle(microfone, "microfone_ativo");
            if (reconhecedor == null) {
                Toast.makeText(this, "Reconhecimento de voz indisponível.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ouvindo) {
                try { reconhecedor.stopListening(); } catch (Exception ignored) {}
                ouvindo = false;
                atualizarConexao();
                return;
            }
            if (falando) pararFala();
            statusIA.setText("🎤  OUVINDO...");
            statusIA.setTextColor(Color.rgb(0,220,255));
            try { reconhecedor.startListening(intentVoz); }
            catch (Exception e) { Toast.makeText(this, "Não foi possível iniciar o microfone.", Toast.LENGTH_SHORT).show(); }
        });
        escuro.setOnClickListener(v -> {
            getSharedPreferences("danike_config",0).edit().putBoolean("modo_escuro",true).apply();
            marcarControle(escuro,true);
            marcarControle(claro,false);
        });
        claro.setOnClickListener(v -> {
            getSharedPreferences("danike_config",0).edit().putBoolean("modo_escuro",false).apply();
            marcarControle(claro,true);
            marcarControle(escuro,false);
        });

        enviar.setOnClickListener(v -> responder());
        entrada.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                responder();
                return true;
            }
            return false;
        });
        botaoVozResposta.setOnClickListener(v -> {
            falaAutomatica = !falaAutomatica;
            getSharedPreferences("danike_config",0).edit().putBoolean("fala_automatica",falaAutomatica).apply();
            if (!falaAutomatica) pararFala();
            atualizarBotaoVozResposta();
        });

        boolean escuroAtivo = getSharedPreferences("danike_config",0).getBoolean("modo_escuro",true);
        marcarControle(escuro, escuroAtivo);
        marcarControle(claro, !escuroAtivo);
        marcarControle(cerebro, getSharedPreferences("danike_config",0).getBoolean("cerebro_ativo",true));
        marcarControle(microfone, getSharedPreferences("danike_config",0).getBoolean("microfone_ativo",true));

        setContentView(raiz);
    }

    Button criarControleIA(String icone, String nome, boolean ativo) {
        Button b = botao(icone + "\n" + nome + "  ●", Color.TRANSPARENT);
        b.setTextSize(10);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setPadding(0,0,0,0);
        marcarControle(b, ativo);
        return b;
    }

    void marcarControle(Button b, boolean ativo) {
        GradientDrawable g = fundo(ativo ? Color.argb(55,255,170,0) : Color.argb(28,0,80,140), 17);
        g.setStroke(dp(1), ativo ? Color.rgb(255,190,35) : Color.rgb(0,170,255));
        b.setBackground(g);
        b.setTextColor(ativo ? Color.rgb(255,235,150) : Color.rgb(180,215,240));
        b.setTag(ativo);
        if (ativo) animarFaiscas(b); else b.clearAnimation();
    }

    void alternarControle(Button b, String chave) {
        boolean ativo = !(b.getTag() instanceof Boolean && (Boolean)b.getTag());
        getSharedPreferences("danike_config",0).edit().putBoolean(chave,ativo).apply();
        marcarControle(b,ativo);
    }

    void animarFaiscas(View v) {
        v.animate().alpha(0.72f).setDuration(260).withEndAction(() ->
                v.animate().alpha(1f).setDuration(260).withEndAction(() -> {
                    if (Boolean.TRUE.equals(v.getTag())) animarFaiscas(v);
                }).start()
        ).start();
    }

'''

s = s[:start] + novo + s[end:]

# Campo para a luz dourada
needle = '    boolean falando = false;'
if 'View luzFala;' not in s:
    s = s.replace(needle, needle + '\n    View luzFala;')

# Ativa/desativa luz dourada junto da fala, sem alterar DaNikeAIFaceView
s = s.replace('        rostoIA.setFalando(true);', '        rostoIA.setFalando(true);\n        if (luzFala != null) { luzFala.setVisibility(View.VISIBLE); luzFala.animate().alpha(1f).setDuration(220).start(); }')
s = s.replace('        if (rostoIA != null) {\n            rostoIA.setFalando(false);\n        }', '        if (rostoIA != null) {\n            rostoIA.setFalando(false);\n        }\n\n        if (luzFala != null) { luzFala.animate().alpha(0f).setDuration(180).withEndAction(() -> luzFala.setVisibility(View.GONE)).start(); }')

p.write_text(s)
print('OK - visual aplicado')
