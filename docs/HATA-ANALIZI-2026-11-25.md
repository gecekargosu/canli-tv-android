# Derin Hata Analizi — UMAY Multi TV 9.86 (25 Kasım 2026)

Kapsam: `MainActivity`, `PlayerActivity`, `MultiActivity`, `RadioPlayerActivity`, `AudioService`,
`Quality`, `AdManager`, `Billing`, `LogoLoader`, `ChannelRepo`, `RecordingsActivity`, `Prefs`.
Yöntem: cihaz testi (bkz. `TEST-RAPORU-2026-11-25.md`) + kod okuma + `logcat`/`dumpsys` kanıtları.

---

## 1. Kök nedeni bulunup düzeltilen hatalar

### 1.1 Radyo: sonsuz HTTP okuması (kritik, 9.85)
- **Belirti:** İstasyon seçilince "bağlanıyor…" yazısı kalıyor, ses yok, çökme yok.
- **Kanıt:** `logcat -s UmayRadio` **boş** (yani `attemptPlay` hiç çağrılmamış), buna karşılık
  `MediaFocusControl: requestAudioFocus` ve genel bildirim var. `dumpsys audio` 22:18:41'de
  "releasing player" gösteriyor.
- **Kök neden:** `AudioService.onStartCommand` → `Quality.resolveLowest()` → `Quality.fetch()`.
  `fetch()` `while ((n = in.read(buf)) > 0)` döngüsüyle **yanıtın tamamını** okuyor. Canlı AAC
  akışında yanıt hiç bitmiyor; okuma 7 sn'lik `setReadTimeout`'a da takılmıyor (her okuma veri
  döndürdüğü için). Sonuç: geri çağrı hiç çalışmıyor, `MediaPlayer` hiç kurulmuyor.
- **Düzeltme:** `.m3u8` içermeyen adreslerde doğrudan `playUrl()`; `fetch()` en fazla 256 KB okuyor.
- **Ders:** "Playlist çözümle" adımı yalnız HLS master adreslerinde çalışmalı; ses akışlarına
  uygulanmamalı.

### 1.2 Radyo: durum makinesi ile gerçek ses durumu çelişiyordu (9.86)
- **Belirti:** Ses duyulsa bile "bağlanıyor…" yazısı ve 15 sn'de bir yeniden bağlanma.
- **Kök neden:** `onInfo(MEDIA_INFO_BUFFERING_START)` → `state = S_BUFFERING`. `isPlayingStatic()`
  `state == S_PLAYING` şartı aradığı için canlı akışta neredeyse hep `false`. Bekçi (`watchdog`)
  ve durum yazısı bu fonksiyona bağlıydı.
- **Düzeltme:** `isAudibleStatic()` = `player != null && preparedOk && player.isPlaying()`.
  Radyo ekranı ve bekçi bunu kullanıyor.
- **Ders:** Canlı yayında "bağlı" durumu, tampon olaylarından bağımsız bir sinyalle ölçülmeli.

### 1.3 Klavye odağı ana listede kalıyordu (9.84)
`MainActivity.open()` klavyeyi kapatmıyordu; oynatıcıdan dönüşte liste klavye altında sıkışıyordu.
`hideSoftInputFromWindow + clearFocus` eklendi.

### 1.4 Radyo üst barı glifleri (9.84) ve kayıt süresi "0 dk" (9.84)
`⏻ ⏱ ⏹` bu cihazda tofu; düz metin. `dur < 60 sn` artık `<1 dk`.

---

## 2. Açık kalan hatalar ve riskler

### 2.1 Radyo hazırlık yarışı (yüksek)
`START_GRACE_MS = 15 sn`; TRT FM'de `prepareAsync` 15 sn'yi aşıyor. Bekçi süre dolunca
`scheduleRetry` → `releasePlayer()` → yeni `MediaPlayer`. Her deneme bir öncekini öldürdüğü için
yavaş ağda **hiç** hazır olmuyor (log: 22:25–22:26 arası 5 sn arayla "deneme 0").
**Öneri:** radyo için 30 sn grace, 8–10 sn retry, `prepareAsync` sürerken release yasağı,
`attempt` sayacının sıfırlanmaması.

### 2.2 Çoklu ekran: sessiz siyah pencere (orta)
`MultiActivity` 3 `Slot` kuruyor; yükleme hatası kullanıcıya hiç yansımıyor. Pencere başına
durum katmanı (yükleniyor / hata / tekrar dene) ve kaynak bazlı zaman aşımı gerekli.
Ayrıca çoklu ekranda sesi kapalı pencerelerin de tam kalitede indirmesi veri tüketimini artırıyor.

### 2.3 Kanal açılış süresi (orta)
ATV ~18 sn. `PlayerActivity` hata/bağlanma katmanı var ama kullanıcıya "neden bekliyorum"
bilgisi ve 20 sn'de yedek adres denemesi yok (`good_tr.json` yedekleri mevcut, bağlanmamış).

### 2.4 Liste performansı
Tüm ekranlar `ListView` + her satırda senkron logo çözümlemesi. 300+ satırda `getView`
maliyeti yüksek; `LogoLoader` disk önbelleği yardımcı oluyor ama görünür alan dışı istekler
sürüyor. `RecyclerView` + `Glide` benzeri önbellek önerilir.

### 2.5 R8 kapalı
`minifyEnabled false`. APK 7,4 MB; sınıf adları açıkta. Play için zorunlu değil, ama
boyut/indirme dönüşümü ve tersine mühendislik maliyeti için açılmalı (keep kuralları: Billing,
UMP, AdMob, Media3).

### 2.6 Abonelik: istemci taraflı doğrulama
`Billing` satın almayı Play'den doğruluyor, ancak **sunucu taraflı makbuz doğrulaması yok**.
Cihazda `Prefs.subActive` değiştirilirse reklamlar kapanır. v1 için kabul edilebilir; gelir
büyüdükçe Play Developer API ile doğrulama ve hesap eşleştirme gerekir.

### 2.7 Reklam: NO_FILL
Kod tarafı doğru (test birimleri release'de gerçek kimliklere dönüyor, UMP var, app-ads.txt
yayında). Sorun çevresel: uygulama mağazada değil → AdMob sınırlı sunum. Play yayını sonrası
izlenmeli; 48 saatte dolmazsa mediation ve native reklam.

### 2.8 Hata günlüğü ve gizlilik
`CrashLog` cihazda tutuyor, gönderim varsayılan kapalı, politika bunu doğru anlatıyor. Ancak
günlük dosyası sınırsız büyüyebilir; 512 KB'ta döndürülmeli.

### 2.9 Yapısal borç
`MainActivity` 61 KB, `PlayerActivity` 62 KB, `SettingsActivity` 57 KB. Tek sınıfta UI + veri +
reklam + kalite mantığı. Otomatik test yok; her sürüm elle 17 adımda doğrulanıyor.
`targetSdk 36` edge-to-edge: üst bar sistem çubuğu altında kalma riski yalnız bu cihazda
(Android 16) test edildi; Android 14/15 ve katlanabilirlerde doğrulanmadı.

---

## 3. Play'e hazır mı? (teknik görüş)

**Evet, kapalı testi başlatacak kadar hazır; üretim yayını için iki şart var.**

| Alan | Durum |
|---|---|
| AAB + imza + versionCode | ✅ 9.86 derlendi, imza temiz |
| targetSdk 36 / minSdk 26 | ✅ |
| Gizlilik politikası, kullanım koşulları, telif bildirimi | ✅ sitede yayında |
| UMP rızası + AD_ID beyanı | ✅ |
| Hesap/üyelik zorunluluğu | ✅ yok |
| Çökme | ✅ 17 adımda yok |
| Radyo oynatma | 🟡 yavaş ağda kararsız (MP-90) |
| Çoklu ekran | 🟡 pencere yükleme durumu görünmüyor (MP-91) |
| Telif | ❌ hukuki doğrulama kullanıcıda (MP-93) |
| Play Console işleri (form, 12 tester/14 gün, abonelik ürünü) | 👤 kullanıcıda |

Kapalı test 14 gün sürdüğü için MP-90/91/92 bu süre içinde düzeltilip üretim öncesi sürüm
çıkarılabilir; bunlar yayını engelleyen Play politikası ihlali değil, kalite sorunları.
