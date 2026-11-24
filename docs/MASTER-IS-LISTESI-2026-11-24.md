# UMAY Multi TV — MASTER İŞ LİSTESİ

**Sürüm:** v9.32 (versionCode 41) · **Tarih:** 24 Kasım 2026
**Kaynaklar:** Claude "Derin Analiz ve Yol Haritası (v9.16)" + ChatGPT "v9.29 denetimi" + bizim iş listesi (#1–#156)
**Kural:** Bu dosya tek yetkili listedir. Eski README/iş listeleri tarihseldir; çelişkide **kaynak kod** ve bu liste geçerlidir.

Durum işaretleri: ✅ bitti · 🔧 kod işi · 👤 kullanıcı/hesap işi · 🧪 cihaz testi gerekli · ⏸️ askıda (senin isteğinle)

---

## 0. Durum özeti (v9.32)

- Kanal: 145 TR kanalı (TRT Haber eklendi) + **55 ülkede 3.434 kanal** (v9.64), sunucu allowlist `/good/<cc>.json`.
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
| M-22 | **Hızlı zap yarışı:** `playRequestId` sayacı; eski isteğin callback'i yeni kanalı ezmesin. | ChatGPT 5.2 | ✅ v9.66 — kodda zaten vardı · cihazda doğrulandı |
| M-23 | **Donma kontrolü:** `checkStall()` `isPlaying()` false iken çıkıyor → BUFFERING'de kör kalıyor. Durum makinesi (IDLE/BUFFERING/READY/ERROR) + tampon süresi + hata sayısı. | ChatGPT 5.1 | ✅ v9.66 — kodda zaten vardı · 12 sn eşiği |
| M-24 | **Ekonomi modu gerçek ses tasarrufu:** arka plan servisi `Quality.resolveLowest()` kullanmıyor; muxed HLS'te video iniyorsa "yalnız ses" iddiası yanlış. Audio-only varyant varsa ona geç, yoksa kullanıcıya söyle. | ChatGPT 6 | ✅ v9.66 — `AudioService` → `resolveLowest` + istek kapısı |
| M-25 | **Veri sayacı kapsamı:** radyo ve arka plan sesi ölçülmüyor; `checkLimit()` yalnız oynatıcıda. Ortak `UsageTracker` + tüm modlarda limit. | ChatGPT 7 | ✅ v9.67 — `AudioService.flushUsage/checkLimit` + `MultiActivity.countData` kodda vardi |
| M-26 | **Kayıt (HlsRecorder):** seçili kalite, AES-128, `EXT-X-MAP`, `EXT-X-BYTERANGE`, token başlıkları, bağlantı kopunca geri çekilme, boş alan kontrolü, kayıt öncesi boyut/süre bilgisi. | ChatGPT 9 / Claude 6 | 🔧 |
| M-27 | PiP ve uyku zamanlayıcısını Media3 sonrası cihazda yeniden test et (v9.32'de denenmedi). | Claude 3.2 | 🧪 |

---

## FAZ 3 — LİSTE SAĞLIĞI, SUNUCU, OTOMATİK ONARIM

| No | İş | Kaynak | Durum |
|---|---|---|---|
| M-28 | **`Good.refreshIfNeeded` hata:** gün damgası indirme başarılı olmadan yazılıyor → o gün bir daha denenmiyor. Damga yalnız başarılı indirme + doğrulama sonrası. | ChatGPT 8.1 | ✅ v9.67 — `Good` damgasi yalniz basarili indirmede yaziliyor |
| M-29 | **Sağlık kontrolü 3 seviye:** HTTP → playlist → **gerçek segment** indirme; `Repair.alive()` şu an yalnız playlist bakıyor. | ChatGPT 8.2 | ✅ v9.66 — `Repair.alive` → son segment `Range` kontrolü |
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
| M-42 | **Ödül sonrası `recreate()` kaldır** (yayın baştan başlıyor); şerit yerinde güncellensin; ödül metni "video reklam kalkar, banner kalır" diye düzeltilsin. | Claude 4.1 / ChatGPT 10.1 | ✅ v9.66 — `recreate()` kaldırıldı, şerit yerinde güncelleniyor |
| M-43 | Reklam yerleşimi: sarı şerit ile banner arasına güvenli boşluk (yanlış tıklama riski). | Claude 4.1 | ✅ v9.69 — şerit ile banner arasına 6dp boşluk |
| M-44 | Reklam sıklığı kullanıcı dostu: ilk açılışta ve ilk kanal açılışında reklam yok, hızlı zap'ta reklam yok. | ChatGPT 10.1 | ✅ v9.66 — `Prefs` kanal açılış sayacı, ilk açılış reklamsız |
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
| M-65 | `Prefs` anahtarları: 32-bit `hashCode()` → SHA-256 (çakışma kanal sayaçlarını karıştırıyor). | ChatGPT 14 | ✅ v9.68 — anahtar tam adres `use:<url>`, eski kayit tasiniyor |
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

### Oturum 24 Kasım 2026 (ikinci yarı) — bağlantı dayanıklılığı, radyo butonları, hata günlüğü (v9.40 → v9.43)

**Bu oturumda biten: 4 madde** (M-20'nin radyo yarısı, M-23 genişletme, M-69 yeni, M-70 yeni)

- **M-23 (genişletildi, ✅):** 15 sn zaman aşımı, 5 sn arayla 3 yeniden bağlanma, sonra sonraki kanal.
  TV ve radyoda "📶 Bağlantı sorunu · yeniden deneniyor (n/3)" ve düzelince "✅ Bağlantı iyi · çalıyor".
- **M-20 (radyo yarısı, ✅ kısmi):** Radyo ekranına **🔄 Tekrar dene** ve **⏭ Sonraki kanal** butonları
  eklendi; cihazda ikisi de test edildi. Yayının gerçekten çaldığı artık `isPlaying()` yerine konum +
  ağ verisi (12 sn'de <8 KB) ile anlaşılıyor; sessiz takılma tespit edilip yeniden bağlanıyor.
  **NOT (yarım):** Radyo hâlâ MediaPlayer'da; tam media3 geçişi (ICY/302, Bluetooth tuşları) yapılmadı.
- **M-69 (yeni, ✅):** Hata günlüğü — çökme + bağlantı olayları `files/events.txt`'ye yazılıyor;
  Ayarlar → Hata bildir → **📧 E-posta** (cengizkilic1980@gmail.com) veya **Paylaş** (WhatsApp/mesaj).
- **M-70 (yeni, ✅):** Radyo seçiminde geçiş reklamı kaldırıldı (sesi kesiyordu). Reklam başka
  ekranda açılırsa radyo duraklıyor ve reklam kapanınca **kendiliğinden devam ediyor** (odak düzeltmesi);
  ekranda "⏸ Duraklatıldı (reklam/başka ses)" yazıyor.

**Cihaz doğrulaması:** 9.43 kuruldu; "✅ Bağlantı iyi · çalıyor" görüldü, **Tekrar dene** ve
**Sonraki kanal** butonları çalıştı, wifi kapat/aç sonrası radyo toparlandı.

**Yayın:** GitHub `v9.43`, site `/indir` 9.43, `releases/v9.43/` APK+AAB+notlar, zip 9.43.

**Sonraki oturum:** M-20 tam media3 geçişi, M-25 radyo veri sayacı, M-15 sunucu filtresi,
M-21 çoklu ekran, M-16 eşik kontrolü, M-11 mağaza görselleri. **Bugün hepsi bitmez** (yaklaşık
20+ madde kaldı; mağaza görselleri ve Play yayını kullanıcı işi).

### Oturum 24 Kasım 2026 (üçüncü yarı) — reklam barı, radyo tasarımı, Play evrakı (v9.44 → v9.45)

**Bu oturumda biten: 3 madde**

- **Banner barı (✅):** Bar artık her ekranda **sabit** (ana ekran, radyo listesi/çaları, ayarlar,
  ülke, kayıtlar, çoklu ekran, hakkında). Ana ekran ve hakkında ekranında hiç yoktu, eklendi.
  Kök neden iki tane: (1) ana ekranda banner listenin içine ekleniyordu, kaydırınca kayboluyordu;
  (2) reklam yüklenirken kendi barımız erken gizleniyor, AdView yüksekliği 0 kalınca bar boş
  görünüyordu. Artık bar min. 56 dp ve **onAdLoaded**'da değişiyor; "No fill" olursa 60 sn sonra
  yeniden deniyor. Cihazda doğrulandı.
- **Radyo paneli (✅):** Boş çerçeve yerine "● CANLI YAYIN" + istasyon adı + "MP3 · 128 kbps ·
  Türkiye" + "UMAY RADYO · canlı akış" içeren panel; cihazda GENÇ KRAL FM ile doğrulandı.
- **Play evrakı (✅ doküman):** `docs/play/PLAY-YAYIN-HAZIRLIK.md` — teknik zorunluluklar, mağaza
  kaydı, beyanlar, **Data safety tablosu**, hukuki belgeler ve sıralı yayın adımları.
  Siteye **Kullanım Koşulları** (`/kullanim-kosullari.html`) ve **Telif/İçerik Kaldırma**
  (`/telif-bildirimi.html`) sayfaları eklendi.

**Banner geliri tespiti:** Cihaz logunda banner için sürekli `No fill.` görülüyor; geçiş ve ödüllü
reklamlar doluyor. Yani bar şu an **gelir üretmiyor** (kendi tanıtım barımız gösteriliyor). Sebep
hesabın "sınırlı reklam sunumu" durumunda olması (Play mağaza bağlantısı gerekiyor). Play yayını
sonrası banner stoğu açılmalı; M-60 ile bağlantılı.

**Yayın:** GitHub `v9.45`, site `/indir` 9.45, `releases/v9.45/`, zip 9.45.

**Sonraki oturum:** Play Console adımları (M-09 Data Safety, M-10 app-ads.txt kök alan adı, M-57/58
mağaza görselleri), M-20/M-72 tam media3, M-25 radyo sayacı, M-15 sunucu filtresi.

### Oturum 24 Kasım 2026 (dördüncü yarı) — radyo sayacı, ülke eşiği, liste denetimi (v9.46 → v9.47)

**Bu oturumda biten: 3 madde**

- **M-25 (✅):** Radyo/arka plan dinlerken harcanan veri `TrafficStats` ile ölçülüp günlük sayaca yazılıyor
  (1 MB'da bir ve durdurma/kapanışta kayıt). **Günlük veri limiti** radyoda da uygulanıyor: limit dolunca
  radyo duruyor, bildirim + olay kaydı düşülüyor. Cihazda doğrulandı (2,3 MB → 2,5 MB).
- **M-16 (✅):** Ülke "hazır" eşiği **30 kanal**; 1–29 kanallı ülkeler "n kanal · yakında (30+ gerekli)"
  etiketiyle görünüyor, boş listeye girilmiyor. TR her zaman hazır.
- **M-14/M-15 (✅ kısmi):** `tools/check-lists.py` eklendi — çıplak IP, `raw.githubusercontent`, ücretli marka,
  HTTP ve kopya kayıtları tarıyor; `--fail` modu var. Çalıştırıldı: **temiz** (TR kanal + radyo + good/*.json).
  Sunucu tarafı "hak durumu bekliyor" filtresi (M-13 `kanallar.json` şeması) hâlâ **yarım**; M-15'in
  otomatik filtre kısmı bu denetimle karşılandı, hak durumu alanı M-13 ile gelecek.

**Yayın:** GitHub `v9.47`, site `/indir` 9.47, `releases/v9.47/`, zip 9.47.

**Sonraki oturum (öncelik):** M-72/M-20 tam media3 radyo, M-21 çoklu ekran media3, M-13 kanal envanteri,
M-11 mağaza görselleri, M-57/58 mağaza metinleri.

### Oturum 24 Kasım 2026 (beşinci yarı) — radyo kapatma şeridi, kanal envanteri (v9.48)

**Bu oturumda biten: 2 madde**

- **M-73 (yeni, ✅):** Radyo arkada çalarken **ana ekranda "🎧 X çalıyor · ⏹ Kapat" şeridi**. Kapat
  dokunuşu servisi durduruyor; radyo çalar ekranındaki Durdur/Çık ve bildirimdeki Durdur zaten vardı.
  Cihazda test edildi: şerit kayboluyor, AudioService çalışmıyor (dumpsys 0).
- **M-13 (✅ ilk adım):** `tools/make-inventory.py` → `site/public/kanallar.json` (177 kayıt:
  145 TV + 32 radyo; ad, tür, adres, kaynak alan adı, **hak durumu**, son kontrol tarihi, durum).
  Çıplak IP / raw.github / ücretli marka otomatik "review". **UYARI (yarım):** uygulama henüz bu
  dosyadan beslenmiyor; M-15 sunucu filtresi ve uygulama entegrasyonu M-13'ün devamı.

**Yayın:** GitHub `v9.48`, site `/indir` 9.48 (+ `/kanallar.json` yayında), `releases/v9.48/`, zip 9.48.

### Oturum 24 Kasım 2026 (altıncı yarı) — TV reklam kesintisi, doğrulama listesi (v9.49)

**Bu oturumda biten: 1 madde**

- **M-74 (✅, cihaz doğrulaması bekliyor):** TV'de tam ekran reklam açıkken yayın duraklatılıyor,
  donma/zaman aşımı sayaçları duruyor; reklam kapanınca kaldığı yerden değil **canlıya yeniden
  bağlanıyor**. Kök neden: reklam sırasında oynatıcı arka planda çalışıp tamponu geride bırakıyor,
  15 sn sayacı da "açılmadı" diyebiliyordu.
- **Doğrulama listesi:** `docs/DOGRULANACAKLAR.md` — 12 cihaz testi, 8 hesap/Play işi, 5 yarım kod işi.

**Durum sayısı (24 Kasım 2026 sonu):** toplam **74 madde** · **14 ✅ bitti** · **7 👤 kullanıcı işi** ·
**53 açık**.

---

## EK MADDELER (24 Kasım 2026, kullanıcı geri bildirimi)

| No | İş | Durum |
|---|---|---|
| M-69 | **Hata günlüğü + e-posta:** çökme ve bağlantı olayları cihazda `events.txt`'ye yazılır; Ayarlar → Hata bildir → 📧 E-posta / Paylaş. | ✅ v9.43 |
| M-70 | **Radyoda reklam kesintisi:** radyo seçiminde geçiş reklamı kaldırıldı; başka ekranda reklam açılırsa radyo duraklar, reklam kapanınca kendiliğinden devam eder. | ✅ v9.43 |
| M-71 | **Radyo geçiş reklamı geliri:** M-70 ile kaybedilen radyo geçiş reklamı yerine radyo listesinde banner + 40 dk sonraki ödüllü teklif korunuyor; ileride radyo içi sesli reklam (opsiyonel) değerlendirilecek. | 🔧 planlı |
| M-72 | **Tam media3 radyo:** MediaPlayer yerine ExoPlayer + MediaSession (ICY/302, Bluetooth/kilit ekranı tuşları). | 🔧 (M-20'nin devamı) |
| M-73 | **Radyoyu her yerden kapat:** radyo arkada çalarken ana ekranda "🎧 X çalıyor · ⏹ Kapat" şeridi. | ✅ v9.48 (cihazda doğrulandı) |
| M-74 | **Reklam sırasında TV duraklıyor:** tam ekran reklam açıkken yayın duraklatılır, reklam kapanınca canlıya yeniden bağlanılır; reklam sırasında donma sayılmaz. | ✅ v9.49 (cihaz doğrulaması bekliyor — D-05) |
| M-75 | **Ses/görüntü senkronu (düşük kalitede ses erken):** kalite tercihi artık varyant playlist URL'iyle değil, master playlist + ExoPlayer track sınırıyla (`setMaxVideoSize`) uygulanıyor; ses ve görüntü aynı master'dan geliyor. | ✅ v9.55 (cihazda 360p'de oynatıldı) |
| M-76 | **Radyo araması görünmüyordu:** klavye açıkken liste 0 yüksekliğe çöküyordu (ekran boş). Pencere `adjustNothing` ile klavyeden etkilenmiyor; sonuçlar klavyenin üstünde görünüyor. | ✅ v9.55 (cihazda doğrulandı) |
| M-77 | **Türkçe karakterli arama:** "super" → "Süper FM", "radyo" → "Radyo 7" (`Lang.fold`); arama sırasında gizlenmiş istasyonlar da listeleniyor. | ✅ v9.55 (cihazda doğrulandı) |
| M-78 | **Alt şerit gezinme çubuğunun altına kayıyordu:** MIUI jest çubuğunda alt inset 0 geliyordu; banner artık `navigation_bar_height` ile en az çubuk yüksekliği kadar yukarıda. | ✅ v9.56 (cihazda doğrulandı) |
| M-79 | **Ayarlar'da sürüm görünür:** başlık altı "Sürüm 9.57 · dokunduğun satır açılır", Hakkında bölümünde "📱 Sürüm 9.57" satırı; numara `versionName`'den geliyor. | ✅ v9.57 (derleme; ekranda doğrulama tablete kurulum sonrası) |
| M-15 | **Radyo internet aramasında alakasız sonuçlar:** sorgu artık istasyon adında **kelime başında** aranıyor ("ntv" → Kentville eşleşmiyor). | ✅ v9.57 (kod; cihazda doğrulama bekliyor) |
| M-80 | **Ağ geri gelince kendiliğinden toparlanma:** `ConnectivityManager` dinleyicisi tek oynatıcıda ve çoklu ekranda yayını yeniden başlatıyor; `ready=false` ile spinner/uyarı takılması giderildi. | ✅ v9.59 (cihazda: Wi-Fi 22/30 sn kesildi, ikisi de toparlandı) |
| M-81 | **Çoklu ekran kalitesi ve veri sayacı:** MediaPlayer'da track sınırı olmadığı için master playlist 1080p'ye çıkabiliyordu; artık 360p varyantı seçiliyor. Ayrıca çoklu ekranda günlük veri sayacı hiç artmıyordu (yalnız ExoPlayer sayıyordu) — `TrafficStats` farkı eklendi. | ✅ v9.61 (cihazda: 3 pencere ≈650 kbps/kanal; sayaç 643,5 → 661,0 MB) |
| M-82 | **"Ne var, ne yok?" ekranı güncellendi:** 25 satır, güncel ürünle eşlendi (çoklu ekran/ağ toparlanma, radyo kapatma şeridi, A/V senkron 360p, veri sayacı, sürüm bilgisi, Play durumu + olmayanların nedeni). | ✅ v9.61 (cihazda görüldü) |
| M-83 | **Ülke seçiminde "indir" sessizce başarısızdı:** sunucuda listesi olmayan ülke 404 dönüyordu; satır artık **"yakında · liste henüz yok"** yazıyor, tekrar dokunmak ağa çıkmıyor. | ✅ v9.62 (cihazda: Suriye/Yunanistan) |
| M-84 | **Ülke dizini:** sunucuda `good/index.json` (~100 B) eklendi; uygulama açılışta bir kez çekip **listesi olmayan 41 ülkeyi** otomatik "yakında · liste henüz yok" yapıyor. 51 ülkeden **10'unda liste var** (br, de, gb, in, mx, ru, sa, se, tr, us). | ✅ v9.63 (cihazda doğrulandı) |
| M-85 | **"Az kanal = yakında" eşiği kaldırıldı:** 30 kanal şartı yerine **1+ kanal = hazır**. İndirme hatası 404 (liste yok) ve ağ hatası olarak ayrıldı; ağ hatası ülkeyi "yakında" işaretlemiyor, sunucuya liste eklenince eski işaret siliniyor. Listeler **10 ülke/~673 kanaldan 55 ülke/3.434 kanala** çıktı; her aday master→en düşük varyant zinciriyle sınandı. | ✅ v9.64 (cihazda: Birleşik Krallık 111, Azerbaycan 3 kanal indi) |
| M-86 | **Ülke sırası:** listesi hazır ülkeler üstte (Türkiye ilk, kanal sayısına göre), sonra indirilebilenler, en sonda "yakında" olanlar. | ✅ v9.64 (cihazda görüldü) |
| M-87 | **Kullanıcı bilgilendirmesi:** ülke ekranına TR/EN/RU/AR **"Kanal açılmıyor mu?"** kartı — reklam kuşağı, IP değişikliği, kanal kapanması ve "Ölü kanalları onar" çözümü. Kısa metin açık, dokununca ayrıntı. | ✅ v9.64 (cihazda görüldü) |
| M-94 | **3'lü izleme setleri bozuktu:** "★ set" ile kaydedilen set `ad:` öneki yüzünden hiç geri yüklenmiyordu; kayıt indeks tuttuğu için ülke/liste değişince başka kanal açılıyordu. Önek ayrıştırması düzeltildi, kayda kanal adresleri eklendi (`u:` alanı), eski kayıtlar uyumlu. | Claude 17. yarı | ✅ v9.67 (cihazda: Test Seti → 2. pencere arte→ADRIA) |
| M-95 | **Çoklu ekran paneli ekrana sığmıyordu:** "★ set" ve sonrası 1280 px'de görünmüyordu, set kaydetmek imkânsızdı. Panel yatay kaydırılabilir; set listesinde ham kod yerine set adı yazılıyor. | Claude 17. yarı | ✅ v9.67 (cihazda: ★ set görünür, ad "Test Seti") |
| M-96 | **Kalite etiketi tercihi gösteriyordu:** tek varyantlı kanalda (3sat) yayın 720p inerken düğme "360p" yazıyordu. Etiket artık oynatıcının gerçek yüksekliği (`onVideoSizeChanged`); düğme metni yayın hazır olunca güncelleniyor. | Claude 18. yarı | ✅ v9.68 (cihazda: 3sat → 720p) |
| M-97 | **Kayıtlı sette bulunamayan kanal sessizce yutuluyordu:** setteki kanal listeden kalkmışsa uygulama başka kanalı açıyor, kullanıcı neden farklı yayın gördüğünü anlamıyordu. Artık "kayıtlı setteki N kanal bu listede yok" uyarısı veriliyor, kalan pencereler eşleşen kanallarla açılıyor. | Claude 19. yarı | ✅ v9.69 (cihazda: TR listesinde DE seti → "4 kanal bu listede yok") |
| M-98 | **Ayarlar’da ülke değeri bayat kalıyordu:** `SettingsActivity` değeri yalnız `onCreate`’de yazıyordu; ülke değiştirip geri dönünce satır eski ülkeyi gösteriyordu. `onResume`’da tazeleniyor. | Claude 19. yarı | ✅ v9.69 (cihazda: TR→DE dönüşünde satır hemen "Almanya") |

### Oturum 24 Kasım 2026 (yedinci yarı) — A/V senkron + 12 maddelik test (v9.55)

**Bu oturumda biten: 3 madde**

- **M-75 (✅):** kullanıcının bildirdiği "düşük kalitede ses 4–7 sn erken" sorununun kök nedeni
  bulundu: kalite seçimi master yerine **tek varyant playlist URL'i** veriyordu; ses ayrı
  rendition'dan gelince senkron kayıyordu. Artık master playlist + `setMaxVideoSize`.
- **M-76 (✅):** radyo aramasında klavye açılınca liste 0 yüksekliğe çöküyordu (kullanıcı boş
  ekran görüyordu). `adjustNothing` ile düzeltildi, cihazda doğrulandı.
- **M-77 (✅):** Türkçe karakter sadeleştirmeli arama + gizli istasyonların da bulunması.

**Cihaz testi (12 madde):** `docs/DOGRULANACAKLAR.md` sonunda tablo. Sonuç: **2 ✅ / 4 kısmen /
6 yapılamadı** (MIUI `svc wifi` engeli, reklam zamanlaması, release `run-as` yokluğu, menü yolu).

**Durum sayısı (24 Kasım 2026, 7. yarı sonu):** toplam **77 madde** · **17 ✅ bitti** ·
**7 👤 kullanıcı işi** · **53 açık**.

**Sonraki oturum:** (1) D-04/D-05/D-12 elle test (uçak modu, reklam, 3'lü mod),
(2) M-72 tam media3 radyo, (3) M-13 `kanallar.json` entegrasyonu + M-15 "review" filtresi.

### Oturum 24 Kasım 2026 (sekizinci yarı) — 9.56: arama + banner + kapasite denetimi

**Bu oturumda biten: 2 madde (M-76 tekrar doğrulandı, M-78 yeni)**

- **M-76 (tekrar):** kullanıcının gönderdiği ekran görüntüsü 9.52 öncesine aitti; 9.55'te
  düzeltilen klavye/liste çökmesi 9.56'da cihazda tekrar doğrulandı ("ntv" araması klavye
  açıkken sonuç verdi, çipler göründü). Ekran görüntüsü: `_build/k3.jpg`.
- **M-78 (✅):** ana/radyo ekranındaki alt şerit (veri + banner) gezinme çubuğunun altında
  kalıyordu. `AdManager.wrap` alt boşluğu artık inset ile `navigation_bar_height`'ın
  büyüğü kadar bırakıyor. Cihazda Paycell banner'ı tam göründü (`_build/k1.jpg`).
- **Yeni zorunlu kural:** `tools/verify-apk.py` + `AGENTS.md`. Her sürümde APK bayt,
  dosya sayısı, açılmış bayt, sınıf sayısı ve imza ölçülür; **düşüş varsa yayın durur**.
  9.55 → 9.56: 400 dosya / 17.504 sınıf / +124 bayt, kayıp yok (`releases/apk-kapasite.json`).
- **Tablet:** ayrı derleme yok; `releases/v9.56/UMAY-MULTI-TV-9.56-tablet.apk` telefon
  APK'sının birebir kopyası (aynı SHA-256).

**Durum sayısı (24 Kasım 2026, 8. yarı sonu):** toplam **78 madde** · **18 ✅ bitti** ·
**7 👤 kullanıcı işi** · **53 açık**.

**Sonraki oturum:** (1) D-04 (uçak modu), D-05 (reklam anı), D-12 (3'lü mod) elle test,
(2) M-72 tam media3 radyo, (3) M-15 radyo-browser "review" filtresi (arama sonuçlarında
yabancı istasyonlar çıkıyor), (4) M-13 `kanallar.json` entegrasyonu.

### Oturum 24 Kasım 2026 (dokuzuncu yarı) — 9.57: sürüm etiketi + arama filtresi (tablet günü)

**Bu oturumda biten: 2 madde**

- **M-79 (✅ kod):** Ayarlar başlığının altı ve "Hakkında" satırı sürüm numarasını gösteriyor.
  Numara `versionName`'den okunuyor; sürüm notu elle güncellenmiyor.
- **M-15 (✅ kod):** radio-browser sonuçları kelime başı eşleşmesiyle süzülüyor; "ntv"
  aramasında "Kentville" gibi yabancı sonuçlar eleniyor.
- **Tablet:** `25040RP0AG` bağlıydı, üzerinde **9.29 farklı imzayla** kurulu olduğu için
  `adb install -r` reddedildi (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`). APK hem
  `releases/v9.57/` hem `İndirilenler/UMAY-MULTI-TV-9.57-tablet.apk` içine bırakıldı.
- **Telefon bağlı değildi:** telefon doğrulamaları bir sonraki oturuma kaldı.

**Durum sayısı (24 Kasım 2026, 9. yarı sonu):** toplam **80 madde** · **20 ✅ bitti** ·
**7 👤 kullanıcı işi** · **53 açık**.

**Sonraki oturum (telefon bağlanınca):** (1) 9.57'yi telefona kur, Ayarlar → sürüm
satırını ve radyo aramasını doğrula, (2) D-04 uçak modu, D-05 reklam anı, D-12 3'lü mod,
(3) tablete temiz kurulum (eski 9.29 kaldırılıp), (4) M-72 tam media3 radyo,
(5) M-13 `kanallar.json` entegrasyonu.

### Oturum 24 Kasım 2026 (onuncu yarı) — 9.59: ağ toparlanması (telefon bağlı)

**Bu oturumda biten: 1 madde + 4 doğrulama**

- **M-80 (✅):** ağ kesilip geri geldiğinde yayın kendiliğinden açılıyor.
  Üç kök neden: (a) ağ dinleyicisi yoktu, (b) yeniden bağlanma yolları `ready` bayrağını
  sıfırlamadığı için spinner/uyarı ekranda kalıyordu, (c) çoklu ekranda ağ dönüşü denemesi yoktu.
  Cihazda: tek oynatıcı (Wi-Fi 22 sn kapalı) ve 3'lü ekran (30 sn kapalı) ikisi de toparlandı.
- **Doğrulananlar:** D-04 (tek oynatıcı toparlanma) ✅, D-12 (3'lü ekran) ✅,
  D-19 (Ayarlar'da sürüm) ✅, D-20 (radyo aramasında “ntv” filtresi) ✅.
- **D-05 (reklam sonrası canlıya dönüş) hâlâ açık:** 9.7 MB'lık test süresinde tam ekran
  reklam çıkmadı; reklam anı yakalanınca bakılacak.

**Durum sayısı (24 Kasım 2026, 10. yarı sonu):** toplam **81 madde** · **21 ✅ bitti** ·
**7 👤 kullanıcı işi** · **53 açık**.

**Sonraki oturum:** (1) D-05 reklam anı, D-08 ülke “yakında”, D-09 hata bildir akışı,
(2) tablete 9.59 kurulumu (dosyadan), (3) M-72 tam media3 radyo, (4) M-13 `kanallar.json`.

### Oturum 24 Kasım 2026 (on birinci yarı) — 9.60/9.61: kalite, veri sayacı, durum ekranı

**Bu oturumda biten: 2 madde + 3 doğrulama**

- **M-81 (✅):** çoklu ekran master playlist yüzünden 1080p'ye çıkabiliyordu → 360p varyantı.
  Ayrıca çoklu ekranda **veri sayacı hiç artmıyordu** (588,0 → 588,0); `TrafficStats` farkı
  eklendi, 72 sn'de 643,5 → 661,0 MB ölçüldü.
- **M-82 (✅):** Ayarlar → “Ne var, ne yok?” listesi 25 satıra çıkarıldı ve ürünle eşlendi.
- **Gerçek ağ testi (düzeltilmiş):** önceki testte mobil veri devreye giriyordu; bu kez
  `mobile_data=0` + `ping → unreachable` doğrulanarak yapıldı. 30 sn'de “(1/3)”, 60 sn'de
  “yayın açılmadı”, ağ dönünce **kendiliğinden canlıya döndü** (577,5 → 584,5 MB).
- **Kalite teyidi:** varsayılan 360p; tek oynatıcı `setMaxVideoSize(∞,360)`, çoklu ekran
  360p varyantı; kanala özel seçim varsa o geçerli.

**Durum sayısı (24 Kasım 2026, 11. yarı sonu):** toplam **83 madde** · **23 ✅ bitti** ·
**7 👤 kullanıcı işi** · **53 açık**.

**Sonraki oturum (kontör azalıyor — en önemliler):**
(1) D-05 reklam sonrası canlıya dönüş, (2) D-08 ülke “yakında”, D-09 hata bildir,
(3) tablete 9.61 kurulumu, (4) M-72 tam media3 radyo, (5) M-13 `kanallar.json`.

### Oturum 24 Kasım 2026 (on ikinci yarı) — 9.62: ülke listesi “yakında”

**Bu oturumda biten: 2 madde**

- **M-83 (✅):** “Ülke seç → indir” hiçbir şey yapmıyordu. Kök neden: `site/good/<cc>.json`
  sunucuda yoksa 404 dönüyor, uygulama bunu sessizce yutuyordu. Artık satır
  “yakında · liste henüz yok” oluyor, tekrar dokunuş ağa çıkmıyor. Cihazda doğrulandı.
- **D-09 (✅):** Hata bildir penceresi + e-posta paylaşım seçicisi açıldı (gönderim yapılmadı).
- **D-08 (✅ kod + cihaz):** Ülke ekranında “hazır” sayısı ve “yakında” etiketi artık görünüyor.

**Durum sayısı (24 Kasım 2026, 12. yarı sonu):** toplam **84 madde** · **24 ✅ bitti** ·
**7 👤 kullanıcı işi** · **53 açık**.

**Sonraki oturum (kota az):** (1) D-05 reklam sonrası dönüş (test reklamıyla),
(2) tablet 9.62 kurulumu, (3) M-72 tam media3 radyo, (4) M-13 `kanallar.json`.

### Oturum 24 Kasım 2026 (on üçüncü yarı) — 9.63: ülke dizini

**Bu oturumda biten: 1 madde**

- **M-84 (✅):** “Kaç ülkede TV yok?” sorusu ürüne girdi: 51 ülkeden **10'unda liste var**,
  **41'inde yok**. Sunucuya `good/index.json` eklendi; uygulama ülke ekranı açılınca bu dizini
  bir kez (~100 bayt) çekip eksik ülkeleri kendiliğinden “yakında · liste henüz yok” yapıyor.
  Cihazda doğrulandı (Azerbaycan/Avusturya/BAE “yakında”, Almanya/ABD “indir”).

**Durum sayısı (24 Kasım 2026, 13. yarı sonu):** toplam **85 madde** · **25 ✅ bitti** ·
**7 👤 kullanıcı işi** · **53 açık**.

**Sonraki oturum:** (1) D-05 reklam sonrası dönüş, (2) tablet 9.63 kurulumu,
(3) M-72 tam media3 radyo, (4) M-13 `kanallar.json` beslemesi.

### Oturum 24 Kasım 2026 (on dördüncü yarı) — 9.64: ülke eşiği, 55 ülke, bilgi kartı

**Bu oturumda biten: 3 madde + 5 doğrulama**

- **M-85 (✅):** "30'dan az kanal → yakında" kuralı kaldırıldı. Artık inen listede **1 kanal varsa**
  ülke hazır. İndirme sonucu üçe ayrıldı: `DL_OK`, `DL_GONE` (404 → "yakında"), `DL_FAIL`
  (ağ → işaretlenmez, tekrar denenebilir). Sunucuya liste eklenince eski "yakında" işareti
  `syncIndex` + `Prefs.clearCountryMiss` ile siliniyor.
- **Kanal listeleri:** 10 ülke/~673 kanaldan **55 ülke / 3.434 kanala** çıktı. Üretim
  `_build/good.py` ile: iptv-org adayları **master playlist → en düşük varyant → segment listesi**
  zinciriyle sınandı, düşenler bir kez daha denendi, iki kez düşen listeye alınmadı.
  Öne çıkanlar: ABD 66→290, Hindistan 71→233, Romanya 180, Arjantin 156, Rusya 53→141,
  Almanya 73→120, Birleşik Krallık 24→115, Meksika 57→114, Kanada 102, İspanya 99,
  Brezilya 49→81, Çin 69, Bulgaristan 66, İsviçre 62, Irak 52, Kore 51.
  Yeni ülkeler: Nijerya, Filipinler, Vietnam, Bangladeş.
  İsveç 85→11: eski liste yalnız "master 200 döndü mü" diye bakıyordu, çoğu Türkiye'den
  coğrafi kilitliydi; yeni liste yalnız gerçekten açılanları tutuyor.
- **M-86 (✅):** ülke ekranı üç kova: hazır (Türkiye ilk, kanal sayısına göre) → indirilebilen → yakında.
- **M-87 (✅):** kullanıcı diliyle bilgi kartı; "kanal açılmadı → program kötü" izlenimini engelliyor.
- **Cihaz testi (9.64, telefon):** Ülke seç → Birleşik Krallık → **indir** → 111 kanal;
  BBC One canlı, **360p** rozeti, 1/111; veri 732,8 → 744,1 MB. Radyo: GB için **40 istasyon**
  indi, Heart 80s "Bağlantı iyi · çalıyor". Azerbaycan **3 kanal** ile "hazır" (eşik kanıtı).
- **Kapasite:** 7.119.005 B · 400 dosya · **17.515 sınıf** (+4) · imza aynı → ✅ kayıp yok.
  GitHub release `v9.64`, site `/indir` 9.64, `good/index.json` 55 kod.

**Durum sayısı (24 Kasım 2026, 14. yarı sonu):** toplam **88 madde** · **28 ✅ bitti** ·
**7 👤 kullanıcı işi** · **53 açık**.

**Sonraki oturum:** (1) tablet 9.64 kurulumu (dosyadan), (2) D-05 reklam sonrası canlıya dönüş,
(3) kanal sayısı düşük ülkeler (SE 11, PH 9, AZ 3, OM 5, DZ 6) için ek doğrulanmış kaynak,
(4) M-72 tam media3 radyo, (5) M-13 `kanallar.json` beslemesi.


### Oturum 24 Kasım 2026 (on beşinci yarı) — 9.65: satır satır hata analizi

**Bu oturumda biten: 6 madde + 4 tur düzeltme + cihaz senaryosu (TR/US/DE)**

- **M-88 (✅):** Radyo **12 saniyede bir kendini yeniden başlatıyordu**. `AudioService.monitor`
  içinde `lastRx` önce güncelleniyor, sonra `rx - lastRx` ölçülüyordu; fark hep 0 →
  "yayın takıldı" sanılıp yeniden bağlanılıyordu. Fark güncellemeden önce alınıyor,
  eşik 8 KB → 2 KB (16 kbps yayınlar için). Cihazda 26 sn log: tek yeniden bağlanma yok.
- **M-89 (✅):** **Uyku zamanlayıcıları kalıcı değildi.** Radyo sayacı ekranın Handler'ında
  tutuluyordu; "◀ Liste" deyince `onDestroy` siliyordu → radyo sabaha kadar çalıyordu.
  Sayaç `AudioService`'e taşındı (`sleepIn/sleepLeft`, `pendingSleep`). TV sayacı
  `Prefs.sleepUntil` ile saklanıyor. Bildirimdeki ⏮⏭ radyoda çalışmıyordu (yalnız TV
  listesinde arıyordu) — radyo listesi eklendi; cihazda GENÇ KRAL FM → Süper FM.
- **M-90 (✅):** **Çökme korumaları.** Boş liste: `idx % list.size()` (ArithmeticException),
  `Slot.setChannel(size-1)`, `list.get(index)`, `names.get(0)`. Ayrıca `ChannelRepo.all()`
  statik listeyi döndürdüğü için ülke değişince arkadaki oynatıcı/çoklu ekran/radyo
  çöküyordu — ekranlar kendi kopyasını tutuyor. `indexOfUrl` -1 döndürüyor.
- **M-91 (✅):** **Ülke indirmesi çökmesi.** `CountryActivity.pick()` indirme sonrası
  `CountryRepo.fetchGood()` ağ çağrısını UI iş parçacığında yapıyordu →
  `NetworkOnMainThreadException`. Arka plana alındı; ABD 263, Almanya 113 kanal indi, çökme yok.
- **M-92 (✅):** **Android 8/9'da kayıtlar görünmüyordu.** `RELATIVE_PATH` sütunu API 29+;
  API<29 için `DATA LIKE` sorgusu. Kayıtlarım'da ".ts → 0 dk" sorunu da giderildi.
- **M-93 (✅):** **Reklam rızası fail-closed.** `loadInterstitial`, `loadRewarded`,
  `maybeTimerAd` artık `adsAllowed` olmadan çalışmıyor. Jetonlu CDN'lerde master→varyant
  `?token` taşınması (`Quality`, `Repair`, `HlsRecorder`), ülke başına `goodDay`,
  `CountryRepo.count` eşiği (200→20 bayt), Hakkında sürümü, sabit "305 kanal/49 radyo"
  metinleri, `ProgressDialog` sızıntıları, ölü `probe()` aynı turda düzeltildi.
- **Doküman:** `docs/HATA-ANALIZI-2026-11-24.md` — 12 maddelik kontrol listesi, 24 bulgu,
  tur kayıtları, TR/US/DE cihaz sonuç tablosu, sonraki tura devredenler.
- **Cihaz senaryosu (9.65, telefon):** temiz kurulum → TR 145 kanal, TRT Haber 360p,
  16 sn kayıt (12,6 MB) + Kayıtlarım; TR radyo 25 istasyon, bildirimden sonraki,
  15 dk uyku; ABD 263 kanal (ABC KAAL), Almanya 113 kanal (3sat), DE radyo 22 (SWR3);
  uçak benzeri ağ kesintisi → "yeniden bağlanıyorum (1/3)" → ağ gelince döndü;
  PiP `mode=pinned`; 3'lü çoklu ekran; favori filtresi. `FATAL` yok.
- **Açık kalan (yeni):** 3sat gibi **tek rendition** yayınlarda 360p veri düşürmüyor
  (kaynak ~8 Mbps); ayrı "sadece ses kaydı" yok; günlük limit uyarısı canlı denenmedi;
  `MultiActivity` set kaydı indeks tutuyor (ülke değişince set kayabilir).
- **Kapasite:** 7.119.765 B · 400 dosya · **17.515 sınıf** (+0) · imza aynı → ✅.
  GitHub release `v9.65`, site `/indir` 9.65.

**Durum sayısı (24 Kasım 2026, 15. yarı sonu):** toplam **94 madde** · **34 ✅ bitti** ·
**7 👤 kullanıcı işi** · **53 açık**.

**Sonraki oturum:** (1) günlük limit uyarısı canlı testi, (2) ölü kanal onarma (yapay ölü URL),
(3) "sadece ses" modu doğrulaması, (4) ödüllü reklam akışı (kullanıcı elle), (5) tablet 9.65
dosyadan kurulum, (6) `MultiActivity` set kaydını URL tabanlı yapmak, (7) tek rendition
kanallarda dürüst kalite etiketi.

### Oturum 24 Kasım 2026 (on altıncı yarı) — 9.66: medya motoru, liste sağlığı, reklam UX

Bu turda master listedeki sıradaki dört açık madde kapatıldı; iki madde ise kodda
zaten uygulanmış olduğu için yalnız doğrulandı.

- **M-24 (✅):** `AudioService.onStartCommand` ham adresi açıyordu; "sadece ses"te
  master playlist ABR ile 1080p'ye çıkabiliyordu. Servis artık
  `Quality.resolveLowest` ile en düşük varyantı çözüp onu açıyor; çözüm arka planda,
  sonuç `ui.post` ile ana iş parçacığına alınıyor ve `currentUrl` değiştiyse istek
  atılıyor (yanlış istasyon çalmıyor). Varyant yoksa özgün adres kullanılıyor.
  Cihazda: "Sadece ses" → oynatıcı kapandı, `media_session` `PLAYING`, yayın kesintisiz.
- **M-29 (✅):** `Repair.alive` üçüncü seviyeye çıktı: playlist'teki son segment
  `Range: bytes=0-2047` ile isteniyor, 200/206 + en az 1 bayt şartı aranıyor. Sunucu
  Range'i yok sayarsa bağlantı ilk okumadan sonra kapatılıyor (tüm segment inmiyor).
  Almanya listesinde ölü kanal olmadığı için cihazda gerçek onarım denenmedi.
- **M-42 (✅):** Ödül sonrası `Activity.recreate()` kaldırıldı; yayın baştan
  başlamıyor, şerit yerinde `"✅ Reklamsız · N dk"` oluyor. Ödüllü reklam ağı
  doldurmadığı için cihazda akış denenemedi.
- **M-44 (✅):** `Prefs.channelOpens` sayacı eklendi; **ilk** kanal açılışında tam
  ekran reklam istenmiyor. Cihazda: yükleme sonrası 1. açılış reklamsız, 2. açılışta
  reklam normal geldi. 60 sn aralık koruması hızlı zap'ı zaten kapsıyordu.
- **M-22 / M-23 (✅):** Kod okumasında `playNow()` içindeki `playRequest` kapısı ve
  `checkStall()` 12 sn + `lastRestart` mantığı yerinde bulundu; tablodaki 🔧 işareti
  eskiydi.
- **D-34 (✅ cihaz):** Limit 5 MB girildi → kanal açılışında "Günlük veri limiti
  doldu · Limit: 5 MB / Kullanılan: 160,2 MB · Devam edilsin mi?" diyalogu; ana ekran
  başlığında "5 MB ⚠ LİMİT AŞILDI". Test sonunda limit "Yok"a çevrildi.
- **Kapasite:** 7.121.045 B · 400 dosya · **17.517 sınıf** (+2) · imza aynı → ✅.
  GitHub release `v9.66`, site `/indir` 9.66 (asset 200).
- **Açık kalan:** ödüllü reklam akışı (kullanıcı elle), tablet 9.66 kurulumu
  `İndirilenler/UMAY-MULTI-TV-9.66-tablet.apk`, gerçek ölü kanalla onarım denemesi.

**Durum sayısı (24 Kasım 2026, 16. yarı sonu):** toplam **94 madde** · **40 ✅ bitti** ·
**7 👤 kullanıcı işi** · **47 açık**.

**Sonraki oturum:** (1) `MultiActivity` set kaydını URL tabanlı yapmak, (2) tek
rendition kanallarda dürüst kalite etiketi, (3) gerçek ölü kanalla `Repair` testi,
(4) ödüllü reklam akışı, (5) M-13 `kanallar.json`, (6) M-72 tam media3 radyo.

### Oturum 24 Kasım 2026 (on yedinci yarı) — 9.67: 3'lü izleme setleri

Sıradaki açık uç, geçen turun "sonraki oturum" listesindeki `MultiActivity` set
kaydıydı. Yaparken iki ek hata çıktı; üçü birlikte düzeltildi.

- **M-94 (✅):** Kayıt okuma koşulu `!saved.startsWith("ad:")` yüzünden **isimli
  setler hiç uygulanmıyordu**; set seçilince varsayılan dört kanal açılıyordu.
  Koşul kaldırıldı, `ad:` öneki ayrıştırmadan önce atılıyor. Ayrıca kayıt artık
  dört pencerenin **kanal adresini** de yazıyor (`|u:<url1>,...`); açarken
  adresler güncel listede aranıp indekse çevriliyor (bulunamazsa eski indeks).
  Eski kayıtlar adres alanı olmadığı için aynen çalışmaya devam ediyor.
  **Cihazda:** "★ set" → "Test Seti" (3sat/ADRIA/Adventure) kaydedildi,
  2. pencere `№6 arte` yapıldı, set yeniden açıldı → 2. pencere `ADRIA`'ya döndü.
- **M-95 (✅):** Kontrol paneli tek satırda sığmıyordu; 1280 px genişlikte
  "★ set" düğmesi ekran dışında kalıyor, set kaydetmek fiilen imkânsız oluyordu.
  Panel `HorizontalScrollView` içine alındı (kaydırma çubuğu gizli).
  Set listesinde ham kod (`ad:Test Seti|0,1,...`) yerine artık set adı görünüyor.
- **M-25 (✅):** Kodda zaten kapalı: `AudioService.flushUsage()` radyo/arka plan
  sesinde `TrafficStats` farkını günlük sayaca yazıyor, `checkLimit()` limit
  dolunca yayını duraklatıyor; çoklu ekran `MultiActivity.countData()` ile ölçülüyor.
- **M-28 (✅):** Kodda zaten kapalı: `Good.refreshIfNeeded` gün damgasını yalnız
  `download()` başarılı olunca yazıyor.
- **Kapasite:** 7.121.589 B · 400 dosya · **17.517 sınıf** (+0) · +544 bayt ·
  imza aynı → ✅. GitHub release `v9.67`, site `/indir` 9.67.
- **Açık kalan:** kayıtlı sette kanal listede yoksa kullanıcıya bilgi verilmiyor;
  `Prefs` anahtarı hâlâ `url.hashCode()` (M-65); ödüllü reklam akışı (kullanıcı elle);
  tablet 9.67 kurulumu.

**Durum sayısı (24 Kasım 2026, 17. yarı sonu):** toplam **96 madde** · **45 ✅ bitti** ·
**7 👤 kullanıcı işi** · **44 açık**.

**Sonraki oturum:** (1) `Prefs` kullanım anahtarını `url.hashCode()` yerine tam
adrese taşımak (M-65), (2) tek rendition kanallarda dürüst kalite etiketi,
(3) gerçek ölü kanalla `Repair` testi, (4) M-13 `kanallar.json`, (5) M-72 media3 radyo.

### Oturum 24 Kasım 2026 (on sekizinci yarı) — 9.68: sayaç anahtarı + dürüst kalite etiketi

- **M-65 (✅):** `Prefs` kullanım anahtarı `url.hashCode()` idi; farklı adresler aynı
  hash'e düşünce kanal sayaçları karışıyordu. Anahtar tam adrese taşındı
  (`use:<url>` / `useS:<url>`), eski kayıt ilk okumada yeni anahtara kopyalanıyor.
  **Cihazda:** ana liste sayaçları korundu (304,8 MB / 110,9 MB / 117,1 MB), veri kaybı yok.
- **M-96 (✅):** Kalite düğmesi tercihi ("360p") yazıyordu; tek varyantlı kanalda yayın
  720p gelirken etiket yanlış kalıyordu. Etiket oynatıcıdan gelen gerçek yüksekliği
  gösteriyor (Media3 `onVideoSizeChanged`). **Cihazda:** 3sat → "720p".
- **Kapasite:** 7.122.373 B · 400 dosya · **17.517 sınıf** (+0) · +784 bayt · imza aynı → ✅.
- **Açık kalan:** tek varyantlı kanalda tercih hâlâ uygulanamıyor (transcode yok, etiket
  doğru söylüyor); gerçek ölü kanalla `Repair` testi; M-13 `kanallar.json`; M-72 media3 radyo;
  ödüllü reklam akışı (kullanıcı elle); tablet 9.68 kurulumu.

**Durum sayısı (24 Kasım 2026, 18. yarı sonu):** toplam **97 madde** · **47 ✅ bitti** ·
**7 👤 kullanıcı işi** · **43 açık**.

**Sonraki oturum:** (1) tek varyantlı kanallarda "bu kanal yalnız 720p yayınlıyor" bilgisi
(kalite diyalogunda), (2) gerçek ölü kanalla `Repair` testi, (3) M-13 `kanallar.json`,
(4) M-72 media3 radyo, (5) kayıtlı sette bulunamayan kanal uyarısı.

### Oturum 24 Kasım 2026 (on dokuzuncu yarı) — 9.69: reklam yerleşimi, set uyarısı, ayar tazeleme

**Sayım düzeltmesi:** önceki yarı raporlarındaki "✅ bitti" sayıları elle tutuluyordu ve
şişmişti. Bu yarıda liste tablosu **programla sayıldı**: tabloda 93 madde + rapor
içindeki M-88…M-93 (7 madde) = **100 madde**. Doğru durum: **50 ✅** ·
**50 açık** (41 kod · 7 kullanıcı · 2 cihaz testi).
Aşağıdaki madde satırları bu sayıma göre güncellendi.

- **M-43 (✅):** Reklam şeridi (sarı "Reklam izle") banner alanına yapışıktı; banner
  yüklenirken/başarısızken kullanıcı banner yerine şeride dokunup farkında olmadan
  reklam açabiliyordu. Şeride 6 dp üst boşluk verildi.
- **M-97 (✅):** Kayıtlı setteki kanal güncel listede yoksa uygulama sessizce başka
  kanalı açıyordu. `resolveUrls` artık eşleşmeyenleri sayıyor ve çoklu ekran açılırken
  "Kayıtlı setteki N kanal bu listede yok (kaldırılmış olabilir)" uyarısı gösteriliyor.
  **Cihazda:** ülke Türkiye'ye alındı, Almanya kanallarıyla kaydedilmiş "Test Seti"
  açıldı → "4 kanal bu listede yok" uyarısı + TR kanallarıyla açıldı (çökme yok).
  Bu aynı zamanda D-44'ü (ülke değişince set) fiilen denemiş oldu.
- **M-98 (✅):** `SettingsActivity` ülke değerini yalnız `onCreate`'de yazıyordu;
  ülke değiştirip geri dönünce satır eski ülkeyi gösteriyordu (cihazda TR'ye geçildiği
  hâlde "Almanya" yazıyordu). Değer `onResume`'da tazeleniyor; cihazda TR→DE dönüşünde
  satır hemen "Almanya" oldu.
- **Kapasite:** aşağıdaki sürüm notunda; sınıf sayısı değişmedi.
- **Açık kalan:** tek varyantlı kanallarda kalite tercihi uygulanamıyor (etiket doğru);
  gerçek ölü kanalla `Repair` testi; M-13 `kanallar.json`; M-72 media3 radyo;
  ödüllü reklam akışı (kullanıcı elle); tablet 9.69 kurulumu.

**Durum sayısı (24 Kasım 2026, 19. yarı sonu):** toplam **100 madde** · **50 ✅ bitti** ·
**50 açık** (41 kod · 7 kullanıcı · 2 cihaz testi).

**Sonraki oturum:** (1) gerçek ölü kanalla `Repair` testi (yapay ölü URL'li test listesi),
(2) M-13 `kanallar.json`, (3) M-72 media3 radyo, (4) M-33 `ChannelRepo` değişmez liste,
(5) tek varyantlı kanal bilgisi kalite diyalogunda.
