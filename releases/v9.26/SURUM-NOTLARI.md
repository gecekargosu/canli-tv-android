# UMAY Multi TV v9.26 (versionCode 35)

Tarih: 23 Kasım 2026

## Bu sürümde düzeltilenler

### 1. Kanal listesi temizliği: 157 → 144
Tüm kanallar tarandı; **çalışmayan 12 kanal** listeden çıkarıldı:

| Kanal | Neden |
|---|---|
| ATV Alanya | Posta TV ile aynı yayın |
| ETV Manisa | yayın yok |
| TGRT Belgesel | çalışan kaynak yok |
| TJK TV 2 | sadece YouTube'da |
| TRT 3 | master var, 360p/720p varyantları 404 |
| Rakuten TV Family Movies (4 ülke) | coğrafi kilit (TR'de açılmıyor) |
| HOT Drama | coğrafi kilit |
| .sci-fi | coğrafi kilit |
| 4ever Drama | coğrafi kilit |

- Ayrıca **TRT Muzik iki kez** ekliydi (biri cinerama yansısı); yinelenen kayıt silindi.
- **TRT Muzik** aslında çalışıyordu (medya.trt.com.tr master + 360p varyantı HTTP 200);
  önceki taramada "ölü" görünmesinin nedeni tarayıcının yalnızca son varyantı denemesiydi.

### 2. Kral Pop TV yeniden çalışıyor
- Ölü kaynak (`dogus-live.daioncdn.net/kralpoptv/playlist.m3u8`) →
  `https://dygvideo.dygdigital.com/live/hls/kralpop`.
- Bu CDN önce **jetonlu adrese 302** yönlendiriyor, Android MediaPlayer zinciri çözemiyordu.
- `Net.follow()` eklendi: oynatmadan önce 301/302 zinciri çözülüp **son adres** kullanılıyor.
  Kalite (360p/720p) seçimi de artık **son adrese göre** yapılıyor; yoksa varyantlar 404 oluyordu.
- Telefonda doğrulandı: Kral Pop TV açıldı (No 72/144).

### 3. Claude P0-5 (ses servisi) tamamlandı
`AudioService` ön planda çalan radyo/ses yayını için:
- **Ses odağı** (`AudioFocusRequest`): arama veya başka uygulama sesi gelince duraklıyor,
  odak geri gelince kaldığı yerden devam ediyor; "duck" isteğinde sesi %20'ye düşürüyor.
- **Kulaklık çıkarma** (`ACTION_AUDIO_BECOMING_NOISY`): yayın hoparlörden devam etmiyor, duraklıyor.
- **WakeLock süresi**: süresiz kilit yerine en fazla **3 saat**; servis kapanınca bırakılıyor.
- **https → http düşürmesi kaldırıldı**: takılan yayın aynı güvenli adresle bir kez daha deneniyor
  (şifresiz http'ye düşmek içerik bütünlüğü ve Play politikası açısından riskliydi).
- **`MultiActivity.onPause`**: `isFinishing()` kontrolü — geri tuşuyla çıkınca arka planda ses kalmıyor.
- **Bildirim izni**: Android 13+ için ızgara ve radyo ekranı açılışta `POST_NOTIFICATIONS` istiyor.

### 4. Manifest sertleştirme
- `android:allowBackup="false"` — ADB/cloud yedeğiyle uygulama verisi (tercihler, jetonlar) çalınmasın.
- `android:supportsRtl="true"` — Arapça arayüzde düzen bozulmasın.

## Doğrulama (telefon: Z5R87DU8V4LBSOVG)
- 144 kanal listelendi (başlıkta "144 kanal").
- TRT Muzik (No 113) açıldı — müzik yayını oynuyor.
- Kral Pop TV (No 72) açıldı.
- TRT 1 (No 2) açıldı — UEFA sponsor kuşağı oynuyor.
- Çökme yok (`logcat` temiz).

## Kalan işler (P0)
- #107 ExoPlayer/media3'e geçiş, #108 sunucu allowlist, #110 keystore şifre rotasyonu,
  #113 app-ads.txt kök alan adı, #114 Data Safety formu, #115 avukat, #116 Play yayını.
