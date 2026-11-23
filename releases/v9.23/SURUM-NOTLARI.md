# UMAY Multi TV — v9.23 (versionCode 32)

23 Kasım 2026 · `CanliTV-9.23.apk` · `CanliTV-9.23.aab`

## 1) Varsayılan kalite artık **360p** (en düşük makul kalite)
- `Prefs.maxHeight` varsayılanı `0 (Otomatik = en iyi kalite)` → **`360p`**.
- Yani tüm kanallar ilk açılışta **en az veri** ile başlıyor; görüntü 360p, ses aynı.
- Kullanıcı isterse yükseltir (🎚 düğmesi).

## 2) Kalite yükseltmede **veri onayı**
- 480p / 720p / 1080p seçilirse önce bilgi + onay: *"720p ≈1,2 GB/saat harcar, mobil veride kotanı hızlı tüketebilir. Mobil verideyken 360p önerilir."*
- Seçenekler: **Yükselt** / **360p'de kal**. Onaylamazsa ayar 360p'de kalır.

## 3) Çoklu ekran (ızgara) düğmesi görünür yere alındı
- Özellik **kaybolmamıştı**: ana ekranda "▦ 3'lü", oynatıcıda "▦ Çoklu" vardı; ama oynatıcı barının **en sonunda** olduğu için ekran dışında kalıyordu.
- Artık **Favori'den hemen sonra**, barın başında: 🏠 · ★ · **▦** · 🎚 · ⏺ · 🎧 · ⧉ · 🔒.

## Cihazda doğrulama
| Test | Sonuç |
|---|---|
| TRT 1 açılış | ✅ 2/157, alt bar düzgün |
| Kalite diyaloğu | ✅ **● 360p** işaretli (varsayılan), her seçenekte GB/saat bilgisi |
| 720p seçimi | ✅ veri uyarısı çıktı, "360p'de kal" ile geri dönüldü |
| ▦ Çoklu | ✅ oynatıcıdan çoklu ekran açıldı (2 pencere) |
