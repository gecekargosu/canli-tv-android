# UMAY Multi TV — v9.8 (versionCode 17)

23 Kasım 2026 · `CanliTV-9.8.apk` · `CanliTV-9.8.aab`

## Düzeltmeler

### 📢 Reklam barı artık hiç boş kalmıyor
- AdMob reklam doldurmadığında (no-fill / internet yok / test cihazı) ekranın altında **boşluk** kalıyordu.
- Artık dolgu gelmezse yerine **kendi tanıtım barımız** geliyor: "UMAY Multi TV · 60 dk reklamsız izlemek
  için ★ menüsünden ödüllü videoyu aç". Yani radyo, TV, ayarlar, hesap — her ekranda alt bar **dolu**.
- Reklam gelirse bar otomatik olarak gerçek reklama dönüşür (aynı alan, ek yer kaplamaz).

### 📻 Açılmayan radyo istasyonu artık takılı bırakmıyor
- İstasyon 9 saniyede ses vermezse "açılmadı" diye işaretleniyor ve **otomatik sıradaki istasyona** geçiliyor
  (üst üste en fazla 3 kez; sonunda "bu ülkedeki istasyonlar açılmıyor, başka ülke dene" yazıyor).
- Örnek: **Radyo 7** gibi ölü kayıtlar artık ekranda "yayın bekleniyor" diye asılı kalmıyor.
- Radyo hatası sayaçları (`Prefs.fails`) TV ile ortak; 2 hatadan sonra kanal/istasyon "ölü" sayılıyor.

## Not: Pixel 7 / Android 13 testi (Appetize)
- Uygulama Pixel 7 · Android 13'te sorunsuz açıldı; kanal ve radyo listeleri, banner, veri satırı çalıştı.
- Cihaz seçimi kritik değil: Android 8+ aynı kodu çalıştırır; Pixel 7 temsili bir cihaz.
- Appetize ücretsiz planı **3 dakikalık oturum** sınırı koyuyor; "sürekli takılma" hissinin bir kısmı budur
  (oturum kesilir). Gerçek takılma sebebi ise yayın kaynağı + `VideoView`; çözüm ExoPlayer (iş listesi #39).
