# UMAY Multi TV — v9.6 (versionCode 15)

23 Kasım 2026 · `CanliTV-9.6.apk` · `CanliTV-9.6.aab`

## Düzeltmeler

### 📢 Banner sistem çubuğunun altında kalıyordu
- Android'in alt gezinme çubuğu (jest çubuğu / geri düğmesi) banner'ın üstüne biniyordu.
- Artık banner konteynerine **sistem çubuğu yüksekliği kadar alt boşluk** ekleniyor; banner her cihazda
  tamamen görünür ve okunur.

### 📻 Radyo listesinde reklam yoktu
- v9.5'te `wrap` yalnızca radyo **çalar** ekranına eklenmişti; radyo **listesi** ekranı atlanmıştı.
- Artık radyo listesinin en altında da banner + veri satırı var.

### 🔤 Radyo çalar üstündeki dikey "RADYO" yazısı
- Üst barda yer kalmadığı için tek harfe sıkışan turuncu başlık tamamen kaldırıldı.
- Üst bar artık sadece okunur düğmelerden oluşuyor: `◀ Liste · 🏠 Ana · ⏻ Çık · ⏱ Uyku · ⏹ Durdur`.

## Yeni: yayın sağlık taraması
- `_build/saglik.py` ile ülke listeleri ve radyo listeleri toplu test edilir;
  sonuç `docs/yayin-saglik-raporu.md` dosyasına yazılır.
- Ölçüm notu: tarama Türkiye'den yapıldığı için ABD/Avrupa kanallarının çoğu **coğrafi kilit**
  nedeniyle açılmıyor; bu, uygulama hatası değil yayıncı kısıtı.

## Test
| Test | Sonuç |
|---|---|
Derleme (APK + AAB) | ✅ |
Kurulum (üstüne) | ✅ |
Radyo listesi + çalar banner konumu | ✅ (sistem çubuğunun üstünde) |
Üst bar | ✅ dikey yazı yok |
