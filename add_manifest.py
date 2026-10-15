import pathlib
manifest = pathlib.Path("app/src/main/AndroidManifest.xml")
s = manifest.read_text()
if "HavePlataformasActivity" not in s:
    s = s.replace('<activity android:name=".SalaHAVEActivity"', '        <activity android:name=".HavePlataformasActivity" />\n        <activity android:name=".SalaHAVEActivity"')
    manifest.write_text(s)
    print("Adicionado no Manifest")
else:
    print("Ja existe")
