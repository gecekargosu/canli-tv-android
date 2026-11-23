# UMAY Multi TV — v9.19 (versionCode 28)

23 Kasım 2026 · `CanliTV-9.19.apk` · `CanliTV-9.19.aab`

## P0-1 — Telif riski temizliği (Claude raporu, en kritik madde)

Claude'un uyarısı doğruydu: gömülü listede ücretli kanalların izinsiz yeniden yayınları ve
çıplak IP kaynakları vardı. Temizlendi.

### Ne çıkarıldı
| Kural | Adet |
|---|---|
"Sinema" grubu (ücretli film kanalları, çoğu çıplak IP) | 137 |
Çıplak IP kaynaklı yayınlar (`http://88.212.x.x` gibi) | 9 |
`raw.githubusercontent.com` kaynaklı yayınlar | 2 |
Abonelik markası (Cinemax/AXN/BBC First/beIN/Digiturk…) | 1 |
**Sonuç: 305 → 156 kanal**, kalan çıplak IP **0**, şifresiz http **59 → 5**

### Yeni araç
- `_build/temizle.py` — kuru çalışma raporu + `--yaz` ile listeyi yeniden üretir.
- `_build/good.py tr 156` — temiz listenin tamamı canlı denenir: **146/156 çalışıyor** (önceden 89/90).

### İndirilen yabancı listeler için kod filtresi
`CountryRepo` içine eklendi: `blocked()` (yetişkin + abonelik markası), `isBareIp()`, `cleanName()`
(`[Geo-blocked]`, `(1080p)` gibi ekler temizlenir). Yani ülke değiştiren kullanıcı da riskli kayıt görmüyor.

## Cihazda doğrulama
| Test | Sonuç |
|---|---|
Ana liste | ✅ **156 kanal**, ilk sıralar: TRT 1, TRT 2, TV8, Star TV, Kanal D, Haber Global, NTV, A Haber |
TRT 1 oynatma | ✅ yayın göründü, sayaç **1/156**, alt bar düzgün |
Listede Cinemax/AXN/BBC First | ✅ yok |
