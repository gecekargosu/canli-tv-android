# Play Kontrol Raporu → Kod Karşılaştırması ve İş Listesi (25 Kasım 2026)

**Kaynak:** `UMAY Multi TV v9.87 — Play Kontrol Raporu ve Öncelikli İş Listesi.pdf` (8 sayfa, 25 Kasım 2026)
**Karşılaştırma:** v9.87 kaynak kodu (`src/com/cengiz/canlitv/`), `assets/channels.m3u`, `assets/radios.m3u`, `site/public/`, `docs/play/`.
**Yöntem:** Rapordaki her madde tek tek kodda arandı. Rapor uygulamayı çalıştırmamış, canlı siteyi görmemiş; cihaz davranışı bizim 9.87 test raporundan alınmış.

Durum: ✅ doğru · 🟡 kısmen doğru / eksik · ❌ yanlış

---

## 1. Doğrulama tablosu (rapor iddiası → kod kanıtı)

| # | Rapor iddiası | Kodda bulunan | Durum |
|---|---|---|---|
| P0-1 | Abonelik var ama belgeler "uygulama içi satın alma yok" diyor | `app/build.gradle:103` `billing:6.2.1`, `Prefs.subActive`, `SettingsActivity:124` "Aylık 20 ₺". `docs/play/magaza-kaydi.md:101` "Uygulama içi satın alma? **Hayır**". Gizlilik/kullanım koşullarında "abonelik" kelimesi **0** kez geçiyor | ✅ |
| P0-2 | "Oynatma reklamla kesilmez" yazıyor, 15 dk'da tam ekran reklam çıkıyor | `Ads.AD_INTERVAL_MS = 15 dk`; `PlayerActivity:509` her saniye `AdManager.maybeTimerAd()`; uyarı yok. `magaza-kaydi.md:73` "oynatma reklamla kesilmez" | ✅ (çelişki gerçek) |
| P0-3 | Üç ayrı yaş beyanı + 5 çocuk kanalı | `magaza-kaydi.md:102` 18+, `PLAY-YUKLEME-9.87.md:32` 18+, `gizlilik-politikasi.html:99` 13+, `kapali-test.md:27` 13+, `AboutActivity:164` 13+. Listede tam **5**: Minika Çocuk, Minika Go, TRT Çocuk, TRT Diyanet Çocuk, Zarok TV (Zarok "Genel" grubunda) | ✅ |
| P0-4 | 6 kanal `app=` parametresiyle çekiliyor | `channels.m3u` satır 10, 12, 14, 20, 28, 108: Star (`app=<uuid>&sid=…`), Kanal D (`app=kanald_web`), Show (`app=<uuid>`), NTV (`app=ntv_web`), TRT Spor Yıldız (`app=web&platform=trtspor`), HT Spor (`app=web`) | ✅ |
| P0-5 | 106 logo marka logosu (92 imgur, 14 Wikipedia) | 122 logonun 92'si `i.imgur.com`, 14'ü `upload.wikimedia.org` = **106** | ✅ |
| P0-6 | Site sayfaları + app-ads.txt doğrulanmalı | `site/public/` içinde 4 sayfa + `app-ads.txt` var, içeriği `google.com, pub-7629051535337276, DIRECT, …`. Worker uzantısız yolu `env.ASSETS`'e bırakıyor — **canlıda test edilmedi** | 🟡 |
| P1-1 | Hata sınıflandırması yok, `BEHIND_LIVE_WINDOW` ayrımı yok, addFail kanalı gizliyor | `PlayerActivity:447` → doğrudan `onPError()`. `BEHIND_LIVE_WINDOW` için özel dal **yok**. Sıra: aynı adres 3× (5 sn), sonra yedek kaynak, sonra `addFail`. İnternet/DNS hatasında `addFail` **yok** (doğru davranış) | ✅ (24 saat kısmı hariç) |
| P1-1b | "addFail 24 saat sonra sıfırlansın" | `Prefs.FAIL_TTL_MS = 3 gün`; sayaç hiç sıfırlanmıyor değil, 3 gün sonra yok sayılıyor | 🟡 (zaten var, süre 3 gün) |
| P1-2 | Çoklu ekranda durum rozeti yok, sessiz pencereler tam kalitede | `MultiActivity:603` "yeniden bağlanıyor…" etiketi + pencere başına yeniden deneme var; **"Tekrar dene" katmanı ve rozet yok**. `Quality` yalnız kullanıcı tercihine göre `setMaxVideoSize` uyguluyor; sessiz pencere için tavan yok | 🟡 |
| P1-3 | Kanal açılışı yavaş, 8 sn ipucu / 20 sn yedek yok | `giveUpIfDead()` 45 sn'de ölü sayıyor; `onReady()` CrashLog'a "hazır: <kanal>" yazıyor ama **süre (ms) yazmıyor**; 8 sn ipucu, 20 sn yedek yok | ✅ |
| P1-4 | ~53 sabit Türkçe metin, `Lang.t` 20 yerde | Sabit metin eşleşmesi **87** (rapordan fazla); `Lang.t` **20** kullanım. `PlayerActivity` 0, `MultiActivity` 0, `MainActivity` 1. Mağaza "4 DİL" diyor | ✅ (daha kötü) |
| P1-4b | "50 ülkenin listesi" gerçek sayıyı yansıtmıyor | `Countries.LIST` = **55** ülke, `site/public/good/` = 55 dosya, **≥30 çalışan kanalı olan 31 ülke** | ✅ |
| P1-5 | Kök manifest 9.74/82 → eski versionCode ile derlenir, Play reddeder | Kök `AndroidManifest.xml` gerçekten 82/9.74; **ama Gradle `defaultConfig` (96/9.87) manifest değerini eziyor** — üretilen AAB/APK 96/9.87 doğrulandı (`verify-apk`). Play reddi riski **yok**; yalnız hijyen sorunu | ❌ (sonuç yanlış) |
| P1-6 | Abonelik doğrulaması yalnız istemcide | `Billing:190` `acknowledgePurchase`, `queryPurchasesAsync(SUBS)` ile tazeleme; karar `Prefs.subActive` (cihazda) | ✅ |
| P1-7 | Radyo 9.87'de ExoPlayer, yeniden test gerek | `AudioService` Media3 ExoPlayer; cihazda 3-4 istasyon doğrulandı, 25'in tamamı değil. 8 HTTP radyonun **hepsi** `network_security_config.xml` istisnasında (streamtheworld / radyotvonline / arabeskinmerkezi + 2 IP) | ✅ |

**Raporun kaçırdığı ek bulgu:** `AdManager` sınıf yorumu hâlâ "her kanal değişiminde bir + izlerken 15 dakikada bir" diyor; kod 3. zap / 180 sn. Kod doğru, yorum eski.

---

## 2. İş listesi

### P0 — 9.88'e girmeden Play'e gönderme (bugün/yarın)

| ID | İş | Dosya | Efor | Kabul |
|---|---|---|---|---|
| **K-01** | Abonelik hikâyesini tek hâle getir: mağaza formu "Uygulama içi satın alma: **Evet** (aylık reklamsız abonelik)", gizlilik + kullanım koşullarına abonelik maddesi (Google Play ödemesi, kart bilgisi uygulamaya girmez, iptal: Play Store → Abonelikler), mağaza metnine tek cümle | `docs/play/magaza-kaydi.md`, `PLAY-YUKLEME-9.87.md`, `site/public/gizlilik-politikasi.html`, `kullanim-kosullari.html` | 1 sa | Console cevabı = politika = mağaza metni = uygulama |
| **K-02** | Zamanlayıcı reklamı kaldır (öneri) ya da "Reklam geliyor" 5 sn bildirimi + metni dürüstleştir. Kural kalsın: 3. zap + 180 sn | `AdManager.maybeTimerAd()`, `PlayerActivity:509`, `Ads.AD_INTERVAL_MS` | 30 dk | 30 dk kesintisiz izlemede habersiz tam ekran reklam yok |
| **K-03** | Hedef kitleyi tek yerde **13+** yap; 5 çocuk kanalını listeden çıkar (122 kanal); politikadan "aile/çocuk kanalları listelenebilir" cümlesini sil | `channels.m3u`, `good_tr.json`, `magaza-kaydi.md`, `PLAY-YUKLEME-9.87.md`, `gizlilik-politikasi.html`, `index.html` | 45 dk | Listede çocuk kanalı 0, üç belgede 13+ |
| **K-04** | 6 parametreli kanal: parametresiz resmî/yansı adres ara; yoksa çıkar veya yayıncıdan yazılı izin. Her sürümde bu 6'sını otomatik dene | `channels.m3u`, `ChannelRepo.fallbacks` | 1–2 sa + karar | Parametresiz çalışan adres ya da listeden çıkış |
| **K-05** | Logoları kaldır: kanal adının baş harfi rozeti (izinli kanalda logo kalabilir) | `LogoLoader`, `tvg-logo` alanları | 1 sa | 106 dış logo → 0 |
| **K-06** | Canlı doğrulama: `/gizlilik-politikasi`, `/kullanim-kosullari`, `/telif-bildirimi`, `/app-ads.txt` telefonda açılıyor mu; `/indir` 9.87'yi gösteriyor mu; AdMob app-ads durumu | `site/worker.js`, `site/public/` | 30 dk | 4 sayfa + app-ads 200, indir 9.87 |

### P1 — 14 günlük kapalı test sırasında

| ID | İş | Dosya | Kabul |
|---|---|---|---|
| **K-07** | Hata sınıflandırması: `ERROR_CODE_BEHIND_LIVE_WINDOW` → `seekToDefaultPosition()+prepare()` (sayaç artmaz); IO timeout/failed → 2-4-8 sn backoff, en çok 3; yalnız 403/404/biçim → yedek kaynak → `addFail`. `FAIL_TTL_MS` 3 gün → 24 saat | `PlayerActivity.onPError`, `Prefs.FAIL_TTL_MS` | Canlı pencere hatası kanalı öldürmüyor |
| **K-08** | Çoklu ekran: pencere başına "Yükleniyor / Açılamadı · Tekrar dene" katmanı + durum rozeti; sesi kapalı pencerelere `setMaxVideoSize(640,360)`; 4'lü modda çözücü uyarısı | `MultiActivity`, `Quality` | Her pencerede durum görünüyor; sessiz pencere düşük çözünürlük |
| **K-09** | Açılış süresi ölçümü: 8 sn'de "yayın yavaş açılıyor" ipucu, 20 sn'de yedek adres, `CrashLog`'a `ms` | `PlayerActivity` | Hangi kanal kaç sn'de açıldı veriyle görünüyor |
| **K-10** | Sabit metinleri `Lang.t`'ye taşı (öncelik TR+EN, en görünür 30 metin). Tamamlanana kadar mağaza metninde "Arayüz: Türkçe; diğer diller kısmen" | `PlayerActivity`, `MainActivity`, `MultiActivity`, `AdManager`, `Lang` | Görünür akışta sabit metin kalmadı |
| **K-11** | Mağaza metnindeki "50 ülkenin listesi" → "55 ülke listesi, 31 ülkede 30+ kanal"; TRT/TBMM/Diyanet yerine "kamu yayıncıları" | `magaza-kaydi.md` | Sayı gerçekle aynı |
| **K-12** | Radyo dayanıklılık testi: 25 istasyonun tamamı + zayıf 4G'de 3 istasyon 10 dk + ekran kilitli 10 dk veri artışı | cihaz | Çalmayan istasyon 0 |

### P2 — Yayından sonra

| ID | İş |
|---|---|
| **K-13** | Sürüm tek kaynak: kök `AndroidManifest.xml`'den `versionCode`/`versionName` kaldır (şu an zararsız — Gradle eziyor, ama iki yerde tutmak karışıklık üretiyor) |
| **K-14** | `AdManager` sınıf yorumunu gerçek kuralla (3. zap / 180 sn) güncelle |
| **K-15** | R8 + `shrinkResources` (keep: Billing, UMP, AdMob, Media3) — APK ~%25 küçülür |
| **K-16** | `ListView` → `RecyclerView`, logo yüklemesini görünür alanla sınırla |
| **K-17** | `CrashLog` 512 KB'ta döndür; otomatik duman testi (17 adım betiği) |
| **K-18** | Android 14/15, tablet ve yatay düzen testi |
| **K-19** | Firebase Analytics (rıza ile) veya Play Console + AdMob raporları: D1/D7 tutundurma, kanal açılış başarısı |
| **K-20** | Mediation, liste içi native reklam, yurtdışı IP ile kanal testi |

---

## 3. Raporda yanlış / eksik bulduklarım

1. **P1-5 yanlış sonuç:** Kök manifest 82/9.74 olsa da Gradle `defaultConfig` değerleri manifesti eziyor; üretilen AAB/APK **96 / 9.87** ve imza doğrulandı. Play reddi riski yok — iş hijyen (K-13), P0 değil.
2. **"addFail 24 saat sonra sıfırlansın":** Sayaç zaten sıfırlanıyor sayılır — `FAIL_TTL_MS` 3 gün. 24 saate çekmek yeterli (K-07).
3. **"~53 sabit metin":** Gerçek sayı daha yüksek (~87 eşleşme); sorun rapordan büyük.
4. **Rapor "5 çocuk kanalı" diyor — doğru**, ama Zarok TV "Genel" grubunda olduğu için grup filtresiyle görünmüyor; çıkarırken grup adına değil kanal adına bakılmalı.
5. **Rapor HTTP riskini "2 kanal" diyor — doğru ve kapsanmış:** `1.rtmp.org` ve `m.live.net.sa` cleartext istisnasında; 8 HTTP radyonun tamamı da istisnada. Ek iş yok.
6. **Rapor abonelik fiyatını "20 ₺/ay" diyor — doğru** (`SettingsActivity:125` "Aylık 20 ₺"), ürün kimliği `umay_reklamsiz_aylik`.

---

## 4. Önerilen sürüm planı

| Sürüm | İçerik |
|---|---|
| **9.88** | K-01…K-06 (metin + form + 5 çocuk kanalı + 6 parametreli kanal kararı + logo) → AAB, `verify-apk --expect-version 9.88` |
| **9.89** | K-07…K-09 (hata sınıflandırma, çoklu ekran durum katmanı, açılış ölçümü) |
| **9.90** | K-10, K-11 (dil ve mağaza metni tutarlılığı) |
| Yayın sonrası | K-13…K-20 |

**Not:** K-01…K-03 kod değil metin/veri işi; 9.88 AAB'sini yeniden üretmek için yalnız `channels.m3u` + `good_tr.json` değişir, Java kodu değişmez.

---

## 5. Uygulama durumu — 25 Kasım 2026 (kod yazıldı, **derleme yapılmadı**)

| ID | Durum | Ne yapıldı |
|---|---|---|
| K-01 | ✅ | `magaza-kaydi.md` + `PLAY-YUKLEME-9.87.md` (9.88 notu) abonelik beyanı, 13+, ödeme satırı; gizlilik/kullanım koşullarına abonelik bölümü |
| K-02 | ✅ | `AdManager.maybeTimerAd` ve `Ads.AD_INTERVAL_MS` silindi; `PlayerActivity` artık izlerken reklam istemiyor. Kural: 3. zap / 180 sn |
| K-03 | ✅ | 5 çocuk kanalı `channels.m3u` + `good_tr.json`'dan çıkarıldı (127 → 120); üç belgede 13+; politikadan "çocuk kanalları listelenebilir" cümlesi silindi |
| K-04 | ✅ | NTV parametresiz (`dogus.daioncdn.net/ntv`), Kanal D + Show TV TürkNet yansısı (canlı doğrulandı). Star TV, HT Spor, TRT Spor Yıldız parametresiz kaynak bulunamadığı için çıkarıldı; TRT Spor eklendi (kamu yayıncısı). `app=` kalan: **0** |
| K-05 | ✅ | `tvg-logo` ve `logo` alanları temizlendi (122 → 0 harici logo); arayüz zaten baş harf rozeti gösteriyor |
| K-06 | 🟡 | Canlı kontrol: `/`, `/gizlilik-politikasi`, `/kullanim-kosullari`, `/telif-bildirimi`, `/app-ads.txt` → hepsi **200**. `/indir` hâlâ 9.82. Yeni politika metinleri ve `/indir` için **site yayınlama** gerekiyor (senin onayın) |
| K-07 | ✅ | `onPlayerError` sınıflandırıldı: `BEHIND_LIVE_WINDOW` → canlı kenara atlama; ağ/timeout → 2-4-8 sn (3 deneme); diğerleri → yedek kaynak → `addFail`. `FAIL_TTL_MS` 24 saat |
| K-08 | ✅ | Çoklu ekranda pencere başına "Açılamadı · dokun → tekrar dene" katmanı; sesi kapalı pencereye 640×360 + 700 kbps tavan; çözücü sayısı 4'ten azsa uyarı |
| K-09 | ✅ | 8 sn "yavaş açılıyor" ipucu, 20 sn'de yedek kaynak, `CrashLog`'a açılış süresi (ms) |
| K-10 | 🟡 | 14 görünür metin `Lang.t`'ye taşındı (oynatıcı hataları, yeniden bağlanma, canlı kenar, çoklu ekran). Kalan ~70 sabit metin durüyor; mağaza metni "diller kısmen" olarak düzeltildi |
| K-11 | ✅ | "120 kanal", "55 ülke (31'inde 30+ kanal)"; TRT/TBMM/Diyanet marka adları mağaza metninden çıkarıldı |
| K-12 | 🟡 | 31 radyonun URL'si tarandı: 26 doğrudan, 4 yönlendirme → 200 ses, 1 ölü (Slow Türk) çıkarıldı, 1 yanlış adres (Süper Show) düzeltildi. **Cihazda 10 dk dinleme testi yapılmadı** |
| K-13 | ✅ | Kök `AndroidManifest.xml`'den `versionCode`/`versionName` kaldırıldı; tek kaynak `build.gradle` |
| K-14 | ✅ | `AdManager` sınıf açıklaması gerçek kuralla güncellendi |
| K-15 | ✅ | R8 + `shrinkResources` açıldı (`app/proguard-rules.pro`: Billing, AdMob/UMP, Media3 keep). APK 7,38 MB → **3,85 MB**, AAB 7,04 → **6,61 MB**. Cihazda açıldı, çökme yok; AdMob SDK başladı (NO_FILL beklenen) |
| K-16 | ⛔ | RecyclerView geçişi yapılmadı (liste dokunma/taşıma/seçim mantığının yeniden yazımı; yayın sonrasına bırakıldı) |
| K-17 | ✅ | `CrashLog` 512 KB'ta `crash.1.txt` / `events.1.txt` olarak döndürülüyor |
| K-18–K-20 | ⛔ | Cihaz/tablet testi, analitik ve mediation bu turun dışında (cihaz + derleme + yeni bağımlılık gerekiyor) |

### Doğrulamalar (bu turda çalıştırılanlar)

- `channels.m3u`: 120 kanal, `app=` 0, `tvg-logo` 0, çocuk kanalı 0.
- `good_tr.json`: 120 kayıt, geçerli JSON.
- Yeni 4 adresin hepsi canlı master playlist döndürüyor (curl).
- 31 radyo URL'si tarandı; kalan tüm adresler 200/302→200.
- Java dosyalarında süslü parantez/parantez dengesi: 0 sorun.
- `maybeTimerAd` / `AD_INTERVAL_MS` referansı: 0.

### Derleme ve cihaz sonucu (25 Kasım 2026, 23:20)

- `bash build.sh` → **ilk derleme hata verdi**: `PlayerActivity:511` anonim `Runnable` içinde
  `Lang.t(this, …)` (`this` = Runnable). `PlayerActivity.this` ile düzeltildi.
- İkinci derleme: **BUILD SUCCESSFUL**. R8 açık hâliyle üçüncü derleme de geçti.
- `verify-apk.py CanliTV.apk --expect-version 9.88` → 9.88 / kod 97, imza
  `f3996c67…`, kapasite **TEMİZ** (391 dosya, 6995 sınıf).
- `make-aab.sh` → `CanliTV.aab` **6.611.602 bayt**; `bundletool dump manifest` →
  `versionCode 97`, `versionName 9.88`, minSdk 26, targetSdk 36.
- Cihaz (Z5R87DU8V4LBSOVG): `adb install -r` başarılı, `MainActivity` açıldı,
  logcat'te `FATAL EXCEPTION` yok, `UmayAds: SDK hazir` görüldü.
- Çıktılar: `releases/v9.88/UMAY-MULTI-TV-9.88.aab` ve `CanliTV-9.88.apk`.

### Claude raporu A-Z doğrulama (9.88 derlemesi + cihaz)

| Rapor maddesi | Durum | Kanıt |
|---|---|---|
| P0-1 abonelik/belge çelişkisi | ✅ | `magaza-kaydi.md:103` IAP **Evet**; gizlilik + kullanım + index'te "Reklamsız abonelik" bölümü |
| P0-2 izlerken reklam | ✅ | `maybeTimerAd`/`AD_INTERVAL_MS` referansı **0**; kod yalnız 3. zap / 180 sn |
| P0-3 yaş + çocuk kanalı | ✅ | `18+` metni **0**; listede Minika/TRT Çocuk/Zarok **0** |
| P0-4 parametreli kanal | ✅ | `app=` **0** (NTV, Kanal D, Show parametresiz; Star/HT/TRT Yıldız çıktı) |
| P0-5 marka logosu | ✅ | `tvg-logo` **0**, `good_tr.json` logo dolu **0** |
| P0-6 site + app-ads | 🟡 | 4 sayfa + app-ads **200**; **canlı politika hâlâ eski metin**, `/indir` 9.82 → yayın bekliyor |
| P1-1 hata sınıflandırma | ✅ | `BEHIND_LIVE_WINDOW`, `isNetworkError`, `FAIL_TTL_MS=24sa` |
| P1-2 çoklu ekran | ✅ | `showFail` dokun-tekrar, `setMaxVideoSize(640,360)`, `warnIfFourPanesUnsafe` |
| P1-3 yavaş açılış | ✅ | 8 sn ipucu, 20 sn yedek, `hazır (N ms)` logu |
| P1-4 dil tutarlılığı | 🟡 | `Lang.t` 20 → **37**, sabit metin 87 → **80**; mağaza "diller kısmen" |
| P1-4b ülke sayısı | ✅ | "55 ülke (31'inde 30+ kanal)" |
| P1-5 sürüm çelişkisi | ✅ | Manifestte sürüm yok; AAB **97/9.88** (`bundletool`) |
| P1-6 Billing | 🟡 | `acknowledgePurchase` + `queryPurchasesAsync` var; doğrulama hâlâ istemcide (v1 kabul) |
| P1-7 radyo | ✅ | 31 URL erişilebilir; cihazda **TRT FM (AAC+) çalıyor** ("✅ Bağlantı iyi"), arka planda 30 sn'de +0,6 MB |
| P2 R8 | ✅ | APK 7,38 → **3,85 MB**, AAB 6,61 MB; cihazda açıldı, çökme yok |
| P2 CrashLog 512 KB | ✅ | `crash.1.txt` / `events.1.txt` döndürme |
| P2 RecyclerView, duman testi, sınıf bölme, tablet/yatay, analitik, mediation, yurtdışı IP | ⛔ | Yapılmadı (yayın sonrası / cihaz / yeni bağımlılık) |
| Mağaza görselleri | ⛔ | Eski ekran görüntüleri 9.87 listesini ve kanal logolarını gösteriyor; 9.88 için yeni görüntü alınmalı |

K-16 (RecyclerView) yapılmadı: `MainActivity` liste ekranının dokunma/taşıma/seçim
mantığını baştan yazmayı gerektiriyor ve yayın öncesi riski yüksek. Yayından sonra
tek başına ele alınmalı.
