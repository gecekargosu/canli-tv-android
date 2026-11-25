# İş listesi — "UMAY Multi TV v9.70 — Yayın Öncesi Tam Rapor" (PDF)

Kaynak: `UMAY Multi TV v9.70 — Yayın Öncesi Tam Rapor.pdf` (25 Kasım 2026, 9 sayfa).
Rapor APK'yı açıp inceleyerek yazılmış; cihazda çalıştırma yapılmamış.
Durum sütunu bu projede **koda bakılarak** doğrulanmıştır.

| # | İş | Raporun gerekçesi | Kodda durum | Öncelik |
|---|----|-------------------|-------------|---------|
| PDF-01 | Kanal listesinden 12 lisanslı yabancı kanalı çıkar (Disney Jr., BabyTV, Spacetoon, BBC Drama, Doctor Who Classic, Wonder, InWonder, Inside Crime, AVA Series, MBC Masr Drama, MBC Drama USA, Persiana Türkiye) | Telif/Play incelemesi riski | Liste veride (`channels.m3u` + `good_tr.json`); kod değişikliği değil, veri temizliği | P0 |
| PDF-02 | 7 alakasız/siyasi kanalı çıkar (Al-Zahra TV Turkic, Almahriah TV, Elsharq TV, Mekameleen TV, UMSA TVU, Cosmos TV, Can Tho TV 2) | Türk kullanıcıyla ilgisiz, incelemede soru | Veri | P0 |
| PDF-03 | Yabancı çocuk kanallarını çıkar, hedef kitleyi 13+ tut (TRT Çocuk gibi kamu kanalları kalabilir) | Play "Aileler" politikası + reklam kısıtı | Veri + mağaza beyanı | P0 |
| PDF-04 | TV8 `?app=…&ce=3` yedek adresini ve Show TV/NOW ölü yedeklerini sil | Başka uygulamanın erişim anahtarını kullanmak gibi görünüyor | `ChannelRepo.FALLBACK` içinde duruyor (doğrulandı) | P0 |
| PDF-05 | Reklam sıklığını yumuşat: her 3. zap, en az 180 sn | Agresif reklam = düşük puan, AdMob "beklenmedik reklam" | `Ads.ZAPS_BEFORE_INTERSTITIAL = 1`, `INTERSTITIAL_GAP_MS = 20 sn` (doğrulandı) | P0 |
| PDF-06 | "Reklam test modu" düğmesini ve "reklam kimliğini oku/kopyala" ekranını release'ten çıkar | Kullanıcı yanlışlıkla test reklamına geçer, gelir düşer | `SettingsActivity.adTestDialog` + `AdsTest` release'te açık (doğrulandı) | P0 |
| PDF-07 | Kayıt özelliğini ilk sürümde kapat veya yalnızca kamu kanallarında aç | .ts dosyası galeriden paylaşılabilir, telif riski | `HlsRecorder` her kanalda açık | P0 |
| PDF-08 | Ölü hesap metinlerini sil (Rusça "Аккаунт", "6-значный код", "Hesabın yoksa misafir olarak devam") | Hesap sistemi yok ama anlatılıyor | Metinler hâlâ var | P1 |
| PDF-09 | ExoPlayer'ı çoklu ekrana ve ses servisine yay (MediaSessionService) | Üçlü ekran/arka plan sesi eski motorda | `MultiActivity` ve `AudioService` hâlâ `MediaPlayer` (doğrulandı) | P1 |
| PDF-10 | ExoPlayer hata ayrımını doğrula (`BehindLiveWindowException`, `errorCode`); uçak modu 5 sn testi | Geçici kopmada sağlam kanal "ölü" sayılıp gizlenebilir | **M-99 ile aynı alan** — ölü/geçici ayrımı yok | P0 |
| PDF-11 | Tüm sabit Türkçe metinleri `Lang`'e taşı, önce EN+TR tamamla | TR ~945 metin, EN/RU/AR kısmi; yarı Türkçe uygulama | Kısmen | P1 |
| PDF-12 | R8/minify + shrinkResources aç | APK kolay kopyalanıyor, boyut büyük | `minifyEnabled false` (doğrulandı) | P1 |
| PDF-13 | Destek için ayrı e-posta adresi (kişisel Gmail yerine) | Kişisel adresin açığa çıkması/spam | `cengizkilic1980@gmail.com` metinlerde | Kullanıcı |
| PDF-14 | Gizlilik sayfasını yeniden kontrol et, `/app-ads.txt` açılıyor mu bak, depoyu private yap | Play/AdMob doğrulaması | Site + GitHub — kullanıcı kararı | Kullanıcı |
| PDF-15 | Play Console: AAB yükle, formlar, mağaza metni, test reklamsız ekran görüntüleri, 12 tester | Yayın süreci | AAB üretiliyor (`make-aab.sh`) | Kullanıcı |
| PDF-16 | `setMaxAdContentRating` değerini G'den PG'ye çıkar | G doldurma oranını ciddi düşürür | `MAX_AD_CONTENT_RATING_G` (doğrulandı) | P1 |
| PDF-17 | Grup adlarını düzgün yaz ("Muzik"/"Cocuk"/"Eglence" → "Müzik"/"Çocuk"/"Eğlence") | Amatör görünüm | Veri kaynaklı; radyo tarafı düzgün, TV grupları veriden geliyor | P2 |
| PDF-18 | MainActivity'nin portre kilidini kaldır, yatay/tablet düzeni ekle | Play "büyük ekran" kalite sinyali | Portre kilitli | P2 |
| PDF-19 | Reklam kurallarını `good/config.json`'dan uzaktan okunur yap | Sürüm çıkarmadan ayar | Sabitler kodda | P2 |
| PDF-20 | GitHub Actions ile günlük kanal sağlık taraması | Ölü kanal sayısını düşük tutar | `tools/` içinde yerel araçlar var | P2 |
| PDF-21 | Hazır ülke sayısını artır, yurtdışı IP ile test et, EN/DE mağaza metni | Yurtdışı Türkler + yüksek eCPM | 50+ ülke var, test yok | P2 |
| PDF-22 | Ulusal kanallara yazılı izin e-postası gönder | Telif riski yönetimi | — | Kullanıcı |
| PDF-23 | Mediation / native reklam / Pro (Play Billing) / Firebase Analytics | Gelir artırma | Yok | P3 |
| PDF-24 | JUnit/Espresso testleri + CI | Regresyon güvenliği | Yok | P3 |

## Raporun "bu akşam" listesi (özet)
1–7 arası maddeler + AAB + Play Console adımları. Raporun kendi sırası:
liste temizliği → reklam yumuşatma → test modunu gizle → ölü metinler → 9.71 (versionCode 80) → kayıt kısıtı → site → Play Console.

## Notlar
- Rapor "AAB yükle" diyor; bizim `make-aab.sh` zaten var, imza aynı (SHA-256 `f3996c67…`).
- Raporun en kritik tespiti kanal listesi: 145 kanaldan 126'ya inmek. Bu bir **veri** işi, kod işi değil.
