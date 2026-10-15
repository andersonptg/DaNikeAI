from pathlib import Path
from datetime import datetime
import shutil
import re
import sys

p = Path("app/src/main/java/com/danike/ai/IAActivity.java")
s = p.read_text()

marker = "raiz.addView(barraModos, barraParams);"
if marker not in s:
    sys.exit("ERRO: código das abas não encontrado. Arquivo não alterado.")

backup = p.with_name(
    p.name + ".backup_ia_chat_" + datetime.now().strftime("%Y%m%d_%H%M%S")
)
shutil.copy2(p, backup)
print("Backup criado:", backup)

# Reforça a visibilidade da barra IA/CHAT.
if "DANIKE_ABAS_VISIVEIS_V2" not in s:
    s = s.replace(marker, marker + """
        // DANIKE_ABAS_VISIVEIS_V2
        barraModos.setClickable(true);
        barraModos.setFocusable(true);
        barraModos.setElevation(dp(40));
        barraModos.bringToFront();
        raiz.post(() -> {
            barraModos.bringToFront();
            barraModos.setVisibility(View.VISIBLE);
        });
""", 1)

# Reduz a espera ao recuperar a escuta após erros/resultados vazios.
s = s.replace(
    "iniciarEscutaContinuaComAtraso(700);",
    "iniciarEscutaContinuaComAtraso(250);"
)
s = s.replace(
    "iniciarEscutaContinuaComAtraso(300);",
    "iniciarEscutaContinuaComAtraso(180);"
)

# Após a fala, retoma com pequeno intervalo controlado.
for method in ("onDone", "onError"):
    pattern = (
        r"(@Override public void " + method +
        r"\(String utteranceId\)\s*\{.*?"
        r"if \(modoEscutaContinua\) )iniciarEscutaContinua\(\);"
    )
    s, count = re.subn(
        pattern,
        r"\1iniciarEscutaContinuaComAtraso(180);",
        s,
        count=1,
        flags=re.S
    )
    if count == 0:
        print("AVISO: retorno da fala não localizado em", method)

p.write_text(s)
print("CORRECAO APLICADA")
