# UMAY Multi TV 9.76

**versionCode 85 · versionName 9.76**

Bu sürüm, 9.75'ten sonra bildirilen ana ekran tasarımı ve "kanallar eksik" sorununu kapatır.

## 1. Üst kısım kompaktlaştırıldı

- Önce: logo + başlık satırı, ayrı düğme satırı, arama satırı, sekme satırı, araç satırı
  → liste ekranın ~%37'sinden başlıyordu ("ekranın 1/3'ü buton").
- Şimdi: logo + başlık + düğmeler **tek satır**; arama + No + Git tek satır; dört sekme
  tek satır; Düzenle/Sırala + "128 kanal" tek satır.
- Liste artık ekranın ~%27'sinden başlıyor, ilk ekranda 8 kanal görünüyor.
- Düğmeler 48dp → 44dp (hâlâ rahat dokunulur), yazı 11sp; sağda boş alan kalmıyor.

## 2. Kanal sekmeleri sadeleşti

- Yalnız dört sekme: **Tümü · ★ Favoriler · 🕘 Son · 🗑 Gizlenenler**.
- Sekmeler ekran genişliğine eşit dağılır; sağa kaydırma yok.
- Kategori sekmeleri (Haber, Spor, Belgesel…) kaldırıldı.

## 3. Veri bilgisi tek yerde

- Üstteki "Bugün: … · limit yok · 360p" satırı kaldırıldı.
- Listenin altındaki sabit şerit artık tüm ayrıntıyı yazıyor:
  `📶 Bugün kullanılan veri: 268,0 MB · limit yok · 🎚 360p`
- Reklamın üstünde tek satır; kaydırınca kaybolmuyor.

## 4. Oynatıcı üst barı

- Düğmeler 62×50 → 68×62 dp, yazı 9 → 10sp, ikon büyütme 1,6× → 2,2×.
- "Bağlantı koptu / reklam aralığı" bilgi şeridi barın üstüne binmiyor (132dp altına alındı).

## 5. Kanallar eksik görünüyordu (kök neden)

- Cihazda liste **128 yerine 32 kanal** gösteriyordu; 1 numara boştu.
- İki ayrı neden bulundu:
  1. Eski sürümlerden kalan toplu "listeden sil" kaydı (`hiddenCh`, 115 URL).
  2. "Ölü kanal" sayacının süresi hiç dolmuyordu; iki kez tökezleyen kanal kalıcı gizleniyordu.
- Düzeltme: 20'den fazla gizli kayıt kaza sayılıp bir kez temizleniyor; ölü sayacı
  yalnız son 3 günü sayıyor ve doğrulanmış liste her tazelendiğinde sıfırlanıyor.
- Cihazda doğrulandı: **128 kanal**, numaralar 1'den başlıyor.

## 6. Kanal listesi (değişmedi, hatırlatma)

**1 Test Alfa · 2 ATV · 3 TV8 · 4 Show TV · 5 Star TV · 6 Kanal D · 7 TRT Haber · 8 TRT 1 …**

## Kapasite denetimi

```
✅ Kapasite denetimi TEMİZ  (9.75 → 9.76)
dosya 400 | sınıf 17.530 | imza f3996c67…b8d5 | 9.76 (85)
```

## Bilinen açık işler

- **ATV**: kullanıcı tarafından sonraya bırakıldı; adres değişmedi.
- Play: kapalı test → 14 gün → üretim başvurusu (kullanıcıda).
