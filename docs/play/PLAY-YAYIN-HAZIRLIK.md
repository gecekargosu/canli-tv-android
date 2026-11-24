# Play Store yayını — resmî/evrak hazırlık kontrol listesi

**Sürüm:** 9.44 · **Paket:** `com.cengiz.canlitv` · **Tarih:** 24 Kasım 2026
**Amaç:** Play incelemesinde takılmamak için gereken belge, beyan ve metinlerin tek listesi.

Durum: ✅ hazır · 🟡 kısmen · 👤 senin yapman gereken (Play Console / hesap)

---

## A. Teknik zorunluluklar (uygulama tarafı)

| Konu | Durum | Not |
|---|---|---|
| AAB (Android App Bundle) | ✅ | `make-aab.sh` → `CanliTV.aab` |
| targetSdk | ✅ | 36 (Play 2026 şartını karşılıyor) |
| minSdk | ✅ | 26 (Android 8+) |
| İmza | ✅ | Yükleme anahtarı `CN=UMAY Multi TV`; Play App Signing açılmalı |
| `versionCode` artışı | ✅ | 53 (9.44) |
| 64-bit | ✅ | mimari bağımsız Java/Kotlin |
| İzin gerekçeleri | ✅ | POST_NOTIFICATIONS, INTERNET, ACCESS_NETWORK_STATE; hassas izin yok |
| Cleartext | ✅ | Varsayılan HTTPS, yalnız beyaz listedeki eski HTTP radyo alan adları |
| Reklam kimliği | ✅ | `com.google.android.gms.permission.AD_ID` yalnız AdMob için |
| Veri toplama | ✅ | Hesap yok, analitik yok, sunucuda profil yok |

## B. Mağaza kaydı (Play Console)

| Alan | Cevap / dosya | Durum |
|---|---|---|
| Uygulama adı | UMAY Multi TV | ✅ |
| Kısa açıklama (80) | `docs/play/magaza-kaydi.md` | ✅ |
| Tam açıklama | Aynı dosya (145 kanal · 22+ radyo · 3'lü izleme) | ✅ |
| Uygulama ikonu 512×512 | `docs/play/ikon-512.png` | ✅ |
| Öne çıkan görsel 1024×500 | `docs/play/one-cikan-1024x500.png` | 🟡 yenilenmeli (reklam/3. taraf kare yok) |
| Telefon ekran görüntüleri | `docs/play/v9-reklam.png`, `v91-radyo.png` | 🟡 reklamsız yeni set çekilmeli |
| Kategori | Eğlence (Video oynatıcı/Medya) | 👤 |
| İletişim e-postası | cengizkilic1980@gmail.com | ✅ |
| Web sitesi | https://umay-multi-tv-gq3zer.freebuff.site | ✅ |
| Gizlilik politikası URL | https://umay-multi-tv-gq3zer.freebuff.site/gizlilik-politikasi | ✅ |
| Kullanım koşulları URL | https://umay-multi-tv-gq3zer.freebuff.site/kullanim-kosullari | ✅ (yeni) |
| Telif/kaldırma URL | https://umay-multi-tv-gq3zer.freebuff.site/telif-bildirimi | ✅ (yeni) |

## C. Beyanlar (Play Console → Uygulama içeriği)

1. **Reklamlar:** "Uygulama reklam içeriyor" → **Evet** (AdMob banner + geçiş + ödüllü).
2. **Uygulama erişimi:** Tüm içerik girişsiz erişilebilir → "Tüm işlevler kısıtlama olmadan kullanılabilir".
3. **İçerik derecelendirmesi:** Sorular → şiddet/cinsellik/kumar **yok**; "kullanıcılar arası etkileşim" **yok**;
   "kullanıcı tarafından oluşturulan içerik paylaşımı" **yok**. Haber kanalları nedeniyle "Haber" uygulaması
   **değildir** (yalnızca yayın izleme); yine de incelemede "haber içeriği barındırır" notu eklenebilir.
4. **Hedef kitle:** 18+ / yetişkin (çocuklara yönelik değil). "Çocuklara yönelik" **Hayır**.
5. **Haber uygulaması:** Hayır.
6. **COVID-19 uygulaması:** Hayır.
7. **Veri güvenliği (Data safety):** aşağıdaki tabloyu birebir doldur.

### Veri güvenliği tablosu (gerçek davranış)

| Veri türü | Toplanıyor mu? | Paylaşılıyor mu? | Amaç | Not |
|---|---|---|---|---|
| Reklam kimliği (AD_ID) | Evet (Google AdMob SDK) | Evet (Google) | Reklam/ölçüm | Uygulama kendi sunucusuna göndermez |
| Cihaz veya diğer kimlikler | Hayır | — | — | — |
| Yaklaşık konum | Hayır | — | — | Ülke yalnız SIM/ağ kodu + dil, sunucuya gitmez |
| Hassas konum | Hayır | — | — | — |
| Kişisel bilgiler (ad/e-posta) | Hayır | — | — | Hesap yok |
| Uygulama etkinliği (izleme geçmişi) | Hayır (yalnız cihazda) | — | — | Favoriler/geçmiş cihazda |
| Uygulama içi aramalar | Hayır | — | — | — |
| Çökme günlükleri | Hayır (otomatik gönderim yok) | — | — | Kullanıcı isterse e-posta ile gönderir |
| Finansal bilgi | Hayır | — | — | — |
| Sağlık | Hayır | — | — | — |
| Mesajlar | Hayır | — | — | — |
| Dosyalar ve belgeler | Hayır | — | — | — |

Ayrıca işaretle: **"Veriler aktarım sırasında şifrelenir" → Evet**; **"Kullanıcı verilerinin silinmesini
talep edebilir" → Evet** (e-posta ile; hesap olmadığı için cihaz verisi kullanıcıda).

## D. Hukuki / telif evrakı

| Belge | Durum | Nerede |
|---|---|---|
| Gizlilik politikası (AdMob/UMP/Cloudflare/3. taraf yayıncılar) | ✅ 2.0 | `/gizlilik-politikasi.html` |
| Kullanım koşulları | ✅ | `/kullanim-kosullari.html` |
| Telif ve içerik kaldırma bildirimi (24–48 saat) | ✅ | `/telif-bildirimi.html` |
| app-ads.txt | 🟡 | `site/public/app-ads.txt` yayında; **kök alan adı** gerekliliği için kendi alan adı (M-10) |
| Marka/isim hakkı | 🟡 | "UMAY Multi TV" ismi ve ikon sana ait; başka marka/logo kullanılmıyor |
| Yayın kaynakları envanteri | 🔧 | M-13 `kanallar.json` (kaynak alan adı + hak durumu) |
| Avukat görüşü (telif) | 👤 | M-18 |

## E. Yayın öncesi son adımlar (sırayla)

1. Play Console → yeni uygulama → **Uygulama imzalama** (Play App Signing) aç.
2. Mağaza kaydını B bölümündeki metinlerle doldur, görselleri yükle (reklamsız yeni set).
3. C bölümündeki beyanları ve Data safety tablosunu birebir gir.
4. **Kapalı test** (en az 12 test kullanıcısı, 14 gün) → `docs/play/kapali-test.md`.
5. Üretime geçiş: AAB yükle → inceleme → yayın.
6. Yayından sonra: güncellemeler yeni `versionCode` ile; aynı imza.

## F. İncelemede en sık takılma nedenleri ve bizim durumumuz

- **Telifli yayın:** En büyük risk. Savunma: yalnız halka açık kaynaklar + 48 saatte kaldırma taahhüdü +
  iletişim adresi. (M-15, M-13 tamamlanınca güçlenir.)
- **Reklam politikası:** Reklamlar içeriğin üzerine binmiyor, zorunlu tıklama yok, ödüllü reklam
  isteğe bağlı. ✅
- **Hassas izin:** Yok. ✅
- **Veri güvenliği uyuşmazlığı:** Beyan ile kod aynı (AdMob dışında veri yok). ✅
- **Kırık bağlantı/boş ekran:** İnceleme sırasında internet zayıfsa "Bağlantı sorunu · yeniden deneniyor"
  ve "Tekrar dene" butonu görünür. ✅
