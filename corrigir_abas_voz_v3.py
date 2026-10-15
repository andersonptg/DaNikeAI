from pathlib import Path
from datetime import datetime
import shutil
import re
import sys

p = Path("app/src/main/java/com/danike/ai/IAActivity.java")
s = p.read_text()

# Confere se o arquivo contém os pontos esperados.
for trecho in (
    "LinearLayout painelChat = new LinearLayout(this);",
    "LinearLayout linhaMensagem = new LinearLayout(this);",
    "Runnable abrirModoChat = () -> {",
    "private void iniciarEscutaContinua() {",
    "boolean modoEscutaContinua = false;"
):
    if trecho not in s:
        sys.exit("ERRO: trecho esperado não encontrado: " + trecho +
                 "\nNenhuma alteração feita.")

backup = p.with_name(
    p.name + ".backup_v3_" + datetime.now().strftime("%Y%m%d_%H%M%S")
)
shutil.copy2(p, backup)
print("BACKUP:", backup)

# 1. Identifica os elementos reais da conversa por tags.
if 'painelChat.setTag("danike_painel_chat_abas");' not in s:
    s = s.replace(
        "LinearLayout painelChat = new LinearLayout(this);",
        'LinearLayout painelChat = new LinearLayout(this);\n'
        '        painelChat.setTag("danike_painel_chat_abas");',
        1
    )

if 'linhaMensagem.setTag("danike_linha_mensagem_abas");' not in s:
    s = s.replace(
        "LinearLayout linhaMensagem = new LinearLayout(this);",
        'LinearLayout linhaMensagem = new LinearLayout(this);\n'
        '        linhaMensagem.setTag("danike_linha_mensagem_abas");',
        1
    )

# 2. Substitui a lógica frágil de índices fixos da aba CHAT.
padrao = (
    r"Runnable abrirModoChat = \(\) -> \{.*?"
    r"\n        modoIA\.setOnClickListener"
)
nova_logica = '''Runnable abrirModoChat = () -> {
            View painel = tela.findViewWithTag("danike_painel_chat_abas");
            View linha = tela.findViewWithTag("danike_linha_mensagem_abas");

            if (painel == null || linha == null) {
                Toast.makeText(this,
                        "Não encontrei os componentes da conversa.",
                        Toast.LENGTH_LONG).show();
                abrirModoIA.run();
                return;
            }

            for (int i = 0; i < tela.getChildCount(); i++) {
                tela.getChildAt(i).setVisibility(View.GONE);
            }

            painel.setVisibility(View.VISIBLE);
            linha.setVisibility(View.VISIBLE);

            tela.setPadding(dp(12), dp(126), dp(12), dp(8));
            camadaMagnexp.setVisibility(View.GONE);
            tela.setVisibility(View.VISIBLE);

            setBotaoAtivo(modoIA, false);
            setBotaoAtivo(modoChat, true);

            if (chatScroll != null) {
                chatScroll.post(() ->
                        chatScroll.fullScroll(View.FOCUS_DOWN));
            }
        };

        modoIA.setOnClickListener'''
s, n = re.subn(padrao, nova_logica, s, count=1, flags=re.S)
if n != 1:
    shutil.copy2(backup, p)
    sys.exit("ERRO ao substituir a aba CHAT. Backup restaurado.")

# 3. Impede tentativas simultâneas de iniciar o reconhecedor.
if "boolean iniciandoEscuta = false;" not in s:
    s = s.replace(
        "boolean modoEscutaContinua = false;",
        "boolean modoEscutaContinua = false;\n"
        "    boolean iniciandoEscuta = false;",
        1
    )

# Libera o estado de inicialização nos retornos do reconhecedor.
s = s.replace(
    "@Override public void onReadyForSpeech(Bundle params) {\n"
    "                ouvindo = true;",
    "@Override public void onReadyForSpeech(Bundle params) {\n"
    "                iniciandoEscuta = false;\n"
    "                ouvindo = true;",
    1
)
s = s.replace(
    "@Override public void onError(int error) {\n"
    "                ouvindo = false;",
    "@Override public void onError(int error) {\n"
    "                iniciandoEscuta = false;\n"
    "                ouvindo = false;",
    1
)
s = s.replace(
    "@Override public void onResults(Bundle resultados) {\n"
    "                ouvindo = false;",
    "@Override public void onResults(Bundle resultados) {\n"
    "                iniciandoEscuta = false;\n"
    "                ouvindo = false;",
    1
)

# Reescreve somente o método que inicia a escuta contínua.
padrao_metodo = (
    r"    private void iniciarEscutaContinua\(\) \{.*?"
    r"\n    private void iniciarEscutaContinuaComAtraso"
)
novo_metodo = '''    private void iniciarEscutaContinua() {
        if (!modoEscutaContinua || reconhecedor == null) return;

        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Evita iniciar sessões concorrentes.
        if (ouvindo || iniciandoEscuta) return;

        try {
            iniciandoEscuta = true;

            if (statusIA != null) {
                statusIA.setText("●  OUVINDO");
                statusIA.setTextColor(Color.rgb(0, 225, 255));
            }
            if (indicador != null) {
                indicador.setText("●  PREPARANDO MICROFONE...");
            }

            reconhecedor.startListening(intentVoz);
        } catch (Exception e) {
            iniciandoEscuta = false;
            ouvindo = false;
            iniciarEscutaContinuaComAtraso(350);
        }
    }

    private void iniciarEscutaContinuaComAtraso'''
s, n = re.subn(padrao_metodo, novo_metodo, s, count=1, flags=re.S)
if n != 1:
    shutil.copy2(backup, p)
    sys.exit("ERRO ao atualizar a escuta. Backup restaurado.")

p.write_text(s)
print("CORRECAO APLICADA")
print("Backup preservado:", backup)
