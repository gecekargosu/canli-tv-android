# UMAY Multi TV 9.48

**Tarih:** 24 Kasım 2026 · versionCode 57

## Radyoyu her yerden kapat (M-73)
- Radyo arkada çalarken **ana ekranın üstünde şerit** çıkıyor: **"🎧 GENÇ KRAL FM çalıyor"** +
  sağda **⏹ Kapat** butonu. Şeride dokunmak da radyoyu kapatıyor.
- Radyo çalar ekranında zaten **Durdur** ve **Çık** var; bildirimde de Durdur butonu duruyor.
- Cihazda test edildi: Kapat → şerit kayboldu, ses servisi durdu (çalışan servis sayısı 0).

## Kanal envanteri (M-13 — ilk adım)
- Yeni araç: `python tools/make-inventory.py` → `site/public/kanallar.json`
  (177 kayıt: 145 TV + 32 radyo; her kayıtta **kaynak alan adı, hak durumu, son kontrol tarihi,
  durum**).
- Şüpheli kaynaklar (çıplak IP, `raw.githubusercontent`, ücretli marka) otomatik **"review"**
  işaretlenir; şu an tüm kayıtlar **public**.
- **Not (yarım):** uygulama henüz `kanallar.json`'dan beslenmiyor; sunucu tarafı hak filtresi
  (M-15) bu dosya yayına alındıktan sonra bağlanacak.

## Notlar
- İmza aynı; üzerine kurulabilir. Yeni izin yok.
