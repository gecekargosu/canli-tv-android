# UMAY Multi TV v9.25 (versionCode 34)

Tarih: 23 Kasım 2026

## Bu sürümde düzeltilenler

### 1. Izgarada boş kalan pencere (ATV ortada açılmıyordu) — kök neden
`MultiActivity` içindeki üç pencere **tek bir `reloadGen` sayacını** paylaşıyordu.
Bir pencere hazırlanınca sayaç artıyor, diğer pencerelerin `Quality.resolve` callback'i
"eski istek" sayılıp sessizce iptal ediliyordu. Sonuç: ortadaki pencere (ATV) hiç
veri kaynağı almıyor, hata da göstermediği için sonsuza kadar siyah kalıyordu.

- Her pencereye **kendi nesil sayacı** verildi.
- 3 saniyede bir çalışan **sağlık denetimi** eklendi:
  - 15 sn içinde hazırlanamayan yayın → yedek kaynak / taze playlist
  - hazır görünüp 10 sn içinde ilk kareyi vermeyen yayın (siyah ekran) → yeniden başlat
  - oynarken konumu 12 sn ilerlemeyen (donan) yayın → taze playlist ile canlı kenara dön
- Hata durumunda sıra: asıl yayın → yedek kaynaklar → taze playlist (toplam 8 deneme).

### 2. TV8 reklam kuşağında takılma
TV8'in "DİJİTAL REKLAM KUŞAĞINDASINIZ" karesi **yayıncının kendi** dijital reklam
aralığı (Daion SSAI). Uygulama bu reklamı atlayamaz; ancak artık:
- 12 sn ilerlemeyen yayın otomatik yeniden başlatılıyor,
- hata olursa TV8'in TürkNet/ERC yansısına geçiliyor,
- tüm oynatıcılar tarayıcı UA'sı ile istek atıyor (bazı CDN'ler aksi hâlde reklam klibi döndürüyor).
Cihazda doğrulandı: reklam aralığı bitince TV8 kendiliğinden Zuhal Topal yayınına döndü.

### 3. Reklam kuralı (kullanıcı isteği)
- Kanal başına ayrı "20 dk reklamsız" **kaldırıldı**.
- Reklamsız süre artık **uygulama geneli 15 dakika** (ödüllü video ile).
- İzlerken **15 dakikada bir** geçiş reklamı.
- **Her kanal değişiminde** geçiş reklamı (art arda zap yapılırsa 20 sn alt sınır).
- Oynatıcı ve liste altındaki çift "▶ Reklam izle" şeridi kaldırıldı → tek banner, her zaman görünür.

### 4. #101 — Doğrulanmış kanal listesi günlük tazeleme
Açılışta günde 1 kez `good/<ülke>.json` sunucudan indirilir; bozuk/boş dosya gömülü listeyi ezmez.

### 5. #102 — TV için kendi M3U/URL ekleme
Ayarlar → Kanal listesi:
- **Kendi kanalımı ekle**: ad + m3u8 adresi (listenin en başında görünür)
- **M3U dosyasından ekle**: telefondan .m3u seç, tüm kanallar içe aktarılır
- **Kendi kanallarım**: eklenenleri gör / kaldır

### 6. #100 — Telif / içerik bildirimi
Ayarlar → Hakkında → **Telif / içerik bildir**: e-posta adresi, kopyalama ve
24-48 saat içinde kaldırma taahhüdü. Adres: cengizkilic1980@gmail.com

### 7. #103 — Yedek kaynaklar genişletildi
ATV'ye ek olarak TV8, Show TV, Kanal D ve NOW için TürkNet/ERC yansıları eklendi.

## Cihaz doğrulaması (Xiaomi, Android 13)
- 3'lü ızgara: üç pencere de oynuyor (TV8 + ATV + üçüncü kanal), boş pencere yok.
- TV8 tekli oynatıcı: reklam aralığı sonrası yayına döndü.
- Ayarlar: Telif satırı, Kendi kanalımı ekle / M3U / Kendi kanallarım satırları, sürüm 9.25.
- Banner tek ve görünür; "Reklam izle" şeridi yok.
- `logcat`: FATAL / AndroidRuntime hatası yok.

## Bilinen sınırlar
- TV8 dijital reklam aralığı yayıncı tarafındadır; uygulama atlayamaz (sadece toparlanır).
- Reklam doldurulmazsa (no-fill) geçiş reklamı gösterilmez, yayın normal açılır.
- ExoPlayer (Media3) geçişi (#39) hâlâ sırada.
