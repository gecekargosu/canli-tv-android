# UMAY Multi TV — v8.0 (versionCode 8)

Tarih: 23 Kasım 2026 · `CanliTV-8.0.apk` (119 KB) · `CanliTV-8.0.aab` (116 KB)

## Yenilikler

### 👤 Hesap (isteğe bağlı üyelik)
- **Ayarlar → Hesap** (veya ana ekran ⚙): *Giriş yap* / *Üye ol* / *Google ile devam et*.
- E-posta + şifre ile kayıt: şifre cihazda **PBKDF2-HMAC-SHA256 (120.000 tur)** özetlenir,
  sunucuya düz şifre gitmez.
- Kayıttan sonra **6 haneli doğrulama kodu** e-postaya gider; kod girilince hesap doğrulanır ve
  **hoş geldin maili** gönderilir.
- **Google ile giriş:** tarayıcıda Google oturumu → ekranda 6 haneli kod → uygulamaya yaz.
- **Zorunlu değil:** "Misafir olarak devam et" her zaman var; hiçbir özellik kilitlenmez.
- Sunucu kapalı/çevrimdışıysa hesap telefonda tutulur, uygulama normal çalışır.

### 🌐 Üyelik sunucusu (yeni)
- `site/worker.js`: Cloudflare Worker + D1 (`users`, `oauth` tabloları).
- Uç noktalar: `/api/register`, `/api/verify`, `/api/resend`, `/api/login`, `/api/health`,
  `/api/google/start`, `/api/google/callback`.
- Kurallar: kod 15 dk geçerli, 6 yanlış denemede kapanır, tekrar gönderme 45 sn.
- Yayında: `https://umay-multi-tv-gq3zer.freebuff.site` (gizlilik politikası aynı sitede).

### 📢 AdMob (konsolda hazır, SDK sonraki sürümde)
- AdMob'da **UMAY Multi TV** uygulaması ve 3 reklam birimi oluşturuldu; kimlikler `Ads.java`'da.
- Bu sürümde **reklam gösterilmiyor** (SDK AAR olduğu için Gradle gerekiyor). Ayrıntı:
  `docs/play/admob-kurulum.md`.

### Düzeltmeler
- Hesap ve Ayarlar ekranları durum çubuğunun altından başlıyor (edge-to-edge boşluğu).
- Ayarlar'daki "Reklam ve üyelik" satırı güncellendi (artık "üyelik yok" demiyor).

## Test edilenler (cihaz Z5R87DU8V4LBSOVG)
| Senaryo | Sonuç |
|---|---|
| Kurulum (`adb install -r`) | ✅ Success |
| Açılış + 305 kanal listesi | ✅ |
| Kanal açma → canlı yayın (TV8) | ✅ oynatıcı, süre/veri sayacı çalışıyor |
| Geri tuşu → ana ekran | ✅ |
| Ayarlar açılışı, yeni "Hesap" satırı | ✅ |
| Hesap ekranı (giriş/üye ol/misafir) | ✅ |
| Kayıt → sunucuya ulaştı, kod adımı | ✅ (posta kapalı mesajı dürüst) |
| Çıkış yap → misafire dönüş | ✅ |
| Çökme kaydı (`logcat -b crash`) | ✅ boş |
| Sunucu uçları (curl) | ✅ health/register/login/verify yanıtları doğru |

## Bilinen eksikler / sıradaki adım
- **E-posta gönderimi kapalı:** `RESEND_API_KEY` + `MAIL_FROM` eklenince açılır (`docs/play/uyelik-kurulum.md`).
- **Google girişi kapalı:** `GOOGLE_CLIENT_ID` + `GOOGLE_CLIENT_SECRET` eklenince açılır (aynı dosya).
- **Reklamlar:** Gradle geçişi + `play-services-ads` (adım adım `docs/play/admob-kurulum.md`).
- Çevrimdışı hesap girişi yalnızca bu telefonda daha önce kayıt olunan hesapla çalışır.
