# UMAY Multi TV — Tam Kullanıcı Senaryosu Testi (25 Kasım 2026)

**Sürüm:** 9.83 → 9.86 (kod 92–95) · **Paket:** `com.cengiz.canlitv`
**Cihaz:** Xiaomi (MIUI, Android 16 / SDK 36), 1280×2772, 4G
**Yöntem:** Uygulama gerçek cihazda kuruldu, `uiautomator` ile kullanıcı gibi dokunuldu; her adımda ekran görüntüsü alındı.
**Görüntü arşivi:** `docs/test-983/` (17 adım) + `docs/test-984/` (düzeltme doğrulaması)
**Video için önerilen sıra:** `01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 09 → 10 → 11 → 12 → 13 → 14 → 15 → 16 → 17`
(Küçük `-s.png` dosyaları önizleme içindir; video için tam boy PNG'leri kullan.)

---

## 1. Adım adım sonuçlar

| # | Senaryo | Beklenen | Sonuç | Ekran |
|---|---|---|---|---|
| 1 | Uygulamayı aç | Ana ekran, marka kartı, 6 sekme, kanal listesi | ✅ Çalıştı | `01-ana-ekran.png` |
| 2 | "Kanal ara" → `trt` | Liste filtrelenir, sayaç düşer | ✅ Çalıştı (127 → 15 kanal) | `02-arama.png` |
| 3 | No alanına `1` + **Git** | "Kanal 1 · Açılsın mı?" onayı | ✅ Çalıştı | `03-no-ile-ac.png` |
| 4 | **Aç** → ATV oynatıcı | Yayın açılır | 🟡 6 sn'de "yeniden bağlanıyor"; ~18 sn'de açıldı | `04-tekil-oynatici.png` |
| 5 | Ayarlar | Abonelik satırı **en üstte** | ✅ 9.83 ile en başa alındı | `05-ayarlar-abonelik-ustte.png` |
| 6 | "Reklamsız abonelik" | Play Billing dialogu, fiyat 20 ₺/ay | ✅ Dialog açıldı; ürün tanımsız olduğu için "satışta değil" | `06-abonelik-dialog.png` |
| 7 | **★ Favori** sekmesi | Sadece favoriler | ✅ Çalıştı | `07-favori.png` |
| 8 | **🕘 Son** sekmesi | Son izlenenler | ✅ Çalıştı | `08-son.png` |
| 9 | **🗑 Gizli** sekmesi | Gizli kanallar + bilgi notu | ✅ Çalıştı | `09-gizli.png` |
| 10 | **✏️ Düzen** | Çoklu seçim çubuğu | ✅ Çalıştı (çift "Bitti" — B-11) | `10-duzenleme.png` |
| 11 | Kanala uzun bas | Kanal menüsü (favori/gizle/numaralandır) | ✅ Çalıştı | `11-kanal-menusu.png` |
| 12 | **▦ Çoklu** (3'lü) | 3 pencere aynı anda | 🟡 2 pencere açıldı, 3. hâlâ siyah (B-08) | `12-coklu-ekran.png` |
| 13 | **📻 Radyo** | 25 istasyon listesi | ✅ Çalıştı | `13-radyo-liste.png` |
| 14 | İstasyon çal | Ses + "canlı yayın" durumu | ❌ 9.83'te hiç açılmadı → 9.85/9.86'da düzeltildi, ağ yavaşsa hâlâ gecikmeli (B-01, B-06) | `14-radyo-calisiyor.png` |
| 15 | Ülke bayrağı | Ülke listesi + kanal sayısı | ✅ Çalıştı | `15-ulke-secimi.png` |
| 16 | **⏺ Kayıtlarım** | Kayıt dosyaları listesi | 🟡 Liste çalıştı, süre "0 dk" görünüyordu (B-05) | `16-kayitlar.png` |
| 17 | Ana ekrana dön | Radyo çalıyorsa üstte "çalıyor" şeridi | ✅ Şerit + **Kapat** çalıştı | `17-ana-ekran-son.png` |

---

## 2. Bulgular (önem sırasıyla)

### B-01 · KRİTİK · Radyo hiç açılmıyordu — **düzeltildi (9.85)**
`AudioService.onStartCommand` her adreste `Quality.resolveLowest()` çağırıyordu. `Quality.fetch()` yanıtı
EOF'a kadar okuyor; canlı AAC akışında EOF hiç gelmediği için okuma **sonsuz** sürüyor, geri çağrı hiç
çalışmıyor, `MediaPlayer` hiç kurulmuyordu. Kullanıcı sadece "bağlanıyor…" görüyordu.
**Düzeltme:** `.m3u8` içermeyen adreslerde doğrudan `playUrl()`; `fetch()` en fazla 256 KB okuyor.

### B-02 · YÜKSEK · Durum yazısı "bağlanıyor…" da kalıyordu — **düzeltildi (9.86)**
Canlı akışta `MEDIA_INFO_BUFFERING_START` durumu `S_BUFFERING` yapıyor; `isPlayingStatic()` bu yüzden
sürekli `false` dönüyordu. Ses duyulsa bile yazı değişmiyor, üstelik 15 sn'de bir yeniden bağlanma
denemesi tetikleniyordu. **Düzeltme:** `AudioService.isAudibleStatic()` (hazır + `isPlaying()`),
radyo ekranı ve bekçi artık bunu kullanıyor.

### B-03 · ORTA · Klavye geri dönüşte açık kalıyordu — **düzeltildi (9.84)**
Aramada klavye açıkken kanal açıp geri dönünce liste klavyenin altında sıkışıyor, "No" alanı odaklı
kalıyordu. **Düzeltme:** `MainActivity.open()` klavyeyi kapatır ve odağı temizler (`test-984/klavye-sonra.png`).

### B-04 · KOZMETİK · Radyo üst barındaki glifler kutu görünüyordu — **düzeltildi (9.84)**
`⏻ Çık`, `⏱ Uyku`, `⏹ Durdur` bu cihazda tofu kutusu çiziyordu; düz metne çevrildi.

### B-05 · ORTA · 1 dakikadan kısa kayıtlar "0 dk" — **düzeltildi (9.84)**
`RecordingsActivity` artık `<1 dk` yazıyor.

### B-06 · AÇIK (YÜKSEK) · Radyo açılışı çok yavaş, bazı ağlarda hiç açılmıyor
`MediaPlayer.prepareAsync()` TRT FM'de 15 sn'yi aşıyor; bekçi `START_GRACE_MS = 15 sn` sonunda
`scheduleRetry` ile oynatıcıyı bırakıp yeniden kuruyor (denemeler ~5 sn arayla). Hazırlık hiç
tamamlanamıyor. **Öneri:** radyo için grace 30 sn, yeniden deneme aralığı 8–10 sn, "hazır değilse
oynatıcıyı bırakma" koşulu; ilk 15 sn'de hata yoksa bekçi hiç dokunmasın.

### B-07 · AÇIK (ORTA) · Kanal açılışı 15–20 sn
ATV 22:14'te ~18 sn'de görüntü verdi (kaynak + cihaz ağı). Kullanıcıya "yayın hazırlanıyor" ilerlemesi
ve ilk 8 sn'de "yavaş açılıyor" ipucu gösterilmeli.

### B-08 · AÇIK (ORTA) · 3'lü ekranda pencere 9 sn sonra hâlâ siyah
3 pencereden biri yüklenmedi (`12-coklu-ekran.png`). Pencere başına durum rozeti (yükleniyor/hata)
ve "tekrar dene" gerekli; hata varsa sessiz siyah kalmasın.

### B-09 · AÇIK (ORTAM) · Reklamlar NO_FILL
AdMob "sınırlı sunum" veriyor (uygulama mağazada değil). Play yayını sonrası açılır; kodda hata yok.

### B-10 · AÇIK (ORTAM) · Abonelik satın alma test edilemiyor
Play Console'da `umay_reklamsiz_aylik` ürünü tanımlı değil; lisans testi hesabı gerekiyor.

### B-11 · AÇIK (KOZMETİK) · Düzen modunda iki "Bitti"
Sekme çubuğundaki kalem "✓ Bitti" olurken düzenleme çubuğunda da "Bitti" var. Tek yerde bırakılmalı.

### B-12 · AÇIK (KOZMETİK) · Radyo sekme çipleri taşıyor
"🎵 Müzik" sağdan kesiliyor (`13-radyo-liste.png`); çip satırı yatay kaydırılabilir olmalı.

---

## 3. Çalışan / çalışmayan özeti

**Çalışıyor:** ana ekran ve marka kartı, arama + filtre sayacı, No+Git ile açma, 6 sekme, gizli/favori/son
listeleri, düzenleme modu, uzun bas menüsü, ülke seçimi, ayarların tüm satırları, abonelik dialogu,
tek kanal oynatma (yavaş), radyo listesi, uyku/ekolayzır arayüzü, kayıt listesi, radyo "çalıyor" şeridi
ve Kapat.

**Çalışmıyor / eksik:** radyonun gerçekten çalması (yavaş ağda), 3'lü ekranda pencere yüklenmesi,
gerçek reklam gösterimi, gerçek abonelik satın alma.

**Çökme:** `logcat -b crash` boş; 17 adımda çökme yok. İmza doğrulaması temiz
(`f3996c67…c92b8d5`, 413 dosya, 18.630 sınıf).
