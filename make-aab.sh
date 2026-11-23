#!/bin/bash
# CanliTV.aab - Play Store icin Android App Bundle (Gradle + upload anahtari).
set -e
cd "$(dirname "$0")"

bash gradle-build.sh bundleRelease

SRC="app/build/outputs/bundle/release/app-release.aab"
[ -f "$SRC" ] || { echo "HATA: $SRC bulunamadi"; exit 1; }
cp "$SRC" CanliTV.aab

echo "HAZIR: $(pwd)/CanliTV.aab  ($(stat -c%s CanliTV.aab) bayt)"
