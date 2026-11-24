# UMAY Multi TV 9.62 — sürüm notları

versionCode **71** · versionName **9.62**

## Ülke seçiminde “indir” hiçbir şey yapmıyordu (M-83)

**Belirti:** Ülke seç → ör. Suriye/Yunanistan → “indir” yazısına dokununca hiçbir şey
olmuyor gibiydi; “yakında” etiketi hiç görünmüyordu.

**Kök neden:** Ülke listeleri `https://umay-multi-tv-gq3zer.freebuff.site/good/<ülke>.json`
adresinden indiriliyor. Sunucuda yalnız hazır ülkeler var (br, de, gb, in, mx…). Olmayan
ülke **404** dönüyor, uygulama bunu “indirilemedi” diye yutuyor ve satır değişmiyordu.
Yani kod doğruydu; **veri yoktu** ve kullanıcıya sebep söylenmiyordu.

**Düzeltme:**

- Liste olmayan ülke bir kez denenir, “liste yok” olarak işaretlenir ve satırda
  **“yakında · liste henüz yok”** yazar.
- Tekrar dokunmak ağa çıkmaz (veri harcamaz), kısa bir uyarı gösterir.
- Uzun basıp “yeniden indir” ile ileride liste eklenince güncellenebilir.

**Cihazda doğrulandı (9.62):** Yunanistan ve Suriye satırları “yakında · liste henüz yok”
oldu; sunucuda listesi olan ülkeler “indir” olarak duruyor (`_build/cs3.jpg`).

## Kapasite denetimi

| Ölçüm | 9.61 | 9.62 |
|---|---|---|
| APK | 7.114.193 B | 7.114.557 B (+364 B) |
| dosya | 400 | 400 |
| sınıf | 17.509 | 17.509 |
| imza | f3996c67… | f3996c67… |

✅ Kayıp yok.
