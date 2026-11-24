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
