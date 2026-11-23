# Kapalı test (12 tester / 14 gün) — adım adım

Play, **yeni kişisel geliştirici hesaplarında** üretime çıkmadan önce kapalı test ister:
en az **12 tester**, **14 gün boyunca kesintisiz** test, sonra "Üretim erişimi" başvurusu.
Bu süre boyunca test **düşmemeli** (bir gün boş kalırsa sayaç etkilenir), bu yüzden
testerlerin uygulamayı silmemesi gerekir.

## 0. Ön koşul

Geliştirici kimlik doğrulaması **onaylanmış** olmalı; aksi hâlde uygulama oluşturulamaz
(şu an Google incelemesinde).

## 1. Uygulamayı oluştur

1. Play Console → **Uygulama oluştur**.
2. Ad: `UMAY Multi TV: Canlı TV Radyo` · Dil: Türkçe · **Uygulama** · **Ücretsiz**.
3. Beyanlar: "Uygulama, Google Play Geliştirici Programı Politikalarına uyar" işaretle.

## 2. Mağaza kaydını doldur

- **Mağaza girişi → Ürün detayları**: metinler `docs/play/magaza-kaydi.md` içinde hazır.
- **Grafikler**: ikon 512×512, öne çıkan görsel 1024×500, en az 2 telefon ekran görüntüsü.
- **Mağaza ayarları**: kategori (Eğlence), iletişim e-postası, gizlilik politikası URL'si.

## 3. Zorunlu formlar

- **Uygulama içeriği**: reklam var → evet; hedef kitle 13+; içerik derecelendirme anketi.
- **Veri güvenliği**: "E-posta adresi toplanıyor (isteğe bağlı hesap)" + "Reklam kimliği".
  Ayrıntılı cevaplar `magaza-kaydi.md` §5.
- **Reklamlar**: uygulama reklam içeriyor → evet.

## 4. AAB'yi yükle

1. **Test → Kapalı test → Yeni sürüm oluştur**.
2. `releases/v9.1/CanliTV-9.1.aab` dosyasını yükle (upload anahtarıyla imzalı, versionCode 10).
3. Play'in "imzalama anahtarını kaydet" uyarısı çıkarsa **Play App Signing**'i kabul et
   (upload anahtarı `upload-keystore.jks`, şifresi `make-aab.sh` içinde).
4. Sürüm notları: `releases/v9.1/SURUM-NOTLARI.md` içeriğini kısaltıp yapıştır.

## 5. Testerleri ekle

1. **Test → Kapalı test → Test kullanıcıları**.
2. **E-posta listesi** oluştur: 12–20 Gmail adresi (kendi ikinci hesabın + tanıdıklar).
3. Listeyi seç, **Kaydet**.
4. Sayfadaki **"Test katılma bağlantısı"**nı kopyala
   (`https://play.google.com/apps/testing/com.cengiz.canlitv` gibi).
5. Bağlantıyı WhatsApp'tan gönder. Tester şunu yapar:
   1. Bağlantıyı açar → **Teste katıl**.
   2. **Uygulamayı indir** (Play Store açılır).
   3. Kurar, uygulamayı **siler değil**, 14 gün arada bir açar.
6. Test sayfasında **"12 tester katıldı"** sayısını takip et; 12'nin altına düşerse süre işlemez.

## 6. 14 gün sonra

1. **Üretim → Üretime erişim iste**.
2. Formda: uygulamanın ne yaptığı, kimin için olduğu, testlerde ne öğrendiğin
   (ör. "12 tester 14 gün kullandı, 3 hata bildirildi ve düzeltildi").
3. Onay gelince **Üretim → Yeni sürüm** → aynı AAB → **İncelemeye gönder**.
4. Google incelemesi genelde 1–7 gün; onaylanınca uygulama Play'de yayınlanır.

## 7. Yayından sonra

- AdMob → **Uygulamalar → UMAY Multi TV → Mağaza ekle** → Play linkini yapıştır.
  "Sınırlı reklam sunumu" kalkar, reklam geliri tam açılır.
- Play linkini `docs/play/README` yerine uygulamanın "Bizi değerlendir" satırı kullanır
  (`com.cengiz.canlitv`).
