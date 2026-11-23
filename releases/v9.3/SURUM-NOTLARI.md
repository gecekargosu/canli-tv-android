# UMAY Multi TV — v9.3 (versionCode 12)

Tarih: 23 Kasım 2026 · `CanliTV-9.3.apk` · `CanliTV-9.3.aab`

## Yenilikler

### 🌍 Radyo artık ülkeye göre (TV gibi)
- Ülke değişince **radyo listesi de o ülkeye döner**: Almanya → SWR3, Deutschlandfunk,
  1LIVE, Bayern 3… (80 istasyon), Rusya → Rus istasyonları, Hindistan/Brezilya/Meksika…
- Kaynak: `radio-browser.info` (ücretsiz, API anahtarsız), ülke başına **bir kez** indirilir
  ve `files/radio_<cc>.m3u` olarak önbelleğe alınır → sonra internet gerekmez.
- Türkiye listesi **uygulamanın içinde** (internet gerekmez).
- İndirme sürerken "Bu ülkenin radyoları indiriliyor…" bilgisi gösterilir; bitince liste yenilenir.

### 🐞 Hata kaydı (crash log)
- Uygulama çökerse cihaza `crash.txt` yazılır (sürüm, cihaz, Android sürümü, yığın izi).
- **Ayarlar → 🐞 Hata bildir** → kaydı WhatsApp/mail ile gönder (kopyala-yapıştır metin).
- Kayıt yoksa "Kayıtlı hata yok" der.

### ⏱ Radyoda uyku zamanlayıcı
- Radyo çalar ekranında **⏱** → 15 / 30 / 60 / 90 dakika → süre dolunca radyo durur
  (gece uyurken açık kalmasın).

### 📧 Üyelik
- Sunucuya **RESEND_API_KEY** ve **MAIL_FROM** eklendi → `/api/health` yanıtı
  **`mail: true`**. Doğrulama kodu ve hoş geldin maili artık gönderilebiliyor
  (gönderen: `onboarding@resend.dev`; şimdilik yalnızca hesap sahibinin adresine —
  tüm kullanıcılara mail için kendi domaini gerekli).

### 🎥 Pazarlama
- `pazarlama/ham-kayit.mp4`: telefonda çekilen **75 saniyelik gerçek ekran kaydı**
  (liste → arama → TRT 1 canlı → 3'lü ekran → radyo → kapatma).

## Bu sürümde doğrulananlar

| Test | Sonuç |
|---|---|
Ülke değiştir → Almanya | ✅ kanal listesi Almanca kanallara döndü (1-2-3.tv, 3sat, 90210…) |
Almanya radyo listesi | ✅ **80 istasyon** indirildi ve listelendi (SWR3, DLF, 1LIVE, Bayern 3…) |
TR radyo listesi (gömülü) | ✅ 32 istasyon |
Radyo çalma | ✅ |
Radyoda Kapat / ⏱ / ⏻ | ✅ |
Ekran boyutu 720×1280 @ 320 dpi (küçük telefon simülasyonu) | ✅ taşma yok, ölçeklendi |
Mail sunucusu | ✅ `{"mail":true}` |
APK/AAB derleme + kurulum | ✅ |

## Yapılmadıklarım (dürüst)
- **EPG (program rehberi):** güvenilir, ücretsiz ve sürekli bir XMLTV kaynağı olmadan
  "şu an ne var" bilgisi yanlış çıkar; yanlış bilgi göstermektense yapmadım.
- **Widget ve Chromecast:** ikisi de ayrı yayın/kütüphane gerektiriyor (widget için
  `AppWidgetProvider` + kanal seçici; Chromecast için Google Cast SDK). Sıradaki sürüme
  planlanabilir — bu sürümde yapılmadı.
- Play yayını: geliştirici kimlik doğrulaması hâlâ Google'da.
- Google ile giriş: `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` yok.
