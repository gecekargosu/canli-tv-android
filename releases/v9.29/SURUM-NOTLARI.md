# UMAY Multi TV v9.29 (versionCode 38)

Tarih: 23 Kasım 2026

## Bu sürümde yeni

### 1. #107 — ExoPlayer (media3) geçişi: ana oynatıcı
- `PlayerActivity` artık **androidx.media3 ExoPlayer** kullanıyor (VideoView/MediaPlayer yerine).
- HLS master + 302 yönlendirme + tarayıcı UA'sı tek yerde; hata durumunda yedek kaynak zinciri korundu.
- PiP, uyku zamanlayıcısı, kumanda, donma denetimi ve "reklam kuşağı" bilgi şeridi aynen çalışıyor.
- Doğrulandı: ATV, Kral Pop TV (302 + jeton), 3sat (Almanya), Haber Global açıldı.
- **Kalan:** 3'lü ızgara (`MultiActivity`) ve radyo servisi hâlâ MediaPlayer — sıradaki adım (#107b).

### 2. #108 — Sunucu allowlist
- Ülke listeleri artık **yalnız kendi sunucumuzdan** (`/good/<cc>.json`) geliyor.
- `iptv-org.github.io` indirmesi tamamen kaldırıldı; uygulama ham liste kabul etmiyor.
- Almanya testi: sunucudan 67 kanal indi, 3sat oynadı.

### 3. #136 — Ekonomi modu (onaylı istek)
- **Ayarlar → Ekonomi modu**: her zaman en düşük çalışan varyant (240/360p).
- Ekran kapanınca video indirmeyi durdurup yalnız sese geçer (ses servisi), ekran açılınca devam eder.
- Veri tasarrufu ayarından farkı: kalite + kapakta ses birlikte.

### 4. Pazarlama görselleri (#140)
`pazarlama/` klasörüne eklendi: ExoPlayer ATV, Kral Pop, Almanya listesi, 3sat yayını,
radyo listesi, radyo çalar, Haber Global, ekonomi modu ekranı.

## Doğrulama
- 144 kanal (TR), 67 kanal (DE) listelendi; ATV / Kral Pop / 3sat / Haber Global oynatıldı.
- Ekonomi modu açıldı ("Açık"), uygulama çökmedi, medya3 derlemesi başarılı (APK 7,1 MB).

## Bilinen sınırlar
- Izgara + radyo servisi MediaPlayer'da (AGP 9 riski orada sürüyor → #107b).
- Test modu açıkken reklam geliri yazmaz (bilinçli).
