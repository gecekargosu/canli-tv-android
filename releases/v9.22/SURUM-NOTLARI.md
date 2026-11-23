# UMAY Multi TV — v9.22 (versionCode 31)

23 Kasım 2026 · `CanliTV-9.22.apk` · `CanliTV-9.22.aab`

## P0-6 — Android 15/16 edge-to-edge: üst inset düzeltildi
- `AdManager.wrap` yalnızca **alt** inset uyguluyordu; Android 15/16'da zorunlu edge-to-edge ile
  başlık/status bar çentiğin altında kalabiliyordu.
- Artık `top + bottom` inset birlikte uygulanıyor (`setPadding(0, top, 0, bottom)`).
- Aynı düzeltme `PlayerActivity`'nin kendi kabında da yapıldı.

## P0-8 — Mağaza kaydı düzeltildi
| Sorun | Düzeltme |
|---|---|
| Başlık **32 karakter** (Play sınırı 30) | **`UMAY Multi TV: Canlı TV Radyo`** = **29 karakter** |
| Açıklamada olmayan kanallar (Show TV, Now TV) | çıkarıldı |
| "305 kanal / 50 radyo" gerçek dışı | **157 kanal / 25 radyo** (kapalı test + durum belgeleri dahil) |
| "60 dakika reklamsız" | **20 dakika** + "video reklamı kalkar, banner kalır" açıklaması |
- Güncellenen dosyalar: `docs/play/magaza-kaydi.md`, `docs/play/kapali-test.md`, `docs/play/durum.md`.

## ATV düzeltmesi (senin bildirdiğin)
- ATV'nin **1080p** kaynağı kararsızdı (3 denemede 1 yanıt). Aynı CDN'in **720p** kaynağı 3/3 çalışıyor.
- ATV kaynağı `atv_1080p` → **`atv_720p`** olarak değiştirildi (hem `channels.m3u` hem `good_tr.json`).

## Cihazda doğrulama
| Test | Sonuç |
|---|---|
| Sürüm | ✅ `versionName=9.22`, versionCode 31 |
| ATV oynatma | ✅ yayın göründü (Kuruluş Osman), sayaç 1/157 |
| Liste | ✅ 157 kanal, ATV 1. sırada |
| İnsets | ✅ üst bar çentiğin altında değil (Android 13'te inset 0 → görünüm aynı) |
