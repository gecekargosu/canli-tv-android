# UMAY Multi TV v9.33 (versionCode 42) — Faz 0: güvenlik

## M-03 — Tek reklam izin kapısı (UMP)
- Banner artık rıza kapısından geçiyor: `canRequestAds()` false ise **hiçbir reklam isteği gönderilmiyor**.
- Önceden banner, UMP güncellemesinden önce yükleniyordu; ayrıca `startAds` istisna durumunda
  izni "var" sayıyordu (fail-open). Artık **fail-closed**: rıza okunamazsa reklam istenmez.
- Rıza sonradan gelirse ana ekrandaki banner kendiliğinden yükleniyor (bekleyen banner kuyruğu).

## M-04 — Gizlilik politikası gerçeğe göre yeniden yazıldı
- "Reklam yok / reklam kimliği yok / sunucuya veri gitmez" ifadeleri kaldırıldı.
- AdMob + AD_ID, Cloudflare, üçüncü taraf yayıncı IP'leri, UMP rızası ve "Ayarlar → Reklam gizlilik tercihleri" yazıldı.
- Hesap/üyelik/Resend bölümleri silindi; yaş beyanı tek: 13+.
- Üç kopya (site, docs/play, index) tek kaynaktan güncellendi; sürüm 2.0, tarih 24 Kasım 2026.

## M-05 — Ülke tespiti artık ağ isteği yapmıyor
- `ipapi.co` çağrısı tamamen kaldırıldı. Ülke, SIM/ağ ülke kodu ve cihaz dilinden okunuyor.
- Üçüncü tarafa IP gönderilmiyor; gizlilik metniyle tutarlı.

## M-06 — Tek imza
- `publicApk` artık **üretim (upload) anahtarıyla** imzalanıyor; Play AAB ile aynı kimlik.
- Not: v9.32 ve öncesi debug anahtarıyla imzalıydı. **Eski kurulum bir kez kaldırılıp yeniden kurulmalı.**

## M-08 — HTTPS zorunlu
- `usesCleartextTraffic=false` + `network_security_config`: yalnızca eski HTTP radyo/kanal
  alan adları için açık istisna var, diğer tüm trafik HTTPS.

## M-02 — Paket gizliliği
- `tools/claude-zip.sh`: `.secrets`, `*.key`, `*.jks`, `keystore.properties`, `konusmalar.txt`
  paket dışı; sızıntı varsa script hata verip durur.

## M-12 — Mağaza metni sayıları
- 157→145 kanal, 25/50→32 radyo, "20 dk reklamsız"→15 dk.

## Durum
- Derleme başarılı. **Cihaz kurulumu MIUI "USB ile yükleme" engeli nedeniyle yapılamadı**;
  bu yüzden banner/rıza davranışı cihazda doğrulanmadı (kod değişikliği derleniyor).
