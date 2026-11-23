# UMAY Multi TV v9.28 (versionCode 37)

Tarih: 23 Kasım 2026

## Bu sürümde yeni

### 1. Ayarlar → Reklam test modu (AdMob test cihazı)
Kendi telefonunda reklamları **test reklamı** olarak görmek için tek dokunuş:

- **Ayarlar → Reklam test modu** satırı → pencere açılır.
- Pencere cihazın **reklam kimliğini (Advertising ID)** gösterir; "Kimliği kopyala" ile panoya alır.
- "Testi aç" → uygulama bu cihazı AdMob test cihazı olarak tanır.
  Reklamlar açılır, ödüllü reklam ödülü çalışır, **para yazmaz ve geçersiz trafik/ban riski oluşmaz**.
- "Testi kapat" → gerçek reklamlar (gelir yazmaya başlar).

Ayrıca AdMob konsolundan kalıcı kayıt için aynı kimlik kullanılabilir:
**AdMob → Ayarlar → Test cihazları → Android → reklam kimliğini yapıştır.**

Teknik: `AdsTest` sınıfı `AdvertisingIdClient` ile kimliği okur, AdMob'un istediği
MD5 (büyük harf) özete çevirir; `Ads`/`AdManager` test modunda
`RequestConfiguration.setTestDeviceIds(...)` uygular. Ayar `Prefs.adTestMode` içinde tutulur.

### 2. Tablet
Aynı APK tabletlerde çalışır (minSdk 26, `resizeableActivity=true`, yoğunluk bağımsız arayüz).
Ana ekran dikey, oynatıcı döner. Tablet için ayrı derleme gerekmez.

## Doğrulama
- Test modu penceresi açıldı, reklam kimliği okundu, "Testi aç" → satır değeri **Açık** oldu.
- Uygulama yeniden başlatıldı, çökme yok, 144 kanal listelendi.
- Reklam sayacı: 18 gösterim ≈ 0,20 TL tahmini (test öncesi gerçek gösterimler).

## Not
Test modu açıkken **hiçbir gelir yazmaz**; mağaza/gerçek kullanım için kapatılmalı.
Hangi reklamın çıkacağını AdMob belirler; aynı reklamın tekrarı konsol ayarlarıyla
(bloklama/mediation) azaltılır, uygulamadan seçilemez.
