# Play'e yükleme — adım adım (9.93 / kod 102)

Bu klasörde gereken her şey var:

```
UMAY-MULTI-TV-9.93.aab      ← Play'e YÜKLENECEK dosya (tek dosya yeter)
CanliTV-9.93.apk            ← telefona kurmak / test için
02-MAGAZA-METNI.md          ← mağaza metinleri (kopyala-yapıştır)
03-DATA-SAFETY.md           ← Veri güvenliği formu cevapları
magaza-gorselleri/          ← ikon 512, öne çıkan görsel, ekran görüntüleri
```

> **ÖNEMLİ:** Ekran görüntünde hesabında **hiç uygulama yok** ("İlk uygulamanızı
> oluşturun"). Play Developer API menüsü (Kurulum → API erişimi) **uygulama
> oluşturulduktan sonra** görünür. Sıra: önce uygulamayı oluştur → sonra API.

---

## 1. Uygulamayı oluştur (5 dakika)

https://play.google.com/console → **Uygulama oluştur**

| Alan | Değer |
|---|---|
| Uygulama adı | `UMAY Multi TV: Canlı TV Radyo` |
| Varsayılan dil | Türkçe (Türkiye) |
| Uygulama mı oyun mu | **Uygulama** |
| Ücretsiz mi ücretli mi | **Ücretsiz** |
| Beyanlar | 3 kutuyu da işaretle (Geliştirici programı politikaları, ABD ihracat yasaları, Play uygulama imzalama şartları) |

**Oluştur** → uygulama paneli açılır.

## 2. Uygulama imzalama (Play App Signing)

Sol menü → **Test ve yayın → Kurulum → Uygulama imzalama**

- **"Google tarafından yönetilen imzalama anahtarını kullan"** seç (**önerilen**).
  Play, AAB'yi kendi anahtarıyla imzalar; bizim `Guard.java` kontrolü Play
  kurulumlarını etkilemez, sorun çıkmaz.
- Yükleme anahtarı: repoda `upload-keystore.jks` + `keystore.properties` hazır
  (yedeklerde de var). Play ilk AAB'de bunu otomatik tanır.

## 3. "Uygulama içeriği" beyanları (zorunlu, ~15 dakika)

Sol menü → **Politika → Uygulama içeriği**. Sırayla:

| Bölüm | Cevap |
|---|---|
| Gizlilik politikası | `https://umay-multi-tv-gq3zer.freebuff.site/gizlilik-politikasi` |
| Uygulama erişimi | Tüm işlevler kısıtlama olmadan kullanılabilir |
| Reklamlar | **Evet, reklam içeriyor** (banner + geçiş + ödüllü) |
| İçerik derecelendirmesi | Anket: şiddet/cinsellik/kumar/uyuşturucu **yok**, kullanıcı etkileşimi **yok** → genelde **PEGI 3 / 13+** çıkar |
| Hedef kitle | **13 yaş ve üzeri**; "çocuklara yönelik" **Hayır** |
| Haber uygulaması | **Hayır** (haber kanalı yayınlıyor ama haber uygulaması değil) |
| Veri güvenliği | `03-DATA-SAFETY.md` dosyasındaki tabloyu birebir gir |
| Devlet uygulaması | Hayır |
| Finansal özellikler | Yok |

## 4. Mağaza kaydı (Store listing)

Sol menü → **Büyüt → Mağaza varlığı → Mağaza kaydı**

- Metinler: `02-MAGAZA-METNI.md` (ad 30, kısa açıklama 80, tam açıklama 4000 karakter)
- **Uygulama ikonu:** `magaza-gorselleri/ikon-512.png` (512×512, 32-bit PNG)
- **Öne çıkan görsel:** `magaza-gorselleri/one-cikan-1024x500.png`
- **Telefon ekran görüntüleri:** `magaza-gorselleri/ekran-*.png` (en az 2, en fazla 8)
- Kategori: **Eğlence** · Etiketler: canlı tv, tv, radyo, haber, spor
- İletişim e-postası: `cengizkilic1980@gmail.com`
- Web sitesi: `https://umay-multi-tv-gq3zer.freebuff.site`

## 5. AAB'yi yükle (kapalı test)

Sol menü → **Test ve yayın → Test → Kapalı test → Yeni test kanalı oluştur**

1. Kanal adı: `Kapalı test`
2. **Sürüm oluştur** → **Yükle** → `UMAY-MULTI-TV-9.93.aab`
3. Sürüm notları (kopyala):

```
İlk sürüm: 120 canlı TV kanalı, 24 radyo, numaralı kanal, favoriler,
3'lü izleme, uyku modu, ödüllü reklamla 15 dk reklamsız.
```

4. **Test kullanıcıları:** e-posta listesi oluştur → **en az 12 kişi**
   (kendi Gmail'lerin + arkadaşların). Liste: `e-postalar.txt`
5. **İnceleme için not** (opsiyonel ama faydalı):

```
Uygulama içerik barındırmaz; yalnızca yayıncı kuruluşların herkese açık
canlı yayın adreslerini oynatır. Telif bildirimi sayfamız vardır ve
24-48 saat içinde kaldırma taahhüdü veriyoruz:
https://umay-multi-tv-gq3zer.freebuff.site/telif-bildirimi
```

6. **Kaydet → İncelemeye gönder.** Onay sonrası test bağlantısı çıkar,
   testçiler o linkten katılır.

## 6. Üretime geçiş

Kapalı test **14 gün** doldurduktan ve en az 12 tester katıldıktan sonra:
**Test ve yayın → Üretim → Sürüm oluştur** → aynı AAB'yi seç → **Yayına al**.

> Not: Play, yeni geliştirici hesaplarında **12 tester × 14 gün** kuralını
> uyguluyor. Bu süre dolmadan üretim butonu açılmaz.

## 7. Yüklemede takılırsan

| Hata | Çözüm |
|---|---|
| "Paket adı zaten kullanılıyor" | Bu hesapta değil başka hesapta kayıtlı; hesabı kontrol et |
| "versionCode 102 zaten var" | AAB doğru; yeni sürüm gerekmiyorsa mevcut sürümü düzenle |
| "İmza uyuşmuyor" | Play App Signing'i *Google tarafından yönetilen* seçtiğinden emin ol |
| "Hedef kitle / Data safety eksik" | Bölüm 3'teki beyanları tamamla |
| "Uygulama erişimi" reddi | "Tüm işlevler kısıtlama olmadan kullanılabilir" seçili olmalı |
