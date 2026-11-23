# UMAY Multi TV — v9.0 (versionCode 9)

Tarih: 23 Kasım 2026 · `CanliTV-9.0.apk` (5,1 MB) · `CanliTV-9.0.aab` (4,9 MB)

## Yenilikler

### 📢 Reklamlar artık gerçekten çalışıyor (AdMob SDK bağlandı)
- Google Mobile Ads SDK (`play-services-ads 24.4.0`) eklendi; proje Gradle ile derleniyor.
- **Banner:** yalnızca kanal listesinin altında. Video oynarken ekranda reklam **yok**.
- **Geçiş reklamı:** her 4. kanal açılışında bir, iki reklam arası en az **10 dakika**.
  Yayın açılırken/kesilirken gösterilmez.
- **Test güvenliği:** debug derlemede Google'ın resmi test birimleri kullanılır
  (`Ads.TEST_MODE`), böylece kendi reklamına tıklayıp hesap kapatma riski yok.
- Reklam yüklenemezse uygulama eskisi gibi çalışmaya devam eder (hiçbir şey bloke olmaz).

### 🛠 Derleme düzeni
- `build.sh` → Gradle ile **halka açık APK** (debug anahtarı, üzerine güncellenebilir).
- `make-aab.sh` → Gradle ile **Play AAB** (upload anahtarı).
- Araçlar proje içinde: `_build/tools/gradle-8.14.3`, `_build/tools/jdk-21*`
  (git'e girmez; `gradle-build.sh` bunları kullanır).

### 📄 AdMob
- AdMob **hesap doğrulaması onaylandı** ("reklam sunumunuz etkinleştirildi").
- `app-ads.txt` yayında: `https://umay-multi-tv-gq3zer.freebuff.site/app-ads.txt`.
- Uygulama durumu "İnceleme gerekli / sınırlı reklam sunumu" — Play'de listelenip mağaza
  bağlantısı eklenince limit kalkar.

## Test edilenler (cihaz Z5R87DU8V4LBSOVG, Android 15)
| Senaryo | Sonuç |
|---|---|
| Gradle derlemesi (debug + release + AAB) | ✅ BUILD SUCCESSFUL |
| Kurulum (`adb install -r`, imza korunarak) | ✅ Success |
| Açılış, 305 kanal listesi | ✅ |
| **Test banner'ı** (liste altı) | ✅ "Test Reklamı 468x60" göründü |
| **Geçiş reklamı** (4. kanal açılışı) | ✅ `acilis #4 sira=true` → reklam gösterildi, kapanınca yeniden yüklendi |
| **Gerçek reklam** (public APK, release kimlikleri) | ✅ Gerçek banner sunuldu (TOD reklamı) |
| Canlı yayın (TV8) | ✅ oynatıcı + sayaç |
| Çökme kaydı (`logcat -b crash`) | ✅ boş |
| Sürüm bilgisi | ✅ versionCode 9 / versionName 9.0 |

## Bilinen eksikler
- **Ödüllü reklam** ("60 dakika reklamsız") birimi hazır ama arayüzü bağlanmadı (v9.1).
- **E-posta gönderimi / Google girişi** hâlâ kapalı: `RESEND_API_KEY`, `MAIL_FROM`,
  `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` secret'ları eklenmeli
  (`docs/play/uyelik-kurulum.md`).
- Play'de uygulama kaydı, geliştirici kimlik doğrulaması onayına bağlı (`docs/play/durum.md`).
