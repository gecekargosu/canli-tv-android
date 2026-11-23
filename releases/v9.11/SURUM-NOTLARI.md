# UMAY Multi TV — v9.11 (versionCode 20)

23 Kasım 2026 · `CanliTV-9.11.apk` · `CanliTV-9.11.aab`

## Yeni: "çalışan kanallar önce" (doğrulanmış liste)

Kanal açılmama şikâyetinin kök çözümü. Artık her ülke için **gerçekten açılan yayınlar** listeleniyor:

1. `_build/good.py` ülke listesindeki adayları tek tek dener (HTTP Range + 7 sn zaman aşımı).
2. Açılanlar `site/public/good/<cc>.json` olarak sunucuya konur.
3. Uygulama ülke listesini indirirken bu dosyayı da indirir (`files/good_<cc>.json`).
4. Sıralama: **önce doğrulanmış çalışan kanallar** (sunucu sırasıyla), sonra kalanlar popülerliğe göre.
5. Dosya yoksa davranış eskisi gibi (çevrimdışı güvenli). TR listesi gömülü olduğu için
   `assets/good_tr.json` uygulamanın içinde taşınır.

Doğrulama sonuçları (bu taramada):

| Ülke | Çalışan / denenen |
|---|---|
🇹🇷 TR | **89 / 90** |
🇸🇪 SE | 85 / 90 |
🇮🇳 IN | 75 / 90 |
🇩🇪 DE | 73 / 90 |
🇺🇸 US | 67 / 90 |
🇲🇽 MX | 59 / 90 |
🇷🇺 RU | 56 / 90 |
🇧🇷 BR | 55 / 90 |
🇸🇦 SA | 53 / 55 |
🇬🇧 GB | 28 / 90 *(İngiltere yayınları çoğunlukla adaya kilitli)* |

- TR'de cihaz testi: **1 TRT 1 · 2 TRT 2 · 3 TV8 · 4 ATV · 5 Star TV · 6 Kanal D · 7 Haber Global · 8 NTV · 9 A Haber** — hepsi doğrulanmış.
- Liste her hafta `python _build/good.py us de tr ...` çalıştırılıp site yeniden yayınlanarak tazelenir.

## Teknik
- `Good.java`: JSON'u okur, `(çalışıyor mu, doğrulanmış sıra, popülerlik)` üçlüsüne göre dizer, numaraları yeniden verir.
- `CountryRepo.fetchGood`: ülke indirmesiyle birlikte doğrulanmış listeyi de indirir; `clear` ikisini de siler.
- Sunucu: `https://umay-multi-tv-gq3zer.freebuff.site/good/<cc>.json` (statik, Cloudflare).
