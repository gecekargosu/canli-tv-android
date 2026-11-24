# UMAY Multi TV 9.68 — sürüm notları

Tarih: 24 Kasım 2026 · versionCode 77 · versionName 9.68

Bu sürüm iki "yanlış bilgi" turu: kanal veri sayaçları birbirine karışabiliyordu
ve kalite etiketi yayının gerçek çözünürlüğünü değil, kullanıcının tercihini
yazıyordu.

## Düzeltmeler

1. **Kanal veri sayaçları çakışabiliyordu (M-65).**
   `Prefs` kullanım anahtarı `url.hashCode()` idi; iki farklı yayın adresi aynı
   hash'e düşünce bir kanalın harcaması diğerinde görünüyor, sayaç sıfırlanmış
   gibi davranıyordu. Anahtar artık **tam adres** (`use:<url>` / `useS:<url>`).
   Eski kayıtlar ilk okumada yeni anahtara taşınıyor; kullanıcının bugüne kadarki
   kanal başına verisi kaybolmuyor.
2. **Kalite etiketi tercihi gösteriyordu (M-96).**
   Düğme `Ayarlar → Yayın kalitesi` değerini yazıyordu ("360p"). Tek varyantlı
   kanallarda (ör. 3sat) tercih uygulanamıyor, yayın 720p/1080p geliyor ama
   etiket "360p" diyordu — kullanıcı saatte 300 MB beklerken 1 GB harcıyordu.
   Etiket artık oynatıcıdan gelen **gerçek yükseklik** (Media3
   `onVideoSizeChanged`). Tercih değiştirilince eski davranış korunuyor:
   önce tercih yazılır, yeni yayın hazır olunca gerçek değer gelir.

## Kapasite denetimi

```
APK     : 7.122.373 bayt · 400 dosya · 17.517 sınıf
önceki  : 9.67 · 7.121.589 bayt · 400 dosya · 17.517 sınıf
fark    : sınıf +0 · dosya +0 · boyut +784 bayt
imza    : f3996c67ef0e0e0420f442fe3fcf6303a17caa88dcba8a2b19a10cd95c92b8d5
✅ Kapasite denetimi TEMİZ
```

## Cihazda denenip doğrulananlar (Xiaomi 2506BPN68G, Android 16)

- 3sat açıldı: kalite düğmesi **720p** gösterdi (tercih 360p) — yayının gerçek
  çözünürlüğü; düzeltme öncesi burada "360p" yazıyordu.
- Ana listeye dönüldü: kanal başına sayaçlar korunmuş
  (General ★ · **304,8 MB (20 dk)**, Music · 110,9 MB (5 dk),
  Documentary · 117,1 MB (5 dk)) → anahtar taşıma veri kaybetmedi.
- `FATAL` yok; tam ekran reklam akışı dokunulmadan geçildi.

## Bilinen açık noktalar

- Kalite etiketi yayın **hazır olana kadar** tercihi gösterir; ekran görüntüsü
  ilk saniyelerde eski değeri gösterebilir.
- Tek varyantlı kanalda tercih "360p" olsa bile yayın 720p iner; uygulama artık
  bunu etikette doğru yazıyor ama veri tüketimini düşürmüyor (transcode yok).
- Tablet kurulumu HyperOS kısıtı nedeniyle elle: `UMAY-MULTI-TV-9.68-tablet.apk`
  hem `releases/v9.68/` hem `İndirilenler` içinde.
