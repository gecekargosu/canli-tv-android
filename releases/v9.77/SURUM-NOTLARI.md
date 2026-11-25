# UMAY Multi TV 9.77 (versionCode 86)

Tarih: 25 Kasım 2026

## Yeni / değişen

- **Sırala kaldırıldı.** Liste sırası zaten numaraya göre sabit; düğme bir işe yaramıyordu.
- **Düzenle** düğmesi sekme satırının sonuna taşındı (kalem simgesi). Üstteki ayrı
  "Düzenle / Sırala" satırı kalktı, liste bir satır daha yukarıdan başlıyor.
- **Kanal sayısı alt şeritte:** `📶 Bugün kullanılan veri · limit · kalite · 📺 127 kanal`.
  Sayı arama/filtre ile birlikte güncellenir; üst başlıkta tekrar etmez.
- **"Reklam izle · 15 dk reklamsız" şeridi** sarıdan uygulamanın turuncusuna (#FF7A18) alındı.
- **Test kanalı ("Test Alfa")** kendi kanal listesinden kaldırıldı.

## Veri sayacı düzeltmeleri (önemli)

- **Çoklu ekranda veri iki kez sayılıyordu:** hem 3 saniyelik izleyici (TrafficStats)
  hem de çıkıştaki oturum sayacı aynı baytları yazıyordu. Tek kaynağa indirildi.
- **Çoklu ekranda sayaç hiç artmıyordu:** cihazda `TrafficStats.getUidRxBytes` 0 dönüyor
  (Android 8+ çoğu cihazda uid istatistiği kaldırıldı). Pencereler artık **ExoPlayer** ile
  oynuyor ve baytlar `CountingDataSource` ile tek tek sayılıyor — tek oynatıcıyla aynı yöntem.
- Kanalların kendi satırındaki tüketim (ör. `ATV · 61,7 MB (7 dk)`) çoklu ekranda da artık işliyor.
- Doğrulama (cihazda):
  - Tek kanal TV8 720p, 90 sn → 273,5 → 289,8 MB (+16,3 MB).
  - 3'lü ekran (TRT Haber + ATV + TRT 1), 20 sn → 289,8 → 302,8 MB (+13 MB), ekran canlı artıyor.

## Play uyumu

- `targetSdk 36` (Android 16) — 31 Ağustos 2026'dan beri yeni uygulama/güncelleme şartı.
- İzinler: INTERNET, ACCESS_NETWORK_STATE, WAKE_LOCK, FOREGROUND_SERVICE(+MEDIA_PLAYBACK),
  POST_NOTIFICATIONS, eski sürümler için depolama (maxSdk 28). Fazladan izin yok.
- Açık metin (http) trafiği varsayılan olarak kapalı; yalnız eski yayın alan adları istisna.

## Paket

- `CanliTV-9.77.apk` — 7.131.257 bayt, imza `f3996c67…b8d5`
- `UMAY-MULTI-TV-9.77.aab` — 6.787.551 bayt (Play yüklemesi için bunu kullan)
- Kapasite denetimi: 400 dosya, 17.528 sınıf, sınıf kaybı yok.
