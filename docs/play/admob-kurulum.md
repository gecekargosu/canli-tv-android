# AdMob — yapılanlar ve kalanlar

Hesap: `cengizkilic1980@gmail.com` · **Hesap doğrulaması: ONAYLANDI** (23 Kasım 2026)

## 1. Konsol durumu (kontrol edildi)

| Ne | Değer |
|---|---|
| Hesap | ✅ "Hesap bilgileriniz doğrulandı ve reklam sunumunuz etkinleştirildi" |
| Uygulama | **UMAY Multi TV** (Android) · ID `5042287034` |
| Uygulama durumu | ⏳ **"İnceleme gerekli / sınırlı reklam sunumu"** — *"Limiti kaldırmak için mağaza ekleyin"* |
| Kurulum görevleri | **3/4** — kalan tek görev: *"Uygulama mağazasına bağlantı oluştur"* (Play yayını gerekir) |
| App ID | `ca-app-pub-7629051535337276~5042287034` |
| Banner | `ca-app-pub-7629051535337276/6107956695` |
| Geçiş | `ca-app-pub-7629051535337276/6126022588` |
| Ödüllü | `ca-app-pub-7629051535337276/4812940915` |
| app-ads.txt | ✅ `https://umay-multi-tv-gq3zer.freebuff.site/app-ads.txt` |

## 2. Kodda yapılanlar (v9.0)

- **Gradle'a geçildi:** `settings.gradle`, `build.gradle`, `app/build.gradle`, `gradle.properties`.
  `bash gradle-build.sh assembleDebug|assembleRelease|bundleRelease`.
- **SDK:** `implementation "com.google.android.gms:play-services-ads:24.4.0"`.
- **Manifest:** `com.google.android.gms.ads.APPLICATION_ID` meta-data'sı eklendi (SDK'sız çöker).
- **`Ads.java`:** kimlikler + `TEST_MODE = BuildConfig.DEBUG` (debug'da Google test birimleri).
- **`AdManager.java`:** `MobileAds.initialize`, liste altı banner, frekans sınırlı geçiş reklamı.
- **`MainActivity`:** banner listenin altına ekleniyor; kanal açılışı `AdManager.maybeInterstitial`
  üzerinden geçiyor (4. açılış + 10 dk kuralı).
- **İmzalama:** halka açık APK debug anahtarıyla (üzerine güncellenebilir), Play AAB upload
  anahtarıyla (`upload-keystore.jks`).

## 3. Doğrulanan testler (cihaz Z5R87DU8V4LBSOVG)

| Test | Sonuç |
|---|---|
| Test banner'ı (debug) | ✅ "Test Reklamı 468x60" liste altında |
| Geçiş reklamı (4. kanal) | ✅ gösterildi, kapanınca yeniden yüklendi |
| Gerçek banner (public APK) | ✅ gerçek reklam sunuldu |
| Çökme | ✅ yok |

## 4. Kalanlar

1. **Play'de yayın** → sonra AdMob → Uygulamalar → UMAY Multi TV → **Mağaza ekle** → Play linki.
   Bu yapılınca "sınırlı reklam sunumu" kalkar, ödeme eşiği (100 ₺) sonrası gelir işler.
2. **Ödüllü reklam arayüzü** ("60 dakika reklamsız") — birim hazır, kod v9.1'de bağlanacak.
3. Play Faturalandırma ile "Reklamları kaldır" (isteğe bağlı, sonraki sürüm).

## 5. Reklam yerleşimi (politika güvenli)

- **Banner:** yalnızca kanal listesinde, listenin altında. Video oynarken / tam ekranda **asla**.
- **Geçiş:** 4. kanal değişiminde bir, **10 dakikada en fazla 1**; yayın kesilerek gösterilmez.
- **Açılış reklamı:** kapalı.
- **Test cihazı:** debug'da Google test birimleri; logcat'te çıkan hashed kimlik
  (`3AF293B6...`) gerekirse `Ads.TEST_DEVICE` yerine yazılabilir.
- Kendi gerçek reklamına tıklamak hesabı kapatır — testleri **her zaman** debug derlemede yap.
