# UMAY Multi TV — v9.1 (versionCode 10)

Tarih: 23 Kasım 2026 · `CanliTV-9.1.apk` (5,1 MB) · `CanliTV-9.1.aab` (4,9 MB)

## Yenilikler

### 🎬 Ödüllü reklam: "60 dakika reklamsız"
- **Ayarlar → Reklamlar** satırı: *"🎬 60 dk reklamsız"*.
- Kısa video izlenince 60 dakika boyunca **hiç reklam gösterilmez** (banner + geçiş kapalı).
- Kalan süre satırda canlı yazıyor ("Reklamsız sürüyor · 57 dakika kaldı").
- Kullanıcı istemezse reklam izlemek zorunda değil; oynatma hiçbir zaman kesilmiyor.

### 📻 Radyo reklamları
- Radyo listesinin altında banner.
- Radyo açılırken frekans sınırlı geçiş reklamı (4. açılışta bir, arası 10 dakika).

### 📺 Oynatıcı altı reklam barı
- Yayının **altında** ince banner (dikey/yatay ikisinde de). Video alanı küçülür,
  **görüntünün üstüne reklam binmez** (AdMob politikası gereği bilinçli tasarım).

### 📄 İndirme sayfası
- `https://umay-multi-tv-gq3zer.freebuff.site/indir` → tek dokunuşla APK indirme sayfası
  (WhatsApp'tan paylaşılacak kısa adres).

### 📚 Mağaza hazırlığı
- `docs/play/magaza-kaydi.md`: Play'e kopyala-yapıştır başlık, kısa/tam açıklama,
  veri güvenliği cevapları, ASO (üst sıralar) notları.
- `docs/play/kapali-test.md`: 12 tester / 14 gün kapalı test adım adım.

## Test edilenler (cihaz Z5R87DU8V4LBSOVG)
| Senaryo | Sonuç |
|---|---|
| Derleme (debug + public APK + AAB) | ✅ |
| Kanal listesi banner'ı | ✅ test reklamı |
| Oynatıcı altı banner | ✅ video kapanmıyor, kontrol çubuğu üstte kalıyor |
| Radyo listesi banner'ı | ✅ (temiz kurulumda göründü) |
| Radyo açılışında geçiş reklamı | ✅ logcat: `acilis #4 sira=true` |
| Ödüllü video | ✅ gösterildi, "Ödül verildi" → 60 dk reklamsız |
| Reklamsız sürede banner/geçiş | ✅ hiçbiri gösterilmedi |
| Çökme | ✅ yok |

## Bilinen eksikler
- Play yayını geliştirici kimlik onayına bağlı (uygulama henüz Play'de değil).
- E-posta gönderimi / Google girişi: sunucuda `RESEND_API_KEY`, `MAIL_FROM`,
  `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` secret'ları bekliyor.
