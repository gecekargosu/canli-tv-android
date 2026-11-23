# UMAY Multi TV — v9.17 (versionCode 26)

23 Kasım 2026 · `CanliTV-9.17.apk` · `CanliTV-9.17.aab`

## Yenilik: Listede olmayan radyoyu internette ara + kendi listene ekle

Arama çubuğuna yazdığın istasyon **listede yoksa**, uygulama artık kendiliğinden internette
(radio-browser.info) arıyor:

- 3+ harf yazınca ve yerel listede eşleşme yoksa arama otomatik başlar (tek sefer).
- Bulunanlar listede görünür: ör. `lofi` → **15 istasyon** (Lofi 24/7, 0R-LO-FI, MoE Lofi, france info (lofi)…).
- Bir sonuca **uzun bas** → **“➕ Kendi listeme ekle (kalıcı)”**.
  Eklenen istasyon **her açılışta listenin en üstünde** durur (`kendi listem · internet`).
  Aynı menüden **“🗑 Kendi listemden çıkar”** ile kaldırılır.
- Arama normalizesi düzeldi: `radyofenomen` yazınca **Radyo Fenomen** de bulunuyor (boşluk farkı).

### Teknik
- `RadioActivity.searchOnline()` — `de1.api.radio-browser.info/json/stations/search?name=…`,
  8 sn zaman aşımı, `hidebroken`, tıklanma sırası, en fazla 15 sonuç.
- `Prefs.customRadios/addCustomRadio/removeCustomRadio/hasCustomRadio` — kalıcı kendi liste.
- `RadioRepo.addCustoms()` — kendi listen en üste ekleniyor.
- `onlineUrls` kümesi — internet sonuçları isim filtresine takılmıyor.

## Test (cihazda, UMAY v9.17)
| Test | Sonuç |
|---|---|
| `lofi` araması | ✅ 15 sonuç listelendi |
| Uzun bas → kendi listeme ekle | ✅ |
| Uygulamayı kapat/aç | ✅ “Lofi 24/7 · kendi listem · internet” en üstte |
| `radyofenomen` (boşluksuz) | ✅ Radyo Fenomen eşleşti |
| Yerel liste (25 istasyon) | ✅ bozulmadı |
