# UMAY Multi TV — v9.16 (versionCode 25)

23 Kasım 2026 · `CanliTV-9.16.apk` · `CanliTV-9.16.aab`

## Yenilikler

### 📻 Radyo listesi artık popülerliğe göre sıralı (iş listesi #40)
- Dinlenme listesi açıldığında **en çok bilinen istasyonlar başta**:
  GENÇ KRAL FM · Süper FM · Metro FM · JOY TÜRK FM · NTV Radyo · TRT FM · Radyo D · Alem FM · Power FM …
- **Aynı marka bir kez** listelenir (TV'deki kural): "Süper FM / SUPER FM 2 / Süper FM 99.9" yerine tek satır.
- Türkçe/ASCII farkı eşitlenir ("Süper" = "SUPER"), böylece kopya satır oluşmaz.
- Kaynak: `Popular.radioRank()` / `radioBrand()` + `RadioRepo.sortPopular()`.

### 🧹 Radyo listesi sayısı
- 32 → **25** istasyon (kopyalar birleşti, ölü olanlar elendi).

## Test (cihazda)
| Test | Sonuç |
|---|---|
| Derleme (APK + AAB) | ✅ |
| Radyo listesi ilk 7 satır | ✅ GENÇ KRAL FM → TRT FM |
| Kopya marka | ✅ tek satır |
| Banner + şerit | ✅ altta, çubuğun üstünde |
