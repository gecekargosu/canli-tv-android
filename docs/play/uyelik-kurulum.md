# Üyelik sistemi — nasıl çalışır, kalan iki ayar

Uygulama tarafı ve sunucu tarafı hazır. E-posta gönderimi ile Google girişi için **iki anahtar**
gerekiyor; onlar eklenince ikisi de kendiliğinden açılır (kod tarafında değişiklik yok).

## 1. Mimari

```
Android (saf HttpURLConnection, ek kütüphane yok)
  AccountActivity ──► Api ──► https://umay-multi-tv-gq3zer.freebuff.site/api/...
                                     │
                          Worker (site/worker.js)
                                     │
                        D1 veritabanı (users, oauth)
                                     │
                          Resend (e-posta) / Google OAuth
```

- **Üyelik isteğe bağlı:** "Misafir olarak devam et" her zaman var; hiçbir özellik kilitli değil.
- Şifre cihazda **PBKDF2-HMAC-SHA256 (120.000 tur)** ile özetlenir, sunucuya **düz şifre gitmez**.
- Doğrulama kodu 6 hane, 15 dakika geçerli, 6 yanlış denemede kapanır, tekrar gönderme 45 saniye sınırlı.
- Sunucu kapalıysa uygulama çevrimdışı çalışır (hesap telefonda tutulur, kanallar aynen açılır).

### Uç noktalar
| İstek | Gövde | Yanıt |
|---|---|---|
| `GET /api/health` | — | `{ ok, mail, google }` |
| `POST /api/register` | `{ email, name, hash }` | kod maili gönderilir |
| `POST /api/verify` | `{ email, code }` | doğrulanır + **hoş geldin maili** |
| `POST /api/resend` | `{ email }` | yeni kod |
| `POST /api/login` | `{ email, hash }` | `{ ok, name, verified }` |
| `GET /api/google/start` | — | Google'a yönlendirir |
| `GET /api/google/callback` | — | ekranda 6 haneli giriş kodu gösterir |

D1: `users(email PK, name, hash, verified, code, code_exp, tries, last_send, created, provider)`,
`oauth(state PK, verifier, exp)`. Tablolar ilk istekte otomatik kurulur.

## 2. Kalan ayar #1 — e-posta gönderimi (Resend)

Şu an `mail:false` olduğu için kod maili **gitmiyor**; uygulama "Hesap oluşturuldu ancak posta
gönderimi kapalı" diyor (dürüst davranıyor, sahte "gönderildi" demiyor).

1. https://resend.com → ücretsiz hesap (3.000 mail/ay).
2. **Domains → Add domain** ya da hızlı test için `onboarding@resend.dev` gönderen adresi.
3. **API Keys → Create** → anahtarı kopyala.
4. Sites panelinde **Secrets** → `RESEND_API_KEY` = anahtar, `MAIL_FROM` = `UMAY Multi TV <no-reply@senin-domainin>`.
5. `curl https://umay-multi-tv-gq3zer.freebuff.site/api/health` → `{"mail":true}` görmelisin.

> Alternatif: kendi SMTP/servisin varsa `sendMail()` fonksiyonundaki `fetch` adresini değiştirmek yeterli.

## 3. Kalan ayar #2 — Google girişi

Şu an `google:false`; uygulamadaki buton `.../giris` sayfasını açar ve "henüz açılmadı" der.

1. https://console.cloud.google.com → proje seç → **APIs & Services → OAuth consent screen**
   (External, uygulama adı "UMAY Multi TV", destek e-postası, gizlilik: `https://umay-multi-tv-gq3zer.freebuff.site/gizlilik-politikasi`).
2. **Credentials → Create credentials → OAuth client ID → Web application**.
3. **Authorized redirect URI:** `https://umay-multi-tv-gq3zer.freebuff.site/api/google/callback`
4. Sites → Secrets: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`.
5. `curl .../api/health` → `{"google":true}`. Uygulamada **Google ile devam et** → tarayıcı →
   Google → ekranda 6 haneli kod → uygulamada "Google'dan aldığım kodla gireceğim" satırına yaz.

> Not: Google, yayına almadan önce uygulamayı "Testing" modunda tutar; 100 test kullanıcısı ekleyebilirsin.

## 4. Ayarlar nerede

- Uygulama: **Ayarlar → Hesap** (üstte) veya ana ekran → ⚙.
- Sunucu: `CANLI-TV-APP/site/worker.js` (+ `site/manifest.json`), deploy: Sites → Publish.
- Yeni sürümde değişiklik: `bash make-aab.sh` (APK + AAB) ve `releases/vX.Y/`.

## 5. Test kayıtları

```
curl -s https://umay-multi-tv-gq3zer.freebuff.site/api/health
curl -s -X POST .../api/register -H "content-type: application/json" -d '{"email":"a@b.com","name":"A","hash":"<64+ karakter>"}'
curl -s -X POST .../api/verify   -H "content-type: application/json" -d '{"email":"a@b.com","code":"123456"}'
```
