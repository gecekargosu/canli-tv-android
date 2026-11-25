# "API" nereden alınır? — 3 ayrı şey, karışmasın

Ekranda gördüğün boş Play Console sayfası **normal**: o hesapta henüz uygulama
yok. Play Developer API menüsü, uygulama oluşturulduktan sonra açılır.

Üç ayrı "anahtar" var ve hiçbiri Play Console'da "API key" düğmesi değil:

---

## ① Play Developer API (abonelik doğrulaması) — P1-6

**Ne?** Bir **JSON dosyası** (servis hesabı anahtarı). "API key" değil.
**Nereden?** İki adım: Google Cloud (anahtarı üretir) + Play Console (yetki verir).

### A) Önce uygulamayı Play'de oluştur
`01-ADIM-ADIM-YUKLE.md` → Bölüm 1. (Bu yapılmadan Play tarafı boş görünür.)

### B) Google Cloud'da servis hesabı (anahtar burada iner)
1. **Doğrudan yeni proje linki** (menüde aramana gerek yok):
   https://console.cloud.google.com/projectcreate
   - Proje adı: `umay-play-verify` → **Oluştur** (1 dk)

   > Menüde "yeni proje" görünmüyorsa sebebi: seçici, sol üstte "Google Cloud"
   > yazısının yanındaki **proje kutusu**dur, sayfa yüklenene kadar boş görünür.
   > Yukarıdaki link bunu atlar.
   >
   > Alternatif: elindeki `JobHunter` projesini de kullanabilirsin
   > (proje no **878769405259**). Play API için yeterlidir; ayrı proje açmak
   > yalnızca düzen için daha temiz.
2. Sağ üstte projenin `umay-play-verify` olduğunu doğrula. Sol menü →
   **API ve Hizmetler → Kitaplık** → ara:
   **"Google Play Android Developer API"** → **Etkinleştir**
   (Bu, aradığın "API"dir; anahtar değil, erişim iznidir.)
3. Sol menü → **IAM ve Yönetici → Hizmet Hesapları → Hizmet hesabı oluştur**
   - Ad: `umay-play-verify` → rol seçme → **Bitti**
4. Listede hesabın satırına tıkla → **Anahtarlar** sekmesi →
   **Anahtar ekle → Yeni anahtar oluştur → JSON → Oluştur**
   → dosya bilgisayara iner (`umay-play-verify-xxxx.json`).
5. Servis hesabının e-postasını kopyala:
   `umay-play-verify@umay-play-verify.iam.gserviceaccount.com`

### C) Play Console'da yetki ver (menü buradadır)
Uygulama oluşturulduktan sonra: **Kurulum (Setup) → API erişimi (API access)**
- **Google Cloud projesini bağla** → `umay-play-verify` → **Bağla**
- Hizmet hesapları listesinde `umay-play-verify` → **Erişim ver**:
  - ✅ Görüntüleme: **finansal veriler, siparişler ve iptal anketi**
  - ✅ **Siparişleri ve abonelikleri yönet**
- **Davet et / Kaydet** → 5-10 dk bekle

### D) Dosyayı bana bırak
```
CANLI-TV-APP\site\.secrets\play-service-account.json
```
Dosyayı indirdiğin adla bırakman yeterli; ben `play-service-account.json`
adına taşırım. **İçeriğini sohbete yapıştırma.**

---

## ② Firebase Analytics — K-19

**Ne?** `google-services.json` (Firebase'in "ben bu uygulamayım" dosyası).
**Nereden?** https://console.firebase.google.com

1. **Proje ekle** → ad: `umay-multi-tv` → Google Analytics'i **aç** → Oluştur
2. Proje ana sayfası → **Android** simgesi
3. Form:
   - Android paket adı: `com.cengiz.canlitv`
   - Takma ad: `UMAY Multi TV`
   - SHA-1: boş bırak
4. **Uygulamayı kaydet** → `google-services.json` indir
5. Dosyayı şuraya koy:
```
CANLI-TV-APP\google-services.json
```
Ben eklentiyi + SDK'yı gradle'a ekler, **kullanıcı onayına bağlı** şekilde
açılış/oynatma olaylarını gönderirim (AB için UMP zaten var).

---

## ③ AdMob — native reklam + SSV

**Ne?** Reklam birimi ID'si (`ca-app-pub-7629051535337276/…`). Gizli değil.
**Nereden?** https://apps.admob.com → Uygulamalar → **UMAY Multi TV**

### Native birim
**Reklam birimleri → Başlayın / Birim ekle → Native** → ad: `umay_liste_native`
→ **Oluştur** → çıkan **birim ID'sini** bana yaz.

### Ödüllü reklamın sunucuda doğrulanması (bugün eklediğim kod için)
Uygulamalar → UMAY Multi TV → **Reklam birimleri** → ödüllü birim
(`ca-app-pub-7629051535337276/4812940915`) → **Sunucu tarafı doğrulama URL'si:**
```
https://umay-multi-tv-gq3zer.freebuff.site/api/adssv
```
Kaydet. Bundan sonra her ödüllü gösterimi sunucuda imzayla doğrulanır.

### Mediation (isteğe bağlı, sonra)
AdMob → **Mediation → Mediation grubu oluştur** → banner → **Reklam kaynağı ekle**
→ Meta Audience Network / AppLovin. Her ağ için o ağda ayrı hesap gerekir;
ID'leri bana verirsen SDK'yı eklerim.

---

## Özet: hangi dosya nereye

| Ne | Nereden iner | Nereye koyacaksın |
|---|---|---|
| Play servis hesabı | Google Cloud → Hizmet hesabı → Anahtarlar → JSON | `site/.secrets/play-service-account.json` |
| Firebase | Firebase konsolu → Android uygulaması | `google-services.json` |
| AdMob native ID | AdMob → Reklam birimleri → Native | sohbete sadece ID |

---

## İngilizce arayüz karşılıkları (Cloud Console)

Konsol İngilizce geliyor; aradığın menülerin İngilizcesi bunlar:

| Türkçe dediğim | Ekranda yazan |
|---|---|
| Yeni proje | **New Project** (proje kutusu → **New project**) |
| API ve Hizmetler | **APIs & Services** (sol üstteki ☰ menüde) |
| Kitaplık | **Library** |
| Etkinleştir | **Enable** |
| IAM ve Yönetici | **IAM & Admin** |
| Hizmet Hesapları | **Service Accounts** |
| Hizmet hesabı oluştur | **Create service account** |
| Oluştur ve devam et | **Create and continue** |
| Rol seçme / Atla | **Continue** → **Done** |
| Anahtarlar | **Keys** sekmesi |
| Anahtar ekle | **Add key → Create new key** |
| JSON | **JSON** → **Create** (dosya iner) |
| Proje ayarları (yeniden adlandırma) | **Project settings** → **Rename** |

### Doğrudan linkler (menü aramadan)
Proje numaran: **114803444551** (`My First Project`)

1. **API'yi etkinleştir:**
   https://console.cloud.google.com/apis/library/androidpublisher.googleapis.com?project=114803444551
   → mavi **ENABLE** düğmesi
2. **Servis hesabı oluştur:**
   https://console.cloud.google.com/iam-admin/serviceaccounts/create?project=114803444551
   → Service account name: `umay-play-verify` → **CREATE AND CONTINUE** → **CONTINUE** → **DONE**
3. **Anahtar (JSON) indir:**
   https://console.cloud.google.com/iam-admin/serviceaccounts?project=114803444551
   → `umay-play-verify` satırına tıkla → **KEYS** → **ADD KEY → Create new key** → **JSON** → **CREATE**
4. **Play Console tarafı (uygulama oluşturulduktan sonra):**
   Play Console → **Setup → API access** → **Link** → projeyi seç →
   servis hesabına **Grant access**: *View financial data*, *Manage orders and subscriptions*

> Projenin adı `My First Project` kaldıysa sorun değil; **Project settings →
> Rename** ile `umay-play-verify` yapabilirsin, zorunlu değil.
> Konsolu Türkçeye çevirmek istersen Google Hesabı → Kişisel bilgiler → Dil
> (tüm Google ürünlerini etkiler).
