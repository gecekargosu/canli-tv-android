# UMAY Multi TV 10.10 (119) — Sürüm Notları

Tarih: 28 Kasım 2026 · Play: **alpha (kapalı test)**, versionCode 119

Bu sürüm **yeni özellik getirmez**; 10.09'da kalan teknik borcu kapatır. Kullanıcıya
görünen davranış aynı kalır (bilerek).

## Ne değişti

### 1. `MainActivity` bölündü: 1272 → 1128 satır
- **`ChannelFilter`** (yeni): "hangi kanallar, hangi sırada" hesabı — sekme filtresi,
  arama, gizlenenler, ölü kanal eleme, kayıtlı kullanıcı sırası ve "🕘 Son" sıralaması.
  Arayüzden bağımsız, saf mantık.
- **`ChannelEdit`** (yeni): düzenleme modu — seçim, tek satır/çoklu taşıma, en üste/en alta,
  silme, sıra kaydı ve "arama varken sıralama yapılmaz" kuralı. Arayüze `Host` ile bağlanır.
- `MainActivity` artık yalnız görünümü kuruyor ve `ChannelEdit.Host`'u uyguluyor.

### 2. YEDEK kopyalarındaki düz yazı şifre temizlendi
`YEDEK/` altındaki 30 dosyada keystore şifresi `*** (keystore.properties) ***` ile
maskelendi (36 satır). Ana `keystore.properties` dosyası **değişmedi**, imzalama çalışıyor.
Not: yedek kopyalardaki `keystore.properties` dosyaları da maskelendiği için o kopyalar
doğrudan imzalı derleme üretemez; şifre ana dosyadan alınır.

## Doğrulama

- `bash build.sh` → BUILD SUCCESSFUL, imza `f3996c67…b8d5`.
- `verify-apk.py --prev releases/v10.09 --expect-version 10.10` → ✅ TEMİZ
  (4.476.343 bayt, 429 dosya, 7866 sınıf; 10.09'a göre **+0 sınıf**, +288 bayt).
- Telefon (Z5R87DU8V4LBSOVG): kuruldu, açıldı, **127 kanal** listelendi, FATAL/ANR yok.
- Düzenleme modu cihazda denendi: `Düzen` → satıra dokun → "1 kanal seçili" →
  `▼` ile kanal bir sıra aşağı taşındı (liste sırası değişti) → `Bitti` ile moddan çıkıldı.
- `tools/check-all.sh --skip-build` → `SONUC: TEMIZ (10.10)`.

## Bilinen sınırlar

- Çoklu seçim + silme (🗑) ve "en üste al" düğmeleri bu turda cihazda denenmedi;
  kod 10.09'daki davranışın birebir taşınmış hâli.
- Play'de "İncelemeye gönder" adımı Console'dan yapılır.
