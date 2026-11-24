# UMAY Multi TV 9.43

**Tarih:** 24 Kasım 2026 · versionCode 52

## Bağlantı sorunları görünür ve kurtarılabilir
- **15 saniye zaman aşımı** (TV ve radyo). Süre dolunca **5 saniye arayla 3 kez** aynı yayına
  yeniden bağlanılır; 3'ü de olmazsa sonraki kanala/istasyona geçilir.
- Ekranda durum yazısı: **"📶 Bağlantı sorunu · yeniden deneniyor (1/3)"**, bağlantı düzelince
  **"✅ Bağlantı iyi · çalıyor"**.
- **Radyo ekranına iki buton:** **🔄 Tekrar dene** ve **⏭ Sonraki kanal**. Cihazda test edildi:
  Tekrar dene yeniden bağlanıyor, Sonraki kanal JOY TÜRK TURKEY'e geçip çaldı.
- **Radyo gerçekten çalıyor mu?** Artık `isPlaying()` yetmiyor: yayın konumu ve ağdan gelen veri
  (12 sn'de <8 KB) izleniyor; sessizce takılan yayın "çalıyor" sayılmıyor, otomatik yeniden bağlanıyor.

## Reklam artık radyoyu kesmiyor
- Radyo listesinden istasyon seçince **geçiş reklamı gösterilmiyor** (ses kesiliyordu).
- Başka ekranda reklam açılırsa radyo **duraklar ve reklam kapanınca kendiliğinden devam eder**
  (ses odağı artık kalıcı bırakmıyor). Ekranda "⏸ Duraklatıldı (reklam/başka ses)" yazar ve
  "reklamı kapatınca devam eder" bilgisi gösterilir.

## Hata günlüğü + e-posta
- Uygulama artık **çökme dışında bağlantı hatalarını da** kaydeder (hangi kanal, kaçıncı deneme).
- **Ayarlar → Hata bildir**: "📧 E-posta" ile destek adresine (cengizkilic1980@gmail.com) sürüm +
  cihaz bilgisi + son olaylar hazır metin olarak gelir; "Paylaş" ile WhatsApp/mesaj da seçilebilir.

## Notlar
- İmza aynı; üzerine kurulabilir. Yeni izin yok.
