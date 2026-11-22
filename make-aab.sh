#!/bin/bash
# CanliTV.aab - Play Store icin Android App Bundle (gradle yok: aapt2 --proto-format + bundletool)
set -e
cd "$(dirname "$0")"

SDK="C:/Users/isitm/AppData/Local/Android/Sdk"
BT="$SDK/build-tools/36.0.0"
PLATFORM="$SDK/platforms/android-34/android.jar"
JAR="_build/bundletool.jar"
OUT="build_aab"
JB="C:/Program Files/Java/jdk-25/bin"
KEYTOOL="$JB/keytool.exe"
JARSIGNER="$JB/jarsigner.exe"
KS="upload-keystore.jks"
KSPASS="UmayMultiTV2026!"
ALIAS="upload"

[ -f "$JAR" ] || { echo "bundletool.jar yok: _build/bundletool.jar"; exit 1; }

echo "[1/6] APK derlemesi (classes.dex icin)"
bash build.sh >/dev/null

echo "[2/6] proto-format kaynaklar"
rm -rf "$OUT"; mkdir -p "$OUT"
"$BT/aapt2.exe" compile --dir res -o "$OUT/res.zip" >/dev/null
"$BT/aapt2.exe" link -o "$OUT/base.apk" \
    -I "$PLATFORM" \
    --manifest AndroidManifest.xml \
    -A assets \
    -R "$OUT/res.zip" \
    --java "$OUT/gen" \
    --min-sdk-version 26 --target-sdk-version 36 \
    --proto-format --auto-add-overlay >/dev/null

echo "[3/6] modul klasoru hazirlaniyor"
rm -rf "$OUT/base"; mkdir -p "$OUT/base"
(cd "$OUT/base" && unzip -q ../base.apk)
cp build/dex/classes.dex "$OUT/base/classes.dex"

echo "[4/6] base.zip (manifest/ dex/ res/ resources.pb assets/)"
rm -rf "$OUT/module"; mkdir -p "$OUT/module/manifest" "$OUT/module/dex"
mv "$OUT/base/AndroidManifest.xml" "$OUT/module/manifest/AndroidManifest.xml"
mv "$OUT/base/classes.dex" "$OUT/module/dex/classes.dex"
[ -f "$OUT/base/resources.pb" ] && mv "$OUT/base/resources.pb" "$OUT/module/resources.pb"
[ -d "$OUT/base/res" ] && mv "$OUT/base/res" "$OUT/module/res"
[ -d "$OUT/base/assets" ] && mv "$OUT/base/assets" "$OUT/module/assets"
python - <<PY
import zipfile, os
root = r"$OUT/module".replace('/', '\\\\')
out = r"$OUT/base.zip".replace('/', '\\\\')
with zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED) as z:
    for d, _, fs in os.walk(root):
        for f in fs:
            p = os.path.join(d, f)
            z.write(p, os.path.relpath(p, root).replace('\\\\', '/'))
print('  base.zip:', os.path.getsize(out), 'bayt')
PY

echo "[5/6] bundletool build-bundle"
if [ ! -f "$KS" ]; then
  echo "  yukleme anahtari olusturuluyor: $KS"
  "$KEYTOOL" -genkeypair -keystore "$KS" -alias "$ALIAS" \
    -storepass "$KSPASS" -keypass "$KSPASS" -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=UMAY Multi TV, O=Cengiz Kilic, C=TR" >/dev/null 2>&1
fi
java -jar "$JAR" build-bundle --modules="$OUT/base.zip" --output=CanliTV.aab --overwrite

echo "[6/6] imzalama (jarsigner)"
"$JARSIGNER" -keystore "$KS" -storepass "$KSPASS" -keypass "$KSPASS" CanliTV.aab "$ALIAS" >/dev/null
"$JARSIGNER" -verify CanliTV.aab | head -1

echo ""
java -jar "$JAR" validate --bundle=CanliTV.aab
echo "HAZIR: $(pwd)/CanliTV.aab  ($(stat -c%s CanliTV.aab) bayt)"
