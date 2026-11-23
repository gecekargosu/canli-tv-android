# UMAY Multi TV — v9.5 (versionCode 14)

Tarih: 23 Kasım 2026 · `CanliTV-9.5.apk` · `CanliTV-9.5.aab`

## Yeni

### 📶 Her ekranda veri göstergesi
- Tüm ekranların altında **"📶 Bugün kullanılan veri: 31,4 MB"** satırı var; 5 saniyede bir kendini güncelliyor.
- Ayarlar → **Veri göstergesi: Açık/Kapalı** ile kapatılabilir.

### 📢 Banner artık her sayfada
Önceden yalnızca kanal listesi ve oynatıcıdaydı. Şimdi: **radyo çalar, radyo listesi, ayarlar, hesap, ülke seç, kayıtlar, durum, çoklu ekran** — hepsinin en altında banner var (içeriğin/videonun üzerine binmez).
- Reklamsız süre (ödüllü 60 dk) aktifken banner gizlenir, veri satırı kalır.

### 🔊 Radyo çalar üst barı okunur hale geldi
- İkonlar artık yazılı ve daha büyük: **◀ Liste · 🏠 Ana · ⏻ Çık · ⏱ Uyku · ⏹ Durdur**.
- Sıkışan "RADYO" başlığı kaldırıldı (düğmeler okunaklı kalsın).

### 🛡 Gemini önerilerinden uygulananlar
- Aynı anda iki tam ekran reklam açılmasını engelleyen kilit (`showingAd`).
- Geçiş reklamı kapanınca **her durumda** istenen işlem çalışıyor (reklam gösterme başarısız olsa bile).

## Doğrulama (cihaz Z5R87DU8V4LBSOVG)
| Test | Sonuç |
|---|---|
Ülke 🇺🇸 ABD'ye geçiş, 1559 kanal indi | ✅ |
ABD radyosu: 80 istasyon, Classic Vinyl HD | ✅ çalıyor + ekolayzır |
Radyo çalar banner'ı | ✅ (test reklamı) |
Ülke seç / ayarlar / hesap ekranları banner + veri | ✅ |
Ana liste banner + veri satırı | ✅ |
Derleme + kurulum (üstüne) | ✅ |

## Bilinen sınırlar
- ABD kanallarının bir kısmı yayın açmıyor (kaynak "Geo-blocked" olarak işaretliyor) — kanal kalitesi kaynağa bağlı.
- Google ile giriş kapalı (OAuth kimliği yok).
- Widget / Chromecast / otomatik ülke: sıradaki sürümler.
