# UMAY Multi TV 9.96 (sürüm kodu 105)

## Düzeltme: "🗑 Gizlenenler" sekmesi her zaman boştu

Kanal silindiğinde uygulama "🗑 Gizlenenler'den geri alabilirsin" diyordu, ancak o sekme
**hiçbir zaman kanal göstermiyordu**; yani listeden silinen kanal geri getirilemiyordu.

**Neden:** `MainActivity.refresh()` gizli sekmede de kategori filtresi uyguluyordu.
`group` değeri `🗑 Gizlenenler` olduğunda kod son `else` dalına düşüp `c.group.equals(group)`
karşılaştırması yapıyor, hiçbir kanalın kategorisi "🗑 Gizlenenler" olmadığı için liste boş kalıyordu.

**Çözüm:** Kategori filtresi gizli sekmede uygulanmaz.

```java
// 9.96: "Gizlenenler" sekmesi bir kategori adi degil; ...
if (!"Tümü".equals(group) && !onlyHidden) {
```

| Dosya | Değişiklik |
|---|---|
| `src/com/cengiz/canlitv/MainActivity.java` | `refresh()` kategori filtresi `!onlyHidden` ile korundu |
| `app/build.gradle` | `versionCode 105`, `versionName '9.96'` |

## Doğrulama

- `tools/verify-apk.py CanliTV.apk --prev releases/v9.95/CanliTV.apk --expect-version 9.96`
  → **Kapasite denetimi TEMİZ** (sınıf +0, dosya +0, imza aynı, boyut +40 bayt).
- Cihazda (MIUI `Z5R87DU8V4LBSOVG`) test: ATV ve TV8 listede seçilip silindi → 🗑 Gizlenenler
  sekmesinde ikisi de göründü → 🗑 düğmesiyle "2 kanal listeye geri eklendi" → liste yeniden 120 kanal.
- Aynı test 9.95'te (104) liste boş kalıyordu; hata saha öncesi yakalandı.

## Not

Gizli sekmedeki kanallar uzun basıldığında menüde "➕ Listeye geri ekle" seçeneği de çıkar;
iki yol da artık çalışıyor.
