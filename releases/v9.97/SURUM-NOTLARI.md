# v9.97 — Wi-Fi takılmaları (ilk şikâyet)

Tarih: 2026-11-26 · versionCode 106

## Ne değişti

| # | Değişiklik | Neden |
|---|---|---|
| 1 | **Tune.java** (yeni) — oynatıcı ağ ayarları tek yerde | Zayıf ağda davranışı öngörülebilir yapmak |
| 2 | Soket okuma beklemesi **8 sn → 25 sn**, bağlanma 12 sn | Bir HLS parçası 8 sn'de gelmeyince oynatıcı hata veriyor, 60 sn'lik tampon çöpe gidiyordu |
| 3 | Yükleme hatası **6 kez sessiz tekrar** (kullanıcıya yansımaz) | Kısa kesintiler artık "yeniden bağlanıyorum" ekranına dönüşmüyor |
| 4 | Video tamponu **25 sn dolu / 90 sn ileri**, radyo **30 sn / 180 sn** | Ağ dalgalanmasını yutmak |
| 5 | **PlayerActivity watchdog hatası**: tampon dolarken (BUFFERING) de "ses yok" sanılıp stop+prepare yapılıyordu | Takılmanın en büyük **programsal** sebebi: her 15 sn'de tampon sıfırlanıyordu |
| 6 | Çoklu ekranda 12 sn → **22 sn** eşiği + tampon ilerliyorsa müdahale yok | Aynı sorunun çoklu ekran karşılığı |
| 7 | Çoklu ekran pencereleri **master playlist + ABR** (sabit 720p URL yerine) | Kalite ağa göre düşebiliyor; sesli pencere 720p/2,5 Mbps, sessiz pencere 360p/0,7 Mbps tavanlı |
| 8 | Radyo (AudioService) aynı ağ ayarları | Radyo takılması da azalır |
| 9 | Takılma anında ekranda **“ağ 2,4 Mbps”** + CrashLog kaydı | Ağ mı uygulama mı olduğunu ayırt etmek için |

## Doğrulama

- `bash gradle-build.sh :app:compileDebugJavaWithJavac` → BUILD SUCCESSFUL
- `python tools/verify-apk.py CanliTV.apk --prev releases/v9.96/CanliTV.apk --expect-version 9.97` → ✅ TEMİZ (sınıf +0, dosya +0)
- APK telefona kuruldu: `versionName=9.97 versionCode=106`
- AAB: 6.668.012 bayt

## Doğrulanacak

- Gerçek zayıf Wi-Fi'de 10-15 dk izleme (kullanıcı testi): takılma sayısı önce/sonra.
- Çoklu ekranda 4 pencere + zayıf ağda 720p tavanının yeterli olup olmadığı.
