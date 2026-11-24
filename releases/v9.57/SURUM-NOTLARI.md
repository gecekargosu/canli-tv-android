# UMAY Multi TV 9.57 — sürüm notları

versionCode **66** · versionName **9.57**

## 1. Ayarlar'da sürüm bilgisi görünür yerde

- Ayarlar ekranının başlık altı artık **"Sürüm 9.57 · dokunduğun satır açılır"** yazıyor;
  ekranı açar açmaz hangi sürümde olduğun görünüyor.
- "Hakkında" bölümündeki satır da **"📱 Sürüm 9.57"** olarak başlıkta sürümü taşıyor,
  dokununca ayrıntı penceresi açılıyor.
- Sürüm numarası derlemeden geliyor (`versionName`), elle yazılmıyor.

## 2. Radyo aramasında alakasız sonuçlar (M-15)

"ntv" yazınca **Kentville** (Kanada) gibi kelime ortasında eşleşen yabancı istasyonlar
geliyordu. Artık internet sonuçları yalnızca sorgu **kelime başında** geçiyorsa listeye
giriyor. "ntv" → NTV Radyo / ntv Live Audio; "Kentville" artık eşleşmiyor.

## 3. Tablet

- Tablet APK'sı: `UMAY-MULTI-TV-9.57-tablet.apk` (telefon APK'sıyla birebir aynı binary).
- Kopyalar `İndirilenler` klasörüne de bırakıldı:
  `UMAY-MULTI-TV-9.57-tablet.apk` ve `UMAY-MULTI-TV-9.57.apk`.
- **Not:** Tabletinde 9.29 **farklı bir imzayla** kurulu olduğu için üzerine kurulum
  reddedildi (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`). Android, imza değişince üzerine
  yazmaya izin vermez; eski sürüm kaldırılıp yenisi kurulmalı (eski sürümün ayarları
  favorileriyle birlikte silinir).

## Kapasite denetimi (zorunlu adım)

| Ölçüm | 9.56 | 9.57 | Fark |
|---|---|---|---|
| APK | 7.112.417 B | 7.112.773 B | +356 B |
| dosya | 400 | 400 | 0 |
| sınıf | 17.504 | 17.504 | 0 |
| imza | f3996c67… | f3996c67… | aynı |

## Doğrulama

| Ne | Sonuç |
|---|---|
| Kapasite denetimi | ✅ kayıp yok |
| Telefon kurulumu | ⏳ telefon bu oturumda bağlı değil |
| Tablet kurulumu | ⏳ imza çakışması — kaldırıp kurulum gerekli |
| Ayarlar'da sürüm | ⏳ kurulumdan sonra görülecek |
