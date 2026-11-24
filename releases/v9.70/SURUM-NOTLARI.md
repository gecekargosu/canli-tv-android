# UMAY Multi TV 9.70 — sürüm notları

Tarih: 24 Kasım 2026 · versionCode 79 · versionName 9.70

Bu turda **kayıt** (HlsRecorder) sağlamlaştırıldı ve **kanal listesine eşzamanlı erişim**
sorunu kapatıldı.

## Kayıt (M-26)

1. **Kalite tercihine uygun varyant.** Master playlist'te eskiden her zaman ilk
   varyant (genelde 1080p) indiriliyordu; Şimdi Ayarlar → Yayın kalitesi tercihi
   kullanılıyor (360p tercih eden kullanıcı kayıtta saatte GB'larca veri harcamıyor).
2. **Token'lı CDN'ler.** Master adresindeki `?token=...` sorgusu segment adreslerine
   taşınıyor; önceden jetonlu yayınlarda segmentler 403 alıp kayıt sessizce boş kalıyordu.
3. **Yanlış "kaydedildi" bilgisi.** fMP4 (`EXT-X-MAP`) ve byte-range yayınlar .ts
   konteynerine yazılamıyordu ama dosya "kaydedildi" görünüp açılmıyordu. Artık net
   uyarı verilip kayıt hiç başlatılmıyor; yayın kayıt sırasında bu biçime geçerse de
   bilgi verilip durduruluyor.
4. **Yer kontrolü.** Kayıt öncesi boş alan bakılıyor; 500 MB'ın altındaysa
   "Yer yok: N MB boş…" uyarısı veriliyor (eskiden kayıt ortasında kesiliyordu).
5. **Hızlı durdur → başlat yarışı.** Önceki kaydın iş parçacığı son segmenti yazarken
   yeni kayıt isteği sessizce yutuluyor, düğme kırmızı kalıp kullanıcı kayıtta
   sanıyordu. Artık istek reddedilirse "Önceki kayıt kapanıyor, bir saniye sonra tekrar
   dene" uyarısı çıkıyor ve düğme yalnız gerçek başlangıçta kırmızıya dönüyor.

## Kanal listesi (M-33)

`ChannelRepo` yüklemesi artık `synchronized`; arka plan işleri (radyo servisi, kanal
taraması, onarım) canlı liste yerine yeni `snapshot()` kopyası üzerinde çalışıyor.
Ülke değiştirilirken liste boşalıp yeniden dolarken `ConcurrentModificationException`
veya yanlış kanala atlama riski kalktı.

## Kapasite denetimi

```
APK     : 7.123.653 bayt · 400 dosya · 17.518 sınıf
önceki  : 9.69 · 7.122.925 bayt · 400 dosya · 17.518 sınıf
fark    : sınıf +0 · dosya +0 · boyut +728 bayt
imza    : f3996c67ef0e0e0420f442fe3fcf6303a17caa88dcba8a2b19a10cd95c92b8d5
✅ Kapasite denetimi TEMİZ
```

## Cihazda denenip doğrulananlar (Xiaomi 2506BPN68G, Android 16)

- 3sat kanalında kayıt iki kez alındı: `3sat_…ts` dosyaları oluştu (2,77 MB ve
  6,66 MB), "Kayıt durduruluyor…" ve bitiş bildirimi geldi, çökme yok.
- Ana liste 113 kanal (Almanya), ülke ekranı ve Ayarlar normal.
- `FATAL EXCEPTION` yok.

## Bilinen açık noktalar

- AES-128 şifreli yayınlar hâlâ kaydedilmiyor (net uyarı veriliyor).
- Kayıt öncesi boyut/süre tahmini yok.
- Kalite tercihinin dosya boyutuna etkisi (360p vs 1080p A/B) cihazda ölçülemedi;
  oyun/mağaza reklamları dokunuşları yutuyor, ölçüm elle yapılmalı.
- Tablet kurulumu HyperOS kısıtı nedeniyle elle: `UMAY-MULTI-TV-9.70-tablet.apk`
  hem `releases/v9.70/` hem `İndirilenler` içinde.
