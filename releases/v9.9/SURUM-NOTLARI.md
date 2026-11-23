# UMAY Multi TV — v9.9 (versionCode 18)

23 Kasım 2026 · `CanliTV-9.9.apk` · `CanliTV-9.9.aab`

## Düzeltmeler

### ⭐ "ATV Alanya" ana ATV'nin önüne geçiyordu
- Sıralama puanlaması parantezli adı (asıl kanal: `ATV (Turkiye)`) yerel yayınla (`ATV Alanya`) eşit sayıyordu.
- Yeni puanlama: **tam ad (0) → parantezli ad (0) → "ATV Haber" gibi ekli ad (1) → yerel (2)**.
- Sonuç (cihazda doğrulandı): **1 TRT 1 · 2 ATV · 3 Star TV · 4 TV8 · 5 Kanal D · 6 Haber Global · 7 NTV · 8 TRT Spor Yıldız**.

### 🌍 İlk açılışta "ülkeni bulduk" önerisi (iş listesi #38)
- Uygulama ilk açılışta IP'den ülke tahmini yapar (`ipapi.co`).
- Ülke destekleniyorsa ve mevcut seçimden farklıysa **bir kez** sorar:
  "Ülkeni bulduk: Almanya — Kanalları ve radyoları bu ülkeye göre yükleyeyim mi?"
- "Evet" → ülke değişir, liste yeniden yüklenir. "Şimdi değil" → bir daha sorulmaz.
- IP'si Türkiye olan kullanıcıya soru çıkmaz (zaten TR seçili).

## Test
| Test | Sonuç |
|---|---|
Derleme (APK + AAB) | ✅ |
Kurulum + açılış | ✅ çökme yok |
TR sırası | ✅ ATV, ATV Alanya'nın önünde |
Banner | ✅ gerçek reklam (TERSİNE İSTANBUL) |
