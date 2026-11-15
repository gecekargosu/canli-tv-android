#!/bin/bash
# Canli TV - APK derleme (gradle yok: aapt2 + javac + d8 + zipalign + apksigner)
set -e
cd "$(dirname "$0")"

SDK="C:/Users/isitm/AppData/Local/Android/Sdk"
BT="$SDK/build-tools/36.0.0"
PLATFORM="$SDK/platforms/android-34/android.jar"
OUT="build"
rm -rf "$OUT"
mkdir -p "$OUT/classes" "$OUT/dex" "$OUT/gen"

echo "[1/7] kaynaklar derleniyor (aapt2 compile)"
"$BT/aapt2.exe" compile --dir res -o "$OUT/res.zip"

echo "[2/7] manifest + kaynaklar baglaniyor (aapt2 link)"
"$BT/aapt2.exe" link -o "$OUT/unsigned.apk" \
    -I "$PLATFORM" \
    --manifest AndroidManifest.xml \
    -A assets \
    -R "$OUT/res.zip" \
    --java "$OUT/gen" \
    --min-sdk-version 26 --target-sdk-version 34 \
    --auto-add-overlay

echo "[3/7] java derleniyor"
find src "$OUT/gen" -name "*.java" > "$OUT/sources.txt"
javac --release 11 -nowarn -classpath "$PLATFORM" -d "$OUT/classes" @"$OUT/sources.txt" > "$OUT/javac.log" 2>&1 || { grep -v "^Note:" "$OUT/javac.log"; echo "JAVA DERLEME HATASI"; exit 1; }

echo "[4/7] dex olusturuluyor (d8)"
find "$OUT/classes" -name "*.class" > "$OUT/classes.txt"
"$BT/d8.bat" --lib "$PLATFORM" --min-api 26 --output "$OUT/dex" @"$OUT/classes.txt" >/dev/null

echo "[5/7] dex apk icine ekleniyor"
cp "$OUT/unsigned.apk" "$OUT/withdex.apk"
python - <<PY
import zipfile
apk = r"$OUT/withdex.apk".replace('/', '\\\\')
z = zipfile.ZipFile(apk, 'a', zipfile.ZIP_DEFLATED)
z.write(r"$OUT/dex/classes.dex".replace('/', '\\\\'), 'classes.dex')
z.close()
PY

echo "[6/7] hizalama (zipalign)"
"$BT/zipalign.exe" -f 4 "$OUT/withdex.apk" "$OUT/aligned.apk"

echo "[7/7] imzalama (apksigner)"
if [ ! -f "debug.keystore" ]; then
  "C:/Program Files/Java/jdk-25/bin/keytool.exe" -genkeypair -keystore debug.keystore -alias androiddebugkey \
    -storepass android -keypass android -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US" >/dev/null 2>&1
fi
"$BT/apksigner.bat" sign --ks debug.keystore --ks-pass pass:android --key-pass pass:android \
    --out CanliTV.apk "$OUT/aligned.apk"
"$BT/apksigner.bat" verify --print-certs CanliTV.apk | head -3

echo ""
echo "HAZIR: $(pwd)/CanliTV.apk  ($(stat -c%s CanliTV.apk) bayt)"
