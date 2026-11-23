# UMAY Multi TV — v9.2 (versionCode 11)

Tarih: 23 Kasım 2026 · `CanliTV-9.2.apk` (5,1 MB) · `CanliTV-9.2.aab` (4,9 MB)

## Yenilikler

### 📻 Radyo artık TV gibi
- **Tür sınıfları:** Tümü · ★ Favoriler · 📰 Haber · ⚽ Spor · 🎵 Müzik · 🕌 Dini · 🗑 Gizlenenler.
- **Favoriler:** radyoya uzun bas → ★ Favorilere ekle; satırda ★ görünür.
- **Kapat:** çalan radyoyu durdurur, ana sayfaya döner.
- **⏻ Çık:** radyoyu durdurur ve uygulamadan tamamen çıkar.
- Radyo çalar ekranında **🏠 ana sayfa** (çalmaya devam eder) ve **⏻ tam çıkış**.

### 🎬 Zorlamayan reklam teklifi
- Radyoda **40 dakika** dinledikten sonra tek seferlik nazik teklif:
  *"Reklamsız devam? Kısa video izle, 60 dakika reklam görme."*
  "Şimdi değil" dersen bir daha sormaz, yayın kesilmez.

### 🐞 Bulunan ve düzeltilen hatalar (tam senaryo testi)
| Hata | Durum |
|---|---|
| Radyo listesi ve radyo çalar ekranı **durum çubuğunun altına giriyordu** (başlık/Düğmeler kesiliyordu) | ✅ `fitsSystemWindows` eklendi |
| Radyo kadranı beyaz bloklar olarak çiziliyordu (okunmuyor) | ✅ koyu kadran + turuncu ibre ile yeniden çizildi |
| Radyo menüsünde favori seçeneği yoktu | ✅ eklendi |
| Radyo kapatınca ses devam ediyordu (bazı akışlarda) | ✅ kapat/çık artık `AudioService.stop` çağırıyor |

### 📥 Site
- `https://umay-multi-tv-gq3zer.freebuff.site/indir` → v9.2 APK'ya bağlandı.

## Test raporu (cihaz Z5R87DU8V4LBSOVG · Android 15)

| Senaryo | Sonuç |
|---|---|
| Soğuk açılış, 305 kanal listesi | ✅ |
| Arama ("trt") | ✅ 4 sonuç (TRT 1, TRT 2, TRT Spor Yıldız, TRT Belgesel) |
| Filtre çipleri (Tümü / Favoriler / Türkçe / Yabancı / Son) | ✅ |
| Kanal açma (TV8) → canlı yayın | ✅ |
| 3'lü çoklu ekran | ✅ |
| Radyo listesi + yeni tür çipleri | ✅ |
| Radyo favori ekle/çıkar | ✅ |
| Radyo çalma (GENÇ KRAL FM) | ✅ |
| Radyoda Kapat → ana sayfa | ✅ | 
| Radyo çalar ekranı görselleri | ✅ (kadran düzeldi) |
| Reklamlar: liste banner'ı, oynatıcı altı banner, geçiş, ödüllü | ✅ v9.1'de doğrulandı, v9.2'de bozulmadı |
| Çökme (`logcat -b crash`) | ✅ uygulama için 0 kayıt (tek kayıt `uiautomator` aracının kendi hatası) |
| Sürüm | ✅ versionCode 11 / 9.2 |

## Bilinen eksikler / sırada
- Play yayını: geliştirici kimlik onayı bekliyor (uygulama henüz Play'de değil).
- E-posta gönderimi: `RESEND_API_KEY` / `MAIL_FROM` yok → mail kapalı.
- Google girişi: `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` yok.
- Zayıf internette liste/oynatma yavaş açılabilir (yayın kaynağına bağlı).
