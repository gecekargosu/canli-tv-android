# 9.90 denetimi — Play mühendisi / yazılım mühendisi / kullanıcı gözü

Tarih: 26 Kasım 2026 · Sürüm: **9.90 (versionCode 99)** · Paket: `com.cengiz.canlitv`

## 1) Play Store mühendisi gözüyle

| Kontrol | Durum | Not |
|---|---|---|
| targetSdk / minSdk | ✅ 36 / 26 | Play zorunluluğu karşılanıyor |
| 64-bit | ✅ | APK'da native kütüphane yok, dex-only |
| İzinler | ✅ | INTERNET, ACCESS_NETWORK_STATE, WAKE_LOCK, FOREGROUND_SERVICE(_MEDIA_PLAYBACK), POST_NOTIFICATIONS; depolama izinleri **maxSdkVersion=28** |
| Fotoğraf/video izni | ✅ yok | Kayıtlar MediaStore ile, READ_MEDIA_* istenmiyor |
| Cleartext trafik | ✅ kapalı | `network_security_config` yalnız yayıncı HTTP adreslerine istisna |
| Reklam beyanı | ✅ | AdMob banner + geçiş + ödüllü; uygulama açılış reklamı **kapalı** (`APP_OPEN_AD=false`) |
| Reklam sıklığı | ✅ | 3. zap / 180 sn; izlerken tam ekran reklam yok |
| IAP beyanı | ✅ Evet | `umay_reklamsiz_aylik`, 20 ₺/ay; `acknowledgePurchase` + `queryPurchasesAsync` |
| Hedef kitle | ✅ 13+ | Listede çocuk kanalı yok, `18+` metni yok |
| app-ads.txt | ✅ | `https://umay-multi-tv-gq3zer.freebuff.site/app-ads.txt` |
| Gizlilik / kullanım | ✅ | Yayında, abonelik bölümü güncel |
| İmza | ✅ | `f3996c67…`, upload-keystore yedekte |
| Sürüm tutarlılığı | ✅ | Tek kaynak `app/build.gradle`, manifestte sürüm yok |

**Riskler:** tek yayıncı hesabı + telifli kanallar (Play telif şikayeti riski kullanıcının kararı),
abonelik doğrulaması istemci tarafında, ülke listeleri dış kaynaklı (kırılabilir).

## 2) Yazılım mühendisi gözüyle

| Alan | Durum |
|---|---|
| R8 + shrink | ✅ APK 3,85 MB (7,38 → %48 küçük) |
| Hata sınıflandırma (TV) | ✅ canlı kenar / ağ / kalıcı ayrımı, 2-4-8 sn retry |
| Yedek kaynak (TV) | ✅ 25 kanal / 94 doğrulanmış adres, otomatik sıralı deneme |
| Yedek kaynak (radyo) | ✅ 13 istasyon / 19 adres |
| Radyo yeniden bağlanma | ✅ 9.90: sessiz, artan aralıkla ~10 dk; istasyon **kendiliğinden değişmez** |
| Radyo listesi | ✅ büyük istasyonlar önce (TRT FM, TRT Radyo 1, Kral, Süper, Metro, Joy…) |
| CrashLog | ✅ 512 KB döndürme, event kaydı |
| i18n | ⚠️ kısmi: `Lang.t` 35 çağrı, ~80 sabit Türkçe metin |
| Liste performansı | ⚠️ RecyclerView dönüşümü yok (BaseAdapter + convertView) |

## 3) Kullanıcı gözüyle (cihaz testi)

| Senaryo | Sonuç |
|---|---|
| TR — TV listesi | ✅ 120 kanal, numara + isim, harf rozeti kaldırıldı |
| TR — TRT 1 / TRT 2 | ✅ video ilerliyor, ses kanalı açık |
| TR — TRT FM radyo | ✅ "Bağlantı iyi · çalıyor", ekolayzır hareket ediyor, veri akıyor |
| TR — radyo sırası | ✅ TRT FM ilk sırada |
| US listesi | ✅ 290 kanal, https 260, örnek 15/15 canlı |
| DE / GB / FR / NL | ✅ 120 / 115 / 120 / 119 kanal, örnek 13-15/15 canlı |
| Radyo kesinti davranışı | ✅ reklam/kesinti sonrası otomatik dönüş, istasyon değişmiyor |

## 4) Bu turda düzeltilen hatalar

1. Radyo, 3 başarısız denemeden sonra **kendiliğinden başka istasyona geçiyordu** → kaldırıldı.
2. Kesintide 15 sn sessizlik + "yayın açılamadı" → artık 1,2/2,4/3,6/4/8 sn artan aralıkla sürekli deneme.
3. 2 kez açılmayan istasyon listeden **siliniyordu** (TRT Radyo 1 kayboluyordu) → kaldırıldı.
4. Radyo sırası rastgeleydi → büyük/ulusal istasyonlar başa alındı.
5. Harf rozetleri (A/T/S) kaldırıldı.
6. 13 ölü TV adresi doğrulanmış kaynaklarla değiştirildi.
7. TV ve radyo için yedek adres dosyaları (`backup_tr.json`, `backup_radio.json`) eklendi.

## 5) Açık maddeler (bilinçli)

- **K-16** RecyclerView dönüşümü (liste dokunma/taşıma mantığı yeniden yazılmalı).
- **P1-4** tam çeviri (~80 sabit metin).
- **P1-6** sunucu taraflı abonelik doğrulama (sunucu yok).
- **K-18–K-20** tablet/yatay test, Firebase Analytics, mediation.
