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
