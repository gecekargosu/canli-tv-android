# UMAY Multi TV 9.74

**Tarih:** 25 Kasım 2026 · versionCode 83

Bu sürüm günlük kullanımda fark edilen küçük ama can sıkıcı sorunları kapatıyor.
ATV yayını bu sürümde değiştirilmedi (ayrıca ele alınacak).

## Düzeltmeler

- **Kalite ↔ veri tasarrufu tutarlılığı:** Veri tasarrufunu kapatınca kalite artık
  "otomatik / kanalın en iyisi"ne değil, **360p**'ye dönüyor. Ayarlar'daki açıklama
  gerçek seçimi yazıyor ("Kapalı: seçili kalite kullanılıyor (360p)").
- **Varyantı olmayan kanal:** 360p seçili ama kanalda yalnız 720p/1080p varsa oynatıcı
  boş kalmıyor; kalite kısıtı kalkıyor, kanal açılıyor ve bir kez
  "Bu kanalda 360p yok · en düşük 720p oynatılıyor" bilgisi gösteriliyor.
- **Çoklu ekran geri tuşu:** Geri tuşu her zaman ana kanal listesine dönüyor;
  pencereler, arka plan sesi ve açık kayıt oturumu kapatılıyor.
- **Kayıt oturumu:** Çoklu ekran kapatılırken açık kayıt da durduruluyor
  (arka planda dosya yazmaya devam etmiyor).
- **Kategori adları:** "Eglence", "Muzik", "Cocuk" gibi kaynak yazımları
  "Eğlence", "Müzik", "Çocuk" olarak gösteriliyor.
- **Dokunma alanları:** Üst bardaki düğmeler en az 48dp (erişilebilirlik) ve artık kendi
  satırında; başlık "UMAY Mul..." diye kesilmiyor.
- **Ayarlar taşması:** Uzun açıklama ve değer satırları artık iki satırda kesiliyor,
  ekran dışına taşmıyor.

## Notlar

- Bu sürümde ATV adresi değişmedi; ATV sorunu ayrı bir iş olarak duruyor.
- Reklam geliri tarafında değişiklik yok; AdMob "sınırlı reklam sunumu" Play yayınına
  bağlı olarak devam ediyor.

## Dosyalar

- `CanliTV-9.74.apk` — telefon ve tablet (aynı APK)
- `UMAY-MULTI-TV-9.74.aab` — Google Play

SHA-256: `CanliTV-9.74.apk` → GitHub sürüm sayfasındaki dosyayla birebir aynı.
