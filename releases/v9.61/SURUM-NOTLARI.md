# UMAY Multi TV 9.61 — sürüm notları

versionCode **70** · versionName **9.61**

## 1. Çoklu ekran artık seçilen kalitede oynuyor (M-81)

Çoklu ekran `MediaPlayer` kullanıyor; burada ExoPlayer'ın track seçimi yok. Master playlist
verildiğinde ABR **en yüksek kaliteyi (1080p)** çekebiliyor, veri hızla gidiyordu.

Düzeltme: çoklu ekran için `Quality.resolveForPlayer` — tercih edilen **varyant** (varsayılan
360p) doğrudan seçiliyor. Ölçüm: 3 pencere ≈ **650 kbps/kanal** (17,5 MB / 72 sn),
yani 360p seviyesi.

## 2. Çoklu ekranda veri sayacı hiç artmıyordu (M-81b)

Ekranda “Bugün kullanılan veri” çoklu ekranda **sabit** kalıyordu (588,0 → 588,0).
Baytlar yalnızca ExoPlayer tarafında sayılıyordu. Artık `TrafficStats` farkı her 3 saniyede
günlük sayaca ekleniyor; çıkarken kalan miktar da yazılıyor.
Doğrulama: 643,5 → 661,0 MB (72 sn, 3 kanal).

## 3. “Ne var, ne yok?” ekranı güncellendi (M-82)

Ayarlar → Hakkında → **Ne var, ne yok?** listesi güncel ürünle eşlendi (25 satır):
çoklu ekran ve ağ toparlanması, radyo kapatma şeridi, A/V senkron + 360p, çoklu ekranda
kalite, veri sayacı/limit, mobil veri onayı, sürüm bilgisi, Play durumu vb.
Olmayanların nedeni de yazılı (altyazı çevirisi, EPG, Chromecast, zamanlanmış kayıt,
yerel kanallar, tablet düzeni).

## 4. Gerçek ağ testi (mobil veri KAPALI)

Önceki testte Wi-Fi kapanınca telefon hücresel veriye geçtiği için sonuç yanıltıcıydı.
Bu kez mobil veri kapalı (`mobile_data=0`, `ping` → *Network is unreachable*) doğrulandı:

| An | Ekran |
|---|---|
| 30 sn kesinti | “📶 Bağlantı sorunu · yeniden bağlanıyorum (1/3)” |
| 60 sn kesinti | “⏳ Yayın durakladı · yeniden bağlanıyor (1/2)” + “Yayın açılmadı…” |
| Ağ dönüşü + 30 sn | ✅ yayın kendiliğinden canlıya döndü (577,5 → 584,5 MB) |

## Kalite teyidi

- Varsayılan **360p** (`Prefs.maxHeight = 360`), ekonomi modu = en düşük.
- Tek oynatıcı: master playlist + `setMaxVideoSize(∞, 360)` → üstte “360p” yazıyor.
- Çoklu ekran: 360p varyantı (yukarıdaki ölçüm).
- İstisna: kullanıcı kanala özel kalite seçtiyse o kullanılır.

## Kapasite denetimi

| Ölçüm | 9.59 | 9.61 |
|---|---|---|
| APK | 7.113.493 B | 7.114.193 B (+700 B) |
| dosya | 400 | 400 |
| sınıf | 17.508 | 17.509 (+1) |
| imza | f3996c67… | f3996c67… |

✅ Kayıp yok.
