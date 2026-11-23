# UMAY Multi TV — v9.12 (versionCode 21)

23 Kasım 2026 · `CanliTV-9.12.apk` · `CanliTV-9.12.aab`

## Değişiklikler

### 📢 Banner artık **her zaman** görünür
- Önceki sürümde 20 dk reklamsız süre aktifken banner da gizleniyordu.
- Artık **reklam gösterimi hiç kesilmiyor**: banner her ekranda, her zaman duruyor.
- 20 dakikalık ödül yalnızca **video (geçiş) reklamını** kaldırır — banner devam eder.

### ⏱ 20 dakikalık reklamsız süre **kanal başına**
- Ödül, izlediğin **kanala özel**: süre o kanal için geçerli, kanal değişince yeni kanalda
  video reklam yeniden devreye girer (istenirse yine ödüllü video izlenebilir).
- Aynı mantık radyo için de: ödül, o **istasyona** özel.
- Şerit durumu artık canlı bilgi veriyor:
  - Süre yoksa: **"▶ Reklam izle · 20 dk reklamsız"** (turuncu, tıklanabilir)
  - Süre varsa: **"✓ Bu kanalda reklamsız · 17 dk kaldı"** (yeşil, tıklanamaz)
- Hangi ekranda kanal bilgisi yoksa (ana sayfa, ayarlar) "en son izlenen kanal" esas alınır.

## Teknik
- `Prefs.adFreeKey` + `setAdFreeUntil(c, ms, key)`.
- `AdManager.isAdFreeFor(c, key)`, `maybeInterstitial(activity, key, then)`, `showRewarded(activity, key, …)`,
  `rewardStrip(activity, key)`, `wrap(activity, view, key)`.
- `PlayerActivity` ve `RadioPlayerActivity` kendi kanal/istasyon adresini geçiriyor.

## Test
| Test | Sonuç |
|---|---|
Derleme (APK + AAB) | ✅ |
Kurulum + ana sayfa | ✅ şerit + gerçek reklam birlikte görünüyor |
Kanal bazlı süre mantığı | ✅ kod yolu (ödüllü video izlenince o kanala yazılıyor)
