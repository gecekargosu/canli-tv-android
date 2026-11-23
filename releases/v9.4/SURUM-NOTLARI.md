# UMAY Multi TV — v9.4 (versionCode 13)

Tarih: 23 Kasım 2026 · `CanliTV-9.4.apk` · `CanliTV-9.4.aab`

## Kritik düzeltmeler

### 🔊 Radyo hiç çalmıyordu (v9.1'den beri)
- Sebep: istasyonu başlatan `show()` çağrısı yanlışlıkla **40 dakikalık ödüllü teklif**
  metodunun sonuna kaymıştı; radyo ekranı açılınca `AudioService.start()` hiç çağrılmıyordu.
- Düzeltme: ekran açılır açılmaz istasyon başlatılıyor. Doğrulama: `UmayRadio: deneme 0:
  https://anadolu.liderhost.com.tr:10853/` + ekranda **"çalıyor · ekolayzır aktif"**.

### 🎚 Ekolayzır hareket etmiyordu
- Sebep: aynı hata (animasyon döngüsü hiç başlamıyordu) + çubuklar sesten bağımsız çiziliyordu.
- Düzeltme: animasyon ekranla birlikte başlıyor ve **yalnızca ses gerçekten çalarken** hareket ediyor;
  ses yoksa çubuklar düz kalıyor (yanlış "çalıyor" izlenimi yok).

### 👤 Üyelik: doğrulama sonrası ana sayfaya dönmüyordu / "Misafir" görünüyordu
- Sebep 1: doğrulama başarılı olunca ekran kapanmıyordu (kullanıcı geri tuşuna basıyordu).
- Sebep 2: Ayarlar ekranı hesap durumunu yalnızca açılışta okuyordu; durum değişince yenilenmiyordu.
- Düzeltme: doğrulama ve giriş başarılı olunca **otomatik ana sayfaya dönüş** + Ayarlar ekranı
  hesap değişince kendini yeniliyor.

## Test (cihaz Z5R87DU8V4LBSOVG)
| Test | Sonuç |
|---|---|
Radyo aç → çalıyor mu | ✅ `isPlaying=true`, ekranda "çalıyor · ekolayzır aktif" |
Ekolayzır çubukları | ✅ ses varken hareket ediyor, yoksa düz |
Kanal listesi / reklamlar | ✅ bozulmadı |
Derleme + kurulum | ✅ |

## Notlar
- Google ile giriş hâlâ kapalı: `GOOGLE_CLIENT_ID` + `GOOGLE_CLIENT_SECRET` gerekli.
- E-posta gönderimi açık ama gönderen `onboarding@resend.dev` olduğu için şimdilik
  yalnızca hesap sahibinin adresine gidiyor (domain gerekli).
