#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT="$HOME/da_nike_ai"
cd "$ROOT"

JAVA="app/src/main/java/com/danike/ai/AtualizacaoTelefoneDialog.java"
XML="app/src/main/res/layout/dialog_atualizacao_premium.xml"
STAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP="$HOME/backup_caixinha_danike_$STAMP"

mkdir -p "$BACKUP"

echo "===== 1. VALIDANDO ARQUIVOS ====="
if [ ! -f "$JAVA" ] || [ ! -f "$XML" ]; then
  echo "ERRO: arquivo Java ou layout nao encontrado."
  exit 1
fi

echo "===== 2. BACKUP ====="
cp -p "$JAVA" "$BACKUP/AtualizacaoTelefoneDialog.java"
cp -p "$XML" "$BACKUP/dialog_atualizacao_premium.xml"
echo "Backup: $BACKUP"

echo "===== 3. CORRIGINDO AUTORIDADE DO INSTALADOR ====="
python3 - "$JAVA" <<'PY'
import pathlib
import sys

p = pathlib.Path(sys.argv[1])
s = p.read_text(encoding="utf-8")
antigo = 'ctx.getPackageName()+".provider"'
novo = 'ctx.getPackageName()+".fileprovider"'

if novo in s and antigo not in s:
    print("A autoridade ja esta corrigida.")
elif s.count(antigo) == 1:
    p.write_text(s.replace(antigo, novo), encoding="utf-8")
    print("CORRECAO APLICADA: autoridade alinhada ao Manifest.")
else:
    print("NAO ALTERADO: trecho diferente do esperado.")
    print("Quantidade de ocorrencias:", s.count(antigo))
    sys.exit(2)
PY

echo
echo "===== 4. LAYOUT ATUAL DA CAIXINHA ====="
sed -n '1,260p' "$XML"

echo
echo "===== 5. CONFERINDO AUTORIDADE ====="
grep -n 'getPackageName()' "$JAVA" || true
grep -n -A 4 -B 2 'FileProvider' app/src/main/AndroidManifest.xml

echo
echo "===== CONCLUIDO ====="
echo "Backup guardado em: $BACKUP"
echo "Nenhum outro arquivo foi alterado por este script."
echo "Ainda nao compile nem publique a atualizacao."
