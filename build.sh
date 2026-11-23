#!/bin/bash
# Canli TV - APK derleme (Gradle + AdMob SDK).
#   bash build.sh  -> CanliTV.apk  (debug anahtariyla imzali, uzerine guncellenebilir)
# Play icin AAB: bash make-aab.sh
# Eski aapt2/javac/d8 yolu kaldirildi: reklam SDK'si AAR oldugu icin Gradle gerekiyor.
set -e
cd "$(dirname "$0")"

bash gradle-build.sh assemblePublicApk

SRC="app/build/outputs/apk/publicApk/app-publicApk.apk"
[ -f "$SRC" ] || { echo "HATA: $SRC bulunamadi"; exit 1; }
cp "$SRC" CanliTV.apk
rm -f CanliTV.apk.idsig

BT="C:/Users/isitm/AppData/Local/Android/Sdk/build-tools/36.0.0"
"$BT/apksigner.bat" verify --print-certs CanliTV.apk | head -3

echo ""
echo "HAZIR: $(pwd)/CanliTV.apk  ($(stat -c%s CanliTV.apk) bayt)"
