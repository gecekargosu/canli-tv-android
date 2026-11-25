# Play'e yüklenebilir miyiz? — karar notu (25 Kasım 2026, 9.74)

Bu not üç soruya cevap verir: **teknik olarak hazır mıyız, Play kabul eder mi, reklam parası olur mu.**
Tahminler açıkça "tahmin" olarak işaretlendi; politika maddeleri Google'ın kendi sayfalarından.

---

## 1. Kısa cevap

| Soru | Cevap |
|---|---|
| Teknik olarak yüklenebilir mi? | **Evet.** AAB, 512 ikon, 1024×500 görsel, 3 ekran görüntüsü, gizlilik/koşullar/telif sayfaları, Data safety tablosu hazır. |
| Ben yükleyebilir miyim? | **Hayır.** Play Console'a senin Google hesabınla giriş + 2 adımlı doğrulama gerekiyor. Yükleme adımlarını senin yerine tıklayamam; ama her ekranı sırayla tarif edip AAB'yi hazır tutuyorum. |
| Şimdi yükleyip yayına çıkar mı? | **Hayır.** Yeni kişisel hesaplarda **12 tester / 14 gün kesintisiz kapalı test** + üretim erişimi başvurusu zorunlu. Takvim süresi kısaltılamaz. |
| Reklam geliri olur mu? | **Olabilir, garanti değil.** Yayın + AdMob'a mağaza bağlantısı sonrası. Şu an AdMob "sınırlı reklam sunumu" veriyor (NO_FILL gördük). |
| Hesabım kapanır mı? | **Risk var, en büyük sebep telif.** Aşağıda ayrıntılı. |

---

## 2. Teknik hazırlık (tamam)

- `releases/v9.74/UMAY-MULTI-TV-9.74.aab` — versionCode 83, targetSdk 36, imzalı.
- İzinler: INTERNET, ACCESS_NETWORK_STATE, WAKE_LOCK, FOREGROUND_SERVICE(+MEDIA_PLAYBACK),
  POST_NOTIFICATIONS, AD_ID. Hassas izin yok.
- Reklam: banner + geçiş + ödüllü, UMP rıza akışı, `app-ads.txt` yayında.
- Veri: hesap yok; hata günlüğü **varsayılan kapalı**, açılırsa günde 1 kez rastgele cihaz kimliğiyle.
- Gizlilik / kullanım koşulları / telif-kaldırma sayfaları yayında.
- Mağaza metinleri: `docs/play/magaza-kaydi.md` (120+ kanal, 25 radyo, gerçek özellikler).
- Görseller: `docs/play/ikon-512.png`, `one-cikan-1024x500.png`, `ekran-9.74-1/2/3`.

**Ürün tarafındaki tek açık iş:** ATV. Kullanıcı "ATV'de sorun var" dedi; 9.74'te 720p açılıyor
ama mağazaya çıkmadan önce bu kanalın güvenilir olması gerekir. Ayrıca ülke listelerinin ilk
açılışta boş gelmemesi (internet yoksa) kontrol edilmeli.

---

## 3. Play kuralları — bizim için kritik olanlar

1. **Kapalı test zorunluluğu.** 13 Kasım 2023'ten sonra açılan **kişisel** hesaplar için:
   en az **12 tester**, **14 gün kesintisiz** opt-in, sonra üretim erişimi başvurusu.
   (support.google.com/googleplay/android-developer/answer/14151465)
2. **Fikri mülkiyet / telif.** "Başkalarının telif hakkını ihlal eden uygulamalara ve geliştirici
   hesaplarına izin vermiyoruz; içeriği kullanma hakkını kanıtlamanız istenebilir."
   (support.google.com/googleplay/android-developer/answer/9888072)
3. **Yaptırım kademeleri.** Ret → hesap itibarını etkilemez. Kaldırma → hemen askıya alma
   değil ama **birden çok kaldırma askıya almaya yol açabilir**. Askıya alma/fesih daha ağır;
   itiraz süresi 28 Ocak 2026'dan sonra **180 gün**.
   (answer/18265346, answer/16659089)
4. **AdMob.** Politika ihlalinde **reklam sunumu veya AdMob hesabı kapatılabilir**; kapatılan
   hesap AdSense/AdMob programlarına bir daha alınmaz. (support.google.com/admob/answer/6128543)

### Bunun bizim uygulamaya anlamı

- Uygulama, **yayıncıların halka açık HLS adreslerini** yeniden yayınlıyor. Halka açık olması
  "yeniden yayınlama izni" demek değil. Türkiye'de yayıncıların çoğu bu tür uygulamalara
  DMCA/telif bildirimi gönderiyor.
- **Gerçekçi senaryo:** uygulama yayına girer, bir süre çalışır, bir yayıncı şikâyet ederse
  uygulama **kaldırılır** (hesap hemen kapanmaz). Aynı hesapla üst üste kaldırma olursa
  **askıya alma** riski büyür.
- **Kötü senaryo:** hak sahibi doğrudan Google'a hak ihlali bildirirse veya uygulama
  "yetkisiz yayın" olarak işaretlenirse **hesap feshi**. Feshedilen hesap için Google
  yeni hesap açmayı kabul etmiyor (kimlik eşleştirmesi). Bu yüzden **ilk uygulamayı
  "deneme" olarak riske atmak** doğru değil.
- **Hafifletici adımlar (kodda/sitede zaten var):** uygulamada kanal barındırılmıyor,
  telif-kaldırma sayfası + e-posta + 48 saat taahhüdü var, özel kanallarda kayıt kapalı.
  Bunlar riski azaltır, **sıfırlamaz**.

---

## 4. "Diğer uygulamalarla devam edebilir miyim?"

Veriye dayalı cevap:

- **Ret (rejection):** hesabın itibarını etkilemez (Google'ın kendi metni). Yeni uygulama
  oluşturmaya devam edebilirsin.
- **Kaldırma (removal):** o uygulama kalkar, hesap ayakta kalır; ancak **birden çok kaldırma
  askıya almaya** yol açabilir.
- **Askıya alma (suspension):** hesap özellikleri kısıtlanır; itiraz gerekir.
- **Fesih (termination):** hesap kapanır, **aynı kimlikle yeni hesap açılamaz** (Google
  politikası ve topluluk deneyimi bu yönde). Bu yüzden fesih "sadece bu uygulamayı kaybetmek"
  değil, **Play'e bir daha yükleyememek** anlamına gelir.

**Tahminim (kesin değil):** İlk uygulama olarak telifli canlı TV uygulaması yüksek riskli.
Ret veya kaldırma olasılığı, "temiz" bir uygulamaya göre belirgin şekilde yüksek. Fesih,
tek seferlik bir kaldırmada değil, tekrarlanan ihlalde gerçekleşiyor.

---

## 5. Reklam geliri gerçekçi mi?

- Gelir, **yayına çıktıktan sonra** AdMob → Uygulama → "Mağaza ekle" ile Play linki girilince tam açılır.
  O ana kadar "sınırlı reklam sunumu" devam eder; cihazda NO_FILL gördük.
- Türkiye trafiğinde banner eCPM'i **tahminen** 0,1–0,5 USD / 1000 gösterim bandında,
  geçiş reklamı daha yüksek. Yani **1000 kişi/gün** bile ciddi gelir demek değil;
  ilk aylarda birkaç dolar beklemek gerçekçi.
- AdMob ödeme eşiği 100 TL. İlk ödeme için bu eşiğin dolması gerekir.
- **"Bedelini alıyor muyuz" sorusunun cevabı:** gösterim başına ödeme AdMob tarafından
  sayılır; uygulama ayrıca rapor göndermek zorunda değil. Ama **gelir = geçerli gösterim**;
  kullanıcı yoksa gelir de yok.

---

## 6. Önerim (sırayla)

1. **Şimdi kapalı testi başlat** (14 gün takvim süresi; en erken bugün başlarsa aralık başında üretim).
   Bu aşamada uygulama herkese açık değil; risk düşük, geri bildirim değerli.
2. **ATV'yi düzelt** (mağazaya çıkmadan). Test kullanıcıları ilk ATV'yi açacak.
3. **Testerleri 15–20 kişi yap** (12'nin altına düşerse sayaç sıfırlanır).
4. Üretim başvurusunda uygulamayı **"canlı TV/radyo oynatıcı"** olarak anlat; telif-kaldırma
   sayfasını ve iletişim adresini göster.
5. **İlk sürümde riski düşür:** yayıncı şikâyeti gelirse 48 saatte kaldır; özel kanallarda
   kayıt zaten kapalı; mağaza metninde kanal sayısını abartma.
6. **Alternatif (daha güvenli ürün):** yalnızca **kendi yayınladığı** resmi akışları (TRT'nin
   resmi embed/YouTube canlıları gibi) listeleyen bir sürüm; ticari kanalları çıkar.
   Gelir düşer, hesap riski belirgin azalır.

**Karar:** Teknik olarak yüklenebilir; **kapalı testi bugün başlatmak mantıklı**. Üretime
çıkmadan ATV düzeltilmeli ve telif riski kabul edilmeli. Ben yükleyemem; Play Console
adımlarını senin tıklaman gerekiyor, ben her adımı ekran ekran tarif ederim.

---

## 7. Kaynaklar

- Kapalı test: https://support.google.com/googleplay/android-developer/answer/14151465
- Fikri mülkiyet: https://support.google.com/googleplay/android-developer/answer/9888072
- Yaptırım kademeleri: https://support.google.com/googleplay/android-developer/answer/18265346
- Fesih itiraz süresi: https://support.google.com/googleplay/android-developer/answer/16659089
- AdMob politika: https://support.google.com/admob/answer/6128543
- AdMob uygulama/hesap kapatma: https://support.google.com/admob/answer/6195033
