# UMAY Multi TV — v9.15 (versionCode 24)

23 Kasım 2026 · `CanliTV-9.15.apk` · `CanliTV-9.15.aab`

## Düzeltmeler

### 📺 TV oynatıcı: görüntü yoktu, sadece "✓ Bu kanalda reklamsız" yazısı görünüyordu
- **Sebep:** reklamsız şerit ve banner, video katmanının (FrameLayout) **içine** eklenmişti.
  Şerit videonun üzerine ortalanıyor, video/oynatma düğmeleri de arkada kalıyordu.
- **Çözüm:** oynatıcı artık dikey bir kapta: **video (üstte, kalan alan)** + **reklamsız şerit** +
  **banner (en altta)**. Şerit ne videonun üstüne biniyor ne de oynatma düğmelerini kapatıyor.

### 📱 Banner sistem çubuğunun altında kalıyordu
- Kaba, Android'in gezinme çubuğu yüksekliği kadar **alt boşluk** eklendi.
- Banner artık telefonun kontrol alanının **tam üstünde**, kesilmeden ve dokunulabilir.

## Test (cihazda, TRT 1)
| Test | Sonuç |
|---|---|
Video görüntüsü | ✅ yayın görünüyor, sayaç işliyor |
Reklamsız şerit + banner | ✅ videonun altında, üst üste binmiyor |
Sistem çubuğu | ✅ banner çubuğun üstünde |
Oynatma düğmeleri (⏪ ⏸ ⏩ ⏰) | ✅ erişilebilir |
