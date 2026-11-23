# Play Store & AdMob — durum defteri

Son kontrol: **23 Kasım 2026** (Play Console ve AdMob konsolu açılıp bakıldı).

## 1. Doğrulamalar — konsoldan okunan gerçek durum

| Doğrulama | Durum | Anlamı |
|---|---|---|
| **Play geliştirici kimliği** | ⏳ **Google inceliyor** — "Google, kimliğinizi doğruluyor. Belgeler yüklendi, birkaç gün sürebilir." | Belgeler yüklü, onay gelmedi. |
| **Play telefon numarası** | ⛔ **Kilitli** — "Diğer doğrulama görevlerini tamamlayın" | Kimlik onaylanmadan açılmıyor. |
| **Play'de uygulama oluşturma** | ⛔ **Kilitli** — "Uygulama oluşturmak için hesap doğrulama işlemlerini tamamlayın" | Kimlik onayı bekliyor. |
| **AdMob hesabı** | ✅ **ONAYLANDI** — "Hesap bilgileriniz doğrulandı ve reklam sunumunuz etkinleştirildi" | Reklam yayınlama izni açık. |
| **AdMob uygulaması** | ⏳ **"İnceleme gerekli / sınırlı reklam sunumu"** — "Limiti kaldırmak için mağaza ekleyin" | Play'de listelenince mağaza bağlantısı eklenecek. |
| **AdMob kurulum görevleri** | **3/4** — kalan: "Uygulama mağazasına bağlantı oluştur" | Play yayınına bağlı. |
| **app-ads.txt** | ✅ Yayında: `https://umay-multi-tv-gq3zer.freebuff.site/app-ads.txt` | `google.com, pub-7629051535337276, DIRECT, ...` |

## 2. Tamamlananlar

- Uygulama v8.0: liste/oynatıcı/çoklu ekran/radyo/50 ülke/4 dil, 305 kanal.
- İsteğe bağlı üyelik: `AccountActivity` + `Api` + `worker.js` + D1 (`users`, `oauth`).
- Üyelik sunucusu yayında: health / register / verify / resend / login / google uçları.
- Gizlilik politikası hesap verisiyle güncellendi, yayında.
- AdMob: uygulama + 3 reklam birimi (banner / geçiş / ödüllü) oluşturuldu, kimlikler kodda.
- **AdMob SDK'sı koda bağlandı** (Gradle + `play-services-ads`) ve **v9.0 olarak yayınlandı**:
  liste altı banner, 4. zap'ta 10 dakikada 1 geçiş reklamı; debug derlemede test birimleri.
  Cihazda test banner'ı, 4. açılışta geçiş reklamı ve **gerçek banner** doğrulandı.
- GitHub: **halka açık** repo + `v8.0` Release (APK indirilebilir).
- Sürüm klasörleri: `releases/v6.0`, `v7.0`, `v8.0`.

## 3. Yapılmayanlar (engel / sıra)

| İş | Neden bekliyor | Kim yapar |
|---|---|---|
| Play'de uygulama kaydı | Kimlik onayı | Google onayı sonrası ben |
| Kapalı test (12 tester / 14 gün) | Uygulama kaydı | Siz (tester listesi) + ben (yükleme) |
| AAB yükleme + mağaza kaydı (açıklama, görseller) | Uygulama kaydı yok | Ben |
| Veri güvenliği formu (e-posta toplanıyor) | Uygulama kaydı yok | Ben |
| 12 tester / 14 gün kapalı test | Play kuralı | Siz + ben |
| AdMob'a mağaza bağlantısı | Play yayını | Ben (yayından sonra) |
| Gerçek reklam sunumu | Play listelemesi + AdMob incelemesi | Google |
| ~~E-posta gönderimi (kod + hoş geldin maili)~~ | ✅ **Açıldı** (`RESEND_API_KEY` + `MAIL_FROM` eklendi, `mail:true`). Not: gönderen `onboarding@resend.dev` olduğu için şimdilik sadece hesap sahibine gider; tüm kullanıcılar için domain gerekir | — |
| Google ile giriş | `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` yok | **Siz** (Cloud Console) |
| ~~Ödüllü reklam ekranı ("60 dk reklamsız")~~ | ✅ **v9.1'de yapıldı** (Ayarlar → Reklamlar) | — |

## 4. Sıradaki adımlar (sırayla)

1. **Kimlik onayı gelince:** Play Console → Uygulama oluştur → `releases/v9.0/CanliTV-9.0.aab`
   (hazır, upload anahtarıyla imzalı, versionCode 9).
2. Mağaza kaydı: kısa/uzun açıklama, 2 ekran görüntüsü, 512 ikon, 1024×500 öne çıkan görsel
   (`docs/play/` içinde hazır), gizlilik politikası URL'si.
3. Veri güvenliği: e-posta adresi toplanıyor (isteğe bağlı hesap) işaretlenir; reklam kimliği
   kullanımı beyan edilir.
4. Kapalı test: 12 tester, 14 gün → üretime başvuru.
5. Yayından sonra AdMob → Uygulama → "Mağaza ekle" → Play bağlantısı; reklamlar gerçek gelire geçer.

## 5. Maliyet/hesap notları

- Play geliştirici hesabı: tek seferlik 25 $ (ödendi).
- AdMob: ücretsiz, ödeme eşiği 100 ₺.
- Resend: ücretsiz 3.000 mail/ay — kod ve hoş geldin mailleri için yeterli.
