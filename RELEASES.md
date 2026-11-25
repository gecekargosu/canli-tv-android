# Sürümler

APK, tablet APK ve AAB dosyaları repoda **tutulmaz** (depoyu şişirmemek için).
Hepsi GitHub Releases sayfasında:

**https://github.com/gecekargosu/canli-tv-android/releases**

- En son sürüm: `v9.74` — `CanliTV-9.74.apk` (telefon/tablet), `UMAY-MULTI-TV-9.74.aab` (Play).
- İndirme sayfası: https://umay-multi-tv-gq3zer.freebuff.site/indir
- Sürüm notları: `releases/vX.YY/SURUM-NOTLARI.md`

## v9.74 — kalite/veri tutarlılığı, çoklu ekran geri, kategori adları

- Veri tasarrufu kapanınca 360p'ye döner; Ayarlar gerçek seçimi yazar.
- Varyantı olmayan kanalda kalite kısıtı kalkar, tek satır bilgi gösterilir.
- Çoklu ekranda geri tuşu ana listeye döner; açık kayıt durdurulur.
- "Eglence/Muzik/Cocuk" → "Eğlence/Müzik/Çocuk".
- Üst bar düğmeleri 48dp; Ayarlar satırları taşmıyor.
- ATV bu sürümde değişmedi (ayrı iş).

## v9.73 — reklam güvenilirliği, hata günlüğü, ATV

- Reklam istek fırtınası (saniyede 1 istek) düzeltildi; backoff + ödüllü otomatik yeniden deneme.
- Banner hatası doğru etiketle ve 30 dakikada bir kaydediliyor; NO_FILL nedeni yazılı.
- Hata günlüğü: satır sarma + en yeniye kaydırma + isteğe bağlı günlük gönderim (`/api/log`).
- ATV 720p birincil (cihazda açıldı), 480p/master yedek.
- İnternet/DNS yokken kanal ölü işaretlenmiyor.
- Gizli geliştirici modu: reklam sayacı, reklamları kapat, reklam teşhisi (kullanıcıya görünmez).
- README ve Play uygunluk/veri beyanı metinleri gerçek davranışa göre güncellendi.

Yerel arşivde aynı dosyalar `releases/` altında durur ama git'e eklenmez.
