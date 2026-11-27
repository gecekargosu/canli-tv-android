# UMAY Multi TV — v9.18 (versionCode 27)

23 Kasım 2026 · `CanliTV-9.18.apk` · `CanliTV-9.18.aab`

Claude'un v9.16 derin analizindeki bulgular tek tek kodda doğrulandı; doğru çıkanlar düzeltildi.

## Düzeltmeler

### 🔴 Bildirimdeki "Durdur" düğmesi çalışmıyordu (P0-5a)
- Bildirim `action="stop"` gönderiyordu, servis `getBooleanExtra("stop")` bekliyordu → hiç eşleşmiyordu.
- Artık `"stop".equals(action)` de kontrol ediliyor ve çalar durdurulup servis kapanıyor.

### 🔴 Radyo dinlerken ⏭ TV kanalına atlıyordu (P0-5b)
- `AudioService.step()` TV listesinde arıyor, radyo URL'sini bulamayınca indeks 0'a düşüyordu.
- Artık çalan yayın TV listesinde yoksa **hiçbir şey yapmıyor** (yanlış kanalın sesi basmıyor).

### 🔴 Kapat/geri sonrası ses arkada sürebiliyordu (P0-5c)
- `PlayerActivity.onPause` `isFinishing()` kontrolü olmadan arka plan servisini başlatıyordu.
- Artık `isFinishing()` ise servis başlatılmıyor.

### 🔴 Arama sonuçları klavye açıkken görünmüyordu (senin bildirdiğin)
- Sonuç gelince klavye otomatik kapanıyor, liste hemen ekranda görünüyor.

### 🔴 İmza şifresi repoda açık yazılıydı (P0-7)
- `app/build.gradle` içindeki `*** (keystore.properties) ***!` kaldırıldı.
- Şifre artık kökteki `keystore.properties` (git'e **girmez**, `.gitignore`'da) veya `UMAY_STOREPASSWORD`/`UMAY_KEYPASSWORD` ortam değişkeninden okunuyor.
- ⚠️ Şifre eskiden herkese açık geçmişte duruyor; Play App Signing açılınca **yeni anahtar** üretilecek.

### ⚖️ Gizlilik politikası gerçeğe uyduruldu (P0-2)
- Üç dosyada "bu sürümde reklam yoktur" yazıyordu (AdMob varken). Yeni metin: AdMob + reklam kimliği (AD_ID), ipapi.co, Cloudflare, üçüncü taraf yayın sunucuları, yerel favori/ayar.
- `site/public/gizlilik-politikasi.html`, `site/public/index.html`, `docs/play/gizlilik-politikasi.html`.

## Test
| Test | Sonuç |
|---|---|
| APK + AAB + release derlemesi | ✅ (imza `keystore.properties` ile) |
| Kurulum (versionCode 27) | ✅ |
| İş listesi | ✅ Claude maddeleri #78-98 olarak eklendi, durumlarıyla |
