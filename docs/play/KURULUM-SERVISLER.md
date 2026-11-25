# Üç anahtarı hazırlama rehberi (P1-6 · K-19 · K-20)

Bu dosya **senin yapacağın** konsol işlerini adım adım anlatır. Her bölümün sonunda
"bana ne vermen gerekiyor" yazar. **Hiçbir anahtarı sohbete yapıştırma** — dosyayı
belirtilen yola koy, ben oradan okurum.

Ön koşul: tüm hesaplarda **cengizkilic1980@gmail.com** ile giriş yapmış ol.

---

## ① P1-6 — Play servis hesabı (abonelik doğrulaması)

Amaç: "abonelik aktif" bilgisini sunucu (Cloudflare Worker) doğrulasın; istemciye
güvenmek zorunda kalmayalım.

### A. Google Cloud tarafı
1. https://console.cloud.google.com → üstten proje seç → **New Project**
   - Ad: `umay-play-verify` → **Create** (1 dakika sürer).
2. Proje seçili iken sol menü → **APIs & Services → Library**
   → ara: **Google Play Android Developer API** → **Enable**.
3. Sol menü → **IAM & Admin → Service Accounts → Create service account**
   - Name: `umay-play-verify`
   - Role: **hiçbir rol seçme** (Play tarafında izin vereceğiz) → **Done**.
4. Oluşan hesabın satırına tıkla → **Keys** sekmesi → **Add key → Create new key**
   → **JSON** → **Create**. Dosya bilgisayara iner
   (`umay-play-verify-xxxx.json`).
5. Servis hesabının e-postasını kopyala (şu biçimde:
   `umay-play-verify@umay-play-verify.iam.gserviceaccount.com`).

### B. Play Console tarafı
1. https://play.google.com/console → **UMAY Multi TV** uygulaması.
2. Sol menü en altta **Setup → API access**
   (= Türkçe arayüzde **Kurulum → API erişimi**).
3. **Link** / **Google Cloud projesini bağla** → `umay-play-verify` projesini seç → **Link**.
4. Aynı sayfada **Service accounts** listesinde `umay-play-verify` görünür
   → **Grant access** (izin ver):
   - App permissions: **View financial data, orders, and cancellation survey responses**
     + **Manage orders and subscriptions** işaretle.
   - **Invite user** / **Save**.
5. 5-10 dakika bekle (izin yayılması).

### C. Bana bırakılacak dosya
İnen JSON dosyasını şu yola koy:

```
CANLI-TV-APP\site\.secrets\play-service-account.json
```

(`site/.secrets/` klasörü `.gitignore` içinde, git'e girmez.)

Ben şunu yapacağım: Worker'a `POST /api/sub` ekleyip `purchases.verify` çağrısıyla
`subscriptionsv2.get` yapacağım; geçerliyse `active=1` yazacağım, istemci de
aboneliği sunucudan okuyacak. Paket adı `com.cengiz.canlitv`, ürün
`umay_reklamsiz_aylik`.

---

## ② K-19 — Firebase Analytics

### A. Firebase projesi
1. https://console.firebase.google.com → **Add project** (veya mevcut projeyi seç)
   - Ad: `umay-multi-tv` → Google Analytics'i **aç** (analytics için gerekli)
   → devam → **Create project**.

### B. Android uygulamasını ekle
1. Proje ana sayfasında **Android** simgesine bas.
2. **Android package name:** `com.cengiz.canlitv`
3. **App nickname:** `UMAY Multi TV`
4. **Debug signing certificate SHA-1:** boş bırakılabilir (Play App Signing kullanıyoruz).
5. **Register app** → `google-services.json` dosyasını indir.

### C. Bana bırakılacak dosya
```
CANLI-TV-APP\google-services.json
```
Ben: `com.google.gms:google-services` eklentisini + `firebase-analytics` SDK'sını
gradle'a ekleyeceğim, onay (consent) akışına bağlayacağım — AB/UK için Analytics
depolama izni (UMP) zaten var, oraya "analytics" seçeneğini de ekleyeceğim.
Gizlilik politikasına "Firebase Analytics" bölümü eklemem gerekecek (ben yazarım).

> Not: Analytics'i **kullanıcı onayına** bağlı tutacağım; onay yoksa hiç veri gitmez.

---

## ③ K-20 — AdMob: native reklam + mediation

### A. Native reklam birimi
1. https://apps.admob.com → **Apps → UMAY Multi TV** (5042287034).
2. **Ad units → Get started / Add ad unit** → **Native** seç.
   - Ad unit name: `umay_liste_native`
   - Format: **Native** → **Create ad unit**.
3. Açılan ekranda **Ad unit ID**'yi kopyala
   (`ca-app-pub-7629051535337276/XXXXXXXXXX`).

### B. Mediation (isteğe bağlı, gelir artırır)
1. AdMob → **Mediation → Create mediation group**.
   - Ad format: **Banner** (önce banner ile başla), Platform: **Android**
   - Ad unit: mevcut banner birimin.
2. **Add ad source** → **Meta Audience Network** veya **AppLovin** (AdMob Bidding
   önerilir). Her ağ için o ağda ayrı hesap + o ağın SDK'sı gerekir:
   - Meta: https://developers.facebook.com → Audience Network hesabı, sonra
     `com.facebook.android:audience-network-sdk` bağımlılığı.
   - AppLovin: https://www.applovin.com → hesap + `com.applovin:applovin-sdk`.
3. Her ağ için AdMob'da **App ID / placement ID** girilir.

### C. Bana bırakılacak bilgi
Sohbete **sadece ID'leri** yaz (gizli değiller, reklam birimi kimlikleri):

```
NATIVE_UNIT_ID = ca-app-pub-7629051535337276/..........
mediation ağı = Meta / AppLovin / yok
```

Ben: `Ads.java`'ya `NATIVE_REAL` ekleyip liste ekranının altına native kart
koyacağım (küçük, "Reklam" etiketli, yanlış tıklama riski olmayan), mediation
seçtiysen ilgili SDK'yı ve `AndroidManifest` meta-data'larını ekleyeceğim.

---

## ④ Kopya/taklit koruması (bu turda yapılan + sırada)

**Yapıldı (9.92):**
- `Guard.java`: Play dışından kurulan APK, **bizim yükleme sertifikamızla** imzalı
  değilse uygulama açılmıyor ("Resmi olmayan sürüm" uyarısı). Play'den gelen
  kurulumlar etkilenmez.
- Yedek: Play dışı kurulumda sertifika SHA-256 karşılaştırması.

**Sıradaki (ücretsiz, senin 10 dakikanı alır): Play Integrity API**
1. Play Console → **Release → App integrity → Play Integrity API** → **Enable**.
2. Aynı sayfada **Cloud project** bağla (①'de açtığın proje olabilir).
3. Bana **Cloud project number**'ı ver (12 haneli sayı).
Ben: istemcide `IntegrityManager` ile token alıp Worker'da doğrulayacağım;
taklit/yeniden paketlenmiş uygulamada verdict `MEETS_DEVICE_INTEGRITY` dönmez ve
yayın açılmaz. Bu, sertifika kontrolünü yamalayan kopyaları da yakalar.

**Ayrıca (hukuk/marka):**
- Play'de yayınlandıktan sonra **telif/marka bildirimi** hakkın var; taklit
  yüklemeleri Play ekibi kaldırır (form: Play Console → policy → report).
- Uygulama içi "resmi site" damgası + mağaza adı tescili caydırıcıdır.
