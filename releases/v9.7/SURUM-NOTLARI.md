# UMAY Multi TV — v9.7 (versionCode 16)

23 Kasım 2026 · `CanliTV-9.7.apk` · `CanliTV-9.7.aab`

## Düzeltmeler

### 📢 Üst üste iki reklam barı (ekran görüntüsündeki hata)
- Radyo listesi kendi banner'ını ekliyordu, sonra `AdManager.wrap` bir tane daha ekliyordu → **iki bar**.
- Radyo listesindeki eski banner kaldırıldı. Artık **tek** banner var (ana sayfa, radyo listesi, radyo çalar,
  ayarlar, hesap, ülke, kayıtlar, durum, çoklu ekran — hepsinde birer tane).

### ⭐ Liste artık "önce bilinen kanallar" sırasında
- ABD listesi rastgele ABC yerel kanallarıyla başlıyordu. Yeni sıralama mantığı: her ülke için tanınmış
  markalar listesi (TRT 1, ATV, Star, Show, Now, TV8, Kanal D / ABC, CBS, NBC, FOX, CNN, ESPN, HBO…)
  ve **her markadan yalnızca bir kanal** başa alınır; yerel tekrarlar arkada kalır.
- Sonrasında listenin kendi sırası gelir (bilinmeyen kanallar sonda).
- Numaralar 1..n olarak yeniden verilir; kullanıcı numara değişiklikleri korunur.
- ABD testi: 1. ABC · 2. CBS · 3. NBC · 4. FOX · 5. CNN · 6. ESPN · 7. HBO · 8. PBS.

### 🚫 Açılmayan kanal artık listede kalmıyor
- Bir kanal **2 kez** açılmazsa "ölü" sayılıp listeden gizlenir (varsayılan açık; Ayarlar → Ölü gizle).
- Kanal açılmazsa uygulama **2,2 saniye sonra otomatik sonraki kanala geçer** (üst üste en fazla 3 kez,
  tüm liste ölüyse döngüye girmez).
- Yayın açılınca o kanalın hata sayacı sıfırlanır.

## Test
| Test | Sonuç |
|---|---|
Çift banner | ✅ tek bar (radyo listesi ekran görüntüsü) |
ABD sıralaması | ✅ ABC, CBS, NBC, FOX, CNN, ESPN, HBO, PBS |
TR sıralaması | ✅ TRT 1, ATV, Star, Show, TV8, Kanal D… |
Derleme + kurulum | ✅ |
