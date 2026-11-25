# MASTER PLUS — birleşik iş listesi (25 Kasım 2026)

İki bağımsız raporun birleştirilmiş hâli:
- **PDF raporu** — `docs/ISLER-PDF-RAPOR-970.md` (APK incelemesi, yayın odaklı)
- **Antigravity raporu** — `docs/ISLER-ANTIGRAVITY-990.md` (derin analiz, UX/kod kalitesi)
- Mevcut liste: `docs/MASTER-IS-LISTESI-2026-11-24.md` (103 madde · 54 ✅)

Tekrarlar birleştirildi; her satırın kaynağı yazılı. Durum kodu: ✅ bitti · 🔧 açık · 👤 senin işin · 🧪 cihaz testi.

---

## 1. Özet: kaç iş kaldı?

| Grup | Adet | Ne demek |
|------|------|----------|
| **P0 — yayın blokeri** | 9 | Play'e göndermeden önce yapılmalı (gerçek risk) |
| **P1 — ilk hafta** | 13 | Yayın sonrası ilk sürümde olması iyi |
| **P2 — sonraki sürümler** | 8 | Konfor/kalite işleri |
| **P3 — büyük refactor** | 3 | Sürüm değil, ayrı proje |
| **👤 Sende** | 6 | Play Console, e-posta, ekran görüntüsü, onay |

**Tek parmağımızı basacağımız yer: P0'ın 9 maddesi.** Bunların 6'sı kod/veri (bende), 3'ü sende.

---

## 2. Play Store'u ne engelliyor? (senin sorunun cevabı)

Yayına çıkmayı **teknik** olarak engelleyen bir şey yok: APK/AAB imzalı, debuggable değil, `allowBackup=false`, cleartext kapalı, hedef SDK 36.
Ama **ret riski** yaratan 5 şey var — hepsi P0:

1. **Kanal listesi (en büyük risk).** 12 lisanslı yabancı içerik + 7 alakasız/siyasi kanal + yabancı çocuk kanalları. Telif şikâyeti → ret veya yayından kaldırma.
2. **TV8 `?app=…` yedek adresi.** Başka uygulamanın erişim anahtarı gibi görünüyor; kaldırılmalı.
3. **Reklam sıklığı.** Şu an `ZAPS_BEFORE_INTERSTITIAL=1` → **her zap'ta** geçiş reklamı, 20 sn arayla. Play "reklam politikası" ve AdMob "beklenmedik reklam" riski.
4. **Test modu + reklam kimliği ekranı** kullanıcıya açık. Gelir düşürür, gizlilik beyanıyla çelişir.
5. **Kayıt (HlsRecorder) her kanalda açık.** Telifli yayının .ts dosyası galeriden paylaşılabilir → ilk sürümde kapatmak en güvenlisi.

Bunlar kapanınca Play'e kapalı test için yükleme yapılabilir. (Kapalı test: 12 kullanıcı / 14 gün şartı — Play Console'dan doğrula.)

---

## 3. P0 — yayın blokerleri (9)

| ID | İş | Kaynak | Durum | Kabul testi |
|----|----|--------|-------|-------------|
| MP-01 | Kanal listesini temizle: 12 lisanslı yabancı + 7 alakasız/siyasi → **126 kanal** | PDF-01/02/03 | ✅ 9.71 | TR listesinde bu adlar aranamıyor; resmî sayaç 126 |
| MP-02 | TV8 `?app=` anahtarlı adres kanaldan ve yedekten kaldırıldı (TürkNet yansısı) | PDF-04 | ✅ 9.71 | Kodda `app=` geçmiyor; kanal açılışında yedek denemesi TV8 için yok |
| MP-03 | Reklam sıklığı: her **3.** zap + en az **180 sn** arayla | PDF-05, ANT-14 | ✅ 9.71 | 3 kanal gez, reklam yok; 4.'de çıkıyor |
| MP-04 | Test modu ve "reklam kimliğini kopyala" ekranı release'te gizli | PDF-06 | ✅ 9.71 | Ayarlar'da satır yok (debug'da var) |
| MP-05 | Kayıt yalnız TRT/TBMM/Diyanet kanallarında | PDF-07 | ✅ 9.71 | Özel kanalda kayıt düğmesi pasif/uyarı veriyor |
| MP-06 | Ölü kanal / geçici kopma ayrımı: 45 sn kuralı | PDF-10, **M-99** | ✅ 9.71 (RFO + BLK cihazda) | BLK açılınca ~45 sn'de hata; uçak modu 5 sn aç-kapa sonrası kanal gizlenmiyor |
| MP-07 | Ödüllü/geçiş sayacı doğru anda sayılsın, test modu sayılmasın | bu oturum | ✅ 9.71 | Sayaç yalnız gerçek gösterimde artıyor |
| MP-08 | AAB üret, imzayı doğrula, Play Console'a kapalı teste yükle | PDF-15 | 👤 | AAB aynı SHA-256 ile imzalı |
| MP-09 | Mağaza metni + test reklamsız ekran görüntüleri + 12 tester + formlar | PDF-15 | 👤 | Console'da "kapalı test incelemede" |

## 4. P1 — ilk hafta (13)

| ID | İş | Kaynak | Durum |
|----|----|--------|-------|
| MP-10 | ExoPlayer'ı çoklu ekrana taşı (MediaPlayer → media3) | PDF-09, ANT-01 | 🔧 |
| MP-11 | Radyo/servis tarafını media3'e taşı (MediaSessionService) | PDF-09 | 🔧 |
| MP-12 | Radyo kapanınca kayıt oturumunu kapat (dosya birikmesi) | ANT-02 | ✅ 9.74 |
| MP-13 | Çoklu ekranda geri tuşu ana listeye dönsün | ANT-03 | ✅ 9.74 |
| MP-14 | Kalite seçimi ↔ veri tasarrufu tutarlılığı (kalite seçilince tasarruf kapanır, "otomatik"e dönülebilir) | ANT-05 | ✅ 9.74 |
| MP-15 | 720p/1080p varyantı olmayan kanalda otomatik düşüş + tek satır açıklama | ANT-04 | ✅ 9.74 |
| MP-16 | Sabit metinleri `Lang`'e taşı, EN+TR tamamla (RU/AR sonra) | PDF-11, ANT-10 | 🔧 |
| MP-17 | Ölü hesap metinleri silindi (4 anahtar) | PDF-08 | ✅ 9.71 |
| MP-18 | `setMaxAdContentRating` G → PG | PDF-16 | ✅ 9.71 |
| MP-19 | R8/minify + shrinkResources aç, sürüm doğrula | PDF-12 | 🔧 |
| MP-20 | Üst bar ikonlarını 48dp dokunma alanına çıkar | ANT-07 | ✅ 9.74 |
| MP-21 | Liste düzenleme: "kaydet" ve geri ekleme donmasını düzelt | ANT-06 | 🧪 önce üret |
| MP-22 | Gizlilik sayfası + `/app-ads.txt` + depoyu private yap | PDF-14 | 👤 |

## 5. P2 — sonraki sürümler (8)

| ID | İş | Kaynak | Durum |
|----|----|--------|-------|
| MP-23 | MainActivity portre kilidini kaldır + yatay tablet düzeni | PDF-18 | 🔧 |
| MP-24 | Reklam kurallarını `good/config.json`'dan uzaktan oku | PDF-19 | 🔧 |
| MP-25 | GitHub Actions ile günlük kanal sağlık taraması | PDF-20 | 🔧 |
| MP-26 | Yurtdışı IP testi + EN/DE mağaza metni | PDF-21 | 👤 |
| MP-27 | Grup adları düzgün ("Muzik" → "Müzik") | PDF-17 | ✅ 9.74 |
| MP-28 | Ayarlar "kullanım verisi" satırı taşması | ANT-08 | ✅ 9.74 |
| MP-29 | ListView → RecyclerView (300+ öğe) | ANT-12 | 🔧 |
| MP-30 | Keystore şifresini ortam değişkenine taşı | ANT-13 | 🔧 |

| MP-34 | **Hata günlüğü**: açılış, kanal hazır, oynatıcı hata kodu, yeniden bağlanma, ölü kanal nedeni, ağ kopması, reklam dolmama — zaman damgasıyla, Ayarlar'dan okunur/gönderilir | bu oturum | ✅ 9.72 |

## 6. P3 — büyük işler (3)

| ID | İş | Kaynak |
|----|----|--------|
| MP-31 | MainActivity/Ads sınıflarını MVP/MVVM'e böl | ANT-11 |
| MP-32 | Mediation / native reklam / Pro (Play Billing) / Firebase Analytics | PDF-23 |
| MP-33 | JUnit + Espresso testleri, CI | PDF-24 |

---

## 7. Eski master listeyle eşleşme

| Bu liste | Eski madde |
|----------|------------|
| MP-06 | **M-99** (ölü kanal sonsuz yükleme) — aynı iş |
| MP-07 | **M-26** reklam sayacı tarafı (kayıt maddesi değil; sayaç bu oturumda düzeltildi) |
| MP-10/11 | **M-72** (media3 radyo) ile aynı alan |
| MP-16 | **M-34** (i18n) |
| MP-19 | **M-40** (R8) |
| MP-23 | **M-37** (inset/çentik/yatay) |
| MP-01/02 | **M-13** (`kanallar.json` veri kaynağı) ile birlikte yapılmalı |

Eski listedeki diğer açık maddeler (M-54 in-app review, M-55 analytics, M-57 EPG, M-59 yedekleme, M-33 kalanlar…) bu listeyle çelişmiyor; P2/P3 altında yaşıyor.

---

## 8. Sıradaki 6 adım (bu sırayla)

1. **MP-07 + MP-06 doğrula** → 9.71 sürümü (sayaç + ölü kanal) → cihaz testi → GitHub/site.
   _Durum (25 Kasım): **9.71 yayınlandı** — cihazda doğrulandı, GitHub release + site güncel._
2. **MP-03 reklam sıklığı** (tek satır sabit) + **MP-04 test modu gizle** → aynı sürümde.
3. **MP-02 TV8/Show/NOW yedeklerini sil** (küçük, temiz).
4. **MP-01 kanal listesi temizliği** (veri; 19 kanal + yabancı çocuklar).
5. **MP-05 kayıt kısıtı** (ilk sürüm için kapat).
6. **MP-08/09** sende: AAB → kapalı test.

Bu 6 adım bitince "Play'e yüklenebilir" noktasına geliyoruz; P1 maddeleri yayın sonrası ilk haftada.

---

## 9. 9.73 oturumu (25 Kasım 2026, akşam)

| ID | İş | Durum | Kanıt |
|----|----|-------|-------|
| MP-35 | Reklam isteği fırtınası (saniyede 1 istek) ve ödüllü otomatik yeniden deneme | ✅ 9.73 | `AdManager` backoff 30 sn → 5 dk |
| MP-36 | Banner hatası yanlış etiketi + günlük taşması | ✅ 9.73 | "banner yüklenemedi: 3 NO_FILL…", 30 dk throttle |
| MP-37 | Reklam teşhisi ekranı (rıza, birimler, hazır reklam, son hata, gösterim) | ✅ 9.73 | Cihazda "rıza EVET · geçiş var · ödüllü var · 206 gösterim" |
| MP-38 | Reklam sayacı/test modu kullanıcıya görünmesin; sahip moduna taşı | ✅ 9.73 | Gizli mod: Ayarlar başlığına 5 dokunuş |
| MP-39 | "Reklamları kapat" (yalnız sahip cihazı) | ✅ 9.73 | Açıkken banner yok, istek gitmiyor |
| MP-40 | Hata günlüğü: isteğe bağlı günlük gönderim + gizlilik metni | ✅ 9.73 | `/api/log` + D1, varsayılan kapalı; cihazdan 6119 bayt kayıt |
| MP-41 | Hata günlüğü penceresi: sarma + en yeniye kaydırma | ✅ 9.73 | Cihazda görüldü |
| MP-42 | ATV kaynağı | ✅ 9.73 | 720p birincil, 480p/master yedek; cihazda açıldı |
| MP-43 | İnternet/DNS yokken kanalı ölü işaretlememe | ✅ 9.73 | `Net.online` + `isDnsError` |
| MP-44 | README/mağaza/Play veri beyanı metinleri | ✅ 9.73 | `README.md`, `docs/play/PLAY-UYGUNLUK-2026-11-25.md` |
| MP-08/09 | AAB yükleme, Data Safety, 12 tester | 👤 sende | AAB: `releases/v9.73/UMAY-MULTI-TV-9.73.aab` |

Kalan açık işler: MP-10/11 (çoklu ekran + radyo media3), MP-19 (R8), MP-23 (yatay tablet),
MP-24 (uzaktan reklam kuralı), MP-25 (günlük sağlık taraması), MP-29/30.
ATV yayını kullanıcı isteğiyle **sona bırakıldı**: 9.74'te 720p adres oynatılıyor ama
kullanıcı "ATV'de sorun var" dedi; ayrı oturumda ele alınacak (bkz. D-70).

---

## 10. 9.74 oturumu (25 Kasım 2026, akşam)

| ID | İş | Durum | Kanıt |
|----|----|-------|-------|
| MP-13 | Çoklu ekranda geri tuşu ana listeye | ✅ 9.74 | Cihazda geri → ana başlık/düğmeler geri geldi |
| MP-14 | Kalite ↔ veri tasarrufu tutarlılığı | ✅ 9.74 | Ayarlar: "Kapalı: seçili kalite kullanılıyor (360p)"; kapatınca 360p |
| MP-15 | Varyantsız kanalda otomatik düşüş | ✅ 9.74 | 360p tercihle ATV: günlük "varyant yok: tercih 360p, gelen 720p", kanal 720p açıldı |
| MP-12 | Kayıt oturumu kapanışı | ✅ 9.74 | Çoklu ekran kapat/geri yolunda `recorder.stop()` (PlayerActivity onDestroy zaten vardı) |
| MP-27 | Grup adları | ✅ 9.74 | Cihazda "Eğlence"; "Eglence" kalmadı |
| MP-20 | 48dp dokunma alanı | ✅ 9.74 | Üst bar düğmeleri `minHeight/minWidth 48dp` |
| MP-28 | Ayarlar satır taşması | ✅ 9.74 | Başlık tek satır, açıklama 2 satır, değer 120dp ile kesiliyor |

---

## 11. 9.77–9.78 oturumu (25 Kasım 2026, gece) — neredeyiz?

### Biten işler (cihazda doğrulandı)

| ID | İş | Durum | Kanıt |
|----|----|-------|-------|
| MP-10 | **Çoklu ekranı ExoPlayer'a taşı** (MediaPlayer → media3) | ✅ 9.77 | 3 pencere oynuyor; veri sayacı 318,3 → 324,0 MB canlı artıyor |
| MP-45 | Çoklu ekranda veri **iki kez** sayılıyordu (watchdog + oturum) | ✅ 9.77 | Tek kaynak: `CountingDataSource`; çıkışta kayıp/çift yazma yok |
| MP-46 | Çoklu ekranda veri **hiç artmıyordu** (UID/TrafficStats 0 dönüyor) | ✅ 9.77 | Kanalların satırları da güncelleniyor (ATV 61,7 MB…) |
| MP-47 | **Show TV açılmıyordu**: TürkNet yansısının varyant listesi boş dönüyor | ✅ 9.78 | Kaynak showtv.com.tr'nin kendi Daion adresine alındı; cihazda oynadı (360p) |
| MP-48 | Üst bar: ikonlar 44→56dp, tam genişlik; başlık 15→19sp; logo 34→46dp | ✅ 9.78 | Ekran: 5 ikon eşit, "UMAY Multi TV" tam |
| MP-49 | Alt başlıkta "kayıt" yazıyordu ama **giriş yoktu** → ⏺ düğmesi | ✅ 9.78 | Kayıtlarım ekranı açılıyor |
| MP-50 | Sekmeler/arama/No/Git/✏️ yazıları büyütüldü; "Gizlenenler" → "Gizli" | ✅ 9.78 | Hepsi tek satırda, kırpma yok |
| MP-51 | **Kalite seçimi ana ekrana** (yeni satır açmadan, sekme satırına) | ✅ 9.78 | "🎚 Oto/360p/720p" düğmesi; seçim veri şeridine yansıyor |
| MP-52 | Sıra: sekmeler üstte, **arama satırı listenin hemen üstünde** | ✅ 9.78 | Ekran |
| MP-53 | **Radyo ekranı sola kaymıştı**: `setFitsSystemWindows(true)` 12dp yan boşluğu sıfırlıyordu | ✅ 9.78 | Radyo artık 12dp içeride; veri şeridi 318,3 MB |
| MP-54 | "Sırala" kaldırıldı (sırayı değiştirmiyordu); Düzenle sekme satırına | ✅ 9.77 | — |
| MP-55 | "Reklam izle" şeridi sarıydı → uygulama turuncusu | ✅ 9.77 | — |

### Doğrulanan sayaçlar (9.78, cihaz)

- Tek kanal (Show TV 360p, ~18 sn): 313,4 → 316,9 MB
- Radyo ekranı: 318,3 MB (aynı gün toplamı, tutarlı)
- 3'lü ekran: 318,3 → 324,0 MB (canlı), çıkışta korunuyor

### Bu plana EKLENEN yeni maddeler

| ID | İş | Neden | Durum |
|----|----|-------|-------|
| MP-56 | **Her ekranda sabit reklam yuvası** (ana sayfa, tekil, çoklu, radyo) — yuva her zaman ayrılır, dolmazsa kendi tanıtım şeridimiz durur | Reklam geliri sürekliliği | ✅ yuva var (9.73'ten beri); AdMob dolmuyor → **mağaza linki açılınca dolacak** |
| MP-57 | **Kaynak sağlık taraması** (MP-25'in acil hâli): kanal URL'lerini günlük dene, master+varyant+ilk segment 200 mü? Bozuksa listeden çıkar/uyarı ver | Show TV örneği: yayın sessizce öldü, kullanıcı gördü | 🔧 sıradaki |
| MP-58 | **Yedek kaynak zorunluluğu**: pinli 5 kanal + ilk 20 kanal için en az 1 doğrulanmış yedek URL | Show TV/ATV tek kaynakla kalıyor | 🔧 kısmi (Show TV, ATV, TV8, KanalD yedekli) |
| MP-59 | Ödüllü reklam sıklığı: dokümanla son hâli (3. zap / 180 sn / 15 dk) **raporla** ve Play metniyle eşleştir | Reklam politikası | ✅ kodda; doküman güncel |
| MP-60 | Logoya hafif parlama animasyonu (1,7 sn alpha 1→0,72) | Dikkat çekme | ✅ 9.78 — abartılıysa tek satırla kapatılır |

### Kalan gerçek işler (öncelik sırası)

1. **MP-57** kaynak sağlık taraması + **MP-58** yedekler → "kanal açmıyor" şikâyetini kökten keser.
2. **MP-11** radyo/servisi media3'e taşı (AudioService hâlâ MediaPlayer).
3. **MP-19** R8/minify, **MP-23** yatay tablet, **MP-29** RecyclerView.
4. **MP-08/09** (sende): AAB → kapalı test → 12 tester / 14 gün.
