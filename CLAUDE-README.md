# UMAY MULTI TV — Claude için proje paketi (v9.7, 23 Kasım 2026)

Bu zip, projenin **çalışan son sürümünün** kaynak kodunu, dokümanlarını, mağaza/reklam
hazırlığını ve sürüm notlarını içerir. Amaç: Claude'un projeyi tek bakışta anlayıp
devam edebilmesi.

## Ne yapar?
Android canlı TV + radyo uygulaması (`com.cengiz.canlitv`, versionName 9.7 / versionCode 16).
- 50 ülke için kanal listesi (iptv-org), ülkeye göre radyo (radio-browser.info)
- 4 dil arayüz: Türkçe / English / Русский / العربية
- Çoklu ekran (2/3/4), PiP, kayıt, uyku zamanlayıcısı, kalite (360p–1080p), veri göstergesi + günlük limit
- İsteğe bağlı üyelik (e-posta + kod doğrulama, Google OAuth hazır ama kimlik yok), misafir modu
- AdMob: banner (her ekranda), geçiş (4. zap / 10 dk), ödüllü (60 dk reklamsız)

## Klasör düzeni
```
src/com/cengiz/canlitv/      Java kaynakları (Activity'ler, repo'lar, reklam, ağ)
assets/                      channels.m3u (TR, 305 kanal), radios.m3u (TR)
res/                         ikonlar, tema
docs/                        is-listesi.md (ana iş listesi), play/ (mağaza, AdMob, üyelik)
docs/yayin-saglik-raporu.md  ülke yayınlarının canlı sağlık taraması (25 Kasım taraması)
releases/v9.7/               son APK + sürüm notları + ekran görüntüsü
site/                        Cloudflare Worker (üyelik API'si + /indir sayfası) + app-ads.txt
pazarlama/                   tanıtım metinleri, çekim planı, ham ekran kaydı (tanıtım videosu)
app/build.gradle             Gradle yapılandırması (AGP 8.13, targetSdk 36)
```

## Nasıl derlenir
```bash
cd CANLI-TV-APP
bash build.sh        # public APK (debug anahtarıyla imzalı → adb install -r çalışır)
bash make-aab.sh     # Play için AAB (upload anahtarı)
```
Gereksinim: JDK 21 + Android SDK 36. Gradle/AGP `_build/tools` içinde gömülü değildir;
`gradle-build.sh` sistemdeki Gradle'ı kullanır. Windows'ta yol Türkçe karakter içerdiği için
`gradle.properties` içinde `android.overridePathCheck=true` var.

## Mimari notları
- `ChannelRepo` ülke listesini yükler, `Popular.arrange` ile "önce tanınmış kanallar" sırasına dizer.
- `CountryRepo` iptv-org m3u dosyalarını indirip `files/country_<cc>.m3u` olarak önbelleğe alır.
- `RadioRepo` radio-browser API'sinden ülke radyolarını çeker, `files/radio_<cc>.m3u`.
- `AdManager` tek yerden reklam yönetimi; `wrap(activity, view)` her ekrana **veri satırı + banner**
  ekler (banner sistem gezinme çubuğunun üstünde kalır).
- `PlayerActivity`: VideoView + MediaPlayer, 13 sn watchdog, 2 hatada "ölü" işaretleme ve
  otomatik sonraki kanal (en fazla 3 kez).
- `Prefs`: tüm ayarlar + günlük veri sayacı + hata sayacı (`fails`).
- Üyelik API'si `site/worker.js` (Cloudflare Worker + D1): `/api/register|verify|login|resend|health|google`.

## Şu anki durum / bilinen sınırlar
- Play Store'da yayın **yok**: geliştirici kimliği (adres belgesi) onayı bekleniyor.
- Google ile giriş kapalı (`GOOGLE_CLIENT_ID` / `SECRET` yok).
- ABD gibi ülkelerde kanalların bir kısmı coğrafi kilitli; sağlık raporunda oranlar var.
- Widget, Chromecast, EPG, otomatik ülke (IP), ExoPlayer geçişi henüz yapılmadı → `docs/is-listesi.md`.
