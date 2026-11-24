# UMAY Multi TV 9.47

**Tarih:** 24 Kasım 2026 · versionCode 56

## Radyo artık veri sayacına yazılıyor (M-25)
- Radyo/arka plan dinlerken harcanan veri **günlük sayaca** ekleniyor (ekranın altındaki
  "Bugün kullanılan veri" satırı ve Ayarlar aynı değeri gösterir).
- **Günlük veri limiti** radyoda da geçerli: limit dolunca radyo durur, bildirimde
  "Günlük veri limiti doldu · radyo durduruldu" yazar ve olay kaydına işlenir.
- Cihazda doğrulandı: radyo çalarken sayaç 2,3 MB → 2,5 MB arttı.

## Ülke listeleri: "hazır" eşiği (M-16)
- Bir ülke **en az 30 kanal** doğrulanmışsa "hazır" görünür.
- 1–29 kanalı olan ülkeler **"n kanal · yakında (30+ gerekli)"** etiketiyle gösterilir; boş liste
  açılmaz. Türkiye her zaman hazır.

## Liste sağlığı denetimi (M-14 / M-15)
- Yeni araç: `python tools/check-lists.py` → çıplak IP, `raw.githubusercontent`, ücretli marka
  (beIN, Digiturk, Cinemax…), HTTP ve kopya kayıtları raporlar; `--fail` ile CI'da kullanılabilir.
- Mevcut durum: **temiz** (TR kanal + radyo + sunucu `good/*.json`).
  İstisna: 46.20.3.207 / 46.20.3.250 (TR radyo yayıncıları, bilinçli olarak beyaz listede).

## Notlar
- İmza aynı; üzerine kurulabilir. Yeni izin yok.
