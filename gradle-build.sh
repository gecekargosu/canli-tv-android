#!/bin/bash
# Gradle derlemesi (AdMob SDK dahil). build.sh = reklamsiz/manuelsiz yedek yol.
#   bash gradle-build.sh assembleDebug      -> app/build/outputs/apk/debug/app-debug.apk
#   bash gradle-build.sh assembleRelease    -> imzali APK
#   bash gradle-build.sh bundleRelease      -> Play icin AAB
set -e
cd "$(dirname "$0")"

TOOLS="$PWD/_build/tools"
JDK=$(ls -d "$TOOLS"/jdk-21* 2>/dev/null | head -1)
GRADLE="$TOOLS/gradle-8.14.3/bin/gradle"

[ -n "$JDK" ] || { echo "HATA: JDK 21 yok ($TOOLS/jdk-21*)"; exit 1; }
[ -f "$GRADLE" ] || { echo "HATA: Gradle yok ($GRADLE)"; exit 1; }

# Gradle 8 manifest icinde "package" ozniteligini kabul etmiyor; kopyasini uret.
mkdir -p _build/gradle
sed 's/ *package="com\.cengiz\.canlitv"//' AndroidManifest.xml > _build/gradle/AndroidManifest.xml

printf 'sdk.dir=C:/Users/isitm/AppData/Local/Android/Sdk\n' > local.properties

export JAVA_HOME="$JDK"
export PATH="$JDK/bin:$PATH"
exec "$GRADLE" --no-daemon "$@"
