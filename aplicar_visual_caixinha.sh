#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

cd "$HOME/da_nike_ai"

XML="app/src/main/res/layout/dialog_atualizacao_premium.xml"
JAVA="app/src/main/java/com/danike/ai/AtualizacaoTelefoneDialog.java"
LOGO="app/src/main/res/drawable/danike_icon.png"
STAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP="$HOME/backup_visual_caixinha_$STAMP"

echo "===== VALIDANDO PROJETO ====="
for f in "$XML" "$JAVA" "$LOGO"; do
  if [ ! -f "$f" ]; then
    echo "ERRO: arquivo nao encontrado: $f"
    exit 1
  fi
done

mkdir -p "$BACKUP"
cp -p "$XML" "$BACKUP/"
cp -p "$JAVA" "$BACKUP/"

echo "Backup criado: $BACKUP"
echo "===== APLICANDO VISUAL PREMIUM ====="

python3 - "$XML" <<'PY'
import pathlib
import sys
import xml.etree.ElementTree as ET

p = pathlib.Path(sys.argv[1])

novo = '''<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#E6000000">

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="20dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="center_horizontal"
                android:background="@drawable/bg_premium_dialog"
                android:padding="24dp">

                <ImageView
                    android:id="@+id/imgLogoAtualizacao"
                    android:layout_width="76dp"
                    android:layout_height="76dp"
                    android:src="@drawable/danike_icon"
                    android:contentDescription="Logo DaNikeAI"
                    android:scaleType="fitCenter"
                    android:adjustViewBounds="true" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="DaNikeAI"
                    android:textSize="25sp"
                    android:textStyle="bold"
                    android:textColor="#FFFFFF"
                    android:layout_marginTop="10dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="ATUALIZAÇÃO DISPONÍVEL"
                    android:textColor="#00D4FF"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:gravity="center"
                    android:layout_marginTop="12dp" />

                <TextView
                    android:id="@+id/txtVersao"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="v1.8.6 • NOVO"
                    android:textColor="#FFFFFF"
                    android:background="@drawable/bg_badge"
                    android:paddingHorizontal="14dp"
                    android:paddingVertical="7dp"
                    android:layout_marginTop="12dp" />

                <TextView
                    android:id="@+id/txtChangelog"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Uma nova versão do DaNikeAI está disponível."
                    android:textColor="#D0D9E5"
                    android:textSize="15sp"
                    android:gravity="center"
                    android:layout_marginTop="20dp"
                    android:lineSpacingExtra="5dp" />

                <Button
                    android:id="@+id/btnAtualizar"
                    android:layout_width="match_parent"
                    android:layout_height="56dp"
                    android:text="BAIXAR ATUALIZAÇÃO"
                    android:textStyle="bold"
                    android:textColor="#FFFFFF"
                    android:backgroundTint="#087FE8"
                    android:layout_marginTop="24dp" />

                <Button
                    android:id="@+id/btnDepois"
                    android:layout_width="match_parent"
                    android:layout_height="48dp"
                    android:text="Lembrar depois"
                    android:textColor="#D0D9E5"
                    android:backgroundTint="#303744"
                    android:layout_marginTop="10dp" />
            </LinearLayout>

            <ProgressBar
                android:id="@+id/progressCirculo"
                android:layout_width="64dp"
                android:layout_height="64dp"
                android:layout_gravity="center"
                android:layout_marginTop="24dp"
                android:indeterminateTint="#00D4FF"
                android:visibility="gone" />

            <ProgressBar
                android:id="@+id/progressBar"
                style="?android:attr/progressBarStyleHorizontal"
                android:layout_width="match_parent"
                android:layout_height="8dp"
                android:layout_marginTop="16dp"
                android:max="100"
                android:progressTint="#00D4FF"
                android:progressBackgroundTint="#354052"
                android:visibility="gone" />

            <TextView
                android:id="@+id/txtProgressInfo"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Preparando atualização..."
                android:textColor="#D0D9E5"
                android:gravity="center"
                android:layout_gravity="center"
                android:layout_marginTop="10dp"
                android:visibility="gone" />
        </LinearLayout>
    </ScrollView>
</FrameLayout>
'''

# Confere a estrutura XML antes de salvar.
root = ET.fromstring(novo)
ns = "{http://schemas.android.com/apk/res/android}"
ids = {
    el.attrib.get(ns + "id", "").split("/")[-1]
    for el in root.iter()
}
obrigatorios = {
    "txtVersao", "txtChangelog", "btnAtualizar",
    "btnDepois", "progressBar", "progressCirculo",
    "txtProgressInfo"
}
if not obrigatorios.issubset(ids):
    raise SystemExit("ERRO: faltam IDs exigidos pelo Java. XML nao alterado.")

p.write_text(novo, encoding="utf-8")
ET.parse(p)
print("XML atualizado e validado.")
print("IDs usados pelo Java foram preservados.")
PY

echo
echo "===== CONFERINDO ALTERACOES ====="
git diff --check -- "$XML" "$JAVA"
git diff -- "$XML"

echo
echo "Visual aplicado. Nenhum APK foi instalado ou publicado."
echo "Backup: $BACKUP"
