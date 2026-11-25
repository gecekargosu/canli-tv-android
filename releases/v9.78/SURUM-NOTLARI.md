# UMAY Multi TV 9.78 (versionCode 87)

Tarih: 25 Kasım 2026

## Ekran / kullanım

- **Üst bar yeniden düzenlendi:** logo 46dp, "UMAY Multi TV" 19sp; altta 5 büyük düğme
  (3'lü · radyo · **kayıt** · bayrak · ayarlar) tam genişliğe eşit dağıtıldı. Önceki sürümde
  başlık "UMAY…" diye kırpılıyor, düğmeler sağa sıkışıp sol tarafta boşluk bırakıyordu.
- **Kayıt girişi eklendi (⏺):** alt başlıkta "kayıt" yazıyordu ama açacak düğme yoktu → Kayıtlarım.
- **Kalite seçimi ana ekrana geldi:** yeni satır açmadan sekme satırında `🎚 Oto / 360p / 720p`.
- **Sıra:** sekmeler (Tümü · Favoriler · Son · Gizli) üstte, **arama + No + Git listenin hemen üstünde**.
- Yazılar büyütüldü: arama 15sp, No 58dp, sekmeler 11,5sp, kalem 15sp. "Gizlenenler" → "Gizli" (kırpılıyordu).
- Logoya hafif parlama animasyonu (1,7 sn, alpha 1→0,72).
- **Radyo ekranı sola kayıyordu:** kök görünümdeki `setFitsSystemWindows(true)` 12dp yan boşluğu
  sıfırlıyor, içerik ekran kenarına yapışıyordu. Kaldırıldı.

## Kanal / yayın

- **Show TV açılmıyordu.** Kök neden: TürkNet yansısının varyant listesi boş dönüyordu
  (master 200, `showtv_*.m3u8` 0 bayt) → oynatıcı sonsuz "yükleniyor" çemberinde kalıyordu.
  Birincil kaynak showtv.com.tr'nin kendi sitesinde gömülü Daion adresine alındı;
  TürkNet yansısı (master + 720p) yedek olarak eklendi. Cihazda 360p oynadı.
- Eski kaynağı önbelleğinde tutan cihazlar da `ChannelRepo` yedeğiyle yeni adrese geçiyor.

## Reklam

- Alt reklam yuvası dört ekranda da (ana sayfa, tekil, çoklu, radyo) sabit ve her zaman ayrılmış
  durumda; reklam dolmadığında yuva boş kalmıyor, turuncu "Reklam izle · 15 dk reklamsız"
  şeridi duruyor. Ödüllü reklam ayrı: kullanıcı dokununca 15 dakika reklamsız.
- Sıklık politikası değişmedi: her 3. kanal değişiminde + en az 180 sn arayla geçiş reklamı,
  ilk açılışta yok, 15 dakikada bir zamanlanmış reklam.

## Doğrulama (cihaz, Z5R87DU8V4LBSOVG)

- Show TV 360p açıldı; veri şeridi 313,4 → 316,9 MB.
- Radyo: 318,3 MB, düzen 12dp içeride.
- 3'lü ekran (TRT Haber + ATV + TRT 1): 318,3 → 324,0 MB canlı artıyor, çıkışta korunuyor.
- Kapasite: 400 dosya · 17.53x sınıf · imza `f3996c67…b8d5`.

## Paket

- `CanliTV-9.78.apk`, `UMAY-MULTI-TV-9.78.aab` (Play yüklemesi için AAB).
