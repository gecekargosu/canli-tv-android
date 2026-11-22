# UMAY Multi TV — v7.0 (versionCode 7)

Yayın tarihi: 23 Kasım 2026
Dosyalar: `CanliTV-7.0.apk` (yükleme için), `CanliTV-7.0.aab` (Play Console için)

## Yenilikler

### 🌍 Ülke seçimi (50 ülke)
- Üst çubuktaki **bayrak** düğmesi → *Ülke seç* ekranı.
- Seçilen ülkenin kanalları [iptv-org](https://github.com/iptv-org/iptv) ülke listesinden indirilir ve
  `files/country_<cc>.m3u` olarak önbelleğe alınır. Liste o ülkeye göre yeniden kurulur.
- **Türkiye gömülü** (assets/channels.m3u) — internet gerekmez, 305 kanal.
- Diğer ülkeler bir kez indirilir, sonra çevrimdışı açılır; listede uzun bas → *yeniden indir*.
- Yeni ekran: `CountryActivity` — "hazır" / "indir" durumu, ilk 50 ülke.

### 🗣 Çoklu dil (tr / en / ru / ar)
- Ayarlar → **Genel → Dil**: 🇹🇷 Türkçe, 🇬🇧 English, 🇷🇺 Русский, 🇸🇦 العربية.
- Başlık, alt başlık, sekmeler, Düzenle/Sırala, ayarlar satırları seçilen dilde gösterilir.
- Kanal adları ve kategorileri yayıncıdan geldiği gibi kalır.
- Dil değişince ana ekran kendini yeniden kurar (`recreate()`).

### Düzeltmeler
- Ülke düğmesi listenin üstüne taşındı; liste artık tam yükseklikte (önceki denemede araç çubuğu
  tüm alanı kaplıyor ve kanal listesi boş görünüyordu).
- Başlık/alt başlık tek satır + kısaltma (`UMAY Multi TV` her zaman okunur).
- `READ_MEDIA_VIDEO` izni kaldırıldı: kayıtlar uygulamanın kendi MediaStore öğeleri olduğu için
  Android 10+ üzerinde izin gerekmiyor (Play "medya izinleri" beyanı riski ortadan kalktı).

## Test durumu (cihaz: Z5R87DU8V4LBSOVG)
- Kurulum: `adb install -r CanliTV-7.0.apk` → **Success**.
- Açılış: çökme kaydı yok (`adb logcat -b crash` boş), liste 305 kanal ile doluyor.
- Ülke değişimi: 🇹🇷 Türkiye → 🇩🇪 Almanya → 303 kanal indi ve listeye geldi → geri 🇹🇷 305 kanal.

## Bilinen eksikler
- Ülke listesi 50 ülke ile sınırlı (iptv-org'ta ~150 ülke var); istenirse genişletilir.
- Arapça/Rusça çeviriler ana ekran + ayarlar ile sınırlı; oynatıcı içi metinler Türkçe.
- Reklam (AdMob) ve Google ile giriş hâlâ yok (Gradle ister, ertelendi).
