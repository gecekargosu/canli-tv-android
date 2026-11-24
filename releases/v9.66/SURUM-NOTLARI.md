# UMAY Multi TV 9.66 — sürüm notları

Tarih: 24 Kasım 2026 · versionCode 75 · versionName 9.66

Bu sürüm **medya motoru + liste sağlığı + reklam deneyimi** turu. Yeni özellik
yok; "çalışıyor" görünen ama yanlış davranan dört nokta düzeltildi.

## Düzeltmeler

1. **"Sadece ses" / arka plan ses gerçekten en düşük kaliteyi kullanıyor (M-24).**
   `AudioService` verilen adresi olduğu gibi açıyordu; adres bir master playlist
   ise oynatıcı ABR ile 1080p'ye kadar çıkabiliyor, yani "en az veri" iddiası
   yanlış oluyordu. Servis artık önce `Quality.resolveLowest` ile en düşük
   varyantı çözüp onu açıyor. Çözüm arka planda yapılıyor; bu arada başka
   istasyon seçilirse (`currentUrl` değiştiyse) eski istek atılıyor, yanlış
   istasyon çalmıyor. Varyant yoksa özgün adres kullanılıyor.
2. **Ölü kanal onarımı artık segmenti de deniyor (M-29).**
   `Repair.alive` yalnız playlist'in 200 dönmesine ve `#EXTINF` bulunmasına
   bakıyordu; sunucu playlist'i verip segmentleri silmişse kanal "sağlam"
   sayılıyor, listede kalıyor ve açılmıyordu. Üçüncü seviye kontrol eklendi:
   playlist'teki son segment `Range: bytes=0-2047` ile isteniyor, 200/206 ve
   en az bir bayt veri şartı aranıyor. Sunucu Range'i yok sayarsa bağlantı ilk
   okumadan sonra kapatılıyor (tüm dosya inmiyor).
3. **Ödüllü reklamdan sonra yayın baştan başlamıyor (M-42).**
   `AdManager.houseBar` ödül alınınca `Activity.recreate()` çağırıyordu; ekran
   yeniden kurulduğu için kanal kapanıp yeniden açılıyor, kullanıcı ödül aldığı
   yayını kaybediyordu. Artık şerit yerinde güncelleniyor
   ("✅ Reklamsız · N dk") ve teşekkür mesajı gösteriliyor.
4. **İlk kanal açılışında tam ekran reklam yok (M-44).**
   Uygulamaya ilk gelen kullanıcı, ilk kanalı açar açmaz reklamla
   karşılaşıyordu. `Prefs`'e kanal açılış sayacı eklendi; ilk açılışta reklam
   atlanıyor, sonraki açılışlarda normal akış (mevcut 60 sn aralık koruması
   hızlı zap'ta reklam göstermiyor).

## Zaten yerinde olan, doğrulanan maddeler

- **M-22 (hızlı zap):** `playNow()` içindeki `playRequest` sayacı eski isteğin
  sonucunu atıyor — hızlı kanal değiştirirken yanlış kanalın açılması engelli.
- **M-23 (BUFFERING'de donma):** `checkStall()` 12 sn eşiğiyle yayını yeniden
  başlatıyor.

## Kapasite denetimi

```
APK     : 7.121.045 bayt · 400 dosya · 17.517 sınıf
önceki  : 9.65 · 7.119.765 bayt · 400 dosya · 17.515 sınıf
fark    : sınıf +2 · dosya +0 · boyut +1.280 bayt
imza    : f3996c67ef0e0e0420f442fe3fcf6303a17caa88dcba8a2b19a10cd95c92b8d5
✅ Kapasite denetimi TEMİZ
```

## Cihazda denenip doğrulananlar (Xiaomi 2506BPN68G, Android 16)

- **Günlük veri limiti (D-34):** limit 5 MB yapıldı, kanal açıldığında
  "Günlük veri limiti doldu — Limit: 5 MB / Kullanılan: 160,2 MB · Devam
  edilsin mi?" diyalogu çıktı; ana ekran başlığı da "5 MB ⚠ LİMİT AŞILDI"
  gösterdi. Test sonunda limit "Yok"a çevrildi.
- **Sadece ses (M-24):** "Sadece ses" ile oynatıcı kapandı, bildirimde
  `media_session` durumu `PLAYING`; yayın kesintisiz devam etti, veri akışı
  video moduna göre düşük kaldı. Çözülen varyantın adı log'da tutulmadığı için
  "en düşük varyant" iddiası kod düzeyinde doğrulandı, ekran görüntüsüyle
  değil.
- **Reklam davranışı (M-44/M-42):** yüklemeden sonraki **ilk** kanal açılışında
  reklam çıkmadı; **ikinci** açılışta tam ekran reklam normal şekilde geldi.
  Ödüllü reklam akışı (M-42) reklam ağı doldurmadığı için cihazda denenemedi.
- **Ölü kanal onarımı (M-29):** Almanya listesinde ölü kanal bulunmadığı için
  "Onar" gerçek segment denemesi yapmadan bitti; segment kontrolü kod
  düzeyinde. Çökme yok.
- Log taraması: `FATAL` / `AndroidRuntime` hatası yok.

## Bilinen açık noktalar

- Ödüllü reklam ve tam ekran reklam akışının otomasyonla testi yapılmıyor
  (reklam ekranında asistana dokunma kuralı).
- Tablet kurulumu HyperOS `INSTALL_FAILED_USER_RESTRICTED` nedeniyle elle:
  `UMAY-MULTI-TV-9.66-tablet.apk` hem `releases/v9.66/` içinde hem
  `İndirilenler` klasöründe.
