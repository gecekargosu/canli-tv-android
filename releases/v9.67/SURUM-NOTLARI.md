# UMAY Multi TV 9.67 — sürüm notları

Tarih: 24 Kasım 2026 · versionCode 76 · versionName 9.67

Bu sürüm **3'lü izleme setleri** turu: kaydedilen set açılmıyordu, panelin yarısı
ekran dışında kalıyordu ve liste ham kod gösteriyordu.

## Düzeltmeler

1. **İsimli setler hiç geri yüklenmiyordu.**
   `MultiActivity` kaydı okurken `!saved.startsWith("ad:")` koşulu vardı; yani
   "★ set" ile kaydedilen her set (kodda `ad:<ad>|...`) bu koşulda eleniyor,
   set seçilse bile varsayılan kanallar açılıyordu. Koşul kaldırıldı, `ad:`
   öneki ayrıştırmadan önce atılıyor.
2. **Set indeks tutuyordu, ülke/liste değişince başka kanal açılıyordu.**
   Kayıt artık `u:` alanında dört pencerenin **kanal adresini** de saklıyor
   (`|u:<url1>,<url2>,...`). Açarken adresler güncel listede aranıp indekse
   çevriliyor; adres bulunamazsa eski indeks kullanılıyor. Eski kayıtlar
   (adres alanı olmayan) bozulmadan çalışmaya devam ediyor.
3. **"★ set" düğmesi ekran dışındaydı.** Kontrol paneli tek satırda sığmıyor,
   1280 px genişlikte "★ set" ve sonrası görünmüyordu; bu yüzden set kaydetmek
   fiilen imkânsızdı. Panel yatay kaydırılabilir yapıldı (kaydırma çubuğu gizli).
4. **Set listesi ham kod gösteriyordu.** Ana ekranda "3'lü izleme setleri"
   listesinde `ad:Tek Seti|0,1,2,3|1000|3|u:...` yazıyordu; artık yalnız set adı
   ("Test Seti") görünüyor.

## Doğrulanan (kodda zaten kapalı olan maddeler)

- **M-25 (veri sayacı kapsamı):** `AudioService.flushUsage/checkLimit` radyo ve
  arka plan sesinde `TrafficStats` farkını günlük sayaca yazıyor ve limit dolunca
  yayını duraklatıyor; çoklu ekran `MultiActivity.countData()` ile ölçülüyor.
  Kod okumasında tam bulundu, ek iş gerekmedi.
- **M-28 (`Good` gün damgası):** damga yalnız `download()` başarılı olunca
  yazılıyor; internet bir kez kopunca o gün tekrar denenmeme sorunu yok.

## Kapasite denetimi

```
APK     : 7.121.589 bayt · 400 dosya · 17.517 sınıf
önceki  : 9.66 · 7.121.045 bayt · 400 dosya · 17.517 sınıf
fark    : sınıf +0 · dosya +0 · boyut +544 bayt
imza    : f3996c67ef0e0e0420f442fe3fcf6303a17caa88dcba8a2b19a10cd95c92b8d5
✅ Kapasite denetimi TEMİZ
```

## Cihazda denenip doğrulananlar (Xiaomi 2506BPN68G, Android 16)

- 3'lü ekranda "★ set" → "Test Seti" kaydedildi (paneller: 3sat / ADRIA /
  Adventure Earth).
- 2. pencere `№6 arte` yapıldı, ana ekrana dönüldü.
- "3'lü izleme setleri" → **Test Seti** seçildi → 2. pencere `arte`'den kayıtlı
  `ADRIA`'ya döndü. (Düzeltme öncesi isimli set hiç uygulanmıyordu.)
- Set listesinde ad "Test Seti" olarak görünüyor (ham kod değil).
- Kaydetme sırasında tam ekran reklam çıktı (dokunulmadı, kendiliğinden kapandı).
- `FATAL` yok.

## Bilinen açık noktalar

- Kayıtlı set, kanal adresi o listede yoksa (kanal kaldırıldıysa) eski indekse
  düşüyor; kullanıcıya "kanal bulunamadı" bilgisi verilmiyor.
- `Prefs` kullanım anahtarı hâlâ `url.hashCode()` (M-65); çakışma riski sürüyor.
- Tablet kurulumu HyperOS kısıtı nedeniyle elle: `UMAY-MULTI-TV-9.67-tablet.apk`
  hem `releases/v9.67/` hem `İndirilenler` içinde.
