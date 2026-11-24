# UMAY Multi TV 9.69 — sürüm notları

Tarih: 24 Kasım 2026 · versionCode 78 · versionName 9.69

Bu turda üç küçük ama kullanıcıyı yanıltan hata kapatıldı; ayrıca **master liste
sayımı düzeltildi** (aşağıda).

## Düzeltmeler

1. **Reklam şeridi banner'a yapışıktı (M-43).**
   Sarı "▶ Reklam izle · 15 dk reklamsız" şeridi banner alanının hemen üstünde,
   boşluksuz duruyordu. Banner yüklenirken veya yüklenemediğinde kullanıcı banner
   yerine şeride dokunup farkında olmadan ödüllü reklam akışını başlatabiliyordu.
   Şeride 6 dp üst boşluk verildi.
2. **Kayıtlı sette bulunamayan kanal sessizce yutuluyordu (M-97).**
   Ülke değişince setteki kanallar listeden kalkabiliyor; uygulama sessizce başka
   kanalı açıyordu. `resolveUrls` artık eşleşmeyenleri sayıyor ve çoklu ekran
   açılırken **"Kayıtlı setteki N kanal bu listede yok (kaldırılmış olabilir)"**
   uyarısı gösteriliyor; pencereler eşleşen kanallarla açılıyor.
3. **Ayarlar'da ülke değeri bayat kalıyordu (M-98).**
   `SettingsActivity` değeri yalnız `onCreate`'de yazıyordu; ülke değiştirip geri
   dönünce satır eski ülkeyi gösteriyordu (cihazda ülke Türkiye olduğu hâlde
   "Almanya" yazıyordu). Değer `onResume`'da tazeleniyor.

## Kapasite denetimi

```
APK     : 7.122.925 bayt · 400 dosya · 17.518 sınıf
önceki  : 9.68 · 7.122.373 bayt · 400 dosya · 17.517 sınıf
fark    : sınıf +1 · dosya +0 · boyut +552 bayt
imza    : f3996c67ef0e0e0420f442fe3fcf6303a17caa88dcba8a2b19a10cd95c92b8d5
✅ Kapasite denetimi TEMİZ
```

## Cihazda denenip doğrulananlar (Xiaomi 2506BPN68G, Android 16)

- Ülke **Türkiye**'ye alındı (145 kanal geldi) → Almanya kanallarıyla kaydedilmiş
  **"Test Seti"** açıldı: *"Kayıtlı setteki 4 kanal bu listede yok (kaldırılmış
  olabilir)"* uyarısı çıktı, pencereler TR kanallarıyla açıldı, çökme yok.
- Ülke **Almanya**'ya geri alındı; Ayarlar ekranı dönüşte satırı hemen "Almanya"
  olarak gösterdi (eski sürümde "Türkiye"de kalıyordu).
- Ana ekranda şerit ile kullanım çubuğu arasındaki boşluk görünür.
- `FATAL` yok.

## Master liste sayımı (düzeltme)

Önceki yarı raporlarındaki "✅ bitti" sayıları elle tutuluyordu ve şişmişti. Liste
artık programla sayılıyor: tabloda **93 madde** + rapor içindeki **M-88…M-93**
(7 madde) = **100 madde**. Doğru durum: **50 ✅** · **50 açık** (41 kod · 7 kullanıcı
· 2 cihaz testi). M-17 ve M-50 satırlarında metin içinde geçen "✅" işareti sayımı
şaşırtmasın diye durum sütunu esas alınıyor.

## Bilinen açık noktalar

- Tek varyantlı kanallarda (ör. 3sat) kalite tercihi uygulanamıyor; etiket doğru
  söylüyor ama veri tüketimi düşmüyor (transcode yok).
- Tablet kurulumu HyperOS kısıtı nedeniyle elle: `UMAY-MULTI-TV-9.69-tablet.apk`
  hem `releases/v9.69/` hem `İndirilenler` içinde.
