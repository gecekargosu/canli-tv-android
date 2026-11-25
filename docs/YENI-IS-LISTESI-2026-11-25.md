# Yeni İş Listesi — Claude / Antigravity / Master Plus karşılaştırması (25 Kasım 2026)

**Kaynak dosyalar**
- `docs/raporlar/UMAY Multi TV — Derin Analiz ve Yol Haritası (v9.16).md` (Claude, 400 satır)
- `docs/raporlar/Antigravity Derin Analiz Raporu – UMAY‑MULTI‑TV.txt` (9.70)
- `docs/MASTER-IS-LISTESI-2026-11-24.md`, `docs/is-listesi.md`, `docs/ISLER-ANTIGRAVITY-990.md`
- `docs/MASTER-PLUS-2026-11-25.md` (bölüm 1–15), `docs/play/PLAY-YAYIN-HAZIRLIK.md`
- Bu turdaki cihaz testi: `docs/TEST-RAPORU-2026-11-25.md`

Durum: ✅ yapıldı · 🟡 kısmi · ❌ yok / geçersiz

---

## 1. Karşılaştırma tablosu

| # | Madde | Kaynak | Durum | Kanıt / not |
|---|---|---|---|---|
| 1 | Telif riski: ücretli/yetkisiz kanalları çıkar | Claude P0-1 | 🟡 | Liste resmî ücretsiz kaynaklara çekildi, `good_tr.json` denetimi var; yine de tüm kaynaklar hukuken doğrulanmadı |
| 2 | Gizlilik politikası gerçeği anlatsın (AdMob + IP) | Claude P0-2 | ✅ | `site/public/gizlilik-politikasi.html` reklam, AD_ID, IP, UMP anlatıyor; `ipapi.co` kodda yok |
| 3 | AB/UK için UMP rıza ekranı | Claude P0-3 | ✅ | `AdManager` içinde `ConsentInformation` + "Reklam gizlilik tercihleri" satırı |
| 4 | Bozuk üyelik sistemini kaldır | Claude P0-4 | ✅ | Giriş/hesap arayüzü yok; metinler yalnız `Lang` içinde kullanılmıyor |
| 5 | Bildirimdeki **Durdur** çalışsın | Claude P0-5 | ✅ | `onStartCommand` `action=stop` yakalıyor |
| 6 | Radyoda ⏭ TV kanalına geçmesin | Claude P0-5 | ✅ | `step()` yalnız radyo listesinde geziyor |
| 7 | Ses odağı (audio focus) | Claude P0-5 | ✅ | `requestAudioFocus` logda görülüyor |
| 8 | Oynatıcı kapanınca ses dursun | Claude P0-5 | ✅ | Tek kanal kapatma testinde ses kesildi |
| 9 | **Radyo gerçekten çalsın** | Claude P0-5 | 🟡 | 9.85'te kök neden (sonsuz playlist okuma) düzeltildi; yavaş ağda hâlâ gecikmeli — B-06 |
| 10 | Mağaza başlığı ≤30 karakter | Claude P0-6 | ✅ | "UMAY Multi TV" |
| 11 | Açıklamada listede olmayan kanal olmasın | Claude P0-6 | ✅ | `docs/play/magaza-kaydi.md` 9.77'de güncellendi |
| 12 | İmza şifresi depoda açık olmasın | Claude P0-7 | ✅ | `keystore.properties` git'te izlenmiyor; `build.gradle` dosyadan okuyor |
| 13 | `READ_MEDIA_VIDEO` iznini kaldır | Antigravity D | ✅ | Manifestte yok (yalnız API≤28 depolama izinleri) |
| 14 | ExoPlayer / Media3'e geç | Claude + Anti | ✅ | `PlayerActivity` Media3 kullanıyor |
| 15 | RecyclerView | Antigravity B | ❌ | Hâlâ `ListView`; 300+ satırda kaydırma maliyeti |
| 16 | Veri tasarrufu ↔ kalite tutarlılığı | Antigravity 2 | ✅ | 360p varsayılan, `Quality` önbelleği |
| 17 | Interstitial frekans sınırı | Antigravity D | ✅ | `AdManager.maybeInterstitial` (4. kanal / 10 dk) |
| 18 | Rewarded reklam bağlansın | Antigravity D | ✅ | "15 dk reklamsız" şeridi çalışıyor |
| 19 | Yatay/dikey reklam barı | Antigravity 4 | ✅ | 4 ekranda doğrulandı (9.81) |
| 20 | İndirme sayfası güncel sürüm | Antigravity 4 | 🟡 | Site 9.82'de; 9.86 AAB/APK yayınlanınca güncellenecek |
| 21 | Back tuşu tutarlılığı (çoklu ekran) | Antigravity C | ✅ | Çoklu ekrandan çıkış ana listeye dönüyor |
| 22 | Üst bar ikon boyutu ≥44–48dp | Anti + Claude | ✅ | 44dp kutular, 21–28sp ikonlar (9.79–9.81) |
| 23 | Tüm metinler `strings.xml` | Antigravity A | 🟡 | `Lang.java` 4 dilli sözlük var; tam externalization yok |
| 24 | R8/minify açık | — | ❌ | `minifyEnabled false`; APK 7,4 MB |
| 25 | Otomatik test (JUnit/Espresso) | Antigravity 4 | ❌ | Hiç yok; her sürüm elle doğrulanıyor |
| 26 | MainActivity refactor (MVP/MVVM) | Antigravity A | ❌ | Tek sınıf ~50 KB |
| 27 | Backend SSL/https | Antigravity E | ✅ | Tüm uçlar https |
| 28 | Data safety formu (AD_ID, e-posta yok) | Antigravity E | ✅ | `PLAY-YAYIN-HAZIRLIK.md` tablosu gerçek davranışla uyumlu |
| 29 | 12 tester / 14 gün kapalı test | Master | 👤 | Kullanıcıda (Play Console) |
| 30 | AAB yükleme + mağaza formları | Master | 👤 | Kullanıcıda |

**Sonuç:** İki AI raporunun maddelerinin büyük kısmı 9.79–9.86 arasında kapatıldı. Kalan gerçek
teknik borç: radyo açılış kararlılığı, çoklu ekran yükleme durumu, liste performansı, test altyapısı,
R8 ve sınıf ayrıştırma. Telif hâlâ tek başına en büyük **yayın** riski (teknik değil, hukuki).

---

## 2. Yeni iş listesi

### P0 — Play yayınından önce (yayını doğrudan etkiler)
- **MP-90 · Radyo açılış kararlılığı:** grace 30 sn, retry 8–10 sn, hazır değilken oynatıcıyı bırakma; 3 istasyonda doğrula. (B-06)
- **MP-91 · Çoklu ekran durum rozetleri:** her pencerede "yükleniyor / hata / tekrar dene"; siyah pencere kalmasın. (B-08)
- **MP-92 · Kanal açılış ipucu:** 8 sn'den sonra "yayın yavaş açılıyor" + iptal; 20 sn'de otomatik yedek adres. (B-07)
- **MP-93 · Telif denetimi:** `good_tr.json` dışındaki kaynakları gözden geçir; ücretli kanal ve ham IP kalmadığını belgele.
- **MP-94 · Yeni AAB:** 9.86 için `make-aab.sh` → Play kapalı test yüklemesi (kullanıcı).

### P1 — Yayından sonra ilk hafta
- **MP-95 · Radyo çip taşması** (B-12) ve **düzen modunda çift "Bitti"** (B-11).
- **MP-96 · Kayıt süresi:** `.ts` için `MediaMetadataRetriever` yerine dosya adı/süre defteri; 1 dk altı `<1 dk` (yapıldı), üstü doğru gösterilsin.
- **MP-97 · Liste performansı:** `RecyclerView` + logo yüklemeyi görünür alanla sınırla.
- **MP-98 · R8 + resource shrinking:** `minifyEnabled true`, keep kuralları; APK'yı ~%25 küçült.
- **MP-99 · Abonelik uçtan uca test:** Play ürünü tanımlandıktan sonra satın al → `Prefs.subActive` → reklamlar kapalı → iade/iptal.

### P2 — Bakım
- **MP-100 · Duman testi:** `_build/user_test.py` gibi bir betikle her sürümde 17 adım + ekran görüntüsü.
- **MP-101 · `Lang` tam externalization** (strings.xml) ve çeviri eksiği kontrolü.
- **MP-102 · MainActivity bölme:** liste/oynatıcı/reklam sorumluluklarını ayır.
- **MP-103 · Site `/indir` sürümü** her yayında otomatik güncel sürüme bağlansın.
