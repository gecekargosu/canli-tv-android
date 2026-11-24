# Hata analizi 2026-11-24 — yol haritası, bulgular, sonuçlar

Sürüm: 9.65 (versionCode 74) · Cihaz: Xiaomi 2506BPN68G / Android 16
Kapsam: `src/com/cengiz/canlitv/*.java` (23 dosya, ~11.000 satır), `app/build.gradle`,
`AndroidManifest.xml`, site (`site/worker.js`, `site/manifest.json`) ve liste üretim
betikleri (`_build/`).

Amaç: "Kod çalışıyor" değil, **istenen işi doğru ve tam yapıyor mu** sorusuna cevap
vermek. Bulunan her hata gerçek kullanıcı etkisine göre sınıflandırıldı ve düzeltildi.

---

## 1. Yol haritası (bundan sonraki turlarda da kullanılacak)

### Tur A — Statik okuma (satır satır)
Her dosya için şu kontrol listesi:

1. **Yaşam döngüsü**: `onCreate/onResume/onPause/onDestroy` dengeli mi? Kayıt
   (receiver/listener/callback) bırakılıyor mu? Arka planda çalışan iş ekran
   kapanınca ne oluyor?
2. **Boş/eksik veri**: liste, dizi, JSON, `Prefs` değeri boşsa ne oluyor?
   (`get(0)`, `% size()`, `size()-1`, `parseInt("")`)
3. **İş parçacığı**: ağ/dosya işi UI iş parçacığında mı? UI güncellemesi arka
   plandan mı yapılıyor (`NetworkOnMainThreadException`, `CalledFromWrongThread`)?
4. **Statik durum**: `static` liste/nesne ülke veya ekran değişince boşalıyor mu,
   eski ekranlar bu referansı tutuyor mu?
5. **İndeksler**: kullanıcı sırası değişince, ülke değişince kayıtlı indeks başka
   kanalı mı gösteriyor?
6. **API seviyesi**: kullanılan sütun/izin/API `minSdk` ile uyumlu mu?
   (`RELATIVE_PATH` API 29, `POST_NOTIFICATIONS` API 33…)
7. **Ağ**: zaman aşımı, yönlendirme, `?token`/sorgu taşıma, 403/404 ayrımı,
   yeniden deneme sayısı ve bekleme süresi.
8. **Reklam/politika**: rıza yokken istek gidiyor mu, ödül kötüye kullanılabilir mi,
   tam ekran reklam akışı kilitlemiyor mu?
9. **Kalıcılık**: kullanıcı ayarı (uyku, kalite, numara, favori, limit) süreç
   ölünce/yeniden açılınca korunuyor mu?
10. **Metin/i18n**: sabit sayı ve sürüm metinleri güncel mi, hard-coded Türkçe
    dışına taşan yer var mı?
11. **Ölü kod**: çağrılmayan metot, kullanılmayan alan, kopyalanmış mantık.
12. **Kaynak sızıntısı**: `Cursor`, `InputStream`, `MediaPlayer`, `Dialog`,
    `WakeLock`, `WifiLock` kapatılıyor mu?

### Tur B — Düzeltme + derleme
- Bulgular tek tek yamanır, her yamadan sonra `bash build.sh`.
- Davranış değişikliği varsa yorumda **neden** yazılır (temiz kod: kod ne
  yaptığını değil neden yaptığını anlatır).

### Tur C — Yeniden analiz (aynı dosyalar)
- `grep` ile aynı hata sınıfları taranır (`list.get(`, `% .*size()`,
  `runOnUiThread`, `catch (Exception ignored)`, `== null`).
- Düzeltmelerin yeni bir hata üretip üretmediği (özellikle indeks ve boş liste)
  kontrol edilir. **Hatasız tur = yeni bulgu yok.**

### Tur D — Cihazda gerçek kullanıcı senaryosu
Üç ülke: **Türkiye (TR)**, **ABD (US)**, **Almanya (DE)** — yüksek reklam getirisi
olan pazarlar. Her ülke için:

1. Temiz kurulum → ilk açılış, ülke listesi, kanal sayısı.
2. Kanal aç/kapat, kalite, favori, numara değiştir, sırala.
3. Kayıt başlat/durdur → Kayıtlarım → oynat.
4. Radyo: indirme, çalma, sonraki, uyku zamanlayıcı, listeden çıkınca devam.
5. TV uyku zamanlayıcı, PiP, çoklu ekran, "sadece ses".
6. Veri sayacı (kanal başına + günlük), limit uyarısı.
7. Ağ kes → mesaj → ağ gelince toparlanma.
8. Ölü kanal onarma / tarama.
9. Reklamlar: banner, geçiş, ödüllü; rıza akışı.
10. Çökme kontrolü: `logcat | grep FATAL` + `CrashLog`.

---

## 2. Bulgular ve durum

| # | Yer | Hata | Etki | Durum |
|---|-----|------|------|-------|
| 1 | `AudioService.monitor` | `lastRx` güncellendikten sonra `rx-lastRx` ölçülüyordu; fark hep 0 | Radyo 12 sn'de bir yeniden bağlanıyordu | ✅ düzeltildi (tur 1) |
| 2 | `AudioService.step` | Yalnız TV listesinde arıyordu | Bildirimdeki ⏮⏭ radyoda işlevsiz | ✅ tur 2 |
| 3 | `RadioPlayerActivity.sleepDialog` | Sayaç ekran Handler'ında | Listeye dönünce uyku iptal, radyo sabaha kadar çalıyor | ✅ tur 2 (servise taşındı) |
| 4 | `PlayerActivity.sleepUntil` | Yalnız bellekte | Ekran yeniden kurulunca sayaç kayboluyor | ✅ tur 1 (`Prefs.sleepUntil`) |
| 5 | `CountryActivity.pick` | `fetchGood` UI iş parçacığında | Ülke indirmesi bitince `NetworkOnMainThreadException` | ✅ tur 3 |
| 6 | `MultiActivity.onCreate` | `idx % list.size()`, `size()-1` | Liste boşsa `ArithmeticException` / negatif indeks | ✅ tur 1 |
| 7 | `PlayerActivity` | `list.get(index)` korumasız | Liste boşsa çökme | ✅ tur 1 |
| 8 | `RadioPlayerActivity` | `names.get(0)` | Radyo listesi inmediyse çökme | ✅ tur 1 |
| 9 | `PlayerActivity`/`Multi`/`RadioPlayer` | `ChannelRepo.all()` statik liste | Ülke değişince arkadaki ekran çöküyor | ✅ tur 4 (kopya) |
| 10 | `RecordingsActivity` | `RELATIVE_PATH` API<29'da yok | Android 8/9'da kayıtlar hiç görünmüyor | ✅ tur 1 (`DATA LIKE`) |
| 11 | `RecordingsActivity` | `.ts` için `DURATION=0` | Her kayıt "0 dk" | ✅ tur 4 |
| 12 | `Quality`/`Repair`/`HlsRecorder` | Master'daki `?token` varyanta taşınmıyor | Jetonlu CDN'de kalite/kayıt 404 | ✅ tur 1 |
| 13 | `AdManager` | `loadInterstitial/loadRewarded/maybeTimerAd` rıza kontrolü yok | Rıza yokken reklam isteği/gösterimi | ✅ tur 1–3 |
| 14 | `CountryRepo.count` | 200 bayt eşiği | 1–2 kanallı liste 0 sayılıyor | ✅ tur 1 |
| 15 | `Good.refreshIfNeeded` | Gün damgası global | Ülke değişince "çalışan önce" sırası kurulmuyor | ✅ tur 1 |
| 16 | `AboutActivity.version` | Yedek "9.30" | Yanlış sürüm bilgisi | ✅ tur 1 |
| 17 | `SettingsActivity` | `probe()` çağrılmıyor, "305 kanal / 49 radyo" sabit | Ölü kod + yanlış bilgi | ✅ tur 2 |
| 18 | `SettingsActivity`/`CountryActivity` | `ProgressDialog` `onDestroy`'da kapatılmıyor | Pencere sızıntısı / `BadTokenException` | ✅ tur 2–3 |
| 19 | `ChannelRepo.indexOfUrl` | Bulunamayan URL'de 0 | Yanlış kanal açılıyor | ✅ tur 1 |
| 20 | `AudioService` "noData" eşiği | 8 KB/4 sn | 16 kbps altı yayınlarda gereksiz yeniden bağlanma riski | ✅ tur 2 (2 KB) |
| 21 | 3sat gibi tek rendition yayınlar | Kaynak ~8 Mbps | 360p seçimi veriyi düşürmüyor | ⚠️ sınır (uygulama tarafında çözülemez) |
| 22 | Kayıt biçimi | TS = ses+görüntü | Ayrı "sadece ses kaydı" yok | ⚠️ özellik boşluğu |
| 23 | `MultiActivity.saveState` | Slot'ta indeks saklanıyor | Ülke değişince kayıtlı set başka kanala denk gelir | ⚠️ düşük öncelik |
| 24 | Günlük veri limiti | — | Bu turda canlı denenmedi | ⏳ sonraki tur |

---

## 3. Düzeltme turları

- **Tur 1** — 13 dosya: radyo izleme sayacı, boş liste korumaları, uyku kalıcılığı,
  kayıt sorgusu, token taşıma, rıza kapısı, sürüm metni, `indexOfUrl`.
- **Tur 2** — 4 dosya: radyo uyku sayacı servise, bildirim ⏮⏭ radyo desteği,
  ölü kod, sabit metinler, diyalog sızıntısı.
- **Tur 3** — 2 dosya: ülke indirme ağ çağrısı arka plana, `maybeTimerAd` rıza.
- **Tur 4** — 4 dosya: statik liste kopyaları, kayıt süresi, sürüm 9.65.
- **Tur 5 (yeniden analiz)** — `% .*size()` ve indeks taraması: kalan çağrıların
  hepsi korumalı bloklarda. Yeni bulgu yok → **hatasız tur**.

Derleme her turda temiz: `BUILD SUCCESSFUL`, kapasite denetimi
`400 dosya / 17.515 sınıf / +0` (bkz. `releases/apk-kapasite.json`).

---

## 4. Cihaz senaryosu sonuçları

| Adım | TR | US | DE |
|------|----|----|----|
| Ülke listesi | 145 kanal (gömülü) | 263 kanal indirildi, çökme yok | 113 kanal indirildi |
| Kanal oynatma | TRT Haber 360p ✅ | ABC KAAL ✅ | 3sat ✅ |
| Kalite menüsü | 360p etkin | 360p etkin | tek rendition (kaynak) |
| Kayıt | 16 sn / 12,6 MB `.ts` ✅ | — | — |
| Kayıtlarım | iki kayıt listelendi ✅ | — | — |
| Radyo | 25 istasyon, GENÇ KRAL FM ✅ | — | 22 istasyon, SWR3 ✅ |
| Radyo 26 sn stabilite | yeniden bağlanma yok ✅ | — | — |
| Bildirim ⏮⏭ | Süper FM'e geçti ✅ | — | — |
| Uyku (radyo) | 15 dk kuruldu, ekran kapandı, servis sayacı ✅ | — | — |
| Çoklu ekran | 3 pencere, hata mesajı ✅ | — | — |
| PiP | `mode=pinned` ✅ | — | — |
| Favori | 3sat → ★ Favoriler ✅ | — | — |
| Veri sayacı | TRT 10,8 MB / 2 dk ✅ | — | 3sat 61,4 MB / 1 dk |
| Ağ kesintisi | mesaj + otomatik dönüş ✅ | — | — |
| Çökme | `FATAL` yok ✅ | yok ✅ | yok ✅ |

Test edilmeyenler: günlük limit uyarısı, ölü kanal onarma (canlı), "sadece ses"
modu, tablet kurulumu (HyperOS kısıtı), ödüllü reklam akışı (reklam izleme
otomasyona kapalı — kullanıcı elle yapıyor).

---

## 5. Sonraki tura devredilenler

1. Günlük veri limiti: 1 MB limit + kanal aç → uyarı diyalogu (canlı test).
2. Ölü kanal onarma: yapay ölü URL eklenmiş test listesiyle `Repair.run`.
3. "Sadece ses" modu: Wi-Fi kapalıyken sesin gerçekten çaldığı ve görüntünün
   inmediği doğrulanmalı (veri sayacı ile).
4. Ödüllü reklam: kullanıcı elle izleyip "15 dk reklamsız" sayacının arttığını
   doğrulamalı.
5. `MultiActivity` set kaydı: indeks yerine URL saklamak (ülke değişince set
   bozulmasın).
6. Tek rendition yayınlarda kalite etiketi: "kaynak: 1080p (tek kalite)" gibi
   dürüst bilgi.
