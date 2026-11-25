# Doğrulanacaklar listesi

Bu dosya, **kodda bitmiş ama cihazda/hesapta doğrulanmamış** işleri tutar.
Her şey bittiğinde bu listeyi tek tek geçeceğiz. Durum: ⬜ bekliyor · 🟨 kısmen doğrulandı · ✅ doğrulandı

---

## A. Cihaz testi bekleyenler (uygulama)

| # | İlgili madde | Ne doğrulanacak | Nasıl | Durum |
|---|---|---|---|---|
| D-01 | M-03 | UMP rızası yokken **hiç reklam isteği gitmiyor**, rıza sonrası banner yükleniyor | Yeni kurulum + AB IP'si ya da Ayarlar → Reklam gizlilik tercihleri; logcat `UmayAds` | 🟨 (TR'de banner yüklendi) |
| D-02 | M-08 | HTTP radyolar yeni HTTPS kuralıyla çalıyor (Radyo 7, NTV Radyo, Süper FM) | Radyo listesinden tek tek çal | ⬜ |
| D-03 | M-22 | Hızlı zap'ta eski kanalın geç cevabı yenisini ezmiyor | 5 kanal arasında hızlı geçiş | ⬜ |
| D-04 | M-23 | TV'de 15 sn sonra 3 kez yeniden bağlanma, sonra sonraki kanal | Wi-Fi kapat→aç, kanal izle | 🟨 (radyo doğrulandı) |
| D-05 | M-70/M-74 | Reklam açıkken TV duruyor, kapanınca **canlıya yeniden bağlanıyor** | 15 dk izle veya test reklamı | ⬜ |
| D-06 | M-28 | Liste yenileme damgası yalnız başarıda | Uçak modu → aç, ertesi gün kontrol | ⬜ |
| D-07 | M-25 | Radyo veri sayacı + **günlük limit dolunca radyo duruyor** | Ayarlar → limit 1 MB → radyo çal | 🟨 (sayaç arttı, limit denenmedi) |
| D-08 | M-16 | 30'dan az kanallı ülkede "yakında" etiketi | Ülke değiştir (örn. Almanya) | ⬜ |
| D-09 | M-69 | Hata bildir → 📧 E-posta diyaloğu ve gönderim | Ayarlar → Hata bildir | ⬜ (kod derlendi, ekran açılmadı) |
| D-10 | M-73 | "🎧 X çalıyor · Kapat" şeridi **diğer ekranlarda** yok (sadece ana ekran) | Ayarlar/Ülke ekranında radyo çal | 🟨 (ana ekranda ✅) |
| D-11 | M-71 (banner) | Gerçek AdMob banner dolduğunda bar görünüyor ve gösterim sayacı artıyor | logcat `UmayAds` + AdMob paneli | 🟨 (bir kez Paycell doldu) |
| D-12 | — | Çoklu ekran (3'lü) + kayıt + uyku zamanlayıcı regresyonu | Sırayla dene | ⬜ |

## B. Hesap / Play Console işleri (senin tarafın)

| # | Madde | Ne gerekiyor | Durum |
|---|---|---|---|
| P-01 | M-01 | Resend anahtarı rotasyonu (v9.32 zip'inde sızmıştı) | ⬜ |
| P-02 | M-07 | Keystore şifresinin düz metinden çıkarılması + upload key reset | ⬜ |
| P-03 | M-09 | Data Safety formu (`docs/play/PLAY-YAYIN-HAZIRLIK.md` tablosundan) | ⬜ |
| P-04 | M-10 | app-ads.txt için **kök alan adı** | ⬜ |
| P-05 | M-18 | Avukat görüşü (telif) | ⬜ |
| P-06 | M-60 | Play Console'da uygulama + kapalı test (12 kişi / 14 gün) | ⬜ |
| P-07 | — | Mağaza görselleri: reklamsız yeni ekran seti (M-11) | ⬜ |
| P-08 | — | AdMob "sınırlı reklam sunumu" limitinin kalkması (Play bağlantısı sonrası) | ⬜ |

## C. Kodda bitmiş, ürün olarak yarım kalanlar

| # | Madde | Eksik parça | Durum |
|---|---|---|---|
| K-01 | M-13 | `kanallar.json` üretiliyor ama **uygulama hâlâ m3u'dan** besleniyor | ⬜ |
| K-02 | M-15 | Sunucu tarafı "review" hak filtresi (kanallar.json üstüne) | ⬜ |
| K-03 | M-20 / M-72 | Radyo hâlâ `MediaPlayer`; tam media3 geçişi yapılmadı | ⬜ |
| K-04 | M-21 | Çoklu ekran hâlâ `MediaPlayer`; media3 + kalite tavanı yok | ⬜ |
| K-05 | M-17 | Kullanıcı M3U ekleme akışının uçtan uca testi | ⬜ |

---

## Doğrulanmışlar (özet)

- TRT Haber oynatma, sayaç (ekran/ana ekran/Ayarlar aynı), 145 kanal listesi.
- Radyo: GENÇ KRAL FM çalıyor, "✅ Bağlantı iyi", **Tekrar dene** ve **Sonraki kanal** butonları.
- Radyo wifi kapat/aç sonrası toparlanma; radyo seçiminde geçiş reklamı yok.
- Radyo veri sayacı artıyor (2,3 → 2,5 MB).
- Ana ekranda "🎧 … çalıyor · ⏹ Kapat" şeridi; Kapat servisi durduruyor.
- Banner barı ana ekran/radyo/ayarlar ekranlarında sabit; bir kez gerçek reklam doldu.

---

## Oturum 24 Kasım 2026 (7. yarı) — 12 maddenin test sonucu (v9.55)

Test yöntemi: gerçek cihaz (MIUI, `Z5R87DU8V4LBSOVG`), 9.54/9.55 kurulumu, `adb`
dokunma + `uiautomator dump` + `logcat` + ekran görüntüsü. "Kanıt" sütunu ölçülen veriyi verir.

| # | Madde | Sonuç | Kanıt / neden |
|---|-------|-------|----------------|
| D-01 | Reklam rıza kapısı (UMP) | **KISMEN ✅** | logcat: `riza bilgisi alinamadi: FormError` → `startAds` → `canRequestAds()` TR'de true döndü → SDK hazır. Kapı çalışıyor (rıza yoksa `return`), ama **rızasız EEA durumu cihazda üretilemedi**. |
| D-02 | `http://` radyolar çalıyor mu | **GEÇERSİZ (kapandı)** | `grep "http://" RadioRepo.java` → 0 sonuç: tüm radyo adresleri https'e taşınmış. `network_security_config` cleartext kapalı. Test edilecek http kaynağı kalmadı. |
| D-03 | Hızlı zap'ta doğru kanal | **KISMEN ✅** | TRT Haber'den 5 hızlı "sonraki" → kanal değişti (TOD tanıtım kartı geldi), eski kanal ekranda kalmadı. Sıra numarası (6/145) başlık gizlendiği için okunamadı. |
| D-04 | Wi-Fi kesintisinde TV toparlanması | **YAPILAMADI** | MIUI, `adb shell svc wifi disable` komutunu sessizce yok saydı (sayaç 25,3→31,2 MB arttı = ağ açık kaldı). Elle uçak moduyla denenmeli. |
| D-05 | Reklam sonrası canlıya dönüş (M-74) | **YAPILAMADI** | Reklam 15 dk'da bir geliyor; test süresinde tam ekran reklam çıkmadı. Kod tarafı derlendi, cihazda gözlem gerekli. |
| D-06 | Gün damgası yalnız başarıda | **YAPILAMADI** | Release imzalı APK'da `run-as` yok; prefs okunamıyor. Ekran/günlük üzerinden bakılacak. |
| D-07 | Radyo veri limiti durduruyor | **KISMEN ✅** | Sayaç canlı: "Bugün kullanılan veri: 4,3 MB" → 5,8 MB → 31,2 MB (TV) ve radyo dinlerken arttı. Limitin **durdurduğu** an test edilmedi (limit değeri UI'dan düşürülmeli). |
| D-08 | Ülke sekmesinde "yakında" | **YAPILAMADI** | Ülke ekranına otomatik geçilemedi (menü yolu farklı). Elle bakılacak. |
| D-09 | Hata bildir → e-posta | **YAPILAMADI** | Ayarlar → Hata bildir akışı bu oturumda açılmadı. |
| D-10 | "🎧 çalıyor" şeridi diğer ekranlarda | **KOD: SADECE ANA EKRAN** | `radioNow` yalnız `MainActivity`'de; About/radyo listesinde yok. İstenen davranış netleşmeli: "diğer ekranlar" kapsamı. |
| D-11 | Banner gerçek dolum | **✅** | Ana ekranda **hepsiburada** banner'ı görüldü (bugün 14:29), radyo listesinde de doldu. No-fill dönemleri sürüyor (AdMob hesap limiti). |
| D-12 | Çoklu ekran regresyonu | **YAPILAMADI** | 3'lü mod bu oturumda açılmadı. |

### Bu oturumda test sırasında bulunan ve düzeltilen 3 gerçek hata

1. **A/V senkron (M-75):** varyant URL'i yerine master playlist + `setMaxVideoSize`.
2. **Radyo araması sonuç göstermiyordu (M-76):** klavye açıkken liste 0 yüksekliğe
   çöküyordu → `adjustNothing`.
3. **Türkçe karakter araması (M-77):** "super" yazınca "Süper FM" bulunmuyordu →
   `Lang.fold` (ı/i, ş/s, ğ/g, ü/u, ö/o, ç/c) + gizli istasyonlar da sonuçta.

### 8. yarı (v9.56) — doğrulananlar / eklenenler

| # | Konu | Sonuç |
|---|------|-------|
| D-13 | Radyo araması klavye açıkken sonuç gösteriyor mu | **✅** `ntv` yazıldı, NTV Radyo + diğerleri göründü (`_build/k3.jpg`) |
| D-14 | Radyo çipleri (Tümü/★/Haber…) klavye açıkken görünüyor mu | **✅** aynı ekran görüntüsünde görünüyor |
| D-15 | Alt banner gezinme çubuğunun üstünde mi | **✅** Paycell banner'ı tam, kırpılmadan (`_build/k1.jpg`) |
| D-16 | APK kapasite kaybı var mı (9.55 → 9.56) | **✅** 400 dosya, 17.504 sınıf, +124 bayt (`releases/apk-kapasite.json`) |
| D-17 | Tablet APK | **⏳ kullanıcıda** — binary telefonla aynı, SHA-256 `4e530038…7aeb8c` |
| D-18 | Radyo aramasında yabancı (Kanada/İspanya) istasyonlar | **⚠️ açık** — radio-browser sonuçları filtresiz geliyor (M-15) |

### 9. yarı (v9.57)

| # | Konu | Sonuç |
|---|------|-------|
| D-17 | Tablet APK kurulumu | **⏳ ENGELLİ** — tablette 9.29 farklı imzayla kurulu; `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Eski sürüm kaldırılmalı (kullanıcı onayı). |
| D-19 | Ayarlar'da sürüm yazıyor mu | **⏳** kod hazır (M-79); ekranda kurulum sonrası bakılacak |
| D-20 | Radyo aramasında yabancı sonuçlar (Kentville vb.) | **⏳** kelime başı filtresi eklendi (M-15); cihazda doğrulanacak |
| D-18 | (önceki) yabancı istasyon sonuçları | **✅ kod düzeltmesi yapıldı**, doğrulama D-20'de |

### Tablet kurulum notu (24 Kasım 2026, 9.57)

- Tablette eski **9.29** farklı imzayla kuruluydu; kullanıcı onayıyla kaldırıldı (`pm list packages` → kayıt yok).
- `adb install` HyperOS tarafından engellendi: **`INSTALL_FAILED_USER_RESTRICTED`** (Ayarlar →
  Geliştirici seçenekleri → **USB ile yükleme** kapalı / Mi hesabı doğrulaması gerekli).
- APK tablete kopyalandı: `/sdcard/Download/UMAY-MULTI-TV-9.57-TABLET.apk`
  (bilgisayarda da `İndirilenler/UMAY-MULTI-TV-9.57-tablet.apk`).
- Kurulum: tablette **Dosyalar → İndirilenler → UMAY-MULTI-TV-9.57-TABLET.apk** → aç → kur.
- İleride adb ile kurmak için tablette "USB ile yükleme" açılmalı.

### 10. yarı (v9.59) — telefon bağlı, ağ testleri

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-04 | TV yayını ağ kesintisinden sonra toparlanıyor mu | **✅** Wi-Fi 22 sn kapalı → açık; 22 sn içinde yayın açıldı, spinner kayboldu, veri 102 → 125,9 MB (`_build/f1.jpg`). |
| D-12 | Çoklu ekran (3 pencere) regresyonu | **✅** üç kanal birlikte oynuyor (`_build/m3.jpg`); ağ 30 sn kesilip gelince üçü de canlıya döndü (`_build/g1.jpg`). |
| D-19 | Ayarlar'da sürüm yazıyor mu | **✅** “Sürüm 9.59 · dokunduğun satır açılır” (`_build/v2.jpg`). |
| D-20 | Radyo aramasında yabancı sonuç | **✅** “ntv” → yalnız NTV Radyo / ntv Live; Kentville yok (`_build/v3.jpg`). |
| D-21 | Ağ geri gelince yayın donuk kalıyordu | **✅ düzeltildi (M-80)** — cihazda doğrulandı. |
| D-05 | Reklam sonrası canlıya dönüş | **⏳ açık** — test süresinde tam ekran reklam çıkmadı. |

### 11. yarı (v9.61) — kalite, sayaç, gerçek ağ testi

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-22 | Tüm kanallar en düşük (360p) açılıyor mu | **✅ kod + cihaz** — varsayılan `maxHeight=360`; oynatıcıda “360p” rozeti; çoklu ekran 360p varyantı (3 pencere ≈650 kbps/kanal). |
| D-23 | Çoklu ekranda veri sayacı | **✅** 643,5 → 661,0 MB / 72 sn (`_build/r1.jpg`, `_build/r2.jpg`). |
| D-24 | Mobil veri kapalıyken ağ kesintisi | **✅** 30 sn “(1/3)”, 60 sn “yayın açılmadı”, ağ dönünce kendiliğinden açıldı (`_build/n3/n4/n5.jpg`). |
| D-25 | “Ne var, ne yok?” güncel mi | **✅** 25 satır, cihazda görüldü (`_build/nv1.jpg`). |
| D-05 | Reklam sonrası canlıya dönüş | **⏳** reklam anı yakalanacak |
| D-08 | Ülke “yakında” etiketi | **⏳** |
| D-09 | Hata bildir → e-posta | **⏳** |

### 12. yarı (v9.61) — ülke ve hata bildir akışı

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-08 | Ülke ekranında hazır sayısı ve "yakında" | **KISMEN ✅** — “Ülke seç” ekranı açıldı; Türkiye “145 kanal · hazır” ve ✓ işaretli, diğer ülkeler “indir” (`_build/ul1.jpg`). **“yakında” etiketi gözlenmedi**: 30'dan az kanalı olan bir ülkenin indirilmesi gerekiyor; indirme dokunuşu bu oturumda tetiklenmedi (kota). |
| D-09 | Ayarlar → Hata bildir | **✅** — “Hata kaydı” penceresi açıldı (sürüm, cihaz bilgisi, son bağlantı hataları, 6000 karakter); “E-posta” → Android paylaşım seçicisi açıldı (`ChooserActivity`). **E-posta gönderilmedi** (kullanıcı hesabı). |
| D-05 | Reklam sonrası canlıya dönüş | **⏳ açık** — tam ekran reklam anı yakalanmadı. |

### 12. yarı eki (v9.62) — ülke “yakında” düzeltmesi

| # | Konu | Sonuç |
|---|------|-------|
| D-08 | Ülke ekranı “yakında” | **✅** sunucuda listesi olmayan ülke (Suriye, Yunanistan) artık “yakında · liste henüz yok”; dokunuş ağa çıkmıyor (`_build/cs3.jpg`). |
| D-26 | Ülke indirme sessiz başarısızlığı | **✅ düzeltildi (M-83)** — 404 artık kullanıcıya sebep olarak gösteriliyor. |

### 13. yarı (v9.63) — ülke dizini

| # | Konu | Sonuç |
|---|------|-------|
| D-27 | Eksik ülkeler otomatik “yakında” görünüyor mu | **✅** ülke ekranı açılınca 41 ülke kendiliğinden “yakında · liste henüz yok”; listesi olan 10 ülke “indir” (`_build/ul2.jpg`). |

### 14. yarı (v9.64) — ülke indirme, oynatma, radyo, bilgi kartı

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-28 | Ülke "indir" gerçekten çalışıyor mu | **✅** Birleşik Krallık indirildi → **111 kanal** listeye geldi (`_build/t6.png`). |
| D-29 | İndirilen kanal açılıyor mu | **✅** BBC One canlı yayın, **360p** rozeti, 1/111, veri 732,8 → 744,1 MB (`_build/t8s.png`). |
| D-30 | İndirilen ülkenin radyosu çalışıyor mu | **✅** GB için **40 istasyon** indi; Heart 80s "Bağlantı iyi · çalıyor" (`_build/t11s.png`, `_build/t12s.png`). |
| D-31 | 30'dan az kanallı ülke "yakında" mı kalıyor | **✅** Azerbaycan **3 kanal** ile indi ve "hazır" göründü (`_build/t16s.png`). |
| D-32 | Bilgi kartı ve yeni sıralama | **✅** "Kanal açılmıyor mu?" kartı Türkçe metinle görünür; Türkiye/UK/AZ üstte, sonra "indir" (`_build/t14s.png`). |
| D-33 | Mobil veri kapalıyken indirme | **⏳** bu oturumda denenmedi (Wi-Fi açıkken test edildi). |

## 24 Kasım 2026 — 9.65 hata analizi sonrası doğrulanacaklar

- **D-34 (✅ v9.66):** Günlük veri limiti 5 MB girildi → kanal açılışında
  "Günlük veri limiti doldu · Limit: 5 MB / Kullanılan: 160,2 MB · Devam edilsin mi?"
  diyalogu (Durdur / Devam et) ve başlıkta "⚠ LİMİT AŞILDI"; test sonunda limit "Yok"a
  çevrildi. Sayaç sıfırlama (gün dönümü) denenmedi.
- **D-35 (⏳):** Ölü kanal onarma: listeye yapay ölü URL eklenmiş test listesiyle
  `Repair.run` → "denendi / geri geldi" sayıları.
- **D-36 (🟡 v9.66):** "Sadece ses" oynatıcıyı kapatıp `media_session` `PLAYING`
  ile çalmaya devam etti; Wi-Fi kapalı senaryo ve "en düşük varyant" ekran kanıtı yok
  (kod düzeyinde doğrulandı).
- **D-37 (⏳):** Ödüllü reklam: kullanıcı elle izleyip "15 dk reklamsız" sayacının
  arttığını ve geçiş reklamının çıkmadığını doğrulamalı.

### 16. yarı (v9.66) — medya motoru, onarım, reklam

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-38 | İlk kanal açılışında reklam çıkıyor mu | **✅** yükleme sonrası 1. kanalda reklam yok; 2. kanalda tam ekran reklam normal geldi (`_build/y21s.png` TOD reklamı = 2. açılış). |
| D-39 | Sadece ses gerçekten düşük veri mi | **🟡** yayın kesintisiz, `PLAYING`; veri farkı video moduna göre düşük ama tek rendition kanalda (3sat) kesin ayrım yapılamadı. |
| D-40 | Ödül sonrası yayın baştan başlıyor mu | **⏳** ödüllü reklam ağı doldurmadı; kullanıcı elle denemeli. |

### 17. yarı (v9.67) — 3'lü izleme setleri

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-41 | Kaydedilen set geri yükleniyor mu | **✅** "Test Seti" kaydedildi, 2. pencere `№6 arte` yapıldı, set yeniden açıldı → 2. pencere `ADRIA`'ya döndü (`_build/y39t.jpg`). |
| D-42 | Set listesinde okunur ad | **✅** Ana ekran → "3'lü izleme setleri" listesinde "Test Seti" (ham `ad:...` kodu değil). |
| D-43 | Panel tüm düğmeler erişilebilir mi | **✅** Panel yatay kaydırılıyor, "★ set" görünür ve çalışıyor (`_build/y32t.jpg`). |
| D-44 | Ülke değişince set doğru kanalı mı açıyor | **⏳** denenmedi (ülke değiştirip set açma turu yapılmadı); adres alanı `u:` kayıtta mevcut. |

### 18. yarı (v9.68) — sayaç anahtarı ve kalite etiketi

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-45 | Sayaç anahtarı değişince eski veri korunuyor mu | **✅** Ana listede kanal başına sayaçlar aynı (304,8 MB · 110,9 MB · 117,1 MB); taşıma ilk okumada yapıldı. |
| D-46 | Kalite etiketi yayının gerçek çözünürlüğünü mü gösteriyor | **✅** Tercih 360p iken 3sat'ta düğme **720p**. |
| D-47 | Çok varyantlı kanalda tercih hâlâ uygulanıyor mu | **⏳** TR kanalında ayrıca denenmedi. |

### 19. yarı (v9.69) — reklam yerleşimi, set uyarısı, ayar tazeleme

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-44 | Ülke değişince kayıtlı set doğru davranıyor mu | **✅** Ülke Türkiye'ye alındı, Almanya seti açıldı → "4 kanal bu listede yok" uyarısı + TR kanallarıyla açıldı (`_build/y42t.jpg`). |
| D-48 | Şerit ile banner arasında güvenli boşluk | **✅** 6 dp boşluk eklendi; ana ekran görüntüsünde şerit kullanım çubuğundan ayrık. |
| D-49 | Ülke değişince Ayarlar satırı tazeleniyor mu | **✅** TR→DE dönüşünde satır hemen "Almanya" (eski sürümde bayat kalıyordu). |
| D-50 | Ölü kanal onarımı gerçek ölü URL ile | **⏳** hâlâ denenmedi (yapay ölü URL'li test listesi gerekiyor). |

### 20. yarı (v9.70) — kayıt sağlamlaştırma, liste yarışı

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-51 | Kayıt kalite tercihini izliyor mu (360p vs 1080p boyut) | **⏳** ölçülemedi: player'daki banner/geçiş reklamları dokunuşları mağaza uygulamasına yutuyor. Kod `Quality.pick` kullanıyor; elle ölçüm gerekiyor. |
| D-52 | fMP4 / byte-range kanalda kayıt uyarısı | **⏳** böyle bir kanal bulunamadı; uyarı yolu kodda hazır. |
| D-53 | Kayıt başlat/durdur/tekrar başlat | **✅** 3sat'ta iki kayıt alındı (2,77 MB + 6,66 MB), bitiş bildirimi geldi, çökme yok. "Önceki kayıt kapanıyor" yarışı yalnız kod düzeyinde denetlendi. |
| D-54 | Ülke değişimi sırasında liste/radyo yarışı | **✅** `snapshot()` sonrası 113 kanal listesi, Ayarlar taraması ve radyo akışı normal; `ConcurrentModificationException` yok. |

### 21. yarı — D-50 araştırması (v9.70 üzerinde)

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-50 | Gerçek ölü kanalla `Repair` testi | **🔧 Kısmen.** Masaüstünde 4 ölü kanal bulundu (#15 BLK, #100 RFO bağlantı yok; #16 BTL, #48 iSTUFF 403). Cihazda BLK açıldı: kanal **hiç hata üretmiyor**, 50-80 sn boyunca "yeniden bağlanıyorum" döngüsünde kalıyor, `addFail` çağrılmıyor → onarım hiç devreye giremiyor. Ayrıntı ve kök nedenler M-99'da. Düzeltme yazıldı ama cihazda doğrulanamadı, geri alındı. |
| D-55 | 9.70 gerileme kontrolü (ölü kanal düzeltmesi geri alındıktan sonra) | **✅** Kaynaktan derlenen APK `releases/v9.70/CanliTV-9.70.apk` ile birebir (0 bayt fark), cihaza yeniden kuruldu. |

### 22. yarı — 9.71 doğrulamaları

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-50 | Gerçek ölü kanalla hata/onarım | **✅** Almanya listesi: RFO 16. sn'de "Bu kanal şu an açılamadı · Bağlantı kurulamadı" gösterdi, ardından sıradaki kanal (RiC) açıldı; BLK Regional TV de "yeniden bağlanıyorum (1/3)" sonrası işaretlenip geçildi. Sonsuz döngü yok (`_build/t16_rfo.jpg`). |
| D-56 | Kanal listesi 126'ya indi mi | **✅** Ülke ekranı "Türkiye · 126 kanal · hazır"; ana başlık 127 (kullanıcının kendi eklediği 1 kanal). |
| D-57 | Kayıt kısıtı | **✅** ATV'de "Kayıt yalnız kamu kanallarında açık (TRT, TBMM, Diyanet)" uyarısı, dosya oluşmadı; TRT 1'de `TRT_1_…ts` oluştu ve durdu. |
| D-58 | Reklam test modu yayında görünmüyor mu | **✅** Ayarlar tarandı: "Reklam test modu" yok, "Reklam sayacım" var (175 reklam · ~₺1,78). |
| D-59 | Reklam sıklığı (3 zap / 180 sn) | **⏳** Cihazda reklam doluluğu olmadığı için gözle doğrulanamadı; kod yolu denetlendi. |
| D-60 | 9.71 kapasite/sürüm denetimi | **✅** `verify-apk --prev v9.70 --expect-version 9.71` temiz: sınıf +0, dosya +0, imza aynı. |

### 23. yarı — 9.73 doğrulamaları

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-61 | Reklam gerçekten gösteriliyor ve sayılıyor mu | **✅** Cihazda gerçek banner (Calvin Klein / hepsiburada) ve tam ekran geçiş reklamı (YDS) gösterildi; teşhis ekranı "rıza EVET, geçiş var, ödüllü var, bu cihazda 206 gösterim" yazdı. |
| D-62 | Banner hatası günlüğü | **✅** Teşhis ekranı `3 NO_FILL — Google şu an bu birime reklam vermiyor · No fill.` gösterdi; günlük artık "banner yüklenemedi" etiketiyle ve 30 dk'da bir yazılıyor. |
| D-63 | Reklamları kapat (sahip) | **✅** Açıkken ana ekranda banner/şerit yok, SDK isteği gitmiyor; kapatınca reklamlar geri geliyor (ana ekranda Calvin Klein banner'ı). |
| D-64 | Reklam sayacı kullanıcıya görünmüyor mu | **✅** Yayın APK'sında satır yok; yalnız Ayarlar başlığına 5 dokunuşla açılan gizli modda görünüyor. |
| D-65 | Günlük otomatik gönderim uçtan uca | **✅** Telefonda açıldı; `POST /api/log` 200, D1'de kayıt: `2026-11-25 18:03:39 9.73 Xiaomi 2506BPN68G · Android 16 (SDK 36) 6119 bayt`. |
| D-66 | ATV oynatma | **✅** 720p adresle açıldı (5 dk, 31,6 MB); master adres cihazda hazırlanmadığı için 720p birincil yapıldı. |
| D-67 | Hata günlüğü penceresi | **✅** Uzun adresler sarılıyor, pencere en yeni olaya kayıyor; "Gönder / Temizle / Kapat" ve "Günlük gönderimi" satırı göründü. |
| D-68 | 9.73 kapasite denetimi | **✅** `verify-apk --prev v9.72 --expect-version 9.73`: sınıf +10, dosya +0, imza `f3996c67…b8d5`, 9.73 (82). |

### 24. yarı — 9.74 doğrulamaları

| # | Konu | Sonuç / kanıt |
|---|------|----------------|
| D-69 | Radyo sesi (kullanıcı bildirimi) | **✅** Kullanıcı radyoyu açıp dinledi, ses geldi. Bu oturumda yeniden dinlenmedi. |
| D-70 | ATV sorunu | **⏳** Kullanıcı "ATV'de sorun var" dedi ve **sona bırakıldı**. 9.74'te ATV 720p açıldı (günlük: `hazır: ATV (Turkiye)`, 5 dk 31,6 MB); kök neden ayrı oturumda incelenecek. |
| D-71 | Çoklu ekran geri tuşu | **✅** 3'lü ekran → geri → ana liste; arka plan sesi/kayıt kapatılıyor. |
| D-72 | Kalite ↔ veri tasarrufu | **✅** 360p seçildi, Ayarlar "Kapalı: seçili kalite kullanılıyor (360p)" yazdı; tasarruf kapatınca 360p'ye dönüyor. |
| D-73 | Varyantsız kanalda düşüş | **✅** 360p tercihle ATV (yalnız 720p) açıldı; günlükte `varyant yok: tercih 360p, gelen 720p`, ekranda 720p. |
| D-74 | Grup adları | **✅** Ana ekran filtresinde "Eğlence" göründü; "Eglence" yok. |
| D-75 | 9.74 kapasite denetimi | **✅** `verify-apk --prev v9.73 --expect-version 9.74`: sınıf +0, dosya +0, 9.74 (83), imza aynı. |

| D-76 | 48dp düğme sonrası başlık düzeni | **✅** İlk denemede başlık "UMAY Mul..." kesildi; düğmeler kendi satırına alındı, başlık ve alt yazı tam görünüyor (`_build/t974_main3.png`). |
| D-77 | Ana sayfa üst boşluğu (9.75) | **✅** `fitsSystemWindows` iki kez uygulanıyordu; kapatıldı. Cihazda başlık durum çubuğunun altında, boşluk yok (`_build/v975_home2.png`). |
| D-78 | TV8 listede | **✅** Sabit kanallar otomatik gizlemeden muaf; cihazda №2 TV8 göründü ve canlı oynadı (720p, 19:14). |
| D-79 | Show TV | **✅** Kaynak HTTP 200 (master + 4 varyant); cihazda liste №3'te, açılışta yayın başladı (TOD reklam katmanı üstteydi, kapatınca liste döndü). Yayın akışı tek başına uzun süre izlenmedi. |
| D-80 | 9.75 kapasite denetimi | **✅** `verify-apk --prev v9.74 --expect-version 9.75`: 400 dosya, 17.530 sınıf, imza `f3996c67…b8d5`, 9.75 (84). |
| D-81 | ATV (№1) yeniden test | **⏳** Bu oturumda açılmadı (kullanıcı erteledi); D-70 açık kalıyor. |
| D-82 | Ana ekran üst kısmı (9.76) | **✅** Tek satır başlık+düğme, 4 sekme, tek veri şeridi; liste ~%27'den başlıyor (`_build/v976_home5.png`). |
| D-83 | Eksik kanal (128 → 32) | **✅** `hiddenCh` 115 kayıt + süresiz ölü sayacı. Temizlendi; cihazda "128 kanal" yazıyor, numaralar 1..128. |
| D-84 | 9.76 kapasite denetimi | **✅** `verify-apk --prev v9.75 --expect-version 9.76`: 400 dosya, 17.530 sınıf, +332 bayt, imza aynı. |
| D-85 | Site /indir 9.76 | **✅** Deploy `succeeded`; `/indir` HTTP 200, bağlantı `CanliTV-9.76.apk`. |
| D-86 | Oynatıcı üst barı | **⏳** Düğmeler/ikonlar büyütüldü (ekran görüntüsü alındı) ama 9.76'da yeniden oynatma testi yapılmadı; TV8 9.75'te oynuyordu. |
