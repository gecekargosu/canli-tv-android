# UMAY Multi TV 9.64 — sürüm notları

versionCode **73** · versionName **9.64**

## 1. "Az kanal varsa yakında" bitti (M-85)

Eskiden bir ülke "hazır" sayılmak için **en az 30 kanal** gerekiyordu. Bu yüzden 24 kanallı
Birleşik Krallık indirilemiyor, "yakında (30+ gerekli)" görünüyordu.

Artık **kanal sayısı 0'dan büyükse ülke hazırdır**. Sunucudan inen doğrulanmış listede kaç
kanal varsa hepsi kullanılabilir.

Ayrıca indirme hatası ikiye ayrıldı:

- **Sunucuda liste yok (404)** → "yakında · liste henüz yok" (tekrar dokunmak ağa çıkmaz).
- **Ağ hatası / zaman aşımı** → ülke "yakında" işaretlenmez; bağlantı düzelince tekrar denenebilir.
- Sunucuya sonradan liste eklendiğinde eski "yakında" işareti **kendiliğinden silinir**.

## 2. Ülke listeleri büyütüldü: 10 ülke / ~673 kanal → 55 ülke / 3.434 kanal

Her ülke için iptv-org adayları **gerçek oynatma zinciriyle** sınandı: master playlist açılıyorsa
en düşük varyantın segment listesi de kontrol edildi; iki kez düşen kanal listeye alınmadı.
Böylece "indirdim ama açılmıyor" oranı düştü.

| Ülke | Önce | 9.64 |
|---|---|---|
| ABD | 66 | **290** |
| Hindistan | 71 | **233** |
| Romanya | — | **180** |
| Arjantin | — | **156** |
| Türkiye | 145 | 145 (gömülü) |
| Rusya | 53 | **141** |
| İtalya | — | **140** |
| İran | — | **132** |
| Almanya | 73 | **120** |
| Fransa | — | **120** |
| Hollanda | — | **119** |
| Birleşik Krallık | 24 | **115** |
| Endonezya | — | **114** |
| Meksika | 57 | **114** |
| Kanada | — | **102** |
| İspanya | — | **99** |
| Brezilya | 49 | **81** |
| Ukrayna | — | **79** |
| Çin | — | **69** |
| Bulgaristan | — | **66** |
| İsviçre | — | **62** |
| Irak | — | **52** |
| Güney Kore | — | **51** |
| Suudi Arabistan | 53 | 51 |
| Yunanistan | — | **48** |
| Avustralya | — | **46** |
| Avusturya | — | **43** |
| Vietnam | — | **41** |
| Pakistan | — | **40** |
| İsrail | — | **36** |
| Sırbistan | — | **33** |
| Lübnan | — | **28** |
| Bangladeş | — | **24** |
| Mısır | — | **21** |
| Belarus | — | **21** |
| Ürdün | — | **20** |
| BAE | — | **17** |
| Estonya | — | **15** |
| Gürcistan | — | **14** |
| Finlandiya | — | **13** |
| Kazakistan | — | **13** |
| Nijerya | — | **13** |
| Fas | — | **12** |
| Hırvatistan | — | **12** |
| Japonya | — | **12** |
| Arnavutluk | — | **11** |
| İsveç | 85 | 11 (yalnız gerçekten açılanlar) |
| Suriye | — | **11** |
| İrlanda | — | **10** |
| Filipinler | — | **9** |
| Katar | — | **8** |
| Danimarka | — | **7** |
| Cezayir | — | **6** |
| Umman | — | **5** |
| Azerbaycan | — | **3** |

> İsveç'te sayı düştü çünkü eski liste yalnız "master playlist 200 döndü mü" diye bakıyordu;
> yayınların çoğu Türkiye'den coğrafi kilitli. Yeni liste yalnız gerçekten açılanları tutuyor.
> Kanal sayısı az olan ülkelerde **radyo** ayrı kaynaktan (radio-browser) gelir ve çalışır.

Yeni ülkeler: Nijerya, Filipinler, Vietnam, Bangladeş (nüfusu en yüksek ülkeler).

## 3. Ülke sırası (M-86)

Ülke ekranı artık üç kova hâlinde sıralanıyor:

1. **Listesi hazır ülkeler** — Türkiye ilk, sonra kanal sayısı çoktan aza.
2. **İndirilebilen ülkeler** (listesi sunucuda var, cihazda yok) — alfabetik.
3. **"Yakında" olan ülkeler** — alfabetik.

Böylece kullanıcı ilk ekranda çalışan ülkeleri görüyor.

## 4. Kullanıcı bilgilendirmesi (M-87)

Ülke ekranına, kullanıcının kendi dilinde (TR/EN/RU/AR) **"Kanal açılmıyor mu?"** kartı eklendi:

- İnternet yayınlarının reklam kuşağında 10–60 saniye durabileceği,
- Yayın adresi (IP) değişince bağlantının kopabileceği ve uygulamanın yeniden denediği,
- Bazı kanalların tamamen kapanabileceği ve listenin eskidiği,
- Çözüm: 10–20 saniye bekleyip yeniden açmak, başka kanal denemek,
  **Ayarlar → Ölü kanalları onar**,
- Ülke listelerinin sunucudan yenilendiği.

Kısa metin her zaman görünür; karta dokununca ayrıntılı çözüm açılır. Bu, "kanal açılmadı →
program kötü" izlenimini engellemeyi amaçlıyor.

## Kapasite denetimi

| Ölçüm | 9.63 | 9.64 |
|---|---|---|
| APK | 7.115.265 B | 7.119.005 B (+3.740 B) |
| dosya | 400 | 400 |
| sınıf | 17.511 | 17.515 (+4) |
| imza | f3996c67… | f3996c67… |

✅ Kayıp yok.
