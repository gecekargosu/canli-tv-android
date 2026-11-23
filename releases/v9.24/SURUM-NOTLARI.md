# UMAY Multi TV — v9.24 (versionCode 33)

23 Kasım 2026 · `CanliTV-9.24.apk` · `CanliTV-9.24.aab`

## #42 — Yedek kaynak (mirror) altyapısı
- `ChannelRepo.FALLBACK`: bir kanalın asıl adresi açılmazsa **sırayla yedek adresler** denenir.
- `PlayerActivity`: hata gelince önce **yedek kaynak** denenir ("Yedek kaynak deneniyor…"), hepsi biterse
  eski davranış (2 hata → sıradaki kanal) devreye girer. Yayın açılınca sayaç sıfırlanır.
- Şu an tanımlı: **ATV** → `atv.m3u8`, `atv_1080p` (720p asıl). Yeni kanallar tek satır eklenerek korunur.

## P0-2 — Yaş/kitle tutarlılığı düzeltildi
- Politika "18 yaş ve üzeri" diyordu, mağaza kaydı "13+"; artık **13+ genel kitle** (mağaza derecelendirmesiyle aynı).
- Aynı metin 3 dosyada: `site/public/gizlilik-politikasi.html`, `site/public/index.html`, `docs/play/gizlilik-politikasi.html`.

## P0-1 — durum (Claude maddeleri)
| Madde | Durum |
|---|---|
1) Resmî/ücretsiz kaynak + çıplak IP ve ücretli kanallar çıksın | ✅ (v9.19-20, 305→157) |
2) Sunucuda izin listesi, ham iptv-org gösterme | ⏳ (#99) |
3) Kullanıcı kendi M3U'sunu ekleyebilsin | ⏳ TV tarafı (#79) — radyoda var (#75) |
4) Telif bildirim e-postası + kaldırma süreci | ⏳ (#100) |
5) Yetişkin filtre + etiket temizleme | ✅ (CountryRepo.blocked/isBareIp/cleanName) |
6) Avukat görüşü | ⏳ senin işin (hukuki tavsiye değil) |

## Cihazda doğrulama
| Test | Sonuç |
|---|---|
| Sürüm | ✅ `9.24`, vCode 33 |
| ATV oynatma | ✅ yayın göründü (1/157) |
| Çökme | ✅ `logcat` FATAL yok |
| Yedek kaynak yolu | ⚠️ kod yolu derlendi; **hata simülasyonu ile denenmedi** |
