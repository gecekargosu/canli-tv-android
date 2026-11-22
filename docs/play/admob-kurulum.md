# AdMob — yapılanlar ve kalanlar

Hesap: `cengizkilic1980@gmail.com` · Durum: **hesap doğrulaması bekliyor** (Google: 24 saat – 2 hafta)

## 1. Konsolda yapıldı (23 Kasım 2026)

| Ne | Değer |
|---|---|
| Uygulama | **UMAY Multi TV** (Android) · ID `5042287034` · durum "İnceleme gerekli" |
| Uygulama kimliği (App ID) | `ca-app-pub-7629051535337276~5042287034` |
| Banner birimi | Liste alt banner · `ca-app-pub-7629051535337276/6107956695` |
| Geçiş birimi | Kanal geçiş reklamı · `ca-app-pub-7629051535337276/6126022588` |
| Ödüllü birim | Kesintisiz 60 dakika · `ca-app-pub-7629051535337276/4812940915` |

Bu kimlikler kodda `src/com/cengiz/canlitv/Ads.java` içinde duruyor.
AdMob'da uygulama **mağazada listeli değil** olarak eklendi; Play'de yayınlanana kadar reklam
sunumu sınırlı olur, yayından sonra AdMob'a Play bağlantısını eklemek gerekir.

## 2. Neden bu sürümde reklam görünmüyor

Google Mobile Ads SDK'sı `com.google.android.gms:play-services-ads` **AAR** paketidir; bu proje
Gradle kullanmadan (`aapt2 → javac → d8`) derleniyor ve AAR'ların manifest/kaynak birleştirmesi
yapılmıyor. SDK'yı elle eklemek 20+ geçişli AAR'ı çekmek demek — kırılgan. Bu yüzden reklam
**bir sonraki sürümde, Gradle geçişiyle** bağlanacak. `Ads.SDK_READY = false` bu yüzden var.

## 3. Gradle geçişi (reklamı açan adım) — sırayla

1. `settings.gradle` + `build.gradle` + `gradle/wrapper/gradle-wrapper.properties` (AGP 8.9+, Gradle 8.11+, JDK 17).
2. `app/build.gradle`:
   ```gradle
   android { namespace "com.cengiz.canlitv"; compileSdk 36; defaultConfig { applicationId "com.cengiz.canlitv"; minSdk 26; targetSdk 36 } }
   dependencies { implementation "com.google.android.gms:play-services-ads:24.4.0" }
   ```
3. `AndroidManifest.xml` içine (application altına):
   ```xml
   <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID"
              android:value="ca-app-pub-7629051535337276~5042287034"/>
   ```
4. `Ads.SDK_READY = true` yap ve `MobileAds.initialize(this, ...)` çağır.
5. **Test cihazı**: `RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("Z5R87DU8V4LBSOVG"))`
   — yayına çıkmadan önce kendi reklamlarına tıklamak hesabı kapatır.
6. Doğrulama: `adb logcat | grep -i "Ads"` → "Ad loaded" satırını gör; test reklamı açılıyorsa SDK hazır.
7. `sites_deploy` gerekmez; Play'e yeni AAB: `bash make-aab.sh`.

## 4. Reklam yerleşimi (politika güvenli)

- **Banner:** yalnızca kanal listesinde, listenin altında. **Video oynarken / yatay tam ekranda asla**
  (videonun üstüne binen banner = "yanıltıcı tıklama" ihlali).
- **Geçiş reklamı:** kanal değiştirirken, **4. zap'ta bir** ve **10 dakikada en fazla 1**.
  Yayın açılırken veya yayın kesilerek gösterilmez.
- **Ödüllü:** kullanıcı kendi ister → "60 dakika reklamsız izle". Zorunlu değil, yayın kesilmez.
- **Uygulama açılış reklamı:** kapalı.
- Reklamlar ilk gün **test reklamı** olarak açılır; AdMob "İnceleme gerekli" durumu kalkmadan
  gerçek reklam gelmez (bu normaldir, hata değil).

## 5. Ek gelir fikri (sonra)

Play Faturalandırma ile "Reklamları kaldır" tek seferlik ürün (ör. 49 TL). Kullanıcı hesabı zaten
var (`Account`), satın alma hesaba bağlanır. Bu da Gradle + Play Billing kütüphanesi ister.
