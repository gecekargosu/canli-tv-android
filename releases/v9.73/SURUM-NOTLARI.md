# UMAY Multi TV 9.73 (versionCode 82)

Tarih: 25 Kasım 2026

## Reklam geliri ve güvenilirlik

- **Reklam istek fırtınası düzeltildi.** Zamanlayıcı her saniye çalıştığı için reklam
  yüklenemediğinde saniyede bir yeni istek gidiyordu. Artık 30 sn → 5 dk arası geri çekilme
  (backoff) var; ödüllü reklam kendiliğinden yeniden deneniyor.
- **Banner hata etiketi düzeltildi**: banner yüklenememesi "geçiş reklamı yüklenemedi" diye
  kaydediliyordu; artık "banner yüklenemedi" ve nedeni (NO_FILL / ağ / geçersiz istek) yazılıyor.
- Aynı reklam hatası günlüğü doldurmasın diye **30 dakikada bir** kayıt (throttle).
- Gelir akışı doğrulandı: gerçek banner + geçiş reklamı cihazda gösterildi; AdMob gösterimi
  SDK üzerinden kendisi raporlar. Teşhis ekranında birim kimlikleri, rıza durumu, hazır
  reklamlar, son hata ve bu cihazda sayılan gösterim görünür.

## Hata günlüğü

- Günlük penceresi: uzun adresler satır sonuyla bölünüyor (sağa taşmıyor) ve pencere
  açılınca **en yeni olaya** kaydırılıyor.
- **Günlük otomatik gönderimi** eklendi (varsayılan **KAPALI**). Kullanıcı açarsa günde en
  fazla bir kez yalnızca teknik kayıtlar `POST /api/log` ile sunucuya gider; ad/e-posta/konum
  gönderilmez. Gizlilik politikası ve Play Data Safety metni buna göre güncellendi.

## TV / radyo

- **ATV**: cihazda açıldığı doğrulanan 720p adres birincil yapıldı; 480p ve master yedek.
  (Master varyant listesi bazı cihazlarda hazırlanmıyordu.)
- İnternet/DNS yokken kanal artık **"ölü" işaretlenmiyor**; "İnternet bağlantısı kurulamadı"
  mesajı gösteriliyor ve sayaç artmıyor. Böylece zayıf Wi-Fi'de sağlam kanallar listeden düşmüyor.

## Geliştirici (yalnız sahip cihazı, kullanıcıya görünmez)

- **Gizli geliştirici modu**: Ayarlar başlığına 5 kez dokun.
  - Reklam sayacım (tahmini kazanç, bu cihaz)
  - **Reklamları kapat** → bu telefonda hiç reklam istenmez/gösterilmez (banner hemen kaldırılır)
  - Reklam teşhisi
  - Geliştirici modu kapat
- Reklam sayacı, test modu ve reklam kapatma **yayınlanan APK'da normal kullanıcıya görünmez**.

## Doküman / depo

- `README.md` gerçek davranışa göre yeniden yazıldı (reklamsız/hesap/302 kanal ifadeleri kaldırıldı).
- `docs/play/PLAY-UYGUNLUK-2026-11-25.md`: Play uygunluk denetimi + Data Safety tablosu.
- Mağaza metni ve Play hazırlık dosyalarındaki hesap/veri beyanları düzeltildi.

## Doğrulama

- `verify-apk`: 9.72 → 9.73, sınıf +7, dosya +0, imza `f3996c67…b8d5`, sürüm 9.73 (82) — TEMİZ.
- Cihaz (Xiaomi 2506BPN68G, Android 16): ATV açıldı (720p, veri 171,1 → 175,6 MB);
  gerçek banner + geçiş reklamı gösterildi; teşhis ekranında rıza EVET, geçiş/ödüllü hazır;
  reklamları kapat → ana ekranda banner kayboldu; hata günlüğü yeni biçimde açıldı.
