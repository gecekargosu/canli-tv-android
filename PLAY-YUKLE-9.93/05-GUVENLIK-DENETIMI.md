# Yayın öncesi teknik ve güvenlik denetimi (9.93)

Tarih: 2026-11-26 · Paket: `com.cengiz.canlitv` · Sürüm: 9.93 (kod 102)

Bu dosya, yayın öncesi kontrol listesinin **kodda ne durumda olduğunu** kanıtıyla yazar.
✔ = var ve doğrulandı · ◐ = kısmen · ✖ = yok · 🔑 = senden bilgi/anahtar gerekiyor

## 1. Güvenlik ve sahiplik

| Madde | Durum | Kanıt / not |
|---|---|---|
| R8 / kod gizleme | ✔ | `app/build.gradle`: `minifyEnabled true`, `shrinkResources true`, `proguard-android-optimize.txt` |
| APK imza doğrulaması | ✔ | `Guard.java` (9.92): Play dışı kurulumda sertifika SHA-256 eşleşmezse uygulama açılmaz |
| Play Integrity API | 🔑 | İstemci+Worker kodu hazır değil; **Cloud project number** gerekiyor (Play Console → App integrity → Play Integrity → Enable) |
| Sunucu taraflı lisans/abonelik kontrolü | 🔑 | `/api/sub` ucu Play Developer API servis hesabı bekliyor (`KURULUM-SERVISLER.md` ①) |
| API anahtarlarını APK'dan çıkarma | ✔ | `src/` + `assets/` tarandı: gömülü API anahtarı/parola yok. AdMob ID'leri ve site adresi **herkese açık** değerlerdir (gizli değil). Resend/Google/LOG_KEY yalnızca Worker secret'larında |
| Yönetici panelini koruma | ✔ | `/api/logs?key=…` → `LOG_KEY` yoksa 404, yanlışsa 401. `/api/log` 64 KB gövde sınırı + alan kısaltma |
| Yedekleme ve kurtarma | ✔ | `YEDEK/UMAY-MULTI-TV-9.9x-*/` + `.tar.gz` (kaynak + imza anahtarı + APK/AAB + sha256), `YEDEK-BILGI.txt` |

## 2. Google Play yayın güvenliği

| Madde | Durum | Not |
|---|---|---|
| Gizlilik politikası | ✔ | `site/public/gizlilik-politikasi.html` (yayında) |
| Data Safety formu | ◐ | Cevaplar `docs/play/PLAY-YAYIN-HAZIRLIK.md` içinde; **formu sen dolduruyorsun** |
| İzin gerekçeleri | ✔ | Depolama izinleri `maxSdkVersion=28`; `usesCleartextTraffic=false` + `network_security_config`; `exported` yalnız launcher |
| Reklam kimliği (AD_ID) | ✔ | Yalnız AdMob SDK kullanıyor; AD_ID izni Play'de beyan edildi |
| Hedef SDK | ✔ | `targetSdk 36` (Play 2026 şartını karşılıyor), `minSdk 26` |
| Ödüllü reklam doğrulaması | ✔ | 9.93: `ServerSideVerificationOptions` + Worker `/api/adssv` (ECDSA imza doğrulaması, D1 kaydı). Kripto testi geçti |
| Çökme/hata raporlama | ✔ | `CrashLog` (cihaz içi + kullanıcı izniyle `/api/log`), `Hata günlüğü` ekranı |
| Destek e-postası | ◐ | Mağaza künyesine `cengizkilic1980@gmail.com` girilmeli (Play Console işi) |
| Mağaza görselleri | ✔ | Ekran görüntüleri `docs/play/*.png`; ikon/feature graphic repoda |

## 3. Reklam ve gelir koruması

| Madde | Durum | Not |
|---|---|---|
| Hesap/birimlerin sahipliği | ✔ | Tüm birimler `ca-app-pub-7629051535337276` (senin AdMob hesabın) |
| Ödülün sunucuda doğrulanması | ✔ | `/api/adssv` (imza doğrulaması + `ad_rewards` tablosu). **AdMob konsolunda SSV URL'si girilmeli** |
| Sahte gösterim/tıklama koruması | ◐ | SSV sayesinde şüpheli hacim görünür; tıklama tarafı AdMob'un kendi filtresinde (bizde ek kod gerekmez) |
| Reklam yüklenmezse çökmeme | ✔ | `AdManager`: yükleme hatasında geri çekilme (30 sn → 5 dk), `AdobeFailure` yolları try/catch |
| Sıklık ve kullanıcı deneyimi | ✔ | Geçiş reklamı aralığı + ödüllü ile 15 dk reklamsız; `APP_OPEN_AD=false` |
| Gelir/gösterim takibi | ◐ | `AdStats` cihazda sayıyor, `Hata günlüğü`ne yazıyor; toplu rapor AdMob panelinde |

## 4. Hukuki / marka / lisans

| Madde | Durum | Not |
|---|---|---|
| Marka araştırması | 🔑 | "UMAY Multi TV" adı için TÜRKPATENT/AB araması senin yapman gereken hukuki adım |
| Logo/görsel sahipliği | ◐ | Tüm görseller projede üretildi (repo geçmişi kanıt); ayrıca belge istersen tarih damgalı arşiv alınabilir |
| Kaynak kodu + geçmiş yedeği | ✔ | GitHub `gecekargosu/canli-tv-android` + `YEDEK/` arşivleri + `git log` |
| 3. taraf lisansları | ✔ | Bağımlılıklar: AndroidX, Media3/ExoPlayer, AdMob, Billing (hepsi Apache-2.0 / Google SDK şartları). GPL/AGPL bileşen yok |
| İçerik yayın hakları | 🔴 | **En büyük risk.** Kanal/radyo akışları üçüncü tarafların resmî yayınlarına bağlanıyor; uygulama içerik barındırmıyor (deep-link/player). Yine de Play "izinsiz yayın" değerlendirmesi yapabilir → telif sahiplerinden izin ya da yalnızca resmî/izinli kaynak listesi politikası gerekir |
| Kullanım koşulları | ◐ | `site/public/kullanim-kosullari.html` var; mağaza künyesine link eklenmeli |

## 5. Yayın sonrası kontrol

| Madde | Durum | Not |
|---|---|---|
| Günlük hata raporları | ✔ | `/api/logs?key=…` ile D1'den okunuyor |
| Aktif kullanıcı/ sürüm takibi | 🔑 | Firebase Analytics (K-19) `google-services.json` bekliyor |
| Uzaktan özellik kapatma | ✖ | Tasarlandı, uygulanmadı: `/api/config` + istemci önbelleği (bir sonraki tur) |
| Sunucu/DB yedeği | ◐ | D1 Sites tarafında yönetiliyor; kritik tablolar (`app_logs`, `ad_rewards`) dışa aktarılabilir |
| Anormal davranış tespiti | ◐ | SSV kayıtları + `/api/log` hacmi; otomatik alarm yok |
| İzinsiz dağıtım takibi | ◐ | `Guard` + Play Integrity planı; web'de APK arama takibi manuel |

## Bu turda yapılan kod değişiklikleri (9.93)

1. `AdManager.loadRewarded`: ödüllü reklama `ServerSideVerificationOptions` (cihaz kimliği
   `custom_data`) eklendi → sunucu tarafında doğrulanabilir ödül.
2. `site/worker.js`: `GET /api/adssv` — Google imzasını ECDSA/P-256 ile doğrular
   (`https://www.gstatic.com/admob/reward/verifier-keys.json`), geçerli kaydı
   `ad_rewards` tablosuna yazar; imza geçmezse 403.
   DER→P1363 dönüşümü ve imza doğrulaması yerel testte **geçti**.

## Sende kalan 3 konsol adımı

1. AdMob → **Apps → UMAY Multi TV → Ad units → ödüllü birim → Server-side verification URL:**
   `https://umay-multi-tv-gq3zer.freebuff.site/api/adssv`
2. Play Console → **App integrity → Play Integrity API → Enable** → bana **Cloud project number**.
3. Mağaza künyesi: destek e-postası + gizlilik/kullanım linki
   (`https://umay-multi-tv-gq3zer.freebuff.site/gizlilik-politikasi`).
