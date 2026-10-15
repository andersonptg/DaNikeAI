from pathlib import Path
from datetime import datetime
import sys

p = Path("app/src/main/java/com/danike/ai/IAActivity.java")
if not p.is_file():
print("ERRO: arquivo IAActivity.java não encontrado.")
sys.exit(1)

s = p.read_text(encoding="utf-8")
old = "@Override public void onPartialResults(Bundle resultados) {}"
new = '''@Override public void onPartialResults(Bundle resultados) {
if (resultados == null || entrada == null) return;
ArrayList<String> parciais = resultados.getStringArrayList(
SpeechRecognizer.RESULTS_RECOGNITION);
if (parciais == null || parciais.isEmpty()) return;
String texto = parciais.get(0);
if (texto == null || texto.trim().isEmpty()) return;
entrada.setText(texto.trim());
entrada.setSelection(entrada.length());
}'''

true_line = "intentVoz.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);"
false_line = "intentVoz.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);"

if s.count(new) == 1 and s.count(true_line) == 1:
print("OK: a transcricao parcial ja esta aplicada.")
sys.exit(0)

if s.count(old) != 1:
print("CANCELADO: metodo de voz diferente do esperado.")
sys.exit(1)

if s.count(true_line) == 1:
pass
elif s.count(false_line) == 1:
s = s.replace(false_line, true_line, 1)
else:
print("CANCELADO: configuracao de voz inesperada.")
sys.exit(1)

s = s.replace(old, new, 1)
backup = p.with_name("IAActivity.java.backup_voz_" + datetime.now().strftime("%Y%m%d_%H%M%S"))
backup.write_text(p.read_text(encoding="utf-8"), encoding="utf-8")
p.write_text(s, encoding="utf-8")

check = p.read_text(encoding="utf-8")
if check.count(new) != 1 or check.count(true_line) != 1:
print("ERRO: verificacao falhou; restaurando backup.")
p.write_text(backup.read_text(encoding="utf-8"), encoding="utf-8")
sys.exit(1)

print("CORRECAO APLICADA")
print("Backup:", backup)
print("Transcricao parcial: ativada")
print("GitHub e Render: nao publicados")
