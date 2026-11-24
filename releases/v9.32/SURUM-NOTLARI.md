# UMAY Multi TV v9.32 (versionCode 41)

Tarih: 24 Kasım 2026

## 1. İnternet sayacı düzeltildi (#155 — öncelikli)
- Sayaç artık **oynatıcının ağdan okuduğu baytları** sayıyor (yeni `CountingDataSource`).
  Eskiden `TrafficStats` kullanılıyordu; cihazda 0 dönebiliyor, oynatma sürerken ekran güncellenmiyordu.
- Ekran (oynatıcı altı), ana ekran ve **Ayarlar aynı sayıyı** gösteriyor. Reklam şeridindeki
  "Bugün kullanılan veri" de aynı biçimde (ondalık virgül) yazıyor.
- Ölçüm kanıtı: TRT Haber 360p 1 dakika → toplam **10,6 MB**; oynatıcı, ana ekran ve Ayarlar üçü de 10,6 MB.
- Sayaç 2 MB'da bir diske yazılıyor; çökme/çıkışta veri kaybolmuyor, kanal değişiminde eski kanala yazılıyor.
- Not: sayaç yalnız yayın verisini ölçer; reklam trafiği ve arka plandaki ses servisi hariç.

## 2. Ülkeye göre tanıtım metni (#156)
- "Program tanıtımı ve özellikleri" artık seçili ülkeye göre yazıyor.
- Türkiye'de TRT/ATV/Star listesi anlatılıyor; başka ülkede "şu an Almanya listesini görüyorsun,
  Türk kanalları görünmez, Ayarlar → Ülke" açıklaması çıkıyor. Ayrı "Ülkeye göre içerik" bölümü var.

## 3. Ülke listesi yoksa bilgilendirme (#148)
- Boş liste yerine: "🇩🇪 Almanya listesi henüz hazır değil… Dokun → tekrar indirmeyi dene".
- Dokununca liste yeniden indirilir, gelince ekran kendini yeniler.

## 4. Ekonomi/arka plan sesi test edildi (#149)
- Yayın açıkken ana ekrana dönüldüğünde akış sürüyor: 25 saniyede 1.671 KB indirme ölçüldü,
  ses odağı uygulamada kaldı. OEM "arka plan sesi kısılabilir" uyarısı verse de akış kesilmiyor.

## Doğrulama
- 145 kanal, TRT Haber oynadı; TR'ye geri dönüldü.
- Sayaç üç ekranda aynı: 10,6 MB. Ağ tarafı ölçüm: 1 dk 360p ≈ 10,6 MB (beklenen ~0,6 Mbps).
- Tanıtım ekranı Türkiye ve Almanya metinleriyle açıldı.
