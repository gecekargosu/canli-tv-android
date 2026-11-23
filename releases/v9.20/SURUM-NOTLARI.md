# UMAY Multi TV — v9.20 (versionCode 29)

23 Kasım 2026 · `CanliTV-9.20.apk` · `CanliTV-9.20.aab`

## P0-3 — AB/UK rıza ekranı (UMP) eklendi

- `com.google.android.ump:user-messaging-platform` bağımlılığı eklendi.
- `AdManager.init` artık sırayla: `requestConsentInfoUpdate` → gerekirse `loadAndShowConsentFormIfRequired` →
  **`canRequestAds()` true değilse reklam hiç istenmiyor** → sonra `MobileAds.initialize`.
- Rıza bilgisi alınamazsa (ağ hatası) uygulama çalışmaya devam eder, SDK başlatılır (eski davranış korunur).
- Ayarlar için hazır: `AdManager.hasPrivacyOptions()` / `showPrivacyOptions()` (satır bir sonraki sürümde ekleniyor).
- Test modunda (`Ads.TEST_MODE`) rıza formunu görebilmek için EEA debug geography ayarlı; üretimde kapalı.

## P0-1 devamı — kayıp popüler kanallar geri geldi
- **ATV (Turkiye)** listeye ve en üste geri eklendi (kaynak canlı doğrulandı, cihazda oynadı).
- **TRT Çocuk** ve **TRT Müzik** eklendi (daha önce hiç yoktu; TRT'nin kendi resmi akışı).
- TR: **157 kanal**, iyi-liste **148**.

## Cihazda doğrulama
| Test | Sonuç |
|---|---|
| Liste | ✅ 157 kanal, **1. sıra ATV** |
| ATV oynatma | ✅ yayın göründü (1/157) |
| Banner reklam | ✅ gerçek reklam geldi (rıza akışı reklamı bozmadı) |
| Rıza (UMP) | ✅ test modunda EEA formu tetikleniyor, canRequestAds akışı çalışıyor |
