# UMAY Multi TV 9.63 — sürüm notları

versionCode **72** · versionName **9.63**

## Ülke ekranı: hangi ülkede liste var, hangisinde yok — tek bakışta (M-84)

**Durum:** Uygulamada **51 ülke** listeleniyor. Sunucuda kanal listesi olan ülke sayısı
**10**: `br, de, gb, in, mx, ru, sa, se, tr, us`. Yani **41 ülkede TV listesi yok**.

Önceden bu 41 ülke de “indir” yazıyordu; kullanıcı tek tek dokunup “indirilemedi” alıyordu.
Artık ülke ekranı açılınca uygulama sunucudan **`good/index.json`** dosyasını bir kez çekiyor
(~100 bayt) ve listede olmayan ülkeleri kendiliğinden **“yakında · liste henüz yok”** olarak
işaretliyor.

- Dokunma: ağa çıkmaz, kısa uyarı verir.
- Uzun basma: yeniden indirme denemesi (ileride liste eklenirse güncellenir).
- Listesi olan ülkeler “indir” olarak kalır, indirilince “N kanal · hazır” olur.

**Cihazda doğrulandı (9.63):** Azerbaycan, Avusturya, BAE, Arjantin, Ermenistan, Avustralya
otomatik “yakında”; Almanya ve ABD “indir”; Türkiye “145 kanal · hazır ✓”.

## Kapasite denetimi

| Ölçüm | 9.62 | 9.63 |
|---|---|---|
| APK | 7.114.557 B | 7.115.265 B (+708 B) |
| dosya | 400 | 400 |
| sınıf | 17.509 | 17.511 (+2) |
| imza | f3996c67… | f3996c67… |

✅ Kayıp yok.
