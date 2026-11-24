# UMAY Multi TV — MASTER İŞ LİSTESİ

**Sürüm:** v9.32 (versionCode 41) · **Tarih:** 24 Kasım 2026
**Kaynaklar:** Claude "Derin Analiz ve Yol Haritası (v9.16)" + ChatGPT "v9.29 denetimi" + bizim iş listesi (#1–#156)
**Kural:** Bu dosya tek yetkili listedir. Eski README/iş listeleri tarihseldir; çelişkide **kaynak kod** ve bu liste geçerlidir.

Durum işaretleri: ✅ bitti · 🔧 kod işi · 👤 kullanıcı/hesap işi · 🧪 cihaz testi gerekli · ⏸️ askıda (senin isteğinle)

---

## 0. Durum özeti (v9.32)

- Kanal: 145 TR kanalı (TRT Haber eklendi), sunucu allowlist `/good/<cc>.json`.
- Oynatıcı: tekli TV = Media3 ExoPlayer; **çoklu ekran, radyo, arka plan sesi = MediaPlayer** (yarım geçiş).
- Veri sayacı: oynatıcı bayt sayımı (v9.32) — ekran/ana ekran/Ayarlar aynı. Radyo ve arka plan sesi **sayılmıyor**.
- Reklam: AdMob banner/geçiş/ödüllü + UMP var; **banner UMP kapısından geçmiyor**.
- Hesap/üyelik: v9.21'de kaldırıldı ✅ (Claude P0-4 / ChatGPT 3.5 kapandı).
- Ses servisi: Durdur düğmesi, audio focus, noisy, izin istekleri düzeltildi ✅ (Claude P0-5 a–d, i).
- Manifest: `allowBackup=false`, `supportsRtl=true` ✅; `usesCleartextTraffic=true` ❌; portre kilidi ❌.
- Gizlilik politikası: hâlâ "reklam yok / sunucuya veri gitmez" diyor ❌ (satır 49), aynı belgede AdMob anlatılıyor (satır 90).
- **`.secrets/` klasörü hâlâ projede ve v9.32 zip'ine girdi** (resend.key). Bu turda zip yeniden üretildi; anahtarın yenilenmesi sende.

---

## FAZ 0 — ACİL GÜVENLİK & UYUMLULUK (Play'den önce şart)

| No | İş | Kaynak | Durum |
|---|---|---|---|
| M-01 | **Resend API anahtarını yenile/iptal et.** `.secrets/resend.key` v9.32 zip'inde dışarı çıktı. Yeni zip gizli dosyasız üretildi ama eski zip'i paylaştıysan anahtar yanmış sayılır. | ChatGPT P0-2 | 👤 (bekliyor) |
| M-02 | `.secrets/` klasörünü proje dışına taşı; zip/build scriptlerine istisna ekle (kalıcı). | ChatGPT P0-2 | ✅ v9.33 (`tools/claude-zip.sh`) |
| M-03 | **Banner'ı UMP kapısına bağla:** `canRequestAds()` false/`Ads.SDK_READY` yokken `loadAd` çağrılmasın; `startAds()` içindeki fail-open (`canRequestAds = true`) kaldırılsın. | ChatGPT P0-1 / Claude P0-3 | ✅ v9.33 (cihaz testi bekliyor) |
| M-04 | **Gizlilik politikası gerçeğe göre yeniden yaz:** tüm dosyalar (docs/play, site/public, index) tek kaynaktan üretilsin; "reklam yok" ve "sunucuya veri gitmez" ifadeleri silinsin; AdMob/ad kimliği/Cloudflare/üçüncü taraf yayıncılar yazılsın. | Claude P0-2 / ChatGPT 3.5 | ✅ v9.33 |
| M-05 | **ipapi.co çağrısını kaldır**, yerine `TelephonyManager` SIM/network ülke kodu + `Locale` kullan (izin ve ağ isteği yok). | Claude 3.3 | ✅ v9.33 |
| M-06 | **İmza stratejisi:** siteden dağıtılan `publicApk` debug anahtarıyla imzalı; Play AAB release anahtarıyla. Tek üretim anahtarı kullan (Play App Signing'e geçince yükleme anahtarıyla imzala), sitedeki APK'yı aynı anahtarla üret. | ChatGPT P0-4 | ✅ v9.33 (eski kurulum kaldırılmalı) |
| M-07 | **Keystore şifresi:** `app/build.gradle`'da düz metin kalmasın; `keystore.properties`/ortam değişkeni; şifre rotasyonu (Play App Signing ile upload key reset). | Claude P0-7 | 👤 (şifre) |
| M-08 | **`usesCleartextTraffic`:** varsayılan HTTPS; yalnız açıkça işaretli HTTP kaynaklar için `networkSecurityConfig` istisnası. TR listesinde 2 HTTP TV + 9 HTTP radyo var. | ChatGPT 8.3 | ✅ v9.33 |
| M-09 | **Play Veri Güvenliği formu** gerçek davranışla doldurulsun (reklam kimliği: toplanır/reklam amacıyla; konum: yok; hesap: yok). | Claude P0-2 | 👤 |
| M-10 | **app-ads.txt kök alan adında** olsun; alan adı + marka sitesi (M-67 ile birlikte). | Claude P0-8 | 👤 |
| M-11 | **Mağaza görsellerinden test reklamı ve üçüncü taraf yayın/logo kareleri çıkarılsın.** | Claude 5.2 / ChatGPT 11 | 🔧 |
| M-12 | Mağaza metni doğrulama: başlık ≤30 ✅, "305 kanal/50 radyo" gibi eski sayılar temizlenmeli, "60 dk reklamsız" → gerçek teklif yazılmalı. | Claude P0-8 | ✅ v9.33 |

---

## FAZ 1 — TELİF VE İÇERİK GÜVENLİĞİ (AdMob hesabını korur)

| No | İş | Kaynak | Durum |
|---|---|---|---|
| M-13 | **Kanal envanteri:** her kanal için kaynak alan adı + "resmi/açık lisans/izinli" + son kontrol tarihi + sağlık durumu içeren `kanallar.json` üret; uygulama yalnız buradan beslensin. | Claude P0-1 / ChatGPT 3.3 | 🔧 |
| M-14 | **Sinema grubunu temizle:** ücretli yayıncı (Cinemax, AXN, BBC First, Star Action…) ve `raw.githubusercontent.com` kaynaklı girdiler çıksın; sinema yalnız resmi ücretsiz kaynaklarla. | Claude P0-1 | 🔧 |
| M-15 | **Hak durumu "bekliyor" olan kanal herkese açık listeye girmesin** (sunucu tarafı filtre); kaldırma talebi 24–48 saatte işlensin, haftalık otomatik kontrol. | Claude P0-1 | 🔧 |
| M-16 | Ülke listeleri için "hazır" eşiği: **≥30 doğrulanmış çalışan kanal** yoksa ülke "yakında" görünsün (v9.32'de boş liste mesajı var ✅, eşik yok). | Claude 3.3 | ✅ v9.33 |
| M-17 | "Kendi listeni ekle" (M3U URL/dosya) modeli güçlendirilsin; adult/xxx filtreleri korunacak (var ✅). | Claude P0-1 | 🔧 |
| M-18 | Hukuki görüş (telif) — avukat. | Claude P0-1 / 👤 | 👤 ⏸️ |
| M-19 | Pazarlama metinlerindeki "kablolu TV'ye para vermeyi bıraktım" benzeri ifadeler yumuşatılsın. | Claude P0-1 | 🔧 |

---

## FAZ 2 — MEDYA MOTORU (en büyük teknik borç)

| No | İş | Kaynak | Durum |
|---|---|---|---|
| M-20 | **Radyo + arka plan sesini Media3'e taşı** (`MediaSessionService`): bildirim, kilit ekranı, Bluetooth/araç tuşları, audio focus hazır gelir. Eski #147'nin yarısı. | Claude P0-5 / ChatGPT 4.1 | 🔧 (askıdaydı) |
| M-21 | **Çoklu ekranı Media3'e taşı:** her pencere ExoPlayer, **sessiz pencerelere ≤480p tavan**, cihaz çözücü sınırı tespiti, pencere başına yeniden bağlanma. Eski #147. | Claude 3.1 / ChatGPT 5.3 | 🔧 (askıdaydı) |
| M-22 | **Hızlı zap yarışı:** `playRequestId` sayacı; eski isteğin callback'i yeni kanalı ezmesin. | ChatGPT 5.2 | 🔧 |
| M-23 | **Donma kontrolü:** `checkStall()` `isPlaying()` false iken çıkıyor → BUFFERING'de kör kalıyor. Durum makinesi (IDLE/BUFFERING/READY/ERROR) + tampon süresi + hata sayısı. | ChatGPT 5.1 | 🔧 |
| M-24 | **Ekonomi modu gerçek ses tasarrufu:** arka plan servisi `Quality.resolveLowest()` kullanmıyor; muxed HLS'te video iniyorsa "yalnız ses" iddiası yanlış. Audio-only varyant varsa ona geç, yoksa kullanıcıya söyle. | ChatGPT 6 | 🔧 |
| M-25 | **Veri sayacı kapsamı:** radyo ve arka plan sesi ölçülmüyor; `checkLimit()` yalnız oynatıcıda. Ortak `UsageTracker` + tüm modlarda limit. | ChatGPT 7 | 🔧 |
| M-26 | **Kayıt (HlsRecorder):** seçili kalite, AES-128, `EXT-X-MAP`, `EXT-X-BYTERANGE`, token başlıkları, bağlantı kopunca geri çekilme, boş alan kontrolü, kayıt öncesi boyut/süre bilgisi. | ChatGPT 9 / Claude 6 | 🔧 |
| M-27 | PiP ve uyku zamanlayıcısını Media3 sonrası cihazda yeniden test et (v9.32'de denenmedi). | Claude 3.2 | 🧪 |

---

## FAZ 3 — LİSTE SAĞLIĞI, SUNUCU, OTOMATİK ONARIM

| No | İş | Kaynak | Durum |
|---|---|---|---|
| M-28 | **`Good.refreshIfNeeded` hata:** gün damgası indirme başarılı olmadan yazılıyor → o gün bir daha denenmiyor. Damga yalnız başarılı indirme + doğrulama sonrası. | ChatGPT 8.1 | 🔧 |
| M-29 | **Sağlık kontrolü 3 seviye:** HTTP → playlist → **gerçek segment** indirme; `Repair.alive()` şu an yalnız playlist bakıyor. | ChatGPT 8.2 | 🔧 |
| M-30 | **Sunucuda günlük cron:** 50 ülke `good/<cc>.json`, `generatedAt`, 7 günden eski dosya yok sayılsın; TR dahil. Eski #143/#144 + #133/#142 (fix listesi) buraya bağlı. | Claude 3.3 / ChatGPT 8 | 🔧 (askıdaydı) |
| M-31 | **Yedek kaynaklar** tüm kanallara yayılsın (şu an 5 kanal). Eski #125. | Claude 3.1 | 🔧 (askıdaydı) |
| M-32 | **CountryRepo indirme sağlamlığı:** boyut sınırı, geçersiz JSON eski önbelleği silmesin, atomik yazma, şema doğrulama. | ChatGPT 8.3 | ✅ v9.33 |
| M-33 | Kanal listesi güvenliği: `ChannelRepo` değişmez liste + tek yükleme noktası, sıralama arka planda; UI/servis iş parçacığı yarışı. | Claude 6 | 🔧 |

---

## FAZ 4 — YERELLEŞTİRME, ERİŞİLEBİLİRLİK, BÜYÜK EKRAN

| No | İş | Kaynak | Durum |
|---|---|---|---|
| M-34 | **`strings.xml` + `values-xx`:** en, tr, ru, ar, de, es, pt-BR, hi; kodda kalan 70+ sabit Türkçe metin (Toast, bildirim, diyalog, reklam şeridi) taşınsın; Arapça RTL. | Claude 3.3 / ChatGPT 11 | 🔧 |
| M-35 | Grup adları ASCII ("Muzik", "Cocuk") → Türkçe karakterli + çevrilebilir. | Claude 3.3 | ✅ v9.33 |
| M-36 | **Tablet/yatay düzen** (eski #141): çoklu ekranda yatay 2×2, dikey 1+2; büyük ekran açılışı. | Claude 3.1 / #141 | 🔧 (askıdaydı) |
| M-37 | **Çentik/edge-to-edge** (eski #121): tüm ekranlara ortak inset yardımcısı (üst+alt+yan), Android 15/16 testi. | Claude P0-6 | 🔧 (askıdaydı) |
| M-38 | **TalkBack + erişilebilirlik** (eski #129): içerik açıklamaları, 48dp dokunma alanı, yazı ölçeği. | ChatGPT 11 / #129 | 🔧 (askıdaydı) |
| M-39 | Radyo ekranındaki boş görsel alan → istasyon logosu / ses görselleştirme. | ChatGPT 11 | 🔧 |
| M-40 | **Boyut/R8** (eski #123): `minifyEnabled true` + `shrinkResources`, AAB küçültme, başlatma süresi ölçümü. | Claude 6 | 🔧 (askıdaydı) |
| M-41 | PiP'te çift ses riski + `isFinishing()` akışı doğrulansın; WakeLock/WifiLock süreleri (Wi-Fi kilidi süresiz). | Claude P0-5 e/f | 🧪 |

---

## FAZ 5 — GELİR

| No | İş | Kaynak | Durum |
|---|---|---|---|
| M-42 | **Ödül sonrası `recreate()` kaldır** (yayın baştan başlıyor); şerit yerinde güncellensin; ödül metni "video reklam kalkar, banner kalır" diye düzeltilsin. | Claude 4.1 / ChatGPT 10.1 | 🔧 |
| M-43 | Reklam yerleşimi: sarı şerit ile banner arasına güvenli boşluk (yanlış tıklama riski). | Claude 4.1 | 🔧 |
| M-44 | Reklam sıklığı kullanıcı dostu: ilk açılışta ve ilk kanal açılışında reklam yok, hızlı zap'ta reklam yok. | ChatGPT 10.1 | 🔧 |
| M-45 | **Native liste reklamı** (her ~8–10 satır). | Claude 4.2 | 🔧 |
| M-46 | **App Open reklamı** (soğuk açılış, günde ≤2, ≥4 saat arayla). | Claude 4.2 | 🔧 |
| M-47 | **Mediation** (AppLovin MAX veya AdMob mediation + 2 ağ) — eski #126. | Claude 4.2 / #126 | 🔧 ⏸️ |
| M-48 | **Pro satın alma** (Play Billing): 4 ekran, kayıt, reklamsız — eski #127. | Claude 4.2 / #127 | 🔧 ⏸️ |
| M-49 | **`AdStats` ayrımı:** cihaz gösterim sayacı ile AdMob gerçek geliri karışmasın — eski #124. | ChatGPT 10.2 / #124 | 🔧 ⏸️ |
| M-50 | Banner yenileme 30–60 sn; içerik derecesi gözden geçir (şu an kısıtlama yok ✅). | Claude 4.2 | 🔧 |

---

## FAZ 6 — MAĞAZA, BÜYÜME, ÖLÇÜM

| No | İş | Kaynak | Durum |
|---|---|---|---|
| M-51 | Yeni ekran görüntüsü seti (v9.32 tasarımı, test reklamsız, 3 pencere + kilit ekranı + ülke + kayıt). | Claude 5.2 / ChatGPT 11 | 🔧 |
| M-52 | 30 sn tanıtım videosu (ilk 3 sn çoklu ekran). | Claude 5.2 | 🔧 |
| M-53 | **ASO:** İngilizce varsayılan listing + tr/ru/ar/de/es/pt-BR/hi; Store Listing Experiments (A/B). | Claude 5.2 | 🔧 + 👤 |
| M-54 | **In-App Review API** (3. oturum + 10 dk izleme, hata yoksa). | Claude 5.3 | 🔧 |
| M-55 | **Analytics + Crashlytics** (rıza ile): D1/D7, kanal başarı oranı, oturum süresi, reklam/DAU. | Claude 5.3 | 🔧 |
| M-56 | **Widget / Chromecast / Android TV (leanback)** — eski listelerde #16/#17. | Claude 5.3 / ChatGPT P2 | 🔧 |
| M-57 | **EPG / program rehberi.** | ChatGPT P2 | 🔧 |
| M-58 | **Marka alan adı + gerçek site** (SSS, telif formu, gizlilik) — M-10 ile birlikte. | Claude 5.4 | 👤 |
| M-59 | **Dışa/içe aktarma** (favoriler+ayarlar yedeği) — eski #128. | #128 | 🔧 ⏸️ |
| M-60 | Play kapalı test (12 kullanıcı/14 gün) + yayın — eski #116. | Claude 5.4 | 👤 |

---

## FAZ 7 — MÜHENDİSLİK HİJYENİ

| No | İş | Kaynak | Durum |
|---|---|---|---|
| M-61 | **`PROJECT_STATE.md`** tek doğru kaynak (sürüm, mimari, çalışan/eksik, bilinen hatalar, son testler, sıradaki iş). Eski dokümanlar `archive/` altına. | ChatGPT 12 | 🔧 |
| M-62 | **Gradle Wrapper + standart yapı** (`app/src/main/...`), Windows'a sabit yollar kaldırılsın, `gradlew` depoya. | ChatGPT 13 | 🔧 |
| M-63 | **GitHub Actions:** her push'ta derleme + lint + imzalı APK artifact. | ChatGPT 13 | 🔧 |
| M-64 | **Test altyapısı:** JUnit birim testleri (URL/başlık/parse/Quality/Usage) + cihaz matrisi (Android 8/10/12/14/15/16, düşük RAM, tablet). | ChatGPT 13 / Claude 6 | 🔧 |
| M-65 | `Prefs` anahtarları: 32-bit `hashCode()` → SHA-256 (çakışma kanal sayaçlarını karıştırıyor). | ChatGPT 14 | 🔧 |
| M-66 | Depo hijyeni: APK/AAB çift kopyaları, `build_aab/`, büyük medya dosyaları depodan çıksın; `.gitignore` genişletilsin. | Claude P0-7 | 🔧 |
| M-67 | `/giris` gibi kalan ölü üyelik rotaları ve sayfaları silinsin (worker 410 dönüyor ama sayfa duruyor). | ChatGPT 14 | 🔧 |
| M-68 | Gizlilik/yaş beyanı tutarlılığı: 13+ mı 18+ mı? Çocuk kanalları için "Aile" bölümü + ebeveyn kilidi. | Claude 4.4 | 🔧 |

---

## ⏸️ ASKIYA ALINANLAR (senin isteğinle — listede duruyor, sırası gelince)

| Eski no | İş | Yeni yeri |
|---|---|---|
| #147 | Izgara+radyo media3 | M-20, M-21 |
| #133/#142 | Sunucu fix listesi | M-30 |
| #125 | Yedek kaynak yaygınlaştırma | M-31 |
| #143/#144 | Günlük otomatik tarama | M-30, M-29 |
| #141 | Tablet/yatay | M-36 |
| #121 | Çentik/edge-to-edge | M-37 |
| #123 | Boyut/R8 | M-40 |
| #124 | Gelir ölçümü ayrımı | M-49 |
| #126 | Mediation | M-47 |
| #127 | Pro | M-48 |
| #128 | Dışa/içe aktarma | M-59 |
| #129 | TalkBack | M-38 |
| #130 | Shimmer | M-38 ile birlikte (küçük) |
| #110 | Keystore şifre rotasyonu | M-07 |
| #113 | app-ads.txt kök alan adı | M-10 |
| #114 | Data Safety formu | M-09 |
| #115 | Hukuki görüş | M-18 |
| #116 | Play yayını | M-60 |
| #152 | WhatsApp numarası | 👤 (numara bekliyor) |

---

## ÖNERİLEN UYGULAMA SIRASI (tur tur)

1. **Tur A — Güvenlik (bu hafta):** M-01, M-02, M-03, M-04, M-05, M-12 → v9.33.
2. **Tur B — Telif & mağaza:** M-13, M-14, M-15, M-11, M-06, M-08 → v9.34.
3. **Tur C — Medya motoru:** M-22, M-23, M-24, M-25 → v9.35; ardından M-20, M-21 → v9.36.
4. **Tur D — Liste sağlığı:** M-28, M-29, M-30, M-31, M-32, M-33 → v9.37.
5. **Tur E — Dil & büyük ekran:** M-34, M-35, M-36, M-37, M-38, M-39, M-40 → v9.38.
6. **Tur F — Gelir:** M-42, M-43, M-44, M-45, M-46, M-50 → v9.39; M-47, M-48, M-49 sonra.
7. **Tur G — Mağaza & büyüme:** M-51…M-58.
8. **Tur H — Mühendislik:** M-61…M-68.
9. **Yayın:** M-09, M-10, M-58, M-60 (sende).

**Tanım: "bitti"** = kod + telefonda doğrulama + sürüm notu + GitHub/site yayını + bu listede ✅.

**v9.33 (24 Kasım):** M-02, M-03, M-04, M-05, M-06, M-08, M-12 kod olarak tamam. M-03 cihazda doğrulanamadı (MIUI kurulum engeli).

Toplam: **68 master madde** (Faz 0–7) + 18 askıdaki eski madde eşlemesi.

---

## OTURUM RAPORLARI

### Oturum 24 Kasım 2026 — cihaz kuruldu, radyo/TV dayanıklılığı (v9.33 → v9.39)

**Kurulum:** MIUI ilk kurulumu adb'den reddediyordu; kullanıcı dosya yöneticisinden kurunca açıldı.
Sonrasında `adb install -r` çalışıyor (doğrulandı) → sonraki sürümler doğrudan kurulabilir.
Ayrıntı: `docs/KURULUM-VE-MIUI.md`.

**Bu oturumda biten (cihazda doğrulanmış):**
- M-03 reklam rıza kapısı: cihazda banner yüklendi (izin alındığında istek gidiyor) ✅
- M-22 zap yarışı, M-23 tamponlama/donma, M-28 liste yenileme: v9.34 telefonda kuruldu, kanal oynatma + sayaç çalıştı ✅
- M-23 genişletildi: zayıf internette kanal/radyo **aynı yayına yeniden bağlanma** (v9.35–v9.39):
  TV'de 2 deneme + yedek kaynak, radyoda 3 hızlı + 12 yavaş deneme, `http://` → `https://` yedeği.
  Cihazda wifi kapat/aç testinde radyo kendini toparladı ✅
- Radyo ekranının 9 sn'de "sıradakine geç" davranışı kaldırıldı; yavaş ağda istasyon "ölü" işaretlenmiyor ✅
- M-08 HTTPS zorunluluğu cihazda: TRT Haber (HTTPS) oynadı, HTTP radyo için istisna listesi çalışıyor ✅
- M-12 metinleri cihazda doğrulandı ("145 kanal", "Reklam izle · 15 dk reklamsız") ✅

**Bu oturumda biten madde sayısı: 7** (M-03, M-08, M-12, M-22, M-23, M-28 + M-20'nin ilk yarısı:
radyo dayanıklılığı). M-20 tam media3 geçişi henüz yapılmadı.

**Sonraki oturumda yapılacaklar (öncelik sırası):**
1. M-20 — Radyoyu media3'e taşı (ICY/302 akışlar, kilit ekranı, Bluetooth tuşları).
2. M-25 — Radyo/arka plan veri sayacı + günlük limit kapsamı.
3. M-16 — Ülke "hazır" eşiği (≥30 çalışan kanal).
4. M-21 — Çoklu ekran media3 + sessiz panellere kalite tavanı.
5. M-15 — Sunucu tarafı `good/<cc>.json` filtresi (çıplak IP / ölü yayın).
6. M-11 — Mağaza görsellerinin reklamsız yenilenmesi.

**Yayın:** GitHub `v9.36` ve `v9.39`, site `/indir` 9.39, zip `UMAY-MULTI-TV-Claude-9.39.zip`, AAB `releases/v9.39/`.
