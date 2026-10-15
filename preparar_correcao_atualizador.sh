#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT="$HOME/da_nike_ai"
cd "$ROOT"

STAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP="$HOME/backup_danike_atualizador_$STAMP"
mkdir -p "$BACKUP"

echo "=========================================="
echo " DA NIKE AI - PREPARAR CORRECAO"
echo "=========================================="

# Verifica se estamos no projeto correto
if [ ! -d app/src/main ]; then
  echo "ERRO: pasta app/src/main nao encontrada."
  exit 1
fi

# Guarda o estado Git, quando disponivel
if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  git status --short > "$BACKUP/git_status.txt"
  git diff > "$BACKUP/alteracoes_git.diff"
  git rev-parse HEAD > "$BACKUP/commit_atual.txt"
fi

# Backup dos fontes e recursos Android
tar -czf "$BACKUP/fontes_recursos.tar.gz" \
  app/src/main/java \
  app/src/main/res \
  app/src/main/AndroidManifest.xml \
  2>"$BACKUP/backup_erros.txt" || {
    echo "ERRO ao criar backup. Confira:"
    cat "$BACKUP/backup_erros.txt"
    exit 1
  }

echo
echo "BACKUP CRIADO:"
echo "$BACKUP/fontes_recursos.tar.gz"
echo
echo "Buscando a tela de atualizacao..."

REPORT="$BACKUP/relatorio_atualizador.txt"
: > "$REPORT"

# Busca referências a diálogos, atualizações e ícones
if command -v rg >/dev/null 2>&1; then
  rg -n -i \
    'atualiza|update|versao|version|download|instal|AlertDialog|MaterialAlertDialogBuilder|ProgressDialog|cloud|nuvem|apk|setView|setPositiveButton|setNegativeButton' \
    app/src/main/java app/src/main/res \
    -g '*.java' -g '*.xml' \
    > "$REPORT" || true
else
  grep -RniE \
    'atualiza|update|versao|version|download|instal|AlertDialog|cloud|nuvem|apk|setView|setPositiveButton|setNegativeButton' \
    app/src/main/java app/src/main/res \
    --include='*.java' --include='*.xml' \
    > "$REPORT" || true
fi

echo "RELATORIO GERADO:"
echo "$REPORT"
echo
echo "===== TRECHOS ENCONTRADOS ====="
sed -n '1,220p' "$REPORT"

echo
echo "=========================================="
echo "DIAGNOSTICO CONCLUIDO"
echo "Nenhum fonte foi modificado."
echo "Backup preservado em: $BACKUP"
echo "=========================================="
