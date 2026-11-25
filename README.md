# UMAY Multi TV (Android)

Ücretsiz, reklam destekli **canlı TV + radyo** uygulaması. Türkiye için 126 kanal ve 32 radyo
uygulamanın içinde hazır gelir; 50 ülkenin listesi istenirse sunucudan indirilir.
**Hesap, üyelik, kredi kartı yoktur**; hiçbir özellik için giriş istenmez.

- Android 8.0+ (minSdk 26), hedef SDK 36
- Son APK/AAB: **GitHub Releases → v9.73** (`CanliTV-9.73.apk`, `UMAY-MULTI-TV-9.73.aab`)
- Sürüm geçmişi: [`RELEASES.md`](RELEASES.md)

## Özellikler

- **Numaralı kanallar** – kumanda gibi: üstteki *No* kutusuna numarayı yaz → **Git** → açılır.
- **📻 Radyo** – 32 Türk internet radyosu; ekran kilitliyken de çalar (ön plan servisi + wake/Wi-Fi lock).
- **Liste yönetimi** – uzun bas → *Listeden gizle*; "🗑 Gizlenenler" sekmesinden geri ekle.
- **Liste düzenleme** – çoklu seçim, ▲▼ ile taşıma, numara değiştirme, silme; her sekmede ayrı ve kalıcı.
- **Sıralama** – çok izlenenler, hiç izlenmeyenler, A→Z, numaraya göre, varsayılana dön.
- **Kalite / veri kontrolü** – 360p, 480p, 720p, 1080p, en düşük ve otomatik; saatlik tahmini GB.
- **Ekonomi modu** – en düşük kalite + ekran kapanınca yalnız ses.
- **Tek ekran oynatıcı** – media3/ExoPlayer, saydam saat, ses çubuğu, önceki/sonraki, favori, kayıt, PiP.
- **Çoklu ekran** – 1–4 pencere; her pencerenin sesi ayrı, kayıtlı setler, aranabilir kanal seçici.
- **Kayıt** – yalnız kamu yayıncılarında (TRT, TBMM, Diyanet); `.ts` olarak `Movies/TV-Kayitlari`.
- **Uyku zamanlayıcısı**, **mobil veri uyarısı**, **veri sayacı + günlük limit**, **kanal sağlık taraması**.
- **🌍 Ülke seçimi (50 ülke)** – iptv-org ülke listesi indirilir, `files/country_<cc>.m3u` olarak saklanır.
- **Arayüz dilleri** – Türkçe, English, Русский, العربية.
- **🐞 Hata günlüğü** – açılmayan kanal, oynatıcı hata kodu, bağlantı kopması, reklam dolmama nedeni
  zaman damgasıyla cihazda tutulur; Ayarlar → *Hata günlüğü*'nden okunur, e-postayla gönderilir veya
  istenirse günde bir kez sunucuya gönderilir (varsayılan **kapalı**).

## Reklamlar (AdMob) ve gelir

- Liste/oynatıcı altında **banner**; video oynarken ekranın üstüne reklam binmez.
- **Geçiş reklamı**: her 3. kanal değişiminde, en az **180 saniye** arayla; izlerken **15 dakikada bir**.
- **Ödüllü reklam**: kullanıcı isterse izler, **15 dakika** reklamsız (banner kalır).
- AB/İngiltere/İsviçre'de **UMP rıza ekranı**; rıza yoksa reklam isteği gönderilmez.
- Reklam yüklenemezse uygulama normal çalışır; yerine kendi "Reklam izle · 15 dk reklamsız" şeridi gelir.
- `app-ads.txt` yayında: <https://umay-multi-tv-gq3zer.freebuff.site/app-ads.txt>
- Gelir, AdMob konsolunda hesap genelinde 1–2 gün gecikmeyle kesinleşir; uygulama ayrıca bir
  "gelir bildirimi" yapmaz — gösterimi Google Ads SDK'sı kendisi raporlar.

## Gizlilik

- Hesap açılmaz; ad/e-posta/konum istenmez, izleme geçmişi sunucuya gönderilmez.
- Favoriler, ayarlar ve sayaçlar yalnız cihazda tutulur.
- Teknik hata günlüğü cihazda tutulur; **günlük gönderimi** yalnız kullanıcı açarsa günde bir kez
  çalışır ve yalnızca teknik kayıtları gönderir.
- Ayrıntı: <https://umay-multi-tv-gq3zer.freebuff.site/gizlilik-politikasi>

## Derleme

Gereken: Android SDK (platform 36, build-tools 36), JDK 21, Gradle 8.14 (araçlar `_build/tools` içinde).

```bash
bash build.sh        # → CanliTV.apk  (üretim imzası, üzerine kurulabilir)
bash make-aab.sh     # → CanliTV.aab  (Play için upload anahtarı)
bash gradle-build.sh assembleDebug   # test reklamlarıyla hızlı deneme
adb install -r CanliTV.apk
```

Her sürümde yayın öncesi zorunlu denetim:

```bash
python tools/verify-apk.py CanliTV.apk --prev releases/v<önceki>/CanliTV-<önceki>.apk --expect-version <yeni>
```

## Proje düzeni

```
src/com/cengiz/canlitv/
  MainActivity.java       liste, numara, filtreler, veri çubuğu, ayarlar, sağlık taraması
  PlayerActivity.java     media3 oynatıcı, saat, uyku, kalite, kayıt, PiP, yeniden bağlanma
  MultiActivity.java      1-4 pencere, pencere sesi, kayıt paneli, setler
  RadioActivity.java      radyo listesi
  RadioPlayerActivity.java / AudioService.java   radyo oynatıcı + arka plan servisi
  RecordingsActivity.java kayıt kütüphanesi (MediaStore)
  HlsRecorder.java        HLS segmentlerini .ts olarak Movies/TV-Kayitlari'na yazar
  Quality.java            master m3u8 varyantlarını (HD/SD) çözer
  Channel.java / ChannelRepo.java / Prefs.java / Net.java / LogoLoader.java
  Countries.java / CountryActivity.java / CountryRepo.java
  SettingsActivity.java   ayarlar; hata günlüğü; gizli geliştirici modu
  CrashLog.java           olay/çökme günlüğü + isteğe bağlı günlük gönderimi
  Ads.java / AdManager.java / AdStats.java / AdsTest.java
  AboutActivity.java / StatusActivity.java / Repair.java / Good.java / Popular.java
assets/channels.m3u       126 TR kanalı (tek tek test edilmiş)
assets/radios.m3u         32 TR radyosu
assets/good_tr.json       "bilinen iyi" TR sırası (çevrimdışı yedek)
site/                     Cloudflare Worker + statik site (gizlilik, indir, app-ads.txt)
```

## Kanal listesi

`assets/channels.m3u` içeriği [iptv-org](https://github.com/iptv-org/iptv) açık kaynaklı halka açık
listesinden üretilir; her URL test edilir (HTTP 200 + `#EXTM3U`). Kanal listesi dört dosyada birlikte
güncellenir: `assets/channels.m3u`, `assets/good_tr.json`, `site/public/good/tr.json`,
`site/public/kanallar.json`.

## Yasal

Yayınlar üçüncü tarafların halka açık akışlarıdır; uygulama yayın barındırmaz. Kaldırma talebi:
`cengizkilic1980@gmail.com` (24–48 saat içinde değerlendirilir).
